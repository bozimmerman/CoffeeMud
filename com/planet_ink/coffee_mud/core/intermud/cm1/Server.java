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
import java.io.*;

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
public class Server extends Thread
{
	private int 			port = 27755;
	private String 			name = "CoffeeMud1";
	private SLinkedList<RequestHandler> handlers = new SLinkedList<RequestHandler>();
	private boolean 		shutdownRequested = false;
	private boolean 		isShutdown = false;
	private ServerSocket 	servSock = null;
	
	public Server(String serverName, int serverPort)
	{
		super("CM1:"+serverName+":"+serverPort);
		this.name=serverName;
		this.port=serverPort;
		shutdownRequested = false;
	}
	
	public void run()
	{
		while(!shutdownRequested)
		{
			try
			{
				servSock = new ServerSocket(port);
				while(!shutdownRequested)
				{
					Socket sock = servSock.accept();
					RequestHandler handler = null;
					try
					{
						handler = new RequestHandler(this,sock);
						handler.sendGreeting("Connected to "+name+", port "+port);
						handlers.add(handler);
						handler.start();
					}
					catch(IOException ioe)
					{
						Log.errOut("CM1Server",ioe.getMessage());
						if(handler!=null)
							handlers.remove(handler);
					}
				}
			}
			catch(Throwable t)
			{
				Log.errOut("CM1Server",t);
			}
			finally
			{
				if(servSock != null)
					try {servSock.close();}catch(Exception e){}
				for(RequestHandler handler : handlers)
					try{ handler.interrupt();}catch(Exception e){}
				handlers.clear();
				Log.sysOut("CM1Server is shutdown");
			}
		}
		isShutdown = true;
	}
	
	public void registerLostHandler(RequestHandler handler)
	{
		handlers.remove(handler);
	}
	
	public void shutdown(Session S)
	{
		shutdownRequested = true;
		long time = System.currentTimeMillis();
		while((System.currentTimeMillis()-time<30000) && (!isShutdown))
		{
			this.interrupt();
			try {Thread.sleep(1000);}catch(Exception e){}
			if((servSock != null)&&(!isShutdown))
				try {servSock.close();}catch(Exception e){}
			try {Thread.sleep(1000);}catch(Exception e){}
		}
	}
}
