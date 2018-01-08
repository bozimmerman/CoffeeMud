package com.planet_ink.coffee_web.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWConfig.DisableFlag;

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
 * One of several important workhorses for the web server.
 * It's job is:
 *  1. act as a pojo information container for an http request.
 *  	1a. these are managed by httpreader
 *  2. parse the high-level parts of that request to populate its pojo fields
 *  	2a. also called by httpreader, who provides the high-level parts
 *  3. track whether its finishRequest method has been called for final parsing
 *  	3a. httpreqprocessor is later used to parse data out
 *  	3b. httpservletrequest also wraps this obj
 * @author Bo Zimmerman
 */
public class CWHTTPRequest implements HTTPRequest
{
	// internal enum for multi-part form parsing
	private enum BoundaryState { BEFOREFIRST, HEADER, BODY }
	
	private static final String 		HTTP_VERSION_HEADER	= "HTTP/";  // prefix for http version headers in request lines
	private static final String 		FUTURE_URL_HEADER 	= "http://"; // prefix for future url syntax
	private static final String 		EOLN				= HTTPIOHandler.EOLN; // a copy of the official end of line
	private static final String 		BOUNDARY_KEY_DEF	= "boundary="; // prefix for boundary var markers for multi-part forms
	private static final InputStream 	emptyInput			= new ByteArrayInputStream(new byte[0]); // quick, easy, empty input
	private static final Charset		utf8				= Charset.forName("UTF-8");

	private HTTPMethod 	 		 	requestType  	= null;		// request type defs to null so that method-not-allowed is generated
	private String 	 			 	requestString	= null;		// full request line, including method, path, etc..
	private final Map<String,String>headers	  		= new Hashtable<String,String>(); // all the base headers received for this request
	private Map<String,String>   	urlParameters	= null;   	// holds url parameters, urlencoded variables, and form-data variables
	private String					queryString		= null; 	// holds url parameters pre-parsed -- the stuff after the ?
	private ByteBuffer	 		 	buffer;	  					// acts as both the line buffer and data buffer
	private int					 	bodyLength		= 0;		// length of the data buffer, and flag that a body was received
	private float				 	httpVer	  		= 1.0f;		// version of this http request (1.0, 1.1, etc)
	private String					simpleHost		= null;
	private InputStream			 	bodyStream		= null;		// the input stream for the main data body
	private String				 	uriPage	  		= null;		// portion of the request without urlparameters
	private boolean				 	isFinished		= false;	// flag as to whether finishRequest and processing is ready
	private List<long[]>		 	byteRanges		= null;		// if this is a ranged request, this will hold the ranges requested
	private final Map<String,String>cookies	  		= new HashMap<String,String>(); // if cookies were received, they are mapped here
	private final InetAddress		address;					// the inet address of the request incoming
	private List<MultiPartData>  	parts		 	= null;		// if this is multi-part request, this will have a list of the parts
	private final boolean		 	isDebugging;				// optomization for the when not debug logging
	private final Logger		 	debugLogger;
	private ByteBuffer	 		 	overFlowBuf 	= null;		// generated when a request buffer overflows to next (pipelining)
	private ByteArrayOutputStream	chunkBytes		= null;
	private Map<String,Double>	 	acceptEnc		= null;
	private final boolean		 	isHttps;
	private final boolean		 	overwriteDups;
	private final int			 	requestPort;
	private final long			 	requestLineSize;
	private final Set<DisableFlag>	disableFlags;
	private List<String>		 	headerRefs  	= new LinkedList<String>();
	private final List<String>		expects	 		= new LinkedList<String>();
	private Map<String,Object>   	reqObjects	 	= new HashMap<String,Object>();

	/**
	 * constructs a request object to handle requests from the given address
	 * @param address the address of the sender of the request
	 * @param isHttps whether this is an https request or not
	 * @param requestPort the port that was connected to for this request
	 * @param overwriteDups true to overwrite dup url parameters, false to add #
	 * @param requestLineSize number of bytes in a new request line
	 * @param debugLogger null, or a logger object to send debug messages to
	 * @param disableFlags a set of config disable flags, if any
	 * @param buffer a buffer to use instead of creating a new one
	 */
	public CWHTTPRequest(InetAddress address, boolean isHttps, int requestPort, boolean overwriteDups, 
						 long requestLineSize, Logger debugLogger, Set<DisableFlag> disableFlags, ByteBuffer buffer)
	{
		this.address=address;
		this.requestLineSize=requestLineSize;
		if(buffer==null)
			this.buffer=ByteBuffer.allocate((int)requestLineSize);
		else
			this.buffer=buffer;
		this.isDebugging = (debugLogger!=null);
		this.debugLogger=debugLogger;
		this.requestPort=requestPort;
		this.isHttps=isHttps;
		this.disableFlags=disableFlags;
		this.overwriteDups=overwriteDups;
	}
	
