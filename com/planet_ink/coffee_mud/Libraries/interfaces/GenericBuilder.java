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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2013 Bo Zimmerman

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
	public String getGenAbilityXML(Ability A);
	public String getPropertiesStr(Environmental E, boolean fromTop);
	public String getOrdPropertiesStr(Environmental E);
	public String getGenMobAbilities(MOB M);
	public String getGenScripts(PhysicalAgent E, boolean includeVars);
	public String getGenMobInventory(MOB M);
	public String getGenPropertiesStr(Environmental E);
	public String unpackErr(String where, String msg);
	public String unpackRoomFromXML(String buf, boolean andContent);
	public String unpackRoomFromXML(List<XMLpiece> xml, boolean andContent);
	public String fillAreaAndCustomVectorFromXML(String buf,  List<XMLpiece> area, List<CMObject> custom, Map<String,String> externalFiles);
	public String fillCustomVectorFromXML(String xml, List<CMObject> custom, Map<String,String> externalFiles);
	public String fillCustomVectorFromXML(List<XMLpiece> xml,  List<CMObject> custom, Map<String,String> externalFiles);
	public String fillAreasVectorFromXML(String buf,  List<List<XMLpiece>> areas, List<CMObject> custom, Map<String,String> externalFiles);
	public void addAutoPropsToAreaIfNecessary(Area newArea);
	public String unpackAreaFromXML(List<XMLpiece> aV, Session S, String overrideAreaType, boolean andRooms);
	public String unpackAreaFromXML(String buf, Session S, String overrideAreaType, boolean andRooms);
	public StringBuffer getAreaXML(Area area,  Session S, Set<CMObject> custom, Set<String> files, boolean andRooms);
	public StringBuffer logTextDiff(String e1, String e2);
	public void logDiff(Environmental E1, Environmental E2);
	public Room makeNewRoomContent(Room room, boolean makeLive);
	public StringBuffer getRoomMobs(Room room, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);
	public StringBuffer getMobXML(MOB mob);
	public StringBuffer getMobsXML(List<MOB> mobs, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);
	public StringBuffer getUniqueItemXML(Item item, int type, Map<String,List<Item>> found, Set<String> files);
	public String addItemsFromXML(String xmlBuffer, List<Item> addHere, Session S);
	public String addMOBsFromXML(String xmlBuffer, List<MOB> addHere, Session S);
	public MOB getMobFromXML(String xmlBuffer);
	public Item getItemFromXML(String xmlBuffer);
	// TYPE= 0=item, 1=weapon, 2=armor
	public StringBuffer getRoomItems(Room room, Map<String,List<Item>> found, Set<String> files, int type); 
	public StringBuffer getItemsXML(List<Item> items, Map<String,List<Item>> found, Set<String> files, int type);
	public StringBuffer getItemXML(Item item);
	public StringBuffer getRoomXML(Room room,  Set<CMObject> custom, Set<String> files, boolean andContent);
	public Ammunition makeAmmunition(String ammunitionType, int number);
	public void setPropertiesStr(Environmental E, String buf, boolean fromTop);
	public void recoverPhysical(Physical P);
	public void setPropertiesStr(Environmental E, List<XMLpiece> V, boolean fromTop);
	public void setOrdPropertiesStr(Environmental E, List<XMLpiece> V);
	public void setGenMobAbilities(MOB M, List<XMLpiece> buf);
	public void setGenScripts(PhysicalAgent E, List<XMLpiece> buf, boolean restoreVars);
	public void setGenMobInventory(MOB M, List<XMLpiece> buf);
	public void populateShops(Environmental E, List<XMLpiece> buf);
	public void setGenPropertiesStr(Environmental E, List<XMLpiece> buf);
	public String getPlayerXML(MOB mob, Set<CMObject> custom, Set<String> files);
	public String addPLAYERsFromXML(String xmlBuffer, List<MOB> addHere, Session S);
	public String getExtraEnvPropertiesStr(Environmental E);
	public void fillFileSet(List<String> V, Set<String> H);
	public void fillFileSet(Environmental E, Set<String> H);
	public String getPhyStatsStr(PhyStats E);
	public String getCharStateStr(CharState E);
	public String getCharStatsStr(CharStats E);
	public String getEnvPropertiesStr(Environmental E);
	public void setCharStats(CharStats E, String props);
	public void setCharState(CharState E, String props);
	public void setPhyStats(PhyStats E, String props);
	public void setEnvProperties(Environmental E, List<XMLpiece> buf);
	public String identifier(Environmental E, Environmental parent);
	public void setExtraEnvProperties(Environmental E, List<XMLpiece> buf);
	public void setAnyGenStat(Physical P, String stat, String value, boolean supportPlusMinusPrefix);
	public void setAnyGenStat(Physical P, String stat, String value);
	public String getAnyGenStat(Physical P, String stat);
	public List<String> getAllGenStats(Physical P);
	public boolean isAnyGenStat(Physical P, String stat);
	public int getGenItemCodeNum(String code);
	public String getGenItemStat(Item I, String code);
	public void setGenItemStat(Item I, String code, String val);
	public int getGenMobCodeNum(String code);
	public String getGenMobStat(MOB M, String code);
	public void setGenMobStat(MOB M, String code, String val);
	public Area copyArea(Area A, String newName);
	public String getFactionXML(MOB mob);
	public void setFactionFromXML(MOB mob, List<XMLpiece> xml);
}
