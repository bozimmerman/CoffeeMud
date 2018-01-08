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

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * TriggeredAffect is an ability interface to denote those properties
 * that are typically non-removable inherent attributes of the things they affect.
 */
public interface TriggeredAffect extends Ability
{
	/** denotes a property whose affects are triggered always */
	public final static int TRIGGER_ALWAYS=1;
	/** denotes a property whose affects are triggered by entering the thing*/
	public final static int TRIGGER_ENTER=2;
	/** denotes a property whose affects are triggered by being hit by the thing*/
	public final static int TRIGGER_BEING_HIT=4;
	/** denotes a property whose affects are triggered by wearing/wielding the thing*/
	public final static int TRIGGER_WEAR_WIELD=8;
	/** denotes a property whose affects are triggered by getting the thing */
	public final static int TRIGGER_GET=16;
	/** denotes a property whose affects are triggered by using/eating/drinking the thing*/
	public final static int TRIGGER_USE=32;
	/** denotes a property whose affects are triggered by putting the thing somewhere*/
	public final static int TRIGGER_PUT=64;
	/** denotes a property whose affects are triggered by mounting the thing*/
	public final static int TRIGGER_MOUNT=128;
	/** denotes a property whose affects are triggered by putting something in or dropping the thing*/
	public final static int TRIGGER_DROP_PUTIN=64;
	/** denotes a property whose affects are triggered by hitting somethign with the thing*/
	public final static int TRIGGER_HITTING_WITH=128;

	/**
	 * This method returns a mask of TRIGGER_* constants denoting what triggers the properties
	 * @see TriggeredAffect#TRIGGER_ALWAYS
	 *
	 * @return  a mask of TRIGGER_* constants denoting what triggers the properties
	 */
	public int triggerMask();
}
