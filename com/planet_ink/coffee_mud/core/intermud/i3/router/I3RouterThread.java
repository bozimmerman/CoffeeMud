package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.I3Exception;
import com.planet_ink.coffee_mud.core.intermud.i3.LPCData;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.PeerMud;
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
import java.util.Map;
import java.util.Vector;

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
	public final static int peerTimeout = 10000;

	//https://wotf.org/i3/irn/v1/
	private java.util.Date				boot_time		= null;
	private int							count			= 1;
	private final String				router_name;
	private final int					port;
	private boolean						running;
	private ListenThread				listen_thread	= null;
	private final int					password;
//	private final String[]				peerRoutersList;
//	private final String				adminEmail;
	private final Map<String, ServerObject>	objects		= new Hashtable<String,ServerObject>(1000, 100);
	private final Map<String, RouterPeer>	routerPeers	= new Hashtable<String, RouterPeer>();
	private final Map<String, NetPeer>	socks			= new Hashtable<String, NetPeer>();
	private final Map<String, PeerMud>	clientMuds 		= new Hashtable<String, PeerMud>();
	private final ChannelList			channels		= new ChannelList();

	protected I3RouterThread(final String router_name,
							final int router_port,
							final int password,
							final String[] routersList,
							final String adminEmail)

	{
		super(Thread.currentThread().getThreadGroup(),
			  "I3Router"+Thread.currentThread().getThreadGroup().getName().charAt(0));
		this.router_name = router_name;
		this.port = router_port;
		this.password = password;
//		this.peerRoutersList = routersList;
//		this.adminEmail = adminEmail;
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
		return (o==this)?0:1;
	}

	@Override
	public String name()
	{
		return "I3Router"+getThreadGroup().getName().charAt(0);
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
		if( routerPeers.containsKey(id) )
		{
			routerPeers.remove(id);
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
			Log.errOut(ID(),"Illegal attempt to invoke run().");
			return;
		}

		try
		{
			listen_thread = new ListenThread(port);
		}
		catch( final java.io.IOException e )
		{
			Log.errOut(ID(),e);
			return;
		}

		boot_time = new java.util.Date();
		Log.sysOut(ID(), "InterMud3 Router started on port "+port);

		synchronized( this )
		{
			objects.clear();
		}

		running = true;
	}

	public RouterPeer getPeer(final String name)
	{
		return routerPeers.get(name);
	}

	protected void processNewConnections()
	{
		final ListenThread listen_thread;
		synchronized(this)
		{
			listen_thread = this.listen_thread;
		}
		if(listen_thread != null)
		{
			// Get new tentative connections
			Socket sock = listen_thread.nextSocket();
			while(sock != null)
			{
				final NetPeer newPeer = new NetPeer(sock);
				synchronized( this )
				{
					socks.put(newPeer.toString(), newPeer);
				}
				sock = listen_thread.nextSocket();
			}
			Packet pkt;
			for(final Iterator<String> i = socks.keySet().iterator();i.hasNext();)
			{
				final String key = i.next();
				final NetPeer peer = socks.get(key);
				final DataInputStream istream = peer.getInputStream();
				try
				{
					if(istream == null)
					{
						peer.close();
						i.remove();
					}
					else
					if((pkt = readPacket(istream))!=null)
					{
						if(pkt.getType() == Packet.PacketType.IRN_STARTUP_REQUEST)
						{
							final IrnStartupRequest ipkt = (IrnStartupRequest)pkt;
							if(((ipkt.target_password == this.password)
								|| (this.password < 0))
							&&(ipkt.target_router.equalsIgnoreCase(this.router_name))
							&&(!this.routerPeers.containsKey(ipkt.sender_router)))
							{
								final String sender = ipkt.sender_router;
								final IRouterPeer rpeer = new IRouterPeer(peer);
								rpeer.password = ipkt.sender_password;
								this.routerPeers.put(sender, rpeer);
								// do not close peer, as we want to keep the socks open
							}
							else
								peer.close();
							i.remove();
						}
						else
						if(pkt.getType() == Packet.PacketType.STARTUP_REQ_3)
						{
							final StartupReq3 mpkt = (StartupReq3)pkt;
							if(((mpkt.password == this.password)
								|| (this.password < 0))
							&&(mpkt.target_router.equalsIgnoreCase(this.router_name))
							&&(!this.clientMuds.containsKey(mpkt.sender_router)))
							{
								final PeerMud newMud = new PeerMud(mpkt.sender_router, peer);
								newMud.setMud(mpkt.makeMud());
								this.clientMuds.put(mpkt.sender_router, newMud);
							}
							i.remove();
						}
						else
							Log.sysOut(ID(), "Rejecting new peer packet type: "+pkt.getType());
					}
					else
					if((System.currentTimeMillis() - peer.connectTime) > peerTimeout)
					{
						peer.close();
						i.remove();
					}
				}
				catch (final IOException e)
				{
					try
					{
						peer.close();
					}
					catch (final IOException e1)
					{
					}
					i.remove();
				}
			}
		}
	}

	protected void processMudPeers()
	{
		final LinkedList<Packet> todo = new LinkedList<Packet>();
		for(final Iterator<String> p = this.clientMuds.keySet().iterator(); p.hasNext();)
		{
			final String peerName = p.next();
			final PeerMud peer = this.clientMuds.get(peerName);
			if(!peer.isConnected())
				p.remove();
			else
			{
				final DataInputStream istream = peer.getInputStream();
				final Packet pkt;
				try
				{
					if(istream == null)
					{
						peer.close();
						p.remove();
					}
					else
					if((pkt = readPacket(istream))!=null)
						todo.add(pkt);
					//TODO: deal with unpinged, or pings
				}
				catch (final IOException e)
				{
					try
					{
						peer.close();
					}
					catch(final Exception e1) {}
					p.remove();
				}
			}
		}
	}

	protected void processRouterPeers()
	{
		final LinkedList<Packet> todo = new LinkedList<Packet>();
		for(final Iterator<String> p = this.routerPeers.keySet().iterator(); p.hasNext();)
		{
			final String peerName = p.next();
			final RouterPeer peer = this.routerPeers.get(peerName);
			if(!peer.isConnected())
			{
				peer.destruct();
				p.remove();
			}
			else
			{
				final DataInputStream istream = peer.getInputStream();
				final Packet pkt;
				try
				{
					if(istream == null)
					{
						peer.destruct();
						p.remove();
					}
					else
					if((pkt = readPacket(istream))!=null)
						todo.add(pkt);
					//TODO: deal with unpinged, or pings
				}
				catch (final IOException e)
				{
					peer.destruct();
					p.remove();
				}
			}
		}
	}

	@Override
	public void run()
	{
		while(running)
		{
			CMLib.s_sleep(250);
			processNewConnections();


			ServerObject[] things;
			synchronized( this )
			{
				things = getObjects();
			}
			{// Check for pending object events
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

	protected Packet readPacket(final DataInputStream istream) throws IOException
	{
		if(istream.available() >= 4)
		{
			if(istream.markSupported())
				istream.mark(32768);
			final int len = istream.readInt();
			if(len > 32768)
			{
				if(istream.markSupported())
					istream.reset();
				istream.skip(istream.available());
				return null;
			}
			if(istream.available() >= len)
			{
				final byte[] tmp = new byte[len];
				istream.readFully(tmp);
				final String cmd=new String(tmp);
				Object o;
				try
				{
					o = LPCData.getLPCData(cmd);
					if((!(o instanceof Vector))
					||(((Vector<?>)o).size()<4))
					{
						Log.errOut(ID(),"390-"+o);
						if(istream.markSupported())
							istream.reset();
						istream.skip(istream.available());
						return null;
					}
					final Vector<?> data=(Vector<?>)o;
					final String typeStr = ((String)data.elementAt(0)).trim().replace("-", "_");
					final PacketType type = PacketType.valueOf(typeStr.toUpperCase());
					if(type == null)
					{
						Log.errOut(ID(),"Unknown packet type: " + typeStr);
						return null;
					}
					final Class<? extends Packet> pktClass = type.packetClass;
					if(pktClass == null)
						Log.errOut(ID(),"Other packet type: " + typeStr);
					else
					{
						try
						{
							final Constructor<? extends Packet> con = pktClass.getConstructor(Vector.class);
							return con.newInstance(data);
						}
						catch( final Exception  e )
						{
							Log.errOut(ID(),type+"-"+e.getMessage());
						}
					}
				}
				catch (final I3Exception e)
				{
					Log.errOut(ID(),"390-"+e.getMessage());
					if(istream.markSupported())
						istream.reset();
					istream.skip(istream.available());
					return null;
				}
			}
			else
			if(istream.markSupported())
				istream.reset();
		}
		return null;
	}


	protected Date getBootTime()
	{
		return boot_time;
	}

	protected String getRouterName()
	{
		return router_name;
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
