package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class I3Cmd extends StdCommand
{
	public I3Cmd(){}

	private String[] access={"I3"};
	public String[] getAccessWords(){return access;}

	public void i3Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"I3"))
			mob.tell("Try I3 LIST, I3 CHANNELS, I3 ADD [CHANNEL], I3 DELETE [CHANNEL], I3 LISTEN [CHANNEL], or I3 INFO [MUD].");
		else
			mob.tell("Try I3 LIST or I3 INFO [MUD-NAME].");
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!(CMClass.I3Interface().i3online()))
		{
			mob.tell("I3 is unavailable.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			i3Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMClass.I3Interface().i3online()))
			mob.tell("I3 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			CMClass.I3Interface().giveI3MudList(mob);
		else
		if(str.equalsIgnoreCase("add"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMClass.I3Interface().i3channelAdd(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("channels"))
			CMClass.I3Interface().giveI3ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("delete"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMClass.I3Interface().i3channelRemove(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("listen"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMClass.I3Interface().i3channelListen(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("silence"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"I3")){ i3Error(mob); return false;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return false;
			}
			CMClass.I3Interface().i3channelSilence(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("info"))
			CMClass.I3Interface().i3mudInfo(mob,Util.combine(commands,1));
		else
			i3Error(mob);

		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
