package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
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
