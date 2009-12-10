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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface GenericBuilder extends CMLibrary
{
    public final static String[] GENITEMCODES={
            "CLASS","USES","LEVEL","ABILITY","NAME",
            "DISPLAY","DESCRIPTION","SECRET","PROPERWORN",
            "WORNAND","BASEGOLD","ISREADABLE","ISDROPPABLE",
            "ISREMOVABLE","MATERIAL","AFFBEHAV",
            "DISPOSITION","WEIGHT","ARMOR",
            "DAMAGE","ATTACK","READABLETEXT","IMG"};
    public final static String[] GENMOBCODES={
            "CLASS","RACE","LEVEL","ABILITY","NAME",
            "DISPLAY","DESCRIPTION","MONEY","ALIGNMENT",
            "DISPOSITION","SENSES","ARMOR",
            "DAMAGE","ATTACK","SPEED","AFFBEHAV",
            "ABLES","INVENTORY","TATTS","EXPS","IMG",
            "FACTIONS","VARMONEY"};
    
    public boolean get(int x, int m);
    public String getGenMOBTextUnpacked(MOB mob, String newText);
    public void resetGenMOB(MOB mob, String newText);
    public int envFlags(Environmental E);
    public void setEnvFlags(Environmental E, int f);
    public String getPropertiesStr(Environmental E, boolean fromTop);
    public String getOrdPropertiesStr(Environmental E);
    public String getGenMobAbilities(MOB M);
    public String getGenScripts(Environmental E, boolean includeVars);
    public String getGenMobInventory(MOB M);
    public String getGenPropertiesStr(Environmental E);
    public String unpackErr(String where, String msg);
    public String unpackRoomFromXML(String buf, boolean andContent);
    public String unpackRoomFromXML(Vector xml, boolean andContent);
    public String fillAreaAndCustomVectorFromXML(String buf,  Vector area, Vector custom, Hashtable externalFiles);
    public String fillCustomVectorFromXML(String xml,  Vector custom, Hashtable externalFiles);
    public String fillCustomVectorFromXML(Vector xml,  Vector custom, Hashtable externalFiles);
    public String fillAreasVectorFromXML(String buf,  Vector areas, Vector custom, Hashtable externalFiles);
    public void addAutoPropsToAreaIfNecessary(Area newArea);
    public String unpackAreaFromXML(Vector aV, Session S, boolean andRooms);
    public String unpackAreaFromXML(String buf, Session S, boolean andRooms);
    public StringBuffer getAreaXML(Area area,  Session S, HashSet custom, HashSet files, boolean andRooms);
    public StringBuffer logTextDiff(String e1, String e2);
    public void logDiff(Environmental E1, Environmental E2);
    public Room makeNewRoomContent(Room room);
    public StringBuffer getRoomMobs(Room room, HashSet custom, HashSet files, Hashtable found);
    public StringBuffer getMobXML(MOB mob);
    public StringBuffer getMobsXML(Vector mobs, HashSet custom, HashSet files, Hashtable found);
    public StringBuffer getUniqueItemXML(Item item, int type, Hashtable found, HashSet files);
    public String addItemsFromXML(String xmlBuffer, Vector addHere, Session S);
    public String addMOBsFromXML(String xmlBuffer, Vector addHere, Session S);
    public MOB getMobFromXML(String xmlBuffer);
    public Item getItemFromXML(String xmlBuffer);
    // TYPE= 0=item, 1=weapon, 2=armor
    public StringBuffer getRoomItems(Room room, Hashtable found, HashSet files, int type); 
    public StringBuffer getItemsXML(Vector items, Hashtable found, HashSet files, int type);
    public StringBuffer getItemXML(Item item);
    public StringBuffer getRoomXML(Room room,  HashSet custom, HashSet files, boolean andContent);
    public void setPropertiesStr(Environmental E, String buf, boolean fromTop);
    public void recoverEnvironmental(Environmental E);
    public void setPropertiesStr(Environmental E, Vector V, boolean fromTop);
    public void setOrdPropertiesStr(Environmental E, Vector V);
    public void setGenMobAbilities(MOB M, Vector buf);
    public void setGenScripts(Environmental E, Vector buf, boolean restoreVars);
    public void setGenMobInventory(MOB M, Vector buf);
    public void populateShops(Environmental E, Vector buf);
    public void setGenPropertiesStr(Environmental E, Vector buf);
    public String getPlayerXML(MOB mob, HashSet custom, HashSet files);
    public String addPLAYERsFromXML(String xmlBuffer, Vector addHere, Session S);
    public String getExtraEnvPropertiesStr(Environmental E);
    public void fillFileSet(Vector V, HashSet H);
    public void fillFileSet(Environmental E, HashSet H);
    public String getEnvStatsStr(EnvStats E);
    public String getCharStateStr(CharState E);
    public String getCharStatsStr(CharStats E);
    public String getEnvPropertiesStr(Environmental E);
    public void setCharStats(CharStats E, String props);
    public void setCharState(CharState E, String props);
    public void setEnvStats(EnvStats E, String props);
    public void setEnvProperties(Environmental E, Vector buf);
    public String identifier(Environmental E, Environmental parent);
    public void setExtraEnvProperties(Environmental E, Vector buf);
    public int getGenItemCodeNum(String code);
    public String getGenItemStat(Item I, String code);
    public void setGenItemStat(Item I, String code, String val);
    public int getGenMobCodeNum(String code);
    public String getGenMobStat(MOB M, String code);
    public void setGenMobStat(MOB M, String code, String val);
    public Area copyArea(Area A, String newName);
    public String getFactionXML(MOB mob);
    public void setFactionFromXML(MOB mob, Vector xml);
}
