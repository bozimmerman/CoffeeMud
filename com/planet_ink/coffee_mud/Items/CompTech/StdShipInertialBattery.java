package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class StdShipInertialBattery extends StdElecCompItem
{
	@Override
	public String ID()
	{
		return "StdShipInertialBattery";
	}

	public StdShipInertialBattery()
	{
		super();
		setName("an inertial battery");
		setDisplayText("an inertial battery sits here.");
		setDescription("");
		super.setPowerCapacity(SpaceObject.VELOCITY_LIGHT/2);
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_INERTIAL;
	}

	private volatile Reference<SpaceShip> myShip 	   = null;

	@Override
	public void setOwner(final ItemPossessor container)
	{
		if((owner instanceof Room)
		&&(((Room)owner).getArea() instanceof SpaceShip))
			((SpaceShip)((Room)owner).getArea()).unregisterListener(TechCommand.ACCELERATION, this);
		super.setOwner(container);
		myShip = null;
		if((container instanceof Room)
		&&(((Room)container).getArea() instanceof SpaceShip))
			((SpaceShip)((Room)container).getArea()).registerListener(TechCommand.ACCELERATION, this);
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
		if(msg.target() == getMyShip())
		{
			final SpaceShip ship = getMyShip();
			if((ship != null)
			&&(msg.targetMinor()==CMMsg.TYP_ACTIVATE)
			&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
			&&(msg.targetMessage()!=null)
			&&(super.activated())
			&&(super.isInstalled())
			&&(ship.speed()>0))
			{
				final TechCommand command=TechCommand.findCommand(msg.targetMessage());
				if(command==null)
					return true;
				final Object[] parms=command.confirmAndTranslate(msg.targetMessage());
				if(parms==null)
					return true;
				if(command!=Technical.TechCommand.ACCELERATION)
					return true;
				final ShipDirectional.ShipDir dir=(ShipDirectional.ShipDir)parms[0];
				final double amount=((Double)parms[1]).doubleValue();
				final boolean isConst = ((Boolean)parms[2]).booleanValue();
				if((dir != ShipDirectional.ShipDir.AFT)
				||(!isConst)
				||(amount<=0))
					return true;
				final double delta = CMLib.space().getAngleDelta(ship.facing(), ship.direction());
				if(delta < 0.000001)
					return true;
				//final double
				final double speedAbsorbAbility = CMath.mul(this.powerRemaining(),
						super.getComputedEfficiency() *
						this.getFinalManufacturer().getEfficiencyPct());
				double removedSpeed=ship.speed();
				final double retainingDelta = 1.0-CMath.div(delta, (Math.PI/2.0));
				if((retainingDelta > 0.0)&&(retainingDelta<1.0)) // retain SOME speed!
					removedSpeed = CMath.mul(ship.speed(), retainingDelta);

				final double[] newDirections = Arrays.copyOf(ship.facing(), 2);
				double efficiency = this.getFinalManufacturer().getEfficiencyPct();
				if(removedSpeed <= speedAbsorbAbility)
					this.setPowerRemaining(Math.round(speedAbsorbAbility-removedSpeed));
				else // failure case
				{
					efficiency *= CMath.div(removedSpeed, ship.speed());
					this.setPowerRemaining(0);
				}
				ship.setSpeed(ship.speed()-removedSpeed);
				if(this.powerRemaining()<0)
					this.setPowerRemaining(0);
				if(efficiency < 1.0)
				{
					final double[] deltas = CMLib.space().getAngleDiff(ship.direction(), ship.facing());
					final double d0 = Math.abs(deltas[0]) - (efficiency * Math.abs(deltas[0]));
					final double d1 = Math.abs(deltas[1]) - (efficiency * Math.abs(deltas[1]));
					deltas[0] += (CMLib.dice().rollPercentage()>50)?d0:-d0;
					deltas[1] += CMLib.dice().getRandomizer().nextBoolean()?d1:-d1;
					CMLib.space().applyAngleDiff(newDirections, deltas);
				}
				ship.direction()[0]=newDirections[0];
				ship.direction()[1]=newDirections[1];
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
		if(!(E instanceof StdShipInertialBattery))
			return false;
		return super.sameAs(E);
	}
}
