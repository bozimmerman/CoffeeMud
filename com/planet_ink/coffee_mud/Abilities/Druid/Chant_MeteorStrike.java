package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_MeteorStrike extends Chant
{
	public String ID() { return "Chant_MeteorStrike"; }
	public String name(){return "Meteor Strike";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth striking at.");
			return false;
		}
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to strike with meteors.");
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

			if(mob.location().show(mob,null,this,affectType(auto),auto?"A devestating meteor shower erupts!":"^S<S-NAME> chant(s) for a devestating meteor shower!^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					invoker=mob;

					int damage = 0;
					int maxDie=adjustedLevel(mob,asLevel);
					damage = Dice.roll(maxDie,6,30);
					if(msg.value()>0)
						damage = (int)Math.round(Util.div(damage,2.0));
					if(target.location()==mob.location())
						MUDFight.postDamage(mob,target,this,damage,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_BASHING,"The meteors <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s) to the sky, but nothing happens.");


		// return whether it worked
		return success;
	}
}
