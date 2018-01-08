package com.planet_ink.coffee_mud.Abilities.interfaces;
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

import java.util.*;

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
 * A trap is a kind of ability that behaves mostly like a property, in that
 * it is not typically invoked as a skill, although there are skills that
 * generate or make use of Trap abilities.
 */
public interface Trap extends Ability
{
	/** deprecated needle trap-type constant for deprecated traps.  Returned by internal trapType() methods. */
	public final static int TRAP_NEEDLE=0;
	/** deprecated pit or blade trap-type constant for deprecated traps.  Returned by internal trapType() methods. */
	public final static int TRAP_PIT_BLADE=1;
	/** deprecated gas trap-type constant for deprecated traps.  Returned by internal trapType() methods. */
	public final static int TRAP_GAS=2;
	/** deprecated spell trap-type constant for deprecated traps.  Returned by internal trapType() methods. */
	public final static int TRAP_SPELL=3;

	/**
	 * Returns whether this trap is a bomb, with delayed effect.
	 * @return true if its a bomb, false otherwise
	 */
	public boolean isABomb();

	/**
	 * When called, this will cause the bomb to begin its countdown
	 * to going off, which can differ from bomb-to-bomb.
	 */
	public void activateBomb();

	/**
	 * Whether this trap has been disabled, as by a thief
	 * @see Trap#disable()
	 * @return true if disabled, false otherwise
	 */
	public boolean disabled();

	/**
	 * Causes the trap to become disabled and inert.  Called
	 * usually by thief type skills.
	 * @see Trap#disabled()
	 */
	public void disable();

	/**
	 * This method causes this trap to take affect against the
	 * given target.  The type of effect can differ from trap
	 * to trap.
	 * @see Trap#sprung()
	 * @param target the target of the effect
	 */
	public void spring(MOB target);

	/**
	 * Returns whether this trap has already been sprung (and is
	 * not yet reset)
	 * @see Trap#spring(MOB)
	 * @return true if it has been sprung, false otherwise.
	 */
	public boolean sprung();

	/**
	 * Causes the trap to become reset and unsprung.  Called
	 * usually by thief type skills.
	 * @param mob the person resetting the trap
	 * @see Trap#canReSetTrap(MOB)
	 */
	public void resetTrap(MOB mob);

	/**
	 * Returns whether the given mob, at the given level, is allowed
	 * to re-set this trap.  This is where level restrictions are enforced,
	 * though no messages should be given.
	 * @see Trap#canSetTrapOn(MOB, Physical)
	 * @see Trap#resetTrap(MOB)
	 * @param mob the trap setter to check
	 * @return true if the given trapper is allowed to reset this trap, false otherwise
	 */
	public boolean canReSetTrap(MOB mob);
	
	/**
	 * Sets the number of ticks to wait after a trap has been sprung,
	 * before it will automatically reset for another victim.  A reset
	 * value of 0 means the trap is only useful once.
	 * @see Trap#getReset()
	 * @param reset the number of ticks between uses
	 */
	public void setReset(int reset);

	/**
	 * Return the number of ticks after a trap has been sprung before it
	 * will automatically reset itself for another victim.  A value of 0
	 * means the trap is only useful once.
	 * @see Trap#setReset(int)
	 * @return the number of ticks between resets
	 */
	public int getReset();

	/**
	 * Returns whether the given mob, at the given level, is allowed
	 * to set this trap.  This is where level restrictions are enforced,
	 * though no messages should be given.
	 * @see Trap#canSetTrapOn(MOB, Physical)
	 * @see Trap#setTrap(MOB, Physical, int, int, boolean)
	 * @param mob the trap setter to check
	 * @param asLevel the level of the trapper, compared to this traps internal level
	 * @return true if the given trapper is allowed to set this trap, false otherwise
	 */
	public boolean maySetTrap(MOB mob, int asLevel);

	/**
	 * Returns a sample set of the components used to make this trap.
	 * @return a vector of item objects
	 */
	public List<Item> getTrapComponents();

	/**
	 * Returns whether the given trapper is currently in a position to set this
	 * trap on the specified object.  Error messages should be delivered to the
	 * trapper if any internal checks aren't made.  Required materials or conditions
	 * are checked here
	 * @see Trap#maySetTrap(MOB, int)
	 * @see Trap#setTrap(MOB, Physical, int, int, boolean)
	 * @param mob the trapper
	 * @param P the object this trap will be set upon
	 * @return true if the trapper has everything he needs to proceed, false otherwise
	 */
	public boolean canSetTrapOn(MOB mob, Physical P);

	/**
	 * Completed the task of setting a trap on a given object. If any materials are
	 * required, this method will consume them.  If it is a bomb, it will still
	 * require activation, however.  This wil also set the reset time based on the
	 * given classlevel and qualifyingClassLevel of the trapper.
	 * @param mob the trapper
	 * @param P the object to set the trap on
	 * @param trapBonus any bonus to the traps effectiveness (0 is normal)
	 * @param qualifyingClassLevel the class-level at which the trapper qualified for this trap
	 * @param permanent true to make the trap reset after being triggered or disarmed, false to destroy
	 * @return the Trap object denoting the trap just added to the target object
	 */
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean permanent);

	/**
	 * A simple display string describing the conditions necesssary to get the canSetTrapOn
	 * method to return true.
	 * @see Trap#canSetTrapOn(MOB, Physical)
	 * @return a descriptive text for this trap.
	 */
	public String requiresToSet();

}
