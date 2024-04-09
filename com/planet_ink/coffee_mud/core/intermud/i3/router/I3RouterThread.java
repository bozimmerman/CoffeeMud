package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Exception;
import com.planet_ink.coffee_mud.core.intermud.i3.LPCData;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

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
	//https://wotf.org/i3/irn/v1/
	protected java.util.Date	boot_time		= null;
	protected AtomicInteger		count			= new AtomicInteger(1);
	protected final NameServer  me;
	protected final int			password;
	protected boolean			running;
	protected int				mudListId		= 0;
	protected int				channelListId	= 0;
	protected ListenThread		listen_thread	= null;

	protected final I3RConnections 			connMonitor = new I3RConnections();
	protected final Map<String, MudPeer>	muds		= new Hashtable<String, MudPeer>();
	protected final Map<String, RouterPeer>	peers		= new Hashtable<String, RouterPeer>();
	protected final Map<String, NetPeer>	socks		= new Hashtable<String, NetPeer>();
	protected final ChannelList				channels	= new ChannelList();

	protected I3RouterThread(final String router_name,
							 final String router_ip,
							 final int router_port,
							 final int password)

	{
		super(Thread.currentThread().getThreadGroup(),
			  "I3Router"+Thread.currentThread().getThreadGroup().getName().charAt(0));
		me = new NameServer(router_ip,router_port,router_name);
		this.password = password;
	}

	@Override
	public String ID()
	{
		return  "I3Router"+getThreadGroup().getName().charAt(0);
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
		if(o == null)
			return -1;
		return (o==this)?0:(o.hashCode()>hashCode())?1:-1;
	}

	@Override
	public String name()
	{
		return "I3Router"+getThreadGroup().getName().charAt(0);
	}

	protected synchronized MudPeer addMudPeer(final MudPeer ob) throws ObjectLoadException
	{
		try
		{
			final MudPeer old = muds.get(ob.mud.mud_name);
			if((old != null) && (old != ob))
			{
				if(!old.isConnected())
					old.destruct();
				return old;
			}
			final Random r = new Random(System.currentTimeMillis());
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				final IrnMudlistDelta delta = new IrnMudlistDelta(rpeer.name);
				delta.mudlist_id = r.nextInt(Integer.MAX_VALUE/1000);
				delta.mudlist.add(ob.mud);
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
			muds.put(ob.mud.mud_name, ob);
		}
		catch( final Exception e )
		{
			throw new ObjectLoadException("Failed to load object: " + e.getMessage());
		}
		return ob;
	}

	protected synchronized RouterPeer addRouterPeer(final RouterPeer ob) throws ObjectLoadException
	{
		try
		{
			final RouterPeer old = peers.get(ob.name);
			if((old != null) && (old != ob))
			{
				if(!old.isConnected())
					old.destruct();
				return old;
			}
			if(peers.containsKey(ob.name))
				peers.get(ob.name).destruct();
			peers.put(ob.name, ob);
		}
		catch( final Exception e )
		{
			throw new ObjectLoadException("Failed to load object: " + e.getMessage());
		}
		return ob;
	}

	protected synchronized MudPeer findMudPeer(final String name)
	{
		if( muds.containsKey(name) )
		{
			final MudPeer ob = muds.get(name);
			if( !ob.getDestructed() )
				return ob;
		}
		return null;
	}

	protected synchronized RouterPeer findRouterPeer(final String name)
	{
		if( peers.containsKey(name) )
		{
			final RouterPeer ob = peers.get(name);
			if(!ob.getDestructed() )
				return ob;
		}
		return null;
	}

	protected synchronized void removeMudPeer(final MudPeer ob)
	{
		if( muds.containsKey(ob.mud.mud_name) )
		{
			final Random r = new Random(System.currentTimeMillis());
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				final IrnMudlistDelta delta = new IrnMudlistDelta(rpeer.name);
				delta.mudlist_id = r.nextInt(Integer.MAX_VALUE/1000);
				delta.mudlist.add(ob.mud);
				ob.mud.state = 0; // mark deleted
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
			muds.remove(ob.mud.mud_name);
		}
	}

	protected synchronized void removeRouterPeer(final RouterPeer ob)
	{
		final String id = ob.getObjectId();

		if( peers.containsKey(id) )
			peers.remove(id);
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
			Log.errOut(ID(),"Illegal attempt to invoke run().");
			return;
		}
		boot_time = new java.util.Date();
		super.start();
		// Load the support classes into objects, since they
		// get thread time.

		try
		{
			listen_thread = new ListenThread(me.port);
		}
		catch( final java.io.IOException e )
		{
			Log.errOut(ID(),e);
			return;
		}
		Log.sysOut(ID(), "InterMud3 Router started on port "+me.port);
		running = true;


		//TODO: restore channels?
		//TODO: restore routers
		//TODO: connect to other routers
	}

	@Override
	public void run()
	{
		while(running)
		{
			CMLib.s_sleep(250);

			{
				try
				{
					connMonitor.processEvent();
				}
				catch( final Exception e )
				{
					Log.errOut(ID(),e);
				}
				// Check for pending object events
				ServerObject[] things;
				synchronized( this )
				{
					things = getObjects();
				}
				for(int i=0; i<things.length; i++)
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
							Log.errOut(ID(),e);
						}
					}
				}
			}
		}
	}

	protected Date getBootTime()
	{
		return boot_time;
	}

	protected String getRouterName()
	{
		return me.name;
	}

	protected int getRouterPassword()
	{
		return this.password;
	}

	protected int getPort()
	{
		return me.port;
	}

	public void shutdown()
	{
		if(running || boot_time == null)
			return;
		running=false;
		if(listen_thread!=null)
		{
			listen_thread.close();
			CMLib.killThread(listen_thread,500,1);
			listen_thread=null;
		}
		for(final ServerObject obj : getObjects())
			obj.destruct();
		boot_time = null;
	}

	protected synchronized Channel findChannel(final String str)
	{
		Channel c  = channels.getChannel(str);
		if(c != null)
			return c;
		for(final RouterPeer p : getPeers())
		{
			c = p.channels.getChannel(str);
			if(c != null)
				return c;
		}
		return null;
	}

	protected synchronized RouterPeer[] getPeers()
	{
		final RouterPeer[] tmp = new RouterPeer[peers.size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(peers.values());
		return objsList.toArray(tmp);
	}

	protected synchronized MudPeer[] getMuds()
	{
		final MudPeer[] tmp = new MudPeer[muds.size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(muds.values());
		return objsList.toArray(tmp);
	}

	protected synchronized ServerObject[] getObjects()
	{
		final MudPeer[] tmp = new MudPeer[muds.size() + peers.size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(muds.values());
		objsList.addAll(peers.values());
		return objsList.toArray(tmp);
	}
}
