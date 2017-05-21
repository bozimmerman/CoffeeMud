package com.planet_ink.coffee_mud.core.intermud.i3.packets;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
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
 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices
 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer
 */

@SuppressWarnings({"unchecked","rawtypes"})
public class Intermud implements Runnable, Persistent, Serializable
{
	public static final long serialVersionUID=0;
	static private Intermud thread = null;

	/**
	 * Sends a packet to the router.  The packet must
	 * be a valid subclass of com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.
	 * This method will then route the packet to the
	 * currently running Intermud instance.
	 * @param p an instance of a subclass of com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet
	 */
	static public void sendPacket(Packet p)
	{
		if(!isConnected())
			return;
		thread.send(p);
	}

	/**
	 * Creates the initial link to an I3 router.
	 * It will handle subsequent reconnections as needed
	 * for as long as the mud process is running.
	 * @param imud an instance of the mudlib implementation of com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices
	 * @param peer and instance of the mudlib implementation of com.planet_ink.coffee_mud.core.intermud.i3.packets.IntermudPeer
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer
	 */
	static public void setup(ImudServices imud, PersistentPeer peer)
	{
		if( thread != null )
		{
			return;
		}
		thread = new Intermud(imud, peer);
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
	static public boolean isAPossibleMUDName(String mud)
	{
		if(!isConnected())
			return false;
		return thread.getMudNameFor(mud) != null;
	}

	/**
	 * Register a fake channel
	 * @param c the remote channel name
	 * @return the local channel name for the specified new local channel name
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices#getLocalChannel
	 */
	static public String registerFakeChannel(String c)
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
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices#getLocalChannel
	 */
	static public String removeFakeChannel(String c)
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
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices#getLocalChannel
	 */
	static public String getLocalChannel(String c )
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
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices#getRemoteChannel
	 */
	static public String getRemoteChannel(String c)
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
	static public boolean isUp(String mud)
	{
		if(!isConnected())
			return false;
		final I3Mud m = thread.getMud(mud);

		if( m == null )
			return false;
		return (m.state == -1);
	}

	private volatile long		lastPingSentTime;
	private boolean 			connected;
	private Socket  			connection;
	private Thread  			input_thread;
	private ImudServices		intermud;
	private int 				modified;
	private DataOutputStream	output;
	private PersistentPeer  	peer;
	private Tickable	  		save_thread;
	public boolean				shutdown=false;
	public DataInputStream 		input;
	public int  			   	attempts;
	public Hashtable		   	banned;
	public ChannelList  	   	channels;
	public MudList  		   	muds;
	public List<NameServer>    	name_servers;
	public int  			   	password;
	public NameServer		   	currentRouter;

	private Intermud(final ImudServices imud, PersistentPeer p)
	{
		super();
		intermud = imud;
		peer = p;
		peer.setPersistent(this);
		connected = false;
		password = -1;
		attempts = 0;
		input_thread = null;
		channels = new ChannelList(-1);
		muds = new MudList(-1);
		banned = new Hashtable();
		name_servers = new Vector();
		String s=CMProps.getVar(CMProps.Str.I3ROUTERS);
		final List<String> V=CMParms.parseCommas(s,true);
		for(int v=0;v<V.size();v++)
		{
			s=V.get(v);
			final List<String> V2=CMParms.parseAny(s,':',true);
			if(V2.size()>=3)
				name_servers.add(new NameServer(V2.get(0),CMath.s_int(V2.get(1)), V2.get(2)));
		}
		modified = Persistent.UNMODIFIED;
		try
		{
			restore();
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
				public int compareTo(CMObject o)
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

				@Override public boolean tick(Tickable ticking, int tickID)
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
								Log.errOut("I3SaveTick","No I3 response received in "+CMLib.time().date2EllapsedTime(ellapsedTime, TimeUnit.MILLISECONDS, false)+". Connected="+Intermud.isConnected());
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
	private synchronized void channelList(Vector packet)
	{
		final Hashtable list = (Hashtable)packet.elementAt(7);
		final Enumeration keys = list.keys();

		synchronized( channels )
		{
			channels.setChannelListId(((Integer)packet.elementAt(6)).intValue());
			while( keys.hasMoreElements() )
			{
				final Channel c = new Channel();
				Object ob;

				c.channel = (String)keys.nextElement();
				ob = list.get(c.channel);
				if( ob instanceof Integer )
				{
					removeChannel(c);
				}
				else
				{
					final Vector info = (Vector)ob;

					c.owner = (String)info.elementAt(0);
					if(info.elementAt(1) instanceof Integer)
						c.type = ((Integer)info.elementAt(1)).intValue();
					else
					if(info.elementAt(1) instanceof List)
						Log.errOut("InterMud","Received unexpected channel-reply: " + CMParms.toListString((List)info.elementAt(1)));
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
				if(CMProps.getVar(CMProps.Str.ADMINEMAIL).indexOf('@')<0)
					Log.errOut("Intermud","Please set ADMINEMAIL in your coffeemud.ini file.");
				final Vector connectionStatuses=new Vector(name_servers.size());
				for(int i=0;i<name_servers.size();i++)
				{
					currentRouter = name_servers.get(i);
					try
					{
						connection = new Socket(currentRouter.ip, currentRouter.port);
						output = new DataOutputStream(connection.getOutputStream());
						send("({\"startup-req-3\",5,\"" + intermud.getMudName() + "\",0,\"" +
							 currentRouter.name + "\",0," + password +
							 "," + muds.getMudListId() + "," + channels.getChannelListId() + "," + intermud.getMudPort() +
							 ",0,0,\""+intermud.getMudVersion()+"\",\""+intermud.getMudVersion()+"\",\""+intermud.getMudVersion()+"\",\"CoffeeMud\"," +
							 "\""+intermud.getMudState()+"\",\""+CMProps.getVar(CMProps.Str.ADMINEMAIL).toLowerCase()+"\",([" +
							 "\"who\":1,\"finger\":1,\"channel\":1,\"tell\":1,\"locate\":1,\"auth\":1,]),([]),})");
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
					final Enumeration e = intermud.getChannels();

					while( e.hasMoreElements() )
					{
						final String chan = (String)e.nextElement();

						send("({\"channel-listen\",5,\"" + intermud.getMudName() + "\",0,\"" +
								currentRouter.name + "\",0,\"" + chan + "\",1,})");
					}
					Log.sysOut("Intermud3","I3 client connection: "+currentRouter.ip+"@"+currentRouter.port);
					break;
				}
				if(!connected)
					for(int e=0;e<connectionStatuses.size();e++)
						Log.errOut("Intermud",(String)connectionStatuses.elementAt(e));
			}
		}
		catch( final Exception e )
		{
			try { Thread.sleep((attempts) * 100l); }
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
	private synchronized void error(Vector packet)
	{
		final Object target = packet.elementAt(5);
		final String msg = (String)packet.elementAt(7);

		if( target instanceof Integer )
		{
			I3Exception e;

			e = new I3Exception(msg);
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

	private synchronized void mudlist(Vector packet)
	{
		Hashtable list;
		Enumeration keys;

		synchronized( muds )
		{
			muds.setMudListId(((Integer)packet.elementAt(6)).intValue());
			list = (Hashtable)packet.elementAt(7);
			keys = list.keys();
			while( keys.hasMoreElements() )
			{
				final I3Mud mud = new I3Mud();
				Object info;

				mud.mud_name = (String)keys.nextElement();
				info = list.get(mud.mud_name);
				if( info instanceof Integer )
				{
					removeMud(mud);
				}
				else
				{
					final Vector v = (Vector)info;
					int total=0;
					for(int vi=0;vi<v.size();vi++)
					{
						if(v.elementAt(vi) instanceof String)
							total+=((String)v.elementAt(vi)).length();
					}
					if(total<1024)
					{
						mud.state = ((Integer)v.elementAt(0)).intValue();
						mud.address = (String)v.elementAt(1);
						mud.player_port = ((Integer)v.elementAt(2)).intValue();
						mud.tcp_port = ((Integer)v.elementAt(3)).intValue();
						mud.udp_port = ((Integer)v.elementAt(4)).intValue();
						mud.mudlib = (String)v.elementAt(5);
						mud.base_mudlib = (String)v.elementAt(6);
						mud.driver = (String)v.elementAt(7);
						mud.mud_type = (String)v.elementAt(8);
						mud.status = (String)v.elementAt(9);
						mud.admin_email = (String)v.elementAt(10);
						addMud(mud);
					}
				}
			}
		}
	}
			 // Hashtable services = (Hashtable)v.elementAt(11);
			 // Hashtable other_info = (Hashtable)v.elementAt(12);

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
			Vector data;

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
				try { new MudAuthRequest(I3Server.getMudName()).send(); } catch(final Exception e) { }
				final long ellapsedTime = System.currentTimeMillis() - intermud.getLastPacketReceivedTime();
				if(ellapsedTime>(60  * 60 * 1000)) // one hour
				{
					Log.errOut("Intermud","No I3 Ping received in "+CMLib.time().date2EllapsedTime(ellapsedTime, TimeUnit.SECONDS, false)+". Connected="+Intermud.isConnected());
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
						while(skipped<len)
							skipped+=input.skipBytes(len);
					}
					catch( final java.io.IOException e )
					{
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
						continue;
					}
				}
				cmd=new String(tmp);
			}
			catch( final java.io.IOException e )
			{
				data = null;
				cmd = null;
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
					data=(Vector)o;
				else
				{
					Log.errOut("InterMud","390-"+o);
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
			final String type = (String)data.elementAt(0);

			if( type.equals("channel-m") || type.equals("channel-e") || type.equals("channel-t") )
			{
				try
				{
					final ChannelPacket p = new ChannelPacket(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud","0-"+e.getMessage());
				}
			}
			else if( type.equals("chan-who-req") )
			{
				try
				{
					final ChannelWhoRequest p = new ChannelWhoRequest(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("chan-user-req") )
			{
				try
				{
					final ChannelUserRequest p = new ChannelUserRequest(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("channel-add") )
			{
				try
				{
					final ChannelAdd p = new ChannelAdd(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("channel-remove") )
			{
				try
				{
					final ChannelDelete p = new ChannelDelete(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("channel-listen") )
			{
				try
				{
					final ChannelListen p = new ChannelListen(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("chan-who-reply") )
			{
				try
				{
					final ChannelWhoReply p = new ChannelWhoReply(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("chan-user-reply") )
			{
				try
				{
					final ChannelUserReply p = new ChannelUserReply(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("chanlist-reply") )
			{
				channelList(data);
			}
			else if( type.equals("locate-reply") )
			{
				try
				{
					final LocateReplyPacket p = new LocateReplyPacket(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("finger-reply") )
			{
				try
				{
					final FingerReply p = new FingerReply(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("finger-req") )
			{
				try
				{
					final FingerRequest p = new FingerRequest(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("locate-req") )
			{
				try
				{
					final LocateQueryPacket p = new LocateQueryPacket(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("mudlist") )
			{
				mudlist(data);
			}
			else if( type.equals("startup-reply") )
			{
				startupReply(data);
			}
			else if( type.equals("tell") )
			{
				try
				{
					final TellPacket p = new TellPacket(data);

					intermud.receive(p);
				}
				catch( final InvalidPacketException e )
				{
					Log.errOut("Intermud",type+"-"+e.getMessage());
				}
			}
			else if( type.equals("who-req") )
			{
				final WhoPacket p = new WhoPacket(data);

				intermud.receive(p);
			}
			else if( type.equals("who-reply") )
			{
				final WhoPacket p = new WhoPacket(data);

				intermud.receive(p);
			}
			else if( type.equals("auth-mud-req") )
			{
				final MudAuthRequest p = new MudAuthRequest(data);

				intermud.receive(p);
			}
			else if( type.equals("auth-mud-reply") )
			{
				final MudAuthReply p = new MudAuthReply(data);

				intermud.receive(p);
			}
			else if( type.equals("error") )
			{
				error(data);
			}
			else if( type.equals("ucache-update") )
			{
				// i have NO idea what to do here
			}
			else
			{
				Log.errOut("Intermud","Other packet: " + type);
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
	public void send(Packet p)
	{
		send(p.toString());
	}

	// Send a formatted mud mode packet to the router
	private void send(String cmd)
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
	private synchronized void startupReply(Vector packet)
	{
		final Vector router_list = (Vector)packet.elementAt(6);

		if( router_list != null )
		{
			final Vector router = (Vector)router_list.elementAt(0);
			final NameServer name_server = name_servers.get(0);

			if( !name_server.name.equals(router.elementAt(0)) )
			{
				// create new name server and connect
				return;
			}
		}
		password = ((Integer)packet.elementAt(7)).intValue();
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
			 if(input!=null) input.close();
		}
		catch(final Exception e)
		{
		}
		try
		{
			 if(connection!=null) connection.close();
		}
		catch(final Exception e)
		{
		}
		if(save_thread!=null)
		{
			CMLib.threads().deleteTick(save_thread, -1);
			save_thread=null;
		}
		try { save(); }
		catch( final PersistenceException e ) { }
		try
		{
			 if(input_thread!=null) CMLib.killThread(input_thread,100,1); 
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
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.packets.ImudServices#getChannels
	 */
	public void addChannel(Channel c)
	{
		channels.addChannel(c);
	}

	/**
	 * Removes a channel from the channel list.
	 * @param c the channel to remove
	 */
	public void removeChannel(Channel c)
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
	public void setChannelList(ChannelList list)
	{
		channels = list;
	}

	/**
	 * Adds a mud to the list of known muds.
	 * @param m the mud to add
	 */
	public void addMud(I3Mud m)
	{
		muds.addMud(m);
		modified = Persistent.MODIFIED;
	}

	private I3Mud getMud(String mud_name)
	{
		return muds.getMud(getMudNameFor(mud_name));
	}

	/**
	 * Removed a mud from the list of known muds.
	 * @param m the mud to remove
	 */
	public void removeMud(I3Mud m)
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
	public void setMudList(MudList list)
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
	public void setPassword(int pass)
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

