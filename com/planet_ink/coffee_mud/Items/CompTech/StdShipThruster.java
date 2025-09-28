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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2025 Bo Zimmerman

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

	protected double			maxThrust		= 8900000;
	protected double			minThrust		= 0;
	protected double			specificImpulse	= 0.33;
	protected boolean			constantThrust	= true;
	protected volatile double	thrust			= 0;
	protected ShipAccelerator	accelerator	= new StdAccelerator(this);

	protected ShipDirectional.ShipDir[] ports	= ShipDirectional.ShipDir.values();

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
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdShipThruster))
			return false;
		return super.sameAs(E);
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
	public double getSpecificImpulse()
	{
		return specificImpulse;
	}

	@Override
	protected double getComputedEfficiency()
	{
		return super.getComputedEfficiency() * this.getInstalledFactor();
	}

	@Override
	public void setSpecificImpulse(final double amt)
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
	
	/**
	 * Standard implementation of a ShipEngine.ShipAccelerator.
	 * This implementation assumes that the engine is mounted
	 * on a ship, and that the ship is in space.  It also assumes
	 * that the engine uses fuel, and that fuel consumption
	 * is based on the thrust requested, and the mass of the ship
	 * being moved.
	 * @author Bo Zimmerman
	 * 
	 */
	protected static class StdAccelerator implements ShipEngine.ShipAccelerator
	{
		/** the engine being controlled */
		protected ShipEngine engine = null;

		/**
		 * Constructor
		 * @param engine the engine being controlled
		 */
		public StdAccelerator(final ShipEngine engine)
		{
			this.engine = engine;
		}
		
		/**
		 * Report an error to the controlling software, and/or the controlling mob
		 * @param controlI the controlling software, or null
		 * @param mob the controlling mob, or null
		 * @param literalMessage a message to tell the mob, or null
		 * @param controlMessage a message to send to the software, or null
		 * @return false always
		 */
		protected boolean reportError(final Software controlI, final MOB mob, final String literalMessage, final String controlMessage)
		{
			if((mob!=null) && (mob.location()==CMLib.map().roomLocation(engine)) && (literalMessage!=null))
				mob.tell(literalMessage);
			if(controlMessage!=null)
			{
				if(controlI!=null)
					controlI.addScreenMessage(controlMessage);
				else
				if(mob!=null)
					mob.tell(CMLib.lang().L("A panel on @x1 reports '@x2'.",engine.name(),controlMessage));
			}
			return false;
		}

		/**
		 * Tell the whole ship a message
		 * @param mob the mob doing the action
		 * @param msgCode the type of message
		 * @param message the message
		 * @return false always
		 */
		protected boolean tellWholeShip(final MOB mob, final int msgCode, final String message)
		{
			Room R=CMLib.map().roomLocation(engine);
			if(R==null)
				R=mob.location();
			if(R!=null)
			{
				if(R.getArea() instanceof SpaceShip)
				{
					for(final Enumeration<Room> r=R.getArea().getProperMap();r.hasMoreElements();)
						r.nextElement().show(mob, null, msgCode, message);
				}
				else
					R.show(mob, null, msgCode, message);
			}
			return false;
		}

		/**
		 * Send a message to all computers on the given circuit, except the given software
		 * @param mob the mob doing the action
		 * @param controlI the controlling software, or null
		 * @param circuitKey the circuit key
		 * @param code the code to send
		 */
		protected void sendComputerMessage(final MOB mob, final Software controlI, final String circuitKey, final String code)
		{
			for(final Iterator<Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
			{
				final Computer C=c.next();
				if((controlI==null)||(C!=controlI.owner()))
				{
					final CMMsg msg2=CMClass.getMsg(mob, C, engine, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(C.okMessage(mob, msg2))
						C.executeMsg(mob, msg2);
				}
			}
		}
		/**
		 * Returns the amount of thrust that hits the max fuse usage.
		 * @return the fuel thrust cap
		 */
		@Override
		public double getFuelThrustCap()
		{
			return 1e12;
		}

		/**
		 * Based on the thrust requested, and the mass of the ship,
		 * return the amount of fuel that should be consumed.
		 * @param portDir the direction of the thrust
		 * @param thrust the amount of thrust requested
		 * @return the amount of fuel to consume
		 */
		@Override
		public int getFuelToConsume(final ShipDir portDir, double thrust)
		{
			final Manufacturer manufacturer=engine.getFinalManufacturer();
			if((portDir != ShipDir.AFT) && (portDir != ShipDir.FORWARD))
				thrust = 1.0;
			final SpaceObject ship = CMLib.space().getSpaceObject(engine, true);
			final double shipMass = (ship != null)?ship.getMass():1e6;
			final double normThrust = thrust / 1e6;
			final double normMass = shipMass / 1e6;
			final double efficiency = manufacturer.getEfficiencyPct();
			final double isp = engine.getSpecificImpulse();
			final double linearBase = (normThrust * normMass) / (isp * efficiency);
			final double max_theta = Math.PI / 2.0;
			final double theta = Math.min(max_theta, (linearBase / getFuelThrustCap()) * max_theta);
			final double fuel = 1000.0 * Math.sin(theta);
			final int fuelInt = (int) Math.round(fuel);
			return ((thrust > 0) && (fuelInt <= 0)) ? 1 : fuelInt;
		}

		/**
		 * Given an amount of injection 0-1, return the amount of thrust
		 * that will actually be sent into the ship's movement.
		 * This may be less than the requested amount, due to
		 * engine limitations.
		 * @param injection the amount of thrust to inject
		 * @return the amount of thrust that will actually be injected
		 */
		@Override
		public double getInjectedThrust(final double injection)
		{
			final double installedFactor = engine.getInstalledFactor();
			final double minThrust = engine.getMinThrust();
			final double maxThrust = engine.getMaxThrust();
			double thrust=installedFactor * (minThrust + ((maxThrust - minThrust) * injection));
			if(thrust > maxThrust)
				thrust=maxThrust;
			final double actualMinThrust = installedFactor * minThrust;
			if (thrust < actualMinThrust)
				thrust = actualMinThrust;
			return thrust;
		}
		
		/**
		 * Execute a thrust action, which is an action
		 * sent to the ship's thruster to cause thrust in
		 * some direction.
		 * @param mob the mob doing the thrusting
		 * @param controlI the software controlling the engine
		 * @param circuitKey the key of the circuit that contains this thruster
		 * @param portDir the direction of the thrust
		 * @param injection the amount of thrust to inject
		 * @param simulation true if this is just a simulation
		 * @return true if the command was successfully executed
		 */
		@Override
		public boolean executeThrust(final MOB mob, final Software controlI, final String circuitKey,
									final ShipDirectional.ShipDir portDir, final double injection, final boolean simulation)
		{
			final LanguageLibrary lang=CMLib.lang();
			final SpaceObject obj=CMLib.space().getSpaceObject(engine, true);
			final Manufacturer manufacturer=engine.getFinalManufacturer();
			final String rumbleWord = (engine instanceof FuelConsumer) ? lang.L("rumbles") : lang.L("hums");
			if(!(obj instanceof SpaceShip))
				return reportError(controlI, mob, lang.L("@x1 @x2 and fires, but nothing happens.",engine.name(),rumbleWord),
						lang.L("Failure: @x1: exhaust ports.",engine.name()));
			final SpaceShip ship=(SpaceShip)obj;
			if((portDir==null)||(injection<0))
				return reportError(controlI, mob, lang.L("@x1 @x2s loudly, but accomplishes nothing.",engine.name(),rumbleWord),
						lang.L("Failure: @x1: exhaust control.",engine.name()));
			if(!CMParms.contains(engine.getAvailPorts(), portDir))
				return reportError(controlI, mob, lang.L("@x1 @x2 a little, but accomplishes nothing.",engine.name(),rumbleWord),
						lang.L("Failure: @x1: port control.",engine.name()));
			final double thrust=getInjectedThrust(injection);
			double amount = thrust;
			if(engine.subjectToWearAndTear())
			{
				if(engine.usesRemaining()<75)
				{
					final double pct = CMath.mul(engine.usesRemaining(), 100.0) + (0.35 * manufacturer.getReliabilityPct());
					if(pct < 1.0)
						amount = amount * pct;
				}
			}
			if(portDir==ShipDirectional.ShipDir.AFT) // when thrusting aft, the thrust is continual, so save it
			{
				if(amount == 0.0)
				{
					engine.setThrust(0.0);
					return false;
				}
				engine.setThrust(thrust); // also, its always the intended amount, not the adjusted amount
			}

			final int fuelToConsume=getFuelToConsume(portDir, thrust); // is based off desired thrust -- NOT the amount of thrust

			final double acceleration;
			if(portDir==ShipDirectional.ShipDir.AFT) // when thrusting aft, there's a smidgeon more power
			{
				acceleration = CMath.div(amount,  ship.getMass()); // actual acceleration from the actual amount
				if(acceleration < 0.000001)
					return reportError(controlI, mob, lang.L("@x1 @x2 loudly, but nothing happens.",engine.name(),rumbleWord),
							lang.L("Failure: @x1: insufficient thrust.",engine.name()));
			}
			else // if we ever make multi-directional thrusters that don't care about facing, change this
				acceleration = injection;
			//if((amount > 1)&&((portDir!=ShipDirComponent.ShipDir.AFT) || (engine.getThrust() > (oldThrust * 10))))
			//	tellWholeShip(me,mob,CMMsg.MSG_NOISE,CMLib.lang().L("You feel a @x2 and hear the blast of @x1.",engine.name(),rumbleWord));
			if(acceleration == 0.0)
			{
				final String code=TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+engine.name()+": insufficient_thrust_capacity.");
				sendComputerMessage(mob,controlI,circuitKey,code);
				return reportError(controlI, mob, lang.L("@x1 @x2 very loudly, but nothing is happening.",engine.name(),rumbleWord),
						lang.L("Failure: @x1: insufficient engine thrust capacity.",engine.name()));
			}
			else
			if(simulation || engine.consumeFuel(fuelToConsume))
			{
				final SpaceObject spaceObject=ship.getShipSpaceObject();
				if(CMSecurity.isDebugging(DbgFlag.SPACEMOVES) && (!simulation)&&(fuelToConsume>1))
				{
					final String word = simulation?"Set Thrust: ":"Thrusting: ";
					Log.debugOut("StdShipThruster",word+engine.name()+" dir="+portDir.name()+" amt="+amount+" acc="+acceleration+" fuel="+fuelToConsume);
				}
				final String code=TechCommand.ACCELERATION.makeCommand(portDir.opposite(),Double.valueOf(acceleration),Boolean.valueOf(engine.isReactionEngine()));
				final int msgType = CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG | (simulation?CMMsg.MASK_INTERMSG:0);
				final CMMsg msg=CMClass.getMsg(mob, spaceObject, engine, CMMsg.NO_EFFECT, null, msgType, code, CMMsg.NO_EFFECT,null);
				if(spaceObject.okMessage(mob, msg))
				{
					spaceObject.executeMsg(mob, msg);
					return true;
				}
			}
			else
			{
				final String code=TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure:_"+engine.name().replace(' ','_')+":_insufficient_fuel.");
				sendComputerMessage(mob,controlI,circuitKey,code);
				return reportError(controlI, mob, lang.L("@x1 @x2 loudly, then sputters down.",engine.name(),rumbleWord),
						lang.L("Failure: @x1: insufficient fuel.",engine.name()));
			}
			return false;
		}
		
		/**
		 * Execute the activate command, which is a command
		 * sent to the ship's thruster to cause thrust in
		 * some direction.
		 * @param msg the thruster message
		 * @param circuitKey the key of the circuit that contains this thruster
		 * @return true if the command was successfully executed
		 */
		@Override
		public boolean executeActivateCommand(final CMMsg msg, final String circuitKey)
		{
			final LanguageLibrary lang=CMLib.lang();
			final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
			final MOB mob=msg.source();
			if(msg.targetMessage()==null)
			{
				engine.setThrust(0);
				return true;
			}
			else
			{
				final TechCommand command=TechCommand.findCommand(msg.targetMessage());
				if(command==null)
					return reportError(controlI, mob, lang.L("@x1 does not respond.",engine.name()), lang.L("Failure: @x1: control failure.",engine.name()));
				final Object[] parms=command.confirmAndTranslate(msg.targetMessage());
				if(parms==null)
					return reportError(controlI, mob, lang.L("@x1 did not respond.",engine.name()), lang.L("Failure: @x1: control syntax failure.",engine.name()));
				if(command == TechCommand.THRUST)
					return executeThrust(mob, controlI, circuitKey, (ShipDirectional.ShipDir)parms[0],((Double)parms[1]).doubleValue(),msg.targetMajor(CMMsg.MASK_INTERMSG));
				return reportError(controlI, mob, lang.L("@x1 refused to respond.",engine.name()), lang.L("Failure: @x1: control command failure.",engine.name()));
			}
		}

		/**
		 * Execute the ongoing thrust command, which is a command
		 * sent to the ship's thruster to continue thrusting.
		 * @param mob the mob acting as agent
		 * @param circuitKey the key of the circuit that contains this thruster
		 * @return true if the command was successfully executed
		 */
		@Override
		public boolean executeOngoingThrustCommand(final MOB mob, final String circuitKey)
		{
			if((engine.getThrust()>0.0)
			&& (CMParms.contains(engine.getAvailPorts(),ShipDirectional.ShipDir.AFT)))
			{
				final int fuelToConsume=getFuelToConsume(ShipDirectional.ShipDir.AFT, engine.getThrust());
				if(engine.consumeFuel(fuelToConsume))
				{
					double derivedInjection = 0.0;
					final double maxMinusMin = engine.getMaxThrust() - engine.getMinThrust();
					if (maxMinusMin > 0)
						derivedInjection = (engine.getThrust() / engine.getInstalledFactor() - engine.getMinThrust()) / maxMinusMin;
					final SpaceObject obj=CMLib.space().getSpaceObject(engine, true);
					if(obj instanceof SpaceShip)
					{
						final SpaceObject ship=((SpaceShip)obj).getShipSpaceObject();
						final String code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT,Double.valueOf(derivedInjection));
						final CMMsg msg2=CMClass.getMsg(mob, engine, null, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(engine.owner() instanceof Room)
						{
							if(engine.owner().okMessage(mob, msg2))
							{
								((Room)engine.owner()).send(mob, msg2);
								return true;
							}
						}
						else
						if(ship.okMessage(mob, msg2))
						{
							ship.executeMsg(mob, msg2);
							return true;
						}
					}
				}
			}
			else
			if(!(engine instanceof PowerGenerator))
			{
				final CMMsg msg2=CMClass.getMsg(mob, engine, engine, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
				if(engine.owner() instanceof Room)
				{
					if(engine.owner().okMessage(mob, msg2))
						((Room)engine.owner()).send(mob, msg2);
				}
				else
				if(engine.okMessage(mob, msg2))
					engine.executeMsg(mob, msg2);
				final String code=TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+engine.name()+": insufficient_fuel.");
				sendComputerMessage(mob, null, circuitKey, code);
				return true;
			}
			return false;
		}
		/**
		 * Execute the deactivate command, which is a command
		 * sent to the ship's thruster to stop thrusting.
		 * @param mob the mob acting as agent
		 * @return true if the command was successfully executed
		 */
		@Override
		public boolean executeDeactivateCommand(MOB mob)
		{
			// when a constant thruster deactivates, all speed stops
			final SpaceObject obj=CMLib.space().getSpaceObject(engine, true);
			if(obj instanceof SpaceShip)
			{
				final SpaceShip ship=(SpaceShip)obj;
				final SpaceObject spaceObject=ship.getShipSpaceObject();
				final String code=TechCommand.ACCELERATION.makeCommand(ShipDirectional.ShipDir.AFT,Double.valueOf(0),Boolean.valueOf(true));
				final CMMsg msg2=CMClass.getMsg(mob, spaceObject, engine, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(spaceObject.okMessage(mob, msg2))
				{
					spaceObject.executeMsg(mob, msg2);
					engine.setThrust(0.0);
					engine.activate(false);
					return true;
				}
			}
			return false;
		}
	}
}