	/**
	 * a semi-copy constructor for pipelining
	 * @param previousRequest the request to model this one after
	 */
	public CWHTTPRequest(CWHTTPRequest previousRequest)
	{
		this(previousRequest.getClientAddress(), 
				previousRequest.isHttps, 
				previousRequest.getClientPort(),
				previousRequest.overwriteDups,
				previousRequest.requestLineSize,
				previousRequest.debugLogger,
				previousRequest.disableFlags,
				previousRequest.overFlowBuf);
	}
	
	/**
	 * Get the address of the requestor
	 * @return an inet address
	 */
	@Override
	public InetAddress getClientAddress()
	{
		return address;
	}

	/** 
	 * Get the portion of the request without urlparameters.
	 * Returns null if the request line has not yet been received
	 * @return the portion of the request without urlparameters.
	 */
	@Override
	public String getUrlPath()
	{
		return uriPage;
	}
	
	/**
	 * Get the entire request line, including method, path, etc
	 * Returns null if the request line has not yet been received
	 * @return the entire request line, including method, path, etc
	 */
	@Override
	public String getFullRequest()
	{
		return requestString;
	}
	
	/**
	 * Returns the http version of this request.  Returns 0.0 if 
	 * a request line has not been received yet.
	 * @return the http version
	 */
	@Override
	public float getHttpVer()
	{
		return httpVer;
	}
	
	/**
	 * The type of this request, or null if the request
	 * line has not yet been received
	 * @return the type of this request
	 */
	@Override
	public HTTPMethod getMethod()
	{
		return requestType;
	}

	/**
	 * If any url parameters, urlencoded body fields, or 
	 * form-data body fields were received, they are all
	 * mapped here.  The field names are normalized to
	 * lowercase, and the values decoded.
	 * @return a set of url keys
	 */
	@Override
	public Set<String> getUrlParameters()
	{
		if(urlParameters==null)
			return new TreeSet<String>();
		return urlParameters.keySet();
	}

	/**
	 * Gets the key fields from the url/form parms
	 * and their values as a copied map
	 * 
	 * @return The parameter names and values
	 */
	@Override
	public Map<String,String> getUrlParametersCopy()
	{
		if(urlParameters==null)
			return new TreeMap<String,String>();
		final Hashtable<String,String> parms=new Hashtable<String,String>();
		parms.putAll(urlParameters);
		return parms;
	}
	
	/**
	 * Returns the simple host as the requestor asked for it
	 * @return simple host name: blah.com
	 */
	@Override
	public String getHost()
	{
		if(simpleHost == null)
		{
			String newSimpleHost = headers.get(HTTPHeader.Common.HOST.toString().toLowerCase());
			if(newSimpleHost != null)
			{
				int x=newSimpleHost.indexOf(':');
				if(x>=0)
					newSimpleHost=newSimpleHost.substring(0,x);
			}
			else
				newSimpleHost = "";
			this.simpleHost = newSimpleHost;
		}
		return simpleHost;
	}
	
	/**
	 * Gets the client's connected-to port
	 * 
	 * @return The clients connected-to port
	 */
	@Override
	public int getClientPort()
	{
		return requestPort;
	}
	
	/**
	 * Returns the FULL host as the requestor asked for it
	 * This is like https://blahblah.com:8080
	 * @return full host info
	 */
	@Override
	public String getFullHost()
	{
		final StringBuilder host=new StringBuilder(isHttps?"https://":"http://");
		host.append(headers.get(HTTPHeader.Common.HOST.toString().toLowerCase()));
		if(((isHttps)&&(requestPort != CWConfig.DEFAULT_SSL_PORT))
		||((!isHttps)&&(requestPort != CWConfig.DEFAULT_HTP_LISTEN_PORT)))
			host.append(":").append(requestPort);
		return host.toString();
	}

	/**
	 * If finishRequest has been called, any remaining portions
	 * of the main body will have been mapped to an input stream
	 * for servlets or other readers to enjoy. NULL is returned
	 * if the request is not yet finished.
	 * @return an input stream for the request body, or null
	 */
	@Override
	public InputStream getBody()
	{
		return bodyStream;
	}

	/**
	 * If this is a ranged request, this will include a list of
	 * all ranges requested as integer arrays.  Each integer array
	 * can be 1 or 2 dimensional, with the first dimension always
	 * being "from" and the second (if available) the "to".
	 * @return a list of integer arrays for ranges requested
	 */
	@Override
	public List<long[]> getRangeAZ()
	{
		return byteRanges;
	}

