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
   Copyright 2016-2018 Bo Zimmerman

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
 * An interface for a particular kind of container that is invisible
 * when closed, holds particular kinds of electrical gear, and allows
 * some level of manipulation of the items inside.  It also may manage
 * the power needs of all containing items, as well as a uniform way
 * of activation.
 * @author Bo Zimmerman
 *
 */
public interface ElecPanel extends Electronics
{
	/**
	 * A list of TechType objects denoting what the
	 * valid types of panels there are.  These determine
	 * that kinds of items the panel can hold.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
	 */
	public static final TechType[] PANELTYPES=
	{
		TechType.ANY,
		TechType.SHIP_WEAPON,
		TechType.SHIP_SHIELD,
		TechType.SHIP_ENGINE,
		TechType.SHIP_SENSOR,
		TechType.SHIP_POWER,
		TechType.SHIP_COMPUTER,
		TechType.SHIP_SOFTWARE,
		TechType.SHIP_ENVIRO_CONTROL,
		TechType.SHIP_GENERATOR,
		TechType.SHIP_DAMPENER,
		TechType.SHIP_TRACTOR,
	};
	
	/**
	 * Gets the type of panel this is, which shows what
	 * sorts of items can be "installed into it.  This method is
	 * a sort of companion to {@link Container#containTypes()}
	 * @see ElecPanel#setPanelType(com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
	 * @return the type of panel this is
	 */
	public TechType panelType();
	
	/**
	 * Sets the type of panel this is, which shows what
	 * sorts of items can be "installed into it.  This method is
	 * a sort of companion to {@link Container#setContainTypes(long)}
	 * @see ElecPanel#setPanelType(com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
	 * @param type the type of panel this is
	 */
	public void setPanelType(TechType type);
}
