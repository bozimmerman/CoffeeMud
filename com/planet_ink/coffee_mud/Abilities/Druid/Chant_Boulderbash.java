package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Chant_Boulderbash extends Chant
{
	public String ID() { return "Chant_Boulderbash"; }
	public String name(){ return "Boulderbash";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 2;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_ROCKS))
		{
			mob.tell("This magic only works in caves, mountainous, or rocky regions, where the rocks will answer to your chant.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A boulder flies through the air!":"^S<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly a huge rock flies at <T-HIM-HER>!^?"));
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				int maxDie =  (int)Math.round(new Integer(adjustedLevel(mob,asLevel)).doubleValue());
				int damage = Dice.roll(maxDie,6,maxDie);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(Util.div(damage,1.5));
				if(target.location()==mob.location())
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_BASHING,"The boulder <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
