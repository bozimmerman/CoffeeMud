package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WizEmote extends StdCommand
{
	public WizEmote(){}

	private String[] access={"WIZEMOTE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()>2)
		{
			String who=(String)commands.elementAt(1);
			String msg=Util.combine(commands,2);
			if(who.toUpperCase().equals("ALL"))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
	  					S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
			{
				boolean found=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE"))
					&&(EnglishParser.containsString(S.mob().name(),who)
						||EnglishParser.containsString(S.mob().location().getArea().name(),who)))
					{
	  					S.stdPrintln("^w"+msg+"^?");
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone or anywhere by that name.");
			}
	    }
	    else
			mob.tell("You must specify either all, or an area/mob name, and an message.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"WIZEMOTE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
