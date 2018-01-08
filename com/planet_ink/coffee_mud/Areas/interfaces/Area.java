package com.planet_ink.coffee_mud.Areas.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2000-2018 Bo Zimmerman, Jeremy Vyska

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
 * An Area is an abstract collection of rooms organized together under a single name
 * in order to share attributes or give some common functionality.  Areas can also
 * include other areas in a parent-child relationship.
 *
 * @author Bo Zimmerman, Jeremy Vyska
 *
 */
public interface Area extends Economics, PhysicalAgent, Places
{
	/**
	 * Return basic attributed flag about the area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#FLAG_THIN
	 * @return either 0, or a bitmap of FLAG_ constants
	 */
	public long flags();

	/**
	 * Returns the technology level supported by this area.  Activities within
	 * rooms within this area will be affected by the results of this flag.
	 * May return THEME_INHERIT if the area inherits a theme from above.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @return a bitmap of the themes supported by this area.
	 */
	public int getThemeCode();

	/**
	 * Returns the technology level supported by this area.  Activities within
	 * rooms within this area will be affected by the results of this flag.
	 * May result in consulting parent areas to determine a theme
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @return a bitmap of the themes supported by this area.
	 */
	public int getTheme();

	/**
	 * Sets the technology level supported by this area.  Activities within
	 * rooms within this area will be affected by the results of this flag.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param level the bitmap representing the tech level
	 */
	public void setTheme(int level);

	/**
	 * Returns the coffeemud .cmare filename that will be used when the EXPORT command
	 * is used in such a way as to auto-generate filenames.
	 * @return the name of the .cmare filename to use
	 */
	public String getArchivePath();

	/**
	 * Sets the coffeemud .cmare filename that will be used when the EXPORT command
	 * is used in such a way as to auto-generate filenames.
	 * @param pathFile  the name of the .cmare filename to use
	 */
	public void setArchivePath(String pathFile);

	/**
	 * Returns a reference to the Climate object that represents the
	 * current and future weather for this area.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Climate
	 * @return a com.planet_ink.coffee_mud.Common.interfaces.Climate object
	 */
	public Climate getClimateObj();

	/**
	 * Sets a reference to the Climate object that represents the
	 * current and future weather for this area.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Climate
	 * @param obj a com.planet_ink.coffee_mud.Common.interfaces.Climate object
	 */
	public void setClimateObj(Climate obj);

	/**
	 * Returns a reference to the TimeClock object that represents the
	 * calendar and date/time for this area.  May be shared by numerous
	 * areas.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock
	 * @return a com.planet_ink.coffee_mud.Common.interfaces.TimeClock object
	 */
	public TimeClock getTimeObj();

	/**
	 * Sets a reference to the TimeClock object that represents the
	 * calendar and date/time for this area.  May be shared by numerous
	 * areas.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock
	 * @param obj a com.planet_ink.coffee_mud.Common.interfaces.TimeClock object
	 */
	public void setTimeObj(TimeClock obj);

	/**
	 * Sets the name of the author of this area, an arbitrary string
	 * @param authorID the author of the area
	 */
	public void setAuthorID(String authorID);

	/**
	 * Returns the name of the author of this area, an arbitrary string
	 * @return the author of the area
	 */
	public String getAuthorID();

	/**
	 * Sets the default currency for this area, which will be referenced by
	 * shopkeepers, bankers, and other mobs.  See Archon's Guide for the proper
	 * format for this string, as it can be anything from a full current
	 * definition, to a reference to an existing one.
	 * @return a currency name/definition
	 */
	public String getCurrency();

