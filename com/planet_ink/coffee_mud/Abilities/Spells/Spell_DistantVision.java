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
public class Spell_DistantVision extends Spell
{
	public String ID() { return "Spell_DistantVision"; }
	public String name(){return "Distant Vision";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Divine a vision of where?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room thisRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room room=(Room)r.nextElement();
			if((Sense.canAccess(mob,room))
			&&(EnglishParser.containsString(room.displayText(),areaName)))
			{
				thisRoom=room;
				break;
			}
		}

		if(thisRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> close(s) <S-HIS-HER> eyes, and invoke(s) a vision.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell("\n\r\n\r");
				FullMsg msg2=new FullMsg(mob,thisRoom,CMMsg.MSG_EXAMINESOMETHING,null);
				thisRoom.executeMsg(mob,msg2);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> close(s) <S-HIS-HER> eyes, incanting, but then open(s) them in frustration.");


		// return whether it worked
		return success;
	}
}
