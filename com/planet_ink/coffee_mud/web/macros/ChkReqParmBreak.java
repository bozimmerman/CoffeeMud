package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ChkReqParmBreak extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			boolean not=false;
			if(key.startsWith("!"))
			{
				key=key.substring(1);
				not=true;
			}
			String equals=(String)parms.get(key);
			String check=httpReq.getRequestParameter(key);
			if(not)
			{
				if((check==null)&&(equals.length()==0))
					return " @break@";
				if(check==null) return "";
				if(check.equalsIgnoreCase(equals))
					return "";
				return " @break@";
			}
			else
			{
				if((check==null)&&(equals.length()==0))
					return "";
				if(check==null) return " @break@";
				if(!check.equalsIgnoreCase(equals))
					return " @break@";
			}
		}
		return "";
	}
}
