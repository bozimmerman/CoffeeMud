package com.planet_ink.coffee_mud.Commands;
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
public class Lock extends StdCommand
{
	public Lock(){}

	private String[] access={"LOCK","LOC"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatTolock=Util.combine(commands,1);
		if(whatTolock.length()==0)
		{
			mob.tell(getScr("Movement","lockerr1"));
			return false;
		}
		Environmental lockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTolock);
		if(dirCode>=0)
			lockThis=mob.location().getExitInDir(dirCode);
		if(lockThis==null)
			lockThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatTolock,Item.WORN_REQ_ANY);

		if((lockThis==null)||(!Sense.canBeSeenBy(lockThis,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",whatTolock));
			return false;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,CMMsg.MSG_LOCK,getScr("Movement","slocks")+CommonStrings.msp("doorlock.wav",10));
		if(lockThis instanceof Exit)
		{
			boolean locked=((Exit)lockThis).isLocked();
			if((mob.location().okMessage(msg.source(),msg))
			&&(!locked))
			{
				mob.location().send(msg.source(),msg);
				if(dirCode<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					if(mob.location().getExitInDir(d)==lockThis)
					{dirCode=d; break;}

				if((dirCode>=0)&&(mob.location().getRoomInDir(dirCode)!=null))
				{
					Room opR=mob.location().getRoomInDir(dirCode);
					Exit opE=mob.location().getPairedExit(dirCode);
					if(opE!=null)
					{
						FullMsg altMsg=new FullMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					int opCode=Directions.getOpDirectionCode(dirCode);
					if((opE!=null)
					&&(opE.isLocked())
					&&(((Exit)lockThis).isLocked()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,getScr("Movement","afterlocks",opE.name(),Directions.getInDirectionName(opCode)));
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
