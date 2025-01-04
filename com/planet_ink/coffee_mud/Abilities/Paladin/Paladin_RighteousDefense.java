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
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2025 Bo Zimmerman

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
public class Paladin_RighteousDefense extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_RighteousDefense";
	}

	private final static String localizedName = CMLib.lang().L("Righteous Defense");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	public Paladin_RighteousDefense()
	{
		super();
		paladinsGroup=new SHashSet<MOB>();
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY|Ability.FLAG_LAW;
	}

	protected volatile boolean oncePerRound = true;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		oncePerRound=true;
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.target() instanceof MOB)
		&&(msg.source()!=mob)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(oncePerRound)
		&&((!((MOB)msg.target()).isInCombat())||(!msg.source().isInCombat()))
		&&(!paladinsGroup.contains(msg.source()))
		&&(((MOB)msg.target()).location()==mob.location())
		&&(appropriateToMyFactions(mob))
		&&(((MOB)msg.target()).phyStats().level()<=mob.phyStats().level()-3)
		&&(!mob.isInCombat())
		&&(CMLib.flags().isGood((MOB)msg.target())||appropriateToMyFactions((MOB)msg.target()))
		&&(mob.mayIFight(msg.source()))
		&&(mob.actions()>=1.0)
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false))
		&&(msg.source().location()==mob.location())
		&&(CMLib.flags().isAliveAwakeMobileUnbound(msg.source(), true))
		&&((!CMLib.factions().isAlignmentLoaded(Align.LAWFUL))||(!CMLib.law().isLegalOfficerHere(msg.source()))))
		{
			if(mob.location().show(mob,msg.target(),this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> leap(s) to the righteous defense of <T-NAME>, standing between <T-HIM-HER> and @x1.",msg.source().name())))
			{
				oncePerRound=false;
				if(msg.source().mayIFight(mob)
				&&(msg.source().getVictim()==msg.target()))
					msg.source().setVictim(mob);
				msg.setTarget(mob);
				if(CMLib.factions().isAlignmentLoaded(Align.GOOD))
				{
					final int goodnes=CMLib.dice().roll(1,adjustedLevel(mob,0)/2,super.getXLEVELLevel(mob));
					CMLib.factions().postSkillFactionChange(mob,this, CMLib.factions().getAlignmentID(), goodnes);
				}
				if(CMLib.factions().isAlignmentLoaded(Align.LAWFUL))
				{
					final int lawness=CMLib.dice().roll(1,adjustedLevel(mob,0)/2,super.getXLEVELLevel(mob));
					CMLib.factions().postSkillFactionChange(mob,this, CMLib.factions().getInclinationID(), lawness);
				}
			}
		}
		return true;
	}
}
