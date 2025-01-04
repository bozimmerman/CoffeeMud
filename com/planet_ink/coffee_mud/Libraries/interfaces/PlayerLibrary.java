package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnections;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PrideCat;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2008-2025 Bo Zimmerman

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
/**
 * The Player Manager is primarily a cache and lookup
 * utility library for player character mobs and player
 * accounts if the account system is enabled.  It is mostly
 * optimized to cache as few players as possible, providing
 * many methods of getting and changing player character
 * information w/o a permanent load.
 *
 * @author Bo Zimmerman
 *
 */
public interface PlayerLibrary extends CMLibrary
{
	/**
	 * Returns null or an account of the given name if it
	 * exists in the database managed by this player lib.
	 *
	 * @see PlayerLibrary#getLoadAccountByEmail(String)
	 *
	 * @param calledThis the name of the account
	 * @return the account found, or null
	 */
	public PlayerAccount getLoadAccount(String calledThis);
	/**
	 * Returns null or an account of the given email addy if it
	 * exists in the database managed by this player lib.
	 *
	 * @see PlayerLibrary#getLoadAccount(String)
	 *
	 * @param email the email of the account
	 * @return the account found, or null
	 */
	public PlayerAccount getLoadAccountByEmail(String email);

	/**
	 * Returns null or an account of the given name if it
	 * exists in the cache managed by this player lib.
	 *
	 * @see PlayerLibrary#getAccountAllHosts(String)
	 *
	 * @param calledThis the name of the account
	 * @return the account found, or null
	 */
	public PlayerAccount getAccount(String calledThis);

	/**
	 * Returns null or an account of the given name if it
	 * exists in the cache managed by this player lib. If
	 * not found, all other player libraries are consulted
	 * if they share the same map as this one.
	 *
	 * @see PlayerLibrary#getAccount(String)
	 *
	 * @param calledThis the name of the account
	 * @return the account found, or null
	 */
	public PlayerAccount getAccountAllHosts(String calledThis);

	/**
	 * Adds a new account to this player manager, for
	 * caching only.
	 *
	 * @param acct the new account
	 */
	public void addAccount(PlayerAccount acct);

	/**
	 * Returns whether an account of the given name exists
	 * in the database managed by this player lib.
	 *
	 * @see PlayerLibrary#accountExistsAllHosts(String)
	 *
	 * @param name the name of the account
	 * @return true if the account exists
	 */
	public boolean accountExists(String name);

	/**
	 * Returns whether an account of the given name exists
	 * in the database managed by this player lib.  If
	 * not found, all other player libraries are consulted
	 * if they share the same map as this one.
	 *
	 * @see PlayerLibrary#accountExists(String)
	 *
	 * @param name the name of the account
	 * @return true if the account exists
	 */
	public boolean accountExistsAllHosts(String name);

	/**
	 * Returns an enumeration of all player account objects
	 *
	 * @see PlayerLibrary#accounts(String, Map)
	 *
	 * @return an enumeration of all player account objects
	 */
	public Enumeration<PlayerAccount> accounts();

	/**
	 * Returns an enumeration of all account objects, with optional sort field,
	 * and optional cache so that subsequent calls are faster.
	 * The sort field is a AcctThinSortCode name.
	 * The cache is a map that can contain a field called "ACCOUNTLISTVECTOR" plus
	 * the sort field, and would be the vector of accounts that can be
	 * enumerated over again.
	 *
	 * @see PlayerLibrary#accounts()
	 *
	 * @param sort null, or AcctThinSortCode name
	 * @param cache map that can contain a cashed enum vector
	 * @return the enum of ALL player accounts
	 */
	public Enumeration<PlayerAccount> accounts(String sort, Map<String, Object> cache);

	/**
	 * Given two player characters, this will return whether they either
	 * share an account.
	 *
	 * @see PlayerLibrary#isSameAccount(MOB, MOB)
	 * @see PlayerLibrary#isSameAccountIP(MOB, MOB)
	 *
	 * @param player1 the first player mob
	 * @param player2 the second player mob
	 * @return whether they are on the same account
	 */
	public boolean isSameAccount(final MOB player1, final MOB player2);

	/**
	 * Given two player characters, this will return whether they either
	 * share an account, or the same ip address.
	 *
	 * @see PlayerLibrary#isSameAccount(MOB, MOB)
	 * @see PlayerLibrary#isSameAccountIP(MOB, MOB)
	 *
	 * @param player1 the first player mob
	 * @param player2 the second player mob
	 * @return whether they are probably the same person
	 */
	public boolean isSameAccountIP(final MOB player1, final MOB player2);

