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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Expire extends StdCommand
{
	public Expire(){}

	private void unprotect(AccountStats stats)
	{
		if(stats instanceof PlayerStats)
		{
			final PlayerStats P=(PlayerStats)stats;
			final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
			if(secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
			{
				secFlags.remove(CMSecurity.SecFlag.NOEXPIRE.name());
				P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
			}
		}
		else
		if(stats instanceof PlayerAccount)
		{
			final PlayerAccount A=(PlayerAccount)stats;
			A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, false);
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.session()==null)
			return false;
		AccountStats stats = null;
		MOB M=null;
		commands.remove(0);
		if(commands.size()<1)
		{
			if(CMProps.isUsingAccountSystem())
				mob.tell(L("You must use the format EXPIRE [ACCOUNT NAME] or EXPIRE [ACCOUNT NAME] [NUMBER OF DAYS/NEVER/NOW]"));
			else
				mob.tell(L("You must use the format EXPIRE [PLAYER NAME] or EXPIRE [PLAYER NAME] [NUMBER OF DAYS/NEVER/NOW]"));
			return false;
		}
		else
		if(commands.size()==1)
		{
			final String playerName=CMStrings.capitalizeAndLower(commands.get(0));
			if(CMProps.isUsingAccountSystem())
				stats = CMLib.players().getLoadAccount(playerName);
			else
			if(CMLib.players().playerExists(playerName))
			{
				M=CMLib.players().getLoadPlayer(playerName);
				if(M!=null)
					stats = CMLib.players().getLoadPlayer(playerName).playerStats();
			}
			if(stats==null)
			{
				mob.tell(L("No player/account named '@x1' was found.",playerName));
				return false;
			}
			unprotect(stats);
			final long timeLeft=stats.getAccountExpiration()-System.currentTimeMillis();
			if(timeLeft<=0)
				mob.tell(L("Player/Account '@x1' is now expired.",playerName));
			else
				mob.tell(L("Player/Account '@x1' currently has @x2 left.",playerName,(CMLib.english().returnTime(timeLeft,0))));
			return false;
		}
		else
		{
			long days;
			final String howLong=commands.get(1);
			if(howLong.equalsIgnoreCase("never"))
				days=Long.MAX_VALUE;
			else
			if(howLong.equalsIgnoreCase("now"))
				days=0;
			else
			if(!CMath.isLong(howLong))
			{
				mob.tell(L("'@x1' is now a proper value.  Try a number of days, the word NOW or the word NEVER.",howLong));
				return false;
			}
			else
				days=CMath.s_long(howLong)*1000*60*60*24;
			final String playerName=CMStrings.capitalizeAndLower(commands.get(0));
			if(CMProps.isUsingAccountSystem())
				stats = CMLib.players().getLoadAccount(playerName);
			else
			if(CMLib.players().playerExists(playerName))
			{
				M=CMLib.players().getLoadPlayer(playerName);
				if(M!=null)
					stats = M.playerStats();
			}
			if(stats==null)
			{
				mob.tell(L("No player/account named '@x1' was found.",playerName));
				return false;
			}
			stats.setLastUpdated(System.currentTimeMillis());
			if(days==Long.MAX_VALUE)
			{
				if(stats instanceof PlayerStats)
				{
					final PlayerStats P=(PlayerStats)stats;
					final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
					if(!secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
					{
						secFlags.add(CMSecurity.SecFlag.NOEXPIRE.name());
						P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
					}
				}
				else
				if(stats instanceof PlayerAccount)
				{
					final PlayerAccount A=(PlayerAccount)stats;
					A.setFlag(PlayerAccount.AccountFlag.NOEXPIRE, true);
				}
				mob.tell(L("Player/Account '@x1' is now protected from expiration.",playerName));
			}
			else
			{
				unprotect(stats);
				stats.setAccountExpiration(days+System.currentTimeMillis());
				final long timeLeft=stats.getAccountExpiration()-System.currentTimeMillis();
				if(timeLeft<=0)
					mob.tell(L("Player/Account '@x1' is now expired.",playerName));
				else
					mob.tell(L("Player/Account '@x1' now has @x2 days left.",playerName,(CMLib.english().returnTime(timeLeft,0))));
			}
			return false;
		}
	}

	private final String[] access=I(new String[]{"EXPIRE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS);
	}

}
