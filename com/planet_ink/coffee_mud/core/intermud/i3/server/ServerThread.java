package com.planet_ink.coffee_mud.core.intermud.i3.server;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * The Server class uses exactly one thread ServerThread object
 * during the course of program execution.  This thread loops
 * until the Server class tells it to shut down.  The loop is
 * executed in the thread's run() method.
 * @author George Reese (borg@imaginary.com), Bo Zimmerman
 * @version 1.1
 * @see com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server
 *
 * Modified in 2013 to cut down on thread use
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ServerThread implements Tickable
{
	private java.util.Date  boot_time=null;
	private int 			count  = 1;
	private final String  		mud_name;
	private final int 			port;
	private boolean 		running;
	private final ImudServices	intermuds;
	private ListenThread 	listen_thread=null;
	private volatile int 	tickStatus=Tickable.STATUS_NOT;
	private Map<String,ServerObject> objects;
	private Map<String,ServerUser>   interactives;

	protected ServerThread(String mname,
						   int mport,
						   ImudServices imud)
						   {
		mud_name = mname;
		port = mport;
		intermuds=imud;
	}

	@Override
	public String ID()
	{
		return "I3ServerThread";
	}

	@Override
	public CMObject newInstance()
	{
		return this;
	}

	@Override
	public CMObject copyOf()
	{
		return this;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(CMObject o)
	{
		return (o==this)?0:1;
	}

	@Override
	public String name()
	{
		return "I3UserThread"+Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected synchronized ServerObject copyObject(String str) throws ObjectLoadException {
		ServerObject ob;

		try
		{
			ob = (ServerObject)Class.forName(str).newInstance();
			count++;
			str = str + "#" + count;
			ob.setObjectId(str);
			objects.put(str, ob);
		}
		catch( final Exception e )
		{
			throw new ObjectLoadException("Failed to load object: " + e.getMessage());
		}
		return ob;
	}

	protected synchronized ServerObject findObject(String str) throws ObjectLoadException {
		ServerObject ob;

		if( objects.containsKey(str) )
		{
			ob = objects.get(str);
			if( ob.getDestructed() )
			{
				ob = null;
			}
		}
		else
		{
			ob = null;
		}
		if( ob == null )
		{
			try
			{
				ob = (ServerObject)Class.forName(str).newInstance();
				ob.setObjectId(str);
				objects.put(str, ob);
			}
			catch( final Exception e )
			{
				throw new ObjectLoadException("Failed to load object: " + e.getMessage());
			}
		}
		return ob;
	}

	protected synchronized void removeObject(ServerObject ob)
	{
		final String id = ob.getObjectId();

		if( !objects.containsKey(id) )
		{
			return;
		}
		objects.remove(id);
		if( interactives.containsKey(id) )
		{
			interactives.remove(id);
		}
	}

	/**
	 * While the mud is running, this method repeats the following
	 * steps over and over:
	 * 
	 * Check for pending user input and trigger user commands
	 * Check for pending object events and execute them
	 * Check for incoming user connections and create an
	 *  	interactive object for each.
	 * 
	 */
	public void start()
	{
		if( boot_time != null )
		{
			Log.errOut("I3Server","Illegal attempt to invoke run().");
			return;
		}

		try
		{
			listen_thread = new ListenThread(port);
		}
		catch( final java.io.IOException e )
		{
			Log.errOut("I3Server",e);
			return;
		}

		try
		{
			Intermud.setup(intermuds,
						   (PersistentPeer)Class.forName("com.planet_ink.coffee_mud.core.intermud.i3.IMudPeer").newInstance());
		}
		catch( final Exception e )
		{
			Log.errOut("I3Server",e);
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
		CMLib.threads().deleteTick(this, Tickable.TICKID_SUPPORT);
		CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT, 250, 1);
	}

	@Override public boolean tick(Tickable ticking, int tickID)
	{
		if(CMSecurity.isDisabled(DisFlag.I3))
			return running;

		tickStatus=Tickable.STATUS_ALIVE;
		ServerObject[] things;
		ServerUser[] users;
		synchronized( this )
		{
			things = getObjects();
			users = getInteractives();
		}
		{// Process all input
			int i;

			for(i=0; i<users.length; i++)
			{
				final ServerUser interactive = users[i];

				if( interactive.getDestructed() )
				{
					continue;
				}
				try
				{
					interactive.processInput();
				}
				catch( final Exception e )
				{
					Log.errOut("IMServerThread",e);
				}
			}
		}
		{// Check for pending object events
			int i;

			for(i=0; i<things.length; i++)
			{
				final ServerObject thing = things[i];

				if( !thing.getDestructed() )
				{
					try
					{
						thing.processEvent();
					}
					catch( final Exception e )
					{
						Log.errOut("IMServerThread",e);
					}
				}
			}
		}
		{// Get new connections
			int i;

			for(i=0; i<5; i++)
			{
				java.net.Socket s;
				ServerUser new_user;

				if(listen_thread!=null)
					s = listen_thread.nextSocket();
				else
					s=null;
				if( s == null )
				{
					break;
				}
				try
				{
					new_user = (ServerUser)copyObject("com.planet_ink.coffee_mud.core.intermud.i3.IMudUser");
				}
				catch( final ObjectLoadException e )
				{
					continue;
				}
				try
				{
					new_user.setSocket(s);
					synchronized( this )
					{
						interactives.put(new_user.getObjectId(), new_user);
						new_user.connect();
					}
				}
				catch( final java.io.IOException e )
				{
					new_user.destruct();
				}
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return running;
	}

	protected Date getBootTime()
	{
		return boot_time;
	}

	protected synchronized ServerUser[] getInteractives()
	{
		final ServerUser[] tmp = new ServerUser[interactives.size()];
		int i = 0;

		for(final ServerUser U : interactives.values())
		{
			tmp[i++] = U;
		}
		return tmp;
	}

	protected String getMudName()
	{
		return mud_name;
	}

	protected int getPort()
	{
		return port;
	}

	public void shutdown()
	{
		running=false;
		Intermud.shutdown();
		if(listen_thread!=null)
		{
			listen_thread.close();
			CMLib.killThread(listen_thread,500,1);
			listen_thread=null;
		}
		boot_time = null;
		CMLib.threads().deleteTick(this, Tickable.TICKID_SUPPORT);
	}

	protected synchronized ServerObject[] getObjects()
	{
		final ServerObject[] tmp = new ServerObject[objects.size()];
		int i = 0;
		for(final ServerObject O : objects.values())
		{
			tmp[i++] = O;
		}
		return tmp;
	}
}
