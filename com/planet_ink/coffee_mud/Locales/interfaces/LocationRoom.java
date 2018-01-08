package com.planet_ink.coffee_mud.Locales.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
 * This is a type of room that can be located as a coordinate on a
 * planet's surface.
 * @author Bo Zimmerman
 *
 */
public interface LocationRoom extends Room
{
	/**
	 * Coordinates of the place -- must be on its planet surface.
	 * Completely derived from the location of its planets code,
	 * the radius of the planet, and the direction from core.
	 * @see LocationRoom#getDirectionFromCore()
	 * @return Coordinates of the place
	 */
	public long[] coordinates();

	/**
	 * Returns the direction from the core of the planet to the
	 * location of this place on its surface.  Distance from core
	 * is always the radius of the planet.
	 * @return direction to this place from planets core.
	 */
	public double[] getDirectionFromCore();

	/**
	 * Sets the direction from the core of the planet to the
	 * location of this place on its surface.  Distance from core
	 * is always the radius of the planet.
	 * @param dir direction to this place from planets core.
	 */
	public void setDirectionFromCore(double[] dir);
}
