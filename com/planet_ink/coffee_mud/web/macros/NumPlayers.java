package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class NumPlayers extends StdWebMacro
{
	public String name()	{return "NumPlayers";}

	public String runMacro(ExternalHTTPRequests httpReq)
	{
		return new Integer(Sessions.size()).toString();
	}

}