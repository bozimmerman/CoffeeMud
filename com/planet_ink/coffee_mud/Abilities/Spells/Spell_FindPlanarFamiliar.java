package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Spell_FindPlanarFamiliar extends Spell_FindFamiliar
{

	@Override
	public String ID()
	{
		return "Spell_FindPlanarFamiliar";
	}

	private final static String localizedName = CMLib.lang().L("Find Planar Familiar");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Find Planar Familiar)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	protected volatile Race mixRace = null;

	protected Race getPlanarRace(final String planeName)
	{
		final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		planeA.setPlanarName(planeName);
		final Map<String,String> planeVars = planeA.getPlaneVars();
		if(planeVars.containsKey(PlanarVar.MIXRACE.toString()))
		{
			final String mixRace = planeVars.get(PlanarVar.MIXRACE.toString());
			final Race firstR=CMClass.getRace(mixRace);
			if(firstR!=null)
				return firstR;
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area areaA=R.getArea();
		final String currentPlane = CMLib.flags().getPlaneOfExistence(mob);
		if((currentPlane != null)
		&&(areaA instanceof SubArea))
		{
			final Race firstR = getPlanarRace(currentPlane);
			if(firstR!=null)
			{
				this.mixRace = firstR;
				return super.invoke(mob, commands, givenTarget, auto, asLevel);
			}
		}
		final Faction inclinationF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());
		final boolean doInclination = (inclinationF!=null)
								&& (mob.fetchFaction(CMLib.factions().getInclinationID())!=Integer.MAX_VALUE);
		final boolean doAlignment = (CMLib.factions().getFaction(CMLib.factions().getAlignmentID())!=null)
								&& (mob.fetchFaction(CMLib.factions().getAlignmentID())!=Integer.MAX_VALUE);
		final int inclination = (doInclination)?mob.fetchFaction(CMLib.factions().getInclinationID()):0;
		final int alignment = (doAlignment)?mob.fetchFaction(CMLib.factions().getAlignmentID()):0;
		final PlanarAbility A=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final Spell_Imprisonment imprison = new Spell_Imprisonment();
		int smallestAlignmentDiffs = Integer.MAX_VALUE;
		String smallestDiffPlane = null;
		for(final String planeName : A.getAllPlaneKeys())
		{
			final Race firstR = getPlanarRace(planeName);
			if(firstR!=null)
			{
				final int diff = imprison.getPlanarDiff(planeName, doAlignment, alignment, doInclination, inclination);
				if(diff < smallestAlignmentDiffs)
				{
					smallestAlignmentDiffs = diff;
					smallestDiffPlane = planeName;
					this.mixRace=firstR;
				}
			}
		}
		if(smallestDiffPlane == null)
		{
			for(final String planeName : A.getAllPlaneKeys())
			{
				final Race firstR = getPlanarRace(planeName);
				if(firstR!=null)
				{
					final int diff = imprison.getPlanarDiff(planeName, doAlignment, alignment, doInclination, inclination);
					if((diff < smallestAlignmentDiffs)
					||(smallestDiffPlane == null))
					{
						smallestAlignmentDiffs = diff;
						smallestDiffPlane = planeName;
						this.mixRace=firstR;
					}
				}
			}
		}
		return super.invoke(mob, commands, givenTarget, auto, asLevel);
	}

	@Override
	protected MOB determineMonster(final MOB caster, final int level)
	{
		final MOB M=super.determineMonster(caster, level);
		final Race R=CMLib.utensils().getMixedRace(mixRace.ID(), M.charStats().getMyRace().ID(), false);
		if(R==null)
		{
			caster.tell(L("This magic does not seem to work right in this place..."));
			return M;
		}
		final String oldraceName = M.baseCharStats().getMyRace().name().toLowerCase();
		M.baseCharStats().setMyRace(R);
		M.charStats().setMyRace(R);
		M.baseCharStats().getMyRace().startRacing(M,false);
		final String oldName=M.name().toLowerCase();
		final String newName = CMLib.english().startWithAorAn(R.name()).toLowerCase();
		M.setName(newName);
		int x=M.displayText().toLowerCase().indexOf(oldName);
		if(x>=0)
			M.setDisplayText(CMStrings.capitalizeFirstLetter(M.displayText().substring(0,x)+newName+M.displayText().substring(x+oldName.length())));
		else
		{
			x=M.displayText().toLowerCase().indexOf(oldraceName);
			if(x>=0)
				M.setDisplayText(CMStrings.capitalizeFirstLetter(M.displayText().substring(0,x)+R.name().toLowerCase()+M.displayText().substring(x+oldraceName.length())));
		}
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		M.resetToMaxState();
		M.text();
		return M;
	}
}