	/**
	 * Removes the given player account from the list of protected
	 * names, and then deletes the account from the manager cache, from
	 * the database, and from the world.  It does not delete the
	 * players in the account, like, at all, so it should be done
	 * last in that process.
	 *
	 * @param deadAccount the account to obliterate
	 */
	public void obliterateAccountOnly(PlayerAccount deadAccount);

	/**
	 * Returns the number of player character mobs currently
	 * cached by this manager.
	 *
	 * @see PlayerLibrary#delPlayer(MOB)
	 * @see PlayerLibrary#addPlayer(MOB)
	 * @see PlayerLibrary#players()
	 *
	 * @return the number of cached players
	 */
	public int numPlayers();

	/**
	 * Adds a reference to the given player char mob to this
	 * manager cache.
	 *
	 * @see PlayerLibrary#delPlayer(MOB)
	 * @see PlayerLibrary#numPlayers()
	 * @see PlayerLibrary#players()
	 *
	 * @param newOne the player mob to add
	 */
	public void addPlayer(MOB newOne);

	/**
	 * Deletes a reference to a player character mob from the
	 * cache.  This does not completely remove the mob from
	 * the game itself, or all their stuff, and is thus
	 * somewhat dangerous to call.
	 *
	 * @see PlayerLibrary#addPlayer(MOB)
	 * @see PlayerLibrary#numPlayers()
	 * @see PlayerLibrary#players()
	 *
	 * @param oneToDel the player char mob to remove from the cache
	 */
	public void delPlayer(MOB oneToDel);

	/**
	 * Returns an enumeration of all cached players in this
	 * manager.
	 *
	 * @see PlayerLibrary#delPlayer(MOB)
	 * @see PlayerLibrary#addPlayer(MOB)
	 * @see PlayerLibrary#numPlayers()
	 *
	 * @return the enumeration of cached player mobs
	 */
	public Enumeration<MOB> players();

	/**
	 * Given a player character name, this will search the list
	 * of cached player mobs and return the one with that name.
	 *
	 *  @see PlayerLibrary#getPlayerAllHosts(String)
	 *
	 * @param calledThis the cached player to get
	 * @return null if not cached, or the player char mob
	 */
	public MOB getPlayer(String calledThis);

	/**
	 * Given a player character name, this will search the list
	 * of cached player mobs and return the one with that name.
	 * If not found in this player manager, it will search all
	 * others that share the same map.
	 *
	 *  @see PlayerLibrary#getPlayer(String)
	 *
	 * @param calledThis the cached player to get
	 * @return null if not cached, or the player char mob
	 */
	public MOB getPlayerAllHosts(String calledThis);

	/**
	 * Given a player name, or substring for searching,
	 * this will return a player character object that is presently
	 * online and in the game.
	 *
	 * @param srchStr the name, or substring if Not exact only
	 * @param exactOnly true to only search full names, false for substring
	 * @return null, or the found online player
	 */
	public MOB findPlayerOnline(final String srchStr, final boolean exactOnly);

	/**
	 * Finds a player character who has the given unique name,
	 * first checking the manager cache, and if not found, goes
	 * to the associated database and loads the character into
	 * the cache and then returns it.
	 *
	 * @see PlayerLibrary#getLoadPlayerByEmail(String)
	 * @see PlayerLibrary#getLoadPlayerAllHosts(String)
	 *
	 * @param last the character name
	 * @return null, or the character found
	 */
	public MOB getLoadPlayer(String last);


	/**
	 * Finds a player character who has the given unique name,
	 * first checking the manager cache, and if not found, goes
	 * to the associated database and loads the character into
	 * the cache and then returns it.  If not found there, it
	 * checks all other player managers who share a map with
	 * this one.
	 *
	 * @see PlayerLibrary#getLoadPlayerByEmail(String)
	 * @see PlayerLibrary#getLoadPlayer(String)
	 *
	 * @param last the character name
	 * @return null, or the character found
	 */
	public MOB getLoadPlayerAllHosts(final String last);

	/**
	 * Finds a player character who has the given email address
	 * (possibly from their account), and loads that character
	 * into the player manager cache and then returns it.  It
	 * will check the cache first, of course.
	 *
	 * @see PlayerLibrary#getLoadPlayer(String)
	 * @see PlayerLibrary#getLoadPlayerAllHosts(String)
	 *
	 * @param email email address to find a player for
	 * @return null, or the player char mob found
	 */
	public MOB getLoadPlayerByEmail(String email);

	/**
	 * Attempts to return the thread id associated with
	 * the given player.  It first checks for a session
	 * with a thread id, and then it looks for a player
	 * library containing the mob, returning its id.
	 *
	 * @param mob the player to look for
	 * @return thread id, or -1 if not found.
	 */
	public int getPlayerThreadId(final MOB mob);

