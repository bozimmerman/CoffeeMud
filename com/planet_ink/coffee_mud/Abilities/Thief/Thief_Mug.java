package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Mug extends ThiefSkill
{
	public String ID() { return "Thief_Mug"; }
	public String name(){ return "Mug";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"MUG"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob.getVictim();
		if(!mob.isInCombat())
		{
			mob.tell("You can only mug someone you are fighting!");
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell("Mug what from "+target.name()+"?");
			return false;
		}
		String itemToSteal=Util.combine(commands,0);
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);
		boolean success=profficiencyCheck(mob,levelDiff,auto);
		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to steal; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to mug <T-NAME> and fails!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str=null;
			int code=(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
					str="<S-NAME> mug(s) <T-NAMESELF>, stealing "+stolen.name()+" from <T-HIM-HER>.";
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str="<S-NAME> attempt(s) to mug <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
				}

			FullMsg msg=new FullMsg(mob,target,this,code,str,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_MALICIOUS,str,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				msg=new FullMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
