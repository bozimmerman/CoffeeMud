package com.planet_ink.coffee_mud.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/*
Copyright 2005-2018 Bo Zimmerman

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
	Similar to Javas pipe, except it's buffered and thread agnostic. 
*/
public class CoffeeIOPipe
{
	private final int[]				buffer;
	private final AtomicInteger		writeDex	= new AtomicInteger(0);
	private final AtomicInteger		readDex		= new AtomicInteger(0);
	private CMInputStream			inStream;
	private final CMOutputStream	outStream;

	/**
	 * Construct a single two-way pipe.
	 * 
	 * @param bufferSize size of the buffer
	 * @param writeCallback optional runnable called when a write occurs.
	 */
	public CoffeeIOPipe(int bufferSize, Runnable writeCallback)
	{
		this.buffer = new int[bufferSize];
		inStream	= new CMInputStream();
		outStream	= new CMOutputStream(writeCallback);
	}

	/**
	 * Construct a single two-way pipe.
	 * 
	 * @param bufferSize size of the buffer
	 */
	public CoffeeIOPipe(int bufferSize)
	{
		this(bufferSize,null);
	}

	/**
	 * The input stream for a single pipe.
	 * 
	 * @author Bo Zimmerman
	 */
	public class CMInputStream extends InputStream
	{
		private boolean closed = false;
		
		@Override
		public int read() throws IOException
		{
			if(closed)
				throw new IOException("Input stream closed.");
			synchronized(buffer)
			{
				if(readDex.get() == writeDex.get())
					return -1;
				final int b=buffer[readDex.getAndIncrement()];
				if(readDex.get() >= buffer.length)
					readDex.set(0);
				return b;
			}
		}
		
		@Override
		public int available() throws IOException 
		{
			if(closed)
				throw new IOException("Input stream closed.");
			final int read=readDex.get();
			final int write=writeDex.get();
			if(read == write)
				return 0;
			if(read > write)
				return buffer.length - read + write;
			return write-read;
		}
		
		@Override
		public void close()
		{
			closed=true;
		}
	}

	/**
	 * The output stream for a single pipe.
	 * 
	 * @author Bo Zimmerman
	 */
	public class CMOutputStream extends OutputStream
	{
		private boolean		closed	= false;
		private Runnable	writeCallback;

		/**
		 * Creates an output stream for a single pipe.
		 * 
		 * @param writeCallback optional callback when a write occurs
		 */
		private CMOutputStream(Runnable writeCallback)
		{
			this.writeCallback=writeCallback;
		}
		
		/**
		 * Creates an output stream for a single pipe.
		 */
		private CMOutputStream()
		{
			this.writeCallback=null;
		}
		
		/**
		 * Sets a callback that occurs when a write
		 * occurs. null is ok. 
		 * 
		 * @param runner the optional callback 
		 */
		public void setWriteCallback(Runnable runner)
		{
			this.writeCallback = runner;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			if(closed)
				throw new IOException("Input stream closed.");
			synchronized(buffer)
			{
				buffer[writeDex.getAndIncrement()] = b & 0xff;
				if(writeDex.get() >= buffer.length)
					writeDex.set(0);
				if(writeDex.get() == readDex.get())
				{
					if(readDex.incrementAndGet() >= buffer.length)
						readDex.set(0);
				}
			}
			if(writeCallback != null)
				writeCallback.run();
		}
		
		@Override
		public void close()
		{
			closed=true;
		}
	}

	/**
	 * Returns the input stream 
	 * @return the input stream 
	 */
	public CMInputStream getInputStream()
	{
		return inStream;
	}
	
	/**
	 * Returns the pipe output stream
	 * @return the pipe output stream
	 */
	public CMOutputStream getOutputStream()
	{
		return outStream;
	}

	/**
	 * Sets a callback that occurs when a write
	 * occurs. null is ok. 
	 * 
	 * @param runner the optional callback 
	 */
	public void setWriteCallback(Runnable runner)
	{
		outStream.setWriteCallback(runner);
	}

