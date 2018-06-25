package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.io.*;
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
public class RocketShipProgram extends GenShipProgram
{
	@Override
	public String ID()
	{
		return "RocketShipProgram";
	}

	protected volatile long nextPowerCycleTmr = System.currentTimeMillis()+(8*1000);

	protected String noActivationMenu="^rNo engine systems found.\n\r";

	protected volatile List<ShipEngine>		engines		= null;
	protected volatile List<TechComponent>	sensors		= null;
	protected volatile List<TechComponent>	components	= null;
	
	protected volatile Double				lastAccelleration	= null;
	protected volatile Double				lastAngle			= null;
	protected volatile Double				lastInject			= null;
	protected volatile RocketStateMachine	rocketState			= null;
	protected volatile SpaceObject			programPlanet		= null;
	protected volatile List<ShipEngine>		programEngines		= null;
	protected final	   List<SpaceObject>	sensorReport		= new LinkedList<SpaceObject>();

	protected final PairSLinkedList<Long,List<SpaceObject>>	sensorReports	= new PairSLinkedList<Long,List<SpaceObject>>();
	
	protected enum RocketStateMachine
	{
		LAUNCHSEARCH,
		LAUNCHCHECK,
		LAUNCHCRUISE,
		STOP,
		PRE_LANDING_STOP,
		LANDING_APPROACH,
		LANDING
	}

	public RocketShipProgram()
	{
		super();
		setName("a shuttle operations disk");
		setDisplayText("a small software disk sits here.");
		setDescription("It appears to be a program to operate a small shuttle or rocket.");

		basePhyStats.setWeight(100);
		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
		recoverPhyStats();
	}

	@Override
	public String getParentMenu()
	{
		return "";
	}

	@Override
	public String getInternalName()
	{
		return "SHIP";
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		return this.getActivationMenu();
	}

	protected synchronized List<TechComponent> getComponent(final TechType type)
	{
		List<TechComponent> components;
		if(circuitKey.length()==0)
			return components=new Vector<TechComponent>(0);
		else
		{
			final List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(circuitKey);
			components=new Vector<TechComponent>(1);
			for(final Electronics E : electronics)
			{
				if ((E instanceof TechComponent) && (E.getTechType()== type))
					components.add((TechComponent)E);
			}
		}
		return components;
	}

	protected synchronized List<ShipEngine> getEngines()
	{
		List<ShipEngine> engines = this.engines;
		if(engines == null)
		{
			engines=new Vector<ShipEngine>(1);
			final List<TechComponent> stuff=getTechComponents();
			for(final Electronics E : stuff)
			{
				if(E instanceof ShipEngine)
					engines.add((ShipEngine)E);
			}
			this.engines = engines;
		}
		return engines;
	}

	protected synchronized List<TechComponent> getTechComponents()
	{
		if(components == null)
		{
			if(circuitKey.length()==0)
				components=new Vector<TechComponent>(0);
			else
			{
				final List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(circuitKey);
				components=new Vector<TechComponent>(1);
				for(final Electronics E : electronics)
				{
					if(E instanceof TechComponent)
						components.add((TechComponent)E);
				}
			}
		}
		return components;
	}

	protected synchronized List<TechComponent> getShipSensors()
	{
		if(sensors == null)
		{
			final List<TechComponent> stuff=getTechComponents();
			sensors=new Vector<TechComponent>(1);
			for(final TechComponent E : stuff)
			{
				if(E.getTechType()==TechType.SHIP_SENSOR)
					sensors.add(E);
			}
		}
		return sensors;
	}

	@Override
	public boolean isActivationString(String word)
	{
		return isCommandString(word, false);
	}

	@Override
	public boolean isDeActivationString(String word)
	{
		return isCommandString(word, false);
	}

	protected TechComponent findComponentByName(List<? extends TechComponent> list, String prefix, String name)
	{
		if(list.size()==0)
			return null;
		name=name.toUpperCase();
		if(name.startsWith(prefix))
		{
			final String numStr=name.substring(6);
			if(!CMath.isInteger(numStr))
				return null;
			final int num=CMath.s_int(numStr);
			if((num>0)&&(num<=list.size()))
				return list.get(num-1);
			return null;
		}
		TechComponent E=(TechComponent)CMLib.english().fetchEnvironmental(list, name, true);
		if(E==null)
			E=(TechComponent)CMLib.english().fetchEnvironmental(list, name, false);
		return E;
	}

	protected ShipEngine findEngineByName(String name)
	{
		return (ShipEngine)findComponentByName(getEngines(), "ENGINE", name);
	}

	protected TechComponent findSensorByName(String name)
	{
		return findComponentByName(getShipSensors(), "SENSOR", name);
	}

	@Override 
	public boolean isCommandString(String word, boolean isActive)
	{
		final Vector<String> parsed=CMParms.parse(word);
		if(parsed.size()==0)
			return false;
		final String uword=parsed.get(0).toUpperCase();
		if(uword.equals("HELP")
		||uword.equals("INFO")
		||uword.equals("STOP")
		||uword.equals("LAND")
		||uword.equals("LAUNCH")
		||uword.equals("ACTIVATE")
		||uword.equals("DEACTIVATE")
		||(uword.startsWith("ENGINE")&&(CMath.isInteger(uword.substring(6))))
		||(uword.startsWith("SENSOR")&&(CMath.isInteger(uword.substring(6))))
		||(uword.startsWith("SYSTEM")&&(CMath.isInteger(uword.substring(6))))
		)
			return true;
		return findEngineByName(uword)!=null;
	}

