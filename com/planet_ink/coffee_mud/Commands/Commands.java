package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Commands extends StdCommand
{
	public Commands(){}

	private String[] access={"COMMANDS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			boolean arc=CMSecurity.isStaff(mob);
			StringBuffer commandList=(StringBuffer)Resources.getResource((arc?"ARC":"")+"COMMAND LIST");
			if(commandList==null)
			{
				commandList=new StringBuffer("");
				int col=0;
				for(Enumeration e=CMClass.commands();e.hasMoreElements();)
				{
					Command C=(Command)e.nextElement();
					String[] access=C.getAccessWords();
					if((access!=null)
					&&(access.length>0)
					&&(access[0].length()>0)
					&&(arc||(C.securityCheck(mob))))
					{
						if(++col>3){ commandList.append("\n\r"); col=0;}
						commandList.append(Util.padRight(access[0],19));
					}
				}
				commandList.append("\n\r\n\rEnter HELP 'COMMAND' for more information on these commands.\n\r");
				Resources.submitResource((arc?"ARC":"")+"COMMAND LIST",commandList);
			}
			mob.session().colorOnlyPrintln("^HComplete commands list:^?\n\r"+commandList.toString(),23);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
