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

	private String[] access={getScr("ChanWho","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String channel=Util.combine(commands,1);
		if((channel==null)||(channel.length()==0))
		{
			mob.tell(getScr("ChanWho","specname"));
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
				mob.tell(getScr("ChanWho","validname"));
				return false;
			}
			CMClass.I3Interface().i3chanwho(mob,channel,mud);
			return false;
		}
		int channelInt=ChannelSet.getChannelIndex(channel.toUpperCase());
		channel=ChannelSet.getChannelName(channelInt);
		if(channelInt<0)
		{
			mob.tell(getScr("ChanWho","validname"));
			return false;
		}
		String head=new String(getScr("ChanWho","listening")+" "+channel+":\n\r");
		StringBuffer buf=new StringBuffer("");
        String mask=ChannelSet.getChannelMask(channelInt);
        boolean areareq=mask.toUpperCase().indexOf("SAMEAREA")>=0;
		for(int s=0;s<Sessions.size();s++)
		{
			Session ses=Sessions.elementAt(s);
			MOB mob2=ses.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();
			if((ChannelSet.mayReadThisChannel(mob,areareq,ses,channelInt))
			&&(mob2!=null)
			&&(Sense.isInTheGame(mob2,true))
			&&((((mob2.envStats().disposition()&EnvStats.IS_CLOAKED)==0)
					||((CMSecurity.isAllowedAnywhere(mob,"CLOAK")||CMSecurity.isAllowedAnywhere(mob,"WIZINV"))&&(mob.envStats().level()>=mob2.envStats().level())))))
					buf.append("["+Util.padRight(mob2.name(),20)+"]\n\r");
		}
		if(buf.length()==0)
			mob.tell(getScr("ChanWho","nobody",head));
		else
			mob.tell(head+buf.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
