package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class IMC2 extends StdCommand
{
	public IMC2(){}

	private String[] access={"IMC2"};
	public String[] getAccessWords(){return access;}

	public void IMC2Error(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"IMC2"))
			mob.tell("Try IMC2 LIST, IMC2 INFO [MUD], or IMC2 CHANNELS.");
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!(CMClass.I3Interface().imc2online()))
		{
			mob.tell("IMC2 is unavailable.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			IMC2Error(mob);
			return false;
		}
		String str=(String)commands.firstElement();
		if(!(CMClass.I3Interface().imc2online()))
			mob.tell("IMC2 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			CMClass.I3Interface().giveIMC2MudList(mob);
		else
		if(str.equalsIgnoreCase("channels"))
			CMClass.I3Interface().giveIMC2ChannelsList(mob);
		else
		if(str.equalsIgnoreCase("info"))
			CMClass.I3Interface().imc2mudInfo(mob,Util.combine(commands,1));
		else
			IMC2Error(mob);

		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