	/**
	 * Returns the default currency for this area, which will be referenced by
	 * shopkeepers, bankers, and other mobs.  See Archon's Guide for the proper
	 * format for this string, as it can be anything from a full current
	 * definition, to a reference to an existing one.
	 * @param currency  a currency name/definition
	 */
	public void setCurrency(String currency);

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * @return the number of such strings defined
	 */
	public int numBlurbFlags();

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * @return the number of such strings defined for this area AND parent areas
	 */
	public int numAllBlurbFlags();

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * This method returns the definition of a named flag
	 * @param flag the name of the flag to return
	 * @return the definition/string value of the flag
	 */
	public String getBlurbFlag(String flag);

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * This method returns the name of an enumerated flag.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getBlurbFlag(String)
	 * @return enumeration of blurb flag keys, call getBlurbFlag(String) for the value
	 */
	public Enumeration<String> areaBlurbFlags();

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * This method adds a new flag.
	 * @param flagPlusDesc the flag name, space, followed by definition
	 */
	public void addBlurbFlag(String flagPlusDesc);

	/**
	 * A blurb flag is a run-time modifiable set of strings that can be added
	 * to an area in order to display them in the HELP entry for an area.
	 * This method deletes an existing flag by name.
	 * @param flagOnly the name of the flag to remove.
	 */
	public void delBlurbFlag(String flagOnly);

	/**
	 * This method causes all proper rooms within this area to have their
	 * run-time generated Skys to be re-generated.  It is called at boot-time
	 * and when areas are reset or re-generated.
	 */
	public void fillInAreaRooms();

	/**
	 * This method causes a given room to have its run-time generated Skys to
	 * be re-generated.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R the Room object to "fill-in"
	 */
	public void fillInAreaRoom(Room R);

	/**
	 * This method adds a new Room to this area.  It is called by the Room.setArea(
	 * method, and should rarely if ever be called directly.  It calls addMetroRoom
	 * on all parent areas to make them aware of it as well.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#addMetroRoom(Room)
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R the Room to add.
	 */
	public void addProperRoom(Room R);

	/**
	 * This method removes an existing Room from this area.  It also removes it
	 * from parent areas.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#delMetroRoom(Room)
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R the Room to delete.
	 */
	public void delProperRoom(Room R);

	/**
	 * Returns a room of the given roomID, if it has already been added by calling
	 * addProperRoom.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param roomID the roomID of the room to return.
	 * @return a reference to the room that the id refers to, IF the room belongs here.
	 */
	public Room getRoom(String roomID);

	/**
	 * Returns whether the given Room object belongs to this Area, even if the
	 * Room object properly has not been loaded yet (due to the area being thin).
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R the Room object to check for
	 * @return whether it belongs to this Area or no.
	 */
	public boolean isRoom(Room R);

	/**
	 * Returns a random room from this area, loading it if necessary.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a reference to a random room from this area.
	 */
	public Room getRandomProperRoom();

	/**
	 * Returns an enumerator for all previously loaded rooms that
	 * properly belongs to this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getCompleteMap()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return an enumerator of Room objects
	 */
	public Enumeration<Room> getProperMap();

	/**
	 * Returns an enumerator for all previously loaded rooms that
	 * properly belongs to this area, along with their skys or underwater
	 * add-ons.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getProperMap()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return an enumerator of Room objects
	 */
	public Enumeration<Room> getFilledProperMap();

	/**
	 * Designates that the given roomID belongs to this Area.
	 * @param roomID the roomID of a room which should belong to this Area.
	 */
	public void addProperRoomnumber(String roomID);

	/**
	 * Designates that the given roomID no longer belongs to this Area.
	 * @param roomID the roomID of a room which should no longer belong to this Area.
	 */
	public void delProperRoomnumber(String roomID);

	/**
	 * This method is the same as getProperMap, except that it will load any
	 * Rooms that belong to the area but have not yet been loaded.  The
	 * Enumerator returned is thus a more complete set than returned by getProperMap
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getProperMap()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return an enumerator of Room objects
	 */
	public Enumeration<Room> getCompleteMap();

	/**
	 * This method is the same as getFilledProperMap, except that it will load any
	 * Rooms that belong to the area but have not yet been loaded.  The
	 * Enumerator returned is thus a more complete set than returned by getFilledProperMap
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getFilledProperMap()
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return an enumerator of Room objects
	 */
	public Enumeration<Room> getFilledCompleteMap();

	/**
	 * Returns whether this area has any proper rooms at all, even if uncached.
	 * @return true if there are no proper rooms, false otherwise
	 */
	public boolean isProperlyEmpty();

