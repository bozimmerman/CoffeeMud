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
public class Email extends StdCommand
{
	public Email(){}

	private String[] access={"EMAIL"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.session()==null)	return true;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return true;

		if((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
			mob.session().println("\n\rYou have no email address on file for this character.");
		else
		{
			if(commands==null) return true;
			String change=mob.session().prompt("You currently have '"+pstats.getEmail()+"' set as the email address for this character.\n\rChange it (y/N)?","N");
			if(change.toUpperCase().startsWith("N")) return false;
		}
        if((CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(commands!=null)
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0))
            mob.session().println("\n\r** Changing your email address will cause you to be logged off, and a new password to be generated and emailed to the new address. **\n\r");
		String newEmail=mob.session().prompt("New E-mail Address:");
		if(newEmail==null) return false;
		newEmail=newEmail.trim();
		if(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"))
		{
			if(newEmail.length()<6) return false;
			if(newEmail.indexOf("@")<0) return false;
			String confirmEmail=mob.session().prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
			if(confirmEmail==null) return false;
			confirmEmail=confirmEmail.trim();
			if(confirmEmail.length()==0) return false;
			if(!(newEmail.equalsIgnoreCase(confirmEmail))) return false;
		}
        pstats.setEmail(newEmail);
        CMClass.DBEngine().DBUpdateEmail(mob);
        if((commands!=null)
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        &&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0))
        {
            String password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+Dice.roll(1,26,-1));
            pstats.setPassword(password);
            CMClass.DBEngine().DBUpdatePassword(mob);
            CMClass.DBEngine().DBWriteJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
                      mob.Name(),
                      mob.Name(),
                      "Password for "+mob.Name(),
                      "Your new password for "+mob.Name()+" is: "+pstats.password()+"\n\rYou can login by pointing your mud client at "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN)+" port(s):"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.",-1);
            mob.tell("You will receive an email with your new password shortly.  Goodbye.");
            if(mob.session()!=null)
            {
                try{Thread.sleep(1000);}catch(Exception e){}
                mob.session().setKillFlag(true);
            }
        }
		return true;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
