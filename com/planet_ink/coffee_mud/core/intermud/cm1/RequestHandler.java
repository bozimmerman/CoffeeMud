package com.planet_ink.coffee_mud.core.intermud.cm1;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.atomic.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class RequestHandler implements Runnable
{
	private static AtomicInteger counter = new AtomicInteger();
	private final String 		 runnableName;
	private boolean 			 isRunning = false;
	private boolean 			 closeMe = false;
	private long 				 idleTime = System.currentTimeMillis();
	private final SocketChannel  chan;
	private SVector<ByteBuffer>	 workingBuffers = new SVector<ByteBuffer>();
	private byte[][]			 markBlocks = DEFAULT_MARK_BLOCKS;
	private static final int 	 BUFFER_SIZE=4096;
	private static final long 	 MAXIMUM_BYTES=1024 * 1024 * 2;
	private static final byte[][]DEFAULT_MARK_BLOCKS = {{'\n','\r'},{'\r','\n'},{'\n'},{'\r'}};
	
	public RequestHandler(SocketChannel chan) throws IOException
	{
		super();
		runnableName="CM1ReqHndler#"+counter.incrementAndGet();
		this.chan=chan;
	}
	
	public void sendMsg(String msg) throws IOException 
	{
		byte[] bytes = (msg+"\r\n").getBytes();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		while(chan.isConnected() && chan.isOpen() && (chan.write(buf)>0))try{Thread.sleep(1);}catch(Exception e){}
	}
	
	public void close()
	{
		closeMe=true;
		try {chan.close();}catch(Exception e){}
	}
	
	public void shutdown()
	{
		long time = System.currentTimeMillis();
		try {chan.close();}catch(Exception e){}
		while((System.currentTimeMillis()-time<30000) && (isRunning))
			try {Thread.sleep(1000);}catch(Exception e){}
	}

	public boolean needsClosing()
	{
		if(closeMe)
			return true;
		if((System.currentTimeMillis() - idleTime) > 10 * 60 * 1000)
			return true;
		if((!chan.isOpen()) || (!chan.isConnected()) || (!chan.isRegistered()))
			return true;
		return false;
	}
	
	public void run()
	{
		isRunning=true;
		synchronized(this)
		{
			try
			{
	    		ByteBuffer buffer = null;
	    		if(workingBuffers.size()>0)
	    			buffer=workingBuffers.lastElement();
	    		if((buffer==null)||(buffer.capacity()==buffer.limit()))
	    			buffer = ByteBuffer.allocate(BUFFER_SIZE);
	    		else
	    		{
	    			buffer.position(buffer.limit());
	    			buffer.limit(buffer.capacity());
	    		}
	    		while (chan.isConnected() && (chan.isOpen()) && (chan.read (buffer) > 0)) 
	    		{
	    			buffer.flip();
	    			int containIndex=-1;
	    			for(int i=0;i<buffer.limit();i++)
	    				if((containIndex=CMParms.containIndex(buffer, markBlocks, i))>=0)
	    				{
	    					workingBuffers.remove(buffer);
	    					if(i>0)
	    					{
	    						ByteBuffer prevBuf = ByteBuffer.allocate(BUFFER_SIZE);
	    						prevBuf.put(buffer.array(),0,i);
	    						prevBuf.flip();
	    						workingBuffers.add(prevBuf);
	    					}
	    					if(((buffer.position() + markBlocks[containIndex].length)>=buffer.limit())
	    					||((buffer.position() + markBlocks[containIndex].length)>=buffer.capacity()))
	    						buffer.position(buffer.limit());
	    					else
		    					buffer.position(i + markBlocks[containIndex].length);
	    					if(buffer.remaining()>0)
	    					{
	    						buffer = ByteBuffer.allocate(BUFFER_SIZE);
	    						buffer.put(buffer);
	    						i=-1;
	    					}
	    					else
	    						buffer = ByteBuffer.allocate(BUFFER_SIZE);
    						buffer.flip();
    						
	    					int fullSize = 0;
	    					for(ByteBuffer buf : workingBuffers)
	    						fullSize += buf.limit();
	    					ByteBuffer finalBuf=ByteBuffer.allocate(fullSize);
	    					for(ByteBuffer buf : workingBuffers)
	    					{
	    						buf.rewind();
	    						finalBuf.put(buf);
	    						workingBuffers.remove(buf);
	    					}
	    					finalBuf.flip();
	    					markBlocks=DEFAULT_MARK_BLOCKS;
	    					execute(new String(finalBuf.array()));
	    				}
	    			if(!workingBuffers.contains(buffer) && (buffer.limit()>0))
	    				workingBuffers.add(buffer);
	    			if(buffer.limit()==buffer.capacity())
	    				buffer=ByteBuffer.allocate(BUFFER_SIZE);
	    			else
	    			{
	    				buffer.position(buffer.limit());
	    				buffer.limit(buffer.capacity());
	    			}
	    			if (((long)BUFFER_SIZE * (long)workingBuffers.size())>MAXIMUM_BYTES)
	    			{
	    				workingBuffers.clear();
	    				shutdown();
	    				return;
	    			}
	    		}
	    		buffer.flip();
	    		try{Thread.sleep(1);}catch(Exception e){}
			}
			catch(Exception e)
			{
				Log.errOut("CM1Hndlr",runnableName+": "+e.getMessage());
				Log.errOut("CM1Hndlr",e);
			}
			finally
			{
				idleTime=System.currentTimeMillis();
				isRunning=false;
			}
		}
	}
	
	public void setEndOfLine(String... msgs)
	{
		byte[][] newBlocks=new byte[msgs.length][];
		int i=0;
		for(String s : msgs)
			newBlocks[i++]=s.getBytes();
		markBlocks=newBlocks;
	}
	
	public void execute(String line)
	{
		new CommandHandler(this,line).run();
	}
}
