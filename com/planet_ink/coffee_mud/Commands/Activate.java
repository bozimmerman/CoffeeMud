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
public class Activate extends StdCommand
{
	public Activate(){}

	private String[] access={"ACTIVATE","ACT","A",">"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Activate what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,what,Wearable.FILTER_ANY);
		Item item=null;
		if(mob.riding() instanceof Electronics)
		{
		    if((E==null)||(cmd.equalsIgnoreCase(">")))
		        item=(Item)mob.riding();
		}
		else
		    commands.removeElementAt(commands.size()-1);
		if((item==null)&&(E instanceof Electronics))
		    item=(Item)E;
		if((E==null)||(!CMLib.flags().canBeSeenBy(E,mob)))
			mob.tell("You don't see anything called "+what+" here that you can activate.");
		else
		if(item==null)
			mob.tell("You can't activate "+E.name()+"'.");

		String rest=CMParms.combine(commands,0);
		CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_ACTIVATE,null,CMMsg.MSG_ACTIVATE,rest,CMMsg.MSG_ACTIVATE,null);
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}
    public boolean securityCheck(MOB mob){return CMSecurity.isASysOp(mob);}

	
}
