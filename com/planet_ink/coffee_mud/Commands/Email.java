package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
		return true;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
