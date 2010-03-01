package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Look extends StdCommand
{
	public Look(){}

	private String[] access={"LOOK","LOO","LO","L"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();
		boolean quiet=false;
		if((commands!=null)&&(commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		String textMsg="<S-NAME> look(s) ";
		if(R==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			Environmental thisThang=null;
			
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("at")))
			   commands.removeElementAt(1);
			else
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("to")))
			   commands.removeElementAt(1);
			String ID=CMParms.combine(commands,1);
			
			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2))&&(CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)!=1))
			{
				if((CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)>=2)!=CMath.bset(mob.getBitmap(), MOB.ATT_BRIEF))
	                CMLib.commands().lookAtExitsShort(R,mob);
				else
	                CMLib.commands().lookAtExits(R,mob);
				return false;
			}
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			
			if(thisThang==null)
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID,Wearable.FILTER_ANY);
			if((thisThang==null)
			&&(commands.size()>2)
			&&(((String)commands.elementAt(1)).equalsIgnoreCase("in")))
			{
				commands.removeElementAt(1);
				String ID2=CMParms.combine(commands,1);
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID2,Wearable.FILTER_ANY);
				if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
				{
					mob.tell("That's not a container.");
					return false;
				}
			}
			int dirCode=-1;
            Environmental lookingTool=null;
			if(thisThang==null)
			{
				dirCode=Directions.getGoodDirectionCode(ID);
				if(dirCode>=0)
				{
					Room room=R.getRoomInDir(dirCode);
					Exit exit=R.getExitInDir(dirCode);
					if((room!=null)&&(exit!=null))
                    {
						thisThang=exit;
                        lookingTool=room;
                    }
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
					if(thisThang==R)
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				CMMsg msg=CMClass.getMsg(mob,thisThang,lookingTool,CMMsg.MSG_LOOK,textMsg+name+".");
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
				if((thisThang instanceof Room)&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))&&(CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)!=1))
				{
					if((CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)>=2)!=CMath.bset(mob.getBitmap(), MOB.ATT_BRIEF))
		                CMLib.commands().lookAtExitsShort(R,mob);
					else
						CMLib.commands().lookAtExits((Room)thisThang,mob);
				}
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

			CMMsg msg=CMClass.getMsg(mob,R,null,CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"at you."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			if((CMath.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
			&&(CMLib.flags().canBeSeenBy(R,mob))
			&&(CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)!=1))
			{
				if((CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)>=2)!=CMath.bset(mob.getBitmap(), MOB.ATT_BRIEF))
	                CMLib.commands().lookAtExitsShort(R,mob);
				else
	                CMLib.commands().lookAtExits(R,mob);
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
