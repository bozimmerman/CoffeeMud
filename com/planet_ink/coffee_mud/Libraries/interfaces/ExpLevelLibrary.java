package com.planet_ink.coffee_mud.Libraries.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpLevelLibrary.ModXP;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2023 Bo Zimmerman

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
 * Library which handles the gaining and losing of experience, as
 * well as the gaining and losing of levels.
 *
 * Also handles giving mobs stats appropriate to their level.
 *
 * @author Bo Zimmerman
 *
 */
public interface ExpLevelLibrary extends CMLibrary
{
	/**
	 * Returns how much experience a player must have to be the given level.
	 * @param mob the mob who has the experience level
	 * @param level the level to base the exp on
	 *
	 * @return the amount of experiene required to be the given level
	 */
	public int getLevelExperience(MOB mob, int level);

	/**
	 * Returns how much experience a player must gain at this level to advance
	 * to the next.
	 * @param mob the mob who has the experience level
	 * @param level the level to check at
	 *
	 * @return the amount of experience
	 */
	public int getLevelExperienceJustThisLevel(MOB mob, int level);

	/**
	 * Calculates the 'Power Level' for the given mob.
	 * The Power Level is determined by the EFFECTCXL from
	 * the ini file, and reflects Affects on the mob cast
	 * by higher level friends.
	 *
	 * @param mob the mob to get the power level of
	 * @return the power level, which might be same as normal level
	 */
	public int getEffectFudgedLevel(final MOB mob);

	/**
	 * Handles a message dealing with an experience change
	 * and applies it if necessary.  Can be called with
	 * any sort of message and it will filter it.
	 * @see ExpLevelLibrary#handleRPExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#adjustedExperience(MOB, MOB, int)
	 * @see ExpLevelLibrary#postExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#gainExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#loseExperience(MOB, String, int)
	 *
	 * @param msg the message to check and maybe handle
	 */
	public void handleExperienceChange(CMMsg msg);

	/**
	 * Handles a message dealing with an RP experience change
	 * and applies it if necessary.  Can be called with
	 * any sort of message and it will filter it.
	 * @see ExpLevelLibrary#handleExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#gainRPExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#loseRPExperience(MOB, String, int)
	 * @see ExpLevelLibrary#postRPExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param msg the message to check and maybe handle
	 */
	public void handleRPExperienceChange(CMMsg msg);

	/**
	 * Given a killer mob, and his victim, and a base
	 * amount of experience, this method will adjust the
	 * experience gain according to context, levels
	 * and so forth
	 * @see ExpLevelLibrary#handleExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#postExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param mob the killer who gains xp
	 * @param victim the victim
	 * @param amount the base amount of xp to gain
	 * @return the adjusted amount of xp
	 */
	public int adjustedExperience(MOB mob, MOB victim, int amount);

	/**
	 * Called whenever a player actually loses RP experience.
	 * @see ExpLevelLibrary#gainRPExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#postRPExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#handleRPExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#loseExperience(MOB, String, int)
	 *
	 * @param mob the mob to take experience from
	 * @param sourceId an arbitrary string denoting the xp source
	 * @param amount the amount of experience to lose
	 */
	public void loseRPExperience(MOB mob, String sourceId, int amount);

	/**
	 * Called whenever a player actually gains RP experience.
	 * @see ExpLevelLibrary#loseRPExperience(MOB, String, int)
	 * @see ExpLevelLibrary#postRPExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#handleRPExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#gainExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param mob the mob to distribute experience to
	 * @param sourceId an arbitrary string denoting the xp source
	 * @param victim the mob killed, if any, to cause the experience gain
	 * @param homageMessage the name, if any, of another mob whose gain experience is
	 *  		  causing this gain
	 * @param amount the amount of experience to gain
	 * @param quiet true if no messages should be given
	 */
	public void gainRPExperience(MOB mob, String sourceId, MOB victim, String homageMessage, int amount, boolean quiet);

	/**
	 * Generates and posts a rolePlay experience gain message, allowing it to
	 * be previewed, modified, and then to happen.
	 * @see ExpLevelLibrary#loseRPExperience(MOB, String, int)
	 * @see ExpLevelLibrary#gainRPExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#handleRPExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#postExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param mob the gainer of the rp xp
	 * @param sourceID null, or arbitrary string that denotes source of the xp
	 * @param target the target of the event that causes the xp to be gained
	 * @param homage null, or person to credit the xp to (or clan, or a message, whatever)
	 * @param amount the amount of xp to gain
	 * @param quiet true to gain xp silently, false to be up front.
	 * @return true if the xp was granted.
	 */
	public boolean postRPExperience(MOB mob, String sourceID, MOB target, String homage, int amount, boolean quiet);

