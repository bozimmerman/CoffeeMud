package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
