package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.EnvStats;

/*
Copyright 2000-2010 Bo Zimmerman

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
* Something that can be affected, and has environmental stats that
* can be affected as well.
* @author Bo Zimmerman
*
*/
public interface Affectable {

	/**
     * Object containing a set of base, unmodified, mostly numeric fields.  The values on the fields
     * in this object will be as they were set by the builder. This object is used as a basis for
     * the recoverEnvStats() method.  See the EnvStats interface for information on the fields herein.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#envStats()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     * @return a set of state fields
     */
	public EnvStats baseEnvStats();
    /**
     * Re-sets the object containing a set of base, unmodified, mostly numeric fields.  The values on the fields
     * in this object will be as they were set by the builder. This object is used as a basis for
     * the recoverEnvStats() method.  See the EnvStats interface for information on the fields herein. This
     * method is rarely called -- the fields therein are usually set using setter methods from the EnvStats
     * interface on the object itself.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#envStats()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     * @param newBaseEnvStats a set of state fields
     */
    public void setBaseEnvStats(EnvStats newBaseEnvStats);
    /**
     * Object containing a set of current, modified, usable, mostly numeric fields.  This object is based on
     * the object from baseEnvStats() and then updated and modified by the recoverEnvStats() method.
     * See the EnvStats interface for information on the fields herein.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#baseEnvStats()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     * @return the current set of state fields
     */
	public EnvStats envStats();
    /**
     * This method copies the baseEnvStats() object into the envStats() object, then makes repeated calls to
     * all surrounding objects  with affectEnvStats(Environmental,EnvStats) method.   Surrounding  objects
     * include the room where the object is located, the Ability objects in the Effects list, the Behaviors
     * in the behaviors list, and race/charclass/area if applicable.  Those methods will then make all necessary
     * adjustments to the values in the new envStats() object.  When it returns, envStats() will have a totally
     * updated object.  This method must be called in code whenever the object is placed on the map, or when
     * anything changes in its environment, such as location, effects, or other states.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#baseEnvStats()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#envStats()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#addEffect(Ability)
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#addBehavior(Behavior)
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     */
	public void recoverEnvStats();

	/**
     * Add a new effect to this object, whether permanent or temporary.  After calling this method,
     * recoverEnvStats() should be called next in case this ability object modifies the stats.
     * An Ability with a given ID() can only be added once per object.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @param to The ability object to add as an effect.
     */
	public void addEffect(Ability to);
    /**
     * Same as addEffect(Ability), but will set the Ability object as never being able to be uninvoked.
     * recoverEnvStats() method  should be called next.
     * An Ability with a given ID() can only be added once per object.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @param to The ability object to add as an effect.
     */
	public void addNonUninvokableEffect(Ability to);
    /**
     * Delete an effect from this object, whether permanent or temporary.  After calling this method,
     * recoverEnvStats() should be called next in case this ability object modified the stats.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#recoverEnvStats()
     * @param to The ability object to remove as an effect on this object
     */
	public void delEffect(Ability to);
    /**
     * Returns the number of ability objects listed as effects on this object.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @return the number of effects this object has
     */
	public int numEffects();
    /**
     * Returns an ability object listed as an effect on this object. May return null even if the index
     * is correct to mark a race condition.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#numEffects()
     * @param index which object to return
     * @return the ability object effecting this object
     */
	public Ability fetchEffect(int index);
    /**
     * Returns an ability object listed as an effect on this object. The object will
     * be the one with the same ID() string as passed in.
     * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
     * @see CMObject#ID()
     * @return the ability object effecting this object
     */
	public Ability fetchEffect(String ID);

}
