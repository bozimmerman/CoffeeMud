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
   Copyright 2019-2020 Bo Zimmerman

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
public class INIEntry extends StdWebMacro
{
	@Override
	public String name()
	{
		return "INIEntry";
	}

	@Override
	public boolean isAdminMacro()
	{
		return false;
	}

	protected String getFinalValue(final Map<String,String> parms, String key)
	{
		key=key.toUpperCase().trim();
		final String iniVal;
		if(CMath.s_valueOf(CMProps.Str.class, key) != null)
			iniVal=CMProps.getVar((CMProps.Str)CMath.s_valueOf(CMProps.Str.class, key));
		else
		if(CMath.s_valueOf(CMProps.Int.class, key) != null)
			iniVal=""+CMProps.getIntVar((CMProps.Int)CMath.s_valueOf(CMProps.Int.class, key));
		else
		if(CMath.s_valueOf(CMProps.Bool.class, key) != null)
			iniVal=""+CMProps.getBoolVar((CMProps.Bool)CMath.s_valueOf(CMProps.Bool.class, key));
		else
			iniVal="";

		if(parms.containsKey("VALUE"))
			return clearWebMacros(iniVal);
		String retVal="";
		if(parms.containsKey("ISNULL"))
		{
			if((iniVal == null) || (iniVal.trim().length()==0))
				retVal="true";
			else
				return "false";
		}
		if(parms.containsKey("ISNOTNULL"))
		{
			if((iniVal != null) && (iniVal.trim().length()>0))
				retVal="true";
			else
				return "false";
		}
		if(parms.containsKey("CONTAINS"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("CONTAINS");
				if(iniVal.indexOf(val)>0)
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTCONTAINS"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("NOTCONTAINS");
				if(iniVal.indexOf(val)>0)
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		if(parms.containsKey("CONTAINSIGNORECASE"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("CONTAINSIGNORECASE");
				if(iniVal.toLowerCase().indexOf(val.toLowerCase())>0)
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTCONTAINSIGNORECASE"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("NOTCONTAINSIGNORECASE");
				if(iniVal.toLowerCase().indexOf(val.toLowerCase())>0)
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		if(parms.containsKey("EQUALS"))
		{
			if(iniVal != null)
			{
				final String val=parms.get("EQUALS");
				if(iniVal.equals(val))
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTEQUALS"))
		{
			if(iniVal != null)
			{
				final String val=parms.get("NOTEQUALS");
				if(iniVal.equals(val))
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		if(parms.containsKey("EQUALSIGNORECASE"))
		{
			if(iniVal != null)
			{
				final String val=parms.get("EQUALSIGNORECASE");
				if(iniVal.equalsIgnoreCase(val))
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTEQUALSIGNORECASE"))
		{
			if(iniVal != null)
			{
				final String val=parms.get("NOTEQUALSIGNORECASE");
				if(iniVal.equalsIgnoreCase(val))
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		if(parms.containsKey("GREATERTHAN"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("GREATERTHAN");
				if(CMath.s_double(iniVal)>CMath.s_double(val))
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTGREATERTHAN"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("NOTGREATERTHAN");
				if(CMath.s_double(iniVal)>CMath.s_double(val))
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		if(parms.containsKey("LESSTHAN"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("LESSTHAN");
				if(CMath.s_double(iniVal)<CMath.s_double(val))
					retVal="true";
				else
					return "false";
			}
			else
				return "false";
		}
		if(parms.containsKey("NOTLESSTHAN"))
		{
			if((iniVal != null)&&(iniVal.length()>0))
			{
				final String val=parms.get("NOTLESSTHAN");
				if(CMath.s_double(iniVal)<CMath.s_double(val))
					return "false";
				else
					retVal="true";
			}
			else
				retVal="true";
		}
		return retVal;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms==null)
			return "";
		if(!parms.containsKey("MASK"))
			return "'MASK' not found!";
		final String mask=parms.get("MASK").toUpperCase();
		if(mask.trim().endsWith("*"))
		{
			final List<String> allKeys = new LinkedList<String>();

			for(final Iterator<String> e=allKeys.iterator();e.hasNext();)
			{
				final String key=e.next().toUpperCase();
				if(key.startsWith(mask.substring(0,mask.length()-1)))
					return getFinalValue(parms, key);
			}
		}
		return getFinalValue(parms, mask);
	}
}
