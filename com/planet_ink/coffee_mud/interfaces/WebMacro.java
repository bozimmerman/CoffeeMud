package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import com.planet_ink.coffee_mud.web.*;


public interface WebMacro
{
	public String ID();
	public String name();

	public boolean isAdminMacro();

	public String runMacro(ExternalHTTPRequests httpReq, String parm);
}