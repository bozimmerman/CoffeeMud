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
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class CheckAuthCode extends StdWebMacro
{
	@Override
	public String name()
	{
		return "CheckAuthCode";
	}

	public Hashtable<String,String> getAuths(HTTPRequest httpReq)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return null;
		final MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob==null)
			return null;
		Hashtable<String,String> auths=(Hashtable)httpReq.getRequestObjects().get("AUTHS_"+mob.Name().toUpperCase().trim());
		if(auths==null)
		{
			auths=new Hashtable<String,String>();
			boolean subOp=false;
			final boolean sysop=CMSecurity.isASysOp(mob);

			final String AREA=httpReq.getUrlParameter("AREA");
			Room R=null;
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if((AREA==null)||(AREA.length()==0)||(AREA.equals(A.Name())))
				{
					if(A.amISubOp(mob.Name()))
					{
						R=A.getRandomProperRoom();
						subOp=true;
						break;
					}
				}
			}
			auths.put("ANYMODAREAS",""+((subOp&&(CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.CMDAREAS)))
														   ||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDAREAS)));
			auths.put("ALLMODAREAS",""+(CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDROOMS)||CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.CMDAREAS)));
			final List<String> dirs=CMSecurity.getAccessibleDirs(mob,mob.location());
			auths.put("ANYFILEBROWSE",""+(dirs.size()>0));
			if(dirs.size()>0)
			{
				int maxLen=Integer.MAX_VALUE;
				int maxOne=-1;
				for(int v=0;v<dirs.size();v++)
				{
					if(dirs.get(v).length()<maxLen)
					{
						maxLen=dirs.get(v).length();
						maxOne=v;
					}
				}
				final String winner=dirs.get(maxOne);
				httpReq.addFakeUrlParameter("BESTFILEBROWSE",winner);
			}
			else
				httpReq.addFakeUrlParameter("BESTFILEBROWSE","");
			auths.put("SYSOP",""+sysop);
			auths.put("SUBOP",""+(sysop||subOp));

			for(final Iterator<CMSecurity.SecFlag> i = CMSecurity.getSecurityCodes(mob,R);i.hasNext();)
				auths.put("AUTH_"+i.next().toString(),"true");
			httpReq.getRequestObjects().put("AUTHS_"+mob.Name().toUpperCase().trim(),auths);
		}
		return auths;
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		boolean finalCondition=false;
		final Hashtable<String,String> auths=getAuths(httpReq);
		if(auths==null)
			return "false";
		final boolean sysop=auths.get("SYSOP").equalsIgnoreCase("true");
		for(String key : parms.keySet())
		{
			final String equals=parms.get(key);
			boolean not=false;
			boolean thisCondition=true;
			if(key.startsWith("||"))
				key=key.substring(2);
			if(key.startsWith("!"))
			{
				key=key.substring(1);
				not=true;
			}
			final String check=sysop?"true":(String)auths.get(key);
			if(not)
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=false;
				else
				if(check==null)
					thisCondition=true;
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
				if(!check.equalsIgnoreCase(equals))
					thisCondition=false;
				else
					thisCondition=true;
			}
			finalCondition=finalCondition||thisCondition;
		}
		if(finalCondition)
			return "true";
		return "false";
	}
}
