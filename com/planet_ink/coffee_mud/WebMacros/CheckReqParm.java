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
   Copyright 2002-2023 Bo Zimmerman

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
public class CheckReqParm extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CheckReqParm";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		Boolean finalCondition=null;
		for(String key : parms.keySet())
		{
			if(key.length()==0)
				continue;
			final String equals=parms.get(key);
			boolean not=false;
			boolean thisCondition=true;
			boolean orCondition=false;
			boolean startswith=false;
			boolean inside=false;
			boolean endswith=false;
			if(key.startsWith("||"))
			{
				orCondition=true;
				key=key.substring(2);
			}
			if(key.startsWith("|/")&&(key.indexOf("/|")>0))
				key=key.substring(key.indexOf("/|")+2);
			if(key.startsWith("<"))
			{
				startswith=true;
				key=key.substring(1);
			}
			if(key.startsWith(">"))
			{
				endswith=true;
				key=key.substring(1);
			}
			if(key.startsWith("*"))
			{
				inside=true;
				key=key.substring(1);
			}

			if(key.startsWith("!"))
			{
				key=key.substring(1);
				not=true;
			}
			final String check=httpReq.getUrlParameter(key);
			if(not)
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=false;
				else
				if(check==null)
					thisCondition=true;
				else
				if(startswith)
				{
					final boolean cc = CMath.isNumber(check);
					final boolean ec = CMath.isNumber(equals);
					if((cc && ec) || (cc && (equals.length()==0)) || (ec && (check.length()==0)))
						thisCondition = CMath.s_double(check) >= CMath.s_double(equals);
					else
						thisCondition=!check.startsWith(equals);
				}
				else
				if(endswith)
				{
					final boolean cc = CMath.isNumber(check);
					final boolean ec = CMath.isNumber(equals);
					if((cc && ec) || (cc && (equals.length()==0)) || (ec && (check.length()==0)))
						thisCondition = CMath.s_double(check) <= CMath.s_double(equals);
					else
						thisCondition=!check.endsWith(equals);
				}
				else
				if(inside)
					thisCondition=!(check.indexOf(equals)>=0);
				else
				if(!check.equalsIgnoreCase(equals))
					thisCondition=true;
				else
					thisCondition=false;
			}
			else
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=true;
				else
				if(check==null)
					thisCondition=false;
				else
				if(startswith)
				{
					final boolean cc = CMath.isNumber(check);
					final boolean ec = CMath.isNumber(equals);
					if((cc && ec) || (cc && (equals.length()==0)) || (ec && (check.length()==0)))
						thisCondition = CMath.s_double(check) < CMath.s_double(equals);
					else
						thisCondition=check.startsWith(equals);
				}
				else
				if(endswith)
				{
					final boolean cc = CMath.isNumber(check);
					final boolean ec = CMath.isNumber(equals);
					if((cc && ec) || (cc && (equals.length()==0)) || (ec && (check.length()==0)))
						thisCondition = CMath.s_double(check) > CMath.s_double(equals);
					else
						thisCondition=check.endsWith(equals);
				}
				else
				if(inside)
					thisCondition=(check.indexOf(equals)>=0);
				else
				if(!check.equalsIgnoreCase(equals))
					thisCondition=false;
				else
					thisCondition=true;
			}
			if(finalCondition==null)
				finalCondition=Boolean.valueOf(thisCondition);
			else
			if(orCondition)
				finalCondition=Boolean.valueOf(finalCondition.booleanValue()||thisCondition);
			else
				finalCondition=Boolean.valueOf(finalCondition.booleanValue()&&thisCondition);
		}
		if((finalCondition==null)
		||(finalCondition.booleanValue()))
			return "true";
		return "false";
	}
}
