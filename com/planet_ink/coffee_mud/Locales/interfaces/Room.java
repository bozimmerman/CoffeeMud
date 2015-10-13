package com.planet_ink.coffee_mud.Locales.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2015 Bo Zimmerman

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
 * A Room, also known as a Locale, is the basic building blocks of a world
 * map.  They represent abstract places where players, mobs, or items can
 * be.  Rooms have titles, dynamic or static descriptions, automatic "skys", 
 * domain codes to guide the engine on its physical makeup, gatherable resources,
 * exits to other Rooms, and can each have their own behaviors and properties.
 * 
 * @author Bo Zimmerman
 *
 */
public interface Room extends PhysicalAgent, ItemPossessor, Places
{
	/**
	 * The room ID is the "address" of the room on the world map.  It
	 * is generally of the format [AREA NAME]#[ID NUMBER] for primary
	 * rooms.  All other rooms, such as GridLocale children, or temporary
	 * or contingent rooms, have an empty string "" room ID.
	 * GridLocale children (@see {@link GridLocale}) in particular may
	 * have an address, if the GridLocale is, itself, a primary room, but
	 * still have an empty roomID.  This is not related to the ID()
	 * method.
	 * @see Room#setRoomID(String)
	 * @return room ID is the "address" of the primary room on the world map
	 */
	public String roomID();
	
	/**
	 * Changes the room ID of this room.  This is not related to the ID()
	 * method.
	 * The room ID is the "address" of the room on the world map.  It
	 * is generally of the format [AREA NAME]#[ID NUMBER] for primary
	 * rooms.  All other rooms, such as GridLocale children, or temporary
	 * or contingent rooms, have an empty string "" room ID.
	 * GridLocale children (@see {@link GridLocale}) in particular may
	 * have an address, if the GridLocale is, itself, a primary room, but
	 * still have an empty roomID.
	 * @see Room#roomID()
	 * @param newRoomID the new room ID of this room
	 */
	public void setRoomID(String newRoomID);

	/** Domain type mask denoting whether the room is indoor (has a roof) @see {@link Room#domainType()} */
	public final static int INDOORS=128;

	/** Domain type mask denoting whether the room is outdoors, in the city @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_CITY=0;
	/** Domain type mask denoting whether the room is outdoors, in the woods @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_WOODS=1;
	/** Domain type mask denoting whether the room is outdoors, in the rocky wastes @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_ROCKS=2;
	/** Domain type mask denoting whether the room is outdoors, in the grassy plains @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_PLAINS=3;
	/** Domain type mask denoting whether the room is outdoors, but underwater (think ocean) @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_UNDERWATER=4;
	/** Domain type mask denoting whether the room is outdoors, up in the sky @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_AIR=5;
	/** Domain type mask denoting whether the room is outdoors, on the surface of the water @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_WATERSURFACE=6;
	/** Domain type mask denoting whether the room is outdoors, in the jungle @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_JUNGLE=7;
	/** Domain type mask denoting whether the room is outdoors, in the swamp @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_SWAMP=8;
	/** Domain type mask denoting whether the room is outdoors, in the desert @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_DESERT=9;
	/** Domain type mask denoting whether the room is outdoors, in the hills @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_HILLS=10;
	/** Domain type mask denoting whether the room is outdoors, in the mountains @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_MOUNTAINS=11;
	/** Domain type mask denoting whether the room is outdoors, at a spaceport @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_SPACEPORT=12;
	/** Domain type mask denoting whether the room is outdoors, at a seaport @see {@link Room#domainType()} */
	public final static int DOMAIN_OUTDOORS_SEAPORT=13;
	
	/** Domain description array indexed by the DOMAIN_OUTDOOR_* constants. @see {@link Room#DOMAIN_OUTDOORS_CITY} */
	public final static String[] DOMAIN_OUTDOOR_DESCS=
	{
		"CITY",
		"WOODS",
		"ROCKY",
		"PLAINS",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"JUNGLE",
		"SWAMP",
		"DESERT",
		"HILLS",
		"MOUNTAINS",
		"SPACEPORT",
		"SEAPORT"
	};

