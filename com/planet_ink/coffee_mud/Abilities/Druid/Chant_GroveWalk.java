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

public class Chant_GroveWalk extends Chant
{
	public String ID() { return "Chant_GroveWalk"; }
	public String name(){ return "Grove Walk";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify the name of the location of another grove where there is a druidic monument.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();


		Room newRoom=null;
		boolean hereok=false;
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(Sense.canAccess(mob,R))
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)&&(I.ID().equals("DruidicMonument")))
					{
						if(R==mob.location())
							hereok=true;
						if(EnglishParser.containsString(R.displayText(),areaName))
						   newRoom=R;
						break;
					}
				}
			if((newRoom!=null)&&(hereok)) break;
		}
		if(!hereok)
		{
			mob.tell("There is no druidic monument here.  You can only use this chant in a druidic grove.");
			return false;
		}
		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"', perhaps it is not a grove?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,newRoom,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and walk(s) around.^?");
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				HashSet h=properTargets(mob,givenTarget,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> emerge(s) from around the stones.");
					FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) around the stones.");
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
			beneficialVisualFizzle(mob,newRoom,"<S-NAME> chant(s) and walk(s) around, but nothing happens.");


		// return whether it worked
		return success;
	}
}
