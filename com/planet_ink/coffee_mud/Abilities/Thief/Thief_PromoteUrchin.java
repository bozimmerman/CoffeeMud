package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_PromoteUrchin extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PromoteUrchin";
	}

	private final static String localizedName = CMLib.lang().L("Promote Urchin");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	private static final String[] triggerStrings =I(new String[] {"PROMOTEURCHIN"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!target.getLiegeID().equals(mob.Name())
		||(target.fetchBehavior("Thiefness")==null)
		||(target.fetchBehavior("Scavenger")==null)
		||(!Thief_MyUrchins.isMyUrchin(target, mob))))
		{
			mob.tell(L("@x1 is not one of your urchins.",target.name(mob)));
			return false;
		}

		final int maxLevel = mob.phyStats().level() - 11 + super.getXLEVELLevel(mob);
		if((target.phyStats().level() >= maxLevel)
		||(target.getExpNeededLevel() == Integer.MAX_VALUE))
		{
			mob.tell(L("You can not promote @x1 any further.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int experienceToLose=getXPCOSTAdjustment(mob,100);
		final int amt = -CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
		if(amt > 0)
			mob.tell(L("You lose @x1 xp in the attempt.",""+amt));
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("<S-NAME> promote(s) <T-NAME>.");
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND,auto?"":str,str,str);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				try
				{
					target.setLiegeID(""); // prevent back-xp giving
					final int targetLevel = target.basePhyStats().level()+1;
					int tries = targetLevel*100;
					while((target.basePhyStats().level()<targetLevel)&&(--tries>0))
					{
						if((target.charStats().getCurrentClass().expless())
						||(target.charStats().getMyRace().expless())
						||(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0))
							CMLib.leveler().level(target);
						else
							CMLib.leveler().postExperience(target,"ABILITY:"+ID(),null,null,target.getExpNeededLevel()+1, false);
					}
				}
				finally
				{
					target.setLiegeID(mob.Name());
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to promote <T-NAME>, but <T-NAME> <T-IS-ARE> not believing it."));

		// return whether it worked
		return success;
	}
}
