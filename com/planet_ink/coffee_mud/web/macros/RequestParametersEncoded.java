package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RequestParametersEncoded extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return httpReq.getRequestEncodedParameters();
	}
}
