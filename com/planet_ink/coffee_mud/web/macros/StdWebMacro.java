package com.planet_ink.coffee_mud.web.macros;
import com.planet_ink.coffee_mud.interfaces.*;

abstract public class StdWebMacro implements WebMacro
{
	public String ID()		{return name();}
	public String name()	{return "UNKNOWN";}

	public String macroID()	{return "@" + name().toUpperCase() + "@";}

	// not yet implemented!
	public boolean isAdminMacro()	{return false;}

	
	public String runMacro(ExternalHTTPRequests httpReq)
	{
		return "[Unimplemented macro!]";
	}

}