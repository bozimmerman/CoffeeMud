package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;


public class MUDServerPort extends StdWebMacro
{
	public String name()	{return "MUDServerPort";}

	public String runMacro(ExternalHTTPRequests httpReq)
	{
		return httpReq.getMUD().getPortStr();
	}

}