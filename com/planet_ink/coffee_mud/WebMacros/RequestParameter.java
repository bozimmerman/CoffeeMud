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
import java.net.URLEncoder;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class RequestParameter extends StdWebMacro
{
	@Override
	public String name()
	{
		return "RequestParameter";
	}

	private static enum MODIFIER
	{
		UPPERCASE,
		LOWERCASE,
		LEFT,
		RIGHT,
		ELLIPSE,
		TRIM,
		AFTER,
		CAPITALCASE,
		PADLEFT,
		PADRIGHT,
	}

	private static HashSet<String> modifiers=new HashSet<String>();
	static
	{
		for(final MODIFIER M : MODIFIER.values())
			modifiers.add(M.name());
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		String str="";
		final java.util.Map<String,String> parms=parseParms(parm);
		for(final String key : parms.keySet())
		{
			if(!modifiers.contains(key))
			{
				if(httpReq.isUrlParameter(key))
					str+=httpReq.getUrlParameter(key);
			}
		}
		boolean capCase=false;
		for(final String key : parms.keySet())
		{
			if(modifiers.contains(key))
			{
				int num = 0;
				final MODIFIER m = (MODIFIER)CMath.s_valueOf(MODIFIER.class, key);
				if(m != null)
				{
					switch(m)
					{
					case UPPERCASE:
						str=str.toUpperCase();
						break;
					case LOWERCASE:
						str=str.toLowerCase();
						break;
					case LEFT:
						num = CMath.s_int(parms.get(MODIFIER.LEFT.name()));
						if((num >0)&& (num < str.length()))
							str=str.substring(0,num);
						break;
					case RIGHT:
						num = CMath.s_int(parms.get(MODIFIER.RIGHT.name()));
						if((num >0)&& (num < str.length()))
							str=str.substring(str.length()-num);
						break;
					case ELLIPSE:
						num = CMath.s_int(parms.get(MODIFIER.ELLIPSE.name()));
						if((num >0)&& (num < str.length()))
							str=str.substring(0,num)+"...";
						break;
					case TRIM:
						str=str.trim();
						break;
					case AFTER:
						num = CMath.s_int(parms.get(MODIFIER.AFTER.name()));
						if((num >0)&& (num < str.length()))
							str=str.substring(num);
						break;
					case CAPITALCASE:
						capCase=true;
						break;
					case PADLEFT:
						num = CMath.s_int(parms.get(MODIFIER.PADLEFT.name()));
						if(num >0)
							str=CMStrings.padLeft(str, num);
						break;
					case PADRIGHT:
						num = CMath.s_int(parms.get(MODIFIER.PADRIGHT.name()));
						if(num >0)
							str=CMStrings.padLeft(str, num);
						break;
					}
				}
			}
		}
		if(capCase)
			str=CMStrings.capitalizeAndLower(str);
		str=clearWebMacros(str);
		return str;
	}
}
