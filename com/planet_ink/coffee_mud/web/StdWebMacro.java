package com.planet_ink.coffee_mud.web;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;


abstract public class StdWebMacro implements WebMacro
{
	public String ID()		{return name();}
	public String name()	{return "UNKNOWN";}

	public String macroID()	{return "@" + name().toUpperCase() + "@";}

	// not yet implemented!
	public boolean isAdminMacro()	{return false;}

	
	public String runMacro(ProcessHTTPrequest httpReq)
	{
		return "[Unimplemented macro!]";
	}

}