package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2008-2022 Bo Zimmerman

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
 * The Generic CMObject Building library is the heart and core of
 * CoffeeMud 2.0, formerly called "Generic".  The purpose is to
 * provide abstract methods for handling the modifiable aspects of
 * CMObjects, such as manipulating and reading fields, converting
 * to and from XML, etc.
 *
 * @author Bo Zimmerman
 *
 */
public interface GenericBuilder extends CMLibrary
{
	/**
	 * Enum for the most basic fields common to
	 * all objects that implement the Item interface.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum GenItemCode
	{
		CLASS,
		USES,
		LEVEL,
		ABILITY,
		NAME,
		DISPLAY,
		DESCRIPTION,
		SECRET,
		PROPERWORN,
		WORNAND,
		BASEGOLD,
		ISREADABLE,
		ISGETTABLE,
		ISDROPPABLE,
		ISREMOVABLE,
		MATERIAL,
		AFFBEHAV,
		DISPOSITION,
		WEIGHT,
		ARMOR,
		DAMAGE,
		ATTACK,
		READABLETEXT,
		IMG
		;
		private static String[] CODES=null;

		public static String[] getAllCodeNames()
		{
			if(CODES==null)
			{
				CODES=new String[GenItemCode.values().length];
				for(final GenItemCode code : GenItemCode.values())
					CODES[code.ordinal()] = code.name();
			}
			return CODES;
		}
	}

	public enum GenMOBCode
	{
		CLASS,
		RACE,
		LEVEL,
		ABILITY,
		NAME,
		DISPLAY,
		DESCRIPTION,
		MONEY,
		ALIGNMENT,
		DISPOSITION,
		SENSES,
		ARMOR,
		DAMAGE,
		ATTACK,
		SPEED,
		AFFBEHAV,
		ABLES,
		INVENTORY,
		TATTS,
		EXPS,
		IMG,
		FACTIONS,
		VARMONEY
		;
		private static String[] CODES=null;

		public static String[] getAllCodeNames()
		{
			if(CODES==null)
			{
				CODES=new String[GenItemCode.values().length];
				for(final GenItemCode code : GenItemCode.values())
					CODES[code.ordinal()] = code.name();
			}
			return CODES;
		}
	}

	public enum GenMOBBonusFakeStats
	{
		QUESTPOINTS,
		FOLLOWERS,
		TRAINS,
		PRACTICES,
		STINK,
		CHARCLASS,
		ALIGNMENT,
		INCLINATION,
		FACTIONID,
		FACTIONAMT,
		DEITY,
		MATTRIB,
		CLAN,
		CLANROLE,
		OBJATTRIB
	}

	public enum GenItemBonusFakeStats
	{
		MATERIALNAME,
		RESOURCENAME,
		LIQUIDREMAINING
	}

	public enum GenPhysBonusFakeStats
	{
		DISPOSITIONSTR,
		SENSESSTR,
		CURRENCY,
		CURRENCY_NAME,
		DENOMINATION_NAME
	}

	public String getGenMOBTextUnpacked(MOB mob, String newText);
	public void resetGenMOB(MOB mob, String newText);
	public int envFlags(Environmental E);
	public void setEnvFlags(Environmental E, int f);
	public String getGenAbilityXML(Ability A);
	public String addAbilitiesFromXml(final String xml, final List<Ability> ables);
	public String getPropertiesStr(Environmental E, boolean fromTop);
	public String getGenScripts(PhysicalAgent E, boolean includeVars);
	public String getGenMobInventory(MOB M);
	public void doGenPropertiesCopy(Environmental fromE, Environmental toE);
	public String unpackRoomFromXML(String buf, boolean andContent);
	public Environmental getUnknownFromXML(final String xml);
	public StringBuffer getUnknownXML(final Environmental obj);
	public CMClass.CMObjectType getUnknownTypeFromXML(final String xml);
	public String getUnknownNameFromXML(final String xml);
	public String unpackRoomFromXML(List<XMLTag> xml, boolean andContent);
	public String unpackRoomFromXML(final Area forceArea, final List<XMLTag> xml, final boolean andContent, final boolean andSave);
	public String fillAreaAndCustomVectorFromXML(String buf,  List<XMLTag> area, List<CMObject> custom, Map<String,String> externalFiles);
	public String fillCustomVectorFromXML(String xml, List<CMObject> custom, Map<String,String> externalFiles);
	public String fillCustomVectorFromXML(List<XMLTag> xml,  List<CMObject> custom, Map<String,String> externalFiles);
	public String fillAreasVectorFromXML(String buf,  List<List<XMLTag>> areas, List<CMObject> custom, Map<String,String> externalFiles);
	public void addAutoPropsToAreaIfNecessary(Area newArea);
	public Area unpackAreaObjectFromXML(String xml) throws CMException;
	public String unpackAreaFromXML(List<XMLTag> aV, Session S, String overrideAreaType, boolean andRooms, boolean savable);
	public String unpackAreaFromXML(String buf, Session S, String overrideAreaType, boolean andRooms);
	public StringBuffer getAreaXML(Area area,  Session S, Set<CMObject> custom, Set<String> files, boolean andRooms);
	public StringBuffer getAreaObjectXML(Area area, Session S, Set<CMObject> custom, Set<String> files, boolean andRooms);
	public StringBuffer logTextDiff(String e1, String e2);
	public void logDiff(Environmental E1, Environmental E2);
	public Room makeNewRoomContent(Room room, boolean makeLive);
	public StringBuffer getRoomMobs(Room room, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);
	public StringBuffer getMobXML(MOB mob);
	public StringBuffer getMobsXML(List<MOB> mobs, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);
	public StringBuffer getUniqueItemXML(Item item, CMObjectType type, Map<String,List<Item>> found, Set<String> files);
	public String addItemsFromXML(String xmlBuffer, List<Item> addHere, Session S);
	public String addMOBsFromXML(String xmlBuffer, List<MOB> addHere, Session S);
	public String addItemsFromXML(List<XMLTag> xml, List<Item> addHere, Session S);
	public String addMOBsFromXML(List<XMLTag> xml, List<MOB> addHere, Session S);
	public String addCataDataFromXML(String xmlBuffer, List<CataData> addHere, List<? extends Physical> nameMatchers, Session S);
	public MOB getMobFromXML(String xmlBuffer);
	public Item getItemFromXML(String xmlBuffer);
	// TYPE= 0=item, 1=weapon, 2=armor
	public StringBuffer getRoomItems(Room room, Map<String,List<Item>> found, Set<String> files, CMObjectType type);
	public StringBuffer getItemsXML(List<Item> items, Map<String,List<Item>> found, Set<String> files, CMObjectType type);
	public StringBuffer getItemXML(Item item);
	public StringBuffer getRoomXML(Room room,  Set<CMObject> custom, Set<String> files, boolean andContent);
	public Ammunition makeAmmunition(String ammunitionType, int number);
	public void setPropertiesStr(Environmental E, String buf, boolean fromTop);
	public void setPropertiesStr(Environmental E, List<XMLTag> V, boolean fromTop);
	public void setGenScripts(PhysicalAgent E, List<XMLTag> buf, boolean restoreVars);
	public void populateShops(ShopKeeper shopKeep, List<XMLTag> buf);
	public String getPlayerXML(MOB mob, Set<CMObject> custom, Set<String> files);
	public String getAccountXML(PlayerAccount account, Set<CMObject> custom, Set<String> files);
	public String addPlayersAndAccountsFromXML(String xmlBuffer, List<PlayerAccount> addAccounts, List<MOB> addMobs, Session S);
	public String getExtraEnvPropertiesStr(Environmental E);
	public void fillFileSet(Environmental E, Set<String> H);
	public void fillFileMap(Environmental E, Map<String,Set<Environmental>> H);
	public String getPhyStatsStr(PhyStats E);
	public String getCharStateStr(CharState E);
	public String getCharStatsStr(CharStats E);
	public String getEnvPropertiesStr(Environmental E);
	public void setCharStats(CharStats E, String props);
	public void setCharState(CharState E, String props);
	public void setPhyStats(PhyStats E, String props);
	public String getQuickName(final String classID, final String miscText);
	public void setEnvProperties(Environmental E, List<XMLTag> buf);
	public void setExtraEnvProperties(Environmental E, List<XMLTag> buf);

	/**
	 * Sets the value of the given stat on the given object,
	 * even if the object is not generic. This includes not only the Generic codes,
	 * but also any fakeitem or fakemob stat codes as well.  All Physical
	 * type objects are supported.  This method supports deltas to numeric
	 * values as well, by sending a value with + or - as a prefix.
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String)
	 * @see GenericBuilder#getAnyGenStat(Physical, String)
	 * @see GenericBuilder#isAnyGenStat(Physical, String)
	 *
	 * @param P the type of object to change
	 * @param stat the stat code to change
	 * @param value the new value for the stat
	 * @param supportPlusMinusPrefix true to support += prefix
	 *
	 */
	public void setAnyGenStat(Physical P, String stat, String value, boolean supportPlusMinusPrefix);

