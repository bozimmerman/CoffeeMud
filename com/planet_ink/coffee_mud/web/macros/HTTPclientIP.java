package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class HTTPclientIP extends StdWebMacro
{
	public String name()	{return "HTTPclientIP";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return httpReq.getHTTPclientIP();
	}
}