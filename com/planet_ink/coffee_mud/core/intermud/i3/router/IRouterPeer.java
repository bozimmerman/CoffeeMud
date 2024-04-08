package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

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
public class IRouterPeer extends NameServer implements RouterPeer
{
	private static final long serialVersionUID = 1L;

	boolean				isRestoring	= false;
	boolean				destructed	= false;
	ChannelList			channels	= new ChannelList();
	Map<String,I3MudX>	muds		= new Hashtable<String,I3MudX>();
	DataInputStream		in			= null;
	DataOutputStream	out			= null;
	Socket				sock		= null;
	SocketAddress		address		= null;
	int					password	= 0;
	long				lastPing	= System.currentTimeMillis();
	long				lastPong	= System.currentTimeMillis();
	int					mudListId	= 0;
	int					chanListId	= 0;
	boolean				initialized	= false;

	public IRouterPeer(final String addr, final int p, final String nom)
	{
		super(addr,p,nom);
	}
	public IRouterPeer(final NameServer srvr, final NetPeer peer, final I3RouterThread baseRouter)
	{
		super(srvr.ip, srvr.port, srvr.name);
		this.sock = peer.sock;
		this.in = peer.getInputStream();
		this.out = peer.getOutputStream();
		peer.sock = null;
		peer.in = null;
		peer.out = null;
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
			final CMFile F=new CMFile("resources/rpeer."+getObjectId(),null);
			if(!F.exists())
				return;

			final ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(F.raw()));
			Object newobj;
			newobj=in.readObject();
			if(newobj instanceof Integer)
			{
				password = ((Integer)newobj).intValue();
				newobj=in.readObject();
				if(newobj instanceof ChannelList)
				{
					channels=(ChannelList)newobj;
					newobj=in.readObject();
					if(newobj instanceof Map)
					{
						@SuppressWarnings("unchecked")
						final Map<String,I3MudX> mudlist = (Map<String,I3MudX>)newobj;
						muds=mudlist;
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer","Unable to read /resources/ppeer."+getObjectId());
		}
		isRestoring=false;
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
			out.writeObject(Integer.valueOf(password));
			out.writeObject(channels);
			out.writeObject(muds);
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
	}

	public void initialize()
	{
		if(initialized)
			return;
		initialized=true;
		try
		{
			final Random r = new Random(System.currentTimeMillis());
			final I3MudX[] muds = I3Router.getMudXPeers();
			for(int i=0;i<muds.length;i+=5)
			{
				final IrnMudlistDelta mlrep = new IrnMudlistDelta(this.name);
				mlrep.mudlist_id = r.nextInt(Integer.MAX_VALUE);
				for(int x=i;x<i+5 && x<muds.length;x++)
					mlrep.mudlist.add(muds[x]);
				mlrep.send();
			}

			final List<Channel> channels = new XArrayList<Channel>(I3Router.getRouter().channels.getChannels().values());
			for(int i=0;i<channels.size();i+=5)
			{
				final IrnChanlistDelta clrep = new IrnChanlistDelta(this.name);
				clrep.chanlist_id = r.nextInt(Integer.MAX_VALUE);
				for(int x=i;x<i+5 && x<channels.size();x++)
					clrep.chanlist.add(channels.get(x));
				clrep.send();
			}
		}
		catch (final InvalidPacketException e)
		{
			e.printStackTrace();
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
			if((pkt = I3Router.readPacket(istream))==null)
			{
				//TODO: deal with unpinged, or pings
				return;
			}
			switch(pkt.getType())
			{
			case IRN_STARTUP_REQUEST:
			case IRN_MUDLIST_REQ:
			case IRN_MUDLIST_DELTA:
			case IRN_CHANLIST_REQ:
			case IRN_CHANLIST_DELTA:
			case IRN_PING:
			case IRN_SHUTDOWN:
				break;
			case IRN_DATA:
				break;
			default:
				Log.errOut("Unexpected message type: "+pkt.getType().name());
				break;
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
		return this.name;
	}

	@Override
	public void setObjectId(final String id)
	{
		name = id;
	}

	@Override
	public void connect()
	{
		if(sock != null)
		{
			if(!sock.isConnected())
			{
				try
				{
					sock.connect(address);
					in = new DataInputStream(sock.getInputStream());
					out = new DataOutputStream(sock.getOutputStream());
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setSocket(final Socket s) throws IOException
	{
		sock = s;
		if(address == null)
			address = s.getRemoteSocketAddress();
		in = new DataInputStream(sock.getInputStream());
		out = new DataOutputStream(sock.getOutputStream());
	}

	@Override
	public boolean isConnected()
	{
		return (sock != null) && (sock.isConnected());
	}

	@Override
	public DataInputStream getInputStream()
	{
		return in;
	}

	@Override
	public DataOutputStream getOutputStream()
	{
		return out;
	}
}
