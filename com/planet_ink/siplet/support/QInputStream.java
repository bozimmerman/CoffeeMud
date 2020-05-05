package com.planet_ink.siplet.support;

import java.io.*;
import java.util.*;
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
public class QInputStream extends InputStream
{
	private final int[]			queueBuf;
	private final int			queueSize;
	private final AtomicInteger	queueRead	= new AtomicInteger(0);
	private final AtomicInteger	queueWrite	= new AtomicInteger(0);

	public QInputStream(final int queueBufSize)
	{
		this.queueSize=queueBufSize;
		this.queueBuf=new int[queueBufSize];
	}

	public void queue(int c) throws IOException
	{
		synchronized(queueBuf)
		{
			if(c!=-1)
			{
				queueBuf[queueWrite.getAndIncrement()]=c;
				if(queueWrite.get()>=queueSize)
					queueWrite.set(0);
			}
		}
	}
	
	@Override
	public int read() throws IOException
	{
		synchronized(queueBuf)
		{
			if(queueRead.get()!=queueWrite.get())
			{
				final int c=queueBuf[queueRead.getAndIncrement()];
				if(queueRead.get()>=queueSize)
					queueRead.set(0);
				return c;
			}
		}
		return -1;
	}

    @Override
	public int available() throws IOException 
    {
    	int ct;
    	final int pwrite=queueWrite.get();
    	final int pread=queueRead.get();
		if(pwrite>=pread)
			ct=pwrite-pread;
		else
			ct=(queueSize-pread)+pwrite;
		return ct;
    }

    @Override
	public void close() throws IOException 
	{
    	queueRead.set(queueWrite.get());
	}
}
