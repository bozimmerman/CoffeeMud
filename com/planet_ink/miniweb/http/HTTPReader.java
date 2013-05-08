package com.planet_ink.miniweb.http;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.io.*;

import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.miniweb.interfaces.DataBuffers;
import com.planet_ink.miniweb.interfaces.HTTPIOHandler;
import com.planet_ink.miniweb.server.MiniWebServer;
import com.planet_ink.miniweb.util.MWDataBuffers;
import com.planet_ink.miniweb.util.WebAddress;
import com.planet_ink.miniweb.util.MiniWebConfig;

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
public class HTTPReader implements HTTPIOHandler, Runnable
{
	private static final AtomicLong 	idCounter		 = new AtomicLong(0);
	
	private volatile boolean 	 		isRunning 		 = false; // the request is currently getting active thread/read time
	private volatile boolean 	 		closeMe 		 = false; // the request is closed, along with its channel

	protected final SocketChannel  		chan;					  // the channel from which the request is read
	protected final String				name;					  // the name of this handler -- to denote a request ID
	protected final boolean				isDebugging;			  // true if the log debug channel is on -- an optomization
	private final long			 		startTime;		   		  // the initial start time of the request, for overall age calculation
	protected final MiniWebConfig		config;		  	  	 	  // mini web configuration variables
	protected final MiniWebServer		server;		  	  	 	  // mini web server managing this request

	private volatile long		 		idleTime 	 	 = 0;	  // the last time this handler went idle
	
	private volatile MWHTTPRequest 		currentReq;			  	  // the parser and pojo of the current request
	private volatile ParseState  		currentState	 = ParseState.REQ_INLINE;	// the current parse state of this request
	
	private volatile HTTPForwarder		forwarder		 = null;  // in the off chance everything is just being forwarded, it goes here
	
	private final LinkedList<DataBuffers>writeables		 = new LinkedList<DataBuffers>();
	
	private final static String			EOLN			 = HTTPIOHandler.EOLN;
	
	private enum ParseState { REQ_INLINE, REQ_EOLN, HDR_INLINE, HDR_EOLN, BODY, FORWARD, DONE } // state enum for high level parsing
	

