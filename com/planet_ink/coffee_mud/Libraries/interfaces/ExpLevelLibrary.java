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
   Copyright 2006-2018 Bo Zimmerman

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
public interface ExpLevelLibrary extends CMLibrary
{
	/**
	 * Returns how much experience a player must have to be the given level.
	 * 
	 * @param level the level to base the exp on
	 * @return the amount of experiene required to be the given level
	 */
	public int getLevelExperience(int level);

	/**
	 * Returns how much experience a player must gain at this level to advance
	 * to the next.
	 * 
	 * @param level the level to check at
	 * @return the amount of experience
	 */
	public int getLevelExperienceJustThisLevel(int level);

	public void handleExperienceChange(CMMsg msg);

	public int adjustedExperience(MOB mob, MOB victim, int amount);

	public boolean postExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);

	public String doBaseLevelAdjustment(MOB mob, int adjuster);

	public void level(MOB mob);

	public void unLevel(MOB mob);

	/**
	 * If the given item is a boardable, this method will post the given
	 * amount of experience to all abord.
	 * @param possibleShip the ship to give experience to
	 * @param amount amount of experience to give to each person found
	 * @return true if experience is posted, false otherwise
	 */
	public boolean postExperienceToAllAboard(Physical possibleShip, int amount);
	
	/**
	 * This method fills in combat and rejuvenation related stats for the given
	 * mob of their current base class at the given level. This method should
	 * create a mob for the caller if mob==null.
	 * 
	 * @param mob the mob to fill out, or null
	 * @param level the level of the mob
	 * @return the filled in mob
	 */
	public MOB fillOutMOB(MOB mob, int level);

	/**
	 * This method fills in combat and rejuvenation related stats for the given
	 * mob of the given class at the given level. This method should create a
	 * mob for the caller.
	 * 
	 * @param C the class to use.
	 * @param level the level of the mob
	 * @return the filled in mob
	 */
	public MOB fillOutMOB(CharClass C, int level);

	/**
	 * Returns the amount of hp the given player would have being their current
	 * base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the amount of hp a pc of this class should have
	 */
	public int getPlayerHitPoints(MOB mob);

	/**
	 * Returns the amount of mana the given mob would have being their current
	 * base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the amount of mana an npc of this class should have
	 */
	public int getLevelMana(MOB mob);

	/**
	 * Returns the range of money the given mob would have being their current
	 * base class. Since money is variable, this is a range low-high
	 * 
	 * @param mob the mob who would be this class
	 * @return the range of money an npc of this class should have
	 */
	public double[] getLevelMoneyRange(MOB mob);

	/**
	 * Returns the number of attacks the given mob would have being their
	 * current base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the number of attacks an npc of this class should have
	 */
	public double getLevelMOBSpeed(MOB mob);

	/**
	 * Returns the amount of movement the given mob would have being their
	 * current base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the amount of movement an npc of this class should have
	 */
	public int getLevelMove(MOB mob);

	/**
	 * Returns the amount of combat prowess the given mob would have being their
	 * current base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the amount of combat prowess an npc of this class should have
	 */
	public int getLevelAttack(MOB mob);

	/**
	 * Returns the armor rating the given mob would have being their current
	 * base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the armor rating an npc of this class should have
	 */
	public int getLevelMOBArmor(MOB mob);

	/**
	 * Returns the amount of damage per hit the given mob would have being their
	 * current base class.
	 * 
	 * @param mob the mob who would be this class
	 * @return the amount of damage per hit an npc of this class should have
	 */
	public int getLevelMOBDamage(MOB mob);

	/**
	 * Called whenever a player actually gains any experience. It actually does
	 * the experience gain for the player as well as determining how much, if
	 * any should be distributed to leiges or clans. Will automatically cause a
	 * call to level if necessary.
	 * 
	 * @see ExpLevelLibrary#level(MOB)
	 * @param mob the mob to distribute experience to
	 * @param victim the mob killed, if any, to cause the experience gain
	 * @param homage the name, if any, of another mob whose gain experience is
	 *            causing this gain
	 * @param amount the amount of experience to gain
	 * @param quiet true if no messages should be given
	 */
	public void gainExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);

	/**
	 * Called whenever a member of this class loses any experience. It actually
	 * does the experience loss for the player as well as determining how much,
	 * if any should be taken awa from leiges or clans. Will automatically cause
	 * an unleveling if necessary.
	 * 
	 * @see ExpLevelLibrary#unLevel(MOB)
	 * @param mob the mob to take experience away from
	 * @param amount the amount of experience to take away
	 */
	public void loseExperience(MOB mob, int amount);
}
