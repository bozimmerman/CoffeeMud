package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DumpFile extends StdCommand
{
	public DumpFile(){}

	private String[] access={"DUMPFILE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("dumpfile {raw} username|all {filename1 ...}");
			return false;
		}
		commands.removeElementAt(0);

		int numFiles = 0;
		int numSessions = 0;
		boolean rawMode=false;

		if(((String)commands.elementAt(0)).equalsIgnoreCase("raw"))
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		String targetName = new String((String)commands.elementAt(0));
		boolean allFlag=(targetName.equalsIgnoreCase("all"));

		commands.removeElementAt(0);

		// so they can do dumpfile (username) RAW filename too
		if(!rawMode && ( ((String)commands.elementAt(0)).equalsIgnoreCase("raw")) )
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		StringBuffer fileText = new StringBuffer("");
		while (commands.size() > 0)
		{
			boolean wipeAfter = true;
			String fn = new String ( (String)commands.elementAt(0) );
			// don't allow any path characters!
			fn.replace('/','_');
			fn.replace('\\','_');
			fn.replace(':','_');

			if (Resources.getResource(fn) != null)
				wipeAfter = false;

			StringBuffer ft = Resources.getFileResource(fn);
			if (ft != null && ft.length() > 0)
			{
				fileText.append("\n\r");
				fileText.append(ft);
				++numFiles;
			}

			if (wipeAfter)
				Resources.removeResource(fn);
			commands.removeElementAt(0);

		}
		if (fileText.length() > 0)
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session thisSession=(Session)Sessions.elementAt(s);

				if (thisSession==null) continue;
				if (thisSession.killFlag() || (thisSession.mob()==null)) continue;
				if(!CMSecurity.isAllowed(mob,thisSession.mob().location(),"DUMPFILE"))
					continue;
				if (allFlag || thisSession.mob().name().equalsIgnoreCase(targetName))
				{
					if (rawMode)
						thisSession.rawPrintln(fileText.toString());
					else
						thisSession.colorOnlyPrintln(fileText.toString());
					++numSessions;
				}
			}
		}
		mob.tell("dumped " + numFiles + " files to " + numSessions + " user(s)");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"DUMPFILE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
