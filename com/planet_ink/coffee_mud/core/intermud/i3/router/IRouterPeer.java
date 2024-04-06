package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
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
public class IRouterPeer implements RouterPeer
{
	boolean			isRestoring	= false;
	boolean			destructed	= false;
	ChannelList		channels	= new ChannelList();
	MudList			muds		= new MudList();
	DataInputStream in			= null;
	DataOutputStream out		= null;
	Socket			sock		= null;
	SocketAddress	address		= null;
	String			password	= "";
	String			myID		= "";
	String			adminEmail	= "";

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
			final CMFile F=new CMFile("resources/rpeer."+myID,null);
			if(!F.exists())
				return;

			final ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(F.raw()));
			Object newobj;
			newobj=in.readObject();
			if(newobj instanceof String)
			{
				password = (String)newobj;
				newobj=in.readObject();
				if(newobj instanceof ChannelList)
				{
					channels=(ChannelList)newobj;
					newobj=in.readObject();
					if(newobj instanceof MudList)
					{
						muds=(MudList)newobj;
						newobj=in.readObject();
						if(newobj instanceof String)
							adminEmail = (String)newobj;
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer","Unable to read /resources/ppeer."+myID);
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
			out.writeObject(password);
			out.writeObject(channels);
			out.writeObject(muds);
			out.writeObject(adminEmail);
			out.flush();
			bout.flush();
			new CMFile("::resources/rpeer."+myID,null).saveRaw(bout.toByteArray());
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
		myID=ob.getClass().getName().substring(ob.getClass().getName().lastIndexOf('.')+1);
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

	@Override
	public void processEvent()
	{
	}

	@Override
	public boolean getDestructed()
	{
		return destructed;
	}

	@Override
	public String getObjectId()
	{
		return myID;
	}

	@Override
	public void setObjectId(final String id)
	{
		myID = id;
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
