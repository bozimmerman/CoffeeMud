package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Snoop extends StdCommand
{
	public Snoop(){}

	private String[] access={"SNOOP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Mind your own business!");
			return false;
		}
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		boolean doneSomething=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if(S.amSnooping(mob.session()))
			{
				if(S.mob()!=null)
					mob.tell("You stop snooping on "+S.mob().name()+".");
				else
					mob.tell("You stop snooping on someone.");
				doneSomething=true;
				S.stopSnooping(mob.session());
			}
		}
		if(commands.size()==0)
		{
			if(!doneSomething)
				mob.tell("Snoop on whom?");
			return false;
		}
		String whom=Util.combine(commands,0);
		boolean snoop=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(EnglishParser.containsString(S.mob().name(),whom)))
			{
				if(S==mob.session())
				{
					mob.tell("no.");
					return false;
				}
				else
				if((!S.amSnooping(mob.session()))
				&&(mob.isASysOp(S.mob().location())))
				{
					mob.tell("You start snooping on "+S.mob().name()+".");
					S.startSnooping(mob.session());
					snoop=true;
					break;
				}
			}
		}
		if(!snoop)
		mob.tell("You can't find anyone by that name.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
