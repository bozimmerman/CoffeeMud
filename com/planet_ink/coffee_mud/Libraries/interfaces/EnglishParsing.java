package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.Libraries.CMChannels;
import com.planet_ink.coffee_mud.Libraries.EnglishParser;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2005-2019 Bo Zimmerman

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
 * A library that deals specifically with language-specific string
 * manipulation, and input parsing.  This library would be a sure-fire
 * candidate for a localizer to alter.
 *
 * @author Bo Zimmerman
 *
 */
public interface EnglishParsing extends CMLibrary
{
	public boolean isAnArticle(String s);
	public String removeArticleLead(String s);
	public String cleanPrepositions(String s);
	public boolean startsWithAnArticle(String s);
	public String stripPunctuation(String str);
	public boolean isPunctuation(final byte b);
	public boolean hasPunctuation(String str);
	public String makePlural(String str);
	public String makeSingular(String str);
	public String makePastTense(String word, String defaultWord);
	public String getFirstWord(final String str);
	public String properIndefiniteArticle(String str);
	public String toEnglishStringList(final String[] V);
	public String toEnglishStringList(final Class<? extends Enum<?>> enumer, boolean andOr);
	public String toEnglishStringList(final Collection<? extends Object> V);
	public String insertUnColoredAdjective(String str, String adjective);
	public String insertAdjectives(String paragraph, String[] adjsToChoose, int pctChance);
	public String startWithAorAn(String str);
	public CMObject findCommand(MOB mob, List<String> commands);
	public boolean evokedBy(Ability thisAbility, String thisWord);
	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord);
	public String getAnEvokeWord(MOB mob, String word);
	public Ability getToEvoke(MOB mob, List<String> commands);
	public boolean preEvoke(MOB mob, List<String> commands, int secondsElapsed, double actionsRemaining);
	public void evoke(MOB mob, Vector<String> commands);
	public boolean containsString(final String toSrchStr, final String srchStr);

	public int getDotNumber(final String srchStr);
	public String bumpDotNumber(String srchStr, int byThisMuch);
	public int getContextNumber(Environmental[] list, Environmental E);
	public int getContextNumber(Collection<? extends Environmental> list, Environmental E);
	public int getContextNumber(ItemCollection cont, Environmental E);
	public String getContextName(Collection<? extends Environmental> list, Environmental E);
	public List<String> getAllContextNames(Collection<? extends Environmental> list, Filterer<Environmental> filter);
	public String getContextName(Environmental[] list, Environmental E);
	public String getContextName(ItemCollection cont, Environmental E);
	public int getContextSameNumber(Environmental[] list, Environmental E);
	public int getContextSameNumber(Collection<? extends Environmental> list, Environmental E);
	public int getContextSameNumber(ItemCollection cont, Environmental E);
	public String getContextSameName(Collection<? extends Environmental> list, Environmental E);
	public String getContextSameName(Environmental[] list, Environmental E);
	public String getContextSameName(ItemCollection cont, Environmental E);
	public List<String> parseWords(final String thisStr);

	/**
	 * Returns a matching exit object from an iterable collection of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the collection of exit objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matching object, or null
	 */
	public Exit fetchExit(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);

	/**
	 * Returns a matching environmental object from an iterable collection of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the collection of environmental objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matching object, or null
	 */
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);

	/**
	 * Returns a matching environmental object from an enumeration of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param iter the enumeraton of environmental objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matching object, or null
	 */
	public Environmental fetchEnvironmental(Enumeration<? extends Environmental> iter, String srchStr, boolean exactOnly);

	/**
	 * Returns a matching environmental object from map of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the map of environmental objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matching object, or null
	 */
	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly);

	/**
	 * Returns a matching environmental object from an iterator of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param iter the iterator of objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matching object, or null
	 */
	public Environmental fetchEnvironmental(Iterator<? extends Environmental> iter, String srchStr, boolean exactOnly);

	/**
	 * Returns a set of matching environmental objects from a collection of them
	 * by using a search string to matching. The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the list of objects to search through
	 * @param srchStr the search string
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a set of matched objects
	 */
	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly);

	/**
	 * Returns a matching item from a collection of them
	 * by using a search string to matching, a required filter, and
	 * a container which may be null.  The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include item ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the list of items to search through
	 * @param srchStr the search string
	 * @param goodLocation a container, or null
	 * @param filter the required filter
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matched item, or null
	 */
	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);

	/**
	 * Returns a set of matching items from a collection of them
	 * by using a search string to matching, a required filter, and
	 * a container which may be null.  The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include item ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the list of items to search through
	 * @param srchStr the search string
	 * @param goodLocation a container, or null
	 * @param filter the required filter
	 * @param exactOnly true for exact matching, false for inexact
	 * @return all matched items
	 */
	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);

	/**
	 * Returns a matching environmental object from a collection of them
	 * by using a search string to matching, a required filter, and for items,
	 * a container which may be null.  The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * @param list the list of objects to search through
	 * @param srchStr the search string
	 * @param goodLocation a container, or null
	 * @param filter the required filter
	 * @param exactOnly true for exact matching, false for inexact
	 * @return a matched object, or null
	 */
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);

	/**
	 * Returns a matching environmental object from a collection of them
	 * by using a search string to matching, a required filter, and for items,
	 * a container which may be null.  The search string may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * Matching may be exact or inexact.  Exact matching must include full
	 * names, display texts, etc.  Inexact can include substrings.  Both are
	 * case-insensitive, however.
	 *
	 * A one-dimensional integer array called counterSlap must be sent
	 * for tracking occurrences.  The occurrence sought will have the
	 * existing counterSlap value subtracted from it before matching,
	 * and will be updated afterwards if a match is found.
	 *
	 * @param list the list of objects to search through
	 * @param srchStr the search string
	 * @param goodLocation a container, or null
	 * @param filter the required filter
	 * @param exactOnly true for exact matching, false for inexact
	 * @param counterSlap a one-dimensional array, usually with 0
	 * @return a matched object, or null.  counterslap is modified
	 */
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly, int[] counterSlap);

	/**
	 * Returns a set of matching items from a given item possessor.  The items
	 * must match a string descriptor given in matchWords, as well as the given
	 * Filterer, and within the given container. The matchWords may include
	 * things like counters 1.itemname, etc, or even all itemname.
	 * The name matches may include item ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.  The mob argument is required due to having
	 * a viewer for the visionMatters argument, and as an alternate possible
	 * source of items when the item possessor (from) is a room.
	 *
	 * @param from The room or mob that possess items to select.
	 * @param mob the mob in the room, or the viewer of the items
	 * @param container the required container that the items should be in, or null
	 * @param matchWords the words that must be matched to select an item
	 * @param filter the required filter for items
	 * @param visionMatters true if the items must be seen by the mob, or false otherwise
	 * @return the list of all selected items, which could be empty.
	 */
	public List<Item> fetchItemList(ItemPossessor from, MOB mob, Item container, List<String> matchWords, Filterer<Environmental> filter, boolean visionMatters);

	/**
	 * Returns a matching shopkeeper from a given and his/her location, or null.
	 * The shopkeeper must match the minimal *end* of the string descriptor given
	 * in matchWords, and, if found, the method will remove those terms from the
	 * matchWords list.  The first word in matchWords is ALWAYS REMOVED, and so
	 * should always be a dummy term.
	 *
	 * The name matches may include object ids, names, or display text.  Exact
	 * matches can be demanded using the anchor $ character on the front, back,
	 * or both, of the match words.
	 *
	 * The returned shopkeeper may not itself implement the shopkeeper
	 * interface, but will, if not implement it, have an effect that
	 * does implement it.
	 *
	 * @param mob the mob looking for a shopkeeper nearby
	 * @param matchWords the search words, possibly modified
	 * @param error the error message to send for bad arguments
	 * @return a matched shopkeeper, with a modified matchWords list
	 */
	public Environmental parseShopkeeper(MOB mob, List<String> matchWords, String error);

	/**
	 * For cases when a string input probably contains an amount of money,
	 * this method is used to determine the amount (not value).  It will attempt
	 * to prefer whatever kind of money the given mine mob has on hand,
	 * if mine is specified at all, which matches the amount in the user
	 * input.  Otherwise, it will rely entirely on whatever clues the
	 * user input provides as to the amount.
	 *
	 * @param mine someone with money, like a mob, or null
	 * @param moneyStr a user input string to parse
	 * @return the amount of money units
	 */
	public long parseNumPossibleGold(Environmental mine, String moneyStr);

	/**
	 * For cases when a string input probably contains an amount of money,
	 * this method is used to determine the currency id.  It will attempt
	 * to prefer whatever kind of currency the given mine mob has on hand,
	 * if mine is specified at all, which matches the amount in the user
	 * input.  Otherwise, it will rely entirely on whatever clues the
	 * user input provides.
	 *
	 * @param mine someone with money, like a mob, or null
	 * @param moneyStr a user input string to parse
	 * @return the currency id to use
	 */
	public String parseNumPossibleGoldCurrency(Environmental mine, String moneyStr);

	/**
	 * For cases when a string input probably contains an amount of money,
	 * this method is used to determine the denomination.  It will attempt
	 * to prefer whatever kind of currency the given mine mob has on hand,
	 * if mine is specified at all.  Otherwise, it relies utterly on the
	 * given currency.
	 *
	 * @param mine someone with money, like a mob, or null
	 * @param currency currency to use if mine is unavail
	 * @param moneyStr a user input string to parse
	 * @return the denomination reflected by the input, or 0.0
	 */
	public double parseNumPossibleGoldDenomination(Environmental mine, String currency, String moneyStr);

	/**
	 * For cases when a string input probably contains an amount of money,
	 * this method is used to determine the currency, denomination, and
	 * number of units of that currency.  It will attempt to prefer
	 * whatever kind of currency the given mob has on hand.
	 *
	 * @param mob the user to use to find the right currency
	 * @param moneyStr the parsable user input
	 * @param correctCurrency a currency id to use, or null to use mob above
	 * @return the currency, denomination, and amount as a triad
	 */
	public Triad<String, Double, Long> parseMoneyStringSDL(MOB mob, String moneyStr, String correctCurrency);

	/**
	 * For cases when a string input must contain an amount of money,
	 * this method is used to determine the currency id reflect by
	 * the whole denomination specified by the given user input.
	 *
	 * @param moneyStr the user input that contains a denomination
	 * @return the currency id reflecting the found denomination, or null
	 */
	public String matchAnyCurrencySet(String moneyStr);

	/**
	 * For cases when a string input must contain an amount of money,
	 * this method is used to determine the Whole Denomination multiplier
	 * specified by the given user input, and optionally, from the given
	 * currency.
	 *
	 * @param currency currency id, or null will check all currencies, but why?
	 * @param moneyStr the user input that contains a denomination
	 * @return the whole denomination found, or 0.0 for no match
	 */
	public double matchAnyDenomination(String currency, String moneyStr);

	/**
	 * Attempts to determine if the given user input specifies an amount
	 * of currency in the given room, and if so, returns that precise
	 * amount parsed from the rooms existing currency.  If the input string
	 * does not seem to specify money, then null is quietly returned.
	 *
	 * @param seer the player looking to get some money
	 * @param room the room the money is in
	 * @param container the container the money is in, or null
	 * @param moneyStr the user input
	 * @return null, or the exact currency object to get
	 */
	public Item parsePossibleRoomGold(MOB seer, Room room, Container container, String moneyStr);

	/**
	 * Attempts to determine if the given user input specifies an amount
	 * of currency owned by the given player, and if so, returns that precise
	 * amount parsed from the players existing currency.  If the input string
	 * does not seem to specify money, then null is quietly returned.  if it
	 * does specify money, but more than the player has, then the player will
	 * receive an error, and null is still returned.
	 *
	 * @param mob the player looking to give away their money
	 * @param container the container it must be in, or null
	 * @param moneyStr the user input
	 * @return the players currency as an object, or null
	 */
	public Item parseBestPossibleGold(MOB mob, Container container, String moneyStr);

	/**
	 * Attempts to determine if the given user command input is specifying
	 * one or more containers, for cases when that would be possible.  It does this by
	 * looking for words like "from", "in", or "on", or the last word, and then
	 * checking whether it specifies a container.  If containers is found, the
	 * given input array is modified.  The containers may be in the players inv,
	 * or in the room.  The word 'all' is parsed out for multiple containers.
	 *
	 * @param mob the player whose commands are parsed
	 * @param commands the commands to parse and check
	 * @param filter filter for allowed containers, or null
	 * @param withContentOnly true if the container must not be empty
	 * @return list of all containers found, which may be empty
	 */
	public List<Container> parsePossibleContainers(MOB mob, List<String> commands, Filterer<Environmental> filter, boolean withContentOnly);

	/**
	 * Attempts to determine if the given user command input is specifying a container
	 * as the last argument, for cases when that would be possible.  It does this by
	 * looking for words like "from", "in", or "on", or the last word, and then
	 * checking whether it specifies a container.  If a container is found, the
	 * given input array is modified.  The container may be in the players inv,
	 * or in the room.
	 *
	 * @param mob the player whose commands are parsed
	 * @param commands the commands to parse and check
	 * @param withStuff true if the container must not be empty
	 * @param filter filter for allowed containers, or null
	 * @return the container found, or null
	 */
	public Item parsePossibleContainer(MOB mob, List<String> commands, boolean withStuff, Filterer<Environmental> filter);

	/**
	 * Converts the given number of milliseconds into ms, s, m, h, d, etc...
	 * If a number of ticks > 0 is also given, it will append them
	 * as an average ms/tick.
	 *
	 * @param millis the number of ellapsed milliseconds to convert
	 * @param ticks the number of ticks to average ms by
	 * @return a string efficiently describing this ellapsed time.
	 */
	public String stringifyElapsedTimeOrTicks(long millis, long ticks);

	/**
	 * Parses user input to determine if the user, who is
	 * specifying an item of some sort, is intending to
	 * specify multiples of those items by using a number
	 * in front of their input.  For example: get 10 bags
	 * This parser assumes the thing being given might
	 * be in the players inventory, or on the ground.
	 * This might modify the given commands list.
	 *
	 * @param mob the player to parse commands from
	 * @param commands the parsed input from the user
	 * @param breakPackages true to allow package breaking
	 * @param checkWhat Possessor (MOB or Room) to check.
	 * @param getOnly true if getting from a room
	 * @return -1 for error, or number to get 1-n.
	 */
	public int parseMaxToGive(MOB mob, List<String> commands, boolean breakPackages, Environmental checkWhat, boolean getOnly);

	/**
	 * Converts the given time specifier, or something that might
	 * be a time specifier, into the appropriate number of milliseconds.
	 * The given time must be a single word.  These are things like:
	 * ticks, seconds, minutes, etc, etc
	 *
	 * @param timeName an amount of time word
	 *
	 * @return the number of milliseconds, or -1
	 */
	public long getMillisMultiplierByName(String timeName);

	/**
	 * Returns the probability 0-100 of the given string being
	 * english.  Used for Scholar stuff.
	 *
	 * @param str the text to inspect
	 * @return 0-100 chance of it being english
	 */
	public int probabilityOfBeingEnglish(String str);

	/**
	 * Returns [best distance] for the given number.
	 * A [best distance] picks the best of dm, km, etc..
	 *
	 * @see EnglishParsing#distanceDescShort(long)
	 *
	 * @param size the full dm distance
	 * @return the best size string
	 */
	public String sizeDescShort(long size);

	/**
	 * Returns [best distance] for the given number.
	 * A [best distance] picks the best of dm, km, etc..
	 *
	 * @see EnglishParsing#sizeDescShort(long)
	 *
	 * @param distance the full dm distance
	 * @return the best distance string
	 */
	public String distanceDescShort(long distance);

	/**
	 * Returns [best distance],[best distance],[best distance]
	 * A [best distance] picks the best of dm, km, etc..
	 *
	 * @see EnglishParsing#sizeDescShort(long)
	 *
	 * @param coords the 3-absolute coords
	 * @return a displayable best distance coordinate
	 */
	public String coordDescShort(long[] coords);

	/**
	 * Rounds the given speed and returns [speed]/sec
	 * @param speed the raw speed double
	 * @return a friendlier display speed per sec
	 */
	public String speedDescShort(double speed);

	/**
	 * Returns direction in [degrees.YY] mark [degrees.YY]
	 *
	 * @see EnglishParsing#directionDescShortest(double[])
	 *
	 * @param dir the direction in radians
	 * @return a short direction string
	 */
	public String directionDescShort(double[] dir);

	/**
	 * Returns direction in [degrees.Y] [space] [degrees.Y]
	 *
	 * @see EnglishParsing#directionDescShort(double[])
	 *
	 * @param dir the direction in radians
	 * @return a shortest direction possible
	 */
	public String directionDescShortest(double[] dir);

	/**
	 * Returns the distance in decameters represented by the given
	 * parsable user-entered string.
	 *
	 * @param dist the user-entered string
	 * @return the distance in decameters
	 */
	public Long parseSpaceDistance(String dist);
}
