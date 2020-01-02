package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class CombatFrenzy extends StdAbility
{

	@Override
	public String ID()
	{
		return "CombatFrenzy";
	}

	private final static String localizedName = CMLib.lang().L("Combat Frenzy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Combat Frenzy)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "COMBATFRENZY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_RACIALABILITY;
	}

	public int hpAdjustment=0;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
			invoker=(MOB)affected;
		final int xlvl=super.getXLEVELLevel(invoker());
		float f=(float)0.1*xlvl;
		if(f>5.0)
			f=5.0f;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(CMath.div(affectableStats.damage(),6.0-f)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(CMath.div(affectableStats.attackAdjustment(),6.0-f)));
		affectableStats.setArmor(affectableStats.armor()+30+(3*xlvl));
	}

	@Override
	public void affectCharState(final MOB affectedMOB, final CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		if(CMath.isInteger(newText))
			hpAdjustment=CMath.s_int(newText);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
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

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		if(target==null)
			return false;
		Room R=CMLib.map().roomLocation(target);
		if(R==null)
			R=mob.location();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("^S<S-NAME> work(s) <S-HIM-HERSELF> into a frenzy!^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(target.location()==R)
				{
					R.show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> go(es) wild!"));
					hpAdjustment=(int)Math.round(CMath.div(target.maxState().getHitPoints(),5.0));
					beneficialAffect(mob,target,asLevel,0);
					final Ability A=target.fetchEffect(ID());
					if(A!=null)
						A.setMiscText(Integer.toString(hpAdjustment));
					target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
					target.recoverMaxState();
					target.recoverPhyStats();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to work <S-HIM-HERSELF> into a frenzy, but can't"));

		// return whether it worked
		return success;
	}
}
