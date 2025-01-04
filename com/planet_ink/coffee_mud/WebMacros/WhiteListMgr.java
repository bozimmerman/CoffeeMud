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
   Copyright 2022-2025 Bo Zimmerman

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
public class WhiteListMgr extends StdWebMacro
{
	@Override
	public String name()
	{
		return "WhiteListMgr";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}
	
	
	protected String readINIFile(final HTTPRequest httpReq, final String key) {
		final List<String> page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.Str.INIPATH));
		final String upperKey = key.toUpperCase().trim();
		for(int p=0;p<page.size();p++)
		{
			final String rawS=page.get(p);
			final String trimS = rawS.trim();
			if(trimS.startsWith("#"))
				continue;
			final String utrimS = trimS.toUpperCase();
			if(utrimS.startsWith(upperKey) && (trimS.substring(key.length()).trim().startsWith("=")))
				return trimS.substring(trimS.indexOf('=')+1).trim();
		}
		return "";
	}
	
	protected synchronized String[] readINIList(final HTTPRequest httpReq, final String key) {
		if(httpReq.getRequestObjects().containsKey(ID()+"_"+key))
			return (String[])httpReq.getRequestObjects().get(ID()+"_"+key);
		final String[] list = CMParms.parseCommas(readINIFile(httpReq,key), true).toArray(new String[0]);
		httpReq.getRequestObjects().put(ID()+"_"+key, list);
		return list;
	}

	protected String updateINIFile(final String key, final String newVal) {
		final StringBuffer buf=new StringBuffer("");
		final List<String> page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.Str.INIPATH));
		final String upperKey = key.toUpperCase().trim();
		for(int p=0;p<page.size();p++)
		{
			final String rawS=page.get(p);
			final String trimS = rawS.trim().toUpperCase();
			final String newS;
			if(trimS.startsWith(upperKey) && (trimS.substring(key.length()).trim().startsWith("=")))
				newS=key+"="+newVal;
			else
				newS=rawS;
			buf.append(newS+"\r\n");
		}
		new CMFile("//"+CMProps.getVar(CMProps.Str.INIPATH),null,CMFile.FLAG_FORCEALLOW).saveText(buf);
		return "";
	}

	static final String[] WTYPES = new String[] {
		"IPSCONN","LOGINS","IPSNEWPLAYERS"
	};
	
	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if((M==null)||(!CMSecurity.isAllowedAnywhere(M, CMSecurity.SecFlag.BAN)))
			return " @break@";

		final java.util.Map<String,String> parms=parseParms(parm);
		String wType = null;
		for(final String w : WTYPES)
			if(parms.containsKey(w))
				wType = w;
		
		String lastKey = "WHITELIST_"+wType;
		String last=httpReq.getUrlParameter(lastKey);
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter(lastKey);
			return "";
		}
		else
		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			final List<String> lst=Arrays.asList(readINIList(httpReq,"WHITELIST"+wType));
			for(int i=0;i<lst.size();i++)
			{
				final String key=lst.get(i);
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!key.equals(lastID))))
				{
					httpReq.addFakeUrlParameter(lastKey,key);
					return "";
				}
				lastID=key;
			}
			httpReq.addFakeUrlParameter(lastKey,"");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		else
		if(parms.containsKey("DELETE")
		&&("DELETE".equals(httpReq.getUrlParameter("FUNC"))))
		{
			wType = httpReq.getUrlParameter("FTYP");
			if(wType == null)
				return "";
			lastKey = "WHITELIST_"+wType;
			last=httpReq.getUrlParameter(lastKey);
			final List<String> lst=new XVector<String>(readINIList(httpReq,"WHITELIST"+wType));
			if(!lst.contains(last))
				return "'"+last+"' was not whitelisted.";
			lst.remove(last);
			CMProps.WhiteList wlTyp = (CMProps.WhiteList)CMath.s_valueOf(CMProps.WhiteList.class, wType);
			if(wlTyp == null)
				return " @break@";
			CMProps.setWhitelist(wlTyp,CMParms.toListString(lst));
			updateINIFile("WHITELIST"+wType,CMParms.toListString(lst));
			httpReq.getRequestObjects().remove(ID()+"_WHITELIST"+wType);
			return "'"+last+"' is no longer whitelisted.";
		}
		else
		if(parms.containsKey("ADD")
		&&("ADD".equals(httpReq.getUrlParameter("FUNC"))))
		{
			wType = httpReq.getUrlParameter("FTYP");
			if(wType == null)
				return "";
			final String entry=httpReq.getUrlParameter("NEWWHITELIST_"+wType);
			if(entry==null)
				return "";
			final List<String> lst=new XVector<String>(readINIList(httpReq,"WHITELIST"+wType));
			if(lst.contains(entry))
				return "'"+entry+"' was already whitelisted.";
			CMProps.WhiteList wlTyp = (CMProps.WhiteList)CMath.s_valueOf(CMProps.WhiteList.class, wType);
			if(wlTyp == null)
				return " @break@";
			lst.add(entry);
			CMProps.setWhitelist(wlTyp,CMParms.toListString(lst));
			updateINIFile("WHITELIST"+wType,CMParms.toListString(lst));
			httpReq.getRequestObjects().remove(ID()+"_WHITELIST"+wType);
			return "'"+entry+"' is now whitelisted.";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}

}
