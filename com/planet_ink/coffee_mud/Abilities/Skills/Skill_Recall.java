package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_Recall extends StdAbility
{
	public String ID() { return "Skill_Recall"; }
	public String name(){ return "Recall";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"RECALL","/"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=(!mob.isInCombat())||profficiencyCheck(mob,0,auto);
		if(success)
		{
			Room recalledRoom=mob.location();
			Room recallRoom=mob.getStartRoom();
			FullMsg msg=new FullMsg(mob,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,auto?getScr("Skills","recallgo1"):getScr("Skills","recallgo2"));
			FullMsg msg2=new FullMsg(mob,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
			if((recalledRoom.okMessage(mob,msg))&&(recallRoom.okMessage(mob,msg2)))
			{
				if(mob.isInCombat())
					CommonMsgs.flee(mob,"NOWHERE");
				recalledRoom.send(mob,msg);
				recallRoom.send(mob,msg2);
				if(recalledRoom.isInhabitant(mob))
					recallRoom.bringMobHere(mob,false);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					
					msg=new FullMsg(follower,recalledRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,auto?getScr("Skills","recallgo1"):getScr("Skills","recallgo3",mob.name()));
					if((follower!=null)
					&&(follower.isMonster())
					&&(follower.location()==recalledRoom)
					&&(recalledRoom.isInhabitant(follower))
					&&(recalledRoom.okMessage(follower,msg)))
					{
						msg2=new FullMsg(follower,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,null);
						if(recallRoom.okMessage(follower,msg2))
						{
							if(follower.isInCombat())
								CommonMsgs.flee(follower,("NOWHERE"));
							recallRoom.send(follower,msg2);
							if(recalledRoom.isInhabitant(follower))
								recallRoom.bringMobHere(follower,false);
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,getScr("Skills","recallgo4"));

		// return whether it worked
		return success;
	}

}
