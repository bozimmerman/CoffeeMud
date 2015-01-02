package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2008-2015 Bo Zimmerman

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
public interface PlayerLibrary extends CMLibrary
{
	public int numPlayers();
	public void addPlayer(MOB newOne);
	public void delPlayer(MOB oneToDel);
	public MOB getPlayer(String calledThis);
	public MOB getLoadPlayer(String last);
	public MOB getLoadPlayerByEmail(String email);
	public PlayerAccount getLoadAccount(String calledThis);
	public PlayerAccount getLoadAccountByEmail(String email);
	public PlayerAccount getAccount(String calledThis);
	public void addAccount(PlayerAccount acct);
	public boolean accountExists(String name);
	public Enumeration<MOB> players();
	public Enumeration<PlayerAccount> accounts();
	public Enumeration<PlayerAccount> accounts(String sort, Map<String, Object> cache);
	public void obliteratePlayer(MOB deadMOB, boolean deleteAssets, boolean quiet);
	public void obliterateAccountOnly(PlayerAccount deadAccount);
	public void renamePlayer(MOB mob, String oldName);
	public boolean playerExists(String name);
	public void forceTick();
	public int savePlayers();
	public Enumeration<ThinPlayer> thinPlayers(String sort, Map<String, Object> cache);
	public int getCharThinSortCode(String codeName, boolean loose);
	public String getThinSortValue(ThinPlayer player, int code);
	public Set<MOB> getPlayersHere(Room room);
	public void changePlayersLocation(MOB mob, Room room);
	public Pair<Long,int[]>[] parsePrideStats(final String[] nextPeriods, final String[] prideStats);
	public int bumpPrideStat(final MOB mob, final AccountStats.PrideStat stat, final int amt);
	public List<Pair<String,Integer>> getTopPridePlayers(TimeClock.TimePeriod period, AccountStats.PrideStat stat);
	public List<Pair<String,Integer>> getTopPrideAccounts(TimeClock.TimePeriod period, AccountStats.PrideStat stat);

	public static final String[] CHAR_THIN_SORT_CODES={ "NAME","CLASS","RACE","LEVEL","AGE","LAST","EMAIL","IP"};
	public static final String[] CHAR_THIN_SORT_CODES2={ "CHARACTER","CHARCLASS","RACE","LVL","HOURS","DATE","EMAILADDRESS","LASTIP"};

	public static final String[] ACCOUNT_THIN_SORT_CODES={ "NAME","LAST","EMAIL","IP","NUMPLAYERS"};

	public static class ThinPlayer
	{
		public String name="";
		public String charClass="";
		public String race="";
		public int level=0;
		public int age=0;
		public long last=0;
		public String email="";
		public String ip="";
		public int exp=0;
		public int expLvl=0;
	}

	public static class ThinnerPlayer
	{
		public String name="";
		public String password="";
		public long expiration=0;
		public String accountName="";
		public String email="";
		public MOB loadedMOB=null;
		public Tickable toTickable()
		{
			return new Tickable()
			{
				@Override public int getTickStatus() {return 0;}
				@Override public String name() { return name;}
				@Override public boolean tick(Tickable ticking, int tickID) { return false;}
				@Override public String ID() { return "StdMOB";}
				@Override public CMObject copyOf() { return this;}
				@Override public void initializeClass() {}
				@Override public CMObject newInstance() { return this;}
				@Override public int compareTo(CMObject o) {return (o==this)?0:-1;}
			};
		}
		public boolean matchesPassword(String checkPass)
		{
			return CMLib.encoder().isARandomHashString(password)
				?CMLib.encoder().checkAgainstRandomHashString(checkPass,password):
					checkPass.equalsIgnoreCase(password);
		}
	}
}
