package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class AbilityNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
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
		long flags=0;
		String flagString=httpReq.getRequestParameter("FLAGS");
		if((flagString!=null)&&(flagString.length()>0))
		{
		    Vector V=CMParms.parseSquiggles(flagString.toUpperCase());
		    for(int i=0;i<Ability.FLAG_DESCS.length;i++)
		        if(V.contains(Ability.FLAG_DESCS[i]))
		            flags=flags|(CMath.pow(2,i));
		}
		
		String lastID="";
        String className=httpReq.getRequestParameter("CLASS");
        boolean genericOnly =parms.containsKey("GENERIC");
        boolean parmsEditable=parms.containsKey("PARMSEDITABLE");
        String levelName=httpReq.getRequestParameter("LEVEL");
        boolean notFlag =parms.containsKey("NOT"); 
        boolean allFlag =parms.containsKey("ALL");
        boolean domainFlag=parms.containsKey("DOMAIN");
        String domain=(String)parms.get("DOMAIN");
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			boolean okToShow=true;
			int classType=A.classificationCode()&Ability.ALL_ACODES;
			if(genericOnly)
			    okToShow=A.isGeneric();
            else
            if(parmsEditable)
                okToShow=((A instanceof ItemCraftor)
                       &&(((ItemCraftor)A).parametersFile()!=null)
                       &&(((ItemCraftor)A).parametersFile().length()>0)
                       &&(((ItemCraftor)A).parametersFormat()!=null)
                       &&(((ItemCraftor)A).parametersFormat().length()>0));
			
			if((className!=null)&&(className.length()>0))
			{
				int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
				if(level<0)
					okToShow=false;
				else
				if(CMLib.ableMapper().getSecretSkill(className,false,A.ID()))
					okToShow=false;
				else
				if((flags>0)&&((A.flags()&flags)!=flags))
				    okToShow=false;
				else
				{
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			else
			if(!allFlag)
			{
				int level=CMLib.ableMapper().getQualifyingLevel("Archon",true,A.ID());
				if(level<0)
					okToShow=false;
				else
				if(CMLib.ableMapper().getAllSecretSkill(A.ID()))
					okToShow=false;
				else
				if((flags>0)&&((A.flags()&flags)!=flags))
				    okToShow=false;
				else
				{
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			if(okToShow)
			{
				if((domainFlag)&&(!domain.equalsIgnoreCase(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5])))
				   okToShow=false;
				boolean containsOne=false;
				for(int i=0;i<Ability.ACODE_DESCS.length;i++)
					if(parms.containsKey(Ability.ACODE_DESCS[i]))
					{ containsOne=true; break;}
				if(containsOne&&(!parms.containsKey(Ability.ACODE_DESCS[classType])))
					okToShow=false;
			}
			if(notFlag) okToShow=!okToShow;
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
		return " @break@";
	}
}
