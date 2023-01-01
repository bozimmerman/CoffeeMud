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
   Copyright 2020-2023 Bo Zimmerman

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
public class DefaultTattooable implements Tattooable, CMCommon
{
	@Override
	public String ID()
	{
		return "DefaultTattooable";
	}

	@Override
	public String name()
	{
		return ID();
	}

	protected CMUniqNameSortSVec<Tattoo>	tattoos	= new CMUniqNameSortSVec<Tattoo>(1);

	@Override
	public Tattooable copyOf()
	{
		try
		{
			final DefaultTattooable copy = new DefaultTattooable();
			copy.tattoos.addAll(tattoos);
			return copy;
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
		return new DefaultTattooable();
	}

	@Override
	public void initializeClass()
	{
	}

	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(final String of)
	{
		final Tattoo T = (Tattoo) CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of));
	}

	@Override
	public void addTattoo(final String of, final int tickDown)
	{
		final Tattoo T = (Tattoo) CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of, tickDown));
	}

	@Override
	public void delTattoo(final String of)
	{
		final Tattoo T = findTattoo(of);
		if (T != null)
			tattoos.remove(T);
	}

	@Override
	public void addTattoo(final Tattoo of)
	{
		if ((of == null)
		|| (of.getTattooName() == null)
		|| (of.getTattooName().length() == 0)
		|| findTattoo(of.getTattooName()) != null)
			return;
		tattoos.addElement(of);
	}

	@Override
	public void delTattoo(final Tattoo of)
	{
		if ((of == null)
		|| (of.getTattooName() == null)
		|| (of.getTattooName().length() == 0))
			return;
		final Tattoo tat = findTattoo(of.getTattooName());
		if (tat == null)
			return;
		tattoos.remove(tat);
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return tattoos.elements();
	}

	@Override
	public Tattoo findTattoo(final String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		if (of.endsWith("*"))
			return tattoos.findStartsWith(of.substring(0, of.length() - 1));
		return tattoos.find(of.trim());
	}

	@Override
	public Tattoo findTattooStartsWith(final String of)
	{
		if ((of == null) || (of.length() == 0))
			return null;
		return tattoos.findStartsWith(of.trim());
	}

}
