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
   Copyright 2004-2020 Bo Zimmerman

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

	public String getHelpFor(final String tag, final String mask)
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

	protected String getFinalValue(final Map<String,String> parms, final CMProps page, final String key, final String mask)
	{
		if(parms.containsKey("VALUE"))
			return clearWebMacros(page.getStr(key));
		if(parms.containsKey("INIHELP"))
		{
			if(parms.containsKey("NOCR"))
				return clearWebMacros(CMStrings.replaceAll(getHelpFor(key,mask),"<BR>","&nbsp;"));
			else
				return clearWebMacros(getHelpFor(key,mask));
		}
		String retVal="";
		if(parms.containsKey("ISNULL"))
		{
			if((page.getStr(key) == null) || (page.getStr(key).trim().length()==0))
				retVal="true";
			else
				return "false";
		}
		if(parms.containsKey("ISNOTNULL"))
		{
			if((page.getStr(key) != null) && (page.getStr(key).trim().length()>0))
				retVal="true";
			else
				return "false";
		}
		if(parms.containsKey("CONTAINS"))
		{
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
			final String iniVal=page.getStr(key);
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
		String last=httpReq.getUrlParameter("INI");
		final boolean descZapperMask = parms.remove("DESCZAPPERMASK") != null;
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
					return getFinalValue(parms, page, key, mask);
				}
			}
		}
		httpReq.addFakeUrlParameter("INI",mask);
		return getFinalValue(parms, page, mask, mask);
	}
}
