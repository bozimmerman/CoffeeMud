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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
/*
   Copyright 2005-2022 Bo Zimmerman

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
 * A ZapperMask is a set of conditions, typically of the form VALUE in SET,
 * KEY=VALUE or VALUE, etc. These masks are used to determine the objects
 * eligibility for some purpose. The most common use is as a mask to
 * determine whether some mob is able to do something, or will have something
 * happen to them.  ZapperMasks are made up of one or more entries, as
 * mentioned above.  These entries have an implicit AND connector between
 * them, whereas the values in each entry are typically an internal OR.
 * e.g.
 * Zappermask = condition entry AND condition entry AND condition entry, etc..
 * Condition Entry = VALUE = OPTION or OPTION or OPTION or OPTION...
 *
 * @author Bo Zimmerman
 */
public interface MaskingLibrary extends CMLibrary
{
	/**
	 * Returns the official help file for the list of all
	 * zappermask codes, customized for the caller.
	 *
	 * @param CR  null, or the type of EOL string to use.
	 * @param word null, or a substitute for the word 'disallow'
	 * @return the customized help file for the zappermask codes
	 */
	public String maskHelp(final String CR, final String word);

	/**
	 * If the given zappermask contains any references to ability or
	 * expertise requirements, this will return the IDs of the
	 * required object.
	 *
	 * @param text the zappermask
	 * @return a list of any required abilities or expertises
	 */
	public List<String> getAbilityEduReqs(final String text);

	/**
	 * Given a zappermask, this will return a brief readable english
	 * description of the mask.  The normal format is "allows only..."
	 * or "disallows ...".
	 *
	 * @see MaskingLibrary#maskDesc(String, boolean)
	 *
	 * @param text  the ZapperMask string
	 * @return a description of the mask
	 */
	public String maskDesc(final String text);

	/**
	 * Given a zappermask, this will return a brief readable english
	 * description of the mask.  The normal format is "allows only..."
	 * or "disallows ...".  If skipFirstWord is sent, then the allows
	 * case returns "only...".
	 *
	 * @see MaskingLibrary#maskDesc(String)
	 *
	 * @param text  the ZapperMask string
	 * @param skipFirstWord true to skip the word 'allows'
	 * @return a description of the mask
	 */
	public String maskDesc(final String text, final boolean skipFirstWord);

	/**
	 * Given a zappermask, this will return a compiled version of the
	 * given string, build a new one if necessary, and return it
	 *
	 * @see MaskingLibrary#getPreCompiledMask(String)
	 *
	 * @param text the zappermask string
	 * @return the compiled zappermask
	 */
	public CompiledZMask maskCompile(final String text);

	/**
	 * Given a zappermask, this will check the internal cache for an
	 * already compiled version of the given string, build a new one
	 * if necessary, and return it
	 *
	 * @see MaskingLibrary#maskCompile(String)
	 *
	 * @param str the zappermask string
	 * @return the compiled zappermask
	 */
	public CompiledZMask getPreCompiledMask(final String str);

	/**
	 * Given a compiled zappermask and a Environmental object, this will return whether the
	 * Environmental passes the filter, or is rejected by it.
	 *
	 * @see MaskingLibrary#maskCheck(CompiledZMask, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(String, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(String, Environmental, boolean)
	 *
	 * @param cset the compiled zappermask to apply to the player
	 * @param E the object to apply the pas to
	 * @param actual true to use base stats, false for adjusted
	 * @return true to pass the given object, false if rejected
	 */
	public boolean maskCheck(final CompiledZMask cset, final Environmental E, final boolean actual);

	/**
	 * Given a zappermask and a Environmental object, this will return whether the
	 * Environmental passes the filter, or is rejected by it.
	 *
	 * @see MaskingLibrary#maskCheck(CompiledZMask, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(String, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(CompiledZMask, Environmental, boolean)
	 *
	 * @param text the zappermask to apply to the player
	 * @param E the object to apply the pas to
	 * @param actual true to use base stats, false for adjusted
	 * @return true to pass the given object, false if rejected
	 */
	public boolean maskCheck(final String text, final Environmental E, final boolean actual);

