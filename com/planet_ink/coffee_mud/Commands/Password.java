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
public class Password extends StdCommand
{
	public Password(){}

	private String[] access={"PASSWORD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
        if(mob.isMonster()) return false;
        String old=mob.session().prompt("Enter your old password : ");
        String nep=mob.session().prompt("Enter a new password    : ");
        String ne2=mob.session().prompt("Enter new password again: ");
        if(!pstats.password().equals(old))
        {
            mob.tell("Your old password was not entered correctly.");
            return false;
        }
        if(!nep.equals(ne2))
        {
            mob.tell("Your new password was not entered the same way twice!");
            return false;
        }
		pstats.setPassword(nep);
		mob.tell("Your password has been changed.");
		CMClass.DBEngine().DBUpdatePassword(mob);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
