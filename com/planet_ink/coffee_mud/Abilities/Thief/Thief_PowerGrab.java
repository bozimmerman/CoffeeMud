package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Thief_PowerGrab extends ThiefSkill
{
	public String ID() { return "Thief_PowerGrab"; }
	public String name(){ return "Power Grab";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"POWERGRAB"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public double castingTime(final MOB mob, final List<String> cmds){return CMProps.getActionSkillCost(ID(),0.0);}
	public double combatCastingTime(final MOB mob, final List<String> cmds){return CMProps.getCombatActionSkillCost(ID(),0.0);}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item possibleContainer=possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);
		Item target=super.getTarget(mob, mob.location(), givenTarget, possibleContainer, commands, Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to power grab something and fail(s).");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|CMMsg.MASK_MAGIC,auto?"":"^S<S-NAME> carefully attempt(s) to acquire <T-NAME>^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int level=target.basePhyStats().level();
				int level2=target.phyStats().level();
				target.basePhyStats().setLevel(1);
				target.phyStats().setLevel(1);
				CMLib.commands().postGet(mob, possibleContainer, target, false);
				target.basePhyStats().setLevel(level);
				target.phyStats().setLevel(level2);
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		return success;
	}
}
