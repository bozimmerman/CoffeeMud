package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Areas extends StdCommand
{
	public Areas(){}

	private String[] access={"AREAS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Vector areasVec=new Vector();
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(Sense.canAccess(mob,A))
				if(!Sense.isHidden(A))
					areasVec.addElement(" "+A.name());
				else
					areasVec.addElement("("+A.name()+")");
		}
		Collections.sort((java.util.List)areasVec);
		StringBuffer msg=new StringBuffer("^HComplete areas list:^?^N\n\r");
		int col=0;
		for(int i=0;i<areasVec.size();i++)
		{
			if((++col)>3)
			{
				msg.append("\n\r");
				col=1;
			}
			msg.append(Util.padRight((String)areasVec.elementAt(i),22)+"^N");
		}
		msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
