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
public class SystemFunction extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if(parms.get("ANNOUNCE")!=null)
		{
			String s=httpReq.getRequestParameter("TEXT");
			if((s!=null)&&(s.length()>0))
			{
				MOB M=((MOB)CMClass.sampleMOB().copyOf());
				Command C=CMClass.getCommand("Announce");
				try{
					C.execute(M,Util.parse("all "+s.trim()));
				}catch(Exception e){}
			}
		}
		if(parms.get("SHUTDOWN")!=null)
		{
com.planet_ink.coffee_mud.application.MUD.globalShutdown(null,(parms.get("RESTART")==null),null);
			return "";
		}
		return "";
	}
}
