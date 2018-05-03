package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Skill_FalseArrest extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_FalseArrest";
	}

	private final static String localizedName = CMLib.lang().L("False Arrest");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"FALSEARREST"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int overrideMana()
	{
		return 50;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(mob==target)
		{
			mob.tell(L("Arrest whom?!"));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		LegalBehavior B=null;
		Area A2=null;
		if(mob.location()!=null)
		{
			B=CMLib.law().getLegalBehavior(mob.location());
			if((B==null)||(!B.hasWarrant(CMLib.law().getLegalObject(mob.location()),target)))
				B=null;
			else
				A2=CMLib.law().getLegalObject(mob.location());
		}

		if(B==null)
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final Area A=e.nextElement();
			if(CMLib.flags().canAccess(mob,A))
			{
				B=CMLib.law().getLegalBehavior(A);
				if((B!=null)
				&&(B.hasWarrant(CMLib.law().getLegalObject(A),target)))
				{
					A2=CMLib.law().getLegalObject(A);
					break;
				}
			}
			B=null;
			A2=null;
		}

		if(B==null)
		{
			mob.tell(L("@x1 is not wanted for anything, anywhere.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;

		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(!success)
		{
			beneficialWordsFizzle(mob,target,L("<S-NAME> frown(s) at <T-NAMESELF>, but lose(s) the nerve."));
			return false;
		}
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,L("<S-NAME> frown(s) at <T-NAMESELF>."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(!B.arrest(A2,mob,target))
			{
				mob.tell(L("You are not able to arrest @x1 at this time.",target.name(mob)));
				return false;
			}
		}
		return success;
	}

}
