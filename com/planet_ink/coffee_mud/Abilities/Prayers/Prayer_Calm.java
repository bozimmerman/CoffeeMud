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

public class Prayer_Calm extends Prayer
{
	public String ID() { return "Prayer_Calm"; }
	public String name(){ return "Calm";}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		boolean someoneIsFighting=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB inhab=mob.location().fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				someoneIsFighting=true;
		}

		if((success)&&(someoneIsFighting))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"A feeling of calmness descends.":"^S<S-NAME> "+prayWord(mob)+" for calmness.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab.isInCombat()))
					{
						inhab.tell("You feel at peace.");
						inhab.makePeace();
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for calmness, but nothing happens.");


		// return whether it worked
		return success;
	}
}
