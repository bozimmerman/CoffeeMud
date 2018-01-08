package com.planet_ink.coffee_mud.Items.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/*
   Copyright 2001-2018 Bo Zimmerman

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
 * Represents a player or monster corpse, dead body, a stiff.
 * Most of the methods either allow interesting details about
 * the original spirit to be divined, or the player stuff to be
 * protected.
 * @author Bo Zimmerman
 *
 */
public interface DeadBody extends Container
{
	/**
	 * Returns the collection of character stats about the
	 * deceased mob.  This is stuff like strength and race.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see DeadBody#setCharStats(CharStats)
	 * @return the collection of character stats
	 */
	public CharStats charStats();
	
	/**
	 * Sets the collection of character stats about the
	 * deceased mob.  This is stuff like strength and race.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see DeadBody#charStats()
	 * @param newStats the collection of character stats
	 */
	public void setCharStats(CharStats newStats);
	
	/**
	 * Gets the deceased's name.  Not the display name of the corpse,
	 * but the original dudes plain old name.
	 * @see DeadBody#setMobName(String)
	 * @return the deceased's name
	 */
	public String getMobName();
	
	/**
	 * Sets the deceased's name.  Not the display name of the corpse,
	 * but the original dudes plain old name.
	 * @see DeadBody#getMobName()
	 * @param newName the deceased's name
	 */
	public void setMobName(String newName);
	
	/**
	 * Gets the hash that uniquely identifies the deceased mob object.
	 * @see DeadBody#setMobHash(int)
	 * @return hash that uniquely identifies the deceased mob object.
	 */
	public int getMobHash();

	/**
	 * Sets the hash that uniquely identifies the deceased mob object.
	 * @see DeadBody#setMobHash(int)
	 * @param newHash hash that uniquely identifies the deceased mob object.
	 */
	public void setMobHash(int newHash);
	
	/**
	 * Gets the deceased's description.  Not the display name of the corpse,
	 * but the original dudes description text.
	 * @see DeadBody#setMobDescription(String)
	 * @return the deceased's description
	 */
	public String getMobDescription();
	
	/**
	 * Sets the deceased's description.  Not the display name of the corpse,
	 * but the original dudes description text.
	 * @see DeadBody#getMobDescription()
	 * @param newDescription the deceased's description
	 */
	public void setMobDescription(String newDescription);
	
	/**
	 * Gets the name of the mob that killed the deceased mob whose corpse this is.
	 * @see DeadBody#setKillerName(String)
	 * @return the name of the mob that killed the deceased mob whose corpse this is.
	 */
	public String getKillerName();
	
	/**
	 * Sets the name of the mob that killed the deceased mob whose corpse this is.
	 * @see DeadBody#setKillerName(String)
	 * @param newName the name of the mob that killed the deceased mob whose corpse this is.
	 */
	public void setKillerName(String newName);

	/**
	 * Gets whether the mob who killed the deceased mob whose corpse this is was a player
	 * or a mob.
	 * @see DeadBody#setIsKillerPlayer(boolean)
	 * @return true if the killer was a player, false otherwise
	 */
	public boolean isKillerPlayer();

	/**
	 * Sets whether the mob who killed the deceased mob whose corpse this is was a player
	 * or a mob.
	 * @see DeadBody#isKillerPlayer()
	 * @param trueFalse true if the killer was a player, false otherwise
	 */
	public void setIsKillerPlayer(boolean trueFalse);
	
	/**
	 * Gets the last message seen by the deceased mob whose corpse this is.  Usually this
	 * would be the killing blow.
	 * @see DeadBody#setLastMessage(String)
	 * @return the last message seen by the deceased mob whose corpse this is
	 */
	public String getLastMessage();
	
	/**
	 * Sets the last message seen by the deceased mob whose corpse this is.  Usually this
	 * would be the killing blow.
	 * @see DeadBody#getLastMessage()
	 * @param lastMsg the last message seen by the deceased mob whose corpse this is
	 */
	public void setLastMessage(String lastMsg);
	
	/**
	 * Gets the weapon wielded by the killer at the time of death of the deceased mob.
	 * @see DeadBody#setKillerTool(Environmental) 
	 * @return the weapon wielded by the killer at the time of death of the deceased mob.
	 */
	public Environmental getKillerTool();
	
	/**
	 * Sets the weapon wielded by the killer at the time of death of the deceased mob.
	 * @see DeadBody#getKillerTool() 
	 * @param tool the weapon wielded by the killer at the time of death of the deceased mob.
	 */
	public void setKillerTool(Environmental tool);
	
	/**
	 * Gets whether this corpse is automatically destroyed by the system after it has been
	 * looted.
	 * @see DeadBody#setIsDestroyAfterLooting(boolean)
	 * @return true to destroy after looting, false otherwise
	 */
	public boolean isDestroyedAfterLooting();
	
	/**
	 * Sets whether this corpse is automatically destroyed by the system after it has been
	 * looted.
	 * @see DeadBody#isDestroyedAfterLooting()
	 * @param truefalse true to destroy after looting, false otherwise
	 */
	public void setIsDestroyAfterLooting(boolean truefalse);
	
	/**
	 * Gets whether the deceased mob whose corpse this is was a player.
	 * @see DeadBody#setIsPlayerCorpse(boolean)
	 * @return true if the deceased was a player, false if a mob
	 */
	public boolean isPlayerCorpse();
	
	/**
	 * Sets whether the deceased mob whose corpse this is was a player.
	 * @see DeadBody#isPlayerCorpse()
	 * @param truefalse true if the deceased was a player, false if a mob
	 */
	public void setIsPlayerCorpse(boolean truefalse);
	
	/**
	 * Gets whether the mob, whose corpse this is, had their PlayerKill flag
	 * on at the time of death, probably denoting a duel of some sort.
	 * @see DeadBody#setMobPKFlag(boolean)
	 * @return true if the mob had their PK flag on, false otherwise
	 */
	public boolean getMobPKFlag();
	
	/**
	 * Sets whether the mob, whose corpse this is, had their PlayerKill flag
	 * on at the time of death, probably denoting a duel of some sort.
	 * @see DeadBody#getMobPKFlag()
	 * @param truefalse true if the mob had their PK flag on, false otherwise
	 */
	public void setMobPKFlag(boolean truefalse);
	
	/**
	 * Gets the real world time, in milliseconds, that the death of this
	 * corpse occurred.
	 * @see DeadBody#setTimeOfDeath(long)
	 * @return the time, in milliseconds, of death
	 */
	public long getTimeOfDeath();
	
	/**
	 * Sets the real world time, in milliseconds, that the death of this
	 * corpse occurred.
	 * @see DeadBody#getTimeOfDeath()
	 * @param time the time, in milliseconds, of death
	 */
	public void setTimeOfDeath(long time);
	
	/**
	 * Sets the mob object belonging to the deceased.
	 * @see DeadBody#getSavedMOB()
	 * @param mob the mob object belonging to the deceased.
	 * @param preserve TODO
	 */
	public void setSavedMOB(MOB mob, boolean preserve);
	
	/**
	 * Gets the mob object belonging to the deceased.
	 * @see DeadBody#setSavedMOB(MOB, boolean)
	 * @return the mob object belonging to the deceased.
	 */
	public MOB getSavedMOB();
}

