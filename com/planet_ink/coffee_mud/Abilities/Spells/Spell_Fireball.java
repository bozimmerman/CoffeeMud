package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Fireball extends Spell
{
	public String ID() { return "Spell_Fireball"; }
	public String name(){return "Fireball";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A huge fireball appears and blazes towards <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, shooting forth a blazing fireball!^?")+CommonStrings.msp("fireball.wav",40));
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				invoker=mob;
                int numDice = (int)Math.round(Util.div(adjustedLevel(mob,asLevel),2.0));
				int damage = Dice.roll(numDice, 10, 10);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(Util.div(damage,2.0));
				MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flaming blast <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and shout(s) at <T-NAMESELF>, but nothing more happens.");

		return success;
	}
}
