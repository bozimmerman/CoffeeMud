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
public class Practice extends StdCommand
{
	public Practice(){}

	private String[] access={"PRACTICE","PRAC"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("AbilityEvoker","pracerr1",""+mob.getPractices()));
			return false;
		}
		commands.removeElementAt(0);

		MOB teacher=null;
		if(commands.size()>1)
		{
			teacher=mob.location().fetchInhabitant((String)commands.lastElement());
			if(teacher!=null) commands.removeElementAt(commands.size()-1);
		}

		String abilityName=Util.combine(commands,0);

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

		if((teacher==null)||((teacher!=null)&&(!Sense.canBeSeenBy(teacher,mob))))
		{
			mob.tell(getScr("AbilityEvoker","pracerr2"));
			return false;
		}

		Ability myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","pracerr3",abilityName));
			return false;
		}
		if(myAbility.isBorrowed(mob))
		{
			mob.tell(getScr("AbilityEvoker","pracerr5",abilityName));
			return false;
		}

		Ability teacherAbility=mob.findAbility(abilityName);
		if(teacherAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","pracerr4",teacher.name(),abilityName));
			return false;
		}

		if(!teacherAbility.canBeTaughtBy(teacher,mob))
			return false;
		if(!teacherAbility.canBePracticedBy(teacher,mob))
			return false;
		FullMsg msg=new FullMsg(teacher,mob,null,CMMsg.MSG_SPEAK,null);
		if(!mob.location().okMessage(mob,msg))
			return false;
		msg=new FullMsg(teacher,mob,null,CMMsg.MSG_NOISYMOVEMENT,getScr("AbilityEvoker","practices",myAbility.name()));
		if(!mob.location().okMessage(mob,msg))
			return false;
		teacherAbility.practice(teacher,mob);
		mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
