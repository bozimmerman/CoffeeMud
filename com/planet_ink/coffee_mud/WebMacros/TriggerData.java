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
import com.planet_ink.coffee_mud.Common.interfaces.Triggerer.TriggerCode;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.TriggerNext.TrigMGField;

import java.util.*;

/*
   Copyright 2022-2024 Bo Zimmerman

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
public class TriggerData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "TriggerData";
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
		final String last=httpReq.getUrlParameter("TRIGGER");
		if(last == null)
			return " @break@";
		final String field = httpReq.getUrlParameter("FIELD");
		if(field == null)
			return " @break@";
		final String triggerDefStr = httpReq.getUrlParameter(field);
		if(triggerDefStr == null)
			return " @break@";
		final List<String[]> trigDefs = TriggerNext.getTrigDefs(httpReq, triggerDefStr);
		if(parms.containsKey("FINAL"))
		{
			final StringBuilder str = new StringBuilder("");
			int x = 0;
			while(httpReq.isUrlParameter("TRIG"+x+"BUILT"))
			{
				final String built = httpReq.getUrlParameter("TRIG"+x+"BUILT");
				if(built.length()<3)
					continue;
				if(x>0)
					str.append(built);
				else
					str.append(built.substring(1));
				x++;
			}
			return htmlOutgoingFilter(str.toString());
		}
		if(parms.containsKey("CODES"))
		{
			final String match = httpReq.getUrlParameter("TRIG"+last+TrigMGField.TRIGTYPE.str);
			final StringBuilder str = new StringBuilder("");
			for(final Triggerer.TriggerCode code : Triggerer.TriggerCode.values())
			{
				str.append("<OPTION VALUE="+code.name()+" ");
				if(code.name().equalsIgnoreCase(match))
					str.append("SELECTED");
				str.append(">"+code.name());
			}
			return str.toString();
		}
		if(parms.containsKey("MATS"))
		{
			final int match = CMath.s_int((""+parms.get("VALUE")).trim());
			final StringBuilder str = new StringBuilder("");
			for(final RawMaterial.Material mat : RawMaterial.Material.values())
			{
				str.append("<OPTION VALUE="+mat.mask());
				if(match == mat.mask())
					str.append(" SELECTED");
				str.append(">"+RawMaterial.CODES.MAT_DESC(mat.mask()));
			}
			for(final int code : RawMaterial.CODES.ALL())
			{
				str.append("<OPTION VALUE="+code);
				if(match == code)
					str.append(" SELECTED");
				str.append(">"+RawMaterial.CODES.NAME(code));
			}
			return str.toString();
		}
		final int trigDex = CMath.s_int(last);
		if((trigDex<0)||(trigDex >= trigDefs.size()))
			return " @break@";
		return "";
	}
}
