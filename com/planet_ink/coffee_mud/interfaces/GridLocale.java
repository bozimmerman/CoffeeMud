package com.planet_ink.coffee_mud.interfaces;

import java.util.Vector;

public interface GridLocale extends Room
{
	public Room getAltRoomFrom(Room loc);
	public void buildGrid();
	public void clearGrid();
	public String getChildLocaleID();
	public boolean isMyChild(Room loc);
	public Vector getAllRooms();
}