	/** Domain type mask denoting whether the room is indoors, and made of worked stone @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_STONE=INDOORS+0;
	/** Domain type mask denoting whether the room is indoors, and made of wood @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_WOOD=INDOORS+1;
	/** Domain type mask denoting whether the room is indoors, and made of unworked/natural stone @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_CAVE=INDOORS+2;
	/** Domain type mask denoting whether the room is indoors, and made of maaaagic @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_MAGIC=INDOORS+3;
	/** Domain type mask denoting whether the room is indoors, and underwater @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_UNDERWATER=INDOORS+4;
	/** Domain type mask denoting whether the room is indoors, but in the air @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_AIR=INDOORS+5;
	/** Domain type mask denoting whether the room is indoors, with a watery surface @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_WATERSURFACE=INDOORS+6;
	/** Domain type mask denoting whether the room is indoors, and made of metal @see {@link Room#domainType()} */
	public final static int DOMAIN_INDOORS_METAL=INDOORS+7;
	
	/** Domain description array indexed by the (DOMAIN_INDOORS_* - Room.INDOORS) constants. @see {@link Room#DOMAIN_INDOORS_STONE} */
	public final static String[] DOMAIN_INDOORS_DESCS=
	{
		"STONE",
		"WOODEN",
		"CAVE",
		"MAGIC",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"METAL"
	};

	/**
	 * Returns the domain-code for this room, which tells you something about its physical makeup,
	 * such as whether it is outdoor, if it's in the woods or surrounded by stone, that sort of thing.
	 * @see Room#INDOORS
	 * @see Room#DOMAIN_INDOORS_CAVE
	 * @see Room#DOMAIN_INDOORS_DESCS
	 * @see Room#DOMAIN_OUTDOORS_CITY
	 * @see Room#DOMAIN_OUTDOOR_DESCS
	 * @return the domain-code for this room
	 */
	public int domainType();
	
	/**
	 * Returns the full resource code for the current gatherable resource in this room.
	 * The value of this method may change from time to time.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.CODES
	 * @see Room#setResource(int)
	 * @see Room#resourceChoices()
	 * @return the full resource code for the current gatherable resource in this room
	 */
	public int myResource();
	
	/**
	 * Sets the full resource code for the current gatherable resource in this room.
	 * The value set by this method may be changed automatically later on.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.CODES
	 * @see Room#myResource()
	 * @see Room#resourceChoices()
	 * @param resourceCode the full resource code for the current gatherable resource in this room
	 */
	public void setResource(int resourceCode);
	
	/**
	 * Returns a list of all resource codes for potentially gatherable resources in this room.
	 * This list is alterable for a given Room/Locale java class.  Changes to any instance will
	 * affect the entire class.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.CODES
	 * @see Room#myResource()
	 * @see Room#setResource(int)
	 * @return the list of all possible resource codes for the current gatherable resources in this room
	 */
	public List<Integer> resourceChoices();
	
	/**
	 * Sets or clears whether any mobs in this room may, in fact, leave it.  This applies
	 * to player and npc mobs equally.
	 * @param onoff true to allow mobility, false to disallow mobility
	 */
	public void toggleMobility(boolean onoff);
	
	/**
	 * Gets whether any mobs in this room may, in fact, leave it.  This applies
	 * to player and npc mobs equally.
	 */
	public boolean getMobility();

