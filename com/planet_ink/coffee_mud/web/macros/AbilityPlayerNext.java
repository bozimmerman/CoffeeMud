package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityPlayerNext extends StdWebMacro
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
		String playerName=httpReq.getRequestParameter("PLAYER");
		MOB M=null;
		if((playerName!=null)&&(playerName.length()>0))
			M=PlayerNext.getMOB(playerName);
		if(M==null)
		{
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			else
				return " @break@";
		}

		for(int a=0;a<M.numAbilities();a++)
		{
			Ability A=M.fetchAbility(a);
			boolean okToShow=true;
			int classType=A.classificationCode()&Ability.ALL_CODES;
			String className=httpReq.getRequestParameter("CLASS");
			
			if((className!=null)&&(className.length()>0))
			{
				int level=CMAble.getQualifyingLevel(className,A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					String levelName=httpReq.getRequestParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(Util.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			else
			{
				int level=CMAble.getQualifyingLevel("Archon",A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					String levelName=httpReq.getRequestParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(Util.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			if(okToShow)
			{
				if(parms.containsKey("DOMAIN")&&(classType==Ability.SPELL))
				{
					String domain=(String)parms.get("DOMAIN");
					if(!domain.equalsIgnoreCase(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5]))
					   okToShow=false;
				}
				else
				{
					boolean containsOne=false;
					for(int i=0;i<Ability.TYPE_DESCS.length;i++)
						if(parms.containsKey(Ability.TYPE_DESCS[i]))
						{ containsOne=true; break;}
					if(containsOne&&(!parms.containsKey(Ability.TYPE_DESCS[classType])))
						okToShow=false;
				}
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