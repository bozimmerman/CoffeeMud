package com.planet_ink.coffee_mud.system;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.IOException;
public class ExternalSystems implements ExternalSystem
{
	public void startTickDown(Environmental E,
									 int tickID,
									 int numTicks)
	{
		ServiceEngine.startTickDown(E,tickID,numTicks);
	}
	public boolean deleteTick(Environmental E, int tickID)
	{
		return ServiceEngine.deleteTick(E,tickID);
	}

	public void DBUpdateFollowers(MOB mob)
	{
		MOBloader.DBUpdateFollowers(mob);
	}
	
	public void DBReadContent(Room thisRoom)
	{
		RoomLoader.DBReadContent(thisRoom);
	}
	public void DBUpdateExits(Room room)
	{
		RoomLoader.DBUpdateExits(room);
	}
	public void DBUpdateMOBs(Room room)
	{
		RoomLoader.DBUpdateMOBs(room);
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
	public StringBuffer listTicks(int whichTick)
	{
		return ServiceEngine.listTicks(whichTick);
	}
	public Area DBCreateArea(String areaName, String areaType)
	{
		return RoomLoader.DBCreate(areaName,areaType);
	}
	public void DBDeleteArea(Area A)
	{
		RoomLoader.DBDelete(A);
	}
	public void DBUpdateArea(Area A)
	{
		RoomLoader.DBUpdate(A);
	}
	
	public void DBDeleteRoom(Room room)
	{
		RoomLoader.DBDelete(room);
	}
	public void DBReadMOB(MOB mob)
	{
		MOBloader.DBRead(mob);
	}
	public void listUsers(MOB mob)
	{
		MOBloader.listUsers(mob);
	}
	public void DBReadFollowers(MOB mob)
	{
		MOBloader.DBReadFollowers(mob);
	}
	public void DBDeleteMOB(MOB mob)
	{
		MOBloader.DBDelete(mob);
	}
	public void DBCreateCharacter(MOB mob)
	{
		MOBloader.DBCreateCharacter(mob);
	}
}
