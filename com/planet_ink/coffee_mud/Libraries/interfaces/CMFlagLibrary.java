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
   Copyright 2005-2025 Bo Zimmerman

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
 * The Flag Library is full of shortcut methods for checking the
 * state of objects in a high level way.  This allows the underlying
 * functionality of those states to change more easily in the future.
 * Most of these check PhyStats disposition and senses flags, but
 * there are also methods for checking room states, sorting through
 * abilities by their flags, and similar methods.
 * I guess it's really a state-checking-catch-all library.
 * @author Bo Zimmerman
 *
 */
public interface CMFlagLibrary extends CMLibrary
{
	/**
	 * Return whether the given mob is blind or otherwise
	 * unable to see due to a strictly personal defect.
	 * @param M the mob to check
	 * @return whether the given mob is not blind
	 */
	public boolean canSee(MOB M);

	/**
	 * Returns whether the given physical object can be located
	 * by magical means.  Applies mostly to items.
	 * @param P the object to check
	 * @return true if it can be located, false otherwise
	 */
	public boolean canBeLocated(Physical P);

	/**
	 * Returns whether the given mob has the ability to see
	 * hidden.
	 * @param M the mob to check
	 * @return whether the mob can see hidden
	 */
	public boolean canSeeHidden(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * hidden items, but not necessarily mobs.
	 * @param M the mob to check
	 * @return whether the mob can see hidden items
	 */
	public boolean canSeeHiddenItems(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * invisible.
	 * @param M the mob to check
	 * @return whether the mob can see invisible
	 */
	public boolean canSeeInvisible(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * evil in people/things.
	 * @param M the mob to check
	 * @return whether the mob can see evil in people/things
	 */
	public boolean canSeeEvil(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * good in people/things.
	 * @param M the mob to check
	 * @return whether the mob can see good in people/things
	 */
	public boolean canSeeGood(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * chaos in people/things.
	 * @param P the thing to check
	 * @return whether the given can see chaos in people/things
	 */
	public boolean canSeeChaos(Physical P);

	/**
	 * Returns whether the given mob has the ability to see
	 * law in people/things.
	 * @param P the thing to check
	 * @return whether the given can see law in people/things
	 */
	public boolean canSeeLaw(Physical P);

	/**
	 * Returns whether the given mob has the ability to see
	 * sneaking mobs.
	 * @param M the mob to check
	 * @return whether the mob can see sneaking mobs
	 */
	public boolean canSeeSneakers(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * magic items as such.
	 * @param M the mob to check
	 * @return whether the mob can see magic items
	 */
	public boolean canSeeBonusItems(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * in the dark.
	 * @param M the mob to check
	 * @return whether the mob can see in the dark
	 */
	public boolean canSeeInDark(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * their enemies in the dark.
	 * @param M the mob to check
	 * @return whether the mob can see their enemies in the dark
	 */
	public boolean canSeeVictims(MOB M);

	/**
	 * Returns whether the given mob has the ability to see
	 * warm blooded mobs in the dark.
	 * @param M the mob to check
	 * @return whether the mob can see warm blooded mobs in the dark
	 */
	public boolean canSeeInfrared(MOB M);

	/**
	 * Return whether the given mob is deaf or otherwise
	 * unable to hear due to a strictly personal defect.
	 * @param M the mob to check
	 * @return whether the given mob is not deaf
	 */
	public boolean canHear(MOB M);

	/**
	 * Return whether the given mob is able to move at all.
	 * @param M the mob to check
	 * @return whether the given mob is not frozen
	 */
	public boolean canMove(MOB M);

	/**
	 * Returns whether the given mob or item is not allowed
	 * to be camped, meaning they won't respawn while
	 * someone is just hanging around waiting in the
	 * room for them to spawn.
	 * @param P the item or mob to check
	 * @return true if they can't be camped
	 */
	public boolean canNotBeCamped(Physical P);

	/**
	 * Returns whether this room allows mobs or players to enter
	 * or leave.  Usually a temporary flag.
	 * @param R the room to check
	 * @return true if its ok to enter and leave
	 */
	public boolean allowsMovement(Room R);

	/**
	 * Returns whether this area allows mobs or players to enter
	 * or leave.  Usually a temporary flag.
	 * @param A the area to check
	 * @return true if its ok to enter and leave
	 */
	public boolean allowsMovement(Area A);

	/**
	 * Return whether the given mob is stuffed or otherwise
	 * unable to smell due to a strictly personal defect.
	 * @param M the mob to check
	 * @return whether the given mob is able to smell
	 */
	public boolean canSmell(MOB M);

	/**
	 * Returns whether the given mob can smell the given
	 * target, by issueing a test 'sniff' message to it.
	 * @param M the mob
	 * @param target the target
	 * @return true if the mob can smell the target
	 */
	public boolean canSmell(final MOB M, final Physical target);

	/**
	 * Return whether the given mob is able to eat or
	 * drink due to the lack of strictly personal defects.
	 * @param M the mob to check
	 * @return whether the given mob is able to eat and drink
	 */
	public boolean canTaste(MOB M);

	/**
	 * Return whether the given mob is mute or otherwise
	 * unable to speak due to a strictly personal defect.
	 * @param M the mob to check
	 * @return whether the given mob is not mute
	 */
	public boolean canSpeak(MOB M);

	/**
	 * Return whether the given mob is able to breathe at all
	 * due to a strictly personal defect.
	 * @param M the mob to check
	 * @return whether the given mob is able to breathe
	 */
	public boolean canBreathe(MOB M);

	/**
	 * Returns whether the given mob is capable of swimming,
	 * even if they are not presently doing so.
	 * @param M the mob to check
	 * @return true if a swimmer
	 */
	public boolean canSwim(final MOB M);

	/**
	 * Return whether the given mob is able to breathe the given
	 * resource due to their race, usually.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @param M the mob to check
	 * @param atmoResource the Resource to check for breathability
	 * @return whether the given mob is able to breathe that
	 */
	public boolean canBreatheThis(MOB M, int atmoResource);

	/**
	 * Return whether the given mob is able to breathe in the given
	 * room due to their race and the atmosphere of the room.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @param M the mob to check
	 * @param R the room the mob is trying to breathe in
	 * @return whether the given mob is able to breathe here
	 */
	public boolean canBreatheHere(MOB M, Room R);

	/**
	 * Returns whether the given mob has the ability to see
	 * metal items as such.
	 * @param M the mob to check
	 * @return whether the mob can see metal items
	 */
	public boolean canSeeMetal(MOB M);

	/**
	 * Returns whether the given thing is allowed to do mundane
	 * things, such as patrolling or tracking or being mobile.
	 * @param P the item or mob
	 * @return true if they can mobile and patrol and such
	 */
	public boolean canTrack(Physical P);

	/**
	 * Returns whether the given mob is allowed to use their auto
	 * attack every tick, if that even applies to this system.
	 * @param M the mob to check
	 * @return true if they are allowed to auto-attack
	 */
	public boolean canAutoAttack(MOB M);

	/**
	 * Returns whether the given mob has the ability to concentrate
	 * enough to cast spells.
	 * @param M the mob to check
	 * @return whether the mob can cast spells and concentrate and stuff
	 */
	public boolean canConcentrate(MOB M);

	/**
	 * Returns whether the given hearer mob can hear the given mob or
	 * object moving around.  Mostly by comparing the sneaking and hear sneakers
	 * as well as general hearing ability.
	 * @param heard the thing or mob moving around
	 * @param hearer the mob who wants to hear it
	 * @return whether the given hearer can hear the heard
	 */
	public boolean canBeHeardMovingBy(Physical heard , MOB hearer);

	/**
	 * Returns whether the given hearer mob can hear the given mob or
	 * object speaking.  Mostly just checks whether the hearer can hear.
	 * @param heard the thing or mob speaking
	 * @param hearer the mob who wants to hear it
	 * @return whether the given hearer can hear the heard
	 */
	public boolean canBeHeardSpeakingBy(Physical heard , MOB hearer);

	/**
	 * Returns whether the given sensor can see or hear the given sensed
	 * mob or object moving around.
	 * @param sensed the thing being sensed
	 * @param sensor the mob who wants to sense it
	 * @return true if the moving sensed can be sensed by the sensor
	 */
	public boolean canSenseMoving(Physical sensed, MOB sensor);

	/**
	 * Returns whether the given sensor mob can sense the given mob or
	 * object coming and going.  Mostly by comparing the sneaking and hear sneakers
	 * as well as general seeing and hearing ability.
	 * @param sensed the thing or mob moving around
	 * @param sensor the mob who wants to sense it
	 * @return whether the given sensor can sense the sensed
	 */
	public boolean canSenseEnteringLeaving(Physical sensed, MOB sensor);

	/**
	 * Returns whether the given mob can 'access' the given area, due
	 * to it not being hidden and sharing a timezone.  Usually applies
	 * to knowledge of, as opposed to entry.
	 * @param mob the mob who wants to access an area
	 * @param A the area to access
	 * @return true if the mob can 'access' the area
	 */
	public boolean canAccess(MOB mob, Area A);

	/**
	 * Returns whether the given mob can 'access' the given room, due
	 * to it not being hidden and sharing a timezone.  Usually applies
	 * to knowledge of, as opposed to entry.
	 * @param mob the mob who wants to access a room
	 * @param R the room to access
	 * @return true if the mob can 'access' the room
	 */
	public boolean canAccess(MOB mob, Room R);

	/**
	 * Checks the system validity of mobs, items, and rooms,
	 * and returns a report if there's a problem and it
	 * should be destroyed.  If a mob report return an
	 * asterisk, then it probably means he just needs
	 * killing, not destroying.
	 *
	 * @param P the room, mob, or item to check
	 * @return null if all is well, and a reason otherwise
	 */
	public String validCheck(final Physical P);

	/**
	 * Returns whether the given affecting (usually a mob) is alive,
	 * awake, mobile, and officially in the game.  This is a lighter
	 * test than some of the others that also check if its busy.
	 * @param affecting the thing to check
	 * @return true if the thing can act at all
	 */
	public boolean canActAtAll(Tickable affecting);

	/**
	 * Returns whether the given affecting (usually a mob) is alive,
	 * awake, mobile, and officially in the game, and not in combat,
	 * sufficiently healthy, and not following anyone.
	 * @param affecting the thing to check
	 * @return true if the thing can act freely
	 */
	public boolean canFreelyBehaveNormal(Tickable affecting);

	/**
	 * Returns whether the given seen mob or item or room or whatever
	 * can be seen by the given seer, given conditions, lighting,
	 * hidden, etc.
	 * @param seen the thing to be seen.
	 * @param seer the seer who wants to see it
	 * @return true if the seer can see the seen
	 */
	public boolean canBeSeenBy(Environmental seen , MOB seer);

	/**
	 * Returns whether the given seen mob or item or room or whatever
	 * can only BARELY be seen by the given seer, given moon lightning
	 * mostly.  Always returns false unless the BARELY applies.
	 * @param seen the thing to be seen.
	 * @param seer the seer who wants to see it
	 * @return true if the seer can see the seen BARELY
	 */
	public boolean canBarelyBeSeenBy(Environmental seen , MOB seer);

	/**
	 * Returns whether the given mob is mounted on a small portable
	 * movable, such as a horse, or boat on water.
	 * @param mob the mob to check
	 * @return true if mounted
	 */
	public boolean isMobileMounted(final MOB mob);

	/**
	 * Returns whether the given item is marked as being readable.
	 * @see CMFlagLibrary#setReadable(Item, boolean)
	 * @param I the item to check
	 * @return true if it is readable, false otherwise
	 */
	public boolean isReadable(Item I);

	/**
	 * Sets whether the given item is marked as being readable.
	 * @see CMFlagLibrary#isReadable(Item)
	 * @param I the item to set
	 * @param truefalse true if readable, false otherwise
	 */
	public void setReadable(Item I, boolean truefalse);

	/**
	 * Returns whether the given item, mob, whatever is marked as being
	 * affected by an uninvokeable spell, chant, prayer, or whatever.
	 * @param F the room, item, or mob to check
	 * @return true if it is enspelled, false otherwise
	 */
	public boolean isEnspelled(Physical F);

	/**
	 * Returns whether the given item is marked as being gettable.
	 * @see CMFlagLibrary#setGettable(Item, boolean)
	 * @param I the item to check
	 * @return true if it is gettable, false otherwise
	 */
	public boolean isGettable(Item I);

	/**
	 * Sets whether the given item is marked as being gettable.
	 * @see CMFlagLibrary#isGettable(Item)
	 * @param I the item to set
	 * @param truefalse true if gettable, false otherwise
	 */
	public void setGettable(Item I, boolean truefalse);

	/**
	 * Returns whether the given item is marked as being droppable.
	 * @see CMFlagLibrary#setDroppable(Item, boolean)
	 * @param I the item to check
	 * @return true if it is droppable, false otherwise
	 */
	public boolean isDroppable(Item I);

	/**
	 * Sets whether the given item is marked as being droppable.
	 * @see CMFlagLibrary#isDroppable(Item)
	 * @param I the item to set
	 * @param truefalse true if droppable, false otherwise
	 */
	public void setDroppable(Item I, boolean truefalse);

	/**
	 * Returns whether the given item is marked as being removable.
	 * @see CMFlagLibrary#setRemovable(Item, boolean)
	 * @param I the item to check
	 * @return true if it is removable, false otherwise
	 */
	public boolean isRemovable(Item I);

	/**
	 * Sets whether the given item is marked as being removable.
	 * @see CMFlagLibrary#isRemovable(Item)
	 * @param I the item to set
	 * @param truefalse true if removable, false otherwise
	 */
	public void setRemovable(Item I, boolean truefalse);

	/**
	 * Returns whether the given item is marked as being an
	 * instance of a mob or item from the catalog.
	 * @param E the item or mob to check
	 * @return true if it is cataloged, false otherwise
	 */
	public boolean isCataloged(Environmental E);

	/**
	 * Returns whether the given object is marked as being allowed
	 * to be saved to the database.  Most created objects are, though
	 * items and mobs generated by temporary spells often are not.
	 * @see CMFlagLibrary#setSavable(Physical, boolean)
	 * @param P the item, mob, whatever to check
	 * @return true if it is saveable, false otherwise
	 */
	public boolean isSavable(Physical P);

	/**
	 * Sets whether the given object is marked as being allowed
	 * to be saved to the database.  Most created objects are, though
	 * items and mobs generated by temporary spells often are not.
	 * @see CMFlagLibrary#isSavable(Physical)
	 * @param P the item, mob, whatever to set
	 * @param truefalse true if it is saveable, false otherwise
	 */
	public void setSavable(Physical P, boolean truefalse);

	/**
	 * Returns whether the given non-mob (item) has
	 * contents which has be seen.  Default is true.
	 * @param P the item to check
	 * @return true if contents have has seen
	 */
	public boolean isWithSeenContents(Physical P);

	/**
	 * Returns whether the given item can not be seen
	 * at a simple look around, but requires either
	 * the ability to see hidden, or a very careful
	 * look (longlook) around.
	 * @param P the item to check
	 * @return true if it is hard to spot.
	 */
	public boolean isHiddenInPlainSight(final Physical P);

	/**
	 * Returns whether the item is kept by their player
	 * owner even if they die and are separated from their
	 * corpse.
	 *
	 * @param P the item to check
	 * @return true if it is kept over death
	 */
	public boolean isKeptOverDeath(final Physical P);

	/**
	 * Returns whether the given item is a container which,
	 * when open, has accessible contents, and is also open.
	 * @param I the item to check
	 * @return true if the contents are accessible right now.
	 */
	public boolean isOpenAccessibleContainer(Item I);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * seeable at all.  This is beyond invisibility to the
	 * metaphysically unseeable.
	 * @param P the room, mob, or item to check
	 * @return true if it is seeable
	 */
	public boolean isSeeable(Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * cloaked. This prevents a kind of global awareness, not
	 * connected to local seeability.
	 * @param P the room, mob, or item to check
	 * @return true if it is cloaked
	 */
	public boolean isCloaked(Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * hidden.
	 * @param P the room, mob, or item to check
	 * @return true if it is hidden
	 */
	public boolean isHidden(Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * invisible.
	 * @param P the room, mob, or item to check
	 * @return true if it is invisible
	 */
	public boolean isInvisible(Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * goodly aligned.
	 * @param P the room, mob, or item to check
	 * @return true if it is goodness
	 */
	public boolean isGood(Physical P);

	/**
	 * Returns whether the given mobby factionmember is
	 * goodly aligned due to factions.
	 * @param M the factionmember to check
	 * @return true if it is goodness
	 */
	public boolean isReallyGood(FactionMember M);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * neutral, which is to say, neither good nor evil.
	 * @param P the room, mob, or item to check
	 * @return true if it is neutral
	 */
	public boolean isNeutral(Physical P);

	/**
	 * Returns whether the given mobby factionmember is
	 * neutrally aligned due to factions.
	 * @param M the factionmember to check
	 * @return true if it is neutral
	 */
	public boolean isReallyNeutral(FactionMember M);

	/**
	 * Returns whether the given mobby factionmember is
	 * evilly aligned due to factions.
	 * @param M the factionmember to check
	 * @return true if it is evilness
	 */
	public boolean isReallyEvil(FactionMember M);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * evilly aligned.
	 * @param P the room, mob, or item to check
	 * @return true if it is evilness
	 */
	public boolean isEvil(Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * lawfully inclined, assuming you are using the law/chaos axis.
	 * @param P the room, mob, or item to check
	 * @return true if it is lawfullness
	 */
	public boolean isLawful(final Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * chaotically inclined, assuming you are using the law/chaos axis.
	 * @param P the room, mob, or item to check
	 * @return true if it is chaoticness
	 */
	public boolean isChaotic(final Physical P);

	/**
	 * Returns whether the given item, mob, room, whatever is
	 * moderately inclined, assuming you are using the law/chaos axis.
	 * @param P the room, mob, or item to check
	 * @return true if it is chaoticness
	 */
	public boolean isModerate(final Physical P);

	/**
	 * Returns whether the given object has a trap set on it.
	 * Whether triggered or no.
	 * @param P the object to check
	 * @return true if there's a trap, false otherwise
	 */
	public boolean isTrapped(Physical P);

	/**
	 * Returns whether the given mob is both a non-player
	 * npc, and is currently being driven (tracking)
	 * towards something.
	 * @param M the mob to check
	 * @return true if its a tracking npc, false otherwise
	 */
	public boolean isATrackingMonster(MOB M);

	/**
	 * Returns whether the given mob is a slave, possibly for
	 * the given master.
	 * @param slaveM the mob to check
	 * @param masterM null, or the master to check for
	 * @return true if its a slave, false otherwise
	 */
	public boolean isASlave(MOB slaveM, MOB masterM);

	/**
	 * Returns whether the given mob is a slave.
	 * @param slaveM the mob to check
	 * @return true if its a slave, false otherwise
	 */
	public boolean isASlave(final MOB slaveM);

	/**
	 * Returns whether the given player or npc mob is being
	 * driven (tracking) towards something
	 * @param M the mob to check
	 * @return true if its a tracking mob, false otherwise
	 */
	public boolean isTracking(MOB M);

	/**
	 * Returns whether the given mob, item whatever is sneaking
	 * @param P the object to check
	 * @return true if its a sneaking mob, false otherwise
	 */
	public boolean isSneaking(Physical P);

	/**
	 * Returns whether the given mob, item, whatever is flagged
	 * as being magical.
	 * @param P the mob/item/whatever to check
	 * @return true if its flagged magical, false otherwise
	 */
	public boolean isABonusItems(Physical P);

	/**
	 * Returns whether the given mob, item, whatever is currently
	 * flagged dark
	 * @param P the mob/item/whatever to check
	 * @return true if its the dark, false otherwise
	 */
	public boolean isInDark(Physical P);

	/**
	 * Returns whether the given mob, item, whatever is currently
	 * flagged as a light source
	 * @param P the mob/item/whatever to check
	 * @return true if its a light source, false otherwise
	 */
	public boolean isLightSource(Physical P);

	/**
	 * Returns whether the given item has an item rejuv
	 * actively monitoring it.
	 * @param I the item to check
	 * @return true if the item is active rejuv tracked
	 */
	public boolean isRejuvingItem(Item I);

	/**
	 * Returns whether the given item, mob, whatever is
	 * glowing.
	 * @param P the item, mob, whatever to check
	 * @return true if it is glowing, false otherwise
	 */
	public boolean isGlowing(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as a golem.
	 * @param P the item, mob, whatever to check
	 * @return true if it is a golem, false otherwise
	 */
	public boolean isGolem(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * sleeping.
	 * @param P the item, mob, whatever to check
	 * @return true if it is sleeping, false otherwise
	 */
	public boolean isSleeping(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * sitting.
	 * @param P the item, mob, whatever to check
	 * @return true if it is sitting, false otherwise
	 */
	public boolean isSitting(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * flying.
	 * @param P the item, mob, whatever to check
	 * @return true if it is flying, false otherwise
	 */
	public boolean isFlying(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * floating freely and uncontrolled, as when not
	 * under gravity.
	 * @param P the item, mob, whatever to check
	 * @return true if it is floating, false otherwise
	 */
	public boolean isFloatingFreely(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * climbing.
	 * @param P the item, mob, whatever to check
	 * @return true if it is climbing, false otherwise
	 */
	public boolean isClimbing(Physical P);

	/**
	 * Returns whether the given room, area, exit, whatever is
	 * crawlable (not necc. crawling).
	 * @param P the room, area, exit, whatever to check
	 * @return true if it is crawlable, false otherwise
	 */
	public boolean isCrawlable(Physical P);

	/**
	 * Returns whether the given mob, item, room, whatever is
	 * in a outdoors non-city room.
	 * @param P the mob, item, room, whatever to check
	 * @return true if it is outdoors, false otherwise
	 */
	public boolean isInWilderness(Physical P);

	/**
	 * Returns whether the given mob, item, room, whatever is
	 * in a city street type room.
	 * @param P the mob, item, room, whatever to check
	 * @return true if it is in a city street, false otherwise
	 */
	public boolean isACityRoom(final Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as swimming/floating.
	 * @param P the item, mob, whatever to check
	 * @return true if it is marked swimming, false otherwise
	 */
	public boolean isSwimming(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * swimming/floating in a watery room.
	 * @param P the item, mob, whatever to check
	 * @return true if it is swimming in water, false otherwise
	 */
	public boolean isSwimmingInWater(Physical P);

	/**
	 * Returns whether the given room, whatever is
	 * airy, such as an in the air, or an open space.
	 * @param R the room to check
	 * @return true if it is airy, false otherwise
	 */
	public boolean isAiryRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * driveable, such as a road, street, etc.
	 * @param R the room to check
	 * @return true if it is driveable, false otherwise
	 */
	public boolean isDrivableRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * watery, such as a water surface, underwater, etc.
	 * @param R the room to check
	 * @return true if it is watery, false otherwise
	 */
	public boolean isWateryRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * watery, such as a deep water surface, underwater, etc.
	 * @param R the room to check
	 * @return true if it is deep watery, false otherwise
	 */
	public boolean isDeepWateryRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * watery, such as a water surface, etc.
	 * @param R the room to check
	 * @return true if it is water surfacy, false otherwise
	 */
	public boolean isWaterySurfaceRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * watery, such as an underwater, etc.
	 * @param R the room to check
	 * @return true if it is underwatery, false otherwise
	 */
	public boolean isUnderWateryRoom(Room R);

	/**
	 * Returns whether the given room, whatever is
	 * the surface of deep water, such as a water surface, etc.
	 * with an underwater room
	 * @param R the room to check
	 * @return true if it is water surfacy, false otherwise
	 */
	public boolean isDeepWaterySurfaceRoom(Room R);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as falling.
	 * @param P the item, mob, whatever to check
	 * @return true if it is marked falling, false otherwise
	 */
	public boolean isFalling(Physical P);

	/**
	 * Returns which direction, if any, the given object
	 * is falling.
	 * @see com.planet_ink.coffee_mud.core.Directions
	 * @param P the item, mob, whatever to check
	 * @return direction it is falling
	 */
	public int getFallingDirection(Physical P);

	/**
	 * Returns whether the given player is
	 * executing a command taking longer than 30 seconds
	 * @param M the mob to check
	 * @return true if it is long running commanding, false otherwise
	 */
	public boolean isRunningLongCommand(MOB M);

	/**
	 * Returns whether the given mob is of an undead race type.
	 * @param mob the mob to check
	 * @return true if it is an undead type, false otherwise
	 */
	public boolean isUndead(MOB mob);

	/**
	 * Returns whether the given race is of an undead race type.
	 * @param R the race to check
	 * @return true if it is an undead type, false otherwise
	 */
	public boolean isUndead(final Race R);

	/**
	 * Returns whether the given race is of an egg-laying type
	 * @param race the race to check
	 * @return true if it lays eggs, false otherwise
	 */
	public boolean isEggLayer(Race race);

	/**
	 * Returns whether the given mob is of a fishy race type.
	 * @param mob the mob to check
	 * @return true if it is a fishy type, false otherwise
	 */
	public boolean isFish(MOB mob);

	/**
	 * Returns whether the given mob is of a marine race type.
	 * @param mob the mob to check
	 * @return true if it is a marine type, false otherwise
	 */
	public boolean isMarine(MOB mob);

	/**
	 * Returns whether the given mob is of an outsider race type.
	 * @param mob the mob to check
	 * @return true if it is an outsider type, false otherwise
	 */
	public boolean isOutsider(MOB mob);

	/**
	 * Returns whether the given mob is of an insect race type.
	 * @param mob the mob to check
	 * @return true if it is an insect type, false otherwise
	 */
	public boolean isInsect(MOB mob);

	/**
	 * Returns whether the given mob is of a vermin race type.
	 * @param mob the mob to check
	 * @return true if it is a vermin type, false otherwise
	 */
	public boolean isVermin(MOB mob);

	/**
	 * Returns whether the given mob is of a vegetable race type.
	 * @param M the mob to check
	 * @return true if it is a vegetable type, false otherwise
	 */
	public boolean isVegetable(MOB M);

	/**
	 * Returns whether the given mob is of a plant-like type.
	 * @param M the mob to check
	 * @return true if it is a plant type, false otherwise
	 */
	public boolean isAPlant(MOB M);

	/**
	 * Returns whether the given item is of a plant-like type.
	 * @param I the item to check
	 * @return true if it is a plant type, false otherwise
	 */
	public boolean isAPlant(Item I);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as unattackable.
	 * @param P the item, mob, whatever to check
	 * @return true if it is marked unattackable, false otherwise
	 */
	public boolean isUnattackable(Physical P);

	/**
	 * Returns whether the given mob is alive, awake, mobile
	 * and not bound up.  With optional explanation!
	 * @see CMFlagLibrary#isAliveAwakeMobile(MOB, boolean)
	 * @param mob the mob to check
	 * @param quiet true to not tell the mob what's wrong
	 * @return true if he/she is alive, awake, etc
	 */
	public boolean isAliveAwakeMobileUnbound(MOB mob, boolean quiet);

	/**
	 * Returns whether the given mob is alive, awake, and mobile.
	 * With optional explanation!
	 * @see CMFlagLibrary#isAliveAwakeMobileUnbound(MOB, boolean)
	 * @param mob the mob to check
	 * @param quiet true to not tell the mob what's wrong
	 * @return true if he/she is alive, awake, etc
	 */
	public boolean isAliveAwakeMobile(MOB mob, boolean quiet);

	/**
	 * Returns whether the given mob is standing (not sitting
	 * or sleeping)
	 * marked as swimming/floating.
	 * @param mob the mob to check
	 * @return true if it is standing, false otherwise
	 */
	public boolean isStanding(MOB mob);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as bound.
	 * @see CMFlagLibrary#isBoundOrHeld(Physical)
	 * @param P the item, mob, whatever to check
	 * @return true if it is marked bound, false otherwise
	 */
	public boolean isBound(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * marked as bound or is paralyzed.
	 * @see CMFlagLibrary#isBound(Physical)
	 * @param P the item, mob, whatever to check
	 * @return true if it is bound or paralyzed, false otherwise
	 */
	public boolean isBoundOrHeld(Physical P);

	/**
	 * Returns whether the given item, mob, whatever is
	 * on fire.
	 * @param seen the item, mob, whatever to check
	 * @return true if it is on fire, false otherwise
	 */
	public boolean isOnFire(Physical seen);

	/**
	 * Returns whether the given mob, item, whatever would
	 * sink or float.  If it's water worthy, it will float.
	 * This checks things like the material of items, the
	 * swimming status of mobs, the boats they are in, etc.
	 * @param P the item, mob, whatever to check
	 * @return true if it is water worthy, false otherwise.
	 */
	public boolean isWaterWorthy(Physical P);

	/**
	 * Returns whether the given mob, item, whatever would
	 * fly or fall.  If it's flight worthy, it will fly.
	 * This checks things like the flight status of items,
	 * and mobs, the vehicles they are in, etc.
	 * @param P the item, mob, whatever to check
	 * @return true if it is in flight, false otherwise.
	 */
	public boolean isInFlight(Physical P);

	/**
	 * Returns whether the given mob is of low animal
	 * intelligence.
	 * @param M the mob to check
	 * @return true if its of animal intelligence, false otherwise
	 */
	public boolean isAnimalIntelligence(MOB M);


	/**
	 * Returns whether the given mob is classifyable as
	 * an animal, being breedable, but with low intelligence.
	 * @param M the mob to check
	 * @return true if its an animal, false otherwise
	 */
	public boolean isAnAnimal(final MOB M);

	/**
	 * Returns whether the given mob, item, whatever has a
	 * behavior making it move around.
	 * @param P the mob, item, whatever to check
	 * @return true if the object will move around, false otherwise
	 */
	public boolean isMobile(PhysicalAgent P);

	/**
	 * Returns whether the first given mob is potentially or
	 * probably agressive to the second mob.
	 * @param M the mob who might be aggressive
	 * @param toM the mob who might be in trouble
	 * @return true if the first mob might be aggressive
	 */
	public boolean isAggressiveTo(MOB M, MOB toM);

	/**
	 * Returns whether the first given mob is potentially or
	 * probably agressive.
	 * @param M the mob who might be aggressive
	 * @return true if the mob might be aggressive
	 */
	public boolean isPossiblyAggressive(MOB M);

	/**
	 * Returns whether the given mob, item (baby) whatever is
	 * a baby, or a mob child born from something, what its
	 * age still being tracked by property.
	 * @param E the potential child
	 * @return true if its a child, false otherwise
	 */
	public boolean isAgedChild(Environmental E);

	/**
	 * Returns whether the given mob (item usually) is a
	 * baby waiting to grow up.
	 * @param E the item to check
	 * @return true if its a baby, false otherwise
	 */
	public boolean isBaby(Environmental E);

	/**
	 * Returns whether the given mob (item usually) is made
	 * of metal or mithril.
	 * @param E the item (or mob) to check
	 * @return true if its made of metal, false otherwise
	 */
	public boolean isMetal(Environmental E);

	/**
	 * Returns the proper type/flavor of direction
	 * for the given mob, which depends on what
	 * sort of place he/she is in.
	 *
	 * @see Directions.DirType
	 * @see CMFlagLibrary#getDirType(Physical)
	 *
	 * @param M the mob to check
	 * @return the DirType code
	 */
	public Directions.DirType getInDirType(final MOB M);

	/**
	 * Returns the proper type/flavor of direction
	 * appropriate to the given object, based on whether
	 * the given physical actually IS a sailing ship,
	 * space ship, caravan, or other.  This might include
	 * a mob standing in for such a vessel.
	 *
	 * @see Directions.DirType
	 * @see CMFlagLibrary#getInDirType(MOB)
	 *
	 * @param P the object to inspect
	 * @return the DirType code
	 */
	public Directions.DirType getDirType(final Physical P);

	/**
	 * If the given thing is related to a ship, it returns
	 * the navBasis of the boardable, such as water or
	 * land-based.
	 * @param E the thing or place to check
	 * @return the Rideable.Basis.*
	 */
	public Rideable.Basis getNavRideBasis(final Environmental E);

	/**
	 * Returns whether the given room, item, whatever has a
	 * deadly effect, such as a trap, autodeath behavior, or
	 * a property with a malicious spell in it.
	 * @param P the room, item, whatever to check
	 * @return true if it has a potentially malicious effect
	 */
	public boolean isDeadlyOrMaliciousEffect(final PhysicalAgent P);

	/**
	 * Returns whether the given Area is actually in the game, or is
	 * just temporary or cached.
	 * @param E the Area to check
	 * @param reqInhabitation meaningless
	 * @return true if the area is in the game, false otherwise
	 */
	public boolean isInTheGame(Area E, boolean reqInhabitation);

	/**
	 * Returns whether the given Room is actually in the game, or is
	 * just temporary or cached.
	 * @param E the Room to check
	 * @param reqInhabitation meaningless
	 * @return true if it is in the game, false otherwise
	 */
	public boolean isInTheGame(Room E, boolean reqInhabitation);

	/**
	 * Returns whether the given Item is actually in the game, and in
	 * a room, or is just temporary or cached.
	 * @param I the Item to check
	 * @param reqInhabitation true if it must be a in room, false otherwise
	 * @return true if it is in the game, false otherwise
	 */
	public boolean isInTheGame(Item I, boolean reqInhabitation);

	/**
	 * Returns whether the given MOB is actually in the game, and in
	 * a room, or is just temporary or cached.
	 * @param M the MOB to check
	 * @param reqInhabitation true if it must be a in room, false otherwise
	 * @return true if it is in the game, false otherwise
	 */
	public boolean isInTheGame(MOB M, boolean reqInhabitation);

	/**
	 * Returns whether the given mob, item, room, area, whatever is actually
	 * in the game, and possibly in a room, or is just temporary or cached.
	 * @param E the mob, item, whatever to check
	 * @param reqInhabitation true if it must be a in room, false otherwise
	 * @return true if it is in the game, false otherwise
	 */
	public boolean isInTheGame(Environmental E, boolean reqInhabitation);

	/**
	 * Returns whether the given item has some effect other than a disease,
	 * poison, or standard property.
	 * @param I the item to check
	 * @return true for a spell or some other enchantment, false otherwise
	 */
	public boolean isEnchanted(Item I);

	/**
	 * Returns whether the given item can be classified as a 'rope' for
	 * various purposes.
	 * @param I the item to check
	 * @return true if its a rope, and false otherwise
	 */
	public boolean isARope(final Item I);

	/**
	 * Returns whether the given invoker mob is controlling the
	 * given mob, who is following the invoker, optionally using
	 * the given Ability.
	 * @param invoker the mob who might be the leader
	 * @param mob the mob who is following the leader
	 * @param A the ability that might be causing the control
	 * @return true if the mob is a controlled follower
	 */
	public boolean isAControlledFollower(MOB invoker, MOB mob, Ability A);

	/**
	 * Returns whether the given invoker mob is controlling a
	 * mob, who is following the invoker, optionally using
	 * the given Ability.
	 * @param invoker the mob who might be the leader
	 * @param A the ability that might be causing the control
	 * @return true if the mob is a controlled follower
	 */
	public boolean hasAControlledFollower(MOB invoker, Ability A);

	/**
	 * Returns the simple word that would describe the alignment
	 * of the given mob or item or whatever.
	 * @param E the mob or item or whatever
	 * @return the name of its alignment, e.g. good, evil, neutral
	 */
	public String getAlignmentName(Environmental E);

	/**
	 * Returns the simple word that would describe the inclination
	 * of the given mob or item or whatever.
	 * @param E the mob or item or whatever
	 * @return the name of its alignment, e.g. lawful, chaotic, moderate
	 */
	public String getInclinationName(final Environmental E);

	/**
	 * Deprecated, but returns the total hide detection score
	 * for the given mob.
	 * @param seer the mob trying to see hidden
	 * @return the total hide detection score
	 */
	public int getDetectScore(MOB seer);

	/**
	 * Deprecated, but returns the total hide score
	 * for the given mob, item, whatever.
	 * @param seen the mob, item trying to be hidden
	 * @return the total hide score
	 */
	public int getHideScore(Physical seen);

	/**
	 * Returns the list of behaviors that have the given behavior
	 * flag(s) set.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior#FLAG_LEGALBEHAVIOR
	 * @param P the mob, item, room, whatever
	 * @param flag the behavior flags
	 * @return the list of behaviors that have the given flag(s) set.
	 */
	public List<Behavior> flaggedBehaviors(PhysicalAgent P, long flag);

	/**
	 * Returns the list of effects that have the given ability
	 * flag(s) set.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags()
	 * @param P the mob, item, room, whatever
	 * @param flag the ability flags
	 * @return the list of effects that have the given flag(s) set.
	 */
	public List<Ability> flaggedAnyAffects(Physical P, long flag);

	/**
	 * Returns the list of effects that have the given ability
	 * flag(s) set.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags()
	 * @param P the mob, item, room, whatever
	 * @param flag the ability flags
	 * @return the list of effects that have the given flag(s) set.
	 */
	public List<Ability> flaggedAffects(Physical P, long flag);

	/**
	 * Returns the list of Abilities that have the given ability
	 * flag(s) set.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags()
	 * @param M the mob
	 * @param flag the ability flags
	 * @return the list of abilities that have the given flag(s) set.
	 */
	public List<Ability> flaggedAbilities(MOB M, long flag);

	/**
	 * Returns the list of effects that have are part of the
	 * given ability domain.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_ABJURATION
	 * @param P the mob, item, room, whatever
	 * @param domain the ability domain
	 * @return the list of effects in the given domain.
	 */
	public List<Ability> domainAnyAffects(Physical P, int domain);

	/**
	 * Returns the list of effects that have are part of the
	 * given ability domain.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_ABJURATION
	 * @param P the mob, item, room, whatever
	 * @param domain the ability domain
	 * @return the list of effects in the given domain.
	 */
	public List<Ability> domainAffects(Physical P, int domain);

	/**
	 * Returns the list of abilities that have are part of the
	 * given ability domain.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_ABJURATION
	 * @param M the mob to check
	 * @param domain the ability domain
	 * @return the list of abilities in the given domain.
	 */
	public List<Ability> domainAbilities(MOB M, int domain);

	/**
	 * Returns the list of effects that are on the given physical,
	 * and were invoked by the given invoker.  The ability flag,
	 * abilityCode, and domain are optional, and may be -1 to ignore.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#DOMAIN_ABJURATION
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#flags()
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_CHANT
	 * @param invoker the invoker to search for, or null
	 * @param P the object to check for effects, REQUIRED
	 * @param flag -1, or an Ability Flag
	 * @param abilityCode -1, or an ability Code
	 * @param domain -1, or an ability domain
	 * @return the list of effects that match
	 */
	public List<Ability> matchedAffects(final MOB invoker, final Physical P, final long flag, final int abilityCode, final int domain);

	/**
	 * Returns the ability type/code name for the given Ability.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_CHANT
	 * @param A the Ability
	 * @return "" or the Ability code of the given Ability
	 */
	public String getAbilityType(Ability A);

	/**
	 * Returns the ability type/code name for the given Ability.
	 * This one has any spaces in the type replaced with _
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_CHANT
	 * @param A the Ability
	 * @return "" or the Ability code of the given Ability
	 */
	public String getAbilityType_(Ability A);

	/**
	 * Returns the ability domain name for the given Ability.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability.DOMAIN#DESCS
	 * @param A the Ability
	 * @return "" or the Ability domain name of the given Ability
	 */
	public String getAbilityDomain(Ability A);

	/**
	 * Returns the ability type code for the given ability type name.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_CHANT
	 * @param name the name of the given ability type
	 * @return the Ability type code of the given name
	 */
	public int getAbilityType(String name);

	/**
	 * Returns the ability type code for the given ability type name.
	 * This one has any spaces in the type replaced with _
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability#ACODE_CHANT
	 * @param name the name of the given ability type
	 * @return the Ability type code of the given name
	 */
	public int getAbilityType_(String name);

	/**
	 * Returns the ability domain bitmask for the given ability domain name.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability.DOMAIN#DESCS
	 * @param name the name of the given ability domain
	 * @return "" or the Ability code of the given name
	 */
	public int getAbilityDomain(String name);

	/**
	 * Returns the friendly descriptive age of the given mob,
	 * whether baby, child, player, whatever.
	 * @see CMFlagLibrary#getAgeYears(Physical)
	 * @param M the mob to check
	 * @return the friendly age of the mob
	 */
	public String getAge(MOB M);

	/**
	 * Returns the age of the mob or item in years, or -1.
	 * @see CMFlagLibrary#getAge(MOB)
	 * @param P the mob or item or baby to inspect
	 * @return the age in years, or -1
	 */
	public int getAgeYears(final Physical P);

	/**
	 * Returns the name of the plane of existence upon which
	 * the given physical object is located, or null for
	 * Prime Material
	 *
	 * @param P the object to check
	 * @return the name of the plane of existence, or null
	 */
	public String getPlaneOfExistence(final Physical P);

	/**
	 * Returns the disposition blurbs that apply to the given seen
	 * mob, item, room, whatever, as soon by the given seer mob.
	 * These are friendly colored string
	 * @param seen the mob, item, room, whatever
	 * @param seer the one seeing
	 * @return the disposition blurbs list
	 */
	public String getDispositionBlurbs(Physical seen , MOB seer);

	/**
	 * Returns whether the two given physical objects appear the exact
	 * same way to the given seer mob.  This is for determining duplicates
	 * in item lists mostly.
	 * @param seer the mob who is seeing stuff
	 * @param seen1 the first object being seen
	 * @param seen2 the first object being seen
	 * @return true if the two seens appear the same way
	 */
	public boolean isSeenTheSameWay(MOB seer, Physical seen1, Physical seen2);

	/**
	 * Returns the present-tense verb that applies to the disposition of the
	 * given seen thing, given the detail about how they verbbing.
	 * @param seen the mob (or item) that is verbbing
	 * @param flag_msgType whether they are arriving, leaving, or sitting there verbbing
	 * @return the appropriate verb word for their disposition
	 */
	public String getPresentDispositionVerb(Physical seen, ComingOrGoing flag_msgType);

	/**
	 * Checks the list of ability objects against the effects of the given mob, item
	 * or whatever.
	 * @param obj the mob, item, room, whatever
	 * @param oneOf the list of abilities that might be affects
	 * @param anyTallF true to return true on ANY, false to return true only for ALL
	 * @return true if the conditions are met, false otherwise
	 */
	public boolean isStillAffectedBy(Physical obj, List<Ability> oneOf, boolean anyTallF);

	/**
	 * Returns a delimited list of verb description of the disposition
	 * of the given disposition bitmap.
	 * @param disposition the bitmap
	 * @param delimiter the string between verbs
	 * @return the delimited list of descriptive disposition verbs
	 */
	public String getDispositionVerbList(final long disposition, final String delimiter);

	/**
	 * Returns a delimited list of Causes verb description of the senses
	 * of the given senses bitmap.
	 * @param senses the bitmap
	 * @param delimiter the string between verbs
	 * @return the delimited list of descriptive senses verbs
	 */
	public String getSensesVerbList(final long senses, final String delimiter);

	/**
	 * Returns a delimited list of CAN descriptions of the senses
	 * of the given senses bitmap.
	 * @param senses the bitmap
	 * @param delimiter the string between descriptions
	 * @return the delimited list of descriptive senses
	 */
	public String getSensesDescList(final long senses, final String delimiter);

	/**
	 * Returns a command-delimited list of dispassionate description of the disposition
	 * of the given physical mob, item, whatever.
	 * @param obj the disposed physical mob, item, whatever
	 * @param useVerbs true to return an active phrase, flags for a state phrase
	 * @return the comma-delimited list of descriptive dispositions
	 */
	public String getDispositionDescList(Physical obj, boolean useVerbs);

	/**
	 * Returns a command-delimited list of dispassionate description of the senses
	 * of the given physical mob, item, whatever.
	 * @param obj the sensing physical mob, item, whatever
	 * @param useVerbs true to return an active phrase, flags for a state phrase
	 * @return the comma-delimited list of descriptive senses
	 */
	public String getSensesDescList(Physical obj, boolean useVerbs);

	/**
	 * Returns the enumerated disposition associated with the given
	 * disposition name, such as ISSWIMMING, etc.
	 * @param name the disposition name
	 * @return the Disposition enum
	 */
	public Disposition getDisposition(String name);

	/**
	 * Returns the enumerated senses index associated with the given
	 * senses name, such as CANSEEDARK, etc.
	 * @param name the senses name
	 * @return the index
	 */
	public Senses getSenses(String name);

	/**
	 * Returns a comma delimited list of the senses masks
	 * on the given mob.  These are lowercase state phrases.
	 * @param mob the mob to check
	 * @return the list of senses mask words
	 */
	public String getSensesStateList(MOB mob);

	/**
	 * Returns a comma delimited list of can see senses-list
	 * strings matching the given mask.
	 *
	 * @param senseMask int senseMask the mask to use
	 * @return a list string
	 */
	public String getMaskedCanSeeList(final int senseMask);

	/**
	 * Returns a comma delimited list of disposition-is
	 * strings matching the given mask.
	 *
	 * @param dispositionMask the mask to use
	 * @return a list string
	 */
	public String getMaskedDispositionIsList(final int dispositionMask);

	/**
	 * Returns a comma delimited list of the senses masks
	 * on the given mob.  These are lowercase state phrases.
	 * @param mob the mob to check
	 * @return the list of senses mask words
	 */
	public String getDispositionStateList(MOB mob);

	/**
	 * Returns whether the given item is affected by
	 * or contains alcohol as a drinkable.
	 * @param thang the mob or item to check
	 * @return true if it is alcohol
	 */
	public boolean isAlcoholic(Physical thang);

	/**
	 * Returns the parents of the given baby or mob
	 *
	 * @param thang the baby or mob
	 * @return the parents, if any, or empty
	 */
	public List<String> getParents(final Physical thang);

	/**
	 * A disposition enum for various flagg methods.
	 * Helps determine the english phrasing of what's
	 * returned.
	 * @author Bo Zimmerman
	 *
	 */
	public enum ComingOrGoing
	{
		ARRIVES,
		LEAVES,
		IS;
	}

	/**
	 * Enum representing the Disposition IS_* constants in PhyStats
	 * The localization and string helper.
	 *
	 * @author Bo Zimmerman
	 */
	public static enum Disposition
	{
		ISUNSEEN(PhyStats.IS_NOT_SEEN),
		ISHIDDEN(PhyStats.IS_HIDDEN),
		ISINVISIBLE(PhyStats.IS_INVISIBLE),
		ISEVIL(PhyStats.IS_EVIL),
		ISGOOD(PhyStats.IS_GOOD),
		ISSNEAKING(PhyStats.IS_SNEAKING),
		ISBONUS(PhyStats.IS_BONUS),
		ISDARK(PhyStats.IS_DARK),
		ISGOLEM(PhyStats.IS_GOLEM),
		ISSLEEPING(PhyStats.IS_SLEEPING),
		ISSITTING(PhyStats.IS_SITTING),
		ISFLYING(PhyStats.IS_FLYING),
		ISSWIMMING(PhyStats.IS_SWIMMING),
		ISGLOWING(PhyStats.IS_GLOWING),
		ISCLIMBING(PhyStats.IS_CLIMBING),
		ISFALLING(PhyStats.IS_FALLING),
		ISLIGHT(PhyStats.IS_LIGHTSOURCE),
		ISBOUND(PhyStats.IS_BOUND),
		ISCLOAKED(PhyStats.IS_CLOAKED),
		ISUNSAVABLE(PhyStats.IS_UNSAVABLE),
		ISCATALOGED(PhyStats.IS_CATALOGED),
		ISUNATTACKABLE(PhyStats.IS_UNATTACKABLE),
		ISCUSTOM(PhyStats.IS_CUSTOM),
		ISUNHELPFUL(PhyStats.IS_UNHELPFUL)
		;
		private final int bitMask;
		private String desc= null;
		private String state = null;
		private String verb = null;
		private Disposition(final int mask)
		{
			bitMask = mask;
		}

		public int getMask()
		{
			return bitMask;
		}

		public String getCode()
		{
			return name();
		}

		public String getIsDesc()
		{
			if(desc == null)
				localize();
			return desc;
		}
		public String getVerb()
		{
			if(verb == null)
				localize();
			return verb;
		}
		public String getState()
		{
			if(state == null)
				localize();
			return state;
		}

		private void localize()
		{
			//CMLib.lang().L("Causes disappearance")
			switch(this)
			{
			case ISUNSEEN:
				desc = CMLib.lang().L("Is never seen");
				state = CMLib.lang().L("unseeable");
				verb = CMLib.lang().L("Causes Nondetectability");
				break;
			case ISHIDDEN:
				desc = CMLib.lang().L("Is hidden");
				state = CMLib.lang().L("hidden");
				verb = CMLib.lang().L("Causes hide");
				break;
			case ISINVISIBLE:
				desc = CMLib.lang().L("Is invisible");
				state = CMLib.lang().L("invisible");
				verb = CMLib.lang().L("Causes invisibility");
				break;
			case ISEVIL:
				desc = CMLib.lang().L("Evil aura");
				state = CMLib.lang().L("evil");
				verb = CMLib.lang().L("Creates Evil aura");
				break;
			case ISGOOD:
				desc = CMLib.lang().L("Good aura");
				state = CMLib.lang().L("good");
				verb =  CMLib.lang().L("Creates Good aura");
				break;
			case ISSNEAKING:
				desc = CMLib.lang().L("Is sneaking");
				CMLib.lang().L("sneaks");
				verb = CMLib.lang().L("Causes sneaking");
				break;
			case ISBONUS:
				desc = CMLib.lang().L("Is magical");
				state = CMLib.lang().L("sacred");
				verb = CMLib.lang().L("Creates magical aura");
				break;
			case ISDARK:
				desc = CMLib.lang().L("Is dark");
				state = CMLib.lang().L("darkness");
				verb = CMLib.lang().L("Creates dark aura");
				break;
			case ISGOLEM:
				desc = CMLib.lang().L("Is golem");
				state = "";
				verb = CMLib.lang().L("Creates golem aura");
				break;
			case ISSLEEPING:
				desc = CMLib.lang().L("Is sleeping");
				state = CMLib.lang().L("sleepy");
				verb = CMLib.lang().L("Causes sleeping");
				break;
			case ISSITTING:
				desc = CMLib.lang().L("Is sitting");
				state = CMLib.lang().L("crawls");
				verb = CMLib.lang().L("Causes sitting");
				break;
			case ISFLYING:
				desc = CMLib.lang().L("Is flying");
				state = CMLib.lang().L("flies");
				verb = CMLib.lang().L("Allows flying");
				break;
			case ISSWIMMING:
				desc = CMLib.lang().L("Is swimming");
				state = CMLib.lang().L("swims");
				verb = CMLib.lang().L("Causes swimming");
				break;
			case ISGLOWING:
				desc = CMLib.lang().L("Is glowing");
				state = CMLib.lang().L("glowing");
				verb = CMLib.lang().L("Causes glowing aura");
				break;
			case ISCLIMBING:
				desc = CMLib.lang().L("Is climbing");
				state = CMLib.lang().L("climbing");
				verb = CMLib.lang().L("Allows climbing");
				break;
			case ISFALLING:
				desc = CMLib.lang().L("Is falling");
				state = CMLib.lang().L("falling");
				verb = CMLib.lang().L("Causes falling");
				break;
			case ISLIGHT:
				desc = CMLib.lang().L("Is a light source");
				state = CMLib.lang().L("shining");
				verb = CMLib.lang().L("Causes a light source");
				break;
			case ISBOUND:
				desc = CMLib.lang().L("Is binding");
				state = CMLib.lang().L("bound");
				verb = CMLib.lang().L("Causes binding");
				break;
			case ISCLOAKED:
				desc = CMLib.lang().L("Is Cloaked");
				state = CMLib.lang().L("cloaked");
				verb = CMLib.lang().L("Causes cloaking");
				break;
			case ISUNSAVABLE:
				desc = CMLib.lang().L("Is never saved");
				state = "";
				verb = CMLib.lang().L("Causes unsavability");
				break;
			case ISCATALOGED:
				desc = CMLib.lang().L("Is cataloged");
				state = "";
				verb = CMLib.lang().L("Created from a template");
				break;
			case ISUNATTACKABLE:
				desc = CMLib.lang().L("Is unattackable");
				state = "";
				verb = CMLib.lang().L("Prevents attackability");
				break;
			case ISCUSTOM:
				desc = CMLib.lang().L("Is something");
				state = "";
				verb = CMLib.lang().L("Causes something...");
				break;
			case ISUNHELPFUL:
				desc = CMLib.lang().L("Is Unhelpful");
				state = "";
				verb = CMLib.lang().L("Prevents helpful attacks");
				break;
			}
		}
	}

	/**
	 * Enum representing the Senses CAN_* constants in PhyStats
	 * The localization and string helper.
	 *
	 * @author Bo Zimmerman
	 */
	public static enum Senses
	{
		CANNOTSEE(PhyStats.CAN_NOT_SEE),
		CANSEEHIDDEN(PhyStats.CAN_SEE_HIDDEN),
		CANSEEINVISIBLE(PhyStats.CAN_SEE_INVISIBLE),
		CANSEEEVIL(PhyStats.CAN_SEE_EVIL),
		CANSEEGOOD(PhyStats.CAN_SEE_GOOD),
		CANSEESNEAKERS(PhyStats.CAN_SEE_SNEAKERS),
		CANSEEBONUS(PhyStats.CAN_SEE_BONUS),
		CANSEEDARK(PhyStats.CAN_SEE_DARK),
		CANSEEINFRARED(PhyStats.CAN_SEE_INFRARED),
		CANNOTHEAR(PhyStats.CAN_NOT_HEAR),
		CANNOTMOVE(PhyStats.CAN_NOT_MOVE),
		CANNOTSMELL(PhyStats.CAN_NOT_SMELL),
		CANNOTTASTE(PhyStats.CAN_NOT_TASTE),
		CANNOTSPEAK(PhyStats.CAN_NOT_SPEAK),
		CANNOTBREATHE(PhyStats.CAN_NOT_BREATHE),
		CANSEEVICTIM(PhyStats.CAN_SEE_VICTIM),
		CANSEEMETAL(PhyStats.CAN_SEE_METAL),
		CANNOTTHINK(PhyStats.CAN_NOT_THINK),
		CANNOTTRACK(PhyStats.CAN_NOT_TRACK),
		CANNOTAUTOATTACK(PhyStats.CAN_NOT_AUTO_ATTACK),
		CANNOTBECAMPED(PhyStats.CAN_NOT_BE_CAMPED),
		CANGRUNTWHENSTUPID(PhyStats.CAN_GRUNT_WHEN_STUPID),
		CANSEEITEMSHIDDEN(PhyStats.CAN_SEE_HIDDEN_ITEMS)
		;
		int bitMask;
		private String desc = null;
		private String verb = null;
		private String state = null;
		private Senses(final int mask)
		{
			bitMask = mask;
		}

		public int getMask()
		{
			return bitMask;
		}

		public String getCode()
		{
			return name();
		}

		public String getDesc()
		{
			if(desc == null)
				localize();
			return desc;
		}

		public String getVerb()
		{
			if(verb == null)
				localize();
			return verb;
		}

		public String getState()
		{
			if(state == null)
				localize();
			return state;
		}

		private void localize()
		{
			switch(this)
			{
			case CANNOTAUTOATTACK:
				desc = CMLib.lang().L("Is not auto-attacking");
				verb = CMLib.lang().L("Prevents auto attacking");
				state = CMLib.lang().L("can't auto attack");
				break;
			case CANNOTBECAMPED:
				desc = CMLib.lang().L("Can not be camped on");
				verb = CMLib.lang().L("Prevents camping");
				state = CMLib.lang().L("can't be camped");
				break;
			case CANNOTBREATHE:
				desc = CMLib.lang().L("Can not breathe");
				verb = CMLib.lang().L("Causes choking");
				state = CMLib.lang().L("can't breathe");
				break;
			case CANNOTHEAR:
				desc = CMLib.lang().L("Is Deaf");
				verb = CMLib.lang().L("Causes Deafness");
				state = CMLib.lang().L("deaf");
				break;
			case CANNOTMOVE:
				desc = CMLib.lang().L("Is Paralyzed");
				verb = CMLib.lang().L("Causes Paralyzation");
				state = CMLib.lang().L("can't move");
				break;
			case CANNOTSEE:
				desc = CMLib.lang().L("Is Blind");
				verb = CMLib.lang().L("Causes Blindness");
				state = CMLib.lang().L("blind");
				break;
			case CANNOTSMELL:
				desc = CMLib.lang().L("Can not smell");
				verb = CMLib.lang().L("Deadens smell");
				state = CMLib.lang().L("can't smell");
				break;
			case CANNOTSPEAK:
				desc = CMLib.lang().L("Is Mute");
				verb = CMLib.lang().L("Causes Muteness");
				state = CMLib.lang().L("can't speak");
				break;
			case CANNOTTASTE:
				desc = CMLib.lang().L("Can not eat");
				verb = CMLib.lang().L("Disallows eating");
				state = CMLib.lang().L("can't eat");
				break;
			case CANNOTTHINK:
				desc = CMLib.lang().L("Can not concentrate");
				verb = CMLib.lang().L("Befuddles the mind");
				state = CMLib.lang().L("can't think straight");
				break;
			case CANNOTTRACK:
				desc = CMLib.lang().L("Is off the grid");
				verb = CMLib.lang().L("Makes un-trackable");
				state = CMLib.lang().L("can't be tracked");
				break;
			case CANSEEBONUS:
				desc = CMLib.lang().L("Can see magic");
				verb = CMLib.lang().L("Allows see magic");
				state = CMLib.lang().L("detect magic");
				break;
			case CANSEEDARK:
				desc = CMLib.lang().L("Can see in the dark");
				verb = CMLib.lang().L("Allows darkvision");
				state = CMLib.lang().L("darkvision");
				break;
			case CANSEEEVIL:
				desc = CMLib.lang().L("Can see evil");
				verb = CMLib.lang().L("Allows see evil");
				state = CMLib.lang().L("detect evil");
				break;
			case CANSEEGOOD:
				desc = CMLib.lang().L("Can see good");
				verb = CMLib.lang().L("Allows see good");
				state = CMLib.lang().L("detect good");
				break;
			case CANSEEHIDDEN:
				desc = CMLib.lang().L("Can see hidden");
				verb = CMLib.lang().L("Allows see hidden");
				state = CMLib.lang().L("see hidden");
				break;
			case CANSEEINFRARED:
				desc = CMLib.lang().L("Has infravision");
				verb = CMLib.lang().L("Allows infravision");
				state = CMLib.lang().L("infravision");
				break;
			case CANSEEINVISIBLE:
				desc = CMLib.lang().L("Can see invisible");
				verb = CMLib.lang().L("Allows see invisible");
				state = CMLib.lang().L("see invisible");
				break;
			case CANSEEITEMSHIDDEN:
				desc = CMLib.lang().L("Can see hidden items");
				verb = CMLib.lang().L("Allows see hidden items");
				state = CMLib.lang().L("see hidden items");
				break;
			case CANSEEMETAL:
				desc = CMLib.lang().L("Can detect metal");
				verb = CMLib.lang().L("Allows detect metal");
				state = CMLib.lang().L("metalvision");
				break;
			case CANSEESNEAKERS:
				desc = CMLib.lang().L("Can detect sneakers");
				verb = CMLib.lang().L("Allows detect sneakers");
				state = CMLib.lang().L("see sneaking");
				break;
			case CANSEEVICTIM:
				desc = CMLib.lang().L("Can detect victims");
				verb = CMLib.lang().L("Allows detect victims");
				state = CMLib.lang().L("detects victims");
				break;
			case CANGRUNTWHENSTUPID:
				desc = CMLib.lang().L("Can grunt");
				verb = CMLib.lang().L("Allows stupid grunting");
				state = CMLib.lang().L("can grunt");
				break;
			}

		}
	}
}
