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
public class CharClassNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CharClassNext";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("CLASS");
		final String base=httpReq.getUrlParameter("BASECLASS");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("CLASS");
			return "";
		}
		final boolean includeSkillOnly=parms.containsKey("INCLUDESKILLONLY");
		final boolean includeAll=parms.containsKey("ALL");
		String lastID="";
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			if(((CMProps.isTheme(C.availabilityCode()))||includeAll)
			&&((!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK))||includeSkillOnly||includeAll||(CMSecurity.isCharClassEnabled(C.ID())))
			&&(!CMSecurity.isCharClassDisabled(C.ID()))
			&&((base==null)||(base.length()==0)||(C.baseClass().equalsIgnoreCase(base))))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("CLASS",C.ID());
					return "";
				}
				lastID=C.ID();
			}
		}
		httpReq.addFakeUrlParameter("CLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
