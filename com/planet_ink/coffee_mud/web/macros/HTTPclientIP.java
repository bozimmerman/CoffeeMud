package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.web.*;

public class HTTPclientIP extends StdWebMacro
{
	public String name()	{return "HTTPclientIP";}

	public String runMacro(ProcessHTTPrequest httpReq)
	{
		return httpReq.getHTTPclientIP();
	}
}