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
	public String bumpDotNumber(String srchStr);
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
	public Exit fetchExit(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Enumeration<? extends Environmental> iter, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly);
	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Iterator<? extends Environmental> iter, String srchStr, boolean exactOnly);
	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly, int[] counterSlap);
	public Environmental parseShopkeeper(MOB mob, List<String> commands, String error);
	public List<Item> fetchItemList(Environmental from, MOB mob, Item container, List<String> commands, Filterer<Environmental> filter, boolean visionMatters);
	public long parseNumPossibleGold(Environmental mine, String itemID);
	public String parseNumPossibleGoldCurrency(Environmental mine, String itemID);
	public double parseNumPossibleGoldDenomination(Environmental mine, String currency, String itemID);
	public Triad<String, Double, Long> parseMoneyStringSDL(MOB mob, String amount, String correctCurrency);
	public long getMillisMultiplierByName(String timeName);
	public String matchAnyCurrencySet(String moneyStr);
	public double matchAnyDenomination(String currency, String moneyStr);
	public Item parsePossibleRoomGold(MOB seer, Room room, Container container, String moneyStr);
	public Item parseBestPossibleGold(MOB mob, Container container, String moneyStr);

	public List<Container> parsePossibleContainers(MOB mob, List<String> commands, Filterer<Environmental> filter, boolean withContentOnly);

	public Item parsePossibleContainer(MOB mob, List<String> commands, boolean withStuff, Filterer<Environmental> filter);

	public String returnTime(long millis, long ticks);
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
