package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder.GenMOBCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerSortCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.Stat;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerFlag;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2008-2024 Bo Zimmerman

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
public class CMPlayers extends StdLibrary implements PlayerLibrary
{
	@Override
	public String ID()
	{
		return "CMPlayers";
	}

	protected SVector<MOB> 				playersList			= new SVector<MOB>();
	protected SVector<PlayerAccount>	accountsList		= new SVector<PlayerAccount>();
	protected boolean					allAccountsLoaded	= false;
	protected CrossRefTreeMap<MOB,Room> playerLocations		= new CrossRefTreeMap<MOB,Room>(Integer.MAX_VALUE,1);
	protected long[] 					autoPurgeDaysLevels	= new long[1];
	protected long[] 					prePurgeLevels		= new long[1];
	protected int						autoPurgeHash		= 0;
	protected PlayerLibrary[]			playerLibList		= new PlayerLibrary[0];

	protected final long[]				topPrideExpiration	= new long[TimeClock.TimePeriod.values().length];
	@SuppressWarnings("unchecked")
	protected final List<Pair<String,Integer>>[][] topPlayers	 = new List[TimeClock.TimePeriod.values().length][PrideStat.values().length];
	@SuppressWarnings("unchecked")
	protected final List<Pair<String,Integer>>[][] topAccounts	 = new List[TimeClock.TimePeriod.values().length][PrideStat.values().length];
	@SuppressWarnings("unchecked")
	protected final List<Pair<String,Integer>>[][] prevTopPlayers= new List[TimeClock.TimePeriod.values().length][PrideStat.values().length];
	@SuppressWarnings("unchecked")
	protected final List<Pair<String,Integer>>[][] prevTopAccounts=new List[TimeClock.TimePeriod.values().length][PrideStat.values().length];

	protected final List<PrideCat>					prideCats	 = new Vector<PrideCat>(2);
	protected final List<Object>					prideCatData = new Vector<Object>(2);
	protected final Map<PrideCat,Map<String,List<Pair<String,Integer>>[][]>> topPlayerCats =
			new Hashtable<PrideCat,Map<String,List<Pair<String,Integer>>[][]>>();
	protected final Map<PrideCat,Map<String,List<Pair<String,Integer>>[][]>> prevTopPlayerCats =
			new Hashtable<PrideCat,Map<String,List<Pair<String,Integer>>[][]>>();

	protected final static List<Pair<String,Integer>>	emptyPride	= new ReadOnlyVector<Pair<String,Integer>>(1);
	protected final static Map<String,PlayerSortCode>	charThinMap	= new Hashtable<String,PlayerSortCode>();
	static
	{
		for(final PlayerSortCode c : PlayerSortCode.values())
		{
			charThinMap.put(c.name(), c);
			charThinMap.put(c.altName.toUpperCase().trim(), c);
		}
	}

	protected enum AcctThinSortCode
	{
		NAME,
		LAST,
		EMAIL,
		IP,
		NUMPLAYERS,
		EXPIRATION
	}

	@Override
	public void propertiesLoaded()
	{
		super.propertiesLoaded();
		prideCats.clear();
		prideCatData.clear();
		final List<String> stats = CMParms.parseCommas(CMProps.getVar(CMProps.Str.PRIDECATS), true);
		for(String stat : stats)
		{
			final int x = stat.indexOf('-');
			String rest="";
			if(x>0)
			{
				rest = stat.substring(x+1);
				stat = stat.substring(0,x);
			}
			final PrideCat cat =(PrideCat)CMath.s_valueOf(PrideCat.class, stat.toUpperCase().trim());
			if(cat == null)
				Log.errOut("Not a PRIDECAT: "+stat.toUpperCase()+": see ini file.");
			else
			{
				prideCats.add(cat);
				if(rest.length()>0)
				{
					switch(cat)
					{
					case LEVEL:
					{
						final int num = CMath.s_int(rest.trim());
						if(num > 0)
							prideCatData.add(Integer.valueOf(num));
						else
							prideCatData.add(new Object());
						break;
					}
					default:
						prideCatData.add(new Object());
						break;
					}
				}
				else
					prideCatData.add(new Object());
			}
		}
	}

	@Override
	public int numPlayers()
	{
		return playersList.size();
	}

	@Override
	public synchronized void addPlayer(final MOB newOne)
	{
		if(getPlayer(newOne.Name())!=null)
			return;
		if(playersList.contains(newOne))
			return;
		PlayerAccount acct = null;
		if(newOne.playerStats()!=null)
			acct=newOne.playerStats().getAccount();
		playersList.add(newOne);
		addAccount(acct);
	}

	@Override
	public synchronized void delPlayer(final MOB oneToDel)
	{
		if(oneToDel != null)
		{
			playersList.remove(oneToDel);
			playerLocations.removeFirst(oneToDel);
			Resources.removePersonalMap(oneToDel);
		}
	}

	@Override
	public Set<MOB> getPlayersHere(final Room room)
	{
		return playerLocations.getSecond(room);
	}

	@Override
	public void changePlayersLocation(final MOB mob, final Room room)
	{
		if(mob != null)
		{
			if(room == null)
				playerLocations.removeFirst(mob);
			else
				playerLocations.change(mob, room);
		}
	}

	@Override
	public ThinPlayer getThinPlayer(final String mobName)
	{
		final MOB M=this.getPlayer(mobName);
		if(M!=null)
			return getThinPlayer(M);
		return CMLib.database().getThinUser(mobName);
	}

	@Override
	public ThinPlayer makeThinModifiablePlayer(final Modifiable mP)
	{
		return new ThinPlayer()
		{
			final Modifiable M = mP;

			@Override
			public String name()
			{
				return M.getStat(GenMOBCode.NAME.name());
			}

			@Override
			public String charClass()
			{
				return M.getStat(GenericBuilder.GenMOBBonusFakeStats.CHARCLASS.name());
			}

			@Override
			public String race()
			{
				return M.getStat(GenMOBCode.RACE.name());
			}

			@Override
			public String gender()
			{
				return M.getStat("GENDER");
			}

			@Override
			public int level()
			{
				return CMath.s_int(M.getStat(GenMOBCode.LEVEL.name()));
			}

			@Override
			public int age()
			{
				return CMath.s_int(M.getStat("AGE"));
			}

			@Override
			public long last()
			{
				return CMath.s_long(M.getStat("LAST"));
			}

			@Override
			public String email()
			{
				return M.getStat("EMAIL");
			}

			@Override
			public String ip()
			{
				return M.getStat("IP");
			}

			@Override
			public int exp()
			{
				return CMath.s_int(M.getStat("EXPERIENCE"));
			}

			@Override
			public int expLvl()
			{
				return CMath.s_int(M.getStat("EXPERIENCENEEDED"));
			}

			@Override
			public String liege()
			{
				return M.getStat("LIEGE");
			}

			@Override
			public String worship()
			{
				return M.getStat(GenericBuilder.GenMOBBonusFakeStats.DEITY.name());
			}

			@Override
			public Enumeration<String> clans()
			{
				final String clan = M.getStat(GenericBuilder.GenMOBBonusFakeStats.CLAN.name());
				if(clan == null)
					return new EmptyEnumeration<String>();
				return new XVector<String>(clan).elements();
			}
		};
	}

	protected ThinPlayer getThinPlayer(final MOB mob)
	{
		return new ThinPlayer()
		{
			final MOB M=mob;

			@Override
			public String name()
			{
				return M.Name();
			}

			@Override
			public String charClass()
			{
				return M.baseCharStats().getCurrentClass().ID();
			}

			@Override
			public String race()
			{
				return M.baseCharStats().getMyRace().ID();
			}

			@Override
			public String gender()
			{
				return M.baseCharStats().genderName();
			}

			@Override
			public int level()
			{
				return M.basePhyStats().level();
			}

			@Override
			public int age()
			{
				return (M.playerStats()!=null)?(int)(M.getAgeMinutes()/60):0;
			}

			@Override
			public long last()
			{
				return (M.playerStats()!=null)?M.playerStats().getLastDateTime():0;
			}

			@Override
			public String email()
			{
				return (M.playerStats()!=null)?M.playerStats().getEmail():"";
			}

			@Override
			public String ip()
			{
				return (M.playerStats()!=null)?M.playerStats().getLastIP():"";
			}

			@Override
			public int exp()
			{
				return M.getExperience();
			}

			@Override
			public int expLvl()
			{
				return M.getExpNeededLevel();
			}

			@Override
			public String liege()
			{
				return M.getLiegeID();
			}

			@Override
			public String worship()
			{
				return M.baseCharStats().getWorshipCharID();
			}

			@Override
			public Enumeration<String> clans()
			{
				return new IteratorEnumeration<String>(new ConvertingIterator<Pair<Clan,Integer>,String>(M.clans().iterator(),
						new Converter<Pair<Clan,Integer>,String>()
						{
							@Override
							public String convert(final Pair<Clan, Integer> obj)
							{
								return obj.first.clanID();
							}
						}
				));
			}
		};
	}

