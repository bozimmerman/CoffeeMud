package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.AckRecord;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.AckStats;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.ReadRoomDisableFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
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
	GCommandLoader	CommandLoader	= null;
	DBConnector		DB				= null;
	JSONObject		changeList		= null;

	public DBInterface(final DBConnector DB, Set<String> privacyV, final JSONObject changeList)
	{
		this.DB=DB;
		DBConnector oldBaseDB=DB;
		this.changeList = changeList;
		final DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MudHost.MAIN_HOST,CMLib.Library.DATABASE);
		if(privacyV == null)
			privacyV = new HashSet<String>();
		if((baseEngine!=null)&&(baseEngine.getConnector()!=DB)&&(baseEngine.isConnected()))
		{
			oldBaseDB=baseEngine.getConnector();
			if(baseEngine instanceof DBInterface)
			{
				this.GAbilityLoader = ((DBInterface) baseEngine).GAbilityLoader;
				this.GCClassLoader = ((DBInterface) baseEngine).GCClassLoader;
				this.GRaceLoader = ((DBInterface) baseEngine).GRaceLoader;
				this.MOBloader = ((DBInterface) baseEngine).MOBloader;
				this.RoomLoader = ((DBInterface) baseEngine).RoomLoader;
				this.DataLoader = ((DBInterface) baseEngine).DataLoader;
				this.StatLoader = ((DBInterface) baseEngine).StatLoader;
				this.PollLoader = ((DBInterface) baseEngine).PollLoader;
				this.VFSLoader = ((DBInterface) baseEngine).VFSLoader;
				this.JournalLoader = ((DBInterface) baseEngine).JournalLoader;
				this.QuestLoader = ((DBInterface) baseEngine).QuestLoader;
				this.ClanLoader = ((DBInterface) baseEngine).ClanLoader;
				this.BackLogLoader = ((DBInterface) baseEngine).BackLogLoader;
				this.CommandLoader = ((DBInterface) baseEngine).CommandLoader;
			}
		}

		if((this.GAbilityLoader == null) || privacyV.contains(DatabaseTables.DBABILITY.toString()))
			this.GAbilityLoader = 	new GAbilityLoader(privacyV.contains(DatabaseTables.DBABILITY.toString()) ? DB : oldBaseDB);
		if((this.GCClassLoader == null) || privacyV.contains(DatabaseTables.DBCHARCLASS.toString()))
			this.GCClassLoader = 	new GCClassLoader(privacyV.contains(DatabaseTables.DBCHARCLASS.toString()) ? DB : oldBaseDB);
		if((this.GRaceLoader == null) || privacyV.contains(DatabaseTables.DBRACE.toString()))
			this.GRaceLoader = 		new GRaceLoader(privacyV.contains(DatabaseTables.DBRACE.toString()) ? DB : oldBaseDB);
		if((this.MOBloader == null) || privacyV.contains(DatabaseTables.DBPLAYERS.toString()))
			this.MOBloader = 		new MOBloader(privacyV.contains(DatabaseTables.DBPLAYERS.toString()) ? DB : oldBaseDB);
		if((this.RoomLoader == null) || privacyV.contains(DatabaseTables.DBMAP.toString()))
			this.RoomLoader = 		new RoomLoader(privacyV.contains(DatabaseTables.DBMAP.toString()) ? DB : oldBaseDB);
		if((this.DataLoader == null) || privacyV.contains(DatabaseTables.DBPLAYERDATA.toString()))
			this.DataLoader = 		new DataLoader(this, privacyV.contains(DatabaseTables.DBPLAYERDATA.toString()) ? DB : oldBaseDB);
		if((this.StatLoader == null) || privacyV.contains(DatabaseTables.DBSTATS.toString()))
			this.StatLoader = 		new StatLoader(privacyV.contains(DatabaseTables.DBSTATS.toString()) ? DB : oldBaseDB);
		if((this.PollLoader == null) || privacyV.contains(DatabaseTables.DBPOLLS.toString()))
			this.PollLoader = 		new PollLoader(privacyV.contains(DatabaseTables.DBPOLLS.toString()) ? DB : oldBaseDB);
		if((this.VFSLoader == null) || privacyV.contains(DatabaseTables.DBVFS.toString()))
			this.VFSLoader = 		new VFSLoader(privacyV.contains(DatabaseTables.DBVFS.toString()) ? DB : oldBaseDB);
		if((this.JournalLoader == null) || privacyV.contains(DatabaseTables.DBJOURNALS.toString()))
			this.JournalLoader = 	new JournalLoader(privacyV.contains(DatabaseTables.DBJOURNALS.toString()) ? DB : oldBaseDB);
		if((this.QuestLoader == null) || privacyV.contains(DatabaseTables.DBQUEST.toString()))
			this.QuestLoader = 		new QuestLoader(privacyV.contains(DatabaseTables.DBQUEST.toString()) ? DB : oldBaseDB);
		if((this.ClanLoader == null) || privacyV.contains(DatabaseTables.DBCLANS.toString()))
			this.ClanLoader = 		new ClanLoader(privacyV.contains(DatabaseTables.DBCLANS.toString()) ? DB : oldBaseDB);
		if((this.BackLogLoader == null) || privacyV.contains(DatabaseTables.DBBACKLOG.toString()))
			this.BackLogLoader = 	new BackLogLoader(privacyV.contains(DatabaseTables.DBBACKLOG.toString()) ? DB : oldBaseDB);
		if((this.CommandLoader == null) || privacyV.contains(DatabaseTables.DBCOMMANDS.toString()))
			this.CommandLoader = 	new GCommandLoader(privacyV.contains(DatabaseTables.DBCOMMANDS.toString()) ? DB : oldBaseDB);
	}

	@Override
	public CMObject newInstance()
	{
		return new DBInterface(DB, CMProps.getPrivateSubSet("DB.*"), this.changeList);
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
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
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
	public List<PlayerLibrary.ThinPlayer> vassals(final String liegeID)
	{
		return MOBloader.vassals(liegeID);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> worshippers(final String deityID)
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
	public int DBReadClanItems(final Map<String,Clan> clans)
	{
		return ClanLoader.DBReadClanItems(clans).size();
	}

	@Override
	public List<MemberRecord> DBReadClanMembers(final String clan)
	{
		return MOBloader.DBClanMembers(clan);
	}

	@Override
	public List<String> DBReadMemberClans(final String userID)
	{
		return MOBloader.DBMemberClans(userID);
	}

	@Override
	public void DBUpdateClanMembership(final String name, final String clan, final int role)
	{
		MOBloader.DBUpdateClanMembership(name, clan, role);
	}

	@Override
	public void DBUpdateClanKills(final String clan, final String name, final int adjMobKills, final int adjPlayerKills)
	{
		MOBloader.DBUpdateClanKills(clan, name, adjMobKills, adjPlayerKills);
	}

	@Override
	public void DBUpdateClanDonates(final String clan, final String name, final double adjGold, final int adjXP, final double adjDues)
	{
		MOBloader.DBUpdateClanDonates(clan, name, adjGold, adjXP, adjDues);
	}

	@Override
	public MemberRecord DBGetClanMember(final String clan, final String name)
	{
		return MOBloader.DBGetClanMember(clan, name);
	}

	@Override
	public void DBUpdateClan(final Clan C)
	{
		ClanLoader.DBUpdate(C);
	}

	@Override
	public void DBUpdateClanItems(final Clan C)
	{
		ClanLoader.DBUpdateItems(C);
	}

	@Override
	public void DBDeleteClan(final Clan C)
	{
		ClanLoader.DBDelete(C);
	}

	@Override
	public void DBCreateClan(final Clan C)
	{
		ClanLoader.DBCreate(C);
	}

	@Override
	public void DBUpdateEmail(final MOB mob)
	{
		MOBloader.DBUpdateEmail(mob);
	}


	@Override
	public String DBAccountEmailSearch(final String email)
	{
		return MOBloader.DBAccountEmailSearch(email);
	}

	@Override
	public String DBPlayerEmailSearch(final String email)
	{
		return MOBloader.DBPlayerEmailSearch(email);
	}

	@Override
	public void DBUpdatePassword(final String name, final String password)
	{
		MOBloader.DBUpdatePassword(name, password);
	}

	@Override
	public Pair<String, Boolean> DBFetchEmailData(final String name)
	{
		return MOBloader.DBFetchEmailData(name);
	}

	@Override
	public void DBUpdatePlayerAbilities(final MOB mob)
	{
		MOBloader.DBUpdateAbilities(mob);
	}

	@Override
	public void DBUpdatePlayerItems(final MOB mob)
	{
		MOBloader.DBUpdateItems(mob);
	}

	@Override
	public void DBUpdateFollowers(final MOB mob)
	{
		MOBloader.DBUpdateFollowers(mob);
	}

	@Override
	public void DBUpdateAccount(final PlayerAccount account)
	{
		MOBloader.DBUpdateAccount(account);
	}

	@Override
	public void DBCreateAccount(final PlayerAccount account)
	{
		MOBloader.DBCreateAccount(account);
	}

	@Override
	public PlayerAccount DBReadAccount(final String Login)
	{
		return MOBloader.DBReadAccount(Login);
	}

	@Override
	public List<PlayerAccount> DBListAccounts(final String mask)
	{
		return MOBloader.DBListAccounts(mask);
	}

	@Override
	public List<String> DBListAccountNames(final String mask)
	{
		return MOBloader.DBListAccountNames(mask);
	}


	@Override
	public void DBScanPrideAccountWinners(final CMCallback<Pair<String,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		MOBloader.DBScanPrideAccountWinners(callBack, scanCPUPercent);
	}

	@Override
	public void DBScanPridePlayerWinners(final CMCallback<Pair<ThinPlayer,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		MOBloader.DBScanPridePlayerWinners(callBack, scanCPUPercent);
	}

	@Override
	public void DBPlayerNameChange(final String oldName, final String newName)
	{
		MOBloader.DBNameChange(oldName, newName);
	}

	@Override
	public void DBReadAreaData(final Area A)
	{
		RoomLoader.DBReadArea(A);
	}

	@Override
	public List<String> findTaggedObjectRooms(final String tag)
	{
		return RoomLoader.findTaggedObjectRooms(tag);
	}

	@Override
	public String DBIsAreaName(final String name)
	{
		return RoomLoader.DBIsAreaName(name);
	}

	@Override
	public RoomContent[] DBReadAreaMobs(final String name)
	{
		return RoomLoader.DBReadAreaMobs(name);
	}

	@Override
	public RoomContent[] DBReadAreaItems(final String name)
	{
		return RoomLoader.DBReadAreaItems(name);
	}

	@Override
	public Room DBReadRoom(final String roomID, final boolean reportStatus)
	{
		return RoomLoader.DBReadRoomData(roomID, reportStatus);
	}

	@Override
	public boolean DBReadAreaFull(final String areaName)
	{
		return RoomLoader.DBReadAreaFull(areaName);
	}

	@Override
	public void DBReadAllRooms(final RoomnumberSet roomsToRead)
	{
		RoomLoader.DBReadAllRooms(roomsToRead);
	}

	@Override
	public int[] DBCountRoomMobsItems(final String roomID)
	{
		return RoomLoader.DBCountRoomMobsItems(roomID);
	}

	@Override
	public Room[] DBReadRoomObjects(final String areaName, final boolean reportStatus)
	{
		return RoomLoader.DBReadRoomObjects(areaName, reportStatus);
	}

	@Override
	public Set<String> DBReadAffectedRoomIDs(final Area parentA, final boolean metro, final String[] propIDs, final String[] propArgs)
	{
		return RoomLoader.DBReadAffectedRoomIDs(parentA, metro, propIDs, propArgs);
	}

	@Override
	public Map<Integer,Pair<String,String>> DBReadIncomingRoomExitIDsMap(final String roomID)
	{
		return RoomLoader.DBReadIncomingRoomExitIDsMap(roomID);
	}

	@Override
	public Room DBReadRoomObject(final String roomIDtoLoad, final boolean loadXML, final boolean reportStatus)
	{
		return RoomLoader.DBReadRoomObject(roomIDtoLoad, loadXML, reportStatus);
	}

	@Override
	public boolean DBReReadRoomData(final Room room)
	{
		return RoomLoader.DBReReadRoomData(room);
	}

	@Override
	public void DBReadRoomExits(final String roomID, final Room room, final boolean reportStatus)
	{
		RoomLoader.DBReadRoomExits(roomID, room, reportStatus);
	}

	@Override
	public Pair<String,String>[] DBReadRoomExitIDs(final String roomID)
	{
		return RoomLoader.getRoomExitIDs(roomID);
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

	private final static Set<ReadRoomDisableFlag> dbReadContentMakeLiveFlags=new XHashSet<ReadRoomDisableFlag>(ReadRoomDisableFlag.STATUS);
	private final static Set<ReadRoomDisableFlag> dbReadContentMakeDeadFlags=new XHashSet<ReadRoomDisableFlag>(
			new ReadRoomDisableFlag[] { ReadRoomDisableFlag.STATUS, ReadRoomDisableFlag.LIVE }
	);
	private final static Set<ReadRoomDisableFlag> dbReadContentMobFlags=new XHashSet<ReadRoomDisableFlag>(
			new ReadRoomDisableFlag[] { ReadRoomDisableFlag.STATUS, ReadRoomDisableFlag.LIVE, ReadRoomDisableFlag.ITEMS }
	);
	private final static Set<ReadRoomDisableFlag> dbReadContentItemFlags=new XHashSet<ReadRoomDisableFlag>(
			new ReadRoomDisableFlag[] { ReadRoomDisableFlag.STATUS, ReadRoomDisableFlag.LIVE, ReadRoomDisableFlag.MOBS }
	);

	@Override
	public void DBReadContent(final String roomID, final Room thisRoom, final boolean makeLive)
	{
		RoomLoader.DBReadContent(roomID, thisRoom, null, null,  makeLive?dbReadContentMakeLiveFlags:dbReadContentMakeDeadFlags);
	}

	@Override
	public void DBReadMobContent(final String roomID, final Room thisRoom)
	{
		RoomLoader.DBReadContent(roomID, thisRoom, null, null,  dbReadContentMobFlags);
	}

	@Override
	public void DBReadItemContent(final String roomID, final Room thisRoom)
	{
		RoomLoader.DBReadContent(roomID, thisRoom, null, null,  dbReadContentItemFlags);
	}

	@Override
	public boolean DBIsSavedRoomItemCopy(final String roomID, final String itemName)
	{
		return RoomLoader.DBIsSavedRoomItemCopy(roomID, itemName);
	}

	@Override
	public Item DBGetSavedRoomItemCopy(final String roomID, final String itemName)
	{
		return RoomLoader.DBGetSavedRoomItemCopy(roomID, itemName);
	}


	@Override
	public Area DBReadAreaObject(final String areaName)
	{
		return RoomLoader.DBReadAreaObject(areaName);
	}

	@Override
	public RoomnumberSet DBReadAreaRoomList(final String areaName, final boolean reportStatus)
	{
		return RoomLoader.DBReadAreaRoomList(areaName, reportStatus);
	}

	@Override
	public void DBCreateThisItem(final String roomID, final Item thisItem)
	{
		RoomLoader.DBCreateThisItem(roomID, thisItem);
	}

	@Override
	public void DBCreateThisMOB(final String roomID, final MOB thisMOB)
	{
		RoomLoader.DBCreateThisMOB(roomID, thisMOB);
	}

	@Override
	public void DBUpdateExits(final Room room)
	{
		RoomLoader.DBUpdateExits(room);
	}

	@Override
	public List<Quest> DBReadQuests()
	{
		return QuestLoader.DBRead();
	}

	@Override
	public void DBUpdateQuest(final Quest Q)
	{
		QuestLoader.DBUpdateQuest(Q);
	}

	@Override
	public void DBUpdateQuests(final List<Quest> quests)
	{
		QuestLoader.DBUpdateQuests(quests);
	}

	@Override
	public String DBReadRoomMOBMiscText(final String roomID, final String mobID)
	{
		return RoomLoader.DBReadRoomMOBMiscText(roomID, mobID);
	}

	@Override
	public String DBReadRoomDesc(final String roomID)
	{
		return RoomLoader.DBReadRoomDesc(roomID);
	}

	@Override
	public Item DBReadRoomItem(final String roomID, final String itemNum)
	{
		return RoomLoader.DBReadRoomItem(roomID, itemNum);
	}

	@Override
	public MOB DBReadRoomMOB(final String roomID, final String mobID)
	{
		return RoomLoader.DBReadRoomMOB(roomID, mobID);
	}

	@Override
	public void DBUpdateTheseMOBs(final Room room, final List<MOB> mobs)
	{
		RoomLoader.DBUpdateTheseMOBs(room, mobs);
	}

	@Override
	public void DBUpdateTheseItems(final Room room, final List<Item> items)
	{
		RoomLoader.DBUpdateTheseItems(room, items);
	}

	@Override
	public void DBUpdateMOBs(final Room room)
	{
		RoomLoader.DBUpdateMOBs(room);
	}

	@Override
	public void DBDeletePlayerPrivateJournalEntries(final String name)
	{
		JournalLoader.DBDeletePlayerPrivateJournalEntries(name);
	}

	@Override
	public void DBUpdateJournal(final String journalID, final JournalEntry entry)
	{
		JournalLoader.DBUpdateJournal(journalID, entry);
	}

	@Override
	public void DBUpdateJournalMetaData(final String journalID, final JournalsLibrary.JournalMetaData metaData)
	{
		JournalLoader.DBUpdateJournalMetaData(journalID, metaData);
	}

	@Override
	public void DBReadJournalMetaData(final String journalID, final JournalsLibrary.JournalMetaData metaData)
	{
		JournalLoader.DBReadJournalSummaryStats(journalID, metaData);
	}

	@Override
	public String DBGetRealJournalName(final String possibleName)
	{
		return JournalLoader.DBGetRealName(possibleName);
	}

	@Override
	public void DBDeleteJournal(final String journalID, final String msgKeyOrNull)
	{
		JournalLoader.DBDelete(journalID, msgKeyOrNull);
	}

	@Override
	public List<String> DBReadJournals()
	{
		return JournalLoader.DBReadJournals();
	}

	@Override
	public JournalEntry DBReadJournalEntry(final String journalID, final String messageKey)
	{
		return JournalLoader.DBReadJournalEntry(journalID, messageKey);
	}

	@Override
	public void DBUpdateMessageReplies(final String key, final int numReplies)
	{
		JournalLoader.DBUpdateMessageReplies(key, numReplies);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending, final int limit)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateRange(final String journalID, final String from, final long startRange, final long endRange)
	{
		return JournalLoader.DBReadJournalMsgsByUpdateRange(journalID, from, startRange, endRange);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByExpiRange(final String journalID, final String from, final long startRange, final long endRange, final String searchStr)
	{
		return JournalLoader.DBReadJournalMsgsByExpiRange(journalID, from, startRange, endRange, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadAllJournalMsgsByExpiDateStr(final String journalID, final long startRange, final String searchStr)
	{
		return JournalLoader.DBReadAllJournalMsgsByExpiDateStr(journalID, startRange, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByTimeStamps(final String journalID, final String from, final long startRange, final long endRange)
	{
		return JournalLoader.DBReadJournalMsgsByTimeStamps(journalID, from, startRange, endRange);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending, final int limit, final String[] tos)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending, final int limit)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending, final int limit, final String[] tos)
	{
		return JournalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, false);
	}

	@Override
	public List<JournalEntry> DBSearchAllJournalEntries(final String journalID, final String searchStr)
	{
		return JournalLoader.DBSearchAllJournalEntries(journalID, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsNewerThan(final String journalID, final String to, final long olderDate)
	{
		return JournalLoader.DBReadJournalMsgsNewerThan(journalID, to, olderDate);
	}

	@Override
	public int DBCountJournalMsgsNewerThan(final String journalID, final String to, final long olderDate)
	{
		return JournalLoader.DBCountJournalMsgsNewerThan(journalID, to, olderDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsOlderThan(final String journalID, final String to, final long newestDate)
	{
		return JournalLoader.DBReadJournalMsgsOlderThan(journalID, to, newestDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsExpiredBefore(final String journalID, final String to, final long newestDate)
	{
		return JournalLoader.DBReadJournalMsgsExpiredBefore(journalID, to, newestDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalPageMsgs(final String journalID, final String parent, final String searchStr, final long newerDate, final int limit)
	{
		return JournalLoader.DBReadJournalPageMsgs(journalID, parent, searchStr, newerDate, limit);
	}

	@Override
	public List<Long> DBReadJournalPages(final String journalID, final String parent, final String searchStr, final int limit)
	{
		return JournalLoader.DBReadJournalPages(journalID, parent, searchStr, limit);
	}

	@Override
	public int DBCountJournal(final String journalID, final String from, final String to)
	{
		return JournalLoader.DBCount(journalID, from, to);
	}

	@Override
	public long[] DBJournalLatestDateNewerThan(final String journalID, final String to, final long olderTime)
	{
		return JournalLoader.DBJournalLatestDateNewerThan(journalID, to, olderTime);
	}

	@Override
	public String DBWriteJournal(final String journalID, final JournalEntry entry)
	{
		return JournalLoader.DBWrite(journalID, entry);
	}

	@Override
	public String DBWriteJournal(final String journalID, final String from, final String to, final String subject, final String message)
	{
		return JournalLoader.DBWrite(journalID, from, to, subject, message);
	}

	@Override
	public String DBWriteJournalEmail(final String mailBoxID, final String journalSource, final String from, final String to, final String subject, final String message)
	{
		return JournalLoader.DBWrite(mailBoxID, journalSource, from, to, subject, message);
	}

	@Override
	public String DBWriteJournalChild(final String journalID, final String journalSource, final String from, final String to, final String parentKey, final String subject, final String message)
	{
		return JournalLoader.DBWrite(journalID, journalSource, from, to, parentKey, subject, message);
	}

	public String DBWrite(final String journalID, final JournalEntry entry)
	{
		return JournalLoader.DBWrite(journalID, entry);
	}

	@Override
	public JournalEntry DBWriteJournalReply(final String journalID, final String key, final String from, final String to, final String subject, final String message)
	{
		return JournalLoader.DBWriteJournalReply(journalID, key, from, to, subject, message);
	}

	@Override
	public void DBDeleteJournalMessagesByFrom(final String journal, final String from)
	{
		JournalLoader.DBDeleteByFrom(journal, from);
	}

	@Override
	public void DBUpdateJournal(final String key, final String subject, final String msg, final long newAttributes)
	{
		JournalLoader.DBUpdateJournal(key, subject, msg, newAttributes);
	}

	@Override
	public void DBUpdateJournalMessageViews(final String key, final int views)
	{
		JournalLoader.DBUpdateJournalMessageViews(key, views);
	}

	@Override
	public void DBTouchJournalMessage(final String key)
	{
		JournalLoader.DBTouchJournalMessage(key);
	}

	@Override
	public void DBTouchJournalMessage(final String key, final long newDate)
	{
		JournalLoader.DBTouchJournalMessage(key, newDate);
	}

	@Override
	public void DBCreateRoom(final Room room)
	{
		RoomLoader.DBCreate(room);
	}

	@Override
	public void DBUpdateRoom(final Room room)
	{
		RoomLoader.DBUpdateRoom(room);
	}

	@Override
	public List<Room> DBReadAreaNavStructure(final String areaName)
	{
		return RoomLoader.DBReadAreaNavStructure(areaName);
	}

	@Override
	public void DBUpdatePlayer(final MOB mob)
	{
		MOBloader.DBUpdate(mob);
	}

	@Override
	public void DBUpdatePlayerStartRooms(final String oldID, final String newID)
	{
		MOBloader.updatePlayerStartRooms(oldID, newID);
	}

	@Override
	public List<String> DBExpiredCharNameSearch(final Set<String> skipNames)
	{
		return MOBloader.DBExpiredCharNameSearch(skipNames);
	}

	@Override
	public void DBUpdatePlayerPlayerStats(final MOB mob)
	{
		MOBloader.DBUpdateJustPlayerStats(mob);
	}

	@Override
	public void DBUpdatePlayerMOBOnly(final MOB mob)
	{
		MOBloader.DBUpdateJustMOB(mob);
	}

	@Override
	public void DBUpdateMOB(final String roomID, final MOB mob)
	{
		RoomLoader.DBUpdateRoomMOB(roomID, mob);
	}

	@Override
	public void DBUpdateItem(final String roomID, final Item item)
	{
		RoomLoader.DBUpdateRoomItem(roomID, item);
	}

	@Override
	public void DBDeleteMOB(final String roomID, final MOB mob)
	{
		RoomLoader.DBDeleteRoomMOB(roomID, mob);
	}

	@Override
	public void DBDeleteItem(final String roomID, final Item item)
	{
		RoomLoader.DBDeleteRoomItem(roomID, item);
	}

	@Override
	public void DBUpdateItems(final Room room)
	{
		RoomLoader.DBUpdateItems(room);
	}

	@Override
	public void DBReCreate(final Room room, final String oldID)
	{
		RoomLoader.DBReCreate(room, oldID);
	}

	@Override
	public PlayerLibrary.ThinnerPlayer DBUserSearch(final String Login)
	{
		return MOBloader.DBUserSearch(Login);
	}

	@Override
	public String DBLeigeSearch(final String Login)
	{
		return MOBloader.DBLeigeSearch(Login);
	}

	@Override
	public PlayerStats DBLoadPlayerStats(final String name)
	{
		return MOBloader.DBLoadPlayerStats(name);
	}

	@Override
	public PairList<String, Long> DBSearchPFIL(final String match)
	{
		return MOBloader.DBSearchPFIL(match);
	}

	@Override
	public void DBCreateArea(final Area A)
	{
		RoomLoader.DBCreate(A);
	}

	@Override
	public void DBDeleteArea(final Area A)
	{
		RoomLoader.DBDelete(A);
	}

	@Override
	public void DBDeleteAreaAndRooms(final Area A)
	{
		RoomLoader.DBDeleteAreaAndRooms(A);
	}

	@Override
	public void DBUpdateArea(final String areaID, final Area A)
	{
		RoomLoader.DBUpdate(areaID, A);
	}

	@Override
	public void DBDeleteRoom(final Room room)
	{
		RoomLoader.DBDelete(room);
	}

	@Override
	public MOB DBReadPlayer(final String name)
	{
		return MOBloader.DBRead(name);
	}

	@Override
	public PairList<String,Integer> DBReadPlayerClans(final String name)
	{
		return MOBloader.DBReadPlayerClans(name);
	}

	@Override
	public Object DBReadPlayerValue(final String name, final PlayerCode code)
	{
		return MOBloader.DBReadPlayerValue(name, code);
	}

	@Override
	public void DBSetPlayerValue(final String name, final PlayerCode code, final Object value)
	{
		MOBloader.DBSetPlayerValue(name, code, value);
	}

	@Override
	public int DBReadPlayerBitmap(final String name)
	{
		return MOBloader.DBReadPlayerBitmap(name);
	}

	@Override
	public List<String[]> DBReadPlayerItemData(final String name, final Filterer<Pair<String,String>> classLocFilter, final Filterer<String> textFilter)
	{
		return MOBloader.DBReadPlayerItemData(name, classLocFilter, textFilter);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		return MOBloader.getExtendedUserList();
	}

	@Override
	public PlayerLibrary.ThinPlayer getThinUser(final String name)
	{
		return MOBloader.getThinUser(name);
	}

	@Override
	public void DBReadFollowers(final MOB mob, final boolean bringToLife)
	{
		MOBloader.DBReadFollowers(mob, bringToLife);
	}

	@Override
	public List<MOB> DBScanFollowers(final String mobName)
	{
		return MOBloader.DBScanFollowers(mobName);
	}

	@Override
	public void DBDeletePlayerOnly(final String mobName)
	{
		MOBloader.DBDeleteCharOnly(mobName);
	}

	@Override
	public void DBDeleteAccount(final PlayerAccount account)
	{
		MOBloader.DBDeleteAccount(account);
	}

	@Override
	public void DBCreateCharacter(final MOB mob)
	{
		MOBloader.DBCreateCharacter(mob);
	}

	@Override
	public void DBDeleteAllPlayerData(final String name)
	{
		DataLoader.DBDeletePlayer(name);
	}

	@Override
	public List<PlayerData> DBReadAllPlayerData(final String playerID)
	{
		return DataLoader.DBReadAllPlayerData(playerID);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(final String playerID, final String section)
	{
		return DataLoader.DBRead(playerID, section);
	}

	@Override
	public List<PlayerData> DBReadPlayerDataByKeyMask(final String section, final String keyMask)
	{
		return DataLoader.DBReadByKeyMask(section, keyMask);
	}

	@Override
	public List<PlayerData> DBReadPlayerDataEntry(final String key)
	{
		return DataLoader.DBReadKey(key);
	}

	@Override
	public int DBCountPlayerData(final String playerID, final String section)
	{
		return DataLoader.DBCount(playerID, section);
	}

	@Override
	public List<String> DBReadPlayerDataKeys(final String playerID, final String section)
	{
		return DataLoader.DBReadPlayerDataKeys(playerID, section);
	}

	@Override
	public int DBCountPlayerData(final String section)
	{
		return DataLoader.DBCountBySection(section);
	}

	@Override
	public List<String> DBReadPlayerDataAuthorsBySection(final String section)
	{
		return DataLoader.DBReadAuthorsBySection(section);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(final String playerID, final String section, final String key)
	{
		return DataLoader.DBRead(playerID, section, key);
	}

	@Override
	public List<PlayerData> DBReadPlayerSectionData(final String section)
	{
		return DataLoader.DBRead(section);
	}

	@Override
	public List<String> DBReadPlayerDataPlayersBySection(final String section)
	{
		return DataLoader.DBReadNames(section);
	}

	@Override
	public boolean DBExistsPlayerData(final String section, final String name)
	{
		return DataLoader.DBExistsData(section, name);
	}

	@Override
	public List<PlayerData> DBReadPlayerData(final String player, final List<String> sections)
	{
		return DataLoader.DBRead(player, sections);
	}

	@Override
	public Set<String> DBReadUniqueSections(final String name)
	{
		return DataLoader.DBReadSections(name);
	}

	@Override
	public void DBDeletePlayerData(final String playerID, final String section)
	{
		DataLoader.DBDelete(playerID, section);
	}

	@Override
	public void DBDeletePlayerData(final String playerID, final String section, final String key)
	{
		DataLoader.DBDelete(playerID, section, key);
	}

	@Override
	public void DBDeletePlayerSectionData(final String section)
	{
		DataLoader.DBDelete(section);
	}

	@Override
	public PlayerData DBReCreatePlayerData(final String name, final String section, final String key, final String xml)
	{
		return DataLoader.DBReCreate(name, section, key, xml);
	}

	@Override
	public void DBUpdatePlayerData(final String name, final String section, final String key, final String xml)
	{
		DataLoader.DBUpdate(name, section, key, xml);
	}

	@Override
	public PlayerData DBCreatePlayerData(final String player, final String section, final String key, final String data)
	{
		return DataLoader.DBCreate(player, section, key, data);
	}

	@Override
	public List<AckRecord> DBReadRaces()
	{
		return GRaceLoader.DBReadRaces();
	}

	@Override
	public void DBUpdateRaceCreationDate(final String raceID)
	{
		GRaceLoader.DBUpdateRaceCreationDate(raceID);
	}

	@Override
	public boolean isRaceExpired(final String raceID)
	{
		return GRaceLoader.isRaceExpired(raceID);
	}

	@Override
	public void registerRaceUsed(final Race R)
	{
		GRaceLoader.registerRaceUsed(R);
	}

	@Override
	public int pruneOldRaces()
	{
		return GRaceLoader.DBPruneOldRaces();
	}

	@Override
	public int updateAllRaceDates()
	{
		return GRaceLoader.updateAllRaceDates();
	}

	@Override
	public void DBDeleteRace(final String raceID)
	{
		GRaceLoader.DBDeleteRace(raceID);
	}

	@Override
	public void DBCreateRace(final String raceID, final String data)
	{
		GRaceLoader.DBCreateRace(raceID, data);
	}

	@Override
	public List<AckRecord> DBReadClasses()
	{
		return GCClassLoader.DBReadClasses();
	}

	@Override
	public void DBDeleteClass(final String classID)
	{
		GCClassLoader.DBDeleteClass(classID);
	}

	@Override
	public void DBCreateClass(final String classID, final String data)
	{
		GCClassLoader.DBCreateClass(classID, data);
	}

	@Override
	public List<AckRecord> DBReadAbilities()
	{
		return GAbilityLoader.DBReadAbilities();
	}

	@Override
	public void DBDeleteAbility(final String classID)
	{
		GAbilityLoader.DBDeleteAbility(classID);
	}

	@Override
	public void DBCreateAbility(final String classID, final String typeClass, final String data)
	{
		GAbilityLoader.DBCreateAbility(classID, typeClass, data);
	}

	@Override
	public List<AckRecord> DBReadCommands()
	{
		return CommandLoader.DBReadCommands();
	}

	@Override
	public AckRecord DBDeleteCommand(final String classID)
	{
		return CommandLoader.DBDeleteCommand(classID);
	}

	@Override
	public void DBCreateCommand(final String classID, final String baseClassID, final String data)
	{
		CommandLoader.DBCreateCommand(classID, baseClassID, data);
	}

	@Override
	public void DBReadArtifacts()
	{
		DataLoader.DBReadArtifacts();
	}

	@Override
	public CoffeeTableRow DBReadStat(final long startTime)
	{
		return StatLoader.DBRead(startTime);
	}

	@Override
	public void DBDeleteStat(final long startTime)
	{
		StatLoader.DBDelete(startTime);
	}

	@Override
	public boolean DBCreateStat(final long startTime, final long endTime, final String data)
	{
		return StatLoader.DBCreate(startTime, endTime, data);
	}

	@Override
	public boolean DBUpdateStat(final long startTime, final String data)
	{
		return StatLoader.DBUpdate(startTime, data);
	}

	@Override
	public List<CoffeeTableRow> DBReadStats(final long startTime, final long endTime)
	{
		return StatLoader.DBReadAfter(startTime, endTime);
	}

	@Override
	public long DBReadOldestStatMs()
	{
		return StatLoader.DBReadOldestStatMs();
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
	public void DBCreatePoll(final String name, final String player, final String subject, final String description, final String optionXML, final int flag, final String qualZapper, final String results, final long expiration)
	{
		PollLoader.DBCreate(name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePoll(final String oldName, final String name, final String player, final String subject, final String description, final String optionXML, final int flag, final String qualZapper, final String results, final long expiration)
	{
		PollLoader.DBUpdate(oldName, name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePollResults(final String name, final String results)
	{
		PollLoader.DBUpdate(name, results);
	}

	@Override
	public void DBDeletePoll(final String name)
	{
		PollLoader.DBDelete(name);
	}

	@Override
	public List<PollData> DBReadPollList()
	{
		return PollLoader.DBReadList();
	}

	@Override
	public PollData DBReadPoll(final String name)
	{
		return PollLoader.DBRead(name);
	}

	@Override
	public CMFile.CMVFSDir DBReadVFSDirectory()
	{
		return VFSLoader.DBReadDirectory();
	}

	@Override
	public CMFile.CMVFSFile DBReadVFSFile(final String filename)
	{
		return VFSLoader.DBRead(filename);
	}

	@Override
	public List<String> DBReadVFSKeysLike(final String partialFilename, final int minMask)
	{
		return VFSLoader.DBReadKeysLike(partialFilename, minMask);
	}

	@Override
	public void DBCreateVFSFile(final String filename, final int bits, final String creator, final long updateTime, final Object data)
	{
		VFSLoader.DBCreate(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBUpSertVFSFile(final String filename, final int bits, final String creator, final long updateTime, final Object data)
	{
		VFSLoader.DBUpSert(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBDeleteVFSFile(final String filename)
	{
		VFSLoader.DBDelete(filename);
	}

	@Override
	public void DBDeleteVFSFileLike(final String partialFilename, final int minMask)
	{
		VFSLoader.DBDeleteLike(partialFilename, minMask);
	}

	@Override
	public List<Triad<String, Integer, Long>> getBackLogEntries(final String channelName, final int subNameField, final int newestToSkip, final int numToReturn)
	{
		return BackLogLoader.getBackLogEntries(channelName, subNameField, newestToSkip, numToReturn);
	}

	@Override
	public int getLowestBackLogIndex(final String channelName, final int subNameField, final long afterDate)
	{
		return BackLogLoader.getLowestBackLogIndex(channelName, subNameField, afterDate);
	}

	@Override
	public List<Triad<String, Integer, Long>> searchBackLogEntries(final String channelName, final int subNameField, final String search, final int numToReturn)
	{
		return BackLogLoader.searchBackLogEntries(channelName, subNameField, search, numToReturn);
	}

	@Override
	public int getBackLogPageEnd(final String channelName, final int subNameField)
	{
		return BackLogLoader.getBackLogPageEnd(channelName, subNameField);
	}

	@Override
	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime)
	{
		BackLogLoader.trimBackLogEntries(channels, maxMessages, oldestTime);
	}

	@Override
	public void addBackLogEntry(final String channelName, final int subNameField, final long timeStamp, final String entry)
	{
		BackLogLoader.addBackLogEntry(channelName, subNameField, entry);
	}

	@Override
	public void checkUpgradeBacklogTable(final ChannelsLibrary channels)
	{
		BackLogLoader.checkUpgradeBacklogTable(channels);
	}

	@Override
	public void delBackLogEntry(final String channelName, final long timeStamp)
	{
		BackLogLoader.delBackLogEntry(channelName, timeStamp);
	}

	@Override
	public int DBRawExecute(final String sql) throws CMException
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
	public List<String[]> DBRawQuery(final String sql) throws CMException
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

	@Override
	public String validateDatabaseVersion()
	{
		return new DDLValidator(DB,changeList).validateDatabaseVersion();
	}

	@Override
	public String upgradeDatabaseVersion()
	{
		return new DDLValidator(DB,changeList).upgradeDatabaseVersion();
	}

	@Override
	public String L(final Class<?> clazz, final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(clazz, str, xs);
	}
}
