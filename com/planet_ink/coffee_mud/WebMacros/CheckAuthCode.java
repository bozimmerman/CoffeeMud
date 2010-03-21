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
public class CheckAuthCode extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public Hashtable getAuths(ExternalHTTPRequests httpReq)
	{
		MOB mob = Authenticate.getAuthenticatedMob(httpReq);
		if(mob==null) return null;
		Hashtable auths=(Hashtable)httpReq.getRequestObjects().get("AUTHS_"+mob.Name().toUpperCase().trim());
		if(auths==null)
		{
			auths=new Hashtable();
			boolean subOp=false;
			boolean sysop=CMSecurity.isASysOp(mob);
			
			String AREA=httpReq.getRequestParameter("AREA");
			Room R=null;
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if((AREA==null)||(AREA.length()==0)||(AREA.equals(A.Name())))
					if(A.amISubOp(mob.Name()))
					{ 
						R=A.getRandomProperRoom();
						subOp=true; 
						break;
					}
			}
			auths.put("ANYMODAREAS",""+((subOp&&(CMSecurity.isAllowedAnywhere(mob,"CMDROOMS")||CMSecurity.isAllowedAnywhere(mob,"CMDAREAS")))
														   ||CMSecurity.isAllowedEverywhere(mob,"CMDROOMS")||CMSecurity.isAllowedEverywhere(mob,"CMDAREAS")));
			auths.put("ALLMODAREAS",""+(CMSecurity.isAllowedEverywhere(mob,"CMDROOMS")||CMSecurity.isAllowedEverywhere(mob,"CMDAREAS")));
            Vector dirs=CMSecurity.getAccessibleDirs(mob,mob.location());
	        auths.put("ANYFILEBROWSE",""+(dirs.size()>0));
            if(dirs.size()>0)
            {
                int maxLen=Integer.MAX_VALUE;
                int maxOne=-1;
                for(int v=0;v<dirs.size();v++)
                    if(((String)dirs.elementAt(v)).length()<maxLen)
                    {
                        maxLen=((String)dirs.elementAt(v)).length();
                        maxOne=v;
                    }
                String winner=(String)dirs.elementAt(maxOne);
                httpReq.addRequestParameters("BESTFILEBROWSE",winner);
            }
            else
                httpReq.addRequestParameters("BESTFILEBROWSE","");
			auths.put("SYSOP",""+sysop);
			auths.put("SUBOP",""+(sysop||subOp));
	        
			Vector V=CMSecurity.getSecurityCodes(mob,R);
			for(int v=0;v<V.size();v++)
				auths.put("AUTH_"+((String)V.elementAt(v)),"true");
			httpReq.getRequestObjects().put("AUTHS_"+mob.Name().toUpperCase().trim(),auths);
		}
		return auths;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		boolean finalCondition=false;
		Hashtable auths=getAuths(httpReq);
		if(auths==null) return "false";
		boolean sysop=((String)auths.get("SYSOP")).equalsIgnoreCase("true");
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String equals=(String)parms.get(key);
			boolean not=false;
			boolean thisCondition=true;
			if(key.startsWith("||")) key=key.substring(2);
			if(key.startsWith("!"))
			{
				key=key.substring(1);
				not=true;
			}
			String check=sysop?"true":(String)auths.get(key);
			if(not)
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=false;
				else
				if(check==null) 
					thisCondition=true;
				else
				if(!check.equalsIgnoreCase(equals))
					thisCondition=true;
				else
					thisCondition=false;
			}
			else
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=true;
				else
				if(check==null) 
					thisCondition=false;
				else
				if(!check.equalsIgnoreCase(equals))
					thisCondition=false;
				else
					thisCondition=true;
			}
			finalCondition=finalCondition||thisCondition;
		}
		if(finalCondition)
			return "true";
		return "false";
	}
}
