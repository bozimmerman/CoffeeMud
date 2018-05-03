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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2006-2018 Bo Zimmerman

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
public interface LegalLibrary extends CMLibrary
{
	/**
	 * The default room description for a room for sale
	 * These get localized later.
	 */
	public final static String SALESTR=" This lot is for sale (look id).";

	/**
	 * The default room description for a room for rent
	 * These get localized later.
	 */
	public final static String RENTSTR=" This lot (look id) is for rent on a monthly basis.";

	/**
	 * The default room title for an indoor room
	 * These get localized later.
	 */
	public final static String INDOORSTR=" An empty room";

	/**
	 * The default room title for an outdoor room
	 * These get localized later.
	 */
	public final static String OUTDOORSTR=" An empty plot";

	public Law getTheLaw(Room R, MOB mob);
	public LegalBehavior getLegalBehavior(Area A);
	public LegalBehavior getLegalBehavior(Room R);
	public Area getLegalObject(Area A);
	public Area getLegalObject(Room R);
	public String getLandOwnerName(Room room);
	public boolean isLandOwnable(Room room);
	public String getPropertyOwnerName(Room room);
	public void colorRoomForSale(Room R, LandTitle title, boolean reset);

	public LandTitle getLandTitle(Area area);
	public LandTitle getLandTitle(Room room);
	public PrivateProperty getPropertyRecord(Area area);
	public PrivateProperty getPropertyRecord(Room room);
	public PrivateProperty getPropertyRecord(Item item);
	public boolean isRoomSimilarlyTitled(LandTitle title, Room R);
	public Set<Room> getHomePeersOnThisFloor(Room room, Set<Room> doneRooms);
	public boolean isHomeRoomDownstairs(Room room);
	public boolean isHomeRoomUpstairs(Room room);
	public boolean doesHavePriviledgesHere(MOB mob, Room room);
	public boolean doesAnyoneHavePrivilegesHere(MOB mob, String overrideID, Room R);
	public boolean doesHavePriviledgesInThisDirection(MOB mob, Room room, Exit exit);
	public boolean doesHavePrivilegesWith(final MOB mob, final PrivateProperty record);
	public boolean doesHaveWeakPrivilegesWith(final MOB mob, final PrivateProperty record);
	public boolean doesHaveWeakPriviledgesHere(MOB mob, Room room);
	public boolean doesOwnThisLand(String name, Room room);
	public boolean doesOwnThisLand(MOB mob, Room room);
	public boolean mayOwnThisItem(MOB mob, Item item);
	public boolean doesOwnThisProperty(String name, Room room);
	public boolean doesOwnThisProperty(MOB mob, Room room);
	public boolean doesOwnThisProperty(MOB mob, PrivateProperty record);
	public boolean robberyCheck(PrivateProperty record, CMMsg msg);
	public MOB getPropertyOwner(PrivateProperty record);
	public boolean canAttackThisProperty(MOB mob, PrivateProperty record);
	public List<LandTitle> getAllUniqueLandTitles(Enumeration<Room> e, String owner, boolean includeRentals);
	public Ability getClericInfusion(Physical room);
	public Deity getClericInfused(Room room);

	public boolean isLegalOfficerHere(MOB mob);
	public boolean isLegalJudgeHere(MOB mob);
	public boolean isLegalOfficialHere(MOB mob);

	public boolean isACity(Area A);
}
