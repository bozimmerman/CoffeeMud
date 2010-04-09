package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
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
import java.io.IOException;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
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
public class DBInterface implements DatabaseEngine
{
    public String ID(){return "DBInterface";}
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
    public DBInterface(DBConnector DB)
    {
    	this.DB=DB;
    	DBConnector oldBaseDB=DB;
    	DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MudHost.MAIN_HOST,CMLib.LIBRARY_DATABASE);
    	if((baseEngine!=null)&&(baseEngine.getConnector()!=DB)&&(baseEngine.isConnected()))
    	    oldBaseDB=baseEngine.getConnector();
        Vector privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true);
        this.GAbilityLoader=new GAbilityLoader(privacyV.contains("ABILITY")?DB:oldBaseDB);
        this.GCClassLoader=new GCClassLoader(privacyV.contains("CHARCLASS")?DB:oldBaseDB);
        this.GRaceLoader=new GRaceLoader(privacyV.contains("RACE")?DB:oldBaseDB);
    	this.MOBloader=new MOBloader(privacyV.contains("PLAYERS")?DB:oldBaseDB);
    	this.RoomLoader=new RoomLoader(privacyV.contains("MAP")?DB:oldBaseDB);
    	this.DataLoader=new DataLoader(privacyV.contains("PLAYERS")?DB:oldBaseDB);
    	this.StatLoader=new StatLoader(privacyV.contains("STATS")?DB:oldBaseDB);
    	this.PollLoader=new PollLoader(privacyV.contains("POLLS")?DB:oldBaseDB);
    	this.VFSLoader=new VFSLoader(privacyV.contains("DBVFS")?DB:oldBaseDB);
    	this.JournalLoader=new JournalLoader(privacyV.contains("JOURNALS")?DB:oldBaseDB);
    	this.QuestLoader=new QuestLoader(privacyV.contains("QUEST")?DB:oldBaseDB);
    	this.ClanLoader=new ClanLoader(privacyV.contains("CLANS")?DB:oldBaseDB);
    }
    public CMObject newInstance(){return new DBInterface(DB);}
    public void initializeClass(){}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public DBConnector getConnector(){ return DB;}
    public boolean activate(){ return true;}
    public boolean shutdown(){ return true;}
    public void propertiesLoaded(){}
    public ThreadEngine.SupportThread getSupportThread() { return null;}
    
	public void vassals(MOB mob, String liegeID)
	{MOBloader.vassals(mob,liegeID);}
	
    public DVector worshippers(String deityID)
    {return MOBloader.worshippers(deityID);}
    
    public List<String> getUserList()
	{return MOBloader.getUserList();}

    public boolean isConnected(){return DB.amIOk();}

	public void DBReadAllClans()
	{ ClanLoader.DBRead();}
	
	public Vector<MemberRecord> DBClanMembers(String clan)
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
	
	public String DBEmailSearch(String email)
	{ return MOBloader.DBEmailSearch(email);}
	
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
    
    public Vector<PlayerAccount> DBListAccounts(String mask)
    { return MOBloader.DBListAccounts(mask);}
    
    public Vector DBReadAreaData(String areaID, boolean reportStatus)
    {return RoomLoader.DBReadAreaData(areaID,reportStatus);}
    
    public Vector DBReadRoomData(String roomID, boolean reportStatus)
    {return RoomLoader.DBReadRoomData(roomID,reportStatus);}
    
    public void DBReadAllRooms(RoomnumberSet roomsToRead)
    { RoomLoader.DBReadAllRooms(roomsToRead);}
    
    public Room DBReadRoomObject(String roomIDtoLoad, boolean reportStatus)
    { return RoomLoader.DBReadRoomObject(roomIDtoLoad, reportStatus);}
    
    public void DBReadRoomExits(String roomID, Vector allRooms, boolean reportStatus)
    {RoomLoader.DBReadRoomExits(roomID,allRooms,reportStatus);}
    
	public void DBReadCatalogs() {RoomLoader.DBReadCatalogs();}
	
	public void DBReadContent(Room thisRoom, Vector rooms)
	{RoomLoader.DBReadContent((thisRoom!=null)?thisRoom.roomID():null,thisRoom, rooms,null,false);}

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
	
	public void DBUpdateQuests(Vector quests)
	{QuestLoader.DBUpdateQuests(quests);}
	
	public String DBReadRoomMOBData(String roomID, String mobID)
	{ return RoomLoader.DBReadRoomMOBData(roomID,mobID);}
	public String DBReadRoomDesc(String roomID)
	{ return RoomLoader.DBReadRoomDesc(roomID);}
	
	public void DBUpdateTheseMOBs(Room room, Vector mobs)
	{RoomLoader.DBUpdateTheseMOBs(room,mobs);}
	
	public void DBUpdateTheseItems(Room room, Vector items)
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
	
	public Vector<String> DBReadJournals()
	{return JournalLoader.DBReadJournals();}
	
	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key)
	{ return JournalLoader.DBReadJournalEntry(Journal, Key);}
	
	public void DBUpdateMessageReplies(String key, int numReplies)
	{ JournalLoader.DBUpdateMessageReplies(key, numReplies);}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgs(String Journal)
	{return JournalLoader.DBReadJournalMsgs(Journal);}
	
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
	
	public void DBCreateRoom(Room room)
	{RoomLoader.DBCreate(room);}
	
	public void DBUpdateRoom(Room room)
	{RoomLoader.DBUpdateRoom(room);}
	
	public void DBUpdatePlayer(MOB mob)
	{MOBloader.DBUpdate(mob);}
    
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
	
	public boolean DBReadUserOnly(MOB mob)
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
	
    public Vector DBScanFollowers(MOB mob)
    {return MOBloader.DBScanFollowers(mob);}
	
	public void DBDeleteMOB(MOB mob)
	{MOBloader.DBDelete(mob);}
	
    public void DBDeleteAccount(PlayerAccount account)
    { MOBloader.DBDeleteAccount(account);}
	
	public void DBCreateCharacter(MOB mob)
	{MOBloader.DBCreateCharacter(mob);}

	public void DBDeletePlayerData(String name)
	{DataLoader.DBDeletePlayer(name);}
	
	public Vector DBReadAllPlayerData(String playerID)
	{ return DataLoader.DBReadAllPlayerData(playerID);}
	
	public Vector DBReadData(String playerID, String section)
	{ return DataLoader.DBRead(playerID,section);}
	
	public Vector DBReadDataKey(String section, String keyMask)
	{ return DataLoader.DBReadKey(section,keyMask);}
	public Vector DBReadDataKey(String key)
	{ return DataLoader.DBReadKey(key);}
	
	public int DBCountData(String playerID, String section)
	{ return DataLoader.DBCount(playerID,section);}
	
	public Vector DBReadData(String playerID, String section, String key)
	{ return DataLoader.DBRead(playerID,section,key);}
	
	public Vector DBReadData(String section)
	{ return DataLoader.DBRead(section);}
    public Vector DBReadData(String player, Vector sections)
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
	
	public Vector DBReadRaces()
	{ return GRaceLoader.DBReadRaces();}
	
	public void DBDeleteRace(String raceID)
	{ GRaceLoader.DBDeleteRace(raceID);}
	
	public void DBCreateRace(String raceID,String data)
	{ GRaceLoader.DBCreateRace(raceID,data);}
	
	public Vector DBReadClasses()
	{ return GCClassLoader.DBReadClasses();}
	
	public void DBDeleteClass(String classID)
	{ GCClassLoader.DBDeleteClass(classID);}
	
	public void DBCreateClass(String classID,String data)
	{ GCClassLoader.DBCreateClass(classID,data);}
	
	public Vector DBReadAbilities()
	{ return GAbilityLoader.DBReadAbilities();}
	
	public void DBDeleteAbility(String classID)
	{ GAbilityLoader.DBDeleteAbility(classID);}
	
	public void DBCreateAbility(String classID,String data)
	{ GAbilityLoader.DBCreateAbility(classID,data);}
	
	public void DBReadArtifacts()
	{ DataLoader.DBReadArtifacts();}
	
	public Object DBReadStat(long startTime)
	{ return StatLoader.DBRead(startTime);}
	
	public void DBDeleteStat(long startTime)
	{ StatLoader.DBDelete(startTime);}
	
	public void DBCreateStat(long startTime,long endTime,String data)
	{ StatLoader.DBCreate(startTime,endTime,data);}
	
	public void DBUpdateStat(long startTime, String data)
	{ StatLoader.DBUpdate(startTime,data);}
	
	public Vector DBReadStats(long startTime)
	{ return StatLoader.DBReadAfter(startTime);}
	
	public String errorStatus()
	{return DB.errorStatus().toString();}
    
	public void resetconnections()
	{DB.reconnect();}
    
    public void DBCreatePoll(String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
    {PollLoader.DBCreate(name,player,subject,description,optionXML,flag,qualZapper,results,expiration);}
    public void DBUpdatePoll(String oldName,String name, String player, String subject, String description, String optionXML, int flag, String qualZapper, String results, long expiration)
    {PollLoader.DBUpdate(oldName,name,player,subject,description,optionXML,flag,qualZapper,results,expiration);}
    public void DBUpdatePollResults(String name, String results)
    {PollLoader.DBUpdate(name,results);}
    public void DBDeletePoll(String name)
    {PollLoader.DBDelete(name);}
    public Vector DBReadPollList()
    {return PollLoader.DBReadList();}
    public Vector DBReadPoll(String name)
    {return PollLoader.DBRead(name);}

    public CMFile.CMVFSDir DBReadVFSDirectory()
    { return VFSLoader.DBReadDirectory();}
    public CMFile.CMVFSFile DBReadVFSFile(String filename)
    { return VFSLoader.DBRead(filename);}
    public void DBCreateVFSFile(String filename, int bits, String creator, Object data)
    { VFSLoader.DBCreate(filename,bits,creator,data);}
    public void DBDeleteVFSFile(String filename)
    { VFSLoader.DBDelete(filename);}
}