	/**
	 * Shutdowns the input stream
	 */
	public void shutdownInput()
	{
		inStream.close();
	}

	/**
	 * Shutdowns the output stream
	 */
	public void shutdownOutput()
	{
		outStream.close();
	}

	/**
	 * Closes the input stream.
	 */
	public void close()
	{
		shutdownInput();
		shutdownOutput();
	}

	/**
	 * A fake socket that consists of a pair of CoffeePipes.
	 * 
	 * @author Bo Zimmerman
	 */
	public static class CoffeePipeSocket extends Socket
	{
		private boolean			isClosed	= false;
		private InetAddress		addr		= null;
		private CoffeeIOPipe	pipe		= null;
		private CoffeeIOPipe	friendPipe	= null;

		/**
		 * Constructs a fake pipey socket.
		 * 
		 * @param addr the fake address to use when asked.
		 * @param myPipe the pipe to one side of the socket
		 * @param friendPipe the pipe to the other side of the socket
		 * @throws IOException
		 */
		public CoffeePipeSocket(InetAddress addr, CoffeeIOPipe myPipe, CoffeeIOPipe friendPipe) throws IOException
		{
			this.addr=addr;
			this.pipe = myPipe;
			this.friendPipe = friendPipe;
		}

		@Override
		public void shutdownInput() throws IOException
		{
			isClosed = true;
		}

		@Override
		public void shutdownOutput() throws IOException
		{
			isClosed = true;
		}

		@Override
		public boolean isConnected()
		{
			return !isClosed;
		}

		@Override
		public boolean isClosed()
		{
			return isClosed;
		}

		@Override
		public synchronized void close() throws IOException
		{
			if (friendPipe != null)
			{
				friendPipe.shutdownInput();
				friendPipe.shutdownOutput();
			}
			this.pipe.shutdownInput();
			this.pipe.shutdownOutput();
			isClosed = true;
		}

		@Override
		public CMInputStream getInputStream() throws IOException
		{
			return pipe.getInputStream();
		}

		@Override
		public CMOutputStream getOutputStream() throws IOException
		{
			return pipe.getOutputStream();
		}

		@Override
		public InetAddress getInetAddress()
		{
			return addr;
		}
	}

	/**
	 * A pair of coffee pipes, for using to build
	 * a pair of coffee pipe sockets.
	 * 
	 * @author Bo Zimmerman
	 */
	public static class CoffeeIOPipes
	{
		private final CoffeeIOPipe	leftPipe;
		private final CoffeeIOPipe	rightPipe;

		/**
		 * Creates a pair of coffee pipes
		 * 
		 * @param bufferSize The size of the buffers, split two ways between each pipe
		 * @param writeCallback the optional callback for when writes occur
		 */
		public CoffeeIOPipes(int bufferSize, Runnable writeCallback)
		{
			this.leftPipe=new CoffeeIOPipe(bufferSize/2, writeCallback);
			this.rightPipe=new CoffeeIOPipe(bufferSize/2, writeCallback);
			CMInputStream lin = this.leftPipe.inStream;
			this.leftPipe.inStream = this.rightPipe.inStream;
			this.rightPipe.inStream = lin;
		}

		/**
		 * Creates a pair of coffee pipes
		 * 
		 * @param bufferSize The size of the buffers, split two ways between each pipe
		 */
		public CoffeeIOPipes(int bufferSize)
		{
			this(bufferSize, null);
		}

		/**
		 * Returns the first pipe
		 * @return the left pipe
		 */
		public CoffeeIOPipe getLeftPipe()
		{
			return leftPipe;
		}

		/**
		 * Returns the second pipe
		 * @return the right pipe
		 */
		public CoffeeIOPipe getRightPipe()
		{
			return rightPipe;
		}

		/**
		 * Closes all pipes.
		 */
		public void close()
		{
			leftPipe.shutdownInput();
			leftPipe.shutdownOutput();
			rightPipe.shutdownInput();
			rightPipe.shutdownOutput();
		}
		
	}
}
