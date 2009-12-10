package com.planet_ink.coffee_mud.Areas.interfaces;
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

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman, Jeremy Vyska

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
 * include other areas in a parent->child relationship.  
 * 
 * @author Bo Zimmerman, Jeremy Vyska
 *
 */
@SuppressWarnings("unchecked")
public interface Area extends Environmental, Economics
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
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @return a bitmap of the themes supported by this area.
	 */
	public int getTechLevel();
	/**
	 * Sets the technology level supported by this area.  Activities within
	 * rooms within this area will be affected by the results of this flag.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param level the bitmap representing the tech level
	 */
	public void setTechLevel(int level);
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
	 * Returns a bitmap of climate flags for this area which will be used to influence
	 * the weather for the area in addition to season and other factors.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#CLIMASK_COLD
	 * @return a CLIMASK bitmap
	 */
	public int climateType();
	/**
	 * Returns a bitmap of climate flags for this area which will be used to influence
	 * the weather for the area in addition to season and other factors.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#CLIMASK_COLD
	 * @param newClimateType a CLIMASK bitmap
	 */
	public void setClimateType(int newClimateType);
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
     * @param which the index into the list of flag definition names
     * @return the flag definition name, call getBlurbFlag(String) for the value
     */
    public String getBlurbFlag(int which);
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
	public Enumeration getProperMap();
    /**
     * Returns an enumerator for all previously loaded rooms that
     * properly belongs to this area, along with their skys or underwater
     * add-ons.
     * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getProperMap()
     * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
     * @return an enumerator of Room objects
     */
	public Enumeration getFilledProperMap();
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
	public Enumeration getCompleteMap();
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
	public Enumeration getMetroMap();
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
	 * Returns a collection of all of this areas rooms which have been loaded, and all rooms
	 * of all child areas which have been loaded, excluding those not yet cached.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @return a vector of Room objects
	 */
	public Vector getMetroCollection();
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
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#STATE_ACTIVE
	 * @param newState
	 */
	public void setAreaState(int newState);
	/**
	 * Area Flags, unlike flags, is a PURELY run-time set that changes depending
	 * upon how the engine is operating on this area or its content.
	 * This method returns the state.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#STATE_ACTIVE
	 * @return a numeric state for this area
	 */
	public int getAreaState();
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
	 * Returns a Vector of player Names that represent the SubOp
	 * list for this area.   A Player with this designation will have their 
	 * AREA_ security flags activated when in this area.
	 * @return Vector of player Names
	 */
	public Vector getSubOpVectorList();
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
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#AREASTAT_AVGALIGN
	 * @return an array of integer statistics
	 */
	public int[] getAreaIStats();
    /**
     * Designates that the area named by the given String will be a Child Area
     * of this area.
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param str the name of an Area
     */
    public void addChildToLoad(String str);
    /**
     * Designates that the area named by the given String will be a Parent Area
     * of this area.
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param str the name of an Area
     */
    public void addParentToLoad(String str);
    /**
     * An Enumerated list of Area objects representing the Children Areas of this
     * Area.
     * A Child Area inherets certain behaviors and property effects from its parents
     * @return an enumeration of Area objects
     */
    public Enumeration getChildren();
    /**
     * Returns a semicolon delimited list of Area names representing the Children Areas of this
     * Area.
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @return a string of semicolon-delimited names
     */
    public String getChildrenList();
    /**
     * Returns the number of Child areas to this Area
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @return the number of children this Area has
     */
    public int getNumChildren();
    /**
     * Returns the enumerated Child Area object for this Area
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param num which child to return
     * @return an Area Child object
     */
    public Area getChild(int num);
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
     * @param named an Area object
     * @return whether the area is a child of this one
     */
    public boolean isChild(Area named);
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
     * @param Adopted an Area object
     */
    public void addChild(Area Adopted);
    /**
     * Designates the given Area object as no longer being Child of this one.
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param Disowned an Area object
     */
    public void removeChild(Area Disowned);
    /**
     * Designates the given Area enumerated as no longer being Child of this one.
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param Disowned the index into this Areas children
     */
    public void removeChild(int Disowned);
    /**
     * Returns whether the Area named MAY BE designated as a child of this Area
     * A Child Area inherets certain behaviors and property effects from its Parents
     * @param newChild an Area to check
     * @return whether the Area named MAY BE designated as a child of this Area
     */
    public boolean canChild(Area newChild);
    /**
     * An Enumerated list of Area objects representing the Parent Areas of this
     * Area.
     * A Parent Area passes down certain behaviors and property effects to its children
     * @return an enumeration of Area objects
     */
    public Enumeration getParents();
    /**
     * Returns a semicolon delimited list of Area names representing the Parent Areas of this
     * Area.
     * A Parent Area passes down certain behaviors and property effects to its children
     * @return a string of semicolon-delimited names
     */
    public String getParentsList();
    /**
     * Returns the number of Parent areas to this Area
     * A Parent Area passes down certain behaviors and property effects to its children
     * @return the number of children this Area has
     */
    public int getNumParents();
    /**
     * Returns the named Parent Area object for this Area
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param num the num of an Parent
     * @return an Area Parent object
     */
    public Area getParent(int num);
    /**
     * Returns the named Parent Area object for this Area
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param named the name of an Area
     * @return an Area Parent object
     */
    public Area getParent(String named);
    /**
     * Returns a Vector of all Parent Area objects to this one, recursively
     * A Parent Area passes down certain behaviors and property effects to its children
     * @return a Vector of Area objects
     */
    public Vector getParentsRecurse();
    /**
     * Returns whether the Area is a Parent of this Area
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param named an Area object
     * @return whether the area is a Parent of this one
     */
    public boolean isParent(Area named);
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
     * @param Adopted an Area object
     */
    public void addParent(Area Adopted);
    /**
     * Designates the given Area object as no longer being Parent of this one.
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param Disowned an Area object
     */
    public void removeParent(Area Disowned);
    /**
     * Designates the given Area enumerated as no longer being Parent of this one.
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param Disowned the index into this Areas children
     */
    public void removeParent(int Disowned);
    /**
     * Returns whether the Area named MAY BE designated as a parent of this Area
     * A Parent Area passes down certain behaviors and property effects to its children
     * @param newParent an Area to check
     * @return whether the Area named MAY BE designated as a parent of this Area
     */
    public boolean canParent(Area newParent);
    /**
     * @author Owner
     * This class implements the getCompleteMap() method by enumerating through
     * the complete list of roomIDs for this area and loading any rooms not yet
     * loaded, all at enumeration-time.
     */
    public class CompleteRoomEnumerator implements Enumeration
    {
    	Enumeration roomEnumerator=null;
    	Area area=null;
    	public CompleteRoomEnumerator(Area myArea){
    		area=myArea;
    		roomEnumerator=area.getProperRoomnumbers().getRoomIDs();
    	}
    	public boolean hasMoreElements(){return roomEnumerator.hasMoreElements();}
    	public Room nextElement()
    	{
    		String roomID=(String)roomEnumerator.nextElement();
    		if(roomID==null) return null;
			Room R=area.getRoom(roomID);
			if(R==null) return nextElement();
			if(R.expirationDate()!=0)
				R.setExpirationDate(R.expirationDate()+(1000*60*10));
			return CMLib.map().getRoom(R);
    	}
    }
    
    public final static String[] THEME_DESCS={"FANTASY","TECH","HEROIC","SKILLONLY"};
	/**	Bitmap flag meaning that the object supports magic.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTechLevel() */
	public final static int THEME_FANTASY=1;
	/**	Bitmap flag meaning that the object supports technology.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTechLevel() */
	public final static int THEME_TECHNOLOGY=2;
	/**	Bitmap flag meaning that the object supports super powers.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTechLevel() */
	public final static int THEME_HEROIC=4;
	/**	Bitmap flag meaning that the object only supports usage of above in Skills.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getTechLevel() */
	public final static int THEME_SKILLONLYMASK=8;
	/**	Indexed description of the THEME_ bitmap constants in all possible combinations.  
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY 
	 */
	public final static String[] THEME_PHRASE={"Unknown",             // 0
	    									   "Fantasy",             // 1
											   "Technical",           // 2
											   "Fantasy & Technical", // 3
											   "Heroic",              // 4
											   "Heroic & Fantasy",    // 5
											   "Heroic & Technical",  // 6
											   "All Allowed"          // 7
	};
	/**	Indexed extended description of the THEME_ bitmap constants in all possible combinations.  
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY 
	 */
	public final static String[] THEME_PHRASE_EXT={"Unavailable",         // 0
											       "Fantasy",             // 1
											       "Technical",           // 2
											       "Fantasy & Technical", // 3
											       "Heroic",              // 4
											       "Heroic & Fantasy",    // 5
											       "Heroic & Technical",  // 6
												   "All Allowed",         // 7
		    									   "Unavail. Skill only", // 8
		    									   "Fantasy Skills Only", // 9
												   "Tech Skill Only",     // 10
												   "Fant&Tech Skill Only",// 11
												   "Powers only",         // 12
												   "Powers & Spells only",// 13
												   "Hero&Tech Skill only",// 14
												   "Any skill only"       // 15
											   
	};	
	/**	Bitmap climate flag meaning that the area has normal weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMASK_NORMAL=0;
	/**	Bitmap climate flag meaning that the area has wet weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMASK_WET=1;
	/**	Bitmap climate flag meaning that the area has cold weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMASK_COLD=2;
	/**	Bitmap climate flag meaning that the area has windy weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMATE_WINDY=4;
	/**	Bitmap climate flag meaning that the area has hot weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMASK_HOT=8;
	/**	Bitmap climate flag meaning that the area has dry weather.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int CLIMASK_DRY=16;
	/**	Indexed description of the CLIMASK_ bitmap constants in all possible combinations.  
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#CLIMASK_NORMAL 
	 */
	public final static String[] CLIMATE_DESCS={"NORMAL","WET","COLD","WINDY","HOT","DRY"};
	/**	Number of CLIMASK_ constants.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int NUM_CLIMATES=6;
	/**	Bitmap climate flag meaning that the area has all weather modifiers.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#climateType() */
	public final static int ALL_CLIMATE_MASK=31;
	
	/**	State flag meaning this area is a THIN type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_THIN=1;
	/**	State flag meaning this area is a INSTANCE parent type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_INSTANCE_PARENT=2;
	/**	State flag meaning this area is a INSTANCE child type area.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#flags() */
	public final static int FLAG_INSTANCE_CHILD=4;
	
	/**	Index into area IStats for population.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_POPULATION=0;
	/**	Index into area IStats for min level.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_MINLEVEL=1;
	/**	Index into area IStats for max level.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_MAXLEVEL=2;
	/**	Index into area IStats for avg level.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_AVGLEVEL=3;
	/**	Index into area IStats for median level.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_MEDLEVEL=4;
	/**	Index into area IStats for avg alignment.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_AVGALIGN=5;
	/**	Index into area IStats for median alignment.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_MEDALIGN=6;
	/**	Index into area IStats for total levels.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_TOTLEVEL=7;
	/**	Index into area IStats for total intelligent levels.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_INTLEVEL=8;
	/**	Index into area IStats for number numbered rooms.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
    public final static int AREASTAT_VISITABLEROOMS=9;
	/**	Index into area IStats for number of these constants.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static int AREASTAT_NUMBER=10;
	/** Descriptions of the various area IStat constants.. see @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaIStats() */
	public final static String[] AREASTAT_DESCS={
		"POPULATION","MIN_LEVEL","MAX_LEVEL","AVG_LEVEL","MED_LEVEL","AVG_ALIGNMENT","MED_ALIGNMENT","TOTAL_LEVELS","TOTAL_INTELLIGENT_LEVELS","VISITABLE_ROOMS"
	};
	
	/**	State flag for area meaning Area is active.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
    public final static int STATE_ACTIVE=0;
	/**	State flag for area meaning Area is passive.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
    public final static int STATE_PASSIVE=1;
	/**	State flag for area meaning Area is frozen.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
    public final static int STATE_FROZEN=2;
	/**	State flag for area meaning Area is dead.  @see com.planet_ink.coffee_mud.Areas.interfaces.Area#getAreaFlags() */
    public final static int STATE_STOPPED=3;
	/**	Amount of time of player absence before an area automatically goes from Active to passive */
    public final static long TIME_PASSIVE_LAPSE=60*1000*30; // 30 mins
    
}
