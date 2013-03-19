package com.planet_ink.miniweb.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
	private static class FileEntry
	{
		public Object data;
		public Long startPos;
		public long endPos;
		public long length;
		public FileEntry(Object o, long length)
		{
			this.data=o;
			this.startPos=Long.valueOf(0);
			this.length=length;
			this.endPos=this.length;
		}
		public FileEntry(Object o, long start, long endPos)
		{
			this.data=o;
			this.startPos=Long.valueOf(start);
			this.endPos=endPos;
			this.length=endPos-start;
		}
	}
	
	private LinkedList<FileEntry> list;
	private LinkedList<Closeable> closers;
	private byte[]						   buffer=null;
	private int							   length=0;
	private long						   lastModifiedTime=0;
	
	public MWDataBuffers()
	{
		list=new LinkedList<FileEntry>();
		closers=new LinkedList<Closeable>();
		created();
	}
	public MWDataBuffers(final ByteBuffer buf, final long lastModifiedTime)
	{
		this();
		add(buf, lastModifiedTime);
		created();
	}
	public MWDataBuffers(final byte[] buf, final long lastModifiedTime)
	{
		this();
		add(buf, lastModifiedTime);
		created();
	}
	public MWDataBuffers(final RandomAccessFile file, final long lastModifiedTime)
	{
		this();
		add(file, lastModifiedTime);
		created();
	}
	
	@SuppressWarnings("resource")
	private ByteBuffer tryNext()
	{
		if(list.size()==0)
			return null;
		accessed();
		FileEntry p=list.getFirst();
		final Object o=p.data;
		if(o instanceof ByteBuffer)
		{
			ByteBuffer b=(ByteBuffer)o;
			if(p.startPos!=null)
			{
				b.rewind();
				if(p.endPos<b.capacity())
					b.limit((int)p.endPos);
				else
					b.limit(b.capacity());
				b.position(p.startPos.intValue());
				p.startPos=null;
			}
			if(b.hasRemaining())
				return b;
			length-=p.length;
			list.removeFirst();
			return tryNext();
		}
		else
		if(o instanceof RandomAccessFile)
		{
			RandomAccessFile r=(RandomAccessFile)o;
			if(p.length<=0)
			{
				closers.add(r);
				list.removeFirst();
				return tryNext();
			}
			if(p.startPos!=null)
			{
				try { r.seek(p.startPos.longValue()); } catch(Exception e){}
				p.startPos=null;
			}
			if(buffer==null)
				buffer=new byte[512768];
			try {
				int len=buffer.length;
				if(len > p.length)
					len=(int)p.length;
				int amountRead=r.read(buffer,0,len);
				if(amountRead<0)
					throw new java.io.IOException();
				p.length=p.length-amountRead;
				ByteBuffer b=ByteBuffer.wrap(buffer,0,amountRead);
				if(p.length<=0)
				{
					closers.add(r);
					list.removeFirst();
				}
				list.addFirst(new FileEntry(b,b.limit()));
				return b;
			} catch (IOException e) {
				closers.add(r);
				length-=p.length;
				list.removeFirst();
				return tryNext();
			}
		}
		else
		{
			list.removeFirst();
			return tryNext();
		}
	}

	@Override
	public boolean hasNext() { return tryNext()!=null;}

	@Override
	public ByteBuffer next() { return tryNext(); }

	@Override
	public void remove() { throw new java.lang.NoSuchMethodError(); }

	private volatile StackTraceElement[] createdStackTrace=null;
	private volatile StackTraceElement[] lastStackTrace=null;
	private final void created() { createdStackTrace=Thread.currentThread().getStackTrace(); }
	private final void accessed() { lastStackTrace=Thread.currentThread().getStackTrace(); }
	private final void finalized() { 
		if(this.length>0) {
			System.err.println("^^^^^^^^^^^^^^^^^^^^^^^\n\rMWDataBuffer Not Closed!\n\r"+this.length+" bytes stranded.\n\rFirst stack trace:");
			if(createdStackTrace!=null) for(StackTraceElement f : createdStackTrace) System.err.println("  "+f.toString());
			System.err.println("Last stack trace:"); 
			if(lastStackTrace!=null) for(StackTraceElement f : lastStackTrace) System.err.println(f.toString());
			System.err.println("VVVVVVVVVVVVVVVVVVVVVVV");
		}
	}
	
	public void finalize() throws Throwable
	{
		if(list.size()>0)
		{
			finalized();
			close();
			System.err.println("MWDataBuffer Not Closed!");
		}
		super.finalize();
	}
	
	@Override
	public void close()
	{
		for(Closeable o : closers)
			try{ ((Closeable)o).close(); } catch(Exception e){}
		for(Object o : list)
			if(o instanceof Closeable)
				try{ ((Closeable)o).close(); } catch(Exception e){}
		list.clear();
		length=0;
	}
	
	@Override
	public void add(final ByteBuffer buf, final long lastModifiedTime)
	{
		list.add(new FileEntry(buf,buf.limit()-buf.position()));
		length += buf.limit() - buf.position();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final byte[] buf, final long lastModifiedTime)
	{
		list.add(new FileEntry(ByteBuffer.wrap(buf),buf.length));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final RandomAccessFile file, final long lastModifiedTime)
	{
		try {
			list.add(new FileEntry(file,file.length()));
			this.length += file.length();
			if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
				this.lastModifiedTime=lastModifiedTime;
		}
		catch(IOException e){ }
	}
	@Override
	public void insertTop(final RandomAccessFile file, final long lastModifiedTime)
	{
		try {
			list.addFirst(new FileEntry(file,file.length()));
			this.length += file.length();
			if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
				this.lastModifiedTime=lastModifiedTime;
		}
		catch(IOException e){ }
	}
	@Override
	public void insertTop(final ByteBuffer buf, final long lastModifiedTime)
	{
		list.addFirst(new FileEntry(buf,buf.limit()-buf.position()));
		length += buf.limit()-buf.position();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void insertTop(final byte[] buf, final long lastModifiedTime)
	{
		list.addFirst(new FileEntry(ByteBuffer.wrap(buf),buf.length));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	
	public void addFlush(final MWDataBuffers buffers)
	{
		this.length += buffers.getLength();
		for(FileEntry o : buffers.list)
			list.add(o);
		buffers.list.clear();
		buffers.length=0;
		if((buffers.lastModifiedTime > this.lastModifiedTime) && (buffers.lastModifiedTime > 0))
			this.lastModifiedTime=buffers.lastModifiedTime;
	}

	@Override
	public ByteBuffer flushToBuffer()
	{
		if((list.size()==1)&&(list.getFirst().data instanceof ByteBuffer))
		{
			ByteBuffer b=(ByteBuffer)list.getFirst().data;
			close();
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
	public void setRanges(final List<int[]> ranges)
	{
		final LinkedList<FileEntry> ranged=new LinkedList<FileEntry>();
		final HashSet<Object> usedObjs=new HashSet<Object>();
		long newLen=0;
		for(int[] range : ranges)
		{
			long pos=0;
			Object startObj=null;
			for(FileEntry p : list)
			{
				if((startObj==null)&&(pos+p.length>=range[0]))
				{
					startObj=p.data;
					Long startPos=Long.valueOf(range[0]-pos);
					usedObjs.add(p.data);
					if(pos+p.endPos>=range[1])
					{
						Long endPos=Long.valueOf(range[1]-pos);
						ranged.add(new FileEntry(p.data,startPos.longValue(),endPos.longValue()));
						newLen=endPos.longValue()-startPos.longValue();
						break; // totally done with this range iteration
					}
					else
						ranged.add(new FileEntry(p.data,startPos.longValue(),p.length-startPos.longValue()));
					newLen=p.length-startPos.longValue();
					pos+=p.length;
				}
				else
				if(startObj==null)
				{
					pos+=p.length;
				}
				else
				if(pos+p.endPos>=range[1])
				{
					Long endPos=Long.valueOf(range[1]-pos);
					newLen+=endPos.longValue();
					usedObjs.add(p.data);
					ranged.add(new FileEntry(p.data,0,endPos.longValue()));
					break;
				}
				else
				{
					ranged.add(p);
					usedObjs.add(p.data);
					newLen+=p.length;
				}
			}
		}
		for(FileEntry p : list)
			if((p.data instanceof Closeable) && (!usedObjs.contains(p.data)))
				try{ ((Closeable)p.data).close(); } catch(IOException e){}
		this.length=(int)newLen;
		list=ranged;
	}
}
