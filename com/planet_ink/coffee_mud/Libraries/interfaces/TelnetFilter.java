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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2025 Bo Zimmerman

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
 * Library for handling the conversion of raw mud output strings
 * to final strings that are sent directly to the users socket
 * connection for rendering by their client.
 *
 * @author Bo Zimmerman
 */
public interface TelnetFilter extends CMLibrary
{
	/**
	 * One of the most useful enums in the game, this
	 * reminds us of all the pronoun placeholder tags
	 * that get replaced at runtime by this very filter
	 * library by the contextually appropriate word.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum Pronoun
	{
		HISHER("-HIS-HER","-h"),
		HIMHER("-HIM-HER","-m"),
		NAME("-NAME",null),
		NAMESELF("-NAMESELF","-s"),
		HESHE("-HE-SHE","-e"),
		ISARE("-IS-ARE",null),
		HASHAVE("-HAS-HAVE",null),
		YOUPOSS("-YOUPOSS","`s"),
		HIMHERSELF("-HIM-HERSELF","-ms"),
		HISHERSELF("-HIS-HERSELF","-hs"),
		SIRMADAM("-SIRMADAM",null),
		MRMS("-MRMS",null),
		MISTERMADAM("-MISTERMADAM",null),
		ISARE2("IS-ARE",null),
		NAMENOART("-NAMENOART",null),
		ACCOUNTNAME("-ACCOUNTNAME",null),
		WITHNAME("-WITHNAME",null)
		;
		/**
		 * This is the -WORD main tag suffix,
		 * add your own S, T, O, etc..
		 */
		public final String suffix;
		/**
		 * No idea what this is.
		 */
		public final String emoteSuffix;

		private Pronoun(final String suffix, final String emoteSuffix)
		{
			this.suffix=suffix;
			this.emoteSuffix=emoteSuffix;
		}
	}

	/**
	 * Returns the suffix strings for the various pronoun tags.
	 * These are like -NAME and -HE-SHE, without the prefix STO.
	 *
	 * @return the suffix strings
	 */
	public Set<String> getPronounSuffixes();


	/**
	 * Simplest, least things done filter.
	 * Only does ', and backslash \ escape codes (\n)
	 * @param msg the filtered message
	 * @return the filtered message
	 */
	public String simpleOutFilter(String msg);

	/**
	 * Does MXP and normal CM ^ color codes, and that's about it.
	 * no word-wrapping, text filtering or ('\','n') to '\n' translations
	 *
	 * @param msg the message to translate
	 * @param S null, or the session object, for color codes
	 * @return the filtered message
	 */
	public String colorOnlyFilter(String msg, Session S);

	/**
	 * Does MXP tag correction if MXP is on, and that's it.
	 * Nothing else.  No word wrapping, coloring, anything.
	 *
	 * @param msg the string to translate
	 * @param S null, or the session for MXP status
	 * @return the filtered string
	 */
	public String mxpSafetyFilter(String msg, Session S);

	/**
	 * Does word wrapping, % and backslash \\ escape codes.
	 * Removes most other things entirely.
	 *
	 * @param msg the string to filter
	 * @param wrap the column to wrap at
	 * @return the lines of the final message
	 */
	public String[] wrapOnlyFilter(String msg, int wrap);

	/**
	 * The real workhorse of this library, which does ALL filtering,
	 * including word wrap, MXP and MSP tags, % and \\ escape codes,
	 * pronoun tags, (s), and all the rest.
	 *
	 * @param S null, or the session for wrap, tags, other info
	 * @param mob the mob who will see the message
	 * @param source the source for S-pronouns
	 * @param target the target for T-pronouns
	 * @param tool the tool for O-pronouns
	 * @param msg the string to filter
	 * @param wrapOnly true to skip pronouns, and (s)
	 * @return the fully filtered string
	 */
	public String fullOutFilter(Session S, MOB mob, Physical source, Environmental target, Environmental tool, String msg, boolean wrapOnly);

	/**
	 * Removes carriage returns, linefeeds, and converts ' to `
	 *
	 * @param s the string to filter
	 * @return the filtered string
	 */
	public String safetyInFilter(String s);

	/**
	 * Safety filter for user input.  Does the ' conversion,
	 * % escape codes, and checks for mxp input.
	 *
	 * @param input the input string
	 * @param permitMXPTags true to allow mxp tags from users
	 * @param isArchon true to allow certain escape codes from users
	 * @return the filtered input
	 */
	public String simpleInFilter(StringBuilder input, boolean permitMXPTags, boolean isArchon);

	/**
	 * Safety filter for user input.  Does the ' conversion,
	 * % escape codes, and checks for mxp input and does
	 * not permit them.
	 *
	 * @param input the input string
	 * @return the filtered input
	 */
	public String simpleInFilter(StringBuilder input);

	/**
	 * Safety filter for user input.  Does the ' conversion,
	 * % escape codes, and checks for mxp input and does
	 * not permit them.  It also converts 10 and 13 to
	 * backslash escape codes.
	 *
	 * @param input the input string
	 * @return the filtered input
	 */
	public String fullInFilter(String input);

	/**
	 * Converts pronoun tags and at-sign variables
	 * to neutral characters, rendering them useless.
	 *
	 * @param s the input string to filter
	 * @return the filtered string
	 */
	public String secondaryUserInputFilter(final String s);
}
