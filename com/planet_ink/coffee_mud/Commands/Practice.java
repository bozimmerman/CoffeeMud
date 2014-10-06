package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2004-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Practice extends StdCommand
{
	public Practice(){}

	private final String[] access=I(new String[]{"PRACTICE","PRAC"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(L("You have @x1 practice points.  Enter HELP PRACTICE for more information.",""+mob.getPractices()));
			return false;
		}
		commands.removeElementAt(0);

		MOB teacher=null;
		boolean triedTeacher=false;
		if(commands.size()>1)
		{
			teacher=mob.location().fetchInhabitant((String)commands.lastElement());
			if(teacher!=null)
			{
				triedTeacher=true;
				commands.removeElementAt(commands.size()-1);
			}
		}

		final String abilityName=CMParms.combine(commands,0);

		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach.findAbility(abilityName)!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}

		final Ability myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell(L("You don't seem to know @x1.",abilityName));
			return false;
		}
		
		if((teacher==null)||(!CMLib.flags().canBeSeenBy(teacher,mob)))
		{
			if(triedTeacher)
				mob.tell(L("That person doesn't seem to be here."));
			else
				mob.tell(L("There doesn't seem to be a teacher to practice with here."));
			return false;
		}

		if(!myAbility.isSavable())
		{
			mob.tell(L("@x1 cannot be practiced, as it is a native skill.",myAbility.name()));
			return false;
		}

		final Ability teacherAbility=mob.findAbility(abilityName);
		if(teacherAbility==null)
		{
			mob.tell(L("@x1 doesn't seem to know @x2.",teacher.name(),abilityName));
			return false;
		}

		if(!teacherAbility.canBeTaughtBy(teacher,mob))
			return false;
		if(!teacherAbility.canBePracticedBy(teacher,mob))
			return false;
		CMMsg msg=CMClass.getMsg(teacher,mob,null,CMMsg.MSG_SPEAK,null);
		if(!mob.location().okMessage(mob,msg))
			return false;
		msg=CMClass.getMsg(teacher,mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> practice(s) '@x1' with <T-NAMESELF>.",myAbility.name()));
		if(!mob.location().okMessage(mob,msg))
			return false;
		teacherAbility.practice(teacher,mob);
		mob.location().send(mob,msg);
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandActionCost(ID());}
	@Override public boolean canBeOrdered(){return false;}


}
