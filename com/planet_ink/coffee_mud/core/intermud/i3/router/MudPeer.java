package com.planet_ink.coffee_mud.core.intermud.i3.router;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.net.NetPeer;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.InvalidPacketException;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.IrnMudlistDelta;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.MudlistPacket;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.Packet;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.StartupReply;
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
public class MudPeer extends NetPeer implements ServerObject, PersistentPeer
{
	I3MudX 			mud;
	boolean			isRestoring	= false;
	boolean			destructed	= false;
	private boolean	initialized	= false;

	public long lastPing = System.currentTimeMillis();
	public long lastPong = System.currentTimeMillis();

	public MudPeer(final String mudName, final Socket sock)
	{
		super(sock);
		mud = new I3MudX(mudName);
	}

	public MudPeer(final String mudName, final NetPeer peer)
	{
		super(peer);
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
			final StartupReply srep = new StartupReply(this.mud.mud_name);
			srep.password = this.mud.password; //TODO: what is this supposed to be?
			srep.send();

			final Random r = new Random(System.currentTimeMillis());
			final I3MudX[] muds = I3Router.getMudXPeers();
			for(int i=0;i<muds.length;i+=5)
			{
				final MudlistPacket mlrep = new MudlistPacket(this.mud.mud_name);
				mlrep.mudlist_id = r.nextInt(Integer.MAX_VALUE);
				for(int x=i;x<i+5 && x<muds.length;x++)
					mlrep.mudlist.add(muds[x]);
				mlrep.send();
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
}
