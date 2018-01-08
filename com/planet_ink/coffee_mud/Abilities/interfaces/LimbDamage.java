package com.planet_ink.coffee_mud.Abilities.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
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
   Copyright 2014-2018 Bo Zimmerman

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
 * An LimbDamage is a kind of ability that denotes missing, broken, 
 * or damaged body parts.  Usually they are body parts from mobs, but 
 * they can technically be anything that can be damaged as parts from 
 * the hosted object.
 */
public interface LimbDamage extends Ability
{
	/**
	 * Returns a fully-qualified list of those parts of the given object which
	 * have not yet gotten damaged from it.  This would be a string set denoting the names
	 * of the specific parts not yet damaged.
	 * @return the set of the name of the remaining pieces.
	 */
	public List<String> unaffectedLimbSet();
	/**
	 * Performs the very dirty business of mangling the item of the given
	 * name on the given target.  An existing instanceof of the LimbDamage
	 * which will act as a property for the target must also be passed in.
	 * It will generate messages if necessary, toss the piece on the ground
	 * if that is appropriate, and do all thats needed.
	 * @param limbName the name of the limb to mangle, fully qualified.
	 * @return the item object representing the newly damaged part, if applicable.
	 */
	public Item damageLimb(String limbName);
	/**
	 * The opposite of the unaffectedLimbSet method, this method returns
	 * the list of the names of those parts which have been damaged.
	 * @return the list of the names of the parts that are damaged!
	 */
	public List<String> affectedLimbNameSet();
	/**
	 * Restores a missing or damaged part, denoted by the given string, and managed by the
	 * given LimbDamage property
	 * @param gone the name of the part to restore.
	 */
	public void restoreLimb(String gone);

}
