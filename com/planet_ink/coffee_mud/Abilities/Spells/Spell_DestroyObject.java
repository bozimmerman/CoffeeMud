package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_DestroyObject extends Spell
{
	public String ID() { return "Spell_DestroyObject"; }
	public String name(){return "Destroy Object";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((target instanceof DeadBody)
		&&(((DeadBody)target).playerCorpse())
		&&(!((DeadBody)target).mobName().equals(mob.Name())))
		{
			mob.tell("You are not allowed to destroy a player corpse.");
			return false;
		}

		if(!super.invoke(mob,commands, givenTarget, auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,((mob.envStats().level()-target.envStats().level())*25),auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),
									(auto?"<T-NAME> begins to glow!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> vanish(es) into thin air!");
				target.destroy();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
