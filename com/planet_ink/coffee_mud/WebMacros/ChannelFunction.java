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
   Copyright 2018-2020 Bo Zimmerman

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
public class ChannelFunction extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ChannelFunction";
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
		final String last=httpReq.getUrlParameter("CHANNEL");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
			if(mob!=null)
			{
				final int code=CMLib.channels().getChannelIndex(last);
				if(code>=0)
				{
					final ChannelsLibrary.CMChannel C=CMLib.channels().getChannel(code);
					if((C!=null)&&(CMSecurity.isJournalAccessAllowed(mob,C.name())))
					{
						if(parms.containsKey("DELETE"))
						{
							final String msgnum=httpReq.getUrlParameter("CHANNELMESSAGE");
							if((msgnum!=null)
							&&(msgnum.length()>0)
							&&(CMath.isNumber(msgnum)))
							{
								httpReq.getRequestObjects().clear();
								C.queue().clear();
								CMLib.database().delBackLogEntry(last, CMath.s_long(msgnum));
								return "Channel message "+msgnum+" deleted.";
							}
							else
								return "No message number?!";
						}
					}
				}
			}
		}
		return "";
	}
}
