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
public class Spell_MageClaws extends Spell
{
	public String ID() { return "Spell_MageClaws"; }
	public String name(){return "Mage Claws";}
	public String displayText(){return "(Mage Claws spell)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool()==null))
		{
			Weapon w=(Weapon)CMClass.getItem("GenWeapon");
			w.setName("a pair of jagged claws");
			w.setWeaponType(Weapon.TYPE_SLASHING);
			w.setWeaponClassification(Weapon.CLASS_NATURAL);
			w.baseEnvStats().setDamage(15);
			w.baseEnvStats().setAttackAdjustment(20);
			w.recoverEnvStats();
			msg.modify(msg.source(),msg.target(),w,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> claws return to normal.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> mage claws.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> hands turn into brutal claws!");
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
