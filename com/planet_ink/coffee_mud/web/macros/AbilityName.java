package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityName extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		String last=(String)httpReq.getRequestParameters().get("ABILITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Ability A=CMClass.getAbility(last);
			if(A!=null)
				return A.Name();
		}
		return "";
	}
}