	public List<SpaceObject> takeSensorReport(final TechComponent sensor)
	{
		final List<SpaceObject> localSensorReport;
		synchronized(sensorReport)
		{
			sensorReport.clear();
		}
		final String code=Technical.TechCommand.SENSE.makeCommand();
		final MOB mob=CMClass.getFactoryMOB();
		try
		{
			final CMMsg msg=CMClass.getMsg(mob, sensor, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(sensor.owner() instanceof Room)
			{
				if(((Room)sensor.owner()).okMessage(mob, msg))
					((Room)sensor.owner()).send(mob, msg);
			}
			else
			if(sensor.okMessage(mob, msg))
				sensor.executeMsg(mob, msg);
		}
		finally
		{
			mob.destroy();
		}
		localSensorReport = new XVector<SpaceObject>(sensorReport.iterator());
		synchronized(sensorReport)
		{
			sensorReport.clear();
			while(this.sensorReports.size()>10)
				this.sensorReports.removeLast();
			this.sensorReports.addFirst(new Pair<Long,List<SpaceObject>>(new Long(System.currentTimeMillis()),localSensorReport));
		}
		return localSensorReport;
	}
	
	@Override
	public String getActivationMenu()
	{
		final StringBuilder str=new StringBuilder();
		str.append("^X").append(CMStrings.centerPreserve(L(" -- Flight Status -- "),60)).append("^.^N\n\r");
		final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if(ship==null)
			str.append("^Z").append(CMStrings.centerPreserve(L(" -- Can Not Determine -- "),60)).append("^.^N\n\r");
		else
		if(ship.getIsDocked() != null)
		{
			str.append("^H").append(CMStrings.padRight(L("Docked at ^w@x1",ship.getIsDocked().displayText(null)),60)).append("^.^N\n\r");
			final SpaceObject planet=CMLib.map().getSpaceObject(ship.getIsDocked(), true);
			if(planet!=null)
				str.append("^H").append(CMStrings.padRight(L("On Planet ^w@x1",planet.Name()),60)).append("^.^N\n\r");
		}
		else
		if((shipSpaceObject==null)||(!CMLib.map().isObjectInSpace(shipSpaceObject)))
			str.append("^Z").append(CMStrings.centerPreserve(L(" -- System Malfunction-- "),60)).append("^.^N\n\r");
		else
		{
			final List<SpaceObject> orbs=CMLib.map().getSpaceObjectsWithin(shipSpaceObject,0,SpaceObject.Distance.LightMinute.dm);
			SpaceObject orbitingPlanet=null;
			SpaceObject altitudePlanet=null;
			for(final SpaceObject orb : orbs)
			{
				if(orb instanceof Area)
				{
					final long distance=CMLib.map().getDistanceFrom(shipSpaceObject, orb);
					if((distance > orb.radius())&&(distance < (orb.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
						altitudePlanet=orb; // since they are sorted, this would be the nearest.
					if((distance > orb.radius()*SpaceObject.MULTIPLIER_ORBITING_RADIUS_MIN)&&(distance<orb.radius()*SpaceObject.MULTIPLIER_ORBITING_RADIUS_MAX))
						orbitingPlanet=orb; // since they are sorted, this would be the nearest.
					break;
				}
			}

			str.append("^H").append(CMStrings.padRight(L("Speed"),10));
			str.append("^N").append(CMStrings.padRight(displayPerSec(Math.round(ship.speed())),20));
			str.append("^H").append(CMStrings.padRight(L("Direction"),10));
			final String dirStr=display(ship.direction());
			str.append("^N").append(CMStrings.padRight(dirStr,20));
			if(orbitingPlanet!=null)
			{
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Orbiting"),10));
				str.append("^N").append(CMStrings.padRight(orbitingPlanet.name(),20));
			}
			else
			if(altitudePlanet != null)
			{
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Altitude"),10));
				str.append("^N").append(CMStrings.padRight(display(CMLib.map().getDistanceFrom(shipSpaceObject, altitudePlanet)-shipSpaceObject.radius()-altitudePlanet.radius()),20));
			}
			else
			{
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Sector"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.map().getSectorName(ship.coordinates()),50));
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Location"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.english().coordDescShort(CMLib.map().getInSectorCoords(shipSpaceObject.coordinates())),20));
			}
			str.append("^H").append(CMStrings.padRight(L("Facing"),10));
			final String facStr=display(ship.facing());
			str.append("^N").append(CMStrings.padRight(facStr,20));
			str.append("\n\r");
		}
		str.append("^N\n\r");

		final List<TechComponent> sensors = this.getShipSensors();
		if(sensors.size()>0)
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Sensors -- "),60)).append("^.^N\n\r");
			int sensorNumber=1;
			for(final TechComponent sensor : sensors)
			{
				str.append("^H").append(CMStrings.padRight(L("SENSOR@x1",""+sensorNumber),9));
				str.append(CMStrings.padRight(sensor.activated()?L("^gACTIVE"):L("^rINACTIVE"),9));
				str.append("^H").append(CMStrings.padRight(sensor.Name(),34));
				str.append("^.^N\n\r");
				final List<SpaceObject> localSensorReport=takeSensorReport(sensor);
				if(localSensorReport.size()==0)
					str.append("^R").append(L("No Report"));
				else
				for(Object o : localSensorReport)
				{
					if(o == spaceObject)
						continue;
					if(o instanceof SpaceObject)
					{
						SpaceObject O=(SpaceObject)o;
						if(O.displayText().length()>0)
							str.append("^W").append(L("Found: ")).append("^N").append(O.displayText());
						else
							str.append("^W").append(L("Found: ")).append("^N").append(O.name());
					}
					else
					if(o instanceof CMObject)
						str.append("^W").append(L("Found: ")).append("^N").append(((CMObject)o).name());
					else
					if(o instanceof String)
						str.append("^W").append(L("Found: ")).append("^N").append(o.toString());
					str.append("^.^N\n\r");
				}
				str.append("^.^N\n\r");
				sensorNumber++;
			}
		}
		
		final List<ShipEngine> engines = getEngines();
		final List<TechComponent> components = getTechComponents();
		if(components.size()> engines.size() + sensors.size())
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Other Systems -- "),60)).append("^.^N\n\r");
			int systemNumber=1;
			for(final TechComponent component : components)
			{
				if((!engines.contains(component))
				&&(!sensors.contains(component)))
				{
					str.append("^H").append(CMStrings.padRight(L("SYSTEM@x1",""+systemNumber),9));
					str.append(CMStrings.padRight(component.activated()?L("^gACTIVE"):L("^rINACTIVE"),9));
					str.append("^H").append(CMStrings.padRight(L("Power"),6));
					str.append("^N").append(CMStrings.padRight(Long.toString(component.powerRemaining()),11));
					str.append("^H").append(CMStrings.padRight(component.Name(),24));
					str.append("^.^N\n\r");
					systemNumber++;
				}
			}
			str.append("^.^N\n\r");
		}
		
		if(engines.size()==0)
			str.append(noActivationMenu);
		else
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Engines -- "),60)).append("^.^N\n\r");
			int engineNumber=1;
			for(final ShipEngine engine : engines)
			{
				str.append("^H").append(CMStrings.padRight(L("ENGINE@x1",""+engineNumber),9));
				str.append(CMStrings.padRight(engine.activated()?L("^gACTIVE"):L("^rINACTIVE"),9));
				if(engine instanceof FuelConsumer)
				{
					str.append("^H").append(CMStrings.padRight(L("Fuel"),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(((FuelConsumer)engine).getFuelRemaining()),11));
				}
				else
				{
					str.append("^H").append(CMStrings.padRight(L("Pwr"),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(engine.powerRemaining()),11));
				}
				str.append("^H").append(CMStrings.padRight(engine.Name(),24));
				str.append("^.^N\n\r");
				engineNumber++;
			}
			str.append("^N\n\r");
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Commands -- "),60)).append("^.^N\n\r");
			str.append("^H").append(CMStrings.padRight(L("[HELP] : Get help."),60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight(L("[INFO] [SYSTEMNAME] : Get Details"),60)).append("\n\r");
			str.append("^X").append(CMStrings.centerPreserve("",60)).append("^.^N\n\r");
			str.append("^N\n\r");
		}
		return str.toString();
	}

	@Override
	public boolean checkActivate(MOB mob, String message)
	{
		return true;
	}

	@Override
	public boolean checkDeactivate(MOB mob, String message)
	{
		return true;
	}

	@Override
	public boolean checkTyping(MOB mob, String message)
	{
		return true;
	}

	protected void trySendMsgToItem(final MOB mob, Item engineE, CMMsg msg)
	{
		if(engineE.owner() instanceof Room)
		{
			if(((Room)engineE.owner()).okMessage(mob, msg))
				((Room)engineE.owner()).send(mob, msg);
		}
		else
		if(engineE.okMessage(mob, msg))
			engineE.executeMsg(mob, msg);
	}

	protected Double calculateTargetInjection(final SpaceShip ship, Double newInject)
	{
		//force/mass is the Gs felt by the occupants.. not force-mass
		//so go ahead and push it up to 3 * g forces on ship
		double targetAccelleration = SpaceObject.ACCELLERATION_TYPICALSPACEROCKET; //
		if(targetAccelleration > ship.speed())
			targetAccelleration = ship.speed();
		if((this.lastAccelleration !=null)
		&&(newInject != null)
		&& (targetAccelleration != 0.0))
		{
			if(this.lastAccelleration.doubleValue() < (targetAccelleration * .1))
				newInject = new Double(newInject.doubleValue()*2.0);
			else
			if(this.lastAccelleration.doubleValue() > (targetAccelleration * 100))
				newInject = new Double(newInject.doubleValue()/2.0);
			else
			if(this.lastAccelleration.doubleValue() < (targetAccelleration * 0.9))
				newInject = new Double(1.07 * newInject.doubleValue());
			else
			if(this.lastAccelleration.doubleValue() > (targetAccelleration * 1.1))
				newInject = new Double(0.93 * newInject.doubleValue());
			else
			if(this.lastAccelleration.doubleValue() < targetAccelleration)
				newInject = new Double(1.01 * newInject.doubleValue());
			else
			if(this.lastAccelleration.doubleValue() > targetAccelleration)
				newInject = new Double(0.98 * newInject.doubleValue());
		}
		return newInject;
	}

	protected void performSimpleThrust(final ShipEngine engineE, final Double thrustInject, boolean alwaysThrust)
	{
		final MOB mob=CMClass.getFactoryMOB();
		try
		{
			this.lastAccelleration =null;
			if(thrustInject != null)
			{
				if((thrustInject != this.lastInject)
				||(!engineE.isConstantThruster()))
				{
					CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(thrustInject.doubleValue()));
					msg.setTargetMessage(code);
					this.trySendMsgToItem(mob, engineE, msg);
					this.lastInject=thrustInject;
				}
			}
		}
		finally
		{
			mob.destroy();
		}
	}
	
	@Override
	public boolean checkPowerCurrent(final int value)
	{
		RocketShipProgram.RocketStateMachine state=this.rocketState;
		if(state == null)
			return super.checkPowerCurrent(value);
		final SpaceObject spaceObj=CMLib.map().getSpaceObject(this,true);
		final SpaceShip ship = (spaceObj instanceof SpaceShip) ? (SpaceShip)spaceObj : null;
		final List<ShipEngine> programEngines=this.programEngines;
		final SpaceObject programPlanet=this.programPlanet;
		final Double lastInject=this.lastInject;
		if((ship==null)||(this.programEngines==null))
		{
			String reason =  (programEngines == null)?"no engines":"";
			reason = (ship==null)?"no ship interface":reason;
			this.rocketState=null;
			this.programEngines=null;
			this.lastInject=null;
			super.addScreenMessage(L("Last program aborted with error ("+reason+")."));
			return super.checkPowerCurrent(value);
 		}
		if((programEngines.size()==0)||(lastInject==null))
		{
			String reason =  (programEngines.size()==0)?"no aft engines":"";
			reason = (lastInject==null)?"no engine injection data":reason;
			this.rocketState=null;
			this.programEngines=null;
			super.addScreenMessage(L("Stop program aborted with error ("+reason+")."));
			return super.checkPowerCurrent(value);
		}
		switch(state)
		{
		case LANDING:
		case LANDING_APPROACH:
		case PRE_LANDING_STOP:
		{
			if(ship.getIsDocked()!=null)
			{
				this.rocketState=null;
				this.programPlanet=null;
				this.programEngines=null;
				this.lastInject=null;
				super.addScreenMessage(L("Landing program completed successfully."));
				return super.checkPowerCurrent(value);
			}
			else
			if(programPlanet==null)
			{
				String reason = "no planetary information";
				this.rocketState=null;
				this.programEngines=null;
				this.lastInject=null;
				super.addScreenMessage(L("Launding program aborted with error ("+reason+")."));
				return super.checkPowerCurrent(value);
			}
			else
			if(this.rocketState!=RocketStateMachine.LANDING)
			{
				final long distance=CMLib.map().getDistanceFrom(ship.coordinates(),programPlanet.coordinates());
				if(distance > (ship.radius() + Math.round(programPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					this.rocketState=RocketStateMachine.LANDING;
			}
			if(this.rocketState!=RocketStateMachine.PRE_LANDING_STOP)
				break;
		}
		//$FALL-THROUGH$
		case STOP:
		{
			if(ship.speed()  <= 0.0)
			{
				ship.setSpeed(0.0); // that's good enough, for now.
				for(final ShipEngine engineE : programEngines)
					performSimpleThrust(engineE,Double.valueOf(0.0), true);
				this.rocketState=null;
				this.programEngines=null;
				this.lastInject=null;
				if(state == RocketStateMachine.STOP)
				{
					super.addScreenMessage(L("Stop program completed successfully."));
					return super.checkPowerCurrent(value);
				}
				else
				{
					this.rocketState=RocketStateMachine.LANDING_APPROACH;
					state=this.rocketState;
				}
			}
			else
			{
				final double[] stopFacing = CMLib.map().getOppositeDir(ship.direction());
				double[] angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
				if((Math.abs(angleDelta[0])+Math.abs(angleDelta[0]))>.02)
				{
					if(!flipForAllStop(ship))
					{
						this.rocketState=null;
						this.programEngines=null;
						this.lastInject=null;
						super.addScreenMessage(L("Stop program aborted with error (directional control failure)."));
						return super.checkPowerCurrent(value);
					}
					if(this.lastInject != null)
					{
						if(ship.speed() < SpaceObject.ACCELLERATION_TYPICALSPACEROCKET)
							this.lastInject = new Double(this.lastInject.doubleValue()/2.0);
						else
						if(ship.speed() < (SpaceObject.ACCELLERATION_TYPICALSPACEROCKET * 2))
							this.lastInject = new Double(this.lastInject.doubleValue()/1.5);
					}
				}
			}
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHCRUISE:
		case LAUNCHSEARCH:
		{
			if(programPlanet==null)
			{
				String reason = "no planetary information";
				this.rocketState=null;
				this.programEngines=null;
				this.lastInject=null;
				super.addScreenMessage(L("Launch program aborted with error ("+reason+")."));
				return super.checkPowerCurrent(value);
			}
			else
			{
				final long distance=CMLib.map().getDistanceFrom(ship, programPlanet);
				if(distance > (programPlanet.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					this.rocketState=null;
					this.lastInject=null;
					super.addScreenMessage(L("Launch program completed. Shutting down thrust."));
					for(final ShipEngine engineE : programEngines)
						performSimpleThrust(engineE,Double.valueOf(0.0), true);
					this.programEngines=null;
					return super.checkPowerCurrent(value);
				}
			}
			break;
		}
		default:
			break;
		}
		Double newInject=this.lastInject;
		switch(state)
		{
		case STOP:
		case PRE_LANDING_STOP:
		{
			newInject=calculateTargetInjection(ship, newInject);
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHSEARCH:
			newInject=calculateTargetInjection(ship, newInject);
		//$FALL-THROUGH$
		case LAUNCHCRUISE:
		{
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
			break;
		}
		case LANDING_APPROACH:
		{
			final double[] dirToPlanet = CMLib.map().getDirection(ship, programPlanet);
			final long distance=CMLib.map().getDistanceFrom(ship, programPlanet) 
					- Math.round(CMath.mul(programPlanet.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)) 
					- ship.radius();
			if(distance <= 0)
				this.rocketState = RocketStateMachine.LANDING;
			else
			{
				for(final ShipEngine engineE : programEngines)
				{
					double ticksToDecellerate = CMath.div(ship.speed(),CMath.div(SpaceObject.ACCELLERATION_TYPICALSPACEROCKET,2.0));
					final double ticksToDestinationAtCurrentSpeed = CMath.div(distance, CMath.div(ship.speed(),2.0));
					if(ticksToDecellerate >= (ticksToDestinationAtCurrentSpeed))
						this.changeFacing(ship, CMLib.map().getOppositeDir(dirToPlanet));
					else
						this.changeFacing(ship, dirToPlanet);
					newInject=calculateTargetInjection(ship, newInject);
					performSimpleThrust(engineE,newInject, false);
				}
				break;
			}
		}
		//$FALL-THROUGH$
		case LANDING:
		{
			final double[] dirToPlanet = CMLib.map().getDirection(ship, programPlanet);
			this.changeFacing(ship, CMLib.map().getOppositeDir(dirToPlanet));
			if(ship.speed()>SpaceObject.ACCELLERATION_TYPICALSPACEROCKET)
				newInject=calculateTargetInjection(ship, newInject);
			else
			if(ship.speed()>1.0)
				newInject=calculateTargetInjection(ship, new Double(ship.speed()-1.0));
			else
				newInject=null;
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, true);
			break;
		}
		default:
			break;
		}
		
		return true;
	}

	public boolean flipForAllStop(final SpaceShip ship)
	{
		final double[] stopFacing = CMLib.map().getOppositeDir(ship.direction());
		return changeFacing(ship, stopFacing);
	}

	public boolean changeFacing(final SpaceShip ship, double[] newFacing)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final boolean isDebugging = CMSecurity.isDebugging(DbgFlag.SPACESHIP);
		try
		{
			// step one, face opposite direction of motion
			if(isDebugging)
				Log.debugOut("flipping to go from "+ship.direction()[0]+","+ship.direction()[1]+"  to  "+newFacing[0]+","+newFacing[1]);
			for(final ShipEngine engineE : engines)
			{
				if((CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.STARBOARD))
				&&(CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.PORT))
				&&(CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.DORSEL))
				&&(CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.VENTRAL)))
				{
					msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					this.lastAngle = null;
					final String code=TechCommand.THRUST.makeCommand(ShipDir.PORT,Double.valueOf(1));
					msg.setTargetMessage(code);
					this.trySendMsgToItem(M, engineE, msg);
					if(this.lastAngle==null)
						break;
					if(isDebugging)
						Log.debugOut("Thrusting 1 to PORT to achieve DELTA, and got a delta of "+this.lastAngle.doubleValue());
					double angleAchievedPerPt = Math.abs(this.lastAngle.doubleValue()); //
					double[] angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
					for(int i=0;i<100;i++)
					{
						if(Math.abs(angleDelta[0]) > 0.00001)
						{
							final TechComponent.ShipDir dir = angleDelta[0] < 0 ? ShipDir.PORT : ShipDir.STARBOARD;
							Double thrust = new Double(Math.abs(angleDelta[0]) / angleAchievedPerPt);
							if(isDebugging)
							{
								Log.debugOut("Delta0="+angleDelta[0]);
								Log.debugOut("Thrusting "+thrust+" to "+dir+" to achieve delta, and go from "+ship.facing()[0]+" to "+newFacing[0]);
							}
							msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
							this.lastAngle = null;
							this.trySendMsgToItem(M, engineE, msg);
							if(this.lastAngle==null)
								break;
						}
						else
							break;
						angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						if(isDebugging)
							Log.debugOut("* Total Deltas now: "+angleDelta[0]+" + "+angleDelta[1] +"=="+((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))));
						if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
							return true;
					}
					angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
					for(int i=0;i<100;i++)
					{
						if(Math.abs(angleDelta[1]) > 0.00001)
						{
							final TechComponent.ShipDir dir = angleDelta[1] < 0 ? ShipDir.VENTRAL : ShipDir.DORSEL;
							Double thrust = new Double(Math.abs(angleDelta[1]) / angleAchievedPerPt);
							if(isDebugging)
							{
								Log.debugOut("Delta1="+angleDelta[1]);
								Log.debugOut("Thrusting "+thrust+" to "+dir+" to achieve delta and go from "+ship.facing()[1]+" to "+newFacing[1]);
							}
							msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
							this.lastAngle = null;
							this.trySendMsgToItem(M, engineE, msg);
							if(this.lastAngle==null)
								break;
						}
						else
							break;
						angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						if(isDebugging)
							Log.debugOut("* Total Deltas now: "+angleDelta[0]+" + "+angleDelta[1] +"=="+((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))));
					}
					if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
						return true;
				}
			}
		}
		finally
		{
			M.destroy();
		}
		return false;
	}
	
	public ShipEngine primeMainThrusters(final SpaceShip ship)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final boolean isDocked = ship.getIsDocked()!=null;
		try
		{
			for(final ShipEngine engineE : engines)
			{
				if((CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.AFT))
				&&(engineE.getMaxThrust()>SpaceObject.ACCELLERATION_G)
				&&(engineE.getMinThrust()<SpaceObject.ACCELLERATION_PASSOUT))
				{
					int tries=100;
					double lastTryAmt=0.0001;
					final CMMsg deactMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
					msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					final double targetAccelleration = SpaceObject.ACCELLERATION_G;
					Double prevAccelleration = new Double(0.0);
					while(--tries>0)
					{
						this.lastAccelleration =null;
						final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(lastTryAmt));
						msg.setTargetMessage(code);
						this.trySendMsgToItem(M, engineE, msg);
						final Double thisLastAccel=this.lastAccelleration ;
						if(thisLastAccel!=null)
						{
							if((thisLastAccel.doubleValue() >= targetAccelleration)
							&&((!isDocked)||(ship.getIsDocked()==null)))
							{
								this.lastInject=new Double(lastTryAmt);
								return engineE;
							}
							else
							if((thisLastAccel.doubleValue()>0.0) && ((targetAccelleration/thisLastAccel.doubleValue())>100))
								lastTryAmt *= 2.0;
							else
							if(prevAccelleration.doubleValue() == thisLastAccel.doubleValue())
								break;
							else
							{
								this.trySendMsgToItem(M, engineE, deactMsg);
								lastTryAmt *= 1.1;
							}
							prevAccelleration = thisLastAccel;
						}
						else
							break;
					}
				}
			}
		}
		finally
		{
			M.destroy();
		}
		return null;
	}
	
	@Override
	public void onTyping(MOB mob, String message)
	{
		synchronized(this)
		{
			Electronics E  = null;
			final Vector<String> parsed=CMParms.parse(message);
			if(parsed.size()==0)
			{
				super.addScreenMessage(L("Error: No command.  Try HELP."));
				return;
			}
			final String uword=parsed.get(0).toUpperCase();
			if(uword.equalsIgnoreCase("HELP"))
			{
				if(parsed.size()==1)
				{
					super.addScreenMessage(L("^HHELP:^N\n\r^N"+"The ACTIVATE command can be used to turn on any engine, "
						+ "sensor, or other system in your ship.  The DEACTIVATE command will turn off any system specified. "
						+ "LAUNCH will take your ship off away from the planet. "
						+ "STOP will attempt to negate all velocity. "
						+ "LAND will land your ship on the nearest planet. "
						+ "INFO (SYSTEMNAME) will read in identifying info. "
						+ "Otherwise, see HELP ENGINE for engine commands, "
						+ "HELP WEAPON for weapon commands."));
					return;
				}
				String secondWord = CMParms.combine(parsed,1);
				if(secondWord.equalsIgnoreCase("ENGINE"))
				{
					super.addScreenMessage(
						L("^HHELP:^N\n\r"
						+"^H[ENGINE#/NAME] ([AFT/PORT/STARBOARD/DORSEL/VENTRAL]) [AMT]\n\r"
						+ "^N"+"The ENGINE command instructs the given " +
						"engine number or name to fire in the appropriate direction. What happens, " +
						"and how quickly, depends largely on the capabilities of the engine. " +
						"Giving a direction is optional, and if not given, AFT is assumed. All "+
						"directions result in corrected bursts, except for AFT, which will result " +
						"in sustained accelleration."));
					return;
				}
				else
				if(secondWord.equals("WEAPON"))
				{
					super.addScreenMessage(
						L("^HHELP:^N\n\r"
						+"^H[WEAPON#/NAME] ([TARGETNAME]) [AMT]\n\r"));
					return;
				}
				else
				{
					super.addScreenMessage(L("^HHELP:^N\n\r^N"+"No help on "+secondWord.toUpperCase()+" available."));
					return;
				}
			}
			else
			if(uword.equalsIgnoreCase("INFO"))
			{
				if(parsed.size()==1)
				{
					super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Please specify the system to query."));
					return;
				}
				String secondWord = CMParms.combine(parsed,1).toUpperCase();
				if(secondWord.startsWith("ENGINE"))
				{
					E=findEngineByName(secondWord);
					if(E==null)
					{
						super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Specified system not found.  No information available."));
						return;
					}
					final ShipEngine E1=(ShipEngine)E;
					final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
					final String activated = E.activated()?" (activated)":"";
					super.addScreenMessage(L("^HINFO:^N\n\r^N"
					+"System model: "+E.name()+activated+"\n\r"
					+"Manufacturer: "+E.getManufacturerName()+"\n\r"
					+"System type : "+E.getTechType().name()+"\n\r"
					+"Tech level  : "+marks[E.techLevel()%marks.length]+"\n\r"
					+"Power       : "+E.powerRemaining()+"/"+E.powerCapacity()+"\n\r"
					+(E.subjectToWearAndTear()
					?"Status      : "+E.usesRemaining()+"%\n\r":"")
					+"Efficiency  : "+Math.round(E1.getFuelEfficiency()*100)+"%\n\r"
					+"Min Thrust  : "+E1.getMinThrust()+"\n\r"
					+"Max Thrust  : "+E1.getMaxThrust()+"\n\r"
					+"Curr Thrust : "+(Math.round(E1.getThrust()*10000)/1000.0)+"\n\r"
					));
					return;
				}
				else
				if(secondWord.startsWith("SENSOR"))
				{
					E=this.findSensorByName(secondWord);
					if(E==null)
					{
						super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Specified system not found.  No information available."));
						return;
					}
					final String activated = E.activated()?" (activated)":"";
					final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
					super.addScreenMessage(L("^HINFO:^N\n\r^N"
					+"System model: "+E.name()+activated+"\n\r"
					+"Manufacturer: "+E.getManufacturerName()+"\n\r"
					+"System type : "+E.getTechType().name()+"\n\r"
					+"Tech level  : "+marks[E.techLevel()%marks.length]+"\n\r"
					+"Power       : "+E.powerRemaining()+"/"+E.powerCapacity()+"\n\r"
					+(E.subjectToWearAndTear()
					?"Status      : "+E.usesRemaining()+"%\n\r":"")
					));
					return;
				}
				else
				if(secondWord.startsWith("SYSTEM"))
				{
					final List<TechComponent> others = new ArrayList<TechComponent>();
					for(TechComponent component : getTechComponents())
					{
						if((!getEngines().contains(component))&&(!getShipSensors().contains(component)))
							others.add(component);
					}
					E=findComponentByName(others,"SYSTEM",secondWord);
					if(E==null)
					{
						super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Specified system not found.  No information available."));
						return;
					}
					final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
					final String activated = E.activated()?" (activated)":"";
					super.addScreenMessage(L("^HINFO:^N\n\r^N"
					+"System model: "+E.name()+activated+"\n\r"
					+"Manufacturer: "+E.getManufacturerName()+"\n\r"
					+"System type : "+E.getTechType().name()+"\n\r"
					+"Tech level  : "+marks[E.techLevel()%marks.length]+"\n\r"
					+"Power       : "+E.powerRemaining()+"/"+E.powerCapacity()+"\n\r"
					+(E.subjectToWearAndTear()
					?"Status      : "+E.usesRemaining()+"%\n\r":"")
					));
					return;
				}
				else
				{
					super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Specified system not found.  No information available."));
					return;
				}
			}
			CMMsg msg = null;
			if(uword.equalsIgnoreCase("ACTIVATE") || uword.equalsIgnoreCase("DEACTIVATE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				String code = null;
				E=findEngineByName(rest);
				if(E!=null)
					code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(.0000001));
				else
					E=findSensorByName(rest);
				if(E==null)
				{
					final List<TechComponent> others = new ArrayList<TechComponent>();
					for(TechComponent component : getTechComponents())
					{
						if((!getEngines().contains(component))&&(!getShipSensors().contains(component)))
							others.add(component);
					}
					E=findComponentByName(others,"SYSTEM",rest);
				}
				if(E!=null)
				{
					if(uword.equalsIgnoreCase("ACTIVATE"))
						msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					else
						msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
				}
			}
			else
			if(uword.equalsIgnoreCase("LAUNCH"))
			{
				final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(ship.getIsDocked() == null)
				{
					super.addScreenMessage(L("Error: Ship is already launched."));
					return;
				}
				if(this.rocketState!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.rocketState=null;
					this.programEngines=null;
				}
				this.programPlanet=CMLib.map().getSpaceObject(ship.getIsDocked(), true);
				ShipEngine engineE =this.primeMainThrusters(ship);
				if(engineE==null)
				{
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
					return;
				}
				this.programEngines=new XVector<ShipEngine>(engineE);
				this.rocketState = RocketShipProgram.RocketStateMachine.LAUNCHSEARCH;
				super.addScreenMessage(L("Launch procedure initialized."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("STOP"))
			{
				final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if((ship.getIsDocked() != null)||(ship.speed()==0.0))
				{
					super.addScreenMessage(L("Error: Ship is already stopped."));
					return;
				}
				if(this.rocketState!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.rocketState=null;
					this.programEngines=null;
				}
				ShipEngine engineE=null;
				if(!flipForAllStop(ship))
				{
					super.addScreenMessage(L("Warning. Stop program cancelled due to engine failure."));
					this.rocketState=null;
					this.programEngines=null;
					return;
				}
				else
				{
					engineE=this.primeMainThrusters(ship);
				}
				if(engineE==null)
				{
					this.rocketState=null;
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				this.programEngines=new XVector<ShipEngine>(engineE);
				this.rocketState = RocketShipProgram.RocketStateMachine.STOP;
				super.addScreenMessage(L("All Stop procedure initialized."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("LAND"))
			{
				final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(ship.getIsDocked() != null)
				{
					super.addScreenMessage(L("Error: Ship is already landed."));
					return;
				}
				if(sensorReports.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify landing position."));
					return;
				}
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeSensorReport(sensor));
				final WorldMap map=CMLib.map();
				Collections.sort(allObjects, new Comparator<SpaceObject>()
				{
					@Override
					public int compare(SpaceObject o1, SpaceObject o2)
					{
						if(o1 == null)
							return (o2 == null) ? 0 : 1;
						if(o2 == null)
							return -1;
						if(o1.coordinates() == null)
							return (o2.coordinates() == null) ? 0 : 1;
						if(o2.coordinates() == null)
							return -1;
						final long distance1 = map.getDistanceFrom(o1, spaceObject);
						final long distance2 = map.getDistanceFrom(o2, spaceObject);
						if(distance1 < distance2)
							return -1;
						if(distance1 > distance2)
							return 1;
						return 0;
					}
				});
				SpaceObject landingPlanet = null;
				for(SpaceObject O : allObjects)
				{
					if((O.coordinates()!=null)&&(O.radius()!=0))
					{
						List<LocationRoom> rooms=CMLib.map().getLandingPoints(ship, O);
						if(rooms.size()>0)
						{
							landingPlanet=O;
							break;
						}
					}
				}
				if(landingPlanet == null)
				{
					for(SpaceObject O : allObjects)
					{
						if((O.coordinates()!=null)&&(O.radius()!=0))
						{
							if(O.getMass() > SpaceObject.MOONLET_MASS)
							{
								landingPlanet=O;
								break;
							}
						}
					}
				}
				
				if(landingPlanet == null)
				{
					super.addScreenMessage(L("No suitable landing target found within near sensor range."));
					return;
				}
				
				if(this.rocketState!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.rocketState=null;
					this.programEngines=null;
				}
				ShipEngine engineE=null;
				if(!flipForAllStop(ship))
				{
					super.addScreenMessage(L("Warning. Landing program cancelled due to engine failure."));
					this.rocketState=null;
					this.programEngines=null;
					return;
				}
				else
				{
					engineE=this.primeMainThrusters(ship);
				}
				if(engineE==null)
				{
					this.rocketState=null;
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				this.programEngines=new XVector<ShipEngine>(engineE);
				// this lands you at the nearest point, which will pick the nearest location room, if any
				//TODO: picking the nearest landing zone, orbiting to it, and THEN landing would be better.
				this.rocketState = RocketShipProgram.RocketStateMachine.PRE_LANDING_STOP;
				final long distance=CMLib.map().getDistanceFrom(ship.coordinates(),landingPlanet.coordinates());
				if(distance > (ship.radius() + Math.round(landingPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					super.addScreenMessage(L("Landing approach procedure initialized."));
				else
					super.addScreenMessage(L("Landing procedure initialized."));
				return;
			}
			else
			if(!uword.equalsIgnoreCase("HELP"))
			{
				ShipEngine engineE=findEngineByName(uword);
				if(engineE==null)
				{
					super.addScreenMessage(L("Error: Unknown engine name or command word '"+uword+"'.   Try HELP."));
					return;
				}
				E=engineE;
				double amount=0;
				TechComponent.ShipDir portDir=TechComponent.ShipDir.AFT;
				if(parsed.size()>3)
				{
					super.addScreenMessage(L("Error: Too many parameters."));
					return;
				}
				if(parsed.size()==1)
				{
					super.addScreenMessage(L("Error: No thrust amount given."));
					return;
				}
				if(!CMath.isNumber(parsed.get(parsed.size()-1)))
				{
					super.addScreenMessage(L("Error: '@x1' is not a valid amount.",parsed.get(parsed.size()-1)));
					return;
				}
				amount=CMath.s_double(parsed.get(parsed.size()-1));
				if(parsed.size()==3)
				{
					portDir=(TechComponent.ShipDir)CMath.s_valueOf(TechComponent.ShipDir.class, parsed.get(1).toUpperCase().trim());
					if(portDir!=null) 
					{ 
						if(!CMParms.contains(engineE.getAvailPorts(), portDir))
						{
							super.addScreenMessage(L("Error: '@x1' is not a valid direction for that engine.  Try: @x2.",parsed.get(1),CMParms.toListString(engineE.getAvailPorts())));
							return;
						}
					}
					else
					if("aft".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), TechComponent.ShipDir.AFT))
						portDir=TechComponent.ShipDir.AFT;
					else
					if("port".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), TechComponent.ShipDir.PORT))
						portDir=TechComponent.ShipDir.PORT;
					else
					if("starboard".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), TechComponent.ShipDir.STARBOARD))
						portDir=TechComponent.ShipDir.STARBOARD;
					else
					if("ventral".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), TechComponent.ShipDir.VENTRAL))
						portDir=TechComponent.ShipDir.VENTRAL;
					else
					if("dorsel".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), TechComponent.ShipDir.DORSEL))
						portDir=TechComponent.ShipDir.DORSEL;
					else
					{
						super.addScreenMessage(L("Error: '@x1' is not a valid direction for that engine.  Try: @x2.",parsed.get(1),CMParms.toListString(engineE.getAvailPorts())));
						return;
					}
				}
				if(amount > 0)
				{
					final String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(amount));
					msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				}
				else
					msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
			}
			if((E!=null) && (msg != null))
			{
				if(E.owner() instanceof Room)
				{
					if(((Room)E.owner()).okMessage(mob, msg))
						((Room)E.owner()).send(mob, msg);
				}
				else
				if(E.okMessage(mob, msg))
					E.executeMsg(mob, msg);
			}
			else
			{
				super.addScreenMessage(L("Error: Unknown command '"+message+"'.   Try HELP."));
			}
		}
	}

	@Override
	public void onActivate(MOB mob, String message)
	{
		onTyping(mob,message);
	}

	@Override
	public void onDeactivate(MOB mob, String message)
	{
		final Vector<String> parsed=CMParms.parse(message);
		if(parsed.size()==0)
		{
			super.addScreenMessage(L("Syntax Error!"));
			return;
		}
		String uword=parsed.get(0).toUpperCase();
		ShipEngine E=findEngineByName(uword);
		if(E!=null)
		{
			onTyping(mob,"\""+uword+"\" "+0);
			return;
		}
		uword=message.toUpperCase();
		E=findEngineByName(uword);
		if(E==null)
		{
			super.addScreenMessage(L("Unknown engine '@x1'!",uword));
			return;
		}
		final CMMsg msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
		if(E.owner() instanceof Room)
		{
			if(((Room)E.owner()).okMessage(mob, msg))
				((Room)E.owner()).send(mob, msg);
		}
		else
		if(E.okMessage(mob, msg))
			E.executeMsg(mob, msg);
		return;
	}

	@Override
	public void onPowerCurrent(int value)
	{
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			engines = null;
			nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_INSTALL:
				engines=null;
				break;
			case CMMsg.TYP_ACTIVATE:
			{
				if(msg.isTarget(CMMsg.MASK_CNTRLMSG) && (msg.targetMessage()!=null))
				{
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(parts);
					if((command == TechCommand.SENSE) 
					&& (msg.tool() instanceof SpaceObject)) // this is a sensor report
					{
						this.sensorReport.add((SpaceObject)msg.tool());
						return;
					}
				}
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
			{
				break;
			}
			}
		}
		else
		if((msg.target() instanceof SpaceShip)
		&&(msg.targetMinor()==CMMsg.TYP_ACTIVATE)
		&&(msg.isTarget(CMMsg.MASK_CNTRLMSG))
		&&(msg.targetMessage()!=null))
		{
			final String[] parts=msg.targetMessage().split(" ");
			final TechCommand command=TechCommand.findCommand(parts);
			if(command == TechCommand.ACCELLERATED)
			{
				final Object[] parms=command.confirmAndTranslate(parts);
				if(parms != null)
				{
					switch((ShipDir)parms[0])
					{
					case AFT:
					case FORWARD:
						if(this.lastAccelleration==null)
							this.lastAccelleration =(Double)parms[1];
						break;
					default:
						if(lastAngle==null)
							this.lastAngle =(Double)parms[1];
						break;
					}
				}
			}
		}

		if((container() instanceof Computer)
		&&(msg.target() == container())
		&&(msg.targetMinor() == CMMsg.TYP_DEACTIVATE))
		{
			this.components = null;
			this.engines = null;
			this.sensors = null;
			this.sensorReport.clear();
			this.sensorReports.clear();
		}
		super.executeMsg(host,msg);
	}
}
