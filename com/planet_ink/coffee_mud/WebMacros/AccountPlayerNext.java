package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
import com.planet_ink.miniweb.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class AccountPlayerNext extends StdWebMacro
{
	public String name() { return "AccountPlayerNext"; }

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("PLAYER");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeUrlParameter("PLAYER");
			return "";
		}
		String accountName=httpReq.getUrlParameter("ACCOUNT");
		if(accountName==null) return "";
		PlayerAccount account=CMLib.players().getLoadAccount(accountName);
		if(account==null) return "";
		
		String lastID="";
		String sort=httpReq.getUrlParameter("SORTBY");
		if(sort==null) sort="";
		Enumeration pe=account.getThinPlayers();
		for(;pe.hasMoreElements();)
		{
			PlayerLibrary.ThinPlayer user=(PlayerLibrary.ThinPlayer)pe.nextElement();
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!user.name.equals(lastID))))
			{
				httpReq.addFakeUrlParameter("PLAYER",user.name);
				return "";
			}
			lastID=user.name;
		}
		httpReq.addFakeUrlParameter("PLAYER","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
