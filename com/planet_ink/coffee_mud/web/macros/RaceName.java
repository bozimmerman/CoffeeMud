package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RaceName extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=httpReq.getRequestParameter("RACE");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Race R=CMClass.getRace(last);
			if(R!=null)
				return R.name();
		}
		return "";
	}
}
