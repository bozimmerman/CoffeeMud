package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * something that is affected by, or affects
 * the environment around them.
 */
public interface Environmental extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public String ID();  // the class name of the object

	// the real name of the object
	public String name(); // the potentially modified name of the thing
	public String Name(); // the base name of the thing
	public void setName(String newName); // set the base name of the object

	// how the object appears at rest,
	public String displayText();
	public void setDisplayText(String newDisplayText);

	// Text displayed when this item is LOOKED at
	public String description();
	public void setDescription(String newDescription);

	public Environmental copyOf();
	public boolean isGeneric();

	/** For internal use by items. This text
	 * is saved for each room instance of an item, and
	 * may be used for behavior modification, description
	 * change, or anything else.
	 */
	public void setMiscText(String newMiscText);
	public String text();


	/** return a new instance of the object*/
	public Environmental newInstance();

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public EnvStats baseEnvStats();
	public EnvStats envStats();
	public void setBaseEnvStats(EnvStats newBaseEnvStats);
	public void recoverEnvStats();

	/** quick and easy access to the basic values in this object */
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(Environmental E);

	/** Manipulation of ability effect objects, which includes
	 * spells, traits, skills, etc.*/
	public void addEffect(Ability to);
	public void addNonUninvokableEffect(Ability to);
	public void delEffect(Ability to);
	public int numEffects();
	public Ability fetchEffect(int index);
	public Ability fetchEffect(String ID);

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to);
	public void delBehavior(Behavior to);
	public int numBehaviors();
	public Behavior fetchBehavior(int index);
	public Behavior fetchBehavior(String ID);

	/**
	 * Parameters for using in 3 dimensional space
	 */
	public int maxRange();
	public int minRange();
}
