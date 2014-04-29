package com.planet_ink.miniweb.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import com.planet_ink.miniweb.interfaces.DataBuffers;
import com.planet_ink.miniweb.interfaces.HTTPIOHandler;
import com.planet_ink.miniweb.server.MiniWebServer;
import com.planet_ink.miniweb.util.MWDataBuffers;
import com.planet_ink.miniweb.util.MiniWebConfig;

/*
Copyright 2012-2014 Bo Zimmerman

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
 * Handles the connection to a remote web server as an async socket reader.
 * This class is instantiated by an HTTPReader (no ssl supported)
 * @author Bo Zimmerman
 *
 */
public class HTTPForwarder implements HTTPIOHandler, Runnable
{
	private static final AtomicLong 	idCounter		 = new AtomicLong(0);

	private final HTTPReader	clientReader;
	private final SocketChannel	webServerChannel;
	private volatile boolean	closeMe				=false;
	private volatile boolean	isRunning			=false;
	private final long			startTime;		   			  // the initial start time of the request, for overall age calculation
	private volatile long		idleTime 	 	 	= 0;	  // the last time this handler went idle
	private final boolean		isDebugging;			  	  // true if the log debug channel is on -- an optomization
	private ByteBuffer			responseBuffer;
	private final String		name;					  	  // the name of this handler -- to denote a request ID
	private final MiniWebConfig config;
	private final MiniWebServer server;

	private final LinkedList<DataBuffers>writeables	= new LinkedList<DataBuffers>();

	public HTTPForwarder(HTTPReader clientReader, MiniWebServer server, SocketChannel webServerChannel)
	{
		this.server=server;
		this.config=server.getConfig();
		this.clientReader=clientReader;
		this.webServerChannel=webServerChannel;
		this.startTime=System.currentTimeMillis();
		this.isDebugging = config.isDebugging();
		this.responseBuffer=ByteBuffer.allocate((int)config.getRequestLineBufBytes());
		this.name="forward#"+idCounter.addAndGet(1);
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

	@Override
	/**
	 * Whenever bytes are received on the remote web channel, they are flushed out and
	 * written directly to the waiting client.  Simple.
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
				while (!closeMe && (( bytesRead = webServerChannel.read(responseBuffer)) > 0) )
				{
					if(!announcedAlready)
					{
						config.getLogger().finer("Processing handler '"+name+"'");
						announcedAlready=true;
					}
					responseBuffer.flip(); // turn the writeable buffer into a "readable" one
					ByteBuffer writeableBuf=ByteBuffer.allocate(responseBuffer.remaining());
					writeableBuf.put(responseBuffer);
					writeableBuf.flip();
					clientReader.writeBytesToChannel(new MWDataBuffers(writeableBuf,0));
					responseBuffer.clear();
				}
				handleWrites();
				if((!closeMe) // if eof is reached, close this channel and mark it for deletion by the web server
				&& ((bytesRead < 0)
					|| (!webServerChannel.isConnected())
					|| (!webServerChannel.isOpen())))
				{
					closeChannels();
				}
			}
			catch(IOException e)
			{
				closeChannels();
				if(isDebugging)
					config.getLogger().finer("ERROR: "+e.getClass().getName()+": "+e.getMessage());
			}
			catch(Exception e)
			{
				closeChannels();
				config.getLogger().throwing("", "", e);
			}
			finally
			{
				isRunning=false;
				idleTime=System.currentTimeMillis();
			}
		}
	}

	/**
	 * Closes the IO channel and marks this handler as closed
	 */
	protected void closeChannels()
	{
		if(!closeMe)
		{
			closeMe=true;
			for(DataBuffers buf : this.writeables)
				buf.close();
			this.writeables.clear();
			if((isDebugging)&&(webServerChannel.isOpen()))
				config.getLogger().finer("Closed request forward handler '"+name+"'");
			try
			{
				webServerChannel.close();
			}catch(Exception e){}
		}
	}

	@Override
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
	}


	/**
	 * Reads bytes from the given buffer into the forwarding channel.
	 * This code is parsed out here so that it can be overridden by HTTPSReader
	 * @param buffers source buffer for the data write
	 * @return the number of bytes written
	 * @throws IOException
	 */
	@Override
	public int writeBlockingBytesToChannel(final DataBuffers buffers) throws IOException
	{
		synchronized(webServerChannel)
		{
			int bytesWritten=0;
			while(buffers.hasNext())
			{
				ByteBuffer buffer=buffers.next();
				while(buffer.remaining()>0)
				{
					int bytesOut=webServerChannel.write(buffer);
					if(bytesOut>=0)
						bytesWritten+=bytesOut;
					if(buffer.remaining()>0)
					{
						try{Thread.sleep(1);}catch(Exception e){}
					}
				}
			}
			return bytesWritten;
		}
	}

	/**
	 * Reads bytes from the given buffer into the forwarding channel.
	 * This code is parsed out here so that it can be overridden by HTTPSReader
	 * @param buffers source buffer for the data write
	 * @throws IOException
	 */
	@Override
	public void writeBytesToChannel(final DataBuffers buffers) throws IOException
	{
		writeables.addLast(buffers);
		handleWrites();
	}

	/**
	 * Cycles through the writeables buffer list and writes as much as it can.
	 * If it can't write any more, it will register the channel for write
	 * notifications.
	 * @throws IOException
	 */
	protected void handleWrites() throws IOException
	{
		synchronized(writeables)
		{
			while(writeables.size()>0)
			{
				DataBuffers bufs=writeables.getFirst();
				while(bufs.hasNext())
				{
					ByteBuffer buf=bufs.next();
					if(buf.remaining()>0)
					{
						webServerChannel.write(buf);
						if(buf.remaining()>0)
						{
							server.registerChannelInterest(webServerChannel, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
							return;
						}
					}
				}
				writeables.removeFirst();
			}
		}
	}

	@Override
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
		if((!webServerChannel.isOpen()) || (!webServerChannel.isConnected()))
			return true;
		if((startTime!=0) && (currentTime - startTime) > (config.getRequestMaxAliveSecs() * 1000))
			return true;
		return false;
	}

	@Override

	/**
	 * Returns true if this request is currently reading/processing the request
	 * @return true if in progress
	 */
	public boolean isRunning() { return isRunning;}

}