	/**
	 * An important method! When the end of headers is received,
	 * calling this method will switch the internal buffer to
	 * receive the main body of the request according to the 
	 * length given.  Any remaining bytes in the line buffer
	 * are transferred to the new one.
	 * @param contentLength the length of the body expected
	 */
	public void setToReceiveContentBody(int contentLength)
	{
		final ByteBuffer previousBuffer = buffer;
		if(previousBuffer.remaining() > contentLength)
		{
			final int overflowSize=previousBuffer.remaining()-contentLength;
			final ByteBuffer tempBuf=ByteBuffer.wrap(previousBuffer.array(), contentLength, overflowSize);
			final int requestLineBufSize=(int)((CWThread)Thread.currentThread()).getConfig().getRequestLineBufBytes();
			this.overFlowBuf=ByteBuffer.allocate(requestLineBufSize);
			this.overFlowBuf.put(tempBuf);
			this.buffer=ByteBuffer.wrap(previousBuffer.array());
		}
		else
		if(contentLength>0)
		{
			this.buffer=ByteBuffer.allocate(contentLength);
			this.buffer.put(previousBuffer);
		}
		
		this.bodyLength=contentLength;
	}

	/**
	 * An important method! When the end of headers is received,
	 * and the headers indicated chunked encoding is forthcoming,
	 * calling this method will switch the internal buffer to
	 * receive the main body of the request according to the 
	 * length given.  Any remaining bytes in the line buffer
	 * are transferred to the new one.
	 * 
	 * @param chunkSize the size of the chunk to expect, or 0 for headers
	 * @return the internal byte buffer ready to write to
	 */
	public ByteBuffer setToReceiveContentChunkedBody(int chunkSize)
	{
		final ByteBuffer previousBuffer = buffer;
		if(chunkSize == 0)
			chunkSize = (int)requestLineSize ; // enough to hold chunk length bits and maybe headers?
		if(this.buffer.capacity() >= previousBuffer.remaining() + chunkSize)
		{
			this.buffer.compact();
		}
		else
		{
			this.buffer=ByteBuffer.allocate(previousBuffer.remaining() + chunkSize); // enough to hold chunk
			this.buffer.put(previousBuffer);
		}
		if(this.chunkBytes == null)
		{
			this.bodyLength=0; // this should be improved as we read ...
			this.chunkBytes=new ByteArrayOutputStream();
		}
		return this.buffer;
	}

	/**
	 * Returns the size of the body buffer in it's current state.  If
	 * receiving chunked encoding, will return bytes received thus far.
	 * @return size of the body buffer or bytes received in chunks
	 */
	public long getBufferSize()
	{
		return this.chunkBytes != null ? this.chunkBytes.size() : this.buffer.capacity();
	}

	/**
	 * Transfers the given number of bytes from the internal buffer
	 * to the chunked body accumulator and ensures the buffer is
	 * large enough to next round of stuff.
	 * @param byteCount the number of bytes to receive
	 * @return the internal buffer, ready to be written to
	 */
	public ByteBuffer receiveChunkedContent(final int byteCount)
	{
		if((this.buffer.remaining() >= byteCount) && (byteCount > 0))
		{
			try
			{
				final byte[] newBuffer = new byte[byteCount];
				buffer.get(newBuffer);
				this.chunkBytes.write(newBuffer);
			}
			catch(Exception e)
			{
				// eat it -- errors don't happen
			}
			this.bodyLength+=byteCount;
		}
		return setToReceiveContentChunkedBody(0); // make sure there's enough space for more stuff
	}
	
	/**
	 * Returns the current internal data parsing buffer.
	 * This will be the request & header "line buffer" up
	 * until setToReceiveContentBody is called, after which
	 * it will be the body content buffer.
	 * @return the current data buffer for the request.
	 */
	public ByteBuffer getBuffer()
	{
		return this.buffer;
	}

	/**
	 * If an Expects header has been received, this will allow
	 * an accessor to check for specific parsed entries.
	 * @param msg the expect to look for, eg. 100-continue
	 * @return true if its found, false otherwise
	 */
	public boolean isExpect(String msg)
	{
		return expects.contains(msg.toLowerCase().trim());
	}
	
	/**
	 * If this was a multi-part request, this will return the
	 * set of multi-parts found therein. Otherwise, this returns null.
	 * @return either a list of multi-parts, or null
	 */
	@Override
	public List<MultiPartData> getMultiParts()
	{
		if(parts==null)
			return new ArrayList<MultiPartData>(1);
		return parts;
	}
	
	/**
	 * Returns whether finishRequest has been called, and important
	 * state change denoting that the request is ready to be processed.
	 * @return true if the request is ready to be processed, false otherwise
	 */
	public boolean isFinished()
	{
		return isFinished;
	}

	/**
	 * Returns whether the given encoding is acceptable, and to what
	 * extent.  If the value returned is 0, then the encoding given
	 * is NOT acceptable.  Any other value depends on the encoding.
	 * @param type an encoding type, such as compress or gzip
	 * @return the value of the coding that is acceptable
	 */
	@Override
	public double getSpecialEncodingAcceptability(String type)
	{
		if(acceptEnc == null) return 0.0;
		final Double val = acceptEnc.get(type.toLowerCase().trim());
		if(val == null) return 0.0;
		return val.doubleValue();
	}
	
