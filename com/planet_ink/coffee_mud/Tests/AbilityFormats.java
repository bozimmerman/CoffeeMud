package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2026-2026 Bo Zimmerman

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

/**
 * Tests every Ability that implements {@link Ability#miscTextFormat()} and then walks
 * the live world -- every room, the mobs and items in those rooms, the items
 * carried by those mobs, and the exits of those rooms -- and for any effect on
 * any of those objects whose Ability is in the set, verifies that the effect's
 * stored text still conforms to its declared format.
 *
 * @author Bo Zimmerman
 */
public class AbilityFormats extends StdTest
{
	@Override
	public String ID()
	{
		return "AbilityFormats";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[0]; // standalone -- not in "all" or any default group
	}

	private final String checkEffects(final Physical host, final Set<String> formatAbilities)
	{
		for(final Enumeration<Ability> f = host.effects(); f.hasMoreElements();)
		{
			final Ability A = f.nextElement();
			if((A == null) || (!formatAbilities.contains(A.ID())))
				continue;
			final String text = A.text();
			if((text == null) || (text.trim().length() == 0))
				continue;
			if(!CMLib.ableParms().isValidMiscText(A.miscTextFormat(), text))
				return ("'"+A.ID()+"' on "+host.Name()+" ("+host.ID()+") has non-conforming misc text: "+text);
		}
		return null;
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final Set<String> formatAbilities = new HashSet<String>();
		for(final Enumeration<Ability> e = CMClass.abilities(); e.hasMoreElements();)
		{
			final Ability A = e.nextElement();
			final String fmt = A.miscTextFormat();
			if((fmt != null) && (fmt.trim().length() > 0) && (!CMParms.FORMAT_UNDEFINED.equalsIgnoreCase(fmt.trim())))
				formatAbilities.add(A.ID());
		}
		for(final Enumeration<Room> r = CMLib.map().rooms(); r.hasMoreElements();)
		{
			final Room R = r.nextElement();
			final String roomErr = checkEffects(R, formatAbilities);
			if(roomErr != null)
				return ("Error#1: "+roomErr);
			for(int m = 0; m < R.numInhabitants(); m++)
			{
				final MOB M = R.fetchInhabitant(m);
				if(M == null)
					continue;
				final String mobErr = checkEffects(M, formatAbilities);
				if(mobErr != null)
					return ("Error#2: "+mobErr);
				for(int i = 0; i < M.numItems(); i++)
				{
					final Item I = M.getItem(i);
					if(I == null)
						continue;
					final String itemErr = checkEffects(I, formatAbilities);
					if(itemErr != null)
						return ("Error#3: "+itemErr);
				}
			}
			for(int i = 0; i < R.numItems(); i++)
			{
				final Item I = R.getItem(i);
				if(I == null)
					continue;
				final String itemErr = checkEffects(I, formatAbilities);
				if(itemErr != null)
					return ("Error#4: "+itemErr);
			}
			for(int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
			{
				final Exit X = R.getRawExit(d);
				if(X == null)
					continue;
				final String exitErr = checkEffects(X, formatAbilities);
				if(exitErr != null)
					return ("Error#5: "+exitErr);
			}
		}
		return null;
	}
}
