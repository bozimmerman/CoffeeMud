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
public class ClassRaceNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String cclass=httpReq.getRequestParameter("CLASS");
		if(cclass.trim().length()==0) return " @break@";
		CharClass C=CMClass.getCharClass(cclass.trim());
		if(C==null) return " @break";
		String last=httpReq.getRequestParameter("RACE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("RACE");
			return "";
		}
		String lastID="";
		MOB mob=CMClass.getMOB("StdMOB");
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			mob.baseCharStats().setStat(i,25);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		for(Enumeration r=CMClass.races();r.hasMoreElements();)
		{
			Race R=(Race)r.nextElement();
			mob.baseCharStats().setMyRace(R);
			mob.recoverCharStats();
			if(((R.availability()==Race.AVAILABLE_ALL)||(parms.containsKey("ALL")))
			   &&(C.qualifiesForThisClass(mob,true)))
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!R.ID().equals(lastID))))
				{
					httpReq.addRequestParameters("RACE",R.ID());
					return "";
				}
				lastID=R.ID();
			}
		}
		httpReq.addRequestParameters("RACE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}
}
