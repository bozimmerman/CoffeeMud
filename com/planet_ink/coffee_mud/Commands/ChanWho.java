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
public class ChanWho extends StdCommand
{
	public ChanWho(){}

	private String[] access={"CHANWHO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String channel=Util.combine(commands,1);
		if((channel==null)||(channel.length()==0))
		{
			mob.tell("You must specify a channel name. Try CHANNELS for a list.");
			return false;
		}
		int x=channel.indexOf("@");
		String mud=null;
		if(x>0)
		{
			mud=channel.substring(x+1);
			int channelInt=ChannelSet.getChannelIndex(channel.substring(0,x).toUpperCase());
			channel=ChannelSet.getChannelName(channelInt).toUpperCase();
			if((channel.length()==0)||(channelInt<0))
			{
				mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
				return false;
			}
			CMClass.I3Interface().i3chanwho(mob,channel,mud);
			return false;
		}
		int channelInt=ChannelSet.getChannelIndex(channel.toUpperCase());
		channel=ChannelSet.getChannelName(channelInt);
		if(channelInt<0)
		{
			mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
			return false;
		}
		String head=new String("\n\rListening on "+channel+":\n\r");
		StringBuffer buf=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session ses=Sessions.elementAt(s);
			if((ChannelSet.mayReadThisChannel(null,false,ses,channelInt))
			&&(ses.mob()!=null)
			&&((((ses.mob().envStats().disposition()&EnvStats.IS_CLOAKED)==0)
					||((CMSecurity.isAllowedAnywhere(mob,"CLOAK")||CMSecurity.isAllowedAnywhere(mob,"WIZINV"))&&(mob.envStats().level()>=ses.mob().envStats().level())))))
					buf.append("["+Util.padRight(ses.mob().name(),20)+"]\n\r");
		}
		if(buf.length()==0)
			mob.tell(head+"Nobody!");
		else
			mob.tell(head+buf.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
