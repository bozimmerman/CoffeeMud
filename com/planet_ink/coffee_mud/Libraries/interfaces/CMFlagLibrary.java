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
   Copyright 2005-2015 Bo Zimmerman

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
	 * Returns whether the given hearer mob can hear the given mob or
	 * object coming and going.  Mostly by comparing the sneaking and hear sneakers
	 * as well as general hearing ability.
	 * @param heard the thing or mob moving around
	 * @param hearer the mob who wants to hear it
	 * @return whether the given hearer can hear the heard
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
	public boolean isReadable(Item I);
	public void setReadable(Item I, boolean truefalse);
	public boolean isEnspelled(Physical F);
	public boolean isGettable(Item I);
	public void setGettable(Item I, boolean truefalse);
	public boolean isDroppable(Item I);
	public void setDroppable(Item I, boolean truefalse);
	public boolean isCataloged(Environmental E);
	public boolean isRemovable(Item I);
	public void setRemovable(Item I, boolean truefalse);
	public boolean isDeadlyOrMaliciousEffect(final PhysicalAgent P);
	public boolean isSavable(Physical P);
	public void setSavable(Physical P, boolean truefalse);
	public boolean hasSeenContents(Physical P);
	public String getAlignmentName(Environmental E);
	public boolean isSeen(Physical P);
	public boolean isCloaked(Physical P);
	public boolean isHidden(Physical P);
	public boolean isInvisible(Physical P);
	public boolean isGood(Physical P);
	public boolean isReallyGood(Physical P);
	public boolean isNeutral(Physical P);
	public boolean isReallyEvil(Physical P);
	public boolean isEvil(Physical P);
	public boolean isTrapped(Physical P);
	public boolean isATrackingMonster(MOB M);
	public boolean isTracking(MOB M);
	public boolean isSneaking(Physical P);
	public boolean isABonusItems(Physical P);
	public boolean isInDark(Physical P);
	public boolean isLightSource(Physical P);
	public boolean isRejuvingItem(Item I);
	public boolean isGlowing(Physical P);
	public boolean isGolem(Physical P);
	public boolean isSleeping(Physical P);
	public boolean isSitting(Physical P);
	public boolean isFlying(Physical P);
	public boolean isClimbing(Physical P);
	public boolean isCrawlable(Physical P);
	public boolean isInWilderness(Physical P);
	public boolean isSwimming(Physical P);
	public boolean isSwimmingInWater(Physical P);
	public boolean isWatery(Environmental E);
	public boolean isFalling(Physical P);
	public boolean isBusy(Physical P);
	public boolean isUndead(MOB mob);
	public boolean isOutsider(MOB mob);
	public boolean isAPlant(Item I);
	public boolean isAPlant(MOB M);
	public boolean isInsect(MOB mob);
	public boolean isVermin(MOB mob);
	public boolean isUnattackable(Physical P);
	public boolean isAliveAwakeMobileUnbound(MOB mob, boolean quiet);
	public boolean isAliveAwakeMobile(MOB mob, boolean quiet);
	public boolean isStanding(MOB mob);
	public boolean isBound(Physical P);
	public boolean isBoundOrHeld(Physical P);
	public boolean isOnFire(Physical seen);
	public boolean isWaterWorthy(Physical P);
	public boolean isInFlight(Physical P);
	public boolean isAnimalIntelligence(MOB M);
	public boolean isVegetable(MOB M);
	public boolean isMobile(PhysicalAgent P);
	public boolean isAggressiveTo(MOB M, MOB toM);
	public boolean isPossiblyAggressive(MOB M);
	public boolean isChild(Environmental E);
	public boolean isBaby(Environmental E);
	public boolean isMetal(Environmental E);
	public List<Behavior> flaggedBehaviors(PhysicalAgent P, long flag);
	public List<Ability> flaggedAnyAffects(Physical P, long flag);
	public List<Ability> flaggedAffects(Physical P, long flag);
	public List<Ability> flaggedAbilities(MOB M, long flag);
	public List<Ability> domainAnyAffects(Physical P, int domain);
	public List<Ability> domainAffects(Physical P, int domain);
	public List<Ability> domainAbilities(MOB M, int domain);
	public String getAbilityType(Ability A);
	public String getAbilityType_(Ability A);
	public String getAbilityDomain(Ability A);
	public int getAbilityType(String name);
	public int getAbilityType_(String name);
	public int getAbilityDomain(String name);
	public String getAge(MOB M);
	public int burnStatus(Environmental E);
	public boolean isInTheGame(Area E, boolean reqInhabitation);
	public boolean isInTheGame(Room E, boolean reqInhabitation);
	public boolean isInTheGame(Item E, boolean reqInhabitation);
	public boolean isInTheGame(MOB E, boolean reqInhabitation);
	public boolean isInTheGame(Environmental E, boolean reqInhabitation);
	public boolean enchanted(Item I);
	public StringBuffer colorCodes(Physical seen , MOB seer);
	public boolean seenTheSameWay(MOB seer, Physical seen1, Physical seen2);
	public String dispositionString(Physical seen, Disposition flag_msgType);
	public boolean stillAffectedBy(Physical obj, List<Ability> oneOf, boolean anyTallF);
	public String dispositionList(int disposition, boolean useVerbs);
	public String sensesList(int disposition, boolean useVerbs);
	public int getDispositionCode(String name);
	public int getSensesCode(String name);
	public String describeSenses(MOB mob);
	public String describeDisposition(MOB mob);
	public int getDetectScore(MOB seer);
	public int getHideScore(Physical seen);
	public boolean hasAControlledFollower(MOB invoker, Ability A);
	public boolean isAControlledFollower(MOB invoker, MOB mob, Ability A);
	
	public enum Disposition
	{
		ARRIVES,
		LEAVES,
		IS;
	}
}