	/**
	 * Given a compiled zappermask and a ThinPlayer object, this will return whether the
	 * ThinPlayer passes the filter, or is rejected by it.
	 *
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer
	 * @see MaskingLibrary#maskCheck(String, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(CompiledZMask, Environmental, boolean)
	 * @see MaskingLibrary#maskCheck(String, Environmental, boolean)
	 *
	 * @param cset the compiled zappermask to apply to the player
	 * @param E the thinplayer object
	 * @return true to pass the thinplayer, false if rejected
	 */
	public boolean maskCheck(final CompiledZMask cset, final PlayerLibrary.ThinPlayer E);

	/**
	 * Given a zappermask and a ThinPlayer object, this will return whether the
	 * ThinPlayer passes the filter, or is rejected by it.
	 *
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer
	 * @see MaskingLibrary#maskCheck(CompiledZMask, com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer)
	 * @see MaskingLibrary#maskCheck(CompiledZMask, Environmental, boolean)
	 * @see MaskingLibrary#maskCheck(String, Environmental, boolean)
	 *
	 * @param text the zappermask to apply to the player
	 * @param E the thinplayer object
	 * @return true to pass the thinplayer, false if rejected
	 */
	public boolean maskCheck(final String text, final PlayerLibrary.ThinPlayer E);

	/**
	 * Parses the given string as a zappermask and returns true
	 * if any parsed bit is a valid zappermask type word.
	 *
	 * @param text the postential zappermask
	 * @param errorSink the list to put the error message in
	 * @return true if any part of the given string looks zappermasky
	 */
	public boolean syntaxCheck(final String text, final List<String> errorSink);

	/**
	 * Given a zappermask string, this method will find any level-check
	 * related criteria, such as level, classlevel, or maxclasslevel,
	 * and returns the minimum level of the criteria.
	 *
	 * @param text the zappermask
	 * @param minMinLevel the default floor to return
	 * @return the minimum level in the mask
	 */
	public int minMaskLevel(final String text, final int minMinLevel);

	/**
	 * Lots of property strings support including zappermasks
	 * by including the string MASK= followed by the remainder of
	 * the string being the zappermask.  This method helps support
	 * those properties.  It will return a two dimensional array
	 * where the first string is the normal arguments, and the
	 * second string is the zappermask, or "" if none was found.
	 *
	 * @param newText the property parameters
	 * @return the property parameter and zappermask array
	 */
	public String[] separateMaskStrs(final String newText);

	/**
	 * Creates an empty always-passing compiled zappermask
	 * object.
	 *
	 * @return an empty always-passing compiled zappermask
	 */
	public CompiledZMask createEmptyMask();

