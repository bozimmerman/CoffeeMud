package com.planet_ink.miniweb.http;

import java.io.File;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.miniweb.http.HTTPMethod;
import com.planet_ink.miniweb.interfaces.DataBuffers;
import com.planet_ink.miniweb.interfaces.FileCacheManager;
import com.planet_ink.miniweb.interfaces.FileManager;
import com.planet_ink.miniweb.interfaces.HTTPFileGetter;
import com.planet_ink.miniweb.interfaces.HTTPIOHandler;
import com.planet_ink.miniweb.interfaces.HTTPOutputConverter;
import com.planet_ink.miniweb.interfaces.HTTPRequest;
import com.planet_ink.miniweb.interfaces.SimpleServlet;
import com.planet_ink.miniweb.interfaces.SimpleServletRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletSession;
import com.planet_ink.miniweb.util.MWDataBuffers;
import com.planet_ink.miniweb.util.MiniWebConfig;
import com.planet_ink.miniweb.util.MWRequestStats;

/*
Copyright 2012-2013 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * Processes a completed http request and generates the output.
 * Designed at the moment for a synchronous process.  
 * To go async, the first problem to solve is guarenteeing that
 * reads continue, but writes happen sequentially.  Perhaps
 * this is done with a queue of some sort?  A runnable set
 * to kick off on another thread when the first request is ready...?
 * 
 * Either way, this class acts mostly as a singleton which is handed
 * a fully formed and finished request to process.  When generateOutput
 * is called, the ByteBuffer containing the entire output is returned.
 * As such, this function fusses over every aspect of a request that
 * the web server supports, such as files, servlets, head requests, etc. 
 * @author Bo Zimmerman
 *
 */
public class HTTPReqProcessor implements HTTPFileGetter
{
	private final MiniWebConfig			config; // the mini web configuration
	private final static String			EOLN	= HTTPIOHandler.EOLN;
	
	public HTTPReqProcessor(MiniWebConfig config)
	{
		this.config=config;
	}

	/**
	 * If the request being processed accepts come sort of content encoding,
	 * then this function will discover if one of the encodings we support
	 * is among them and, if so, will encode the data as appropriate and
	 * return the encoded buffer, or the old buffer if nothing was done to it.
	 * If the buffer is encoded, the appropriate content header is added
	 * to the given headers map, otherwise, nothing is done to that either.
	 * @param request the request being processed
	 * @param file the file being outputted
	 * @param buffers the fully formed output buffer
	 * @param headers set of extra headers to potentially add to
	 * @return a new buffer if changed, or null if not changed
	 * @throws HTTPException
	 */
	private DataBuffers handleEncodingRequest(HTTPRequest request, final File file, final DataBuffers buffers, Map<HTTPHeader,String> headers) throws HTTPException
	{
		if(buffers.getLength()==0) 
			return null;
		double deflatePreference = request.getSpecialEncodingAcceptability("deflate");
		if(deflatePreference==0.0) 
			deflatePreference = request.getSpecialEncodingAcceptability("x-deflate");
		
		double gzipPreference = request.getSpecialEncodingAcceptability("gzip");
		if(gzipPreference==0.0) 
			gzipPreference = request.getSpecialEncodingAcceptability("x-gzip");
		
		double nonzipPreference = request.getSpecialEncodingAcceptability("*");
		
		if((deflatePreference==0.0) && (gzipPreference==0.0))
		{
			// assume servlet or someone else will encode it properly
			return buffers;
		}
		if((nonzipPreference > deflatePreference) && (nonzipPreference > gzipPreference))
		{
			return buffers;
		}
		
		String compressorName;
		DataBuffers compressedBytes;
		if((deflatePreference>0.0)&&(deflatePreference>gzipPreference))
		{
			// deflate between ie & everyone else appears to be a problem
			// so, all else being equal, prefer gzip below
			compressorName="deflate";
			compressedBytes=config.getFileCache().compressFileData(file, FileCacheManager.CompressionType.DEFLATE, buffers);
		}
		else
		{
			compressorName="gzip";
			compressedBytes=config.getFileCache().compressFileData(file, FileCacheManager.CompressionType.GZIP, buffers);
		}
		if(compressedBytes != buffers)
			headers.put(HTTPHeader.CONTENT_ENCODING,compressorName);
		return compressedBytes;
	}
	
	
	/**
	 * Check a given range request data to see if it is within the acceptable
	 * limits.  Returns a final range first,last if successful, and throws
	 * an exception otherwise
	 * @param rangeAZ the range being checked
	 * @param limit the size of the source buffer
	 * @return a first,last 
	 * @throws HTTPException
	 */
	private int[] checkRangeRequest(int[] rangeAZ, int limit) throws HTTPException
	{
		final int firstByte=rangeAZ[0];
		if((firstByte<0) || (firstByte>=limit))
			throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
		int lastByte=limit;
		if(rangeAZ.length==2)
		{
			lastByte=rangeAZ[1]+1;
			if((lastByte<firstByte)|| (lastByte>limit))
				throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
		}
		return new int[]{firstByte,lastByte};
	}

