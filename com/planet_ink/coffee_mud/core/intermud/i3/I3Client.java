package com.planet_ink.coffee_mud.core.intermud.i3;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The Intermud class is the central focus of incoming
 * and outgoing Intermud 3 packets.  It creates the link
 * to the I3 router, handles reconnection, and routing
 * of packets to the mudlib.  The mudlib is responsible
 * for providing two specific objects to interface with
 * this object:
 * an implementation of com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices
 * an implementation of com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer
 * To start up the Intermud connection, call the class
 * method setup().
 * The class itself creates an instance of itself and
 * serves as a way to interface to the rest of the mudlib.
 * When the mudlib needs to send a packet, it sends it
 * through a class method which then routes it to the
 * proper instance of Intermud.
 * @author George Reese
 * @version 1.0
 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices
 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer
 */

public class I3Client implements Runnable, Persistent, Serializable
{
	public static final long serialVersionUID=0;
	static private I3Client thread = null;

	/**
	 * Sends a packet to the router.  The packet must
	 * be a valid subclass of com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.
	 * This method will then route the packet to the
	 * currently running Intermud instance.
	 * @param p an instance of a subclass of com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet
	 */
	static public void sendPacket(final Packet p)
	{
		if(!isConnected())
			return;
		thread.send(p);
	}

	/**
	 * Creates the initial link to an I3 router.
	 * It will handle subsequent reconnections as needed
	 * for as long as the mud process is running.
	 * @param routersList - client list of host:port:service
	 * @param adminEmail email address of mud admin
	 * @param imud an instance of the mudlib implementation of com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices
	 * @param peer and instance of the mudlib implementation of com.planet_ink.coffee_mud.core.intermud.i3.packets.IntermudPeer
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer
	 */
	static public void setup(final String[] routersList, final String adminEmail,
							 final ImudServices imud, final PersistentPeer peer)
	{
		if( thread != null )
		{
			return;
		}
		thread = new I3Client(routersList, adminEmail, imud, peer);
	}

	/**
	 * Translates a user entered mud name into the mud's
	 * canonical name.
	 * @param mud the user entered mud name
	 * @return the specified mud's canonical name
	 */
	static public String translateName(String mud)
	{
		if(!isConnected())
			return "";
		final String s=thread.getMudNameFor(mud);
		if(s!=null)
			return s;
		mud = mud.toLowerCase().replace('.', ' ');
		return mud;
	}

	/**
	 * Translates a user entered mud name into the mud's
	 * canonical name.
	 * @param mud the user entered mud name
	 * @return the specified mud's canonical name
	 */
	static public boolean isAPossibleMUDName(final String mud)
	{
		if(!isConnected())
			return false;
		return thread.getMudNameFor(mud) != null;
	}

	/**
	 * Register a fake channel
	 * @param c the remote channel name
	 * @return the local channel name for the specified new local channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getLocalChannel
	 */
	static public String registerFakeChannel(final String c)
	{
		if((!isConnected())||(thread.intermud.getLocalChannel(c).length()>0))
			return "";
		String name=c.toUpperCase();
		final int x=1;
		while(thread.intermud.getRemoteChannel(name).length()>0)
			name=c.toUpperCase()+x;
		final CMChannel chan=CMLib.channels().createNewChannel(name, c, "", "+FAKE", new HashSet<ChannelFlag>(), "","");
		if(thread.intermud.addChannel(chan))
			return chan.name();
		return "";
	}

	/**
	 * Register a fake channel
	 * @param c the remote channel name
	 * @return the local channel name for the specified new local channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getLocalChannel
	 */
	static public String removeFakeChannel(final String c)
	{
		if((!isConnected())||(thread.intermud.getLocalChannel(c).length()==0))
			return "";
		final String mask=thread.intermud.getRemoteMask(c);
		final String name=thread.intermud.getLocalChannel(c);
		if((mask.equalsIgnoreCase("+FAKE"))
		&&(thread.intermud.delChannel(c)))
			return name;
		return "";
	}

