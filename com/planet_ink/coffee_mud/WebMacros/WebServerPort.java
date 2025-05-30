package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWConfig;
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
public class WebServerPort extends StdWebMacro
{
	@Override
	public String name()
	{
		return "WebServerPort";
	}

	protected String getServerWebPort(final Map<String,String> parms)
	{
		final String serverType = parms.containsKey("ADMIN") ? "ADMIN" : "PUB";
		for(final MudHost host : CMLib.hosts())
		{
			try
			{
				final String var = host.executeCommand("WEBSERVER "+serverType+" PORT");
				if(var.length()>0)
					return var;
			}
			catch (final Exception e)
			{
			}
		}
		return null;
	}
	
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms.containsKey("CURRENT"))
			return Integer.toString(httpReq.getClientPort());
		if(parms.containsKey("SERVER"))
		{
			final String val = getServerWebPort(parms);
			if(val != null)
				return val;
		}
		if(Thread.currentThread() instanceof CWThread)
		{
			final CWConfig config=((CWThread)Thread.currentThread()).getConfig();
			return CMParms.toListString(config.getHttpListenPorts());
		}
		if(httpReq.getClientPort()==0)
		{
			final String val = getServerWebPort(parms);
			if(val != null)
				return val;
		}
		return Integer.toString(httpReq.getClientPort());
	}

}