	/**
	 * Returns a RoomnumberSet for all rooms that properly belong to this area, including
	 * those not yet loaded.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet
	 * @return a com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet object
	 */
	public RoomnumberSet getProperRoomnumbers();

	/**
	 * Sets the RoomnumberSet for all rooms that properly belong to this area, including
	 * those not yet loaded.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet
	 * @param set a com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet object
	 */
	public void setProperRoomnumbers(RoomnumberSet set);

	/**
	 * Returns a RoomnumberSet for all rooms that properly belong to this area,
	 * excluding those not yet loaded.
	 * @return a com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet object
	 */
	public RoomnumberSet getCachedRoomnumbers();

	/**
	 * Returns a count of all cached rooms that belong to this area, excluding skys
	 * and auto-generated rooms.
	 * @return a count of the number of rooms that have an ID
	 */
	public int numberOfProperIDedRooms();

	/**
	 * Returns a count of all cached rooms that belong to this area, which have been
	 * loaded.
	 * @return the number of rooms loaded.
	 */
	public int properSize();

	/**
	 * Returns an enumerator for all previously loaded rooms that
	 * properly belongs to this area AND to any child areas.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return an enumerator of Room objects
	 */
	public Enumeration<Room> getMetroMap();

	/**
	 * Designates that a given Room object belongs to one of this areas
	 * children.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R a Room object from one of this areas child areas.
	 */
	public void addMetroRoom(Room R);

	/**
	 * Designates that a given Room object no longer belongs to one of this areas
	 * children.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param R a Room object formerly from one of this areas child areas.
	 */
	public void delMetroRoom(Room R);

	/**
	 * Designates that a given roomid represents a room which
	 * belongs to one of this areas children.
	 * @param roomID a roomid for a room
	 */
	public void addMetroRoomnumber(String roomID);

	/**
	 * Designates that a given roomid represents a room which no longer
	 * belongs to one of this areas children.
	 * @param roomID a roomid for a former room
	 */
	public void delMetroRoomnumber(String roomID);

	/**
	 * Returns a count of all cached rooms that belong to this area, or to a child
	 * area, which have been loaded.
	 * @return the count of rooms in this metro area
	 */
	public int metroSize();

	/**
	 * Returns whether the given Area object is either THIS area, a child of this
	 * area, or a decendent.
	 * @param A an Area object
	 * @return whether the Area is downward-kin
	 */
	public boolean inMyMetroArea(Area A);

	/**
	 * Returns a random room from this area, or one of its children, loading it if necessary.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a random Room object from this or a child area
	 */
	public Room getRandomMetroRoom();

	/**
	 * Generates a new RoomID for a new Room in this area.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param startRoom the room connected to the upcoming new one (or null)
	 * @param direction the direction from the startRoom the new one will go
	 * @return a generated new RoomID for the new Room
	 */
	public String getNewRoomID(Room startRoom, int direction);

	/**
	 * Area Flags, unlike flags, is a PURELY run-time set that changes depending
	 * upon how the engine is operating on this area or its content.
	 * This method changes the state.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area.State
	 * @param newState the new state to put this entire area into
	 */
	public void setAreaState(State newState);

	/**
	 * Area Flags, unlike flags, is a PURELY run-time set that changes depending
	 * upon how the engine is operating on this area or its content.
	 * This method returns the state.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area.State
	 * @return a numeric state for this area
	 */
	public State getAreaState();

	/**
	 * Adds a SubOp to this area.  This must be a valid Player Name.  A Player with
	 * this designation will have their AREA security flags activated when in this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#delSubOp(String)
	 * @param username a players Name
	 */
	public void addSubOp(String username);

	/**
	 * Removes a SubOp to this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#addSubOp(String)
	 * @param username a players Name
	 */
	public void delSubOp(String username);

	/**
	 * Returns whether the given player name is a SubOp to this area.  This must be
	 * a valid Player Name.  A Player with this designation will have their
	 * AREA_ security flags activated when in this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#addSubOp(String)
	 * @param username a players Name
	 * @return whether the players name is on the subop list for this area
	 */
	public boolean amISubOp(String username);

