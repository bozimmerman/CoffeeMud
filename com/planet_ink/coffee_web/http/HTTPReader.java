package com.planet_ink.coffee_web.http;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.ProtocolHandler;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_web.util.ThrottleSpec;
import com.planet_ink.coffee_web.util.WebAddress;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.Log.Type;

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
 * Handler of http request reading duties.  Instances of this class will handle
 * reading a request from the first byte to the last, populating an HTTPRequest
 * object with the help of that classes internal parsers.  
 * 
 * An internal state of high level request reading is maintained throughout
 * the process, as well as statistical information about the age of this request,
 * whether it is still running, whether it is being cancelled, etc.
 * @author Bo Zimmerman
 *
 */
public class HTTPReader implements HTTPIOHandler, ProtocolHandler, Runnable
{
	private static final AtomicLong 	idCounter		 = new AtomicLong(0);
	
	private final Semaphore				readSemaphore	 = new Semaphore(1);
	private volatile AtomicBoolean 		isRunning 		 = new AtomicBoolean(false); // the request is currently getting active thread/read time
	private volatile boolean 	 		closeMe 		 = false; // the request is closed, along with its channel
	private volatile boolean 	 		closeRequested 	 = false; // the request is closed, along with its channel

	protected final SocketChannel  		chan;					  // the channel from which the request is read
	protected final String				name;					  // the name of this handler -- to denote a request ID
	protected final boolean				isDebugging;			  // true if the log debug channel is on -- an optomization
	protected final CWConfig			config;					  // mini web configuration variables
	protected final WebServer			server;					  // mini web server managing this request

	private final AtomicLong	 		startTime 	 	 = new AtomicLong(0);				  // the initial start time of the request, for overall age calculation
	private final AtomicLong	 		idleTime 	 	 = new AtomicLong(System.currentTimeMillis());  // the last time this handler went idle
	
	private volatile CWHTTPRequest 		currentReq;			  	  // the parser and pojo of the current request
	private volatile ParseState  		currentState	 = ParseState.REQ_INLINE;	// the current parse state of this request
	private volatile int				nextChunkSize	 = 0;
	private volatile boolean			willProcessNext	 = false;  // whether the read buffer will be processed even without new data
	
	private volatile HTTPForwarder		forwarder		 = null;  // in the off chance everything is just being forwarded, it goes here
	private volatile ThrottleSpec		outputThrottle	 = null;
	private volatile ProtocolHandler	protocolHandler	 = this;
	private volatile int				lastHttpStatus	 = 0;
	
	private final LinkedList<DataBuffers>writeables		 = new LinkedList<DataBuffers>();
	
	private final static String			EOLN			 = HTTPIOHandler.EOLN;
	private static final Charset		utf8			 = Charset.forName("UTF-8");
	
	private enum ParseState  // state enum for high level parsing 
	{ 
		REQ_INLINE, 
		REQ_EOLN, 
		HDR_INLINE, 
		HDR_EOLN, 
		CHUNKED_HEADER_INLINE, 
		CHUNKED_HEADER_EOLN, 
		CHUNKED_ENDER_INLINE, 
		CHUNKED_ENDER_EOLN, 
		CHUNKED_TRAILER_INLINE, 
		CHUNKED_TRAILER_EOLN, 
		CHUNKED_BODY, 
		BODY, 
		FORWARD, 
		DONE 
	}
	

	/**
	 * Constructor takes the server managing this request, and the channel to read from and write to.
	 * @param server the web server managing this runner, a place to get the config and register new ops
	 * @param chan the channel to read from and write to
	 * @throws IOException
	 */
	public HTTPReader(WebServer server, SocketChannel chan) throws IOException
	{
		super();
		this.config=server.getConfig();
		this.server=server;
		this.chan=chan;
		this.isDebugging=config.isDebugging();
		final Logger debugLogger = (isDebugging)?config.getLogger():null;
		final ByteBuffer requestBuffer=ByteBuffer.allocate((int)config.getRequestLineBufBytes());
		final boolean isHttps= this.getReaderType().equals("https");
		final int localPort=chan.socket().getLocalPort();
		final InetAddress clientAddress=chan.socket().getInetAddress();
		final long requestLineSize=config.getRequestLineBufBytes();
		final boolean overwriteDups=config.getDupPolicy()==CWConfig.DupPolicy.OVERWRITE;
		this.currentReq=new CWHTTPRequest(clientAddress,isHttps, localPort, overwriteDups, requestLineSize, debugLogger, config.getDisableFlags(), requestBuffer);
		this.name=getReaderType()+"#"+idCounter.addAndGet(1);
		this.startTime.set(System.currentTimeMillis());
	}

