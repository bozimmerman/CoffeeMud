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
	public void DBCreate(Room room, String LocaleID);
	public void DBUpdateRoom(Room room);
	public void DBUpdate(MOB mob);
	public void DBUpdateItems(Room room);
	public void DBReCreate(Room room, String oldID);
	public void DBDelete(Room room);
	public void DBRead(MOB mob);
	public void listUsers(MOB mob);
	public void DBReadFollowers(MOB mob);
	public void DBDelete(MOB mob);
	public void DBCreateCharacter(MOB mob);
	public void clearDebri(Room room, int taskCode);
	public StringBuffer listTicks();
	public void DBUserSearch(MOB mob, String Login);
}
