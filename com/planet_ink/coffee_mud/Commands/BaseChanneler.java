package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class BaseChanneler extends StdCommand
{
	public static void channelTo(Session ses,
								 boolean areareq,
								 int channelInt,
								 CMMsg msg,
								 MOB mob)
	{
		if(ChannelSet.mayReadThisChannel(mob,areareq,ses,channelInt)
		&&(ses.mob().okMessage(ses.mob(),msg)))
		{
			MOB M=ses.mob();
			M.executeMsg(M,msg);
			if(msg.trailerMsgs()!=null)
			{
				for(int i=0;i<msg.trailerMsgs().size();i++)
				{
					CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
					if((msg!=msg2)&&(M.okMessage(M,msg2)))
						M.executeMsg(M,msg2);
				}
				msg.trailerMsgs().clear();
			}
		}
	}
	
	public static void reallyChannel(MOB mob,
									 String channelName,
									 String message,
									 boolean systemMsg)
	{
		int channelInt=ChannelSet.getChannelInt(channelName);
		if(channelInt<0) return;
		
		message=CommonStrings.applyFilter(message,CommonStrings.SYSTEM_CHANNELFILTER);
		
		String mask=ChannelSet.getChannelMask(channelInt);
		channelName=ChannelSet.getChannelName(channelInt);

		CMMsg msg=null;
		if(systemMsg)
		{
			String str="["+channelName+"] '"+message+"'^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
				str=" "+str;
			msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^q"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q<S-NAME>"+str);
		}
		else
		if((message.startsWith(":")||message.startsWith(","))
		   &&(message.trim().length()>3))
		{
			String msgstr=message.substring(1);
			Vector V=Util.parse(msgstr);
			Social S=Socials.FetchSocial(V,true);
			if(S==null) S=Socials.FetchSocial(V,false);
			if(S!=null)
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
			else
			{
				msgstr=CommonStrings.applyFilter(msgstr,CommonStrings.SYSTEM_EMOTEFILTER);
				String str="["+channelName+"] <S-NAME> "+msgstr+"^?^.";
				msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^q"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q"+str);
			}
		}
		else
			msg=new FullMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_GENERAL|CMMsg.MSG_SPEAK,"^Q^qYou "+channelName+" '"+message+"'^?^.",CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^q<S-NAME> "+channelName+"S '"+message+"'^?^.");
		if((mob.location()!=null)
		&&((!mob.location().isInhabitant(mob))||(mob.location().okMessage(mob,msg))))
		{
			boolean areareq=mask.toUpperCase().indexOf("SAMEAREA")>=0;
			ChannelSet.channelQueUp(channelInt,msg);
			for(int s=0;s<Sessions.size();s++)
			{
				Session ses=Sessions.elementAt(s);
				channelTo(ses,areareq,channelInt,msg,mob);
			}
		}
		if((CMClass.I3Interface().i3online()&&(CMClass.I3Interface().isI3channel(channelName)))
		||(CMClass.I3Interface().imc2online()&&(CMClass.I3Interface().isIMC2channel(channelName))))
			CMClass.I3Interface().i3channel(mob,channelName,message);
	}
}
