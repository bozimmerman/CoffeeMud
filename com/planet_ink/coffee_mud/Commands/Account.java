package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.CMColor;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class Account extends StdCommand
{
	public Account()
	{
	}

	private final String[]	access	= I(new String[] { "ACCOUNT" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static StringBuffer showCharLong(String bgColor, MOB seer, ThinPlayer who)
	{

		final StringBuffer msg=new StringBuffer("");
		msg.append("[^w"+bgColor);
		final int[] cols={
			CMLib.lister().fixColWidth(10,seer.session()),
			CMLib.lister().fixColWidth(10,seer.session()),
			CMLib.lister().fixColWidth(5,seer.session())
		};
		final MOB pM=CMLib.players().getPlayer(who.name());
		CharClass C=(pM!=null)?pM.charStats().getCurrentClass():null;
		if(C==null)
			C=CMClass.getCharClass(who.charClass());
		if(C==null)
			C=CMClass.findCharClass(who.charClass());
		if(C==null)
		{
			final MOB mob=CMLib.players().getLoadPlayer(who.name());
			if(mob==null)
				return new StringBuffer("");
			C=mob.charStats().getCurrentClass();
		}
		
		Race R=(pM!=null)?pM.charStats().getMyRace():null;
		if(R==null)
			R=CMClass.getRace(who.race());
		if(R==null)
			R=CMClass.getRace(who.race());
		if(R==null)
		{
			final MOB mob=CMLib.players().getLoadPlayer(who.name());
			if(mob==null)
				return new StringBuffer("");
			R=mob.charStats().getMyRace();
		}
		
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			if(C.raceless())
				msg.append(CMStrings.padRight(" ",cols[0])+" ");
			else
				msg.append(CMStrings.padRight(R.name(),cols[0])+" ");
		}
		
		String levelStr=(pM!=null)?(""+pM.phyStats().level()):null;
		if(levelStr == null)
			levelStr=""+who.level();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		{
			if(R.classless())
				msg.append(CMStrings.padRight(" ",cols[1])+" ");
			else
				msg.append(CMStrings.padRight(C.name(),cols[1])+" ");
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		{
			if(C.leveless()||R.leveless())
				msg.append(CMStrings.padRight(" ",cols[2]));
			else
				msg.append(CMStrings.padRight(levelStr,cols[2]));
		}
		msg.append("^w"+bgColor+"] ^b"+bgColor + who.name()+"^N ");
		final MOB mobOn = CMLib.players().getPlayer(who.name());
		if((mobOn != null)&&(mobOn.session() != null)&&(!mobOn.session().isStopped()))
			msg.append("^y*^N");
		msg.append("\n\r");
		return msg;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuilder str=new StringBuilder();
		final PlayerAccount account;
		if(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS) && (commands.size()>1))
		{
			String name = CMStrings.capitalizeAndLower(CMParms.combine(commands,1));
			if(CMLib.players().accountExists(name))
				account = CMLib.players().getLoadAccount(name);
			else
			if(CMLib.players().playerExists(name))
			{
				final MOB M=CMLib.players().getLoadPlayer(name);
				if(M!=null)
				{
					final PlayerStats pStats = M.playerStats();
					account = pStats == null ? null : pStats.getAccount();
				}
				else
					account = null;
			}
			else
			{
				mob.tell(L("No account or player found: '@x1'",name));
				return false;
			}
		}
		else
		{
			final PlayerStats pStats = mob.playerStats();
			account = pStats == null ? null : pStats.getAccount();
		}
		
		if(account != null)
		{
			str.append("^X"+CMStrings.padRight(L("Account"), 15)+"^N: ").append(account.getAccountName()).append("\n\r");
			if(account.getAccountExpiration() > 0)
			{
				str.append("^X"+CMStrings.padRight(L("Expires"), 15)+"^N: ");
				if(System.currentTimeMillis() > account.getAccountExpiration())
					str.append(L("Expired!"));
				else
				{
					str.append(CMLib.time().date2String(account.getAccountExpiration()));
				}
				str.append("\n\r");
			}
			str.append("\n\r");
			str.append(CMStrings.padRight(L("^X@x1's characters:",account.getAccountName()),40)).append("^.^N\n\r");
			boolean toggle = false;
			for (final Enumeration<ThinPlayer> p=account.getThinPlayers(); p.hasMoreElements();)
			{
				ThinPlayer player = p.nextElement();
				str.append("^N");
				str.append(showCharLong("",mob,player));
				toggle = !toggle;
			}
			str.append("^N");
		}
		else
			str.append("?!");
		mob.tell(str.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return (CMProps.isUsingAccountSystem()) && (mob != null) && (mob.playerStats()!=null) && (mob.playerStats().getAccount() != null);
	}
}
