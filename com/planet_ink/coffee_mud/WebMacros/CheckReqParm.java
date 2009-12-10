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
public class CheckReqParm extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		boolean finalCondition=false;
		for(Enumeration e=parms.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String equals=(String)parms.get(key);
			boolean not=false;
			boolean thisCondition=true;
            boolean startswith=false;
            boolean inside=false;
            boolean endswith=false;
			if(key.startsWith("||")) key=key.substring(2);
            if(key.startsWith("<")){
                startswith=true;
                key=key.substring(1);
            }
            if(key.startsWith(">")){
                endswith=true;
                key=key.substring(1);
            }
            if(key.startsWith("*")){
                inside=true;
                key=key.substring(1);
            }
			
			if(key.startsWith("!"))
			{
				key=key.substring(1);
				not=true;
			}
			String check=httpReq.getRequestParameter(key);
			if(not)
			{
				if((check==null)&&(equals.length()==0))
					thisCondition=false;
				else
				if(check==null) 
					thisCondition=true;
                else
                if(startswith)
                    thisCondition=!check.startsWith(equals);
                else
                if(endswith)
                    thisCondition=!check.endsWith(equals);
                else
                if(inside)
                    thisCondition=!(check.indexOf(equals)>=0);
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
                if(startswith)
                    thisCondition=check.startsWith(equals);
                else
                if(endswith)
                    thisCondition=check.endsWith(equals);
                else
                if(inside)
                    thisCondition=(check.indexOf(equals)>=0);
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
