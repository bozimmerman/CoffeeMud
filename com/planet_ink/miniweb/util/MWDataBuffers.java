package com.planet_ink.miniweb.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import com.planet_ink.miniweb.interfaces.DataBuffers;
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

public class MWDataBuffers implements DataBuffers 
{
	private final LinkedList<Pair<Object,Long>>	list;
	private byte[]			 					buffer=null;
	private int									length=0;
	private long								lastModifiedTime=0;
	//TODO: Remove/Comment out this stuff after the leak is found.
	private volatile StackTraceElement[]		lastStackTrace=new StackTraceElement[0];
	private volatile StackTraceElement[]		createdStackTrace=new StackTraceElement[0];
	private volatile StackTraceElement[]		closedStackTrace=new StackTraceElement[0];
	
	public MWDataBuffers()
	{
		list=new LinkedList<Pair<Object,Long>>();
		createdStackTrace=Thread.currentThread().getStackTrace();
	}
	public MWDataBuffers(final ByteBuffer buf, final long lastModifiedTime)
	{
		this();
		add(buf, lastModifiedTime);
		createdStackTrace=Thread.currentThread().getStackTrace();
	}
	public MWDataBuffers(final byte[] buf, final long lastModifiedTime)
	{
		this();
		add(buf, lastModifiedTime);
		createdStackTrace=Thread.currentThread().getStackTrace();
	}
	public MWDataBuffers(final InputStream stream, final int length, final long lastModifiedTime)
	{
		this();
		add(stream, length, lastModifiedTime);
		createdStackTrace=Thread.currentThread().getStackTrace();
	}
	
	@SuppressWarnings("resource")
	private ByteBuffer tryNext()
	{
		if(list.size()==0)
			return null;
		lastStackTrace=Thread.currentThread().getStackTrace();
		Pair<Object,Long> p=list.getFirst();
		final Object o=p.first;
		if(o instanceof ByteBuffer)
		{
			ByteBuffer b=(ByteBuffer)o;
			if(b.hasRemaining())
				return b;
			length-=b.limit();
			list.removeFirst();
			return tryNext();
		}
		else
		{
			if(p.second.longValue()<=0)
			{
				try { ((InputStream)o).close(); } catch (IOException e1) {}
				list.removeFirst();
				return tryNext();
			}
			else
			{
				InputStream i=(InputStream)o;
				if(buffer==null)
					buffer=new byte[512768];
				try {
					int len=buffer.length;
					if(len > p.second.longValue())
						len=(int)p.second.longValue();
					int amountRead=i.read(buffer,0,len);
					if(amountRead<0)
						throw new java.io.IOException();
					p.second=Long.valueOf(p.second.longValue()-amountRead);
					ByteBuffer b=ByteBuffer.wrap(buffer,0,amountRead);
					if(p.second.longValue()<=0)
					{
						try { ((InputStream)o).close(); } catch (IOException e1) {}
						list.removeFirst();
					}
					list.addFirst(new Pair<Object,Long>(b,Long.valueOf(b.limit())));
					return b;
				} catch (IOException e) {
					try { i.close(); } catch (IOException e1) {}
					length-=p.second.longValue();
					list.removeFirst();
					return tryNext();
				}
			}
		}
	}

	@Override
	public boolean hasNext() { return tryNext()!=null;}

	@Override
	public ByteBuffer next() { return tryNext(); }

	@Override
	public void remove() { throw new java.lang.NoSuchMethodError(); }

	public void finalize() throws Throwable
	{
		if(list.size()>0)
		{
			close();
			System.err.println("^^^^^^^^^^^^^^^^^^^^^^^");
			System.err.println("MWDataBuffer Not Closed!");
			System.err.println("First stack trace:");
			if(createdStackTrace!=null)
			for(StackTraceElement f : createdStackTrace)
				System.err.println("  "+f.toString());
			System.err.println("Last stack trace:");
			if(lastStackTrace!=null)
			for(StackTraceElement f : lastStackTrace)
				System.err.println(f.toString());
			System.err.println("Closed stack trace:");
			if(closedStackTrace!=null)
			for(StackTraceElement f : closedStackTrace)
				System.err.println(f.toString());
			System.err.println("VVVVVVVVVVVVVVVVVVVVVVV");
		}
		super.finalize();
	}
	
