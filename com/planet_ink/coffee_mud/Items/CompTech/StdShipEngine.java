package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Items.CompTech.StdShipThruster.StdAccelerator;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.ShipEngine.ShipAccelerator;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2012-2025 Bo Zimmerman

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
public class StdShipEngine extends StdCompGenerator implements ShipEngine
{
	@Override
	public String ID()
	{
		return "StdShipEngine";
	}

	protected double			maxThrust		= 8900000;
	protected double			minThrust		= 0;
	protected double			thrust			= 0;
	protected double			specificImpulse	= 0.33;
	protected boolean			constantThrust	= true;
	protected final long[]		lastThrustMs	= new long[] { 0 };
	protected ShipAccelerator	accelerator		= new StdAccelerator(this);

	protected ShipDirectional.ShipDir[] ports = ShipDirectional.ShipDir.values();

	public StdShipEngine()
	{
		super();
		setName("a ships engine");
		basePhyStats.setWeight(50000);
		setDisplayText("a ships engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdShipEngine))
			return false;
		return super.sameAs(E);
	}

	@Override
	public double getSpecificImpulse()
	{
		return specificImpulse;
	}

	@Override
	public void setSpecificImpulse(final double amt)
	{
		specificImpulse = amt;
	}

	@Override
	public double getMaxThrust()
	{
		return maxThrust;
	}

	@Override
	public void setMaxThrust(final double max)
	{
		maxThrust = max;
	}

	@Override
	public double getThrust()
	{
		return thrust;
	}

	@Override
	public void setThrust(final double current)
	{
		thrust = current;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_ENGINE;
	}

	@Override
	protected boolean willConsumeFuelIdle()
	{
		return getThrust() > 0;
	}

	@Override
	protected double getComputedEfficiency()
	{
		return super.getComputedEfficiency() * this.getInstalledFactor();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(accelerator.executeActivateCommand(msg, circuitKey))
					activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(activated())
					accelerator.executeDeactivateCommand(msg.source());
				setThrust(0);
				activate(false);
				break;
			case CMMsg.TYP_POWERCURRENT:
			{
				if(activated())
					accelerator.executeOngoingThrustCommand(msg.source(), circuitKey);
				break;
			}
			}
		}
	}

	@Override
	public double getMinThrust()
	{
		return minThrust;
	}

	@Override
	public void setMinThrust(final double min)
	{
		this.minThrust = min;
	}

	@Override
	public boolean isReactionEngine()
	{
		return constantThrust;
	}

	@Override
	public void setReactionEngine(final boolean isConstant)
	{
		constantThrust = isConstant;
	}

	/**
	 * Gets set of available thrust ports on this engine.
	 * @see ShipEngine#setAvailPorts(ShipDirectional.ShipDir[])
	 * @return the set of available thrust ports.
	 */
	@Override
	public ShipDirectional.ShipDir[] getAvailPorts()
	{
		return ports;
	}

	/**
	 * Sets set of available thrust ports on this engine.
	 * @see ShipEngine#getAvailPorts()
	 * @param ports the set of available thrust ports.
	 */
	@Override
	public void setAvailPorts(final ShipDirectional.ShipDir[] ports)
	{
		this.ports = ports;
	}
}
