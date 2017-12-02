package com.planet_ink.coffee_mud.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/*
Copyright 2005-2017 Bo Zimmerman

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
	private final int[]			buffer;
	private final AtomicInteger	writeDex	= new AtomicInteger(0);
	private final AtomicInteger	readDex		= new AtomicInteger(0);
	private InputStream			inStream;
	private final OutputStream	outStream;

	public CoffeeIOPipe(int bufferSize)
	{
		this.buffer = new int[bufferSize];
		inStream	= new CMInputStream();
		outStream	= new CMOutputStream();
	}

	private class CMInputStream extends InputStream
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

	private class CMOutputStream extends OutputStream
	{
		private boolean closed = false;
		
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
		}
		
	    @Override
	    public void close()
	    {
	    	closed=true;
	    }
	}
	
	public InputStream getInputStream()
	{
		return inStream;
	}
	
	public OutputStream getOutputStream()
	{
		return outStream;
	}

	public void shutdownInput()
	{
		try
		{
			inStream.close();
		}
		catch (IOException e)
		{
		}
	}
	
	public void shutdownOutput()
	{
		try
		{
			outStream.close();
		}
		catch (IOException e)
		{
		}
	}
	
	public void close()
	{
		shutdownInput();
		shutdownOutput();
	}
	
	public static class CoffeeIOPipes
	{
		private final CoffeeIOPipe	leftPipe;
		private final CoffeeIOPipe	rightPipe;

		public CoffeeIOPipes(int bufferSize)
		{
			this.leftPipe=new CoffeeIOPipe(bufferSize/2);
			this.rightPipe=new CoffeeIOPipe(bufferSize/2);
			InputStream lin = this.leftPipe.inStream;
			this.leftPipe.inStream = this.rightPipe.inStream;
			this.rightPipe.inStream = lin;
		}
		
		public CoffeeIOPipe getLeftPipe()
		{
			return leftPipe;
		}
		
		public CoffeeIOPipe getRightPipe()
		{
			return rightPipe;
		}
		
		public void close()
		{
			leftPipe.shutdownInput();
			leftPipe.shutdownOutput();
			rightPipe.shutdownInput();
			rightPipe.shutdownOutput();
		}
		
	}
	
}
