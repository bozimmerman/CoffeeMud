package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class CheckAuthCode extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		Hashtable auths=new Hashtable();
		String login=Authenticate.getLogin(httpReq);
		if(!Authenticate.authenticated(httpReq,login,Authenticate.getPassword(httpReq)))
			return "false";
		MOB mob=CMMap.getLoadPlayer(login);
		if(mob==null) return "false";
		boolean subOp=false;
		boolean sysop=CMSecurity.isASysOp(mob);
		
		String AREA=httpReq.getRequestParameter("AREA");
		Room R=null;
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if((AREA==null)||(AREA.length()==0)||(AREA.equals(A.Name())))
				if(A.amISubOp(mob.Name()))
				{ 
					if((R==null)&&(A.getProperMap().hasMoreElements()))
						R=(Room)A.getProperMap().nextElement();
					subOp=true; 
					break;
				}
		}
		auths.put("ANYMODAREAS",""+((subOp&&(CMSecurity.isAllowedAnywhere(mob,"CMDROOMS")||CMSecurity.isAllowedAnywhere(mob,"CMDAREAS")))
													   ||CMSecurity.isAllowedEverywhere(mob,"CMDROOMS")||CMSecurity.isAllowedEverywhere(mob,"CMDAREAS")));
		auths.put("ALLMODAREAS",""+(CMSecurity.isAllowedEverywhere(mob,"CMDROOMS")||CMSecurity.isAllowedEverywhere(mob,"CMDAREAS")));
		auths.put("SYSOP",""+sysop);
		auths.put("SUBOP",""+(sysop||subOp));
		Vector V=CMSecurity.getSecurityCodes(mob,R);
		for(int v=0;v<V.size();v++)
			auths.put("AUTH_"+((String)V.elementAt(v)),"true");
		boolean finalCondition=false;
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
			String check=(String)auths.get(key);
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
		else
			return "false";
	}
}
