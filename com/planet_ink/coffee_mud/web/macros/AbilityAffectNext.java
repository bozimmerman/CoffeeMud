package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class AbilityAffectNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ABILITY");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.getRequestParameters().remove("ABILITY");
			return "";
		}
		String lastID="";
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			boolean okToShow=true;
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			int classType=A.classificationCode()&Ability.ALL_CODES;
			okToShow=(CMAble.getQualifyingLevel("Archon",A.ID())<0);
			if(okToShow)
			{
				String ableType=(String)httpReq.getRequestParameters().get("ABILITYTYPE");
				if((ableType!=null)&&(ableType.length()>0))
					parms.put(ableType,ableType);
				boolean containsOne=false;
				for(int i=0;i<Ability.TYPE_DESCS.length;i++)
					if(parms.containsKey(Ability.TYPE_DESCS[i]))
					{ containsOne=true; break;}
				if(containsOne&&(!parms.containsKey(Ability.TYPE_DESCS[classType])))
					okToShow=false;
			}
			if(parms.containsKey("NOT")) okToShow=!okToShow;
			if(okToShow)
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))))
				{
					httpReq.getRequestParameters().put("ABILITY",A.ID());
					return "";
				}
				lastID=A.ID();
			}
		}
		httpReq.getRequestParameters().put("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}