package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Announce extends StdCommand
{
	public Announce(){}

	private String[] access={"ANNOUNCE"};
	public String[] getAccessWords(){return access;}

	public void sendAnnounce(String announcement, Session S)
	{
	  	StringBuffer Message=new StringBuffer("");
	  	int alignType=1;
	  	if(S.mob().getAlignment()<350)
	  		alignType=0;
	  	else
	  	if(S.mob().getAlignment()<650) alignType= 2;
	  	switch(alignType)
	  	{
	  	  case 0:
	  	    Message.append("^rA terrifying voice bellows out of Hell '");
	  	    break;
	  	  case 1:
	  	    Message.append("^wAn awe-inspiring voice thunders down from Heaven '");
	  	    break;
	  	  case 2:
	  	    Message.append("^pA powerful voice rings out '");
	  	    break;
	  	}
	  	Message.append(announcement);
	  	Message.append("'.^N");
	  	S.stdPrintln(Message.toString());
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		if(commands.size()>1)
		{
			if(((String)commands.elementAt(1)).toUpperCase().equals("ALL"))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)&&(S.mob().location()!=null)&&(mob.isASysOp(S.mob().location())))
						sendAnnounce((String)Util.combine(commands,2),S);
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
					&&(mob.isASysOp(S.mob().location()))
					&&(EnglishParser.containsString(S.mob().name(),(String)commands.elementAt(1))))
					{
						sendAnnounce(Util.combine(commands,2),S);
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone by that name.");
			}
		}
		else
			mob.tell("You can either send a message to everyone on the server or a single user using \n\r    ANNOUNCE [ALL|(USER NAME)] (MESSAGE) \n\rGood aligned players will perceive it as coming from heaven, evil from hell, and neutral from out of nowhere.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
