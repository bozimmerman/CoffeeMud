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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public interface MaskingLibrary extends CMObject
{
    public Hashtable getMaskCodes();
    public String rawMaskHelp();
    public String maskHelp(String CR, String word);
    public boolean tattooCheck(Vector V, char plusMinus, int fromHere, MOB mob);
    public boolean levelCheck(String text, char prevChar, int lastPlace, int lvl);
    public Vector levelCompiledHelper(String str, char c, Vector entry);
    public StringBuffer levelHelp(String str, char c, String append);
    public boolean fromHereEqual(Vector V, char plusMinus, int fromHere, String find);
    public boolean factionCheck(Vector V, char plusMinus, int fromHere, MOB mob);
    public boolean nameCheck(Vector V, char plusMinus, int fromHere, Environmental E);
    public boolean areaCheck(Vector V, char plusMinus, int fromHere, Environmental E);
    public boolean itemCheck(Vector V, char plusMinus, int fromHere, MOB mob);
    public boolean wornCheck(Vector V, char plusMinus, int fromHere, MOB mob);
    public boolean fromHereStartsWith(Vector V, char plusMinus, int fromHere, String find);
    public boolean fromHereEndsWith(Vector V, char plusMinus, int fromHere, String find);
    public String maskDesc(String text);
    public Vector maskCompile(String text);
    public boolean maskCheck(Vector cset, Environmental E);
    public boolean maskCheck(String text, Environmental E);
    
    public final String DEFAULT_MASK_HELP =
        "+SYSOP (allow archons or area staff to bypass the rules)  <BR>"
        +"-SYSOP (always <WORD> archons and area staff)  <BR>"
        +"-PLAYER (<WORD> all players) <BR>"
        +"-MOB (<WORD> all mobs/npcs)  <BR>"
        +"-CLASS  (<WORD> all classes)  <BR>"
        +"-BASECLASS  (<WORD> all base classes)  <BR>"
        +"+thief +mage +ranger (create exceptions to -class and -baseclass) <BR>"
        +"-thief -mage  -ranger (<WORD> only listed classes)<BR>"
        +"-RACE (<WORD> all races)  <BR>"
        +"+elf +dwarf +human +half +gnome (create exceptions to -race)  <BR>"
        +"-elf -dwarf -human -half -gnome (<WORD> only listed races)  <BR>"
        +"-RACECAT (<WORD> all racial categories)  <BR>"
        +"+RACECAT (do not <WORD> all racial categories)  <BR>"
        +"+elf +insect +humanoid +canine +gnome (create exceptions to -racecat)  <BR>"
        +"-elf -insect -humanoid -canine -gnome (create exceptions to +racecat)  <BR>"
        +"-ALIGNMENT (<WORD> all alignments)  <BR>"
        +"+evil +good +neutral (create exceptions to -alignment)  <BR>"
        +"-evil -good -neutral (<WORD> only listed alignments)  <BR>"
        +"-GENDER (<WORD> all genders)  <BR>"
        +"+male +female +neuter (create exceptions to -gender)  <BR>"
        +"-male -female -neuter (<WORD> only listed genders)  <BR>"
        +"-FACTION (<WORD> all faction and values, even a lack of faction) <BR>"
        +"+myfactionrange +myotherfactionrange (create exceptions to -faction) <BR>"
        +"-myfactionrange -myotherfactionrange (<WORD> only named faction range)<BR>"
        +"-TATTOOS (<WORD> all tattoos, even a lack of a tatoo) <BR>"
        +"+mytatto +thistattoo +anytattoo etc..  (create exceptions to -tattoos) <BR>"
        +"+TATTOOS (do not <WORD> any or no tattoos) <BR>"
        +"-mytattoo -anytatto, etc.. (create exceptions to +tattoos) <BR>"
        +"-LEVEL (<WORD> all levels)  <BR>"
        +"+=1 +>5 +>=7 +<13 +<=20 (create exceptions to -level using level ranges)  <BR>"
        +"-=1 ->5 ->=7 -<13 -<=20 (<WORD> only listed levels range) <BR>"
        +"-NAMES (<WORD> everyone) <BR>"
        +"+bob \"+my name\" etc.. (create name exceptions to -names) <BR>"
        +"+NAMES (do not <WORD> anyone who has a name) <BR>"
        +"-bob \"-my name\" etc.. (create name exceptions to +names) <BR>"
        +"-CLAN (<WORD> anyone, even no clan) <BR>"
        +"+Killers \"+Holy Avengers\" etc.. (create clan exceptions to -clan) <BR>"
        +"+CLAN (do not <WORD> anyone, even non clan people) <BR>"
        +"-Killers \"-Holy Avengers\" etc.. (create clan exceptions to +clan) <BR>"
        +"-DEITY (<WORD> anyone, even no deity) <BR>"
        +"+Apollo \"+Grothon The Great\" etc.. (create deity exceptions to -deity) <BR>"
        +"+DEITY (do not <WORD> anyone, even non deity worshipping people) <BR>"
        +"-Apollo \"-rothon The Great\" etc.. (create deity exceptions to +deity) <BR>"
        +"-ANYCLASS (<WORD> all multi-class combinations)  <BR>"
        +"+thief +mage +ranger (exceptions -anyclass, allow any levels) <BR>"
        +"+ANYCLASS (do not <WORD> all multi-class combinations)  <BR>"
        +"-thief -mage -ranger (exceptions to +anyclass, disallow any levels) <BR>"
        +"-STR X (<WORD> those with strength greater than X)  <BR>"
        +"+STR X (<WORD> those with strength less than X)  <BR>"
        +"-INT X (<WORD> those with intelligence greater than X)  <BR>"
        +"+INT X (<WORD> those with intelligence less than X)  <BR>"
        +"-WIS X (<WORD> those with wisdom greater than X)  <BR>"
        +"+WIS X (<WORD> those with wisdom less than X)  <BR>"
        +"-CON X (<WORD> those with constitution greater than X)  <BR>"
        +"+CON X (<WORD> those with constitution less than X)  <BR>"
        +"-CHA X (<WORD> those with charisma greater than X)  <BR>"
        +"+CHA X (<WORD> those with charisma less than X)  <BR>"
        +"-DEX X (<WORD> those with dexterity greater than X)  <BR>"
        +"+DEX X (<WORD> those with dexterity less than X) <BR>"
        +"-AREA (<WORD> in all areas) <BR>"
        +"\"+my areaname\" etc.. (create exceptions to +area) <BR>"
        +"+AREA (do not <WORD> any areas) <BR>"
        +"\"-my areaname\" etc.. (create exceptions to -area) <BR>"
        +"-ITEM \"+item name\" etc... (<WORD> only those with an item name) <BR>"
        +"-WORN \"+item name\" etc... (<WORD> only those wearing item name) <BR>"
        +"-EFFECTS (<WORD> anyone, even no effects) <BR>"
        +"+Sleep \"+Wood Chopping\" etc.. (create name exceptions to -effects) <BR>"
        +"+EFFECTS (do not <WORD> anyone, even non effected people) <BR>"
        +"-Sleep \"-Wood Chopping\" etc.. (create name exceptions to +effects) <BR>"
        +"-MATERIAL \"+WOODEN\" etc.. (<WORN> only items of added materials) <BR>"
        +"+MATERIAL \"-WOODEN\" etc.. (Do not <WORN> items of subtracted materials) <BR>"
        +"-RESOURCES \"+OAK\" etc.. (<WORN> only items of added resources) <BR>"
        +"+RESOURCES \"-OAK\" etc.. (Do not <WORN> items of subtracted resources) <BR>"
        +"-JAVACLASS \"+GENMOB\" etc.. (<WORN> only objects of added java class) <BR>"
        +"+JAVACLASS \"-GENITEM\" etc.. (Do not <WORN> objs of subtracted classes) <BR>"
        +"-RESOURCES \"+OAK\" etc.. (<WORN> only items of added resources) <BR>"
        +"+RESOURCES \"-OAK\" etc.. (Do not <WORN> items of subtracted resources) <BR>"
        +"-ABILITY X (<WORD> those with magical ability less than X)  <BR>"
        +"+ABILITY X (<WORD> those with magical ability greater than X) <BR>"
        +"-VALUE X (<WORD> those with value or money less than X)  <BR>"
        +"+VALUE X (<WORD> those with value or money greater than X) <BR>"
        +"-WEIGHT X (<WORD> those weighing less than X)  <BR>"
        +"+WEIGHT X (<WORD> those weighing more than X) <BR>"
        +"-ARMOR X (<WORD> those with armor bonus less than X)  <BR>"
        +"+ARMOR X (<WORD> those with armor bonus more than X) <BR>"
        +"-DAMAGE X (<WORD> those with damage bonus less than X)  <BR>"
        +"+DAMAGE X (<WORD> those with damage bonus more than X) <BR>"
        +"-ATTACK X (<WORD> those with attack bonus less than X)  <BR>"
        +"+ATTACK X (<WORD> those with attack bonus more than X) <BR>"
        +"-WORNON \"+TORSO\" etc.. (<WORN> only items wearable on added locs) <BR>"
        +"+WORNON \"-NECK\" etc.. (Do not <WORN> items wearable on subtracted locs) <BR>"
        +"-DISPOSITION \"+ISHIDDEN\" etc.. (<WORN> only with added dispositions) <BR>"
        +"+DISPOSITION \"-ISHIDDEN\" etc.. (Do not <WORN> only with sub disp) <BR>"
        +"-SENSES \"+CANSEEDARK\" etc.. (<WORN> only those with added sens.) <BR>"
        +"+SENSES \"-CANSEEDARK\" etc.. (Do not <WORN> those with subtracted sens.) <BR>"
        +"-HOUR X (<WORD> always, unless the hour is X)  <BR>"
        +"+HOUR X (<WORD> those only when the hour is X) <BR>"
        +"-SEASON FALL (<WORD> those only when season is FALL)  <BR>"
        +"+SEASON SPRING (<WORD> those whenever the season is SPRING) <BR>"
        +"-MONTH X (<WORD> those only when month number is X)  <BR>"
        +"+MONTH X (<WORD> those whenever the month number is X)";

}
