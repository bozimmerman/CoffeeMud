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
   Copyright 2015-2018 Bo Zimmerman

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

	private int		tickDown	= 0;
	private String	tattooName	= "";

	@Override
	public String name()
	{
		return tattooName;
	}

	@Override
	public Tattoo set(String name)
	{
		tattooName = name.toUpperCase().trim();
		return this;
	}

	@Override
	public Tattoo set(String name, int down)
	{
		tattooName = name.toUpperCase().trim();
		tickDown = down;
		return this;
	}

	/**
	 * @return the tickDown
	 */
	@Override
	public final int getTickDown()
	{
		return tickDown;
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
		return ((tickDown > 0) ? (tickDown + " ") : "") + tattooName;
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
	public int compareTo(CMObject o)
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
	public int tickDown()
	{
		return --tickDown;
	}

	@Override
	public Tattoo parse(final String tattooCode)
	{
		if((tattooCode==null)
		||(tattooCode.length()==0))
			return this;
		tattooName=tattooCode;
		if(Character.isDigit(tattooName.charAt(0)))
		{
			final int x=tattooName.indexOf(' ');
			if((x>0)
			&&(CMath.isNumber(tattooName.substring(0,x).trim())))
			{
				tickDown=CMath.s_int(tattooName.substring(0,x));
				tattooName=tattooName.substring(x+1).trim();
			}
		}
		return this;
	}
}
