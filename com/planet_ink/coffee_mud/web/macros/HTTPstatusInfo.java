package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;


public class HTTPstatusInfo extends StdWebMacro
{
	public String name()	{return "HTTPstatusInfo";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return httpReq.getHTTPstatusInfo();
	}

}