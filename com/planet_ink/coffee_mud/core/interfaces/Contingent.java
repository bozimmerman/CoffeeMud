package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
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
 * One step above a basic CMObject is an object that is contingent on its
 * environment.  It can be created and destroyed, savable, or not.
 * @see com.planet_ink.coffee_mud.core.CMClass
 * @author Bo Zimmerman
 *
 */
public interface Contingent extends CMObject
{
	/**
	 * Utterly and permanently destroy this object, not only removing it from the map, but
	 * causing this object to be collected as garbage by Java.  Containers, rooms. and mobs who have
	 * their destroy() method called will also call the destroy() methods on all items and other
	 * objects listed as content, recursively.
	 */
	public void destroy();
	/**
	 * Whether, if this object is in a room, whether it is appropriate to save this object to
	 * the database as a permanent feature of its container.  It always returns true except
	 * under unique circumstances.
	 * @return true, usually.
	 */
	public boolean isSavable();
	/**
	 * Whether the destroy() method has been previousy called on this object.
	 * @return whether the object is destroy()ed.
	 */
	public boolean amDestroyed();
	/**
	 * Sets whether this object can be saved as a permanent aspect of
	 * its host.
	 * @see Contingent#isSavable()
	 * @param truefalse whether this behavior can be saved as part of its host.
	 */
	public void setSavable(boolean truefalse);
}
