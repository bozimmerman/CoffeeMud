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
   Copyright 2005-2018 Bo Zimmerman

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
	public String cleanArticles(String s);
	public boolean startsWithAnArticle(String s);
	public String stripPunctuation(String str);
	public boolean hasPunctuation(String str);
	public String makePlural(String str);
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
	public List<String> parseWords(final String thisStr);
	public Exit fetchExit(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Iterable<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Enumeration<? extends Environmental> iter, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Map<String, ? extends Environmental> list, String srchStr, boolean exactOnly);
	public List<Environmental> fetchEnvironmentals(List<? extends Environmental> list, String srchStr, boolean exactOnly);
	public Environmental fetchEnvironmental(Iterator<? extends Environmental> iter, String srchStr, boolean exactOnly);
	public Item fetchAvailableItem(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
	public List<Item> fetchAvailableItems(List<Item> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
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
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly);
	public Environmental fetchAvailable(Collection<? extends Environmental> list, String srchStr, Item goodLocation, Filterer<Environmental> filter, boolean exactOnly, int[] counterSlap);
	public Environmental parseShopkeeper(MOB mob, List<String> commands, String error);
	public List<Item> fetchItemList(Environmental from, MOB mob, Item container, List<String> commands, Filterer<Environmental> filter, boolean visionMatters);
	public long numPossibleGold(Environmental mine, String itemID);
	public String numPossibleGoldCurrency(Environmental mine, String itemID);
	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID);
	public Triad<String, Double, Long> parseMoneyStringSDL(MOB mob, String amount, String correctCurrency);
	public long getMillisMultiplierByName(String timeName);
	public String matchAnyCurrencySet(String moneyStr);
	public double matchAnyDenomination(String currency, String moneyStr);
	public Item possibleRoomGold(MOB seer, Room room, Container container, String moneyStr);
	public Item bestPossibleGold(MOB mob, Container container, String moneyStr);
	public List<Container> possibleContainers(MOB mob, List<String> commands, Filterer<Environmental> filter, boolean withContentOnly);
	public Item possibleContainer(MOB mob, List<String> commands, boolean withStuff, Filterer<Environmental> filter);
	public String returnTime(long millis, long ticks);
	public int calculateMaxToGive(MOB mob, List<String> commands, boolean breakPackages, Environmental checkWhat, boolean getOnly);
	public int probabilityOfBeingEnglish(String str);
	public String sizeDescShort(long size);
	public String distanceDescShort(long distance);
	public String coordDescShort(long[] coords);
	public String speedDescShort(double speed);
	public String directionDescShort(double[] dir);
	public Long parseSpaceDistance(String dist);
}