	@Override
	public MOB getLoadPlayerByEmail(final String email)
	{
		for(final Enumeration<MOB> e=players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if((M!=null)&&(M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email)))
				return M;
		}
		for(final Enumeration<ThinPlayer> e=thinPlayers("",null);e.hasMoreElements();)
		{
			final ThinPlayer P=e.nextElement();
			if(P.email().equalsIgnoreCase(email))
				return getLoadPlayer(P.name());
		}
		return null;
	}

	@Override
	public void unloadOfflinePlayer(final MOB mob)
	{
		if((mob!=null)
		&&(!CMLib.flags().isInTheGame(mob, false))
		&&((mob.session()==null)||(mob.session().isStopped())))
		{
			final PlayerStats pStats = mob.playerStats();
			if(pStats != null)
			{
				pStats.getExtItems().delAllItems(true);
				if(getPlayer(mob.Name())==mob)
				{
					delPlayer(mob);
					mob.destroy();
				}
				else // check other hosts
				{
					for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
					{
						if(pLib2.getPlayer(mob.Name())==mob)
						{
							delPlayer(mob);
							mob.destroy();
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isSameAccount(final MOB player1, final MOB player2)
	{
		if((player1==null)||(player2==null))
			return false;
		if((player1.playerStats()==null)||(player2.playerStats()==null))
			return false;
		return player1.playerStats().getAccount()==player2.playerStats().getAccount();
	}

	@Override
	public boolean isSameAccountIP(final MOB player1, final MOB player2)
	{
		if((player1==null)||(player2==null))
			return false;
		if((player1.playerStats()==null)||(player2.playerStats()==null))
			return false;
		if(player1.playerStats().getAccount()==player2.playerStats().getAccount())
			return true;
		if((player1.session()==null)||(player2.session()==null))
			return false;
		if(player1.session().getAddress().equals(player2.session().getAddress()))
			return true;
		return false;
	}

	@Override
	public PlayerAccount getLoadAccount(final String calledThis)
	{
		PlayerAccount A = getAccount(calledThis);
		if(A!=null)
			return A;
		if(allAccountsLoaded)
			return null;
		A=CMLib.database().DBReadAccount(calledThis);
		if(A!=null)
			addAccount(A);
		return A;
	}

	@Override
	public synchronized void addAccount(final PlayerAccount acct)
	{
		if(acct==null)
			return;
		if(accountsList.contains(acct))
			return;
		for(final PlayerAccount A : accountsList) // dont consolodate this.
		{
			if(A.getAccountName().equals(acct.getAccountName()))
				return;
		}
		accountsList.add(acct);
	}

	@Override
	public PlayerAccount getLoadAccountByEmail(final String email)
	{
		if(!CMProps.isUsingAccountSystem())
			return null;
		for(final Enumeration<PlayerAccount> e=accounts();e.hasMoreElements();)
		{
			final PlayerAccount P=e.nextElement();
			if(P.getEmail().equalsIgnoreCase(email))
				return P;
		}
		for(final Enumeration<PlayerAccount> e=accounts("",null);e.hasMoreElements();)
		{
			final PlayerAccount P=e.nextElement();
			if(P.getEmail().equalsIgnoreCase(email))
				return P;
		}
		return null;
	}

	@Override
	public PlayerAccount getAccount(String calledThis)
	{
		calledThis=CMStrings.capitalizeAndLower(calledThis);
		for(final PlayerAccount A : accountsList)
		{
			if(A.getAccountName().equals(calledThis))
				return A;
		}

		for (final MOB M : playersList)
		{
			if((M.playerStats()!=null)
			&&(M.playerStats().getAccount()!=null)
			&&(M.playerStats().getAccount().getAccountName().equals(calledThis)))
			{
				addAccount(M.playerStats().getAccount());
				return M.playerStats().getAccount();
			}
		}
		return null;
	}

	protected PlayerLibrary[] getOtherPlayerLibAllHosts()
	{
		if(this.playerLibList.length>0)
			return this.playerLibList;
		final List<PlayerLibrary> list=new ArrayList<PlayerLibrary>();
		list.add(this);
		final WorldMap map=CMLib.map();
		for(final Enumeration<CMLibrary> pl=CMLib.libraries(CMLib.Library.PLAYERS); pl.hasMoreElements(); )
		{
			final PlayerLibrary pLib2 = (PlayerLibrary)pl.nextElement();
			if((pLib2 != null)
			&&(!list.contains(pLib2))
			&&(map == CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib2), Library.MAP)))
				list.add(pLib2);
		}
		this.playerLibList = list.toArray(this.playerLibList);
		return this.playerLibList;
	}

	@Override
	public PlayerAccount getAccountAllHosts(final String calledThis)
	{
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			final PlayerAccount pA2=pLib2.getAccount(calledThis);
			if(pA2!=null)
				return pA2;
		}
		return null;
	}

	@Override
	public List<String> getPlayerLists()
	{
		return CMLib.database().getUserList();
	}

	@Override
	public List<String> getPlayerListsAllHosts()
	{
		final List<String> finalList = new Vector<String>();
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			finalList.addAll(pLib2.getPlayerLists());
		}
		return finalList;
	}

	@Override
	public MOB getPlayer(String calledThis)
	{
		calledThis=CMStrings.capitalizeAndLower(calledThis);
		for (final MOB M : playersList)
		{
			if (M.Name().equals(calledThis))
				return M;
		}
		return null;
	}

	@Override
	public MOB getPlayerAllHosts(final String calledThis)
	{
		MOB M = getPlayer(calledThis);
		if(M!=null)
			return M;
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			M=pLib2.getPlayer(calledThis);
			if(M!=null)
				return M;
		}
		return null;
	}

	@Override
	public int getPlayerThreadId(final MOB mob)
	{
		if((mob==null)||(!mob.isPlayer()))
			return -1;
		final Session sess = mob.session();
		if(sess != null)
			return sess.getGroupID();
		if(this.isLoadedPlayer(mob))
			return CMLib.getLibraryThreadID(this);
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			if((pLib2 != this)
			&&(pLib2.isLoadedPlayer(mob)))
				return CMLib.getLibraryThreadID(pLib2);
		}
		return -1;
	}

	@Override
	public MOB getLoadPlayerAllHosts(final String last)
	{
		final MOB M = getLoadPlayer(last);
		if(M!=null)
			return M;
		final Set<DatabaseEngine> dbset=new HashSet<DatabaseEngine>();
		dbset.add(CMLib.database());
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			if(pLib2 != this)
			{
				final MOB chkM = pLib2.getPlayer(last);
				if(chkM != null)
					return chkM;
				final char threadId = CMLib.getLibraryThreadID(pLib2);
				final DatabaseEngine otherEngine = CMLib.get(threadId)._database();
				if((!dbset.contains(otherEngine))
				&&(otherEngine.DBUserSearch(last)!=null))
				{
					dbset.add(otherEngine);
					final MOB[] outerMob = new MOB[1];
					CMLib.threads().executeRunnable(threadId, new Runnable() {
						final MOB[] innerMob = outerMob;
						final String name = last;
						@Override
						public void run()
						{
							innerMob[0] = pLib2.getLoadPlayer(name);
						}

					});
					if(outerMob[0]!=null)
						return outerMob[0];
				}
			}
		}
		return null;
	}

	@Override
	public MOB getLoadPlayer(final String last)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return null;
		MOB M=getPlayer(last);
		if(M!=null)
			return M;
		if(playerExists(last))
		{
			M=CMLib.database().DBReadPlayer(CMStrings.capitalizeAndLower(last));
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setLastUpdated(M.playerStats().getLastDateTime());
			M.recoverPhyStats();
			M.recoverCharStats();
			final Race R=M.baseCharStats().getMyRace();
			if(R.isGeneric())
				CMLib.database().DBUpdateRaceCreationDate(R.ID());
			Ability A=null;
			for(int a=0;a<M.numAbilities();a++)
			{
				A=M.fetchAbility(a);
				if(A!=null)
					A.autoInvocation(M, false);
			}
			M.recoverMaxState();
			M.resetToMaxState(); // how did we get along w/o this?!
		}
		return M;
	}

	@Override
	public String getLiegeOfUserAllHosts(String userName)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return "";
		if((userName==null)
		||(userName.trim().length()==0))
			return "";
		userName=CMStrings.capitalizeAndLower(userName);
		final MOB M=getPlayerAllHosts(userName);
		if(M!=null)
			return M.getLiegeID();
		else
		if(playerExistsAllHosts(userName))
		{
			final String tryLeige=CMLib.database().DBLeigeSearch(userName);
			if(tryLeige!=null)
				return tryLeige;
			for(final PlayerLibrary pLib : getOtherPlayerLibAllHosts())
			{
				final DatabaseEngine db = (DatabaseEngine)CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib), Library.DATABASE);
				if(db != CMLib.database())
				{
					final String tryLeige2=db.DBLeigeSearch(userName);
					if(tryLeige2!=null)
						return tryLeige2;

				}
			}
		}
		return "";
	}

	@Override
	public boolean accountExists(String name)
	{
		if(name==null)
			return false;
		name=CMStrings.capitalizeAndLower(name);
		return getLoadAccount(name)!=null;
	}

	@Override
	public boolean accountExistsAllHosts(final String name)
	{
		if(name==null)
			return false;
		for(final PlayerLibrary pLib2 : getOtherPlayerLibAllHosts())
		{
			if(pLib2.accountExists(name))
				return true;
		}
		return false;
	}

	@Override
	public boolean isLoadedPlayer(final MOB M)
	{
		return playersList.contains(M);
	}

	@Override
	public boolean isLoadedPlayer(String mobName)
	{
		if(mobName==null)
			return false;
		mobName=CMStrings.capitalizeAndLower(mobName);
		for(final MOB M: playersList)
		{
			if(M.Name().equals(mobName))
				return true;
		}
		return false;
	}

	@Override
	public boolean playerExists(String name)
	{
		if(name==null)
			return false;
		name=CMStrings.capitalizeAndLower(name);
		for(final MOB M: playersList)
		{
			if(M.Name().equals(name))
				return true;
		}
		return CMLib.database().DBUserSearch(name)!=null;
	}

	@Override
	public boolean playerExistsAllHosts(String name)
	{
		if(name==null)
			return false;
		name=CMStrings.capitalizeAndLower(name);
		if(playerExists(name))
			return true;
		for(final PlayerLibrary pLib : getOtherPlayerLibAllHosts())
		{
			if(pLib != this)
			{
				for(final Enumeration<MOB> m=pLib.players();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if(M.Name().equals(name))
						return true;
				}
				final DatabaseEngine db = (DatabaseEngine)CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib), Library.DATABASE);
				if((db != CMLib.database()) && (db.DBUserSearch(name)!=null))
					return true;
			}
		}
		return false;
	}

	@Override
	public Enumeration<MOB> players()
	{
		return playersList.elements();
	}

	@Override
	public Enumeration<PlayerAccount> accounts()
	{
		return accountsList.elements();
	}

	@Override
	public List<Pair<String,Integer>> getTopPridePlayers(final TimeClock.TimePeriod period, final PrideStat stat)
	{
		List<Pair<String,Integer>> top=topPlayers[period.ordinal()][stat.ordinal()];
		if(top == null)
			top=emptyPride;
		return top;
	}

	@Override
	public List<Pair<String,Integer>> getPreviousTopPridePlayers(final TimeClock.TimePeriod period, final PrideStat stat)
	{
		List<Pair<String,Integer>> top=prevTopPlayers[period.ordinal()][stat.ordinal()];
		if(top == null)
			top=emptyPride;
		return top;
	}

	@Override
	public List<Pair<String,Integer>> getTopPridePlayers(final PrideCat category, final String catUnit, final TimeClock.TimePeriod period, final PrideStat stat)
	{
		if((!prideCats.contains(category))||(catUnit==null)||(catUnit.length()==0))
			return emptyPride;

		final Map<String,List<Pair<String,Integer>>[][]> topUnits=topPlayerCats.get(category);
		if(topUnits == null)
			return emptyPride;
		final List<Pair<String,Integer>>[][] topPrides = topUnits.get(catUnit.toUpperCase().trim());
		if(topPrides == null)
			return emptyPride;
		final List<Pair<String,Integer>> top=topPrides[period.ordinal()][stat.ordinal()];
		if(top == null)
			return emptyPride;
		return top;
	}

	@Override
	public List<Pair<String,Integer>> getPreviousTopPridePlayers(final PrideCat category, final String catUnit, final TimeClock.TimePeriod period, final PrideStat stat)
	{
		if((!prideCats.contains(category))||(catUnit==null)||(catUnit.length()==0))
			return emptyPride;

		final Map<String,List<Pair<String,Integer>>[][]> topUnits=prevTopPlayerCats.get(category);
		if(topUnits == null)
			return emptyPride;
		final List<Pair<String,Integer>>[][] topPrides = topUnits.get(catUnit.toUpperCase().trim());
		if(topPrides == null)
			return emptyPride;
		final List<Pair<String,Integer>> top=topPrides[period.ordinal()][stat.ordinal()];
		if(top == null)
			return emptyPride;
		return top;
	}

	@Override
	public List<Pair<String,Integer>> getTopPrideAccounts(final TimeClock.TimePeriod period, final PrideStat stat)
	{
		List<Pair<String,Integer>> top=topAccounts[period.ordinal()][stat.ordinal()];
		if(top == null)
			top=emptyPride;
		return top;
	}

	@Override
	public List<Pair<String,Integer>> getPreviousTopPrideAccounts(final TimeClock.TimePeriod period, final PrideStat stat)
	{
		List<Pair<String,Integer>> top=prevTopAccounts[period.ordinal()][stat.ordinal()];
		if(top == null)
			top=emptyPride;
		return top;
	}

	private void removePrideStat(final List<Pair<String,Integer>> top, final String name, final int start)
	{
		for(int i=start;i<top.size();i++)
		{
			if(top.get(i).first.equals(name))
			{
				top.remove(i);
				break;
			}
		}
	}

	@Override
	public int bumpPrideStat(final MOB mob, final PrideStat stat, final int amt)
	{
		if((amt != 0)&&(mob!=null))
		{
			if(mob.session() != null)
			{
				final PlayerLibrary lib=CMLib.get(mob.session())._players();
				if(lib != this)
					return lib.bumpPrideStat(mob, stat, amt);
			}
			final PlayerStats pstats=mob.playerStats();
			if(pstats != null)
			{
				if(!pstats.isSet(PlayerFlag.NOSTATS))
					pstats.bumpPrideStat(stat, amt);
				if(!pstats.isSet(PlayerFlag.NOTOP))
				{
					adjustTopPrideStats(topPlayers,mob.Name(),true,"",stat,pstats);
					for(int p=0;p<prideCats.size();p++)
					{
						final PrideCat pcat = prideCats.get(p);
						final Object prideObj = prideCatData.get(p);
						final Map<String,List<Pair<String,Integer>>[][]> topPlayerCat;
						if(topPlayerCats.containsKey(pcat))
							topPlayerCat = topPlayerCats.get(pcat);
						else
						{
							topPlayerCat=new Hashtable<String,List<Pair<String,Integer>>[][]>();
							topPlayerCats.put(pcat, topPlayerCat);
						}
						String catUnit = getPrideCatValue(mob,pcat,prideObj);
						if(catUnit == null)
							continue;
						catUnit = catUnit.toUpperCase().trim();
						final List<Pair<String,Integer>>[][] topPlayerChart;
						if(topPlayerCat.containsKey(catUnit))
							topPlayerChart = topPlayerCat.get(catUnit);
						else
						{
							topPlayerChart = newPrideStatTopChart();
							topPlayerCat.put(catUnit, topPlayerChart);
						}
						adjustTopPrideStats(topPlayerChart,mob.Name(),true, pcat.name(), stat,pstats);
					}
				}
				final PlayerAccount acct=pstats.getAccount();
				if(acct != null)
				{
					if(!acct.isSet(AccountFlag.NOSTATS))
						pstats.getAccount().bumpPrideStat(stat,amt);
					if(!acct.isSet(AccountFlag.NOTOP))
						adjustTopPrideStats(topAccounts,pstats.getAccount().getAccountName(),false,"",stat,pstats.getAccount());
				}
				return amt;
			}
		}
		return 0;
	}

	protected void deleteFakePrideStatPermanently(final Pair<String,Integer> entry,
			final PrideStat stat, final String catUnit, final boolean player)
	{
		if((entry.first!=null)
		&&(entry.first.indexOf(' ')>0))
		{
			final String subKey = (catUnit + "/" + stat.ordinal() + "/" + entry.second).toUpperCase();
			final String key = entry.first+"/"+subKey.hashCode();
			if(player)
				CMLib.database().DBDeletePlayerData(entry.first, "SYSTEM_PRIDE_PARCHIVE", key);
			else
				CMLib.database().DBDeletePlayerData(entry.first, "SYSTEM_PRIDE_AARCHIVE", key);
		}
	}

	/**
	 * Called during every bump to potentially bump a top 10 pride stat final value
	 * based on some recent change.
	 *
	 * @param topWhat the real top stat charts for all periods
	 * @param name the user who bumped
	 * @param player true if this is a player entry, false for account
	 * @param subKey "", or the pride stat category in question
	 * @param stat the pridestat that bumped for the user
	 * @param pstats account OR player stats object, to get data from
	 */
	protected void adjustTopPrideStats(final List<Pair<String,Integer>>[][] topWhat, final String name,
			final boolean player, final String subKey, final PrideStat stat, final PrideStats pstats)
	{
		final int prideTopSize = CMProps.getIntVar(CMProps.Int.PRIDECOUNT);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			final List<Pair<String,Integer>> top=topWhat[period.ordinal()][stat.ordinal()];
			if(top == null)
				continue;
			synchronized(top)
			{
				final int pVal=pstats.getPrideStat(period, stat);
				if(pVal <= 0)
					removePrideStat(top,name,0);
				else
				{
					boolean found=false;
					for(int i=0;i<top.size();i++)
					{
						if(top.get(i).first.equals(name))
						{
							found=true; // true replacement
							top.get(i).second=Integer.valueOf(pVal);
							break;
						}
						else
						if(pVal > top.get(i).second.intValue())
						{
							// true addition, pushing others down
							top.add(i,new Pair<String,Integer>(name,Integer.valueOf(pVal)));
							// if *this* user ALSO had a lower record, remove it
							removePrideStat(top,name,i+1);
							//this might push a fake entry off the top topPrideStats
							if(top.size()>prideTopSize)
								deleteFakePrideStatPermanently(top.get(prideTopSize), stat, subKey, player);
							found=true;
							break;
						}
					}
					if((!found)&&(top.size()<prideTopSize))
						top.add(new Pair<String,Integer>(name,Integer.valueOf(pVal)));
				}
			}
		}
	}

	@Override
	public void renamePlayer(final MOB mob, final String oldName)
	{
		final String newName = mob.Name();
		CMLib.database().DBPlayerNameChange(oldName, newName);
		for(final Enumeration<MOB> p=CMLib.players().players();p.hasMoreElements();)
		{
			final MOB playerM=p.nextElement();
			if(playerM.baseCharStats().getWorshipCharID().equalsIgnoreCase(oldName))
				playerM.baseCharStats().setWorshipCharID(newName);
			if(playerM.getLiegeID().equalsIgnoreCase(oldName))
				playerM.setLiegeID(newName);
		}
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			final Quest Q=CMLib.quests().fetchQuest(q);
			if(Q.wasWinner(oldName))
			{
				Q.declareWinner("-"+oldName);
				Q.declareWinner(newName);
			}
		}
		final PlayerStats pstats = mob.playerStats();
		if(pstats!=null)
		{
			final PlayerAccount account = pstats.getAccount();
			if(account != null)
			{
				account.delPlayer(oldName);
				account.addNewPlayer(mob);
				CMLib.database().DBUpdateAccount(account);
				account.setLastUpdated(System.currentTimeMillis());
			}
			for(final Enumeration<Item> e=pstats.getExtItems().items(); e.hasMoreElements();)
			{
				final Item I=e.nextElement();
				if(I instanceof PrivateProperty)
				{
					if(((PrivateProperty)I).getOwnerName().equalsIgnoreCase(oldName))
						((PrivateProperty)I).setOwnerName(newName);
				}
			}
		}

		for(final Enumeration<Room> r=CMLib.map().roomsFilled();r.hasMoreElements();)
		{
			Room R=r.nextElement();
			if((R!=null)&&(R.roomID().length()>0))
			{
				synchronized(CMClass.getSync("SYNC"+R.roomID()))
				{
					R=CMLib.map().getRoom(R);
					boolean changed=false;
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A==null)
							continue;
						if(A instanceof LandTitle)
						{
							if(((LandTitle)A).getOwnerName().equals(oldName))
							{
								((LandTitle)A).setOwnerName(newName);
								changed=true;
							}
						}
						else
						if(A.text().equals(oldName))
						{
							changed=true;
							A.setMiscText(newName);
						}
					}
					if(changed)
					{
						CMLib.database().DBUpdateRoom(R);
					}
				}
			}
		}
	}

	protected List<Quad<PrideCat,String,PrideStat,Integer>> findPrideStatEntries(
			final PrideCat pcat, final String pcatUnit, final String userId, final List<Pair<String,Integer>>[][] top)
	{
		final List<Quad<PrideCat,String,PrideStat,Integer>> found = new ArrayList<Quad<PrideCat,String,PrideStat,Integer>>(1);
		final int x = TimeClock.TimePeriod.ALLTIME.ordinal();
		if(top[x] != null)
		{
			for(int y=0;y<top[x].length;y++) // iterate over the pride Stat codes
			{
				if(top[x][y] != null)
				{
					for(final Pair<String,Integer> p : top[x][y])
					{
						if((p != null)
						&&(userId.equalsIgnoreCase(p.first)))
						{
							final PrideStat stat = PrideStat.values()[y];
							found.add(new Quad<PrideCat,String,PrideStat,Integer>(pcat,pcatUnit,stat,p.second));
						}
					}
				}
			}
		}
		return found;
	}

	protected String getRecordXML(final String catName, final List<Pair<String,Integer>>[][] cat)
	{
		final StringBuilder str = new StringBuilder("");
		str.append("<STAT CAT=\""+catName+"\">");
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period == TimePeriod.ALLTIME)
				continue;
			str.append("<PERIOD TYPE=\""+period.name()+"\">");
			for(final PrideStat pstat : PrideStat.values())
			{
				final List<Pair<String,Integer>> stats = cat[period.ordinal()][pstat.ordinal()];
				if(stats != null)
				{
					str.append("<STAT TYPE=\""+pstat.name()+"\">");
					for(int i=0;i<10;i++)
					{
						if((i<stats.size())&&(stats.get(i).first.trim().length()>0))
							str.append("<TOP NAME=\""+stats.get(i).first+"\">"+stats.get(i).second.toString()+"</TOP>");
					}
					str.append("</STAT>");
				}
			}
			str.append("</PERIOD>");
		}
		str.append("</STAT>");
		return str.toString();
	}

	protected void putRecordXML(final List<Pair<String,Integer>>[][] tops, final XMLLibrary.XMLTag statRoot)
	{
		for(final XMLLibrary.XMLTag perTag : statRoot.contents())
		{
			final TimePeriod period = (TimePeriod)CMath.s_valueOf(TimePeriod.class, perTag.getParmValue("TYPE"));
			if(period != null)
			{
				for(final XMLLibrary.XMLTag statTag : perTag.contents())
				{
					final PrideStat pride = (PrideStat)CMath.s_valueOf(PrideStat.class, statTag.getParmValue("TYPE"));
					if(pride != null)
					{
						tops[period.ordinal()][pride.ordinal()] = new XVector<Pair<String,Integer>>();
						for(final XMLLibrary.XMLTag valTag : statTag.contents())
						{
							final String name = valTag.getParmValue("NAME");
							final Integer value = Integer.valueOf(CMath.s_int(valTag.value()));
							tops[period.ordinal()][pride.ordinal()].add(new Pair<String,Integer>(name,value));
						}
					}
				}
			}
		}
	}

	protected void loadPreviousPrideRecords()
	{
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period == TimePeriod.ALLTIME)
				continue;
			for(final PrideStat pstat : PrideStat.values())
			{
				clearTopStatChart(prevTopPlayers[period.ordinal()][pstat.ordinal()]);
				clearTopStatChart(prevTopAccounts[period.ordinal()][pstat.ordinal()]);
			}
		}
		prevTopPlayerCats.clear();
		final String filename = "::/resources/sys_reports/"+name()+"_prev_top_records.xml";
		final CMFile F=new CMFile(filename, null);
		if(!F.exists())
			return;
		final List<XMLLibrary.XMLTag> all = CMLib.xml().parseAllXML(F.text());
		final XMLLibrary.XMLTag statsTag = CMLib.xml().getPieceFromPieces(all, "STATS");
		if(statsTag != null)
		{
			for(final XMLLibrary.XMLTag statTag : statsTag.contents())
			{
				if(statTag.tag().equals("STAT"))
				{
					final String type = statTag.getParmValue("CAT");
					if(type == null)
						continue;
					if(type.equals("PLAYERS"))
						putRecordXML(prevTopPlayers, statTag);
					else
					if(type.equals("ACCOUNTS"))
						putRecordXML(prevTopAccounts, statTag);
				}
				else
				if(statTag.tag().equals("PRIDECAT"))
				{
					final PrideCat catType = (PrideCat)CMath.s_valueOf(PrideCat.class, statTag.getParmValue("TYPE"));
					if(catType != null)
					{
						final Map<String,List<Pair<String,Integer>>[][]> catNames = new Hashtable<String,List<Pair<String,Integer>>[][]>();
						prevTopPlayerCats.put(catType, catNames);
						for(final XMLLibrary.XMLTag catTag : statTag.contents())
						{
							final String catName = catTag.getParmValue("CAT");
							if(catName == null)
								continue;
							catNames.put(catName, newPrideStatTopChart());
							putRecordXML(catNames.get(catName), catTag);
						}
					}
				}
			}
		}
	}

	protected void savePreviousPrideRecords()
	{
		final StringBuilder str = new StringBuilder("");

		str.append("<STATS>");
		str.append(getRecordXML("PLAYERS", prevTopPlayers));
		str.append(getRecordXML("ACCOUNTS", prevTopAccounts));
		for(final PrideCat catType : PrideCat.values())
		{
			if(prevTopPlayerCats.containsKey(catType))
			{
				str.append("<PRIDECAT TYPE=\""+catType.name()+"\">");
				final Map<String,List<Pair<String,Integer>>[][]> catNames = prevTopPlayerCats.get(catType);
				for(final String catName : catNames.keySet())
				{
					final List<Pair<String,Integer>>[][] cat = catNames.get(catName);
					str.append(getRecordXML(catName,cat));
				}
				str.append("</PRIDECAT>");
			}
		}
		str.append("</STATS>");
		final String dir="::/resources/sys_reports/";
		final CMFile dirF=new CMFile(dir, null);
		if(!dirF.exists())
			dirF.mkdir();
		final String filename = "::/resources/sys_reports/"+name()+"_prev_top_records.xml";
		final CMFile F=new CMFile(filename, null);
		F.saveText(str.toString());
	}

	protected void preservePrideRecords(final MOB deadMOB)
	{
		final List<Quad<PrideCat,String,PrideStat,Integer>> records = new ArrayList<Quad<PrideCat,String,PrideStat,Integer>>();
		records.addAll(findPrideStatEntries(PrideCat.PLAYER, "", deadMOB.Name(), topPlayers));
		String accountName = "";
		if((deadMOB.playerStats()!=null)
		&&(deadMOB.playerStats().getAccount() != null))
		{
			accountName = deadMOB.playerStats().getAccount().getAccountName();
			records.addAll(findPrideStatEntries(PrideCat.ACCOUNT, "", accountName, topAccounts));
		}
		for(final PrideCat pcat : prideCats)
		{
			final Map<String,List<Pair<String,Integer>>[][]> subCat = this.topPlayerCats.get(pcat);
			if(subCat != null)
			{
				for(final String subCatUnit : subCat.keySet())
					records.addAll(findPrideStatEntries(pcat, subCatUnit, deadMOB.Name(), subCat.get(subCatUnit)));
			}
		}
		final Calendar C = Calendar.getInstance();
		final String nameSuffix = " " + C.get(Calendar.YEAR) + "/" + C.get(Calendar.DAY_OF_YEAR);
		for(final Quad<PrideCat,String,PrideStat,Integer> preservedRec : records)
		{
			String nameToUse;
			final String section;
			if(preservedRec.first == PrideCat.ACCOUNT)
			{
				nameToUse = CMStrings.limit(accountName + nameSuffix,100);
				section = "SYSTEM_PRIDE_AARCHIVE";
			}
			else
			{
				section = "SYSTEM_PRIDE_PARCHIVE";
				nameToUse = CMStrings.limit(deadMOB.Name() + nameSuffix,100);
			}
			final PrideCat pcat = preservedRec.first;
			final PrideStat pstat = preservedRec.third;
			final Integer value = preservedRec.fourth;
			final String subKey = (preservedRec.second + "/" + pstat.ordinal() + "/" + value.toString()).toUpperCase();
			final String key = nameToUse+"/"+subKey.hashCode();
			final String xml =
				"<PCAT>"+pcat.name()+"</PCAT>"
			  + "<PUNIT>" + CMLib.xml().parseOutAngleBrackets(preservedRec.second)+"</PUNIT>"
			  + "<PSTAT>" + pstat.name()+"</PSTAT>"
			  + "<PVALUE>" + value.toString()+"</PVALUE>";
			CMLib.database().DBReCreatePlayerData(nameToUse, section, key, xml);
		}
	}

	@Override
	public void obliteratePlayer(MOB deadMOB, final boolean deleteAssets, final boolean quiet)
	{
		if(deadMOB==null)
			return;
		if(getPlayer(deadMOB.Name())!=null)
		{
			deadMOB=getPlayer(deadMOB.Name());
			delPlayer(deadMOB);
		}
		CMLib.journals().unsubscribeFromAll(deadMOB.Name());
		for(final Session S : CMLib.sessions().allIterable())
		{
			if((!S.isStopped())&&(S.mob()!=null)&&(S.mob().Name().equals(deadMOB.Name())))
				deadMOB=S.mob();
		}
		if(deadMOB.playerStats()!=null)
		{
			final PlayerAccount A=deadMOB.playerStats().getAccount();
			if(A!=null)
				A.delPlayer(deadMOB);
		}
		final Room deadLoc=deadMOB.location();
		final CMMsg msg=CMClass.getMsg(deadMOB,null,CMMsg.MSG_RETIRE,(quiet)?null:L("A horrible death cry is heard throughout the land."));
		if(deleteAssets)
		{
			if(deadLoc!=null)
				deadLoc.send(deadMOB,msg);

		}
		final Session session = deadMOB.session();
		if((session!=null)&&(!session.isStopped()))
		{
			session.logout(true);
			if(session!=null)
				session.stopSession(false,false,false);
		}
		if(deleteAssets)
		{
			try
			{
				for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null)&&(R!=deadLoc))
					{
						if(R.okMessage(deadMOB,msg))
							R.sendOthers(deadMOB,msg);
						else
						{
							addPlayer(deadMOB);
							// your session is still gone, but at least you are not
							return;
						}
					}
				}
			}
			catch(final NoSuchElementException e)
			{
			}
		}
		final StringBuffer newNoPurge=new StringBuffer("");
		final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		boolean somethingDone=false;
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				final String B=protectedOnes.get(b);
				if(!B.equalsIgnoreCase(deadMOB.name()))
					newNoPurge.append(B+"\n");
				else
					somethingDone=true;
			}
			if(somethingDone)
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
		}

		final PlayerStats pStats = deadMOB.playerStats();
		if(pStats != null)
			pStats.getExtItems().delAllItems(true);
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.PLAYERPURGES, deadMOB);
		if(channels.size()>0)
		{
			String name=deadMOB.Name();
			if((pStats != null)
			&&(pStats.getAccount()!=null))
				name+=" ("+pStats.getAccount().getAccountName()+")";
			final String msgStr = CMLib.lang().fullSessionTranslation("@x1 has just been deleted.",name);
			for(int i=0;i<channels.size();i++)
			{
				CMLib.commands().postChannel(channels.get(i),deadMOB.clans(),msgStr,true);
			}
		}
		CMLib.coffeeTables().bump(deadMOB,CoffeeTableRow.STAT_PURGES);

		CMLib.database().DBDeletePlayerOnly(deadMOB.Name());
		preservePrideRecords(deadMOB);
		deadMOB.delAllItems(false);
		for(int i=0;i<deadMOB.numItems();i++)
		{
			final Item I=deadMOB.getItem(i);
			if(I!=null)
				I.setContainer(null);
		}
		deadMOB.delAllItems(false);
		CMLib.database().DBUpdatePlayerItems(deadMOB);
		final int numFollowers=deadMOB.numFollowers();
		for(int f=0;f<numFollowers;f++)
		{
			final MOB follower=deadMOB.fetchFollower(0);
			if(follower!=null)
				follower.setFollowing(null);
		}
		if(deleteAssets)
		{
			CMLib.database().DBUpdateFollowers(deadMOB);
		}
		deadMOB.delAllAbilities();
		deadMOB.delAllEffects(false);
		deadMOB.delAllBehaviors(); // this can happen too
		CMLib.database().DBUpdatePlayerAbilities(deadMOB);
		if(deleteAssets)
		{
			CMLib.database().DBDeletePlayerPrivateJournalEntries(deadMOB.Name());
			CMLib.database().DBDeleteAllPlayerData(deadMOB.Name());
		}
		final PlayerStats pstats = deadMOB.playerStats();
		if(pstats!=null)
		{
			final PlayerAccount account = pstats.getAccount();
			if(account != null)
			{
				account.delPlayer(deadMOB);
				CMLib.database().DBUpdateAccount(account);
				account.setLastUpdated(System.currentTimeMillis());
			}
		}
		if(deleteAssets)
		{
			for(int q=0;q<CMLib.quests().numQuests();q++)
			{
				final Quest Q=CMLib.quests().fetchQuest(q);
				if(Q.wasWinner(deadMOB.Name()))
					Q.declareWinner("-"+deadMOB.Name());
			}
		}

		Log.sysOut(deadMOB.name()+" has been deleted.");
		deadMOB.destroy();
	}

	@Override
	public synchronized void obliterateAccountOnly(PlayerAccount deadAccount)
	{
		deadAccount = getLoadAccount(deadAccount.getAccountName());
		if(deadAccount==null)
			return;
		accountsList.remove(deadAccount);

		final StringBuffer newNoPurge=new StringBuffer("");
		final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		boolean somethingDone=false;
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				final String B=protectedOnes.get(b);
				if(!B.equalsIgnoreCase(deadAccount.getAccountName()))
					newNoPurge.append(B+"\n");
				else
					somethingDone=true;
			}
			if(somethingDone)
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
		}

		CMLib.journals().unsubscribeFromAll(deadAccount.getAccountName());
		CMLib.database().DBDeleteAccount(deadAccount);
		Log.sysOut(deadAccount.getAccountName()+" has been deleted.");
	}

	@Override
	public boolean savePlayer(final MOB mob)
	{
		final int threadId = CMLib.getLibraryThreadID(Library.PLAYERS, this);
		final CMLib lib=CMLib.get(threadId);
		return savePlayer(mob, lib, false, true) == 1;
	}

	protected int savePlayer(final MOB mob, final CMLib lib, final boolean noCachePlayers, final boolean forceSave)
	{
		try
		{
			if(mob.amDestroyed())
				return 0;
			final PlayerStats pStats=mob.playerStats();
			if(pStats == null)
				return 0;
			if((!mob.isMonster()) && (pStats.isSavable())) // if they are presently online, because session
			{
				lib._factions().updatePlayerFactions(mob,mob.location(), false);
				//setThreadStatus(serviceClient,"just saving "+mob.Name());
				lib._database().DBUpdatePlayerMOBOnly(mob);
				if(mob.Name().length()==0)
					return 0;
				//setThreadStatus(serviceClient,"saving "+mob.Name()+", "+mob.numItems()+" items");
				lib._database().DBUpdatePlayerItems(mob);
				//setThreadStatus(serviceClient,"saving "+mob.Name()+", "+mob.numAbilities()+" abilities");
				lib._database().DBUpdatePlayerAbilities(mob);
				//setThreadStatus(serviceClient,"saving "+mob.numFollowers()+" followers of "+mob.Name());
				lib._database().DBUpdateFollowers(mob);
				final PlayerAccount account = pStats.getAccount();
				pStats.setLastUpdated(System.currentTimeMillis());
				if(account!=null)
				{
					//setThreadStatus(serviceClient,"saving account "+account.getAccountName()+" for "+mob.Name());
					lib._database().DBUpdateAccount(account);
					account.setLastUpdated(System.currentTimeMillis());
				}
			}
			else
			if(pStats.isSavable())
			{
				if((pStats.getLastUpdated()==0)
				||(pStats.getLastUpdated()<pStats.getLastDateTime())
				||(noCachePlayers && (!lib._flags().isInTheGame(mob, true)))
				||(forceSave))
				{
					if(noCachePlayers && (!lib._flags().isInTheGame(mob, true)))
						mob.delAllEffects(true);
					//setThreadStatus(serviceClient,"just saving "+mob.Name());
					lib._database().DBUpdatePlayerMOBOnly(mob);
					if(mob.Name().length()==0)
						return 0;
					//setThreadStatus(serviceClient,"just saving "+mob.Name()+", "+mob.numItems()+" items");
					lib._database().DBUpdatePlayerItems(mob);
					//setThreadStatus(serviceClient,"just saving "+mob.Name()+", "+mob.numAbilities()+" abilities");
					lib._database().DBUpdatePlayerAbilities(mob);
					pStats.setLastUpdated(System.currentTimeMillis());
				}
				if(noCachePlayers && (!lib._flags().isInTheGame(mob, true)))
				{
					if(pStats != null)
						pStats.getExtItems().delAllItems(true);
					delPlayer(mob);
					mob.destroy();
				}
			}
			return 1;
		}
		catch(final Throwable t)
		{
			Log.errOut(mob.Name(),t);
		}
		return 0;
	}

	@Override
	public int savePlayers()
	{
		final boolean noCachePlayers=CMProps.getBoolVar(CMProps.Bool.PLAYERSNOCACHE);
		final int threadId = CMLib.getLibraryThreadID(Library.PLAYERS, this);
		final CMLib lib=CMLib.get(threadId);
		for(final MOB mob : playersList)
			savePlayer(mob, lib, noCachePlayers, false);
		return playersList.size();
	}

	@Override
	public String getSortValue(final MOB player, final PlayerSortCode code)
	{
		if(code == null)
			return player.Name();
		switch(code)
		{
		case NAME:
			return player.Name();
		case CLASS:
			return player.baseCharStats().getCurrentClass().name();
		case RACE:
			return player.baseCharStats().getMyRace().name();
		case LEVEL:
			return Integer.toString(player.basePhyStats().level());
		case AGE:
			return Long.toString(player.getAgeMinutes());
		case LAST:
			if(!player.isPlayer())
				return "";
			return Long.toString(player.playerStats().getLastDateTime());
		case EMAIL:
			if(!player.isPlayer())
				return "";
			return player.playerStats().getEmail();
		case IP:
			if(!player.isPlayer())
				return "";
			return player.playerStats().getLastIP();
		}
		return player.Name();
	}

	@Override
	public Object getThinSortValue(final ThinPlayer player, final PlayerSortCode code)
	{
		switch(code)
		{
		case NAME:
			return player.name();
		case CLASS:
			return player.charClass();
		case RACE:
			return player.race();
		case LEVEL:
			return Integer.valueOf(player.level());
		case AGE:
			return Integer.valueOf(player.age());
		case LAST:
			return Long.valueOf(player.last());
		case EMAIL:
			return player.email();
		case IP:
			return player.ip();
		}
		return player.name();
	}

	protected String getThinSortValue(final PlayerAccount account, final AcctThinSortCode code)
	{
		switch(code)
		{
		case NAME:
			return account.getAccountName();
		case LAST:
			return Long.toString(account.getLastDateTime());
		case EMAIL:
			return account.getEmail();
		case IP:
			return account.getLastIP();
		case NUMPLAYERS:
			return Integer.toString(account.numPlayers());
		case EXPIRATION:
			return Long.toString(account.getAccountExpiration());
		}
		return account.getAccountName();
	}

	@Override
	public PlayerSortCode getCharThinSortCode(String codeName, final boolean loose)
	{
		if(codeName == null)
			return null;
		codeName=codeName.toUpperCase().trim();
		if(!loose || charThinMap.containsKey(codeName))
			return charThinMap.get(codeName);
		for(final String key : charThinMap.keySet())
		{
			if(key.startsWith(codeName))
				return charThinMap.get(key);
		}
		return null;
	}

	protected AcctThinSortCode getAccountThinSortCode(String codeName, final boolean loose)
	{
		if(codeName == null)
			return null;
		codeName=codeName.toUpperCase().trim();
		if(CMParms.containsAsString(AcctThinSortCode.values(), codeName))
			return AcctThinSortCode.valueOf(codeName);
		if(!loose)
			return null;
		for(final AcctThinSortCode a : AcctThinSortCode.values())
		{
			if(a.name().startsWith(codeName))
				return a;
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<ThinPlayer> thinPlayers(final String sort, final Map<String, Object> cache)
	{
		Vector<PlayerLibrary.ThinPlayer> V=(cache==null)?null:(Vector<PlayerLibrary.ThinPlayer>)cache.get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=new Vector<PlayerLibrary.ThinPlayer>();
			V.addAll(CMLib.database().getExtendedUserList());
			final PlayerSortCode code=getCharThinSortCode(sort,false);
			if((sort.length()>0)
			&&(code != null)
			&&(V.size()>1))
			{
				final PlayerLibrary lib = CMLib.players();
				Collections.sort(V, new Comparator<PlayerLibrary.ThinPlayer>() {
					@Override
					public int compare(final ThinPlayer o1, final ThinPlayer o2)
					{
						if(o1 == null)
							return (o2 == null) ? 0 : -1;
						if(o2 == null)
							return 1;
						final Comparable<Object> c1 = (Comparable<Object>)lib.getThinSortValue(o1, code);
						final Comparable<Object> c2 = (Comparable<Object>)lib.getThinSortValue(o2,code);
						final int x= c1.compareTo(c2);
						if(x != 0)
							return x;
						return lib.getThinSortValue(o1, PlayerSortCode.NAME).toString().compareTo(lib.getThinSortValue(o2,PlayerSortCode.NAME).toString());
					}
				});
			}
			if(cache!=null)
				cache.put("PLAYERLISTVECTOR"+sort,V);
		}
		return V.elements();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Pair<Long,int[]>[] parsePrideStats(final String[] nextPeriods, final String[] prideStats)
	{
		final long now=System.currentTimeMillis();
		final List<Pair<Long,int[]>> finalStats=new ArrayList<Pair<Long,int[]>>(TimeClock.TimePeriod.values().length);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			final Pair<Long,int[]> p=new Pair<Long,int[]>(Long.valueOf(0),new int[PrideStat.values().length]);
			if(period==TimeClock.TimePeriod.ALLTIME)
				p.first=Long.valueOf(Long.MAX_VALUE);
			else
			if((nextPeriods!=null)&&(nextPeriods.length>period.ordinal())&&(nextPeriods[period.ordinal()].length()>0))
				p.first=Long.valueOf(CMath.s_long(nextPeriods[period.ordinal()]));
			if(now>p.first.longValue())
				p.first=Long.valueOf(period.nextPeriod());
			else
			if((prideStats.length>period.ordinal())&&(prideStats[period.ordinal()].length()>1))
			{
				final String[] prides=prideStats[period.ordinal()].split(",");
				for(final PrideStat stat : PrideStat.values())
				{
					if(prides.length > stat.ordinal())
					{
						final String statVal=prides[stat.ordinal()];
						if(statVal.length()>0)
							p.second[stat.ordinal()]=CMath.s_int(statVal);
					}
				}
			}
			finalStats.add(p);
		}
		return finalStats.toArray(new Pair[0]);
	}

	@Override
	public MOB findPlayerOnline(final String srchStr, final boolean exactOnly)
	{
		final MOB[] srch=new MOB[3];
		for(final Enumeration<MOB> p=players();p.hasMoreElements();)
		{
			final MOB M=p.nextElement();
			if((M!=null)
			&&(M.session()!=null)
			&&(CMLib.sessions().isSession(M.session())))
			{
				if(M.Name().equalsIgnoreCase(srchStr))
					return M;
				else
				if(M.name().equalsIgnoreCase(srchStr))
					srch[0]=M;
				// keep looking for players
				if(!exactOnly)
				{
					if(CMLib.english().containsString(M.Name(),srchStr))
						srch[1]=M;
					if(CMLib.english().containsString(M.name(),srchStr))
						srch[2]=M;
				}
			}
		}
		for(int i=0;i<srch.length;i++)
		{
			if(srch[i]!=null)
				return srch[i];
		}
		return null;
	}

	@Override
	public PlayerLibrary.ThinnerPlayer newThinnerPlayer()
	{
		return new PlayerLibrary.ThinnerPlayer()
		{
			private String name="";
			private String password="";
			private String accountName="";
			private String email="";
			private long expiration = 0;
			private MOB loadedMOB = null;

			@Override
			public String name()
			{
				return name;
			}

			@Override
			public ThinnerPlayer name(final String name)
			{
				this.name = name;
				return this;
			}

			@Override
			public String password()
			{
				return password;
			}

			@Override
			public ThinnerPlayer password(final String password)
			{
				this.password = password;
				return this;
			}

			@Override
			public long expiration()
			{
				return expiration;
			}

			@Override
			public ThinnerPlayer expiration(final long expiration)
			{
				this.expiration = expiration;
				return this;
			}

			@Override
			public String accountName()
			{
				return accountName;
			}

			@Override
			public ThinnerPlayer accountName(final String accountName)
			{
				this.accountName = accountName;
				return this;
			}

			@Override
			public String email()
			{
				return email;
			}

			@Override
			public ThinnerPlayer email(final String email)
			{
				this.email = email;
				return this;
			}

			@Override
			public MOB loadedMOB()
			{
				return loadedMOB;
			}
			@Override
			public ThinnerPlayer loadedMOB(final MOB mob)
			{
				this.loadedMOB = mob;
				return this;
			}

		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<PlayerAccount> accounts(final String sort, final Map<String, Object> cache)
	{
		Vector<PlayerAccount> V=(cache==null)?null:(Vector<PlayerAccount>)cache.get("ACCOUNTLISTVECTOR"+sort);
		if(V==null)
		{
			V=new Vector<PlayerAccount>();
			if(!allAccountsLoaded)
			{
				if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTSNOCACHE))
					V.addAll(CMLib.database().DBListAccounts(null));
				else
				{
					final List<PlayerAccount> rV=CMLib.database().DBListAccounts(null);
					for(final PlayerAccount A : rV)
						addAccount(A);
					allAccountsLoaded=true;
					V.addAll(accountsList);
				}
			}
			else
				V.addAll(accountsList);
			final AcctThinSortCode code=getAccountThinSortCode(sort,false);
			if(code==null)
				return V.elements();
			else
			if(V.size()>1)
			{
				final Vector<PlayerAccount> unV=V;
				V=new Vector<PlayerAccount>();
				while(unV.size()>0)
				{
					PlayerAccount A=unV.get(0);
					String loweStr=getThinSortValue(A,code);
					PlayerAccount lowestA=A;
					for(int i=1;i<unV.size();i++)
					{
						A=unV.get(i);
						final String val=getThinSortValue(A,code);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestA=A;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestA=A;
						}
					}
					unV.remove(lowestA);
					V.add(lowestA);
				}
			}
			if(cache!=null)
				cache.put("ACCOUNTLISTVECTOR"+sort,V);
		}
		return V.elements();
	}

	private boolean isProtected(final List<String> protectedOnes, final String name)
	{
		boolean protectedOne=false;
		for(int p=0;p<protectedOnes.size();p++)
		{
			final String P=protectedOnes.get(p);
			if(P.equalsIgnoreCase(name))
			{
				protectedOne=true;
				break;
			}
		}
		if(protectedOne)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(serviceClient.getName(),name+" is protected from purging.");
			return true;
		}
		return false;
	}

	private boolean autoPurge()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.AUTOPURGE))
			return true;
		final String mask=CMProps.getVar(CMProps.Str.AUTOPURGE);
		if(mask.hashCode() != this.autoPurgeHash)
		{
			final int lastLevel=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)+100;
			final List<String> entries = CMParms.parseCommas(mask.trim(),true);
			final long[][] presorted=CMLib.utensils().compileConditionalRange(entries, 2, 0, lastLevel);
			autoPurgeDaysLevels=new long[lastLevel+1];
			prePurgeLevels=new long[lastLevel+1];
			for (int i = 0; i < autoPurgeDaysLevels.length; i++)
				autoPurgeDaysLevels[i] = 0;
			for (int i = 0; i < prePurgeLevels.length; i++)
				prePurgeLevels[i] = 0;
			for(int i=0;i<presorted.length;i++)
			{
				final long[] set=presorted[i];
				if((set==null)||(set.length<2))
				{
					Log.errOut("CMPlayers","Error in AUTOPURGE definition #"+(i+1)+" in coffeemud.ini file! Fix immediately!!");
					continue;
				}
				final long val=set[0];
				if(set[0]<=0)
					continue;
				final long prepurge=set[1];
				long realVal=(val*TimeManager.MILI_DAY);
				long purgePoint=realVal-(prepurge*TimeManager.MILI_DAY);
				if(val <= 0)
				{
					realVal = 0;
					purgePoint = 0;
				}
				if(autoPurgeDaysLevels[i]==0)
					autoPurgeDaysLevels[i]=realVal;
				if(prePurgeLevels[i]==0)
					prePurgeLevels[i]=purgePoint;
			}
			this.autoPurgeHash=mask.hashCode();
		}
		setThreadStatus(serviceClient,"autopurge process");
		final List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if(protectedOnes==null)
			protectedOnes=new Vector<String>();

		final List<String> warnedOnes=Resources.getFileLineVector(Resources.getFileResource("warnedplayers.ini",false));
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
			Log.debugOut(serviceClient.getName(),"Autopurge process start. "+allUsers.size()+" users loaded, "+protectedOnes.size()+" protected, and "+warnedOnes.size()+" previously warned.");

		final StringBuilder warnStr=new StringBuilder("");
		final Map<String,Long> warnMap=new TreeMap<String,Long>();
		boolean warnChanged=false;
		if((warnedOnes!=null)&&(warnedOnes.size()>0))
		{
			for(int b=0;b<warnedOnes.size();b++)
			{
				final String codedWarnStr=warnedOnes.get(b).trim();
				if(codedWarnStr.trim().length()>0)
				{
					final int firstSpace=codedWarnStr.indexOf(' ');
					final String warnedName=codedWarnStr.substring(0, firstSpace).toUpperCase().trim();
					final int lastSpace=codedWarnStr.lastIndexOf(' ');
					final long warningDateTime=CMath.s_long(codedWarnStr.substring(lastSpace+1).trim());
					if((warningDateTime > 0)
					&& (System.currentTimeMillis() < (warningDateTime + (10 * TimeManager.MILI_DAY))))
					{
						warnStr.append(codedWarnStr+"\n");
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
							Log.debugOut(serviceClient.getName(),"Warn loaded: "+warnedName+" last warned on "+CMLib.time().date2String(warningDateTime));
						warnMap.put(warnedName, Long.valueOf(warningDateTime));
					}
					else
						warnChanged=true;
				}
			}
			if(warnChanged)
				Resources.updateFileResource("::warnedplayers.ini",warnStr);
			warnChanged=false;
		}
		for(final ThinPlayer user : allUsers)
		{
			final String name=user.name();
			final int level=user.level();
			final long userLastLoginDateTime=user.last();
			long purgeDateTime;
			long warnDateTime;
			if(level>=autoPurgeDaysLevels.length)
			{
				if(autoPurgeDaysLevels[autoPurgeDaysLevels.length-1]==0)
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
						Log.debugOut(serviceClient.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Character is active.");
					continue;
				}
				purgeDateTime=userLastLoginDateTime + autoPurgeDaysLevels[autoPurgeDaysLevels.length-1];
				warnDateTime=userLastLoginDateTime + prePurgeLevels[prePurgeLevels.length-1];
			}
			else
			if(level>=0)
			{
				if(autoPurgeDaysLevels[level]==0)
				{
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
						Log.debugOut(serviceClient.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+".  Character is active.");
					continue;
				}
				purgeDateTime=userLastLoginDateTime + autoPurgeDaysLevels[level];
				warnDateTime=userLastLoginDateTime + prePurgeLevels[level];
			}
			else
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
					Log.debugOut(serviceClient.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+" is level "+level+".  Skipping.");
				continue;
			}

			if((System.currentTimeMillis()>purgeDateTime)||(System.currentTimeMillis()>warnDateTime))
			{
				if(isProtected(protectedOnes, name))
					continue;

				long foundWarningDateTime=-1;
				if(warnMap.containsKey(name.toUpperCase().trim()))
					foundWarningDateTime = warnMap.get(name.toUpperCase().trim()).longValue();
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				{
					Log.debugOut(serviceClient.getName(),
							name+" last on "+CMLib.time().date2String(userLastLoginDateTime)
							+" will be warned on "+CMLib.time().date2String(warnDateTime)
							+" and purged on "+CMLib.time().date2String(purgeDateTime)
							+((foundWarningDateTime<0)?" never warned":(" last warned on "+CMLib.time().date2String(foundWarningDateTime))));
				}

				if((foundWarningDateTime<0)
				&&(System.currentTimeMillis()>warnDateTime))
				{
					final MOB M=getLoadPlayer(name);
					if((M!=null)
					&&(M.playerStats()!=null)
					&&((M.playerStats().getAccount()==null)
						||(!M.playerStats().getAccount().isSet(AccountFlag.NOCHARPURGE))))
					{
						warnStr.append(M.name()+" "+M.playerStats().getEmail()+" "+System.currentTimeMillis()+"\n");
						warnMap.put(M.Name().toUpperCase().trim(),Long.valueOf(System.currentTimeMillis()));
						warnChanged=true;
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
							Log.debugOut(serviceClient.getName(),name+" is now warned.");
						warnPrePurge(M,purgeDateTime-System.currentTimeMillis());
					}
				}
				else
				if((System.currentTimeMillis()>purgeDateTime)
				&&(foundWarningDateTime > 0)
				&&((System.currentTimeMillis()-foundWarningDateTime)>TimeManager.MILI_DAY))
				{
					final MOB M=getLoadPlayer(name);
					if((M!=null)
					&&(!CMSecurity.isASysOp(M))
					&&(!CMSecurity.isAllowedAnywhere(M, CMSecurity.SecFlag.NOPURGE))
					&&((M.playerStats()==null)
						||(M.playerStats().getAccount()==null)
						||(!M.playerStats().getAccount().isSet(AccountFlag.NOCHARPURGE))))
					{
						obliteratePlayer(M,true, true);
						M.destroy();
						Log.sysOut(serviceClient.getName(),"AutoPurged user "+name+" ("+M.basePhyStats().level()+"). Last logged in "+(CMLib.time().date2String(userLastLoginDateTime))+".");
					}
				}
			}
			else
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(serviceClient.getName(),name+" last on "+CMLib.time().date2String(userLastLoginDateTime)+" will be warned on "+CMLib.time().date2String(warnDateTime)+" and purged on "+CMLib.time().date2String(purgeDateTime));
			if(warnChanged)
				Resources.updateFileResource("::warnedplayers.ini",warnStr);
		}

		// accounts!
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.PURGEACCOUNTS))&&(CMProps.getIntVar(CMProps.Int.ACCOUNTPURGEDAYS)>0))
		{
			for(final Enumeration<PlayerAccount> pe=CMLib.players().accounts("",null); pe.hasMoreElements();)
			{
				final PlayerAccount PA=pe.nextElement();
				if((PA.numPlayers() > 0)
				||(isProtected(protectedOnes, PA.getAccountName())))
					continue;
				final long lastDateTimePurge = PA.getLastDateTime() + (TimeManager.MILI_DAY * CMProps.getIntVar(CMProps.Int.ACCOUNTPURGEDAYS));
				final long lastUpdatedPurge = PA.getLastUpdated() + (TimeManager.MILI_DAY * CMProps.getIntVar(CMProps.Int.ACCOUNTPURGEDAYS));
				final long accountExpPurge = PA.getAccountExpiration() + (TimeManager.MILI_DAY * CMProps.getIntVar(CMProps.Int.ACCOUNTPURGEDAYS));
				long lastTime = lastDateTimePurge;
				if(lastUpdatedPurge > lastTime)
					lastTime=lastUpdatedPurge;
				if(accountExpPurge > lastTime)
					lastTime=accountExpPurge;
				if(System.currentTimeMillis()>lastTime)
				{
					Log.sysOut(serviceClient.getName(),"AutoPurged account "+PA.getAccountName()+".");
					CMLib.players().obliterateAccountOnly(PA);
				}
			}
		}
		return true;
	}

	private void warnPrePurge(final MOB mob, long timeLeft)
	{
		// check for valid recipient
		if(mob==null)
			return;

		if((mob.playerStats()==null)
		||(mob.playerStats().getEmail().length()==0)) // no email addy to forward TO
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(serviceClient.getName(),"Unable to warn "+mob.Name()+" due to lacking an email address.");
			return;
		}

		final boolean canReceiveRealEmail =
				(mob.playerStats()!=null)
				&&(mob.playerStats().getEmail().length()>0)
				&&(mob.isAttributeSet(MOB.Attrib.AUTOFORWARD))
				&&((mob.playerStats().getAccount()==null)
					||(!mob.playerStats().getAccount().isSet(AccountFlag.NOAUTOFORWARD)));
		if(!canReceiveRealEmail)
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(serviceClient.getName(),mob.Name()+" Opting out of auto-forward.");
		}

		if(CMSecurity.isDisabled(DisFlag.SMTPCLIENT))
		{
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.AUTOPURGE))
				Log.debugOut(serviceClient.getName(),"Unable to warn "+mob.Name()+" due to smtp disable.");
			return;
		}

		//  timeLeft is in millis
		final String from="AutoPurgeWarning";
		final String to=mob.Name();
		final String subj=CMProps.getVar(CMProps.Str.MUDNAME)+" Autopurge Warning: "+to;
		String textTimeLeft="";
		if(timeLeft<0)
			timeLeft = 1000*60*60*24;
		if(timeLeft>(1000*60*60*24*2))
		{
			final int days=(int)CMath.div((double)timeLeft,1000*60*60*24);
			textTimeLeft = days + " days";
		}
		else
		{
			final int hours=(int)CMath.div((double)timeLeft,1000*60*60);
			textTimeLeft = hours + " hours";
		}

		final String msg=L("Your character, @x1, is going to be autopurged by the system in @x2.  ",to,textTimeLeft)+
						 L("If you would like to keep this character active, please re-login.  This is an automated message, please do not reply.");
		CMLib.smtp().emailOrJournal(from, from, to, subj, CMLib.coffeeFilter().simpleOutFilter(msg));
	}

	/**
	 * Called at boot time to build up the pre-existing pride stat Top-10 charts.
	 * Since it's called at boot time, it will never remove a fake pride stat,
	 * since those are all legit at this point.
	 *
	 * @param userID the user id whose stats are being accumulated
	 * @param allData the users data, which may all get unused
	 * @param top the actual top 10 chart for all periods
	 * @param now the current time
	 */
	public void accumulatePrideStats(final String userID, final Pair<Long,int[]>[] allData,
									 final List<Pair<String,Integer>>[][] top, final long now)
	{
		final int prideTopSize = CMProps.getIntVar(CMProps.Int.PRIDECOUNT);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(allData.length > period.ordinal())
			{
				final Pair<Long,int[]> p=allData[period.ordinal()];
				if(p == null)
					continue;
				final List<Pair<String,Integer>>[] topPeriods=top[period.ordinal()];
				if((period==TimeClock.TimePeriod.ALLTIME)||(now < p.first.longValue()))
				{
					for(final PrideStat pride : PrideStat.values())
					{
						if((p.second.length>pride.ordinal())
						&&(p.second[pride.ordinal()]>0))
						{
							final int val=p.second[pride.ordinal()];
							final List<Pair<String,Integer>> topPrides=topPeriods[pride.ordinal()];
							final int oldSize=topPrides.size();
							for(int i=0;i<topPrides.size();i++)
							{
								if(val >= topPrides.get(i).second.intValue())
								{
									topPrides.add(i, new Pair<String,Integer>(userID,Integer.valueOf(val)));
									while(topPrides.size()>prideTopSize)
										topPrides.remove(topPrides.size()-1);
									break;
								}
							}
							if((oldSize==topPrides.size())&&(topPrides.size()<prideTopSize))
								topPrides.add(new Pair<String,Integer>(userID,Integer.valueOf(val)));
						}
					}
				}
			}
		}
	}

	protected List<Pair<String,Integer>>[][] newPrideStatTopChart()
	{
		final int prideTopSize = CMProps.getIntVar(CMProps.Int.PRIDECOUNT);
		@SuppressWarnings("unchecked")
		final List<Pair<String,Integer>>[][] top=new Vector[TimeClock.TimePeriod.values().length][PrideStat.values().length];
		for(int x=0;x<top.length;x++)
		{
			for(int y=0;y<top[x].length;y++)
				top[x][y]=new Vector<Pair<String,Integer>>(prideTopSize+1);
		}
		return top;
	}

	protected String getPrideCatValue(final MOB mob, final PrideCat cat, final Object data)
	{
		switch(cat)
		{
		case PLAYER:
			return mob.name();
		case ACCOUNT:
			if((mob.playerStats()!=null)
			&&(mob.playerStats().getAccount()!=null))
				return mob.playerStats().getAccount().getAccountName();
			return null;
		case BASECLASS:
			return mob.baseCharStats().getCurrentClass().baseClass();
		case CLASS:
			return mob.baseCharStats().getCurrentClass().ID();
		case GENDER:
			return mob.baseCharStats().genderName();
		case LEVEL:
		{
			int level = mob.basePhyStats().level();
			if(data instanceof Integer)
				level = (int)Math.round(Math.floor(CMath.div(level,  ((Integer)data).intValue())))+1;
			return ""+level;
		}
		case RACE:
			return mob.baseCharStats().getMyRace().ID();
		case RACECAT:
			return mob.baseCharStats().getMyRace().racialCategory();
		case CLAN:
		{
			if(mob.clans().iterator().hasNext())
				return mob.clans().iterator().next().first.clanID();
			break;
		}
		}
		return null;
	}

	protected String getPrideCatValue(final ThinPlayer mob, final PrideCat cat, final Object data)
	{
		switch(cat)
		{
		case PLAYER:
			return mob.name();
		case ACCOUNT:
			return null;
		case BASECLASS:
		{
			if(mob.charClass()==null)
				return null;
			final CharClass C=CMClass.findCharClass(mob.charClass());
			if(C != null)
				return C.baseClass();
			return null;
		}
		case CLASS:
			return mob.charClass();
		case GENDER:
			return mob.gender();
		case LEVEL:
		{
			int level = mob.level();
			if(level <= 0)
				return null;
			if(data instanceof Integer)
				level = (int)Math.round(Math.floor(CMath.div(level,  ((Integer)data).intValue())))+1;
			return ""+level;
		}
		case RACE:
			return mob.race();
		case RACECAT:
		{
			if(mob.race()==null)
				return null;
			final Race R = CMClass.findRace(mob.race());
			if(R != null)
				return R.racialCategory();
			return null;
		}
		case CLAN:
		{
			final Enumeration<String> clans = mob.clans();
			if((clans!=null)&&(clans.hasMoreElements()))
				return clans.nextElement();
			return null;
		}
		}
		return null;
	}

	@Override
	public void resetAllPrideStats()
	{
		CMLib.threads().executeRunnable(new Runnable()
		{
			@Override
			public void run()
			{
				// first, do the player-related pride stats
				final long now = System.currentTimeMillis();
				{
					final Map<PrideCat,Map<String,List<Pair<String,Integer>>[][]>>
						catTops = new HashMap<PrideCat,Map<String,List<Pair<String,Integer>>[][]>>();
					for(final PrideCat cat : prideCats)
						catTops.put(cat, new Hashtable<String,List<Pair<String,Integer>>[][]>());
					final List<Pair<String,Integer>>[][] top=newPrideStatTopChart();
					CMLib.database().DBScanPridePlayerWinners(new CMCallback<Pair<ThinPlayer,Pair<Long,int[]>[]>>() {
						@Override
						public void callback(final Pair<ThinPlayer, Pair<Long, int[]>[]> t)
						{
							final ThinPlayer P = t.first;
							final Pair<Long,int[]>[] allData = t.second;
							accumulatePrideStats(P.name(), allData, top, now);
							for(int p=0;p<prideCats.size();p++)
							{
								final PrideCat pcat = prideCats.get(p);
								final Object prideObj = prideCatData.get(p);
								final Map<String,List<Pair<String,Integer>>[][]> topPlayerCat;
								if(catTops.containsKey(pcat))
									topPlayerCat = catTops.get(pcat);
								else
								{
									topPlayerCat=new Hashtable<String,List<Pair<String,Integer>>[][]>();
									catTops.put(pcat, topPlayerCat);
								}
								String catUnit = getPrideCatValue(P,pcat,prideObj);
								if(catUnit == null)
									continue;
								catUnit = catUnit.toUpperCase().trim();
								final List<Pair<String,Integer>>[][] topPlayerChart;
								if(topPlayerCat.containsKey(catUnit))
									topPlayerChart = topPlayerCat.get(catUnit);
								else
								{
									topPlayerChart = newPrideStatTopChart();
									topPlayerCat.put(catUnit, topPlayerChart);
								}
								accumulatePrideStats(P.name(), allData, topPlayerChart, now);
							}
						}
					}, (short)5);
					for(int x=0;x<top.length;x++)
					{
						for(int y=0;y<top[x].length;y++)
							topPlayers[x][y]=top[x][y];
					}
					for(final PrideCat pcat : prideCats)
					{
						final Map<String,List<Pair<String,Integer>>[][]> catMap = catTops.get(pcat);
						final Map<String,List<Pair<String,Integer>>[][]> topPlayerCat;
						if(topPlayerCats.containsKey(pcat))
							topPlayerCat = topPlayerCats.get(pcat);
						else
						{
							topPlayerCat=new Hashtable<String,List<Pair<String,Integer>>[][]>();
							topPlayerCats.put(pcat, topPlayerCat);
						}
						for(final String catUnit : catMap.keySet())
						{
							final List<Pair<String,Integer>>[][] catUnitChart = catMap.get(catUnit);
							final List<Pair<String,Integer>>[][] topPlayerChart;
							if(topPlayerCat.containsKey(catUnit))
								topPlayerChart = topPlayerCat.get(catUnit);
							else
							{
								topPlayerChart = newPrideStatTopChart();
								topPlayerCat.put(catUnit, topPlayerChart);
							}
							for(int x=0;x<catUnitChart.length;x++)
							{
								for(int y=0;y<catUnitChart[x].length;y++)
									topPlayerChart[x][y]=catUnitChart[x][y];
							}
						}
					}
				}
				// first, do the account-related pride stats
				if(CMProps.isUsingAccountSystem())
				{
					final List<Pair<String,Integer>>[][] top=newPrideStatTopChart();
					CMLib.database().DBScanPrideAccountWinners(new CMCallback<Pair<String,Pair<Long,int[]>[]>>() {
						@Override
						public void callback(final Pair<String, Pair<Long, int[]>[]> t)
						{
							final String userID = t.first;
							final Pair<Long,int[]>[] allData = t.second;
							accumulatePrideStats(userID, allData, top, now);
						}
					}, (short)5);
					for(int x=0;x<top.length;x++)
					{
						for(int y=0;y<top[x].length;y++)
							topAccounts[x][y]=top[x][y];
					}
				}
				for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
					topPrideExpiration[period.ordinal()] = period.nextPeriod();
			}
		});
	}

	@Override
	public Object getPlayerValue(String playerName, final PlayerCode code)
	{
		playerName=CMStrings.capitalizeAndLower(playerName);
		MOB M = getPlayer(playerName);
		if(M==null)
			M=getPlayerAllHosts(playerName);
		if(M!=null)
		{
			switch(code)
			{
			case ABLES:
				return new XVector<Ability>(M.abilities());
			case AFFBEHAV:
			{
				final XVector<CMObject> affBehav=new XVector<CMObject>();
				affBehav.addAll(M.behaviors());
				for(final Enumeration<Ability> a=M.effects();a.hasMoreElements();)
				{
					final Ability eA=a.nextElement();
					if((eA!=null)&&(!eA.canBeUninvoked()))
						affBehav.add(eA);
				}
				return affBehav;
			}
			case ALIGNMENT:
				return Integer.valueOf(M.fetchFaction(CMLib.factions().getAlignmentID()));
			case ARMOR:
				return Integer.valueOf(M.basePhyStats().armor());
			case ATTACK:
				return Integer.valueOf(M.basePhyStats().attackAdjustment());
			case CHARCLASS:
				return M.baseCharStats().getCurrentClass();
			case DAMAGE:
				return Integer.valueOf(M.basePhyStats().damage());
			case DESCRIPTION:
				return M.description();
			case EXPERS:
				return new XVector<String>(M.expertises());
			case FACTIONS:
			{
				final Vector<Pair<String,Integer>> fdata=new Vector<Pair<String,Integer>>();
				for(final Enumeration<String> f=M.factions();f.hasMoreElements();)
				{
					final String ID = f.nextElement();
					fdata.add(new Pair<String,Integer>(ID,Integer.valueOf(M.fetchFaction(ID))));
				}
				return fdata;
			}
			case INVENTORY:
			{
				final Vector<Triad<String,String,String>> items=new Vector<Triad<String,String,String>>();
				for(final Enumeration<Item> i= M.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					items.add(new Triad<String,String,String>(I.databaseID(),I.ID(),I.text()));
				}
				return items;
			}
			case LEVEL:
				return Integer.valueOf(M.basePhyStats().level());
			case MONEY:
				return CMLib.beanCounter().getMoneyItems(M, null);
			case NAME:
				return M.name();
			case RACE:
				return M.baseCharStats().getMyRace();
			case TATTS:
				return new XVector<Tattoo>(M.tattoos());
			case ACCOUNT:
				if((M.playerStats()!=null)
				&&(M.playerStats().getAccount()!=null))
					return M.playerStats().getAccount().getAccountName();
				return "";
			case AGE:
				return Long.valueOf(M.getAgeMinutes());
			case CHANNELMASK:
				return Integer.valueOf((M.playerStats()!=null)?M.playerStats().getChannelMask():0);
			case CLANS:
				return new XVector<Pair<Clan, Integer>>(M.clans().iterator());
			case COLOR:
				return M.playerStats()!=null?M.playerStats().getColorStr():"";
			case DEITY:
				return M.baseCharStats().getWorshipCharID();
			case EMAIL:
				return M.playerStats()!=null?M.playerStats().getEmail():"";
			case EXPERIENCE:
				return Integer.valueOf(M.getExperience());
			case HEIGHT:
				return Integer.valueOf(M.basePhyStats().height());
			case HITPOINTS:
				return Integer.valueOf(M.baseState().getHitPoints());
			case LASTDATE:
				return Long.valueOf(M.playerStats()!=null?M.playerStats().getLastDateTime():0);
			case LASTIP:
				return M.playerStats()!=null?M.playerStats().getLastIP():"";
			case LEIGE:
				return M.getLiegeID();
			case LOCATION:
				return CMLib.map().getExtendedRoomID(M.location());
			case MANA:
				return Integer.valueOf(M.baseState().getMana());
			case MATTRIB:
				return Long.valueOf(M.getAttributesBitmap());
			case MOVES:
				return Integer.valueOf(M.baseState().getMovement());
			case PASSWORD:
			{
				final PlayerStats pStats = M.playerStats();
				if(pStats != null)
				{
					if((CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
					&&(pStats.getAccount()!=null))
						return pStats.getAccount().getPasswordStr();
					else
						return pStats.getPasswordStr();
				}
				return "";
			}
			case PRACTICES:
				return Integer.valueOf(M.getPractices());
			case QUESTPOINTS:
				return Integer.valueOf(M.getQuestPoint());
			case STARTROOM:
				return CMLib.map().getExtendedRoomID(M.getStartRoom());
			case TRAINS:
				return Integer.valueOf(M.getTrains());
			case WEIGHT:
				return Integer.valueOf(M.basePhyStats().weight());
			case WIMP:
				return Integer.valueOf(M.getWimpHitPoint());
			}
		}
		else
		if(CMLib.players().playerExists(playerName))
			return CMLib.database().DBReadPlayerValue(playerName, code);
		else
		if(CMLib.players().playerExistsAllHosts(playerName))
		{
			for(final PlayerLibrary pLib : getOtherPlayerLibAllHosts())
			{
				if(pLib.playerExists(playerName))
				{
					final DatabaseEngine db = (DatabaseEngine)CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib), Library.DATABASE);
					if(db != CMLib.database())
					{
						final Object tryVal=db.DBReadPlayerValue(playerName, code);
						if(tryVal!=null)
							return tryVal;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setPlayerValue(String playerName, final PlayerCode code, final Object value)
	{
		playerName=CMStrings.capitalizeAndLower(playerName);
		MOB M = getPlayer(playerName);
		if(M==null)
			M=getPlayerAllHosts(playerName);
		if(M!=null)
		{
			switch(code)
			{
			case ABLES:
			{
				@SuppressWarnings("unchecked")
				final XVector<Ability> newfdata=new XVector<Ability>((List<Ability>)value);
				final XVector<Ability> oldfdata=new XVector<Ability>(M.abilities());
				final List<Ability>[] deltas = newfdata.makeDeltas(oldfdata, new Comparator<Ability>()
				{
					@Override
					public int compare(final Ability o1, final Ability o2)
					{
						return o1.ID().compareTo(o2.ID());
					}
				});
				for(final Ability p : deltas[1])
					M.delAbility(M.fetchAbility(p.ID()));
				for(final Ability p : deltas[0])
					M.addAbility(p);
				for(final Ability p : newfdata)
				{
					final Ability oldA=M.fetchAbility(p.ID());
					if(oldA!=null)
					{
						oldA.setProficiency(p.proficiency());
						if((oldA!=p)
						&&(!oldA.text().equals(p.text())))
							oldA.setMiscText(p.text());
					}
				}
				break;
			}
			case AFFBEHAV:
			{
				@SuppressWarnings("unchecked")
				final XVector<CMObject> newodata=new XVector<CMObject>((List<CMObject>)value);
				@SuppressWarnings("unchecked")
				final XVector<CMObject> oldodata=new XVector<CMObject>((List<CMObject>)getPlayerValue(playerName,code));
				final List<CMObject>[] deltas = newodata.makeDeltas(oldodata, new Comparator<CMObject>()
				{
					@Override
					public int compare(final CMObject o1, final CMObject o2)
					{
						final String o1id = (o1 instanceof Ability)?("A"+o1.ID()):("B"+o1.ID());
						final String o2id = (o2 instanceof Ability)?("A"+o2.ID()):("B"+o2.ID());
						return o1id.compareTo(o2id);
					}
				});
				for(final CMObject o : deltas[1])
				{
					if(o instanceof Behavior)
						M.delBehavior((Behavior)o);
					else
						M.delEffect((Ability)o);
				}
				for(final CMObject o : deltas[0])
				{
					if(o instanceof Behavior)
						M.addBehavior((Behavior)o);
					else
						M.addNonUninvokableEffect((Ability)o);
				}
				for(final CMObject o : newodata)
				{
					if(o instanceof Behavior)
					{
						final Behavior oldB=M.fetchBehavior(o.ID());
						if(oldB!=null)
						if((oldB!=o)
						&&(!oldB.getParms().equals(((Behavior)o).getParms())))
							oldB.setParms(((Behavior)o).getParms());
					}
					else
					{
						final Ability oldA=M.fetchEffect(o.ID());
						if(oldA!=null)
						{
							oldA.setProficiency(((Ability)o).proficiency());
							if((oldA!=o)
							&&(!oldA.text().equals(((Ability)o).text())))
								oldA.setMiscText(((Ability)o).text());
						}
					}
				}
				break;
			}
			case ALIGNMENT:
				if(CMath.s_int(""+value)==Integer.MAX_VALUE)
					M.removeFaction(CMLib.factions().getAlignmentID());
				else
					M.addFaction(CMLib.factions().getAlignmentID(), CMath.s_int(""+value));
				break;
			case ARMOR:
				M.basePhyStats().setArmor(CMath.s_int(""+value));
				M.recoverPhyStats();
				break;
			case ATTACK:
				M.basePhyStats().setAttackAdjustment(CMath.s_int(""+value));
				M.recoverPhyStats();
				break;
			case CHARCLASS:
			{
				final CharClass newClass = (CharClass)value;
				if(newClass!=null)
					M.baseCharStats().setCurrentClass(newClass);
				break;
			}
			case DAMAGE:
				M.basePhyStats().setDamage(CMath.s_int(""+value));
				M.recoverPhyStats();
				break;
			case DESCRIPTION:
				M.setDescription(""+value);
				break;
			case EXPERS:
			{
				@SuppressWarnings("unchecked")
				final XVector<String> newList = new XVector<String>((List<String>)value);
				final XVector<String> oldList = new XVector<String>(M.expertises());
				final List<String>[] deltas = newList.makeDeltas(oldList, newList.anyComparator);
				for(final String s : deltas[0])
					M.addExpertise(s);
				for(final String s : deltas[1])
					M.delExpertise(s);
				break;
			}
			case FACTIONS:
			{
				@SuppressWarnings("unchecked")
				final XVector<Pair<String,Integer>> newfdata=new XVector<Pair<String,Integer>>((List<Pair<String,Integer>>)value);
				@SuppressWarnings("unchecked")
				final XVector<Pair<String,Integer>> oldfdata=new XVector<Pair<String,Integer>>((List<Pair<String,Integer>>)getPlayerValue(playerName,code));
				final List<Pair<String, Integer>>[] deltas = newfdata.makeDeltas(oldfdata, new Comparator<Pair<String, Integer>>()
				{
					@Override
					public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2)
					{
						return o1.first.compareTo(o2.first);
					}
				});
				for(final Pair<String, Integer> p : deltas[1])
					M.removeFaction(p.first);
				for(final Pair<String, Integer> p : newfdata)
					M.addFaction(p.first, p.second.intValue());
				break;
			}
			case INVENTORY:
			{
				@SuppressWarnings("unchecked")
				final XVector<Triad<String,String,String>> newList = new XVector<Triad<String,String,String>>((List<Triad<String,String,String>>)value);
				@SuppressWarnings("unchecked")
				final XVector<Triad<String,String,String>> oldList = new XVector<Triad<String,String,String>>((List<Triad<String,String,String>>)getPlayerValue(playerName, code));
				final List<Triad<String,String,String>>[] deltas = newList.makeDeltas(oldList, new Triad.TripleComparator<String,String,String>());
				for(final Triad<String,String, String> s : deltas[1])
				{
					for(final Enumeration<Item> i= M.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I.ID().equals(s.second))
						&&(I.databaseID().equals(s.first))
						&&(I.text().equals(s.third)))
						{
							M.delItem(I);
							break;
						}
					}
				}
				for(final Triad<String,String, String> s : deltas[0])
				{
					final Item I=CMClass.getItem(s.second);
					if(I!=null)
					{
						I.setMiscText(s.third);
						I.setDatabaseID(s.first);
						M.addItem(I);
					}
				}
				break;
			}
			case LEVEL:
			{
				final int newLevel=CMath.s_int(""+value);
				M.basePhyStats().setLevel(newLevel);
				M.recoverPhyStats();
				M.baseCharStats().setClassLevel(M.baseCharStats().getCurrentClass(), M.basePhyStats().level() - M.baseCharStats().combinedSubLevels());
				M.recoverCharStats();
				break;
			}
			case MONEY:
			{
				final List<Coins> old=CMLib.beanCounter().getMoneyItems(M, null);
				for(final Coins C : old)
					M.delItem(C);
				@SuppressWarnings("unchecked")
				final List<Coins> newCs=(List<Coins>)value;
				for(final Coins C : newCs)
					M.addItem(C);
				break;
			}
			case NAME:
				return;
			case RACE:
				if(value instanceof Race)
					M.baseCharStats().setMyRace((Race)value);
				M.recoverCharStats();
				break;
			case TATTS:
			{
				@SuppressWarnings("unchecked")
				final XVector<Tattoo> newTatts = new XVector<Tattoo>((List<Tattoo>)value);
				final XVector<Tattoo> oldTatts = new XVector<Tattoo>(M.tattoos());
				final List<Tattoo>[] deltas = newTatts.makeDeltas(oldTatts, new Comparator<Tattoo>()
				{
					@Override
					public int compare(final Tattoo o1, final Tattoo o2)
					{
						return o1.name().compareTo(o2.name());
					}
				});
				for(final Tattoo t : deltas[0])
					M.addTattoo(t);
				for(final Tattoo t : deltas[1])
					M.delTattoo(t);
				break;
			}
			case ACCOUNT:
				return;
			case AGE:
				M.setAgeMinutes(CMath.s_long(""+value));
				break;
			case CHANNELMASK:
				if(M.playerStats()!=null)
					M.playerStats().setChannelMask(CMath.s_int(""+value));
				break;
			case CLANS:
			{
				@SuppressWarnings("unchecked")
				final XVector<Pair<Clan, Integer>> newTatts = new XVector<Pair<Clan, Integer>>((List<Pair<Clan, Integer>>)value);
				final XVector<Pair<Clan, Integer>> oldTatts = new XVector<Pair<Clan, Integer>>(M.clans().iterator());
				final List<Pair<Clan, Integer>>[] deltas = newTatts.makeDeltas(oldTatts, new Comparator<Pair<Clan, Integer>>()
				{
					@Override
					public int compare(final Pair<Clan, Integer> o1, final Pair<Clan, Integer> o2)
					{
						return o1.first.clanID().compareTo(o2.first.clanID());
					}
				});
				for(final Pair<Clan, Integer> p : deltas[1])
					M.setClan(p.first.clanID(), -1);
				for(final Pair<Clan, Integer> p : newTatts)
					M.setClan(p.first.clanID(), p.second.intValue());
				break;
			}
			case COLOR:
				if(M.playerStats()!=null)
					M.playerStats().setColorStr(""+value);
				break;
			case DEITY:
				M.baseCharStats().setWorshipCharID(""+value);
				M.recoverCharStats();
				break;
			case EMAIL:
				if(M.playerStats()!=null)
					M.playerStats().setEmail(""+value);
				break;
			case EXPERIENCE:
				M.setExperience(CMath.s_int(""+value));
				break;
			case HEIGHT:
				M.basePhyStats().setHeight(CMath.s_int(""+value));
				M.recoverPhyStats();
				break;
			case HITPOINTS:
				M.baseState().setHitPoints(CMath.s_int(""+value));
				M.recoverMaxState();
				break;
			case LASTDATE:
				if(M.playerStats()!=null)
					M.playerStats().setLastDateTime(CMath.s_long(""+value));
				break;
			case LASTIP:
				if(M.playerStats()!=null)
					M.playerStats().setLastIP(""+value);
				break;
			case LEIGE:
				M.setLiegeID(""+value);
				break;
			case LOCATION:
			{
				final Room R=CMLib.map().getRoom(""+value);
				if(R!=null)
					R.bringMobHere(M, true);
				break;
			}
			case MANA:
				M.baseState().setMana(CMath.s_int(""+value));
				M.recoverMaxState();
				break;
			case MATTRIB:
				M.setAttributesBitmap(CMath.s_long(""+value));
				break;
			case MOVES:
				M.baseState().setMovement(CMath.s_int(""+value));
				M.recoverMaxState();
				break;
			case PASSWORD:
				// nopenope
				break;
			case PRACTICES:
				M.setPractices(CMath.s_int(""+value));
				break;
			case QUESTPOINTS:
				M.setQuestPoint(CMath.s_int(""+value));
				break;
			case STARTROOM:
			{
				final Room R=CMLib.map().getRoom(""+value);
				if(R!=null)
					M.setStartRoom(R);
				break;
			}
			case TRAINS:
				M.setTrains(CMath.s_int(""+value));
				break;
			case WEIGHT:
				M.basePhyStats().setWeight(CMath.s_int(""+value));
				M.recoverPhyStats();
				break;
			case WIMP:
				M.setWimpHitPoint(CMath.s_int(""+value));
				break;
			}
		}
		else
		if(CMLib.players().playerExists(playerName))
			CMLib.database().DBSetPlayerValue(playerName, code, value);
		else
		if(CMLib.players().playerExistsAllHosts(playerName))
		{
			for(final PlayerLibrary pLib : getOtherPlayerLibAllHosts())
			{
				if(pLib.playerExists(playerName))
				{
					final DatabaseEngine db = (DatabaseEngine)CMLib.library(CMLib.getLibraryThreadID(Library.PLAYERS, pLib), Library.DATABASE);
					if(db != CMLib.database())
					{
						db.DBSetPlayerValue(playerName, code, value);
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean activate()
	{
		if(!super.activate())
			return false;
		if(serviceClient==null)
		{
			name="THPlayers"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK|Tickable.TICKID_LONGERMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
			resetAllPrideStats();
			loadPreviousPrideRecords();
		}
		return true;
	}

	protected void selectRandomTitle(final PlayerStats pStats)
	{
		if(pStats == null)
			return;
		final List<String> randomSelections = new ArrayList<String>();
		for(final String title : pStats.getTitles())
		{
			if(pStats.getTitleRandom(title, null))
				randomSelections.add(title);
		}
		if(randomSelections.size()<2)
			return;
		final String choice = randomSelections.get(CMLib.dice().roll(1, randomSelections.size(), -1));
		pStats.addTitle(choice);
	}

	protected void clearTopStatChart(final List<Pair<String,Integer>> top)
	{
		if(top!=null)
		{
			synchronized(top)
			{
				top.clear();
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.PLAYERTHREAD)
		&&(tickStatus == Tickable.STATUS_NOT))
		{
			try
			{
				tickStatus=Tickable.STATUS_ALIVE;
				isDebugging=CMSecurity.isDebugging(DbgFlag.PLAYERTHREAD);
				if(checkDatabase() && CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				{
					setThreadStatus(serviceClient,"not saving players");
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.PLAYERTHREAD)))
					{
						setThreadStatus(serviceClient,"checking player titles");
						for(final MOB M : playersList)
						{
							if((M.playerStats()!=null)
							&&(M.playerStats().isSavable())
							&&(CMLib.flags().isInTheGame(M,true)))
							{
								//boolean didSomething =
								CMLib.awards().evaluateAutoTitles(M);
								//didSomething =
								CMLib.achievements().evaluatePlayerAchievements(M);// || didSomething;
								CMLib.achievements().evaluateAccountAchievements(M);// || didSomething;
								//if(didSomething)&&(!CMLib.flags().isInTheGame(M,true)))
								//	CMLib.database().DBUpdatePlayerMOBOnly(M);
								selectRandomTitle(M.playerStats());
							}
						}
						autoPurge();
						setThreadStatus(serviceClient,"saving players");
						if(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS))
							savePlayers();
						setThreadStatus(serviceClient,"not saving players");
					}
					setThreadStatus(serviceClient,"expiring top metrics");
					final long now=System.currentTimeMillis();
					final boolean debugTopThread = CMSecurity.isDebugging(DbgFlag.TOPTHREAD);
					if(debugTopThread)
						Log.debugOut(name()+": Current Time: "+Math.round(now/60000));
					boolean anythingReset = false;
					for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
					{
						if(period == TimeClock.TimePeriod.ALLTIME)
							continue;
						if(debugTopThread)
							Log.debugOut(name()+": "+period.name()+": Expires @"+Math.round(topPrideExpiration[period.ordinal()]/60000));
						if(now > topPrideExpiration[period.ordinal()])
						{
							anythingReset = true;
							topPrideExpiration[period.ordinal()] = period.nextPeriod();
							if(debugTopThread)
								Log.debugOut(name()+": "+period.name()+": Next Expire @"+Math.round(topPrideExpiration[period.ordinal()]/60000));
							for(final PrideStat stat : PrideStat.values())
							{
								prevTopAccounts[period.ordinal()][stat.ordinal()]
										=new XVector<Pair<String,Integer>>(topAccounts[period.ordinal()][stat.ordinal()]);
								clearTopStatChart(topAccounts[period.ordinal()][stat.ordinal()]);
								prevTopPlayers[period.ordinal()][stat.ordinal()]
										=new XVector<Pair<String,Integer>>(topPlayers[period.ordinal()][stat.ordinal()]);
								clearTopStatChart(topPlayers[period.ordinal()][stat.ordinal()]);
							}
							for(final PrideCat pcat : prideCats)
							{
								if(topPlayerCats.containsKey(pcat))
								{
									final Map<String,List<Pair<String,Integer>>[][]> topPlayerCat =
											topPlayerCats.get(pcat);
									final Map<String,List<Pair<String,Integer>>[][]> prevTopPlayerCat;
									if(!prevTopPlayerCats.containsKey(pcat))
									{
										prevTopPlayerCat = new Hashtable<String,List<Pair<String,Integer>>[][]>();
										prevTopPlayerCats.put(pcat, prevTopPlayerCat);
									}
									else
										prevTopPlayerCat = prevTopPlayerCats.get(pcat);
									for(final String key : topPlayerCat.keySet())
									{
										final List<Pair<String,Integer>>[][] keyChart = topPlayerCat.get(key);
										final List<Pair<String,Integer>>[][] prevKeyChart;
										if(prevTopPlayerCat.containsKey(key))
											prevKeyChart = prevTopPlayerCat.get(key);
										else
										{
											prevKeyChart = newPrideStatTopChart();
											prevTopPlayerCat.put(key, prevKeyChart);
										}
										for(final PrideStat stat : PrideStat.values())
										{
											prevKeyChart[period.ordinal()][stat.ordinal()]
													=new XVector<Pair<String,Integer>>(keyChart[period.ordinal()][stat.ordinal()]);
											clearTopStatChart(keyChart[period.ordinal()][stat.ordinal()]);
										}
									}
								}
							}
						}
					}
					if(anythingReset)
						savePreviousPrideRecords();
					setThreadStatus(serviceClient,"not doing anything");
				}
			}
			finally
			{
				tickStatus=Tickable.STATUS_NOT;
				setThreadStatus(serviceClient,"sleeping");
			}
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		playersList.clear();
		playerLocations.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
}
