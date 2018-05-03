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
   Copyright 2002-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ClassRaceNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ClassRaceNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String cclass=httpReq.getUrlParameter("CLASS");
		if(cclass.trim().length()==0)
			return " @break@";
		final CharClass C=CMClass.getCharClass(cclass.trim());
		if(C==null)
			return " @break";
		final String last=httpReq.getUrlParameter("RACE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("RACE");
			return "";
		}
		String lastID="";
		for(final Enumeration r=MobData.sortedRaces(httpReq);r.hasMoreElements();)
		{
			final Race R=(Race)r.nextElement();
			if(((CMLib.login().isAvailableRace(R))
				||(parms.containsKey("ALL")))
			&&(C.isAllowedRace(R)))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!R.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("RACE",R.ID());
					return "";
				}
				lastID=R.ID();
			}
		}
		httpReq.addFakeUrlParameter("RACE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
