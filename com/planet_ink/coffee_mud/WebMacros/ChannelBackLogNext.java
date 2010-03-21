package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;




/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class ChannelBackLogNext extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("CHANNELBACKLOG");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("CHANNELBACKLOG");
			return "";
		}
		String channel=httpReq.getRequestParameter("CHANNEL");
		if(channel==null) return " @break@";
		int channelInt=CMLib.channels().getChannelIndex(channel);
		if(channelInt<0) return " @break@";
		MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob!=null)
		{
			if(CMLib.channels().mayReadThisChannel(mob,channelInt,true))
			{
			    Vector que=CMLib.channels().getChannelQue(channelInt);
				while(true)
				{
				    int num=CMath.s_int(last);
				    last=""+(num+1);
					httpReq.addRequestParameters("CHANNELBACKLOG",last);
					if((num<0)||(num>=que.size()))
					{
						httpReq.addRequestParameters("CHANNELBACKLOG","");
						if(parms.containsKey("EMPTYOK"))
							return "<!--EMPTY-->";
						return " @break@";
					}
					boolean areareq=CMLib.channels().getChannelFlags(channelInt).contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
					CMMsg msg=(CMMsg)que.elementAt(num);
					String str=null;
					if((mob==msg.source())&&(msg.sourceMessage()!=null))
					    str=msg.sourceMessage();
					else
					if((mob==msg.target())&&(msg.targetMessage()!=null))
					    str=msg.targetMessage();
					else
					if(msg.othersMessage()!=null)
					    str=msg.othersMessage();
					else
					    str="";
					str=CMStrings.removeColors(str);
					if(CMLib.channels().mayReadThisChannel(msg.source(),areareq,mob,channelInt,true))
						return clearWebMacros(CMLib.coffeeFilter().fullOutFilter(mob.session(),mob,msg.source(),msg.target(),msg.tool(),CMStrings.removeColors(str),false));
				}
			}
			return "";
		}
		return "";
	}
}