	/**
	 * Generates and posts a normal experience gain message, allowing it to
	 * be previewed, modified, and then to happen.
	 * @see ExpLevelLibrary#handleExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#gainExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#loseExperience(MOB, String, int)
	 * @see ExpLevelLibrary#postRPExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param mob the gainer of the xp, usually the killer
	 * @param sourceID arbitrary string denoting the source of the xp
	 * @param victim the victim of the event that causes the xp to be gained
	 * @param homage null, or person to credit the xp to (or clan, or a message, whatever)
	 * @param amount the amount of xp to gain
	 * @param quiet true to gain xp silently, false to be up front.
	 * @return true if the xp was granted.
	 */
	public int postExperience(MOB mob, String sourceID, MOB victim, String homage, int amount, boolean quiet);

	/**
	 * Causes the given mob to gain a level, with all that entails
	 * @see ExpLevelLibrary#unLevel(MOB)
	 *
	 * @param mob the mob to gain the level
	 */
	public void level(MOB mob);

	/**
	 * Causes the given mob to lose a level, with all that entails
	 * This might include losing skills/abilities, and always includes
	 * things like losing trains and pracs
	 *
	 * @see ExpLevelLibrary#level(MOB)
	 *
	 * @param mob the mob to lose the level
	 */
	public void unLevel(MOB mob);

	/**
	 * Checks whether the given Command is assigned as the Deferred XP Assignment
	 * command in the coffeemud.ini file.  If it is, then a Defer command
	 * is returned instead, thus making the original command consumed.
	 *
	 * @param mob the mob who is commanding
	 * @param C the command the mob wants to do
	 * @param cmds the command line that generated the command
	 * @return the command to actually execute, which is usually the one given
	 */
	public Command deferCommandCheck(final MOB mob, final Command C, List<String> cmds);

	/**
	 * If the given item is a boardable, this method will post the given
	 * amount of experience to all aboard.
	 *
	 * @param possibleShip the ship to give experience to
	 * @param sourceID an abitrary string denoting the source of the xp
	 * @param amount amount of experience to give to each person found
	 * @param target the vanquished whatever that was the reason for the xp
	 * @return true if experience is posted, false otherwise
	 */
	public boolean postExperienceToAllAboard(Physical possibleShip, String sourceID, int amount, Physical target);

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
	 * @param R the race to use.
	 * @param level the level of the mob
	 * @return the filled in mob
	 */
	public MOB fillOutMOB(CharClass C, Race R, int level);

	/**
	 * Returns the amount of hp the given player would have being their current
	 * base class.
	 * @see ExpLevelLibrary#getLevelHitPoints(MOB)
	 *
	 * @param mob the mob
	 * @return the amount of hp a pc should have
	 */
	public int getPlayerHitPoints(MOB mob);

	/**
	 * Returns the amount of mana the given npc mob should have
	 * @see ExpLevelLibrary#getLevelMana(MOB)
	 * @see ExpLevelLibrary#getLevelMove(MOB)
	 * @see ExpLevelLibrary#getLevelMoneyRange(MOB)
	 * @see ExpLevelLibrary#getPlayerHitPoints(MOB)
	 *
	 * @param mob the mob who would have hit points
	 * @return the amount of hp an npc should have
	 */
	public int getLevelHitPoints(MOB mob);

	/**
	 * Returns the amount of mana the given mob would have being their current
	 * base class.
	 * @see ExpLevelLibrary#getLevelMove(MOB)
	 * @see ExpLevelLibrary#getLevelMoneyRange(MOB)
	 * @see ExpLevelLibrary#getLevelHitPoints(MOB)
	 *
	 * @param mob the mob
	 * @return the amount of mana an npc should have
	 */
	public int getLevelMana(MOB mob);

	/**
	 * Returns the range of money the given mob would have being their current
	 * base class. Since money is variable, this is a range low-high
	 * @see ExpLevelLibrary#getLevelMana(MOB)
	 * @see ExpLevelLibrary#getLevelMove(MOB)
	 * @see ExpLevelLibrary#getLevelHitPoints(MOB)
	 *
	 * @param mob the mob
	 * @return the range of money an npc should have
	 */
	public double[] getLevelMoneyRange(MOB mob);

	/**
	 * Returns the number of attacks the given mob would have being their
	 * current base class.
	 * @see ExpLevelLibrary#getLevelMOBArmor(MOB)
	 * @see ExpLevelLibrary#getLevelAttack(MOB)
	 * @see ExpLevelLibrary#getLevelMOBDamage(MOB)
	 *
	 * @param mob the mob
	 * @return the number of attacks an npc should have
	 */
	public double getLevelMOBSpeed(MOB mob);

	/**
	 * Returns the combat power level of the mob based
	 * on basic combat stats.
	 *
	 * @param M the mob to get the power level of
	 * @return the power level
	 */
	public int getPowerLevel(final MOB M);

	/**
	 * Returns the amount of movement the given mob would have being their
	 * current base class.
	 * @see ExpLevelLibrary#getLevelMana(MOB)
	 * @see ExpLevelLibrary#getLevelMoneyRange(MOB)
	 * @see ExpLevelLibrary#getLevelHitPoints(MOB)
	 *
	 * @param mob the mob
	 * @return the amount of movement an npc should have
	 */
	public int getLevelMove(MOB mob);

