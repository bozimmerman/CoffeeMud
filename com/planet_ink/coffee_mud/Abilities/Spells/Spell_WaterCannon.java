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
public class Spell_WaterCannon extends Spell
{
	public String ID() { return "Spell_WaterCannon"; }
	public String name(){return "Water Cannon";}
	public int minRange(){return 2;}
	public int maxRange(){return 3;}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

   public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"<S-NAME> incant(s) at <T-NAMESELF> and geyser of water blasts towards <T-HIM-HER>.");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_WATER|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				int maxDie=(int)Math.round(Util.div(adjustedLevel(mob,asLevel),2.0));
				damage += Dice.roll(maxDie,8,15);
				mob.location().send(mob,msg2);
				if((msg2.value()>0)||(msg.value()>0))
					damage = (int)Math.round(Util.div(damage,2.0));

				if(target.location()==mob.location())
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_WATER,Weapon.TYPE_BASHING,"The water blast <DAMAGE> <T-NAME>!");

				int percentage = Dice.roll(1, 100, 0);
				if(percentage < 10)
				{
      				FullMsg msg3=new FullMsg(mob,target,this,affectType(auto),"<T-NAME> is knocked down by the water cannon.");
					if(mob.location().okMessage(mob,msg3))
					{
					   mob.location().send(mob, msg3);
					}
					affectEnvStats(target, target.envStats());
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell.");

		// return whether it worked
		return success;
	}
}
