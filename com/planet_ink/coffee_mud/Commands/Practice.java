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
public class Practice extends StdCommand
{
	public Practice(){}

	private String[] access={"PRACTICE","PRAC"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("You have "+mob.getPractices()+" practice points.  Enter HELP PRACTICE for more information.");
			return false;
		}
		commands.removeElementAt(0);

		MOB teacher=null;
		if(commands.size()>1)
		{
			teacher=mob.location().fetchInhabitant((String)commands.lastElement());
			if(teacher!=null) commands.removeElementAt(commands.size()-1);
		}

		String abilityName=CMParms.combine(commands,0);

		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach.findAbility(abilityName)!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}

		if((teacher==null)||(!CMLib.flags().canBeSeenBy(teacher,mob)))
		{
			mob.tell("That person doesn't seem to be here.");
			return false;
		}

		Ability myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell("You don't seem to know "+abilityName+".");
			return false;
		}
		if(!myAbility.savable())
		{
			mob.tell(abilityName+" cannot be practiced, as it is a native skill.");
			return false;
		}

		Ability teacherAbility=mob.findAbility(abilityName);
		if(teacherAbility==null)
		{
			mob.tell(teacher.name()+" doesn't seem to know "+abilityName+".");
			return false;
		}

		if(!teacherAbility.canBeTaughtBy(teacher,mob))
			return false;
		if(!teacherAbility.canBePracticedBy(teacher,mob))
			return false;
		CMMsg msg=CMClass.getMsg(teacher,mob,null,CMMsg.MSG_SPEAK,null);
		if(!mob.location().okMessage(mob,msg))
			return false;
		msg=CMClass.getMsg(teacher,mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> practice(s) '"+myAbility.name()+"' with <T-NAMESELF>.");
		if(!mob.location().okMessage(mob,msg))
			return false;
		teacherAbility.practice(teacher,mob);
		mob.location().send(mob,msg);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
