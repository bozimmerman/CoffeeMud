package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Thief_Mug extends ThiefSkill
{
	public String ID() { return "Thief_Mug"; }
	public String name(){ return "Mug";}
	protected int canAffectCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"MUG"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob.getVictim();
		if(!mob.isInCombat())
		{
			mob.tell("You can only mug someone you are fighting!");
			return false;
		}
        String itemToSteal="all";
        if(!auto)
        {
    		if(commands.size()<1)
    		{
    			mob.tell("Mug what from "+target.name()+"?");
    			return false;
    		}
    		itemToSteal=CMParms.combine(commands,0);
        }
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);
		boolean success=proficiencyCheck(mob,levelDiff,auto);
		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to mug <T-NAME>!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to mug you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to mug <T-NAME> and fails!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str=null;
			int code=(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Wearable.IN_INVENTORY)))
					str="<S-NAME> mug(s) <T-NAMESELF>, stealing "+stolen.name()+" from <T-HIM-HER>.";
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str="<S-NAME> attempt(s) to mug <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
				}

			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_MALICIOUS,str,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				msg=CMClass.getMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=CMClass.getMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
