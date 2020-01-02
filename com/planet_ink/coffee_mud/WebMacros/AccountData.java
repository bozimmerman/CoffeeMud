package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
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
   Copyright 2010-2020 Bo Zimmerman

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
public class AccountData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AccountData";
	}

	@Override
	public boolean isAdminMacro()
	{
		return true;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("ACCOUNT");
		if(last==null)
			return "";
		if(last.length()>0)
		{
			final PlayerAccount A = CMLib.players().getLoadAccount(last);
			if(A==null)
				return "";
			if(parms.containsKey("NAME")||parms.containsKey("ACCOUNT"))
				return clearWebMacros(A.getAccountName());
			if(parms.containsKey("CLASS"))
				return clearWebMacros(A.ID());
			if(parms.containsKey("LASTIP"))
				return ""+A.getLastIP();
			if(parms.containsKey("LASTDATETIME"))
				return ""+CMLib.time().date2String(A.getLastDateTime());
			if(parms.containsKey("EMAIL"))
				return ""+A.getEmail();
			if(parms.containsKey("BONUSLANGS"))
				return ""+A.getBonusLanguageLimits();
			if(parms.containsKey("BONUSCHARLIMIT"))
				return ""+A.getBonusCharsLimit();
			if(parms.containsKey("BONUSCHARONLINE"))
				return ""+A.getBonusCharsOnlineLimit();
			if(parms.containsKey("BONUSALLCOMMONSKILLS"))
				return ""+A.getBonusCommonSkillLimits();
			if(parms.containsKey("BONUSCRAFTINGSKILLS"))
				return ""+A.getBonusCraftingSkillLimits();
			if(parms.containsKey("BONUSNONCRAFTINGSKILLS"))
				return ""+A.getBonusNonCraftingSkillLimits();
			if(parms.containsKey("NOTES"))
				return ""+A.getNotes();
			if(parms.containsKey("TATTOOS"))
			{
				final StringBuilder str = new StringBuilder("");
				for(final Enumeration<Tattoo> e=A.tattoos();e.hasMoreElements();)
					str.append(e.nextElement().toString()+", ");
				return str.toString();
			}
			if(parms.containsKey("ACCTEXPIRATION"))
			{
				if(A.isSet(PlayerAccount.AccountFlag.NOEXPIRE))
					return "Never";
				return ""+CMLib.time().date2String(A.getAccountExpiration());
			}
			for(final PlayerAccount.AccountFlag flag : PlayerAccount.AccountFlag.values())
			{
				if(parms.containsKey("IS"+flag.name()))
					return ""+A.isSet(flag);
			}
			if(parms.containsKey("FLAGS"))
			{
				final String old=httpReq.getUrlParameter("FLAGS");
				List<String> set=null;
				if(old==null)
				{
					final String matList=A.getStat("FLAGS");
					set=CMParms.parseCommas(matList,true);
				}
				else
				{
					String id="";
					set=new Vector<String>();
					for(int i=0;httpReq.isUrlParameter("FLAG"+id);id=""+(++i))
						set.add(httpReq.getUrlParameter("FLAG"+id));
				}
				final StringBuffer str=new StringBuffer("");
				if(parms.containsKey("READONLY"))
					str.append(CMParms.toListString(set));
				else
				{
					for (final PlayerAccount.AccountFlag element : PlayerAccount.AccountFlag.values())
					{
						str.append("<OPTION VALUE=\""+element.name()+"\"");
						if(set.contains(element.name()))
							str.append(" SELECTED");
						str.append(">"+CMStrings.capitalizeAndLower(element.name()));
					}
				}
				return str.toString();
			}
			if(parms.containsKey("IGNORE"))
				return ""+CMParms.toListString(A.getIgnored());
		}
		return "";
	}
}
