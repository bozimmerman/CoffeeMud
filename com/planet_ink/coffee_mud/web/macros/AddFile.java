package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AddFile extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms==null)
			return "";
		StringBuffer buf=new StringBuffer("");
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String file=(String)e.nextElement();
			if(file.length()>0)
				buf.append(httpReq.getPageContent(file));
		}
		return buf.toString();
	}
}
