package com.planet_ink.coffee_mud.system;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.IOException;
public class ExternalSystems implements ExternalSystem
{
	public void startTickDown(Tickable E,
							  int tickID,
							  int numTicks)
	{
		ServiceEngine.startTickDown(E,tickID,numTicks);
	}
	public void suspendTicking(Tickable E, int tickID)
	{
		ServiceEngine.suspendTicking(E,tickID);
	}
	public void resumeTicking(Tickable E, int tickID)
	{
		ServiceEngine.resumeTicking(E,tickID);
	}
	public void vassals(MOB mob, String leigeID)
	{
		MOBloader.vassals(mob,leigeID);
	}
	public Vector userList()
	{
		return MOBloader.userList();
	}

	public boolean deleteTick(Tickable E, int tickID)
	{
		return ServiceEngine.deleteTick(E,tickID);
	}

	public void DBClanFill(String clan, Vector members, Vector roles, Vector lastDates)
	{ MOBloader.DBClanFill(clan,members,roles,lastDates);}
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

	public void DBUpdateFollowers(MOB mob)
	{
		MOBloader.DBUpdateFollowers(mob);
	}

	public String systemReport(String itemCode)
	{
		return ServiceEngine.report(itemCode);
	}

	public void DBReadContent(Room thisRoom, Hashtable rooms)
	{
		RoomLoader.DBReadContent(thisRoom, rooms);
	}
	public void DBUpdateExits(Room room)
	{
		RoomLoader.DBUpdateExits(room);
	}
	public void DBReadQuests(MudHost myHost){QuestLoader.DBRead(myHost);}
	public void DBUpdateQuest(Quest Q)
	{
		QuestLoader.DBUpdateQuest(Q);
	}
	public void DBUpdateQuests(Vector quests)
	{
		QuestLoader.DBUpdateQuests(quests);
	}
	public void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		RoomLoader.DBUpdateTheseMOBs(room,mobs);
	}
	public void DBUpdateMOBs(Room room)
	{
		RoomLoader.DBUpdateMOBs(room);
	}
	public void DBDeleteJournal(String Journal, int which)
	{
		JournalLoader.DBDelete(Journal,which);
	}
	public Vector DBReadJournal(String Journal)
	{
		return JournalLoader.DBRead(Journal);
	}
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message, int which)
	{
		JournalLoader.DBWrite(Journal,from,to,subject,message,which);
	}
	public void DBCreateRoom(Room room, String LocaleID)
	{
		RoomLoader.DBCreate(room,LocaleID);
	}
	public void DBUpdateRoom(Room room)
	{
		RoomLoader.DBUpdateRoom(room);
	}
	public void DBUpdateMOB(MOB mob)
	{
		MOBloader.DBUpdate(mob);
	}
	public void DBUpdateRoomMOB(String keyName, Room room, MOB mob)
	{
		RoomLoader.DBUpdateRoomMOB(keyName,room,mob);
	}
	public void DBUpdateItems(Room room)
	{
		RoomLoader.DBUpdateItems(room);
	}
	public void DBReCreate(Room room, String oldID)
	{
		RoomLoader.DBReCreate(room,oldID);
	}
	public void clearDebri(Room room, int taskCode)
	{
		ServiceEngine.clearDebri(room,taskCode);
	}
	public boolean DBUserSearch(MOB mob, String Login)
	{
		return MOBloader.DBUserSearch(mob,Login);
	}
	public boolean DBReadUserOnly(MOB mob)
	{
		return MOBloader.DBReadUserOnly(mob);
	}
	public void tickAllTickers(Room here)
	{
		ServiceEngine.tickAllTickers(here);
	}
	public String tickInfo(String which)
	{
		return ServiceEngine.tickInfo(which);
	}
	public Area DBCreateArea(String areaName, String areaType)
	{
		return RoomLoader.DBCreate(areaName,areaType);
	}
	public void DBDeleteArea(Area A)
	{
		RoomLoader.DBDelete(A);
	}
	public void DBUpdateArea(String keyName, Area A)
	{
		RoomLoader.DBUpdate(keyName,A);
	}

	public void DBDeleteRoom(Room room)
	{
		RoomLoader.DBDelete(room);
	}
	public void DBReadMOB(MOB mob)
	{
		MOBloader.DBRead(mob);
	}
	public Vector getUserList()
	{
		return MOBloader.getUserList();
	}
	public void DBReadFollowers(MOB mob, boolean bringToLife)
	{
		MOBloader.DBReadFollowers(mob, bringToLife);
	}
	public void DBDeleteMOB(MOB mob)
	{
		MOBloader.DBDelete(mob);
	}
	public void DBCreateCharacter(MOB mob)
	{
		MOBloader.DBCreateCharacter(mob);
	}

	public Vector DBReadData(String playerID, String section)
	{ return DataLoader.DBRead(playerID,section);}
	public Vector DBReadData(String playerID, String section, String key)
	{ return DataLoader.DBRead(playerID,section,key);}
	public Vector DBReadData(String section)
	{ return DataLoader.DBRead(section);}
	public void DBDeleteData(String playerID, String section)
	{ DataLoader.DBDelete(playerID,section);}
	public void DBDeleteData(String playerID, String section, String key)
	{ DataLoader.DBDelete(playerID,section,key);}
	public void DBDeleteData(String section)
	{ DataLoader.DBDelete(section);}
	public void DBCreateData(String player, String section, String key, String data)
	{ DataLoader.DBCreate(player,section,key,data);}
	public Vector DBReadRaces()
	{ return DataLoader.DBReadRaces();}
	public void DBDeleteRace(String raceID)
	{ DataLoader.DBDeleteRace(raceID);}
	public void DBCreateRace(String raceID,String data)
	{ DataLoader.DBCreateRace(raceID,data);}
}
