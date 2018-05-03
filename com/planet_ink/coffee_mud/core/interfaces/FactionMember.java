package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.Common.interfaces.Tattoo;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/*
   Copyright 2015-2018 Bo Zimmerman

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
*
* Something that can belong to one or more factions, and have rank
* (also called "faction") with it.
* @see com.planet_ink.coffee_mud.Common.interfaces.Faction
* @author Bo Zimmerman
*
*/
public interface FactionMember
{
	/**
	 * Adds a new faction to this member, with the given initial rank
	 * @see FactionMember
	 * @param of the facton ID to add to this
	 * @param start the initial rank/value in the new faction
	 */
	public void addFaction(String of, int start);
	
	/**
	 * If this is already a member of the given faction, then the 
	 * value or rank in that faction will be adjusted by the given
	 * amount.  If this is NOT yet a member of the given faction, then
	 * the faction is added with the amount as an initial value.
	 * @see FactionMember
	 * @param of the facton ID to add or alter
	 * @param amount the amount to alter the faction by, or initial value
	 */
	public void adjustFaction(String of, int amount);
	
	/**
	 * Returns an enumeration of all the faction id this is a member of.
	 * @see FactionMember
	 * @return an enumeration of all the faction id this is a member of.
	 */
	public Enumeration<String> factions();
	
	/**
	 * Returns an enumeration of the faction range ids that represent the
	 * rank that this member has in each of their factions.  So there is
	 * one range returned per faction.
	 * @see FactionMember
	 * @return an enumeration of the faction ranges
	 */
	public List<String> fetchFactionRanges();
	
	/**
	 * Returns whether this is a member of the given faction and has some
	 * rank.
	 * @see FactionMember
	 * @param which the faction id to search for
	 * @return true if this is a member of the given faction, false otherwise
	 */
	public boolean hasFaction(String which);
	
	/**
	 * Returns the faction rank/value that this member has in the given
	 * faction id.
	 * @see FactionMember
	 * @param which the faction id to return rank/value in
	 * @return the rank value, or Integer.MAX_VALUE if no value found
	 */
	public int fetchFaction(String which);
	
	/**
	 * Returns a friendly viewable list of all the factions that this is
	 * a member of, along with the rank/value this member has in that
	 * faction.  The list is semicolon-delimited.
	 * @see FactionMember
	 * @return friendly viewable list of all the factions
	 */
	public String getFactionListing();
	
	/**
	 * Removes this as a member of the given faction, losing all rank.
	 * @see FactionMember
	 * @param which the faction id to remove
	 */
	public void removeFaction(String which);
	
	/**
	 * Copies the factions that the given member belongs to into this.
	 * @see FactionMember
	 * @param source the source of the factions to copy
	 */
	public void copyFactions(FactionMember source);
}
