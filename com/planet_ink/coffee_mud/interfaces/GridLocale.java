package com.planet_ink.coffee_mud.interfaces;

import java.util.Vector;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface GridLocale extends Room
{
	public Room getAltRoomFrom(Room loc, int direction);
	public void buildGrid();
	public void clearGrid(Room bringBackHere);
	public String getChildLocaleID();
	public boolean isMyChild(Room loc);
	public String getChildCode(Room loc);
	public Room getChild(String childCode);
	public Room getRandomChild();
	public int getChildX(Room loc);
	public int getChildY(Room loc);
	public Vector getAllRooms();
	public int xSize();
	public int ySize();
	public void setXSize(int x);
	public void setYSize(int y);
}
