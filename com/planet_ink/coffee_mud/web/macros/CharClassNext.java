package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class CharClassNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("CLASS");
		String base=(String)httpReq.getRequestParameters().get("BASECLASS");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("CLASS");
			return "";
		}
		String lastID="";
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if(((C.playerSelectable())||(parms.containsKey("ALL")))
			&&((base==null)||(base.length()==0)||(C.baseClass().equalsIgnoreCase(base))))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.ID().equals(lastID))))
				{
					httpReq.getRequestParameters().put("CLASS",C.ID());
					return "";
				}
				lastID=C.ID();
			}
		}
		httpReq.getRequestParameters().put("CLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}