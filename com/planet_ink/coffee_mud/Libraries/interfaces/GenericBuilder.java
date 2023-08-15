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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2008-2023 Bo Zimmerman

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

	/**
	 * The basic enum of GenMOB Modifiable
	 * codes used by the Modifiable interface
	 * on all mob derivatives.
	 *
	 * @author Bo Zimmerman
	 */
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

	/**
	 * An enum for properties found on
	 * MOB objects that are derivative,
	 * or otherwise not available in
	 * this form from the Modifiable
	 * interface.
	 *
	 * @author Bo Zimmerman
	 */
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
		OBJATTRIB,
		BUDGETRESETDATE,
		INVENTORYRESETDATE
	}

	/**
	 * An enum for properties found on
	 * Item objects that are derivative,
	 * or otherwise not available in
	 * this form from the Modifiable
	 * interface.
	 *
	 * @author Bo Zimmerman
	 */
	public enum GenItemBonusFakeStats
	{
		MATERIALNAME,
		RESOURCENAME,
		LIQUIDREMAINING
	}

	/**
	 * An enum for properties found on
	 * many different Physical objects
	 * that are derivative, or otherwise
	 * not available from the Modifiable
	 * interface.
	 *
	 * @author Bo Zimmerman
	 */
	public enum GenPhysBonusFakeStats
	{
		DISPOSITIONSTR,
		SENSESSTR,
		CURRENCY,
		CURRENCY_NAME,
		DENOMINATION_NAME,
		OWNER
	}

	/**
	 * Some object types (items and exits) still
	 * use a compressed numeric form for some of their
	 * properties.  This method will return the value
	 * of that special flag.
	 *
	 * @see GenericBuilder#setSpecialEnvFlags(Environmental, int)
	 *
	 * @param E the object to get envflags for
	 * @return the value of the special flags
	 */
	public int getSpecialEnvFlags(Environmental E);

	/**
	 * Some object types (items and exits) still
	 * use a compressed numeric form for some of their
	 * properties.  This method will set the value
	 * of that special flag.
	 *
	 * @see GenericBuilder#getSpecialEnvFlags(Environmental)
	 *
	 * @param E the object to set envflags for
	 * @param f the value of the special flags
	 */
	public void setSpecialEnvFlags(Environmental E, int f);

	/**
	 * Called when a new area enters the game in order to add any
	 * Automatic/Default properties to the area that are specified
	 * in the INI file.
	 *
	 * @param newArea the area to add things to, if necessary
	 */
	public void addAutoPropsToAreaIfNecessary(Area newArea);

	/**
	 * Duplicates the contents of an official database room by
	 * reloading the room and its contents into a new room
	 * object.  You can optionally activate the room as a full
	 * map member.
	 *
	 * @param room the room with an id to duplicate
	 * @param makeLive true to make the room live, or false to leave passive and unticking
	 * @return the new room object
	 */
	public Room makeNewRoomContent(Room room, boolean makeLive);

	/**
	 * Given an ability, including a generic one, this will construct
	 * an xml document representing the ability.
	 *
	 * @see GenericBuilder#unpackAbilitiesFromXml(String, List)
	 *
	 * @param A the ability to get xml for
	 * @return the xml
	 */
	public String getGenAbilityXML(Ability A);

	/**
	 * Given an xml document containing one or more ABILITY tags, this will
	 * parse out the Ability objects, including generic types, and add them
	 * to the given list.
	 *
	 * @see GenericBuilder#getGenAbilityXML(Ability)
	 *
	 * @param xml the xml document with AbILITY tags
	 * @param ables the list to put ability objects into
	 * @return "" if all went well, and an error message otherwise
	 */
	public String unpackAbilitiesFromXml(final String xml, final List<Ability> ables);

	/**
	 * Given an xml document for a single item, mob, room,
	 * or exit, this will return the fully formed object
	 * represented by the document.
	 *
	 * @see GenericBuilder#getUnknownXML(Environmental)
	 *
	 * @param xml the xml document
	 * @return the item, mob, room, or exit object
	 */
	public Environmental unpackUnknownFromXML(final String xml);

	/**
	 * Given an item, mob, room, or exit, this will
	 * return the full XML document for that object.
	 *
	 * @see GenericBuilder#unpackUnknownFromXML(String)
	 *
	 * @param obj the object to get xml for
	 * @return the xml
	 */
	public String getUnknownXML(final Environmental obj);

	/**
	 * Given an xml document that starts with a MOB, ITEM, AROOM, or
	 * EXIT tag, this will return the appropriate CMOBjectType
	 *
	 * @see CMClass.CMObjectType
	 * @see GenericBuilder#getUnknownNameFromXML(String)
	 *
	 * @param xml the xml document to get the type of
	 * @return the object type
	 */
	public CMClass.CMObjectType getUnknownTypeFromXML(final String xml);

	/**
	 * Given an xml document with a mob, item, or room, this will
	 * return the value of the first NAME or RDISP tag found herein.
	 *
	 * @see GenericBuilder#getUnknownTypeFromXML(String)
	 *
	 * @param xml the xml document to get a name from
	 * @return the name
	 */
	public String getUnknownNameFromXML(final String xml);

	/**
	 * An xml helper function that merely parses the given xml document containing an AREA tag
	 * into its various content tags, and puts them into the given area list.  It will
	 * also parse out any custom objects and external files contained therein into the custom
	 * and externalFiles object given.
	 *
	 * @see GenericBuilder#fillAreasVectorFromXML(String, List, List, Map)
	 *
	 * @param buf the xml document containing AREAS tag
	 * @param area the list to put AREA tag contents into
	 * @param custom the required list to put any generic races or classes
	 * @param externalFiles the optional list to put a map of file paths to their contents
	 * @return "" if all went well, or an error message
	 */
	public String fillAreaAndCustomVectorFromXML(String buf,  List<XMLTag> area, List<CMObject> custom, Map<String,String> externalFiles);

	/**
	 * An xml helper function that merely parses the given xml document containing an AREAS tag
	 * into its various AREA tags, and puts the contents into the given areas list.  It will
	 * also parse out any custom objects and external files contained therein into the custom
	 * and externalFiles object given.
	 *
	 * @see GenericBuilder#fillAreaAndCustomVectorFromXML(String, List, List, Map)
	 *
	 * @param buf the xml document containing AREAS tag
	 * @param areas the list to put AREA tag contents into
	 * @param custom the required list to put any generic races or classes
	 * @param externalFiles the optional list to put a map of file paths to their contents
	 * @return "" if all went well, or an error message
	 */
	public String fillAreasVectorFromXML(String buf,  List<List<XMLTag>> areas, List<CMObject> custom, Map<String,String> externalFiles);

	/**
	 * Given a string xml document with CUSTOM tag containing generic races, classes, and
	 * external files and scripts, this method will recreate the appropriate objects to
	 * populate into the custom list, and separate file paths from their string contents
	 * into the externalFiles map.
	 *
	 * @param xml the xml document containing the CUSTOM tag
	 * @param custom required list to put genraces and classes into
	 * @param externalFiles null, or optional map to put filepaths, file contents into
	 * @return "" or an error message
	 */
	public String fillCustomVectorFromXML(String xml, List<CMObject> custom, Map<String,String> externalFiles);

	/**
	 * Given a string xml document, and an environmental to unpack the xml properties
	 * into, this will, well, do that.  Send fromTop to handle ordinary properties
	 * for standard items.
	 *
	 * @see GenericBuilder#getEnvironmentalMiscTextXML(Environmental, boolean)
	 * @see GenericBuilder#unpackEnvironmentalMiscTextXML(Environmental, List, boolean)
	 *
	 * @param E the environmental object to populate
	 * @param buf the xml doc
	 * @param fromTop true to unpack xml for standard objects
	 */
	public void unpackEnvironmentalMiscTextXML(Environmental E, String buf, boolean fromTop);

	/**
	 * Given a parsed xml document, and an environmental to unpack the xml properties
	 * into, this will, well, do that.  Send fromTop to handle ordinary properties
	 * for standard items.
	 *
	 * @see GenericBuilder#getEnvironmentalMiscTextXML(Environmental, boolean)
	 * @see GenericBuilder#unpackEnvironmentalMiscTextXML(Environmental, String, boolean)
	 *
	 * @param E the environmental object to populate
	 * @param V the parsed xml doc
	 * @param fromTop true to unpack xml for standard objects
	 */
	public void unpackEnvironmentalMiscTextXML(Environmental E, List<XMLTag> V, boolean fromTop);

	/**
	 * Gives an environmental object of a more basic sort, such as a mob or item,
	 * this will return the XML representation of that object.  This will work
	 * for standard or generic objects, and you can specify with the fromTop
	 * variable whether you want full xml for standard objects.
	 *
	 * @see GenericBuilder#unpackEnvironmentalMiscTextXML(Environmental, List, boolean)
	 * @see GenericBuilder#unpackEnvironmentalMiscTextXML(Environmental, String, boolean)
	 *
	 * @param E the object to get xml for
	 * @param fromTop true to get all xml for a standard object
	 * @return the xml document
	 */
	public String getEnvironmentalMiscTextXML(Environmental E, boolean fromTop);

	/**
	 * Given a database room, this function will grab its real contents from
	 * the database, and construct an xml document consisting of the unique
	 * items in the room.  It will also filter based on type, or populate
	 * a set with the paths of any externally used files, as
	 * well as use a map to keep track of duplicates.
	 *
	 * @param room the room to get items from
	 * @param found a map for keeping track of dups
	 * @param files a set for any file paths used
	 * @param type the object type filter
	 * @return the xml document of items
	 */
	public String getRoomItems(Room room, Map<String,List<Item>> found, Set<String> files, CMObjectType type);

	/**
	 * Given a String xml document containing a ITEMs tag, this will build a
	 * set of item objects and add them to the given list.  An optional session
	 * can handle any progress reporting.  Returns an error, or "".
	 *
	 * @see GenericBuilder#getUniqueItemsXML(List, Map, Set, CMObjectType)
	 * @see GenericBuilder#getItemXML(Item)
	 * @see GenericBuilder#addItemsFromXML(List, List, Session)
	 * @see GenericBuilder#unpackItemFromXML(String)
	 *
	 * @param xmlBuffer the xml document with the ITEMs tag
	 * @param addHere the list to add the item objects to
	 * @param S an optional session object for progress
	 * @return "", or an error message
	 */
	public String addItemsFromXML(String xmlBuffer, List<Item> addHere, Session S);

	/**
	 * Given a pre-parsed xml document containing a ITEMs tag, this will build a
	 * set of item objects and add them to the given list.  An optional session
	 * can handle any progress reporting.  Returns an error, or "".
	 *
	 * @see GenericBuilder#getUniqueItemsXML(List, Map, Set, CMObjectType)
	 * @see GenericBuilder#getItemXML(Item)
	 * @see GenericBuilder#addItemsFromXML(String, List, Session)
	 * @see GenericBuilder#unpackItemFromXML(String)
	 *
	 * @param xml the xml document with the ITEMs tag
	 * @param addHere the list to add the item objects to
	 * @param S an optional session object for progress
	 * @return "", or an error message
	 */
	public String addItemsFromXML(List<XMLTag> xml, List<Item> addHere, Session S);

	/**
	 * Given an xml document with a ITEM tag in it, this will extract and create the
	 * associated item object.  Any generic races or scripts must already exist.
	 *
	 * @see GenericBuilder#getUniqueItemsXML(List, Map, Set, CMObjectType)
	 * @see GenericBuilder#getItemXML(Item)
	 * @see GenericBuilder#addItemsFromXML(String, List, Session)
	 * @see GenericBuilder#addItemsFromXML(List, List, Session)
	 *
	 * @param xmlBuffer the xml document with the item tag
	 * @return the item object, or null
	 */
	public Item unpackItemFromXML(String xmlBuffer);

	/**
	 * Given a list of item objects, this will return an xml document of the set of
	 * ITEMs, but only returning any unique item from the list, relegating dups
	 * to the give map.  It will also populate sets with any script paths or
	 * generic races and classes found.
	 *
	 * @see GenericBuilder#getItemXML(Item)
	 * @see GenericBuilder#addItemsFromXML(String, List, Session)
	 * @see GenericBuilder#addItemsFromXML(List, List, Session)
	 * @see GenericBuilder#unpackItemFromXML(String)
	 *
	 * @param items the list of items to create xml for
	 * @param found required map to keep track of dups, for some reason
	 * @param files any script paths found
	 * @param type object type of item to grab
	 * @return the xml document of the unique items from the list
	 */
	public String getUniqueItemsXML(List<Item> items, Map<String,List<Item>> found, Set<String> files, CMObjectType type);

	/**
	 * Returns an xml document representing the given item, wrapped in a neat
	 * ITEM tag.  No custom objects or scripts are captured.
	 *
	 * @see GenericBuilder#addItemsFromXML(String, List, Session)
	 * @see GenericBuilder#addItemsFromXML(List, List, Session)
	 * @see GenericBuilder#getUniqueItemsXML(List, Map, Set, CMObjectType)
	 * @see GenericBuilder#unpackItemFromXML(String)
	 *
	 * @param item the item to get the xml of
	 * @return the full xml of the item
	 */
	public String getItemXML(Item item);

	/**
	 * Confirms the length of a misc text XML document for a
	 * generic mob, and if it is stored in the DB, retreives it
	 * before returning the final misc text xml document.
	 *
	 * @param mob the mob whose misc text this is
	 * @param newText the misc text
	 * @return the final misc text
	 */
	public String getGenMOBTextUnpacked(MOB mob, String newText);

	/**
	 * Given a mob and its generic misc text, this method
	 * will reset the mob with the given misc text values,
	 * and reset its hit points, mana, etc.  This is often
	 * called during generic mob rebirth.
	 *
	 * @param mob the mob to repopulate
	 * @param newText the misctext for the mob
	 */
	public void resetGenMOB(MOB mob, String newText);

	/**
	 * Given a string xml document containing a MOBS tag, this will build a
	 * set of mob objects and add them to the given list.  An optional session
	 * can handle any progress reporting.  Returns an error, or "".
	 *
	 * @see GenericBuilder#getMobXML(MOB)
	 * @see GenericBuilder#addMOBsFromXML(List, List, Session)
	 * @see GenericBuilder#getUniqueMobsXML(List, Set, Set, Map)
	 * @see GenericBuilder#unpackMobFromXML(String)
	 *
	 * @param xmlBuffer the xml document with the MOBS tag
	 * @param addHere the list to add the mob objects to
	 * @param S an optional session object for progress
	 * @return "", or an error message
	 */
	public String addMOBsFromXML(String xmlBuffer, List<MOB> addHere, Session S);

	/**
	 * Given a database room, this function will grab its real contents from
	 * the database, and construct an xml document consisting of the unique
	 * mobs in the room.  It will also populate a set with custom races and
	 * classes, and a set with the paths of any externally used files, as
	 * well as use a map to keep track of duplicates.
	 *
	 * @param room the room to get mobs from
	 * @param custom a set for any generic classes or races
	 * @param files a set for any file paths used
	 * @param found a map for keeping track of dups
	 * @return the xml document of mobs
	 */
	public String getRoomMobs(Room room, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);

	/**
	 * Given a pre-parsed xml document containing a MOBS tag, this will build a
	 * set of mob objects and add them to the given list.  An optional session
	 * can handle any progress reporting.  Returns an error, or "".
	 *
	 * @see GenericBuilder#getMobXML(MOB)
	 * @see GenericBuilder#addMOBsFromXML(String, List, Session)
	 * @see GenericBuilder#getUniqueMobsXML(List, Set, Set, Map)
	 * @see GenericBuilder#unpackMobFromXML(String)
	 *
	 * @param xml the xml document with the MOBS tag
	 * @param addHere the list to add the mob objects to
	 * @param S an optional session object for progress
	 * @return "", or an error message
	 */
	public String addMOBsFromXML(List<XMLTag> xml, List<MOB> addHere, Session S);

	/**
	 * Returns an xml document representing the given mob, wrapped in a neat
	 * MOB tag.  No custom objects or scripts are captured.
	 *
	 * @see GenericBuilder#addMOBsFromXML(String, List, Session)
	 * @see GenericBuilder#addMOBsFromXML(List, List, Session)
	 * @see GenericBuilder#getUniqueMobsXML(List, Set, Set, Map)
	 * @see GenericBuilder#unpackMobFromXML(String)
	 *
	 * @param mob the mob to get the xml of
	 * @return the full xml of the mob
	 */
	public String getMobXML(MOB mob);

	/**
	 * Given a list of mob objects, this will return an xml document of the set of
	 * MOBs, but only returning any unique mobs from the list, relegating dups
	 * to the give map.  It will also populate sets with any script paths or
	 * generic races and classes found.
	 *
	 * @see GenericBuilder#getMobXML(MOB)
	 * @see GenericBuilder#unpackMobFromXML(String)
	 * @see GenericBuilder#addMOBsFromXML(List, List, Session)
	 * @see GenericBuilder#addMOBsFromXML(String, List, Session)
	 *
	 * @param mobs the list of mobs to create xml for
	 * @param custom any generic races/classes found
	 * @param files any script paths found
	 * @param found required map to keep track of dups, for some reason
	 * @return the xml document of the unique mobs from the list
	 */
	public String getUniqueMobsXML(List<MOB> mobs, Set<CMObject> custom, Set<String> files, Map<String,List<MOB>> found);

	/**
	 * Given an xml document with a MOB tag in it, this will extract and create the
	 * associated mob object.  Any generic races or scripts must already exist.
	 *
	 * @see GenericBuilder#getUniqueMobsXML(List, Set, Set, Map)
	 * @see GenericBuilder#getMobXML(MOB)
	 * @see GenericBuilder#addMOBsFromXML(List, List, Session)
	 * @see GenericBuilder#addMOBsFromXML(String, List, Session)
	 *
	 * @param xmlBuffer the xml document with the mob tag
	 * @return the mob object, or null
	 */
	public MOB unpackMobFromXML(String xmlBuffer);

	/**
	 * Gives a parsed XML document containing the contents of room xml, with RCLAS tag, this
	 * will extract the room, optionally including mobs/items inside it, and add it to
	 * whatever world map area it belongs in, or the forced area if given.
	 *
	 * @see GenericBuilder#getRoomXML(Room, Set, Set, boolean)
	 *
	 * @param forceArea null to use a map area, or an area object to add the room to
	 * @param xml the pre-parsed xml document containing room tags
	 * @param andContent true to also build mobs/items, or false to ignore them
	 * @param andSave true to save the room to the db, or false to not.
	 * @return an error message, or ""
	 */
	public String unpackRoomFromXML(final Area forceArea, final List<XMLTag> xml, final boolean andContent, final boolean andSave);

	/**
	 * Given an xml document containing an AROOM tag, this method will extract the room,
	 * and optionally the mobs/items inside it, and add it to whatever world map area
	 * it is supposed to belong to.
	 *
	 * @see GenericBuilder#getRoomXML(Room, Set, Set, boolean)
	 *
	 * @param buf the xml document containing AROOM tag
	 * @param andContent true to also extract mob/items or false otherwise
	 * @return an error message, or "" if all is well
	 */
	public String unpackRoomFromXML(String buf, boolean andContent);

	/**
	 * Generates an xml document of the given room, and optionally all of its contents.  It will
	 * also split out any files, or local generic races and classes for optional inclusion.
	 *
	 * @see GenericBuilder#unpackRoomFromXML(String, boolean)
	 * @see GenericBuilder#unpackRoomFromXML(Area, List, boolean, boolean)
	 *
	 * @param room the room to generate an xml document for
	 * @param custom optional set to put generic races/classes into
	 * @param files optional set to put the paths to scripts used in the room
	 * @param andContent true to include mobs/items, false otherwise
	 * @return the xml document for the room
	 */
	public String getRoomXML(Room room,  Set<CMObject> custom, Set<String> files, boolean andContent);

	/**
	 * Unpack an XML document containing an AREA tag into an actual area object,
	 * including any rooms, mobs, items, etc.  Saves nothing to the db, or to
	 * the world map.  Just returns the populated object.
	 *
	 * @see GenericBuilder#getAreaObjectXML(Area, Session, Set, Set, boolean)
	 * @see GenericBuilder#getAreaXML(Area, Session, Set, Set, boolean)
	 *
	 * @param xml the xml document containing an AREA tag
	 * @return the populated area object
	 * @throws CMException any unpacking errors, bad xml, etc
	 */
	public Area unpackAreaObjectFromXML(String xml) throws CMException;

	/**
	 * Given a pre-parsed XML document containing an ACLAS tag, this method will unpack the given area
	 * into an area object, optionally complete with rooms, items, mobs, etc.  An optional session
	 * can be sent which will receive progress messages.  An optional area class id can be sent to
	 * override whatever is in the xml document.  The area can also optionally be saved to the
	 * database and added to the game map.
	 *
	 * @see GenericBuilder#getAreaObjectXML(Area, Session, Set, Set, boolean)
	 * @see GenericBuilder#getAreaXML(Area, Session, Set, Set, boolean)
	 *
	 * @param aV parsed XML document of the inside of the AREA tag.
	 * @param S an optional session for monitoring, or null
	 * @param overrideAreaType null, or an area class id
	 * @param andRooms true to also unpack rooms, mobs, and items, false otherwise
	 * @param savable true to save to the db and add to the map
	 * @return any errors, or "" for no errors
	 */
	public String unpackAreaFromXML(List<XMLTag> aV, Session S, String overrideAreaType, boolean andRooms, boolean savable);

	/**
	 * Generates an xml document of the given area, and optionally all of its rooms.  It will
	 * also split out any files, or local generic races and classes for optional inclusion.
	 * The area must be in the database, as the rooms will be refreshed from there.
	 *
	 * @see GenericBuilder#unpackAreaFromXML(List, Session, String, boolean, boolean)
	 * @see GenericBuilder#unpackAreaObjectFromXML(String)
	 *
	 * @param area the area to generate an xml document for
	 * @param S optional session for progress messages, or null
	 * @param custom optional set to put generic races/classes into
	 * @param files optional set to put the paths to scripts used in the room
	 * @param andRooms true to include rooms/mobs/items, false otherwise
	 * @return the xml document for the area
	 */
	public String getAreaXML(Area area,  Session S, Set<CMObject> custom, Set<String> files, boolean andRooms);

	/**
	 * Generates an xml document of the given area, and optionally all of its rooms.  It will
	 * also split out any files, or local generic races and classes for optional inclusion.
	 * The area need not be in the database, as the room xml will be generated from memory.
	 *
	 * @see GenericBuilder#unpackAreaFromXML(List, Session, String, boolean, boolean)
	 * @see GenericBuilder#unpackAreaObjectFromXML(String)
	 *
	 * @param area the area to generate an xml document for
	 * @param S optional session for progress messages, or null
	 * @param custom optional set to put generic races/classes into
	 * @param files optional set to put the paths to scripts used in the room
	 * @param andRooms true to include rooms/mobs/items, false otherwise
	 * @return the xml document for the area
	 */
	public String getAreaObjectXML(Area area, Session S, Set<CMObject> custom, Set<String> files, boolean andRooms);

	/**
	 * Create a standard GenAmmunition Item, representing a bundle of ammo,
	 * of the give type and number.
	 *
	 * @param ammunitionType the type of ammo "arrows" "bullets", etc.
	 * @param number the number of ammo in the bundle
	 * @return the new GenAmmunition object
	 */
	public Ammunition makeAmmunition(String ammunitionType, int number);

	/**
	 * Given parsed xml tags, this method will extract the basic shop
	 * settings into the given ShopKeeper object, as well as populate
	 * the shop inventory itself from the given xml.
	 *
	 * @param shopKeep the shopkeeper to populate
	 * @param buf the parsed xml doc with STORE, etc tags
	 */
	public void populateShops(ShopKeeper shopKeep, List<XMLTag> buf);

	/**
	 * Give a player/character mob, this will return an xml document of the mob object,
	 * as well as any items, etc contained therein.  Dependency custom objects and
	 * script file paths can optionally be captured during this process.
	 *
	 * @see GenericBuilder#addPlayersAndAccountsFromXML(String, List, List, Session)
	 *
	 * @param mob the player/character mob to get the xml of
	 * @param custom optional set in which to capture custom classes, races
	 * @param files optional set in which to capture script paths
	 * @return the xml document of the character
	 */
	public String getPlayerXML(MOB mob, Set<CMObject> custom, Set<String> files);

	/**
	 * Given a player account, this will return an XML document of the account objects, as
	 * well as all characters contained in the account.  Dependency custom objects and
	 * script file paths can optionally be captured during this process.
	 *
	 * @see GenericBuilder#addPlayersAndAccountsFromXML(String, List, List, Session)
	 *
	 * @param account the account to get the xml of
	 * @param custom optional set in which to capture custom classes, races
	 * @param files optional set in which to capture script paths
	 * @return the xml document of the account
	 */
	public String getAccountXML(PlayerAccount account, Set<CMObject> custom, Set<String> files);

	/**
	 * Creates player account objects and player character MOB objects from the
	 * given XML document.  If a Session is sent, then any confirmations or
	 * special messages are done through that session.  This does not actually
	 * save the accounts or characters to the database.
	 *
	 * @see GenericBuilder#getPlayerXML(MOB, Set, Set)
	 * @see GenericBuilder#getAccountXML(PlayerAccount, Set, Set)
	 *
	 * @param xmlBuffer the XML document
	 * @param addAccounts the list to put accounts into
	 * @param addMobs the list to put character mobsinto
	 * @param S an optional session object for interaction
	 * @return any errors that occurred, or "" for all-OK
	 */
	public String addPlayersAndAccountsFromXML(String xmlBuffer, List<PlayerAccount> addAccounts, List<MOB> addMobs, Session S);

	/**
	 * Attempts to fill the given set full of script file paths from the object
	 * given by recursively inspecting it for embedded scripts, digging into
	 * mob and store inventory and so forth.
	 *
	 * @see GenericBuilder#fillFileMap(Environmental, Map)
	 *
	 * @param E the top level object to dig for script paths
	 * @param H the set of script paths found
	 */
	public void fillFileSet(Environmental E, Set<String> H);

	/**
	 * Attempts to fill the given map of script file paths to set of object that use the
	 * script.   Does so recursively, going into mob and store inventory and so forth.
	 *
	 * @see GenericBuilder#fillFileSet(Environmental, Set)
	 *
	 * @param E the top level object to go digging for script paths
	 * @param H the map to fill.
	 */
	public void fillFileMap(final Environmental E, final Map<String,Set<Environmental>> H);

	/**
	 * Given the PhyStats object, returns the encoded string
	 * representing the values in the object.  The encoding is
	 * the values of the object, in ordinal order, separated
	 * by | chars.
	 *
	 * @see GenericBuilder#setPhyStats(PhyStats, String)
	 *
	 * @param E the PhyStats Object
	 * @return the encoded values object
	 */
	public String getPhyStatsStr(PhyStats E);

	/**
	 * Given the CharState object, returns the encoded string
	 * representing the values in the object.  The encoding is
	 * the values of the object, in ordinal order, separated
	 * by | chars.
	 *
	 * @see GenericBuilder#setCharState(CharState, String)
	 *
	 * @param E the CharState Object
	 * @return the encoded values object
	 */
	public String getCharStateStr(CharState E);

	/**
	 * Given the CharStats object, returns the encoded string
	 * representing the values in the object.  The encoding is
	 * the values of the object, in ordinal order, separated
	 * by | chars.
	 *
	 * @see GenericBuilder#setCharStats(CharStats, String)
	 *
	 * @param E the CharStats Object
	 * @return the encoded values object
	 */
	public String getCharStatsStr(CharStats E);

	/**
	 * From the given Environmental, returns xml of certain object
	 *  settings that come from the objects most basic interfaces,
	 * such as Physical, Economic, PhysicalAgent, extra bonus stats,
	 * and scripts.
	 *
	 * @see GenericBuilder#unpackExtraEnvironmentalXML(Environmental, List)
	 * @see GenericBuilder#getGenScriptsXML(PhysicalAgent, boolean)
	 *
	 * @param E the object to give settings to
	 * @return the xml tags containing settings
	 */
	public String getExtraEnvironmentalXML(Environmental E);

	/**
	 * If the given PhysicalAgent contains any attached Scripts, this
	 * will bundle them up into an XML document and return them.  If
	 * includeVars is set to true, it will also bundle all currently
	 * set script variables.
	 *
	 * @see GenericBuilder#unpackGenScriptsXML(PhysicalAgent, List, boolean)
	 *
	 * @param E the object that might be scripted
	 * @param includeVars true to return any vars also
	 * @return the xml document, or ""
	 */
	public String getGenScriptsXML(PhysicalAgent E, boolean includeVars);

	/**
	 * Sets the values in the given CharStats object from the
	 * encoded string.  The encoding is the values of the
	 * object in ordinal order separated by | chars
	 *
	 * @see GenericBuilder#getCharStatsStr(CharStats)
	 *
	 * @param E the CharStats object to alter
	 * @param props the encoded values
	 */
	public void setCharStats(CharStats E, String props);

	/**
	 * Sets the values in the given CharState object from the
	 * encoded string.  The encoding is the values of the
	 * object in ordinal order separated by | chars
	 *
	 * @see GenericBuilder#getCharStateStr(CharState)
	 *
	 * @param E the CharState object to alter
	 * @param props the encoded values
	 */
	public void setCharState(CharState E, String props);

	/**
	 * Sets the values in the given PhyStats object from the
	 * encoded string.  The encoding is the values of the
	 * object in ordinal order separated by | chars
	 *
	 * @see GenericBuilder#getPhyStatsStr(PhyStats)
	 *
	 * @param E the PhyStats object to alter
	 * @param props the encoded values
	 */
	public void setPhyStats(PhyStats E, String props);

	/**
	 * Weird function.  Given an optional xml doc in miscText, it will
	 * return the value of the first &lt;NAME&gt; tag found.  If not,
	 * then, given a standard mob or item class id, this will return
	 * the name of that standard object.
	 *
	 * @param classID the optional standard class id object
	 * @param miscText the optional xml document with NAME tag
	 * @return a name derived from one of the fields, or ""
	 */
	public String getQuickName(final String classID, final String miscText);

	/**
	 * From the given XML tags, set certain given object settings
	 * that come from the objects most basic interfaces, such as
	 * Physical, Economic, PhysicalAgent, extra bonus stats
	 *
	 * @see GenericBuilder#getExtraEnvironmentalXML(Environmental)
	 *
	 * @param E the object to give settings to
	 * @param buf the xml tags containing settings
	 */
	public void unpackExtraEnvironmentalXML(Environmental E, List<XMLTag> buf);

	/**
	 * From the given XML tags, set any object level Scripts attached to the
	 * given PhysicalAgent object.  If restoreVars is true, then any variable
	 * values stored in the XML document are also restored.
	 *
	 * @see GenericBuilder#getGenScriptsXML(PhysicalAgent, boolean)
	 *
	 * @param E the object to give scripts to
	 * @param buf the xml tags containing scripts
	 * @param restoreVars true to look for saved variables and restore them
	 */
	public void unpackGenScriptsXML(PhysicalAgent E, List<XMLTag> buf, boolean restoreVars);

	/**
	 * Sets the value of the given stat on the given object,
	 * even if the object is not generic. This includes not only the Generic codes,
	 * but also any fakeitem or fakemob stat codes as well.  All Physical
	 * type objects are supported.  This method supports deltas to numeric
	 * values as well, by sending a value with + or - as a prefix.
	 *
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
	 *
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
	 *
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
	 *
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
	 *
	 * @param P the object to get stat codes for
	 * @return the list of stat codes.
	 */
	public List<String> getAllGenStats(Physical P);

	/**
	 * Removes any qualifying prefixes from stat names, to
	 * reveal the underlying stat code.  Prefixes include
	 * things like CURRENT_, CURRENT ,BASE_, BASE ,MAX_, and MAX
	 *
	 * @param stat the possibly qualified stat code
	 * @return the underlying state code
	 */
	public String getFinalStatName(String stat);

	/**
	 * Returns the ordinal of the given code in the basic
	 * genitem stat codes.  It does not rely on the
	 * Modifiable interface.  It also does not support any
	 * of the fake GenItemBonusFakeStats stats.
	 *
	 * @see GenericBuilder#getGenItemStat(Item, String)
	 * @see GenericBuilder#setGenItemStat(Item, String, String)
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
	 *
	 * @see GenericBuilder#getGenItemCodeNum(String)
	 * @see GenericBuilder#setGenItemStat(Item, String, String)
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
	 *
	 * @see GenericBuilder#getGenItemStat(Item, String)
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
	 *
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
	 *
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
	 *
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
	 * mob or list into a complete xml doc for storage in the db.
	 * @see GenericBuilder#unpackFactionFromXML(MOB, List)
	 *
	 * @param mob the mob to grab faction associations from
	 * @param lst optional list to grab faction info from
	 * @return the xml doc of all factions on the mob or list
	 */
	public String getFactionXML(MOB mob, List<Pair<String, Integer>> lst);

	/**
	 * Sets the faction values and associations on the given
	 * mob from a pre-parsed xml doc.  The list of tags
	 * must include one called "FACTIONS".
	 * @return TODO
	 *
	 * @see GenericBuilder#getFactionXML(MOB, List)
	 *
	 * @param mob the mob to set faction associations on
	 * @param xml the list of pre-parsed xml tags
	 */
	public List<Pair<String, Integer>> unpackFactionFromXML(MOB mob, List<XMLTag> xml);

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
