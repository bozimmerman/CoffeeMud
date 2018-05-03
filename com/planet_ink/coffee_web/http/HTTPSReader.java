package com.planet_ink.coffee_web.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_web.util.CWConfig;

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
 * A version of the HTTPReader that handles ssl connections
 * @see HTTPReader
 * @author Bo Zimmerman
 *
 */
public class HTTPSReader extends HTTPReader
{
	private final SSLEngine	 sslEngine;				// an instance of the sslengine handling this request
	private final ByteBuffer sslIncomingBuffer;		// translation buffer holding raw incoming data before putting into app buffer
	private final ByteBuffer sslOutgoingBuffer;		// translation buffer holding ssl outgoing data after pulling from app buffer
	private final ByteBuffer appSizedBuf;			// When user app outgoing buffer is too large, a small bug breaks it into pieces
	private boolean		 	 handshakeComplete	= false; // set to true when handshaking is completed

	
	/**
	 * Constructor takes the server managing this request, and the channel to read from and write to,
	 * and the sslcontext
	 * @param server the web server managing this handler
	 * @param chan the channel to read from and write to
	 * @param sslContext the ssl context is important
	 * @throws IOException
	 */
	public HTTPSReader(WebServer server, SocketChannel chan, SSLContext sslContext) throws IOException
	{
		super(server, chan);
		sslEngine=sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		sslEngine.beginHandshake();
		final SSLSession session = sslEngine.getSession();
		final int netBufferMax = session.getPacketBufferSize();
		appSizedBuf=ByteBuffer.allocate(session.getApplicationBufferSize());
		sslIncomingBuffer=ByteBuffer.allocate(netBufferMax);
		sslOutgoingBuffer=ByteBuffer.allocate(netBufferMax);
	}
	
	/**
	 * Constructor takes the server managing this request, and the channel to read from and write to.
	 * It will generate a generic, useless, ssl context
	 * @param server the web server managing this handler
	 * @param chan the channel to read from and write to
	 * @throws IOException
	 */
	public HTTPSReader(WebServer server,  SocketChannel chan) throws IOException
	{
		this(server, chan, generateNewContext(server.getConfig()));
	}

	/**
	 * Returns a descriptive string for whether this is 
	 * an ssl or http reader
	 * @return the word https
	 */
	@Override
	protected String getReaderType()
	{
		return "https";
	}
	
	/**
	 * Inspired by: http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/sslengine/SSLEngineSimpleDemo.java
	 * @param config  configuration for ssl context
	 * @return the global ssl context
	 */
	public static synchronized final SSLContext generateNewContext(CWConfig config)
	{
		try
		{
			if((config.getSslKeystorePath()==null)
			||(config.getSslKeystorePassword()==null)
			||(config.getSslKeystoreType()==null)
			||(config.getSslKeyManagerEncoding()==null))
			{
				config.getLogger().finer("SSL not configured.");
				return null;
			}

			final char[] passphrase = config.getSslKeystorePassword().toCharArray();
			final KeyStore keyStore = KeyStore.getInstance(config.getSslKeystoreType());
			final FileManager mgr=config.getFileManager();
			keyStore.load(mgr.getFileStream(mgr.createFileFromPath(config.getSslKeystorePath())), passphrase);
	
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(config.getSslKeyManagerEncoding());
			kmf.init(keyStore, passphrase);
	
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(config.getSslKeyManagerEncoding());
			tmf.init(keyStore);
	
			final SSLContext mySSLContext = SSLContext.getInstance("SSLv3");
			mySSLContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			return mySSLContext;
		}
		catch(final Exception e)
		{
			config.getLogger().throwing("", "", e);
			return null;
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
	@Override
	protected int readBytesFromClient(ByteBuffer buffer) throws IOException
	{
		try
		{
			int bytesRead= chan.read(sslIncomingBuffer);
			if((bytesRead<=0)&&(sslIncomingBuffer.position()==0))
				return bytesRead;
			sslIncomingBuffer.flip();
			SSLEngineResult result = sslEngine.unwrap(sslIncomingBuffer, buffer);
			sslIncomingBuffer.compact();
			if(handshakeComplete)
				return buffer.position();
			HandshakeStatus status=sslEngine.getHandshakeStatus();
			while((status != HandshakeStatus.NOT_HANDSHAKING) && (!this.isCloseable()))
			{
				switch(status)
				{
				case NEED_TASK:
				{
				    Runnable runnable;
				    while ((runnable = sslEngine.getDelegatedTask()) != null) 
				    {
						runnable.run();
				    }
				    status=sslEngine.getHandshakeStatus();
					break;
				}
				case NEED_UNWRAP:
					bytesRead= chan.read(sslIncomingBuffer);
					if(sslIncomingBuffer.position()<=0) 
						return buffer.position();
					sslIncomingBuffer.flip();
					result = sslEngine.unwrap(sslIncomingBuffer, buffer);
					sslIncomingBuffer.compact();
					status=sslEngine.getHandshakeStatus();
					break;
				case NEED_WRAP:
					sslOutgoingBuffer.clear();
					result = sslEngine.wrap(ByteBuffer.wrap(new byte[0]), sslOutgoingBuffer);
					if(result.bytesProduced() > 0)
					{
						sslOutgoingBuffer.flip();
						chan.write(sslOutgoingBuffer);
						//super.writeBlockingBytesToChannel(sslOutgoingBuffer);
					}
					status=sslEngine.getHandshakeStatus();
					break;
				case FINISHED:
				    status=HandshakeStatus.NOT_HANDSHAKING;
				    break;
				case NOT_HANDSHAKING:
					break;
				}
			}
			config.getLogger().finer(name+" completed ssl handshake");
			handshakeComplete=true;
			return buffer.position();
		}
		catch(final SSLException ssle)
		{
			config.getLogger().severe(ssle.getMessage());
			return -1;
		}
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
		while(buffers.hasNext())
		{
			final ByteBuffer buffer=buffers.next();
			while(buffer.hasRemaining())
			{
				appSizedBuf.clear();
				if(buffer.remaining() <= appSizedBuf.remaining())
					appSizedBuf.put(buffer);
				else
				while((buffer.hasRemaining()) && (appSizedBuf.hasRemaining()))
					appSizedBuf.put(buffer.get());
				appSizedBuf.flip();
				while(appSizedBuf.hasRemaining())
				{
					final ByteBuffer outBuf=ByteBuffer.allocate(sslOutgoingBuffer.capacity());
					sslEngine.wrap(appSizedBuf, outBuf);
					if(outBuf.position() > 0)
					{
						outBuf.flip();
						super.writeBytesToChannel(new CWDataBuffers(outBuf, 0, false));
					}
				}
			}
		}
		buffers.close();
	}
	
	/**
	 * Closes the IO channel and marks this handler as closed
	 */
	@Override
	protected void closeChannels()
	{
		try
		{
			sslEngine.closeInbound();
		}
		catch (final SSLException e)
		{ }
		sslEngine.closeOutbound();
		super.closeChannels();
	}
}
