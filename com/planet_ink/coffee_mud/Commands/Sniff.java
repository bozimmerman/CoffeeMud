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
public class Sniff extends StdCommand
{
	public Sniff(){}

	private String[] access={"SNIFF","SMELL"};
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
		String textMsg="<S-NAME> sniff(s) ";
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
			if(thisThang!=null)
			{
				String name=" <T-NAMESELF>";
 				if(thisThang instanceof Room)
				{
					if(thisThang==mob.location())
						name="around";
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,CMMsg.MSG_SNIFF,textMsg+name+".");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
				if(((String)commands.elementAt(0)).toUpperCase().startsWith("E"))
				{
					mob.tell("Sniff what?");
					return false;
				}

			FullMsg msg=new FullMsg(mob,mob.location(),null,CMMsg.MSG_SNIFF,(quiet?null:textMsg+"around."),CMMsg.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"at you."),CMMsg.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