	/**
	 * Returns a String representing the local channel
	 * name for the specified remote channel by
	 * calling the ImudServices implementation of
	 * getLocalChannel().
	 * @param c the remote channel name
	 * @return the local channel name for the specified remote channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getLocalChannel
	 */
	static public String getLocalChannel(final String c )
	{
		if(!isConnected())
			return "";
		return thread.intermud.getLocalChannel(c);
	}

	/**
	 * Returns a String representing the remote channel
	 * name for the specified local channel by
	 * calling the ImudServices implementation of
	 * getRemoteChannel().
	 * @param c the local channel name
	 * @return the remote channel name for the specified local channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getRemoteChannel
	 */
	static public String getRemoteChannel(final String c)
	{
		if(!isConnected())
			return "";
		return thread.intermud.getRemoteChannel(c);
	}

	/**
	 * Determines whether or not the specified mud is up.
	 * You may pass user entered mud names, as this method
	 * will take the time to convert to a canonical name.
	 * @param mud the name of the mud being checked
	 * @return true if the mud is currently up, false otherwise
	 */
	static public boolean isUp(final String mud)
	{
		if(!isConnected())
			return false;
		final I3Mud m = thread.getMud(mud);

		if( m == null )
			return false;
		return (m.state == -1);
	}

	private volatile long		lastPingSentTime;
	private boolean				connected;
	private Socket				connection;
	private Thread				input_thread;
	private ImudServices		intermud;
	private int					modified;
	private DataOutputStream	output;
	private PersistentPeer		peer;
	private Tickable			save_thread;
	public boolean				shutdown	= false;
	public DataInputStream		input;
	public int					attempts;
	public ChannelList			channels;
	public MudList				muds;
	public List<NameServer>		name_servers;
	public int					password;
	public NameServer			currentRouter;
	public String				adminEmail;

	public Hashtable<String,String>	banned;

	private I3Client(final String[] routersList, final String adminEmail,
					 final ImudServices imud, final PersistentPeer p)
	{
		super();
		this.intermud = imud;
		this.peer = p;
		this.peer.setPersistent(this);
		this.connected = false;
		this.password = -1;
		this.attempts = 0;
		this.input_thread = null;
		this.channels = new ChannelList(-1);
		this.muds = new MudList(-1);
		this.banned = new Hashtable<String,String>();
		this.adminEmail = adminEmail;
		this.name_servers = new Vector<NameServer>();
		modified = Persistent.UNMODIFIED;
		try
		{
			// make sure name_servers is loaded first, because it MATTERS!
			restore();
			final Map<String,NameServer> tempNSSet = new HashMap<String,NameServer>();
			for(final NameServer ns: this.name_servers)
				tempNSSet.put(ns.name, ns);
			this.name_servers.clear();
			for(final String router: routersList)
			{
				final List<String> V=CMParms.parseAny(router,':',true);
				if(V.size()>=3)
				{
					final String host = V.get(0);
					final int port = CMath.s_int(V.get(1));
					final String service = V.get(2);
					if(tempNSSet.containsKey(service))
						this.name_servers.add(tempNSSet.get(service));
					else
						this.name_servers.add(new NameServer(host, port, service));
				}
			}
		}
		catch( final PersistenceException e )
		{
			password = -1;
			Log.errOut("Intermud",e);
		}
		channels = new ChannelList(-1);
		muds = new MudList(-1);
		if((save_thread==null)||(!CMLib.threads().isTicking(save_thread, Tickable.TICKID_SUPPORT)))
		{
			save_thread=CMLib.threads().startTickDown(new Tickable()
			{
				private final int tickStatus=Tickable.STATUS_NOT;

				@Override
				public String ID()
				{
					return "I3SaveTick"+Thread.currentThread().getThreadGroup().getName().charAt(0);
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
					return ID();
				}

				@Override
				public int getTickStatus()
				{
					return tickStatus;
				}

				@Override
				public boolean tick(final Tickable ticking, final int tickID)
				{
					try
					{
						if(CMSecurity.isDisabled(CMSecurity.DisFlag.I3))
						{
							lastPingSentTime=System.currentTimeMillis();
							return !shutdown;
						}
						else
						{
							final long ellapsedTime = System.currentTimeMillis()-imud.getLastPacketReceivedTime();
							if(ellapsedTime>(60  * 60 * 1000)) // one hour
							{
								Log.errOut("I3SaveTick","No I3 response received in "+CMLib.time().date2EllapsedTime(ellapsedTime, TimeUnit.MILLISECONDS, false)+". Connected="+I3Client.isConnected());
								CMLib.threads().executeRunnable(new Runnable()
								{
									@Override
									public void run()
									{
										try
										{
											imud.resetLastPacketReceivedTime();
											I3Server.shutdown();
											CMLib.hosts().get(0).executeCommand("START I3");
											Log.errOut("I3SaveTick","Restarted your Intermud system.  To stop receiving these messages, DISABLE the I3 system.");
										}
										catch(final Exception e)
										{
										}
									}
								});
							}
						}
						save();
					}
					catch( final PersistenceException e )
					{
					}
					return !shutdown;
				}
			}, Tickable.TICKID_SUPPORT, 30).getClientObject();
		}
		connect();
	}