	/**
	 * Sets the value of the given stat on the given object,
	 * even if the object is not generic. This includes not only the Generic codes,
	 * but also any fakeitem or fakemob stat codes as well.  All Physical
	 * type objects are supported.
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String, boolean)
	 * @see GenericBuilder#getAnyGenStat(Physical, String)
	 * @see GenericBuilder#isAnyGenStat(Physical, String)
	 *
	 * @param P the type of object to change
	 * @param stat the stat code to change
	 * @param value the new value for the stat
	 */
	public void setAnyGenStat(Physical P, String stat, String value);

	/**
	 * Returns the value of the given stat on the given object,
	 * even if the object is not generic. This includes not only the Generic codes,
	 * but also any fakeitem or fakemob stat codes as well.  All Physical
	 * type objects are supported.
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String)
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String, boolean)
	 * @see GenericBuilder#isAnyGenStat(Physical, String)
	 *
	 * @param P the type of object to read
	 * @param stat the stat code to read
	 * @return the value of the stat, or ""
	 */
	public String getAnyGenStat(Physical P, String stat);

	/**
	 * Returns whether the given string represents any of the
	 * "AnyGen" stat codes for the given type object, even if the
	 * object is not generic.  This includes not only the Generic codes,
	 * but also any fakeitem or fakemob stat codes as well.  All Physical
	 * type objects are supported.
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String)
	 * @see GenericBuilder#setAnyGenStat(Physical, String, String, boolean)
	 * @see GenericBuilder#getAnyGenStat(Physical, String)
	 *
	 * @param P the type of object to check
	 * @param stat the stat code to check
	 * @return true if the stat code can apply, false otherwise
	 */
	public boolean isAnyGenStat(Physical P, String stat);

