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
public class Spell_Reinforce extends Spell
{
	public String ID() { return "Spell_Reinforce"; }
	public String name(){return "Reinforce";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if(!target.subjectToWearAndTear())
		{	mob.tell(target.name()+" cannot be reinforced."); return false;}
		else
		if(target.usesRemaining()<100)
		{	mob.tell(target.name()+" must be repaired before it can be reinforced."); return false;}

		if(!super.invoke(mob,commands, givenTarget, auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,((mob.envStats().level()-target.envStats().level())*5),auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),
									(auto?"<T-NAME> begins to shimmer!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.usesRemaining()>=150)
					mob.tell(target.name()+" cannot be reinforced further.");
				else
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> begin(s) to glow and harden!");
					target.setUsesRemaining(target.usesRemaining()+50);
					target.recoverEnvStats();
					mob.location().recoverRoomStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
