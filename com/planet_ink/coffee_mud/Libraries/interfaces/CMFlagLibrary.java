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
   Copyright 2000-2013 Bo Zimmerman

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
public interface CMFlagLibrary extends CMLibrary
{
	public final static int flag_arrives=0;
	public final static int flag_leaves=1;
	public final static int flag_is=2;
	
	public boolean canSee(MOB M);
	public boolean canBeLocated(Physical P);
	public boolean canSeeHidden(MOB M);
	public boolean canSeeInvisible(MOB M);
	public boolean canSeeEvil(MOB M);
	public boolean canSeeGood(MOB M);
	public boolean canSeeSneakers(MOB M);
	public boolean canSeeBonusItems(MOB M);
	public boolean canSeeInDark(MOB M);
	public boolean canSeeVictims(MOB M);
	public boolean canSeeInfrared(MOB M);
	public boolean canHear(MOB M);
	public boolean canMove(MOB M);
	public boolean allowsMovement(Room R);
	public boolean allowsMovement(Area A);
	public boolean canSmell(MOB M);
	public boolean canTaste(MOB M);
	public boolean canSpeak(MOB M);
	public boolean canBreathe(MOB M);
	public boolean canBreatheHere(MOB M, Room R);
	public boolean canSeeMetal(MOB M);
	public boolean canWorkOnSomething(MOB M);
	public boolean canConcentrate(MOB M);
	public boolean isReadable(Item I);
	public boolean isEnspelled(Physical F);
	public boolean isGettable(Item I);
	public boolean isDroppable(Item I);
	public boolean isCataloged(Environmental E);
	public boolean isRemovable(Item I);
	public boolean isDeadlyOrMaliciousEffect(final PhysicalAgent P);
	public boolean isSavable(Physical P);
	public void setSavable(Physical P, boolean truefalse);
	public boolean hasSeenContents(Physical P);
	public void setReadable(Item I, boolean truefalse);
	public void setGettable(Item I, boolean truefalse);
	public void setDroppable(Item I, boolean truefalse);
	public void setRemovable(Item I, boolean truefalse);
	public boolean isSeen(Physical P);
	public boolean isCloaked(Physical P);
	public boolean isHidden(Physical P);
	public boolean isInvisible(Physical P);
	public boolean isReallyGood(Physical P);
	public boolean isReallyEvil(Physical P);
	public boolean isEvil(Physical P);
	public boolean isTrapped(Physical P);
	public boolean isATrackingMonster(MOB M);
	public boolean isTracking(MOB M);
	public boolean isGood(Physical P);
	public String getAlignmentName(Environmental E);
	public boolean isNeutral(Physical P);
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
	public boolean isSwimming(Physical P);
	public boolean isSwimmingInWater(Physical P);
	public boolean isFalling(Physical P);
	public boolean isBusy(Physical P);
	public boolean isUnattackable(Physical P);
	public boolean canBeHeardMovingBy(Physical heard , MOB hearer);
	public boolean canBeHeardSpeakingBy(Physical heard , MOB hearer);
	public boolean canSenseMoving(Physical sensed, MOB sensor);
	public boolean canSenseEnteringLeaving(Physical sensed, MOB sensor);
	public boolean aliveAwakeMobileUnbound(MOB mob, boolean quiet);
	public boolean aliveAwakeMobile(MOB mob, boolean quiet);
	public boolean isStanding(MOB mob);
	public boolean isBound(Physical P);
	public boolean isBoundOrHeld(Physical P);
	public boolean isOnFire(Physical seen);
	public boolean canBeSeenBy(Environmental seen , MOB seer);
	public boolean canBarelyBeSeenBy(Environmental seen , MOB seer);
	public StringBuffer colorCodes(Physical seen , MOB seer);
	public boolean seenTheSameWay(MOB seer, Physical seen1, Physical seen2);
	public String dispositionString(Physical seen, int flag_msgType);
	public boolean isWaterWorthy(Physical P);
	public boolean isInFlight(Physical P);
	public boolean isAnimalIntelligence(MOB M);
	public boolean isVegetable(MOB M);
	public boolean isMobile(PhysicalAgent P);
	public boolean isAggressiveTo(MOB M, MOB toM);
	public boolean isPossiblyAggressive(MOB M);
	public boolean isChild(Environmental E);
	public boolean isBaby(Environmental E);
	public List<Behavior> flaggedBehaviors(PhysicalAgent P, long flag);
	public List<Ability> flaggedAnyAffects(Physical P, long flag);
	public List<Ability> flaggedAffects(Physical P, long flag);
	public List<Ability> flaggedAbilities(MOB M, long flag);
	public List<Ability> domainAnyAffects(Physical P, int domain);
	public List<Ability> domainAffects(Physical P, int domain);
	public List<Ability> domainAbilities(MOB M, int domain);
	public String getAge(MOB M);
	public boolean canAccess(MOB mob, Area A);
	public boolean canAccess(MOB mob, Room R);
	public boolean isMetal(Environmental E);
	public int burnStatus(Environmental E);
	public boolean isInTheGame(Area E, boolean reqInhabitation);
	public boolean isInTheGame(Room E, boolean reqInhabitation);
	public boolean isInTheGame(Item E, boolean reqInhabitation);
	public boolean isInTheGame(MOB E, boolean reqInhabitation);
	public boolean isInTheGame(Environmental E, boolean reqInhabitation);
	public boolean enchanted(Item I);
	public boolean stillAffectedBy(Physical obj, List<Ability> oneOf, boolean anyTallF);
	public String dispositionList(int disposition, boolean useVerbs);
	public String sensesList(int disposition, boolean useVerbs);
	public int getDispositionCode(String name);
	public int getSensesCode(String name);
	public String getAbilityType(Ability A);
	public String getAbilityDomain(Ability A);
	public String describeSenses(MOB mob);
	public String describeDisposition(MOB mob);
	public int getAbilityType(String name);
	public int getAbilityDomain(String name);
	public int getDetectScore(MOB seer);
	public int getHideScore(Physical seen);
	public boolean canActAtAll(Tickable affecting);
	public boolean canFreelyBehaveNormal(Tickable affecting);
	public boolean hasAControlledFollower(MOB invoker, Ability A);
	public boolean isAControlledFollower(MOB invoker, MOB mob, Ability A);
}
