package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface ThreadEngine
{
	// tick related
	public void startTickDown(Tickable E,
							  int tickID,
							  int numTicks);
	public boolean deleteTick(Tickable E, int tickID);
	public void suspendTicking(Tickable E, int tickID);
	public void resumeTicking(Tickable E, int tickID);
	public void clearDebri(Room room, int taskCode);
	public String tickInfo(String which);
	public void tickAllTickers(Room here);
	public String systemReport(String itemCode);
	public boolean isTicking(Tickable E, int tickID);
	public void shutdownAll();
	public Enumeration tickGroups();
}
