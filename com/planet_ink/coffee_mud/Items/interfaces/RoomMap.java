package com.planet_ink.coffee_mud.Items.interfaces;
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
 * An item that, when read, shows you a graphical map
 * of an area.
 * @author Bo Zimmerman
 */
public interface RoomMap extends Item
{
	/**
	 * Gets the name(s) of the area(s) being mapped,
	 * semicolon delimited.
	 * @see RoomMap#setMapArea(String)
	 * @return the name(s) of the area(s) being mapped
	 */
	public String getMapArea();
	
	/**
	 * Sets the name(s) of the area(s) being mapped,
	 * semicolon delimited.
	 * @see RoomMap#getMapArea()
	 * @param mapName the name(s) of the area(s) being mapped
	 */
	public void setMapArea(String mapName);
	
	/**
	 * Causes the area(s) to be re-mapped.  Area name(s)
	 * must have been set before calling.
	 */
	public void doMapArea();
}