	/**
	 * If an accept-encoding request is received, this method will parse it and
	 * fill the acceptEnc list so that the HTTPRequestProcessor can render
	 * a more interesting response body.
	 * @param encDefStr the raw accept-encoding request string
	 * @return the list of 1 or 2 dimensional integer arrays
	 * @throws HTTPException
	 */
	private Map<String,Double> parseAcceptEncodingRequest(String encDefStr) throws HTTPException
	{
		final String[] allEncDefs = encDefStr.split(",");
		final Map<String,Double> encs = new TreeMap<String,Double>();
		for(final String encDef : allEncDefs)
		{
			final String[] encDefParts = encDef.split(";",2);
			if(encDefParts.length==1)
				encs.put(encDefParts[0].trim().toLowerCase(),Double.valueOf(1.0));
			else
			{
				try
				{
					final int eqDex=encDefParts[1].indexOf('=');
					if((eqDex>0) && (encDefParts[1].substring(0, eqDex).trim().equalsIgnoreCase("q")))
					{
						final double qVal = Double.parseDouble(encDefParts[1].substring(eqDex+1).trim());
						if(qVal>0.0)
							encs.put(encDefParts[0].trim().toLowerCase(),Double.valueOf(qVal));
					}
				}
				catch(final NumberFormatException e)
				{
					// just ignore this
				}
			}
		}
		if(encs.size() > 0)
			return encs;
		return null;
	}
	
	/**
	 * If a range-request header is received, this will parse the request and generate a 
	 * list of ranges requested  as integer arrays.  Each integer array
	 * can be 1 or 2 dimensional, with the first dimension always
	 * being "from" and the second (if available) the "to".
	 * Will also "correct" the existing range request header for response compatibility.
	 * @param rangeDefStr the raw range request string
	 * @return the list of 1 or 2 dimensional integer arrays
	 * @throws HTTPException
	 */
	private List<long[]> parseRangeRequest(String rangeDefStr) throws HTTPException
	{
		final int descriptorIndex = rangeDefStr.indexOf('=');
		if(descriptorIndex>0)
		{
			final String desc = rangeDefStr.substring(0,descriptorIndex).trim();
			if(!desc.equalsIgnoreCase("bytes"))
				throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
			rangeDefStr=rangeDefStr.substring(descriptorIndex+1).trim();
		}
		final String[] allRangeDefs = rangeDefStr.split(",");
		final List<long[]> ranges = new LinkedList<long[]>();
		for(final String rangeDef : allRangeDefs)
		{
			final String[] rangeAZSetStrs = rangeDef.split("-");
			try
			{
				long[] rangeSetAZ;
				if(rangeAZSetStrs.length==1)
				{
					rangeSetAZ= new long[1];
				}
				else
				{
					rangeSetAZ= new long[2];
					rangeSetAZ[1] = Long.parseLong(rangeAZSetStrs[1]);
				}
				rangeSetAZ[0] = Long.parseLong(rangeAZSetStrs[0]);
				ranges.add(rangeSetAZ);
			}
			catch(final NumberFormatException e)
			{
				throw HTTPException.standardException(HTTPStatus.S416_REQUEST_RANGE_NOT_SATISFIED);
			}
		}
		if(ranges.size() > 0)
			return ranges;
		return null;
	}
	
	/**
	 * Internal utility method whose only purpose is to return whether the given
	 * buffer at the given index starts with the entire compare buffer sent.
	 * @param buf the buffer to look into
	 * @param bufStart the place in the buffer to start the compare
	 * @param compare the entire byte array to compare the buffer portion to
	 * @return true if a match is found, false otherwise
	 */
	private boolean startsWith(final byte[] buf, final int bufStart, final byte[] compare)
	{
		if((bufStart+compare.length)>=buf.length)
			return false;
		for(int i=0;i<compare.length;i++)
			if(buf[bufStart+i]!=compare[i])
				return false;
		return true;
	}

