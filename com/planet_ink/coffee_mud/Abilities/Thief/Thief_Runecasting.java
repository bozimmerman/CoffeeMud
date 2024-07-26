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
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
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
public class Thief_Runecasting extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Runecasting";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Runecasting)");
		else
			return "";
	}

	private final static String localizedName = CMLib.lang().L("Runecasting");

	@Override
	public String name()
	{
		return localizedName;
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
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"RUNECASTING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected int tickUp = 0;
	protected MOB forM = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected == invoker())
		{
			if((forM == null)
			||(!CMLib.flags().isInTheGame(forM, true))
			||(invoker()==null)
			||(forM.location() != invoker().location()))
			{
				invoker().tell(L("I guess @x1 didn't really care.",forM.name()));
				unInvoke();
				return false;
			}
			tickUp++;
			if(tickUp >= 3)
			{

			}
			else
			if(tickUp >= 2)
			{

			}
			else
			{

			}
		}
		return true;
	}

	protected AutoProperties getApplicableAward(final MOB mob, final Filterer<AutoProperties> filter)
	{
		for(final Enumeration<AutoProperties> p = CMLib.awards().getAutoProperties();p.hasMoreElements();)
		{
			final AutoProperties P = p.nextElement();
			if((filter.passesFilter(P))
			&&(CMLib.masking().maskCheck(P.getPlayerCMask(), mob, true)))
			{
				//TODO:
			}
		}
		return null;
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=super.getTarget(mob, commands, givenTarget, false, false);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob, target,this,CMMsg.MSG_THIEF_ACT,
					auto?L("<T-NAME> has a runecasting vision!"):
					L("<S-NAME> cast(s) <S-HIS-HER> rune cubes for <T-NAME>..."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Thief_Runecasting rA = (Thief_Runecasting)beneficialAffect(mob, target, asLevel, 0);
				if(rA != null)
				{
					rA.forM = target;
					rA.tickUp = 0;
				}
			}
		}
		else
			beneficialVisualFizzle(mob, target,L("<S-NAME> cast(s) rune cubes for <T-NAMESELF, but is confused."));
		return success;
	}
}
