package com.planet_ink.coffee_mud.core.interfaces;
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
   Copyright 2003-2018 Bo Zimmerman

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
 * An interface for classes and objects which may affect mobs, rooms, items, and other Environmental types
 * By altering their stats and state objects using the layer system.
 * @author Bo Zimmerman
 *
 */
public interface StatsAffecting
{
	/**
	 * This method is called by the recoverPhyStats() method on other Environmental objects.  It is used
	 * to transform the Environmental basePhyStats() object into a finished phyStats() object,  both of
	 * which are objects implementing the PhyStats interface.  See those methods for more information.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats
	 * @see Environmental
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#basePhyStats()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#phyStats()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#recoverPhyStats()
	 * @param affected the host of the PhyStats object being affected
	 * @param affectableStats the particular PhyStats object being affected
	 */
	public void affectPhyStats(Physical affected, PhyStats affectableStats);
	/**
	 * This method is called by the recoverCharStats() method on other MOB objects.  It is used
	 * to transform the MOB baseCharStats() object into a finished charStats() object,  both of
	 * which are objects implementing the CharStats interface.  See those methods for more information.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#baseCharStats()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#charStats()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#recoverCharStats()
	 * @param affectedMob the host of the CharStats object being affected
	 * @param affectableStats the particular CharStats object being affected
	 */
	public void affectCharStats(MOB affectedMob, CharStats affectableStats);
	/**
	 * This method is called by the recoverCharState() method on other MOB objects.  It is used
	 * to transform the MOB baseCharState() object into a finished charState() object,  both of
	 * which are objects implementing the CharState interface.  See those methods for more information.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#baseState()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#curState()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#recoverMaxState()
	 * @param affectedMob the host of the CharState object being affected
	 * @param affectableMaxState the particular CharState object being affected
	 */
	public void affectCharState(MOB affectedMob, CharState affectableMaxState);
}
