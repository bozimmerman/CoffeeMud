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
public class Look extends StdCommand
{
	public Look(){}

	private String[] access={"EXAMINE","EXAM","EXA","LOOK","LOO","LO","L"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean quiet=false;
		if((commands!=null)&&(commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		String textMsg="<S-NAME> look(s) ";
		if(mob.location()==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			Environmental thisThang=null;
			
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("at")))
			   commands.removeElementAt(1);
			else
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("to")))
			   commands.removeElementAt(1);
			String ID=Util.combine(commands,1);
			
			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
			{
				mob.location().listExits(mob);
				return false;
			}
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			
			if(thisThang==null)
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Item.WORN_REQ_ANY);
			if((thisThang==null)
			&&(commands.size()>2)
			&&(((String)commands.elementAt(1)).equalsIgnoreCase("in")))
			{
				commands.removeElementAt(1);
				String ID2=Util.combine(commands,1);
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID2,Item.WORN_REQ_ANY);
				if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
				{
					mob.tell("That's not a container.");
					return false;
				}
			}
			int dirCode=-1;
			if(thisThang==null)
			{
				dirCode=Directions.getGoodDirectionCode(ID);
				if(dirCode>=0)
				{
					Room room=mob.location().getRoomInDir(dirCode);
					Exit exit=mob.location().getExitInDir(dirCode);
					if((room!=null)&&(exit!=null))
						thisThang=exit;
					else
					{
						mob.tell("You don't see anything that way.");
						return false;
					}
				}
			}
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,CMMsg.MSG_LOOK,textMsg+name+".");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				if((thisThang instanceof Room)&&(Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS)))
					((Room)thisThang).listExits(mob);
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
				if(((String)commands.elementAt(0)).toUpperCase().startsWith("E"))
				{
					mob.tell("Examine what?");
					return false;
				}

			FullMsg msg=new FullMsg(mob,mob.location(),null,CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"at you."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if((Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
			&&(Sense.canBeSeenBy(mob.location(),mob)))
				mob.location().listExits(mob);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
