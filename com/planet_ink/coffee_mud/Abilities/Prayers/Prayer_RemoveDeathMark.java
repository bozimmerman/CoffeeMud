package com.planet_ink.coffee_mud.Abilities.Prayers;

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
public class Prayer_RemoveDeathMark extends Prayer
{
	public String ID() { return "Prayer_RemoveDeathMark"; }
	public String name(){ return "Remove Death Mark";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Hashtable remove=new Hashtable();
		Ability E=target.fetchEffect("Thief_Mark");
		if(E!=null) remove.put(E,target);
		E=target.fetchEffect("Thief_ContractHit");
		if(E!=null) remove.put(E,target);
		for(Enumeration e=CMMap.players();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M!=null)&&(M!=target))
			{
				E=M.fetchEffect("Thief_Mark");
				if((E!=null)&&(E.text().startsWith(target.Name()+"/")))
					remove.put(E,M);
			}
		}

		if((success)&&(remove.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"^SA glow surrounds <T-NAME>.^?":"^S<S-NAME> call(s) on "+hisHerDiety(mob)+" for <T-NAME> to be released from <T-HIS-HER> death mark.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration e=remove.keys();e.hasMoreElements();)
				{
					Ability A=(Ability)e.nextElement();
					MOB M=(MOB)remove.get(A);
					A.unInvoke();
					M.delEffect(A);
				}

			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> call(s) on "+hisHerDiety(mob)+" to release <T-NAME> from <T-HIS-HER> death mark, but nothing happens.");


		// return whether it worked
		return success;
	}
}
