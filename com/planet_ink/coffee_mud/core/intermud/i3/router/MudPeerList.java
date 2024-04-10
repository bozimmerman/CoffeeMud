package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

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
public class MudPeerList implements Serializable, PersistentPeer
{
	public static final long serialVersionUID=0;

	private int id;
	protected final Map<String,MudPeer> list;
	private int modified;

	private boolean isRestoring = false;
	private static final String restoreFilename = "resources/mpeers.I3Router";

	public MudPeerList()
	{
		super();
		id = 0;
		modified = Persistent.MODIFIED;
		list = new Hashtable<String,MudPeer>();
	}

	public MudPeerList(final int i)
	{
		this();
		id = i;
	}

	public void addMud(final MudPeer mud)
	{
		if(( mud.mud.mud_name == null )||( mud.mud.mud_name.length() == 0 ))
		{
			return;
		}
		{ // temp hack
			final char c = mud.mud.mud_name.charAt(0);

			if( !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && c != '(' )
			{
				return;
			}
		}
		if( list.containsKey(mud.mud.mud_name) )
		{
			mud.mud.modified = Persistent.MODIFIED;
		}
		else
		{
			mud.mud.modified = Persistent.NEW;
		}
		list.put(mud.mud.mud_name, mud);
		modified = Persistent.MODIFIED;
	}

	public MudPeer getMud(final String mud)
	{
		if( !list.containsKey(mud) )
		{
			return null;
		}
		final MudPeer tmp = list.get(mud);

		if( tmp.mud.modified == Persistent.DELETED )
		{
			return null;
		}
		return tmp;
	}

	public void removeMud(final MudPeer mud)
	{
		if( mud.mud.mud_name == null )
		{
			return;
		}
		mud.mud.modified = Persistent.DELETED;
		modified = Persistent.MODIFIED;
	}

	public int getMudListId()
	{
		return id;
	}

	public void setMudListId(final int x)
	{
		id = x;
		modified = Persistent.MODIFIED;
	}

	public Map<String,MudPeer> getMuds()
	{
		return list;
	}

	@Override
	public void restore() throws PersistenceException
	{
		if(isRestoring)
			return;
		isRestoring = true;
		try
		{
			final CMFile F=new CMFile(restoreFilename,null);
			if(F.exists())
			{
				try(final ObjectInputStream din = new ObjectInputStream(new ByteArrayInputStream(F.raw())))
				{
					if(din.available()>0)
					{
						this.id = din.readInt();
						final int numEntries = din.readInt();
						for(int i=0;i<numEntries;i++)
						{
							final I3MudX m = (I3MudX)din.readObject();
							final MudPeer rpeer = new MudPeer(m.mud_name,(Socket)null);
							rpeer.mud = m;
							m.state = 0; // mark as down
							m.connected = false; // mark as down
							m.modified = Persistent.MODIFIED;
							this.list.put(m.mud_name, rpeer);
						}
					}
				}
			}
			this.modified = Persistent.UNMODIFIED;
		}
		catch(final Exception e)
		{
			Log.errOut("RouterPeerList","Unable to read "+restoreFilename);
		}
		finally
		{
			isRestoring = false;
		}
	}

	@Override
	public void save() throws PersistenceException
	{
		if(this.modified == Persistent.UNMODIFIED)
			return;
		try
		{
			final CMFile F=new CMFile(restoreFilename,null);
			if(!F.exists())
			{
				if(!F.getParentFile().exists())
					F.getParentFile().mkdirs();
			}
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try(ObjectOutputStream out = new ObjectOutputStream(bout))
			{
				out.writeInt(id);
				out.writeInt(list.size());
				for(final MudPeer p : list.values())
					if(p.mud != null)
						out.writeObject(p.mud);
			}
			bout.close();
			F.saveRaw(bout.toByteArray());
			this.modified = Persistent.UNMODIFIED;
		}
		catch(final Exception e)
		{
			Log.errOut("MudPeerList","Unable to read "+restoreFilename);
		}
	}

	@Override
	public void setPersistent(final Persistent ob)
	{
	}

	@Override
	public boolean isRestoring()
	{
		return isRestoring;
	}
}

