package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class WebServerVersion extends StdWebMacro
{
	public String name()	{return "WebServerVersion";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
//		return httpReq.getWebServer().ServerVersionString;
		// it's static
		return httpReq.ServerVersionString();
	}

}