package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class NoChannel extends StdCommand
{
	public NoChannel(){}

	private String[] access=null;
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim().substring(2);
		commands.removeElementAt(0);
		int channelNum=-1;
		for(int c=0;c<ChannelSet.getNumChannels();c++)
		{
			if(ChannelSet.getChannelName(c).toUpperCase().startsWith(channelName))
			{
				channelNum=c;
				channelName=ChannelSet.getChannelName(c);
			}
		}
		if((channelNum<0)
		||(!MUDZapper.zapperCheck(ChannelSet.getChannelMask(channelNum),mob)))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}
		if(!Util.isSet(pstats.getChannelMask(),channelNum))
		{
			pstats.setChannelMask(pstats.getChannelMask()|(1<<channelNum));
			mob.tell("The "+channelName+" channel has been turned off.  Use `"+channelName.toUpperCase()+"` to turn it back on.");
		}
		else
			mob.tell("The "+channelName+" channel is already off.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
