package com.planet_ink.coffee_mud.core.interfaces;
import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
/*
   Copyright 2011-2024 Bo Zimmerman

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
import com.planet_ink.coffee_mud.core.CMLib;

/**
 * An abstract interface for something capable of grouping mobs together
 * @author Bo Zimmerman
 *
 */
public interface MOBCollection
{

	/**
	 * Searches the inhabitants of this room for a mob with the given
	 * ID(), name, or display name.  If nothing is found, it does a
	 * substring search as well.  This method also respects index
	 * suffixes, such as .1, .2 to specify which of identical mobs
	 * to return.
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchInhabitantExact(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param inhabitantID the name, id, or keyword to search for
	 * @return the first mob to match the search string
	 */
	public MOB fetchInhabitant(String inhabitantID);

	/**
	 * Searches the inhabitants of this room for a mob with the given
	 * ID(), name, or display name.  This method also respects index
	 * suffixes, such as .1, .2 to specify which of identical mobs
	 * to return.
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param inhabitantID the name, id, or keyword to search for
	 * @return the first mob to match the search string
	 */
	public MOB fetchInhabitantExact(String inhabitantID);

	/**
	 * Searches the inhabitants of this room for mobs with the given
	 * ID(), name, or display name.  If nothing is found, it does a
	 * substring search as well.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param inhabitantID the name, id, or keyword to search for
	 * @return all the mobs that match the search string
	 */
	public List<MOB> fetchInhabitants(String inhabitantID);

	/**
	 * Returns the inhabitant mob in this room at the given
	 * index, or null if there are none at that index. The
	 * index is, of course, 0 based.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param i the index of the mob
	 * @return the mob inhabitant at that index
	 */
	public MOB fetchInhabitant(int i);

	/**
	 * Returns an enumeration of all the inhabitants of
	 * this room.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @return an enumeration of all the inhabitants
	 */
	public Enumeration<MOB> inhabitants();

	/**
	 * Adds the given mob to this room as an inhabitant.
	 * Does *not* register the new location with the mob,
	 * so you would need to also call setLocation
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#setLocation(Room)
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param mob the mob to add to this room
	 */
	public void addInhabitant(MOB mob);

	/**
	 * Removes the given mob from this room as an inhabitant.
	 * Does *not* un-register the new location with the mob,
	 * so you would need to also call setLocation
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#setLocation(Room)
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param mob the mob to remove from this room
	 */
	public void delInhabitant(MOB mob);

	/**
	 * Returns the number of all the inhabitants of
	 * this room.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#inhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @return the number of inhabitants
	 */
	public int numInhabitants();

	/**
	 * Returns whether the given mob is an inhabitant of this room.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param mob the mob to look for
	 * @return true if the mob is here, false otherwise
	 */
	public boolean isInhabitant(MOB mob);

	/**
	 * Removes all the mobs from this room as inhabitants and
	 * optionally destroys the mob objects as well.
	 * Does *not* un-register the new location with the mob,
	 * so you would need to also call setLocation if you don't
	 * destroy them.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#setLocation(Room)
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#fetchRandomInhabitant()
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#addInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @param destroy true to also destroy the mob objects, false otherwise
	 */
	public void delAllInhabitants(boolean destroy);

	/**
	 * Returns a random inhabitant mob in this room, or null
	 * if there are none.
	 * @see MOBCollection#fetchInhabitant(String)
	 * @see MOBCollection#fetchInhabitant(int)
	 * @see MOBCollection#fetchInhabitants(String)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#delAllInhabitants(boolean)
	 * @see MOBCollection#inhabitants()
	 * @see MOBCollection#delInhabitant(MOB)
	 * @see MOBCollection#isInhabitant(MOB)
	 * @see MOBCollection#numInhabitants()
	 * @see MOBPossessor#bringMobHere(MOB, boolean)
	 * @see Room#numPCInhabitants()
	 * @see Room#eachInhabitant(EachApplicable)
	 * @return the random mob inhabitant
	 */
	public MOB fetchRandomInhabitant();

}
