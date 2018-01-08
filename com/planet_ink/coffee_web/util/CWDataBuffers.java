package com.planet_ink.coffee_web.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.planet_ink.coffee_web.interfaces.DataBuffers;
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

public class CWDataBuffers implements DataBuffers 
{
	private static final byte[] EOLNBYTES = "\r\n".getBytes();
	private static final byte[] EOCHUNKBYTES = "0\r\n\r\n".getBytes();
	
	private static class FileEntry
	{
		public final Object data;
		public Long startPos;
		public final long endPos;
		public long length;
		public final boolean isChunkable;
		public FileEntry(Object o, long start, long endPos, boolean isChunkable)
		{
			this.data=o;
			this.startPos=Long.valueOf(start);
			this.endPos=endPos;
			this.length=endPos-start;
			this.isChunkable=isChunkable;
		}
	}
	
	private LinkedList<FileEntry> 		list;
	private final LinkedList<Closeable> closers;
	private byte[]						buffer=null;
	private long						length=0;
	private int							chunkSize;
	private boolean						chunkFixed = false;
	private long						lastModifiedTime=0;
	
	public CWDataBuffers()
	{
		list=new LinkedList<FileEntry>();
		closers=new LinkedList<Closeable>();
		created();
	}
	public CWDataBuffers(final ByteBuffer buf, final long lastModifiedTime, final boolean isChunkable)
	{
		this();
		add(buf, lastModifiedTime, isChunkable);
		created();
	}
	public CWDataBuffers(final byte[] buf, final long lastModifiedTime, final boolean isChunkable)
	{
		this();
		add(buf, lastModifiedTime, isChunkable);
		created();
	}
	public CWDataBuffers(final RandomAccessFile file, final long lastModifiedTime, final boolean isChunkable)
	{
		this();
		add(file, lastModifiedTime, isChunkable);
		created();
	}
	
	private ByteBuffer doChunk(ByteBuffer topBuffer)
	{
		if(!chunkFixed)
		{
			chunkFixed = true;
			list.add(new FileEntry(ByteBuffer.wrap(EOCHUNKBYTES),0,EOCHUNKBYTES.length,false));
		}
		if(topBuffer.remaining() <= chunkSize)
		{
			list.removeFirst();
			final String sizeStr = Integer.toHexString(topBuffer.remaining()); 
			final ByteBuffer newBuf=ByteBuffer.allocate(topBuffer.remaining()+ sizeStr.length() + EOLNBYTES.length + EOLNBYTES.length);
			newBuf.put(sizeStr.getBytes());
			newBuf.put(EOLNBYTES);
			newBuf.put(topBuffer);
			newBuf.put(EOLNBYTES);
			newBuf.clear();
			list.addFirst(new FileEntry(newBuf,0,newBuf.limit(),false));
			return newBuf;
		}
		else
		{
			final String sizeStr = Integer.toHexString(chunkSize); 
			final ByteBuffer newBuf=ByteBuffer.allocate(chunkSize + sizeStr.length() + EOLNBYTES.length + EOLNBYTES.length);
			final byte[] readBuf = new byte[chunkSize];
			topBuffer.get(readBuf);
			newBuf.put(sizeStr.getBytes());
			newBuf.put(EOLNBYTES);
			newBuf.put(readBuf);
			newBuf.put(EOLNBYTES);
			newBuf.clear();
			list.addFirst(new FileEntry(newBuf,0,newBuf.limit(),false));
			return newBuf;
		}
	}
	
