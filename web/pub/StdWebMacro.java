package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.CMClass;
import com.planet_ink.coffee_mud.exceptions.*;
import java.util.*;

public class StdWebMacro implements WebMacro
{
	public String ID()		{return name();}
	public String name()	{return "UNKNOWN";}

	public boolean isAdminMacro()	{return false;}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
	{
		return "[Unimplemented macro!]";
	}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
	protected StringBuffer helpHelp(StringBuffer s)
	{
		if(s!=null)
		{
			s=new StringBuffer(s.toString());
			int x=s.toString().indexOf("\n\r");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\n\r");}
			x=s.toString().indexOf("\r\n");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\r\n");}
			int count=0;
			x=0;
			int lastSpace=0;
			while((x>=0)&&(x<s.length()))
			{
				count++;
				if(s.charAt(x)==' ')
					lastSpace=x;
				if((s.charAt(x)=='<')
				   &&(x<s.length()-4)
				   &&(s.substring(x,x+4).equalsIgnoreCase("<BR>")))
				{
					count=0;
					x=x+4;
					lastSpace=x+4;
				}
				if(count>=70)
				{
					s.replace(lastSpace,lastSpace+1,"<BR>");
					lastSpace=lastSpace+4;
					x=lastSpace+4;
					count=0;
				}
				else
					x++;
			}
			return s;
		}
		else
			return new StringBuffer("");
	}
	
	protected Hashtable parseParms(String parm)
	{
		Hashtable requestParms=new Hashtable();
		if((parm!=null)&&(parm.length()>0))
		{
			while(parm.length()>0)
			{
				int x=parm.indexOf("&");
				String req=null;
				if(x>=0)
				{
					req=parm.substring(0,x);
					parm=parm.substring(x+1);
				}
				else
				{
					req=parm;
					parm="";
				}
				if(req!=null)
				{
					x=req.indexOf("=");
					if(x>=0)
						requestParms.put(req.substring(0,x).trim().toUpperCase(),req.substring(x+1).trim());
					else
						requestParms.put(req.trim().toUpperCase(),req.trim());
				}
			}
		}
		return requestParms;
	}
}