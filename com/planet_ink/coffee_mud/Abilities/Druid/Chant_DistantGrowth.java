package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_DistantGrowth extends Chant
{
	public String ID() { return "Chant_DistantGrowth"; }
	public String name(){ return "Distant Growth";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ROOMS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<1)
		{
			mob.tell("Grow plants where?");
			return false;
		}

		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room anyRoom=null;
		Room newRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((EnglishParser.containsString(R.displayText(),areaName))
			&&(Sense.canAccess(mob,R)))
			{
				anyRoom=R;
				if(((R.domainType()&Room.INDOORS)==0)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_CITY)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
				&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
				{
				    newRoom=R;
				    break;
				}
			}
		}

		if(newRoom==null)
		{
			if(anyRoom==null)
				mob.tell("You don't know of an place called '"+Util.combine(commands,0)+"'.");
			else
			if((anyRoom.domainType()==Room.DOMAIN_OUTDOORS_CITY)
			||(anyRoom.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
				mob.tell("There IS such a place, but it is an overtrodden street, so your magic would fail.");
			else
			if((anyRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(anyRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
				mob.tell("There IS such a place, but it is on or in the water, so your magic would fail.");
			else
				mob.tell("There IS such a place, but it is not outdoors, so your magic would fail.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> chant(s) about a far away place.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=Chant_SummonPlants.buildPlant(mob,newRoom);
				mob.tell("You feel a new connection with "+newItem.name());
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) about a far away place, but the magic fades.");


		// return whether it worked
		return success;
	}
}
