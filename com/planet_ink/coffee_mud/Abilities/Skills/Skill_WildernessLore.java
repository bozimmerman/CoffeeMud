package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_WildernessLore extends StdAbility
{
	public String ID() { return "Skill_WildernessLore"; }
	public String name(){ return "Wilderness Lore";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"WILDERNESSLORE","WLORE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}
	public int usageType(){return USAGE_MANA;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_HANDS,"<S-NAME> take(s) a quick look at the terrain.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			switch(mob.location().domainType())
			{
			case Room.DOMAIN_INDOORS_METAL:
				mob.tell("You are in a metal structure.");
				break;
			case Room.DOMAIN_OUTDOORS_SPACEPORT:
				mob.tell("You are at a space port.");
				break;
			case Room.DOMAIN_OUTDOORS_CITY:
				mob.tell("You are on a city street.");
				break;
			case Room.DOMAIN_OUTDOORS_WOODS:
				mob.tell("You are in a forest.");
				break;
			case Room.DOMAIN_OUTDOORS_ROCKS:
				mob.tell("You are on a rocky plain.");
				break;
			case Room.DOMAIN_OUTDOORS_PLAINS:
				mob.tell("You are on the plains.");
				break;
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
				mob.tell("You are under the water.");
				break;
			case Room.DOMAIN_OUTDOORS_AIR:
				mob.tell("You are up in the air.");
				break;
			case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				mob.tell("You are on the surface of the water.");
				break;
			case Room.DOMAIN_OUTDOORS_JUNGLE:
				mob.tell("You are in a jungle.");
				break;
			case Room.DOMAIN_OUTDOORS_SWAMP:
				mob.tell("You are in a swamp.");
				break;
			case Room.DOMAIN_OUTDOORS_DESERT:
				mob.tell("You are in a desert.");
				break;
			case Room.DOMAIN_OUTDOORS_HILLS:
				mob.tell("You are in the hills.");
				break;
			case Room.DOMAIN_OUTDOORS_MOUNTAINS:
				mob.tell("You are on a mountain.");
				break;
			case Room.DOMAIN_INDOORS_STONE:
				mob.tell("You are in a stone structure.");
				break;
			case Room.DOMAIN_INDOORS_WOOD:
				mob.tell("You are in a wooden structure.");
				break;
			case Room.DOMAIN_INDOORS_CAVE:
				mob.tell("You are in a cave.");
				break;
			case Room.DOMAIN_INDOORS_MAGIC:
				mob.tell("You are in a magical place.");
				break;
			case Room.DOMAIN_INDOORS_UNDERWATER:
				mob.tell("You are under the water.");
				break;
			case Room.DOMAIN_INDOORS_AIR:
				mob.tell("You are up in a large indoor space.");
				break;
			case Room.DOMAIN_INDOORS_WATERSURFACE:
				mob.tell("You are inside, on the surface of the water.");
				break;
			}
			switch(mob.location().domainConditions())
			{
			case Room.CONDITION_COLD:
				mob.tell("It is cold here.");
				break;
			case Room.CONDITION_HOT:
				mob.tell("It is warm here.");
				break;
			case Room.CONDITION_WET:
				mob.tell("It is very wet here.");
				break;
			}
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_HANDS,"<S-NAME> take(s) a quick look around, but get(s) confused.");
		return success;
	}

}
