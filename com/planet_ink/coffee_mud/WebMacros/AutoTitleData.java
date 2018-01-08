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
   Copyright 2007-2018 Bo Zimmerman

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
public class AutoTitleData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AutoTitleData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("AUTOTITLE");
		if((last==null)&&(!parms.containsKey("EDIT")))
			return " @break@";

		if(parms.containsKey("EDIT"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.TITLES))
				return "[authentication error]";
			final String req=httpReq.getUrlParameter("ISREQUIRED");
			String newTitle=httpReq.getUrlParameter("TITLE");
			if((req!=null)&&(req.equalsIgnoreCase("on")))
				newTitle="{"+newTitle+"}";
			final String newMask=httpReq.getUrlParameter("MASK");
			if((newTitle==null)||(newMask==null)||(newTitle.length()==0))
				return "[missing data error]";

			if((last!=null)&&((last.length()==0)&&(CMLib.titles().isExistingAutoTitle(newTitle))))
			{
				CMLib.titles().reloadAutoTitles();
				return "[new title already exists!]";
			}

			final String error=CMLib.titles().evaluateAutoTitle(newTitle+"="+newMask,false);
			if(error!=null)
				return "[error: "+error+"]";

			if((last!=null)&&(CMLib.titles().isExistingAutoTitle(last)))
			{
				final String err=CMLib.titles().deleteTitleAndResave(last);
				if(err!=null)
				{
					CMLib.titles().reloadAutoTitles();
					return err;
				}
			}
			CMLib.titles().appendAutoTitle("\n"+newTitle+"="+newMask);
		}
		else
		if(parms.containsKey("DELETE"))
		{
			final MOB M = Authenticate.getAuthenticatedMob(httpReq);
			if(M==null)
				return "[authentication error]";
			if(!CMSecurity.isAllowed(M,M.location(),CMSecurity.SecFlag.TITLES))
				return "[authentication error]";
			if(last==null)
				return " @break@";
			if(!CMLib.titles().isExistingAutoTitle(last))
				return "Unknown title!";
			final String err=CMLib.titles().deleteTitleAndResave(last);
			if(err==null)
				return "Auto-Title deleted.";
			return err;
		}
		else
		if(last==null)
			return " @break@";
		final StringBuffer str=new StringBuffer("");

		if(parms.containsKey("MASK"))
		{
			String mask=httpReq.getUrlParameter("MASK");
			if((mask==null)&&(last!=null)&&(last.length()>0))
				mask=CMLib.titles().getAutoTitleMask(last);
			if(mask!=null)
				str.append(CMStrings.replaceAll(mask,"\"","&quot;")+", ");
		}
		if(parms.containsKey("TITLE"))
		{
			String title=httpReq.getUrlParameter("TITLE");
			if(title==null)
				title=last;
			if(title!=null)
			{
				if(title.startsWith("{")&&title.endsWith("}"))
					title=title.substring(1,title.length()-1);
				str.append(title+", ");
			}
		}
		if(parms.containsKey("ISREQUIRED"))
		{
			String req=httpReq.getUrlParameter("ISREQUIRED");
			if((req==null)&&(last!=null))
				req=(last.startsWith("{")&&last.endsWith("}"))?"on":"";
			if(req!=null)
				str.append((req.equalsIgnoreCase("on")?"CHECKED":"")+", ");
		}
		if(parms.containsKey("MASKDESC"))
		{
			String mask=httpReq.getUrlParameter("MASK");
			if((mask==null)&&(last!=null)&&(last.length()>0))
				mask=CMLib.titles().getAutoTitleMask(last);
			if(mask!=null)
				str.append(CMLib.masking().maskDesc(mask)+", ");
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}
}
