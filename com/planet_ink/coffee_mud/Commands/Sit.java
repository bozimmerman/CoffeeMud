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
public class Sit extends StdCommand
{
	public Sit(){}

	private String[] access={"SIT","REST","R"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(CMLib.flags().isSitting(mob))
		{
			mob.tell("You are already sitting!");
			return false;
		}
		if(commands.size()<=1)
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SIT,"<S-NAME> sit(s) down and take(s) a rest.");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		String possibleRideable=CMParms.combine(commands,1);
		Environmental E=null;
		if(possibleRideable.length()>0)
		{
			E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Wearable.FILTER_UNWORNONLY);
			if((E==null)||(!CMLib.flags().canBeSeenBy(E,mob)))
			{
				mob.tell("You don't see '"+possibleRideable+"' here.");
				return false;
			}
			if(E instanceof MOB)
			{
				Command C=CMClass.getCommand("Mount");
				if(C!=null) return C.execute(mob,commands,metaFlags);
			}
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr="<S-NAME> "+((Rideable)E).mountString(CMMsg.TYP_SIT,mob)+" <T-NAME>.";
		else
			mountStr="<S-NAME> sit(s) on <T-NAME>.";
		CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SIT,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
