package com.planet_ink.coffee_mud.Commands;
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
public class Open extends BaseItemParser
{
	public Open(){}

	private String[] access={"OPEN","OP","O"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatToOpen=Util.combine(commands,1);
		if(whatToOpen.length()==0)
		{
			mob.tell(getScr("Movement","openerr1"));
			return false;
		}
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",whatToOpen));
			return false;
		}
		String openWord=((openThis==null)||(!(openThis instanceof Exit)))?getScr("Movement","sopenword"):((Exit)openThis).openWord();
		FullMsg msg=new FullMsg(mob,openThis,null,CMMsg.MSG_OPEN,(getScr("Movement","sopens",openWord))+CommonStrings.msp("dooropen.wav",10));
		if(openThis instanceof Exit)
		{
			boolean open=((Exit)openThis).isOpen();
			if((mob.location().okMessage(msg.source(),msg))
			&&(!open))
			{
				mob.location().send(msg.source(),msg);

				if(dirCode<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					if(mob.location().getExitInDir(d)==openThis)
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
					if((opE!=null)&&(opE.isOpen())&&(((Exit)openThis).isOpen()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,getScr("Movement","afteropens",opE.name(),Directions.getInDirectionName(opCode)));
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
