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
		String last=(String)httpReq.getRequestParameters().get("BEHAVIOR");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("BEHAVIOR");
			return "";
		}
		String lastID="";
		for(int b=0;b<CMClass.behaviors.size();b++)
		{
			Race B=(Race)CMClass.behaviors.elementAt(b);
			if((B.playerSelectable())||(parms.containsKey("ALL")))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))))
				{
					httpReq.getRequestParameters().put("BEHAVIOR",B.ID());
					return "";
				}
				lastID=B.ID();
			}
		}
		httpReq.getRequestParameters().put("BEHAVIOR","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}