	// Handles an incoming channel list packet
	@SuppressWarnings("unchecked")
	private synchronized void channelList(final Vector<?> packet)
	{
		final Hashtable<String,Vector<?>> list = (Hashtable<String,Vector<?>>)packet.elementAt(7);
		final Enumeration<String> keys = list.keys();

		synchronized( channels )
		{
			channels.setChannelListId(((Integer)packet.elementAt(6)).intValue());
			while( keys.hasMoreElements() )
			{
				final Channel c = new Channel();
				Object ob;

				c.channel = keys.nextElement();
				ob = list.get(c.channel);
				if( ob instanceof Integer )
				{
					removeChannel(c);
				}
				else
				{
					final Vector<?> info = (Vector<?>)ob;

					c.owner = (String)info.elementAt(0);
					if(info.elementAt(1) instanceof Integer)
						c.type = ((Integer)info.elementAt(1)).intValue();
					else
					if(info.elementAt(1) instanceof List)
						Log.errOut("InterMud","Received unexpected channel-reply: " + CMParms.toListString((List<?>)info.elementAt(1)));
					addChannel(c);
				}
			}
		}
		modified = Persistent.MODIFIED;
	}

	public static NameServer getNameServer()
	{
		if(thread==null)
			return null;
		if(thread.currentRouter!=null)
			return thread.currentRouter;
		if(thread.name_servers==null)
			return null;
		if(thread.name_servers.size()==0)
			return null;
		return thread.name_servers.get(0);
	}

