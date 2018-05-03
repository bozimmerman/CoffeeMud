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
   Copyright 2006-2018 Bo Zimmerman

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
public class ExpertiseData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ExpertiseData";
	}

	// valid parms include help, ranges, quality, target, alignment, domain,
	// qualifyQ, auto
	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("EXPERTISE");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(last);
			if(E!=null)
			{
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuilder s=CMLib.help().getHelpText(E.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(E.name(),null,false);
					int limit=78;
					if(parms.containsKey("LIMIT"))
						limit=CMath.s_int(parms.get("LIMIT"));
					str.append(helpHelp(s,limit));
				}
				if(parms.containsKey("SHORTHELP"))
				{
					String s=CMLib.help().getHelpFile().getProperty(E.ID().toUpperCase());
					if(s==null)
						s=CMLib.help().getArcHelpFile().getProperty(E.ID().toUpperCase());
					if(s==null)
						s=CMLib.help().getHelpFile().getProperty(E.name().toUpperCase().replace(' ','_'));
					if(s==null)
						s=CMLib.help().getArcHelpFile().getProperty(E.name().toUpperCase().replace(' ','_'));
					if(s!=null)
					{
						if(s.toUpperCase().trim().startsWith("<EXPERTISE>"))
							s=s.trim().substring(11);
						str.append(helpHelp(new StringBuilder(s))+", ");
					}
					else
					{
						StringBuilder s2=CMLib.help().getHelpText(E.ID(),null,false);
						if(s2==null)
							s2=CMLib.help().getHelpText(E.name(),null,false);
						str.append(helpHelp(s2));
					}
				}
				if(parms.containsKey("NAME"))
					str.append(E.name()+", ");
				if(parms.containsKey("COST"))
					str.append(E.costDescription()+", ");
				if(parms.containsKey("REQUIRES"))
					str.append(CMLib.masking().maskDesc(E.allRequirements())+", ");
				if(parms.containsKey("ALLOWS"))
				{
					ExpertiseLibrary.ExpertiseDefinition def=null;
					Ability A=null;
					for(final Iterator<String> i=CMLib.ableMapper().getAbilityAllowsList(E.ID());i.hasNext();)
					{
						final String allowStr=i.next();
						def=CMLib.expertises().getDefinition(allowStr);
						if(def!=null)
							str.append(def.name()+", ");
						else
						{
							A=CMClass.getAbility(allowStr);
							if(A!=null)
								str.append(A.Name()+", ");
						}
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
