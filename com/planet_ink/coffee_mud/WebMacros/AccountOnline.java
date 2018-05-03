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
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "AccountOnline";
	}

	public static final int MAX_IMAGE_SIZE=50*1024;

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final String last=httpReq.getUrlParameter("ACCOUNT");
		if(last==null)
			return " @break@";
		final java.util.Map<String,String> parms=parseParms(parm);
		if(last.length()>0)
		{
			PlayerAccount A = CMLib.players().getAccount(last);
			MOB onlineM=null;
			if(A!=null)
			for(final Enumeration<String> m = A.getPlayers();m.hasMoreElements();)
			{
				final MOB M = CMLib.players().getPlayer(m.nextElement());
				if((M!=null)&&(M.session()!=null))
					onlineM=M;
			}
			if(parms.size()==0)
				return Boolean.toString(onlineM != null);
			else
			{
				final String login=Authenticate.getLogin(httpReq);
				if(Authenticate.authenticated(httpReq,login,Authenticate.getPassword(httpReq)))
				{
					if(A==null)
						A = CMLib.players().getLoadAccount(last);
					if(A==null)
						return "false";
					boolean canBan=false;
					boolean canBoot=false;
					boolean canModify=false;
					final MOB authM=CMLib.players().getLoadPlayer(login);
					if((authM!=null)&&(authM.playerStats()!=null)&&(authM.playerStats().getAccount().getAccountName().equals(A.getAccountName())))
					{
						canBan=true;
						canBoot=true;
						canModify=true;
					}
					else
					if(authM!=null)
					{
						if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BAN))
							canBan=true;
						if(CMSecurity.isAllowedEverywhere(authM,CMSecurity.SecFlag.BOOT))
							canBoot=true;
						if(CMSecurity.isAllowedEverywhere(authM, CMSecurity.SecFlag.CMDPLAYERS))
							canModify=true;
					}

					if(canBan&&(parms.containsKey("BANBYNAME")))
						CMSecurity.ban(last);
					if(canBan&&(parms.containsKey("BANBYIP")))
						CMSecurity.ban(A.getLastIP());
					if(canBan&&(parms.containsKey("BANBYEMAIL")))
						CMSecurity.ban(A.getEmail());
					if(canModify&&(parms.containsKey("EXPIRENEVER")))
					{
						A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, true);
						CMLib.database().DBUpdateAccount(A);
					}
					if(canModify&&(parms.containsKey("EXPIRENOW")))
					{
						A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, false);
						A.setAccountExpiration(System.currentTimeMillis());
						CMLib.database().DBUpdateAccount(A);
					}
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