	/**
	 * Returns the list of basic generic state codes applicable
	 * to the given object.  It does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenItem or GenMobBonusFakeStats stats.
	 * @param P the object to get stat codes for
	 * @return the list of stat codes.
	 */
	public List<String> getAllGenStats(Physical P);

	/**
	 * Removes any qualifying prefixes from stat names, to
	 * reveal the underlying stat code.  Prefixes include
	 * things like CURRENT_, CURRENT ,BASE_, BASE ,MAX_, and MAX
	 * @param stat the possibly qualified stat code
	 * @return the underlying state code
	 */
	public String getFinalStatName(String stat);

	/**
	 * Returns the ordinal of the given code in the basic
	 * genitem stat codes.  It does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenItemBonusFakeStats stats.
	 * @see GenericBuilder#getGenItemStat(MOB, String)
	 * @see GenericBuilder#setGenItemStat(MOB, String, String)
	 *
	 * @param code the stat code
	 * @return the ordinal of the stat
	 */
	public int getGenItemCodeNum(String code);

	/**
	 * Gets the value of a basic genitem stat, even if the mob given
	 * is not generic.  Therefore, it does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenItemBonusFakeStats stats.
	 * @see GenericBuilder#getGenItemCodeNum(String)
	 * @see GenericBuilder#setGenItemStat(MOB, String, String)
	 *
	 * @param I the item object to read
	 * @param code the stat code to return the value of
	 * @return the value of the stat
	 */
	public String getGenItemStat(Item I, String code);

