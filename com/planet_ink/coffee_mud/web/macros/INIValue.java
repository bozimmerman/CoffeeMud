package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class INIValue extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String getHelpFor(String tag, String mask)
	{
		Vector help=new Vector();
		if(mask.endsWith("*")) mask=mask.substring(0,mask.length()-1);
		Vector page=INI.loadEnumerablePage(CommonStrings.getVar(CommonStrings.SYSTEM_INIPATH));
		boolean found=false;
		boolean clearNext=false;
		for(int p=0;p<page.size();p++)
		{
			String s=((String)page.elementAt(p)).trim();
			if(s.startsWith("#")||s.startsWith("!")) 
			{
				if(clearNext) help.clear();
				clearNext=false;
				int y=s.indexOf(mask);
				if(y==0) 
					found=true;
				else
				if((y>0)&&(!Character.isLetter(s.charAt(y-1))))
				   found=true;
				help.addElement(s.substring(1).trim());
				continue;
			}
			int x=s.indexOf("=");
			if(x<0) x=s.indexOf(":");
			else
			{
				int y=s.indexOf(mask);
				if(y==0) 
					found=true;
				else
				if((y>0)&&(!Character.isLetter(s.charAt(y-1))))
				   found=true;
			}
			if((found)&&(help.size()>0))
			{
				StringBuffer str=new StringBuffer("");
				for(int i=0;i<help.size();i++)
					str.append(((String)help.elementAt(i))+"<BR>");
				return str.toString();
			}
			clearNext=true;
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
			INI page=INI.loadPropPage(CommonStrings.getVar(CommonStrings.SYSTEM_INIPATH));
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
			Vector page=INI.loadEnumerablePage(CommonStrings.getVar(CommonStrings.SYSTEM_INIPATH));
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
						INI realPage=INI.loadPropPage(CommonStrings.getVar(CommonStrings.SYSTEM_INIPATH));
						if(realPage!=null) return realPage.getStr(id);
					}
					return "";
				}
				lastID=id;
			}
			httpReq.addRequestParameters("INI","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}
		else
		{
			if(!parms.containsKey("MASK")) 
				return "'MASK' not found!";
			String mask=((String)parms.get("MASK")).toUpperCase();
			INI page=INI.loadPropPage(CommonStrings.getVar(CommonStrings.SYSTEM_INIPATH));
			if((page==null)||(!page.loaded)) return "";
			if(mask.trim().endsWith("*"))
				for(Enumeration e=page.keys();e.hasMoreElements();)
				{
					String key=((String)e.nextElement()).toUpperCase();
					if(key.startsWith(mask.substring(0,mask.length()-1)))
					{
						httpReq.addRequestParameters("INI",key);
						if(parms.containsKey("VALUE"))
							return page.getStr(key);
						else
						if(parms.containsKey("INIHELP"))
							return getHelpFor(key,mask);
						return "";
					}
				}
			httpReq.addRequestParameters("INI",mask);
			if(parms.containsKey("VALUE"))
				return page.getStr(mask);
			else
			if(parms.containsKey("INIHELP"))
				return getHelpFor(mask,mask);
			return "";
		}
	}
}
