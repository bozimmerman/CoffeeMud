package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityRaceNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ABILITY");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("ABILITY");
			return "";
		}
		String ableType=httpReq.getRequestParameter("ABILITYTYPE");
		if((ableType!=null)&&(ableType.length()>0))
			parms.put(ableType,ableType);
		String domainType=httpReq.getRequestParameter("DOMAIN");
		if((domainType!=null)&&(domainType.length()>0))
			parms.put("DOMAIN",domainType);
		
		String lastID="";
		String raceID=httpReq.getRequestParameter("RACE");
		Race R=null;
		if((raceID!=null)&&(raceID.length()>0))
			R=CMClass.getRace(raceID);
		if(R==null)
		{
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}

		for(int a=0;a<R.racialAbilities(null).size();a++)
		{
			Ability A=(Ability)R.racialAbilities(null).elementAt(a);
			boolean okToShow=true;
			int level=CMAble.getQualifyingLevel(R.ID(),A.ID());
			if(level<0)
				okToShow=false;
			else
			{
				String levelName=httpReq.getRequestParameter("LEVEL");
				if((levelName!=null)&&(levelName.length()>0)&&(Util.s_int(levelName)!=level))
					okToShow=false;
			}
			if(parms.containsKey("NOT")) okToShow=!okToShow;
			if(okToShow)
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.ID().equals(lastID))))
				{
					httpReq.addRequestParameters("ABILITY",A.ID());
					return "";
				}
				lastID=A.ID();
			}
		}
		httpReq.addRequestParameters("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}