	/**
	 * Sets the value of a basic genitem stat, even if the mob given
	 * is not generic.  Therefore, it does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenItemBonusFakeStats stats.
	 * @see GenericBuilder#getGenItemStat(MOB, String)
	 * @see GenericBuilder#getGenItemCodeNum(String)
	 *
	 * @param I the item object to modify
	 * @param code the stat code to modify
	 * @param val the value to set the stat to
	 */
	public void setGenItemStat(Item I, String code, String val);

	/**
	 * Returns the ordinal of the given code in the basic
	 * genmob stat codes.
	 * @see GenericBuilder#getGenMobStat(MOB, String)
	 * @see GenericBuilder#setGenMobStat(MOB, String, String)
	 *
	 * @param code the stat code
	 * @return the ordinal of the stat
	 */
	public int getGenMobCodeNum(String code);

	/**
	 * Gets the value of a basic genmob stat, even if the mob given
	 * is not generic.  Therefore, it does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenMOBBonusFakeStats stats.
	 * @see GenericBuilder#getGenMobCodeNum(String)
	 * @see GenericBuilder#setGenMobStat(MOB, String, String)
	 *
	 * @param M the mob object to read
	 * @param code the stat code to return the value of
	 * @return the value of the stat
	 */
	public String getGenMobStat(MOB M, String code);

	/**
	 * Sets the value of a basic genmob stat, even if the mob given
	 * is not generic.  Therefore, it does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenMOBBonusFakeStats stats.
	 * @see GenericBuilder#getGenMobStat(MOB, String)
	 * @see GenericBuilder#getGenMobCodeNum(String)
	 *
	 * @param M the mob object to modify
	 * @param code the stat code to modify
	 * @param val the value to set the stat to
	 */
	public void setGenMobStat(MOB M, String code, String val);

	/**
	 * This makes either a full database, or simple memory copy
	 * of the given area with the given new name
	 *
	 * @param A the area to copy, including rooms, items, etc..
	 * @param newName the name of the area copy
	 * @param setSavable true to save to db, false for ram
	 * @return the new area object
	 */
	public Area copyArea(Area A, String newName, boolean setSavable);

	/**
	 * Converts all the faction values and associations on the given
	 * mob into a complete xml doc for storage in the db.
	 * @see GenericBuilder#setFactionFromXML(MOB, List)
	 *
	 * @param mob the mob to grab faction associations from
	 * @return the xml doc of all factions on the mob
	 */
	public String getFactionXML(MOB mob);

	/**
	 * Sets the faction values and associations on the given
	 * mob from a pre-parsed xml doc.  The list of tags
	 * must include one called "FACTIONS".
	 *
	 * @see GenericBuilder#getFactionXML(MOB)
	 *
	 * @param mob the mob to set faction associations on
	 * @param xml the list of pre-parsed xml tags
	 */
	public void setFactionFromXML(MOB mob, List<XMLTag> xml);

	/**
	 * Returns all of the given effect Abilities on the given Affectable as a semicolon delimited
	 * string of Ability IDs.  If any of the abilities contain parameters, they come after the
	 * ability and another semicolon.  This method can't really capture all permutations and
	 * combinations, but, well, it seemed like a good idea at the time.
	 * @see Affectable#effects()
	 * @see GenericBuilder#getCodedSpellsOrBehaviors(String)
	 *
	 * @param I the Affectable one to look at the effects of
	 * @return the coded string of those effects
	 */
	public String getCodedSpellsOrBehaviors(PhysicalAgent I);

	/**
	 * Parses the coded effects available from an ability parameter column and generates
	 * the Ability objects with any parameters of their own.
	 * @see Affectable#effects()
	 * @see GenericBuilder#getCodedSpellsOrBehaviors(PhysicalAgent)
	 *
	 * @param spells the coded ability parameter affectable effects string
	 * @return the list of ability which are the effects
	 */
	public List<CMObject> getCodedSpellsOrBehaviors(String spells);
}
