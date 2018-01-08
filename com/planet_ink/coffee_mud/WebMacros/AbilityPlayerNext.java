package com.planet_ink.coffee_mud.WebMacros;
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
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class AbilityPlayerNext extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AbilityPlayerNext";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ABILITY");
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("ABILITY");
			return "";
		}
		final String ableType=httpReq.getUrlParameter("ABILITYTYPE");
		if((ableType!=null)&&(ableType.length()>0))
			parms.put(ableType,ableType);
		final String domainType=httpReq.getUrlParameter("DOMAIN");
		if((domainType!=null)&&(domainType.length()>0))
			parms.put("DOMAIN",domainType);

		String lastID="";
		final String playerName=httpReq.getUrlParameter("PLAYER");
		MOB M=null;
		if((playerName!=null)&&(playerName.length()>0))
			M=CMLib.players().getLoadPlayer(playerName);
		if(M==null)
		{
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}

		final Vector<Ability> abilities=new Vector<Ability>();
		HashSet<String> foundIDs=new HashSet<String>();
		for(final Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!foundIDs.contains(A.ID())))
			{
				foundIDs.add(A.ID());
				abilities.addElement(A);
			}
		}
		foundIDs.clear();
		foundIDs=null;
		for(int a=0;a<abilities.size();a++)
		{
			final Ability A=abilities.elementAt(a);
			boolean okToShow=true;
			final int classType=A.classificationCode()&Ability.ALL_ACODES;
			final String className=httpReq.getUrlParameter("CLASS");

			if((className!=null)&&(className.length()>0))
			{
				final int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					final String levelName=httpReq.getUrlParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			else
			{
				final int level=CMLib.ableMapper().getQualifyingLevel("Archon",true,A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					final String levelName=httpReq.getUrlParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			if(okToShow)
			{
				if(parms.containsKey("DOMAIN")&&(classType==Ability.ACODE_SPELL))
				{
					final String domain=parms.get("DOMAIN");
					if(!domain.equalsIgnoreCase(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5]))
						okToShow=false;
				}
				else
				{
					boolean containsOne=false;
					for (final String element : Ability.ACODE_DESCS)
					{
						if(parms.containsKey(element))
						{
							containsOne=true;
							break;
						}
					}
					if(containsOne&&(!parms.containsKey(Ability.ACODE_DESCS[classType])))
						okToShow=false;
				}
			}
			if(parms.containsKey("NOT"))
				okToShow=!okToShow;
			if(okToShow)
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("ABILITY",A.ID());
					return "";
				}
				lastID=A.ID();
			}
		}
		httpReq.addFakeUrlParameter("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