	/**
	 * Room titles and descriptions can be coded with xml/html-like tags that denote
	 * different parts that are shown or hidden depending on the weather, the season,
	 * the time of day, or even whether the player has been to the room before.
	 * This enumerator is used to define those tags and give the engine clues
	 * on how to quickly interpret them when building dynamic titles and descriptions.
	 * @see Room#setDisplayText(String)
	 * @see Room#setDescription(String)
	 * @author Bo Zimmerman
	 */
	public enum VariationCode
	{
		SUMMER('S',TimeClock.Season.SUMMER.ordinal()),
		SPRING('S',TimeClock.Season.SPRING.ordinal()),
		WINTER('S',TimeClock.Season.WINTER.ordinal()),
		FALL('S',TimeClock.Season.FALL.ordinal()),
		DAY('C',TimeClock.TimeOfDay.DAY.ordinal()),
		DAYTIME('C',TimeClock.TimeOfDay.DAY.ordinal()),
		NIGHT('C',TimeClock.TimeOfDay.NIGHT.ordinal()),
		NIGHTTIME('C',TimeClock.TimeOfDay.NIGHT.ordinal()),
		DAWN('C',TimeClock.TimeOfDay.DAWN.ordinal()),
		DUSK('C',TimeClock.TimeOfDay.DUSK.ordinal()),
		RAIN('W',Climate.WEATHER_RAIN),
		SLEET('W',Climate.WEATHER_SLEET),
		SNOW('W',Climate.WEATHER_SNOW),
		CLEAR('W',Climate.WEATHER_CLEAR),
		HEATWAVE('W',Climate.WEATHER_HEAT_WAVE),
		THUNDERSTORM('W',Climate.WEATHER_THUNDERSTORM),
		BLIZZARD('W',Climate.WEATHER_BLIZZARD),
		WINDY('W',Climate.WEATHER_WINDY),
		DROUGHT('W',Climate.WEATHER_DROUGHT),
		DUSTSTORM('W',Climate.WEATHER_DUSTSTORM),
		COLD('W',Climate.WEATHER_WINTER_COLD),
		HAIL('W',Climate.WEATHER_HAIL),
		CLOUDY('W',Climate.WEATHER_CLOUDY),
		SWIMMING('M',PhyStats.IS_SWIMMING),
		FLYING('M',PhyStats.IS_FLYING),
		CRAWLING('M',PhyStats.IS_SITTING),
		SITTING('M',PhyStats.IS_SITTING),
		FALLING('M',PhyStats.IS_FALLING),
		CLIMBING('M',PhyStats.IS_CLIMBING),
		INVISIBLE('M',PhyStats.IS_INVISIBLE),
		HIDDEN('M',PhyStats.IS_HIDDEN),
		VISITED('V',-1),
		ELSE('\n',-1),
		VARIES('\r',-1)
		;
		public final char c;
		public final int num;
		public final String openTag;
		public final String closeTag;

		private VariationCode(char c, int num)
		{
			this.c=c;
			this.num=num;
			openTag="<"+toString()+">";
			closeTag="</"+toString()+">";
		}
	}
	
	/**
	 * When a room is created, this method is called to inspect the item contents
	 * for any rejuvinating items and, if found, causes them to start being tracked.
	 * Rejuvinating items are items that once, removed from a room, restore themselves
	 * after the rejuv-time clears.
	 */
	public void startItemRejuv();
	
	/**
	 * Causes the room stat affects to be reevaluated, as well as the stat affects
	 * of every inhabitant, exit, and item in the room.
	 * @see Physical#recoverPhyStats()
	 * @see MOB#recoverCharStats()
	 */
	public void recoverRoomStats();

	/**
	 * If this room has attached temporary rooms, such as a sky in an outdoor room,
	 * or underwater rooms in the ocean, this method will empty all of those rooms
	 * of their content, and then destroy those rooms.
	 * @see Room#giveASky(int)
	 */
	public void clearSky();
	
	/**
	 * If this room requires attached temporary rooms, such as a sky in an outdoor
	 * room, or underwater rooms in the ocean, this method will create those rooms
	 * at the given left of depth.
	 * @see Room#clearSky()
	 * @param depth the diameter of the sky or underwater to create
	 */
	public void giveASky(int depth);
	
	/**
	 * Returns the Area to which this room belongs.  Guarenteed to be non-null.
	 * @see Room#setArea(Area)
	 * @see Area
	 * @return the Area to which this room belongs
	 */
	public Area getArea();
	
