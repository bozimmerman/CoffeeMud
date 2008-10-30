package com.planet_ink.coffee_mud.core.intermud.net;
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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
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
 */
@SuppressWarnings("unchecked")
public class ListenThread extends Thread {
    private ServerSocket listen;
    private Vector clients;

    public ListenThread(int port) throws java.io.IOException {
		super("ListenThread");
		setName("ListenThread");
        clients = new Vector(10, 2);
        listen = new ServerSocket(port);
        setDaemon(true);
        start();
    }

    public void run() {
        while( listen!=null && !listen.isClosed() ) {
            Socket client;

            try {
                client = listen.accept();
                synchronized( clients ) {
                    clients.addElement(client);
                }
            }
            catch( java.io.IOException e ) {
            }
        }
    }

	public void close()
	{
		try
		{
			if(listen!=null) listen.close();
		}
		catch(Exception e){}
	}
	
    public Socket nextSocket() {
        Socket client;

        synchronized( clients ) {
            if( clients.size() > 0 ) {
                client = (Socket)clients.elementAt(0);
                clients.removeElementAt(0);
            }
            else {
                client = null;
            }
        }
        return client;
    }
}
