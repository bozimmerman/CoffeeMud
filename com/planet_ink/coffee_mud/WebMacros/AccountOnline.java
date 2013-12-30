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
   Copyright 2000-2014 Bo Zimmerman

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
public class AccountOnline extends StdWebMacro
{
	public String name() { return "AccountOnline"; }

	public static final int MAX_IMAGE_SIZE=50*1024;
	
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		String last=httpReq.getUrlParameter("ACCOUNT");
		if(last==null) return " @break@";
		java.util.Map<String,String> parms=parseParms(parm);
		if(last.length()>0)
		{
			PlayerAccount A = CMLib.players().getAccount(last);
			MOB onlineM=null;
			if(A!=null)
			for(Enumeration<String> m = A.getPlayers();m.hasMoreElements();)
			{
				MOB M = CMLib.players().getPlayer(m.nextElement());
				if((M!=null)&&(M.session()!=null))
					onlineM=M;
			}
			if(parms.size()==0)
				return Boolean.toString(onlineM != null);
			else 
			{
				String login=Authenticate.getLogin(httpReq);
				if(Authenticate.authenticated(httpReq,login,Authenticate.getPassword(httpReq)))
				{
					if(A==null)
						A = CMLib.players().getLoadAccount(last);
					if(A==null) return "false";
					boolean canBan=false;
					boolean canBoot=false;
					
					MOB authM=CMLib.players().getLoadPlayer(login);
					if((authM!=null)&&(authM.playerStats()!=null)&&(authM.playerStats().getAccount().accountName().equals(A.accountName())))
					{
						canBan=true;
						canBoot=true;
					}
					else
					if(authM!=null)
					{
						if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BAN))
							canBan=true;
						if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BOOT))
							canBoot=true;
					}
					
					if(canBan&&(parms.containsKey("BANBYNAME")))
						CMSecurity.ban(last);
					if(canBan&&(parms.containsKey("BANBYIP")))
						CMSecurity.ban(A.lastIP());
					if(canBan&&(parms.containsKey("BANBYEMAIL")))
						CMSecurity.ban(A.getEmail());
					if((onlineM!=null)&&(onlineM.session()!=null))
					{
						if(canBoot&&(parms.containsKey("BOOT")))
						{
							onlineM.session().stopSession(false,false,false);
							return "false";
						}
						return "true";
					}
				}
			}
		}
		return "false";
	}
}
