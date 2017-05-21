package com.planet_ink.coffee_mud.core.intermud.i3.server;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
 * com.planet_ink.coffee_mud.core.intermud.i3.server.Server
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
 * Last Update: 960921
 * @author George Reese
 * @version 1.0
 */
public class I3Server 
{
	static private ServerThread serverClient = null;
	static private boolean started = false;

	/**
	 * Creates a server thread if one has not yet been
	 * created.
	 * @throws ServerSecurityException thrown if an attempt to call start()
	 * is made once the server is running.
	 * @param mud the name of the mud being started
	 * @param port the port of the server
	 * @param imud a library for interaction with base system
	 */
	static public void start(String mud,
							 int port,
							 ImudServices imud) throws ServerSecurityException
	{
		try
		{
			if( started )
			{
				throw new ServerSecurityException("Illegal attempt to start Server.");
			}
			started = true;
			serverClient = new ServerThread(mud, port, imud);
			Log.sysOut("I3Server", "InterMud3 Core (c)1996 George Reese");
			serverClient.start();
		}
		catch(final Exception e)
		{
			serverClient=null;
			Log.errOut("I3Server",e);
		}
	}

	/**
	 * Returns a distinct copy of the class identified.
	 * @throws ObjectLoadException thrown when a problem occurs loading the object
	 * @param file the name of the class being loaded
	 * @return a distinct copy of the class identified
	 */
	static public ServerObject copyObject(String file) throws ObjectLoadException {
		return serverClient.copyObject(file);
	}

	/**
	 * Returns original of the class identified.
	 * @throws ObjectLoadException thrown when a problem occurs loading the object
	 * @param file the name of the class being loaded
	 * @return original of the class identified
	 */
	static public ServerObject findObject(String file) throws ObjectLoadException {
		return serverClient.findObject(file);
	}

	static public ServerUser[] getInteractives()
	{
		return serverClient.getInteractives();
	}

	static public String getMudName()
	{
		return serverClient.getMudName();
	}

	static public int getPort()
	{
		return serverClient.getPort();
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
		serverClient.shutdown();
		started=false;
		}
		catch(final Exception e)
		{
		}
	}

	static public void removeObject(ServerObject ob)
	{
		if( !ob.getDestructed() )
		{
			return;
		}
		serverClient.removeObject(ob);
	}
}
