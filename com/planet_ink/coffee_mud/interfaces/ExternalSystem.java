package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ExternalSystem
{
	// tick related
	public void startTickDown(Environmental E,
									 int tickID,
									 int numTicks);
	public boolean deleteTick(Environmental E, int tickID);
	
	public void DBUpdateFollowers(MOB mob);
	public void DBReadContent(Room thisRoom, Hashtable rooms);
	public void DBUpdateExits(Room room);
	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room, String LocaleID);
	public void DBUpdateRoom(Room room);
	public void DBUpdateMOB(MOB mob);
	public void DBUpdateItems(Room room);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadMOB(MOB mob);
	public void DBUpdateIP(MOB mob);
	public void DBClanFill(String clan, Vector members, Vector roles);
	public void DBUpdateClan(String name, String clan, int role);
	public void listUsers(MOB mob, int sortBy);
	public Vector userList();
	public void DBReadFollowers(MOB mob);
	public void DBDeleteMOB(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public Area DBCreateArea(String areaName, String areaType);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(Area A);
	public Vector DBReadJournal(String Journal);
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message, int which);
	public void DBDeleteJournal(String Journal, int which);
	public void suspendTicking(Environmental E, int tickID);
	public void resumeTicking(Environmental E, int tickID);
	public void clearDebri(Room room, int taskCode);
	public StringBuffer listTicks(int whichTick);
	public boolean DBReadUserOnly(MOB mob);
	public boolean DBUserSearch(MOB mob, String Login);
	public void vassals(MOB mob, String leigeID);
	public void tickAllTickers(Room here);
	public StringBuffer systemReport();
}