	/**
	 * This method does the hard ugly work of parsing a data body as a multi-part request
	 * and generating a list of those parts, potentially recursively! It is called as part
	 * of the finishRequest process that finalizes a request for processing.
	 * 
	 * Recursive multi-parts are parsed in-line on the existing un-copied buffer (uncopied is good, since
	 * lots of multi-parts get turned into high level fields and otherwise dont get their own buffers).
	 * Because of this, the current index into the main buffer has to be preserved between calls to parse,
	 * which is why a read/write index array is used. 
	 * 
	 * While form-data and urlencoded parts get put into the main request urlParameters set, multi-Parts can
	 * still have their own headers, and their unique variables as well.  However, as a longtime servlet 
	 * writer, I like having the various ways key/pairs are sent to a web server abstracted into a single
	 * list.
	 * 
	 * The algorithm for parsing the multi-part from the main data is a simple state machine whose states
	 * are defined in the BoundaryState enum.
	 * 
	 * @param boundaryDefStr the raw multi-part request header value that contains the part boundary definition
	 * @param index a read/write index into the raw whole data content buffer 
	 * @return a list of multi-parts, if any (a zero size can be returned).
	 * @throws HTTPException any parsing exceptions generated
	 */
	private List<MultiPartData> parseMultipartContent(String boundaryDefStr, final int[] index) throws HTTPException
	{
		final byte[] buf=buffer.array();
		final String[] parts=boundaryDefStr.split(";");
		final Map<String,String> urlParmsFound=new HashMap<String,String>();
		String boundary = null;
		for(String part : parts)
		{
			part=part.trim();
			if(part.startsWith(BOUNDARY_KEY_DEF))
			{
				boundary=part.substring(BOUNDARY_KEY_DEF.length());
				break;
			}
		}
		if(boundary == null)
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		final byte[] firstBoundaryBytes=("--"+boundary+EOLN).getBytes();
		final byte[] boundaryBytes=(EOLN+"--"+boundary+EOLN).getBytes();
		final byte[] lastBoundaryBytes=(EOLN+"--"+boundary+"--").getBytes();
		final byte[] eolnBytes=EOLN.getBytes();
		BoundaryState state=BoundaryState.BEFOREFIRST;
		int i=index[0];
		int stateIndex=-1;
		final List<MultiPartData> allParts = new LinkedList<MultiPartData>();
		MultiPartData currentPart = null;
		while(i<buf.length)
		{
			switch(state)
			{
			case BEFOREFIRST:
				if(startsWith(buf,i,firstBoundaryBytes))
				{
					i+=firstBoundaryBytes.length-1;
					stateIndex=i+1;
					state=BoundaryState.HEADER;
					currentPart=new MultiPartData();
				}
				break;
			case HEADER:
				if(startsWith(buf,i,eolnBytes))
				{
					final String headerLine=new String(Arrays.copyOfRange(buf,stateIndex,i),utf8);
					i+=eolnBytes.length-1;
					stateIndex=i+1;
					state=BoundaryState.HEADER;
					if(headerLine.length()==0)
					{
						state=BoundaryState.BODY;
					}
					else
					{
						final String[] headerParts=headerLine.split(":",2);
						if(headerParts.length==2) // non header data is, strangely, OK
						{
							final String headerKey=headerParts[0].toLowerCase().trim();
							if(currentPart!=null)
							{
								if((headerParts[1].length()>0)&&(headerParts[1].charAt(0)==' '))
									headerParts[1]=headerParts[1].substring(1);
								if(headerKey.equals(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()))
									currentPart.setContentType(headerParts[1]);
								else
								if(headerKey.equals(HTTPHeader.Common.CONTENT_DISPOSITION.lowerCaseName()))
									currentPart.setDisposition(headerParts[1]);
								else
									currentPart.getHeaders().put(headerKey, headerParts[1]);
							}
						}
					}
				}
				break;
			case BODY:
			{
				final boolean simpleBoundry=startsWith(buf,i,boundaryBytes);
				boolean lastBoundry=false;
				if(!simpleBoundry)
					lastBoundry=startsWith(buf,i,lastBoundaryBytes);
				if((simpleBoundry || lastBoundry) && (currentPart!=null))
				{
					final int startOfFinalBuffer=stateIndex;
					final int endOfFinalBuffer=i;
					i+=boundaryBytes.length-1;
					stateIndex=i+1;
					allParts.add(currentPart);
					index[0]=i;
					if(currentPart.getContentType().startsWith("multipart/"))
					{
						// recursion, yea!
						if (isDebugging) debugLogger.finest("Got multipart recursion");
						final List<MultiPartData> subParts=parseMultipartContent(currentPart.getContentType(), index);
						currentPart.getSubParts().addAll(subParts);
						i=index[0];
					}
					else
					if(currentPart.getContentType().startsWith("application/x-www-form-urlencoded"))
					{
						if (isDebugging) debugLogger.finest("Got multipart url data");
						parseUrlEncodedKeypairs(new String(Arrays.copyOfRange(buf, startOfFinalBuffer, endOfFinalBuffer),utf8));
						allParts.remove(currentPart);
					}
					else
					if(currentPart.getDisposition().equalsIgnoreCase("form-data")
					&& currentPart.getVariables().containsKey("name")
					&& !currentPart.getVariables().containsKey("filename")
					)
					{
						final String key=currentPart.getVariables().get("name").toLowerCase();
						if (isDebugging) debugLogger.finest("Got multipart "+currentPart.getContentType()+" "+currentPart.getDisposition()+" named "+key);
						final String value=new String(Arrays.copyOfRange(buf, startOfFinalBuffer, endOfFinalBuffer),utf8);
						if(urlParmsFound.containsKey(key) && !overwriteDups)
						{
							int x=1;
							while(urlParmsFound.containsKey(key+x))
								x++;
							urlParmsFound.put(key+x, value);
						}
						else
						{
							urlParmsFound.put(key, value);
						}
						allParts.remove(currentPart);
					}
					else
					{
						currentPart.setData(Arrays.copyOfRange(buf, startOfFinalBuffer, endOfFinalBuffer));
						if (isDebugging) debugLogger.finest("Got "+currentPart.getContentType()+" "+currentPart.getDisposition()+" of "+currentPart.getData().length+" bytes");
					}
					
					if(simpleBoundry)
					{
						currentPart=new MultiPartData();
						state=BoundaryState.HEADER;
					}
					else
					if(lastBoundry)
					{
						if (isDebugging) debugLogger.finest("Completed "+allParts.size()+" multiparts");
						for(final String key : urlParmsFound.keySet())
							addUrlParameter(key,urlParmsFound.get(key));
						return allParts;
					}
				}
				break;
			}
			}
			i++;
		}
		index[0]=i;
		for(final String key : urlParmsFound.keySet())
			addUrlParameter(key,urlParmsFound.get(key));
		return allParts;
	}
	