	/**
	 * Constructor takes the server managing this request, and the channel to read from and write to.
	 * @param server the web server managing this runner, a place to get the config and register new ops
	 * @param chan the channel to read from and write to
	 * @param registerOps a list to add to when you need the server to make channel changes
	 * @throws IOException
	 */
	public HTTPReader(MiniWebServer server, SocketChannel chan) throws IOException
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
		this.currentReq=new MWHTTPRequest(clientAddress,isHttps, localPort, requestLineSize, debugLogger, requestBuffer);
		this.name=getReaderType()+"#"+idCounter.addAndGet(1);
		this.startTime=System.currentTimeMillis();
	}

	/**
	 * Returns a descriptive string for whether this is 
	 * an ssl or http reader
	 * @return
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
				for(DataBuffers buf : this.writeables)
					buf.close();
				this.writeables.clear();
			}
			if((isDebugging)&&(chan.isOpen()))
				config.getLogger().fine("Closed request handler '"+name);
			try {
				chan.close();
			}catch(Exception e){}
			if(forwarder!=null)
				forwarder.closeChannels();
		}
	}
	
	/**
	 * Closes the IO channel and marks this handler as closed
	 * Also waits until this runnable is no longer in progress
	 */
	public void closeAndWait()
	{
		closeChannels();
		long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (isRunning))
		{
			try { Thread.sleep(100); }catch(Exception e){}
		}
		if(forwarder!=null)
			forwarder.closeAndWait();
	}

	/**
	 * Returns true if this request is currently reading/processing the request
	 * @return true if in progress
	 */
	public boolean isRunning() { return isRunning && ((forwarder==null) || forwarder.isRunning());}

	/**
	 * Returns true if this handler is either closed, or needs to be
	 * due to timing out in one way or another.
	 * @return true if this handler is done
	 */
	public boolean isCloseable()
	{
		if(closeMe)
			return true;
		final long currentTime=System.currentTimeMillis();
		if((idleTime!=0) && ((currentTime - idleTime) > config.getRequestMaxIdleMs()))
			return true;
		if((!chan.isOpen()) || (!chan.isConnected()) || (!chan.isRegistered()))
			return true;
		if((startTime!=0) && (currentTime - startTime) > (config.getRequestMaxAliveSecs() * 1000))
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
			
			SocketChannel forwarderChannel = SocketChannel.open(address.getAddress());
			if (forwarderChannel == null) 
				throw new IOException("Unable to create channel.");
			synchronized(forwarderChannel)
			{
				if(!forwarderChannel.finishConnect())
					throw new HTTPException(HTTPStatus.S500_INTERNAL_ERROR);
				HTTPForwarder forwarder=new HTTPForwarder(this, server, forwarderChannel);
				forwarderChannel.configureBlocking (false);
				final String webContext=address.getContext();
				StringBuilder urlPage=new StringBuilder("");
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
				StringBuilder s=new StringBuilder(currentReq.getMethod().toString()).append(' ').append(urlPage)
							.append(" HTTP/").append(currentReq.getHttpVer()).append(EOLN);
				for(String headerKey : currentReq.getAllHeaderReferences(true))
					if(headerKey.equalsIgnoreCase(HTTPHeader.HOST.name()))
						s.append(HTTPHeader.HOST.name()).append(": ").append(address.getHost()).append(':').append(address.getPort()).append(EOLN);
					else
						s.append(headerKey).append(": ").append(currentReq.getHeader(headerKey.toLowerCase().trim())).append(EOLN);
				if(config.isDebugging())
					config.getLogger().fine(forwarder.getName()+": "+address.getAddressStr()+": "+s.toString().replace('\n', ' ').replace('\r', ' '));
				server.registerNewHandler(forwarderChannel, forwarder);
				this.forwarder=forwarder;
				return s.toString();
			}
		}
		catch (IOException e)
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
	 * 
	 * @param buffer the bytebuffer to process data from
	 * @throws HTTPException
	 */
	private void processBuffer(final ByteBuffer buffer) throws HTTPException
	{
		ParseState state = currentState; // since currentState is volatile, lets cache local before tinkering with it
		try
		{
			switch(state)
			{
			case BODY: // wile in a body state, there's nothing to do but see if its done
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
					ByteBuffer writeableBuf=ByteBuffer.allocate(buffer.remaining());
					writeableBuf.put(buffer);
					writeableBuf.flip();
					forwarder.writeBytesToChannel(new MWDataBuffers(writeableBuf,0));
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
					case REQ_INLINE:
						if(c=='\r')
							state=ParseState.REQ_EOLN;
						break;
					case REQ_EOLN:
					{
						if (c=='\n')
						{
							String requestLine = new String(buffer.array(), lastEOLIndex, buffer.position()-lastEOLIndex-2);
							lastEOLIndex=buffer.position();
							state=ParseState.HDR_INLINE;
							if (requestLine.length()>0)
							{
								currentReq.parseRequest(requestLine);
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
					case HDR_EOLN:
					{
						if (c=='\n')
						{
							final String headerLine = new String(buffer.array(), lastEOLIndex, buffer.position()-lastEOLIndex-2);
							lastEOLIndex=buffer.position();
							if(headerLine.length()>0) 
							{
								String host = currentReq.parseHeaderLine(headerLine);
								state=ParseState.HDR_INLINE;
								if(host!=null)
								{
									int x=host.indexOf(':');
									if(x>0) host=host.substring(0, x); // we only care about the host, we KNOW the port.
									Pair<String,WebAddress> forward=config.getPortForward(host,currentReq.getClientPort(),currentReq.getUrlPath());
									if(forward != null)
									{
										String requestLine=startPortForwarding(forward.second, forward.first);
										if(forwarder!=null)
										{
											DataBuffers out=new MWDataBuffers();
											out.add(ByteBuffer.wrap(requestLine.getBytes()),0);
											ByteBuffer writeableBuf=ByteBuffer.allocate(buffer.remaining());
											writeableBuf.put(buffer);
											writeableBuf.flip();
											out.add(writeableBuf,0);
											forwarder.writeBytesToChannel(out);
											buffer.clear();
											state=ParseState.FORWARD;
										}
									}
									currentReq.getAllHeaderReferences(true);
								}
							}
							else // a blank line means the end of the header section!!!
							{
								if("chunked".equalsIgnoreCase(currentReq.getHeader(HTTPHeader.TRANSFER_ENCODING.lowerCaseName())))
								{
									//TODO: Implement!
								}
								else
								{
									//the headers will tell you what to do next "BODY" is too vague
									final String contentLengthStr=currentReq.getHeader(HTTPHeader.CONTENT_LENGTH.lowerCaseName());
									if(contentLengthStr!=null)
									{
										try
										{
											// moment of truth, do we have a body forthcoming?
											int contentLength = Integer.parseInt(contentLengthStr);
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
										catch(NumberFormatException ne)
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
									writeBytesToChannel(new MWDataBuffers(ByteBuffer.wrap(HTTPIOHandler.CONT_RESPONSE),0));
							}
						}
						else // an error! Ignore this line!
						{
							lastEOLIndex=buffer.position();
							state=ParseState.REQ_INLINE;
						}
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
		catch(IOException ioe)
		{
			config.getLogger().throwing("", "", ioe);
			throw HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR);
		}
		finally
		{
			currentState = state; // the state was cached local, so copy back to memory when done
		}
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
						int bytesWritten=chan.write(buffer);
						if(bytesWritten>=0)
							written+=bytesWritten;
						if(buffer.remaining()>0)
						{
							try{Thread.sleep(1);}catch(Exception e){}
						}
					}
				}
				writeables.removeFirst();
			}
			return written;
		}
	}
	
	/**
	 * Reads bytes from the given buffer into the local channel.
	 * This code is parsed out here so that it can be overridden by HTTPSReader
	 * @param buffers source buffer for the data write
	 * @throws IOException
	 */
	public void writeBytesToChannel(final DataBuffers buffers) throws IOException
	{
		synchronized(this.writeables)
		{
			writeables.addLast(buffers);
		}
		handleWrites();
	}
	
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
					chan.write(buf);
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
	public void run()
	{
		synchronized(this) // don't let mulitple readers in at the same time, ever.
		{
			isRunning=true; // for record keeping purposes
			idleTime=0;
			try // begin of the IO error handling and record integrity block
			{
				int bytesRead=0; // read bytes until we can't any more
				boolean announcedAlready=!isDebugging;
				try // begin of the http exception handling block
				{
					while (!closeMe && (( bytesRead = readBytesFromClient(currentReq.getBuffer())) > 0) )
					{
						if(!announcedAlready)
						{
							config.getLogger().fine("Processing handler '"+name+"'");
							announcedAlready=true;
						}
						processBuffer(currentReq.getBuffer()); // process any data received
						if(currentReq.isFinished()) // if the request was completed, generate output!
						{
							HTTPReqProcessor processor = new HTTPReqProcessor(config);
							writeBytesToChannel(processor.generateOutput(currentReq));
							// after output, prepare for a second request on this channel
							currentReq=new MWHTTPRequest(currentReq);
							currentState=ParseState.REQ_INLINE;
						}
					}
				}
				catch(HTTPException me) // if an exception is generated, go ahead and send it out
				{
					writeBytesToChannel(me.generateOutput(currentReq));
					// have to assume any exception caused
					// before a finish is malformed and needs a closed connection.
					if(currentReq.isFinished())
					{
						currentReq=new MWHTTPRequest(currentReq);
						currentState=ParseState.REQ_INLINE;
					}
					else
						closeChannels();
				}
				handleWrites();
				if((!closeMe) // if eof is reached, close this channel and mark it for deletion by the web server
				&& ((bytesRead < 0) || (!chan.isConnected()) || (!chan.isOpen())))
				{
					closeChannels();
					currentState=ParseState.DONE;
				}
			}
			catch(IOException e)
			{
				closeChannels();
				currentState=ParseState.DONE; // a common case when client closes first
				if(isDebugging)
					config.getLogger().fine("ERROR: "+e.getClass().getName()+": "+e.getMessage());
			}
			catch(Exception e)
			{
				closeChannels();
				currentState=ParseState.DONE;
				config.getLogger().throwing("", "", e);
			}
			finally
			{
				isRunning=false;
				idleTime=System.currentTimeMillis();
			}
		}
	}
}
