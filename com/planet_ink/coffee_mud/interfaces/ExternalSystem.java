package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ExternalSystem
{
	// tick related
	public void startTickDown(Tickable E,
							  int tickID,
							  int numTicks);
	public boolean deleteTick(Tickable E, int tickID);
	public void suspendTicking(Tickable E, int tickID);
	public void resumeTicking(Tickable E, int tickID);
	
	public void DBUpdateFollowers(MOB mob);
	public void DBReadContent(Room thisRoom, Hashtable rooms);
	public void DBUpdateExits(Room room);
	public void DBUpdateTheseMOBs(Room room, Vector mobs);
	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room, String LocaleID);
	public void DBUpdateRoom(Room room);
	public void DBUpdateMOB(MOB mob);
	public void DBUpdateItems(Room room);
	public void DBUpdateQuests(Vector quests);
	public void DBUpdateQuest(Quest Q);
	public void DBReadQuests(Host myHost);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadMOB(MOB mob);
	public void DBClanFill(String clan, Vector members, Vector roles, Vector lastDates);
	public void DBUpdateClanMembership(String name, String clan, int role);
	public void DBUpdateClan(Clan C);
	public void DBDeleteClan(Clan C);
	public void DBCreateClan(Clan C);
	public void DBUpdateEmail(MOB mob);
	public Vector getUserList();
	public Vector userList();
	public void DBReadFollowers(MOB mob, boolean bringToLife);
	public void DBDeleteMOB(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public Area DBCreateArea(String areaName, String areaType);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(Area A);
	public Vector DBReadJournal(String Journal);
	public void DBWriteJournal(String Journal, String from, String to, String subject, String message, int which);
	public void DBDeleteJournal(String Journal, int which);
	public void clearDebri(Room room, int taskCode);
	public String tickInfo(String which);
	public boolean DBReadUserOnly(MOB mob);
	public boolean DBUserSearch(MOB mob, String Login);
	public void vassals(MOB mob, String leigeID);
	public void tickAllTickers(Room here);
	public String systemReport(String itemCode);
}
