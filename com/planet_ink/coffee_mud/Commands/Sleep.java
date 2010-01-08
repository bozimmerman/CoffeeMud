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
public class Sleep extends StdCommand
{
	public Sleep(){}

	private String[] access={"SLEEP","SL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(CMLib.flags().isSleeping(mob))
		{
			mob.tell("You are already asleep!");
			return false;
		}
		if(commands.size()<=1)
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SLEEP,"<S-NAME> lay(s) down and take(s) a nap.");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		String possibleRideable=CMParms.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Wearable.FILTER_UNWORNONLY);
		if((E==null)||(!CMLib.flags().canBeSeenBy(E,mob)))
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return false;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr="<S-NAME> "+((Rideable)E).mountString(CMMsg.TYP_SLEEP,mob)+" <T-NAME>.";
		else
			mountStr="<S-NAME> sleep(s) on <T-NAME>.";
		String sourceMountStr=null;
		if(!CMLib.flags().canBeSeenBy(E,mob))
			sourceMountStr=mountStr;
		else
		{
			sourceMountStr=CMStrings.replaceAll(mountStr,"<T-NAME>",E.name());
			sourceMountStr=CMStrings.replaceAll(sourceMountStr,"<T-NAMESELF>",E.name());
		}
		CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SLEEP,sourceMountStr,mountStr,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
