package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.database.ClanLoader;
import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.database.DataLoader;
import com.planet_ink.coffee_mud.core.database.GAbilityLoader;
import com.planet_ink.coffee_mud.core.database.GCClassLoader;
import com.planet_ink.coffee_mud.core.database.GRaceLoader;
import com.planet_ink.coffee_mud.core.database.JournalLoader;
import com.planet_ink.coffee_mud.core.database.MOBloader;
import com.planet_ink.coffee_mud.core.database.PollLoader;
import com.planet_ink.coffee_mud.core.database.QuestLoader;
import com.planet_ink.coffee_mud.core.database.RoomLoader;
import com.planet_ink.coffee_mud.core.database.StatLoader;
import com.planet_ink.coffee_mud.core.database.VFSLoader;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
 * Not really much point in saying a lot here.
 * This has all the methods most closely related
 * to reading from, writing to, and updating
 * the database.  That's all there is to it.
 *
 * @author Bo Zimmerman
 *
 */
public interface DatabaseEngine extends CMLibrary
{
	/**
	 * An enum of all the database table types.
	 * These are the dividers by which different
	 * connections to different databases can be
	 * assigned to different tables.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum DatabaseTables
	{
		DBABILITY,
		DBCHARCLASS,
		DBRACE,
		DBPLAYERS,
		DBPLAYERDATA,
		DBMAP,
		DBSTATS,
		DBPOLLS,
		DBVFS,
		DBJOURNALS,
		DBQUEST,
		DBCLANS,
		DBBACKLOG,
		DBCOMMANDS
	}

	/**
	 * Flags for reading room content
	 * @author Bo Zimmerman
	 *
	 */
	public static enum ReadRoomDisableFlag
	{
		STATUS,
		LIVE,
		MOBS,
		ITEMS
	}

	/**
	 * Returns the database status, formatted for html.
	 * @return the database status, formatted for html.
	 */
	public String errorStatus();

	/**
	 * Forces all existing database connections to be closed,
	 * and then re-open.
	 */
	public void resetConnections();

	/**
	 * Returns the connector object to the database, allowing
	 * SQL statements to be run.
	 * @return the connector object to the database
	 */
	public DBConnector getConnector();

	/**
	 * "Pings" all connections to the database by issueing
	 * a "SELECT 1 FROM CMCHAR".
	 * @return the number of connections pinged
	 */
	public int pingAllConnections();

	/**
	 * "Pings" all connections to the database by issueing
	 * a "SELECT 1 FROM CMCHAR", if the connection has
	 * not seen any action in the given number of milliseconds.
	 * @param overrideTimeoutIntervalMillis the connection timeout
	 * @return the number of connections pinged
	 */
	public int pingAllConnections(final long overrideTimeoutIntervalMillis);

	/**
	 * Returns whether the database is connected.
	 * @return whether the database is connected.
	 */
	public boolean isConnected();

	/**
	 * Executes an arbitrary SQL statement against your
	 * main database and returns the response number/code.
	 * @param sql the SQL statement
	 * @return the exec response number (usually num rows)
	 * @throws CMException any errors that occur
	 */
	public int DBRawExecute(String sql) throws CMException;

	/**
	 * Executes an arbitrary SQL query against your
	 * main database and returns the results as a list of
	 * string arrays, where each array is a row, and each
	 * column is a column from the query.
	 * @param sql the SQL query
	 * @return the results of the query
	 * @throws CMException any errors that occur
	 */
	public List<String[]> DBRawQuery(String sql) throws CMException;

	/**
	 * Table category: DBMAP
	 * Loads both the mob and item catalogs into the catalog library.
	 */
	public void DBReadCatalogs();

	/**
	 * Table category: DBMAP
	 * Logs all the items in outer space into the space map
	 */
	public void DBReadSpace();

	/**
	 * Table category: DBMAP
	 * This method is used to load the content (items and mobs) of the
	 * given room id into the given room object, and optionally activate
	 * the contents to live use.  startItemRejuv() is not called, however.
	 *
	 * @param roomID the id of the room to load
	 * @param thisRoom the room object to load the content into (required!)
	 * @param makeLive true to bring the mobs to life, false to leave them dead.
	 */
	public void DBReadContent(String roomID, Room thisRoom, boolean makeLive);

	/**
	 * Table category: DBMAP
	 * This method is used to load the content (mobs only) of the
	 * given room id into the given room object, without activating
	 * the contents to live use.  startItemRejuv() is not called also.
	 *
	 * @param roomID the id of the room to load
	 * @param thisRoom the room object to load the content into (required!)
	 */
	public void DBReadMobContent(final String roomID, final Room thisRoom);

	/**
	 * Table category: DBMAP
	 * This method is used to load the content (items only) of the
	 * given room id into the given room object, without activating
	 * the contents to live use.  startItemRejuv() is not called also.
	 *
	 * @param roomID the id of the room to load
	 * @param thisRoom the room object to load the content into (required!)
	 */
	public void DBReadItemContent(final String roomID, final Room thisRoom);

	/**
	 * Table category: DBMAP
	 * Check if an item with the given name is saved in this room.
	 *
	 * @param roomID the id of the room to load
	 * @param itemName the name of the item to look for
	 * @return true if an item with that name was found, false otherwise
	 */
	public boolean DBIsSavedRoomItemCopy(final String roomID, final String itemName);

	/**
	 * Table category: DBMAP
	 * Return the first instance of an item with the given name in the db for
	 * the given room.
	 *
	 * @param roomID the id of the room to load
	 * @param itemName the name of the item to look for
	 * @return true if an item with that name was found, false otherwise
	 */
	public Item DBGetSavedRoomItemCopy(final String roomID, final String itemName);

	/**
	 * Table category: DBMAP
	 * Reloads the basic data of the given area, with a prefilled Name.
	 * It does not load or alter rooms or anything like that, only
	 * the internal variables of the area.
	 *
	 * @see DatabaseEngine#DBIsAreaName(String)
	 * @see DatabaseEngine#DBReadAreaRoomList(String, boolean)
	 * @see DatabaseEngine#DBReadAreaObject(String)
	 * @see DatabaseEngine#DBReadAreaFull(String)
	 *
	 * @param A the area to reload
	 */
	public void DBReadAreaData(Area A);

	/**
	 * Table category: DBMAP
	 * Reloads the basic data of the given area by exact Name.
	 * It does not load or alter rooms or anything like that, only
	 * the internal variables of the area. Does not add the
	 * area to the map.
	 *
	 * @see DatabaseEngine#DBIsAreaName(String)
	 * @see DatabaseEngine#DBReadAreaRoomList(String, boolean)
	 * @see DatabaseEngine#DBReadAreaData(Area)
	 * @see DatabaseEngine#DBReadAreaFull(String)
	 *
	 * @param areaName the name of the area to load
	 *
	 * @return the Area object
	 */
	public Area DBReadAreaObject(String areaName);

	/**
	 * Table category: DBMAP
	 * Reloads the given area by exact Name. This includes the
	 * rooms, and the mobs, and the items, and it adds it to the
	 * map.  It's complete and total and working.
	 *
	 * @see DatabaseEngine#DBIsAreaName(String)
	 * @see DatabaseEngine#DBReadAreaRoomList(String, boolean)
	 * @see DatabaseEngine#DBReadAreaData(Area)
	 * @see DatabaseEngine#DBReadAreaObject(String)
	 *
	 * @param areaName the name of the area to load
	 *
	 * @return true if the area was read into the system map
	 */
	public boolean DBReadAreaFull(String areaName);

	/**
	 * Table category: DBMAP
	 *
	 * Checks for the database for an area with approximately the
	 * given name, returning the correct name if found, false
	 * otherwise.
	 *
	 * @see DatabaseEngine#DBReadAreaData(Area)
	 * @see DatabaseEngine#DBReadAreaRoomList(String, boolean)
	 * @see DatabaseEngine#DBReadAreaObject(String)
	 * @see DatabaseEngine#DBReadAreaFull(String)
	 *
	 * @param name the name to search for (hopefully case insensitive)
	 * @return the real name, case-correct, or NULL if not found
	 */
	public String DBIsAreaName(String name);

	/**
	 * Returns a thin record of all mobs in an area
	 * proper
	 *
	 * @param name the name of the area
	 * @return all the room content records
	 */
	public RoomContent[] DBReadAreaMobs(String name);

	/**
	 * Returns a thin record of all items in an area
	 * proper
	 *
	 * @param name the name of the area
	 * @return all the room content records
	 */
	public RoomContent[] DBReadAreaItems(String name);

