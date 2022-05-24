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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder.GenItemCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder.GenMOBCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2008-2022 Bo Zimmerman

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
	public PlayerAccount getLoadAccount(String calledThis);
	public PlayerAccount getLoadAccountByEmail(String email);

	public PlayerAccount getAccount(String calledThis);
	public PlayerAccount getAccountAllHosts(String calledThis);

	public void addAccount(PlayerAccount acct);

	public boolean accountExists(String name);
	public boolean accountExistsAllHosts(String name);

	public Enumeration<PlayerAccount> accounts();
	public Enumeration<PlayerAccount> accounts(String sort, Map<String, Object> cache);

	public boolean isSameAccount(final MOB player1, final MOB player2);
	public boolean isSameAccountIP(final MOB player1, final MOB player2);

	public void obliterateAccountOnly(PlayerAccount deadAccount);

	public int numPlayers();
	public void addPlayer(MOB newOne);
	public void delPlayer(MOB oneToDel);
	public Enumeration<MOB> players();

	public MOB getPlayer(String calledThis);
	public MOB getPlayerAllHosts(String calledThis);

	public MOB getLoadPlayer(String last);
	public MOB getLoadPlayerByEmail(String email);

	public List<String> getPlayerLists();
	public List<String> getPlayerListsAllHosts();

	public boolean isLoadedPlayer(final MOB M);
	public boolean isLoadedPlayer(final String mobName);

	public boolean playerExists(String name);
	public boolean playerExistsAllHosts(String name);

	public String getLiegeOfUserAllHosts(final String userName);

	public MOB findPlayerOnline(final String srchStr, final boolean exactOnly);

	public void obliteratePlayer(MOB deadMOB, boolean deleteAssets, boolean quiet);

	public void renamePlayer(MOB mob, String oldName);

	public void unloadOfflinePlayer(final MOB mob);

	public void forceTick();
	public int savePlayers();

	public ThinPlayer getThinPlayer(final String mobName);
	public PlayerLibrary.ThinnerPlayer newThinnerPlayer();

	/**
	 * Given a possible player sort code string, and an optional cache of
	 * pre-loaded key pairs, one of which might be "PLAYERLISTVECTOR"+sort,
	 * pointing to a vector of pre-loaded thinplayers, this will return
	 * an enumeration of all players in the game.
	 *
	 * @see PlayerLibrary.PlayerSortCode
	 * @see PlayerLibrary.ThinPlayer
	 * @see PlayerLibrary#getThinPlayer(String)
	 * @see PlayerLibrary#newThinnerPlayer()
	 *
	 * @param sort "", or a player sort code name
	 * @param cache null, or a map that might have a prior list
	 * @return an enumeration of all players, as thin-ones
	 */
	public Enumeration<ThinPlayer> thinPlayers(String sort, Map<String, Object> cache);

	/**
	 * Given a possible sort code name, and now exact of a match you want,
	 * this might return a player sort code match.
	 *
	 * @see PlayerLibrary.PlayerSortCode
	 * @see PlayerLibrary.ThinPlayer
	 * @see PlayerLibrary#getSortValue(MOB, PlayerSortCode)
	 * @see PlayerLibrary#getThinSortValue(ThinPlayer, PlayerSortCode)
	 * @see PlayerLibrary#getThinPlayer(String)
	 *
	 * @param codeName the possible code name
	 * @param loose true for startswith matches, false for exact
	 * @return null or the player sort code
	 */
	public PlayerSortCode getCharThinSortCode(String codeName, boolean loose);

	/**
	 * Given a player character thinplayer obj, and a sort code, this will
	 * return the sortable string value of that attribute,
	 *
	 * @see PlayerLibrary.PlayerSortCode
	 * @see PlayerLibrary.ThinPlayer
	 * @see PlayerLibrary#getSortValue(MOB, PlayerSortCode)
	 * @see PlayerLibrary#getCharThinSortCode(String, boolean)
	 * @see PlayerLibrary#getThinPlayer(String)
	 *
	 * @param player the character thinplayer obj
	 * @param code the code for the value to return
	 * @return the value of the code from the character
	 */
	public String getThinSortValue(ThinPlayer player, PlayerSortCode code);

	/**
	 * Given a player character mob, and a sort code, this will
	 * return the sortable string value of that attribute,
	 *
	 * @see PlayerLibrary.PlayerSortCode
	 * @see PlayerLibrary#getThinSortValue(ThinPlayer, PlayerSortCode)
	 * @see PlayerLibrary#getCharThinSortCode(String, boolean)
	 *
	 * @param player the character mob
	 * @param code the code for the value to return
	 * @return the value of the code from the character
	 */
	public String getSortValue(MOB player, PlayerSortCode code);

	/**
	 * Given a player name, and code representing an attribute of the player
	 * character, this will fetch the value on the player, regardless of
	 * whether they are online or no. See PlayerCode enum for information
	 * on proper object type for the return value.
	 *
	 * @see PlayerLibrary.PlayerCode
	 * @see PlayerLibrary#setPlayerValue(String, PlayerCode, Object)
	 *
	 * @param playerName player character name
	 * @param code PlayerCode to fetch
	 * @return value the character value
	 */
	public Object getPlayerValue(final String playerName, final PlayerCode code);

	/**
	 * Given a player name, and code representing an attribute of the player
	 * character, and a new value appropriate to that code, this will change
	 * the value on the player, regardless of whether they are online or no.
	 * See PlayerCode enum for information on proper object type for
	 * the value.
	 *
	 * @see PlayerLibrary.PlayerCode
	 * @see PlayerLibrary#getPlayerValue(String, PlayerCode)
	 *
	 * @param playerName player character name
	 * @param code PlayerCode to change
	 * @param value the new value
	 */
	public void setPlayerValue(final String playerName, final PlayerCode code, final Object value);

	/**
	 * Sometimes the list of players in a given room needs to be
	 * determined rather instantly.  This method helps do that
	 * by returning all player chars in the given room,
	 * or null.
	 *
	 * @see PlayerLibrary#changePlayersLocation(MOB, Room)
	 *
	 * @param room the room curious about
	 * @return the set of chars, or null
	 */
	public Set<MOB> getPlayersHere(Room room);

	/**
	 * Sometimes the list of players in a given room needs to be
	 * determined rather instantly.  This method helps do that
	 * by keeping a double-hash reference of players to their
	 * rooms.
	 *
	 * @see PlayerLibrary#getPlayersHere(Room)
	 *
	 * @param mob the player to keep tracking
	 * @param room the room the player is in, or null to remove him altogther
	 */
	public void changePlayersLocation(MOB mob, Room room);

	/**
	 * Causes the pride stats list to be reloaded from the
	 * player database.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 */
	public void resetAllPrideStats();

	/**
	 * Given an array of timestamps indexed by period ordinals, and previous
	 * pride stat values similarly indexed, this will generate an array
	 * of pairs representing the data.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat, int)
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param nextPeriods the timestamps by period
	 * @param prideStats the previous stat values by period
	 * @return the array of pairs
	 */
	public Pair<Long,int[]>[] parsePrideStats(final String[] nextPeriods, final String[] prideStats);

	/**
	 * Given a player who did something that triggers a change in a pride stat, the pride
	 * stat that changed, and the amount it changed by, this will update the pride lists
	 * and return the given value.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param mob the player char who did something
	 * @param stat the pride stat reflecting what they did
	 * @param amt the amount to change the stat by + or -
	 * @return the give amt, or 0
	 */
	public int bumpPrideStat(final MOB mob, final AccountStats.PrideStat stat, final int amt);

	/**
	 * Returns the top winning character names and the associated values that got them there, for the given time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top character for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getTopPridePlayers(TimeClock.TimePeriod period, AccountStats.PrideStat stat);

	/**
	 * Returns the top winning account names and the associated values that got them there, for the given time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top accounts for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getTopPrideAccounts(TimeClock.TimePeriod period, AccountStats.PrideStat stat);

	/**
	 * For systems that list users and allow sorting, here
	 * is an enum of the popular sort codes, along with
	 * popular alias for the code names.
	 *
	 * @author Bo Zimmerman
	 */
	public enum PlayerSortCode
	{
		NAME("CHARACTER"),
		CLASS("CHARCLASS"),
		RACE("RACE"),
		LEVEL("LVL"),
		AGE("HOURS"),
		LAST("DATE"),
		EMAIL("EMAILADDRESS"),
		IP("LASTIP")
		;
		public String altName;
		private PlayerSortCode(final String ln)
		{
			this.altName=ln;
		}
	}

	/**
	 * The player library can query individual fields from player
	 * characters regardless of whether the player is cached or
	 * not.  These are the fields that are available for direct
	 * reading, along with the format of their values
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum PlayerCode
	{
		/**
		 * String
		 */
		NAME,
		/**
		 * String
		 */
		PASSWORD,
		/**
		 * CharClass
		 */
		CHARCLASS,
		/**
		 * Race
		 */
		RACE,
		/**
		 * Integer
		 */
		HITPOINTS,
		/**
		 * Integer
		 */
		LEVEL,
		/**
		 * Integer
		 */
		MANA,
		/**
		 * Integer
		 */
		MOVES,
		/**
		 * String
		 */
		DESCRIPTION,
		/**
		 * Integer
		 */
		ALIGNMENT,
		/**
		 * Integer
		 */
		EXPERIENCE,
		/**
		 * String
		 */
		DEITY,
		/**
		 * Integer
		 */
		PRACTICES,
		/**
		 * Integer
		 */
		TRAINS,
		/**
		 * Long
		 */
		AGE,
		/**
		 * Integer
		 */
		MONEY,
		/**
		 * Integer
		 */
		WIMP,
		/**
		 * Integer
		 */
		QUESTPOINTS,
		/**
		 * String
		 */
		LOCATION,
		/**
		 * String
		 */
		STARTROOM,
		/**
		 * Long
		 */
		LASTDATE,
		/**
		 * Integer
		 */
		CHANNELMASK,
		/**
		 * Integer
		 */
		ATTACK,
		/**
		 * Integer
		 */
		ARMOR,
		/**
		 * Integer
		 */
		DAMAGE,
		/**
		 * Integer
		 */
		MATTRIB,
		/**
		 * String
		 */
		LEIGE,
		/**
		 * Integer
		 */
		HEIGHT,
		/**
		 * Integer
		 */
		WEIGHT,
		/**
		 * String
		 */
		COLOR,
		/**
		 * String
		 */
		LASTIP,
		/**
		 * String
		 */
		EMAIL,
		/**
		 * List<Tattoo>
		 */
		TATTS,
		/**
		 * List<String>
		 */
		EXPERS,
		/**
		 * String
		 */
		ACCOUNT,
		/**
		 * List<Pair<String,Integer>> (factionid, value)
		 */
		FACTIONS,
		/**
		 * List<Triad<String,String,String>> (dbid, item class, item txt)
		 */
		INVENTORY,
		/**
		 * List<Ability>
		 */
		ABLES,
		/**
		 * List<CMObject> (behavior and ability objects)
		 */
		AFFBEHAV,
		/**
		 * List<Clan,Integer>
		 */
		CLANS
	}

	/**
	 * Sometimes the system does not need the entire mob object
	 * and all associated attributes to make decisions about
	 * a particular player char.  In cases like these, a smaller
	 * and more light-weight view of the char is available
	 * through this interface.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface ThinPlayer
	{
		/**
		 * Return the char name
		 * @return the char name
		 */
		public String name();

		/**
		 * Return the char char Class
		 * @return the char char Class
		 */
		public String charClass();

		/**
		 * Return the char race
		 * @return the char race
		 */
		public String race();

		/**
		 * Return the char level
		 * @return the char level
		 */
		public int level();

		/**
		 * Return the char age/hrs/mins
		 * @return the char age/hrs/mins
		 */
		public int age();

		/**
		 * Return the last date
		 * @return the last date
		 */
		public long last();

		/**
		 * Return the char email
		 * @return the char email
		 */
		public String email();

		/**
		 * Return the last ip
		 * @return the last ip
		 */
		public String ip();

		/**
		 * Return the char experience
		 * @return the char experience
		 */
		public int exp();

		/**
		 * Return the char experience next lvl
		 * @return the char experience next lvl
		 */
		public int expLvl();

		/**
		 * Return the char liege/mate
		 * @return the char liege/mate
		 */
		public String liege();

		/**
		 * Return the char worship/deity
		 * @return the char worship/deity
		 */
		public String worship();
	}

	/**
	 * The absolute smallest whole view of a player char
	 * is the ThinnerPlayer interface, which requires
	 * the least work pulling from the database,
	 * short of pulling only a single field.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface ThinnerPlayer
	{
		/**
		 * Get the character name
		 * @return the character name
		 */
		public String name();

		/**
		 * Builder to change the character name
		 * @param name the character name
		 * @return this
		 */
		ThinnerPlayer name(String name);

		/**
		 * Get the char password
		 * @return the char password
		 */
		public String password();

		/**
		 * Builder to change the char password
		 * @param password the char password
		 * @return this
		 */
		ThinnerPlayer password(String password);

		/**
		 * Get the char/acct expiration date
		 * @return the char/acct expiration date
		 */
		public long expiration();

		/**
		 * Builder to change the char/acct expiration date
		 * @param expiration the char/acct expiration date
		 * @return this
		 */
		ThinnerPlayer expiration(long expiration);

		/**
		 * Get the account name
		 * @return the account name
		 */
		public String accountName();

		/**
		 * Builder to change the account name
		 * @param accountName the account name
		 * @return this
		 */
		ThinnerPlayer accountName(String accountName);

		/**
		 * Get the char/acct email
		 * @return the char/acct email
		 */
		public String email();

		/**
		 * Builder to change the char/acct email
		 * @param email the char/acct email
		 * @return this
		 */
		ThinnerPlayer email(String email);
		/**
		 * Get the actual mob object
		 * @return the actual mob object
		 */
		public MOB loadedMOB();

		/**
		 * Builder to change the actual mob object
		 * @param mob the actual mob object
		 * @return this
		 */
		ThinnerPlayer loadedMOB(MOB mob);
	}
}
