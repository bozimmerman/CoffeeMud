package com.planet_ink.coffee_mud.web.macros;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.web.*;


public class WebServerName extends StdWebMacro
{
	public String name()	{return "WebServerName";}

	public String runMacro(ProcessHTTPrequest httpReq)
	{
		return httpReq.getWebServer().getPartialName();
	}

}