	/**
	 * An important method that denotes a final "end of stream" for the entire request, including the body.
	 * When called, the request will give everything a final lookover to see if the body can be folded
	 * back into other fields in the request, otherwise the final body content buffer reader is prepared
	 * and isFinished is set to true, signaling that this request, for better or worse, can be processed.
	 * 
	 * While form-data and urlencoded parts get put into the main request urlParameters set, multi-Parts can
	 * still have their own headers, and their unique variables as well.  However, as a longtime servlet 
	 * writer, I like having the various ways key/pairs are sent to a web server abstracted into a single
	 * list.
	 * 
	 * @throws HTTPException
	 */
	public void finishRequest() throws HTTPException
	{
		// first, a final error if no host header found
		if(!headers.containsKey(HTTPHeader.Common.HOST.lowerCaseName()))
			throw new HTTPException(HTTPStatus.S400_BAD_REQUEST, "<html><body><h2>No Host: header received</h2>HTTP 1.1 requests must include the Host: header.</body></html>");
		
		// if this is a range request, get the byte ranges ready for the One Who Will Generate Output
		if(headers.containsKey(HTTPHeader.Common.RANGE.lowerCaseName()) && (!disableFlags.contains(CWConfig.DisableFlag.RANGED)))
		{
			if (isDebugging) debugLogger.finest("Got range request!");
			byteRanges=parseRangeRequest(headers.get(HTTPHeader.Common.RANGE.lowerCaseName()));
		}
		
		if(chunkBytes != null)
		{
			this.bodyLength = chunkBytes.size();
			this.buffer = ByteBuffer.wrap(chunkBytes.toByteArray());
			chunkBytes = null;
		}
		
		// if no body was sent, there is nothing left to do, at ALL
		if(bodyLength == 0)
		{
			bodyStream = emptyInput;
		}
		else // if this entire body is one url-encoded string, parse it into the urlParameters and clear the body
		{
			bodyStream = new ByteArrayInputStream(buffer.array());
			if(headers.containsKey(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()) 
			&&(headers.get(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()).startsWith("application/x-www-form-urlencoded")))
			{
				final String byteStr=new String(buffer.array(),utf8);
				parseUrlEncodedKeypairs(byteStr);
				if (isDebugging) debugLogger.finest("Urlencoded data: "+byteStr);
				buffer=ByteBuffer.wrap(new byte[0]); // free some memory early, why don't ya
			}
			else // if this is some sort of multi-part thing, then the entire body is forfeit and MultiPartDatas are generated
			if(headers.containsKey(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()) 
			&&(headers.get(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()).startsWith("multipart/")))
			{
				if (isDebugging) debugLogger.finest("Got multipart request");
					final String boundaryDefStr=headers.get(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName());
				parts = parseMultipartContent(boundaryDefStr, new int[]{0});
				buffer=ByteBuffer.wrap(new byte[0]); // free some memory early, why don't ya
			}
			else // otherwise, this is an unhandled or generic body of data.. prepare the input bodystream
			{
				if (isDebugging) debugLogger.finest("Got generic body");
				buffer.position(0);
				buffer.limit(buffer.capacity());
			}
		}
		isFinished = true; // by setting this flag, we signal our doneness.
	}
	
	/**
	 * When cookie data is received in a Cookie header, its a special parsing
	 * case, since we maintain a nice list of cookies for servlets and such.
	 * @param cookieData the unparsed line of cookie key/pairs
	 */
	private void parseCookieData(final String cookieData)
	{
		final String[] allCookies = cookieData.split(";");
		for(final String cookiePair : allCookies)
		{
			final String[] pairStrs = cookiePair.split("=",2);
			if(pairStrs.length==2)
				cookies.put(pairStrs[0].trim(),pairStrs[1]);
			else
				cookies.put(pairStrs[0].trim(),"");
		}
	}
	
