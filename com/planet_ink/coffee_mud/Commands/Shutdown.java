package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Shutdown extends StdCommand
{
	public Shutdown(){}

	private String[] access={"SHUTDOWN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons can do that.");
			return false;
		}
		if(mob.isMonster()) return false;
		boolean keepItDown=true;
		boolean noPrompt=false;
		String externalCommand=null;
		for(int i=commands.size()-1;i>=1;i--)
		{
			String s=(String)commands.elementAt(i);
			if(s.equalsIgnoreCase("RESTART"))
			{ keepItDown=false; commands.removeElementAt(i);}
			else
			if(s.equalsIgnoreCase("NOPROMPT"))
			{ noPrompt=true; commands.removeElementAt(i); }
		}
		if((!keepItDown)&&(commands.size()>1))
			externalCommand=Util.combine(commands,1);

		if((!noPrompt)
		&&(!mob.session().confirm("Are you fully aware of the consequences of this act (y/N)?","N")))
			return false;

		if(keepItDown)
			Log.errOut("CommandProcessor",mob.Name()+" starts system shutdown...");
		else
		if(externalCommand!=null)
			Log.errOut("CommandProcessor",mob.Name()+" starts system restarting '"+externalCommand+"'...");
		else
			Log.errOut("CommandProcessor",mob.Name()+" starts system restart...");
		mob.tell("Starting shutdown...");
		//*TODO Fix Shutdown!!

		//if(ExternalPlay.myHost!=null)
		//	myHost.shutdown(mob.session(),keepItDown,externalCommand);
		//else
		//{
			mob.tell("Shutdown failed.  No host.");
			Log.errOut("CommandProcessor","Shutdown failed.  No host.");
		//}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
