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
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity.DeityWorshipper;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2006-2023 Bo Zimmerman

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

/**
 * The primary legal matters handled by this library are
 * property law: who is permitted to do what, where.  It
 * also handles access to legal codes, and manages standards
 * related to property transfer markers and property
 * titles.
 *
 * @author Bo Zimmerman
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

	/**
	 * If a room is in an area subject to law and order,
	 * this method will return the legal policy object that
	 * applies to it.
	 *
	 * @see LegalLibrary#getLegalObject(Area)
	 * @see LegalLibrary#getLegalBehavior(Area)
	 * @see LegalLibrary#getLegalObject(Room)
	 * @see LegalLibrary#getLegalBehavior(Room)
	 *
	 * @param R the room to check
	 * @return null, or the legal policies
	 */
	public Law getTheLaw(Room R);

	/**
	 * If an area is subject to law and order,
	 * this method will return the legal system that
	 * applies to it.
	 *
	 * @see LegalLibrary#getTheLaw(Room)
	 * @see LegalLibrary#getLegalObject(Area)
	 * @see LegalLibrary#getLegalObject(Room)
	 * @see LegalLibrary#getLegalBehavior(Room)
	 *
	 * @param A the area to check
	 * @return null, or the legal system
	 */
	public LegalBehavior getLegalBehavior(Area A);


	/**
	 * If an area is subject to law and order,
	 * this method will return the exact area object
	 * that the legal system directly applies to.
	 *
	 * @see LegalLibrary#getTheLaw(Room)
	 * @see LegalLibrary#getLegalBehavior(Area)
	 * @see LegalLibrary#getLegalObject(Room)
	 * @see LegalLibrary#getLegalBehavior(Room)
	 *
	 * @param A the area to check
	 * @return null, or the legal area
	 */
	public Area getLegalObject(Area A);

	/**
	 * If a room is in an area subject to law and order,
	 * this method will return the legal system that
	 * applies to it.
	 *
	 * @see LegalLibrary#getTheLaw(Room)
	 * @see LegalLibrary#getLegalObject(Area)
	 * @see LegalLibrary#getLegalBehavior(Area)
	 * @see LegalLibrary#getLegalObject(Room)
	 *
	 * @param R the room to check
	 * @return null, or the legal system
	 */
	public LegalBehavior getLegalBehavior(Room R);

	/**
	 * If a room is in an area subject to law and order,
	 * this method will return the exact area object
	 * that the legal system directly applies to.
	 *
	 * @see LegalLibrary#getTheLaw(Room)
	 * @see LegalLibrary#getLegalObject(Area)
	 * @see LegalLibrary#getLegalBehavior(Area)
	 * @see LegalLibrary#getLegalBehavior(Room)
	 *
	 * @param R the room to check
	 * @return null, or the legal area
	 */
	public Area getLegalObject(Room R);

	/**
	 * Returns the name of the owner of the given room.
	 * This might include its area.
	 *
	 * @see LegalLibrary#getPropertyOwnerName(Room)
	 * @see LegalLibrary#isLandOwnable(Room)
	 *
	 * @param room the room to check
	 * @return "", or the name of the owner
	 */
	public String getLandOwnerName(Room room);

	/**
	 * Returns whether the given room has a land
	 * title that makes it possible to own, which
	 * might include the area.
	 *
	 * @see LegalLibrary#getPropertyOwnerName(Room)
	 * @see LegalLibrary#getLandOwnerName(Room)
	 *
	 * @param room the room to check
	 * @return true if it can be privately owned
	 */
	public boolean isLandOwnable(Room room);

	/**
	 * Returns the name of the owner of the given room.
	 * This might include its area.
	 *
	 * @see LegalLibrary#isLandOwnable(Room)
	 * @see LegalLibrary#getLandOwnerName(Room)
	 *
	 * @param room the room to check
	 * @return "" or the name of the owner
	 */
	public String getPropertyOwnerName(Room room);

	/**
	 * If the given room with the given land title was recently
	 * created, or recently lost its owner and has gone back into
	 * a for-sale state, then this method will update or create
	 * its ID and fix its title and description.  Will save the
	 * room info to the DB.
	 *
	 * @param R the room that's for sale
	 * @param title the rooms land title
	 * @param reset reset the room description
	 */
	public void colorRoomForSale(Room R, LandTitle title, boolean reset);

	/**
	 * Returns the private property record of the given area,
	 * if one exists.
	 *
	 * @see LegalLibrary#getPropertyRecord(Room)
	 * @see LegalLibrary#getPropertyRecord(Area)
	 * @see LegalLibrary#getPropertyRecord(Item)
	 * @see LegalLibrary#getPropertyRecord(MOB)
	 * @see LegalLibrary#getLandTitle(Room)
	 *
	 * @param area the area to check
	 * @return null, or the LandTitle record
	 */
	public LandTitle getLandTitle(Area area);

	/**
	 * Returns the private property record of the given room,
	 * if one exists, or its area.
	 *
	 * @see LegalLibrary#getPropertyRecord(Room)
	 * @see LegalLibrary#getPropertyRecord(Area)
	 * @see LegalLibrary#getPropertyRecord(Item)
	 * @see LegalLibrary#getPropertyRecord(MOB)
	 * @see LegalLibrary#getLandTitle(Area)
	 *
	 * @param room the room to check
	 * @return null, or the LandTitle record
	 */
	public LandTitle getLandTitle(Room room);

	/**
	 * Returns the private property record of the given area,
	 * if one existsa.
	 * This could also be in a boardable.
	 *
	 * @see LegalLibrary#getPropertyRecord(Room)
	 * @see LegalLibrary#getPropertyRecord(Item)
	 * @see LegalLibrary#getPropertyRecord(MOB)
	 * @see LegalLibrary#getLandTitle(Area)
	 * @see LegalLibrary#getLandTitle(Room)
	 *
	 * @param area the area to check
	 * @return null, or the privateproperty record
	 */
	public PrivateProperty getPropertyRecord(Area area);

	/**
	 * Returns the private property record of the given mob,
	 * if one exists.  Slaves happen.
	 *
	 * @see LegalLibrary#getPropertyRecord(Room)
	 * @see LegalLibrary#getPropertyRecord(Item)
	 * @see LegalLibrary#getPropertyRecord(Area)
	 * @see LegalLibrary#getLandTitle(Area)
	 * @see LegalLibrary#getLandTitle(Room)
	 *
	 * @param mob the mob to check
	 * @return null, or the privateproperty record
	 */
	public PrivateProperty getPropertyRecord(MOB mob);


	/**
	 * Returns the private property record of the given room,
	 * if one exists, or its area.
	 * This could also be in a boardable.
	 *
	 * @see LegalLibrary#getPropertyRecord(Area)
	 * @see LegalLibrary#getPropertyRecord(Item)
	 * @see LegalLibrary#getPropertyRecord(MOB)
	 * @see LegalLibrary#getLandTitle(Area)
	 * @see LegalLibrary#getLandTitle(Room)
	 *
	 * @param room the room to check
	 * @return null, or the privateproperty record
	 */
	public PrivateProperty getPropertyRecord(Room room);

	/**
	 * Returns the private property record of the given item,
	 * if one exists.
	 * This could be carryables or boardables.
	 *
	 * @see LegalLibrary#getPropertyRecord(Area)
	 * @see LegalLibrary#getPropertyRecord(Room)
	 * @see LegalLibrary#getPropertyRecord(MOB)
	 * @see LegalLibrary#getLandTitle(Area)
	 * @see LegalLibrary#getLandTitle(Room)
	 *
	 * @param item the item to check
	 * @return null, or the privateproperty record
	 */
	public PrivateProperty getPropertyRecord(Item item);

	/**
	 * Given a title with an owner, this will return whether
	 * the given room, or any room adjacent to the given room,
	 * is owned by the same owner.
	 *
	 * @param title the title to serve as basis
	 * @param R the room to start checking from
	 * @return true if the room has the same owner
	 */
	public boolean isRoomSimilarlyTitled(LandTitle title, Room R);

	/**
	 * Returns all interconnected rooms that are private
	 * property and ownable, even if owned by no one. Does
	 * not return any upstairs or downstairs rooms.
	 *
	 *  @see LegalLibrary#isHomeRoomUpstairs(Room)
	 *  @see LegalLibrary#isHomeRoomDownstairs(Room)
	 *
	 * @param room the room to check
	 * @param doneRooms a required set to put all peer rooms in
	 * @return the given set
	 */
	public Set<Room> getHomePeersOnThisFloor(Room room, Set<Room> doneRooms);

	/**
	 * Given a room that is presumably a piece of property, this will return
	 * whether the given room is downstairs (not necc directly) of property
	 * rooms, even if owned by someone else.  Caves generally don't count.
	 * If a room has no upstairs, it can't be downstairs.
	 *
	 *  @see LegalLibrary#isHomeRoomUpstairs(Room)
	 *  @see LegalLibrary#getHomePeersOnThisFloor(Room, Set)
	 *
	 * @param room the room to check whether it is downstairs
	 * @return true if the given room is upstairs
	 */
	public boolean isHomeRoomDownstairs(Room room);

	/**
	 * Given a room that is presumably a piece of property, this will return
	 * whether the given room is upstairs (not necc directly) of property
	 * rooms, even if owned by someone else.  Caves generally don't count.
	 *
	 *  @see LegalLibrary#isHomeRoomDownstairs(Room)
	 *  @see LegalLibrary#getHomePeersOnThisFloor(Room, Set)
	 *
	 * @param room the room to check whether it is upstairs
	 * @return true if the given room is upstairs
	 */
	public boolean isHomeRoomUpstairs(Room room);

	/**
	 * Returns whether the given mob, or an optional given property owner,
	 * or literally anyone else in the room, has normal privileges in the
	 * room.
	 *
	 * A mob has normal privileges with record to property if they are the owner, married
	 * to the owner, has HOME_PRIVS clan privileges with clan owned property, or with the
	 * property of a clan that your clan is friendly with.  Followers don't matter.
	 *
	 * @see LegalLibrary#doesHavePrivilegesWith(MOB, PrivateProperty)
	 * @see LegalLibrary#doesHavePriviledgesHere(MOB, Room)
	 * @see LegalLibrary#doesHavePriviledgesInThisDirection(MOB, Room, Exit)
	 *
	 * @param mob the mob to check first
	 * @param overrideID "", or another property owner
	 * @param R the room with possible privileged
	 * @return true if there is a privileged one present
	 */
	public boolean doesAnyoneHavePrivilegesHere(MOB mob, String overrideID, Room R);

	/**
	 * A mob has normal privileges with record to property if they are the owner, married
	 * to the owner, has HOME_PRIVS clan privileges with clan owned property, or with the
	 * property of a clan that your clan is friendly with.  Followers don't matter.
	 * This returns whether the given mob has normal privileges in the given room.
	 *
	 * @see LegalLibrary#doesHavePrivilegesWith(MOB, PrivateProperty)
	 * @see LegalLibrary#doesAnyoneHavePrivilegesHere(MOB, String, Room)
	 * @see LegalLibrary#doesHavePriviledgesInThisDirection(MOB, Room, Exit)
	 *
	 * @param mob the mob
	 * @param room the room
	 * @return true if the mob has normal privileges in the room
	 */
	public boolean doesHavePriviledgesHere(MOB mob, Room room);

	/**
	 * Given a mob, a room the mob is currently in, and  an exit in that room that
	 * the mob would enter, this will return whether the given mob has normal
	 * privileges in the room in the direction of the exit.
	 *
	 * A mob has normal privileges with record to property if they are the owner, married
	 * to the owner, has HOME_PRIVS clan privileges with clan owned property, or with the
	 * property of a clan that your clan is friendly with.  Followers don't matter.
	 *
	 * @see LegalLibrary#doesHavePrivilegesWith(MOB, PrivateProperty)
	 * @see LegalLibrary#doesAnyoneHavePrivilegesHere(MOB, String, Room)
	 * @see LegalLibrary#doesHavePriviledgesHere(MOB, Room)
	 *
	 * @param mob the mob
	 * @param room the mobs current room
	 * @param exit the exit in the room denoting the direction
	 * @return true if the mob has privileges in that directions room
	 */
	public boolean doesHavePriviledgesInThisDirection(MOB mob, Room room, Exit exit);

	/**
	 * A mob has normal privileges with record to property if they are the owner, married
	 * to the owner, has HOME_PRIVS clan privileges with clan owned property, or with the
	 * property of a clan that your clan is friendly with.  Followers don't matter.
	 *
	 * @see LegalLibrary#doesHaveWeakPrivilegesWith(MOB, PrivateProperty)
	 * @see LegalLibrary#doesHavePriviledgesInThisDirection(MOB, Room, Exit)
	 * @see LegalLibrary#doesHavePriviledgesHere(MOB, Room)
	 * @see LegalLibrary#doesAnyoneHavePrivilegesHere(MOB, String, Room)
	 *
	 * @param mob the mob to check
	 * @param record the property to check
	 * @return true if the mob has normal privileges with the property
	 */
	public boolean doesHavePrivilegesWith(final MOB mob, final PrivateProperty record);

	/**
	 * Weak privileges are had when one owns property, is married to the
	 * owner, or is a member of the clan that owns the property
	 * (privileges notwithstanding).
	 *
	 * @see LegalLibrary#doesHaveWeakPriviledgesHere(MOB, Room)
	 *
	 * @param mob the mob to check
	 * @param record the record to check
	 * @return true if the mob has weak privileges with the property record
	 */
	public boolean doesHaveWeakPrivilegesWith(final MOB mob, final PrivateProperty record);

	/**
	 * Weak privileges are had when one owns a room, is married to the
	 * owner, is following someone who is an owner, or is a member of
	 * the clan that owns the property (privileges notwithstanding).
	 *
	 * @see LegalLibrary#doesHaveWeakPrivilegesWith(MOB, PrivateProperty)
	 *
	 * @param mob the mob to check
	 * @param room the room to check
	 * @return true if the mob has weak privileges in the room
	 */
	public boolean doesHaveWeakPriviledgesHere(MOB mob, Room room);

	/**
	 * Someone is considered the direct owner if they are the named owner.
	 * No other rules can make one a direct owner.  Not even marriage.
	 * The property does have to be owned by someone to have a direct
	 * owner, however.
	 *
	 * @see LegalLibrary#doesOwnThisLand(MOB, Room)
	 *
	 * @param name the name (mob or clan) to check for ownership status
	 * @param room the property to check
	 * @return true if the given name is the owner, false otherwise
	 */
	public boolean isLandOwnersName(String name, Room room);

	/**
	 * Someone is considered the owner if they are the named owner, married
	 * to the named owner, with property ownership privileges to clan owned
	 * property, or are Following someone who is otherwise an owner of some
	 * sort.
	 *
	 * @see LegalLibrary#isLandOwnersName(String, Room)
	 *
	 * @param mob the mob to check for ownership status
	 * @param room the property to check
	 * @return true if the given mob is an owner, false otherwise
	 */
	public boolean doesOwnThisLand(MOB mob, Room room);

	/**
	 * Given a mob and an item, this will return whether the mob
	 * has weak privileges with a property record on the item.
	 * It also affirms someone is following someone with weak
	 * privileges, or if the mob is in a room in which they
	 * have actual privileges.  If the given item has no property
	 * record of its own, it will return whether the mob has weak
	 * privileges with the room the item is in.
	 * Items could include boardables, but also any item with a
	 * privateproperty property.
	 *
	 * @see LegalLibrary#doesHavePrivilegesWith(MOB, PrivateProperty)
	 * @see LegalLibrary#doesHaveWeakPrivilegesWith(MOB, PrivateProperty)
	 *
	 * @param mob the mob to check possible item ownership of
	 * @param item the item to check
	 * @return true if there is some sort of ownership privilege
	 */
	public boolean mayOwnThisItem(MOB mob, Item item);

	/**
	 * Someone is considered the direct owner if they are the named owner.
	 * No other rules can make one a direct owner.  Not even marriage.
	 * The property does have to be owned by someone to have a direct
	 * owner, however.
	 *
	 * @see LegalLibrary#doesOwnThisProperty(MOB, PrivateProperty)
	 * @see LegalLibrary#doesOwnThisProperty(MOB, Room)
	 *
	 * @param name the name (mob or clan) to check for ownership status
	 * @param room the property to check
	 * @return true if the given name is the owner, false otherwise
	 */
	public boolean isPropertyOwnersName(String name, Room room);

	/**
	 * Someone is considered the owner if they are the named owner, married
	 * to the named owner, with property ownership privileges to clan owned
	 * property, or are Following someone who is otherwise an owner of some
	 * sort.
	 *
	 * @see LegalLibrary#doesOwnThisProperty(MOB, PrivateProperty)
	 * @see LegalLibrary#isPropertyOwnersName(String, Room)
	 *
	 * @param mob the mob to check for ownership status
	 * @param room the property to check
	 * @return true if the given mob is an owner, false otherwise
	 */
	public boolean doesOwnThisProperty(MOB mob, Room room);

	/**
	 * Someone is considered the owner if they are the named owner, married
	 * to the named owner, with property ownership privileges to clan owned
	 * property, or are Following someone who is otherwise an owner of some
	 * sort.
	 *
	 * @see LegalLibrary#doesOwnThisProperty(MOB, Room)
	 * @see LegalLibrary#isPropertyOwnersName(String, Room)
	 *
	 * @param mob the mob to check for ownership status
	 * @param record the property record to check
	 * @return true if the given mob is an owner, false otherwise
	 */
	public boolean doesOwnThisProperty(MOB mob, PrivateProperty record);

	/**
	 * This method is called whenever an action occurs on private property to
	 * determine whether the action might start a "robbery" state.  Robbery
	 * might occur when an item on private property is picked up by someone
	 * who lacks privileges to do so. This method can cancel the given
	 * event by returning false if need be.  If a robbery seems to occur,
	 * and there is a witness, then a crime may be added.  The stolen item
	 * itself will also attain a property that makes it unsellable.
	 *
	 * @see LegalLibrary#doesHavePriviledgesHere(MOB, Room)
	 *
	 * @param record the property record for the room
	 * @param msg the event to check
	 * @param quiet true to not report anything to the source of the event
	 * @return true to allow the event to occur, false to immediately cancel it
	 */
	public boolean robberyCheck(PrivateProperty record, CMMsg msg, boolean quiet);

	/**
	 * If the owner of the given property record is a clan, this will return
	 * the property owner mob.  If owned by a mob, it will return that mob.
	 * Either way, this may cause a player to be loaded permanently.
	 *
	 * @param record the property record
	 * @return the property owner mob
	 */
	public MOB getPropertyOwner(PrivateProperty record);

	/**
	 * Property can be attacked if it is unowned, owned by the attacker,
	 * is owned by a clan whose leader the attacker may fight, or owned
	 * by a person whom the attacker may fight.
	 *
	 * @param mob the attacker
	 * @param record the property record for the given property
	 * @return true if the attacker is allowed to attack it
	 */
	public boolean canAttackThisProperty(MOB mob, PrivateProperty record);

	/**
	 * Scans the given area metro for property titles, returning only the
	 * unique ones (so rooms groups under one title return only the 1 title).
	 *
	 * @param A the area whose rooms and children to scan for titles
	 * @param owner null for all titles, * for all owned titles, or owner name
	 * @param includeRentals true to include rental property, false otherwise
	 * @return the list of titles found
	 */
	public List<LandTitle> getAllUniqueLandTitles(Area A, String owner, boolean includeRentals);

	/**
	 * Cleric Infusion is actually more a part of Divine Law
	 * than the normal kind.  It refers to an official link
	 * between a specific deity and a particular item, place,
	 * or person.
	 *
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.Deity.DeityWorshipper
	 *
	 * @param P the thing to check for infusion
	 * @return null, or a deityworshipper object
	 */
	public DeityWorshipper getClericInfusion(Physical P);

	/**
	 * Cleric Infusion is actually more a part of Divine Law
	 * than the normal kind.  It refers to an official link
	 * between a specific deity and a particular item, place,
	 * or person.
	 *
	 * @param P the thing to check for infusion
	 * @return null, or the name of a deity
	 */
	public String getClericInfused(Physical P);

	/**
	 * Returns whether the given mob is legally considered
	 * a Police Officer in the place where the mob is presently
	 * located when this method is called.
	 *
	 * @see LegalLibrary#isLegalJudgeHere(MOB)
	 * @see LegalLibrary#isLegalOfficialHere(MOB)
	 *
	 * @param mob the mob to check
	 * @return true if a Police Officer, false otherwise
	 */
	public boolean isLegalOfficerHere(MOB mob);

	/**
	 * Returns whether the given mob is legally considered
	 * a Judge in the place where the mob is presently
	 * located when this method is called.
	 *
	 * @see LegalLibrary#isLegalOfficerHere(MOB)
	 * @see LegalLibrary#isLegalOfficialHere(MOB)
	 *
	 * @param mob the mob to check
	 * @return true if a judge, false otherwise
	 */
	public boolean isLegalJudgeHere(MOB mob);

	/**
	 * Returns whether the given mob is either a police
	 * officer or a judge in the place where the mob is
	 * presently located when this is called.
	 *
	 * @see LegalLibrary#isLegalJudgeHere(MOB)
	 * @see LegalLibrary#isLegalOfficerHere(MOB)
	 *
	 * @param mob the mob to check
	 * @return true if judge/officer, false otherwise
	 */
	public boolean isLegalOfficialHere(MOB mob);

	/**
	 * Returns whether the stats for the given area reflect
	 * that it is legally considered a City.
	 *
	 * @param A the area to check
	 * @return true if its a city, false otherwise
	 */
	public boolean isACity(Area A);
}
