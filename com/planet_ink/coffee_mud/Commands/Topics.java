package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Topics extends ATopics
{
	public Topics(){}

	private String[] access={"TOPICS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Properties helpFile=MUDHelp.getHelpFile();
		if(helpFile.size()==0)
		{
			if(mob!=null)
				mob.tell("No help is available.");
			return false;
		}

		doTopics(mob,helpFile,"HELP", "PLAYER TOPICS");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
