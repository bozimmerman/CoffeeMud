package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class DeityNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("DEITY");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("DEITY");
			return "";
		}
		String lastID="";
		for(Enumeration d=CMMap.deities();d.hasMoreElements();)
		{
			Deity D=(Deity)d.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!D.Name().equals(lastID))))
			{
				httpReq.addRequestParameters("DEITY",D.Name());
				return "";
			}
			lastID=D.Name();
		}
		httpReq.addRequestParameters("DEITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}