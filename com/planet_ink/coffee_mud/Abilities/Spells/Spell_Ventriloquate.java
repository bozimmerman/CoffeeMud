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
public class Spell_Ventriloquate extends Spell
{
	public String ID() { return "Spell_Ventriloquate"; }
	public String name(){return "Ventriloquate";}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<2)
		{
			mob.tell("You must specify who or what to cast this on, and what you want said.");
			return false;
		}
		Environmental target=mob.location().fetchFromRoomFavorItems(null,(String)commands.elementAt(0),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_SPEAK,"^T<T-NAME> say(s) '"+Util.combine(commands,1)+"'^?.");
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempts to ventriloquate through <T-NAMESELF>, but noone is fooled.");


		// return whether it worked
		return success;
	}
}
