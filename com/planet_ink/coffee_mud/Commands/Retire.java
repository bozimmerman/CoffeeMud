package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Retire extends StdCommand
{
	public Retire(){}

	private String[] access={"RETIRE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.isMonster()) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		mob.tell("^HThis will delete your player from the system FOREVER!");
		String pwd=mob.session().prompt("If that's what you want, re-enter your password:","");
		if(pwd.length()==0) return false;
		if(!pwd.equalsIgnoreCase(pstats.password()))
		{
			mob.tell("Password incorrect.");
			return false;
		}
		mob.tell("^HThis will delete your player from the system FOREVER!");
		pwd=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
		if(pwd.equalsIgnoreCase("Y"))
		{
			mob.tell("Fine!  Goodbye then!");
			CoffeeUtensils.obliteratePlayer(mob,false);
		}
		else
			mob.tell("Whew.  Close one.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
