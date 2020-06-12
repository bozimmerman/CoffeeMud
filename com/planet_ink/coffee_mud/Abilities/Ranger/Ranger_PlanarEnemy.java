package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Ranger_PlanarEnemy extends Ranger_Enemy1
{
	@Override
	public String ID()
	{
		return "Ranger_PlanarEnemy";
	}

	private final static String localizedName = CMLib.lang().L("Planar Enemy");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String pickAnEnemy(final MOB mob)
	{
		if(mob==null)
			return miscText;
		final PlanarAbility planeAble = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final String category;
		if(CMLib.flags().isEvil(mob))
			category="lower";
		else
		if(CMLib.flags().isGood(mob))
			category="higher";
		else
			category="";
		final List<String> choices = new ArrayList<String>();
		final List<String> choicesl = new ArrayList<String>();
		final List<String> choicesh = new ArrayList<String>();
		final int mobAlign = mob.fetchFaction(CMLib.factions().getAlignmentID());
		for(final String planeKey : planeAble.getAllPlaneKeys())
		{
			final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
			final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
			if(catStr != null)
			{
				final List<String> categories=CMParms.parseCommas(catStr.toLowerCase(), true);
				if((category.length()==0)||(!categories.contains(category)))
				{
					choicesl.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
					final int align=CMath.s_int(planeVars.get(PlanarVar.ALIGNMENT.toString()));
					if((align - mobAlign) > 15000)
						choices.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
					else
					if((align - mobAlign) > 10000)
						choicesh.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
				}
			}
		}
		if(choicesh.size()==0)
			choicesh.addAll(choicesl);
		if(choices.size()==0)
			choices.addAll(choicesh);
		if(choices.size()==0)
			choices.addAll(planeAble.getAllPlaneKeys());
		else
		{
			choicesl.clear();
			if(mob.fetchFaction(CMLib.factions().getInclinationID())!=Integer.MAX_VALUE)
			{
				final Faction myF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());
				final int myInclination=mob.fetchFaction(CMLib.factions().getInclinationID());
				final Faction.FRange myFR = myF.fetchRange( myInclination);
				for(final String planeKey : choices)
				{
					final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
					final String factions = planeVars.get(PlanarVar.FACTIONS.toString());
					if(factions!=null)
					{
						final PairList<String,String> factionList=new PairVector<String,String>(CMParms.parseSpaceParenList(factions));
						for(final Pair<String,String> p : factionList)
						{
							final String factionName = p.first;
							if(p.first.equals("*"))
								continue;
							Faction F=null;
							if(CMLib.factions().isFactionID(factionName))
								F=CMLib.factions().getFaction(factionName);
							if(F==null)
								F=CMLib.factions().getFactionByName(factionName);
							if((F!=null)
							&&(F.factionID().equalsIgnoreCase(CMLib.factions().getInclinationID())))
							{
								final Faction.FRange FR;
								if(CMath.isInteger(p.second))
									FR=F.fetchRange(CMath.s_int(p.second));
								else
									FR = F.fetchRange(p.second);
								if(FR==myFR)
									choicesl.add(planeKey);
							}
						}
					}
				}
			}
			if(choicesl.size()>0)
			{
				choices.clear();
				choices.addAll(choicesl);
			}
		}
		if(choices.size()==0)
			return "";
		return CMStrings.capitalizeAllFirstLettersAndLower(choices.get(CMLib.dice().roll(1, choices.size(), -1)));
	}

	protected volatile Area lastArea = null;
	protected volatile boolean isEnemyArea = false;

	protected boolean isTheEnemy(final MOB mob)
	{
		if(mob != null)
		{
			final Area curArea = (mob.getStartRoom()!=null) ? mob.getStartRoom().getArea() : null;
			if(curArea != lastArea)
			{
				lastArea = curArea;
				final String itsPlane = CMLib.flags().getPlaneOfExistence(curArea);
				isEnemyArea = (itsPlane != null) && itsPlane.equalsIgnoreCase(text());
			}
			return isEnemyArea;
		}
		return false;
	}
}