	/**
	 * Returns the amount of combat prowess the given mob would have being their
	 * current base class.
	 * @see ExpLevelLibrary#getLevelMOBArmor(MOB)
	 * @see ExpLevelLibrary#getLevelMOBDamage(MOB)
	 * @see ExpLevelLibrary#getLevelMOBSpeed(MOB)
	 *
	 * @param mob the mob
	 * @return the amount of combat prowess an npc should have
	 */
	public int getLevelAttack(MOB mob);

	/**
	 * Returns the armor rating the given mob would have being their current
	 * base class.
	 * @see ExpLevelLibrary#getLevelAttack(MOB)
	 * @see ExpLevelLibrary#getLevelMOBDamage(MOB)
	 * @see ExpLevelLibrary#getLevelMOBSpeed(MOB)
	 *
	 * @param mob the mob
	 * @return the armor rating an npc should have
	 */
	public int getLevelMOBArmor(MOB mob);

	/**
	 * Returns the amount of damage per hit the given mob would have being their
	 * current base class.
	 * @see ExpLevelLibrary#getLevelMOBArmor(MOB)
	 * @see ExpLevelLibrary#getLevelAttack(MOB)
	 * @see ExpLevelLibrary#getLevelMOBSpeed(MOB)
	 *
	 *
	 * @param mob the mob
	 * @return the amount of damage per hit an npc should have
	 */
	public int getLevelMOBDamage(MOB mob);

	/**
	 * Called whenever a player actually gains any experience. It actually does
	 * the experience gain for the player as well as determining how much, if
	 * any should be distributed to leiges or clans. Will automatically cause a
	 * call to level if necessary.
	 * @see ExpLevelLibrary#level(MOB)
	 * @see ExpLevelLibrary#handleExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#postExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#loseExperience(MOB, String, int)
	 * @see ExpLevelLibrary#gainRPExperience(MOB, String, MOB, String, int, boolean)
	 *
	 * @param mob the mob to distribute experience to
	 * @param sourceId arbitrary string denoting the source of the xp
	 * @param victim the mob killed, if any, to cause the experience gain
	 * @param homage the name, if any, of another mob whose gain experience is
	 *  		  causing this gain
	 * @param amount the amount of experience to gain
	 * @param quiet true if no messages should be given
	 */
	public void gainExperience(MOB mob, String sourceId, MOB victim, String homage, int amount, boolean quiet);

	/**
	 * Called whenever a member loses any experience. It actually
	 * does the experience loss for the player as well as determining how much,
	 * if any should be taken away from leiges or clans. Will automatically cause
	 * an unleveling if necessary.
	 * @see ExpLevelLibrary#unLevel(MOB)
	 * @see ExpLevelLibrary#handleExperienceChange(CMMsg)
	 * @see ExpLevelLibrary#postExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#gainExperience(MOB, String, MOB, String, int, boolean)
	 * @see ExpLevelLibrary#loseRPExperience(MOB, String, int)
	 *
	 * @param mob the mob to take experience away from
	 * @param sourceId arbitrary string denoting the source of xp
	 * @param amount the amount of experience to take away
	 */
	public void loseExperience(MOB mob, String sourceId, int amount);

	/**
	 * Given an encoded string of xp mods (see help on
	 * Prop_ModExperience), this will return the parsed
	 * and ready to use mod objects.
	 *
	 * @see ExpLevelLibrary#handleXPMods(MOB, MOB, ModXP, String, boolean, int)
	 *
	 * @param modStr the encoded mods, or "", or null
	 * @return an array of xpmod objects
	 */
	public ModXP[] parseXPMods(final String modStr);

	/**
	 * Modifies the given amount of experience and returns
	 * the modification, according to the global rules
	 * defined by XPMOD in the ini file.
	 *
	 * @see ExpLevelLibrary#parseXPMods(String)
	 *
	 * @param mob the receiver or loser of xp
	 * @param target a possible target of the action
	 * @param mod the mods to apply
	 * @param sourceID an arbitrary string denoting the xp source
	 * @param useTarget true to use the target for masking
	 * @param amount the amount of xp tentatively
	 * @return the modified xp
	 */
	public int handleXPMods(final MOB mob, final MOB target,
							final ModXP mod,
							final String sourceID, final boolean useTarget,
							final int amount);

	/**
	 * Class for tracking conditional changes to XP.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static class ModXP
	{
		/**
		 * Enum for tracking direction of an xp change
		 * @author Bo Zimmerman
		 *
		 */
		public enum DirectionCheck
		{
			POSITIVE, NEGATIVE, POSINEGA
		}

		public String			operationFormula	= "";
		public boolean			selfXP				= false;
		public boolean			rideOK				= false;
		public boolean			targetOnly			= false;
		public DirectionCheck	dir					= DirectionCheck.POSITIVE;
		public CompiledFormula	operation			= null;
		public CompiledZMask	mask				= null;
		public String			tmask				= "";
	}

}
