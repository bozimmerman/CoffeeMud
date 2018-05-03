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
   Copyright 2013-2018 Bo Zimmerman

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
public class ChannelInfo extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ChannelInfo";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("CHANNEL");
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("ALLFLAGS"))
		{
			for(final ChannelsLibrary.ChannelFlag flag : ChannelsLibrary.ChannelFlag.values())
				str.append("FLAG_"+flag.name()).append(", ");
		}
		else
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final int code=CMLib.channels().getChannelIndex(last);
			if(code>=0)
			{
				final ChannelsLibrary.CMChannel C=CMLib.channels().getChannel(code);
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText("CHANNEL_"+last,null,false);
					if(s==null)
						s=CMLib.help().getHelpText(last,null,false);
					int limit=78;
					if(parms.containsKey("LIMIT"))
						limit=CMath.s_int(parms.get("LIMIT"));
					str.append(helpHelp(s,limit)).append(", ");
				}
				if(parms.containsKey("ID"))
					str.append(code).append(", ");
				if(parms.containsKey("NAME"))
					str.append(C.name()).append(", ");
				if(parms.containsKey("COLOROVERRIDE"))
					str.append(C.colorOverrideWords()).append(", ");
				if(parms.containsKey("I3NAME"))
					str.append(C.i3name()).append(", ");
				if(parms.containsKey("IMC2NAME"))
					str.append(C.imc2Name()).append(", ");
				if(parms.containsKey("MASK"))
					str.append(C.mask()).append(", ");
				if(parms.containsKey("FLAGSET"))
				{
					for(final ChannelsLibrary.ChannelFlag flag : ChannelsLibrary.ChannelFlag.values())
					{
						httpReq.addFakeUrlParameter("FLAG_"+flag.name(), 
								C.flags().contains(flag)?(parms.containsKey("SELECTED")?"selected":parms.containsKey("CHECKED")?"checked":"on"):"");
					}
				}
				for(final ChannelsLibrary.ChannelFlag flag : ChannelsLibrary.ChannelFlag.values())
				{
					if(parms.containsKey("FLAG_"+flag.name().toUpperCase().trim()))
						str.append(C.flags().contains(flag)?(parms.containsKey("SELECTED")?"selected":parms.containsKey("CHECKED")?"checked":"on"):"").append(", ");
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
