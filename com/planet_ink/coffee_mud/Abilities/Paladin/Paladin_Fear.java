package com.planet_ink.coffee_mud.Abilities.Paladin;
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

/*
   Copyright 2002-2023 Bo Zimmerman

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
public class Paladin_Fear extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_Fear";
	}

	private final static String localizedName = CMLib.lang().L("Paladin`s Fear");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COMMUNING;
	}

	public Paladin_Fear()
	{
		super();
		paladinsGroup=new SHashSet<MOB>();
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY|Ability.FLAG_FEARING|Ability.FLAG_CHAOS;
	}

	protected volatile int fearDown = 5;

	@Override
	public boolean tick(final Tickable tickable, final int tickID)
	{
		if(!super.tick(tickable, tickID))
			return false;
		if(--fearDown>0)
			return true;
		fearDown = 10;
		final Physical P = affected;
		if(!(P instanceof MOB))
			return true;
		final MOB mob = (MOB)P;
		final Room R=mob.location();
		if((R==null)||(R.numInhabitants()<2))
			return true;
		final int chance = 10 + (adjustedLevel(mob,0)/5)+(2*super.getXLEVELLevel(mob));
		if((CMLib.dice().rollPercentage()>chance)
		||((mob.fetchAbility(ID())!=null)&&(!super.proficiencyCheck(mob, 0, false))))
			return true;
		final List<MOB> targets=new ArrayList<MOB>(R.numInhabitants());
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(mob.mayIFight(M))
			&&(!paladinsGroup.contains(M)))
				targets.add(M);
		}
		if(targets.size()==0)
			return true;
		final MOB targetM =targets.get(CMLib.dice().roll(1, targets.size(), -1));
		switch(CMLib.dice().roll(1, 4, 0))
		{
		case 1:
			targetM.enqueCommand(new XVector<String>("CRINGE",mob.name()), 0, 1.0);
			break;
		case 2:
			targetM.enqueCommand(new XVector<String>("COWER",mob.name()), 0, 1.0);
			break;
		case 3:
		{
			final HashMap<MOB,MOB> vicMap = new HashMap<MOB,MOB>();
			for(final MOB M : mob.getGroupMembers(new HashSet<MOB>()))
				vicMap.put(M, M.getVictim());
			for(final MOB M : targetM.getGroupMembers(new HashSet<MOB>()))
				vicMap.put(M, M.getVictim());
			final Room rR=mob.location();
			final Ability A=CMClass.getAbility("Spell_Spook");
			if(A!=null)
			{
				A.invoke(mob, targetM, true, 0);
				if((rR != mob.location())
				&&(!CMLib.flags().isMobile(targetM)))
					CMLib.tracking().markToWanderHomeLater(targetM);
			}
			for(final MOB M : vicMap.keySet())
				M.setVictim(vicMap.get(M));
			break;
		}
		case 4:
		{
			final HashMap<MOB,MOB> vicMap = new HashMap<MOB,MOB>();
			for(final MOB M : mob.getGroupMembers(new HashSet<MOB>()))
				vicMap.put(M, M.getVictim());
			for(final MOB M : targetM.getGroupMembers(new HashSet<MOB>()))
				vicMap.put(M, M.getVictim());
			final Ability A=CMClass.getAbility("Spell_Nightmare");
			if(A!=null)
				A.startTickDown(mob, targetM, 2);
			for(final MOB M : vicMap.keySet())
				M.setVictim(vicMap.get(M));
			break;
		}
		}
		return true;
	}
}