	/**
	 * Returns a semicolon delimited list of player Names that represent the SubOp
	 * list for this area.   A Player with this designation will have their
	 * AREA_ security flags activated when in this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#addSubOp(String)
	 * @return a semicolon delimited list of player Names.
	 */
	public String getSubOpList();

	/**
	 * Sets the semicolon delimited list of player Names that represent the SubOp
	 * list for this area.   A Player with this designation will have their
	 * AREA_ security flags activated when in this area.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#addSubOp(String)
	 * @param list  a semicolon delimited list of player Names.
	 */
	public void setSubOpList(String list);

	/**
	 * Returns a enumeration of player Names that represent the SubOp
	 * list for this area.   A Player with this designation will have their
	 * AREA_ security flags activated when in this area.
	 * @return enumeration of player Names
	 */
	public Enumeration<String> subOps();

	/**
	 * Returns a descriptive list of statistics about this area based on a
	 * snapshot from getAreaIStats(), which is cached after being generated.
	 * This stringbuffer returned is user-readable.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats()
	 * @return a user readable string describing stats about the area.
	 */
	public StringBuffer getAreaStats();

	/**
	 * Returns an integer array of statistics about this area based on
	 * a snapshot generated the first time it is called.  This array is
	 * the cached for future calls, but can be unloaded from resources
	 * using the UNLOAD command, to force a re-generation.
	 * The array is dereferenced using AREASTAT_ constants.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area.Stats
	 * @return an array of integer statistics
	 */
	public int[] getAreaIStats();

	/**
	 * Returns a fake player-level, to be used instead of median or average
	 * actual mob level, for cases where you want an area to reflect its
	 * higher level mobs over low level trash.  Returns 0 if none is applied
	 * and the median or average should instead be used.
	 * @see Area#setPlayerLevel(int)
	 * @return the player level, or 0
	 */
	public int getPlayerLevel();
	
	/**
	 * Sets a fake player-level, to be used instead of median or average
	 * actual mob level, for cases where you want an area to reflect its
	 * higher level mobs over low level trash.  Returns 0 if none is applied
	 * and the median or average should instead be used.
	 * @see Area#getPlayerLevel()
	 * @param level the player level, or 0
	 */
	public void setPlayerLevel(int level);
	
	
	/**
	 * An enumerator list of Area objects representing the Children Areas of this
	 * Area.
	 * A Child Area inherets certain behaviors and property effects from its parents
	 * @return an enumerator of Area objects
	 */
	public Enumeration<Area> getChildren();

	/**
	 * Returns the named Child Area object for this Area
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param named the name of an Area
	 * @return an Area Child object
	 */
	public Area getChild(String named);

	/**
	 * Returns whether the Area is a child of this Area
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param area an Area object
	 * @return whether the area is a child of this one
	 */
	public boolean isChild(Area area);

	/**
	 * Returns whether the Area named is a child of this Area
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param named the name of an Area
	 * @return whether the area named is a child of this one
	 */
	public boolean isChild(String named);

	/**
	 * Designates the given Area object as a Child of this one.
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param area an Area object
	 */
	public void addChild(Area area);

	/**
	 * Designates the given Area object as no longer being Child of this one.
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param area an Area object
	 */
	public void removeChild(Area area);

	/**
	 * Returns whether the Area named MAY BE designated as a child of this Area
	 * A Child Area inherets certain behaviors and property effects from its Parents
	 * @param area an Area to check
	 * @return whether the Area named MAY BE designated as a child of this Area
	 */
	public boolean canChild(Area area);

	/**
	 * An enumerator list of Area objects representing the Parent Areas of this
	 * Area.
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @return an enumerator of Area objects
	 */
	public Enumeration<Area> getParents();

	/**
	 * Returns the named Parent Area object for this Area
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param named the name of an Area
	 * @return an Area Parent object
	 */
	public Area getParent(String named);

