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
public class Spell_Dismissal extends Spell
{
	public String ID() { return "Spell_Dismissal"; }
	public String name(){return "Dismissal";}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_MOVING|Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		if(target.getStartRoom()==null)
		{
			int levelDiff=target.envStats().level()-mob.envStats().level();
			if(levelDiff<0) levelDiff=0;
			success=profficiencyCheck(mob,-(levelDiff*5),auto);
		}
		else
			success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive spell!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target.getStartRoom()==null)
						target.destroy();
					else
					{
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> vanish(es) in dismissal!");
						target.getStartRoom().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> appear(s)!");
						target.getStartRoom().bringMobHere(target,false);
						CommonMsgs.look(target,true);
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive but fizzled spell!");


		// return whether it worked
		return success;
	}
}
