package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_Frenzy extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Frenzy";
	}

	private final static String localizedName = CMLib.lang().L("Frenzy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Frenzy spell)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public int hpAdjustment=0;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
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
		affectableStats.setArmor(affected.basePhyStats().armor()+30+(3*xlvl));
	}

	@Override
	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	@Override
	public void setMiscText(String newText)
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> scream(s) at <T-NAMESELF>!^?"));
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
			return beneficialWordsFizzle(mob,target,L("<S-NAME> scream(s) wildly at <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
