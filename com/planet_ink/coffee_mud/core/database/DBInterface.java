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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.JournalEntry;
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
public class DBInterface implements DatabaseEngine
{
	public String ID(){return "DBInterface";}
	public String name() { return ID();}
	
	MOBloader MOBloader=null;
	RoomLoader RoomLoader=null;
	DataLoader DataLoader=null;
	StatLoader StatLoader=null;
	PollLoader PollLoader=null;
	VFSLoader VFSLoader=null;
	JournalLoader JournalLoader=null;
	QuestLoader QuestLoader=null;
	GAbilityLoader GAbilityLoader=null;
	GRaceLoader GRaceLoader=null;
	GCClassLoader GCClassLoader=null;
	ClanLoader ClanLoader=null;
	DBConnector DB=null;
	
	public DBInterface(DBConnector DB, Set<String> privacyV)
	{
		this.DB=DB;
		DBConnector oldBaseDB=DB;
		DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MudHost.MAIN_HOST,CMLib.Library.DATABASE);
		if((privacyV!=null)&&(baseEngine!=null)&&(baseEngine.getConnector()!=DB)&&(baseEngine.isConnected()))
			oldBaseDB=baseEngine.getConnector();
		this.GAbilityLoader=new GAbilityLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBABILITY.toString())?DB:oldBaseDB);
		this.GCClassLoader=new GCClassLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBCHARCLASS.toString())?DB:oldBaseDB);
		this.GRaceLoader=new GRaceLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBRACE.toString())?DB:oldBaseDB);
		this.MOBloader=new MOBloader((privacyV==null)||privacyV.contains(DatabaseTables.DBPLAYERS.toString())?DB:oldBaseDB);
		this.RoomLoader=new RoomLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBMAP.toString())?DB:oldBaseDB);
		this.DataLoader=new DataLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBPLAYERS.toString())?DB:oldBaseDB);
		this.StatLoader=new StatLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBSTATS.toString())?DB:oldBaseDB);
		this.PollLoader=new PollLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBPOLLS.toString())?DB:oldBaseDB);
		this.VFSLoader=new VFSLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBVFS.toString())?DB:oldBaseDB);
		this.JournalLoader=new JournalLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBJOURNALS.toString())?DB:oldBaseDB);
		this.QuestLoader=new QuestLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBQUEST.toString())?DB:oldBaseDB);
		this.ClanLoader=new ClanLoader((privacyV==null)||privacyV.contains(DatabaseTables.DBCLANS.toString())?DB:oldBaseDB);
	}
	public CMObject newInstance()
	{
		return new DBInterface(DB, CMProps.getPrivateSubSet("DB.*"));
	}
	public void initializeClass(){}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public DBConnector getConnector(){ return DB;}
	
	public boolean activate(){ return true;}
	public boolean shutdown(){ return true;}
	public void propertiesLoaded(){}
	public TickClient getServiceClient() { return null;}
	public MOB.Tattoo parseTattoo(String tattoo)
	{return MOBloader.parseTattoo(tattoo);}
	
	public void vassals(MOB mob, String liegeID)
	{MOBloader.vassals(mob,liegeID);}
	
	public DVector worshippers(String deityID)
	{return MOBloader.worshippers(deityID);}
	
	public List<String> getUserList()
	{return MOBloader.getUserList();}

	public boolean isConnected(){return DB.amIOk();}

	public void DBReadAllClans()
	{ ClanLoader.DBRead();}
	
	public List<MemberRecord> DBClanMembers(String clan)
	{ return MOBloader.DBClanMembers(clan);}
	
	public void DBUpdateClanMembership(String name, String clan, int role)
	{ MOBloader.DBUpdateClan(name,clan,role);}
	
	public void DBUpdateClan(Clan C)
	{ ClanLoader.DBUpdate(C);}
	
	public void DBDeleteClan(Clan C)
	{ ClanLoader.DBDelete(C);}
	
	public void DBCreateClan(Clan C)
	{ ClanLoader.DBCreate(C);}

	public void DBUpdateEmail(MOB mob)
	{ MOBloader.DBUpdateEmail(mob);}
	
	public String DBPlayerEmailSearch(String email)
	{ return MOBloader.DBPlayerEmailSearch(email);}
	
	public void DBUpdatePassword(String name, String password)
	{ MOBloader.DBUpdatePassword(name, password);}
	
	public String[] DBFetchEmailData(String name)
	{ return MOBloader.DBFetchEmailData(name);}
		
	public void DBUpdatePlayerAbilities(MOB mob)
	{ MOBloader.DBUpdateAbilities(mob);}

	public void DBUpdatePlayerItems(MOB mob)
	{ MOBloader.DBUpdateItems(mob);}
	
	public void DBUpdateFollowers(MOB mob)
	{MOBloader.DBUpdateFollowers(mob);}

	public void DBUpdateAccount(PlayerAccount account)
	{ MOBloader.DBUpdateAccount(account);}
	
	public void DBCreateAccount(PlayerAccount account)
	{ MOBloader.DBCreateAccount(account);}
	
	public PlayerAccount DBReadAccount(String Login)
	{ return MOBloader.DBReadAccount(Login);}
	
	public List<PlayerAccount> DBListAccounts(String mask)
	{ return MOBloader.DBListAccounts(mask);}
	
	public void DBPlayerNameChange(String oldName, String newName)
	{ MOBloader.DBNameChange(oldName, newName);}
	
	public Area DBReadArea(Area A)
	{ return RoomLoader.DBReadArea(A); }

	public Map<String, Room> DBReadRoomData(String roomID, boolean reportStatus)
	{return RoomLoader.DBReadRoomData(roomID,reportStatus);}
	
	public void DBReadAllRooms(RoomnumberSet roomsToRead)
	{ RoomLoader.DBReadAllRooms(roomsToRead);}
	
	public Room[] DBReadRoomObjects(String areaName, boolean reportStatus)
	{ return RoomLoader.DBReadRoomObjects(areaName, reportStatus); }
	
	public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
	{ return RoomLoader.DBReadRoomObject(roomIDtoLoad, reportStatus); }
	
	public boolean DBReReadRoomObject(Room room)
	{ return RoomLoader.DBReReadRoomObject(room); }
	
	public void DBReadRoomExits(String roomID, Room room, boolean reportStatus)
	{RoomLoader.DBReadRoomExits(roomID,room,reportStatus);}
	
	public void DBReadCatalogs() {RoomLoader.DBReadCatalogs();}
	
	public void DBReadContent(String roomID, Room thisRoom, boolean makeLive)
	{RoomLoader.DBReadContent(roomID,thisRoom, null,null,false,makeLive);}

	public RoomnumberSet DBReadAreaRoomList(String areaName, boolean reportStatus)
	{return RoomLoader.DBReadAreaRoomList(areaName,reportStatus);}

	public void DBCreateThisItem(String roomID, Item thisItem)
	{RoomLoader.DBCreateThisItem(roomID,thisItem);}
	
	public void DBCreateThisMOB(String roomID, MOB thisMOB)
	{RoomLoader.DBCreateThisMOB(roomID,thisMOB);}
	
	public void DBUpdateExits(Room room)
	{RoomLoader.DBUpdateExits(room);}
	
	public void DBReadQuests(MudHost myHost)
	{QuestLoader.DBRead(myHost);}
	
	public void DBUpdateQuest(Quest Q)
	{QuestLoader.DBUpdateQuest(Q);}
	
	public void DBUpdateQuests(List<Quest> quests)
	{QuestLoader.DBUpdateQuests(quests);}
	
	public String DBReadRoomMOBData(String roomID, String mobID)
	{ return RoomLoader.DBReadRoomMOBData(roomID,mobID);}
	public String DBReadRoomDesc(String roomID)
	{ return RoomLoader.DBReadRoomDesc(roomID);}
	
	public void DBUpdateTheseMOBs(Room room, List<MOB> mobs)
	{RoomLoader.DBUpdateTheseMOBs(room,mobs);}
	
	public void DBUpdateTheseItems(Room room, List<Item> items)
	{RoomLoader.DBUpdateTheseItems(room,items);}
	
	public void DBUpdateMOBs(Room room)
	{RoomLoader.DBUpdateMOBs(room);}
	
	public void DBDeletePlayerJournals(String name)
	{JournalLoader.DBDeletePlayerData(name);}
	
	public void DBUpdateJournal(String Journal, JournalsLibrary.JournalEntry entry)
	{JournalLoader.DBUpdateJournal(Journal,entry);}
	
	public void DBUpdateJournalStats(String Journal, JournalsLibrary.JournalSummaryStats stats)
	{JournalLoader.DBUpdateJournalStats(Journal,stats);}
	
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats)
	{JournalLoader.DBReadJournalSummaryStats(stats);}
	
	public String DBGetRealJournalName(String possibleName)
	{ return JournalLoader.DBGetRealName(possibleName);}

	public void DBDeleteJournal(String Journal, String msgKeyOrNull)
	{JournalLoader.DBDelete(Journal, msgKeyOrNull);}
	
	public List<String> DBReadJournals()
	{return JournalLoader.DBReadJournals();}
	
	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key)
	{ return JournalLoader.DBReadJournalEntry(Journal, Key);}
	
	public void DBUpdateMessageReplies(String key, int numReplies)
	{ JournalLoader.DBUpdateMessageReplies(key, numReplies);}
	
	public List<JournalEntry> DBReadJournalMsgs(String Journal)
	{return JournalLoader.DBReadJournalMsgs(Journal);}
	
	public Vector<JournalsLibrary.JournalEntry> DBSearchAllJournalEntries(String Journal, String searchStr)
	{return JournalLoader.DBSearchAllJournalEntries(Journal,  searchStr);}

	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate)
	{return JournalLoader.DBReadJournalMsgsNewerThan(Journal, to, olderDate);}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalPageMsgs(String Journal, String parent, String searchStr, long newerDate, int limit)
	{ return JournalLoader.DBReadJournalPageMsgs(Journal, parent, searchStr, newerDate, limit);}
	
	public int DBCountJournal(String Journal, String from, String to)
	{ return JournalLoader.DBCount(Journal,from,to);}
	
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime)
	{ return JournalLoader.DBJournalLatestDateNewerThan(Journal, to, olderTime);}
	
	public void DBWriteJournal(String Journal, JournalsLibrary.JournalEntry entry)
	{JournalLoader.DBWrite(Journal,entry);}
	
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message)
	{JournalLoader.DBWrite(Journal,from,to,subject,message);}
	
	public void DBWriteJournalEmail(String MailBox, String journalSource, String from, String to, String subject, String message)
	{JournalLoader.DBWrite(MailBox,journalSource,from,to,subject,message);}
	
	public void DBWriteJournalChild(String Journal, String journalSource, String from, String to, String parentKey, String subject, String message)
	{JournalLoader.DBWrite(Journal,journalSource,from,to,parentKey,subject,message); }
	
	public void DBWrite(String Journal, JournalsLibrary.JournalEntry entry)
	{JournalLoader.DBWrite(Journal, entry);}
	
	public void DBWriteJournalReply(String Journal, String key, String from, String to, String subject, String message)
	{JournalLoader.DBWriteJournalReply(Journal, key, from, to, subject, message);}
	
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes)
	{JournalLoader.DBUpdateJournal(key,subject,msg,newAttributes);}
	
	public void DBViewJournalMessage(String key, int views)
	{JournalLoader.DBViewJournalMessage(key, views);}
	
	public void DBTouchJournalMessage(String key)
	{JournalLoader.DBTouchJournalMessage(key);}
	
	public void DBTouchJournalMessage(String key, long newDate)
	{JournalLoader.DBTouchJournalMessage(key, newDate);}
	
	public void DBCreateRoom(Room room)
	{RoomLoader.DBCreate(room);}
	
	public void DBUpdateRoom(Room room)
	{RoomLoader.DBUpdateRoom(room);}
	
	public void DBUpdatePlayer(MOB mob)
	{MOBloader.DBUpdate(mob);}
	
	public List<String> DBExpiredCharNameSearch(Set<String> skipNames)
	{ return MOBloader.DBExpiredCharNameSearch(skipNames); }
	
	public void DBUpdatePlayerPlayerStats(MOB mob)
	{MOBloader.DBUpdateJustPlayerStats(mob);}
	
	public void DBUpdatePlayerMOBOnly(MOB mob)
	{MOBloader.DBUpdateJustMOB(mob);}
	
	public void DBUpdateMOB(String roomID, MOB mob)
	{RoomLoader.DBUpdateRoomMOB(roomID,mob);}
	
	public void DBUpdateItem(String roomID, Item item)
	{RoomLoader.DBUpdateRoomItem(roomID,item);}
	
	public void DBDeleteMOB(String roomID, MOB mob)
	{RoomLoader.DBDeleteRoomMOB(roomID,mob);}
	
	public void DBDeleteItem(String roomID, Item item)
	{RoomLoader.DBDeleteRoomItem(roomID,item);}
	
	public void DBUpdateItems(Room room)
	{RoomLoader.DBUpdateItems(room);}
	
	public void DBReCreate(Room room, String oldID)
	{RoomLoader.DBReCreate(room,oldID);}
	
	public PlayerLibrary.ThinnerPlayer DBUserSearch(String Login)
	{return MOBloader.DBUserSearch(Login);}
	
	public String DBReadUserOnly(MOB mob)
	{return MOBloader.DBReadUserOnly(mob);}
	
	public void DBCreateArea(Area A)
	{RoomLoader.DBCreate(A);}
	
	public void DBDeleteArea(Area A)
	{RoomLoader.DBDelete(A);}
	
	public void DBUpdateArea(String keyName, Area A)
	{RoomLoader.DBUpdate(keyName,A);}

	public void DBDeleteRoom(Room room)
	{RoomLoader.DBDelete(room);}
	
	public void DBReadPlayer(MOB mob)
	{MOBloader.DBRead(mob);}
	
	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{return MOBloader.getExtendedUserList();}
	
	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{ return MOBloader.getThinUser(name);}
	
	public void DBReadFollowers(MOB mob, boolean bringToLife)
	{MOBloader.DBReadFollowers(mob, bringToLife);}
	
	public List<MOB> DBScanFollowers(MOB mob)
	{return MOBloader.DBScanFollowers(mob);}
	
	public void DBDeletePlayer(MOB mob, boolean deleteAssets)
	{MOBloader.DBDelete(mob, deleteAssets);}
	
	public void DBDeleteAccount(PlayerAccount account)
	{ MOBloader.DBDeleteAccount(account);}
	
	public void DBCreateCharacter(MOB mob)
	{MOBloader.DBCreateCharacter(mob);}

	public void DBDeletePlayerData(String name)
	{DataLoader.DBDeletePlayer(name);}
	
	public List<PlayerData> DBReadAllPlayerData(String playerID)
	{ return DataLoader.DBReadAllPlayerData(playerID);}
	
	public List<PlayerData> DBReadData(String playerID, String section)
	{ return DataLoader.DBRead(playerID,section);}
	
	public List<PlayerData> DBReadDataKey(String section, String keyMask)
	{ return DataLoader.DBReadKey(section,keyMask);}
	public List<PlayerData> DBReadDataKey(String key)
	{ return DataLoader.DBReadKey(key);}
	
	public int DBCountData(String playerID, String section)
	{ return DataLoader.DBCount(playerID,section);}
	
	public List<PlayerData> DBReadData(String playerID, String section, String key)
	{ return DataLoader.DBRead(playerID,section,key);}
	
	public List<PlayerData> DBReadData(String section)
	{ return DataLoader.DBRead(section);}
	public List<PlayerData> DBReadData(String player, List<String> sections)
	{ return DataLoader.DBRead(player, sections);}

	public void DBDeleteData(String playerID, String section)
	{ DataLoader.DBDelete(playerID,section);}
	
	public void DBDeleteData(String playerID, String section, String key)
	{ DataLoader.DBDelete(playerID,section,key);}
	
	public void DBDeleteData(String section)
	{ DataLoader.DBDelete(section);}
	
	public void DBReCreateData(String name, String section, String key, String xml)
	{ DataLoader.DBReCreate(name,section,key,xml);}
	public void DBUpdateData(String key, String xml)
	{ DataLoader.DBUpdate(key,xml);}
	
	public void DBCreateData(String player, String section, String key, String data)
	{ DataLoader.DBCreate(player,section,key,data);}
	
	public List<AckRecord> DBReadRaces()
	{ return GRaceLoader.DBReadRaces();}
	
	public void DBDeleteRace(String raceID)
	{ GRaceLoader.DBDeleteRace(raceID);}
	
	public void DBCreateRace(String raceID,String data)
	{ GRaceLoader.DBCreateRace(raceID,data);}
	
	public List<AckRecord> DBReadClasses()
	{ return GCClassLoader.DBReadClasses();}
	
	public void DBDeleteClass(String classID)
	{ GCClassLoader.DBDeleteClass(classID);}
	
	public void DBCreateClass(String classID,String data)
	{ GCClassLoader.DBCreateClass(classID,data);}
	
	public List<AckRecord> DBReadAbilities()
	{ return GAbilityLoader.DBReadAbilities();}
	
	public void DBDeleteAbility(String classID)
	{ GAbilityLoader.DBDeleteAbility(classID);}
	
	public void DBCreateAbility(String classID, String typeClass, String data)
	{ GAbilityLoader.DBCreateAbility(classID, typeClass, data);}
	
	public void DBReadArtifacts()
	{ DataLoader.DBReadArtifacts();}
	
	public Object DBReadStat(long startTime)
	{ return StatLoader.DBRead(startTime);}
	
	public void DBDeleteStat(long startTime)
	{ StatLoader.DBDelete(startTime);}
	
	public boolean DBCreateStat(long startTime,long endTime,String data)
	{ return StatLoader.DBCreate(startTime,endTime,data);}
	
	public boolean DBUpdateStat(long startTime, String data)
	{ return StatLoader.DBUpdate(startTime,data);}
	
	public List<CoffeeTableRow> DBReadStats(long startTime)
	{ return StatLoader.DBReadAfter(startTime);}
	
	public String errorStatus()
	{return DB.errorStatus().toString();}
	
	public void resetConnections()
	{DB.reconnect();}
	
	public int pingAllConnections(final long overrideTimeoutIntervalMillis)
	{   return DB.pingAllConnections("SELECT 1 FROM CMCHAR", overrideTimeoutIntervalMillis); }
	
	public int pingAllConnections()
	{   return DB.pingAllConnections("SELECT 1 FROM CMCHAR"); }
	
	public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
	{PollLoader.DBCreate(name,player,subject,description,optionXML,flag,qualZapper,results,expiration);}
	public void DBUpdatePoll(String oldName,String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
	{PollLoader.DBUpdate(oldName,name,player,subject,description,optionXML,flag,qualZapper,results,expiration);}
	public void DBUpdatePollResults(String name, String results)
	{PollLoader.DBUpdate(name,results);}
	public void DBDeletePoll(String name)
	{PollLoader.DBDelete(name);}
	public List<PollData> DBReadPollList()
	{return PollLoader.DBReadList();}
	public PollData DBReadPoll(String name)
	{return PollLoader.DBRead(name);}

	public CMFile.CMVFSDir DBReadVFSDirectory()
	{ return VFSLoader.DBReadDirectory();}
	public CMFile.CMVFSFile DBReadVFSFile(String filename)
	{ return VFSLoader.DBRead(filename);}
	public void DBCreateVFSFile(String filename, int bits, String creator, long updateTime, Object data)
	{ VFSLoader.DBCreate(filename,bits,creator,updateTime,data);}
	public void DBUpSertVFSFile(String filename, int bits, String creator, long updateTime, Object data)
	{ VFSLoader.DBUpSert(filename,bits,creator,updateTime,data);}
	
	public void DBDeleteVFSFile(String filename)
	{ VFSLoader.DBDelete(filename);}
	
	public int DBRawExecute(String sql) throws CMException
	{
		DBConnection DBToUse=null;
		try
		{
			DBToUse=DB.DBFetch();
			return DBToUse.update(sql,0);
		}
		catch(Exception e)
		{
			throw new CMException((e.getMessage()==null)?"Unknown error":e.getMessage());
		}
		finally
		{
			if(DBToUse!=null)
				DB.DBDone(DBToUse);
		}
	}
	
	public List<String[]> DBRawQuery(String sql) throws CMException
	{
		DBConnection DBToUse=null;
		List<String[]> results=new LinkedList<String[]>();
		try
		{
			DBToUse=DB.DBFetch();
			ResultSet R=DBToUse.query(sql);
			ResultSetMetaData metaData=R.getMetaData();
			if(metaData!=null)
			{
				List<String> header=new LinkedList<String>();
				for(int c=1;c<=metaData.getColumnCount();c++)
					header.add(CMStrings.padRight(metaData.getColumnName(c), metaData.getColumnDisplaySize(c)));
				results.add(header.toArray(new String[0]));
			}
			while(R.next())
			{
				List<String> row=new LinkedList<String>();
				try
				{
					for(int i=1;;i++)
					{
						Object o=R.getObject(i);
						if(o==null)
							row.add("null");
						else
							row.add(o.toString());
					}
				}
				catch(Exception e)
				{
					
				}
				results.add(row.toArray(new String[0]));
			}
		}
		catch(Exception e)
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
