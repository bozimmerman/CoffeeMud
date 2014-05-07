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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2014 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>  	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */

@SuppressWarnings("rawtypes")
public class FactionList extends StdCommand
{
	public FactionList(){}

	private final String[] access=_i(new String[]{"FACTIONS","FAC"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=new StringBuffer(_("\n\r^HFaction Standings:^?^N\n\r"));
		boolean none=true;
		final XVector<String> list=new XVector<String>(mob.fetchFactions());
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
				mob.session().colorOnlyPrintln(_("\n\r^HNo factions apply.^?^N"));
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
		final Double fill=Double.valueOf(CMath.div(CMLib.factions().getRangePercent(factionID,faction),4));
		for(int i=0;i<fill.intValue();i++)
		{
			bar.append("*");
		}
		return bar.toString();
	}


	@Override public boolean canBeOrdered(){return true;}


}