	@SuppressWarnings("resource")
	private ByteBuffer tryNext()
	{
		if(list.size()==0)
			return null;
		accessed();
		final FileEntry p=list.getFirst();
		final Object o=p.data;
		if(o instanceof ByteBuffer)
		{
			final ByteBuffer b=(ByteBuffer)o;
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
			{
				if(p.isChunkable && (chunkSize>0))
					return doChunk(b);
				return b;
			}
			length-=p.length;
			list.removeFirst();
			return tryNext();
		}
		else
		if(o instanceof RandomAccessFile)
		{
			final RandomAccessFile r=(RandomAccessFile)o;
			if(p.length<=0)
			{
				closers.add(r);
				list.removeFirst();
				return tryNext();
			}
			if(p.startPos!=null)
			{
				try { r.seek(p.startPos.longValue()); } catch(final Exception e){}
				p.startPos=null;
			}
			if(buffer==null)
				buffer=new byte[512768];
			try
			{
				int len=buffer.length;
				if(len > p.length)
					len=(int)p.length;
				final int amountRead=r.read(buffer,0,len);
				if(amountRead<0)
					throw new java.io.IOException();
				p.length=p.length-amountRead;
				final ByteBuffer b=ByteBuffer.wrap(buffer,0,amountRead);
				if(p.length<=0)
				{
					closers.add(r);
					list.removeFirst();
				}
				list.addFirst(new FileEntry(b,0,b.limit(),p.isChunkable));
				if(p.isChunkable && (chunkSize>0))
					return doChunk(b);
				return b;
			}
			catch (final IOException e)
			{
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

	@Override public boolean hasNext() { return tryNext()!=null;}

	@Override public ByteBuffer next() { return tryNext(); }

	@Override public void remove() { throw new java.lang.NoSuchMethodError(); }

	private final void created() { }
	private final void accessed() { }
	private final void finalized() { }
	public void finalize() throws Throwable
	{
		if(list.size()>0)
		{
			finalized();
			close();
			System.err.println("DataBuffer Not Closed!");
		}
		super.finalize();
	}
	
	@Override
	public void close()
	{
		for(final Closeable o : closers)
			try{ o.close(); } catch(final Exception e){}
		for(final Object o : list)
			if(o instanceof Closeable)
				try{ ((Closeable)o).close(); } catch(final Exception e){}
		list.clear();
		length=0;
	}
	
	@Override
	public void add(final ByteBuffer buf, final long lastModifiedTime, final boolean isChunkable)
	{
		list.add(new FileEntry(buf,0,buf.limit()-buf.position(),isChunkable));
		length += buf.limit() - buf.position();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final byte[] buf, final long lastModifiedTime, final boolean isChunkable)
	{
		list.add(new FileEntry(ByteBuffer.wrap(buf),0,buf.length,isChunkable));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void add(final RandomAccessFile file, final long lastModifiedTime, final boolean isChunkable)
	{
		try
		{
			list.add(new FileEntry(file,0,file.length(),isChunkable));
			this.length += file.length();
			if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
				this.lastModifiedTime=lastModifiedTime;
		}
		catch(final IOException e){ }
	}
	@Override
	public void insertTop(final RandomAccessFile file, final long lastModifiedTime, final boolean isChunkable)
	{
		try
		{
			list.addFirst(new FileEntry(file,0,file.length(),isChunkable));
			this.length += file.length();
			if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
				this.lastModifiedTime=lastModifiedTime;
		}
		catch(final IOException e){ }
	}
	
	@Override
	public ByteBuffer splitTopBuffer(final int sizeOfTopBuffer)
	{
		if((list.size()==0) || (sizeOfTopBuffer <= 0))
			throw new IllegalArgumentException();
		final boolean isChunkable=list.get(0).isChunkable;
		ByteBuffer topB = next();
		if(topB.remaining() < sizeOfTopBuffer)
			throw new IllegalArgumentException();
		final byte[] newBytes = new byte[sizeOfTopBuffer];
		topB.get(newBytes);
		ByteBuffer newTopB=ByteBuffer.wrap(newBytes);
		list.addFirst(new FileEntry(newTopB,0,newTopB.limit()-newTopB.position(),isChunkable));
		return next();
	}

	@Override
	public void insertTop(final ByteBuffer buf, final long lastModifiedTime, final boolean isChunkable)
	{
		list.addFirst(new FileEntry(buf,0,buf.limit()-buf.position(),isChunkable));
		length += buf.limit()-buf.position();
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	@Override
	public void insertTop(final byte[] buf, final long lastModifiedTime, final boolean isChunkable)
	{
		list.addFirst(new FileEntry(ByteBuffer.wrap(buf),0,buf.length,isChunkable));
		length += buf.length;
		if((lastModifiedTime != this.lastModifiedTime) && (lastModifiedTime > 0))
			this.lastModifiedTime=lastModifiedTime;
	}
	
	public void addFlush(final CWDataBuffers buffers)
	{
		this.length += buffers.getLength();
		for(final FileEntry o : buffers.list)
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
			final ByteBuffer b=(ByteBuffer)list.getFirst().data;
			close();
			return b;
		}
		final ByteArrayOutputStream bout=new ByteArrayOutputStream();
		while(hasNext())
		{
			final ByteBuffer buf=next();
			if(buf.hasRemaining())
			{
				bout.write(buf.array(),buf.position(),buf.limit());
				buf.position(buf.limit());
			}
		}
		close();
		return ByteBuffer.wrap(bout.toByteArray());
	}
	
	@Override public long getLength(){ return length; }
	
	@Override
	public Date getLastModified()
	{
		if(this.lastModifiedTime==0)
			return new Date();
		return new Date(this.lastModifiedTime); 
	}
	
	@Override
	public void setRanges(final List<long[]> ranges)
	{
		final LinkedList<FileEntry> ranged=new LinkedList<FileEntry>();
		final HashSet<Object> usedObjs=new HashSet<Object>();
		long newLen=0;
		for(final long[] range : ranges)
		{
			long pos=0;
			Object startObj=null;
			for(final FileEntry p : list)
			{
				if((startObj==null)&&(pos+p.length>=range[0]))
				{
					startObj=p.data;
					final Long startPos=Long.valueOf(range[0]-pos);
					usedObjs.add(p.data);
					if(pos+p.endPos>=range[1])
					{
						final Long endPos=Long.valueOf(range[1]-pos);
						ranged.add(new FileEntry(p.data,startPos.longValue(),endPos.longValue(),false));
						newLen=endPos.longValue()-startPos.longValue();
						break; // totally done with this range iteration
					}
					else
						ranged.add(new FileEntry(p.data,startPos.longValue(),p.length-startPos.longValue(),false));
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
					final Long endPos=Long.valueOf(range[1]-pos);
					newLen+=endPos.longValue();
					usedObjs.add(p.data);
					ranged.add(new FileEntry(p.data,0,endPos.longValue(),false));
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
		for(final FileEntry p : list)
			if((p.data instanceof Closeable) && (!usedObjs.contains(p.data)))
				try{ ((Closeable)p.data).close(); } catch(final IOException e){}
		this.length=newLen;
		list=ranged;
	}
	
	@Override
	public void setChunked(final int chunkSize)
	{
		this.chunkSize = chunkSize;
	}
}
