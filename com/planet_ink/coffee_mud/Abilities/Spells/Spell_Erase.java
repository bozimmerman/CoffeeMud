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
public class Spell_Erase extends Spell
{
	public String ID() { return "Spell_Erase"; }
	public String name(){return "Erase Scroll";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode() {	return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Erase what?.");
			return false;
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((target==null)||((target!=null)&&(!(target instanceof Scroll))&&(!Sense.isReadable(target))))
		{
			mob.tell("You can't erase that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"The words on <T-NAME> fade.":"^S<S-NAME> whisper(s), and then rub(s) on <T-NAMESELF>, making the words fade.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Scroll)
					((Scroll)target).setScrollText("");
				else
					target.setReadableText("");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> whisper(s), and then rub(s) on <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
