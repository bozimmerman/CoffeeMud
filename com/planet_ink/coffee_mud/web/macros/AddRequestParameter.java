package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AddRequestParameter extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String str="";
		Hashtable parms=parseParms(parm);
		
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key!=null)
			{
				String val=(String)parms.get(key);
				if(val==null) val="";
				if((val.equals("++")&&(httpReq.isRequestParameter(key))))
					val=""+(Util.s_int(httpReq.getRequestParameter(key))+1);
				else
				if((val.equals("--")&&(httpReq.isRequestParameter(key))))
					val=""+(Util.s_int(httpReq.getRequestParameter(key))-1);
			
				httpReq.addRequestParameters(key,val);
			}
		}
		return str;
	}
}
