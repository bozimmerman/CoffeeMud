package com.planet_ink.coffee_mud.Items.ShipTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2014 Bo Zimmerman

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
public class StdShipThruster extends StdCompFuelConsumer implements ShipComponent.ShipEngine
{
	@Override public String ID(){	return "StdShipThruster";}
	
	protected float 	installedFactor	= 1.0F;
	protected int		maxThrust		= 900000;
	protected int		thrust			= 0;
	protected long		specificImpulse	= SpaceObject.VELOCITY_SUBLIGHT;
	protected double	fuelEfficiency	= 0.33;

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
	
	protected static double getThrustFactor() 
	{ 
		return 100.0; 
	}

	protected static double getFuelDivisor() 
	{ 
		return 100.0; 
	}

	@Override public double getFuelEfficiency() { return fuelEfficiency; }
	@Override public void setFuelEfficiency(double amt) { fuelEfficiency=amt; }
	@Override public float getInstalledFactor() { return installedFactor; }
	@Override public void setInstalledFactor(float pct) { if((pct>=0.0)&&(pct<=2.0)) installedFactor=pct; }
	@Override public int getMaxThrust(){return maxThrust;}
	@Override public void setMaxThrust(int max){maxThrust=max;}
	@Override public int getThrust(){return thrust;}
	@Override public void setThrust(int current){thrust=current;}
	@Override public long getSpecificImpulse() { return specificImpulse; }
	@Override
	public void setSpecificImpulse(long amt)
	{
		if(amt > 0)
			specificImpulse = amt;
	}

	@Override public TechType getTechType() { return TechType.SHIP_ENGINE; }
	@Override protected boolean willConsumeFuelIdle() { return getThrust()>0; }

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
		for(final Iterator<Electronics.Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
		{
			final Electronics.Computer C=c.next();
			if((controlI==null)||(C!=controlI.owner()))
			{
				final CMMsg msg2=CMClass.getMsg(mob, C, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(C.okMessage(mob, msg2))
					C.executeMsg(mob, msg2);
			}
		}
	}
	
	public static boolean executeThrust(final ShipEngine me, final String circuitKey, final MOB mob, final Software controlI, final ShipEngine.ThrustPort portDir, final int amount)
	{
		final LanguageLibrary lang=CMLib.lang();
		final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
		final Manufacturer manufacturer=me.getFinalManufacturer();
		if(!(obj instanceof SpaceShip))
			return reportError(me, controlI, mob, lang.L("@x1 rumbles and fires, but nothing happens.",me.name(mob)), lang.L("Failure: @x1: exhaust ports.",me.name(mob)));
		final SpaceShip ship=(SpaceShip)obj;
		if((portDir==null)||(amount<0))
			return reportError(me, controlI, mob, lang.L("@x1 rumbles loudly, but accomplishes nothing.",me.name(mob)), lang.L("Failure: @x1: exhaust control.",me.name(mob)));
		double thrust=Math.round(me.getInstalledFactor() * (amount + ship.getMass()));
		if(thrust > me.getMaxThrust())
			thrust=me.getMaxThrust();
		thrust=(int)Math.round(manufacturer.getReliabilityPct() * thrust);

		if(portDir==ThrustPort.AFT) // when thrusting aft, the thrust is continual, so save it
			me.setThrust((int)Math.round(thrust));
		
		final int fuelToConsume=(int)Math.round(CMath.ceiling(thrust*me.getFuelEfficiency()*Math.max(.33, Math.abs(2.0-manufacturer.getEfficiencyPct()))/getFuelDivisor()));
		final long accelleration=Math.round(Math.ceil(CMath.div(thrust*getThrustFactor(),ship.getMass())));
		if(amount > 1)
			tellWholeShip(me,mob,CMMsg.MSG_NOISE,CMLib.lang().L("You feel a rumble and hear the blast of @x1.",me.name(mob)));
		if(accelleration == 0)
		{
			final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_ENGINE, "Failure: "+me.name()+": insufficient_thrust_capacity.");
			sendComputerMessage(me,circuitKey,mob,controlI,code);
			return reportError(me, controlI, mob, lang.L("@x1 rumbles very loudly, but nothing is happening.",me.name(mob)), lang.L("Failure: @x1: insufficient engine thrust capacity.",me.name(mob)));
		}
		else
		if(me.consumeFuel(fuelToConsume))
		{
			final SpaceObject spaceObject=ship.getShipSpaceObject();
			final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(portDir,Integer.valueOf((int)accelleration),Long.valueOf(me.getSpecificImpulse()));
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
			return reportError(me, controlI, mob, lang.L("@x1 rumbles loudly, then sputters down.",me.name(mob)), lang.L("Failure: @x1: insufficient fuel.",me.name(mob)));
		}
		return false;
	}

	public static boolean executeCommand(ShipEngine me, String circuitKey, CMMsg msg)
	{
		final LanguageLibrary lang=CMLib.lang();
		final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
		final MOB mob=msg.source();
		final String[] parts=msg.targetMessage().split(" ");
		final TechCommand command=TechCommand.findCommand(parts);
		if(command==null)
			return reportError(me, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
		final Object[] parms=command.confirmAndTranslate(parts);
		if(parms==null)
			return reportError(me, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
		if(command == TechCommand.THRUST)
			return executeThrust(me, circuitKey, mob, controlI, (ShipEngine.ThrustPort)parms[0],((Integer)parms[1]).intValue());
		return reportError(me, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
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
				me.activate(false);
				//TODO:what does the ship need to know?
				break;
			case CMMsg.TYP_POWERCURRENT:
			{
				final Manufacturer manufacturer=me.getFinalManufacturer();
				final int fuelToConsume=(int)Math.round(CMath.ceiling(me.getThrust()*me.getFuelEfficiency()*Math.max(.33, Math.abs(2.0-manufacturer.getEfficiencyPct()))/getFuelDivisor()));
				if(me.consumeFuel(fuelToConsume))
				{
					final SpaceObject obj=CMLib.map().getSpaceObject(me, true);
					if(obj instanceof SpaceShip)
					{
						final SpaceShip ship=(SpaceShip)obj;
						final long accelleration=Math.round(Math.ceil(CMath.div(me.getThrust()*getThrustFactor(),(double)ship.getMass())));
						final String code=Technical.TechCommand.ACCELLLERATION.makeCommand(ThrustPort.AFT,Integer.valueOf((int)accelleration),Long.valueOf(me.getSpecificImpulse()));
						final CMMsg msg2=CMClass.getMsg(msg.source(), ship, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(ship.okMessage(msg.source(), msg2))
							ship.executeMsg(msg.source(), msg2);
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