	/**
	 * If it is determined that a request is being forwarded, previous request
	 * builds need to be re-built, especially the headers.  This will return
	 * the string of header data up to this point.
	 * @param clearAfter set to true to clear the lines so no more are added.
	 * @return the header data.
	 */
	public List<String> getAllHeaderReferences(boolean clearAfter) 
	{ 
		if(this.headerRefs!=null)
		{
			final List<String> headerRefs=this.headerRefs;
			if(clearAfter) this.headerRefs=null;
			return headerRefs;
		}
		return new Vector<String>(0);
	}
	
	/**
	 * A simple static method for parsing a header line and adding it to the given map.
	 * @param headerLine the unparsed raw line of headerness
	 * @param headers the map to put the header in, if it was valid
	 * @return the name of the header, or null if bad parse
	 */
	public static HTTPHeader parseHeaderLine(final String headerLine, final Map<HTTPHeader,String> headers)
	{
		final int x = headerLine.indexOf(':'); // first : is the right :
		if(x > 0)
		{
			final String headerRawKey=headerLine.substring(0,x);
			final String headerKey = headerRawKey.toLowerCase().trim(); // lowercase is normalized!!!
			final String headerValue = headerLine.substring(x+1).trim();
			HTTPHeader header = HTTPHeader.Common.find(headerKey);
			if(header == null)
			{
				header = HTTPHeader.Common.createNew(headerKey);
			}
			headers.put(header , headerValue);
			return header;
		}
		return null;
	}
	
	/**
	 * When a line of the header is received, we need to find out if its a cookie
	 * and parse it elsewhere.  Otherwise, the header is put into our headers
	 * map, with the name of the  header normalized to lowercase for quick
	 * searches.
	 * @param headerLine the unparsed raw line of headerness
	 * @return the host name sent in the request, if one exists
	 * @throws HTTPException
	 */
	public String parseHeaderLine(final String headerLine) throws HTTPException
	{
		final int x = headerLine.indexOf(':'); // first : is the right :
		if(x > 0)
		{
			final String headerRawKey=headerLine.substring(0,x);
			if(headerRefs != null)
				headerRefs.add(headerRawKey);
			final String headerKey = headerRawKey.toLowerCase().trim(); // lowercase is normalized!!!
			final String headerValue = headerLine.substring(x+1).trim();
			headers.put(headerKey , headerValue);
			if(headerKey.equals(HTTPHeader.Common.HOST.lowerCaseName())) // special case!
				return headerValue;
			else
			if(headerKey.equals(HTTPHeader.Common.COOKIE.lowerCaseName())) // special case!
				parseCookieData(headerValue);
			else
			if(headerKey.equals(HTTPHeader.Common.EXPECT.lowerCaseName())) // special case!
				expects.addAll(Arrays.asList(headerValue.toLowerCase().split(";")));
			else
			if(headerKey.equals(HTTPHeader.Common.ACCEPT_ENCODING.lowerCaseName())) // special case!
				acceptEnc=parseAcceptEncodingRequest(headerValue);
			if (isDebugging) debugLogger.finer("Header received: "+headerLine);
			return null;
		}
		else
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
	}

	/**
	 * Internal method for adding either a url key/pair, a urlencoded key/pair, or a form-data
	 * key/pair.  The key is normalized to lowercase and trimmed.
	 * @param name the name of the field
	 * @param value the decoded value of the field.
	 */
	private void addUrlParameter(String name, String value)
	{
		if(urlParameters == null)
		{
			synchronized(this)
			{
				if(urlParameters == null)
				{
					urlParameters = new Hashtable<String,String>();
				}
			}
		}
		urlParameters.put(name.toLowerCase().trim(), value);
	}
	
	/**
	 * When url-encoded data is received, this method is called to parse out
	 * the key-pairs and put their decoded keys and values into the urlParameters
	 * list.
	 * @param parts the raw undecoded urlencoded line of data
	 * @throws HTTPException
	 */
	private void parseUrlEncodedKeypairs(String parts) throws HTTPException
	{
		try
		{
			final String[] urlParmArray = parts.split("&");
			final Map<String,String> urlParmsFound=new HashMap<String,String>();
			for(final String urlParm : urlParmArray)
			{
				final int equalDex = urlParm.indexOf('=');
				final String key;
				final String value;
				if(equalDex < 0)
				{
					key=URLDecoder.decode(urlParm,"UTF-8");
					value="";
				}
				else
				{
					key=URLDecoder.decode(urlParm.substring(0,equalDex),"UTF-8");
					value=URLDecoder.decode(urlParm.substring(equalDex+1),"UTF-8");
				}
				if(urlParmsFound.containsKey(key) && !overwriteDups)
				{
					int x=1;
					while(urlParmsFound.containsKey(key+x))
						x++;
					urlParmsFound.put(key+x, value);
				}
				else
				{
					urlParmsFound.put(key, value);
				}
			}
			for(final String key : urlParmsFound.keySet())
				addUrlParameter(key,urlParmsFound.get(key));
		}
		catch(final UnsupportedEncodingException ex)
		{
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		}
	}
	
