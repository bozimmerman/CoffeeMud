package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class MUDServerStatus extends StdWebMacro
{
	public String name()	{return "MUDServerStatus";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		return CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS);
	}

}