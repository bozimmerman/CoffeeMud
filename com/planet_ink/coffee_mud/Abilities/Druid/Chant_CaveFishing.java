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

public class Chant_CaveFishing extends Chant
{
	public String ID() { return "Chant_CaveFishing"; }
	public String name(){ return "Cave Fishing";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ROOMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;

		Environmental waterSrc=null;
		if((target.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		||(target.domainType()==Room.DOMAIN_INDOORS_UNDERWATER))
			waterSrc=target;
		else
		if(target.domainType()==Room.DOMAIN_INDOORS_CAVE)
		{
			for(int i=0;i<target.numItems();i++)
			{
				Item I=target.fetchItem(i);
				if((I instanceof Drink)
				&&(I.container()==null)
				&&(((Drink)I).liquidType()==EnvResource.RESOURCE_FRESHWATER)
				&&(!Sense.isGettable(I)))
					waterSrc=I;
			}
			if(waterSrc==null)
			{
				mob.tell("There is no water source here to fish in.");
				return false;
			}
		}
		else
		{
			mob.tell("This chant cannot be used outdoors.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Fish start swimming around in "+target.name()+"!");
					target.setResource(EnvResource.FISHES[Dice.roll(1,EnvResource.FISHES.length,-1)]);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAME>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