	/**
	 * Returns a list of all Parent Area objects to this one, recursively
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @return a list of Area objects
	 */
	public List<Area> getParentsRecurse();

	/**
	 * Returns whether the Area is a Parent of this Area
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param area an Area object
	 * @return whether the area is a Parent of this one
	 */
	public boolean isParent(Area area);

	/**
	 * Returns whether the Area named is a Parent of this Area
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param named the name of an Area
	 * @return whether the area named is a Parent of this one
	 */
	public boolean isParent(String named);

	/**
	 * Designates the given Area object as a Parent of this one.
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param area an Area object
	 */
	public void addParent(Area area);

	/**
	 * Designates the given Area object as no longer being Parent of this one.
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param area an Area object
	 */
	public void removeParent(Area area);

	/**
	 * Returns whether the Area named MAY BE designated as a parent of this Area
	 * A Parent Area passes down certain behaviors and property effects to its children
	 * @param newParent an Area to check
	 * @return whether the Area named MAY BE designated as a parent of this Area
	 */
	public boolean canParent(Area newParent);

	/**
	 * Class to hold a reference to a child area instance,
	 * and the inhabitants who belong there.
	 * @author Bo Zimmerman
	 */
	public static class AreaInstanceChild
	{
		/** List of players and their pets that belong in this instance */
		public final List<WeakReference<MOB>> mobs;
		/** Reference to the actual area where they go. */
		public final Area A;
		/** the time the instance was created */
		public final long creationTime;
		public AreaInstanceChild(final Area A, final List<WeakReference<MOB>> mobs)
		{
			this.A=A;
			this.mobs=mobs;
			this.creationTime = System.currentTimeMillis();
		}
	}

	public final static String[] THEME_BIT_NAMES={"FANTASY","TECH","HEROIC","SKILLONLY"};
	/**	Bitmap flag meaning that the object supports magic.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_INHERIT=0;
	/**	Bitmap flag meaning that the object supports magic.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_FANTASY=1;
	/**	Bitmap flag meaning that the object supports technology.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_TECHNOLOGY=2;
	/**	Bitmap flag meaning that the object supports super powers.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_HEROIC=4;
	/**	Bitmap flag meaning that the object supports ALL themes.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_ALLTHEMES=THEME_FANTASY|THEME_TECHNOLOGY|THEME_HEROIC;
	/**	Bitmap flag meaning that the object only supports usage of above in Skills.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTheme() */
	public final static int THEME_SKILLONLYMASK=8;
	/** Indexed description of the THEME_ bitmap constants in all possible combinations -- in upper/single word format */
	public final static String[] THEME_NAMES={"INHERITED","FANTASY","TECH","FANTAST+TECH","HEROIC","HEROIC+FANTASY","HEROIC+TECH","ALL","SKILLONLY"};
	/**	Indexed description of the THEME_ bitmap constants in all possible combinations.  In readable format.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 */
	public final static String[] THEME_PHRASE={"Inherited",   		  // 0
											   "Fantasy",   		  // 1
											   "Technical", 		  // 2
											   "Fantasy & Technical", // 3
											   "Heroic",			  // 4
											   "Heroic & Fantasy",    // 5
											   "Heroic & Technical",  // 6
											   "All Allowed"		  // 7
	};
	/**	Indexed extended description of the THEME_ bitmap constants in all possible combinations.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 */
	public final static String[] THEME_PHRASE_EXT={"Unavailable",   	  // 0
												   "Fantasy",   		  // 1
												   "Technical", 		  // 2
												   "Fantasy & Technical", // 3
												   "Heroic",			  // 4
												   "Heroic & Fantasy",    // 5
												   "Heroic & Technical",  // 6
												   "All Allowed",   	  // 7
												   "Unavail. Skill only", // 8
												   "Fantasy Skills Only", // 9
												   "Tech Skill Only",     // 10
												   "Fant&Tech Skill Only",// 11
												   "Powers only",   	  // 12
												   "Powers & Spells only",// 13
												   "Hero&Tech Skill only",// 14
												   "Any skill only" 	  // 15

	};

