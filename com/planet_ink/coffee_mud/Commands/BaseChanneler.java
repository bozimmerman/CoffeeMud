package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
								 MOB sender)
	{
        MOB M=ses.mob();
		if(CMLib.channels().mayReadThisChannel(sender,areareq,ses,channelInt)
        &&(M.location()!=null)
		&&(M.location().okMessage(ses.mob(),msg)))
		{
			M.executeMsg(M,msg);
			if(msg.trailerMsgs()!=null)
			{
				for(int i=0;i<msg.trailerMsgs().size();i++)
				{
					CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
					if((msg!=msg2)&&(M.location()!=null)&&(M.location().okMessage(M,msg2)))
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
		int channelInt=CMLib.channels().getChannelIndex(channelName);
		if(channelInt<0) return;
		
		message=CMProps.applyINIFilter(message,CMProps.SYSTEM_CHANNELFILTER);
		
        Vector flags=CMLib.channels().getChannelFlags(channelInt);
		channelName=CMLib.channels().getChannelName(channelInt);

		CMMsg msg=null;
		if(systemMsg)
		{
			String str="["+channelName+"] '"+message+"'^</CHANNEL^>^?^.";
			if((!mob.name().startsWith("^"))||(mob.name().length()>2))
				str=" "+str;
			msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q^<CHANNEL \""+channelName+"\"^>"+str,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^<CHANNEL \""+channelName+"\"^><S-NAME>"+str);
		}
		else
		if(message.startsWith(",")
		||(message.startsWith(":")
			&&(message.length()>1)
			&&(Character.isLetter(message.charAt(1))||message.charAt(1)==' ')))
		{
			String msgstr=message.substring(1);
			Vector V=CMParms.parse(msgstr);
			Social S=CMLib.socials().FetchSocial(V,true);
			if(S==null) S=CMLib.socials().FetchSocial(V,false);
			if(S!=null)
				msg=S.makeChannelMsg(mob,channelInt,channelName,V,false);
			else
			{
				msgstr=CMProps.applyINIFilter(msgstr,CMProps.SYSTEM_EMOTEFILTER);
                if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
                    msgstr=msgstr.trim();
                else
                    msgstr=" "+msgstr.trim();
				String srcstr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+mob.name()+msgstr+"^</CHANNEL^>^?^.";
				String reststr="^<CHANNEL \""+channelName+"\"^>["+channelName+"] <S-NAME>"+msgstr+"^</CHANNEL^>^?^.";
				msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q"+srcstr,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q"+reststr);
			}
		}
		else
			msg=CMClass.getMsg(mob,null,null,CMMsg.MASK_CHANNEL|CMMsg.MASK_ALWAYS|CMMsg.MSG_SPEAK,"^Q^<CHANNEL \""+channelName+"\"^>You "+channelName+" '"+message+"'^</CHANNEL^>^?^.",CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),"^Q^<CHANNEL \""+channelName+"\"^><S-NAME> "+channelName+"S '"+message+"'^</CHANNEL^>^?^.");
		if((mob.location()!=null)
		&&((!mob.location().isInhabitant(mob))||(mob.location().okMessage(mob,msg))))
		{
			boolean areareq=flags.contains("SAMEAREA");
			CMLib.channels().channelQueUp(channelInt,msg);
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				Session ses=CMLib.sessions().elementAt(s);
				channelTo(ses,areareq,channelInt,msg,mob);
			}
		}
		if((CMLib.intermud().i3online()&&(CMLib.intermud().isI3channel(channelName)))
		||(CMLib.intermud().imc2online()&&(CMLib.intermud().isIMC2channel(channelName))))
			CMLib.intermud().i3channel(mob,channelName,message);
	}
}
