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
public class Thief_Peek extends ThiefSkill
{
	public String ID() { return "Thief_Peek"; }
	public String name(){ return "Peek";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PEEK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("Peek at whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target==mob)
		{
			mob.tell("You cannot peek at yourself. Try Inventory.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());

		boolean success=profficiencyCheck(mob,-(levelDiff*(!Sense.canBeSeenBy(mob,target)?0:10)),auto);
		int discoverChance=(int)Math.round(Util.div(target.charStats().getStat(CharStats.WISDOM),30.0))+(levelDiff*5);
		if(!Sense.canBeSeenBy(mob,target))
			discoverChance-=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;


		if(!success)
		{
			if(Dice.rollPercentage()<discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"Your peek attempt fails; <T-NAME> spots you!",CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> tries to peek at your inventory and fails!",CMMsg.NO_EFFECT,null);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
			{
				mob.tell(auto?"":"Your peek attempt fails.");
				return false;
			}
		}
		else
		{
			String str=null;
			if(Dice.rollPercentage()<discoverChance)
				str=auto?"":"<S-NAME> peek(s) at your inventory.";

			FullMsg msg=new FullMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_THIEF_ACT|CMMsg.MASK_EYES),auto?"":"<S-NAME> peek(s) at <T-NAME>s inventory.",CMMsg.MSG_LOOK,str,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				msg=new FullMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> peek(s) at <T-NAME>s inventory.",CMMsg.MSG_OK_VISUAL,str,(str==null)?CMMsg.NO_EFFECT:CMMsg.MSG_OK_VISUAL,str);
				mob.location().send(mob,msg);
				StringBuffer msg2=CommonMsgs.getInventory(mob,target);
				if(msg2.length()==0)
					mob.tell(target.charStats().HeShe()+" is carrying:\n\rNothing!\n\r");
				else
					mob.session().wraplessPrintln(target.charStats().HeShe()+" is carrying:\n\r"+msg2.toString());
			}
		}
		return success;
	}

}
