package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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

import java.sql.*;
import java.util.*;
import java.io.IOException;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.AckRecord;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.AckStats;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
/*
   Copyright 2004-2018 Bo Zimmerman

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
public class DBInterface implements DatabaseEngine
{
	@Override
	public String ID()
	{
		return "DBInterface";
	}

	@Override
	public String name()
	{
		return ID();
	}

	MOBloader		MOBloader		= null;
	RoomLoader		RoomLoader		= null;
	DataLoader		DataLoader		= null;
	StatLoader		StatLoader		= null;
	PollLoader		PollLoader		= null;
	VFSLoader		VFSLoader		= null;
	JournalLoader	JournalLoader	= null;
	QuestLoader		QuestLoader		= null;
	GAbilityLoader	GAbilityLoader	= null;
	GRaceLoader		GRaceLoader		= null;
	GCClassLoader	GCClassLoader	= null;
	ClanLoader		ClanLoader		= null;
	BackLogLoader	BackLogLoader	= null;
	DBConnector		DB				= null;

	public DBInterface(DBConnector DB, Set<String> privacyV)
	{
		this.DB=DB;
		DBConnector oldBaseDB=DB;
		final DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MudHost.MAIN_HOST,CMLib.Library.DATABASE);
		if(privacyV == null)
			privacyV = new HashSet<String>();
		if((baseEngine!=null)&&(baseEngine.getConnector()!=DB)&&(baseEngine.isConnected()))
			oldBaseDB=baseEngine.getConnector();
		
		this.GAbilityLoader = 	new GAbilityLoader(privacyV.contains(DatabaseTables.DBABILITY.toString()) ? DB : oldBaseDB);
		this.GCClassLoader = 	new GCClassLoader(privacyV.contains(DatabaseTables.DBCHARCLASS.toString()) ? DB : oldBaseDB);
		this.GRaceLoader = 		new GRaceLoader(privacyV.contains(DatabaseTables.DBRACE.toString()) ? DB : oldBaseDB);
		this.MOBloader = 		new MOBloader(privacyV.contains(DatabaseTables.DBPLAYERS.toString()) ? DB : oldBaseDB);
		this.RoomLoader = 		new RoomLoader(privacyV.contains(DatabaseTables.DBMAP.toString()) ? DB : oldBaseDB);
		this.DataLoader = 		new DataLoader(this, privacyV.contains(DatabaseTables.DBPLAYERDATA.toString()) ? DB : oldBaseDB);
		this.StatLoader = 		new StatLoader(privacyV.contains(DatabaseTables.DBSTATS.toString()) ? DB : oldBaseDB);
		this.PollLoader = 		new PollLoader(privacyV.contains(DatabaseTables.DBPOLLS.toString()) ? DB : oldBaseDB);
		this.VFSLoader = 		new VFSLoader(privacyV.contains(DatabaseTables.DBVFS.toString()) ? DB : oldBaseDB);
		this.JournalLoader = 	new JournalLoader(privacyV.contains(DatabaseTables.DBJOURNALS.toString()) ? DB : oldBaseDB);
		this.QuestLoader = 		new QuestLoader(privacyV.contains(DatabaseTables.DBQUEST.toString()) ? DB : oldBaseDB);
		this.ClanLoader = 		new ClanLoader(privacyV.contains(DatabaseTables.DBCLANS.toString()) ? DB : oldBaseDB);
		this.BackLogLoader = 	new BackLogLoader(privacyV.contains(DatabaseTables.DBBACKLOG.toString()) ? DB : oldBaseDB);
	}

	@Override
	public CMObject newInstance()
	{
		return new DBInterface(DB, CMProps.getPrivateSubSet("DB.*"));
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public DBConnector getConnector()
	{
		return DB;
	}

	@Override
	public boolean activate()
	{
		return true;
	}

	@Override
	public boolean shutdown()
	{
		return true;
	}

	@Override
	public void propertiesLoaded()
	{
	}

	@Override
	public TickClient getServiceClient()
	{
		return null;
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> vassals(String liegeID)
	{
		return MOBloader.vassals(liegeID);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> worshippers(String deityID)
	{
		return MOBloader.worshippers(deityID);
	}

	@Override
	public List<String> getUserList()
	{
		return MOBloader.getUserList();
	}

	@Override
	public boolean isConnected()
	{
		return DB.amIOk();
	}

	@Override
	public List<Clan> DBReadAllClans()
	{
		return ClanLoader.DBRead();
	}

	@Override
	public void DBReadClanItems(Map<String,Clan> clans)
	{
		ClanLoader.DBReadClanItems(clans);
	}

	@Override
	public List<MemberRecord> DBReadClanMembers(String clan)
	{
		return MOBloader.DBClanMembers(clan);
	}

	@Override
	public void DBUpdateClanMembership(String name, String clan, int role)
	{
		MOBloader.DBUpdateClanMembership(name, clan, role);
	}

	@Override
	public void DBUpdateClanKills(String clan, String name, int adjMobKills, int adjPlayerKills)
	{
		MOBloader.DBUpdateClanKills(clan, name, adjMobKills, adjPlayerKills);
	}

	@Override
	public MemberRecord DBGetClanMember(String clan, String name)
	{
		return MOBloader.DBGetClanMember(clan, name);
	}

	@Override
	public void DBUpdateClan(Clan C)
	{
		ClanLoader.DBUpdate(C);
	}

	@Override
	public void DBUpdateClanItems(final Clan C)
	{
		ClanLoader.DBUpdateItems(C);
	}

	@Override
	public void DBDeleteClan(Clan C)
	{
		ClanLoader.DBDelete(C);
	}

	@Override
	public void DBCreateClan(Clan C)
	{
		ClanLoader.DBCreate(C);
	}

	@Override
	public void DBUpdateEmail(MOB mob)
	{
		MOBloader.DBUpdateEmail(mob);
	}

	@Override
	public String DBPlayerEmailSearch(String email)
	{
		return MOBloader.DBPlayerEmailSearch(email);
	}

	@Override
	public void DBUpdatePassword(String name, String password)
	{
		MOBloader.DBUpdatePassword(name, password);
	}

	@Override
	public Pair<String, Boolean> DBFetchEmailData(String name)
	{
		return MOBloader.DBFetchEmailData(name);
	}

	@Override
	public void DBUpdatePlayerAbilities(MOB mob)
	{
		MOBloader.DBUpdateAbilities(mob);
	}

	@Override
	public void DBUpdatePlayerItems(MOB mob)
	{
		MOBloader.DBUpdateItems(mob);
	}

	@Override
	public void DBUpdateFollowers(MOB mob)
	{
		MOBloader.DBUpdateFollowers(mob);
	}

	@Override
	public void DBUpdateAccount(PlayerAccount account)
	{
		MOBloader.DBUpdateAccount(account);
	}

	@Override
	public void DBCreateAccount(PlayerAccount account)
	{
		MOBloader.DBCreateAccount(account);
	}

	@Override
	public PlayerAccount DBReadAccount(String Login)
	{
		return MOBloader.DBReadAccount(Login);
	}

	@Override
	public List<PlayerAccount> DBListAccounts(String mask)
	{
		return MOBloader.DBListAccounts(mask);
	}

	@Override
	public List<Pair<String, Integer>>[][] DBScanPrideAccountWinners(int topThisMany, short scanCPUPercent)
	{
		return MOBloader.DBScanPrideAccountWinners(topThisMany, scanCPUPercent);
	}

	@Override
	public List<Pair<String, Integer>>[][] DBScanPridePlayerWinners(int topThisMany, short scanCPUPercent)
	{
		return MOBloader.DBScanPridePlayerWinners(topThisMany, scanCPUPercent);
	}

	@Override
	public void DBPlayerNameChange(String oldName, String newName)
	{
		MOBloader.DBNameChange(oldName, newName);
	}

	@Override
	public void DBReadAreaData(Area A)
	{
		RoomLoader.DBReadArea(A);
	}

	@Override
	public String DBIsAreaName(String name)
	{
		return RoomLoader.DBIsAreaName(name);
	}
	
	@Override
	public Room DBReadRoom(String roomID, boolean reportStatus)
	{
		return RoomLoader.DBReadRoomData(roomID, reportStatus);
	}

	@Override
	public boolean DBReadAreaFull(String areaName)
	{
		return RoomLoader.DBReadAreaFull(areaName);
	}
	
	@Override
	public void DBReadAllRooms(RoomnumberSet roomsToRead)
	{
		RoomLoader.DBReadAllRooms(roomsToRead);
	}

	@Override
	public int[] DBCountRoomMobsItems(String roomID)
	{
		return RoomLoader.DBCountRoomMobsItems(roomID);
	}
	
	@Override
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus)
	{
		return RoomLoader.DBReadRoomObjects(areaName, reportStatus);
	}

	@Override
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
	{
		return RoomLoader.DBReadRoomObject(roomIDtoLoad, reportStatus);
	}

	@Override
	public boolean DBReReadRoomData(Room room)
	{
		return RoomLoader.DBReReadRoomData(room);
	}

	@Override
	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus)
	{
		RoomLoader.DBReadRoomExits(roomID, room, reportStatus);
	}

	@Override
	public void DBReadCatalogs()
	{
		RoomLoader.DBReadCatalogs();
	}

	@Override
	public void DBReadSpace()
	{
		RoomLoader.DBReadSpace();
	}

	@Override
	public void DBReadContent(String roomID, Room thisRoom, boolean makeLive)
	{
		RoomLoader.DBReadContent(roomID, thisRoom, null, null, false, makeLive);
	}

	@Override
	public Area DBReadAreaObject(String areaName)
	{
		return RoomLoader.DBReadAreaObject(areaName);
	}
	
	@Override
	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
	{
		return RoomLoader.DBReadAreaRoomList(areaName, reportStatus);
	}

	@Override
	public void DBCreateThisItem(String roomID, Item thisItem)
	{
		RoomLoader.DBCreateThisItem(roomID, thisItem);
	}

	@Override
	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{
		RoomLoader.DBCreateThisMOB(roomID, thisMOB);
	}

	@Override
	public void DBUpdateExits(Room room)
	{
		RoomLoader.DBUpdateExits(room);
	}

	@Override
	public List<Quest> DBReadQuests()
	{
		return QuestLoader.DBRead();
	}

	@Override
	public void DBUpdateQuest(Quest Q)
	{
		QuestLoader.DBUpdateQuest(Q);
	}

	@Override
	public void DBUpdateQuests(List<Quest> quests)
	{
		QuestLoader.DBUpdateQuests(quests);
	}

	@Override
	public String DBReadRoomMOBMiscText(String roomID, String mobID)
	{
		return RoomLoader.DBReadRoomMOBMiscText(roomID, mobID);
	}

	@Override
	public String DBReadRoomDesc(String roomID)
	{
		return RoomLoader.DBReadRoomDesc(roomID);
	}

	@Override
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs)
	{
		RoomLoader.DBUpdateTheseMOBs(room, mobs);
	}

	@Override
	public void DBUpdateTheseItems(Room room, List<Item> items)
	{
		RoomLoader.DBUpdateTheseItems(room, items);
	}

	@Override
	public void DBUpdateMOBs(Room room)
	{
		RoomLoader.DBUpdateMOBs(room);
	}

	@Override
	public void DBDeletePlayerPrivateJournalEntries(String name)
	{
		JournalLoader.DBDeletePlayerPrivateJournalEntries(name);
	}

	@Override
	public void DBUpdateJournal(String journalID, JournalEntry entry)
	{
		JournalLoader.DBUpdateJournal(journalID, entry);
	}

	@Override
	public void DBUpdateJournalMetaData(String journalID, JournalsLibrary.JournalMetaData metaData)
	{
		JournalLoader.DBUpdateJournalMetaData(journalID, metaData);
	}

	@Override
	public void DBReadJournalMetaData(String journalID, JournalsLibrary.JournalMetaData metaData)
	{
		JournalLoader.DBReadJournalSummaryStats(journalID, metaData);
	}

	@Override
	public String DBGetRealJournalName(String possibleName)
	{
		return JournalLoader.DBGetRealName(possibleName);
	}

	@Override
	public void DBDeleteJournal(String journalID, String msgKeyOrNull)
	{
		JournalLoader.DBDelete(journalID, msgKeyOrNull);
	}

	@Override
	public List<String> DBReadJournals()
	{
		return JournalLoader.DBReadJournals();
	}

	@Override
	public JournalEntry DBReadJournalEntry(String journalID, String messageKey)
	{
		return JournalLoader.DBReadJournalEntry(journalID, messageKey);
	}

	@Override
	public void DBUpdateMessageReplies(String key, int numReplies)
	{
		JournalLoader.DBUpdateMessageReplies(key, numReplies);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending, int limit)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(String journalID, boolean ascending, int limit, String[] tos)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending, int limit)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(String journalID, boolean ascending, int limit, String[] tos)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, false);
	}

	@Override
	public List<JournalEntry> DBSearchAllJournalEntries(String journalID, String searchStr)
	{
		return JournalLoader.DBSearchAllJournalEntries(journalID, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsNewerThan(String journalID, String to, long olderDate)
	{
		return JournalLoader.DBReadJournalMsgsNewerThan(journalID, to, olderDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsOlderThan(String journalID, String to, long newestDate)
	{
		return JournalLoader.DBReadJournalMsgsOlderThan(journalID, to, newestDate);
	}
	
	@Override
	public List<JournalEntry> DBReadJournalPageMsgs(String journalID, String parent, String searchStr, long newerDate, int limit)
	{
		return JournalLoader.DBReadJournalPageMsgs(journalID, parent, searchStr, newerDate, limit);
	}

	@Override
	public int DBCountJournal(String journalID, String from, String to)
	{
		return JournalLoader.DBCount(journalID, from, to);
	}

	@Override
	public long[] DBJournalLatestDateNewerThan(String journalID, String to, long olderTime)
	{
		return JournalLoader.DBJournalLatestDateNewerThan(journalID, to, olderTime);
	}

	@Override
	public void DBWriteJournal(String journalID, JournalEntry entry)
	{
		JournalLoader.DBWrite(journalID, entry);
	}

	@Override
	public void DBWriteJournal(String journalID, String from, String to, String subject, String message)
	{
		JournalLoader.DBWrite(journalID, from, to, subject, message);
	}

	@Override
	public void DBWriteJournalEmail(String mailBoxID, String journalSource, String from, String to, String subject, String message)
	{
		JournalLoader.DBWrite(mailBoxID, journalSource, from, to, subject, message);
	}

	@Override
	public void DBWriteJournalChild(String journalID, String journalSource, String from, String to, String parentKey, String subject, String message)
	{
		JournalLoader.DBWrite(journalID, journalSource, from, to, parentKey, subject, message);
	}

	public void DBWrite(String journalID, JournalEntry entry)
	{
		JournalLoader.DBWrite(journalID, entry);
	}

	@Override
	public JournalEntry DBWriteJournalReply(String journalID, String key, String from, String to, String subject, String message)
	{
		return JournalLoader.DBWriteJournalReply(journalID, key, from, to, subject, message);
	}

	@Override
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes)
	{
		JournalLoader.DBUpdateJournal(key, subject, msg, newAttributes);
	}

	@Override
	public void DBUpdateJournalMessageViews(String key, int views)
	{
		JournalLoader.DBUpdateJournalMessageViews(key, views);
	}

	@Override
	public void DBTouchJournalMessage(String key)
	{
		JournalLoader.DBTouchJournalMessage(key);
	}

	@Override
	public void DBTouchJournalMessage(String key, long newDate)
	{
		JournalLoader.DBTouchJournalMessage(key, newDate);
	}

	@Override
	public void DBCreateRoom(Room room)
	{
		RoomLoader.DBCreate(room);
	}

	@Override
	public void DBUpdateRoom(Room room)
	{
		RoomLoader.DBUpdateRoom(room);
	}

	@Override
	public void DBUpdatePlayer(MOB mob)
	{
		MOBloader.DBUpdate(mob);
	}

	@Override
	public List<String> DBExpiredCharNameSearch(Set<String> skipNames)
	{
		return MOBloader.DBExpiredCharNameSearch(skipNames);
	}

	@Override
	public void DBUpdatePlayerPlayerStats(MOB mob)
	{
		MOBloader.DBUpdateJustPlayerStats(mob);
	}

	@Override
	public void DBUpdatePlayerMOBOnly(MOB mob)
	{
		MOBloader.DBUpdateJustMOB(mob);
	}

	@Override
	public void DBUpdateMOB(String roomID, MOB mob)
	{
		RoomLoader.DBUpdateRoomMOB(roomID, mob);
	}

	@Override
	public void DBUpdateItem(String roomID, Item item)
	{
		RoomLoader.DBUpdateRoomItem(roomID, item);
	}

	@Override
	public void DBDeleteMOB(String roomID, MOB mob)
	{
		RoomLoader.DBDeleteRoomMOB(roomID, mob);
	}

	@Override
	public void DBDeleteItem(String roomID, Item item)
	{
		RoomLoader.DBDeleteRoomItem(roomID, item);
	}

	@Override
	public void DBUpdateItems(Room room)
	{
		RoomLoader.DBUpdateItems(room);
	}

	@Override
	public void DBReCreate(Room room, String oldID)
	{
		RoomLoader.DBReCreate(room, oldID);
	}

	@Override
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{
		return MOBloader.DBUserSearch(Login);
	}

	@Override
	public void DBCreateArea(Area A)
	{
		RoomLoader.DBCreate(A);
	}

	@Override
	public void DBDeleteArea(Area A)
	{
		RoomLoader.DBDelete(A);
	}

	@Override
	public void DBDeleteAreaAndRooms(Area A)
	{
		RoomLoader.DBDeleteAreaAndRooms(A);
	}

	@Override
	public void DBUpdateArea(String areaID, Area A)
	{
		RoomLoader.DBUpdate(areaID, A);
	}

	@Override
	public void DBDeleteRoom(Room room)
	{
		RoomLoader.DBDelete(room);
	}

	@Override
	public MOB DBReadPlayer(String name)
	{
		return MOBloader.DBRead(name);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		return MOBloader.getExtendedUserList();
	}

	@Override
	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{
		return MOBloader.getThinUser(name);
	}

	@Override
	public void DBReadFollowers(MOB mob, boolean bringToLife)
	{
		MOBloader.DBReadFollowers(mob, bringToLife);
	}

	@Override
	public List<MOB> DBScanFollowers(String mobName)
	{
		return MOBloader.DBScanFollowers(mobName);
	}

	@Override
	public void DBDeletePlayerOnly(String mobName)
	{
		MOBloader.DBDeleteCharOnly(mobName);
	}

	@Override
	public void DBDeleteAccount(PlayerAccount account)
	{
		MOBloader.DBDeleteAccount(account);
	}

	@Override
	public void DBCreateCharacter(MOB mob)
	{
		MOBloader.DBCreateCharacter(mob);
	}

	@Override
	public void DBDeleteAllPlayerData(String name)
	{
		DataLoader.DBDeletePlayer(name);
	}

	@Override
	public List<PlayerData> DBReadAllPlayerData(String playerID)
	{
		return DataLoader.DBReadAllPlayerData(playerID);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(String playerID, String section)
	{
		return DataLoader.DBRead(playerID, section);
	}

	@Override
	public List<PlayerData> DBReadPlayerDataByKeyMask(String section, String keyMask)
	{
		return DataLoader.DBReadByKeyMask(section, keyMask);
	}

	@Override
	public List<PlayerData> DBReadPlayerDataEntry(String key)
	{
		return DataLoader.DBReadKey(key);
	}

	@Override
	public int DBCountPlayerData(String playerID, String section)
	{
		return DataLoader.DBCount(playerID, section);
	}

	@Override
	public int DBCountPlayerData(String section)
	{
		return DataLoader.DBCountBySection(section);
	}

	@Override
	public List<String> DBReadPlayerDataAuthorsBySection(String section)
	{
		return DataLoader.DBReadAuthorsBySection(section);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(String playerID, String section, String key)
	{
		return DataLoader.DBRead(playerID, section, key);
	}

	@Override
	public List<PlayerData> DBReadPlayerSectionData(String section)
	{
		return DataLoader.DBRead(section);
	}

	@Override
	public List<String> DBReadPlayerDataPlayersBySection(String section)
	{
		return DataLoader.DBReadNames(section);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(String player, List<String> sections)
	{
		return DataLoader.DBRead(player, sections);
	}

	@Override
	public void DBDeletePlayerData(String playerID, String section)
	{
		DataLoader.DBDelete(playerID, section);
	}

	@Override
	public void DBDeletePlayerData(String playerID, String section, String key)
	{
		DataLoader.DBDelete(playerID, section, key);
	}

	@Override
	public void DBDeletePlayerSectionData(String section)
	{
		DataLoader.DBDelete(section);
	}

	@Override
	public PlayerData DBReCreatePlayerData(String name, String section, String key, String xml)
	{
		return DataLoader.DBReCreate(name, section, key, xml);
	}

	@Override
	public void DBUpdatePlayerData(String key, String xml)
	{
		DataLoader.DBUpdate(key, xml);
	}

	@Override
	public PlayerData DBCreatePlayerData(String player, String section, String key, String data)
	{
		return DataLoader.DBCreate(player, section, key, data);
	}

	@Override
	public List<AckRecord> DBReadRaces()
	{
		return GRaceLoader.DBReadRaces();
	}

	@Override
	public void DBPruneOldRaces()
	{
		GRaceLoader.DBPruneOldRaces();
	}

	@Override
	public void DBDeleteRace(String raceID)
	{
		GRaceLoader.DBDeleteRace(raceID);
	}

	@Override
	public void DBCreateRace(String raceID, String data)
	{
		GRaceLoader.DBCreateRace(raceID, data);
	}

	@Override
	public List<AckRecord> DBReadClasses()
	{
		return GCClassLoader.DBReadClasses();
	}

	@Override
	public void DBDeleteClass(String classID)
	{
		GCClassLoader.DBDeleteClass(classID);
	}

	@Override
	public void DBCreateClass(String classID, String data)
	{
		GCClassLoader.DBCreateClass(classID, data);
	}

	@Override
	public List<AckRecord> DBReadAbilities()
	{
		return GAbilityLoader.DBReadAbilities();
	}

	@Override
	public void DBDeleteAbility(String classID)
	{
		GAbilityLoader.DBDeleteAbility(classID);
	}

	@Override
	public void DBCreateAbility(String classID, String typeClass, String data)
	{
		GAbilityLoader.DBCreateAbility(classID, typeClass, data);
	}

	@Override
	public void DBReadArtifacts()
	{
		DataLoader.DBReadArtifacts();
	}

	@Override
	public CoffeeTableRow DBReadStat(long startTime)
	{
		return StatLoader.DBRead(startTime);
	}

	@Override
	public void DBDeleteStat(long startTime)
	{
		StatLoader.DBDelete(startTime);
	}

	@Override
	public boolean DBCreateStat(long startTime, long endTime, String data)
	{
		return StatLoader.DBCreate(startTime, endTime, data);
	}

	@Override
	public boolean DBUpdateStat(long startTime, String data)
	{
		return StatLoader.DBUpdate(startTime, data);
	}

	@Override
	public List<CoffeeTableRow> DBReadStats(long startTime, long endTime)
	{
		return StatLoader.DBReadAfter(startTime, endTime);
	}

	@Override
	public String errorStatus()
	{
		return DB.errorStatus().toString();
	}

	@Override
	public void resetConnections()
	{
		DB.reconnect();
	}

	@Override
	public int pingAllConnections(final long overrideTimeoutIntervalMillis)
	{
		return DB.pingAllConnections("SELECT 1 FROM CMCHAR", overrideTimeoutIntervalMillis);
	}

	@Override
	public int pingAllConnections()
	{
		return DB.pingAllConnections("SELECT 1 FROM CMCHAR");
	}

	@Override
	public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
	{
		PollLoader.DBCreate(name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePoll(String oldName, String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
	{
		PollLoader.DBUpdate(oldName, name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePollResults(String name, String results)
	{
		PollLoader.DBUpdate(name, results);
	}

	@Override
	public void DBDeletePoll(String name)
	{
		PollLoader.DBDelete(name);
	}

	@Override
	public List<PollData> DBReadPollList()
	{
		return PollLoader.DBReadList();
	}

	@Override
	public PollData DBReadPoll(String name)
	{
		return PollLoader.DBRead(name);
	}

	@Override
	public CMFile.CMVFSDir DBReadVFSDirectory()
	{
		return VFSLoader.DBReadDirectory();
	}

	@Override
	public CMFile.CMVFSFile DBReadVFSFile(String filename)
	{
		return VFSLoader.DBRead(filename);
	}

	@Override
	public void DBCreateVFSFile(String filename, int bits, String creator, long updateTime, Object data)
	{
		VFSLoader.DBCreate(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBUpSertVFSFile(String filename, int bits, String creator, long updateTime, Object data)
	{
		VFSLoader.DBUpSert(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBDeleteVFSFile(String filename)
	{
		VFSLoader.DBDelete(filename);
	}

	@Override
	public List<Pair<String, Long>> getBackLogEntries(String channelName, final int newestToSkip, final int numToReturn)
	{
		return BackLogLoader.getBackLogEntries(channelName, newestToSkip, numToReturn);
	}

	@Override
	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime)
	{
		BackLogLoader.trimBackLogEntries(channels, maxMessages, oldestTime);
	}

	@Override
	public void addBackLogEntry(String channelName, final String entry)
	{
		BackLogLoader.addBackLogEntry(channelName, entry);
	}

	@Override
	public int DBRawExecute(String sql) throws CMException
	{
		DBConnection DBToUse=null;
		try
		{
			DBToUse=DB.DBFetch();
			return DBToUse.update(sql,0);
		}
		catch(final Exception e)
		{
			throw new CMException((e.getMessage()==null)?"Unknown error":e.getMessage());
		}
		finally
		{
			if(DBToUse!=null)
				DB.DBDone(DBToUse);
		}
	}

	@Override
	public List<String[]> DBRawQuery(String sql) throws CMException
	{
		DBConnection DBToUse=null;
		final List<String[]> results=new LinkedList<String[]>();
		try
		{
			DBToUse=DB.DBFetch();
			final ResultSet R=DBToUse.query(sql);
			final ResultSetMetaData metaData=R.getMetaData();
			if(metaData!=null)
			{
				final List<String> header=new LinkedList<String>();
				for(int c=1;c<=metaData.getColumnCount();c++)
					header.add(CMStrings.padRight(metaData.getColumnName(c), metaData.getColumnDisplaySize(c)));
				results.add(header.toArray(new String[0]));
			}
			while(R.next())
			{
				final List<String> row=new LinkedList<String>();
				try
				{
					for(int i=1;;i++)
					{
						final Object o=R.getObject(i);
						if(o==null)
							row.add("null");
						else
							row.add(o.toString());
					}
				}
				catch(final Exception e)
				{

				}
				results.add(row.toArray(new String[0]));
			}
		}
		catch(final Exception e)
		{
			throw new CMException((e.getMessage()==null)?"Unknown error":e.getMessage());
		}
		finally
		{
			if(DBToUse!=null)
				DB.DBDone(DBToUse);
		}
		return results;
	}
}
