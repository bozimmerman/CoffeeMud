package com.planet_ink.coffee_mud.Areas.interfaces;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.interfaces.*;
/* 
Copyright 2000-2006 Bo Zimmerman

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
public interface GridZones extends Environmental
{
	public boolean isMyGridChild(Room loc);
	public String getGridChildCode(Room loc);
	public Room getGridChild(String childCode);
	public Room getRandomGridChild();
	public int getGridChildX(Room loc);
	public int getGridChildY(Room loc);
	public int[] getRoomXY(String roomID);
	public int xGridSize();
	public int yGridSize();
	public void setXGridSize(int x);
	public void setYGridSize(int y);
	public Room getGridChild(int x, int y);
}
