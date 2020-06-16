package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
 * This interface denotes an ability governs the behavior of another
 * plane of existence, how its data is stored and retreived, how
 * the mobs and items and rooms appear and behave inside.
 */
public interface PlanarAbility extends Ability
{

	/**
	 * Get the name of the current plane
	 * @return the planarName
	 */
	public String getPlanarName();

	/**
	 * Set the plane to use for this planar ability
	 *
	 * @param planeName the plane to use
	 */
	public void setPlanarName(final String planeName);

	/**
	 * Get the current level of this plane
	 * @return the planarLevel
	 */
	public int getPlanarLevel();

	/**
	 * Change the planar level, hopefully before rooms
	 * are generated.
	 *
	 * @param level the level to change to.
	 */
	public void setPlanarLevel(final int level);

	/**
	 * Get the return room when leaving/entering this plane
	 * @return the oldRoom
	 */
	public Room getOldRoom();

	/**
	 * Set the return room when leaving/entering this plane
	 * @param oldRoom the oldRoom to set
	 */
	public void setOldRoom(Room oldRoom);

	/**
	 * The chosen-from-a-list prefix to use on mobs in this plane
	 * @return the planarPrefix
	 */
	public String getPlanarPrefix();

	/**
	 * Applies the room display and description coloring to the
	 * given room according to the rules of this plane.
	 *
	 * @param room the room to change permanently.
	 */
	public void doPlanarRoomColoring(final Room room);

	/**
	 * Applies the prefix and random promotions string to the given
	 * mob according to the parsed configuration for the current plane.
	 * Also takes an optional eliteBump 1-dimensional array to return
	 * the elite level of the mob applies.
	 *
	 * @param M the mob to apply
	 * @param eliteBump null, or 1-dimensional array with 0
	 */
	public void applyMobPrefix(final MOB M, final int[] eliteBump);

	/**
	 * Returns whether the given mob can be planarfied according
	 * to the parsed configuration for the current plane.
	 *
	 * @param M the mob to apply
	 * @return true if the mob can be planarfied
	 */
	public boolean isPlanarMob(final MOB M);

	/**
	 * The xtra difficulty level of this plane
	 * @return the hardBumpLevel
	 */
	public int getHardBumpLevel();

	/**
	 * Modify the xtra difficulty level of this plane
	 * @param hardBumpLevel the hardBumpLevel to set
	 */
	public void setHardBumpLevel(int hardBumpLevel);

	/**
	 * Get the key/pair definitions for this plane
	 * @return the planeVars
	 */
	public Map<String, String> getPlaneVars();

	/**
	 * Creates a new, or edits an existing plane.  The rule must
	 * be the same format as found in planesofexistence.txt, except
	 * that it should not start with the plane name in quotes.
	 *
	 * The result value will start with "ERROR:" if there are one
	 * or more errors.  Otherwise, it will contains the fields
	 * modified or null if the ADD was successful.
	 *
	 * @param planeName the plane to add or edit
	 * @param rule the new rule definition
	 * @return what was done.
	 */
	public String addOrEditPlane(final String planeName, final String rule);

	/**
	 * Removes a plane of existence, forever.
	 * @param planeName the name of the plane to delete
	 * @return true if the delete was successful, false otherwise
	 */
	public boolean deletePlane(final String planeName);

	/**
	 * Get the pct change and name of available
	 * mob promotions for this plane
	 * @return the promotions
	 */
	public PairList<Integer, String> getPromotions();

	/**
	 * Get the categories that apply to this plane
	 * @return the categories
	 */
	public List<String> getCategories();

	/**
	 * Get the planes that oppose this plane
	 * @return the opposed planes
	 */
	public List<String> getOpposed();

	/**
	 * Get the list of behaviors and parms for this plane
	 * @return the behavList
	 */
	public PairList<String, String> getBehavList();

	/**
	 * Get the list of room effects and args for this plane
	 * @return the reffectList
	 */
	public PairList<String, String> getReffectList();

	/**
	 * Get the list of faction ids and values for this plane
	 * @return the factionList
	 */
	public PairList<String, String> getFactionList();

	/**
	 * Calculates the area effects and behaviors, not including
	 * absorb lists, which would affect the planar area.
	 *
	 * @return the list of abilities and behaviors
	 */
	public List<CMObject> getAreaEffectsBehavs();

	/**
	 * Get the CharStat STAT_* ID of the stat that gives bonus
	 * damage on this plane
	 * @return the bonusDmgStat
	 */
	public int getBonusDmgStat();

	/**
	 * Get the seq of required weapon flags for hurting things
	 * in this plane.
	 * @return the reqWeapons
	 */
	public Set<String> getReqWeapons();

	/**
	 * Get the number of extra recover ticks for players on this plane
	 * @return the recoverRate
	 */
	public int getRecoverRate();

	/**
	 * Get the extra fatigue ticks for players on this plane
	 * @return the fatigueRate
	 */
	public int getFatigueRate();

	/**
	 * Get the special attribute flags for this plane
	 * @return the specFlags
	 */
	public Set<PlanarSpecFlag> getSpecFlags();

	/**
	 * Get the mob/item level adjustment formula for this plane.
	 * (at)x1 = base areas median level, (at)x2 = specific mob/item level
	 * (at)@x2 = the plane traveling players level
	 * @return the levelFormula
	 */
	public CompiledFormula getLevelFormula();

	/**
	 * Get the bonus ability list for this plane.
	 * Clearly, it's complicated.
	 * @return the enableList
	 */
	public PairList<Pair<Integer, Integer>, PairList<String, String>> getEnableList();

	/**
	 * Get the definition for the given plane
	 * @param planeName the name of the plane to get definitions for
	 * @return the definitions map
	 */
	public Map<String,String> getPlanarVars(String planeName);

	/**
	 * Return the list of all non prime-material planes.
	 * These can be use as keys to the planar var maps.
	 * @return the complete list of planes of existence
	 */
	public List<String> getAllPlaneKeys();

	/**
	 * Returns a friendly list of all the planes.
	 *
	 * @return a friendly list.
	 */
	public String listOfPlanes();


	/**
	 * Completely destroyed the given plane of existence, kicking
	 * all players inside it back to the starting room they came
	 * from.
	 *
	 * @param planeA the plane to destroy.
	 */
	public void destroyPlane(final Area planeA);

	/**
	 * The definitions variables for the attributes of each plane
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum PlanarVar
	{
		ID,
		TRANSITIONAL,
		ALIGNMENT,
		PREFIX,
		LEVELADJ,
		MOBRESIST,
		SETSTAT,
		BEHAVAFFID,
		ADJSTAT,
		ADJSIZE,
		ADJUST,
		MOBCOPY,
		BEHAVE,
		ENABLE,
		WEAPONMAXRANGE,
		BONUSDAMAGESTAT,
		REQWEAPONS,
		ATMOSPHERE,
		AREABLURBS,
		ABSORB,
		HOURS,
		RECOVERRATE,
		FATIGUERATE,
		REFFECT,
		AEFFECT,
		SPECFLAGS,
		MIXRACE,
		ELITE,
		ROOMCOLOR,
		ROOMADJS,
		FACTIONS,
		CATEGORY,
		OPPOSED,
		PROMOTIONS,
		LIKE,
		DESCRIPTION
	}

	/**
	 * The special attribute flags for planes
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum PlanarSpecFlag
	{
		NOINFRAVISION,
		BADMUNDANEARMOR,
		ALLBREATHE
	}

}
