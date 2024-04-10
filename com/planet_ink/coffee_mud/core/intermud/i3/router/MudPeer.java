package com.planet_ink.coffee_mud.core.intermud.i3.router;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.XArrayList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChanlistReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelAdd;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelAdmin;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelDelete;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelEmote;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelListen;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelMessage;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelTargetEmote;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelUserReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelUserRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelWhoReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ChannelWhoRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ErrorPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.FingerReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.FingerRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.InvalidPacketException;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnChanlistDelta;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnChanlistRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnData;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnMudlistDelta;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnMudlistRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnPing;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnShutdown;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnStartupRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.LocateQueryPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.LocateReplyPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.MudAuthRequest;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.MudPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.MudlistPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet.PacketType;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.PingPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.ShutdownPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReply;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReq3;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.TellPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.UCacheUpdate;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.WhoReplyPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.WhoReqPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.server.ServerObject;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class MudPeer implements ServerObject, PersistentPeer, NetPeer
{
	I3MudX					mud;
	boolean					isRestoring	= false;
	boolean					destructed	= false;
	private boolean			initialized	= false;
	public List<Channel>	listening	= new Vector<Channel>();
	public Socket			sock;
	public DataInputStream	in;
	public DataOutputStream	out;
	final long[]			timeoutCtr 	= new long[] {0};
	public final long		connectTime	= System.currentTimeMillis();

	public long lastPing = System.currentTimeMillis();
	public long lastPong = System.currentTimeMillis();

	public MudPeer(final String mudName, final Socket sock)
	{
		this.sock = sock;
		if(sock != null)
		{
			try
			{
				this.in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				this.out = new DataOutputStream(sock.getOutputStream());
				initialized = false;
				destructed = false;
			}
			catch (final IOException e)
			{
			}
		}
		mud = new I3MudX(mudName);
	}

	public MudPeer(final String mudName, final NetPeer peer)
	{
		if(peer != null)
		{
			this.sock = peer.getSocket();
			this.in = peer.getInputStream();
			this.out = peer.getOutputStream();
			peer.clearSocket();
		}
		mud = new I3MudX(mudName);
	}

	public void setMud(final I3MudX mud)
	{
		this.mud = mud;
	}

	public I3MudX getMud()
	{
		return this.mud;
	}

	/**
	 * Gets data about this peer from storage and gives it
	 * back to the object for which this peer exists.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if an error occurs during restore
	 */
	@Override
	public void restore() throws PersistenceException
	{
		isRestoring=true;
		try
		{
			final CMFile F=new CMFile("resources/mpeer."+getObjectId(),null);
			if(!F.exists())
				return;

			final ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(F.raw()));
			Object newobj;
			newobj=in.readObject();
			if(newobj instanceof I3MudX)
				this.mud = (I3MudX)newobj;
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer","Unable to read /resources/ppeer."+getObjectId());
		}
		finally
		{
			isRestoring=false;
		}
	}

	/**
	 * Triggers a save of its peer.  Implementing classes
	 * should do whatever it takes to save the object in
	 * this method.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if a problem occurs in saving
	 */
	@Override
	public void save() throws PersistenceException
	{
		try
		{
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final ObjectOutputStream out=new ObjectOutputStream(bout);
			out.writeObject(mud);
			out.flush();
			bout.flush();
			new CMFile("::resources/rpeer."+getObjectId(),null).saveRaw(bout.toByteArray());
			out.close();
			bout.close();
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer",e.getMessage());
		}
	}

	/**
	 * Assigns a persistent object to this peer for
	 * persistence operations.
	 * @param ob the implementation of com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent that this is a peer for
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent
	 */
	@Override
	public void setPersistent(final Persistent ob)
	{
	}

	/**
	 * An implementation uses this to tell its Persistent
	 * that it is in the middle of restoring.
	 * @return true if a restore operation is in progress
	 */
	@Override
	public boolean isRestoring()
	{
		return isRestoring;
	}

	@Override
	public void destruct()
	{
		destructed = true;
		initialized	= false;
		isRestoring	= false;
		try
		{
			if(in != null)
				in.close();
		}
		catch (final IOException e){ }
		try
		{
			if(out != null)
				out.close();
		}
		catch (final IOException e){ }
		try
		{
			if(sock != null)
				sock.close();
		}
		catch (final IOException e){ }
		I3Router.getRouter().muds.removeMud(this);
	}

	public void initialize()
	{
		if(initialized)
			return;
		initialized=true;
		destructed = false;
		isRestoring	= false;
		try
		{
			final StartupReply srep = new StartupReply(this.mud.mud_name);
			srep.password = this.mud.password; //what is this supposed to be?
			srep.send();

			final XArrayList<I3MudX> muds = new XArrayList<I3MudX>();
			muds.addAll(I3Router.getMudXPeers());
			for(final RouterPeer peer : I3Router.getRouterPeers())
			{
				for(final I3MudX mud : peer.muds.getMudXList())
					muds.add(mud);
			}
			for(int i=0;i<muds.size();i+=5)
			{
				final MudlistPacket mlrep = new MudlistPacket(this.mud.mud_name);
				mlrep.mudlist_id = I3Router.getMudListId();
				for(int x=i;x<i+5 && x<muds.size();x++)
					mlrep.mudlist.add(muds.get(x));
				mlrep.send();
			}
			final XArrayList<Channel> chans = new XArrayList<Channel>();
			chans.addAll(I3Router.getRouter().channels.getChannels().values());
			for(final RouterPeer peer : I3Router.getRouterPeers())
			{
				for(final Channel chan : peer.channels.getChannels().values())
					chans.add(chan);
			}
			for(int i=0;i<muds.size();i+=5)
			{
				final ChanlistReply clrep = new ChanlistReply(this.mud.mud_name);
				clrep.chanlist_id = I3Router.getChannelListId();
				for(int x=i;x<i+5 && x<chans.size();x++)
					clrep.chanlist.add(chans.get(x));
				clrep.send();
			}
		}
		catch (final InvalidPacketException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * route to the proper mud, or next router
	 *
	 * @param pkt
	 */
	private void routePacket(final MudPacket pkt)
	{
		if((pkt.target_mud == null)
		||(pkt.target_mud.length()==0))
			return;
		final MudPeer mud = I3Router.findMudPeer(pkt.target_mud);
		if(mud != null)
		{
			I3Router.writePacket(pkt);
			return;
		}
		try
		{
			final RouterPeer[] peers = I3Router.getRouterPeers();
			for(final RouterPeer peer : peers)
			{
				final I3MudX rmud = peer.muds.getMud(pkt.target_mud);
				if((rmud != null)
				&&(rmud.connected))
				{
					final IrnData dataPacket = new IrnData(peer.name, pkt);
					dataPacket.send();
					return;
				}
			}
			for(final RouterPeer peer : peers)
			{
				final I3MudX rmud = peer.muds.getMud(pkt.target_mud);
				if(rmud != null)
				{
					final IrnData dataPacket = new IrnData(peer.name, pkt);
					dataPacket.send();
					return;
				}
			}
			this.sendError("unk-dst", "Unknown mud '"+pkt.target_mud+"'", pkt);
			Log.errOut("Mud not found: "+pkt.target_mud);
			return;
		}
		catch (final InvalidPacketException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * send to all listening muds, and peer routers
	 * @param pkt
	 */
	private void sendChannelMessage(final MudPacket pkt)
	{
		String channel;
		switch(pkt.getType())
		{
		case CHANNEL_E:
			channel = ((ChannelEmote)pkt).channel;
			break;
		case CHANNEL_M:
			channel = ((ChannelMessage)pkt).channel;
			break;
		case CHANNEL_T:
			channel = ((ChannelTargetEmote)pkt).channel;
			break;
		default:
			Log.errOut("Unchanneled message type: "+pkt.getType().name());
			return;
		}
		final Channel chan = I3Router.findChannel(channel);
		if(chan == null)
		{
			sendError("unk-channel", "Unknown channel '"+channel+"'.", pkt);
			return;
		}
		for(final MudPeer peer : I3Router.getMudPeers())
		{
			if((peer != this)
			&&(peer.listening.contains(chan)))
				I3Router.writePacket(pkt, peer);
		}
		for(final RouterPeer peer : I3Router.getRouterPeers())
		{
			final IrnData chanData = new IrnData(peer.name, pkt);
			try
			{
				chanData.send();
			}
			catch (final InvalidPacketException e)
			{
			}
		}
	}

	public void channelAddRequest(final ChannelAdd pkt)
	{
		Channel c = I3Router.findChannel(pkt.channel);
		if (c != null)
			sendError("bad-channel","The channel "+pkt.channel+" already exists.",pkt);
		else
		{
			c = new Channel();
			c.channel = pkt.channel;
			c.modified = Persistent.MODIFIED;
			c.owner = pkt.sender_mud;
			c.type = pkt.channelType;
			I3Router.getRouter().channels.addChannel(c);
			I3Router.getRouter().channels.setChannelListId(I3Router.getChannelListId()+1);
			final IrnChanlistDelta delta = new IrnChanlistDelta("");
			delta.chanlist_id = I3Router.getChannelListId();
			delta.chanlist.add(c);
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				delta.target_router = rpeer.name;
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

	public void channelRemoveRequest(final ChannelDelete pkt)
	{
		final Channel c = I3Router.findChannel(pkt.channel);
		if (c == null)
			sendError("bad-channel","The channel "+pkt.channel+" doesnt exists.",pkt);
		else
		{
			I3Router.getRouter().channels.removeChannel(c);
			I3Router.getRouter().channels.setChannelListId(I3Router.getChannelListId()+1);
			c.modified = Persistent.DELETED;
			final IrnChanlistDelta delta = new IrnChanlistDelta("");
			delta.chanlist.add(c);
			delta.chanlist_id = I3Router.getChannelListId();
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				delta.target_router = rpeer.name;
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

	public void channelAdminRequest(final ChannelAdmin pkt)
	{
		final Channel c = I3Router.findChannel(pkt.channel);
		if (c == null)
			sendError("bad-channel","The channel "+pkt.channel+" doesnt exists.",pkt);
		else
		if(!c.owner.equals(mud.mud_name))
			sendError("not-allowed","The channel "+pkt.channel+" may not be altered by you.",pkt);
		else
		if((pkt.addlist.size()>0)
		||(pkt.removelist.size()>0))
		{
			c.mudlist.addAll(pkt.addlist);
			c.mudlist.removeAll(pkt.removelist);
			I3Router.getRouter().channels.setChannelListId(I3Router.getChannelListId()+1);
			final IrnChanlistDelta delta = new IrnChanlistDelta("");
			delta.chanlist_id = I3Router.getChannelListId();
			delta.chanlist.add(c);
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				delta.target_router = rpeer.name;
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

	public void sendError(final String errorCode, final String errorMessage, final Packet packet)
	{
		if(packet instanceof MudPacket)
			sendMudError(errorCode, errorMessage,(MudPacket)packet);
		else
		if(packet instanceof IrnPacket)
			sendIrnError(errorCode, errorMessage,(IrnPacket)packet);
	}

	public void sendMudError(final String errorCode, final String errorMessage, final MudPacket packet)
	{
		try
		{
			final ErrorPacket pkt = new ErrorPacket(mud.mud_name,packet.sender_name, errorCode, errorMessage, packet.toString());
			pkt.sender_mud = I3Router.getRouterName();
			pkt.send();
		}
		catch (final InvalidPacketException e)
		{
			Log.errOut(e);
		}
	}

	public void sendIrnError(final String errorCode, final String errorMessage, final IrnPacket packet)
	{
		try
		{
			final ErrorPacket pkt = new ErrorPacket(mud.mud_name,packet.sender_router, errorCode, errorMessage, packet.toString());
			pkt.sender_mud = I3Router.getRouterName();
			pkt.send();
		}
		catch (final InvalidPacketException e)
		{
			Log.errOut(e);
		}
	}

	public void channelListenRequest(final ChannelListen pkt)
	{
		final Channel c = I3Router.findChannel(pkt.channel);
		if (c == null)
			sendError("unk-channel","The channel "+pkt.channel+" is unknown.",pkt);
		else
		{
			if(pkt.onoff == 0)
				listening.remove(c);
			else
			switch(c.type)
			{
			case 1: // selective admission
			case 2: // selective admission & filtered
				if(!c.mudlist.contains(pkt.sender_mud))
					sendError("not-allowed", "Not allowed to listen to this channel.", pkt);
				else
					listening.add(c);
				break;
			default: // selective ban
				if(c.mudlist.contains(pkt.sender_mud))
					sendError("not-allowed", "Not allowed to listen to this channel.", pkt);
				else
					listening.add(c);
				break;
			}
		}
	}

	public void locateUserRequest(final LocateQueryPacket pkt)
	{
		for(final MudPeer peer : I3Router.getMudPeers())
		{
			if((peer != this)
			&&(peer.isConnected()))
				I3Router.writePacket(pkt, peer);
		}
		for(final RouterPeer peer : I3Router.getRouterPeers())
		{
			final IrnData chanData = new IrnData(peer.name, pkt);
			try
			{
				chanData.send();
			}
			catch (final InvalidPacketException e)
			{
			}
		}
	}

	public void startupRequest3(final StartupReq3 pkt)
	{
		if(((pkt.password == I3Router.getRouterPassword())
			|| (I3Router.getRouterPassword() < 0))
		&&(pkt.target_router.equalsIgnoreCase(I3Router.getRouterName())))
		{
			setMud(pkt.makeMud(this));
			final Random r = new Random(System.currentTimeMillis());
			for(final RouterPeer rpeer : I3Router.getRouterPeers())
			{
				final IrnMudlistDelta delta = new IrnMudlistDelta(rpeer.name);
				delta.mudlist_id = r.nextInt(Integer.MAX_VALUE/1000);
				delta.mudlist.add(mud);
				try
				{
					delta.send();
				}
				catch (final InvalidPacketException e)
				{
					Log.errOut(e);
				}
			}
			initialized = false; // force it to reply to the startup
		}
	}

	@Override
	public void processEvent()
	{
		if(!isConnected())
		{
			destruct();
			return;
		}
		final DataInputStream istream = getInputStream();
		if(istream == null)
		{
			destruct();
			return;
		}
		if(!initialized)
			initialize();

		try
		{
			final Packet pkt;
			if((pkt = I3Router.readPacket(this))==null)
			{
				final long now = System.currentTimeMillis();
				if(((now - this.lastPing) > 60000)
				&&((now - this.lastPong) > 60000))
				{
					final PingPacket ppkt = new PingPacket(mud.mud_name);
					ppkt.sender_mud = I3Router.getRouterName();
					try
					{
						ppkt.send();
					}
					catch (final InvalidPacketException e)
					{
						Log.errOut(e);
					}
				}
				return;
			}
			if(!(pkt instanceof MudPacket))
			{
				sendError("not-allowed", "Not allowed to send this packet.", pkt);
				Log.errOut("Unwanted message type: "+pkt.getType().name() + " from "+mud.mud_name);
				return;
			}
			lastPong = System.currentTimeMillis();
			final MudPacket mudpkt = (MudPacket)pkt;
			switch(mudpkt.getType())
			{
			case CHANNEL_ADD:
				channelAddRequest((ChannelAdd)pkt);
				break;
			case CHANNEL_LISTEN:
				channelListenRequest((ChannelListen)pkt);
				break;
			case CHANNEL_REMOVE:
				channelRemoveRequest((ChannelDelete)pkt);
				break;
			case CHANNEL_ADMIN:
				channelAdminRequest((ChannelAdmin)pkt);
				break;
			case LOCATE_REQ:
				locateUserRequest((LocateQueryPacket)pkt);
				break;
			case SHUTDOWN:
				{
					destruct();
					I3Router.removeObject(this);
					return;
				}
			case STARTUP_REQ_3:
				startupRequest3((StartupReq3)pkt);
				break;
			case CHANNEL_E:
			case CHANNEL_M:
			case CHANNEL_T:
				sendChannelMessage(mudpkt);
				break;
			case ERROR:
			case AUTH_MUD_REQ:
			case PING_REQ:
			case CHAN_USER_REPLY:
			case CHAN_USER_REQ:
			case CHAN_WHO_REPLY:
			case CHAN_WHO_REQ:
			case FINGER_REPLY:
			case FINGER_REQ:
			case LOCATE_REPLY:
			case TELL:
			case WHO_REPLY:
			case WHO_REQ:
				routePacket(mudpkt);
				break;
			case UCACHE_MUD_UPDATE: // ignore cache packets, just because
			case UCACHE_UPDATE: // ignore cache packets, just because
			case STARTUP_REPLY: // a mud can't send a startup reply to any other mud, or the router
			case CHANLIST_REPLY: // a mud can't send a channel-list to any other mud, or the router
			case MUDLIST: // a mud can't send a mudlist to any other mud, or the router
			case IRN_CHANLIST_DELTA:
			case IRN_CHANLIST_REQ:
			case IRN_DATA:
			case IRN_MUDLIST_DELTA:
			case IRN_MUDLIST_REQ:
			case IRN_PING:
			case IRN_SHUTDOWN:
			case IRN_STARTUP_REQUEST:
				sendError("not-allowed", "Not allowed to send this packet.", pkt);
				Log.errOut("Unwanted message type: "+pkt.getType().name() + " from "+mud.mud_name);
				return;
			}
		}
		catch (final IOException e)
		{
			destruct();
			Log.errOut(getObjectId(),e);
		}
	}

	@Override
	public boolean getDestructed()
	{
		return destructed;
	}

	@Override
	public String getObjectId()
	{
		return mud.mud_name;
	}

	@Override
	public void setObjectId(final String id)
	{
		mud.mud_name = id;
	}

	@Override
	public Socket getSocket()
	{
		return sock;
	}

	@Override
	public boolean isConnected()
	{
		final boolean conn = (sock != null) && (sock.isConnected());
		if(!conn)
			initialized	= false;
		return conn;
	}

	@Override
	public DataInputStream getInputStream()
	{
		return (isConnected()) ? in : null;
	}

	@Override
	public DataOutputStream getOutputStream()
	{
		return (isConnected()) ? out : null;
	}

	@Override
	public void clearSocket()
	{
		initialized	= false;
		isRestoring	= false;
		sock = null;
		in = null;
		out = null;
	}

	@Override
	public void close() throws IOException
	{
		if((sock != null)
		&&(isConnected()))
		{
			in.close();
			out.flush();
			out.close();
			sock = null;
			in = null;
			out = null;
		}
		initialized	= false;
		isRestoring	= false;
	}


	@Override
	public long getConnectTime()
	{
		return this.connectTime;
	}

	@Override
	public long[] getSockTimeout()
	{
		synchronized(this)
		{
			return timeoutCtr;
		}
	}
}
