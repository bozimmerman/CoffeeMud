package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2005-2018 Bo Zimmerman

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
public class ChannelBackLogNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ChannelBackLogNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("CHANNELBACKLOG");
		String pageNumStr=httpReq.getUrlParameter("CHANNELBACKLOGPAGE");
		String pageSizeStr=httpReq.getUrlParameter("CHANNELBACKLOGPAGESIZE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("CHANNELBACKLOG");
			return "";
		}
		final String channel=httpReq.getUrlParameter("CHANNEL");
		if(channel==null)
			return " @break@";
		final int channelInt=CMLib.channels().getChannelIndex(channel);
		if(channelInt<0)
			return " @break@";
		httpReq.addFakeUrlParameter("CHANNELBACKLOGNEXTPAGE", "false");
		final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob!=null)
		{
			if(CMLib.channels().mayReadThisChannel(mob,channelInt,true))
			{
				int pageSize = CMath.s_int(pageSizeStr);
				if(pageSize <=0)
					pageSize = 100;
				int pageNum = CMath.s_int(pageNumStr); // page 0 is OK
				@SuppressWarnings("unchecked")
				List<ChannelsLibrary.ChannelMsg> que=(List<ChannelsLibrary.ChannelMsg>)httpReq.getRequestObjects().get("CHANNELMSG_"+channelInt+" QUE "+pageSize+" "+pageNum);
				if(que==null)
				{
					final List<ChannelsLibrary.ChannelMsg> oldQue=CMLib.channels().getChannelQue(channelInt,pageNum * pageSize, pageSize);
					que=new ArrayList<ChannelsLibrary.ChannelMsg>(oldQue.size());
					que.addAll(oldQue);
					httpReq.getRequestObjects().put("CHANNELMSG_"+channelInt+" QUE "+pageSize+" "+pageNum,que);
				}
				httpReq.addFakeUrlParameter("CHANNELBACKLOGNEXTPAGE", Boolean.toString(que.size()>=pageSize));

				final long now=System.currentTimeMillis();
				long elapsedTime;
				while(true)
				{
					final int num=CMath.s_int(last);
					final int doNum=que.size()-num-1;
					last=""+(num+1);
					httpReq.addFakeUrlParameter("CHANNELBACKLOG",last);
					if((doNum<0)||(doNum>=que.size()))
					{
						httpReq.addFakeUrlParameter("CHANNELBACKLOG","");
						if(parms.containsKey("EMPTYOK"))
							return "<!--EMPTY-->";
						return " @break@";
					}
					final boolean areareq=CMLib.channels().getChannel(channelInt).flags().contains(ChannelsLibrary.ChannelFlag.SAMEAREA);

					final ChannelsLibrary.ChannelMsg cmsg=que.get(doNum);
					final CMMsg msg=cmsg.msg();
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
					elapsedTime=now-cmsg.sentTimeMillis();
					elapsedTime=Math.round(elapsedTime/1000L)*1000L;
					if(elapsedTime<0)
					{
						Log.errOut("Channel","Wierd elapsed time: now="+now+", then="+cmsg.sentTimeMillis());
						elapsedTime=0;
					}
					str += " ("+CMLib.time().date2SmartEllapsedTime(elapsedTime,false)+" ago)";
					if(CMLib.channels().mayReadThisChannel(msg.source(),areareq,mob,channelInt,true))
						return clearWebMacros(CMLib.coffeeFilter().fullOutFilter(mob.session(),mob,msg.source(),msg.target(),msg.tool(),CMStrings.removeColors(str),false));
				}
			}
			return "";
		}
		return "";
	}
}
