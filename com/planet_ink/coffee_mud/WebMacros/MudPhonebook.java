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
   Copyright 2002-2025 Bo Zimmerman

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
public class MudPhonebook extends StdWebMacro
{
	@Override
	public String name()
	{
		return "MudPhonebook";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
		final List<Object> entries = new ArrayList<Object>();
		for(final MudHost host : CMLib.hosts())
		{
			try
			{
				final MiniJSON.JSONObject entry = new MiniJSON.JSONObject();
				final char threadCode = host.threadGroup().getName().charAt(0);
				final String name = CMProps.instance(threadCode).getStr(CMProps.Str.MUDNAME);
				final boolean account=CMProps.instance(threadCode).getInt(CMProps.Int.COMMONACCOUNTSYSTEM)>1;
				entry.put("name", name);
				entry.put("port", Long.valueOf(host.getPort()));
				entry.put("accounts", Boolean.valueOf(account));
				entries.add(entry);
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
		}
		obj.put("phonebook", entries.toArray());
		return obj.toString();
	}

}
