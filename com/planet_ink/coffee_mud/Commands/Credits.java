package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Credits extends StdCommand
{
	public Credits(){}

	private String[] access={"CREDITS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer credits=Resources.getFileResource("text"+File.separatorChar+"credits.txt");
		if((credits!=null)&&(mob.session()!=null))
			mob.session().rawPrintln(credits.toString());
		else
			mob.tell("CoffeeMud is (C)2001-2004 by Bo Zimmerman");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
