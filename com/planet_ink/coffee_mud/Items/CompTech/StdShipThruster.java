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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class StdShipThruster extends StdCompFuelConsumer implements ShipEngine
{
	@Override
	public String ID()
	{
		return "StdShipThruster";
	}

	protected int		maxThrust		= 8900000;
	protected int		minThrust		= 0;
	protected double	thrust			= 0;
	protected long		specificImpulse	= SpaceObject.VELOCITY_SUBLIGHT;
	protected double	fuelEfficiency	= 0.33;
	protected boolean	constantThrust	= true;
	
	protected TechComponent.ShipDir[] ports 		= TechComponent.ShipDir.values(); 

	public StdShipThruster()
	{
		super();
		setName("a thruster engine");
		basePhyStats.setWeight(5000);
		setDisplayText("a thruster engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setCapacity(basePhyStats.weight()+100000);
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipThruster))
			return false;
		return super.sameAs(E);
	}

	protected static double getFuelDivisor() 
	{ 
		return 100.0; 
	}

	@Override
	public double getFuelEfficiency()
	{
		return fuelEfficiency;
	}

	@Override
	public void setFuelEfficiency(double amt)
	{
		fuelEfficiency = amt;
	}

	@Override
	public int getMaxThrust()
	{
		return maxThrust;
	}

	@Override
	public void setMaxThrust(int max)
	{
		maxThrust = max;
	}

	@Override
	public double getThrust()
	{
		return thrust;
	}

	@Override
	public void setThrust(double current)
	{
		thrust = current;
	}

	@Override
	public long getSpecificImpulse()
	{
		return specificImpulse;
	}

	@Override
	protected double getComputedEfficiency()
	{
		return super.getComputedEfficiency() * this.getInstalledFactor();
	}
	
	@Override
	public void setSpecificImpulse(long amt)
	{
		if(amt > 0)
			specificImpulse = amt;
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
	public int getMinThrust()
	{
		return minThrust;
	}

	@Override
	public void setMinThrust(int min)
	{
		this.minThrust = min;
	}

	@Override
	public boolean isConstantThruster()
	{
		return constantThrust;
	}
	
	@Override
	public void setConstantThruster(boolean isConstant)
	{
		constantThrust = isConstant;
	}
	
	/**
	 * Gets set of available thrust ports on this engine.
	 * @see ShipEngine#setAvailPorts(TechComponent.ShipDir[])
	 * @return the set of available thrust ports.
	 */
	@Override
	public TechComponent.ShipDir[] getAvailPorts()
	{
		return ports;
	}

	/**
	 * Sets set of available thrust ports on this engine.
	 * @see ShipEngine#getAvailPorts()
	 * @param ports the set of available thrust ports.
	 */
	@Override
	public void setAvailPorts(TechComponent.ShipDir[] ports)
	{
		this.ports = ports;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		executeThrusterMsg(this, myHost, circuitKey, msg);
	}

	public static boolean reportError(final ShipEngine me, final Software controlI, final MOB mob, final String literalMessage, final String controlMessage)
	{
		if((mob!=null) && (mob.location()==CMLib.map().roomLocation(me)) && (literalMessage!=null))
			mob.tell(literalMessage);
		if(controlMessage!=null)
		{
			if(controlI!=null)
				controlI.addScreenMessage(controlMessage);
			else
			if((mob!=null)&&(me!=null))
				mob.tell(CMLib.lang().L("A panel on @x1 reports '@x2'.",me.name(mob),controlMessage));
		}
		return false;
	}

	public static boolean tellWholeShip(final ShipEngine me, final MOB mob, final int msgCode, final String message)
	{
		Room R=CMLib.map().roomLocation(me);
		if(R==null)
			R=mob.location();
		if(R!=null)
		{
			if(R.getArea() instanceof SpaceShip)
			{
				for(Enumeration<Room> r=R.getArea().getProperMap();r.hasMoreElements();)
					r.nextElement().show(mob, null, msgCode, message);
			}
			else
				R.show(mob, null, msgCode, message);
		}
		return false;
	}

	protected static void sendComputerMessage(final ShipEngine me, final String circuitKey, final MOB mob, final Item controlI, final String code)
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
	
	private static int getFuelToConsume(final ShipEngine me, final Manufacturer manufacturer, double thrust)
	{
		final int fuel=(int)Math.round(
			CMath.ceiling(
				thrust
				*me.getFuelEfficiency()
				*Math.max(.33, Math.abs(2.0-manufacturer.getEfficiencyPct())) 
				/ getFuelDivisor()
			)
		);
		if((thrust>0)&&(fuel<=0))
			return 1;
		return fuel;
	}
	
	public static boolean executeThrust(final ShipEngine me, final String circuitKey, final MOB mob, final Software controlI, final TechComponent.ShipDir portDir, final double amount)
	{
		final LanguageLibrary lang=CMLib.lang();
		final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
		final Manufacturer manufacturer=me.getFinalManufacturer();
		final String rumbleWord = (me instanceof FuelConsumer) ? "rumble" : "hum";
		if(!(obj instanceof SpaceShip))
			return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s and fires, but nothing happens.",me.name(mob)), lang.L("Failure: @x1: exhaust ports.",me.name(mob)));
		final SpaceShip ship=(SpaceShip)obj;
		if((portDir==null)||(amount<0))
			return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s loudly, but accomplishes nothing.",me.name(mob)), lang.L("Failure: @x1: exhaust control.",me.name(mob)));
		if(!CMParms.contains(me.getAvailPorts(), portDir))
			return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s a little, but accomplishes nothing.",me.name(mob)), lang.L("Failure: @x1: port control.",me.name(mob)));
		double thrust=me.getInstalledFactor() * amount;
		if(thrust > me.getMaxThrust())
			thrust=me.getMaxThrust();
		if(me.subjectToWearAndTear())
		{
			if(me.usesRemaining()<75)
			{
				final double pct = CMath.mul(me.usesRemaining(), 100.0) + (0.35 * manufacturer.getReliabilityPct());
				if(pct < 1.0)
					thrust = thrust * pct;
			}
		}
		else
			thrust=manufacturer.getReliabilityPct() * thrust;
		final double oldThrust = me.getThrust();
		if(portDir==TechComponent.ShipDir.AFT) // when thrusting aft, the thrust is continual, so save it
			me.setThrust(amount); // also, its always the intended amount, not the adjusted amount
		
		final int fuelToConsume=getFuelToConsume(me, manufacturer, thrust);
		
		final double activeThrust;
		if(portDir==TechComponent.ShipDir.AFT) // when thrusting aft, there's a smidgeon more power
		{
			activeThrust = (thrust * me.getSpecificImpulse() / ship.getMass());
			if(activeThrust < me.getMinThrust())
			{
				return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s loudly, but nothing happens.",me.name(mob)), lang.L("Failure: @x1: insufficient thrust.",me.name(mob)));
			}
		}
		else
			activeThrust = thrust;
		
		final double accelleration=activeThrust;
		if((amount > 1)&&((portDir!=TechComponent.ShipDir.AFT) || (me.getThrust() > oldThrust)))
			tellWholeShip(me,mob,CMMsg.MSG_NOISE,CMLib.lang().L("You feel a "+rumbleWord+" and hear the blast of @x1.",me.name(mob)));
		if(accelleration == 0)
		{
			final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+me.name()+": insufficient_thrust_capacity.");
			sendComputerMessage(me,circuitKey,mob,controlI,code);
			return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s very loudly, but nothing is happening.",me.name(mob)), lang.L("Failure: @x1: insufficient engine thrust capacity.",me.name(mob)));
		}
		else
		if(me.consumeFuel(fuelToConsume))
		{
			final SpaceObject spaceObject=ship.getShipSpaceObject();
			final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(portDir,Double.valueOf(accelleration),Boolean.valueOf(me.isConstantThruster()));
			final CMMsg msg=CMClass.getMsg(mob, spaceObject, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(spaceObject.okMessage(mob, msg))
			{
				spaceObject.executeMsg(mob, msg);
				return true;
			}
		}
		else
		{
			final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure:_"+me.name().replace(' ','_')+":_insufficient_fuel.");
			sendComputerMessage(me,circuitKey,mob,controlI,code);
			return reportError(me, controlI, mob, lang.L("@x1 "+rumbleWord+"s loudly, then sputters down.",me.name(mob)), lang.L("Failure: @x1: insufficient fuel.",me.name(mob)));
		}
		return false;
	}

	public static boolean executeCommand(ShipEngine me, String circuitKey, CMMsg msg)
	{
		final LanguageLibrary lang=CMLib.lang();
		final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
		final MOB mob=msg.source();
		if(msg.targetMessage()==null)
		{
			me.setThrust(0);
			return true;
		}
		else
		{
			final String[] parts=msg.targetMessage().split(" ");
			final TechCommand command=TechCommand.findCommand(parts);
			if(command==null)
				return reportError(me, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
			final Object[] parms=command.confirmAndTranslate(parts);
			if(parms==null)
				return reportError(me, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
			if(command == TechCommand.THRUST)
				return executeThrust(me, circuitKey, mob, controlI, (TechComponent.ShipDir)parms[0],((Double)parms[1]).doubleValue());
			return reportError(me, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
		}
	}

	public static void executeThrusterMsg(ShipEngine me, Environmental myHost, String circuitKey, CMMsg msg)
	{
		if(msg.amITarget(me))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(executeCommand(me, circuitKey, msg))
					me.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				me.setThrust(0);
				if(me.activated())
				{
					// when a constant thruster deactivates, all speed stops
					final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
					if(obj instanceof SpaceShip)
					{
						final MOB mob=msg.source();
						final SpaceShip ship=(SpaceShip)obj;
						final SpaceObject spaceObject=ship.getShipSpaceObject();
						final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(0),Boolean.valueOf(true));
						final CMMsg msg2=CMClass.getMsg(mob, spaceObject, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(spaceObject.okMessage(mob, msg2))
							spaceObject.executeMsg(mob, msg2);
					}
				}
				me.activate(false);
				break;
			case CMMsg.TYP_POWERCURRENT:
			{
				final Manufacturer manufacturer=me.getFinalManufacturer();
				final int fuelToConsume=(int)Math.round(CMath.ceiling(me.getThrust()*me.getFuelEfficiency()*Math.max(.33, Math.abs(2.0-manufacturer.getEfficiencyPct()))/getFuelDivisor()));
				if(me.consumeFuel(fuelToConsume))
				{
					if((me.getThrust() > 0) && (me.isConstantThruster()) && (CMParms.contains(me.getAvailPorts(),TechComponent.ShipDir.AFT)))
					{
						final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
						if(obj instanceof SpaceShip)
						{
							final SpaceObject ship=((SpaceShip)obj).getShipSpaceObject();
							final String code=Technical.TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(me.getThrust()));
							final CMMsg msg2=CMClass.getMsg(msg.source(), me, null, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
							if(me.owner() instanceof Room)
							{
								if(me.owner().okMessage(msg.source(), msg2))
									((Room)me.owner()).send(msg.source(), msg2);
							}
							else
							if(ship.okMessage(msg.source(), msg2))
								ship.executeMsg(msg.source(), msg2);
						}
					}
				}
				else
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), me, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
					if(me.owner() instanceof Room)
					{
						if(((Room)me.owner()).okMessage(msg.source(), msg2))
							((Room)me.owner()).send(msg.source(), msg2);
					}
					else
					if(me.okMessage(msg.source(), msg2))
						me.executeMsg(msg.source(), msg2);
					final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+me.name()+": insufficient_fuel.");
					sendComputerMessage(me,circuitKey,msg.source(),null,code);
				}
				break;
			}
			}
		}
	}
}
