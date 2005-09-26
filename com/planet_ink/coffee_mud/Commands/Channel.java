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
public class Channel extends BaseChanneler
{
	public Channel(){}
	public static String[] access=null;
	public String[] getAccessWords()
	{
		if(access!=null) return access;
		access=ChannelSet.getChannelNames();
		if(access!=null)
		{
			for(int i=0;i<access.length;i++)
				if(access[i].equalsIgnoreCase("AUCTION"))
					access[i]="";
		}
		return access;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()>2)&&(commands.firstElement() instanceof Boolean))
		{
			boolean systemMsg=((Boolean)commands.firstElement()).booleanValue();
			String channelName=(String)commands.elementAt(1);
			String message=(String)commands.elementAt(2);
			reallyChannel(mob,channelName,message,systemMsg);
			return true;
		}
		return channel(mob, commands, false);
	}

	public boolean channel(MOB mob, Vector commands, boolean systemMsg)
	{
		PlayerStats pstats=mob.playerStats();
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim();
		commands.removeElementAt(0);
		int channelInt=ChannelSet.getChannelIndex(channelName);
		int channelNum=ChannelSet.getChannelCodeNumber(channelName);

		if((pstats!=null)&&(Util.isSet(pstats.getChannelMask(),channelInt)))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(getScr("Channel","channelon",channelName,channelName.toUpperCase()));
			return false;
		}
		
		if(Util.bset(mob.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell(getScr("Channel","quietalon"));
			return false;
		}

		if(commands.size()==0)
		{
			mob.tell(getScr("Channel","what",channelName));
			return false;
		}

		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		if(!MUDZapper.zapperCheck(ChannelSet.getChannelMask(channelInt),mob))
		{
			mob.tell(getScr("Channel","notava"));
			return false;
		}
        
        Vector flags=ChannelSet.getChannelFlags(channelInt);
		if((mob.getClanID().equalsIgnoreCase(""))&&(flags.contains("CLANONLY")))
		{
            mob.tell(getScr("Channel","clandontex"));
            return false;
		}
        
        if(flags.contains("READONLY"))
        {
            mob.tell(getScr("Channel","readonly"));
            return false;
        }
        
		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(((String)commands.firstElement()).equalsIgnoreCase("last"))
		&&(Util.isNumber((String)commands.lastElement())))
		{
			int num=Util.s_int((String)commands.lastElement());
			Vector que=ChannelSet.getChannelQue(channelInt);
			if(que.size()==0)
			{
				mob.tell(getScr("Channel","noentries"));
				return false;
			}
			if(num>que.size()) num=que.size();
			boolean areareq=flags.contains("SAMEAREA");
			for(int i=que.size()-num;i<que.size();i++)
			{
				CMMsg msg=(CMMsg)que.elementAt(i);
				channelTo(mob.session(),areareq,channelInt,msg,msg.source());
			}
		}
		else
			reallyChannel(mob,channelName,Util.combine(commands,0),systemMsg);
		return false;
	}

	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
