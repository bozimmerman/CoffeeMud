package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class MUDServerVersion extends StdWebMacro
{
	public String name()	{return "MUDServerVersion";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return "CoffeeMud-MainServer/" + CommonStrings.getVar(CommonStrings.SYSTEM_MUDVER);
	}

}