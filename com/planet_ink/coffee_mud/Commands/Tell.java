package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Tell extends StdCommand
{
	public Tell(){}

	private String[] access={"TELL","T"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Tell whom what?");
			return false;
		}
		commands.removeElementAt(0);
		MOB target=null;
		String targetName=((String)commands.elementAt(0)).toUpperCase();
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&((thisSession.mob().name().equalsIgnoreCase(targetName))
				  ||(thisSession.mob().Name().equalsIgnoreCase(targetName))))
			{
				target=thisSession.mob();
				break;
			}
		}
		if(target==null)
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&((EnglishParser.containsString(thisSession.mob().name(),targetName))
				  ||(EnglishParser.containsString(thisSession.mob().Name(),targetName))))
			{
				target=thisSession.mob();
				break;
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Tell them what?");
			return false;
		}
		if(target==null)
		{
			if(targetName.indexOf("@")>=0)
			{
				String mudName=targetName.substring(targetName.indexOf("@")+1);
				targetName=targetName.substring(0,targetName.indexOf("@"));
				if(!(CMClass.I3Interface().i3online()))
					mob.tell("I3 is unavailable.");
				else
					CMClass.I3Interface().i3tell(mob,targetName,mudName,combinedCommands);
				return false;
			}
			else
			{
				mob.tell("That person doesn't appear to be online.");
				return false;
			}
		}
		CommonMsgs.say(mob,target,combinedCommands,true,true);
		if((target.session()!=null)
		&&(target.session().afkFlag()))
			mob.tell(target.name()+" is AFK at the moment.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