	private synchronized void connect()
	{
		if(shutdown)
			return;
		attempts++;
		try
		{
			if(name_servers.size()==0)
				Log.sysOut("Intermud3","No I3 routers defined in coffeemud.ini file.");
			else
			{
				if(adminEmail.indexOf('@')<0)
					Log.errOut("Intermud","Please set ADMINEMAIL in your coffeemud.ini file.");
				final Vector<String> connectionStatuses=new Vector<String>(name_servers.size());
				for(int i=0;i<name_servers.size();i++)
				{
					currentRouter = name_servers.get(i);
					try
					{
						connection = new Socket(currentRouter.ip, currentRouter.port);
						output = new DataOutputStream(connection.getOutputStream());
						final Map<String,Integer> services = new HashMap<String,Integer>();
						services.put("who", Integer.valueOf(1));
						services.put("finger", Integer.valueOf(1));
						services.put("channel", Integer.valueOf(1));
						services.put("tell", Integer.valueOf(1));
						services.put("locate", Integer.valueOf(1));
						services.put("auth", Integer.valueOf(1));
						final Map<String,String> other = new HashMap<String,String>();
						final StartupReq3 pkt = new StartupReq3(currentRouter.name,password,
								muds.getMudListId(),channels.getChannelListId(),intermud.getMudPort(),0,0,
								intermud.getMudVersion(),intermud.getMudVersion(),intermud.getMudVersion(),"CoffeeMud",
								intermud.getMudState(),CMProps.getVar(CMProps.Str.ADMINEMAIL).toLowerCase(),
								services,other);
						send(pkt);
					}
					catch(final java.io.IOException e)
					{
						connectionStatuses.addElement(currentRouter.ip+": "+currentRouter.port+": "+e.getMessage());
						continue;
					}
					connected = true;
					input_thread = new Thread(Thread.currentThread().getThreadGroup(),this);
					input_thread.setDaemon(true);
					input_thread.setName(("I3Client:"+currentRouter.ip+"@"+currentRouter.port));
					input_thread.start();
					final Enumeration<String> e = intermud.getChannels();

					while( e.hasMoreElements() )
					{
						final String chan = e.nextElement();
						final ChannelListen pkt = new ChannelListen(chan,1);
						pkt.target_mud = currentRouter.name;
						send(pkt);
					}
					Log.sysOut("Intermud3","I3 client connection: "+currentRouter.ip+"@"+currentRouter.port);
					break;
				}
				if(!connected)
				{
					for(int e=0;e<connectionStatuses.size();e++)
						Log.errOut("Intermud",connectionStatuses.elementAt(e));
				}
			}
		}
		catch( final Exception e )
		{
			try
			{
				Thread.sleep((attempts) * 100l);
			}
			catch( final InterruptedException ignore )
			{
				if(shutdown)
				{
					Log.sysOut("Intermud","Shutdown!");
					return;
				}
			}
			connect();
		}
	}

	// Handles an incoming error packet
	private synchronized void error(final Vector<?> packet)
	{
		final Object target = packet.elementAt(5);
		final String msg = (String)packet.elementAt(7);

		if( target instanceof Integer )
		{
			final I3Exception e = new I3Exception(msg);
			final String cmd=e.getMessage();
			if(cmd!=null)
			{
				Log.errOut("InterMud","276-"+cmd);
			}
		}
		else
		{
		}
	}

	private synchronized void mudlist(final MudlistPacket pkt)
	{
		synchronized( muds )
		{
			muds.setMudListId(pkt.mudlist_id);
			for(final I3MudX mudx : pkt.mudlist)
			{
				if(mudx.modified != Persistent.DELETED)
					addMud(mudx);
			}
		}
	}

	@Override
	public void restore() throws PersistenceException {
		if( modified != Persistent.UNMODIFIED )
		{
			throw new PersistenceException("Restoring over changed data.");
		}
		peer.restore();
		modified = Persistent.UNMODIFIED;
	}

	public static boolean isConnected()
	{
		if(thread==null)
			return false;
		return thread.connected;
	}

	public void logMemory()
	{
		try
		{
			System.gc();
			Thread.sleep(1500);
		}
		catch(final Exception e)
		{
		}
		final long free=Runtime.getRuntime().freeMemory()/1024;
		final long total=Runtime.getRuntime().totalMemory()/1024;
		Log.errOut("Intermud", "Memory usage: "+(total-free)+"kb");
	}