	@Override
	public void close()
	{
		closedStackTrace=Thread.currentThread().getStackTrace();
		for(Object o : list)
			if(o instanceof InputStream)
				try{ ((InputStream)o).close(); } catch(Exception e){}
		list.clear();
		length=0;
	}
	
	@Override
	public void add(final ByteBuffer buf, final long lastModifiedTime)
	{
		list.add(new Pair<Object,Long>(buf,Long.valueOf(buf.limit())));
		length += buf.limit();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final byte[] buf, final long lastModifiedTime)
	{
		list.add(new Pair<Object,Long>(ByteBuffer.wrap(buf),Long.valueOf(buf.length)));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final InputStream stream, final int length, final long lastModifiedTime)
	{
		list.add(new Pair<Object,Long>(stream,Long.valueOf(length)));
		this.length += length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void insertTop(final ByteBuffer buf, final long lastModifiedTime)
	{
		list.addFirst(new Pair<Object,Long>(buf,Long.valueOf(buf.limit())));
		length += buf.limit();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void insertTop(final byte[] buf, final long lastModifiedTime)
	{
		list.addFirst(new Pair<Object,Long>(ByteBuffer.wrap(buf),Long.valueOf(buf.length)));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void insertTop(final InputStream stream, final int length, final long lastModifiedTime)
	{
		list.addFirst(new Pair<Object,Long>(stream,Long.valueOf(length)));
		this.length += length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	
	public void addFlush(final MWDataBuffers buffers)
	{
		this.length += buffers.getLength();
		for(Pair<Object,Long> o : buffers.list)
			list.add(o);
		buffers.list.clear();
		buffers.length=0;
		if((buffers.lastModifiedTime > this.lastModifiedTime) && (buffers.lastModifiedTime > 0))
			this.lastModifiedTime=buffers.lastModifiedTime;
	}

	@Override
	public ByteBuffer flushToBuffer()
	{
		if((list.size()==1)&&(list.getFirst().first instanceof ByteBuffer))
		{
			ByteBuffer b=(ByteBuffer)list.getFirst().first;
			list.clear();
			length=0;
			return b;
		}
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		while(hasNext())
		{
			ByteBuffer buf=next();
			if(buf.hasRemaining())
			{
				bout.write(buf.array(),buf.position(),buf.limit());
				buf.position(buf.limit());
			}
		}
		close();
		return ByteBuffer.wrap(bout.toByteArray());
	}
	
	@Override
	public int getLength(){ return length; }
	
	@Override
	public Date getLastModified()
	{
		if(this.lastModifiedTime==0)
			return new Date();
		return new Date(this.lastModifiedTime); 
	}

	@Override
	public void skip(long to)
	{ 
		if(to>=length)
		{
			close();
			return;
		}
		long basePosition=0;
		while(hasNext() && (basePosition<to))
		{
			ByteBuffer buf=next();
			if((basePosition+(long)buf.remaining())<=to)
			{
				basePosition+=(long)buf.remaining();
				buf.position(buf.limit());
			}
			else
			{
				buf.position((int)(buf.position()+(to-basePosition)));
				break;
			}
		}
	}
	
	@Override
	public void limit(long at)
	{ 
		if(at>=length)
			return;
		long amount=length-at;
		for(Iterator<Pair<Object,Long>> i=list.descendingIterator();i.hasNext() && (amount>0);)
		{
			Pair<Object,Long> p=i.next();
			if(p.second.longValue()<=amount)
			{
				if(p.first instanceof InputStream)
					try { ((InputStream)p.first).close(); } catch (IOException e1) {}
				i.remove();
				length-=p.second.longValue();
				amount-=p.second.longValue();
			}
			else
			{
				p.second=Long.valueOf(p.second.longValue()-amount);
				length-=amount;
				if(p.first instanceof ByteBuffer)
					((ByteBuffer)p.first).limit(((ByteBuffer)p.first).limit()-(int)amount);
				amount=0;
			}
		}
	}
}
