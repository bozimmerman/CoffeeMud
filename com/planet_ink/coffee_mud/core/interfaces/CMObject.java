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
   Copyright 2005-2018 Bo Zimmerman

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
 * The general base interface which is implemented by every class
 * which the CoffeeMud ClassLoader (CMClass) handles.
 * @see com.planet_ink.coffee_mud.core.CMClass
 * @author Bo Zimmerman
 *
 */
public interface CMObject extends Cloneable, Comparable<CMObject>
{
	/**
	 * The CoffeeMud Java Class ID shared by all instances of
	 * this object.  Unlike the Java Class name, this method
	 * does not include package information.  However, it must
	 * return a String value unique to its class category in
	 * the ClassLoader.  Class categories include Libraries, Common,
	 * Areas, Abilities, Behaviors, CharClasses, Commands, Exits
	 * Locales, MOBS, Races, WebMacros, Basic Items, Armor,
	 * Weapons, ClanItems, Tech.  The name is typically identical
	 * to the class name.
	 * @return the name of this class
	 */
	public String ID();
	/**
	 * The displayable name of this object.  May be modified by phyStats() object. Is
	 * derived from the Name().
	 * @see  Environmental#Name()
	 * @return the modified final name of this object on the map.
	 */
	public String name();
	/**
	 * Returns a new instance of this class.
	 * @return a new instance of this class
	 */
	public CMObject newInstance();
	/**
	 * Similar to Cloneable.clone(), but does its best to make sure that
	 * any internal objects to this class are also copyOfed.
	 * @return a clone of this object
	 */
	public CMObject copyOf();

	/**
	 * Called ONCE after all objects are loaded, but before the map is read in
	 * during initialization.
	 */
	public void initializeClass();
	
}
