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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
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

	MOBloader		mobLoader		= null;
	RoomLoader		roomLoader		= null;
	PDataLoader		pDataLoader		= null;
	ADataLoader		aDataLoader		= null;
	StatLoader		statLoader		= null;
	PollLoader		pollLoader		= null;
	VFSLoader		vfsLoader		= null;
	JournalLoader	journalLoader	= null;
	QuestLoader		questLoader		= null;
	GAbilityLoader	gAbilityLoader	= null;
	GRaceLoader		gRaceLoader		= null;
	GCClassLoader	gcClassLoader	= null;
	ClanLoader		clanLoader		= null;
	BackLogLoader	backLogLoader	= null;
	GCommandLoader	commandLoader	= null;
	DBConnector		dbConnector				= null;
	JSONObject		changeList		= null;

	public DBInterface(final DBConnector DB, Set<String> privacyV, final JSONObject changeList)
	{
		this.dbConnector=DB;
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
				this.gAbilityLoader = ((DBInterface) baseEngine).gAbilityLoader;
				this.gcClassLoader = ((DBInterface) baseEngine).gcClassLoader;
				this.gRaceLoader = ((DBInterface) baseEngine).gRaceLoader;
				this.mobLoader = ((DBInterface) baseEngine).mobLoader;
				this.roomLoader = ((DBInterface) baseEngine).roomLoader;
				this.pDataLoader = ((DBInterface) baseEngine).pDataLoader;
				this.aDataLoader = ((DBInterface) baseEngine).aDataLoader;
				this.statLoader = ((DBInterface) baseEngine).statLoader;
				this.pollLoader = ((DBInterface) baseEngine).pollLoader;
				this.vfsLoader = ((DBInterface) baseEngine).vfsLoader;
				this.journalLoader = ((DBInterface) baseEngine).journalLoader;
				this.questLoader = ((DBInterface) baseEngine).questLoader;
				this.clanLoader = ((DBInterface) baseEngine).clanLoader;
				this.backLogLoader = ((DBInterface) baseEngine).backLogLoader;
				this.commandLoader = ((DBInterface) baseEngine).commandLoader;
			}
		}

		if((this.gAbilityLoader == null) || privacyV.contains(DatabaseTables.DBABILITY.toString()))
			this.gAbilityLoader = 	new GAbilityLoader(privacyV.contains(DatabaseTables.DBABILITY.toString()) ? DB : oldBaseDB);
		if((this.gcClassLoader == null) || privacyV.contains(DatabaseTables.DBCHARCLASS.toString()))
			this.gcClassLoader = 	new GCClassLoader(privacyV.contains(DatabaseTables.DBCHARCLASS.toString()) ? DB : oldBaseDB);
		if((this.gRaceLoader == null) || privacyV.contains(DatabaseTables.DBRACE.toString()))
			this.gRaceLoader = 		new GRaceLoader(privacyV.contains(DatabaseTables.DBRACE.toString()) ? DB : oldBaseDB);
		if((this.mobLoader == null) || privacyV.contains(DatabaseTables.DBPLAYERS.toString()))
			this.mobLoader = 		new MOBloader(privacyV.contains(DatabaseTables.DBPLAYERS.toString()) ? DB : oldBaseDB);
		if((this.roomLoader == null) || privacyV.contains(DatabaseTables.DBMAP.toString()))
			this.roomLoader = 		new RoomLoader(privacyV.contains(DatabaseTables.DBMAP.toString()) ? DB : oldBaseDB);
		if((this.pDataLoader == null) || privacyV.contains(DatabaseTables.DBPLAYERDATA.toString()))
			this.pDataLoader = 		new PDataLoader(this, privacyV.contains(DatabaseTables.DBPLAYERDATA.toString()) ? DB : oldBaseDB);
		if((this.aDataLoader == null) || privacyV.contains(DatabaseTables.DBMAP.toString()))
			this.aDataLoader = 		new ADataLoader(this, privacyV.contains(DatabaseTables.DBMAP.toString()) ? DB : oldBaseDB);
		if((this.statLoader == null) || privacyV.contains(DatabaseTables.DBSTATS.toString()))
			this.statLoader = 		new StatLoader(privacyV.contains(DatabaseTables.DBSTATS.toString()) ? DB : oldBaseDB);
		if((this.pollLoader == null) || privacyV.contains(DatabaseTables.DBPOLLS.toString()))
			this.pollLoader = 		new PollLoader(privacyV.contains(DatabaseTables.DBPOLLS.toString()) ? DB : oldBaseDB);
		if((this.vfsLoader == null) || privacyV.contains(DatabaseTables.DBVFS.toString()))
			this.vfsLoader = 		new VFSLoader(privacyV.contains(DatabaseTables.DBVFS.toString()) ? DB : oldBaseDB);
		if((this.journalLoader == null) || privacyV.contains(DatabaseTables.DBJOURNALS.toString()))
			this.journalLoader = 	new JournalLoader(privacyV.contains(DatabaseTables.DBJOURNALS.toString()) ? DB : oldBaseDB);
		if((this.questLoader == null) || privacyV.contains(DatabaseTables.DBQUEST.toString()))
			this.questLoader = 		new QuestLoader(privacyV.contains(DatabaseTables.DBQUEST.toString()) ? DB : oldBaseDB);
		if((this.clanLoader == null) || privacyV.contains(DatabaseTables.DBCLANS.toString()))
			this.clanLoader = 		new ClanLoader(privacyV.contains(DatabaseTables.DBCLANS.toString()) ? DB : oldBaseDB);
		if((this.backLogLoader == null) || privacyV.contains(DatabaseTables.DBBACKLOG.toString()))
			this.backLogLoader = 	new BackLogLoader(privacyV.contains(DatabaseTables.DBBACKLOG.toString()) ? DB : oldBaseDB);
		if((this.commandLoader == null) || privacyV.contains(DatabaseTables.DBCOMMANDS.toString()))
			this.commandLoader = 	new GCommandLoader(privacyV.contains(DatabaseTables.DBCOMMANDS.toString()) ? DB : oldBaseDB);
	}

	@Override
	public CMObject newInstance()
	{
		return new DBInterface(dbConnector, CMProps.getPrivateSubSet("DB.*"), this.changeList);
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
		return dbConnector;
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
		return mobLoader.vassals(liegeID);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> worshippers(final String deityID)
	{
		return mobLoader.worshippers(deityID);
	}

	@Override
	public List<String> getUserList()
	{
		return mobLoader.getUserList();
	}

	@Override
	public boolean isConnected()
	{
		return dbConnector.amIOk();
	}

	@Override
	public List<Clan> DBReadAllClans()
	{
		return clanLoader.DBRead();
	}

	@Override
	public int DBReadClanItems(final Map<String,Clan> clans)
	{
		return clanLoader.DBReadClanItems(clans).size();
	}

	@Override
	public List<MemberRecord> DBReadClanMembers(final String clan)
	{
		return mobLoader.DBClanMembers(clan);
	}

	@Override
	public List<String> DBReadMemberClans(final String userID)
	{
		return mobLoader.DBMemberClans(userID);
	}

	@Override
	public void DBUpdateClanMembership(final String name, final String clan, final int role)
	{
		mobLoader.DBUpdateClanMembership(name, clan, role);
	}

	@Override
	public void DBUpdateClanKills(final String clan, final String name, final int adjMobKills, final int adjPlayerKills)
	{
		mobLoader.DBUpdateClanKills(clan, name, adjMobKills, adjPlayerKills);
	}

	@Override
	public void DBUpdateClanDonates(final String clan, final String name, final double adjGold, final int adjXP, final double adjDues)
	{
		mobLoader.DBUpdateClanDonates(clan, name, adjGold, adjXP, adjDues);
	}

	@Override
	public MemberRecord DBGetClanMember(final String clan, final String name)
	{
		return mobLoader.DBGetClanMember(clan, name);
	}

	@Override
	public void DBUpdateClan(final Clan C)
	{
		clanLoader.DBUpdate(C);
	}

	@Override
	public void DBUpdateClanItems(final Clan C)
	{
		clanLoader.DBUpdateItems(C);
	}

	@Override
	public void DBDeleteClan(final Clan C)
	{
		clanLoader.DBDelete(C);
	}

	@Override
	public void DBCreateClan(final Clan C)
	{
		clanLoader.DBCreate(C);
	}

	@Override
	public void DBUpdateEmail(final MOB mob)
	{
		mobLoader.DBUpdateEmail(mob);
	}


	@Override
	public String DBAccountEmailSearch(final String email)
	{
		return mobLoader.DBAccountEmailSearch(email);
	}

	@Override
	public String DBPlayerEmailSearch(final String email)
	{
		return mobLoader.DBPlayerEmailSearch(email);
	}

	@Override
	public void DBUpdatePassword(final String name, final String password)
	{
		mobLoader.DBUpdatePassword(name, password);
	}

	@Override
	public Pair<String, Boolean> DBFetchEmailData(final String name)
	{
		return mobLoader.DBFetchEmailData(name);
	}

	@Override
	public void DBUpdatePlayerAbilities(final MOB mob)
	{
		mobLoader.DBUpdateAbilities(mob);
	}

	@Override
	public void DBUpdatePlayerItems(final MOB mob)
	{
		mobLoader.DBUpdateItems(mob);
	}

	@Override
	public void DBUpdateFollowers(final MOB mob)
	{
		mobLoader.DBUpdateFollowers(mob);
	}

	@Override
	public void DBUpdateAccount(final PlayerAccount account)
	{
		mobLoader.DBUpdateAccount(account);
	}

	@Override
	public void DBCreateAccount(final PlayerAccount account)
	{
		mobLoader.DBCreateAccount(account);
	}

	@Override
	public PlayerAccount DBReadAccount(final String Login)
	{
		return mobLoader.DBReadAccount(Login);
	}

	@Override
	public List<PlayerAccount> DBListAccounts(final String mask)
	{
		return mobLoader.DBListAccounts(mask);
	}

	@Override
	public List<String> DBListAccountNames(final String mask)
	{
		return mobLoader.DBListAccountNames(mask);
	}


	@Override
	public void DBScanPrideAccountWinners(final CMCallback<Pair<String,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		mobLoader.DBScanPrideAccountWinners(callBack, scanCPUPercent);
	}

	@Override
	public void DBScanPridePlayerWinners(final CMCallback<Pair<ThinPlayer,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		mobLoader.DBScanPridePlayerWinners(callBack, scanCPUPercent);
	}

	@Override
	public void DBPlayerNameChange(final String oldName, final String newName)
	{
		mobLoader.DBNameChange(oldName, newName);
	}

	@Override
	public void DBReadAreaData(final Area A)
	{
		roomLoader.DBReadArea(A);
	}

	@Override
	public List<String> findTaggedObjectRooms(final String tag)
	{
		return roomLoader.findTaggedObjectRooms(tag);
	}

	@Override
	public String DBIsAreaName(final String name)
	{
		return roomLoader.DBIsAreaName(name);
	}

	@Override
	public RoomContent[] DBReadAreaMobs(final String name)
	{
		return roomLoader.DBReadAreaMobs(name);
	}

	@Override
	public RoomContent[] DBReadAreaItems(final String name)
	{
		return roomLoader.DBReadAreaItems(name);
	}

	@Override
	public Room DBReadRoom(final String roomID, final boolean reportStatus)
	{
		return roomLoader.DBReadRoomData(roomID, reportStatus);
	}

	@Override
	public boolean DBReadAreaFull(final String areaName)
	{
		return roomLoader.DBReadAreaFull(areaName);
	}

	@Override
	public void DBReadAllRooms(final RoomnumberSet roomsToRead)
	{
		roomLoader.DBReadAllRooms(roomsToRead);
	}

	@Override
	public int[] DBCountRoomMobsItems(final String roomID)
	{
		return roomLoader.DBCountRoomMobsItems(roomID);
	}

	@Override
	public Room[] DBReadRoomObjects(final String areaName, final boolean reportStatus)
	{
		return roomLoader.DBReadRoomObjects(areaName, reportStatus);
	}

	@Override
	public Set<String> DBReadAffectedRoomIDs(final Area parentA, final boolean metro, final String[] propIDs, final String[] propArgs)
	{
		return roomLoader.DBReadAffectedRoomIDs(parentA, metro, propIDs, propArgs);
	}

	@Override
	public Map<Integer,Pair<String,String>> DBReadIncomingRoomExitIDsMap(final String roomID)
	{
		return roomLoader.DBReadIncomingRoomExitIDsMap(roomID);
	}

	@Override
	public Room DBReadRoomObject(final String roomIDtoLoad, final boolean loadXML, final boolean reportStatus)
	{
		return roomLoader.DBReadRoomObject(roomIDtoLoad, loadXML, reportStatus);
	}

	@Override
	public boolean DBReReadRoomData(final Room room)
	{
		return roomLoader.DBReReadRoomData(room);
	}

	@Override
	public void DBReadRoomExits(final String roomID, final Room room, final boolean reportStatus)
	{
		roomLoader.DBReadRoomExits(roomID, room, reportStatus);
	}

	@Override
	public Pair<String,String>[] DBReadRoomExitIDs(final String roomID)
	{
		return roomLoader.getRoomExitIDs(roomID);
	}

	@Override
	public void DBReadCatalogs()
	{
		roomLoader.DBReadCatalogs();
	}

	@Override
	public void DBReadSpace()
	{
		roomLoader.DBReadSpace();
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
		roomLoader.DBReadContent(roomID, thisRoom, null, null,  makeLive?dbReadContentMakeLiveFlags:dbReadContentMakeDeadFlags);
	}

	@Override
	public void DBReadMobContent(final String roomID, final Room thisRoom)
	{
		roomLoader.DBReadContent(roomID, thisRoom, null, null,  dbReadContentMobFlags);
	}

	@Override
	public void DBReadItemContent(final String roomID, final Room thisRoom)
	{
		roomLoader.DBReadContent(roomID, thisRoom, null, null,  dbReadContentItemFlags);
	}

	@Override
	public boolean DBIsSavedRoomItemCopy(final String roomID, final String itemName)
	{
		return roomLoader.DBIsSavedRoomItemCopy(roomID, itemName);
	}

	@Override
	public Item DBGetSavedRoomItemCopy(final String roomID, final String itemName)
	{
		return roomLoader.DBGetSavedRoomItemCopy(roomID, itemName);
	}


	@Override
	public Area DBReadAreaObject(final String areaName)
	{
		return roomLoader.DBReadAreaObject(areaName);
	}

	@Override
	public RoomnumberSet DBReadAreaRoomList(final String areaName, final boolean reportStatus)
	{
		return roomLoader.DBReadAreaRoomList(areaName, reportStatus);
	}

	@Override
	public void DBCreateThisItem(final String roomID, final Item thisItem)
	{
		roomLoader.DBCreateThisItem(roomID, thisItem);
	}

	@Override
	public void DBCreateThisMOB(final String roomID, final MOB thisMOB)
	{
		roomLoader.DBCreateThisMOB(roomID, thisMOB);
	}

	@Override
	public void DBUpdateExits(final Room room)
	{
		roomLoader.DBUpdateExits(room);
	}

	@Override
	public List<Quest> DBReadQuests()
	{
		return questLoader.DBRead();
	}

	@Override
	public void DBUpdateQuest(final Quest Q)
	{
		questLoader.DBUpdateQuest(Q);
	}

	@Override
	public void DBUpdateQuests(final List<Quest> quests)
	{
		questLoader.DBUpdateQuests(quests);
	}

	@Override
	public String DBReadRoomMOBMiscText(final String roomID, final String mobID)
	{
		return roomLoader.DBReadRoomMOBMiscText(roomID, mobID);
	}

	@Override
	public String DBReadRoomDesc(final String roomID)
	{
		return roomLoader.DBReadRoomDesc(roomID);
	}

	@Override
	public Item DBReadRoomItem(final String roomID, final String itemNum)
	{
		return roomLoader.DBReadRoomItem(roomID, itemNum);
	}

	@Override
	public MOB DBReadRoomMOB(final String roomID, final String mobID)
	{
		return roomLoader.DBReadRoomMOB(roomID, mobID);
	}

	@Override
	public void DBUpdateTheseMOBs(final Room room, final List<MOB> mobs)
	{
		roomLoader.DBUpdateTheseMOBs(room, mobs);
	}

	@Override
	public void DBUpdateTheseItems(final Room room, final List<Item> items)
	{
		roomLoader.DBUpdateTheseItems(room, items);
	}

	@Override
	public void DBUpdateMOBs(final Room room)
	{
		roomLoader.DBUpdateMOBs(room);
	}

	@Override
	public void DBDeletePlayerPrivateJournalEntries(final String name)
	{
		journalLoader.DBDeletePlayerPrivateJournalEntries(name);
	}

	@Override
	public void DBUpdateJournal(final String journalID, final JournalEntry entry)
	{
		journalLoader.DBUpdateJournal(journalID, entry);
	}

	@Override
	public void DBUpdateJournalMetaData(final String journalID, final JournalsLibrary.JournalMetaData metaData)
	{
		journalLoader.DBUpdateJournalMetaData(journalID, metaData);
	}

	@Override
	public void DBReadJournalMetaData(final String journalID, final JournalsLibrary.JournalMetaData metaData)
	{
		journalLoader.DBReadJournalSummaryStats(journalID, metaData);
	}

	@Override
	public String DBGetRealJournalName(final String possibleName)
	{
		return journalLoader.DBGetRealName(possibleName);
	}

	@Override
	public void DBDeleteJournal(final String journalID, final String msgKeyOrNull)
	{
		journalLoader.DBDelete(journalID, msgKeyOrNull);
	}

	@Override
	public List<String> DBReadJournals()
	{
		return journalLoader.DBReadJournals();
	}

	@Override
	public JournalEntry DBReadJournalEntry(final String journalID, final String messageKey)
	{
		return journalLoader.DBReadJournalEntry(journalID, messageKey);
	}

	@Override
	public void DBUpdateMessageReplies(final String key, final int numReplies)
	{
		journalLoader.DBUpdateMessageReplies(key, numReplies);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending, final int limit)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateRange(final String journalID, final String from, final long startRange, final long endRange)
	{
		return journalLoader.DBReadJournalMsgsByUpdateRange(journalID, from, startRange, endRange);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByExpiRange(final String journalID, final String from, final long startRange, final long endRange, final String searchStr)
	{
		return journalLoader.DBReadJournalMsgsByExpiRange(journalID, from, startRange, endRange, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadAllJournalMsgsByExpiDateStr(final String journalID, final long startRange, final String searchStr)
	{
		return journalLoader.DBReadAllJournalMsgsByExpiDateStr(journalID, startRange, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByTimeStamps(final String journalID, final String from, final long startRange, final long endRange)
	{
		return journalLoader.DBReadJournalMsgsByTimeStamps(journalID, from, startRange, endRange);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByUpdateDate(final String journalID, final boolean ascending, final int limit, final String[] tos)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, true);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, Integer.MAX_VALUE, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending, final int limit)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, false);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsByCreateDate(final String journalID, final boolean ascending, final int limit, final String[] tos)
	{
		return journalLoader.DBReadJournalMsgsSorted(journalID, ascending, limit, tos, false);
	}

	@Override
	public List<JournalEntry> DBSearchAllJournalEntries(final String journalID, final String searchStr)
	{
		return journalLoader.DBSearchAllJournalEntries(journalID, searchStr);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsNewerThan(final String journalID, final String to, final long olderDate)
	{
		return journalLoader.DBReadJournalMsgsNewerThan(journalID, to, olderDate);
	}

	@Override
	public int DBCountJournalMsgsNewerThan(final String journalID, final String to, final long olderDate)
	{
		return journalLoader.DBCountJournalMsgsNewerThan(journalID, to, olderDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsOlderThan(final String journalID, final String to, final long newestDate)
	{
		return journalLoader.DBReadJournalMsgsOlderThan(journalID, to, newestDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalMsgsExpiredBefore(final String journalID, final String to, final long newestDate)
	{
		return journalLoader.DBReadJournalMsgsExpiredBefore(journalID, to, newestDate);
	}

	@Override
	public List<JournalEntry> DBReadJournalPageMsgs(final String journalID, final String parent, final String searchStr, final long newerDate, final int limit)
	{
		return journalLoader.DBReadJournalPageMsgs(journalID, parent, searchStr, newerDate, limit);
	}

	@Override
	public List<Long> DBReadJournalPages(final String journalID, final String parent, final String searchStr, final int limit)
	{
		return journalLoader.DBReadJournalPages(journalID, parent, searchStr, limit);
	}

	@Override
	public int DBCountJournal(final String journalID, final String from, final String to)
	{
		return journalLoader.DBCount(journalID, from, to);
	}

	@Override
	public long[] DBJournalLatestDateNewerThan(final String journalID, final String to, final long olderTime)
	{
		return journalLoader.DBJournalLatestDateNewerThan(journalID, to, olderTime);
	}

	@Override
	public String DBWriteJournal(final String journalID, final JournalEntry entry)
	{
		return journalLoader.DBWrite(journalID, entry);
	}

	@Override
	public String DBWriteJournal(final String journalID, final String from, final String to, final String subject, final String message)
	{
		return journalLoader.DBWrite(journalID, from, to, subject, message);
	}

	@Override
	public String DBWriteJournalEmail(final String mailBoxID, final String journalSource, final String from, final String to, final String subject, final String message)
	{
		return journalLoader.DBWrite(mailBoxID, journalSource, from, to, subject, message);
	}

	@Override
	public String DBWriteJournalChild(final String journalID, final String journalSource, final String from, final String to, final String parentKey, final String subject, final String message)
	{
		return journalLoader.DBWrite(journalID, journalSource, from, to, parentKey, subject, message);
	}

	public String DBWrite(final String journalID, final JournalEntry entry)
	{
		return journalLoader.DBWrite(journalID, entry);
	}

	@Override
	public JournalEntry DBWriteJournalReply(final String journalID, final String key, final String from, final String to, final String subject, final String message)
	{
		return journalLoader.DBWriteJournalReply(journalID, key, from, to, subject, message);
	}

	@Override
	public void DBDeleteJournalMessagesByFrom(final String journal, final String from)
	{
		journalLoader.DBDeleteByFrom(journal, from);
	}

	@Override
	public void DBUpdateJournal(final String key, final String subject, final String msg, final long newAttributes)
	{
		journalLoader.DBUpdateJournal(key, subject, msg, newAttributes);
	}

	@Override
	public void DBUpdateJournalMessageViews(final String key, final int views)
	{
		journalLoader.DBUpdateJournalMessageViews(key, views);
	}

	@Override
	public void DBTouchJournalMessage(final String key)
	{
		journalLoader.DBTouchJournalMessage(key);
	}

	@Override
	public void DBTouchJournalMessage(final String key, final long newDate)
	{
		journalLoader.DBTouchJournalMessage(key, newDate);
	}

	@Override
	public void DBCreateRoom(final Room room)
	{
		roomLoader.DBCreate(room);
	}

	@Override
	public void DBUpdateRoom(final Room room)
	{
		roomLoader.DBUpdateRoom(room);
	}

	@Override
	public List<Room> DBReadAreaNavStructure(final String areaName)
	{
		return roomLoader.DBReadAreaNavStructure(areaName);
	}

	@Override
	public void DBUpdatePlayer(final MOB mob)
	{
		mobLoader.DBUpdate(mob);
	}

	@Override
	public void DBUpdatePlayerStartRooms(final String oldID, final String newID)
	{
		mobLoader.updatePlayerStartRooms(oldID, newID);
	}

	@Override
	public List<String> DBExpiredCharNameSearch(final Set<String> skipNames)
	{
		return mobLoader.DBExpiredCharNameSearch(skipNames);
	}

	@Override
	public void DBUpdatePlayerPlayerStats(final MOB mob)
	{
		mobLoader.DBUpdateJustPlayerStats(mob);
	}

	@Override
	public void DBUpdatePlayerMOBOnly(final MOB mob)
	{
		mobLoader.DBUpdateJustMOB(mob);
	}

	@Override
	public void DBUpdateMOB(final String roomID, final MOB mob)
	{
		roomLoader.DBUpdateRoomMOB(roomID, mob);
	}

	@Override
	public void DBUpdateItem(final String roomID, final Item item)
	{
		roomLoader.DBUpdateRoomItem(roomID, item);
	}

	@Override
	public void DBDeleteMOB(final String roomID, final MOB mob)
	{
		roomLoader.DBDeleteRoomMOB(roomID, mob);
	}

	@Override
	public void DBDeleteItem(final String roomID, final Item item)
	{
		roomLoader.DBDeleteRoomItem(roomID, item);
	}

	@Override
	public void DBUpdateItems(final Room room)
	{
		roomLoader.DBUpdateItems(room);
	}

	@Override
	public void DBReCreate(final Room room, final String oldID)
	{
		roomLoader.DBReCreate(room, oldID);
	}

	@Override
	public PlayerLibrary.ThinnerPlayer DBUserSearch(final String Login)
	{
		return mobLoader.DBUserSearch(Login);
	}

	@Override
	public String DBLeigeSearch(final String Login)
	{
		return mobLoader.DBLeigeSearch(Login);
	}

	@Override
	public PlayerStats DBLoadPlayerStats(final String name)
	{
		return mobLoader.DBLoadPlayerStats(name);
	}

	@Override
	public PairList<String, Long> DBSearchPFIL(final String match)
	{
		return mobLoader.DBSearchPFIL(match);
	}

	@Override
	public void DBCreateArea(final Area A)
	{
		roomLoader.DBCreate(A);
	}

	@Override
	public void DBDeleteArea(final Area A)
	{
		roomLoader.DBDelete(A);
	}

	@Override
	public void DBDeleteAreaAndRooms(final Area A)
	{
		roomLoader.DBDeleteAreaAndRooms(A);
	}

	@Override
	public void DBUpdateArea(final String areaID, final Area A)
	{
		roomLoader.DBUpdate(areaID, A);
	}

	@Override
	public void DBDeleteRoom(final Room room)
	{
		roomLoader.DBDelete(room);
	}

	@Override
	public MOB DBReadPlayer(final String name)
	{
		return mobLoader.DBRead(name);
	}

	@Override
	public PairList<String,Integer> DBReadPlayerClans(final String name)
	{
		return mobLoader.DBReadPlayerClans(name);
	}

	@Override
	public Object DBReadPlayerValue(final String name, final PlayerCode code)
	{
		return mobLoader.DBReadPlayerValue(name, code);
	}

	@Override
	public void DBSetPlayerValue(final String name, final PlayerCode code, final Object value)
	{
		mobLoader.DBSetPlayerValue(name, code, value);
	}

	@Override
	public int DBReadPlayerBitmap(final String name)
	{
		return mobLoader.DBReadPlayerBitmap(name);
	}

	@Override
	public List<String[]> DBReadPlayerItemData(final String name, final Filterer<Pair<String,String>> classLocFilter, final Filterer<String> textFilter)
	{
		return mobLoader.DBReadPlayerItemData(name, classLocFilter, textFilter);
	}

	@Override
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		return mobLoader.getExtendedUserList();
	}

	@Override
	public PlayerLibrary.ThinPlayer getThinUser(final String name)
	{
		return mobLoader.getThinUser(name);
	}

	@Override
	public void DBReadFollowers(final MOB mob, final boolean bringToLife)
	{
		mobLoader.DBReadFollowers(mob, bringToLife);
	}

	@Override
	public List<MOB> DBScanFollowers(final String mobName)
	{
		return mobLoader.DBScanFollowers(mobName);
	}

	@Override
	public void DBDeletePlayerOnly(final String mobName)
	{
		mobLoader.DBDeleteCharOnly(mobName);
	}

	@Override
	public void DBDeleteAccount(final PlayerAccount account)
	{
		mobLoader.DBDeleteAccount(account);
	}

	@Override
	public void DBCreateCharacter(final MOB mob)
	{
		mobLoader.DBCreateCharacter(mob);
	}

	@Override
	public List<PAData> DBReadAllAreaData(final String areaID)
	{
		return aDataLoader.DBReadAllAreaData(areaID);
	}

	@Override
	public void DBDeleteAllAreaData(final String name)
	{
		aDataLoader.DBDeleteArea(name);
	}

	@Override
	public List<PAData> DBReadAreaData(final String areaID, final String section)
	{
		return aDataLoader.DBRead(areaID, section);
	}

	@Override
	public List<String> DBReadAreaDataKeys(final String areaID, final String section)
	{
		return aDataLoader.DBReadAreaDataKeys(areaID, section);
	}

	@Override
	public int DBCountAreaData(final String areaID, final String section)
	{
		return aDataLoader.DBCount(areaID, section);
	}

	@Override
	public int DBCountAreaData(final String section)
	{
		return aDataLoader.DBCountBySection(section);
	}

	@Override
	public List<String> DBReadAreaDataAreaNamesBySection(final String section)
	{
		return aDataLoader.DBReadAreaNamesBySection(section);
	}

	@Override
	public void DBDeleteAreaData(final String areaID, final String section)
	{
		aDataLoader.DBDelete(areaID, section);
	}

	@Override
	public List<PAData> DBReadAreaData(final String area, final List<String> sections)
	{
		return aDataLoader.DBRead(area, sections);
	}

	@Override
	public Set<String> DBReadUniqueAreaSections(final String name)
	{
		return aDataLoader.DBReadSections(name);
	}

	@Override
	public List<String> DBReadAreaDataAreasBySection(final String section)
	{
		return aDataLoader.DBReadNames(section);
	}

	@Override
	public boolean DBExistsAreaData(final String section, final String name)
	{
		return aDataLoader.DBExistsData(section, name);
	}

	@Override
	public List<PAData> DBReadAreaSectionData(final String section)
	{
		return aDataLoader.DBRead(section);
	}

	@Override
	public void DBDeleteAreaSectionData(final String section)
	{
		aDataLoader.DBDelete(section);
	}

	@Override
	public List<PAData> DBReadAreaData(final String areaID, final String section, final String key)
	{
		return aDataLoader.DBRead(areaID, section, key);
	}

	@Override
	public List<PAData> DBReadAreaDataByKeyMask(final String section, final String keyMask)
	{
		return aDataLoader.DBReadByKeyMask(section, keyMask);
	}

	@Override
	public List<PAData> DBReadAreaDataEntry(final String key)
	{
		return aDataLoader.DBReadKey(key);
	}

	@Override
	public void DBUpdateAreaData(final String areaID, final String section, final String key, final String xml)
	{
		aDataLoader.DBUpdate(areaID, section, key, xml);
	}

	@Override
	public void DBDeleteAreaData(final String areaID, final String section, final String key)
	{
		aDataLoader.DBDelete(areaID, section, key);
	}

	@Override
	public PAData DBReCreateAreaData(final String name, final String section, final String key, final String xml)
	{
		return aDataLoader.DBReCreate(name, section, key, xml);
	}

	@Override
	public PAData DBCreateAreaData(final String area, final String section, final String key, final String data)
	{
		return aDataLoader.DBCreate(area, section, key, data);
	}

	@Override
	public void DBDeleteAllPlayerData(final String name)
	{
		pDataLoader.DBDeletePlayer(name);
	}

	@Override
	public List<PAData> DBReadAllPlayerData(final String playerID)
	{
		return pDataLoader.DBReadAllPlayerData(playerID);
	}

	@Override
	public List<PAData> DBReadPlayerData(final String playerID, final String section)
	{
		return pDataLoader.DBRead(playerID, section);
	}

	@Override
	public List<PAData> DBReadPlayerDataByKeyMask(final String section, final String keyMask)
	{
		return pDataLoader.DBReadByKeyMask(section, keyMask);
	}

	@Override
	public List<PAData> DBReadPlayerDataEntry(final String key)
	{
		return pDataLoader.DBReadKey(key);
	}

	@Override
	public int DBCountPlayerData(final String playerID, final String section)
	{
		return pDataLoader.DBCount(playerID, section);
	}

	@Override
	public List<String> DBReadPlayerDataKeys(final String playerID, final String section)
	{
		return pDataLoader.DBReadPlayerDataKeys(playerID, section);
	}

	@Override
	public int DBCountPlayerData(final String section)
	{
		return pDataLoader.DBCountBySection(section);
	}

	@Override
	public List<String> DBReadPlayerDataAuthorsBySection(final String section)
	{
		return pDataLoader.DBReadAuthorsBySection(section);
	}

	@Override
	public List<PAData> DBReadPlayerData(final String playerID, final String section, final String key)
	{
		return pDataLoader.DBRead(playerID, section, key);
	}

	@Override
	public List<PAData> DBReadPlayerSectionData(final String section)
	{
		return pDataLoader.DBRead(section);
	}

	@Override
	public List<String> DBReadPlayerDataPlayersBySection(final String section)
	{
		return pDataLoader.DBReadNames(section);
	}

	@Override
	public boolean DBExistsPlayerData(final String section, final String name)
	{
		return pDataLoader.DBExistsData(section, name);
	}

	@Override
	public List<PAData> DBReadPlayerData(final String player, final List<String> sections)
	{
		return pDataLoader.DBRead(player, sections);
	}

	@Override
	public Set<String> DBReadUniquePlayerSections(final String name)
	{
		return pDataLoader.DBReadSections(name);
	}

	@Override
	public void DBDeletePlayerData(final String playerID, final String section)
	{
		pDataLoader.DBDelete(playerID, section);
	}

	@Override
	public void DBDeletePlayerData(final String playerID, final String section, final String key)
	{
		pDataLoader.DBDelete(playerID, section, key);
	}

	@Override
	public void DBDeletePlayerSectionData(final String section)
	{
		pDataLoader.DBDelete(section);
	}

	@Override
	public PAData DBReCreatePlayerData(final String name, final String section, final String key, final String xml)
	{
		return pDataLoader.DBReCreate(name, section, key, xml);
	}

	@Override
	public void DBUpdatePlayerData(final String name, final String section, final String key, final String xml)
	{
		pDataLoader.DBUpdate(name, section, key, xml);
	}

	@Override
	public PAData DBCreatePlayerData(final String player, final String section, final String key, final String data)
	{
		return pDataLoader.DBCreate(player, section, key, data);
	}

	@Override
	public List<AckRecord> DBReadRaces()
	{
		return gRaceLoader.DBReadRaces();
	}

	@Override
	public void DBUpdateRaceCreationDate(final String raceID)
	{
		gRaceLoader.DBUpdateRaceCreationDate(raceID);
	}

	@Override
	public boolean isRaceExpired(final String raceID)
	{
		return gRaceLoader.isRaceExpired(raceID);
	}

	@Override
	public void registerRaceUsed(final Race R)
	{
		gRaceLoader.registerRaceUsed(R);
	}

	@Override
	public int pruneOldRaces()
	{
		return gRaceLoader.DBPruneOldRaces();
	}

	@Override
	public int updateAllRaceDates()
	{
		return gRaceLoader.updateAllRaceDates();
	}

	@Override
	public void DBDeleteRace(final String raceID)
	{
		gRaceLoader.DBDeleteRace(raceID);
	}

	@Override
	public void DBCreateRace(final String raceID, final String data)
	{
		gRaceLoader.DBCreateRace(raceID, data);
	}

	@Override
	public List<AckRecord> DBReadClasses()
	{
		return gcClassLoader.DBReadClasses();
	}

	@Override
	public void DBDeleteClass(final String classID)
	{
		gcClassLoader.DBDeleteClass(classID);
	}

	@Override
	public void DBCreateClass(final String classID, final String data)
	{
		gcClassLoader.DBCreateClass(classID, data);
	}

	@Override
	public List<AckRecord> DBReadAbilities()
	{
		return gAbilityLoader.DBReadAbilities();
	}

	@Override
	public void DBDeleteAbility(final String classID)
	{
		gAbilityLoader.DBDeleteAbility(classID);
	}

	@Override
	public void DBCreateAbility(final String classID, final String typeClass, final String data)
	{
		gAbilityLoader.DBCreateAbility(classID, typeClass, data);
	}

	@Override
	public List<AckRecord> DBReadCommands()
	{
		return commandLoader.DBReadCommands();
	}

	@Override
	public AckRecord DBDeleteCommand(final String classID)
	{
		return commandLoader.DBDeleteCommand(classID);
	}

	@Override
	public void DBCreateCommand(final String classID, final String baseClassID, final String data)
	{
		commandLoader.DBCreateCommand(classID, baseClassID, data);
	}

	@Override
	public void DBReadArtifacts()
	{
		aDataLoader.DBReadArtifacts();
	}

	@Override
	public CoffeeTableRow DBReadStat(final long startTime)
	{
		return statLoader.DBRead(startTime);
	}

	@Override
	public void DBDeleteStat(final long startTime)
	{
		statLoader.DBDelete(startTime);
	}

	@Override
	public boolean DBCreateStat(final long startTime, final long endTime, final String data)
	{
		return statLoader.DBCreate(startTime, endTime, data);
	}

	@Override
	public boolean DBUpdateStat(final long startTime, final String data)
	{
		return statLoader.DBUpdate(startTime, data);
	}

	@Override
	public List<CoffeeTableRow> DBReadStats(final long startTime, final long endTime)
	{
		return statLoader.DBReadAfter(startTime, endTime);
	}

	@Override
	public long DBReadOldestStatMs()
	{
		return statLoader.DBReadOldestStatMs();
	}


	@Override
	public String errorStatus()
	{
		return dbConnector.errorStatus().toString();
	}

	@Override
	public void resetConnections()
	{
		dbConnector.reconnect();
	}

	@Override
	public int pingAllConnections(final long overrideTimeoutIntervalMillis)
	{
		return dbConnector.pingAllConnections("SELECT 1 FROM CMCHAR", overrideTimeoutIntervalMillis);
	}

	@Override
	public int pingAllConnections()
	{
		return dbConnector.pingAllConnections("SELECT 1 FROM CMCHAR");
	}

	@Override
	public void DBCreatePoll(final String name, final String player, final String subject, final String description, final String optionXML, final int flag, final String qualZapper, final String results, final long expiration)
	{
		pollLoader.DBCreate(name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePoll(final String oldName, final String name, final String player, final String subject, final String description, final String optionXML, final int flag, final String qualZapper, final String results, final long expiration)
	{
		pollLoader.DBUpdate(oldName, name, player, subject, description, optionXML, flag, qualZapper, results, expiration);
	}

	@Override
	public void DBUpdatePollResults(final String name, final String results)
	{
		pollLoader.DBUpdate(name, results);
	}

	@Override
	public void DBDeletePoll(final String name)
	{
		pollLoader.DBDelete(name);
	}

	@Override
	public List<PollData> DBReadPollList()
	{
		return pollLoader.DBReadList();
	}

	@Override
	public PollData DBReadPoll(final String name)
	{
		return pollLoader.DBRead(name);
	}

	@Override
	public CMFile.CMVFSDir DBReadVFSDirectory()
	{
		return vfsLoader.DBReadDirectory();
	}

	@Override
	public CMFile.CMVFSFile DBReadVFSFile(final String filename)
	{
		return vfsLoader.DBRead(filename);
	}

	@Override
	public List<String> DBReadVFSKeysLike(final String partialFilename, final int minMask)
	{
		return vfsLoader.DBReadKeysLike(partialFilename, minMask);
	}

	@Override
	public void DBCreateVFSFile(final String filename, final int bits, final String creator, final long updateTime, final Object data)
	{
		vfsLoader.DBCreate(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBUpSertVFSFile(final String filename, final int bits, final String creator, final long updateTime, final Object data)
	{
		vfsLoader.DBUpSert(filename, bits, creator, updateTime, data);
	}

	@Override
	public void DBDeleteVFSFile(final String filename)
	{
		vfsLoader.DBDelete(filename);
	}

	@Override
	public void DBDeleteVFSFileLike(final String partialFilename, final int minMask)
	{
		vfsLoader.DBDeleteLike(partialFilename, minMask);
	}

	@Override
	public List<Triad<String, Integer, Long>> getBackLogEntries(final String channelName, final int subNameField, final int newestToSkip, final int numToReturn)
	{
		return backLogLoader.getBackLogEntries(channelName, subNameField, newestToSkip, numToReturn);
	}

	@Override
	public int getLowestBackLogIndex(final String channelName, final int subNameField, final long afterDate)
	{
		return backLogLoader.getLowestBackLogIndex(channelName, subNameField, afterDate);
	}

	@Override
	public List<Triad<String, Integer, Long>> searchBackLogEntries(final String channelName, final int subNameField, final String search, final int numToReturn)
	{
		return backLogLoader.searchBackLogEntries(channelName, subNameField, search, numToReturn);
	}

	@Override
	public int getBackLogPageEnd(final String channelName, final int subNameField)
	{
		return backLogLoader.getBackLogPageEnd(channelName, subNameField);
	}

	@Override
	public void trimBackLogEntries(final String[] channels, final int maxMessages, final long oldestTime)
	{
		backLogLoader.trimBackLogEntries(channels, maxMessages, oldestTime);
	}

	@Override
	public void addBackLogEntry(final String channelName, final int subNameField, final long timeStamp, final String entry)
	{
		backLogLoader.addBackLogEntry(channelName, subNameField, entry);
	}

	@Override
	public void checkUpgradeBacklogTable(final ChannelsLibrary channels)
	{
		backLogLoader.checkUpgradeBacklogTable(channels);
	}

	@Override
	public void delBackLogEntry(final String channelName, final long timeStamp)
	{
		backLogLoader.delBackLogEntry(channelName, timeStamp);
	}

	@Override
	public int DBRawExecute(final String sql) throws CMException
	{
		DBConnection DBToUse=null;
		try
		{
			DBToUse=dbConnector.DBFetch();
			return DBToUse.update(sql,0);
		}
		catch(final Exception e)
		{
			throw new CMException((e.getMessage()==null)?"Unknown error":e.getMessage());
		}
		finally
		{
			if(DBToUse!=null)
				dbConnector.DBDone(DBToUse);
		}
	}

	@Override
	public List<String[]> DBRawQuery(final String sql) throws CMException
	{
		DBConnection DBToUse=null;
		final List<String[]> results=new LinkedList<String[]>();
		try
		{
			DBToUse=dbConnector.DBFetch();
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
				dbConnector.DBDone(DBToUse);
		}
		return results;
	}

	@Override
	public String validateDatabaseVersion()
	{
		return new DDLValidator(dbConnector,changeList).validateDatabaseVersion();
	}

	@Override
	public String upgradeDatabaseVersion()
	{
		return new DDLValidator(dbConnector,changeList).upgradeDatabaseVersion();
	}

	@Override
	public String L(final Class<?> clazz, final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(clazz, str, xs);
	}
}
