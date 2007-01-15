package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2007 Bo Zimmerman

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
    public static final int FLAG_STR=0;
    public static final int FLAG_DOT=1;
    public static final int FLAG_ALL=2;
    
    public boolean isAnArticle(String s);
    public String cleanArticles(String s);
    public Object findCommand(MOB mob, Vector commands);
    public boolean evokedBy(Ability thisAbility, String thisWord);
    public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord);
    public String getAnEvokeWord(MOB mob, String word);
    public Ability getToEvoke(MOB mob, Vector commands);
    public boolean preEvoke(MOB mob, Vector commands, int secondsElapsed, double actionsRemaining);
    public void evoke(MOB mob, Vector commands);
    public boolean containsString(String toSrchStr, String srchStr);
    public String bumpDotNumber(String srchStr);
    public Object[] fetchFlags(String srchStr);
    public Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly);
    public Environmental fetchEnvironmental(Hashtable list, String srchStr, boolean exactOnly);
    public Environmental fetchEnvironmental(Environmental[] list, String srchStr, boolean exactOnly);
    public Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly);
    public Environmental fetchAvailable(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly);
    public Environmental parseShopkeeper(MOB mob, Vector commands, String error);
    public Vector fetchItemList(Environmental from, MOB mob, Item container, Vector commands, int preferredLoc, boolean visionMatters);
    public long numPossibleGold(Environmental mine, String itemID);
    public String numPossibleGoldCurrency(Environmental mine, String itemID);
    public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID);
    public String matchAnyCurrencySet(String itemID);
    public double matchAnyDenomination(String currency, String itemID);
    public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID);
    public Item bestPossibleGold(MOB mob, Container container, String itemID);
    public Vector possibleContainers(MOB mob, Vector commands, int wornReqCode, boolean withContentOnly);
    public Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornReqCode);
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp) throws IOException;
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK) throws IOException;
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint) throws IOException;
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, String help) throws IOException;
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, String help) throws IOException;
    public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String FieldDisp, boolean emptyOK, boolean rawPrint, String help) throws IOException;
    public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp) throws IOException;
    public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String FieldDisp, String help) throws IOException;
    public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp) throws IOException;
    public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String FieldDisp, String help) throws IOException;
    public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp) throws IOException;
    public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String FieldDisp, String help) throws IOException;
    public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String FieldDisp) throws IOException;
    public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String FieldDisp, String help) throws IOException;
    public void promptStatStr(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field) throws IOException;
    public void promptStatStr(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field, boolean emptyOK) throws IOException;
    public void promptStatInt(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field) throws IOException;
    public void promptStatInt(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field) throws IOException;
    public void promptStatBool(MOB mob, CMModifiable E, int showNumber, int showFlag, String FieldDisp, String Field) throws IOException;
    public void promptStatBool(MOB mob, CMModifiable E, String help, int showNumber, int showFlag, String FieldDisp, String Field) throws IOException;
    public String returnTime(long millis, long ticks);
}
