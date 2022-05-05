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
   Copyright 2006-2022 Bo Zimmerman

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
 * The localization library for CoffeeMud works by building a
 * set of property files that do string matching and replacement
 * from all of the game strings that start with L().
 *
 * @author Bo Zimmerman
 *
 */
public interface LanguageLibrary extends CMLibrary
{
	/**
	 * Clears the cache of both the translator and the parser,
	 * allowing it all to be reloaded from the properties
	 * files on the filesystem.
	 */
	public void clear();

	/**
	 * After an output string has gone through all the other
	 * processes, if there are any other translations to be
	 * done, this will handle it.  This is called immediately
	 * before an entire block of text is sent to the user.
	 *
	 * @param item the string to send to the user
	 * @return null, or a new string to send to the user
	 */
	public String finalTranslation(String item);

	/**
	 * A translator for static internal string, such
	 * as those found on interfaces.  How this manages to set the
	 * locale before a lot of those strings get loaded by
	 * Java is probably a problem.
	 *
	 * @param str the static string
	 * @return the translated static string
	 */
	public String sessionTranslation(String item);

	/**
	 * A translator for static internal string arrays, such
	 * as those found on interfaces.  How this manages to set the
	 * locale before a lot of those classes get loaded by
	 * Java is probably a problem.
	 *
	 * @param str the static string array
	 * @return the translated static string array
	 */
	public String[] sessionTranslation(final String[] str);

	/**
	 * This is the main output and variable translator.  Most internal
	 * system strings go through this method for localization.  This
	 * also handles the @x1 @x2, ... variable replacements.
	 *
	 * @param str the variable-laden english system string
	 * @param xs variable values, NOT translated
	 * @return the full translated and variable-replaced string
	 */
	public String fullSessionTranslation(final String str, final String ... xs);

	/**
	 * The filter translation is an output translation that only handles
	 * pronoun tags, like S-NAME, T-YOUPOSS, etc.  After the engine generates
	 * the proper english replacement (usually a mob or object name), then
	 * this filter is given a pass as it to localize the output.
	 *
	 * @param item ONLY the replacement from a tag
	 * @return null, or the localized replacement string
	 */
	public String filterTranslation(String item);

	/**
	 * Initializes the localization library by setting the
	 * language and state code, thus pointing the mud at which
	 * translation properties files to use.
	 *
	 * @param lang the language code, default en
	 * @param state the state code, default TX
	 */
	public void setLocale(String lang, String state);

	/**
	 * Accepts user-entered pre-parsed command list, and generates
	 * a list containing one or more translated full command lists.
	 * This is called whenever the user enters a command at the
	 * main prompt.
	 *
	 * @param CMDS the pre-parsed command list
	 * @return a list with the translated command list
	 */
	public List<List<String>> preCommandParser(List<String> CMDS);

	/**
	 * This unique translator is mostly run at boot time to
	 * translate the cached command words of commands, abilities,
	 * and similar things to the localized term.  Most input translators
	 * take a localized term to generate an english word the system
	 * can process, whereas this does the reverse: translate english
	 * into the localized language so that the user can match that
	 * term directly.
	 *
	 * @param str a single command word
	 * @return the given word, or the translated word
	 */
	public String commandWordTranslation(final String str);

	/**
	 * When a command or some other process is requesting access to
	 * a room or inventory item by name, or sometimes and exit, then
	 * this parser is called with the user input to translate things
	 * into a final search string.
	 *
	 * @param item the user entered item name
	 * @return null to use the users word, or the new translated string
	 */
	public String preItemParser(String item);

	/**
	 * When a command or some other process is requesting access to
	 * a room or inventory item by name, and the entry from the user
	 * fails to find the item, this filter will be called to provide
	 * a "second chance" item name.  Or null to skip the second chance.
	 *
	 * @param item the user entered item name
	 * @return null, or a substitute item name to try
	 */
	public String failedItemParser(String item);

	/**
	 * During login or other input that does not involve commands,
	 * this input parser can translate input.
	 *
	 * @param words the input
	 * @return the new input
	 */
	public String rawInputParser(String words);

