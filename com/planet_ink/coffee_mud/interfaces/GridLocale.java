package com.planet_ink.coffee_mud.interfaces;

import java.util.Vector;

public interface GridLocale extends Room
{
	public Room getAltRoomFrom(Room loc);
	public void buildGrid();
	public void clearGrid();
	public String getChildLocaleID();
	public boolean isMyChild(Room loc);
	public String getChildCode(Room loc);
	public Room getChild(String childCode);
	public Vector getAllRooms();
	public int xSize();
	public int ySize();
	public void setXSize(int x);
	public void setYSize(int y);
}
