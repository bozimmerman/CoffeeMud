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

public class Prayer_FountainLife extends Prayer
{
	public String ID() { return "Prayer_FountainLife"; }
	public String name(){ return "Fountain of Life";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Room SpringLocation=null;
	private Item littleSpring=null;
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,"The fountain of life dries up.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroy();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the fountain of life.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String itemID = "LifeFountain";

				Item newItem=(Item)CMClass.getMiscMagic(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,asLevel,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a fountain of life, but there is no answer.");

		// return whether it worked
		return success;
	}
}
