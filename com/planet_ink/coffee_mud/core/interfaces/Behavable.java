package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine;

/*
   Copyright 2006-2018 Bo Zimmerman

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
* Something that can behave -- means almost everything!
* @author Bo Zimmerman
*
*/
public interface Behavable
{
	/**
	 * Add a new behavior to this object.  After calling this method,
	 * recoverPhyStats() should be called next in case this behavior object modifies the stats.
	 * A Behavior with a given ID() can only be added once per object.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#recoverPhyStats()
	 * @param to The behavior object to add.
	 */
	public void addBehavior(Behavior to);

	/**
	 * Delete a behavior from this object.  After calling this method,
	 * recoverPhyStats() should be called next in case this behavior object modified the stats.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.core.interfaces.Affectable#recoverPhyStats()
	 * @param to The behavior object to remove.
	 */
	public void delBehavior(Behavior to);

	/**
	 * The number of behaviors this object has.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @return the number of behaviors
	 */
	public int numBehaviors();

	/**
	 * Returns a behavior object on this object. May return null even if the index
	 * is correct to mark a race condition.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see com.planet_ink.coffee_mud.core.interfaces.PhysicalAgent#numBehaviors()
	 * @param index which object to return
	 * @return the behavior object
	 */
	public Behavior fetchBehavior(int index);

	/**
	 * Returns a behavior object listed on this object. The object will
	 * be the one with the same ID() string as passed in.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @see CMObject#ID()
	 * @param ID the Behavior ID to look for
	 * @return the behavior object
	 */
	public Behavior fetchBehavior(String ID);

	/**
	 * Returns an enumerator of all the behaviors on this object.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
	 * @return an enumerator of all the behaviors on this object.
	 */
	public Enumeration<Behavior> behaviors();

	/**
	 * Applies the given code to each behavior on this object
	 * @param applier code to execute against each object
	 */
	public void eachBehavior(final EachApplicable<Behavior> applier);

	/**
	 * Add a new runnable script to this object.  Objects which are
	 * not mobs or areas will gain a temporary tick service for
	 * this script.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine
	 * @param s the scripting engine, fully populated, to add
	 */
	public void addScript(ScriptingEngine s);

	/**
	 * Removes all behaviors from this object.
	 */
	public void delAllBehaviors();

	/**
	 * Remove a running script from this object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine
	 * @param s the specific scripting engine to remove
	 */
	public void delScript(ScriptingEngine s);

	/**
	 * Removes all executing scripts from this object.
	 */
	public void delAllScripts();

	/**
	 * Return the number of scripts running on this object
	 * @return number of scripts
	 */
	public int numScripts();

	/**
	 * Retrieve one of the enumerated scripts running on this
	 * object
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine
	 * @param x which script to return
	 * @return the scripting engine
	 */
	public ScriptingEngine fetchScript(int x);

	/**
	 * Returns an enumerator of all the scripts on this object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine
	 * @return an enumerator of all the scripts on this object.
	 */
	public Enumeration<ScriptingEngine> scripts();

	/**
	 * Applies the given code to each scripting engine on this object
	 * @param applier code to execute against each object
	 */
	public void eachScript(final EachApplicable<ScriptingEngine> applier);
}
