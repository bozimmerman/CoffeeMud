package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip.ShipFlag;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2018-2024 Bo Zimmerman

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
public class StdShipGravityGen extends StdElecCompItem
{
	@Override
	public String ID()
	{
		return "StdShipGravityGen";
	}

	public StdShipGravityGen()
	{
		super();
		setName("a gravity generator");
		setDisplayText("a gravity generator sits here.");
		setDescription("");
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_ENVIRO_CONTROL;
	}

	private volatile Reference<SpaceShip> myShip 	   = null;

	@Override
	public void setOwner(final ItemPossessor container)
	{
		super.setOwner(container);
		myShip = null;
	}

	protected synchronized SpaceShip getMyShip()
	{
		if(myShip == null)
		{
			final Area area = CMLib.map().areaLocation(this);
			if(area instanceof SpaceShip)
				myShip = new WeakReference<SpaceShip>((SpaceShip)area);
			else
				myShip = new WeakReference<SpaceShip>(null);
		}
		return myShip.get();
	}


	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_POWERCURRENT:
			{
				if(activated())
				{
					final SpaceShip ship=getMyShip();
					if((!ship.getShipFlag(ShipFlag.ARTI_GRAV))
					&&(!ship.getShipFlag(ShipFlag.IN_THE_AIR)))
					{
						final double efficiency = this.getFinalManufacturer().getEfficiencyPct();
						final double reliability = this.getFinalManufacturer().getReliabilityPct();
						double wearAndTear = 1.0;
						if(this.subjectToWearAndTear() && this.usesRemaining()<100)
							wearAndTear = Math.min(1.0, reliability * CMath.div(this.usesRemaining(), 100.0));
						final double amountNeeded = ship.getArea().numberOfProperIDedRooms();
						final double powerUsed = amountNeeded * efficiency; // eff is from 0.5 (great) to 2.0 (terrible)
						double powerFactor = 1.0;
						if(powerUsed > this.powerRemaining())
						{
							//TODO: auto-draw from auxillary power? batteries?
							powerFactor = CMath.div(this.powerRemaining(), powerUsed);
							this.setPowerRemaining(0);
						}
						else
							this.setPowerRemaining(Math.round(this.powerRemaining() - powerUsed));
						if(CMLib.dice().rollPercentage()<CMath.mul(150.0, wearAndTear * powerFactor))
							ship.setShipFlag(ShipFlag.ARTI_GRAV, true);
					}
				}
				break;
			}
			}
		}
		return true;
	}

	protected static void sendComputerMessage(final Technical me, final String circuitKey, final MOB mob, final Item controlI, final String code)
	{
		for(final Iterator<Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
		{
			final Computer C=c.next();
			if((controlI==null)||(C!=controlI.owner()))
			{
				final CMMsg msg2=CMClass.getMsg(mob, C, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(C.okMessage(mob, msg2))
					C.executeMsg(mob, msg2);
			}
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdShipDampener))
			return false;
		return super.sameAs(E);
	}
}
