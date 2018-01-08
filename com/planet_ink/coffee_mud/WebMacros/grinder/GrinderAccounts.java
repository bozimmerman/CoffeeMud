package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
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
public class GrinderAccounts
{
	public String name()
	{
		return "GrinderAccounts";
	}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		final String last=httpReq.getUrlParameter("ACCOUNT");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final PlayerAccount A=CMLib.players().getLoadAccount(last);
			if(A!=null)
			{
				String newName=A.getAccountName();
				String str=null;
				String err="";
				str=httpReq.getUrlParameter("NAME");
				if((str!=null)&&(!str.equalsIgnoreCase(A.getAccountName())))
				{
					str=CMStrings.capitalizeAndLower(str);
					if(CMLib.players().getLoadAccount(str)==null)
						newName=str;
					else
						err="Account name '"+str+"' already exists";
				}
				str=httpReq.getUrlParameter("EMAIL");
				if(str!=null)
					A.setEmail(str);
				str=httpReq.getUrlParameter("NOTES");
				if(str!=null)
					A.setNotes(str);
				str=httpReq.getUrlParameter("BONUSLANGS");
				if(str != null)
					A.setBonusLanguageLimits(CMath.s_int(str));
				str=httpReq.getUrlParameter("BONUSCHARLIMIT");
				if(str != null)
					A.setBonusCharsLimit(CMath.s_int(str));
				str=httpReq.getUrlParameter("BONUSCHARONLINE");
				if(str != null)
					A.setBonusCharsOnlineLimit(CMath.s_int(str));
				str=httpReq.getUrlParameter("BONUSALLCOMMONSKILLS");
				if(str != null)
					A.setBonusCommonSkillLimits(CMath.s_int(str));
				str=httpReq.getUrlParameter("BONUSCRAFTINGSKILLS");
				if(str != null)
					A.setBonusCraftingSkillLimits(CMath.s_int(str));
				str=httpReq.getUrlParameter("BONUSNONCRAFTINGSKILLS");
				if(str != null)
					A.setBonusNonCraftingSkillLimits(CMath.s_int(str));
				str=httpReq.getUrlParameter("TATTOOS");
				if(str!=null)
				{
					List<Tattoo> oldTatts = new XVector<Tattoo>(A.tattoos());
					for(Tattoo t : oldTatts)
						A.delTattoo(t);
					List<String> tattNames = CMParms.parseCommas(str,true);
					for(String tattName : tattNames)
						A.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tattName));
				}
				str=httpReq.getUrlParameter("EXPIRATION");
				if(str!=null)
				{
					if(str.equalsIgnoreCase("Never"))
						A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, true);
					else
					if(!CMLib.time().isValidDateString(str))
						err="Invalid date string given.";
					else
					{
						A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, false);
						final Calendar C=CMLib.time().string2Date(str);
						A.setAccountExpiration(C.getTimeInMillis());
					}
				}
				String id="";
				final StringBuffer flags=new StringBuffer("");
				for(int i=0;httpReq.isUrlParameter("FLAG"+id);id=""+(++i))
					flags.append(httpReq.getUrlParameter("FLAG"+id)+",");
				A.setStat("FLAGS",flags.toString());
				if(err.length()>0)
					return err;
				else
				if(!newName.equalsIgnoreCase(A.getAccountName()))
				{
					final Vector<MOB> V=new Vector<MOB>();
					for(final Enumeration<String> es=A.getPlayers();es.hasMoreElements();)
					{
						final String playerName=es.nextElement();
						final MOB playerM=CMLib.players().getLoadPlayer(playerName);
						if((playerM!=null)&&(!CMLib.flags().isInTheGame(playerM,true)))
							V.addElement(playerM);
					}
					CMLib.database().DBDeleteAccount(A);
					A.setAccountName(newName);
					CMLib.database().DBCreateAccount(A);
					for(final MOB playerM : V)
						CMLib.database().DBUpdatePlayerPlayerStats(playerM);
					httpReq.addFakeUrlParameter("ACCOUNT", newName);
				}
				else
				{
					CMLib.database().DBUpdateAccount(A);
				}
			}
		}
		return "";
	}
}
