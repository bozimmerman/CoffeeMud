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
public class Thief_ConcealItem extends ThiefSkill
{
	public String ID() { return "Thief_ConcealItem"; }
	public String name(){ return "Conceal Item";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ITEMCONCEAL","ICONCEAL","CONCEALITEM"};
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public void affectEnvStats(Environmental host, EnvStats stats)
	{
		super.affectEnvStats(host,stats);
		stats.setDisposition(stats.disposition()|EnvStats.IS_HIDDEN);
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_GET))
		{
			Environmental E=affected;
			unInvoke();
			E.delEffect(this);
			E.recoverEnvStats();
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("What item would you like to conceal?");
			return false;
		}
		Item item=super.getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(item==null) return false;
		
		if((!auto)&&(item.envStats().weight()>((adjustedLevel(mob,asLevel)*2))))
		{
			mob.tell("You aren't good enough to conceal anything that large.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,item,null,CMMsg.MSG_THIEF_ACT,"<S-NAME> conceal(s) <T-NAME>.",CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=(Ability)super.copyOf();
				A.setInvoker(mob);
				item.addNonUninvokableEffect(A);
				item.recoverEnvStats();
			}
		}
		else
			beneficialVisualFizzle(mob,item,"<S-NAME> attempt(s) to coneal <T-NAME>, but fail(s).");
		return success;
	}
}