	/**
	 * Returns a list of all player char names
	 * in the database.
	 *
	 * @see PlayerLibrary#getPlayerListsAllHosts()
	 *
	 * @return list of all player names
	 */
	public List<String> getPlayerLists();

	/**
	 * Returns a list of all player char names from all
	 * databases connected to the same map as this
	 * player manager.
	 *
	 * @see PlayerLibrary#getPlayerLists()
	 *
	 * @return list of all player names
	 */
	public List<String> getPlayerListsAllHosts();

	/**
	 * Returns whether the given player char mob is currently
	 * cached by this player manager.
	 *
	 * @see PlayerLibrary#isLoadedPlayer(String)
	 *
	 * @param M the player char
	 * @return true if cached, false otherwise
	 */
	public boolean isLoadedPlayer(final MOB M);

	/**
	 * Returns whether the given player char name is currently
	 * cached by this player manager.
	 *
	 * @see PlayerLibrary#isLoadedPlayer(MOB)
	 *
	 * @param mobName the player char name
	 * @return true if cached, false otherwise
	 */
	public boolean isLoadedPlayer(final String mobName);

	/**
	 * Given a player name, this will attempt to find them and
	 * return whether they actually exist regardless of whether
	 * they've been cached or not.
	 *
	 * @see PlayerLibrary#playerExistsAllHosts(String)
	 *
	 * @param name the player name
	 * @return true if the player exists anywhere
	 */
	public boolean playerExists(String name);

	/**
	 * Given a player name, this will attempt to find them and
	 * return whether they actually exist regardless of whether
	 * they've been cached or not, and checking all user
	 * databases that share a map with the caller.
	 *
	 * @see PlayerLibrary#playerExists(String)
	 *
	 * @param name the player name
	 * @return true if the player exists anywhere
	 */
	public boolean playerExistsAllHosts(String name);

	/**
	 * Given a player name, this will attempt to find the name of the
	 * liege of this player, regardless of whether they've been cached
	 * or not, and checking all user databases that share a map with
	 * the caller.
	 *
	 * @param userName the player char name
	 * @return "", or the name of the player's liege
	 */
	public String getLiegeOfUserAllHosts(final String userName);

	/**
	 * Obliterates a cached loaded player character from the
	 * database, from the cache, from everything.  Deletes a character
	 * completely and forever.
	 *
	 * @param deadMOB the player char mob object
	 * @param deleteAssets true to send retirement msg to the world and kill player data
	 * @param quiet true to do this silently, false otherwise
	 */
	public void obliteratePlayer(MOB deadMOB, boolean deleteAssets, boolean quiet);

	/**
	 * Renaming a player is quite involved, as there are so many tables and
	 * objects index by the players names.  Calling this method attempts
	 * to take care of as much as possible.  To use it, first set the mob
	 * objects new name, then call this with the old one.
	 *
	 * @param mob the player char who has been renamed
	 * @param oldName the previous name
	 */
	public void renamePlayer(MOB mob, String oldName);

	/**
	 * Given a player char mob object, this will ensure that
	 * the char is indeed not currently in the game and, if
	 * so, remove them from the player cache without saving
	 * them.
	 *
	 * @param mob the player char to unload
	 */
	public void unloadOfflinePlayer(final MOB mob);

	/**
	 * Saves all cached players.  If the INI file does not
	 * support the player cache, this will throw out players who
	 * are offline, but otherwise leave them cached.
	 *
	 * @return the number of players saved
	 */
	public int savePlayers();

	/**
	 * Saves the given player, and all their followers and
	 * all their things, no matter what.
	 *
	 * @param mob the player to save
	 * @return true if the player saves, false otherwise
	 */
	public boolean savePlayer(final MOB mob);

	/**
	 * Factory method for a ThinnerPlayer object, so that
	 * new features can be added in the future w/o having
	 * to change many files.
	 *
	 * @see PlayerLibrary.ThinnerPlayer
	 *
	 * @return a ThinnerPlayer object you can modify
	 */
	public PlayerLibrary.ThinnerPlayer newThinnerPlayer();

	/**
	 * Given a player name, this will construct a ThinPlayer
	 * object if the player char is already cached, or
	 * a new one from the database if not.
	 *
	 * @see PlayerLibrary.ThinPlayer
	 * @see PlayerLibrary#newThinnerPlayer()
	 * @see PlayerLibrary#thinPlayers(String, Map)
	 *
	 * @param mobName the char name
	 * @return the ThinPlayer
	 */
	public ThinPlayer getThinPlayer(final String mobName);

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
	public Object getThinSortValue(ThinPlayer player, PlayerSortCode code);

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
	 * Constructs a ThinPlayer object out of a Modifiable that implements the following
	 * fields: NAME, CHARCLASS, RACE, GENDER, LEVEL, AGE, LAST, EMAIL, IP, EXPERIENCE,
	 * EXPERIENCENEEDED, LIEGE, DEITY, CLAN
	 * @param mP the Modifiable object to use
	 * @return the ThinPlayer, entirely fake
	 */
	public ThinPlayer makeThinModifiablePlayer(final Modifiable mP);

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
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 */
	public void resetAllPrideStats();

