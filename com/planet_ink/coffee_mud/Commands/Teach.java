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
public class Teach extends StdCommand
{
	public Teach(){}

	private String[] access={"TEACH"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("AbilityEvoker","teacherr1"));
			return false;
		}
		commands.removeElementAt(0);


		MOB student=mob.location().fetchInhabitant((String)commands.elementAt(0));
		if((student==null)||((student!=null)&&(!Sense.canBeSeenBy(student,mob))))
		{
			mob.tell(getScr("AbilityEvoker","teacherr2"));
			return false;
		}
		commands.removeElementAt(0);


		String abilityName=Util.combine(commands,0);
		Ability realAbility=CMClass.findAbility(abilityName,student.charStats());
		Ability myAbility=null;
		if(realAbility!=null)
			myAbility=mob.fetchAbility(realAbility.ID());
		else
			myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","teacherr3",abilityName));
			return false;
		}
		if(!myAbility.canBeTaughtBy(mob,student))
			return false;
		if(!myAbility.canBeLearnedBy(mob,student))
			return false;
		if(student.fetchAbility(myAbility.ID())!=null)
		{
			mob.tell(getScr("AbilityEvoker","teacherr4",student.name()));
			return false;
		}
		if((student.session()!=null)&&(!student.session().confirm(mob.Name()+" wants to teach you "+myAbility.name()+".  Is this Ok (y/N)?","N")))
			return false;
		FullMsg msg=new FullMsg(mob,student,null,CMMsg.MSG_SPEAK,null);
		if(!mob.location().okMessage(mob,msg))
			return false;
		msg=new FullMsg(mob,student,null,CMMsg.MSG_TEACH,getScr("AbilityEvoker","teaches",myAbility.name()));
		if(!mob.location().okMessage(mob,msg))
			return false;
		myAbility.teach(mob,student);
		mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