	/**
	 * Friendly names for the varioius ISO language codes,
	 * which will probably be put in a file some day.
	 */
	public String[][] ISO_LANG_CODES = {
		{"AA","Afar"},
		{"AB","Abkhazian"},
		{"AF","Afrikaans"},
		{"AM","Amharic"},
		{"AR","Arabic"},
		{"AS","Assamese"},
		{"AY","Aymara"},
		{"AZ","Azerbaijani"},
		{"BA","Bashkir"},
		{"BE","Byelorussian"},
		{"BG","Bulgarian"},
		{"BH","Bihari"},
		{"BI","Bislama"},
		{"BN","Bengali"},
		{"BO","Tibetan"},
		{"BR","Breton"},
		{"CA","Catalan"},
		{"CO","Corsican"},
		{"CS","Czech"},
		{"CY","Welsh"},
		{"DA","Danish"},
		{"DE","German"},
		{"DZ","Bhutani"},
		{"EL","Greek"},
		{"EN","English"},
		{"EO","Esperanto"},
		{"ES","Spanish"},
		{"ET","Estonian"},
		{"EU","Basque"},
		{"FA","Persian"},
		{"FI","Finnish"},
		{"FJ","Fiji"},
		{"FO","Faeroese"},
		{"FR","French"},
		{"FY","Frisian"},
		{"GA","Irish"},
		{"GD","Gaelic"},
		{"GL","Galician"},
		{"GN","Guarani"},
		{"GU","Gujarati"},
		{"HA","Hausa"},
		{"HI","Hindi"},
		{"HR","Croatian"},
		{"HU","Hungarian"},
		{"HY","Armenian"},
		{"IA","Interlingua"},
		{"IE","Interlingue"},
		{"IK","Inupiak"},
		{"IN","Indonesian"},
		{"IS","Icelandic"},
		{"IT","Italian"},
		{"IW","Hebrew"},
		{"JA","Japanese"},
		{"JI","Yiddish"},
		{"JW","Javanese"},
		{"KA","Georgian"},
		{"KK","Kazakh"},
		{"KL","Greenlandic"},
		{"KM","Cambodian"},
		{"KN","Kannada"},
		{"KO","Korean"},
		{"KS","Kashmiri"},
		{"KU","Kurdish"},
		{"KY","Kirghiz"},
		{"LA","Latin"},
		{"LN","Lingala"},
		{"LO","Laothian"},
		{"LT","Lithuanian"},
		{"LV","Latvian"},
		{"MG","Malagasy"},
		{"MI","Maori"},
		{"MK","Macedonian"},
		{"ML","Malayalam"},
		{"MN","Mongolian"},
		{"MO","Moldavian"},
		{"MR","Marathi"},
		{"MS","Malay"},
		{"MT","Maltese"},
		{"MY","Burmese"},
		{"NA","Nauru"},
		{"NE","Nepali"},
		{"NL","Dutch"},
		{"NO","Norwegian"},
		{"OC","Occitan"},
		{"OM","Oromo"},
		{"OR","Oriya"},
		{"PA","Punjabi"},
		{"PL","Polish"},
		{"PS","Pashto"},
		{"PT","Portuguese"},
		{"QU","Quechua"},
		{"RM","Rhaeto-Romance"},
		{"RN","Kirundi"},
		{"RO","Romanian"},
		{"RU","Russian"},
		{"RW","Kinyarwanda"},
		{"SA","Sanskrit"},
		{"SD","Sindhi"},
		{"SG","Sangro"},
		{"SH","Serbo-Croatian"},
		{"SI","Singhalese"},
		{"SK","Slovak"},
		{"SL","Slovenian"},
		{"SM","Samoan"},
		{"SN","Shona"},
		{"SO","Somali"},
		{"SQ","Albanian"},
		{"SR","Serbian"},
		{"SS","Siswati"},
		{"ST","Sesotho"},
		{"SU","Sudanese"},
		{"SV","Swedish"},
		{"SW","Swahili"},
		{"TA","Tamil"},
		{"TE","Tegulu"},
		{"TG","Tajik"},
		{"TH","Thai"},
		{"TI","Tigrinya"},
		{"TK","Turkmen"},
		{"TL","Tagalog"},
		{"TN","Setswana"},
		{"TO","Tonga"},
		{"TR","Turkish"},
		{"TS","Tsonga"},
		{"TT","Tatar"},
		{"TW","Twi"},
		{"UK","Ukrainian"},
		{"UR","Urdu"},
		{"UZ","Uzbek"},
		{"VI","Vietnamese"},
		{"VO","Volapuk"},
		{"WO","Wolof"},
		{"XH","Xhosa"},
		{"YO","Yoruba"},
		{"ZH","Chinese"},
		{"ZU","Zulu"}
	};
}
