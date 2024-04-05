package com.planet_ink.coffee_mud.core.intermud.i3.router;
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

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * The Router class uses exactly one thread RouterThread object
 * during the course of program execution.  This thread loops
 * until the Router class tells it to shut down.  The loop is
 * executed in the thread's run() method.
 * @author George Reese (borg@imaginary.com), Bo Zimmerman
 * @version 1.1
 * @see com.planet_ink.coffee_mud.core.intermud.i3.router.I3Router
 */
public class I3RouterThread extends Thread implements CMObject
{
	private java.util.Date				boot_time		= null;
	private int							count			= 1;
	private final String				mud_name;
	private final int					port;
	private boolean						running;
	private ListenThread				listen_thread	= null;
//	private final String				password;
//	private final String[]				peerRoutersList;
//	private final String				adminEmail;
	private Map<String, ServerObject>	objects;
	private Map<String, RouterPeer>		interactives;

	protected I3RouterThread(final String mname,
							final int mport,
							final String password,
							final String[] routersList,
							final String adminEmail)

	{
		this.mud_name = mname;
		this.port = mport;
//		this.password = password;
//		this.peerRoutersList = routersList;
//		this.adminEmail = adminEmail;
	}

	@Override
	public String ID()
	{
		return "I3RouterThread";
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
	public int compareTo(final CMObject o)
	{
		return (o==this)?0:1;
	}

	@Override
	public String name()
	{
		return "I3RouterThread"+Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	protected synchronized ServerObject copyObject(String str) throws ObjectLoadException
	{
		ServerObject ob;

		try
		{
			ob = (ServerObject)Class.forName(str).getDeclaredConstructor().newInstance();
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

	protected synchronized ServerObject findObject(final String str) throws ObjectLoadException
	{
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
				ob = (ServerObject)Class.forName(str).getDeclaredConstructor().newInstance();
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

	protected synchronized void removeObject(final ServerObject ob)
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
		super.start();
		if( boot_time != null )
		{
			Log.errOut("I3RouterThread","Illegal attempt to invoke run().");
			return;
		}

		try
		{
			listen_thread = new ListenThread(port);
		}
		catch( final java.io.IOException e )
		{
			Log.errOut("I3RouterThread",e);
			return;
		}

		boot_time = new java.util.Date();
		Log.sysOut("I3Router", "InterMud3 Router started on port "+port);

		synchronized( this )
		{
			objects = new Hashtable<String,ServerObject>(1000, 100);
		}

		running = true;
	}

	@Override
	public void run()
	{
		while(running)
		{
			CMLib.s_sleep(250);
			ServerObject[] things;
			synchronized( this )
			{
				things = getObjects();
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
							Log.errOut("I3RouterThread",e);
						}
					}
				}
			}
			{// Get new connections
				int i;

				for(i=0; i<5; i++)
				{
					java.net.Socket s;
					RouterPeer new_peer;

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
						new_peer = (RouterPeer)copyObject("com.planet_ink.coffee_mud.core.intermud.i3.router.IRouterPeer");
					}
					catch( final ObjectLoadException e )
					{
						continue;
					}
					try
					{
						new_peer.setSocket(s);
						synchronized( this )
						{
							interactives.put(new_peer.getObjectId(), new_peer);
							new_peer.connect();
						}
					}
					catch( final java.io.IOException e )
					{
						new_peer.destruct();
					}
				}
			}
		}
	}

	protected Date getBootTime()
	{
		return boot_time;
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
		if(listen_thread!=null)
		{
			listen_thread.close();
			CMLib.killThread(listen_thread,500,1);
			listen_thread=null;
		}
		boot_time = null;
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
