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
public class RaceClassNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String race=httpReq.getRequestParameter("RACE");
		if(race.length()==0) return " @break@";
		Race R=CMClass.getRace(race);
		String last=httpReq.getRequestParameter("CLASS");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("CLASS");
			return "";
		}
		String lastID="";
		MOB mob=CMClass.getMOB("StdMOB");
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			mob.baseCharStats().setStat(i,25);
		mob.baseCharStats().setMyRace(R);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if(((C.playerSelectable())||(parms.containsKey("ALL")))
			   &&(C.qualifiesForThisClass(mob,true)))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!C.ID().equals(lastID))))
				{
					httpReq.addRequestParameters("CLASS",C.ID());
					return "";
				}
				lastID=C.ID();
			}
		}
		httpReq.addRequestParameters("CLASS","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}
