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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class GenImpulseThruster extends GenShipFuellessThruster
{
	@Override
	public String ID()
	{
		return "GenImpulseThruster";
	}

	public GenImpulseThruster()
	{
		super();
		setName("a generic impulse thruster");
		basePhyStats.setWeight(5000);
		setDisplayText("a generic impulse thruster sits here.");
		setDescription("");
		super.setMaxThrust(CMath.mul(SpaceObject.VELOCITY_LIGHT,50000.0));
	}
	
	private static final double maxSpeed =  CMath.div(SpaceObject.VELOCITY_LIGHT,4.0);
			
	private volatile SpaceObject ship = null;

	@Override
	public void setOwner(ItemPossessor owner)
	{
		super.setOwner(owner);
		ship = null;
	}
	
	public SpaceObject getShip()
	{
		if(ship == null)
			ship = CMLib.space().getSpaceObject(this, true);
		return ship;
	}
	
	@Override
	public double getMaxThrust()
	{
		final SpaceObject ship = getShip();
		if (ship instanceof SpaceShip)
		{
			final double phi = CMLib.space().getAngleDelta(ship.direction(), ((SpaceShip)ship).facing());
			final double cosPhi = Math.cos(phi);
			final double vParallel = ship.speed() * cosPhi;
			final double limitingV = Math.max(0.0, vParallel);
			final double effectiveMaxV = maxSpeed - limitingV;
			return CMath.mul(effectiveMaxV, ship.getMass());
		}
		return super.getMaxThrust();
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenImpulseThruster))
			return false;
		final String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
		{
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		}
		return true;
	}
}
