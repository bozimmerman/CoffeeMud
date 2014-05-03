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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalEntry;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public interface DatabaseEngine extends CMLibrary
{
	public static enum DatabaseTables {
		DBABILITY,DBCHARCLASS,DBRACE,DBPLAYERS,DBMAP,
		DBSTATS,DBPOLLS,DBVFS,DBJOURNALS,DBQUEST,DBCLANS
	}

	public String errorStatus();
	public void resetConnections();
	public DBConnector getConnector();
	public int pingAllConnections();
	public int pingAllConnections(final long overrideTimeoutIntervalMillis);
	// DBABLES, DBCCLASS, DBRACES, DBPLAYERS, DBMAP, DBSTATS, DBPOLLS, DBVFS, DBJOURNALS, DBQUESTS, DBCLANS

	public void DBUpdateFollowers(MOB mob);
	public void DBReadCatalogs();
	public void DBReadContent(String roomID, Room thisRoom, boolean makeLive);
	public Area DBReadArea(Area A);
	public Map<String, Room> DBReadRoomData(String roomID, boolean reportStatus);
	public boolean DBReReadRoomObject(Room room);
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus);
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus);
	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus);
	public void DBUpdateExits(Room room);
	public void DBCreateThisItem(String roomID, Item thisItem);
	public void DBCreateThisMOB(String roomID, MOB thisMOB);
	public String DBReadRoomMOBData(String roomID, String mobID);
	public String DBReadRoomDesc(String roomID);
	public void DBReadAllRooms(RoomnumberSet roomsToRead);
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs);
	public void DBUpdateTheseItems(Room room, List<Item> item);
	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room);
	public void DBUpdateRoom(Room room);
	public List<Pair<String,Integer>>[][] DBScanPridePlayerWinners(int topThisMany, short scanCPUPercent);
	public List<Pair<String,Integer>>[][] DBScanPrideAccountWinners(int topThisMany, short scanCPUPercent);
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus);
	public void DBUpdatePlayer(MOB mob);
	public List<String> DBExpiredCharNameSearch(Set<String> skipNames);
	public void DBUpdatePlayerPlayerStats(MOB mob);
	public void DBUpdatePlayerMOBOnly(MOB mob);
	public void DBUpdatePlayerAbilities(MOB mob);
	public void DBUpdatePlayerItems(MOB mob);
	public void DBUpdateAccount(PlayerAccount account);
	public void DBCreateAccount(PlayerAccount account);
	public void DBDeleteAccount(PlayerAccount account);
	public PlayerAccount DBReadAccount(String Login);
	public List<PlayerAccount> DBListAccounts(String mask);
	public void DBPlayerNameChange(String oldName, String newName);
	public void DBUpdateMOB(String roomID, MOB mob);
	public void DBUpdateItem(String roomID, Item item);
	public void DBDeleteMOB(String roomID, MOB mob);
	public void DBDeleteItem(String roomID, Item item);
	public void DBUpdateItems(Room room);
	public void DBUpdateQuests(List<Quest> quests);
	public void DBUpdateQuest(Quest Q);
	public void DBReadQuests(MudHost myHost);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadPlayer(MOB mob);
	public List<MemberRecord> DBClanMembers(String clan);
	public MemberRecord DBGetClanMember(String clan, String name);
	public void DBUpdateClanKills(String clan, String name, int adjMobKills, int adjPlayerKills);
	public void DBUpdateClanMembership(String name, String clan, int role);
	public void DBReadAllClans();
	public void DBUpdateClan(Clan C);
	public void DBDeleteClan(Clan C);
	public void DBCreateClan(Clan C);
	public void DBUpdateEmail(MOB mob);
	public void DBUpdatePassword(String name, String password);
	public boolean isConnected();
	public String[] DBFetchEmailData(String name);
	public String DBPlayerEmailSearch(String email);
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList();
	public PlayerLibrary.ThinPlayer getThinUser(String name);
	public List<String> getUserList();
	public List<MOB> DBScanFollowers(MOB mob);
	public void DBReadFollowers(MOB mob, boolean bringToLife);
	public void DBDeletePlayer(MOB mob, boolean deleteAssets);
	public void DBCreateCharacter(MOB mob);
	public void DBCreateArea(Area A);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(String keyName,Area A);
	public List<String> DBReadJournals();
	public void DBUpdateJournalStats(String Journal, JournalsLibrary.JournalSummaryStats stats);
	public void DBUpdateJournal(String Journal, JournalsLibrary.JournalEntry entry);
	public Vector<JournalsLibrary.JournalEntry> DBSearchAllJournalEntries(String Journal, String searchStr);
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats);
	public void DBUpdateMessageReplies(String key, int numReplies);
	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key);
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalPageMsgs(String Journal, String parent, String searchStr, long newerDate, int limit);
	public List<JournalEntry> DBReadJournalMsgs(String Journal);
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate);
	public int DBCountJournal(String Journal, String from, String to);
	public void DBWriteJournal(String Journal, JournalsLibrary.JournalEntry entry);
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message);
	public void DBWriteJournalEmail(String MailBox, String journalSource, String from, String to, String subject, String message);
	public void DBWriteJournalReply(String Journal, String key, String from, String to, String subject, String message);
	public void DBWriteJournalChild(String Journal, String journalSource, String from, String to, String parentKey, String subject, String message);
	public void DBDeleteJournal(String Journal, String msgKeyOrNull);
	public String DBGetRealJournalName(String possibleName);
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime);
	public void DBDeletePlayerJournals(String name);
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes);
	public void DBViewJournalMessage(String key, int views);
	public void DBTouchJournalMessage(String key);
	public void DBTouchJournalMessage(String key, long newDate);
	public String DBReadUserOnly(MOB mob);
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login);
	public List<PlayerLibrary.ThinPlayer> vassals(MOB mob, String liegeID);
	public DVector worshippers(String deityID);
	public List<PlayerData> DBReadAllPlayerData(String playerID);
	public List<PlayerData> DBReadData(String playerID, String section);
	public int DBCountData(String playerID, String section);
	public List<PlayerData> DBReadData(String playerID, String section, String key);
	public List<PlayerData> DBReadDataKey(String section, String keyMask);
	public List<PlayerData> DBReadDataKey(String key);
	public List<PlayerData> DBReadData(String section);
	public List<PlayerData> DBReadData(String player, List<String> sections);
	public void DBDeletePlayerData(String name);
	public void DBDeleteData(String playerID, String section);
	public void DBDeleteData(String playerID, String section, String key);
	public void DBUpdateData(String key, String xml);
	public void DBReCreateData(String name, String section, String key, String xml);
	public void DBDeleteData(String section);
	public void DBCreateData(String player, String section, String key, String data);
	public void DBReadArtifacts();
	public List<AckRecord> DBReadRaces();
	public void DBDeleteRace(String raceID);
	public void DBCreateRace(String raceID,String data);
	public List<AckRecord> DBReadClasses();
	public void DBDeleteClass(String classID);
	public void DBCreateClass(String classID,String data);
	public List<AckRecord> DBReadAbilities();
	public void DBDeleteAbility(String classID);
	public void DBCreateAbility(String classID, String typeClass, String data);
	public Object DBReadStat(long startTime);
	public void DBDeleteStat(long startTime);
	public boolean DBCreateStat(long startTime,long endTime,String data);
	public boolean DBUpdateStat(long startTime, String data);
	public List<CoffeeTableRow> DBReadStats(long startTime);
	public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration);
	public void DBUpdatePoll(String OldName, String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration);
	public void DBUpdatePollResults(String name, String results);
	public void DBDeletePoll(String name);
	public List<PollData> DBReadPollList();
	public PollData DBReadPoll(String name);
	public CMFile.CMVFSDir DBReadVFSDirectory();
	public CMFile.CMVFSFile DBReadVFSFile(String filename);
	public void DBCreateVFSFile(String filename, int bits, String creator, long updateTime, Object data);
	public void DBUpSertVFSFile(String filename, int bits, String creator, long updateTime, Object data);
	public void DBDeleteVFSFile(String filename);
	public MOB.Tattoo parseTattoo(String tattoo);
	public int DBRawExecute(String sql) throws CMException;
	public List<String[]> DBRawQuery(String sql) throws CMException;

	public static class PlayerData
	{
		public String who="";
		public String section="";
		public String key="";
		public String xml="";
	}

	public static class PollData
	{
		public String name="";
		public long flag=0;
		public String byName="";
		public String subject="";
		public String description="";
		public String options="";
		public String qual="";
		public String results="";
		public long expiration=0;

	}

	public static class AckRecord
	{
		public String ID="";
		public String data="";
		public String typeClass="GenAbility";
		public AckRecord(String id, String dataStr, String type)
		{
			ID=id;
			data=dataStr;
			if((type!=null)&&(type.length()>0))
				typeClass=type;
		}
	}

}
