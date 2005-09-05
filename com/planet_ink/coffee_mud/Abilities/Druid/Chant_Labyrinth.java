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

public class Chant_Labyrinth extends Chant
{
	public String ID() { return "Chant_Labyrinth"; }
	public String name(){ return "Labyrinth";}
	public String displayText(){return "(Labyrinth)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	Room oldRoom=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if((canBeUninvoked())&&(room instanceof GridLocale)&&(oldRoom!=null))
			((GridLocale)room).clearGrid(oldRoom);
		super.unInvoke();
	}

    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if((msg.sourceMinor()==CMMsg.TYP_QUIT)
        &&(affected instanceof Room)
        &&(((Room)affected).isInhabitant(msg.source())))
            unInvoke();
        return super.okMessage(host,msg);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("You must be in a cave to create a labyrinth.");
			return false;
		}
		if(mob.location().roomID().length()==0)
		{
			mob.tell("You cannot invoke the plant maze here.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, null, this, affectType(auto), auto?"":"^S<S-NAME> chant(s) twistedly!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Something is happening...");

				Room newRoom=CMClass.getLocale("CaveMaze");
				((GridLocale)newRoom).setXSize(10);
				((GridLocale)newRoom).setYSize(10);
				newRoom.setDisplayText("The Labyrinth");
				newRoom.addNonUninvokableEffect(CMClass.getAbility("Prop_NoTeleportOut"));
				StringBuffer desc=new StringBuffer("");
				desc.append("You are lost in dark twisting caverns.  The darkness covers you like a blanket. Every turn looks the same.");
				newRoom.setArea(mob.location().getArea());
				oldRoom=mob.location();
				newRoom.setDescription(desc.toString());
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().rawDoors()[d];
					Exit E=mob.location().rawExits()[d];
					if((R!=null)&&(R.roomID().length()>0))
					{
						newRoom.rawDoors()[d]=R;
						newRoom.rawExits()[d]=E;
					}
				}
				newRoom.getArea().fillInAreaRoom(newRoom);
				beneficialAffect(mob,newRoom,asLevel,0);
				Vector everyone=new Vector();
				for(int m=0;m<oldRoom.numInhabitants();m++)
				{
					MOB follower=oldRoom.fetchInhabitant(m);
					everyone.addElement(follower);
				}

				for(int m=0;m<everyone.size();m++)
				{
					MOB follower=(MOB)everyone.elementAt(m);
					if(follower==null) continue;
					Room newerRoom=((GridLocale)newRoom).getRandomChild();
					FullMsg enterMsg=new FullMsg(follower,newerRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of thin air.");
					FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,affectType(auto),"<S-NAME> disappear(s) into the labyrinth.");
					if(oldRoom.okMessage(follower,leaveMsg)&&newerRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
							follower.makePeace();
						oldRoom.send(follower,leaveMsg);
						newerRoom.bringMobHere(follower,false);
						newerRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CommonMsgs.look(follower,true);
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) twistedly, but the magic fades.");

		// return whether it worked
		return success;
	}
}
