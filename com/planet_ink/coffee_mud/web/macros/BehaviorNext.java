package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class BehaviorNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BEHAVIOR");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("BEHAVIOR");
			return "";
		}
		String lastID="";
		for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
		{
			Behavior B=(Behavior)b.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!B.ID().equals(lastID))))
			{
				httpReq.addRequestParameters("BEHAVIOR",B.ID());
				return "";
			}
			lastID=B.ID();
		}
		httpReq.addRequestParameters("BEHAVIOR","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}