	/**
	 * Sets the Area to which this room should belong, which also notifies the Area
	 * object that this room is a new member, and removing itself from any previous
	 * Area it belonged to in the past.
	 * @see Area
	 * @see Room#getArea()
	 * @param newArea the new area that this room belongs ro
	 */
	public void setArea(Area newArea);
	
	/**
	 * If this room is a grid-child room, this method is called to notify this child
	 * as to the identity of its parent gridlocale room.
	 * @see Room#getGridParent()
	 * @see GridLocale
	 * @param room the GridLocale parent room to this room
	 */
	public void setGridParent(GridLocale room);
	
	/**
	 * If this room is a grid-child room, this method is called to return
	 * the identity of its parent gridlocale room.
	 * @see Room#setGridParent(GridLocale)
	 * @see GridLocale
	 * @return the GridLocale parent room to this room
	 */
	public GridLocale getGridParent();

	/**
	 * Returns array of the Raw room objects that this room connects to via
	 * exits.  This array should only be used by low-level engine calls that
	 * understand the implications of accessing rooms in this way.  This is 
	 * because a Room returned by this method may be temporary, or even just
	 * a stand-in for a real room that is instantiated later.
	 * Always call {@link Room#getRoomInDir(int)} if you want a proper Room
	 * object.
	 * These rooms are indexed by Direction code.
	 * @see Directions
	 * @see Room#prepareRoomInDir(Room, int)
	 * @see Room#getRawExit(int)
	 * @return an array of Raw room objects that this room connects to
	 */
	public Room[] rawDoors();
	
	/**
	 * Returns the room in the given direction from this room which, depending
	 * on the exit, may be traveled to.  Returns null if there is no such room.
	 * @param direction the Direction from this room to the next
	 * @see Directions
	 * @see Room#prepareRoomInDir(Room, int)
	 * @see Room#getRawExit(int)
	 * @return the room in the given direction, or null
	 */
	public Room getRoomInDir(int direction);
	
	/**
	 * This method is called by an adjoining room to resolve this room to its
	 * final object.  There's no reason to call it from the "outside".
	 * @param fromRoom the room being traveled from
	 * @param direction the direction from the fromRoom being travelled in
	 * @return will either return this, or a resolved Rom object.
	 */
	public Room prepareRoomInDir(Room fromRoom, int direction);
	
	/**
	 * Returns the raw unresolved exit found in this room in the given
	 * direction.  This method should only be called by internal engine
	 * systems, as it may return a temporary object.  Instead, you should
	 * always call {@link Room#getExitInDir(int)} if you want the final
	 * Exit object in the given direction.  This method returns null if 
	 * there is no exit in the given direction.
	 * @see Room#setRawExit(int, Exit)
	 * @see Room#getExitInDir(int)
	 * @see Room#getReverseExit(int)
	 * @see Room#getPairedExit(int)
	 * @see Directions
	 * @param direction the direction in this room to look for an exit in
	 * @return the raw Exit object in that direction, or null
	 */
	public Exit getRawExit(int direction);
	
	/**
	 * Sets the Exit object found in this room in the given direction.  
	 * @see Room#getRawExit(int)
	 * @see Room#getExitInDir(int)
	 * @see Room#getReverseExit(int)
	 * @see Room#getPairedExit(int)
	 * @see Directions
	 * @param direction the direction in this room to look for an exit in
	 * @param E the raw Exit object in that direction, or null
	 */
	public void setRawExit(int direction, Exit E);
	
	/**
	 * Returns the Exit opposite this one, in the Room in the given direction.
	 * For example, if the direction is east, this will return the west door
	 * in the room to the east.
	 * @see Room#getRawExit(int)
	 * @see Room#getExitInDir(int)
	 * @see Room#setRawExit(int, Exit)
	 * @see Room#getPairedExit(int)
	 * @see Directions
	 * @param direction the direction from this room to get the reverse exit for
	 * @return the reverse exit, or null if there is no room (or no reverse exit)
	 */
	public Exit getReverseExit(int direction);
	
