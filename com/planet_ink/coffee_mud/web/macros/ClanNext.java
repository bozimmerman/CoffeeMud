package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ClanNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("CLAN");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("CLAN");
			return "";
		}
		String lastID="";
		for(Enumeration c=Clans.clans();c.hasMoreElements();)
		{
			Clan C=(Clan)c.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.ID().equals(lastID))))
			{
				httpReq.addRequestParameters("CLAN",C.ID());
				return "";
			}
			lastID=C.ID();
		}
		httpReq.addRequestParameters("CLAN","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}