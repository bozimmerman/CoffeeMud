package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
 * A Language ability represents both the ability to understand one or more
 * spoken or written languages, and the ability to speak one or more spoken
 * languages.  A single ability usually represents a single language, but
 * may support multiple simultaneously.
 */
public interface Language extends Ability
{
	/**
	 * Returns the name of this language when it is in written form.
	 * This is usually the same as the spoken form.
	 * @return the name of this language when it is in written form.
	 */
	public String writtenName();

	/**
	 * Returns a list of the languages understood by this ability
	 * @return vector of language ids supported (usually 1 element == ID())
	 */
	public List<String> languagesSupported();

	/**
	 * Returns whether the given language is translated by this one
	 * @param language the language to test
	 * @return true if this language translates (usually ID() == language)
	 */
	public boolean translatesLanguage(String language);

	/**
	 * Returns the understanding profficiency in the given supported language
	 * @param language the language to test for (usually ID())
	 * @return the profficiency of this ability in the language (0-100)
	 */
	public int getProficiency(String language);

	/**
	 * Returns whether this language is currently being spoken
	 * @param language the language to test for (usually ID())
	 * @return true if spoken
	 */
	public boolean beingSpoken(String language);

	/**
	 * Changes whether this language is currently being spoken
	 * @param language the language to set (usually ID())
	 * @param beingSpoken whether it is being spoken
	 */
	public void setBeingSpoken(String language, boolean beingSpoken);

	/**
	 * Returns the direct word to word translation hashtable
	 * @param language the language to translate directory (usually ID())
	 * @return the hashtable of word-word translations
	 */
	public Map<String, String> translationHash(String language);

	/**
	 * Returns the word-length rough-translation vector of string arrays for the given language
	 * The first string array in the vector represents 1 letter words, the second 2,
	 * and so forth.
	 * @param language the language to return the vector for (usually ID())
	 * @return the vector of word-length rough translation string arrays
	 */
	public List<String[]> translationVector(String language);

	/**
	 * Returns a language translation of the given word in the given language
	 * @param language the language to use (usually ID())
	 * @param word the word to translate
	 * @return the translated word
	 */
	public String translate(String language, String word);
}
