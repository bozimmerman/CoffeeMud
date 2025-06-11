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
   Copyright 2025-2025 Bo Zimmerman

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
public class TellMessageNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "TellMessageNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return " @break@";
		final java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("TELLMESSAGE");
		final String whom=httpReq.getUrlParameter("TELLWHOM");
		final String pageNumStr=httpReq.getUrlParameter("TELLMESSAGEPAGE");
		final String pageSizeStr=httpReq.getUrlParameter("TELLMESSAGEPAGESIZE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("TELLMESSAGE");
			return "";
		}
		httpReq.addFakeUrlParameter("TELLMESSAGENEXTPAGE", "false");
		final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob!=null)
		{
			int pageSize = CMath.s_int(pageSizeStr);
			if(pageSize <=0)
				pageSize = 25;
			final int pageNum = CMath.s_int(pageNumStr); // page 0 is OK
			final List<PlayerStats.TellMsg> que = buildTellPage(httpReq, M.playerStats(), whom, pageNum, pageSize);
			httpReq.addFakeUrlParameter("TELLMESSAGENEXTPAGE", Boolean.toString(que.size()>=(pageNum*pageSize)));

			final long now=System.currentTimeMillis();
			long elapsedTime;
			while(true)
			{
				final int num=CMath.s_int(last);
				last=""+(num+1);
				httpReq.addFakeUrlParameter("TELLMESSAGE",last);
				if((num<0)||(num>=que.size()))
				{
					httpReq.addFakeUrlParameter("TELLMESSAGE","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					return " @break@";
				}
				final PlayerStats.TellMsg cmsg=que.get(num);
				String str=cmsg.message();
				httpReq.addFakeUrlParameter("TELLFROM",cmsg.from());
				httpReq.addFakeUrlParameter("TELLTO",cmsg.to());
				str=CMStrings.removeColors(str);
				elapsedTime=now-cmsg.time();
				elapsedTime=Math.round(elapsedTime/1000L)*1000L;
				if(elapsedTime<0)
				{
					Log.errOut("Weird elapsed time: now="+now+", then="+cmsg.time());
					elapsedTime=0;
				}
				str += " ("+CMLib.time().date2SmartEllapsedTime(elapsedTime,false)+" ago)";
				return clearWebMacros(str);
			}
		}
		return "";
	}

	protected static final List<PlayerStats.TellMsg> buildTellPage(
			final HTTPRequest httpReq, final PlayerStats pStats, final String whom, final int pageNum, final int pageSize)
	{
		final String cacheKey = "TELLMSG"+whom+" QUE "+pageSize+" "+pageNum;
		@SuppressWarnings("unchecked")
		List<PlayerStats.TellMsg> que=(List<PlayerStats.TellMsg>)httpReq.getRequestObjects().get(cacheKey);
		if(que==null)
		{
			que = new XArrayList<PlayerStats.TellMsg>();
			final List<PlayerStats.TellMsg> baseQue;
			if(whom != null)
			{
				baseQue = new XArrayList<PlayerStats.TellMsg>(pStats.getTellStack());
				for(int i=baseQue.size()-1;i>=0;i--)
				{
					final PlayerStats.TellMsg msg = baseQue.get(i);
					if(((msg.from()==null)||(!msg.from().equalsIgnoreCase(whom)))
					&&((msg.to()==null)||(!msg.to().equalsIgnoreCase(whom))))
						baseQue.remove(i);
				}
			}
			else
				baseQue=pStats.getTellStack();
			int endAt = (pageNum+1)*pageSize;
			if(endAt > baseQue.size())
				endAt = baseQue.size();
			for(int i=(pageNum * pageSize);i<endAt;i++)
				que.add(baseQue.get(i));
			httpReq.getRequestObjects().put(cacheKey,que);
		}
		return que;
	}
}
