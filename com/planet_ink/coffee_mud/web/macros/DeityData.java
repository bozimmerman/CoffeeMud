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
public class DeityData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include description, worshipreq, clericreq,
	// worshiptrig, clerictrig, worshipsintrig,clericsintrig,powertrig

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("DEITY");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Deity D=CMMap.getDeity(last);
			if(D!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("DESCRIPTION"))
					str.append(D.description()+", ");
				if(parms.containsKey("WORSHIPREQ"))
					str.append(D.getWorshipRequirementsDesc()+", ");
				if(parms.containsKey("CLERICREQ"))
					str.append(D.getClericRequirementsDesc()+", ");
				if(D.numCurses()>0)
				{
					if(parms.containsKey("WORSHIPSINTRIG"))
						str.append(D.getWorshipSinDesc()+", ");
					if(parms.containsKey("CLERICSINTRIG"))
						str.append(D.getClericSinDesc()+", ");
				}
				
				if(D.numPowers()>0)
				if(parms.containsKey("POWERTRIG"))
					str.append(D.getClericPowerupDesc()+", ");
				if(D.numBlessings()>0)
				{
					if(parms.containsKey("WORSHIPTRIG"))
						str.append(D.getWorshipTriggerDesc()+", ");
					if(parms.containsKey("CLERICTRIG"))
						str.append(D.getClericTriggerDesc()+", ");
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
