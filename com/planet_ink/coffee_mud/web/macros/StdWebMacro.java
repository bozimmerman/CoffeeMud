package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.CMClass;
import com.planet_ink.coffee_mud.utils.DVector;
import com.planet_ink.coffee_mud.exceptions.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
				if((s.charAt(x)=='-')
				&&(x>4)
				&&(s.charAt(x-1)=='-')
				&&(s.charAt(x-2)=='-')
				&&(s.charAt(x-3)=='-'))
				{
					count=0;
					lastSpace=x;
				}
				if((s.charAt(x)=='!')
				&&(x>4)
				&&(s.charAt(x-1)==' ')
				&&(s.charAt(x-2)==' ')
				&&(s.charAt(x-3)==' '))
				{
					count=0;
					lastSpace=x;
				}
				if(count==70)
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
	
	protected DVector parseOrderedParms(String parm)
	{
		DVector requestParms=new DVector(2);
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
						requestParms.addElement(req.substring(0,x).trim().toUpperCase(),req.substring(x+1).trim());
					else
						requestParms.addElement(req.trim().toUpperCase(),req.trim());
				}
			}
		}
		return requestParms;
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
