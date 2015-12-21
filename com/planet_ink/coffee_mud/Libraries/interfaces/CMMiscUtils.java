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
import com.planet_ink.coffee_mud.Libraries.CMMap;
import com.planet_ink.coffee_mud.Libraries.CoffeeUtensils;
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
 * The MiscUtils (Miscellaneous Utilities) library should not exist.
 * Its purpose is to expose universal functionality that doesn't
 * seem to fit anywhere else.  Usually, however, it does fit
 * elsewhere just fine, but the elsewhere felt too crowded already
 * to receive it.  I'm sure this library will always exist, but the
 * interface is definitely subject to change.
 * 
 * @author Bo Zimmerman
 */
public interface CMMiscUtils extends CMLibrary
{
	/**
	 * Checks the given player mob for the format of their prompt in
	 * their playerstats and generates a fully formed prompt, complete
	 * with all variables filled in.
	 * @param mob the mob to build a prompt for
	 * @return the fully filled in customized prompt string
	 */
	public String builtPrompt(MOB mob);

	public String getFormattedDate(Environmental E);
	public double memoryUse ( Environmental E, int number );
	public String niceCommaList(List<?> V, boolean andTOrF);
	public long[][] compileConditionalRange(List<String> condV, int numDigits, final int startOfRange, final int endOfRange);

	public void outfit(MOB mob, List<Item> items);
	public Language getLanguageSpoken(Physical P);
	public boolean reachableItem(MOB mob, Environmental E);
	public void extinguish(MOB source, Physical target, boolean mundane);
	public boolean armorCheck(MOB mob, int allowedArmorLevel);
	public boolean armorCheck(MOB mob, Item I, int allowedArmorLevel);
	public void recursiveDropMOB(MOB mob, Room room, Item thisContainer, boolean bodyFlag);
	public List<Item> deepCopyOf(Item theContainer);
	public void confirmWearability(MOB mob);
	public int processVariableEquipment(MOB mob);

	public Trap makeADeprecatedTrap(Physical unlockThis);
	public void setTrapped(Physical myThang, boolean isTrapped);
	public void setTrapped(Physical myThang, Trap theTrap, boolean isTrapped);
	public Trap fetchMyTrap(Physical myThang);

	public MOB getMobPossessingAnother(MOB mob);
	public void roomAffectFully(CMMsg msg, Room room, int dirCode);
	public List<DeadBody> getDeadBodies(Environmental container);
	public boolean resurrect(MOB tellMob, Room corpseRoom, DeadBody body, int XPLevel);

	public Item isRuinedLoot(MOB mob, Item I);

	public void swapRaces(Race newR, Race oldR);
	public void reloadCharClasses(CharClass oldC);

	public boolean canBePlayerDestroyed(final MOB mob, final Item I, final boolean ignoreBodies);
	
	public boolean disInvokeEffects(Environmental E);
	public int disenchantItem(Item target);
}
