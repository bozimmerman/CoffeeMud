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
public class Spell_GreaterInvisibility extends Spell
{
	public String ID() { return "Spell_GreaterInvisibility"; }
	public String name(){return "Greater Invisibility";}
	public String displayText(){return "(Greater Invisibility)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fade(s) back into view.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=this.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> cast(s) a spell on <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> fade(s) from view!");

				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) softly to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
