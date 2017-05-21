package com.planet_ink.coffee_mud.core.intermud.i3.net;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

public class ListenThread extends Thread 
{
	private final ServerSocket listen;
	private final Vector<Socket> clients;

	public ListenThread(int port) throws java.io.IOException 
	{
		super("I3Listener@"+port);
		clients = new Vector<Socket>(10, 2);
		listen = new ServerSocket(port);
		setDaemon(true);
		start();
	}

	@Override
	public void run()
	{
		while( listen!=null && !listen.isClosed() )
		{
			Socket client;
			if(CMSecurity.isDisabled(DisFlag.I3))
			{
				clients.clear();
				CMLib.s_sleep(100);
				continue;
			}
			try
			{
				client = listen.accept();
				synchronized( clients )
				{
					clients.addElement(client);
				}
				if(CMSecurity.isDebugging(DbgFlag.I3))
					Log.debugOut("I3Connection: "+client.getRemoteSocketAddress());
				else
				if(clients.size()>100)
					Log.errOut("Excessive I3 connections: "+client.getRemoteSocketAddress());
			}
			catch( final java.io.IOException e )
			{
			}
		}
	}

	public void close()
	{
		try
		{
			if(listen!=null)
				listen.close();
			clients.clear();
			this.interrupt();
		}
		catch(final Exception e)
		{
		}
	}

	public Socket nextSocket()
	{
		Socket client;

		synchronized( clients )
		{
			if( clients.size() > 0 )
			{
				client = clients.elementAt(0);
				clients.removeElementAt(0);
			}
			else
			{
				client = null;
			}
		}
		return client;
	}
}
