package com.planet_ink.coffee_mud.interfaces;

import com.planet_ink.coffee_mud.web.*;


public interface WebMacro
{
	public String ID();
	public String name();

	public String macroID();
	
	public boolean isAdminMacro();

	public String runMacro(ProcessHTTPrequest httpReq);
}