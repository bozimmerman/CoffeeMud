package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;
import java.util.*;

public class StdWebMacro implements WebMacro
{
	public String ID()		{return name();}
	public String name()	{return "UNKNOWN";}

	// not yet implemented!
	public boolean isAdminMacro()	{return false;}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException
	{
		return "[Unimplemented macro!]";
	}
	
	protected StringBuffer helpHelp(StringBuffer s)
	{
		if(s!=null)
		{
			int x=s.toString().indexOf("\n\r");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\n\r");}
			x=s.toString().indexOf("\r\n");
			while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\r\n");}
			x=s.toString().lastIndexOf("<BR>");
			int count=0;
			int lastSpace=x+4;
			while((x>=0)&&(x<s.length()))
			{
				count++;
				if(s.charAt(x)==' ')
					lastSpace=x;
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