	/**
	 * The set of mask types.  Each of these reflects some stat or
	 * aspect of a CoffeeMud object that is being tested.  ZapperMask
	 * entries are typically of the type VALUE in SET.  For this reason
	 * there are two type of keys: -TYPE, which means "disallow all values"
	 * but are then followed by exceptions to that rule, and +TYPE which
	 * means "allow any value" but is then followed by exceptions to that
	 * rule, listing values that would cause a mask failure.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum ZapperKey
	{
		_PLAYER("-PLAYER"),
		_NPC("-MOB"),
		_CHANCE,
		_CLASS("-CLASSES"),
		CLASS("CLASSES"),
		_BASECLASS("-BASECLASSES"),
		BASECLASS("+BASECLASSES"),
		_CLASSTYPE("-CLASSTYPE"),
		CLASSTYPE("+CLASSTYPE"),
		_RACE("-RACES"),
		RACE("+RACES"),
		_ALIGNMENT("-ALIGNMENTS","-ALIGN"),
		ALIGNMENT,
		_GENDER("-GENDERS"),
		GENDER,
		_LEVEL("-LEVELS"),
		_CLASSLEVEL("-CLASSLEVELS"),
		_TATTOO("-TATTOOS"),
		TATTOO("+TATTOOS"),
		_NAME("-NAMES"),
		NAME("+NAMES"),
		_RACECAT("-RACECATS"),
		RACECAT("+RACECATS"),
		_CLAN("-CLANS"),
		CLAN("+CLANS"),
		_ANYCLASS("-ANYCLASSES"),
		ANYCLASS("+ANYCLASSES"),
		_ANYCLASSLEVEL("-ANYCLASSLEVELS"),
		ANYCLASSLEVEL("+ANYCLASSLEVELS"),
		_ADJSTRENGTH("-ADJSTR"),
		ADJSTRENGTH("+ADJSTR"),
		_ADJINTELLIGENCE("-ADJINT"),
		ADJINTELLIGENCE("+ADJINT"),
		_ADJWISDOM("-ADJWIS"),
		ADJWISDOM("+ADJWIS"),
		_ADJDEXTERITY("-ADJDEX"),
		ADJDEXTERITY("+ADJDEX"),
		_ADJCONSTITUTION("-ADJCON"),
		ADJCONSTITUTION("+ADJCON"),
		_ADJCHARISMA("-ADJCHA"),
		ADJCHARISMA("+ADJCHA"),
		_AREA("-AREAS"),
		AREA("+AREAS"),
		ITEM,
		_ITEM,
		LVLGR,
		LVLGE,
		LVLLT,
		LVLLE,
		LVLEQ,
		EFFECT("+EFFECTS"),
		_EFFECT("-EFFECTS"),
		_DEITY,
		DEITY,
		_FACTION,
		FACTION,
		_WORN,
		WORN,
		_MATERIAL,
		MATERIAL,
		_RESOURCE,
		RESOURCE,
		_JAVACLASS,
		JAVACLASS,
		_ABILITY("-ABILITIES","-ABLE","-ABLES"),
		ABILITY("+ABILITIES","+ABLE","+ABLES"),
		_WORNON,
		WORNON,
		_VALUE,
		VALUE,
		_WEIGHT,
		WEIGHT,
		_ARMOR,
		ARMOR,
		_DAMAGE,
		DAMAGE,
		_ATTACK,
		ATTACK,
		_DISPOSITION,
		DISPOSITION,
		_SENSES,
		SENSES,
		_HOUR,
		HOUR,
		_SEASON,
		SEASON,
		_WEEK,
		WEEK,
		_WEEKOFYEAR,
		WEEKOFYEAR,
		_YEAR,
		YEAR,
		_MONTH,
		MONTH,
		_SECURITY("-SECURITIES","-SEC"),
		SECURITY("+SECURITIES","+SEC"),
		_EXPERTISE("-EXPERTISES"),
		EXPERTISE("+EXPERTISES"),
		_SKILL("-SKILLS"),
		SKILL("+SKILLS"),
		_QUALLVL,
		QUALLVL,
		_STRENGTH("-STR"),
		STRENGTH("+STR"),
		_INTELLIGENCE("-INT"),
		INTELLIGENCE("+INT"),
		_WISDOM("-WIS"),
		WISDOM("+WIS"),
		_DEXTERITY("-DEX"),
		DEXTERITY("+DEX"),
		_CONSTITUTION("-CON"),
		CONSTITUTION("+CON"),
		_CHARISMA("-CHA"),
		CHARISMA("+CHA"),
		_HOME,
		HOME,
		_SKILLFLAG("-SKILLFLAGS"),
		SKILLFLAG("+SKILLFLAGS"),
		_MAXCLASSLEVEL("-MAXCLASSLEVELS"),
		_WEATHER,
		WEATHER,
		_DAY,
		DAY,
		_DAYOFYEAR,
		DAYOFYEAR,
		_SYSOP,
		SYSOP,
		_SUBOP,
		SUBOP,
		_QUESTWIN,
		QUESTWIN,
		_GROUPSIZE,
		GROUPSIZE,
		_IF,
		IF,
		_OR,
		OR,
		_MOOD("-MOODS"),
		MOOD("+MOODS"),
		_ACCCHIEVE("-ACCCHIEVES"),
		ACCCHIEVE("+ACCCHIEVES"),
		_ISHOME("-ISHOME"),
		ISHOME("+ISHOME"),
		_IFSTAT("-IFSTAT"),
		IFSTAT("+IFSTAT"),
		_SUBNAME("-SUBNAME"),
		SUBNAME("+SUBNAME"),
		_WEAPONCLASS("-WEAPONCLASS"),
		WEAPONCLASS("+WEAPONCLASS"),
		_WEAPONTYPE("-WEAPONTYPE"),
		WEAPONTYPE("+WEAPONTYPE"),
		_WEAPONAMMO("-WEAPONAMMO"),
		WEAPONAMMO("+WEAPONAMMO"),
		_ACCOUNT("-ACCOUNTS"),
		ACCOUNT("+ACCOUNTS"),
		_AREAINSTANCE("-AREAINSTANCE"),
		AREAINSTANCE("+AREAINSTANCE"),
		_AREABLURB("-AREABLURB"),
		AREABLURB("+AREABLURB"),
		_LOCATION,
		LOCATION,
		_OFFICER,
		OFFICER,
		_JUDGE,
		JUDGE,
		_PORT,
		PORT,
		_PLANE,
		PLANE,
		_RIDE,
		RIDE,
		_FOLLOW,
		FOLLOW,
		;
		private final String[] keys;
		private ZapperKey(final String... exts)
		{
			final List<String> k = new ArrayList<String>();
			k.add(name().startsWith("_") ? ("-"+name().substring(1)) : ("+"+name()));
			for(final String x : exts)
			{
				if(!k.contains(x))
					k.add(x);
			}
			keys = k.toArray(new String[0]);
		}

		public String[] keys()
		{
			return keys;
		}
	}

	/**
	 * A Compiled Mask Entry is a condition, typically of
	 * the form VALUE in SET, or KEY=VALUE, or VALUE in RANGE.
	 * These are an OR condition as part of a larger ZapperMask.
	 *
	 * @see MaskingLibrary.CompiledZMask
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CompiledZMaskEntry
	{
		/**
		 * Returns the type of value to check for.
		 *
		 * @see MaskingLibrary.ZapperKey
		 *
		 * @return the type of value to check for.
		 */
		public ZapperKey maskType();

