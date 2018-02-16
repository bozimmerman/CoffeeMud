package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Skill_Swim extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Swim";
	}

	private final static String	localizedName	= CMLib.lang().L("Swim");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Swimming)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SWIM" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public double castingTime(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSkillActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFABLETIME), 50.0), 1.0));
	}

	@Override
	public double combatCastingTime(final MOB mob, final List<String> cmds)
	{
		return CMProps.getSkillCombatActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMABLETIME), 50.0), 1.0));
	}

	public boolean placeToSwim(Room r2)
	{
		if((r2==null)
		||(!CMLib.flags().isWateryRoom(r2)))
			return false;
		return true;
	}

	public boolean placeToSwim(Environmental E)
	{
		return placeToSwim(CMLib.map().roomLocation(E));
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}

	@Override
	public int[] usageCost(MOB mob, boolean ignoreClassOverride)
	{
		int[] cost = super.usageCost(mob, ignoreClassOverride);
		if((mob != null)&&(mob.isRacialAbility(ID())))
			return new int[cost.length];
		return cost;
	}

	@Override
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		if(secondsElapsed==0)
		{
			final int dirCode=CMLib.directions().getDirectionCode(CMParms.combine(commands,0));
			if(dirCode<0)
			{
				mob.tell(L("Swim where?"));
				return false;
			}
			final Room r=mob.location().getRoomInDir(dirCode);
			if(CMLib.flags().isFloatingFreely(mob))
			{
				// swimming in no grav is OK
			}
			else
			if(!placeToSwim(mob.location()))
			{
				if(!placeToSwim(r))
				{
					mob.tell(L("There is no water to swim on that way."));
					return false;
				}
			}
			else
			if((r!=null)
			&&(r.domainType()==Room.DOMAIN_OUTDOORS_AIR)
			&&(r.domainType()==Room.DOMAIN_INDOORS_AIR))
			{
				mob.tell(L("There is no water to swim on that way."));
				return false;
			}

			if((mob.riding()!=null)
			&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_WATER)
			&&(mob.riding().rideBasis()!=Rideable.RIDEABLE_AIR))
			{
				mob.tell(L("You need to get off @x1 first!",mob.riding().name()));
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> start(s) swimming @x1.",CMLib.directions().getDirectionName(dirCode)));
			final Room R=mob.location();
			if((R!=null)&&(R.okMessage(mob,msg)))
				R.send(mob,msg);
			else
				return false;
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final int dirCode=CMLib.directions().getDirectionCode(CMParms.combine(commands,0));
		if(!preInvoke(mob,commands,givenTarget,auto,asLevel,0,0.0))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		final Room R=mob.location();
		if((R!=null)
		&&(R.okMessage(mob,msg)))
		{
			R.send(mob,msg);
			success=proficiencyCheck(mob,0,auto);
			if(!success)
				R.show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> struggle(s) against the water, making no progress."));
			else
			{
				if(mob.fetchEffect(ID())==null)
				{
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					try
					{
						mob.addEffect(A);
						mob.recoverPhyStats();
						CMLib.tracking().walk(mob,dirCode,false,false);
					}
					finally
					{
						mob.delEffect(A);
					}
				}
				else
					CMLib.tracking().walk(mob,dirCode,false,false);
			}
			mob.recoverPhyStats();
			if(mob.location()!=R)
				mob.location().show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		}
		return success;
	}
}