	/**	State flag meaning this area is a THIN type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_THIN=1;
	/**	State flag meaning this area is a INSTANCE parent type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_INSTANCE_PARENT=2;
	/**	State flag meaning this area is a INSTANCE child type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_INSTANCE_CHILD=4;

	/** Various area IStat constants.. see @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public static enum Stats
	{
		POPULATION,
		MIN_LEVEL,
		MAX_LEVEL,
		AVG_LEVEL,
		MED_LEVEL,
		AVG_ALIGNMENT,
		MED_ALIGNMENT,
		TOTAL_LEVELS,
		TOTAL_INTELLIGENT_LEVELS,
		VISITABLE_ROOMS,
		INDOOR_ROOMS,
		MIN_ALIGNMENT,
		MAX_ALIGNMENT
	}

	/**
	 * State flag for areas.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaState()
	 */
	public static enum State
	{
		ACTIVE, /** Area is mobile, threaded, and running */
		PASSIVE, /** Area is threaded, and running, but not mobile */
		FROZEN, /** Area is thread, but not running or mobile */
		STOPPED/** Area is neither threaded, running, nor mobile */

	}
	/**	Amount of time of player absence before an area automatically goes from Active to passive */
	public final static long TIME_PASSIVE_LAPSE=60*1000*30; // 30 mins

	/**
	 * @author Owner
	 * This enumerator is for loading any rooms not yet
	 * loaded, all at enumeration-time.
	 */
	public class RoomIDEnumerator implements Enumeration<Room>
	{
		private Enumeration<String> roomIDEnumerator=null;
		private Area area=null;

		public RoomIDEnumerator(Area myArea)
		{
			area=myArea;
			roomIDEnumerator=area.getProperRoomnumbers().getRoomIDs();
		}
		
		@Override 
		public boolean hasMoreElements()
		{
			return roomIDEnumerator.hasMoreElements();
		}
		
		@Override
		public Room nextElement()
		{
			final String roomID=roomIDEnumerator.nextElement();
			if(roomID==null)
				return null;
			final Room R=area.getRoom(roomID);
			if(R==null)
				return nextElement();
			if(R.expirationDate()!=0)
				R.setExpirationDate(R.expirationDate()+(1000*60*10));
			return CMLib.map().getRoom(R);
		}
	}

	/**
	 * @author Owner
	 * This class implements the getCompleteMap() method by enumerating through
	 * the complete list of roomIDs for this area and loading any rooms not yet
	 * loaded, all at enumeration-time.
	 */
	public class CompleteRoomEnumerator implements Enumeration<Room>
	{
		private MultiEnumeration<Room> roomEnumerators=null;

		public CompleteRoomEnumerator(MultiEnumeration<Room> enums)
		{
			roomEnumerators=enums;
		}

		public CompleteRoomEnumerator(Enumeration<Room> enu)
		{
			roomEnumerators=new MultiEnumeration<Room>(enu);
		}

		@Override
		public boolean hasMoreElements()
		{
			return roomEnumerators.hasMoreElements();
		}

		@Override
		public Room nextElement()
		{
			final Room room=roomEnumerators.nextElement();
			if(room instanceof GridLocale)
				roomEnumerators.addEnumeration(new IteratorEnumeration<Room>(((GridLocale) room).getAllRooms().iterator()));
			if((room == null) && (hasMoreElements()))
				return nextElement();
			return room;
		}
	}

	/**
	 * Comparator for tree sets, comparing room ids of rooms
	 * @author Bo Zimmerman
	 */
	public static class RoomIDComparator implements Comparator<String>
	{
		@Override
		public int compare(String arg0, String arg1)
		{
			return arg0.compareToIgnoreCase(arg1);
		}
	}

	/**
	 * Comparator for tree sets, comparing room ids of rooms
	 * @author Bo Zimmerman
	 */
	public static class RoomComparator implements Comparator<Room>
	{
		@Override
		public int compare(Room arg0, Room arg1)
		{
			return arg0.roomID().compareToIgnoreCase(arg1.roomID());
		}
	}

}