	/**
	 * If the request being processed is a range request, this method will
	 * take the old output buffer and return a new one with the new range.
	 * If it does nothing at all, it will simply return null;
	 * If there is a range parsing error, an http exception is thrown
	 * @param request the request being processed
	 * @param buffers the fully formed output buffer
	 * @return true if a range was set, false otherwise
	 * @throws HTTPException
	 */
	private boolean setRangeRequests(HTTPRequest request, final DataBuffers buffers) throws HTTPException
	{
		final List<int[]> rangeXYSets = request.getRangeAZ();
		if(rangeXYSets!=null)
		{
			List<int[]> ranges=new LinkedList<int[]>();
			for(int[] range : rangeXYSets)
				ranges.add(checkRangeRequest(range,buffers.getLength()));
			buffers.setRanges(ranges);
			return true;
		}
		return false;
	}
	
	/**
	 * Internal method to generate a standard set of headers for a standard request.
	 * @param request the request being processed
	 * @param status the final http status being returned
	 * @param headers set of extra headers to return
	 * @param response the body of the response
	 * @return ByteBuffer containing the entire header part of the response plus the response
	 * @throws HTTPException
	 */
	private ByteBuffer generateStandardHeaderResponse(HTTPRequest request, HTTPStatus status, Map<HTTPHeader,String> headers, DataBuffers response) throws HTTPException
	{
		StringBuilder str=new StringBuilder("");
		str.append("HTTP/").append(request.getHttpVer()).append(" ").append(status.getStatusCode()).append(" ").append(status.description());
		str.append(EOLN);
		for(HTTPHeader header : headers.keySet())
			str.append(header.makeLine(headers.get(header)));
		if(response != null)
			str.append(HTTPHeader.CONTENT_LENGTH.makeLine(response.getLength()));
		else
			str.append(HTTPHeader.CONTENT_LENGTH.makeLine(0));
		if(response != null)
			str.append(HTTPHeader.LAST_MODIFIED.makeLine(HTTPIOHandler.DATE_FORMAT.format(response.getLastModified())));
		if(config.isDebugging())
			config.getLogger().fine("Response: "+str.toString().replace('\r', ' ').replace('\n', ' '));
		str.append(HTTPIOHandler.SERVER_HEADER);
		str.append(HTTPIOHandler.CONN_HEADER);
		str.append(HTTPHeader.getKeepAliveHeader());
		str.append(HTTPHeader.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(new Date(System.currentTimeMillis()))));
		if((status == HTTPStatus.S206_PARTIAL_CONTENT)&&(response!=null))
			str.append(HTTPHeader.CONTENT_RANGE.makeLine(request.getHeader(HTTPHeader.RANGE.lowerCaseName())+"/"+response.getLength()));
		str.append(HTTPIOHandler.RANGE_HEADER);
		str.append(EOLN);
		return ByteBuffer.wrap(str.toString().getBytes());
	}
	
	/**
	 * Internal method to generate a full standard response to a standard request.
	 * The returned buffer will include all the headers and everything needed to send straight out to
	 * the client.
	 * @param request the request being processed
	 * @param status the final http status being returned
	 * @param headers set of extra headers to return
	 * @param response the body of the response
	 * @return data buffers containing the entire response, including headers
	 * @throws HTTPException
	 */
	private DataBuffers generateStandardResponse(HTTPRequest request, HTTPStatus status, Map<HTTPHeader,String> headers, DataBuffers response) throws HTTPException
	{
		ByteBuffer header=generateStandardHeaderResponse(request, status, headers, response);
		if((response != null) && (response.getLength() > 0))
		{
			response.insertTop(header, 0);
			return response;
		}
		if(response!=null)
			return new MWDataBuffers(header, response.getLastModified().getTime());
		return new MWDataBuffers(header, System.currentTimeMillis());
	}

	/**
	 * Examines the given mime type to see if it conforms to acceptable
	 * client headers in the request.  This method will do nothing, or
	 * throw an exception if it does not conform.
	 * @param request the request being processed
	 * @param mimeType the mimetype of the response generated
	 * @throws HTTPException
	 */
	private void confirmMimeType(HTTPRequest request, final MIMEType mimeType) throws HTTPException
	{
		String mimeMaskStr = request.getHeader(HTTPHeader.ACCEPT.lowerCaseName());
		if(mimeMaskStr!=null)
		{
			String[] mimeMasks = mimeMaskStr.split(",");
			boolean matchedOne=false;
			for(String mimeMask : mimeMasks)
			{
				if(mimeType.matches(mimeMask))
				{
					matchedOne=true;
					break;
				}
			}
			if(!matchedOne)
				throw HTTPException.standardException(HTTPStatus.S406_NOT_ACCEPTABLE);
		}
	}
	
	/**
	 * After a uri has been broken apart and inspected, this method is called
	 * to reassemble it into a valid File path using local file separators.
	 * If you wish to add a special directory root for html docs, this would
	 * be the appropriate place to do it.
	 * @param request the request being processed
	 * @return the full assembled file
	 */
	public File assembleFileRequest(HTTPRequest request)
	{
		final String[] url = request.getUrlPath().split("/");
		StringBuilder fullPath = new StringBuilder("/");
		if(url.length > 1)
		{
			List<String> fixedUrl=new ArrayList<String>(url.length-1);
			for(int i=1;i<url.length;i++)
				if(url[i].equals(".."))
				{
					if(fixedUrl.size()>0)
						fixedUrl.remove(fixedUrl.size()-1);
				}
				else
				if((!url[i].equals("."))&&(url[i].length()>0))
					fixedUrl.add(url[i]);
			if(fixedUrl.size()>0)
			{
				fullPath.append(fixedUrl.get(0));
				for(int i=1;i<fixedUrl.size();i++)
					fullPath.append('/').append(fixedUrl.get(i));
			}
		}
		final String fullPathStr=fullPath.toString();
		String host=request.getHost();
		int x=host.indexOf(':');
		if(x>0) host=host.substring(0, x); // we only care about the host, we KNOW the port.
		final Pair<String,String> newPath=config.getMount(host,request.getClientPort(),fullPathStr);
		File finalFile;
		FileManager mgr=config.getFileManager();
		if(newPath == null)
			finalFile = mgr.createFileFromPath(fullPathStr.replace('/', mgr.getFileSeparator()));
		else
			finalFile=mgr.createFileFromPath((newPath.second+fullPathStr.substring(newPath.first.length())).replace('/', mgr.getFileSeparator())); // subtract one for the /
		// see if the path we have is complete, or if there's an implicit default page requested.
		if(request.getUrlPath().endsWith("/"))
			finalFile=mgr.createFileFromPath(finalFile,config.getDefaultPage());
		return finalFile;
	}
	
	/**
	 * If the request contained an eTag marker in an if-none-match request, this will
	 * return that marker in a one-dimentional string array.  If not, such an array
	 * will be returned anyway, but with a null entry that can be modified when the
	 * file is actually read off the hard drive.
	 * @param request the request being processed
	 * @return a one-dimensional (modifiable) string
	 */
	private String[] generateETagMarker(HTTPRequest request)
	{
		String possibleETag=request.getHeader(HTTPHeader.IF_NONE_MATCH.lowerCaseName());
		if(possibleETag != null)
		{
			possibleETag=possibleETag.trim();
			if(possibleETag.startsWith("\"") && possibleETag.endsWith("\""))
				possibleETag=possibleETag.substring(1, possibleETag.length()-1);
			return new String[]{possibleETag};
		}
		return new String[1];
	}

	/**
	 * Find a session associated with this request, prepped into the response if found.
	 * @param request the request being processed
	 * @param servletResponse the response to prep, if its found.
	 * @return the session found or created
	 */
	private SimpleServletSession getServletSession(HTTPRequest request, MWServletResponse servletResponse)
	{
		SimpleServletSession session;
		String oldSessionID=request.getCookie("mwsessid");
		if(oldSessionID == null)
		{
			session = config.getSessions().createSession(request);
			servletResponse.setCookie("mwsessid", session.getSessionId());
		}
		else
		{
			session = config.getSessions().findOrCreateSession(oldSessionID);
		}
		return session;
	}
	
	/**
	 * if the request url root context matched one of the servlets from the servlet manager, then
	 * this method will actually call the servlet and return its output buffer, while also
	 * handling any stats adjustments.
	 * 
	 * @param request the request being processed
	 * @param servletClass the servlet class to execute
	 * @return the output from the servlet
	 * @throws HTTPException
	 */
	private DataBuffers executeServlet(HTTPRequest request, final Class<? extends SimpleServlet> servletClass) throws HTTPException
	{
		// servlet found -- full stream ahead </pun>
		MWServletResponse servletResponse = new MWServletResponse(); // generate a response object
		SimpleServletSession session=getServletSession(request, servletResponse); // get or create a session object
		SimpleServletRequest servletRequest = new MWServletRequest(session, request);
		try
		{
			MWRequestStats stats = config.getServletMan().getServletStats(servletClass);
			long startTime = System.nanoTime(); // for stat keeping
			try
			{
				stats.startProcessing(); // synchronization is not required, so long as endProcessing is always called
				SimpleServlet servletInstance = servletClass.newInstance(); // instantiate a new servlet instance!
				if(request.getMethod() == HTTPMethod.GET)
					servletInstance.doGet(servletRequest, servletResponse);
				else
				if(request.getMethod() == HTTPMethod.POST)
					servletInstance.doPost(servletRequest, servletResponse);
				servletInstance.service(request.getMethod(), servletRequest, servletResponse);
				return servletResponse.generateOutput(request); // the generated output, yea!
			}
			finally
			{
				session.touch();
				stats.endProcessing(System.nanoTime() - startTime);
			}
		}
		catch (HTTPException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			config.getLogger().throwing("", "", e);
			throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
		}
	}

	/**
	 * Will look for an if-modified-since header.  If none is found, nothing happens. 
	 * If the given buffer was modified since the given date, however, it will throw
	 * a NOT_MODIFIED exception, otherwise it will do nothing.
	 * @param request the request containing the headers
	 * @param buffers the buffers containing a modified date
	 */
	private void checkIfModifiedSince(HTTPRequest request, final DataBuffers buffers) throws HTTPException
	{
		final String lastModifiedSince=request.getHeader(HTTPHeader.IF_MODIFIED_SINCE.lowerCaseName()); 
		if(lastModifiedSince != null)
		{
			try
			{
				final long sinceDate = Math.round(Math.floor((HTTPIOHandler.DATE_FORMAT.parse(lastModifiedSince.trim()).getTime())/1000.0))*1000;
				final long lastModDate = Math.round(Math.floor((buffers.getLastModified().getTime())/1000.0))*1000;
				if(sinceDate >= lastModDate)
					throw HTTPException.standardException(HTTPStatus.S304_NOT_MODIFIED);
			}
			catch(ParseException e) { }
		}
	}
	
	/**
	 * Retreives a buffer set containing the possibly cached contents of the file. 
	 * This can trigger file reads, servlet calls and other ways
	 * of generating body data.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	public DataBuffers getFileData(HTTPRequest request) throws HTTPException
	{
		// split the uri by slashes -- the first char is always /!!
		String[] url = request.getUrlPath().split("/");
		// first thing is to check for servlets
		if(url.length > 1)
		{
			Class<? extends SimpleServlet> servletClass = config.getServletMan().findServlet(url[1]);
			if(servletClass != null)
			{
				return executeServlet(request,servletClass);
			}
		}
		
		// not a servlet, so it must be a file path
		final File pageFile = assembleFileRequest(request);
		if(pageFile.isDirectory())
			throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
		
		final MIMEType mimeType = MIMEType.getMIMEType(pageFile.getName());
		DataBuffers buffers = null;
		try
		{
			buffers = new MWDataBuffers(); // before forming output, process range request
			Class<? extends HTTPOutputConverter> converterClass=config.getConverters().findConverter(mimeType);
			if(converterClass != null)
			{
				buffers=config.getFileCache().getFileData(pageFile, null);
				//checkIfModifiedSince(request,buffers); this is RETARDED!!!
				HTTPOutputConverter converter;
				try { 
					converter = converterClass.newInstance();
					return new MWDataBuffers(converter.convertOutput(config, request, HTTPStatus.S200_OK, buffers.flushToBuffer()), System.currentTimeMillis());
				} catch (Exception e) { }
				return buffers;
			}
			else
			{
				return config.getFileCache().getFileData(pageFile, null);
			}
		}
		catch(HTTPException e)
		{
			buffers.close();
			throw e;
		}
	}
	
	/**
	 * Generates a bytebuffer representing the results of the request 
	 * contained herein.  HTTP errors can still be generated, however,
	 * so those are watched for.
	 * 
	 * Requests can trigger file reads, servlet calls and other ways
	 * of generating body and header data.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	public DataBuffers generateOutput(HTTPRequest request) throws HTTPException
	{
		if(request.getUrlPath().length()>1)
		{
			Class<? extends SimpleServlet> servletClass = config.getServletMan().findServlet(request.getUrlPath().substring(1));
			if(servletClass != null)
			{
				return executeServlet(request,servletClass);
			}
		}
		
		// not a servlet, so it must be a file path
		final File pageFile = assembleFileRequest(request);
		if(pageFile.isDirectory()) //TODO: support directory browsing someday
		{
			HTTPException movedException=HTTPException.standardException(HTTPStatus.S301_MOVED_PERMANENTLY);
			movedException.getErrorHeaders().put(HTTPHeader.LOCATION, request.getFullHost() + request.getUrlPath() + "/");
			throw movedException;
		}
		
		final MIMEType mimeType = MIMEType.getMIMEType(pageFile.getName());
		final Map<HTTPHeader,String> extraHeaders=new TreeMap<HTTPHeader, String>();
		extraHeaders.put(HTTPHeader.CONTENT_TYPE, mimeType.getType());
		confirmMimeType(request,mimeType);
		
		HTTPStatus responseStatus = HTTPStatus.S200_OK;
		DataBuffers buffers = null;
		try
		{
			buffers = new MWDataBuffers(); // before forming output, process range request
			switch(request.getMethod())
			{
			case HEAD:
			case GET:
			case POST:
			{
				Class<? extends HTTPOutputConverter> converterClass=config.getConverters().findConverter(mimeType);
				if(converterClass != null)
				{
					buffers=config.getFileCache().getFileData(pageFile, null);
					//checkIfModifiedSince(request,buffers); this is RETARDED!!!
					try
					{
						HTTPOutputConverter converter;
						converter = converterClass.newInstance();
						extraHeaders.put(HTTPHeader.CACHE_CONTROL, "no-cache");
						final long dateTime=System.currentTimeMillis();
						extraHeaders.put(HTTPHeader.EXPIRES, HTTPIOHandler.DATE_FORMAT.format(Long.valueOf(dateTime)));
						buffers=new MWDataBuffers(converter.convertOutput(config, request, HTTPStatus.S200_OK, buffers.flushToBuffer()), dateTime);
						buffers = handleEncodingRequest(request, null, buffers, extraHeaders);
					}
					catch (Exception e)
					{
						config.getLogger().throwing("", "", e);
						throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
					}
				}
				else
				{
					final String[] eTagMarker =generateETagMarker(request);
					buffers=config.getFileCache().getFileData(pageFile, eTagMarker);
					if((eTagMarker[0]!=null)&&(eTagMarker[0].length()>0))
						extraHeaders.put(HTTPHeader.ETAG, eTagMarker[0]);
					checkIfModifiedSince(request,buffers);
					buffers = handleEncodingRequest(request, pageFile, buffers, extraHeaders);
				}
				if(setRangeRequests(request, buffers))
					responseStatus = HTTPStatus.S206_PARTIAL_CONTENT;
				break;
			}
			default:
				break;
			}
			// finally, generate the response headers and body
			switch(request.getMethod())
			{
			case HEAD:
				MWDataBuffers header=new MWDataBuffers(generateStandardHeaderResponse(request, responseStatus, extraHeaders, buffers), buffers.getLastModified().getTime());
				buffers.close();
				return header;
			case GET:
			case POST:
				return generateStandardResponse(request, responseStatus, extraHeaders, buffers);
			default:
			{
				final HTTPException exception = new HTTPException(HTTPStatus.S405_METHOD_NOT_ALLOWED);
				exception.getErrorHeaders().put(HTTPHeader.ALLOW, HTTPMethod.getAllowedList());
				throw exception;
			}
			}
		}
		catch(HTTPException e)
		{
			if(buffers!=null)
				buffers.close();
			throw e;
		}
	}
}