	/**
	 * When a main request line is received, this method parses that request
	 * and populates the appropriate fields in the pojo portion, throwing
	 * an exception if anything is malformed about it. 
	 * @param requestLine the raw request line (eq GET / HTTP/1.1)
	 * @throws HTTPException
	 */
	public void parseRequest(String requestLine) throws HTTPException
	{
		requestString=requestLine;
		final String[] parts = requestLine.split(" ");
		if(parts.length != 3)
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		
		if(isDebugging)
			debugLogger.finest("Request: "+requestString);
		
		// first, parse the http version number from the last part
		if(!parts[2].startsWith(HTTP_VERSION_HEADER))
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		try
		{
			this.httpVer=Float.parseFloat(parts[2].substring(HTTP_VERSION_HEADER.length()));
		}
		catch(final NumberFormatException e)
		{
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		}
		
		// now parse the first part of the request for a valid method
		try
		{
			requestType = HTTPMethod.valueOf(parts[0]);
		}
		catch(final java.lang.IllegalArgumentException ae)
		{
			// do nothing
			requestType = null;
		}
		if(requestType == null)
		{
			final HTTPException exception = new HTTPException(HTTPStatus.S405_METHOD_NOT_ALLOWED);
			exception.getErrorHeaders().put(HTTPHeader.Common.ALLOW, HTTPMethod.getAllowedList());
			throw exception;
		}
		
		// lastly, parse the url portion, which could get complicated due to the stupid 
		// future uri support caveat
		try
		{
			String url = parts[1];
			if(url.startsWith(FUTURE_URL_HEADER)) // its weird, but we have to support those
			{
				url = url.substring(FUTURE_URL_HEADER.length());
				final int endOfUrl = url.indexOf('/');
				if(endOfUrl > 0)
				{
					headers.put(HTTPHeader.Common.HOST.lowerCaseName(), url.substring(0, endOfUrl));
					url = url.substring(endOfUrl);
				}
				else
					throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
			}
			final int urlEncodeSeparator=url.indexOf('?');
			if(urlEncodeSeparator >= 0)
			{
				uriPage = URLDecoder.decode(url.substring(0,urlEncodeSeparator),"UTF-8");
				queryString = url.substring(urlEncodeSeparator+1);
				parseUrlEncodedKeypairs(queryString);
			}
			else
			{
				uriPage = URLDecoder.decode(url,"UTF-8");
				queryString = "";
			}
		}
		catch(final UnsupportedEncodingException e)
		{
			throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
		}
	}

	/**
	 * Gets a specific parameter as parsed from request url
	 * 
	 * @param name The parameter name
	 * @return The parameter value
	 */
	@Override
	public String getUrlParameter(String name)
	{
		if(urlParameters==null) return null;
		return urlParameters.get(name.toLowerCase());
	}

	/**
	 * Gets whether a specific parameter is from request url
	 * 
	 * @param name The parameter name
	 * @return true if the parameter exists, false otherwise
	 */
	@Override
	public boolean isUrlParameter(String name)
	{
		if(urlParameters==null) return false;
		return urlParameters.containsKey(name.toLowerCase());
	}

	/**
	 * Gets a request header as supplied by the client
	 * 
	 * @param name The header name
	 * @return The header value
	 */
	@Override
	public String getHeader(String name)
	{
		return headers.get(name.toLowerCase());
	}

	/**
	 * Return the value of a cookie sent to the server in this request.
	 * @return the cookie value
	 */
	@Override
	public String getCookie(String name)
	{
		return cookies.get(name);
	}

	/**
	 * Return the query string, all the stuff in the request after the ?
	 * @return the query string
	 */
	@Override
	public String getQueryString()
	{
		return queryString;
	}

	/**
	 * Gets the key cookie names
	 * 
	 * @return The cookie names
	 */
	@Override
	public Set<String> getCookieNames()
	{
		return cookies.keySet();
	}

	@Override
	public void addFakeUrlParameter(String name, String value)
	{
		if(urlParameters==null)
			urlParameters=new HashMap<String,String>();
		urlParameters.put(name.toLowerCase(), value);
	}
	
	@Override
	public void removeUrlParameter(String name)
	{
		if(urlParameters!=null)
			urlParameters.remove(name.toLowerCase());
	}

	@Override
	public Map<String,Object> getRequestObjects()
	{
		if(reqObjects==null)
			reqObjects=new HashMap<String,Object>();
		return reqObjects;
	}
}
