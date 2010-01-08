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
public class Sniff extends StdCommand
{
	public Sniff(){}

	private String[] access={"SNIFF","SMELL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean quiet=false;
		if((commands!=null)&&(commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		String textMsg="<S-NAME> sniff(s)";
		if(mob.location()==null) 
		    return false;
		
		if((commands!=null)&&(commands.size()>1))
		{
			Environmental thisThang=null;
			
			String ID=CMParms.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;
			
			if(thisThang==null)
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Wearable.FILTER_ANY);
			if(thisThang!=null)
			{
				String name=" <T-NAMESELF>";
 				if(thisThang instanceof Room)
				{
					if(thisThang==mob.location())
						name=" around";
				}
				CMMsg msg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_SNIFF,textMsg+name+".");
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

			CMMsg msg=CMClass.getMsg(mob,mob.location(),null,CMMsg.MSG_SNIFF,(quiet?null:textMsg+" around."),CMMsg.MSG_SNIFF,(quiet?null:textMsg+" you."),CMMsg.MSG_SNIFF,(quiet?null:textMsg+" around."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
    public double combatActionsCost(MOB mob, Vector cmds){return 0.25;}
	public boolean canBeOrdered(){return true;}

	
}
