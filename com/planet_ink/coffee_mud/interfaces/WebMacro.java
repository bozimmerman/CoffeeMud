package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import com.planet_ink.coffee_mud.web.*;
import com.planet_ink.coffee_mud.exceptions.*;


public interface WebMacro
{
	public String ID();
	public String name();

	public boolean isAdminMacro();
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException;
}