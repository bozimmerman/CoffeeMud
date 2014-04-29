package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Prayer_Earthshield extends Prayer
{
	@Override public String ID() { return "Prayer_Earthshield"; }
	@Override public String name(){ return "Earthshield";}
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;}
	@Override public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	@Override public String displayText(){ return "(In Earthshield)";}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		affectableStats.setArmor(affectableStats.armor()-5 - (adjustedLevel(invoker(),0)/4));
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectableState.setMovement(affectableState.getMovement()/2);
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> earth shield vanishes.");
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=beneficialAffect(mob,target,asLevel,0);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> covered by an Earth Shield!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but flub(s) it.");


		// return whether it worked
		return success;
	}
}