		/**
		 * The set of acceptable values, or parameters
		 * to the particular mask.  These are very
		 * dependent on the above mask type.
		 *
		 * @return the parameters to the mask type
		 */
		public Object[] parms();
	}

	/**
	 * A Compiled ZapperMask is a set of conditions, typically
	 * of the form VALUE in SET, KEY=VALUE or VALUE, etc. These
	 * masks are used to select among a stream of objects, in order
	 * to determine the objects elligibility for some purpose.
	 * The most common use is as a mask to determine whether some
	 * mob is able to do something, or will have something happen
	 * to them.  ZapperMasks are made up of one or more entries,
	 * as mentioned above.  These entries have an implicit AND
	 * connector between them, whereas the entries themselves are
	 * typically an internal OR (see above).
	 *
	 * @see MaskingLibrary.CompiledZMaskEntry
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CompiledZMask
	{
		/**
		 * Returns a list of ZapperMask entries, where all the
		 * list entries must pass for the mask to pass
		 *
		 * @see MaskingLibrary.CompiledZMaskEntry
		 *
		 * @return a list of entries
		 */
		public CompiledZMaskEntry[][] entries();

		/**
		 * Returns whether the mask is empty and always passes
		 *
		 * @return true if this mask always passes
		 */
		public boolean empty();

		/**
		 * As some mask entries only apply to items, set this flag
		 * if an item MIGHT be masked, or if the mask contains item
		 * entries, and thus an item needs creating to do the check.
		 *
		 * @return true to create an item, false otherwise
		 */
		public boolean useItemFlag();

		/**
		 * As some mask entries only apply to rooms, set this flag
		 * if an item MIGHT be masked, or if the mask contains location
		 * entries, and thus a room needs referencing to do the check.
		 *
		 * @return true to create or reference a room, false otherwise
		 */
		public boolean useRoomFlag();
	}
}
