package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Channels extends StdCommand
{
	public Channels(){}

	private String[] access={"CHANNELS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		StringBuffer buf=new StringBuffer("Available channels: \n\r");
		int col=0;
		String[] names=ChannelSet.getChannelNames();
		for(int x=0;x<names.length;x++)
			if(MUDZapper.zapperCheck(ChannelSet.getChannelMask(x),mob))
			{
				if((++col)>3)
				{
					buf.append("\n\r");
					col=1;
				}
				String channelName=names[x];
				String onoff="";
				if(Util.isSet((int)pstats.getChannelMask(),x))
					onoff=" (OFF)";
				buf.append(Util.padRight(channelName+onoff,24));
			}
		if(names.length==0)
			buf.append("None!");
		else
			buf.append("\n\rUse NOCHANNELNAME (ex: NOGOSSIP) to turn a channel off.");
		mob.tell(buf.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
