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
public class Push extends Go
{
	public Push(){}

	private String[] access={"PUSH"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Environmental openThis=null;
		String dir="";
		int dirCode=-1;
		Environmental E=null;
		if(commands.size()>1)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
			{
			    if((mob.location().getRoomInDir(dirCode)==null)
			    ||(mob.location().getExitInDir(dirCode)==null)
			    ||(!mob.location().getExitInDir(dirCode).isOpen()))
			    {
			        mob.tell("You can't push anything that way.");
			        return false;
			    }
			    E=mob.location().getRoomInDir(dirCode);
			    dir=" "+Directions.getDirectionName(dirCode);
			    commands.removeElementAt(commands.size()-1);
			}
		}
		if(dir.length()==0)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
				openThis=mob.location().getExitInDir(dirCode);
		}
		String whatToOpen=Util.combine(commands,1);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return false;
		}
		int malmask=(openThis instanceof MOB)?CMMsg.MASK_MALICIOUS:0;
		FullMsg msg=new FullMsg(mob,openThis,E,CMMsg.MSG_PUSH|malmask,"<S-NAME> push(es) <T-NAME>"+dir+".");
		if(mob.location().okMessage(mob,msg))
		{
		    mob.location().send(mob,msg);
		    if((dir.length()>0)&&(msg.tool() instanceof Room))
		    {
		        Room R=(Room)msg.tool();
		        dirCode=MUDTracker.findRoomDir(mob,R);
		        if(dirCode>=0)
		        {
			        if(openThis instanceof Item)
			            R.bringItemHere((Item)openThis,Item.REFUSE_PLAYER_DROP);
			        else
			        if(openThis instanceof MOB)
			            move((MOB)openThis,dirCode,((MOB)openThis).isInCombat(),false,true,true);
		        }
		    }
		            
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
