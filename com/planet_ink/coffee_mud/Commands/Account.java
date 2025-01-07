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
import com.planet_ink.coffee_mud.Libraries.Brown;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerSortCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2024 Bo Zimmerman

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

	public static StringBuffer showCharLong(final String bgColor, final MOB seer, final ThinPlayer who)
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
		final MOB cachedWhoM = CMLib.players().getPlayer(who.name());
		if((cachedWhoM != null)
		&&(cachedWhoM.session() != null))
		{
			final PlayerStats pStats=cachedWhoM.playerStats();
			final Session sess = cachedWhoM.session();
			if((pStats != null)
			&&(sess != null)
			&&(sess.isAfk()))
			{
				final int tells=pStats.queryTellStack(null, cachedWhoM.Name(), Long.valueOf(System.currentTimeMillis()-sess.getIdleMillis())).size();
				final int gtells=pStats.queryGTellStack(null, cachedWhoM.Name(), Long.valueOf(System.currentTimeMillis()-sess.getIdleMillis())).size();
				if((tells>0)||(gtells>0))
					msg.append(" ^T(tells)^?");
			}
		}
		if((who.email().length()>0)
		&&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0)
		&&(CMLib.database().DBCountJournalMsgsNewerThan(CMProps.getVar(CMProps.Str.MAILBOX), who.name(), 0)>0))
			msg.append(" ^H(mail)^?");
		final List<String> postalChains=new ArrayList<String>();
		PostOffice P=null;
		boolean postFound=false;
		for(final Enumeration<PostOffice> e=CMLib.city().postOffices();e.hasMoreElements();)
		{
			P=e.nextElement();
			if((P!=null)
			&&(!postalChains.contains(P.postalChain()))
			&&(!postFound))
			{
				postalChains.add(P.postalChain());
				final List<String> keys = CMLib.database().DBReadPlayerDataKeys(who.name(), P.postalChain());
				for(String key : keys)
				{
					final int x=key.indexOf(';');
					if(x<0)
						continue;
					key=key.substring(0,x);
					final PostOffice P2=CMLib.city().getPostOffice(P.postalChain(),key);
					if(P2==null)
						continue;
					msg.append(" ^r(post)^?");
					postFound=true;
					break;
				}
			}
		}
		msg.append("\n\r");
		return msg;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final StringBuilder str=new StringBuilder();
		final PlayerAccount account;
		final boolean showLastLogin;
		if(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS) && (commands.size()>1))
		{
			final String name = CMStrings.capitalizeAndLower(commands.get(1));
			if(CMLib.players().accountExists(name))
			{
				account = CMLib.players().getLoadAccount(name);
				commands.remove(1);
			}
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
				commands.remove(1);
			}
			else
			{
				mob.tell(L("No account or player found: '@x1'",name));
				return false;
			}
			if(mob.playerStats()!=null)
				showLastLogin=mob.playerStats().getAccount()!=account;
			else
				showLastLogin=false;
		}
		else
		{
			final PlayerStats pStats = mob.playerStats();
			account = pStats == null ? null : pStats.getAccount();
			showLastLogin=false;
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
			if(showLastLogin)
			{
				str.append("^X"+CMStrings.padRight(L("Last Login"), 15)+"^N: ");
				str.append(CMLib.time().date2String(account.getLastDateTime()));
				str.append("\n\r");
			}
			str.append("\n\r");
			str.append(CMStrings.padRight(L("^X@x1's characters:",account.getAccountName()),40)).append("^.^N\n\r");
			boolean toggle = false;
			final String sortByStr;
			if(commands.size()==1)
				sortByStr="NAME";
			else
			{
				sortByStr=CMParms.combine(commands,1).toUpperCase().trim();
			}
			final PlayerSortCode sortBy = CMLib.players().getCharThinSortCode(sortByStr,true);
			if(sortBy==null)
			{
				mob.tell(L("Unrecognized sort criteria: @x1",sortByStr));
				return false;
			}
			final PlayerLibrary lib = CMLib.players();
			final List<ThinPlayer> players = new XVector<ThinPlayer>(account.getThinPlayers());
			Collections.sort(players, new Comparator<PlayerLibrary.ThinPlayer>() {
				@Override
				public int compare(final ThinPlayer o1, final ThinPlayer o2)
				{
					if(o1 == null)
						return (o2 == null) ? 0 : -1;
					if(o2 == null)
						return 1;
					@SuppressWarnings("unchecked")
					final Comparable<Object> c1 = (Comparable<Object>)lib.getThinSortValue(o1, sortBy);
					@SuppressWarnings("unchecked")
					final Comparable<Object> c2 = (Comparable<Object>)lib.getThinSortValue(o2,sortBy);
					final int x= c1.compareTo(c2);
					if(x != 0)
						return x;
					return lib.getThinSortValue(o1, PlayerSortCode.NAME).toString().compareTo(lib.getThinSortValue(o2,PlayerSortCode.NAME).toString());
				}
			});

			for (final ThinPlayer player : players)
			{
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
	public boolean securityCheck(final MOB mob)
	{
		return (CMProps.isUsingAccountSystem()) && (mob != null) && (mob.playerStats()!=null) && (mob.playerStats().getAccount() != null);
	}
}
