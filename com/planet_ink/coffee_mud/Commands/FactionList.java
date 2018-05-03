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
 * Portions Copyright (c) 2004-2018 Bo Zimmerman
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
	public FactionList(){}

	private final String[] access=I(new String[]{"FACTIONS","FAC"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer(L("\n\r^HFaction Standings:^?^N\n\r"));
		boolean none=true;
		final XVector<String> list=new XVector<String>(mob.factions());
		list.sort();
		for (final String name : list)
		{
			final Faction F=CMLib.factions().getFaction(name);
			if((F!=null)&&(F.showInFactionsCommand()))
			{
				none=false;
				msg.append(formatFactionLine(name,mob.fetchFaction(name)));
			}
		}
		if(!mob.isMonster())
			if(none)
				mob.session().colorOnlyPrintln(L("\n\r^HNo factions apply.^?^N"));
			else
				mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

	public String formatFactionLine(String name,int faction)
	{
		final StringBuffer line=new StringBuffer();
		line.append("  "+CMStrings.padRight(CMStrings.capitalizeAndLower(CMLib.factions().getName(name).toLowerCase()),21)+" ");
		final Faction.FRange FR=CMLib.factions().getRange(name,faction);
		if(FR==null)
			line.append(CMStrings.padRight(""+faction,17)+" ");
		else
			line.append(CMStrings.padRight(FR.name(),17)+" ");
		line.append("[");
		line.append(CMStrings.padRight(calcRangeBar(name,faction),25));
		line.append("]\n\r");
		return line.toString();
	}

	public String calcRangeBar(String factionID, int faction)
	{
		final StringBuffer bar=new StringBuffer();
		final Faction F=CMLib.factions().getFaction(factionID);
		if(F==null)
			return bar.toString();
		double numLower=0;
		double numTotal=0;
		double pctThisFaction = 0;
		for(final Enumeration<Faction.FRange> r=F.ranges(); r.hasMoreElements();)
		{
			Faction.FRange range=r.nextElement();
			if(range.low() > faction)
				numLower+=1.0;
			numTotal+=1.0;
		}
		final Faction.FRange FR=F.fetchRange(faction);
		if((FR!=null)&&(FR.high() > FR.low()))
			pctThisFaction = (faction - FR.low()) / (FR.high() - FR.low());
		final double fillBit=(25.0 / numTotal);
		final double fill = (fillBit * (numTotal - numLower)) + (fillBit * pctThisFaction); 
		for(int i=0;i<fill;i++)
			bar.append("*");
		return bar.toString();
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