	/**
	 * Given an array of timestamps indexed by period ordinals, and previous
	 * pride stat values similarly indexed, this will generate an array
	 * of pairs representing the data.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
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
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param mob the player char who did something
	 * @param stat the pride stat reflecting what they did
	 * @param amt the amount to change the stat by + or -
	 * @return the give amt, or 0
	 */
	public int bumpPrideStat(final MOB mob, final PrideStats.PrideStat stat, final int amt);

	/**
	 * Returns the top winning character names and the associated values that got them there, for the given time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top character for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getTopPridePlayers(TimeClock.TimePeriod period, PrideStats.PrideStat stat);

	/**
	 * Returns the top winning character names and the associated values that got them there, for the previous time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top character for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getPreviousTopPridePlayers(TimeClock.TimePeriod period, PrideStats.PrideStat stat);

	/**
	 * Returns the top winning character names and the associated values that got them there, for the given time
	 * period and given pridestat, in the given Pride Category, for the given Pride Category value.
	 *
	 * @see PlayerLibrary#getPreviousTopPridePlayers(PrideCat, String, com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @param category the pride category from the INI file, like RACE, CLASS, etc..
	 * @param catUnit the category value to return data for, like Orc, Fighter, etc..
	 * @param period the time period to get the top accounts for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getTopPridePlayers(final PrideCat category, final String catUnit, final TimeClock.TimePeriod period, final PrideStats.PrideStat stat);

	/**
	 * Returns the top winning character names and the associated values that got them there, for the previous time
	 * period and given pridestat, in the given Pride Category, for the given Pride Category value.
	 *
	 * @see PlayerLibrary#getTopPridePlayers(PrideCat, String, com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @param category the pride category from the INI file, like RACE, CLASS, etc..
	 * @param catUnit the category value to return data for, like Orc, Fighter, etc..
	 * @param period the time period to get the top accounts for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getPreviousTopPridePlayers(final PrideCat category, final String catUnit, final TimeClock.TimePeriod period, final PrideStats.PrideStat stat);

	/**
	 * Returns the top winning account names and the associated values that got them there, for the given time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPrideAccounts(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top accounts for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getTopPrideAccounts(TimeClock.TimePeriod period, PrideStats.PrideStat stat);

	/**
	 * Returns the top winning account names and the associated values that got them there, for the previous given time
	 * period and given pridestat.
	 *
	 * @see PlayerLibrary#getTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * @see PlayerLibrary#getPreviousTopPridePlayers(com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat)
	 * #see PlayerLibrary#bumpPrideStat(MOB, com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat, int)
	 * @see PlayerLibrary#parsePrideStats(String[], String[])
	 * @see PlayerLibrary#resetAllPrideStats()
	 *
	 * @param period the time period to get the top accounts for
	 * @param stat the pridestat to find winners for
	 * @return the list of top winners
	 */
	public List<Pair<String,Integer>> getPreviousTopPrideAccounts(TimeClock.TimePeriod period, PrideStats.PrideStat stat);

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
	 * Public supported pride stat categories
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum PrideCat
	{
		ACCOUNT, // global placeholder
		PLAYER, // global placeholder
		CLASS,
		RACE,
		BASECLASS,
		RACECAT,
		LEVEL,
		GENDER,
		CLAN
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
		 * List&lt;Coin&gt;
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
		 * List&lt;Tattoo&gt;
		 */
		TATTS,
		/**
		 * List&lt;String&gt;
		 */
		EXPERS,
		/**
		 * String
		 */
		ACCOUNT,
		/**
		 * List&lt;Pair&lt;String,Integer&gt;&gt; (factionid, value)
		 */
		FACTIONS,
		/**
		 * String[] (dbid, item class, item txt, loID, worn, uses, lvl, abilty, heit)
		 */
		INVENTORY,
		/**
		 * List&lt;Ability&gt;
		 */
		ABLES,
		/**
		 * List&lt;CMObject&gt; (behavior and ability objects)
		 */
		AFFBEHAV,
		/**
		 * List&lt;Clan,Integer&gt;
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

		/**
		 * Return the char gender name
		 * @return the char gender name
		 */
		public String gender();

		/**
		 * Returns an enumerator over this mobs clans.
		 * This may result in a query, so use sparingly.
		 */
		public Enumeration<String> clans();
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
