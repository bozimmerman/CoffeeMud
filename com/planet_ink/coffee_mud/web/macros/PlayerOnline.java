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
public class PlayerOnline extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String ban(String banMe)
	{
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
				return "false";
		}
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		if(banMe.trim().length()>0) str.append(banMe+"\n");
		Resources.updateResource("banned.ini",str);
		Resources.saveFileResource("banned.ini");
		return "true";
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS);

		String last=httpReq.getRequestParameter("PLAYER");
		Hashtable parms=parseParms(parm);
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			MOB M=CMMap.getLoadPlayer(last);
			if(M!=null)
			{
				if(parms.containsKey("BANBYNAME"))
					ban(last);
				
				if(M.session()!=null)
				{
					if(parms.containsKey("BOOT"))
					{
						M.session().setKillFlag(true);
						return "false";
					}
					else
					if(parms.containsKey("BANBYIP"))
						ban(M.session().getAddress());
					return "true";
				}
			}
		}
		return "false";
	}
}
