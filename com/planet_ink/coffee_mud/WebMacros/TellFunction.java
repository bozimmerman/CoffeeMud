package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
public class TellFunction extends StdWebMacro
{
	@Override
	public String name()
	{
		return "TellFunction";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob!=null)
		{
			final String whom=httpReq.getUrlParameter("TELLWHOM");
			final String pageNumStr=httpReq.getUrlParameter("TELLMESSAGEPAGE");
			final String pageSizeStr=httpReq.getUrlParameter("TELLMESSAGEPAGESIZE");
			final String msgnum=httpReq.getUrlParameter("TELLMESSAGE");
			int pageSize = CMath.s_int(pageSizeStr);
			if(pageSize <=0)
				pageSize = 25;
			final int pageNum = CMath.s_int(pageNumStr); // page 0 is OK
			final int num = CMath.isNumber(msgnum)?CMath.s_int(msgnum):-1;
			if(parms.containsKey("DELETE"))
			{
				if(msgnum == null)
					return " @break@";
				final List<PlayerStats.TellMsg> que = TellMessageNext.buildTellPage(httpReq, mob.playerStats(), whom, pageNum, pageSize);
				if((num>=0)&&(num<que.size()))
				{
					httpReq.getRequestObjects().clear();
					if(mob.playerStats()!=null)
						mob.playerStats().getTellStack().remove(que.get(num));
					return "Tell message "+num+" deleted.";
				}
				else
					return "No message number?!";
			}
			else
			if(parms.containsKey("POST"))
			{
				if((msgnum == null)||(whom==null))
					return " @break@";
				final Command tellC = CMClass.getCommand("Tell");
				Boolean success;
				try
				{
					success = (Boolean)tellC.executeInternal(mob, 0, whom, msgnum);
				}
				catch (final IOException e)
				{
					success = Boolean.FALSE;
				}
				if(!success.booleanValue())
					return "No message sent.";
			}
			else
				return " @break@";
		}
		return "";
	}
}
