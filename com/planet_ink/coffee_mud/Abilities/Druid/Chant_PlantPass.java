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

public class Chant_PlantPass extends Chant
{
	public String ID() { return "Chant_PlantPass"; }
	public String name(){ return "Plant Pass";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify the name of the location of one of your plants.  Use your 'My Plants' skill if necessary.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();

		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(myPlant==null)
		{
			mob.tell("There doesn't appear to be any of your plants here to travel through.");
			return false;
		}

		Vector candidates=Druid_MyPlants.myPlantRooms(mob);
		Room newRoom=null;
		for(int m=0;m<candidates.size();m++)
		{
			Room room=(Room)candidates.elementAt(m);
			if(EnglishParser.containsString(room.displayText(),areaName))
			{
			   newRoom=room;
			   break;
			}
		}
		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"', perhaps you have nothing growing there?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF> and <S-IS-ARE> drawn into it!^?");
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				HashSet h=properTargets(mob,givenTarget,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> emerge(s) from the ground.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> <S-IS-ARE> sucked into "+myPlant.name()+".");
					if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CommonMsgs.flee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CommonMsgs.look(follower,true);
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
