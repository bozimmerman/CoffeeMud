package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/**
 * An interface for something capable of possessing MOBs
 * @author Bo Zimmerman
 *
 */
public interface MOBPossessor extends MOBCollection
{
	/**
	 * A workhorse method that removes the given mob (and anything
	 * they are riding or being ridden by, recursively, and
	 * optionally any followers, and their riders, recursively)
	 * and places them all in this room. It does not affect those
	 * that the given mob are themselves following, however.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#inhabitants()
	 * @see Room#numPCInhabitants()
	 * @see MOBCollection#numInhabitants()
	 * @see MOBCollection#eachInhabitant(EachApplicable)
	 * @param mob the mob to move from where he is, to here
	 * @param andFollowers true to include followers, false otherwise
	 */
	public void bringMobHere(MOB mob, boolean andFollowers);

}
