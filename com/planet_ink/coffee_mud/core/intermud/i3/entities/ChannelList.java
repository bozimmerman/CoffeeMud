package com.planet_ink.coffee_mud.core.intermud.i3.entities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistentPeer;
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
import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
public class ChannelList implements Serializable, PersistentPeer
{
	public static final long serialVersionUID=0;
	private int id;
	private final Hashtable<String,Channel> list;

	private boolean isRestoring = false;
	private static final String restoreFilename = "resources/channels.I3Router";

	public ChannelList()
	{
		super();
		id = 0;
		list = new Hashtable<String,Channel>(10, 5);
	}

	public ChannelList(final int i)
	{
		this();
		id = i;
	}

	public void addChannel(final Channel c )
	{
		if( c.channel == null )
		{
			return;
		}
		list.put(c.channel, c);
	}

	public Channel getChannel(final String channel)
	{
		if( !list.containsKey(channel) )
		{
			return null;
		}
		return list.get(channel);
	}

	public void removeChannel(final Channel c)
	{
		if( c.channel == null )
		{
			return;
		}
		list.remove(c.channel);
	}

	public int getChannelListId()
	{
		return id;
	}

	public void setChannelListId(final int x)
	{
		id = x;
	}

	public Hashtable<String,Channel> getChannels()
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
					this.id = din.readInt();
					final int numEntries = din.readInt();
					for(int i=0;i<numEntries;i++)
					{
						final Channel cs = (Channel)din.readObject();
						this.list.put(cs.channel, cs);
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("NameServerList",e);
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
			final CMFile F=new CMFile(restoreFilename,null);
			if(!F.exists())
			{
				if(!F.getParentFile().exists())
					F.getParentFile().mkdirs();
			}
			try(ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream()))
			{
				out.writeInt(id);
				out.writeInt(list.size());
				for(final Channel ns : list.values())
					if(ns.channel.length()>0)
						out.writeObject(ns);
			}
		}
		catch(final Exception e)
		{
			Log.errOut("NameServerList",e);
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
