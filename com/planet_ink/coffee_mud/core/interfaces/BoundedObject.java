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
   Copyright 2013-2024 Bo Zimmerman

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
 * The interface represents an object of mapped 3d space.
 * It can be represented as a cube, or a sphere, which can
 * be translated to a tube.
 *
 * @author Bo Zimmerman
 *
 */
public interface BoundedObject
{
	/**
	 * Returns the cubic representation of this bounded object.
	 *
	 * @return the cubic representation of this bounded object.
	 */
	public BoundedCube getCube();

	/**
	 * Returns the spherical representation of this bounded object.
	 *
	 * @return the spherical representation of this bounded object.
	 */
	public BoundedSphere getSphere();

	/**
	 * Represents the radius, or 1/2 a side length, of this object.
	 *
	 * @return the radius, or 1/2 a side length, of this object.
	 */
	public long radius();

	/**
	 * The center of this object in xyz coordinates.
	 *
	 * @return the center of this object in xyz coordinates
	 */
	public Coord3D center();
}
