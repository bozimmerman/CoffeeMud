package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_IceHealing extends Prayer
{
	public String ID() { return "Prayer_IceHealing"; }
	public String name(){ return "Ice Healing";}
	public String displayText(){ return "(Ice Healing)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY|Ability.FLAG_HEALING;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The aura of ice healing around you fades.");
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		   &&((msg.sourceMinor()==CMMsg.TYP_COLD)
			   ||(msg.sourceMinor()==CMMsg.TYP_WATER))
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			int recovery=(int)Math.round(Util.div((msg.value()),2.0));
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The icey attack heals <S-NAME> "+recovery+" points.");
			MUDFight.postHealing(mob,mob,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,recovery,null);
			return false;
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("You already healed by ice.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for icey healing.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"An aura surrounds <S-NAME>.");
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for icey healing, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
