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
public class Spell_AnimateItem extends Spell
{
	public String ID() { return "Spell_AnimateItem"; }
	public String name(){return "Animate Item";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<2)
		{
			mob.tell("You must specify what to cast this on, and then what you want it to emote.");
			return false;
		}
		Vector V=new Vector();
		V.addElement(commands.elementAt(0));
		Item target=getTarget(mob,mob.location(),givenTarget,V,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> "+Util.combine(commands,1)+".");
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> twitch(es) oddly, but does nothing more.");


		// return whether it worked
		return success;
	}
}
