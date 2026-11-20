package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;

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
 * Tests every Behavior that implements {@link Behavior#parmsFormat()} and then walks
 * the live world -- every area, area room, the mobs and items in those rooms, the
 * items carried by those mobs, and the exits of those rooms -- and for any behavior
 * on any of those objects whose Behavior is in the set, verifies that the behavior's
 * stored parms still conform to its declared format.
 *
 * Behaviors whose parms are empty or blank are skipped, the same way the ability
 * counterpart skips empty effect text; their formats are covered by Validparms.
 *
 * @author Bo Zimmerman
 */
public class BehaviorFormats extends StdTest
{
	@Override
	public String ID()
	{
		return "BehaviorFormats";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[0]; // standalone -- not in "all" or any default group
	}

	private final String checkBehaviors(final PhysicalAgent host, final Map<String, String> formatBehaviors)
	{
		for(final Enumeration<Behavior> b = host.behaviors(); b.hasMoreElements();)
		{
			final Behavior B = b.nextElement();
			if(B == null)
				continue;
			final String format = formatBehaviors.get(B.ID());
			if(format == null)
				continue;
			final String parms = B.getParms();
			if((parms == null) || (parms.trim().length() == 0))
				continue;
			if(!CMLib.ableParms().isValidMiscText(format, parms))
				return ("Behavior ID: '"+B.ID()+"' on `"+host.Name()+"` in `"+CMLib.map().getExtendedRoomID(CMLib.map().getStartRoom(host))+"` has non-conforming parms: "+parms);
		}
		return null;
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final Map<String, String> formatBehaviors = new HashMap<String, String>();
		final Set<String> allowed = new HashSet<String>();
		for(final String id : commands)
			allowed.add(id.toUpperCase().trim());
		for(final Enumeration<Behavior> e = CMClass.behaviors(); e.hasMoreElements();)
		{
			final Behavior B = e.nextElement();
			final String fmt = B.parmsFormat();
			if((fmt != null)
			&& (fmt.trim().length() > 0)
			&& (!CMParms.FORMAT_UNDEFINED.equalsIgnoreCase(fmt.trim()))
			&& ((allowed.size()==0)||(allowed.contains(B.ID().toUpperCase()))))
				formatBehaviors.put(B.ID(), fmt);
		}
		final StringBuilder errors = new StringBuilder("");
		for(final Enumeration<Area> a = CMLib.map().areas(); a.hasMoreElements();)
		{
			final Area A = a.nextElement();
			final String areaErr = checkBehaviors(A, formatBehaviors);
			if(areaErr != null)
				errors.append("Error#A: "+areaErr).append("\n\r");
		}
		for(final Enumeration<Room> r = CMLib.map().rooms(); r.hasMoreElements();)
		{
			final Room R = r.nextElement();
			final String roomErr = checkBehaviors(R, formatBehaviors);
			if(roomErr != null)
				errors.append("Error#R: "+roomErr).append("\n\r");
			for(int m = 0; m < R.numInhabitants(); m++)
			{
				final MOB M = R.fetchInhabitant(m);
				if(M == null)
					continue;
				final String mobErr = checkBehaviors(M, formatBehaviors);
				if(mobErr != null)
					errors.append("Error#M: "+mobErr).append("\n\r");
				for(int i = 0; i < M.numItems(); i++)
				{
					final Item I = M.getItem(i);
					if(I == null)
						continue;
					final String itemErr = checkBehaviors(I, formatBehaviors);
					if(itemErr != null)
						errors.append("Error#MI: "+itemErr).append("\n\r");
				}
			}
			for(int i = 0; i < R.numItems(); i++)
			{
				final Item I = R.getItem(i);
				if(I == null)
					continue;
				final String itemErr = checkBehaviors(I, formatBehaviors);
				if(itemErr != null)
					errors.append("Error#I: "+itemErr).append("\n\r");
			}
			for(int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
			{
				final Exit X = R.getRawExit(d);
				if(X == null)
					continue;
				final String exitErr = checkBehaviors(X, formatBehaviors);
				if(exitErr != null)
					errors.append("Error#E: "+exitErr).append("\n\r");
			}
		}
		if(errors.length()>0)
			return errors.toString().trim();
		return null;
	}
}
