package com.planet_ink.coffee_mud.Abilities.Thief;


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
public class Thief_Palm extends ThiefSkill
{
	public String ID() { return "Thief_Palm"; }
	public String name(){ return "Palm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PALM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int combatCastingTime(){return 0;}
	public int castingTime(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean success=profficiencyCheck(mob,0,auto);
		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to palm something and fail(s).");
		else
		{
			if((commands.size()>0)&&(!((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
			   commands.addElement("UNOBTRUSIVELY");
			try
			{
				Command C=CMClass.getCommand("Get");
				commands.insertElementAt("GET",0);
				if(C!=null) C.execute(mob,commands);
			}
			catch(Exception e)
			{}
		}
		return success;
	}
}
