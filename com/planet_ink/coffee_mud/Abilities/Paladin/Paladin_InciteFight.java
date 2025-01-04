package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class Paladin_InciteFight extends StdAbility
{
	@Override
	public String ID()
	{
		return "Paladin_InciteFight";
	}

	private final static String localizedName = CMLib.lang().L("Incite Fight");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"INCITEFIGHT","INCITE"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	protected Behavior aggroB = null;

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if(aggroB != null)
			aggroB.executeMsg(affecting, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(aggroB != null)
		{
			if(aggroB.tick(ticking, tickID))
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canBeTaughtBy(final MOB teacher, final MOB student)
	{
		if(!super.canBeTaughtBy(teacher, student))
			return false;
		if(!this.appropriateToMyFactions(student))
		{
			teacher.tell(L("@x1 lacks the moral disposition to learn '@x2'.",student.name(), name()));
			student.tell(L("You lack the moral disposition to learn '@x1'.",name()));
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!PaladinSkill.paladinAlignmentCheck(this, mob, auto))
			return false;

		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(target.isInCombat())
		{
			mob.tell(L("@x1 is already fighting someone!",target.name(mob)));
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,
					auto?L("<T-NAME> exude(s) an aura of inciteful rage."):L("<S-NAME> incite(s) <T-NAME> to fight."));
			final boolean makePeace = target.getVictim()==null;
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Paladin_InciteFight A = (Paladin_InciteFight)maliciousAffect(mob, target, asLevel, 0, CMMsg.TYP_MIND);
				if(A!=null)
				{
					A.aggroB = CMClass.getBehavior("Aggressive");
					A.aggroB.startBehavior(target);
					A.aggroB.setParms("MOBKILL");
				}
				else
					success = false;
				if(makePeace)
				{
					target.makePeace(true);
					final MOB victim=target.getVictim();
					if((victim!=null)
					   &&(victim.getVictim()==target))
						victim.makePeace(true);
				}
			}
		}
		else
			maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to incite <T-NAME>'s fight, but fail(s)."));

		// return whether it worked
		return success;
	}
}
