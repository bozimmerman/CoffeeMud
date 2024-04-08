package com.planet_ink.coffee_mud.core.intermud.i3.entities;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Copyright (c) 1996 George Reese, 2024-2024 Bo Zimmerman
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
public class NameServerList implements Serializable
{
	public static final long serialVersionUID=0;

	private int id;
	private final Hashtable<String,NameServer> list;
	private int modified;

	public NameServerList()
	{
		super();
		id = -1;
		modified = Persistent.MODIFIED;
		list = new Hashtable<String,NameServer>();
	}

	public NameServerList(final int i)
	{
		this();
		id = i;
	}

	public int getModified()
	{
		return modified;
	}

	public void setModified(final int x)
	{
		modified = x;
	}

	public void addNameServer(final NameServer mud)
	{
		if(( mud.name == null )||( mud.name.length() == 0 ))
		{
			return;
		}
		{ // temp hack
			final char c = mud.name.charAt(0);

			if( !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && c != '(' )
			{
				return;
			}
		}
		if( list.containsKey(mud.name) )
		{
			if(mud != list.get(mud.name))
				list.put(mud.name, mud);
			mud.modified = Persistent.MODIFIED;
		}
		else
		{
			list.put(mud.name, mud);
			mud.modified = Persistent.NEW;
		}
		modified = Persistent.MODIFIED;
	}

	public NameServer getNameServer(final String mud)
	{
		if( !list.containsKey(mud) )
		{
			return null;
		}
		final NameServer tmp = list.get(mud);

		if( tmp.modified == Persistent.DELETED )
		{
			return null;
		}
		return tmp;
	}

	public void removeNameServer(final NameServer mud)
	{
		if( mud.name == null )
		{
			return;
		}
		if((list.containsKey(mud.name))
		&&(list.get(mud.name)==mud))
			list.get(mud.name).modified = Persistent.DELETED;
		modified = Persistent.MODIFIED;
	}

	public int getNameServerListId()
	{
		return id;
	}

	public void setNameServerListId(final int x)
	{
		id = x;
	}

	public static Filterer<NameServer> nmFilter = new Filterer<NameServer>()
	{
		@Override
		public boolean passesFilter(final NameServer obj)
		{
			if(obj == null)
				return false;
			if(obj.modified == Persistent.DELETED)
				return false;
			return true;
		}
	};

	public  Iterator<NameServer> getNameServers()
	{
		return new FilteredIterator<NameServer>(list.values().iterator(), nmFilter);
	}
}

