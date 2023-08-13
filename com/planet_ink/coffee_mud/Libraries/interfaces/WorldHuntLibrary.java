package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
/*
   Copyright 2005-2023 Bo Zimmerman

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
public interface WorldHuntLibrary extends CMLibrary
{
	public List<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis);
	public List<Room> findAreaRoomsLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public Room findAreaRoomLiberally(MOB mob, Area A, String cmd, String srchWhatAERIPMVK, int timePct);
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public Room findFirstRoom(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct);
	public MOB findFirstInhabitant(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<MOB> findInhabitantsFavorExact(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, int timePct);
	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public Item findFirstRoomItem(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct);
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Environmental> findShopStockers(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Environmental findFirstShopStocker(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public Item findFirstInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct);
	public boolean isHere(CMObject E2, Area here);
	public boolean isHere(CMObject E2, Room here);
}
