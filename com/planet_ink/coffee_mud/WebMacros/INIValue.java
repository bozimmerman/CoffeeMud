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

public class INIValue extends StdWebMacro
{
	@Override
	public String name()
	{
		return "INIValue";
	}

	@Override
	public boolean isAdminMacro()
	{
		return false;
	}

	public String getHelpFor(String tag, String mask)
	{
		final Vector<String> help=new Vector<String>();
		final List<String> page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.Str.INIPATH));
		boolean startOver=false;
		for(int p=0;p<page.size();p++)
		{
			final String s=page.get(p).trim();
			if(s.trim().length()==0)
				startOver=true;
			else
			if(s.startsWith("#")||s.startsWith("!"))
			{
				if(startOver)
					help.clear();
				startOver=false;
				help.addElement(s.substring(1).trim());
			}
			else
			{
				final int x=s.indexOf('=');
				if((x>=0)
				&&(help.size()>0)
				&&((s.substring(0,x).equals(mask)
					||(mask.endsWith("*")&&(s.substring(0,x).startsWith(mask.substring(0,mask.length()-1)))))))
				{
					final StringBuffer str=new StringBuffer("");
					for(int i=0;i<help.size();i++)
						str.append(help.elementAt(i)+"<BR>");
					return str.toString();
				}
				help.clear();
				startOver=false;
			}
		}
		return "";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms==null)
			return "";
		String last=httpReq.getUrlParameter("INI");
		boolean descZapperMask = parms.remove("DESCZAPPERMASK") != null;
		if(last == null)
			last = parms.remove("INI");
		if((parms.size()==0)&&(last!=null)&&(last.length()>0))
		{
			final CMProps page=CMProps.loadPropPage(CMProps.getVar(CMProps.Str.INIPATH));
			if((page==null)||(!page.isLoaded()))
				return "";
			if(!descZapperMask)
				return page.getStr(last);
			return CMLib.masking().maskDesc(page.getStr(last));
		}
		if(parms.containsKey("RESET"))
		{
			if(last!=null)
				httpReq.removeUrlParameter("INI");
			return "";
		}
		if(parms.containsKey("NEXT"))
		{
			if(!parms.containsKey("MASK"))
				return " @break@";
			final String mask=parms.get("MASK").toUpperCase().trim();
			String lastID="";
			final List<String> page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.Str.INIPATH));
			for(int p=0;p<page.size();p++)
			{
				final String s=page.get(p).trim();
				if(s.startsWith("#")||s.startsWith("!"))
					continue;
				int x=s.indexOf('=');
				if(x<0)
					x=s.indexOf(':');
				if(x<0)
					continue;
				final String id=s.substring(0,x).trim().toUpperCase();
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!id.equals(lastID))))
				{
					if(mask.endsWith("*"))
					{
						if(!id.startsWith(mask.substring(0,mask.length()-1)))
							continue;
					}
					else
					if(!mask.equalsIgnoreCase(id))
						continue;
					httpReq.addFakeUrlParameter("INI",id);
					if(parms.containsKey("VALUE"))
					{
						final CMProps realPage=CMProps.loadPropPage(CMProps.getVar(CMProps.Str.INIPATH));
						if(realPage!=null)
							return realPage.getStr(id);
					}
					return "";
				}
				lastID=id;
			}
			httpReq.addFakeUrlParameter("INI","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		if(!parms.containsKey("MASK"))
			return "'MASK' not found!";
		final String mask=parms.get("MASK").toUpperCase();
		final CMProps page=CMProps.loadPropPage(CMProps.getVar(CMProps.Str.INIPATH));
		if((page==null)||(!page.isLoaded()))
			return "";
		if(mask.trim().endsWith("*"))
		{
			for(final Enumeration<Object> e=page.keys();e.hasMoreElements();)
			{
				final String key=((String)e.nextElement()).toUpperCase();
				if(key.startsWith(mask.substring(0,mask.length()-1)))
				{
					httpReq.addFakeUrlParameter("INI",key);
					if(parms.containsKey("VALUE"))
						return clearWebMacros(page.getStr(key));
					else
					if(parms.containsKey("INIHELP"))
						return clearWebMacros(getHelpFor(key,mask));
					return "";
				}
			}
		}
		httpReq.addFakeUrlParameter("INI",mask);
		if(parms.containsKey("VALUE"))
			return clearWebMacros(page.getStr(mask));
		else
		if(parms.containsKey("INIHELP"))
		{
			if(parms.containsKey("NOCR"))
				return clearWebMacros(CMStrings.replaceAll(getHelpFor(mask,mask),"<BR>","&nbsp;"));
			else
				return clearWebMacros(getHelpFor(mask,mask));
		}
		return "";
	}
}