	/**
	 * Table category: DBMAP
	 * Permanently Loads and returns a single Room object,
	 * without populating its contents yet. This is often done in
	 * preparation to read the content, the exits, or both.
	 * It sets the area and room ID, as if the room will have a
	 * permanent home in the game.
	 *
	 * @see DatabaseEngine#DBReadContent(String, Room, boolean)
	 * @see DatabaseEngine#DBReReadRoomData(Room)
	 * @see DatabaseEngine#DBReadRoomObject(String, boolean, boolean)
	 *
	 * @param roomID the room id of the room object to load
	 * @param reportStatus true to populate global status, false otherwise
	 * @return the room loaded, or null if it could not be
	 */
	public Room DBReadRoom(String roomID, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * Reloads the title, description, affects, and other internal
	 * fields of the given room.
	 * @param room the room to re-read dat afor
	 * @return true, unless something went wrong
	 */
	public boolean DBReReadRoomData(Room room);

	/**
	 * Table category: DBMAP
	 * Read the Room object of the given roomID and returns
	 * the object.  It does not load the contents.  The
	 * difference between this and dbreadroom is beyond me.
	 * I don't think this method actually adds the room
	 * to an area or to the map.
	 * @see DatabaseEngine#DBReadRoom(String, boolean)
	 * @param roomIDtoLoad the id of the room to load
	 * @param loadXML populates room effects, behavs, etc
	 * @param reportStatus true to populate global status, false otherwise
	 * @return the room loaded, or null if it could not be
	 */
	public Room DBReadRoomObject(String roomIDtoLoad, boolean loadXML, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * Reads all the Room objects in the given area name
	 * and returns them as an array of rooms.  It does not load the
	 * item or mob contents.  The rooms are not actually
	 * added to the Area or the map.
	 * @param areaName the area name of rooms to load
	 * @param reportStatus true to populate global status, false otherwise
	 * @return the rooms loaded
	 */
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * Reads all the room ids that are affected by the given array of
	 * Ability IDs as properties/affects.
	 *
	 * @param parentA the parent area to limit yourself to
	 * @param metro true to also search children
	 * @param propIDs the list of Ability IDs.
	 * @param propArgs the list of ability arguments to search for
	 * @return the list of room ids
	 */
	public Set<String> DBReadAffectedRoomIDs(final Area parentA, final boolean metro, final String[] propIDs, String[] propArgs);

	/**
	 * Table category: DBMAP
	 * Reads all the room ids and exits that link to the given room id
	 * @param roomID the room id to find links to
	 * @return the map of directions to a pair of outgoing room ids and exit ids
	 */
	public Map<Integer,Pair<String,String>> DBReadIncomingRoomExitIDsMap(final String roomID);

	/**
	 * Table category: DBMAP
	 * Reads the room description of the given room id and
	 * returns it.
	 * @param roomID the room id of the description to read
	 * @return the description, or null
	 */
	public String DBReadRoomDesc(String roomID);

	/**
	 * Table category: DBMAP
	 * Counts the number of mobs and items in the room
	 * according to the database, and returns the counts
	 * as a numeric array where the first element is the
	 * number of mobs and the second the number of items.
	 * @param roomID the room id to return counts for
	 * @return the counts as a 2 entry array
	 */
	public int[] DBCountRoomMobsItems(String roomID);

	/**
	 * Table category: DBMAP
	 * Returns a sterile prototype of the given room item
	 * freshly created.  It must exist in a room.
	 *
	 * @param roomID the room id
	 * @param itemNum the item database id code
	 * @return the item object
	 */
	public Item DBReadRoomItem(final String roomID, final String itemNum);

	/**
	 * Table category: DBMAP
	 * Returns a sterile prototype of the given room mob
	 * freshly created.  It must exist in a room.
	 *
	 * @param roomID the room id
	 * @param mobID the mob database id code
	 * @return the mob object
	 */
	public MOB DBReadRoomMOB(final String roomID, final String mobID);

	/**
	 * Table category: DBMAP
	 * Reads the exits of the room with the given room id
	 * and populates them into the given room object. It
	 * also connects each exit to the room if it can
	 * get to it through the given rooms area object.
	 *
	 * @param roomID the room id
	 * @param room the room object to populate
	 * @param reportStatus true to populate global status, false otherwise
	 */
	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * Reads the exits of the room with the given room id
	 * and populates them into the pair array, indexed by
	 * direction.  The pair is exit class id, next room id
	 *
	 * @param roomID the room id
	 * @return the pair of exit class id, next room id, indexed by direction
	 */
	public Pair<String,String>[] DBReadRoomExitIDs(final String roomID);

	/**
	 * Table category: DBMAP
	 * Resaves the given rooms exit objects, and
	 * linkages to other rooms.
	 * @param room the room whose exits to save
	 */
	public void DBUpdateExits(Room room);

	/**
	 * Table category: DBMAP
	 * Inserts the given item into the given room id
	 * in the database, regardless of its worthiness to
	 * be there.
	 * @param roomID the room id to save the item at
	 * @param thisItem the item to save
	 */
	public void DBCreateThisItem(String roomID, Item thisItem);

	/**
	 * Table category: DBMAP
	 * Inserts the given mob into the given room id
	 * in the database, regardless of its worthiness to
	 * be there.
	 * @param roomID the room id to save the mob at
	 * @param thisMOB the mob to save
	 */
	public void DBCreateThisMOB(String roomID, MOB thisMOB);

	/**
	 * Table category: DBMAP
	 * Given a specific room id, and a specific mob id for
	 * that room (this is a unique coded string found only
	 * in the db, and loaded as the database id), this method
	 * returns the misctext for the mob.
	 * @see Environmental#text()
	 * @see MOB#databaseID()
	 * @param roomID the room id
	 * @param mobID the unique mob string for that room
	 * @return the misc text of the mob
	 */
	public String DBReadRoomMOBMiscText(String roomID, String mobID);

	/**
	 * Table category: DBMAP
	 * This method does a conventional boot load of all rooms and areas
	 * in the given set, including exits and content.  Thin areas
	 * are not loaded, as normal.  Logging and status are as boot.
	 * Rooms and Areas are added to the map.
	 * @param roomsToRead null to read all, or the set of rooms to read
	 */
	public void DBReadAllRooms(RoomnumberSet roomsToRead);

	/**
	 * Table category: DBMAP
	 * Deletes all mobs from the given room from the database and then
	 * adds the given mobs to the database.
	 * @see DatabaseEngine#DBUpdateMOBs(Room)
	 * @param room the savable room
	 * @param mobs the mobs in the room that need saving
	 */
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs);

	/**
	 * Table category: DBMAP
	 * Deletes all items from the given room from the database and then
	 * adds the given items to the database.
	 * @param room the savable room
	 * @param item the items in the room that need saving
	 */
	public void DBUpdateTheseItems(Room room, List<Item> item);

	/**
	 * Table category: DBMAP
	 * Deletes all mobs from the given room from the database and then
	 * adds all mobs currently in the room which are savable back to
	 * the database.
	 * @see DatabaseEngine#DBUpdateTheseMOBs(Room, List)
	 * @param room the savable room
	 */
	public void DBUpdateMOBs(Room room);

	/**
	 * Table category: DBMAP
	 * Creates the basic room object entry in the data.  Does not
	 * save content or exits, just the room object stuff.
	 * @param room the room to save
	 */
	public void DBCreateRoom(Room room);

	/**
	 * Table category: DBMAP
	 * Updates only the room object for the given room, not the
	 * items or content -- just the title, description, behaviors
	 * and properties, that sort of thing.  No exits either.
	 * Just the room.
	 * @param room the room that needs resaving.
	 */
	public void DBUpdateRoom(Room room);

	/**
	 * Table category: DBMAP
	 * Reads all the room numbers for the area with the given name from the
	 * database and returns a compressed roomnumberset object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.RoomnumberSet
	 * @param areaName the name of the area to load numbers from
	 * @param reportStatus true to update the global status, false otherwise
	 * @return the rooms in this area, as a compressed set
	 */
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus);

	/**
	 * Table category: DBMAP
	 * Assuming the given mob is savable, this method will update the
	 * database record for the given mob in the given room by deleting
	 * the mob from the room's database record and re-creating him in it.
	 * @param roomID the id of the room that the mob was in
	 * @param mob the mob to save
	 */
	public void DBUpdateMOB(String roomID, MOB mob);

	/**
	 * Table category: DBMAP
	 * Assuming the given item is savable, this method will update the
	 * database record for the given item in the given room by deleting
	 * the item from the room's database record and re-creating it in it.
	 * @param roomID the id of the room that the item was in
	 * @param item the item to save
	 */
	public void DBUpdateItem(String roomID, Item item);

	/**
	 * Table category: DBMAP
	 * Removes the given mob, and only the given mob, from the database
	 * records for the given room id.
	 * @param roomID the id of the room that the mob was in
	 * @param mob the mob to remove
	 */
	public void DBDeleteMOB(String roomID, MOB mob);

	/**
	 * Table category: DBMAP
	 * Removes the given item, and only the given item, from the database
	 * records for the given room id.
	 * @param roomID the id of the room that the item was in
	 * @param item the item to remove
	 */
	public void DBDeleteItem(String roomID, Item item);

	/**
	 * Table category: DBMAP
	 * Updates all of the savable items in the given room by
	 * removing all item records from the database and re-inserting
	 * all of the current item records back in.
	 * @param room the room to update
	 */
	public void DBUpdateItems(Room room);

	/**
	 * Table category: DBMAP
	 * Changes the room ID of the given room in the database
	 * by updating all of the records with the old room id
	 * to instead use the new room id of the room given.
	 * @param room the room with the new id
	 * @param oldID the old room id
	 */
	public void DBReCreate(Room room, String oldID);

	/**
	 * Table category: DBMAP
	 * Deletes the room and all of its exits and contents
	 * from the database entirely.
	 * @param room the room to blow away
	 */
	public void DBDeleteRoom(Room room);

	/**
	 * Table category: DBMAP
	 * Creates a new area record in the database.
	 * Only does the area record, not the rooms.
	 * @param A the area to create.
	 */
	public void DBCreateArea(Area A);

	/**
	 * Table category: DBMAP
	 * Removes the given area record from the database.
	 * Only does the area record, not the rooms.
	 * @param A the area to destroy.
	 */
	public void DBDeleteArea(Area A);

	/**
	 * Table category: DBMAP
	 * Removes the given area record from the database.
	 * Also removes all the proper rooms from the DB,
	 * along with exits, items, and characters in those
	 * rooms.
	 * @param A the area to destroy.
	 */
	public void DBDeleteAreaAndRooms(Area A);

	/**
	 * Table category: DBMAP
	 * Updates the area record in the database with the
	 * given areaID with the data from the given area
	 * object.  The areaID and the name of the area can
	 * be different for area renamings.
	 * @param areaID the current db area name
	 * @param A the area data to save
	 */
	public void DBUpdateArea(String areaID, Area A);

	/**
	 * Table category: DBPLAYERS
	 * Updates an existing account record in the database
	 * by altering its key fields in the existing record.
	 * @param account the account to update
	 */
	public void DBUpdateAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * Inserts a new row into the account record in the
	 * database.  Only works the first time for a given
	 * account name
	 * @param account the account to insert
	 */
	public void DBCreateAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * Removes only the given account from the database.
	 * Does not delete players or any player data.
	 * @param account the account to delete
	 */
	public void DBDeleteAccount(PlayerAccount account);

	/**
	 * Table category: DBPLAYERS
	 * Loads an account with the given name from the
	 * database, populates a playeraccount object,
	 * and returns it. Does not load players, only
	 * the account record.
	 * @param Login the name of the account to load
	 * @return the player account object
	 */
	public PlayerAccount DBReadAccount(String Login);

	/**
	 * Table category: DBPLAYERS
	 * Populates and returns a list of player account
	 * objects that match the given lowercase substring.
	 * Does this by doing a full scan. :/
	 * @param mask lowercase substring to search for or null
	 * @return the list of playeraccount objects
	 */
	public List<PlayerAccount> DBListAccounts(String mask);

	/**
	 * Table category: DBPLAYERS
	 * Scans the player pride stat xml and calls back your method
	 * on every chunk of player data found.  You should use this
	 * data to compile your top 10 pride stat indexes.
	 *
	 * The cpu percent is the percent (0-100) of each second of work
	 * to spend actually working.  The balance is spent sleeping.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat
	 * @param callBack a call back containing the user id and data for each period
	 * @param scanCPUPercent the percent (0-100) to spend working
	 * @return the arrays of lists of top winner players
	 */
	public void DBScanPridePlayerWinners(final CMCallback<Pair<ThinPlayer,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent);

	/**
	 * Table category: DBPLAYERS
	 * Scans the account pride stat xml and calls back your method
	 * on every chunk of account data found.  You should use this
	 * data to compile your top 10 pride stat indexes.
	 *
	 * The cpu percent is the percent (0-100) of each second of work
	 * to spend actually working.  The balance is spent sleeping.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat
	 * @param callBack a call back containing the user id and data for each period
	 * @param scanCPUPercent the percent (0-100) to spend working
	 * @return the arrays of lists of top winner accounts
	 */
	public void DBScanPrideAccountWinners(final CMCallback<Pair<String,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent);

	/**
	 * Table category: DBPLAYERS
	 * Does a complete update of the given player mob,
	 * including their items, abilities, and account.
	 * Followers are not included.
	 * @param mob the player mob to update
	 */
	public void DBUpdatePlayer(MOB mob);

	/**
	 * Changes all player start from from the old to new room id, where
	 * applicable.  This is called when a room's id number is changed.
	 *
	 * @param oldID the old room id
	 * @param newID the new room id
	 */
	public void DBUpdatePlayerStartRooms(final String oldID, final String newID);

	/**
	 * Table category: DBPLAYERS
	 * If this system uses the character expiration system, then
	 * this method will scan all the players for expired characters,
	 * and return the list of names, or an empty list if there are
	 * none.  The skipNames is a list of protected names that are
	 * never expired.
	 * @param skipNames the names to never expire, or null
	 * @return the list of expired names, or an empty list
	 */
	public List<String> DBExpiredCharNameSearch(Set<String> skipNames);

