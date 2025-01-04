package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ExtAbility;
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
public class Fighter_SootheMount extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_SootheMount";
	}

	private final static String	localizedName	= CMLib.lang().L("Soothe Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedDisplayText	= CMLib.lang().L("(Soothe Mount)");

	@Override
	public String displayText()
	{
		return localizedDisplayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SOOTHE", "SOOTHEMOUNT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_MINDALTERING;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.target() instanceof MOB)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
		{
			final MOB target=(MOB)msg.target();
			if((!msg.source().isInCombat())
			&&(target.getVictim()!=msg.source())
			&&(target.location()==msg.source().location()))
			{
				msg.source().tell(L("You feel too soothed to do that."));
				if(msg.source().getVictim()==target)
				{
					msg.source().makePeace(true);
					msg.source().setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)
		&&(givenTarget ==null))
		{
			mob.tell(L("You must specify a target mount to soothe!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target==mob)
		{
			mob.tell(L("You are already soothed."));
			return false;
		}

		if(target.phyStats().level()>adjustedLevel(mob,asLevel)+(adjustedLevel(mob,asLevel)/5))
		{
			mob.tell(L("@x1 is a bit too powerful to soothe.",target.charStats().HeShe()));
			return false;
		}

		final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(mob);
		if(((!choices.containsSecond(target.baseCharStats().getMyRace()))
			&&(!choices.containsFirst(target.baseCharStats().getMyRace().racialCategory())))
		||(!CMLib.flags().isAnAnimal(target)))
		{
			mob.tell(L("@x1 is not the sort that would heed you.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> soothe(s) <T-NAME>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.combat().makePeaceInGroup(target);
				target.makePeace(true);
				target.setVictim(null);
				super.beneficialAffect(mob, target, asLevel, 10 + super.getXLEVELLevel(mob));
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to soothe <T-NAMESELF>, but fail(s)."));

		return success;
	}

}
