package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityPowersNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return " @break@";

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ABILITY");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("ABILITY");
			return "";
		}
		
		String lastID="";
		String deityName=httpReq.getRequestParameter("DEITY");
		Deity D=null;
		if((deityName!=null)&&(deityName.length()>0))
			D=CMMap.getDeity(deityName);
		if(D==null)
		{
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}

		for(int a=0;a<D.numPowers();a++)
		{
			Ability A=D.fetchPower(a);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.ID().equals(lastID))))
			{
				httpReq.addRequestParameters("ABILITY",A.ID());
				return "";
			}
			lastID=A.ID();
		}
		httpReq.addRequestParameters("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}