	/**
	 * Table category: DBPLAYERS
	 * Updates only the player stats record for the given player.
	 * These are player-unique variables, such as the prompt.
	 * @param mob the player to update
	 */
	public void DBUpdatePlayerPlayerStats(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Updates only the base mob data in the database for
	 * the given player.  This includes playerstat data
	 * and clan affiliation records, but not items, or
	 * abilities, or other such stuff.
	 * @param mob the player mob to update
	 */
	public void DBUpdatePlayerMOBOnly(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Updates only the ability records in the database
	 * for the given player.  This also includes effects,
	 * behaviors, and scripts on players, but nothing else.
	 * @param mob the player to update
	 */
	public void DBUpdatePlayerAbilities(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Updates only the item/inventory/equipment records
	 * in the database for the given player.  Nothing else.
	 * @param mob the player to update
	 */
	public void DBUpdatePlayerItems(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * This method deletes and re-saves the non-player
	 * npc followers of the given player mob.
	 * @param mob the mob whose followers to save
	 */
	public void DBUpdateFollowers(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Reads and populates a player MOB object
	 * from the database. Does not include
	 * followers, but does items and abilities.
	 * @param name the name of the player
	 * @return the player mob object, or null
	 */
	public MOB DBReadPlayer(String name);

	/**
	 * Table category: DBPLAYERS
	 * Returns only the attributes bitmap for the given
	 * player name
	 * @param name the name of the player
	 * @return the attributes bitmap
	 */
	public int DBReadPlayerBitmap(String name);

	/**
	 * Table category: DBPLAYERS
	 * Returns only list of clan names and roles this player belongs to
	 * player name
	 * @param name the player to read clan info for
	 * @return the list of clan names and roles
	 */
	public PairList<String,Integer> DBReadPlayerClans(String name);

	/**
	 * Table category: DBPLAYERS
	 * Returns a specific piece of player data from the database.
	 * @see PlayerLibrary.PlayerCode
	 * @param name the player to read info for
	 * @param code the piece of data to return
	 * @return the specific data, usually a string, Integer, or list of pairs
	 */
	public Object DBReadPlayerValue(final String name, final PlayerCode code);

	/**
	 * Table category: DBPLAYERS
	 * Sets a specific piece of player data in the database.
	 * @see PlayerLibrary.PlayerCode
	 * @param name the player to set info for
	 * @param code the piece of data to set
	 * @param value the specific data, usually a string, Integer, or list of pairs
	 */
	public void DBSetPlayerValue(final String name, final PlayerCode code, final Object value);

	/**
	 * Table category: DBPLAYERS
	 * Reads the item dbid, class and misc text of each item in the given
	 * players inventory, along with all 9 fields a growing item needs.
	 * @param name the name of the player
	 * @param classLocFilter null, or a filter for the itemid and/or location string
	 * @param textFilter null, or a filter for the misctext
	 * @return the list of matching items
	 */
	public List<String[]> DBReadPlayerItemData(final String name, final Filterer<Pair<String,String>> classLocFilter, final Filterer<String> textFilter);

	/**
	 * Table category: DBPLAYERS
	 * Renames all player records belonging to the old
	 * name to the new name.  Does nothing to existing
	 * cached objects, and the new name better not already
	 * exist!
	 * @param oldName the previous existing name in the db
	 * @param newName the new name to change them to
	 */
	public void DBPlayerNameChange(String oldName, String newName);

	/**
	 * Table category: DBPLAYERS
	 * Updates the email address of the given player in the
	 * database.  Does not update the account system.
	 * @param mob the mob containing the email addy to change
	 */
	public void DBUpdateEmail(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Updates the password of the given player in the
	 * database.  Does not update the account system.
	 * @param name the mob name containing the pw to change
	 * @param password the new password string
	 */
	public void DBUpdatePassword(String name, String password);

	/**
	 * Table category: DBPLAYERS
	 * Returns the email address and autoforward flag for the
	 * player with the given name.  Does not check accounts.
	 * Does, however, check the cache, so its not necessarily
	 * a db check.
	 * @param name the name of the player to get info for
	 * @return the email address and autoforward flag
	 */
	public Pair<String, Boolean> DBFetchEmailData(String name);

	/**
	 * Table category: DBPLAYERS
	 * Returns the name of the player with the given email
	 * address.
	 * @param email the email address to look for
	 * @return the name of the player, or null if not found
	 */
	public String DBPlayerEmailSearch(String email);

	/**
	 * Table category: DBPLAYERS
	 * Returns the list of all characters as thinplayer
	 * objects.  This is the whole bloody list.
	 * @see PlayerLibrary.ThinPlayer
	 * @return the list of all players
	 */
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList();

	/**
	 * Table category: DBPLAYERS
	 * Returns a ThinPlayer object with information about the
	 * character with the given name.
	 * @see PlayerLibrary.ThinPlayer
	 * @param name the name of the character to return.
	 * @return the thin player of this user, or null if not found
	 */
	public PlayerLibrary.ThinPlayer getThinUser(String name);

	/**
	 * Table category: DBPLAYERS
	 * Returns a list of all the characters in the database.
	 * Each and every one.
	 * @return the list of all character names
	 */
	public List<String> getUserList();

	/**
	 * Table category: DBPLAYERS
	 * Queries and creates mob objects for all the followers of
	 * the player with the given name and returns the list.
	 * It does not bring them to life, add to to any mob,
	 * or start them ticking.  It just makes and returns the
	 * mob objects.
	 * @see DatabaseEngine#DBReadFollowers(MOB, boolean)
	 * @param mobName the name of the mob to return
	 * @return the list of follower mob objects
	 */
	public List<MOB> DBScanFollowers(String mobName);

	/**
	 * Table category: DBPLAYERS
	 * Loads the followers of the given mob and optionally
	 * brings them to life. This will query and create the
	 * follower mob objects, add them to the given mob
	 * at the very least.
	 * @see DatabaseEngine#DBScanFollowers(String)
	 * @param mob the mob whose players to load
	 * @param bringToLife true to bring them to life, false not
	 */
	public void DBReadFollowers(MOB mob, boolean bringToLife);

	/**
	 * Table category: DBPLAYERS
	 * Loads the player stats object for the given mob.  This
	 * will be a new instance, unconnected to any given online
	 * player, and is for reference or searching only.
	 *
	 * @see DatabaseEngine#DBSearchPFIL(String)
	 *
	 * @param name the name of the player
	 * @return the player stats object, or null.
	 */
	public PlayerStats DBLoadPlayerStats(String name);

	/**
	 * Table category: DBPLAYERS
	 * Searches all player PFIL records for the given match.
	 * Returns the names, and last login dates from the DB.
	 *
	 * @param match the substring to find
	 * @return the list of names and login dates
	 */
	public PairList<String, Long> DBSearchPFIL(String match);

	/**
	 * Table category: DBPLAYERS
	 * Removes the character and clan affiliation records
	 * from the database and nothing else.  Not abilities, or
	 * items, or followers, or anything else... there are
	 * other methods for that.  This just does in the char
	 * record and clan affiliation.
	 * @param mobName the mob to delete
	 */
	public void DBDeletePlayerOnly(String mobName);

	/**
	 * Table category: DBPLAYERS
	 * Creates the character record for the given mob in
	 * the CMCHAR table in the database, and updates
	 * the account record if any.  Does not handle items,
	 * followers, abilities, or anything else -- just the
	 * base character record.
	 * @param mob the character to create
	 */
	public void DBCreateCharacter(MOB mob);

	/**
	 * Table category: DBPLAYERS
	 * Attempts to return an extremely thin player record by
	 * searching the database for a character with the exact given
	 * name.
	 * @param Login the name to look for
	 * @return null if not found, or a thinner player record
	 */
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login);

	/**
	 * Table category: DBPLAYERS
	 * Returns the leige for the given player
	 *
	 * @param Login the player for whom the leige is needed
	 * @return the name of the leige, or "", or null
	 */
	public String DBLeigeSearch(final String Login);

	/**
	 * Table category: DBPLAYERS
	 * Attempts to return a list of all characters who are
	 * listed as vassals of the character with the given exact
	 * name.  Vassals are characters that are SERVEing another
	 * player.
	 * @param liegeID the character name who would be the liege
	 * @return a list containing as many thin vassal records
	 */
	public List<PlayerLibrary.ThinPlayer> vassals(String liegeID);

	/**
	 * Table category: DBPLAYERS
	 * Attempts to return a list of all characters who are
	 * listed as worshippers of the deity with the given exact
	 * name.
	 * @param deityID the exact name of the deity to look for
	 * @return a list containing as many thin worshipper records
	 */
	public List<PlayerLibrary.ThinPlayer> worshippers(String deityID);

	/**
	 * Table category: DBQUEST
	 * Updates the entire complete quest list by deleting all quests
	 * from the database and re-inserting the given list of quests.
	 * @see Quest
	 * @see DatabaseEngine#DBUpdateQuest(Quest)
	 * @see DatabaseEngine#DBReadQuests()
	 * @param quests the list of quests to end up with
	 */
	public void DBUpdateQuests(List<Quest> quests);

	/**
	 * Table category: DBQUEST
	 * Updates the given quest in the database by deleting
	 * the old one and re-inserting this one.
	 * @see DatabaseEngine#DBUpdateQuests(List)
	 * @see DatabaseEngine#DBReadQuests()
	 * @see Quest
	 * @param Q the quest to update
	 */
	public void DBUpdateQuest(Quest Q);

	/**
	 * Table category: DBQUEST
	 * Reads all the Quest objects from the database and
	 * returns them as a list.  The Quests are pre-parsed
	 * and ready to go.
	 * @see DatabaseEngine#DBUpdateQuests(List)
	 * @see DatabaseEngine#DBUpdateQuest(Quest)
	 * @see Quest
	 * @return the list of quests
	 */
	public List<Quest> DBReadQuests();

	/**
	 * Table category: DBCLANS
	 * Given an exact clan name, this method returns the
	 * entire membership as MemberRecords.
	 * @see MemberRecord
	 * @see DatabaseEngine#DBGetClanMember(String, String)
	 * @see DatabaseEngine#DBUpdateClanMembership(String, String, int)
	 * @see DatabaseEngine#DBUpdateClanKills(String, String, int, int)
	 * @param clan the name of the clan to read members for
	 * @return the list of all the clans members
	 */
	public List<MemberRecord> DBReadClanMembers(String clan);

	/**
	 * Table category: DBCLANS
	 * Given a user, this will return the clans that user
	 * belongs to.  You an then lookup the clan and get their
	 * member records.
	 *
	 * @see DatabaseEngine#DBGetClanMember(String, String)
	 * @see DatabaseEngine#DBUpdateClanMembership(String, String, int)
	 * @see DatabaseEngine#DBUpdateClanKills(String, String, int, int)
	 * @param clan the name of the clan to read members for
	 * @return the list of all the members clans
	 */
	public List<String> DBReadMemberClans(String userID);

	/**
	 * Table category: DBCLANS
	 * Reads information about a single clan member of the given exact
	 * name from the clan of the given exact name.
	 * @see MemberRecord
	 * @see DatabaseEngine#DBReadClanMembers(String)
	 * @see DatabaseEngine#DBUpdateClanMembership(String, String, int)
	 * @see DatabaseEngine#DBUpdateClanKills(String, String, int, int)
	 * @param clan the name of the clan to read a member for
	 * @param name the name of the member to read in
	 * @return the member record, or null
	 */
	public MemberRecord DBGetClanMember(String clan, String name);

	/**
	 * Table category: DBCLANS
	 * Updates the Role of the clan member of the given exact name
	 * for the given exact clan.
	 * @see DatabaseEngine#DBReadClanMembers(String)
	 * @see DatabaseEngine#DBGetClanMember(String, String)
	 * @see DatabaseEngine#DBUpdateClanKills(String, String, int, int)
	 * @param name the name of the member to update
	 * @param clan the name of the clan to update a member for
	 * @param role the new role constant
	 */
	public void DBUpdateClanMembership(String name, String clan, int role);

	/**
	 * Table category: DBCLANS
	 * Updates the clan-kill counts for the clan member of the given
	 * exact name for the given exact clan
	 * @param clan the name of the clan to update a member for
	 * @param name the name of the member to update
	 * @param adjMobKills the number of ADDITIONAL (plus or minus) clan mob kills
	 * @param adjPlayerKills the number of ADDITIONAL (plus or minus) clan pvp kills
	 */
	public void DBUpdateClanKills(String clan, String name, int adjMobKills, int adjPlayerKills);

	/**
	 * Table category: DBCLANS
	 * Updates the clan-donation counts for the clan member of the given
	 * exact name for the given exact clan
	 * @param clan the name of the clan to update a member for
	 * @param name the name of the member to update
	 * @param adjGold the number of ADDITIONAL (plus or minus) clan gold donations
	 * @param adjXP the number of ADDITIONAL (plus or minus) clan xp adjustments
	 * @param adjDues the number of ADDITIONAL (plus or minus) clan dues made
	 */
	public void DBUpdateClanDonates(String clan, String name, double adjGold, int adjXP, double adjDues);

	/**
	 * Table category: DBCLANS
	 * Reads the entire list of clans, not including their stored items.
	 * The list of clans is then returned for adding to the official
	 * list, or whatever.
	 * @see Clan
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBUpdateClan(Clan)
	 * @see DatabaseEngine#DBUpdateClanItems(Clan)
	 * @see DatabaseEngine#DBDeleteClan(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBCreateClan(Clan)
	 * @return the official list of clan objects
	 */
	public List<Clan> DBReadAllClans();

	/**
	 * Table category: DBCLANS
	 * Reads the entire list of clan items, and uses the map
	 * to get the clan object, and then adds the given item
	 * to both the clan object and  to the World.
	 * @see Clan
	 * @see DatabaseEngine#DBReadAllClans()
	 * @see DatabaseEngine#DBUpdateClan(Clan)
	 * @see DatabaseEngine#DBUpdateClanItems(Clan)
	 * @see DatabaseEngine#DBDeleteClan(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBCreateClan(Clan)
	 * @param clans the map of clanids to clan objects
	 * @return number of items loaded overall
	 */
	public int DBReadClanItems(Map<String,Clan> clans);

	/**
	 * Table category: DBCLANS
	 * Updates the given clan objects record in the database.
	 * Does not update the clan items, however.
	 * @see Clan
	 * @see DatabaseEngine#DBReadAllClans()
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBUpdateClanItems(Clan)
	 * @see DatabaseEngine#DBDeleteClan(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBCreateClan(Clan)
	 * @param C the clan to update
	 */
	public void DBUpdateClan(Clan C);

	/**
	 * Table category: DBCLANS
	 * Updates the external clan items in the database
	 * for the given clan by deleting all the records
	 * and re-inserting them.
	 * @see Clan
	 * @see DatabaseEngine#DBReadAllClans()
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBUpdateClan(Clan)
	 * @see DatabaseEngine#DBDeleteClan(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBCreateClan(Clan)
	 * @param C the clan whose items to update
	 */
	public void DBUpdateClanItems(Clan C);

	/**
	 * Table category: DBCLANS
	 * Removes the given clan, all of its items and records,
	 * membership, and everything about it from the database.
	 * @see Clan
	 * @see DatabaseEngine#DBReadAllClans()
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBUpdateClan(Clan)
	 * @see DatabaseEngine#DBUpdateClanItems(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBCreateClan(Clan)
	 * @param C the clan to delete
	 */
	public void DBDeleteClan(Clan C);

	/**
	 * Table category: DBCLANS
	 * Creates the given clan in the database.
	 * @see Clan
	 * @see DatabaseEngine#DBReadAllClans()
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBUpdateClan(Clan)
	 * @see DatabaseEngine#DBUpdateClanItems(Clan)
	 * @see DatabaseEngine#DBReadClanItems(Map)
	 * @see DatabaseEngine#DBDeleteClan(Clan)
	 * @param C the clan to create
	 */
	public void DBCreateClan(Clan C);

	/**
	 * Table category: DBJOURNALS
	 * Returns the list of every journalID in the database that
	 * has at least one message, or an intro.
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @return the list of every journal ID
	 */
	public List<String> DBReadJournals();

	/**
	 * Table category: DBJOURNALS
	 * Updates the entire database record for the given journal
	 * entry, which must have already been created.
	 * @see JournalEntry
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @param journalID the name/id of the journal
	 * @param entry the complete entry record
	 */
	public void DBUpdateJournal(String journalID, JournalEntry entry);

	/**
	 * Table category: DBJOURNALS
	 * Reads an individual message from the given journal by its
	 * message key.
	 * @see JournalEntry
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @param journalID the name/id of the journal
	 * @param messageKey the message key
	 * @return the complete journal entry, or null if it wasn't found
	 */
	public JournalEntry DBReadJournalEntry(String journalID, String messageKey);

	/**
	 * Table category: DBJOURNALS
	 * Creates a new entry in the journal.  If the entries key already
	 * exists, this will make an error.  If the entries key is null, however,
	 * it will generate a new one.
	 * @see JournalEntry
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @param journalID the name/id of the journal
	 * @param entry the enttry to create
	 * @return the new entry key or null
	 */
	public String DBWriteJournal(String journalID, JournalEntry entry);

	/**
	 * Table category: DBJOURNALS
	 * Creates a new entry in this journal.  Will generate a new key.
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @param journalID the name/id of the journal
	 * @param from the author of the message
	 * @param to who the message is to, such as ALL
	 * @param subject the subject of the message
	 * @param message the message to write
	 * @return the new entry key or null
	 */
	public String DBWriteJournal(String journalID, String from, String to, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * Deletes enter a specific message, or all messages, from the given
	 * journal.  Deleting all messages deletes the journal.  Take care,
	 * because of the null thing, this method is dangerous. :)
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, String, String, long)
	 * @param journalID the name/id of the journal
	 * @param msgKeyOrNull the message key, or null to delete ALL messages
	 */
	public void DBDeleteJournal(String journalID, String msgKeyOrNull);

	/**
	 * Table category: DBJOURNALS
	 * Updates the existing journal message record in the database.
	 * Nothing is optional, it updates all of the given fields.
	 * @see DatabaseEngine#DBReadJournals()
	 * @see DatabaseEngine#DBReadJournalEntry(String, String)
	 * @see DatabaseEngine#DBUpdateJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, JournalEntry)
	 * @see DatabaseEngine#DBWriteJournal(String, String, String, String, String)
	 * @see DatabaseEngine#DBDeleteJournal(String, String)
	 * @param messageKey the unique message key
	 * @param subject the new message subject
	 * @param msg the new message text
	 * @param newAttributes the new message attributes bitmap
	 */
	public void DBUpdateJournal(String messageKey, String subject, String msg, long newAttributes);

	/**
	 * Table category: DBJOURNALS
	 * Writes a new journal entry formatted for the email system and generates
	 * a new message key.
	 * @see com.planet_ink.coffee_mud.core.CMProps.Str#MAILBOX
	 * @param mailBoxID the journal name/id of the email system MAILBOX
	 * @param journalSource the name/id of the journal that originated the message
	 * @param from who the author of the email
	 * @param to the recipient
	 * @param subject the subject of the message
	 * @param message the email message
	 * @return the new entry key or null
	 */
	public String DBWriteJournalEmail(String mailBoxID, String journalSource, String from, String to, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * Adds to an existing journal entry by taking on a fake/stupid reply as a text
	 * appendage to the main message.  I should do something better some day, and
	 * I did to forums right, but, well, that's how these still are, just like Zelch 64 1.0.
	 * @param journalID the name/id of the journal
	 * @param messageKey the key of the message to append to
	 * @param from who the Reply is from
	 * @param to who the recipient of the reply is
	 * @param subject the subject of the reply
	 * @param message the reply text
	 * @return the updated journal entry
	 */
	public JournalEntry DBWriteJournalReply(String journalID, String messageKey, String from, String to, String subject, String message);

	/**
	 * Deletes all messages in the given journal from the given source.
	 *
	 * @param journal the journal
	 * @param from the source
	 */
	public void DBDeleteJournalMessagesByFrom(String journal, String from);

	/**
	 * Table category: DBJOURNALS
	 * Searches all the messages in the journal for the search string as
	 * a (typically) case-insensitive substring of either the subject
	 * or the message text.  Returns up to 100 journal entries that
	 * form a match.
	 * @param journalID the name/id of the journal to search
	 * @param searchStr the search substring
	 * @return the list of journal entries that match
	 */
	public List<JournalEntry> DBSearchAllJournalEntries(String journalID, String searchStr);

	/**
	 * Table category: DBJOURNALS
	 * For forum journals, updates the number of replies registered with the
	 * parent message represented by the given message Key.
	 * @see DatabaseEngine#DBReadJournalMetaData(String, com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData)
	 * @see DatabaseEngine#DBUpdateJournalMetaData(String, com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData)
	 * @param messageKey the key of the parent op message
	 * @param numReplies the new number of replies to register
	 */
	public void DBUpdateMessageReplies(String messageKey, int numReplies);

	/**
	 * Table category: DBJOURNALS
	 * Takes an empty JournalMetaData object, and the journal NAME
	 *  and fills in the rest by querying the database.
	 * @see DatabaseEngine#DBUpdateMessageReplies(String, int)
	 * @see DatabaseEngine#DBUpdateJournalMetaData(String, com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData)
	 * @param journalID the name of the journals whose stats to update
	 * @param metaData the created metadata object to fill in
	 */
	public void DBReadJournalMetaData(String journalID, JournalsLibrary.JournalMetaData metaData);

	/**
	 * Table category: DBJOURNALS
	 * Primarily for forum journals, this method updates all of the given
	 * meta data, such as the intro, and so forth by deleting the old
	 * record and re-inserting it into the database.
	 * @see DatabaseEngine#DBReadJournalMetaData(String, com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalMetaData)
	 * @see DatabaseEngine#DBUpdateMessageReplies(String, int)
	 * @param journalID the name of the journals whose stats to update
	 * @param metaData the metadata to resave into the database
	 */
	public void DBUpdateJournalMetaData(String journalID, JournalsLibrary.JournalMetaData metaData);

	/**
	 * Table category: DBJOURNALS
	 * Returns a window of messages from the given journal, either primary messages or replies
	 * to messages, sorted by date, that matches a search, and can be limited, and older (or newer)
	 * than a given timestamp.
	 * @param journalID the name of the journal/forum to load from
	 * @param parent the parent message (for getting replies), or null
	 * @param searchStr the string to search for in the msg/subject, or null
	 * @param newerDate 0 for all real msgs, parent for newer than timestamp, otherwise before timestamp
	 * @param limit the maximum number of messages to return
	 * @return the journal entries/messages that match this query
	 */
	public List<JournalEntry> DBReadJournalPageMsgs(String journalID, String parent, String searchStr, long newerDate, int limit);

	/**
	 * Table category: DBJOURNALS
	 * Returns the timestamp of the message at the top of every page, given the limit.
	 *
	 * @param journalID the name of the journal/forum to load from
	 * @param parent the parent message (for getting replies), or null
	 * @param searchStr the string to search for in the msg/subject, or null
	 * @param limit the maximum number of messages to return
	 * @return the list of timestamps for each page
	 */
	public List<Long> DBReadJournalPages(String journalID, String parent, String searchStr, int limit);

	/**
	 * Table category: DBJOURNALS
	 * Returns all the messages in the given journal, optionally sorted by update date, ascending.
	 * This is a legacy method, and should be used with care.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @return the list of all the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending);

	/**
	 * Table category: DBJOURNALS
	 * Returns a limited number of messages from the given journal, optional sorted by update date,
	 * ascending.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @param limit the number of messages to return, max
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending, int limit);

	/**
	 * Table category: DBJOURNALS
	 * Returns all messages from the given journal, in which the update time
	 * lies within the given ranges.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @see DatabaseEngine#DBReadJournalMsgsByTimeStamps(String, String, long, long)
	 * @see DatabaseEngine#DBReadJournalMsgsByExpiRange(String, String, long, long, String)
	 * @param journalID the journal to read all the messages from
	 * @param from the source of the entry or null or ''
	 * @param startRange the starting timestamp for the range of update times
	 * @param endRange the ending timestamp for the range of update times
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByUpdateRange(String journalID, final String from, long startRange, long endRange);

	/**
	 * Table category: DBJOURNALS
	 * Returns all messages from the given journal, in which the expiration time
	 * lies within the given ranges.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @see DatabaseEngine#DBReadJournalMsgsByTimeStamps(String, String, long, long)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateRange(String, String, long, long)
	 * @param journalID the journal to read all the messages from
	 * @param from the source of the entry or null or ''
	 * @param startRange the starting timestamp for the range of update times
	 * @param endRange the ending timestamp for the range of update times
	 * @param searchStr null, or a search str to match in the DATA (metadata)
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByExpiRange(String journalID, final String from, long startRange, long endRange, String searchStr);

	/**
	 * Table category: DBJOURNALS
	 * Returns all messages from the given journal, in which the expiration time
	 * is after the given range, and the date string contains the given search term.
	 * !!NOTE!! - this method does not correct/fix update timestamp issues!
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @see DatabaseEngine#DBReadJournalMsgsByTimeStamps(String, String, long, long)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateRange(String, String, long, long)
	 * @param journalID the journal to read all the messages from
	 * @param startRange the starting timestamp for the range of update times
	 * @param searchStr null, or a search str to match in the DATA (metadata)
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadAllJournalMsgsByExpiDateStr(final String journalID, final long startRange, final String searchStr);

	/**
	 * Table category: DBJOURNALS
	 * Returns all messages from the given journal, in which either the update time
	 * starts within the given ranges or the expiration time ends within it.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateRange(String, String, long, long)
	 * @see DatabaseEngine#DBReadJournalMsgsByExpiRange(String, String, long, long, String)
	 * @param journalID the journal to read all the messages from
	 * @param from the source of the entry or null or ''
	 * @param startRange the starting timestamp for the range of times
	 * @param endRange the ending timestamp for the range of times
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByTimeStamps(String journalID, final String from, long startRange, long endRange);

	/**
	 * Table category: DBJOURNALS
	 * Returns a limited number of messages from the given journal, optionally sorted by update date,
	 * ascending, but only those marked as TO the given string array.
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean)
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @param limit the number of messages to return, max
	 * @param tos return only messages marked TO one of the names in this array
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending, int limit, String[] tos);

	/**
	 * Table category: DBJOURNALS
	 * Returns all the messages in the given journal, optionally sorted by create date, ascending.
	 * This is a legacy method, and should be used with care.
	 * @see DatabaseEngine#DBReadJournalMsgsByCreateDate(String, boolean, int)
	 * @see DatabaseEngine#DBReadJournalMsgsByCreateDate(String, boolean, int, String[])
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @return the list of all the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending);

	/**
	 * Table category: DBJOURNALS
	 * Returns a limited number of messages from the given journal, optional sorted by create date,
	 * ascending.
	 * @see DatabaseEngine#DBReadJournalMsgsByCreateDate(String, boolean)
	 * @see DatabaseEngine#DBReadJournalMsgsByUpdateDate(String, boolean, int, String[])
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @param limit the number of messages to return, max
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending, int limit);

	/**
	 * Table category: DBJOURNALS
	 * Returns a limited number of messages from the given journal, optionally sorted by create date,
	 * ascending, but only those marked as TO the given string array.
	 * @see DatabaseEngine#DBReadJournalMsgsByCreateDate(String, boolean, int)
	 * @see DatabaseEngine#DBReadJournalMsgsByCreateDate(String, boolean)
	 * @param journalID the journal to read all the messages from
	 * @param ascending true to order the read entries by date
	 * @param limit the number of messages to return, max
	 * @param tos return only messages marked TO one of the names in this array
	 * @return the list of the messages in this journal, an empty list, or null on error
	 */
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending, int limit, String[] tos);

	/**
	 * Table category: DBJOURNALS
	 * Returns all the messages optionally sent to the given user (or ALL) that
	 * are newer than the given date.
	 * @see DatabaseEngine#DBReadJournalMsgsOlderThan(String, String, long)
	 * @param journalID the name/ID of the journal/forum
	 * @param to NULL, ALL, or a user the messages were sent to
	 * @param olderDate the date beyond which to return messages for
	 * @return the list of messages that were found
	 */
	public List<JournalEntry> DBReadJournalMsgsNewerThan(String journalID, String to, long olderDate);

	/**
	 * Table category: DBJOURNALS
	 * Counts all the messages optionally sent to the given user (or ALL) that
	 * are newer than the given date.
	 * @see DatabaseEngine#DBReadJournalMsgsOlderThan(String, String, long)
	 * @param journalID the name/ID of the journal/forum
	 * @param to NULL, ALL, or a user the messages were sent to
	 * @param olderDate the date beyond which to return messages for
	 * @return the count of messages that were found
	 */
	public int DBCountJournalMsgsNewerThan(final String journalID, final String to, final long olderDate);

	/**
	 * Table category: DBJOURNALS
	 * Returns all the messages optionally sent to the given user (or ALL) that
	 * are older than the given date.
	 * @see DatabaseEngine#DBReadJournalMsgsNewerThan(String, String, long)
	 * @see DatabaseEngine#DBReadJournalMsgsExpiredBefore(String, String, long)
	 * @param journalID the name/ID of the journal/forum
	 * @param to NULL, ALL, or a user the messages were sent to
	 * @param newestDate the date before which to return messages for
	 * @return the list of messages that were found
	 */
	public List<JournalEntry> DBReadJournalMsgsOlderThan(String journalID, String to, long newestDate);

	/**
	 * Table category: DBJOURNALS
	 * Returns all the messages optionally sent to the given user (or ALL) that
	 * are expired before the given date.
	 * @see DatabaseEngine#DBReadJournalMsgsNewerThan(String, String, long)
	 * @see DatabaseEngine#DBReadJournalMsgsOlderThan(String, String, long)
	 * @param journalID the name/ID of the journal/forum
	 * @param to NULL, ALL, or a user the messages were sent to
	 * @param newestDate the date before which to return messages for
	 * @return the list of messages that were found
	 */
	public List<JournalEntry> DBReadJournalMsgsExpiredBefore(final String journalID, final String to, final long newestDate);

	/**
	 * Table category: DBJOURNALS
	 * Returns the number of messages found in the given journal optionally
	 * sent from the given user name, and/or optionally to the given user
	 * name.
	 * @param journalID the name/id of the journal
	 * @param from NULL, or the user id of a user the messages are from
	 * @param to NULL, or the user id of a user the messages are to
	 * @return the number of messages found
	 */
	public int DBCountJournal(String journalID, String from, String to);

	/**
	 * Table category: DBJOURNALS
	 * Writes a new journal message/entry to the database.  One which is
	 * probably a reply to a parent message, denoted by the parentKey,
	 * which is the message key of the parent.  This method also updates
	 * the number of replies being tracked for the given parent message.
	 *
	 * @param journalID the name/id of the journal/forum
	 * @param journalSource the originating name/id of an originating journal?
	 * @param from who the message is written by, a user id
	 * @param to who the message is to, or ALL
	 * @param parentKey the message key of the parent message this is a reply to
	 * @param subject the subject of the reply message
	 * @param message the message text of the reply message
	 * @return the new entry key or null
	 */
	public String DBWriteJournalChild(String journalID, String journalSource, String from, String to,
									  String parentKey, String subject, String message);

	/**
	 * Table category: DBJOURNALS
	 * A silly function that queries the database for a journal of the given exact
	 * name, and if it is found, it returns it, otherwise it returns null.
	 * @param possibleName the possible name of a journal
	 * @return null, or the name returned
	 */
	public String DBGetRealJournalName(String possibleName);

	/**
	 * Table category: DBJOURNALS
	 * This method returns a two dimensional array, where the first long
	 * is the update timestamp of the latest message in the journal, optionally
	 * to the given recipient,  and the second long is the number of messages
	 * found that are newer than the given timestamp.
	 * @param journalID the journal id/name to search
	 * @param to NULL, or ALL, or the recipient name
	 * @param olderTime the time After which to count messages
	 * @return the array with latest update timestamp, and the number of newer msgs
	 */
	public long[] DBJournalLatestDateNewerThan(String journalID, String to, long olderTime);

	/**
	 * Table category: DBJOURNALS
	 * This message deletes all private messages with the given
	 * user id as a recipient.  This is all the messages TO the given
	 * user, which might include other kinds of user documents, such as
	 * email, bank accounts, mail, and the like.
	 * @param name the name of the user to delete journal entries from
	 */
	public void DBDeletePlayerPrivateJournalEntries(String name);

	/**
	 * Table category: DBJOURNALS
	 * Sets the number of views for the given specific journal message.
	 * @param messageKey the message key of the message to update
	 * @param views the number of views to mark.
	 */
	public void DBUpdateJournalMessageViews(String messageKey, int views);

	/**
	 * Table category: DBJOURNALS
	 * Updates the given journal message with an update time of
	 * the current date/timestamp.
	 * @param messageKey the message to touch.
	 */
	public void DBTouchJournalMessage(String messageKey);

	/**
	 * Table category: DBJOURNALS
	 * Updates the given journal message with a specific update
	 * time of the given date/timestamp
	 * @param messageKey the key of the message to touch
	 * @param newDate the date/time to set the message update time to
	 */
	public void DBTouchJournalMessage(String messageKey, long newDate);

	/**
	 * Table category: DBPLAYERDATA
	 * Just as it says, this method reads all player data, which is
	 * the only way to get all bank data at the same time, for example.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBDeleteAllPlayerData(String)
	 * @param playerID the user id of the player to read all data for
	 * @return all of this players data.
	 */
	public List<PlayerData> DBReadAllPlayerData(String playerID);

	/**
	 * Table category: DBPLAYERDATA
	 * Deletes all player data belonging to the player, of all
	 * types and sections, all over.  BOOM.
	 * @see DatabaseEngine#DBReadAllPlayerData(String)
	 * @param name the user id/name of the player to delete all data of
	 */
	public void DBDeleteAllPlayerData(String name);

	/**
	 * Table category: DBPLAYERDATA
	 * Read a specific set of data for the given player, belonging
	 * to the given section
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBCountPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 * @see DatabaseEngine#DBReadPlayerDataKeys(String, String)
	 * @param playerID the user id for the player to read data for
	 * @param section the section/type of data to read.
	 * @return the data for the player in the section
	 */
	public List<PlayerData> DBReadPlayerData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Read a specific set of keys for the given player, belonging
	 * to the given section
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBCountPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 * @param playerID the user id for the player to read data for
	 * @param section the section/type of data to read.
	 * @return the keys for the player in the section
	 */
	public List<String> DBReadPlayerDataKeys(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Counts the number of rows of data/entries
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 * @see DatabaseEngine#DBReadPlayerDataKeys(String, String)
	 * @param playerID the user id of the player to count the data of
	 * @param section the section/type of data to count
	 * @return the number of entries for the given player and section
	 */
	public int DBCountPlayerData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Counts the number of rows of data/entries per section.
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 * @see DatabaseEngine#DBReadPlayerDataKeys(String, String)
	 *
	 * @param section the cross-player section of data
	 * @return the number of entries for the given section
	 */
	public int DBCountPlayerData(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Returns the list of unique authors for a given section
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 *
	 * @param section the cross-player section of data
	 * @return the unique authors for a given section
	 */
	public List<String> DBReadPlayerDataAuthorsBySection(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Deletes all of the data for the given player of the
	 * given section/type.
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBCountPlayerData(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, List)
	 * @param playerID the user id of the player to delete the data of
	 * @param section the section/type of data to delete
	 */
	public void DBDeletePlayerData(String playerID, String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads in all data for the given player which also belongs to any
	 * one of the given sections/data types.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBCountPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBExistsPlayerData(String, String)
	 * @param player the user id of the player to delete the data of
	 * @param sections the sections/types of data to return records for
	 * @return the player data records that match the player and sections
	 */
	public List<PlayerData> DBReadPlayerData(String player, List<String> sections);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads in all unique sections with the given owner/name
	 *
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerData(String, String)
	 * @see DatabaseEngine#DBCountPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerData(String, String)
	 * @see DatabaseEngine#DBExistsPlayerData(String, String)
	 * @param name the user id of the player/clan/whatever
	 * @return the list of unique sections
	 */
	public Set<String> DBReadUniqueSections(String name);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads in all unique player names for all players for the given
	 * data type/section.
	 * @see DatabaseEngine#DBReadPlayerSectionData(String)
	 * @see DatabaseEngine#DBDeletePlayerSectionData(String)
	 * @see DatabaseEngine#DBExistsPlayerData(String, String)
	 * @param section the section to read player names for
	 * @return the list of all unique names in this section
	 */
	public List<String> DBReadPlayerDataPlayersBySection(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Check all unique player names for all players for the given
	 * name/section.
	 * @see DatabaseEngine#DBReadPlayerSectionData(String)
	 * @see DatabaseEngine#DBReadPlayerDataPlayersBySection(String)
	 * @see DatabaseEngine#DBDeletePlayerSectionData(String)
	 * @param section the section to read player names for
	 * @param name the name of the player
	 * @return true if the player data exists
	 */
	public boolean DBExistsPlayerData(final String section, final String name);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads in all player data for all players for the given
	 * data type/section. Use this sparingly!
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerDataPlayersBySection(String)
	 * @see DatabaseEngine#DBExistsPlayerData(String, String)
	 * @see DatabaseEngine#DBDeletePlayerSectionData(String)
	 * @param section the section, type of data to delete
	 * @return the player data in the given section for all players
	 */
	public List<PlayerData> DBReadPlayerSectionData(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Deletes all player data of the given section/type.
	 * @see DatabaseEngine#DBReadPlayerDataPlayersBySection(String)
	 * @see DatabaseEngine#DBReadPlayerSectionData(String)
	 * @param section the section, type of data to delete
	 */
	public void DBDeletePlayerSectionData(String section);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads what is probably a single player data entry, but could be more.
	 * All fields are required.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerDataByKeyMask(String, String)
	 * @see DatabaseEngine#DBReadPlayerDataEntry(String)
	 * @param playerID the name/userid of the player to read data for
	 * @param section the section/type of data to return
	 * @param key the key of the specific entry(s) to return
	 * @return the player data entry to return
	 */
	public List<PlayerData> DBReadPlayerData(String playerID, String section, String key);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads a list of data entries in a given section and selecting
	 * keys by a regular expression.  This is a scanning call by section.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerData(String, String, String)
	 * @see DatabaseEngine#DBReadPlayerDataEntry(String)
	 * @param section the section/type of data to return
	 * @param keyMask the regular expression to match against keys
	 * @return the player data entries returned.
	 */
	public List<PlayerData> DBReadPlayerDataByKeyMask(String section, String keyMask);

	/**
	 * Table category: DBPLAYERDATA
	 * Reads what is probably a single player data entry, but could be more?
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReadPlayerDataByKeyMask(String, String)
	 * @see DatabaseEngine#DBReadPlayerData(String, String, String)
	 * @param key the key of the specific entry(s) to return
	 * @return the player data entry to return
	 */
	public List<PlayerData> DBReadPlayerDataEntry(String key);

	/**
	 * Table category: DBPLAYERDATA
	 * Updates what is probably a single player data entry, but could be more,
	 * by setting the xml data itself for a given key.
	 * All fields are required.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBDeletePlayerData(String, String, String)
	 * @see DatabaseEngine#DBReCreatePlayerData(String, String, String, String)
	 * @param playerID the name/userid of the player to delete data for
	 * @param section the section/type of data to delete
	 * @param key the key of the specific entry(s) to update
	 * @param xml the new data to save for this entry.
	 */
	public void DBUpdatePlayerData(String playerID, String section, String key, String xml);

	/**
	 * Table category: DBPLAYERDATA
	 * Deletes what is probably a single player data entry, but could be more.
	 * All fields are required.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBUpdatePlayerData(String, String, String, String)
	 * @see DatabaseEngine#DBReCreatePlayerData(String, String, String, String)
	 * @param playerID the name/userid of the player to delete data for
	 * @param section the section/type of data to delete
	 * @param key the key of the specific entry(s) to delete
	 */
	public void DBDeletePlayerData(String playerID, String section, String key);

	/**
	 * Table category: DBPLAYERDATA
	 * Creates or Updates a single player data entry.  This is the same as an
	 * upsert where, if the key already exists, an update is done, otherwise
	 * an insert is done.
	 * All fields are required.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReCreatePlayerData(String, String, String, String)
	 * @see DatabaseEngine#DBCreatePlayerData(String, String, String, String)
	 *
	 * @param name the userid/player id of the entry to create/update
	 * @param section the section/type of data to create/update
	 * @param key the key of the specific entry(s) to create/update
	 * @param xml the new data to save for this entry.
	 * @return the filled in PlayerData record, for both create and update
	 */
	public PlayerData DBReCreatePlayerData(String name, String section, String key, String xml);

	/**
	 * Table category: DBPLAYERDATA
	 * Creates a single player data entry.
	 * All fields are required.
	 * @see DatabaseEngine.PlayerData
	 * @see DatabaseEngine#DBReCreatePlayerData(String, String, String, String)
	 * @see DatabaseEngine#DBCreatePlayerData(String, String, String, String)
	 *
	 * @param player the userid/player id of the entry to create
	 * @param section the section/type of data to create
	 * @param key the key of the specific entry(s) to create
	 * @param data the new xml to save for this entry.
	 * @return the filled in PlayerData record
	 */
	public PlayerData DBCreatePlayerData(String player, String section, String key, String data);

	/**
	 * Table category: DBPLAYERDATA
	 * Initializes the artifact ability tickers by reading them
	 * from the CMPDAT table, creating a Prop_Artifact ability,
	 * and setting it to start ticking.
	 */
	public void DBReadArtifacts();

	/**
	 * Table category: DBRACE
	 * Reads all records from the CMGRAC table and returns the
	 * AckRecord for all of them in a list, to do with as you please.
	 * These are the generic races.
	 *
	 * @see DatabaseEngine.AckRecord
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#DBDeleteRace(String)
	 * @see DatabaseEngine#DBCreateRace(String, String)
	 *
	 * @return the generic race records
	 */
	public List<AckRecord> DBReadRaces();

	/**
	 * Table category: DBRACE
	 * Returns whether the given race ID has expired and should
	 * be recreated at the next opportunity.
	 *
	 * @param raceID the race ID to check
	 * @see DatabaseEngine#DBReadRaces()
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#DBDeleteRace(String)
	 * @see DatabaseEngine#DBCreateRace(String, String)
	 * @return true if the race is expired
	 */
	public boolean isRaceExpired(final String raceID);

	/**
	 * Table category: DBRACE
	 * Updates the race creation timestamp, to prevent its re-creation.
	 *
	 * @param raceID the race ID to update
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBReadRaces()
	 * @see DatabaseEngine#DBDeleteRace(String)
	 * @see DatabaseEngine#DBCreateRace(String, String)
	 * @see DatabaseEngine#updateAllRaceDates()
	 * @see DatabaseEngine#pruneOldRaces()
	 */
	public void DBUpdateRaceCreationDate(final String raceID);

	/**
	 * Table category: DBRACE
	 * Registers a generic race as being used recently
	 *
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#updateAllRaceDates()
	 * @see DatabaseEngine#pruneOldRaces()
	 *
	 * @param R the race to update
	 */
	public void registerRaceUsed(final Race R);

	/**
	 * Table category: DBRACE
	 * Registers a generic race as being used recently
	 *
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#updateAllRaceDates()
	 *
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#registerRaceUsed(Race)
	 * @see DatabaseEngine#updateAllRaceDates()
	 *
	 * @return number of races pruned
	 */
	public int pruneOldRaces();

	/**
	 * Table category: DBRACE
	 * Updates all generic races used recently.
	 *
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#registerRaceUsed(Race)
	 * @see DatabaseEngine#pruneOldRaces()
	 *
	 * @return the number of generic races updated
	 */
	public int updateAllRaceDates();

	/**
	 * Table category: DBRACE
	 * Removes a generic race from the CMGRAC table.
	 * @see DatabaseEngine#DBReadRaces()
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#DBCreateRace(String, String)
	 *
	 * @param raceID the ID of the race to delete
	 */
	public void DBDeleteRace(String raceID);

	/**
	 * Table category: DBRACE
	 * Creates a new entry in the generic races (CMGRAC)
	 * table with the given unique ID and xml definition
	 * data.
	 * @see DatabaseEngine#DBReadRaces()
	 * @see DatabaseEngine#isRaceExpired(String)
	 * @see DatabaseEngine#DBUpdateRaceCreationDate(String)
	 * @see DatabaseEngine#DBDeleteRace(String)
	 * @param raceID the unique RaceID
	 * @param data the xml data defining the generic race
	 */
	public void DBCreateRace(String raceID, String data);

	/**
	 * Table category: DBCHARCLASS
	 * Reads all records from the CMCCAC table and returns the
	 * AckRecord for all of them in a list, to do with as you please.
	 * These are the generic charclasses.
	 *
	 * @see DatabaseEngine.AckRecord
	 * @see DatabaseEngine#DBDeleteClass(String)
	 * @see DatabaseEngine#DBCreateClass(String, String)
	 *
	 * @return the generic charclass records
	 */
	public List<AckRecord> DBReadClasses();

	/**
	 * Table category: DBCHARCLASS
	 * Removes a generic charclass from the CMCCAC table.
	 * @see DatabaseEngine#DBReadClasses()
	 * @see DatabaseEngine#DBCreateClass(String, String)
	 * @param classID the ID of the charclass to delete
	 */
	public void DBDeleteClass(String classID);

	/**
	 * Table category: DBCHARCLASS
	 * Creates a new entry in the generic charclasses (CMCCAC)
	 * table with the given unique ID and xml definition
	 * data.
	 * @see DatabaseEngine#DBReadClasses()
	 * @see DatabaseEngine#DBDeleteClass(String)
	 * @param classID the unique CharClassID
	 * @param data the xml data defining the generic charclass
	 */
	public void DBCreateClass(String classID,String data);

	/**
	 * Table category: DBABILITY
	 * Reads all records from the CMGAAC table and returns the
	 * AckRecord for all of them in a list, to do with as you please.
	 * These are the generic abilities.
	 *
	 * @see DatabaseEngine.AckRecord
	 * @see DatabaseEngine#DBDeleteAbility(String)
	 * @see DatabaseEngine#DBCreateAbility(String, String, String)
	 *
	 * @return the generic ability records
	 */
	public List<AckRecord> DBReadAbilities();

	/**
	 * Removes a generic ability from the CMGAAC table.
	 * @see DatabaseEngine#DBReadAbilities()
	 * @see DatabaseEngine#DBCreateAbility(String, String, String)
	 * @param classID the ID of the ability to delete
	 */
	public void DBDeleteAbility(String classID);

	/**
	 * Table category: DBABILITY
	 * Creates a new entry in the generic ability (CMGAAC)
	 * table with the given unique ID and xml definition
	 * data.
	 * @see DatabaseEngine#DBReadAbilities()
	 * @see DatabaseEngine#DBDeleteAbility(String)
	 * @param classID the unique AbilityID
	 * @param typeClass the Ability class ID to base off of (GenAbility, etc.)
	 * @param data the xml data defining the generic ability
	 */
	public void DBCreateAbility(String classID, String typeClass, String data);

	/**
	 * Table category: DBCOMMAND
	 * Reads all records from the CMCDAC table and returns the
	 * AckRecord for all of them in a list, to do with as you please.
	 * These are the generic commands.
	 *
	 * @see DatabaseEngine.AckRecord
	 * @see DatabaseEngine#DBDeleteCommand(String)
	 * @see DatabaseEngine#DBCreateCommand(String, String, String)
	 *
	 * @return the generic command records
	 */
	public List<AckRecord> DBReadCommands();

	/**
	 * Table category: DBCOMMAND
	 * Removes a generic command from the CMCDAC table.
	 * @see DatabaseEngine#DBReadCommands()
	 * @see DatabaseEngine#DBCreateCommand(String, String, String)
	 * @param classID the ID of the command to delete
	 */
	public AckRecord DBDeleteCommand(String classID);

	/**
	 * Table category: DBCOMMAND
	 * Creates a new entry in the generic command (CMCDAC)
	 * table with the given unique ID and xml definition
	 * data.
	 * @see DatabaseEngine#DBReadCommands()
	 * @see DatabaseEngine#DBDeleteCommand(String)
	 * @param classID the unique CommandID
	 * @param baseClassID the Command class ID to base off of
	 * @param data the xml data defining the generic command
	 */
	public void DBCreateCommand(String classID, String baseClassID, String data);

	/**
	 * Table category: DBSTATS
	 * Reads a days worth of stats from the CMSTAT table in
	 * the database.  Returning as a CofeeTableRow object
	 * populated with the days data. The start time has the
	 * correct date, and an hr/min/sec/ms of 0s.
	 *
	 * @see DatabaseEngine#DBUpdateStat(long, String)
	 * @see DatabaseEngine#DBDeleteStat(long)
	 * @see DatabaseEngine#DBCreateStat(long, long, String)
	 * @see DatabaseEngine#DBReadStats(long, long)
	 * @see CoffeeTableRow
	 * @param startTime the timestamp of the day start
	 * @return the row of data for that day.
	 */
	public CoffeeTableRow DBReadStat(long startTime);

	/**
	 * Table category: DBSTATS
	 * Deletes a days worth of stats from the CMSTAT table
	 * in the database.   The start time has the
	 * correct date, and an hr/min/sec/ms of 0s.
	 * @see DatabaseEngine#DBUpdateStat(long, String)
	 * @see DatabaseEngine#DBCreateStat(long, long, String)
	 * @see DatabaseEngine#DBReadStats(long, long)
	 * @see DatabaseEngine#DBReadStat(long)
	 *
	 * @param startTime the timestamp of the day to delete
	 */
	public void DBDeleteStat(long startTime);

	/**
	 * Table category: DBSTATS
	 * Creates a days worth of stats in the CMSTAT table
	 * in the database.   The start time has the
	 * correct date, and an hr/min/sec/ms of 0s. The
	 * end time is whatever.
	 * @see DatabaseEngine#DBUpdateStat(long, String)
	 * @see DatabaseEngine#DBDeleteStat(long)
	 * @see DatabaseEngine#DBReadStats(long, long)
	 * @see DatabaseEngine#DBReadStat(long)
	 *
	 * @param startTime the timestamp of the day start
	 * @param endTime the timestamp of the day end
	 * @param data the xml data to insert.
	 * @return true if insert was successful, false otherwise
	 */
	public boolean DBCreateStat(long startTime,long endTime,String data);

	/**
	 * Table category: DBSTATS
	 * Updates a single days worth of stats in the CMSTAT table
	 * in the database.   The start time has the
	 * correct date, and an hr/min/sec/ms of 0s.
	 * @see DatabaseEngine#DBDeleteStat(long)
	 * @see DatabaseEngine#DBCreateStat(long, long, String)
	 * @see DatabaseEngine#DBReadStats(long, long)
	 * @see DatabaseEngine#DBReadStat(long)
	 *
	 * @param startTime the timestamp of the day start
	 * @param data the xml data to use.
	 * @return true if the update succeeded, false otherwise
	 */
	public boolean DBUpdateStat(long startTime, String data);

	/**
	 * Table category: DBSTATS
	 * Read all or a group of statistic rows within a time range.
	 * Each row represents a rl 'day' of data.

	 * @see DatabaseEngine#DBUpdateStat(long, String)
	 * @see DatabaseEngine#DBDeleteStat(long)
	 * @see DatabaseEngine#DBCreateStat(long, long, String)
	 * @see DatabaseEngine#DBReadStat(long)
	 * @see CoffeeTableRow
	 *
	 * @param startTime the timestamp of the first row
	 * @param endTime 0, or the end time of the last row.
	 * @return the group of statistics requested.
	 */
	public List<CoffeeTableRow> DBReadStats(long startTime, long endTime);

	/**
	 * Table category: DBSTATS
	 * Read the oldest start recorded.

	 * @see DatabaseEngine#DBUpdateStat(long, String)
	 * @see DatabaseEngine#DBDeleteStat(long)
	 * @see DatabaseEngine#DBCreateStat(long, long, String)
	 * @see DatabaseEngine#DBReadStat(long)
	 *
	 * @return startTime the timestamp of the first row
	 */
	public long DBReadOldestStatMs();

	/**
	 * Table category: DBPOLLS
	 * Creates a new poll in the DBPOLLS table.  Most of the arguments are
	 * self explanatory.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBUpdatePoll(String, String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePollResults(String, String)
	 * @see DatabaseEngine#DBDeletePoll(String)
	 * @see DatabaseEngine#DBReadPollList()
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @param name the unique name of the poll
	 * @param player the user/character id of the creator
	 * @param subject the title/subject of the poll
	 * @param description the long descriptions
	 * @param optionXML choices format &lt;OPTIONS&gt;&lt;OPTION&gt;option text...
	 * @param flag flag bitmap, see {@link com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN}
	 * @param qualZapper the zapper mask for who can answer the poll
	 * @param results &lt;RESULTS&gt;&lt;RESULT&gt;&lt;USER&gt;&lt;IP&gt;&lt;ANS&gt;
	 * @param expiration the rl date/timestamp of when the poll auto-closes
	 */
	public void DBCreatePoll(String name, String player, String subject, String description, String optionXML,
							 int flag, String qualZapper, String results, long expiration);

	/**
	 * Table category: DBPOLLS
	 * Updates and/or renames a poll in the DBPOLLS table.  Most of the arguments are
	 * self explanatory.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBCreatePoll(String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePollResults(String, String)
	 * @see DatabaseEngine#DBDeletePoll(String)
	 * @see DatabaseEngine#DBReadPollList()
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @param OldName required, the unique old name of the poll, or current name
	 * @param name the unique new name of the poll, or the current one
	 * @param player the user/character id of the creator
	 * @param subject the title/subject of the poll
	 * @param description the long descriptions
	 * @param optionXML choices format &lt;OPTIONS&gt;&lt;OPTION&gt;option text...
	 * @param flag flag bitmap, see {@link com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN}
	 * @param qualZapper the zapper mask for who can answer the poll
	 * @param results xml doc: &lt;RESULTS&gt;&lt;RESULT&gt;&lt;USER&gt;&lt;IP&gt;&lt;ANS&gt;
	 * @param expiration the rl date/timestamp of when the poll auto-closes
	 */
	public void DBUpdatePoll(String OldName, String name, String player, String subject, String description,
							 String optionXML, int flag, String qualZapper, String results, long expiration);

	/**
	 * Table category: DBPOLLS
	 * Updates the results xml array for an existing poll.  Called when a new result is added, removed,
	 * or modified.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBCreatePoll(String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePoll(String, String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBDeletePoll(String)
	 * @see DatabaseEngine#DBReadPollList()
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @param name the unique name of the poll
	 * @param results xml doc: &lt;RESULTS&gt;&lt;RESULT&gt;&lt;USER&gt;&lt;IP&gt;&lt;ANS&gt;
	 */
	public void DBUpdatePollResults(String name, String results);

	/**
	 * Table category: DBPOLLS
	 * Deletes a poll, and all its options and results, forever.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBCreatePoll(String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePoll(String, String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePollResults(String, String)
	 * @see DatabaseEngine#DBReadPollList()
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @param name the unique name of the poll to kill
	 */
	public void DBDeletePoll(String name);

	/**
	 * Table category: DBPOLLS
	 * Reads the raw data for all the polls from DBPOLLs table.
	 *
	 * @see PollData
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBCreatePoll(String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePoll(String, String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePollResults(String, String)
	 * @see DatabaseEngine#DBDeletePoll(String)
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @return the list of PollData objects
	 */
	public List<PollData> DBReadPollList();

	/**
	 * Table category: DBPOLLS
	 * Reads the raw data for a specific poll of a given name.
	 *
	 * @see PollData
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see DatabaseEngine#DBCreatePoll(String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePoll(String, String, String, String, String, String, int, String, String, long)
	 * @see DatabaseEngine#DBUpdatePollResults(String, String)
	 * @see DatabaseEngine#DBDeletePoll(String)
	 * @see DatabaseEngine#DBReadPoll(String)
	 *
	 * @param name the unique name of the poll to read
	 * @return the raw poll data for that poll, as a PollData object
	 */
	public PollData DBReadPoll(String name);

	/**
	 * Table category: DBVFS
	 * Reads the root of the VFS (DBFS) database filesystem.
	 * It returns the CMFile.CMVFSDir virtual object describing
	 * the database filesystem root and its contents.
	 *
	 * @see com.planet_ink.coffee_mud.core.CMFile.CMVFSDir
	 * @see DatabaseEngine#DBReadVFSFile(String)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 *
	 * @return the dbfs root filesystem
	 */
	public CMFile.CMVFSDir DBReadVFSDirectory();

	/**
	 * Table category: DBVFS
	 * Reads the complete DBFS file record for the given filepath.  The
	 * path does not begin with a /.
	 *
	 * @see com.planet_ink.coffee_mud.core.CMFile.CMVFSFile
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBDeleteVFSFileLike(String, int)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 *
	 * @param filename the path of the file to read
	 * @return the complete file record, including data
	 */
	public CMFile.CMVFSFile DBReadVFSFile(String filename);

	/**
	 * Table category: DBVFS
	 * Reads the vfs record keys for the given files.  The
	 * filename does not begin with a /, and is also case-insensitive
	 * and partial/LIKE.
	 * @param minMask 0, or minimum mask value
	 * @param partialFilename the partial filename of the files to read
	 *
	 * @see com.planet_ink.coffee_mud.core.CMFile.CMVFSFile
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBDeleteVFSFileLike(String, int)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 *
	 * @return the complete file records, including data
	 */
	public List<String> DBReadVFSKeysLike(final String partialFilename, int minMask);

	/**
	 * Table category: DBVFS
	 * Creates a new file in the DBFS filesystem stored in the CBVFS table.
	 * The filename does not begin with a /.  The data may be a String,
	 * StringBuffer, or byte array.  The bits are found in CMFile.
	 * @see CMFile#VFS_MASK_MASKSAVABLE
	 *
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBDeleteVFSFileLike(String, int)
	 * @see DatabaseEngine#DBReadVFSFile(String)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 *
	 * @param filename the full name/path
	 * @param bits toggle bits about the file
	 * @param creator the character id of the file creator/owner
	 * @param updateTime the timestamp of the files creation/update time
	 * @param data the file content, String, StringBuffer, or byte array
	 */
	public void DBCreateVFSFile(String filename, int bits, String creator, long updateTime, Object data);

	/**
	 * Table category: DBVFS
	 * Creates or updates a file in the DBFS filesystem stored in the CBVFS table.
	 * The filename does not begin with a /.  The data may be a String,
	 * StringBuffer, or byte array.  The bits are found in CMFile.
	 * @see CMFile#VFS_MASK_MASKSAVABLE
	 *
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBDeleteVFSFileLike(String, int)
	 * @see DatabaseEngine#DBReadVFSFile(String)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 *
	 * @param filename the full name/path
	 * @param bits toggle bits about the file
	 * @param creator the character id of the file creator/owner
	 * @param updateTime the timestamp of the files creation/update time
	 * @param data the file content, String, StringBuffer, or byte array
	 */
	public void DBUpSertVFSFile(String filename, int bits, String creator, long updateTime, Object data);

	/**
	 * Table category: DBVFS
	 * Deletes a file from the DBFS in the DBVFS table.  The
	 * path does not begin with a /.
	 *
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBReadVFSFile(String)
	 * @see DatabaseEngine#DBDeleteVFSFileLike(String, int)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 *
	 * @param filename the full path filename of the file to kill
	 */
	public void DBDeleteVFSFile(String filename);

	/**
	 * Table category: DBVFS
	 * Deletes file(s) from the DBFS in the DBVFS table.  The
	 * path does not begin with a /.  The argument is a partial
	 * case-insensitive filename.
	 * @param minMask 0, or minimum mask value
	 * @param partialFilename the partial case-insensitive filename
	 *
	 * @see DatabaseEngine#DBReadVFSDirectory()
	 * @see DatabaseEngine#DBReadVFSFile(String)
	 * @see DatabaseEngine#DBDeleteVFSFile(String)
	 * @see DatabaseEngine#DBCreateVFSFile(String, int, String, long, Object)
	 * @see DatabaseEngine#DBUpSertVFSFile(String, int, String, long, Object)
	 */
	public void DBDeleteVFSFileLike(final String partialFilename, int minMask);

	/**
	 * Table category: DBBACKLOG
	 * Adds a CHANNEL message to the backlog table
	 *
	 * @see DatabaseEngine#getBackLogEntries(String, int, int, int)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#trimBackLogEntries(String[], int, long)
	 *
	 * @param channelName the unique name of the channel
	 * @param subNameField the sub-category of the channel message
	 * @param timeStamp the time the message was added in millis
	 * @param entry message
	 */
	public void addBackLogEntry(String channelName, int subNameField, long timeStamp, final String entry);

	/**
	 * Table category: DBBACKLOG
	 * Removes a CHANNEL message from the backlog table
	 * @see DatabaseEngine#getBackLogEntries(String, int, int, int)
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 * @see DatabaseEngine#trimBackLogEntries(String[], int, long)
	 * @param channelName the unique name of the channel
	 * @param timeStamp the time the message was posted
	 */
	public void delBackLogEntry(final String channelName, final long timeStamp);

	/**
	 * Table category: DBBACKLOG
	 * Returns a list of channel messages for the given channel and criteria.
	 * The list returned includes the message, index, and the timestamp of the
	 * message.  The list is date-sorted, so list returns can ge "paged"
	 * by setting the number to skip and the number to return.
	 *
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#trimBackLogEntries(String[], int, long)
	 * @see DatabaseEngine#getBackLogPageEnd(String, int)
	 *
	 * @param channelName the unique name of the channel to return messages from
	 * @param subNameField any extra query field, or 0
	 * @param newestToSkip the number of "newest" messages to skip
	 * @param numToReturn the number of total messages to return
	 * @return a list of applicable messages, coded as string,timestamp
	 */
	public List<Triad<String,Integer,Long>> getBackLogEntries(String channelName, int subNameField, final int newestToSkip, final int numToReturn);

	/**
	 * Table category: DBBACKLOG
	 * Returns the lowest backlog index number of messages after the given timestamp
	 *
	 * @param channelName the unique name of the channel to return messages from
	 * @param subNameField any extra query field, or 0
	 * @param afterDate the date after which we want the lowest index
	 * @return the lowest index, or -1
	 */
	public int getLowestBackLogIndex(final String channelName, final int subNameField, final long afterDate);

	/**
	 * Table category: DBBACKLOG
	 * Returns matching channel messages for the given channel and criteria.
	 * The list returned includes the message, index, and the timestamp of the
	 * message.  The list is date-sorted, so list returns can ge "paged"
	 * by setting the number to skip and the number to return.
	 *
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#trimBackLogEntries(String[], int, long)
	 * @see DatabaseEngine#getBackLogPageEnd(String, int)
	 *
	 * @param channelName the unique name of the channel to return messages from
	 * @param subNameField any extra query field, or 0
	 * @param search the substring to search for
	 * @param numToReturn the number of total messages to return
	 * @return a list of applicable messages, coded as string,timestamp
	 */
	public List<Triad<String, Integer, Long>> searchBackLogEntries(final String channelName, final int subNameField, final String search, final int numToReturn);

	/**
	 * Table category: DBBACKLOG
	 * Returns the highest page index for the given channel in the backlog table
	 *
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#trimBackLogEntries(String[], int, long)
	 * @see DatabaseEngine#getBackLogEntries(String, int, int, int)
	 *
	 * @param channelName the unique name of the channel to return messages from
	 * @param subNameField any extra query field, or 0
	 * @return the highest page index for messages
	 */
	public int getBackLogPageEnd(String channelName, int subNameField);

	/**
	 * Table category: DBBACKLOG
	 * This is a periodic maintenance method which will go through the
	 * list of unique channel names, and trim them according to the maximum
	 * number of messages to retain (absolute), and the oldest message
	 * to return (absolute timestamp -- no 0 nonsense).  Both criteria
	 * will be used in the trimming.
	 * @see DatabaseEngine#getBackLogEntries(String, int, int, int)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 *
	 * @param channels the list of channels to go through.
	 * @param maxMessages the maximum number of messages to retain
	 * @param oldestTime the oldest message to retain
	 */
	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime);

	/**
	 * Table category: DBBACKLOG
	 * This method checks if the backlog table requires any upgrades and, if so, does them.
	 * @see DatabaseEngine#getBackLogEntries(String, int, int, int)
	 * @see DatabaseEngine#delBackLogEntry(String, long)
	 * @see DatabaseEngine#addBackLogEntry(String, int, long, String)
	 * @param channels the channels library to which the backlog applies
	 */
	public void checkUpgradeBacklogTable(final ChannelsLibrary channels);

	/**
	 * Table category: DBPLAYERDATA
	 * A record of Data information from the database.
	 * This is usually a XML document record of some sort.
	 * Since it is keyed by player, it is typically very safe to obliterate
	 * all records belonging to a player name whenever the player
	 * needs to go.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface PlayerData
	{
		/**
		 * Gets the official Name of the player that
		 * owns this record.
		 * @see DatabaseEngine.PlayerData#who(String)
		 * @return name of the player
		 */
		public String who();

		/**
		 * Sets the official Name of the player that
		 * owns this record.
		 * @see DatabaseEngine.PlayerData#who()
		 * @param who name of the player
		 * @return this
		 */
		public PlayerData who(String who);

		/**
		 * Gets the section/category to which this
		 * data belongs.
		 * @see DatabaseEngine.PlayerData#section(String)
		 * @return the data category
		 */
		public String section();

		/**
		 * Sets the section/category to which this
		 * data belongs.
		 * @see DatabaseEngine.PlayerData#section()
		 * @param section the data category
		 * @return this
		 */
		public PlayerData section(String section);

		/**
		 * Sets the unique key that identifies this record
		 * of data.  It must be unique in the whole DB,
		 * and can be anything from a GUID to a player/
		 * section combination.
		 * @see DatabaseEngine.PlayerData#key(String)
		 * @return the unique record key
		 */
		public String key();

		/**
		 * Sets the unique key that identifies this record
		 * of data.  It must be unique in the whole DB,
		 * and can be anything from a GUID to a player/
		 * section combination.
		 * @see DatabaseEngine.PlayerData#key()
		 * @param key the unique record key
		 * @return this
		 */
		public PlayerData key(String key);

		/**
		 * Gets the actual data document that is the payload
		 * of this record.  It is typically an XML
		 * document.
		 * @see DatabaseEngine.PlayerData#xml(String)
		 * @return the xml document payload
		 */
		public String xml();

		/**
		 * Sets the actual data document that is the payload
		 * of this record.  It is typically an XML
		 * document.
		 * @see DatabaseEngine.PlayerData#xml()
		 * @param xml the xml document payload
		 * @return this
		 */
		public PlayerData xml(String xml);
	}

	/**
	 * Table category: DBPOLLS
	 * Raw record entry for the DBPOLLS table, where each
	 * record represents an entire poll and all of its
	 * results.
	 *
	 * @author Bo Zimmerman
	 */
	public static interface PollData
	{
		/**
		 * The unique Name of the poll
		 *
		 * @return unique Name of the poll
		 */
		public String name();

		/**
		 * Special flag bitmap for this poll.
		 *
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
		 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ACTIVE
		 *
		 * @return flag bitmap
		 */
		public long flag();

		/**
		 * The player Name of the author of the poll.
		 *
		 * @return Name of the author of the poll.
		 */
		public String authorName();

		/**
		 * The short title of the poll.
		 *
		 * @return short title of the poll.
		 */
		public String subject();

		/**
		 * The long description of the poll.
		 *
		 * @return long description of the poll.
		 */
		public String description();

		/**
		 * The options/multiple-choices for this
		 * poll.  In XML document format.
		 *
		 * @return the options to choose from xml
		 */
		public String optionsXml();

		/**
		 * The Zapper Mask to decide who may participate
		 * in this poll.
		 *
		 * @return the qualifying zapper mask
		 */
		public String qualifyingMask();

		/**
		 * The player name keyed results/choices for
		 * this poll.  In XML document format.
		 *
		 * @return the results chosen in xml
		 */
		public String resultsXml();

		/**
		 * The real-timestamp after which this poll
		 * is expired.
		 *
		 * @return the expiration timestamp of the poll
		 */
		public long expiration();
	}

	/**
	 * Table category: DBRACE
	 * A record of the creation timstamp for
	 * a generic race
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AckStats
	{
		/**
		 * The Race ID
		 *
		 * @return the race id
		 */
		public String ID();

		/**
		 * The creation timestamp
		 *
		 * @return creation timestamp
		 */
		public long creationDate();
	}

	/**
	 * Table category: DBRACE, DBCHARCLASS, DBABILITY
	 *
	 * A data record for a generic race or class or
	 * generic ability.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface AckRecord
	{
		/**
		 * The race, ability, or char class ID
		 *
		 * @return race or class ID
		 */
		public String ID();

		/**
		 * The XML document describing the race
		 * or class or ability this record represents.
		 *
		 * @return the xml document
		 */
		public String data();

		/**
		 * The base class that is used to build the object
		 * denoted by this record.  Typically GenRace,
		 * GenCharClass, or GenAbility.
		 *
		 * @return the base class
		 */
		public String typeClass();
	}


	/**
	 * Table category: CMROCH, CMROIT
	 * A record from one of the above tables
	 * A room content object
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface RoomContent
	{
		/**
		 * The Object Class ID
		 *
		 * @return the class id
		 */
		public String ID();

		/**
		 * The Object name
		 *
		 * @return name
		 */
		public String name();

		/**
		 * The room ID
		 *
		 * @return room id
		 */
		public String roomID();

		/**
		 * The object DB key
		 *
		 * @return room object db key
		 */
		public String dbKey();

		/**
		 * A has of content obj data
		 *
		 * @return the content obj data
		 */
		public int contentHash();
	}

}
