package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
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
public class GrinderCommands
{
	public String name()
	{
		return "GrinderCommands";
	}

	public static String modifyCommand(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final Command oldA, final Modifiable C)
	{
		final String replaceCommand=httpReq.getUrlParameter("REPLACE");
		if((replaceCommand != null)
		&& (replaceCommand.length()>0)
		&& (replaceCommand.indexOf('=')>0))
		{
			final int eq=replaceCommand.indexOf('=');
			final String field=replaceCommand.substring(0,eq);
			final String value=replaceCommand.substring(eq+1);
			httpReq.addFakeUrlParameter(field, value);
			httpReq.addFakeUrlParameter("REPLACE","");
		}
		String newid=httpReq.getUrlParameter("NEWID");
		newid = CMStrings.replaceAll(newid, " ", "");
		String old;
		old=httpReq.getUrlParameter("HELP");
		if(old != null)
			C.setStat("HELP",old);
		old=httpReq.getUrlParameter("WORDLIST");
		if(old != null)
			C.setStat("ACTIONS",old.toUpperCase());
		old=httpReq.getUrlParameter("SCRIPT");
		if(old != null)
		{
			final List<String> lines = CMParms.parseAny(old.toUpperCase(), '\n', true);
			boolean found=false;
			for(final String l : lines)
			{
				final String s = l.trim();
				if(s.startsWith("FUNCTION_PROG")
				&&(s.substring(13).trim().startsWith("EXECUTE")))
					found=true;
			}
			if(!found)
				return "Illegal Script -- must contain FUNCTION_PROG EXECUTE";
			C.setStat("SCRIPT",old);
		}
		old=httpReq.getUrlParameter("SECMASK");
		if(old != null)
			C.setStat("SECMASK",old);
		old=httpReq.getUrlParameter("ACOST");
		if(old != null)
		{
			if(!CMath.isNumber(old))
				return "Illegal action cost.";
			C.setStat("ACOST",""+CMath.s_double(old));
		}
		old=httpReq.getUrlParameter("CCOST");
		if(old != null)
		{
			if(!CMath.isNumber(old))
				return "Illegal combat cost.";
			C.setStat("CCOST",""+CMath.s_double(old));
		}

		if((newid!=null)
		&&(newid.length()>0)
		&&(!newid.equalsIgnoreCase(C.ID()))
		&&(CMClass.getCommand(newid)==null)
		&&(C!=null))
			C.setStat("CLASS", newid);
		return "";
	}
}
