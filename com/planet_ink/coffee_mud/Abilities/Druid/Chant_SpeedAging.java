package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_SpeedAging extends Chant
{
	public String ID() { return "Chant_SpeedAging"; }
	public String name(){ return "Speed Aging";}
	protected int canAffectCode(){return 0;}
	public int quality(){return Ability.OK_OTHERS;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY,true);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect("Age");
				if((!(target instanceof MOB))&&(A==null))
				{
					if(target instanceof Food)
					{
						mob.tell(target.name()+" rots away!");
						((Item)target).destroy();
					}
					else
					if(target instanceof Item)
					{
						switch(((Item)target).material()&EnvResource.MATERIAL_MASK)
						{
							case EnvResource.MATERIAL_CLOTH:
							case EnvResource.MATERIAL_FLESH:
							case EnvResource.MATERIAL_LEATHER:
							case EnvResource.MATERIAL_PAPER:
							case EnvResource.MATERIAL_VEGETATION:
							case EnvResource.MATERIAL_WOODEN:
							{
								mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" rots away!");
								((Item)target).destroy();
								break;
							}
						default:
							mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages, but nothing happens to it.");
							break;
						}
					}
					else
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages, but nothing happens to it.");
					success=false;
				}
				else
				if((A==null)||(A.displayText().length()==0))
				{
					MOB M=(MOB)target;
					mob.location().show(M,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> age(s) a bit.");
					M.setAgeHours(M.getAgeHours()+(M.getAgeHours()/10));
				}
				else
				{
					long start=Util.s_long(A.text());
					long age=System.currentTimeMillis()-start;
					age=age+(age/10);
					if(age<(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME))
						age=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME);
					A.setMiscText(""+(start-age));
					if(target instanceof MOB)
						mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> age(s) a bit.");
					else
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" ages a bit.");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}
