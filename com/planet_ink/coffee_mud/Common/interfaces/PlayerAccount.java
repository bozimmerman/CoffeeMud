package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.interfaces.Modifiable;
import com.planet_ink.coffee_mud.core.interfaces.Tattooable;

/**
 * An interface for a base player account.  If this system is enabled, this
 * represents essentially a "container" for various characters, who
 * share a login and potentially an expiration date.
 */
public interface PlayerAccount extends CMCommon, AccountStats, Modifiable, Tattooable
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
	 * Removes a player of this name from this account.
	 * @param name the name of the player to remove.
	 */
	public void delPlayer(String name);

	/**
	 * Retrieves a fake account mob, for forum and
	 * other access systems not directly relayed to gameplay.
	 * @return mob the fake player.
	 */
	public MOB getAccountMob();

	/**
	 * Returns the real name if the player is on this account
	 * @param name the name look for check
	 * @return real name if it exists and null otherwise
	 */
	public String findPlayer(String name);

	/**
	 * Returns this accounts name
	 * @return this accounts name
	 */
	public String getAccountName();

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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#setFlag(AccountFlag, boolean)
	 * @param flag the flag name
	 * @return true if it is set, false if not
	 */
	public boolean isSet(AccountFlag flag);

	/**
	 * Sets or unsets an account-wide flag.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount#isSet(AccountFlag)
	 * @param flag the flag name
	 * @param setOrUnset true to set it, false to unset
	 */
	public void setFlag(AccountFlag flag, boolean setOrUnset);

	/**
	 * Returns the number of bonus characters online available to
	 * this account.
	 * 
	 * @see PlayerAccount#setBonusCharsOnlineLimit(int)
	 * 
	 * @return the number of bonus chars online
	 */
	public int getBonusCharsOnlineLimit();

	/**
	 * Sets the number of bonus characters online available to
	 * this account.
	 * 
	 * @see PlayerAccount#getBonusCharsOnlineLimit()
	 * 
	 * @param bonus the number of bonus chars online
	 */
	public void setBonusCharsOnlineLimit(int bonus);

	/**
	 * Returns the number of bonus characters available to
	 * this account.
	 * 
	 * @see PlayerAccount#setBonusCharsLimit(int)
	 * 
	 * @return the number of bonus chars
	 */
	public int getBonusCharsLimit();

	/**
	 * Sets the number of bonus characters available to
	 * this account.
	 * 
	 * @see PlayerAccount#getBonusCharsLimit()
	 * 
	 * @param bonus the number of bonus chars
	 */
	public void setBonusCharsLimit(int bonus);

	/**
	 * Populates this account object with all the data
	 * from the given one, replacing any existing internal
	 * data.
	 * @param otherAccount the data to copy from.
	 */
	public void copyInto(PlayerAccount otherAccount);
	
	/**
	 * Various account-level flags
	 * @author Bo Zimmerman
	 *
	 */
	public enum AccountFlag
	{
		/** Constant for account flags that overrides number of characters limitation */
		NUMCHARSOVERRIDE,
		/** Constant for account flags that overrides account expiration */
		NOEXPIRE,
		/** Constant for account flags that overrides account expiration */
		CANEXPORT,
		/** Constant for account flags that overrides account expiration */
		MAXCONNSOVERRIDE,
		/** Constant for account flags that overrides account expiration */
		ANSI,
		/** Constant for account flags that overrides account expiration */
		ACCOUNTMENUSOFF
		;
		/**
		 * Returns a comma-delimited list of strings representing the accountflag values
		 * @return a comma-delimited list of strings representing the accountflag values
		 */
		public static String getListString()
		{
			return CMParms.toListString(AccountFlag.values());
		}
	}
}
