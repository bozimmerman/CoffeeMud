package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.CMModifiable;

/**
 * An interface for a base player account.  If this system is enabled, this
 * represents essentially a "container" for various characters, who
 * share a login and potentially an expiration date.
 */
public interface PlayerAccount extends CMCommon, AccountStats, CMModifiable
{
	/**
	 * Return an enumeration of the fully loaded players
	 * that belong to this account.
	 * @return an enumeration of player mob objects
	 */
	public Enumeration<MOB> getLoadPlayers();
	
	/**
	 * Return an enumeration of the semi-loaded players
	 * that belong to this account.
	 * @return an enumeration of thinplayer objects
	 */
	public Enumeration<PlayerLibrary.ThinPlayer> getThinPlayers();
	
	/**
	 * Returns the number of players this account currently 
	 * has listed.
	 * @return the number of players 
	 */
	public int numPlayers();
	
	/**
	 * Return an enumeration of the players names
	 * that belong to this account.
	 * @return an enumeration of player names
	 */
	public Enumeration<String> getPlayers();
	
	/**
	 * Adds a new player to this account.
	 * @param mob the new player to add.
	 */
	public void addNewPlayer(MOB mob);
	
	/**
	 * Removes a player from this account.
	 * This is typically a precursor to deleting the player.
	 * @param mob the player to delete.
	 */
	public void delPlayer(MOB mob);

	/**
	 * Retrieves a fake account mob, for forum and 
	 * other access systems not directly relayed to gameplay.
	 * @return mob the fake player.
	 */
	public MOB getAccountMob();
	
	/**
	 * Returns whether the name is a player on this account 
	 * @param name the name to check
	 * @return true if it exists and false otherwise
	 */
	public boolean isPlayer(String name);
	
	/**
	 * Returns this accounts name
	 * @return this accounts name
	 */
	public String accountName();
	
	/**
	 * Sets this accounts unique name
	 * @param name the accounts name
	 */
	public void setAccountName(String name);
	
	/**
	 * Sets the names of all the players that belong to this account
	 * @param names the names of the players
	 */
	public void setPlayerNames(Vector<String> names);
	
	/**
	 * Checks whether the given string flag is set for this account.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#setFlag(String, boolean)
	 * @param flagName the flag name
	 * @return true if it is set, false if not
	 */
	public boolean isSet(String flagName);
	
	/**
	 * Sets or unsets an account-wide flag.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#isSet(String)
	 * @param flagName the flag name
	 * @param setOrUnset true to set it, false to unset
	 */
	public void setFlag(String flagName, boolean setOrUnset);
	
	/** Constant for account flags that overrides number of characters limitation */
	public final static String FLAG_NUMCHARSOVERRIDE="NUMCHARSOVERRIDE";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_NOEXPIRE="NOEXPIRE";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_CANEXPORT="CANEXPORT";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_MAXCONNSOVERRIDE="MAXCONNSOVERRIDE";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_ANSI="ANSI";
	/** Constant for account flags that overrides account expiration */
	public final static String FLAG_ACCOUNTMENUSOFF="ACCOUNTMENUSOFF";
	
	/** list of account flags */
	public final static String[] FLAG_DESCS = {FLAG_NUMCHARSOVERRIDE,FLAG_NOEXPIRE,FLAG_CANEXPORT,FLAG_MAXCONNSOVERRIDE,FLAG_ANSI,FLAG_ACCOUNTMENUSOFF};
}
