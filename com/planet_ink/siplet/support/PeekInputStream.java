package com.planet_ink.siplet.support;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class PeekInputStream extends InputStream
{
	private final InputStream	in;
	private final int[]			peekBuf;
	private final int			peekSize;
	private final AtomicInteger	peekRead	= new AtomicInteger(0);
	private final AtomicInteger	peekWrite	= new AtomicInteger(0);

	public PeekInputStream(final InputStream in, final int peekBufSize)
	{
		this.in=in;
		this.peekSize=peekBufSize;
		this.peekBuf=new int[peekBufSize];
	}

	private void pushPeek(final int c)
	{
		synchronized(peekBuf)
		{
			if(c!=-1)
			{
				peekBuf[peekWrite.getAndIncrement()]=c;
				if(peekWrite.get()>=peekSize)
					peekWrite.set(0);
			}
		}
	}
	
	public int peek() throws IOException
	{
		if((peekWrite.get()==peekRead.get()-1)
		||((peekRead.get()==0)&&(peekWrite.get()==peekSize-1)))
			return -1;
		if(in.available()==0)
			return -1;
		int c=in.read();
		pushPeek(c);
		return c;
	}
	
	@Override
	public int read() throws IOException
	{
		synchronized(peekBuf)
		{
			if(peekRead.get()!=peekWrite.get())
			{
				final int c=peekBuf[peekRead.getAndIncrement()];
				if(peekRead.get()>=peekSize)
					peekRead.set(0);
				return c;
			}
		}
		final int c=in.read();
		return c;
	}

    @Override
	public int available() throws IOException 
    {
    	int ct;
    	final int pwrite=peekWrite.get();
    	final int pread=peekRead.get();
		if(pwrite>=pread)
			ct=pwrite-pread;
		else
			ct=(peekSize-pread)+pwrite;
		final int avail = ct + in.available();
		return avail;
    }

    @Override
	public void close() throws IOException 
	{
    	peekRead.set(peekWrite.get());
    	in.close();
	}
}
