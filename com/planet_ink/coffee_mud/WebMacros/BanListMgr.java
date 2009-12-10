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
public class BanListMgr extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("BANNEDONE");
		if(parms.containsKey("RESET"))
		{
			if(last!=null) httpReq.removeRequestParameter("BANNEDONE");
			return "";
		}
		else
		if(parms.containsKey("NEXT"))
		{
			String lastID="";
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			for(int i=0;i<banned.size();i++)
			{
				String key=(String)banned.elementAt(i);
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!key.equals(lastID))))
				{
					httpReq.addRequestParameters("BANNEDONE",key);
					return "";
				}
				lastID=key;
			}
			httpReq.addRequestParameters("BANNEDONE","");
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}
		else
		if(parms.containsKey("DELETE"))
		{
			String key=httpReq.getRequestParameter("BANNEDONE");
			if(key==null) return "";
            CMSecurity.unban(key);
			return "'"+key+"' no longer banned.";
		}
		else
		if(parms.containsKey("ADD"))
		{
			String key=httpReq.getRequestParameter("NEWBANNEDONE");
			if(key==null) return "";
            CMSecurity.ban(key);
			return "'"+key+"' is now banned.";
		}
		else
		if(last!=null)
			return last;
		return "<!--EMPTY-->";
	}
	
}
