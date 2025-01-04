package com.planet_ink.coffee_mud.Common;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
public class DefaultTattoo implements Tattoo
{
	@Override
	public String ID()
	{
		return "DefaultTattoo";
	}

	private long	expires		= 0;
	private String	tattooName	= "";

	@Override
	public String name()
	{
		return tattooName;
	}

	@Override
	public Tattoo set(final String name)
	{
		tattooName = name.toUpperCase().trim();
		return this;
	}

	@Override
	public Tattoo set(final String name, final int down)
	{
		tattooName = name.toUpperCase().trim();
		if(down == 0)
			expires = 0;
		else
			expires = System.currentTimeMillis() + (down * CMProps.getTickMillis());
		return this;
	}

	/**
	 * @param tickDown the tickDown
	 */
	@Override
	public final void setTickDown(final int tickDown)
	{
		if(tickDown == 0)
			expires = 0;
		else
			expires = System.currentTimeMillis() + (tickDown * CMProps.getTickMillis());
	}

	/**
	 * @return the tickDown
	 */
	@Override
	public final int getTickDown()
	{
		if(expires == 0)
			return 0;
		final long diff = expires - System.currentTimeMillis();
		if(diff <= 0)
			return 1;
		return (int)Math.round(Math.ceil(CMath.div(diff, CMProps.getTickMillis())));
	}

	/**
	 * @return the tattooName
	 */
	@Override
	public final String getTattooName()
	{
		return tattooName;
	}

	@Override
	public String toString()
	{
		return ((expires > 0) ? (getTickDown() + " ") : "") + tattooName;
	}

	@Override
	public Tattoo copyOf()
	{
		try
		{
			return (Tattoo) this.clone();
		}
		catch (final Exception e)
		{
			return this;
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if (o == null)
			return 1;
		return (this == o) ? 0 : this.ID().compareTo(o.ID());
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultTattoo();
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public long expirationDate()
	{
		return expires;
	}

	@Override
	public void setExpirationDate(final long dateTime)
	{
		expires = dateTime;
	}

	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof Tattoo)
			return this.tattooName.equals(((Tattoo)o).getTattooName());
		return false;
	}

	@Override
	public Tattoo parse(final String tattooCode)
	{
		if((tattooCode==null)
		||(tattooCode.length()==0))
			return this;
		tattooName=tattooCode;
		expires = 0;
		if(Character.isDigit(tattooName.charAt(0)))
		{
			final int x=tattooName.indexOf(' ');
			if(x>0)
			{
				final String tdstr = tattooName.substring(0,x).trim();
				if(CMath.isNumber(tdstr))
				{
					tattooName=tattooName.substring(x+1).trim();
					setTickDown(CMath.s_int(tdstr));
				}
			}
		}
		return this;
	}

	@Override
	public int hashCode()
	{
		return tattooName.hashCode();
	}
}
