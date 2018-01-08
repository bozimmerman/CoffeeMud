package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/*
   Copyright 2010-2018 Bo Zimmerman

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
*
* Something that exists in the physical world and can be affected by the
* world
* @see com.planet_ink.coffee_mud.core.interfaces.Environmental
* @author Bo Zimmerman
*
*/
public interface Physical extends Environmental, Affectable
{
	/**
	 * Returns the displayText, but as seen by the given viewer.
	 * Can differ from displayText() without being saved to the DB.
	 * Display Texts are normally the way something appears in a
	 * room, or is the roomTitle of rooms.
	 * @see Environmental#displayText()
	 * @param viewerMob the mob viewing the physical thing
	 * @return the displayText as seen by the viewer
	 */
	public String displayText(MOB viewerMob);

	/**
	 * Returns the name, but as seen by the given viewer.
	 * Can differ from name() without being saved to the DB.
	 * @see Environmental#name()
	 * @param viewerMob the mob viewing the physical thing
	 * @return the name as seen by the viewer
	 */
	public String name(MOB viewerMob);
	/**
	 * Returns the description, but as seen by the given viewer.
	 * Can differ from description() without being saved to the DB.
	 * Descriptions are normally the way something appears when
	 * looked at, or is the long description of rooms.
	 * @see Environmental#description()
	 * @param viewerMob the mob viewing the physical thing
	 * @return the description as seen by the viewer
	 */
	public String description(MOB viewerMob);
}
