package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
//import com.planet_ink.coffee_mud.system.*;
//import com.planet_ink.coffee_mud.utils.*;
//import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.web.*;


public class HTTPstatusInfo extends StdWebMacro
{
	public String name()	{return "HTTPstatusInfo";}

	public String runMacro(ProcessHTTPrequest httpReq)
	{
		return httpReq.getHTTPstatusInfo();
	}

}