package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Help extends StdCommand
{
	public Help(){}
	
	private String[] access={"HELP"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String helpStr=Util.combine(commands,1);
		if(MUDHelp.getHelpFile().size()==0)
		{
			mob.tell("No help is available.");
			return false;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
			thisTag=Resources.getFileResource("help"+File.separatorChar+"help.txt");
		else
			thisTag=MUDHelp.getHelpText(helpStr,MUDHelp.getHelpFile(),mob);
		if((thisTag==null)&&(mob.isASysOp(mob.location())))
			thisTag=MUDHelp.getHelpText(helpStr,MUDHelp.getArcHelpFile(),mob);
		if(thisTag==null)
		{
			mob.tell("No help is available on '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.errOut("Help",mob.Name()+" wanted help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(thisTag.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
