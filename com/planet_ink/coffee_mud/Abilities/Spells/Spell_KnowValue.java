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
public class Spell_KnowValue extends Spell
{
	public String ID() { return "Spell_KnowValue"; }
	public String name(){return "Know Value";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> weigh(s) the value of <T-NAMESELF> carefully.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String str=null;
				if(target.value()<=0)
					str=target.name()+" isn't worth anything.";
				else
				if(target.value()==0)
					str=target.name()+" is worth one puny gold piece";
				else
					str=target.name()+" is worth "+target.value()+" gold pieces";
				if(mob.isMonster())
					CommonMsgs.say(mob,null,str,false,false);
				else
					mob.tell(str);
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> weigh(s) the value of <T-NAMESELF>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
