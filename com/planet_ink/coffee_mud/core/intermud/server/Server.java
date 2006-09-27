package com.planet_ink.coffee_mud.core.intermud.server;
import com.planet_ink.coffee_mud.core.intermud.server.*;
import com.planet_ink.coffee_mud.core.intermud.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.intermud.packets.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
/*
 * com.planet_ink.coffee_mud.core.intermud.server.Server
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
public class Server {
    static private ServerThread thread = null;
    static private boolean started = false;

    /**
     * Creates a server thread if one has not yet been
     * created.
     * @exception DatabaseException thrown if the database is unreachable
     * for some reason
     * @exception ServerSecurityException thrown if an attempt to call start()
     * is made once the server is running.
     * @param mud the name of the mud being started
     */
    static public void start(String mud, 
							 int port,
							 ImudServices imud) 
	{
		try
		{
			if( started ) {
			    throw new ServerSecurityException("Illegal attempt to start Server.");
			}
		    started = true;
			thread = new ServerThread(mud, port, imud);
			thread.setDaemon(true);
			Log.sysOut("I3Server", "InterMud3 Core (c)1996 George Reese");
			thread.start();
		}
		catch(Exception e)
		{
			thread=null;
			Log.errOut("I3Server",e.getMessage());
		}
    }

    /**
     * Returns a distinct copy of the class identified.
     * @exception ObjectLoadException thrown when a problem occurs loading the object
     * @param file the name of the class being loaded
     */
    static public ServerObject copyObject(String file) throws ObjectLoadException {
        return thread.copyObject(file);
    }

    static public ServerObject findObject(String file) throws ObjectLoadException {
        return thread.findObject(file);
    }

    static public ServerUser[] getInteractives() {
        return thread.getInteractives();
    }

    static public String getMudName() {
        return thread.getMudName();
    }

    static public int getPort() {
        return thread.getPort();
    }

	static public void shutdown()
	{
		try{
		thread.shutdown();
		started=false;
		CMLib.killThread(thread,500,1);
		}catch(Exception e){}
	}
	
    static public void removeObject(ServerObject ob) {
        if( !ob.getDestructed() ) {
            return;
        }
        thread.removeObject(ob);
    }
}
