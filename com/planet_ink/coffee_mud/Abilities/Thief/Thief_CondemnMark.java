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
   Copyright 2020-2021 Bo Zimmerman

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
public class Thief_CondemnMark extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_CondemnMark";
	}

	private final static String localizedName = CMLib.lang().L("Condemn Mark");

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
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	private static final String[] triggerStrings =I(new String[] {"CONDEMNMARK"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	public MOB getMark(final MOB mob)
	{
		final Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}

	public int getMarkTicks(final MOB mob)
	{
		final Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}

	final LimitedTreeSet<MOB> recentCondemnations = new LimitedTreeSet<MOB>(60000L*60,100,true);

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getMark(mob);
		if(target==null)
		{
			mob.tell(L("You need to have marked someone before you can condemn him or her."));
			return false;
		}

		if(recentCondemnations.contains(target))
		{
			mob.tell(L("Accusing this mark again so soon strains credibility."));
			return false;
		}

		final Deity deityM=target.charStats().getMyDeity();
		if(deityM==null)
		{
			mob.tell(L("@x1 lack(s) a deity to condemn before.",target.name(mob)));
			return false;
		}

		if(getMarkTicks(mob)<(30-(adjustedLevel(mob,asLevel)/5)-super.getXLEVELLevel(mob)))
		{
			mob.tell(L("You must observe this mark longer to determine their sins."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=(target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)))*15);
		if(levelDiff<0)
			levelDiff=0;
		final boolean success=proficiencyCheck(mob,-levelDiff,auto);
		if(!success)
		{
			maliciousFizzle(mob,target,L("<S-NAME> attempt(s) condemn <T-NAMESELF>, but <S-IS-ARE> way too obvious."));
			return false;
		}

		final CMMsg msg=CMClass.getMsg(mob,target,this,
				CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MALICIOUS,L("<S-NAME> condemn(s) <T-NAMESELF> before @x1.",deityM.Name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final CMMsg eventMsg=CMClass.getMsg(target, deityM, null,
					CMMsg.MSG_HOLYEVENT, null, 
					CMMsg.MSG_HOLYEVENT, null, 
					CMMsg.NO_EFFECT, Deity.HolyEvent.CURSING.toString());
			mob.location().send(target, eventMsg);
		}
		return success;
	}

}
