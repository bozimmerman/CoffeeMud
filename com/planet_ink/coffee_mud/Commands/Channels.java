package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
				boolean onoff=Util.isSet(pstats.getChannelMask(),x);
				buf.append(Util.padRight("^<CHANNELS '"+(onoff?"":"NO")+"'^>"+channelName+"^</CHANNELS^>"+(onoff?" (OFF)":""),24));
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
