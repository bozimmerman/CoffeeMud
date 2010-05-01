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
	private final String runnableName;
	private boolean isRunning = false;
	private long idleTime = System.currentTimeMillis();
	private final SocketChannel chan;
	private SVector<ByteBuffer>	workingBuffers = new SVector<ByteBuffer>();
	private SVector<String> lines=new SVector<String>();
	private char eolChar = '\n';
	
	public RequestHandler(SocketChannel chan) throws IOException
	{
		super();
		runnableName="CM1ReqHndler#"+counter.incrementAndGet();
		this.chan=chan;
	}
	
	public void sendMsg(String msg) throws IOException 
	{
		byte[] bytes = (msg+"\n").getBytes();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		while(chan.isConnected() && chan.isOpen() && (chan.write(buf)>0))try{Thread.sleep(1);}catch(Exception e){}
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
	    		{
	    			//System.out.println(this.runnableName+": using new buffer");
	    			buffer = ByteBuffer.allocate(1024);
	    		}
	    		else
	    		{
	    			//System.out.println(this.runnableName+": using old buffer: "+buffer.limit());
	    			buffer.position(buffer.limit());
	    			buffer.limit(buffer.capacity());
	    		}
	    		while (chan.isConnected() && (chan.read (buffer) > 0)) 
	    		{
	    			buffer.flip();
	    			//System.out.println(this.runnableName+": buffer has "+buffer.limit()+" bytes now");
	    			for(int i=0;i<buffer.limit();i++)
	    				if(buffer.get(i)==eolChar)
	    				{
	    					//System.out.println(runnableName+": buffer has \\n at "+i);
	    					buffer.position(i);
	    					ByteBuffer lastBuffer=buffer;
	    					if(buffer.remaining()>0)
	    					{
	    						ByteBuffer nxtBuf = ByteBuffer.allocate(buffer.remaining());
	    						nxtBuf.put(buffer);
	    						nxtBuf.flip();
	    						//System.out.println(this.runnableName+": NEXT buffer has "+nxtBuf.limit()+" chars.");
	    						buffer = ByteBuffer.allocate(1024);
	    						buffer.put(nxtBuf);
	    						buffer.rewind();
	    						buffer.limit(i);
	    					}
	    					int fullSize = 0;
	    					for(ByteBuffer buf : workingBuffers)
	    						fullSize += buf.limit();
	    					//System.out.println(runnableName+": full size "+fullSize);
	    					ByteBuffer finalBuf=ByteBuffer.allocate(fullSize);
	    					for(ByteBuffer buf : workingBuffers)
	    					{
	    						buf.rewind();
	    						finalBuf.put(buf);
	    						workingBuffers.remove(buf);
	    						if(buf == lastBuffer)
	    							break;
	    					}
	    					finalBuf.flip();
	    					//System.out.println(runnableName+": finalBuf has "+finalBuf.limit()+"/"+finalBuf.capacity()+" bytes");
	    					lines.add(new String(finalBuf.array()));
	    					if(lastBuffer == buffer)
	    						buffer=ByteBuffer.allocate(1024);
	    				}
	    			if(!workingBuffers.contains(buffer))
	    				workingBuffers.add(buffer);
	    			if(buffer.limit()==buffer.capacity())
	    				buffer=ByteBuffer.allocate(1024);
	    			else
	    			{
	    				buffer.position(buffer.limit());
	    				buffer.limit(buffer.capacity());
	    			}
	    		}
	    		buffer.flip();
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
}