	/**
	 * Returns a descriptive string for whether this is 
	 * an ssl or http reader
	 * @return the reader type, http or https
	 */
	protected String getReaderType()
	{
		return "http";
	}
	
	/**
	 * Returns the name of this handler.
	 * @return the name of this handler
	 */
	@Override
	public String getName()
	{
		return name;
	}
	
	/**
	 * Closes the IO channel and marks this handler as closed
	 */
	protected void closeChannels()
	{
		if(!closeMe)
		{
			closeMe=true;
			synchronized(this.writeables)
			{
				for(final DataBuffers buf : this.writeables)
					buf.close();
				this.writeables.clear();
			}
			if((isDebugging)&&(chan.isOpen()))
				config.getLogger().finer("Closed request handler '"+name);
			try
			{
				chan.close();
			}
			catch(final Exception e){}
			if(forwarder!=null)
				forwarder.closeChannels();
			if(this.protocolHandler != this)
				this.protocolHandler.closeAndWait();
		}
	}
	
	/**
	 * Closes the IO channel and marks this handler as closed
	 * Also waits until this runnable is no longer in progress
	 */
	@Override
	public void closeAndWait()
	{
		closeChannels();
		final long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (isRunning.get()))
		{
			try { Thread.sleep(100); }catch(final Exception e){}
		}
		if(forwarder!=null)
			forwarder.closeAndWait();
	}

	/**
	 * Returns true if this request is currently reading/processing the request
	 * @return true if in progress
	 */
	@Override 
	public boolean isRunning() 
	{ 
		return isRunning.get() && ((forwarder==null) || forwarder.isRunning());
	}

	@Override
	public boolean isTimedOut()
	{
		final long currentTime=System.currentTimeMillis();
		final long idleTime = this.idleTime.get();
		if(idleTime > 0)
		{
			final long idleDiffTime = isRunning.get() ? 0 : (currentTime - idleTime);
			if(idleDiffTime > config.getRequestMaxIdleMs())
			{
				if (isDebugging) config.getLogger().finest("Idle Timed out: "+this.getName()+" "+idleDiffTime +">"+ config.getRequestMaxIdleMs());
				return true;
			}
		}
		final long totalDiffTime = (currentTime - startTime.get()); 
		if((startTime.get()!=0) && (totalDiffTime > (config.getRequestMaxAliveSecs() * 1000)))
		{
			if (isDebugging) config.getLogger().finest("Over Timed Out: "+this.getName()+" "+totalDiffTime +">"+ (config.getRequestMaxAliveSecs() * 1000));
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this handler is either closed, or needs to be
	 * due to timing out in one way or another.
	 * @return true if this handler is done
	 */
	@Override
	public boolean isCloseable()
	{
		if(closeMe)
			return true;
		if((!chan.isOpen()) || (!chan.isConnected()) || (!chan.isRegistered()))
		{
			if (isDebugging) config.getLogger().finest("Disconnected: "+this.getName());
			return true;
		}
		if(this.protocolHandler != this)
		{
			if(this.protocolHandler.isTimedOut())
				return true;
		}
		else
		if(isTimedOut())
			return true;
		if((forwarder!=null) && (forwarder.isCloseable()))
			return true;
		return false;
	}

	
	/**
	 * Starts port forwarding for a normal httpreader
	 * @param address the address to forward it to.
	 * @param context the context that the initial request asked for
	 */
	protected String startPortForwarding(final WebAddress address, final String context) throws HTTPException
	{
		forwarder=null;
		try
		{
			
			final SocketChannel forwarderChannel = SocketChannel.open(address.getAddress());
			if (forwarderChannel == null) 
				throw new IOException("Unable to create channel.");
			synchronized(forwarderChannel)
			{
				if(!forwarderChannel.finishConnect())
					throw new HTTPException(HTTPStatus.S500_INTERNAL_ERROR);
				final HTTPForwarder forwarder=new HTTPForwarder(this, server, forwarderChannel);
				forwarderChannel.configureBlocking (false);
				final String webContext=address.getContext();
				final StringBuilder urlPage=new StringBuilder("");
				final String restOfUri=currentReq.getUrlPath().substring(context.length());
				if(webContext.length()>0)
					urlPage.append(webContext);
				if(restOfUri.length()>0)
				{
					if(restOfUri.startsWith("/"))
					{
						if(urlPage.length()==0)
							urlPage.append(restOfUri);
						else
						if(urlPage.charAt(urlPage.length()-1)=='/')
							urlPage.append(restOfUri.substring(1));
						else
							urlPage.append(restOfUri);
					}
					else
					if(urlPage.length()==0)
						urlPage.append('/').append(restOfUri);
					else
					if(urlPage.charAt(urlPage.length()-1)=='/')
						urlPage.append(restOfUri);
					else
						urlPage.append('/').append(restOfUri);
				}
				if(urlPage.length()==0) urlPage.append('/');
				final StringBuilder s=new StringBuilder(currentReq.getMethod().toString()).append(' ').append(urlPage)
							.append(" HTTP/").append(currentReq.getHttpVer()).append(EOLN);
				for(final String headerKey : currentReq.getAllHeaderReferences(true))
					if(headerKey.equalsIgnoreCase(HTTPHeader.Common.HOST.name()))
						s.append(HTTPHeader.Common.HOST.name()).append(": ").append(address.getHost()).append(':').append(address.getPort()).append(EOLN);
					else
						s.append(headerKey).append(": ").append(currentReq.getHeader(headerKey.toLowerCase().trim())).append(EOLN);
				if(config.isDebugging())
					config.getLogger().finer(forwarder.getName()+": "+address.getAddressStr()+": "+s.toString().replace('\n', ' ').replace('\r', ' '));
				server.registerNewHandler(forwarderChannel, forwarder);
				this.forwarder=forwarder;
				return s.toString();
			}
		}
		catch (final IOException e)
		{
			config.getLogger().throwing("", "", e);
			throw new HTTPException(HTTPStatus.S500_INTERNAL_ERROR);
		}
	}
	
	/**
	 * Here it is, the mighty mighty state machine.
	 * This ginormous function handles high level state for request reading.
	 * It is handed a ByteBuffer recently written to, which it then flips
	 * and reads out of (in the case of a request state) or simply checks
	 * for errors in the case of a body read state.  
	 * 
	 * The buffer may be modified if an actionable request piece is portioned
	 * off of it.  It will also modify the internal state of parsing as needed.
	 * It will help populate the HTTPRequest object as needed, but will not
	 * generate the output.  It may, however, mark the request as complete
	 * if the state warrants.
	 * 
	 * The method should exit with the buffer in the same writeable state it
	 * was handed.
	 * @param request the request currently being parsed
	 * @param buffer the bytebuffer to process data from
	 * 
	 * @throws HTTPException
	 */
	@Override
	public DataBuffers processBuffer(HTTPIOHandler handler, HTTPRequest request, ByteBuffer buffer) throws HTTPException
	{
		ParseState state = currentState; // since currentState is volatile, lets cache local before tinkering with it
		try
		{
			switch(state)
			{
			case BODY: // while in a body state, there's nothing to do but see if its done
			{
				if(buffer.position() >= buffer.capacity()) // the > part is a little silly
				{
					currentReq.finishRequest();
					state=ParseState.DONE;
				}
				break;
			}
			case FORWARD:
			{
				if(forwarder==null)
					state=ParseState.DONE;
				else
				{
					buffer.flip(); // turn the writeable buffer into a "readable" one
					final ByteBuffer writeableBuf=ByteBuffer.allocate(buffer.remaining());
					writeableBuf.put(buffer);
					writeableBuf.flip();
					forwarder.writeBytesToChannel(new CWDataBuffers(writeableBuf,0,false));
					buffer.clear();
				}
				break;
			}
			default:
			{
				buffer.flip(); // turn the writeable buffer into a "readable" one
				int lastEOLIndex = 0; // the marker for the last place an EOLN was found
				char c;	// current character being examined
				while(buffer.position() < buffer.limit())
				{
					c=(char)buffer.get();
					switch(state)
					{
					case CHUNKED_ENDER_INLINE:
					{
						if(c=='\r')
							state=ParseState.CHUNKED_ENDER_EOLN;
						else
							throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
						break;
					}
					case CHUNKED_ENDER_EOLN:
					{
						if(c=='\n')
						{
							lastEOLIndex=buffer.position();
							state=ParseState.CHUNKED_HEADER_INLINE;
						}
						else
							throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
						break;
					}
					case CHUNKED_HEADER_INLINE:
					{
						if(c=='\r')
							state=ParseState.CHUNKED_HEADER_EOLN;
						break;
					}
					case CHUNKED_HEADER_EOLN:
					{
						if (c=='\n')
						{
							final String chunkSizeStr = new String(Arrays.copyOfRange(buffer.array(), lastEOLIndex, buffer.position()-2),utf8).trim();
							lastEOLIndex=buffer.position();
							if (chunkSizeStr.length()>0)
							{
								final String[] parts=chunkSizeStr.split(";");
								this.nextChunkSize = Integer.parseInt(parts[0],16);
								if(this.nextChunkSize == 0) // we've reached the last chunk
								{
									buffer = currentReq.setToReceiveContentChunkedBody((int)config.getRequestLineBufBytes());
									buffer.flip();
									lastEOLIndex=0;
									state=ParseState.CHUNKED_TRAILER_INLINE;
								}
								else
								{
									 // check for illegal request
									if((this.nextChunkSize + currentReq.getBufferSize()) > config.getRequestMaxBodyBytes())
									{
										throw HTTPException.standardException(HTTPStatus.S413_REQUEST_ENTITY_TOO_LARGE);
									}
									buffer = currentReq.setToReceiveContentChunkedBody(this.nextChunkSize);
									buffer.flip();
									lastEOLIndex=0;
									state=ParseState.CHUNKED_BODY;
								}
							}
							else
								throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
						}
						else
							throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
						break;
					}
					case REQ_INLINE:
						if(c=='\r')
							state=ParseState.REQ_EOLN;
						break;
					case REQ_EOLN:
					{
						if (c=='\n')
						{
							final String requestLine = new String(Arrays.copyOfRange(buffer.array(), lastEOLIndex, buffer.position()-2),utf8);
							lastEOLIndex=buffer.position();
							state=ParseState.HDR_INLINE;
							if (requestLine.length()>0)
							{
								try
								{
									currentReq.parseRequest(requestLine);
								}
								catch(final NumberFormatException ne)
								{
									throw HTTPException.standardException(HTTPStatus.S400_BAD_REQUEST);
								}
							}
							else
							{
								// ignore blank lines here -- perhaps someone telnetted in.
							}
						}
						else // an error! Ignore this line!
						{
							lastEOLIndex=buffer.position();
							state=ParseState.REQ_INLINE;
						}
						break;
					}
					case HDR_INLINE:
						if(c=='\r')
							state=ParseState.HDR_EOLN;
						break;
					case CHUNKED_TRAILER_INLINE:
						if(c=='\r')
							state=ParseState.CHUNKED_TRAILER_EOLN;
						break;
					case CHUNKED_TRAILER_EOLN:
					case HDR_EOLN:
					{
						if (c=='\n')
						{
							final String headerLine = new String(Arrays.copyOfRange(buffer.array(), lastEOLIndex, buffer.position()-2),utf8);
							lastEOLIndex=buffer.position();
							if(headerLine.length()>0) 
							{
								String host = currentReq.parseHeaderLine(headerLine);
								if (state == ParseState.CHUNKED_TRAILER_EOLN)
									state=ParseState.CHUNKED_TRAILER_INLINE;
								else
									state=ParseState.HDR_INLINE;
								
								if(host!=null)
								{
									final int x=host.indexOf(':');
									if(x>0) host=host.substring(0, x); // we only care about the host, we KNOW the port.
									final Pair<String,WebAddress> forward=config.getPortForward(host,currentReq.getClientPort(),currentReq.getUrlPath());
									if((forward != null) && (state != ParseState.CHUNKED_TRAILER_INLINE))
									{
										final String requestLine=startPortForwarding(forward.second, forward.first);
										if(forwarder!=null)
										{
											final DataBuffers out=new CWDataBuffers();
											out.add(ByteBuffer.wrap(requestLine.getBytes()),0,false);
											final ByteBuffer writeableBuf=ByteBuffer.allocate(buffer.remaining());
											writeableBuf.put(buffer);
											writeableBuf.flip();
											out.add(writeableBuf,0,false);
											forwarder.writeBytesToChannel(out);
											buffer.clear();
											state=ParseState.FORWARD;
										}
									}
									outputThrottle = config.getResponseThrottle(host,currentReq.getClientPort(),currentReq.getUrlPath());
									currentReq.getAllHeaderReferences(true);
								}
							}
							else // a blank line means the end of the header section!!!
							{
								if(state == ParseState.CHUNKED_TRAILER_EOLN)
								{
									currentReq.finishRequest();
									state=ParseState.DONE;
								}
								else
								if("chunked".equalsIgnoreCase(currentReq.getHeader(HTTPHeader.Common.TRANSFER_ENCODING.lowerCaseName())))
								{
									state=ParseState.CHUNKED_HEADER_INLINE;
									buffer = currentReq.setToReceiveContentChunkedBody(0); // prepare for chunk length/headers
									buffer.flip();
									lastEOLIndex=0;
								}
								else
								{
									//the headers will tell you what to do next "BODY" is too vague
									final String contentLengthStr=currentReq.getHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName());
									if(contentLengthStr!=null)
									{
										try
										{
											// moment of truth, do we have a body forthcoming?
											final int contentLength = Integer.parseInt(contentLengthStr);
											if ((contentLength < 0) || (contentLength > config.getRequestMaxBodyBytes())) // illegal request
											{
												throw HTTPException.standardException(HTTPStatus.S413_REQUEST_ENTITY_TOO_LARGE);
											}
											else
											if(contentLength == 0) // no content means we are done .. finish the request
											{
												currentReq.setToReceiveContentBody(contentLength);
												currentReq.finishRequest();
												state=ParseState.DONE;
											}
											else // a positive content length means we should prepare to receive the body
											{
												currentReq.setToReceiveContentBody(contentLength);
												state=ParseState.BODY;
												// the line buffer might have contained the entire body, so check for that state and finish
												// if necessary
												if(currentReq.getBuffer().position() >= currentReq.getBuffer().capacity())
												{
													currentReq.finishRequest();
													state=ParseState.DONE;
												}
											}
										}
										catch(final NumberFormatException ne)
										{
											throw HTTPException.standardException(HTTPStatus.S411_LENGTH_REQUIRED);
										}
									}
									else
									{
										// we have an http exception for this, but why be a jerk
										currentReq.finishRequest();
										state=ParseState.DONE;
									}
								}
							}
							// continue
							if((state != ParseState.DONE)
							&&(currentReq.isExpect("100-continue")))
							{
								if(currentReq.getHttpVer()>1.0)
									writeBytesToChannel(new CWDataBuffers(ByteBuffer.wrap(HTTPIOHandler.CONT_RESPONSE),0,false));
							}
						}
						else // an error! Ignore this line!
						{
							lastEOLIndex=buffer.position();
							state=ParseState.REQ_INLINE;
						}
						break;
					}
					case CHUNKED_BODY: // while in a body state, there's nothing to do but see if its done
					{
						buffer.position(buffer.position()-1);
						final int len = (buffer.limit() >= this.nextChunkSize) ? this.nextChunkSize : buffer.limit(); 
						buffer = this.currentReq.receiveChunkedContent(len);
						buffer.flip();
						this.nextChunkSize -= len;
						if(this.nextChunkSize <= 0)
							state=ParseState.CHUNKED_ENDER_INLINE;
						break;
					}
					case FORWARD: // you can't get there from here 
					case BODY: // you can't get there from here 
						break;
					case DONE: // if done, we're done
						break;
					}
				}
				
				// check the new state
				switch(state)
				{
				case DONE:
					// just here to be clear that there's nothing left to do...
				case FORWARD:
					// nothing more to do in this case
					break;
				default:
				{
					// nothing to do .. 
					if(lastEOLIndex==0)
					{
						buffer.position(buffer.limit());
						buffer.limit(buffer.capacity());
					}
					else
					if(lastEOLIndex > buffer.limit())
					{
						buffer.clear();
					}
					else
					{
						buffer.position(lastEOLIndex);
						buffer.compact();
					}
					
					if(state==ParseState.REQ_EOLN)
						state=ParseState.REQ_INLINE;
					else
					if(state==ParseState.HDR_EOLN)
						state=ParseState.HDR_INLINE;
					break;
				}
				}
				break;
			}
			}
		}
		catch(final IOException ioe)
		{
			config.getLogger().throwing("", "", ioe);
			throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
		}
		finally
		{
			currentState = state; // the state was cached local, so copy back to memory when done
		}
		if(currentReq.isFinished())
		{
			final HTTPReqProcessor processor = new HTTPReqProcessor(config);
			final DataBuffers bufs = processor.generateOutput(currentReq);
			lastHttpStatus = processor.getLastHttpStatusCode();
			return bufs;
		}
		return null;
	}
	
	/**
	 * Reads bytes from the local channel into the given buffer, returning
	 * the number of bytes read.  This code is parsed out here so that it
	 * can be overridden by HTTPSReader
	 * @param buffer target buffer for the data read
	 * @return the number of bytes read (decoded)
	 * @throws IOException
	 */
	protected int readBytesFromClient(final ByteBuffer buffer) throws IOException
	{
		return chan.read (buffer);
	}

	/**
	 * Reads bytes from the given buffer into the local channel.
	 * This code is parsed out here so that it can be overridden by HTTPSReader
	 * @param buffers source buffer for the data write
	 * @return the number of bytes written
	 * @throws IOException
	 */
	@Override
	@Deprecated
	public int writeBlockingBytesToChannel(final DataBuffers buffers) throws IOException
	{
		int written=0;
		DataBuffers bufs=null;
		synchronized(writeables)
		{
			writeables.addLast(buffers);
			while(writeables.size()>0)
			{
				bufs=writeables.getFirst();
				while(bufs.hasNext())
				{
					ByteBuffer buffer=buffers.next();
					while(buffer.remaining()>0)
					{
						long maxToWrite = buffer.remaining();
						if(outputThrottle != null)
						{
							maxToWrite=outputThrottle.request(maxToWrite);
							if(maxToWrite < buffer.remaining())
								buffer = bufs.splitTopBuffer((int)maxToWrite);
						}
						final int bytesWritten=chan.write(buffer);
						if(bytesWritten>=0)
						{
							written+=bytesWritten;
						}
						if(outputThrottle != null)
						{
							outputThrottle.registerWritten(bytesWritten);
						}
						if(buffer.remaining()>0)
						{
							try
							{
								Thread.sleep(1);
							}
							catch(Exception e)
							{
							}
						}
					}
				}
				writeables.removeFirst();
			}
			return written;
		}
	}
	
	/**
	 * Parses CGI Input content, populating the given header map, and returning the content body
	 * as a bytebuffer ready for reading.
	 * @param inputBuffer the input buffer to read content from
	 * @param headerOutput the map to put headers into
	 * @return the bytebuffer with the content body, or null if the impossible occurs
	 */
	public static final ByteBuffer parseCGIContent(final ByteBuffer inputBuffer, final Map<HTTPHeader,String> headerOutput)
	{
		char c;	// current character being HDR_INLINE
		ParseState state = ParseState.HDR_INLINE;
		int lastEOLIndex = 0;
		final int origPosition = inputBuffer.position();
		while(inputBuffer.position() < inputBuffer.limit())
		{
			c=(char)inputBuffer.get();
			switch(state)
			{
			case HDR_INLINE:
				if(c=='\r')
					state=ParseState.HDR_EOLN;
				break;
			case HDR_EOLN:
			{
				if (c=='\n')
				{
					final String headerLine = new String(Arrays.copyOfRange(inputBuffer.array(), lastEOLIndex, inputBuffer.position()-2),utf8);
					lastEOLIndex=inputBuffer.position();
					if(headerLine.length()>0) 
					{
						if(headerOutput != null)
							CWHTTPRequest.parseHeaderLine(headerLine, headerOutput);
						state=ParseState.HDR_INLINE;
					}
					else // a blank line means the end of the header section!!!
					{
						inputBuffer.compact();
						inputBuffer.flip();
						return inputBuffer;
					}
				}
				else // an error!
				{
					inputBuffer.position(origPosition);
					return inputBuffer;
				}
				break;
			}
			default:
				return null; // this can't happen
			}
		}
		return ByteBuffer.wrap(new byte[0]);
	}
	
	/**
	 * Reads bytes from the given buffer into the local channel.
	 * This code is parsed out here so that it can be overridden by HTTPSReader
	 * @param buffers source buffer for the data write
	 * @throws IOException
	 */
	@Override
	public void writeBytesToChannel(final DataBuffers buffers) throws IOException
	{
		synchronized(this.writeables)
		{
			writeables.addLast(buffers);
		}
		handleWrites();
	}
	
	/**
	 * Actual do an async write from the internal buffers.  Writes as much as
	 * it can, then, when it can't write any more, register channel for more
	 * interest.
	 * @throws IOException
	 */
	protected void handleWrites() throws IOException
	{
		DataBuffers bufs = null;
		synchronized(writeables)
		{
			while(writeables.size()>0)
			{
				bufs=writeables.getFirst();
				while(bufs.hasNext())
				{
					ByteBuffer buf=bufs.next();
					if(outputThrottle != null)
					{
						final long maxToWrite=outputThrottle.request(buf.remaining());
						if(maxToWrite < buf.remaining())
							buf = bufs.splitTopBuffer((int)maxToWrite);
					}
					final int written = chan.write(buf);
					if(outputThrottle != null)
					{
						outputThrottle.registerWritten(written);
					}
					if(buf.remaining()>0)
					{
						server.registerChannelInterest(chan, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						return;
					}
				}
				bufs.close();
				writeables.removeFirst();
			}
		}
	}

	/**
	 * Main handler for the request reading and processing.  For a single request, it all happens here.
	 * The bytes are read out of the channel until there's none left to read.  processBuffer above is called
	 * to manage the state of parsing.
	 * 
	 * When parsing is complete, if the request is done, output is generated and written to the channel.
	 * Otherwise, we fall out and wait to be called again when more data is available to be read.
	 */
	@Override
	public void run()
	{
		try
		{
			int timeoutSeconds = config.getMaxThreadTimeoutSecs();
			if(timeoutSeconds <= 0)
				timeoutSeconds = Integer.MAX_VALUE;
			if(!this.readSemaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)) // don't let mulitple readers in at the same time, ever.
				return;
		}
		catch (InterruptedException e1)
		{
			return;
		}
		isRunning.set(true); // for record keeping purposes
		idleTime.set(0);
		try // begin of the IO error handling and record integrity block
		{
			int bytesRead=0; // read bytes until we can't any more
			boolean announcedAlready=!isDebugging;
			final boolean accessLogging = config.getLogger().isLoggable(Level.FINE);
			final StringBuilder accessLog = ( accessLogging )? new StringBuilder() : null;
			try // begin of the http exception handling block
			{
				while (!closeMe && ((( bytesRead = readBytesFromClient(currentReq.getBuffer())) > 0) || (willProcessNext)) )
				{
					willProcessNext = false;
					if(!announcedAlready)
					{
						config.getLogger().finer("Processing handler '"+name+"'");
						announcedAlready=true;
					}
					final DataBuffers bufs = protocolHandler.processBuffer(this, currentReq, currentReq.getBuffer()); // process any data received
					if((bufs != null) && (bufs.getLength() > 0))
						writeBytesToChannel(bufs);
					if(currentReq.isFinished()) // if the request was completed, generate output!
					{
						if((accessLog != null)&&(bufs != null))
						{
							accessLog.append(Log.makeLogEntry(Log.Type.access, Thread.currentThread().getName(), 
								currentReq.getClientAddress().getHostAddress()
								+" "+currentReq.getHost()+":"+currentReq.getClientPort()
								+" \""+currentReq.getFullRequest()+" \" "+lastHttpStatus+" "+bufs.getLength())).append("\n");
						}
						// after output, prepare for a second request on this channel
						final String closeHeader = currentReq.getHeader(HTTPHeader.Common.CONNECTION.lowerCaseName()); 
						if((closeHeader != null) && (closeHeader.trim().equalsIgnoreCase("close")))
							closeRequested = true;
						else
						if(this.protocolHandler == this)
						{
							currentReq=new CWHTTPRequest(currentReq);
							currentState=ParseState.REQ_INLINE;
						}
					}
				}
			}
			catch(final HTTPException me) // if an exception is generated, go ahead and send it out
			{
				final DataBuffers bufs=me.generateOutput(currentReq);
				writeBytesToChannel(bufs);
				if(accessLog != null)
				{
					accessLog.append(Log.makeLogEntry(Log.Type.access, Thread.currentThread().getName(), 
						currentReq.getClientAddress().getHostAddress()
						+" "+currentReq.getHost()+":"+currentReq.getClientPort()
						+" \""+currentReq.getFullRequest()+"\" "+me.getStatus().getStatusCode()+" "+bufs.getLength())).append("\n");
				}
				// have to assume any exception caused
				// before a finish is malformed and needs a closed connection.
				if(currentReq.isFinished())
				{
					final String closeHeader = currentReq.getHeader(HTTPHeader.Common.CONNECTION.lowerCaseName()); 
					if((closeHeader != null) && (closeHeader.trim().equalsIgnoreCase("close")))
						closeRequested = true;
					else
					if(this.protocolHandler == this)
					{
						currentReq=new CWHTTPRequest(currentReq);
						currentState=ParseState.REQ_INLINE;
					}
				}
				else
					closeChannels();
				if(me.getStatus() == HTTPStatus.S101_SWITCHING_PROTOCOLS)
				{
					if(me.getNewProtocolHandler() != null)
						this.protocolHandler = me.getNewProtocolHandler();
				}
			}
			finally
			{
				if((accessLogging)&&(accessLog!=null)&&(accessLog.length()>1))
				{
					if(config.getLogger().getClass().equals(Log.class))
						((Log)config.getLogger()).rawStandardOut(Type.access,accessLog.substring(0,accessLog.length()-1),Integer.MIN_VALUE);
					else
						config.getLogger().fine(accessLog.substring(0,accessLog.length()-1));
				}
			}
			handleWrites();
			 // if eof is reached, close this channel and mark it for deletion by the web server
			if(!closeMe)
			{
				if ((bytesRead < 0) 
				|| (!chan.isConnected()) 
				|| (!chan.isOpen())
				|| (closeRequested && (writeables.size()==0)))
				{
					closeChannels();
					currentState=ParseState.DONE;
				}
			}
			
		}
		catch(final IOException e)
		{
			if(isDebugging) config.getLogger().finer("Closing "+getName()+" due to: "+e.getClass().getName()+": "+e.getMessage());
			closeChannels();
			currentState=ParseState.DONE; // a common case when client closes first
		}
		catch(final Exception e)
		{
			closeChannels();
			currentState=ParseState.DONE;
			config.getLogger().throwing("", "", e);
		}
		finally
		{
			this.readSemaphore.release();
			idleTime.set(System.currentTimeMillis());
			isRunning.set(false);
		}
	}

	@Override
	public boolean scheduleProcessing()
	{
		if(!closeMe)
		{
			willProcessNext=true;
			server.registerChannelInterest(chan, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		}
		return !closeMe;
	}
}
