package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Unbinding extends CommonSkill
{
	public String ID() { return "Unbinding"; }
	public String name(){ return "Unbinding";}
	private static final String[] triggerStrings = {"UNBIND","UNTIE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

	    MOB target=getTarget(mob,commands,givenTarget);
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(profficiencyCheck(mob,0,auto))
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> unbind(s) <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to unbind <T-NAMESELF>, but fail(s).");
		return true;
	}
}