	@Override
	public void run()
	{
		try
		{
			connection.setSoTimeout(60000);
			input = new DataInputStream(connection.getInputStream());
		}
		catch( final java.io.IOException e )
		{
			input = null;
			connected = false;
		}
		lastPingSentTime = System.currentTimeMillis();

		while( connected && (!shutdown))
		{
			Vector<?> data;

			try { Thread.sleep(100); }
			catch( final InterruptedException e )
			{
				if(shutdown)
				{
					Log.sysOut("Intermud","Shutdown!!");
					return;
				}
			}

			if(CMSecurity.isDisabled(CMSecurity.DisFlag.I3))
			{
				continue;
			}
			else
			if((!shutdown) && (System.currentTimeMillis()-lastPingSentTime)>( 30 * 60 * 1000))
			{
				lastPingSentTime=System.currentTimeMillis();
				try
				{
					new MudAuthRequest(I3Server.getMudName()).send();
				}
				catch (final Exception e)
				{
				}
				final long ellapsedTime = System.currentTimeMillis() - intermud.getLastPacketReceivedTime();
				if(ellapsedTime>(60  * 60 * 1000)) // one hour
				{
					Log.errOut("Intermud","No I3 Ping received in "+CMLib.time().date2EllapsedTime(ellapsedTime, TimeUnit.SECONDS, false)+". Connected="+I3Client.isConnected());
					CMLib.threads().executeRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								//logMemory();
								I3Server.shutdown();
								CMLib.hosts().get(0).executeCommand("START I3");
								Log.errOut("Intermud","Restarted your Intermud system.  To stop receiving these messages, DISABLE the I3 system.");
							}
							catch(final Exception e)
							{
							}
						}
					});
				}
			}

			String cmd;

			try
			{
				int len=0;
				while(!shutdown)
				{
					try
					{ // please don't compress this again
						len = input.readInt();
						break;
					}
					catch(final java.io.IOException e)
					{
						if((e.getMessage()==null)||(e.getMessage().toUpperCase().indexOf("TIMED OUT")<0))
							throw e;
						CMLib.s_sleep(1000);
						continue;
					}
				}
				if(len>65536)
				{
					int skipped=0;
					try
					{ // please don't compress this again
						Thread.sleep(10);
						while(input.available()>0)
						{
							skipped += input.skipBytes(input.available());
							Thread.sleep(10);
						}
					}
					catch( final Exception e )
					{
						e.printStackTrace();
					}
					Log.errOut("Intermud","Got illegal packet: "+skipped+"/"+len+" bytes.");
					continue;
				}
				final byte[] tmp = new byte[len];

				final long startTime=System.currentTimeMillis();
				while(!shutdown)
				{
					try
					{ // please don't compress this again
						input.readFully(tmp);
						break;
					}
					catch(final java.io.IOException e)
					{
						if((e.getMessage()==null)||(e.getMessage().toUpperCase().indexOf("TIMED OUT")<0))
							throw e;
						CMLib.s_sleep(1000);
						if((System.currentTimeMillis()-startTime)>(10 * 60 * 1000))
							throw e;
						Log.errOut("Intermud","Timeout receiving packet sized "+len);
						while(input.available()>0)
							input.skipBytes(input.available());
						continue;
					}
				}
				cmd=new String(tmp);
			}
			catch( final java.io.IOException e )
			{
				data = null;
				cmd = null;
				if((input_thread != null)
				&&(!input_thread.isInterrupted()))
					input_thread.interrupt();
				connected = false;
				try { Thread.sleep(1200); }
				catch (final InterruptedException ee)
				{
					if(shutdown)
					{
						Log.sysOut("Intermud","Shutdown!!!");
						return;
					}
				}
				connect();
				final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
				if(errMsg!=null)
					Log.errOut("InterMud","384-"+errMsg);
				return;
			}
			try
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.I3))
					Log.sysOut("Intermud","Receiving: "+cmd);
				final Object o=LPCData.getLPCData(cmd);
				if(o instanceof Vector)
					data=(Vector<?>)o;
				else
				{
					Log.errOut("InterMud","390-"+o);
					try
					{
						while(input.available()>0)
							input.skip(input.available());
					}
					catch (final IOException e) { }
					continue;
				}
			}
			catch( final I3Exception e )
			{
				final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
				if(errMsg!=null)
					Log.errOut("InterMud","389-"+errMsg);
				continue;
			}
			// Figure out the packet type and send it to the mudlib
			final String typeStr = ((String)data.elementAt(0)).trim().replace("-", "_");
			final PacketType type = PacketType.valueOf(typeStr.toUpperCase());
			if(type == null)
			{
				Log.errOut("Intermud","Unknown packet type: " + typeStr);
				return;
			}
			Packet pkt = null;
			final Class<? extends Packet> pktClass = type.packetClass;
			if(pktClass != null)
			{
				try
				{
					final Constructor<? extends Packet> con = pktClass.getConstructor(Vector.class);
					pkt = con.newInstance(data);
				}
				catch( final Exception  e )
				{
					Log.errOut("Intermud","Error constructing :"+type+" packet:"+e.getMessage());
					Log.debugOut("Intermud",e);
				}
			}
			switch(type)
			{
			case MUDLIST:
				if(pkt instanceof MudlistPacket)
					mudlist((MudlistPacket)pkt);
				break;
			case STARTUP_REPLY:
				if(pkt instanceof StartupReply)
					startupReply((StartupReply)pkt);
				break;
			case ERROR:
				error(data);
				break;
			case PING_REQ:
				Log.sysOut("Intermud","Ping request: " + data);
				break;
			case UCACHE_UPDATE:
				//UCacheUpdate update = new UCacheUpdate(data);
				//Log.debugOut("Intermud","UCache packet has # data: " + data.size());
				//Log.debugOut("Intermud","UCache Data: "+CMParms.combineQuoted(data,0));
				/*
				 * This is for tracking the users on other muds.  Not sure why you'd
				 * want to do that though.  The format is as the class says.
				 * data size = 9, indexed 0-8, with only the last 3 fields mattering.
				 */
				break;
			case CHANLIST_REPLY:
				channelList(data);
				break;
			default:
				if(pktClass == null)
					Log.errOut("Intermud","Other packet type: " + typeStr);
				else
				if(pkt == null)
					Log.errOut("Intermud","Bad packet type: "+type);
				else
					intermud.receive(pkt);
			}
		}
	}

	@Override
	public void save() throws PersistenceException {
		if( modified == Persistent.UNMODIFIED )
		{
			return;
		}
		peer.save();
		modified = Persistent.UNMODIFIED;
	}

	/**
	 * Sends any valid subclass of Packet to the router.
	 * @param p the packet to send
	 */
	public void send(final Packet p)
	{
		send(p.toString());
	}

	// Send a formatted mud mode packet to the router
	private void send(final String cmd)
	{
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.I3))
			Log.sysOut("Intermud","Sending: "+cmd);
		try
		{
			// Remove non-printables, as required by the I3 specification
			// (Contributed by David Green <green@couchpotato.net>)
			final byte[] packet = cmd.getBytes("ISO-8859-1");
			for (int i = 0; i < packet.length; i++)
			{
				// 160 is a non-breaking space. We'll consider that "printable".
				if ( (packet[i]&0xFF) < 32 || ((packet[i]&0xFF) >= 127 && (packet[i]&0xFF) <= 159))
				{
					// Java uses it as a replacement character,
					// so it's probably ok for us too.
					packet[i] = '?';
				}
			}
			output.writeInt(packet.length);
			output.write(packet);
		}
		catch( final java.io.IOException e )
		{
			final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
			if(errMsg!=null)
			{
				Log.errOut("InterMud","557-"+errMsg);
			}
		}
	}

	// Handle a startup reply packet
	private synchronized void startupReply(final StartupReply pkt)
	{
		final List<NameServer> router_list = pkt.routers;

		if(( router_list != null )
		&&(router_list.size()>0))
		{
			final NameServer router = router_list.get(0);
			final NameServer current_name_server = name_servers.get(0);
			if( !current_name_server.name.equals(router.name) )
			{
				name_servers.remove(current_name_server);
				// create new name server and connect
				for(int l=router_list.size()-1;l>=0;l--)
					name_servers.add(0, router_list.get(l));
				try
				{
					input.close();
					output.close();
					connection.close();
				}
				catch (final IOException e)
				{
				}
				connected = false;
				if(input_thread != null)
					input_thread.interrupt();
				connect();
				return;
			}
		}
		password = pkt.password;
		modified = Persistent.MODIFIED;
	}

	/**
	 * Shuts down the connection to the router without
	 * reconnecting.
	 * @see java.lang.Runnable#run()
	 */
	public void stop()
	{
		connected = false;
		shutdown=true;
		try
		{
			if (input != null)
				input.close();
		}
		catch(final Exception e)
		{
		}
		try
		{
			if (connection != null)
				connection.close();
		}
		catch(final Exception e)
		{
		}
		if(save_thread!=null)
		{
			CMLib.threads().deleteTick(save_thread, -1);
			save_thread=null;
		}
		try
		{
			save();
		}
		catch (final PersistenceException e)
		{
		}
		try
		{
			if (input_thread != null)
				CMLib.killThread(input_thread, 100, 1);
		}
		catch(final Exception e)
		{
		}
		input_thread=null;
		shutdown=false;
	}

	/**
	 * Adds a channel to the channel list.
	 * This does not subscribe the mud to that channel.
	 * In order to subscribe, the channel needs to be
	 * added to the ImudServices implementation's getChannels()
	 * method.
	 * @param c the channel to add to the list of known channels
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.ImudServices#getChannels
	 */
	public void addChannel(final Channel c)
	{
		channels.addChannel(c);
	}

	/**
	 * Removes a channel from the channel list.
	 * @param c the channel to remove
	 */
	public void removeChannel(final Channel c)
	{
		channels.removeChannel(c);
	}

	/**
	 * @return the list of currently known channels
	 */
	public ChannelList getChannelList()
	{
		return channels;
	}

	/**
	 * Sets the channel list to a new channel list.
	 * @param list the new channel list
	 */
	public void setChannelList(final ChannelList list)
	{
		channels = list;
	}

	/**
	 * Adds a mud to the list of known muds.
	 * @param m the mud to add
	 */
	public void addMud(final I3Mud m)
	{
		muds.addMud(m);
		modified = Persistent.MODIFIED;
	}

	private I3Mud getMud(final String mud_name)
	{
		return muds.getMud(getMudNameFor(mud_name));
	}

	/**
	 * Removed a mud from the list of known muds.
	 * @param m the mud to remove
	 */
	public void removeMud(final I3Mud m)
	{
		muds.removeMud(m);
		modified = Persistent.MODIFIED;
	}

	/**
	 * @return the list of known muds
	 */
	public MudList getMudList()
	{
		return muds;
	}

	/**
	 * @return the list of known muds
	 */
	public static MudList getAllMudsList()
	{
		if(!isConnected())
			return new MudList(-1);
		return thread.muds;
	}
	/**
	 * @return the list of known muds
	 */
	public static ChannelList getAllChannelList()
	{
		if(!isConnected())
			return new ChannelList();
		return thread.channels;
	}
	/**
	 * Sets the list of known muds to the specified list.
	 * @param list the new list of muds
	 */
	public void setMudList(final MudList list)
	{
		muds = list;
	}

	private String getMudNameFor(String mud)
	{
		mud = mud.toLowerCase().replace('.', ' ');
		for(final String cmd : muds.getMuds().keySet())
		{
			if( mud.equalsIgnoreCase(cmd) )
			{
				return cmd;
			}
		}
		for(final String cmd : muds.getMuds().keySet())
		{
			if( CMLib.english().containsString(cmd,mud) )
			{
				return cmd;
			}
		}
		return null;
	}

	/**
	 * @return the I3 password for this mud
	 */
	 public int getPassword()
	 {
		return password;
	 }

	/**
	 * Sets the Intermud 3 password.
	 * @param pass the new password
	 */
	public void setPassword(final int pass)
	{
		password = pass;
	}

	public static void shutdown()
	{
		if(thread!=null)
			thread.stop();
		thread=null;
	}
}

