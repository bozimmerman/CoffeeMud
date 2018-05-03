package com.planet_ink.coffee_mud.core.intermud.cm1;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMThreadFactory;
import com.planet_ink.coffee_mud.core.threads.CMThreadPoolExecutor;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
   Copyright 2010-2018 Bo Zimmerman

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
	private String		 name = "";
	private int 		 port = 27755;
	private boolean 	 shutdownRequested = false;
	private boolean 	 isShutdown = false;
	private Selector	 servSelector = null;
	private final String iniFile;
	private CMProps 	 page;
	private ServerSocketChannel
						 servChan = null;
	private final SHashtable<SocketChannel,RequestHandler>
						 handlers = new SHashtable<SocketChannel,RequestHandler>();

	public CM1Server(String serverName, String iniFile)
	{
		super(serverName);
		if(!loadPropPage(iniFile))
			throw new IllegalArgumentException();
		final int serverPort = page.getInt("PORT");
		this.iniFile=iniFile;
		name=serverName+"@"+serverPort;
		setName(name);
		port=serverPort;
		shutdownRequested = false;
	}

	public String getINIFilename() 
	{ 
		return iniFile;
	}

	protected boolean loadPropPage(String iniFile)
	{
		if (page==null || !page.isLoaded())
		{
			page=new CMProps (iniFile);
			if(!page.isLoaded())
			{
				Log.errOut(getName(),"failed to load " + iniFile);
				return false;
			}
		}
		return true;
	}

	@Override
	public void run()
	{
		while(!shutdownRequested)
		{
			try
			{
				servChan = ServerSocketChannel.open();
				final ServerSocket serverSocket = servChan.socket();
				servSelector = Selector.open();
				if((page.getStr("BIND")!=null)&&(page.getStr("BIND").trim().length()>0))
					serverSocket.bind (new InetSocketAddress(InetAddress.getByName(page.getStr("BIND")),port));
				else
					serverSocket.bind (new InetSocketAddress (port));
				Log.sysOut("Started "+name+" on port "+port);
				servChan.configureBlocking (false);
				servChan.register (servSelector, SelectionKey.OP_ACCEPT);
			}
			catch(final IOException e)
			{
				Log.errOut(e);
				Log.errOut("CM1Server failed to start.");
				shutdownRequested=true;
				break;
			}
			try
			{
				shutdownRequested = false;
				while (!shutdownRequested)
				{
					try
					{
						final int n = servSelector.select();
						if (n == 0) 
							continue;

						final Iterator<SelectionKey> it = servSelector.selectedKeys().iterator();
						while (it.hasNext())
						{
							final SelectionKey key = it.next();
							if (key.isAcceptable())
							{
								final ServerSocketChannel server = (ServerSocketChannel) key.channel();
								final SocketChannel channel = server.accept();
								if (channel != null)
								{
									final RequestHandler handler=new RequestHandler(channel,page.getInt("IDLETIMEOUTMINS"));
									channel.configureBlocking (false);
									channel.register (servSelector, SelectionKey.OP_READ, handler);
									handlers.put(channel,handler);
									handler.sendMsg("CONNECTED TO "+name.toUpperCase());
								}
								//sayHello (channel);
							}
							try
							{
								if (key.isReadable())
								{
									final RequestHandler handler = (RequestHandler)key.attachment();
									if((!handler.isRunning())&&(!handler.needsClosing()))
										CMLib.threads().executeRunnable(handler);
								}
							}
							finally
							{
								it.remove();
							}
						}
						for(final SocketChannel schan : handlers.keySet())
						{
							try
							{
								final RequestHandler handler=handlers.get(schan);
								if((handler!=null)&&(handler.needsClosing()))
									handler.shutdown();
							}
							catch(final Exception e)
							{
							}
						}
					}
					catch(final CancelledKeyException t)
					{
						// ignore
					}
				}
			}
			catch(final Exception t)
			{
				Log.errOut("CM1Server",t);
			}
			finally
			{
				if(servSelector != null)
				{
					try
					{
						servSelector.close();
					}
					catch(final Exception e)
					{
					}
				}
				if(servChan != null)
				{
					try
					{
						servChan.close();
					}
					catch(final Exception e)
					{
					}
				}
				for(final SocketChannel schan : handlers.keySet())
				{
					try
					{
						final RequestHandler handler=handlers.get(schan);
						if(handler!=null)
							handler.shutdown();
					}
					catch(final Exception e)
					{
					}
				}
				handlers.clear();
				Log.sysOut("CM1Server","Shutdown complete");
			}
		}
		isShutdown = true;
	}

	public void shutdown()
	{
		shutdownRequested = true;
		final long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (!isShutdown))
		{
			CMLib.s_sleep(1000);
			if(servSelector != null)
			{
				try 
				{
					servSelector.close();
				}
				catch(final Exception e)
				{
				}
			}
			CMLib.s_sleep(1000);
			if((servChan != null)&&(!isShutdown))
			{
				try 
				{
					servChan.close();
				}
				catch(final Exception e)
				{
				}
			}
			CMLib.s_sleep(1000);
			this.interrupt();
		}
	}
}
