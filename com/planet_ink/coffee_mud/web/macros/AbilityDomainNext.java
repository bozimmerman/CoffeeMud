package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityDomainNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("DOMAIN");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("DOMAIN");
			return "";
		}
		String lastID="";
		for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
		{
			String S=Ability.DOMAIN_DESCS[i];
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!S.equals(lastID))))
			{
				httpReq.addRequestParameters("DOMAIN",S);
				return "";
			}
			lastID=S;
		}
		httpReq.addRequestParameters("DOMAIN","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}