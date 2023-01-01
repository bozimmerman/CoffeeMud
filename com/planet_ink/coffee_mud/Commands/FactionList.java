package com.planet_ink.coffee_mud.Commands;
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

import java.util.*;
/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2023 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class FactionList extends StdCommand
{
	public FactionList()
	{
	}

	private final String[] access=I(new String[]{"FACTIONS","FAC"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer(L("\n\r^HFaction Standings:^?^N\n\r"));
		boolean none=true;
		final String args = CMParms.combine(commands,1).toUpperCase();
		final Enumeration<String> factions;
		if(args.length()==0)
			factions=mob.factions();
		else
		{
			final Faction F=CMLib.factions().getFactionByName(args);
			if(F!=null)
				factions=new XVector<String>(F.factionID()).elements();
			else
			{
				final Vector<String> facsV=new XVector<String>();
				for(final Enumeration<String> s=mob.factions();s.hasMoreElements();)
				{
					final String fid=s.nextElement();
					final Faction.FRange rF=CMLib.factions().getRange(fid,mob.fetchFaction(fid));
					if((rF!=null)
					&&(rF.name().toUpperCase().startsWith(args)))
						facsV.addElement(fid);
				}
				if(facsV.size()>0)
					factions=facsV.elements();
				else
				{
					factions = new FilteredEnumeration<String>(mob.factions(),new Filterer<String>() {
						@Override
						public boolean passesFilter(final String obj)
						{
							final Faction F=CMLib.factions().getFaction(obj);
							if((F!=null)
							&&(CMLib.english().containsString(F.name(), args)))
								return true;
							return false;
						}
					});
				}
			}
		}

		final XVector<String> list=new XVector<String>(factions);
		list.sort();
		final int prowessCode = CMProps.getIntVar(CMProps.Int.COMBATPROWESS);
		final boolean useFactionWords=CMProps.Int.Prowesses.FACTION_RANGE.is(prowessCode);
		final int[] cols={
				CMLib.lister().fixColWidth(28,mob.session()),
				CMLib.lister().fixColWidth(17,mob.session()),
				CMLib.lister().fixColWidth(25,mob.session())
			};
		for (final String name : list)
		{
			final Faction F=CMLib.factions().getFaction(name);
			if((F!=null)&&(F.showInFactionsCommand()))
			{
				none=false;
				msg.append(formatFactionLine(cols,name,mob.fetchFaction(name),useFactionWords));
			}
		}
		if(!mob.isMonster())
			if(none)
				mob.session().colorOnlyPrintln(L("\n\r^HNo factions apply.^?^N"));
			else
				mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

	public String formatFactionLine(final int[] cols, final String name, final int faction, final boolean showWords)
	{
		final StringBuffer line=new StringBuffer();
		line.append("  "+CMStrings.padRight(CMStrings.capitalizeAllFirstLettersAndLower(CMLib.factions().getName(name).toLowerCase()),cols[0])+" ");
		final Faction.FRange FR=CMLib.factions().getRange(name,faction);
		if(FR==null)
			line.append(CMStrings.padRight(""+faction,cols[1])+" ");
		else
		{
			if(showWords)
				line.append(CMStrings.padRight(FR.name(),cols[1])+" ");
			else
				line.append(CMStrings.padRight(FR.name()+" ("+faction+")",cols[1])+" ");
		}
		line.append("[");
		line.append(CMStrings.padRight(calcRangeBar(name,faction),cols[2]));
		line.append("]\n\r");
		return line.toString();
	}

	public String calcRangeBar(final String factionID, final int faction)
	{
		final StringBuffer bar=new StringBuffer();
		final Faction F=CMLib.factions().getFaction(factionID);
		if(F==null)
			return bar.toString();
		final double totalRange = F.maximum() - F.minimum();
		if(totalRange == 0)
			return "";
		final double absFaction = faction-F.minimum();
		final double pct = absFaction/totalRange;
		final int numStars = (int)Math.round(pct * 25.0);
		return CMStrings.repeat('*', numStars);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