	/**
	 * Returns the Exit opposite this one, in the Room in the given direction,
	 * but only if the two exits exist, and have the same Door status.
	 * For example, if the direction is east, this will return the west door
	 * in the room to the east, but only if this east exit also has a door.
	 * @see Room#getRawExit(int)
	 * @see Room#getExitInDir(int)
	 * @see Room#setRawExit(int, Exit)
	 * @see Room#getReverseExit(int)
	 * @see Directions
	 * @param direction the direction from this room to get the reverse exit for
	 * @return the reverse exit, or null if there is no room (or no reverse exit)
	 */
	public Exit getPairedExit(int direction);
	
	/**
	 * Returns the Exit in this room, in the given direction.
	 * @see Room#getRawExit(int)
	 * @see Room#getPairedExit(int)
	 * @see Room#setRawExit(int, Exit)
	 * @see Room#getReverseExit(int)
	 * @param direction the direction from this room to get the exit for
	 * @return the Exit in this room, in the given direction
	 */
	public Exit getExitInDir(int direction);

	/**
	 * Returns the index number which represents which mob's "turn" it is in combat.
	 * This only applies when turn-based combat systems are being used.
	 * The index is into the ordinal list of mobs in this room.
	 * @return  the index number which represents which mob's "turn" it is in combat
	 */
	public int getCombatTurnMobIndex();
	
	/**
	 * Sets the index number which represents which mob's "turn" it is in combat.
	 * This only applies when turn-based combat systems are being used.
	 * The index is into the ordinal list of mobs in this room.
	 * @param index  the index number which represents which mob's "turn" it is in combat
	 */
	public void setCombatTurnMobIndex(final int index);

	public int pointsPerMove(MOB mob);
	public int thirstPerRound(MOB mob);

	public void send(MOB source, CMMsg msg);
	public void sendOthers(MOB source, CMMsg msg);
	public void showHappens(int allCode, String allMessage);
	public void showHappens(int allCode, Environmental like, String allMessage);
	public boolean show(MOB source, Environmental target, int allCode, String allMessage);
	public boolean show(MOB source, Environmental target, Environmental tool, int allCode, String allMessage);
	public boolean show(MOB source, Environmental target, Environmental tool, int srcCode, int tarCode, int othCode, String allMessage);
	public boolean show(MOB source, Environmental target, Environmental tool, int srcCode, String srcMessage, int tarCode, String tarMessage, int othCode, String othMessage);
	public boolean show(MOB source, Environmental target, Environmental tool, int allCode, String srcMessage, String tarMessage, String othMessage);
	public boolean showOthers(MOB source, Environmental target, int allCode, String allMessage);
	public boolean showSource(MOB source, Environmental target, int allCode, String allMessage);
	public boolean showOthers(MOB source, Environmental target, Environmental tool, int allCode, String allMessage);
	public boolean showSource(MOB source, Environmental target, Environmental tool, int allCode, String allMessage);

	public boolean isHere(Environmental E);

	public MOB fetchInhabitant(String inhabitantID);
	public List<MOB> fetchInhabitants(String inhabitantID);
	public Enumeration<MOB> inhabitants();
	public void addInhabitant(MOB mob);
	public void delInhabitant(MOB mob);
	public void delAllInhabitants(boolean destroy);
	public int numInhabitants();
	public boolean isInhabitant(MOB mob);
	public MOB fetchRandomInhabitant();
	public MOB fetchInhabitant(int i);
	public int numPCInhabitants();
	public void bringMobHere(MOB mob, boolean andFollowers);
	/**
	 * Applies the given applier Java code to each mob in this room
	 * @param applier code to execute against each object
	 */
	public void eachInhabitant(final EachApplicable<MOB> applier);

	public String getContextName(Environmental E);

	public Exit fetchExit(String itemID);
	public PhysicalAgent fetchFromRoomFavorExits(String thingName);
	public PhysicalAgent fetchFromRoomFavorItems(Item goodLocation, String thingName);
	public PhysicalAgent fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter);
	public PhysicalAgent fetchFromRoomFavorMOBs(Item goodLocation, String thingName);
	public PhysicalAgent fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter);
	public PhysicalAgent fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter);
}
