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
public class Paladin_ChaosRage extends StdAbility
{
	@Override
	public String ID()
	{
		return "Paladin_ChaosRage";
	}

	private final static String localizedName = CMLib.lang().L("Chaos Rage");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Chaos Rage)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"CHAOSRAGE","RAGE"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY|Ability.FLAG_CHAOS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	public int hpAdjustment=0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
			invoker=(MOB)affected;
		if(invoker!=null)
		{
			final int xlvl=getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(CMath.div(affectableStats.damage(),6.0-CMath.mul(0.2,xlvl))));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(CMath.div(affectableStats.attackAdjustment(),6.0-CMath.mul(0.2,xlvl))));
			affectableStats.setArmor(affectableStats.armor()+20+(2*xlvl));
		}
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	@Override
	public void unInvoke()
	{
		if(affecting() instanceof MOB)
		{
			final MOB mob=(MOB)affecting();

			super.unInvoke();

			if(canBeUninvoked())
			{
				if(mob.curState().getHitPoints()<=hpAdjustment)
					mob.curState().setHitPoints(1);
				else
					mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
				mob.tell(L("You feel calmer."));
				mob.recoverMaxState();
			}
		}
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
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if((target.fetchEffect(this.ID())!=null)||(target.fetchEffect("Fighter_Berzerk")!=null))
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already in a chaotic rage."), commands);
			return false;
		}

		if((!auto)&&(!mob.isInCombat()))
		{
			mob.tell(L("You aren't in combat!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,L("<T-NAME> get(s) a chaotic rage in <T-HIS-HER> eyes!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				hpAdjustment=(int)Math.round(CMath.div(target.maxState().getHitPoints(),5.0));
				beneficialAffect(mob,target,asLevel,0);
				target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
				target.recoverMaxState();
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> huff(s) and grunt(s), but can't build enough rage."));
		return success;
	}
}
