/**
 * imaginary.server.ServerThread
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
 * Copyright (c) 1996 George Reese
 * The actual thread that runs the mud.
 */

package com.planet_ink.coffee_mud.i3.server;
import com.planet_ink.coffee_mud.i3.net.ListenThread;
import com.planet_ink.coffee_mud.utils.Log;
import com.planet_ink.coffee_mud.i3.packets.Intermud;
import com.planet_ink.coffee_mud.i3.packets.ImudServices;
import com.planet_ink.coffee_mud.i3.persist.PersistentPeer;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The Server class uses exactly one thread ServerThread object
 * during the course of program execution.  This thread loops
 * until the Server class tells it to shut down.  The loop is
 * executed in the thread's run() method.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 * @see imaginary.server.Server
 */
public class ServerThread extends Thread {
    private java.util.Date      boot_time=null;
    private int                 count  = 1;
    private Hashtable           interactives;
    private String              mud_name;
    private Hashtable           objects;
    private int                 port;
    private boolean             running;
	private ImudServices		intermuds;
    private ListenThread listen_thread=null;

    protected ServerThread(String mname, 
						   int mport,
						   ImudServices imud) {
        super("ServerThread");
        setName("ServerThread");
        setPriority(Thread.NORM_PRIORITY + 1);
        mud_name = mname;
        port = mport;
		intermuds=imud;
    }

    protected synchronized ServerObject copyObject(String str) throws ObjectLoadException {
        ServerObject ob;

        try {
            ob = (ServerObject)Class.forName(str).newInstance();
            count++;
            str = str + "#" + count;
            ob.setObjectId(str);
            objects.put(str, ob);
        }
        catch( Exception e ) {
            throw new ObjectLoadException("Failed to load object: " + e.getMessage());
        }
        return ob;
    }

    protected synchronized ServerObject findObject(String str) throws ObjectLoadException {
        ServerObject ob;

        if( objects.containsKey(str) ) {
            ob = (ServerObject)objects.get(str);
            if( ob.getDestructed() ) {
                ob = null;
            }
        }
        else {
            ob = null;
        }
        if( ob == null ) {
            try {
                ob = (ServerObject)Class.forName(str).newInstance();
                ob.setObjectId(str);
                objects.put(str, ob);
            }
            catch( Exception e ) {
                throw new ObjectLoadException("Failed to load object: " + e.getMessage());
            }
        }
        return ob;
    }

    protected synchronized void removeObject(ServerObject ob) {
        String id = ob.getObjectId();

        if( !objects.containsKey(id) ) {
            return;
        }
        objects.remove(id);
        if( interactives.containsKey(id) ) {
            interactives.remove(id);
        }
    }

    /**
     * While the mud is running, this method repeats the following
     * steps over and over:
     * <OL>
     * <LI> Check for pending user input and trigger user commands
     * <LI> Check for pending object events and execute them
     * <LI> Check for incoming user connections and create an
     *      interactive object for each.
     * </OL>
     */
    public void run() 
	{

        if( boot_time != null ) {
            Log.errOut("I3Server","Illegal attempt to invoke run().");
			return;
        }
        try {
            listen_thread = new ListenThread(port);
        }
        catch( java.io.IOException e ) {
            Log.errOut("I3Server",e.getMessage());
			return;
        }
		
        try {
            Intermud.setup(intermuds,
                           (PersistentPeer)Class.forName("com.planet_ink.coffee_mud.i3.IMudPeer").newInstance());
        }
        catch( Exception e ) {
            Log.errOut("I3Server",e.getMessage());
			return;
        }
		
        boot_time = new java.util.Date();
		Log.sysOut("I3Server", "InterMud3 Server started on port "+port);
		
        synchronized( this ) 
		{
            objects = new Hashtable(1000, 100);
            interactives = new Hashtable(50, 20);
        }
		
        running = true;
        while( running ) {
            ServerObject[] things;
            ServerUser[] users;
			
			try{
				Thread.sleep(100);
			}catch(Exception e){running=false;}

            synchronized( this ) {
                things = getObjects();
                users = getInteractives();
            }
            {// Process all input
                int i;

                for(i=0; i<users.length; i++) {
                    ServerUser interactive = users[i];


                    if( interactive.getDestructed() ) {
                        continue;
                    }
                    try {
                        interactive.processInput();
                    }
                    catch( Exception e ) {
						Log.errOut("IMServerThread",e);
                    }
                }
            }
            {// Check for pending object events
                int i;

                for(i=0; i<things.length; i++) {
                    ServerObject thing = things[i];

                    if( !thing.getDestructed() ) {
                        try {
                            thing.processEvent();
                        }
                        catch( Exception e ) {
							Log.errOut("IMServerThread",e);
                        }
                    }
                }
            }
            {// Get new connections
                int i;

                for(i=0; i<5; i++) {
                    java.net.Socket s;
                    ServerUser new_user;

                    s = listen_thread.nextSocket();
                    if( s == null ) {
                        break;
                    }
                    try {
                        new_user = (ServerUser)copyObject("com.planet_ink.coffee_mud.i3.IMudUser");
                    }
                    catch( ObjectLoadException e ) {
                        continue;
                    }
                    try {
                        new_user.setSocket(s);
                        synchronized( this ) {
                            interactives.put(new_user.getObjectId(), new_user);
                            new_user.connect();
                        }
                    }
                    catch( java.io.IOException e ) {
                        new_user.destruct();
                    }
                }
            }
        }
    }

    protected Date getBootTime() {
        return boot_time;
    }

    protected synchronized ServerUser[] getInteractives() {
        ServerUser[] tmp = new ServerUser[interactives.size()];
        Enumeration e = interactives.elements();
        int i = 0;

        while( e.hasMoreElements() ) {
            tmp[i++] = (ServerUser)e.nextElement();
        }
        return tmp;
    }

    protected String getMudName() {
        return mud_name;
    }

    protected int getPort() {
        return port;
    }

	public void shutdown()
	{
		running=false;
		Intermud.shutdown();
		if(listen_thread!=null)
		{
			listen_thread.close();
			listen_thread.interrupt();
			listen_thread=null;
		}
		boot_time = null;
		try{ Thread.sleep(100);}catch(Exception e){}
	}
	
    protected synchronized ServerObject[] getObjects() {
        ServerObject[] tmp = new ServerObject[objects.size()];
        Enumeration e = objects.elements();
        int i = 0;

        while( e.hasMoreElements() ) {
            tmp[i++] = (ServerObject)e.nextElement();
        }
        return tmp;
    }
}