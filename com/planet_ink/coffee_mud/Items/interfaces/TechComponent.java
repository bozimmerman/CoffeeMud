package com.planet_ink.coffee_mud.Items.interfaces;

import java.util.Set;

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
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
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
 * A TechComponent is a type of electronics item that must be installed into
 * a panel that can accept it.  Typically for space ships.
 * 
 * @author Bo Zimmerman
 *
 */
public interface TechComponent extends Electronics
{
	/**
	 * The installed factor is how well the equipment was installed. 0-1.0
	 * @see TechComponent#setInstalledFactor(float)
	 * @return installed factor is how well the equipment was installed. 0-1.0
	 */
	public float getInstalledFactor();

	/**
	 * The installed factor is how well the equipment was installed. 0-1.0
	 * @see TechComponent#getInstalledFactor()
	 * @param pct installed factor is how well the equipment was installed. 0-1.0
	 */
	public void setInstalledFactor(float pct);

	/**
	 * Returns whether this item is installed properly, which is 
	 * according to internal rules of each component type.
	 * @return true if its installed, false if its just there.
	 */
	public boolean isInstalled();
	
	/**
	 * Sets the amount of the capacity of this component that can gain
	 * power every time a power current is received.
	 * @see TechComponent#getRechargeRate()
	 * 
	 * @param pctCapPer the amount of capacity per tick
	 */
	public void setRechargeRate(float pctCapPer);

	/**
	 * Gets the amount of the capacity of this component that can gain
	 * power every time a power current is received.
	 * @see TechComponent#setRechargeRate(float)
	 * 
	 * @return amtPer the amount of capacity per tick
	 */
	public float getRechargeRate();
	
	/**
	 * The ThrustPort enum is for the different thrust ports, denoting
	 * the port, by its direction location.
	 * @author Bo Zimmerman
	 */
	public enum ShipDir
	{
		AFT,
		PORT,
		VENTRAL,
		DORSEL,
		STARBOARD,
		FORWARD
	}
}
