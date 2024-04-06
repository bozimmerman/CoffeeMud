package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
/*
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
 * The mudlib interface to the server.
 */
/**
 * The Server class is the mudlib's interface to the
 * Imaginary Mud Server.  It is responsible with knowing all
 * internal information about the server.
 * @author George Reese, Bo Zimmerman
 * @version 1.0
 */
public class I3Router
{
	static private I3RouterThread routerThread = null;
	static private boolean started = false;

	/**
	 * Creates a server thread if one has not yet been
	 * created.
	 * @throws ServerSecurityException thrown if an attempt to call start()
	 * is made once the server is running.
	 * @param mud the name of the mud being started
	 * @param port the port of the server
	 * @param password the password for this server
	 * @param routersList peer list of host:port:service
	 * @param adminEmail email address of mud admin
	 */
	static public void start(final String mud,
							 final int port,
							 final String password,
							 final String[] routersList,
							 final String adminEmail) throws ServerSecurityException
	{
		try
		{
			if( started )
			{
				throw new ServerSecurityException("Illegal attempt to start Router.");
			}
			started = true;
			routerThread = new I3RouterThread(mud, port, password, routersList, adminEmail);
			Log.sysOut("I3Router", "InterMud3 Core (c)1996 George Reese");
			routerThread.start();
		}
		catch(final Exception e)
		{
			routerThread=null;
			Log.errOut("I3Server",e);
		}
	}

	public static I3RouterThread getRouter()
	{
		return I3Router.routerThread;
	}

	/**
	 * Returns a distinct copy of the class identified.
	 * @throws ObjectLoadException thrown when a problem occurs loading the object
	 * @param file the name of the class being loaded
	 * @return a distinct copy of the class identified
	 */
	static public ServerObject copyObject(final String file) throws ObjectLoadException {
		return routerThread.copyObject(file);
	}

	/**
	 * Returns original of the class identified.
	 * @throws ObjectLoadException thrown when a problem occurs loading the object
	 * @param file the name of the class being loaded
	 * @return original of the class identified
	 */
	static public ServerObject findObject(final String file) throws ObjectLoadException {
		return routerThread.findObject(file);
	}

	static public String getMudName()
	{
		return routerThread.getMudName();
	}

	static public int getPort()
	{
		return routerThread.getPort();
	}

	static public void shutdown()
	{
		try
		{
			try
			{
				final ShutdownPacket shutdown=new ShutdownPacket();
				shutdown.send();
			}
			catch(final Exception e)
			{
			}
			routerThread.shutdown();
			started=false;
		}
		catch(final Exception e)
		{
		}
	}

	static public void removeObject(final ServerObject ob)
	{
		if( !ob.getDestructed() )
		{
			return;
		}
		routerThread.removeObject(ob);
	}
}
