package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prompt extends StdCommand
{
	public Prompt(){}
	
	private String[] access={"PROMPT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.session()==null) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		if(commands.size()==1)
			mob.session().rawPrintln("Your prompt is currently set at:\n\r"+pstats.getPrompt());
		else
		{
			String str=Util.combine(commands,1);
			if(("DEFAULT").startsWith(str.toUpperCase()))
				pstats.setPrompt("");
			else
				pstats.setPrompt(str);
			mob.session().rawPrintln("Your prompt is currently now set at:\n\r"+pstats.getPrompt());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
