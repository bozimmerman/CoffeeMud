package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Spell_MagicalAura extends Spell
{
	public String ID() { return "Spell_MagicalAura"; }
	public String name(){return "Magical Aura";}
	public String displayText(){return "(Magical Aura)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(canBeUninvoked())
			if(affected instanceof MOB)
				((MOB)affected).tell("Your magical aura fades.");

		super.unInvoke();

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell("There is already a magical aura around "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A magical aura appears around <T-NAME>.":"^S<S-NAME> invoke(s) a magical aura around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				target.recoverEnvStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a magical aura, but fail(s).");

		return success;
	}
}
