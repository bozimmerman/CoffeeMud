package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.exceptions.HTTPRedirectException;

public class MOTD extends StdCommand
{
	public MOTD(){}

	private String[] access={"MOTD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
        String msg = Resources.getFileResource("text"+File.separatorChar+"motd.txt").toString();
		if(msg.length()==0)
		{
			mob.tell("This feature is not enabled.");
			return false;
		}
		
		if((commands!=null)
		&&(commands.size()>1)
		&&Util.combine(commands,1).equalsIgnoreCase("AGAIN")
		&&(msg.length()>0))
		{
			try
			{
				if(msg.startsWith("<cmvp>"))
					msg=new String(CMClass.httpUtils().doVirtualPage(msg.substring(6).getBytes()));
			}
			catch(HTTPRedirectException e){}
			if(mob.session()!=null)
				mob.session().unfilteredPrintln(msg+"\n\r--------------------------------------\n\r");
			return false;
		}
		
		if(Util.bset(mob.getBitmap(),MOB.ATT_AUTOLOOT))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned on.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned off.");
		}
		mob.tell("Enter MOTD AGAIN to see the message over again.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
