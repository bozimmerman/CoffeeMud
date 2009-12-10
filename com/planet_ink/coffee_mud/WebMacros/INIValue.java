package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class INIValue extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String getHelpFor(String tag, String mask)
	{
		Vector help=new Vector();
		Vector page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
		boolean startOver=false;
		for(int p=0;p<page.size();p++)
		{
			String s=((String)page.elementAt(p)).trim();
			if(s.trim().length()==0)
				startOver=true;
			else
			if(s.startsWith("#")||s.startsWith("!"))
			{
				if(startOver) help.clear();
				startOver=false;
				help.addElement(s.substring(1).trim());
			}
			else
			{
				int x=s.indexOf("=");
				if((x>=0)
				&&(help.size()>0)
				&&((s.substring(0,x).equals(mask)
					||(mask.endsWith("*")&&(s.substring(0,x).startsWith(mask.substring(0,mask.length()-1)))))))
				{
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<help.size();i++)
						str.append(((String)help.elementAt(i))+"<BR>");
					return str.toString();
				}
				help.clear();
				startOver=false;
			}
		}
		return "";
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms==null) return "";
		String last=httpReq.getRequestParameter("INI");
		if((parms.size()==0)&&(last!=null)&&(last.length()>0))
		{
            CMProps page=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
			if((page==null)||(!page.loaded)) return "";
			return page.getStr(last);
		}
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("INI");
			return "";
		}
		if(parms.containsKey("NEXT"))
		{
			if(!parms.containsKey("MASK")) 
				return " @break@";
			String mask=((String)parms.get("MASK")).toUpperCase().trim();
			String lastID="";
			Vector page=CMProps.loadEnumerablePage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
			for(int p=0;p<page.size();p++)
			{
				String s=((String)page.elementAt(p)).trim();
				if(s.startsWith("#")||s.startsWith("!")) 
					continue;
				int x=s.indexOf("=");
				if(x<0) x=s.indexOf(":");
				if(x<0) continue;
				String id=s.substring(0,x).trim().toUpperCase();
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
					httpReq.addRequestParameters("INI",id);
					if(parms.containsKey("VALUE"))
					{
                        CMProps realPage=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
						if(realPage!=null) return realPage.getStr(id);
					}
					return "";
				}
				lastID=id;
			}
			httpReq.addRequestParameters("INI","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		if(!parms.containsKey("MASK")) 
			return "'MASK' not found!";
		String mask=((String)parms.get("MASK")).toUpperCase();
        CMProps page=CMProps.loadPropPage(CMProps.getVar(CMProps.SYSTEM_INIPATH));
		if((page==null)||(!page.loaded)) return "";
		if(mask.trim().endsWith("*"))
			for(Enumeration e=page.keys();e.hasMoreElements();)
			{
				String key=((String)e.nextElement()).toUpperCase();
				if(key.startsWith(mask.substring(0,mask.length()-1)))
				{
					httpReq.addRequestParameters("INI",key);
					if(parms.containsKey("VALUE"))
                        return clearWebMacros(page.getStr(key));
					else
					if(parms.containsKey("INIHELP"))
                        return clearWebMacros(getHelpFor(key,mask));
					return "";
				}
			}
		httpReq.addRequestParameters("INI",mask);
		if(parms.containsKey("VALUE"))
            return clearWebMacros(page.getStr(mask));
		else
		if(parms.containsKey("INIHELP"))
            return clearWebMacros(getHelpFor(mask,mask));
		return "";
	}
}
