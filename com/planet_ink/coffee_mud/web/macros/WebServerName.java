package com.planet_ink.coffee_mud.web.macros;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class WebServerName extends StdWebMacro
{
	public String name()	{return "WebServerName";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return httpReq.getWebServerPartialName();
	}

}