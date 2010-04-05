package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public interface DatabaseEngine extends CMLibrary
{
	public String errorStatus();
	public void resetconnections();
    public DBConnector getConnector();
	// DBABLES, DBCCLASS, DBRACES, DBPLAYERS, DBMAP, DBSTATS, DBPOLLS, DBVFS, DBJOURNALS, DBQUESTS, DBCLANS  
	
	public void DBUpdateFollowers(MOB mob);
	public void DBReadCatalogs();
	public void DBReadContent(Room thisRoom, Vector rooms);
    public Vector DBReadAreaData(String areaID, boolean reportStatus);
    public Vector DBReadRoomData(String roomID, boolean reportStatus);
    public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus);
    public void DBReadRoomExits(String roomID, Vector allRooms, boolean reportStatus);
	public void DBUpdateExits(Room room);
	public void DBCreateThisItem(String roomID, Item thisItem);
	public void DBCreateThisMOB(String roomID, MOB thisMOB);
	public String DBReadRoomMOBData(String roomID, String mobID);
	public String DBReadRoomDesc(String roomID);
    public void DBReadAllRooms(RoomnumberSet roomsToRead);
	public void DBUpdateTheseMOBs(Room room, Vector mobs);
	public void DBUpdateTheseItems(Room room, Vector item);
	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room);
	public void DBUpdateRoom(Room room);
    public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus);
	public void DBUpdatePlayer(MOB mob);
    public void DBUpdatePlayerPlayerStats(MOB mob);
    public void DBUpdatePlayerMOBOnly(MOB mob);
    public void DBUpdatePlayerAbilities(MOB mob);
    public void DBUpdatePlayerItems(MOB mob);
    public void DBUpdateAccount(PlayerAccount account);
    public void DBCreateAccount(PlayerAccount account);
    public void DBDeleteAccount(PlayerAccount account);
    public PlayerAccount DBReadAccount(String Login);
    public Vector<PlayerAccount> DBListAccounts(String mask);
	public void DBUpdateMOB(String roomID, MOB mob);
	public void DBUpdateItem(String roomID, Item item);
	public void DBDeleteMOB(String roomID, MOB mob);
	public void DBDeleteItem(String roomID, Item item);
	public void DBUpdateItems(Room room);
	public void DBUpdateQuests(Vector quests);
	public void DBUpdateQuest(Quest Q);
	public void DBReadQuests(MudHost myHost);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadPlayer(MOB mob);
	public Vector<MemberRecord> DBClanMembers(String clan);
	public void DBUpdateClanMembership(String name, String clan, int role);
	public void DBReadAllClans();
	public void DBUpdateClan(Clan C);
	public void DBDeleteClan(Clan C);
	public void DBCreateClan(Clan C);
	public void DBUpdateEmail(MOB mob);
	public void DBUpdatePassword(String name, String password);
    public boolean isConnected();
	public String[] DBFetchEmailData(String name);
	public String DBEmailSearch(String email);
    public List<PlayerLibrary.ThinPlayer> getExtendedUserList();
    public PlayerLibrary.ThinPlayer getThinUser(String name);
    public List<String> getUserList();
    public Vector DBScanFollowers(MOB mob);
	public void DBReadFollowers(MOB mob, boolean bringToLife);
	public void DBDeleteMOB(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public void DBCreateArea(Area A);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(String keyName,Area A);
	public Vector DBReadJournals();
	public void DBUpdateJournalStats(String Journal, JournalsLibrary.JournalSummaryStats stats);
	public void DBUpdateJournal(String Journal, JournalsLibrary.JournalEntry entry);
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats);
	public void DBUpdateMessageReplies(String key, int numReplies);
	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key);
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalPageMsgs(String Journal, String parent, String searchStr, long newerDate, int limit);
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgs(String Journal);
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate);
	public int DBCountJournal(String Journal, String from, String to);
	public void DBWriteJournal(String Journal, JournalsLibrary.JournalEntry entry);
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message);
	public void DBWriteJournalReply(String Journal, String key, String from, String to, String subject, String message);
	public void DBDeleteJournal(String Journal, String msgKeyOrNull);
    public String DBGetRealJournalName(String possibleName);
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime);
	public void DBDeletePlayerJournals(String name);
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes);
	public void DBViewJournalMessage(String key, int views);
	public void DBTouchJournalMessage(String key);
	public boolean DBReadUserOnly(MOB mob);
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login);
	public void vassals(MOB mob, String liegeID);
    public DVector worshippers(String deityID);
	public Vector DBReadAllPlayerData(String playerID);
	public Vector DBReadData(String playerID, String section);
	public int DBCountData(String playerID, String section);
	public Vector DBReadData(String playerID, String section, String key);
	public Vector DBReadDataKey(String section, String keyMask);
	public Vector DBReadDataKey(String key);
	public Vector DBReadData(String section);
    public Vector DBReadData(String player, Vector sections);
	public void DBDeletePlayerData(String name);
	public void DBDeleteData(String playerID, String section);
	public void DBDeleteData(String playerID, String section, String key);
    public void DBUpdateData(String key, String xml);
    public void DBReCreateData(String name, String section, String key, String xml);
	public void DBDeleteData(String section);
	public void DBCreateData(String player, String section, String key, String data);
	public void DBReadArtifacts();
	public Vector DBReadRaces();
	public void DBDeleteRace(String raceID);
	public void DBCreateRace(String raceID,String data);
	public Vector DBReadClasses();
	public void DBDeleteClass(String classID);
	public void DBCreateClass(String classID,String data);
	public Vector DBReadAbilities();
	public void DBDeleteAbility(String classID);
	public void DBCreateAbility(String classID,String data);
	public Object DBReadStat(long startTime);
	public void DBDeleteStat(long startTime);
	public void DBCreateStat(long startTime,long endTime,String data);
	public void DBUpdateStat(long startTime, String data);
	public Vector DBReadStats(long startTime);
    public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration);
    public void DBUpdatePoll(String OldName, String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration);
    public void DBUpdatePollResults(String name, String results);
    public void DBDeletePoll(String name);
    public Vector DBReadPollList();
    public Vector DBReadPoll(String name);
    public CMFile.CMVFSDir DBReadVFSDirectory();
    public CMFile.CMVFSFile DBReadVFSFile(String filename);
    public void DBCreateVFSFile(String filename, int bits, String creator, Object data);
    public void DBDeleteVFSFile(String filename);
    
    public static class PlayerData
    {
    	public String who="";
    	public String section="";
    	public String key="";
    	public String xml="";
    }
    
}
