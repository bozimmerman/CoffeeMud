package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
/*
   Copyright 2016-2018 Bo Zimmerman

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
 * A physical object in the world that is capable of engaging in combat
 * with others.
 * 
 * @author Bo Zimmerman
 *
 */
public interface Combatant extends PhysicalAgent
{
	/**
	 * Returns whether this combatant is in an active combat state
	 * @see MOB#getVictim()
	 * @see MOB#setVictim(MOB)
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#makePeace(boolean)
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @return true if this combatant is in combat, false otherwise
	 */
	public boolean isInCombat();

	/**
	 * Sets the distance between this combatant and the current combat
	 * victim.  This method only matters if the combatant is in combat
	 * and getCombatant() returns a non-null value.  
	 * This method does not reciprocate by setting the range to
	 * target of the combat target.
	 * @see MOB#getVictim()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#rangeToTarget()
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @param newRange the range from this combatant to their target
	 */
	public void setRangeToTarget(int newRange);

	/**
	 * Gets the distance between this combatant and the current combat
	 * victim.  This method only matters if the combatant is in combat
	 * and getCombatant() returns a non-null value.
	 * @see MOB#getVictim()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @return newRange the range from this combatant to their target
	 */
	public int rangeToTarget();

	/**
	 * Gets the compass direction between this combatant and the current combat
	 * victim.  This method only matters if the combatant is in combat
	 * and getCombatant() returns a non-null value.
	 * @see MOB#getVictim()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @return cardinal direction from this combatant to their target
	 */
	public int getDirectionToTarget();
	
	/**
	 * Returns whether this combatant is permitted to attack the
	 * given combatant, both this combatant and the potential target are alive,
	 * both the combatant and the target are confirmed to be the same
	 * place.
	 * @see MOB#getVictim()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayPhysicallyAttack(PhysicalAgent)
	 * @param victim the potential combat target
	 * @return true if this combatant can attack the given combatant, false otherwise
	 */
	public boolean mayPhysicallyAttack(PhysicalAgent victim);

	/**
	 * Returns whether this combatant is both permitted to attack the
	 * given combatant, and that both this combatant and the potential target
	 * are alive.  Being in the same place is not necessary.
	 * @see MOB#getVictim()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayPhysicallyAttack(PhysicalAgent)
	 * @param victim the potential combat target
	 * @return true if this combatant can fight the given combatant, false otherwise
	 */
	public boolean mayIFight(PhysicalAgent victim);

	/**
	 * Clears the combat state between this combatant and their
	 * target, clears the targets combat state, as well as
	 * that of any followers of this combatant.  It is at best
	 * an approximation of a universal combat ender.
	 * @see Combatant#isInCombat()
	 * @see MOB#getVictim()
	 * @see MOB#setVictim(MOB)
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#getCombatant()
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @param includePlayerFollowers false to apply only to npc followers, true for npc and player
	 */
	public void makePeace(boolean includePlayerFollowers);
	
	/**
	 * If this mob is in combat, this returns the mob that this mob is
	 * targeting. If this method returns null, the mob is not in combat.
	 * @see Combatant#isInCombat()
	 * @see Combatant#setCombatant(PhysicalAgent)
	 * @see Combatant#makePeace(boolean)
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @return the combat target, or null for a peace state
	 */
	public PhysicalAgent getCombatant();
	
	/**
	 * Sets the mob that this mob is targeting for combat, which
	 * either puts them into, or clears their combat state. 
	 * If a null value, the mob is no longer fighting.
	 * @see Combatant#isInCombat()
	 * @see Combatant#getCombatant()
	 * @see Combatant#makePeace(boolean)
	 * @see Combatant#setRangeToTarget(int)
	 * @see Combatant#mayIFight(PhysicalAgent)
	 * @param other the combat target, or null for a peace state
	 */
	public void setCombatant(PhysicalAgent other);
}
