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
	protected long				nextSaveTime	= System.currentTimeMillis();
	protected long				nextConnTime	= System.currentTimeMillis();
	protected ListenThread		listen_thread	= null;
	protected ThreadGroup		threadGroup		= null;

	public	  boolean			running			= false;

	protected final I3RConnections 			connMonitor = new I3RConnections();
	protected final Map<String, NetPeer>	socks		= new Hashtable<String, NetPeer>();
	protected final RouterPeerList			peers		= new RouterPeerList();
	protected final MudPeerList				muds		= new MudPeerList();
	protected final ChannelList				channels	= new ChannelList();

	protected I3RouterThread(final String router_name,
							 final String router_ip,
							 final int router_port,
							 final int password)

	{
		super(initialThreadGroup(),
			  "I3Router"+initialThreadGroup().getName().charAt(0));
		me = new NameServer(router_ip,router_port,router_name);
		this.password = password;
	}

	private static ThreadGroup initialThreadGroup()
	{
		final ThreadGroup grp = Thread.currentThread().getThreadGroup();
		if(grp != null)
			return grp;
		return new ThreadGroup("0-I3R");
	}

	public ThreadGroup threadGroup()
	{
		if(super.getThreadGroup() != null)
			return super.getThreadGroup();
		if(threadGroup == null)
			threadGroup = initialThreadGroup();
		return threadGroup;
	}

	@Override
	public String ID()
	{
		return  "I3Router"+threadGroup().getName().charAt(0);
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
			final MudPeer old = muds.getMud(ob.mud_name);
			if((old != null) && (old != ob))
			{
				if(!old.isConnected())
					old.destruct();
			}
			muds.setMudListId(muds.getMudListId()+1);
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				final IrnMudlistDelta delta = new IrnMudlistDelta(rpeer.name);
				delta.mudlist_id = I3Router.getMudListId();
				delta.mudlist.add(ob);
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
			muds.addMud(ob);
			ob.state = -1; // mark online
			for(final MudPeer rpeer : I3Router.getMudPeers())
			{
				if((!rpeer.isConnected())||(rpeer==ob))
					continue;
				final MudlistPacket delta = new MudlistPacket(rpeer.mud_name);
				delta.mudlist_id = I3Router.getMudListId();
				delta.mudlist.add(ob);
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
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
			final RouterPeer old = peers.getRouter(ob.name);
			if((old != null) && (old != ob))
			{
				if(!old.isConnected())
					old.destruct();
				return old;
			}
			peers.addRouter(ob);
		}
		catch( final Exception e )
		{
			throw new ObjectLoadException("Failed to load object: " + e.getMessage());
		}
		return ob;
	}

	protected synchronized MudPeer findMudPeer(final String name)
	{
		final MudPeer ob = muds.getMud(name);
		if( ob != null )
		{
			if( !ob.getDestructed() )
				return ob;
		}
		return null;
	}

	protected synchronized RouterPeer findRouterPeer(final String name)
	{
		final RouterPeer ob = peers.getRouter(name);
		if( ob != null )
		{
			if( !ob.getDestructed() )
				return ob;
		}
		return null;
	}

	protected synchronized MudPeer getMudPeer(final String name)
	{
		return muds.getMud(name);
	}

	protected synchronized RouterPeer getRouterPeer(final String name)
	{
		return peers.getRouter(name);
	}

	protected synchronized void removeMudPeer(final MudPeer ob)
	{
		if( muds.getMud(ob.mud_name) != null )
		{
			muds.setMudListId(muds.getMudListId()+1);
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				if(!rpeer.isConnected())
					continue;
				final IrnMudlistDelta delta = new IrnMudlistDelta(rpeer.name);
				delta.mudlist_id = I3Router.getMudListId();
				delta.mudlist.add(ob);
				ob.state = 0; // mark deleted YES!
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
			muds.removeMud(ob);
			ob.state = 0; // mark deleted YES!
			for(final MudPeer rpeer : I3Router.getMudPeers())
			{
				if(!rpeer.isConnected())
					continue;
				final MudlistPacket delta = new MudlistPacket(rpeer.mud_name);
				delta.mudlist_id = I3Router.getMudListId();
				delta.mudlist.add(ob);
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
		}
	}

	protected synchronized void removeRouterPeer(final RouterPeer ob)
	{
		peers.removeRouter(ob);
	}

	public void initializePeer(final RouterPeer peer)
	{
		try
		{
			final IrnStartupRequest req1 = new IrnStartupRequest(peer.name);
			req1.sender_password = this.password;
			req1.target_password = peer.password;
			req1.send();
		}
		catch (final InvalidPacketException e)
		{
			Log.errOut(e);
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
			Log.errOut(ID(),"Illegal attempt to invoke run().");
			return;
		}
		boot_time = new java.util.Date();
		running = true; // must be before start
		super.start();
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

		try
		{
			channels.restore();
		}
		catch (final PersistenceException e)
		{
			Log.errOut(e);
		}
		try
		{
			muds.restore();
		}
		catch (final PersistenceException e)
		{
			Log.errOut(e);
		}
		try
		{
			peers.restore();
		}
		catch (final PersistenceException e)
		{
			Log.errOut(e);
		}
		this.nextConnTime = System.currentTimeMillis() - 1;
	}

	@Override
	public void run()
	{
		try
		{
			nextSaveTime = System.currentTimeMillis() + 120000;
			// nextConnTime - init in start
			while(running)
			{
				CMLib.s_sleep(250);
				if(System.currentTimeMillis() >= nextSaveTime)
				{
					nextSaveTime = System.currentTimeMillis() + 120000;
					try
					{
						muds.save();
						channels.save();
						peers.save();
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
				if(System.currentTimeMillis() > nextConnTime)
				{
					nextConnTime = System.currentTimeMillis() + 300000;
					for(final RouterPeer peer : peers.getRouters().values())
					{
						try
						{
							if(!peer.isConnected())
							{
								peer.connect();
								if(peer.isConnected())
									initializePeer(peer);
							}
						}
						catch(final Exception e)
						{
							Log.errOut(e);
						}
					}
				}
				else
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
			Log.sysOut("I3RouterThread shut down.");
		}
		finally
		{
			running=false;
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
		if(!running || boot_time == null)
			return;
		running=false;
		if(listen_thread!=null)
		{
			listen_thread.close();
			CMLib.killThread(listen_thread,500,1);
			listen_thread=null;
		}
		try
		{
			muds.save();
			channels.save();
			peers.save();
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		try
		{
			this.interrupt();
			Thread.sleep(501);
		}
		catch(final Exception e)
		{
			Log.errOut(e);
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
		final RouterPeer[] tmp = new RouterPeer[peers.getRouters().size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(peers.getRouters().values());
		return objsList.toArray(tmp);
	}

	protected synchronized MudPeer[] getMuds()
	{
		final MudPeer[] tmp = new MudPeer[muds.getMuds().size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(muds.getMuds().values());
		return objsList.toArray(tmp);
	}

	protected synchronized ServerObject[] getObjects()
	{
		final ServerObject[] tmp = new ServerObject[muds.getMuds().size() + peers.getRouters().size()];
		final List<ServerObject> objsList = new XArrayList<ServerObject>(muds.getMuds().values());
		objsList.addAll(peers.getRouters().values());
		return objsList.toArray(tmp);
	}
}
