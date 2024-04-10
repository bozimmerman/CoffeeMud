package com.planet_ink.coffee_mud.core.intermud.i3.entities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
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
public class ChannelList implements Serializable
{
	public static final long serialVersionUID=0;
	private int id;
	private final Hashtable<String,Channel> list;

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
}
