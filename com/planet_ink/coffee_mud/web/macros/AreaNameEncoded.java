package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AreaNameEncoded extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=(String)httpReq.getRequestParameters().get("AREA");
		if(last==null) return "";
		if(last.length()>0)
		{
			Area A=CMMap.getArea(last);
			if(A!=null)
			{
				try
				{
					return URLEncoder.encode(A.name(),"UTF-8");
				}  
				catch(java.io.UnsupportedEncodingException e)
				{
					Log.errOut(name(),"Wrong Encoding");
				}
			}
		}
		return "";
	}
}
