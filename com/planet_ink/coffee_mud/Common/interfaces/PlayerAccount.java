package com.planet_ink.coffee_mud.Common.interfaces;

import java.util.Enumeration;
import java.util.Vector;

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
}
