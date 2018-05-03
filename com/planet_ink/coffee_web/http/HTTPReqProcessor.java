package com.planet_ink.coffee_web.http;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.FileCacheManager;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.interfaces.HTTPFileGetter;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletSession;
import com.planet_ink.coffee_web.util.ChunkSpec;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.RequestStats;
import com.planet_ink.coffee_mud.core.collections.Pair;

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * To go async, the first problem to solve is guaranteeing that
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
	private final CWConfig		config; // the mini web configuration
	private int					lastHttpStatusCode	= HTTPStatus.S500_INTERNAL_ERROR.getStatusCode();
	
	private final static String	EOLN	= HTTPIOHandler.EOLN;
	
	
	public HTTPReqProcessor(CWConfig config)
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
		
		if((buffers.getLength() > config.getFileCompMaxFileBytes()) || (buffers.getLength() < 256))
		{
			return buffers;
		}
		
		double deflatePreference = request.getSpecialEncodingAcceptability("deflate");
		if(deflatePreference==0.0) 
			deflatePreference = request.getSpecialEncodingAcceptability("x-deflate");
		
		double gzipPreference = request.getSpecialEncodingAcceptability("gzip");
		if(gzipPreference==0.0) 
			gzipPreference = request.getSpecialEncodingAcceptability("x-gzip");
		
		final double nonzipPreference = request.getSpecialEncodingAcceptability("*");
		
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
			headers.put(HTTPHeader.Common.CONTENT_ENCODING,compressorName);
		return compressedBytes;
	}
	
	
	/**
	 * Returns the last status code generated by this processor.
	 * Returns 500 if generateOutput has not yet been called.
	 * @see HTTPReqProcessor#generateOutput(HTTPRequest)
	 * @return the last status code generated by this processor.
	 */
	public int getLastHttpStatusCode()
	{
		return lastHttpStatusCode;
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
	private long[] checkRangeRequest(long[] rangeAZ, long limit) throws HTTPException
	{
		final long firstByte=rangeAZ[0];
		if((firstByte<0) || (firstByte>=limit))
			throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
		long lastByte=limit;
		if(rangeAZ.length==2)
		{
			lastByte=rangeAZ[1]+1;
			if((lastByte<firstByte)|| (lastByte>limit))
				throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
		}
		return new long[]{firstByte,lastByte};
	}

	/**
	 * If the request being processed is a range request, this method will
	 * take the old output buffer and return a new one with the new range.
	 * If it does nothing at all, it will simply return null;
	 * If there is a range parsing error, an http exception is thrown
	 * @param request the request being processed
	 * @param buffers the fully formed output buffer
	 * @return the final range, or null if no range set, or an exception of course
	 * @throws HTTPException
	 */
	private long[] setRangeRequests(HTTPRequest request, final DataBuffers buffers) throws HTTPException
	{
		final List<long[]> rangeXYSets = request.getRangeAZ();
		if((rangeXYSets!=null)&&(rangeXYSets.size()>0))
		{
			final List<long[]> ranges=new LinkedList<long[]>();
			final long[] fullRange=new long[]{buffers.getLength(),0};
			for(final long[] range : rangeXYSets)
			{
				final long[] newRange = checkRangeRequest(range,buffers.getLength());
				if(newRange[0] < fullRange[0])
					fullRange[0]=newRange[0];
				if(newRange[1] > fullRange[1])
					fullRange[1]=newRange[1];
			}
			if(fullRange[0] < fullRange[1])
			{
				ranges.add(fullRange);
				buffers.setRanges(ranges);
				return new long[]{fullRange[0],fullRange[1]-1};
			}
		}
		return null;
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
		final StringBuilder str=new StringBuilder("");
		final String overrideStatus = headers.get(HTTPHeader.Common.STATUS);
		if(overrideStatus != null)
			str.append("HTTP/").append(request.getHttpVer()).append(" ").append(overrideStatus);
		else
			str.append("HTTP/").append(request.getHttpVer()).append(" ").append(status.getStatusCode()).append(" ").append(status.description());
		str.append(EOLN);
		for(final HTTPHeader header : headers.keySet())
			str.append(header.makeLine(headers.get(header)));
		if((!headers.containsKey(HTTPHeader.Common.TRANSFER_ENCODING))
		||(!headers.get(HTTPHeader.Common.TRANSFER_ENCODING).equals("chunked")))
		{
			if(response != null)
				str.append(HTTPHeader.Common.CONTENT_LENGTH.makeLine(response.getLength()));
			else
				str.append(HTTPHeader.Common.CONTENT_LENGTH.makeLine(0));
		}
		if(response != null)
			str.append(HTTPHeader.Common.LAST_MODIFIED.makeLine(HTTPIOHandler.DATE_FORMAT.format(response.getLastModified())));
		if(config.isDebugging())
			config.getLogger().finer("Response: "+str.toString().replace('\r', ' ').replace('\n', ' '));
		str.append(HTTPIOHandler.SERVER_HEADER);
		str.append(HTTPIOHandler.CONN_HEADER);
		str.append(HTTPHeader.Common.getKeepAliveHeader());
		str.append(HTTPHeader.Common.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(new Date(System.currentTimeMillis()))));
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
		final ByteBuffer header=generateStandardHeaderResponse(request, status, headers, response);
		if((response != null) && (response.getLength() > 0))
		{
			response.insertTop(header, 0,false);
			return response;
		}
		if(response!=null)
			return new CWDataBuffers(header, response.getLastModified().getTime(),false);
		return new CWDataBuffers(header, System.currentTimeMillis(),false);
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
		final String mimeMaskStr = request.getHeader(HTTPHeader.Common.ACCEPT.lowerCaseName());
		if(mimeMaskStr!=null)
		{
			final String[] mimeMasks = mimeMaskStr.split(",");
			boolean matchedOne=false;
			for(final String mimeMask : mimeMasks)
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
	 * @return the full assembled file path
	 */
	@Override
	public String assembleFilePath(HTTPRequest request)
	{
		final String[] url = request.getUrlPath().split("/");
		final StringBuilder fullPath = new StringBuilder("/");
		if(url.length > 1)
		{
			final List<String> fixedUrl=new ArrayList<String>(url.length-1);
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
		return fullPath.toString();
	}
	
	/**
	 * After a final file path is assembled, this method
	 * returns a file object appropriate to accessing the
	 * given path.
	 * @param request the request being processed
	 * @param filePath the path being accessed
	 * @return the File object
	 */
	@Override
	public File createFile(final HTTPRequest request, String filePath)
	{
		final Pair<String,String> mountPath=config.getMount(request.getHost(),request.getClientPort(),filePath);
		if(mountPath != null)
		{
			String newFullPath=filePath.substring(mountPath.first.length());
			if(newFullPath.startsWith("/")&&mountPath.second.endsWith("/"))
				newFullPath=newFullPath.substring(1);
			filePath = (mountPath.second+newFullPath);
		}
		final FileManager mgr=config.getFileManager();
		File finalFile = mgr.createFileFromPath(filePath.replace('/', mgr.getFileSeparator()));
		// see if the path we have is complete, or if there's an implicit default page requested.
		if(request.getUrlPath().endsWith("/"))
		{
			final File dirFile = finalFile;
			finalFile=mgr.createFileFromPath(finalFile,config.getDefaultPage());
			if((!finalFile.exists())&&(dirFile.exists())&&(dirFile.isDirectory()))
			{
				String browseCode = config.getBrowseCode(request.getHost(),request.getClientPort(),filePath);
				if(browseCode != null) // it's allowed to be browsed
					finalFile = dirFile;
			}
		}
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
		String possibleETag=request.getHeader(HTTPHeader.Common.IF_NONE_MATCH.lowerCaseName());
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
	private SimpleServletSession getServletSession(HTTPRequest request, ServletResponse servletResponse)
	{
		SimpleServletSession session;
		final String oldSessionID=request.getCookie("cwsessid");
		if(oldSessionID == null)
		{
			session = config.getSessions().createSession(request);
			servletResponse.setCookie("cwsessid", session.getSessionId());
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
		final ServletResponse servletResponse = new ServletResponse(); // generate a response object
		final SimpleServletSession session=getServletSession(request, servletResponse); // get or create a session object
		final SimpleServletRequest servletRequest = new ServletRequest(session, request);
		try
		{
			final RequestStats stats = config.getServletMan().getServletStats(servletClass);
			final long startTime = System.nanoTime(); // for stat keeping
			try
			{
				stats.startProcessing(); // synchronization is not required, so long as endProcessing is always called
				final SimpleServlet servletInstance = servletClass.newInstance(); // instantiate a new servlet instance!
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
				lastHttpStatusCode=servletResponse.getStatusCode();
			}
		}
		catch (final HTTPException e)
		{
			throw e;
		}
		catch (final Exception e)
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
		final String lastModifiedSince=request.getHeader(HTTPHeader.Common.IF_MODIFIED_SINCE.lowerCaseName()); 
		if(lastModifiedSince != null)
		{
			try
			{
				final long sinceDate = Math.round(Math.floor((HTTPIOHandler.DATE_FORMAT.parse(lastModifiedSince.trim()).getTime())/1000.0))*1000;
				final long lastModDate = Math.round(Math.floor((buffers.getLastModified().getTime())/1000.0))*1000;
				if(sinceDate >= lastModDate)
					throw HTTPException.standardException(HTTPStatus.S304_NOT_MODIFIED);
			}
			catch(final ParseException e) { }
			catch(final NumberFormatException e) { }
			catch(final ArrayIndexOutOfBoundsException e) { }
		}
	}
	
	/**
	 * Checks for, and if found, funs a cgi script descripted by the given request path, 
	 * for the given request object.
	 * Returns the response databauffer, parsed to oblivion into the given headers.
	 * @param reqPath the request url to parse
	 * @param request the request object to process
	 * @return the body of the data generated by the cgi script, or null if not a cgi path
	 * @throws HTTPException an exception
	 */
	protected DataBuffers checkAndExecuteCGI(final String reqPath, final HTTPRequest request, final Map<HTTPHeader, String> headers) throws HTTPException
	{
		final Pair<String,String> cgiMount=config.getCGIMount(request.getHost(),request.getClientPort(),reqPath);
		if(cgiMount == null)
			return null;

		String cgiMountPath=cgiMount.first;
		String cgiLocalExePath=cgiMount.second;
		String remainderSubPath=reqPath;
		remainderSubPath=remainderSubPath.substring(cgiMountPath.length());
		while(remainderSubPath.startsWith("/"))
			remainderSubPath=remainderSubPath.substring(1);
		int nextSlash=remainderSubPath.indexOf('/');
		final int quesMark=remainderSubPath.lastIndexOf('?',nextSlash);
		if(quesMark>0)
			nextSlash=quesMark;
		File cgiFile = createFile(request, cgiLocalExePath);
		if((!cgiFile.exists())||(cgiFile.isDirectory()))
		{
			String cgiExeName = remainderSubPath;
			if(nextSlash>0)
			{
				cgiExeName = remainderSubPath.substring(0,nextSlash);
				remainderSubPath = remainderSubPath.substring(nextSlash);
			}
			else
				remainderSubPath="";
			if(!cgiLocalExePath.endsWith(""+config.getFileManager().getFileSeparator()))
				cgiLocalExePath+=config.getFileManager().getFileSeparator();
			cgiLocalExePath += cgiExeName;
			if(!cgiMountPath.endsWith("/"))
				cgiMountPath += "/";
			cgiMountPath += cgiExeName;
			cgiFile = createFile(request, cgiLocalExePath);
			if((!cgiFile.exists())||(cgiFile.isDirectory()))
				throw HTTPException.standardException(HTTPStatus.S404_NOT_FOUND);
		}
		else
		{
			if(nextSlash>0)
			{
				cgiMountPath = remainderSubPath.substring(0,nextSlash);
				remainderSubPath = remainderSubPath.substring(nextSlash);
			}
			else
				remainderSubPath="";
		}
		final String cgiExecPath = cgiFile.getAbsolutePath();
		final String cgiRootStr = cgiFile.getParentFile().getAbsolutePath();
		final CGIProcessor cgiProcessor = new CGIProcessor(cgiExecPath, cgiRootStr, cgiMountPath, remainderSubPath);
		final ByteBuffer output = cgiProcessor.convertOutput(config, request, cgiFile, HTTPStatus.S200_OK, ByteBuffer.wrap(new byte[0]));
		return new CWDataBuffers(HTTPReader.parseCGIContent(output, headers), System.currentTimeMillis(), false);
	}
	
	/**
	 * Retrieves a buffer set containing the possibly cached contents of the file. 
	 * This can trigger file reads, servlet calls and other ways
	 * of generating body data.  Apparently UNUSED internally, it must
	 * be for embedded usage.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	@Override
	public DataBuffers getFileData(HTTPRequest request) throws HTTPException
	{
		// split the uri by slashes -- the first char is always /!!
		final String[] url = request.getUrlPath().split("/");
		// first thing is to check for servlets
		if(url.length > 1)
		{
			final Class<? extends SimpleServlet> servletClass = config.getServletMan().findServlet(url[1]);
			if(servletClass != null)
			{
				return executeServlet(request,servletClass);
			}
		}
		
		// not a servlet, so it must be a file path
		final String reqPath = assembleFilePath(request);
		DataBuffers buffers = checkAndExecuteCGI(reqPath,request,null);
		if(buffers != null)
			return buffers;
		
		final File pathFile = createFile(request, reqPath);
		final File pageFile;
		if(pathFile.isDirectory())
		{
			pageFile=config.getFileManager().createFileFromPath(config.getBrowsePage());
			//TODO: check this: throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
		}
		else
		{
			pageFile = pathFile;
		}
		
		final MIMEType mimeType = MIMEType.All.getMIMETypeByExtension(pageFile.getName());
		try
		{
			buffers = new CWDataBuffers(); // before forming output, process range request
			final Class<? extends HTTPOutputConverter> converterClass=config.getConverters().findConverter(mimeType);
			if(converterClass != null)
			{
				buffers=config.getFileCache().getFileData(pageFile, null);
				//checkIfModifiedSince(request,buffers); this is RETARDED!!!
				HTTPOutputConverter converter;
				try { 
					converter = converterClass.newInstance();
					return new CWDataBuffers(converter.convertOutput(config, request, pathFile, HTTPStatus.S200_OK, buffers.flushToBuffer()), System.currentTimeMillis(), true);
				}
				catch (final Exception e) { }
				return buffers;
			}
			else
			{
				return config.getFileCache().getFileData(pageFile, null);
			}
		}
		catch(final HTTPException e)
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
	@Override
	public DataBuffers generateOutput(HTTPRequest request) throws HTTPException
	{
		DataBuffers buffers = null;
		try
		{
			if(request.getUrlPath().length()>1)
			{
				final Class<? extends SimpleServlet> servletClass = config.getServletMan().findServlet(request.getUrlPath().substring(1));
				if(servletClass != null)
				{
					return executeServlet(request,servletClass);
				}
			}
		
			final Map<HTTPHeader,String> extraHeaders=new HashMap<HTTPHeader, String>();
			HTTPStatus responseStatus = HTTPStatus.S200_OK;
			// not a servlet, so it must be a file path
			final String reqPath = assembleFilePath(request);
			buffers = checkAndExecuteCGI(reqPath,request,extraHeaders);
			if(buffers == null)
			{
				final File pageFile;
				final File pathFile;
				pathFile = createFile(request, reqPath);
				if(pathFile.isDirectory())
				{
					if(!request.getUrlPath().endsWith("/"))
					{
						final HTTPException movedException=HTTPException.standardException(HTTPStatus.S301_MOVED_PERMANENTLY);
										movedException.getErrorHeaders().put(HTTPHeader.Common.LOCATION, request.getFullHost() + request.getUrlPath() + "/");
						throw movedException;
					}
					pageFile=config.getFileManager().createFileFromPath(config.getBrowsePage());
				}
				else
				{
					pageFile = pathFile;
				}
				buffers = new CWDataBuffers(); // before forming output, process range request
				switch(request.getMethod())
				{
				case HEAD:
				case GET:
				case POST:
				{
					final MIMEType mimeType = MIMEType.All.getMIMEType(pageFile.getName());
					extraHeaders.put(HTTPHeader.Common.CONTENT_TYPE, mimeType.getType());
					confirmMimeType(request,mimeType);
					
					final Class<? extends HTTPOutputConverter> converterClass=config.getConverters().findConverter(mimeType);
					if(converterClass != null)
					{
						buffers=config.getFileCache().getFileData(pageFile, null);
						//checkIfModifiedSince(request,buffers); this is RETARDED!!!
						try
						{
							HTTPOutputConverter converter;
							converter = converterClass.newInstance();
							extraHeaders.put(HTTPHeader.Common.CACHE_CONTROL, "no-cache");
							final long dateTime=System.currentTimeMillis();
							if(dateTime >= 0)
								extraHeaders.put(HTTPHeader.Common.EXPIRES, HTTPIOHandler.DATE_FORMAT.format(Long.valueOf(dateTime)));
							buffers=new CWDataBuffers(converter.convertOutput(config, request, pathFile, HTTPStatus.S200_OK, buffers.flushToBuffer()), dateTime, true);
							buffers = handleEncodingRequest(request, null, buffers, extraHeaders);
						}
						catch (final Exception e)
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
							extraHeaders.put(HTTPHeader.Common.ETAG, eTagMarker[0]);
						checkIfModifiedSince(request,buffers);
						buffers = handleEncodingRequest(request, pageFile, buffers, extraHeaders);
					}
					if(buffers == null)
					{
						throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
					}
					final long fullSize = buffers.getLength();
					final long[] fullRange = setRangeRequests(request, buffers);
					if(fullRange != null)
					{
						responseStatus = HTTPStatus.S206_PARTIAL_CONTENT;
						extraHeaders.put(HTTPHeader.Common.CONTENT_RANGE, "bytes "+fullRange[0]+"-"+fullRange[1]+"/"+fullSize);
					}
					break;
				}
				default:
					break;
				}
			}

			lastHttpStatusCode=responseStatus.getStatusCode();

			String specifiedHost = request.getHost(); // check for chunking
			int chunkedSize = 0;
			if(specifiedHost != null)
			{
				final ChunkSpec chunkSpec = config.getChunkSpec(specifiedHost, request.getClientPort(), request.getUrlPath());
				if((chunkSpec != null) && (buffers.getLength() >= chunkSpec.getMinFileSize())) 
				{
					chunkedSize = chunkSpec.getChunkSize(); // set chunking flag
					extraHeaders.put(HTTPHeader.Common.TRANSFER_ENCODING, "chunked");
				}
			}
			
			// finally, generate the response headers and body
			switch(request.getMethod())
			{
			case HEAD:
				final DataBuffers header=new CWDataBuffers(generateStandardHeaderResponse(request, responseStatus, extraHeaders, buffers), buffers.getLastModified().getTime(), false);
				buffers.close();
				return header;
			case GET:
			case POST:
				final DataBuffers buf = generateStandardResponse(request, responseStatus, extraHeaders, buffers);
				buf.setChunked(chunkedSize);
				return buf;
			default:
			{
				final HTTPException exception = new HTTPException(HTTPStatus.S405_METHOD_NOT_ALLOWED);
				exception.getErrorHeaders().put(HTTPHeader.Common.ALLOW, HTTPMethod.getAllowedList());
				throw exception;
			}
			}
		}
		catch(final HTTPException e)
		{
			if(buffers!=null)
				buffers.close();
			throw e;
		}
	}
}
