package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RequestParameterEncoded extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String str="";
		Hashtable parms=parseParms(parm);
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			try
			{
				if(httpReq.getRequestParameters().containsKey(key))
					str+=URLEncoder.encode((String)httpReq.getRequestParameters().get(key),"UTF-8");
			}  
			catch(java.io.UnsupportedEncodingException ex)
			{
				Log.errOut(name(),"Wrong Encoding");
			}
		}
		return str;
	}
}
