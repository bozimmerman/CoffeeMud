package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Quiet extends StdCommand
{
	public Quiet(){}

	private String[] access={"QUIET"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		boolean turnedoff=false;
		String[] names=ChannelSet.getChannelNames();
		for(int c=0;c<names.length;c++)
		{
			if(!Util.isSet(pstats.getChannelMask(),c))
			{
				pstats.setChannelMask(pstats.getChannelMask()|(1<<c));
				turnedoff=true;
			}
		}
		if(turnedoff)
			mob.tell("All channels have been turned off.");
		else
		{
			mob.tell("All channels have been turned back on.");
			pstats.setChannelMask(0);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
