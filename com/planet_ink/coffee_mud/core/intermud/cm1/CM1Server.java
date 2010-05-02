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
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

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
public class CM1Server extends Thread
{
	private String		name = "";
	private int 		port = 27755;
	private boolean 	shutdownRequested = false;
	private boolean 	isShutdown = false;
	private Selector	servSelector = null;
	private ServerSocketChannel	
						servChan = null;
	private SHashtable<SocketChannel,RequestHandler> 
						handlers = new SHashtable<SocketChannel,RequestHandler>();
	private ThreadPoolExecutor 
						threadPool = new ThreadPoolExecutor(3, 3, 30, TimeUnit.SECONDS, new UniqueEntryBlockingQueue<Runnable>(256));
	
	
	public CM1Server(String serverName, int serverPort)
	{
		super("CM1:"+serverName+":"+serverPort);
		name=serverName;
		this.port=serverPort;
		shutdownRequested = false;
	}
	
	public void run()
	{
		while(!shutdownRequested)
		{
			try
			{
				servChan = ServerSocketChannel.open();
				ServerSocket serverSocket = servChan.socket();
				servSelector = Selector.open();
				serverSocket.bind (new InetSocketAddress (port));
				Log.sysOut("CM1Server","Started "+name+" on port "+port);
				servChan.configureBlocking (false);
				servChan.register (servSelector, SelectionKey.OP_ACCEPT);
				while (!shutdownRequested)
				{
				   int n = servSelector.select();
				   if (n == 0) continue;
				   
				   Iterator<SelectionKey> it = servSelector.selectedKeys().iterator();
				   while (it.hasNext()) 
				   {
				      SelectionKey key = it.next();
				      if (key.isAcceptable()) 
				      {
				         ServerSocketChannel server = (ServerSocketChannel) key.channel();
				         SocketChannel channel = server.accept();
				         if (channel != null) 
				         {
				            channel.configureBlocking (false);
				            channel.register (servSelector, SelectionKey.OP_READ);
				            RequestHandler handler=new RequestHandler(channel);
				            handlers.put(channel,handler);
					        handler.sendMsg("CONNECTED TO "+name.toUpperCase());
				         } 
				         //sayHello (channel);
				      }
				      if (key.isReadable()) 
				      {
				    	  RequestHandler handler = handlers.get(key.channel());
				  		  threadPool.execute(handler);
				      }
				      it.remove();
				    }
					for(SocketChannel schan : handlers.keySet())
						try
						{
							RequestHandler handler=handlers.get(schan);
							if((handler!=null)&&(handler.needsClosing()))
								handler.shutdown();
						}
						catch(Exception e){}
				}
			}
			catch(Throwable t)
			{
				Log.errOut("CM1Server",t);
			}
			finally
			{
				if(servSelector != null)
					try {servSelector.close();}catch(Exception e){}
				if(servChan != null)
					try {servChan.close();}catch(Exception e){}
				for(SocketChannel schan : handlers.keySet())
					try
					{
						RequestHandler handler=handlers.get(schan);
						if(handler!=null)handler.shutdown();
					}
					catch(Exception e){}
				handlers.clear();
				threadPool.shutdown();
				Log.sysOut("CM1Server is shutdown");
			}
		}
		isShutdown = true;
	}
	
	public void shutdown()
	{
		shutdownRequested = true;
		threadPool.shutdown();
		long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (!isShutdown))
		{
			this.interrupt();
			try {Thread.sleep(1000);}catch(Exception e){}
			if(servSelector != null)
				try {servSelector.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
			if((servChan != null)&&(!isShutdown))
				try {servChan.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
		}
	}
}
