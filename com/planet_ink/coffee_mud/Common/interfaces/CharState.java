package com.planet_ink.coffee_mud.Common.interfaces;
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
 * An object to access and change fields representing the most variable aspects of a MOB
 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
 * @author Bo Zimmerman
 *
 */
public interface CharState extends CMCommon, Modifiable
{
	/** stat constant for hit points */
	public final static int STAT_HITPOINTS=0;
	/** stat constant for mana */
	public final static int STAT_MANA=1;
	/** stat constant for movement */
	public final static int STAT_MOVE=2;
	/** stat constant for hunger */
	public final static int STAT_HUNGER=3;
	/** stat constant for thirst */
	public final static int STAT_THIRST=4;
	/** stat constant for thirst */
	public final static int STAT_TICKSHUNGRY=5;
	/** stat constant for thirst */
	public final static int STAT_TICKSTHIRSTY=6;
	/** stat constant for number of other stat constants */
	public final static int STAT_NUMSTATS=7;

	/** constant representing how many ticks between hunger/thirst messages*/
	public final static int ANNOYANCE_DEFAULT_TICKS=60;
	/** constant representing something*/
	public final static int REAL_TICK_ADJUST_FACTOR=5;
	/** constant representing how many ticks a MOB can  be thirsty before death */
	public final static int DEATH_THIRST_TICKS=(30*30)*6; // 6 hours
	/** constant representing how many ticks a MOB can  be hungry before death */
	public final static int DEATH_HUNGER_TICKS=(30*30)*12; // 12 hours
	/** constant for how many fatigue points are lost per tick of rest */
	public final static long REST_PER_SLEEP=CMProps.getTickMillis()*200;
	/** constant for how many fatigue points are lost per tick of rest */
	public final static long REST_PER_SIT=CMProps.getTickMillis()*10;
	/** constant for how many fatigue points are required to be considered fatigued */
	public final static long FATIGUED_MILLIS=CMProps.getTickMillis()*3000;
	/** constant for how many fatigue points are required to be considered exhausted */
	public final static long FATIGUED_EXHAUSTED_MILLIS=FATIGUED_MILLIS*10;

	/**
	 * Get primary combat stats as displayable code string
	 * @return primary combat stats as displayable code string
	 */
	public String getCombatStats();

   /**
	 * Get the number of fatigue points for the player
	 * @return number of fatigue points
	 */
	public long getFatigue();
	/**
	 * Set the number of fatigue points
	 * @param newVal number of fatigue points
	 */
	public void setFatigue(long newVal);
	/**
	 * Set the number of fatigue points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param max the highest amount to allow the fatigue number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjFatigue(long byThisMuch, CharState max);

	/**
	 * Get the number of hit points for the player
	 * @return number of hit points
	 */
	public int getHitPoints();
	/**
	 * Set the number of hit points
	 * @param newVal number of hit points
	 */
	public void setHitPoints(int newVal);

	/**
	 * Used to bump and/or read the number of ticks
	 * that this user has been consecutively thirsty
	 * @param bumpUp true to bump the number by one
	 * @return the ticks of thirstiness
	 */
	public int adjTicksThirsty(boolean bumpUp);

	/**
	 * Used to bump and/or read the number of ticks
	 * that this user has been consecutively hungry
	 * @param bumpUp true to bump the number by one
	 * @return the ticks of hungriness
	 */
	public int adjTicksHungry(boolean bumpUp);

	/**
	 * Set the number of hit points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param max the highest amount to allow the hit points number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjHitPoints(int byThisMuch, CharState max);

	/**
	 * Get the number of hunger points for the player
	 * @return number of hunger points
	 */
	public int getHunger();
	/**
	 * Set the number of hunger points
	 * @param newVal number of hunger points
	 */
	public void setHunger(int newVal);
	/**
	 * Set the number of hunger points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param maxHunger the highest amount to allow the hunger number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjHunger(int byThisMuch, int maxHunger);
	/**
	 * This method is used to recalculate the maximum thirhungerst for a mob, based
	 * on their weight and the default maximum hunger
	 * @param baseWeight the base weight of  the mob
	 * @return the new maximum hunger to set
	 */
	public int maxHunger(int baseWeight);

	/**
	 * Get the number of thirst points for the player
	 * @return number of thirst points
	 */
	public int getThirst();
	/**
	 * Set the number of thirst points
	 * @param newVal number of thirst points
	 */
	public void setThirst(int newVal);
	/**
	 * Set the number of thirst points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param maxThirst the highest amount to allow the thirst number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjThirst(int byThisMuch, int maxThirst);
	/**
	 * This method is used to recalculate the maximum thirst for a mob, based
	 * on their weight and the default maximum thirst
	 * @param baseWeight the base weight of  the mob
	 * @return the new maximum thirst to set
	 */
	public int maxThirst(int baseWeight);

	/**
	 * Get the number of mana points for the player
	 * @return number of mana points
	 */
	public int getMana();
	/**
	 * Set the number of mana points
	 * @param newVal number of mana points
	 */
	public void setMana(int newVal);
	/**
	 * Set the number of mana points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param max the highest amount to allow the mana number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjMana(int byThisMuch, CharState max);

	/**
	 * Get the number of movement points for the player
	 * @return number of movement points
	 */
	public int getMovement();
	/**
	 * Set the number of movement points
	 * @param newVal number of movement points
	 */
	public void setMovement(int newVal);
	/**
	 * Set the number of movement points, respecting boundaries. 0 is always lowest.
	 * @param byThisMuch a positive or negative change in value
	 * @param max the highest amount to allow the movement number to reach
	 * @return whether the highest or lowest boundary was reached
	 */
	public boolean adjMovement(int byThisMuch, CharState max);

	/**
	 * Sets all the values in this object to a single given value
	 * @param def the value to give to all
	 */
	public void setAllValues(int def);

	/**
	 * Resets all the stats in this object to their factory defaults.
	 */
	public void reset();

	/**
	 * Copies the internal data of this object into another of kind.
	 * @param intoState another CharState object.
	 */
	public void copyInto(CharState intoState);

	/**
	 * Whether this object instance is functionally identical to the object passed in.  Works by repeatedly
	 * calling getStat on both objects and comparing the values.
	 * @see #getStatCodes()
	 * @see #getStat(String)
	 * @param E the object to compare this one to
	 * @return whether this object is the same as the one passed in
	 */
	public boolean sameAs(CharState E);
}
