package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RaceNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("RACE");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("RACE");
			return "";
		}
		String lastID="";
		for(Enumeration r=CMClass.races();r.hasMoreElements();)
		{
			Race R=(Race)r.nextElement();
			if((R.availability()==Race.AVAILABLE_ALL)||(parms.containsKey("ALL")))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!R.ID().equals(lastID))))
				{
					httpReq.addRequestParameters("RACE",R.ID());
					return "";
				}
				lastID=R.ID();
			}
		}
		httpReq.addRequestParameters("RACE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}