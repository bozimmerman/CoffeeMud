package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;


public class AreaTbl extends StdWebMacro
{
	public String name()	{return "AreaTbl";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return httpReq.WebHelperhtmlAreaTbl();
	}

}