package com.planet_ink.coffee_mud.interfaces;
public interface ExternalSystem
{
	// tick related
	public void startTickDown(Environmental E,
									 int tickID,
									 int numTicks);
	public boolean deleteTick(Environmental E, int tickID);
	
	public void DBUpdateFollowers(MOB mob);
	public void DBReadContent(Room thisRoom);
	public void DBUpdateExits(Room room);
	public void DBUpdateMOBs(Room room);
	public void DBCreateRoom(Room room, String LocaleID);
	public void DBUpdateRoom(Room room);
	public void DBUpdateMOB(MOB mob);
	public void DBUpdateItems(Room room);
	public void DBReCreate(Room room, String oldID);
	public void DBDeleteRoom(Room room);
	public void DBReadMOB(MOB mob);
	public void listUsers(MOB mob);
	public void DBReadFollowers(MOB mob);
	public void DBDeleteMOB(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public Area DBCreateArea(String areaName, String areaType);
	public void DBDeleteArea(Area A);
	public void DBUpdateArea(Area A);
	public void clearDebri(Room room, int taskCode);
	public StringBuffer listTicks();
	public boolean DBUserSearch(MOB mob, String Login);
}
