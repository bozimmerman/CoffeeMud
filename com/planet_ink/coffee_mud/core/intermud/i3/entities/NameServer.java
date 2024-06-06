package com.planet_ink.coffee_mud.core.intermud.i3.entities;
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
public class NameServer implements Serializable
{
	public static final long serialVersionUID=0;

	public String	ip;
	public String	name;
	public int		port;
	public int		modified;

	public NameServer(final String addr, final int p, final String nom)
	{
		super();
		if(addr.startsWith("/"))
			ip = addr.substring(1);
		else
			ip = addr;
		port = p;
		name = nom;
	}

	public NameServer(final NameServer other)
	{
		super();
		ip = other.ip;
		port = other.port;
		name = other.name;
	}

	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof NameServer)
		{
			final NameServer n = (NameServer)o;
			return n.ip.equalsIgnoreCase(ip)
					&& n.name.equalsIgnoreCase(name)
					&& n.port == port;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return (((ip.hashCode() << 8) ^ (name.hashCode()) << 8)) ^ port;
	}
}
