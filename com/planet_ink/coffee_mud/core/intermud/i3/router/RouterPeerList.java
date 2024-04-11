package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3RMud;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.RNameServer;
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

/**
 * Copyright (c) 2024-2024 Bo Zimmerman
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
public class RouterPeerList implements Serializable, PersistentPeer
{
	public static final long serialVersionUID=0;

	private int id;
	protected final Map<String,RouterPeer> list;
	private int modified;

	private boolean isRestoring = false;
	private static final String restoreFilename = "resources/rpeers.I3Router";

	public RouterPeerList()
	{
		super();
		id = 0;
		modified = Persistent.MODIFIED;
		list = new Hashtable<String,RouterPeer>();
	}

	public RouterPeerList(final int i)
	{
		this();
		id = i;
	}

	public void addRouter(final RouterPeer router)
	{
		if(( router.name == null )||( router.name.length() == 0 ))
		{
			return;
		}
		{ // temp hack
			final char c = router.name.charAt(0);

			if( !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && c != '(' )
			{
				return;
			}
		}
		if( list.containsKey(router.name) )
		{
			router.modified = Persistent.MODIFIED;
		}
		else
		{
			router.modified = Persistent.NEW;
		}
		list.put(router.name, router);
		modified = Persistent.MODIFIED;
	}

	public RouterPeer getRouter(final String mud)
	{
		if( !list.containsKey(mud) )
		{
			return null;
		}
		final RouterPeer tmp = list.get(mud);

		if( tmp.modified == Persistent.DELETED )
		{
			return null;
		}
		return tmp;
	}

	public void removeRouter(final RouterPeer mud)
	{
		if( mud.name == null )
		{
			return;
		}
		mud.modified = Persistent.DELETED;
		modified = Persistent.MODIFIED;
	}

	public int getRouterListId()
	{
		return id;
	}

	public void setRouterListId(final int x)
	{
		id = x;
		this.modified = Persistent.MODIFIED;
	}

	public Map<String,RouterPeer> getRouters()
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
				final ByteArrayInputStream bin = new ByteArrayInputStream(F.raw());
				try(final ObjectInputStream din = new ObjectInputStream(bin))
				{
					if(din.available()>0)
					{
						this.id = din.readInt();
						final int numEntries = din.readInt();
						for(int i=0;i<numEntries;i++)
						{
							final RNameServer ns = (RNameServer)din.readObject();
							final RouterPeer rpeer = new RouterPeer(ns,null);
							this.list.put(ns.name, rpeer);
						}
					}
				}
			}
			this.modified = Persistent.UNMODIFIED;
		}
		catch(final Exception e)
		{
			Log.errOut("RouterPeerList",e);
		}
		finally
		{
			isRestoring = false;
		}
	}

	@Override
	public void save() throws PersistenceException
	{
		try
		{
			if(this.modified == Persistent.UNMODIFIED)
				return;
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
				for(final RouterPeer ns : list.values())
					if(ns.name.length()>0)
					{
						final RNameServer rns = new RNameServer(ns);
						out.writeObject(rns);
					}
			}
			bout.close();
			F.saveRaw(bout.toByteArray());
			this.modified = Persistent.MODIFIED;
		}
		catch(final Exception e)
		{
			Log.errOut("RouterPeerList","Unable to save "+restoreFilename);
			Log.errOut(e);
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

