package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
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
	protected final List<CMObject>			sensorReport		= new LinkedList<CMObject>();
	
	protected enum RocketStateMachine
	{
		LAUNCHSEARCH,
		LAUNCHCHECK,
		LAUNCHCRUISE,
		STOP
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
				final List<CMObject> localSensorReport;
				synchronized(sensorReport)
				{
					sensorReport.clear();
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
					localSensorReport = new SLinkedList<CMObject>(sensorReport.iterator());
					sensorReport.clear();
				}
				
				if(localSensorReport.size()==0)
					str.append("^R").append(L("No Report"));
				else
				for(CMObject o : localSensorReport)
				{
					if(o == spaceObject)
						continue;
					if(o instanceof SpaceObject)
					{
						SpaceObject O=(SpaceObject)o;
						if(O.displayText().length()>0)
							str.append("^W").append(L("Found: ")).append("^N").append(O.displayText());
						else
							str.append("^W").append(L("Found: ")).append("^N").append(o.name());
					}
					else
						str.append("^W").append(L("Found: ")).append("^N").append(o.name());
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
			str.append("^H").append(CMStrings.padRight(L("[HELP] : Get help. [INFO] [SYSTEMNAME] : Get Details"),60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight(L("[ENGINE#/NAME] ([AFT/PORT/STARBOARD/DORSEL/VENTRAL]) [AMT]"),60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight(L("[WEAPON#/NAME] ([TARGETNAME]) [AMT]"),60)).append("\n\r");
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
	
	@Override
	public boolean checkPowerCurrent(final int value)
	{
		final RocketShipProgram.RocketStateMachine state=this.rocketState;
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
		case STOP:
		{
			if(ship.speed() == 0.0)
			{
				this.rocketState=null;
				this.programEngines=null;
				this.lastInject=null;
				super.addScreenMessage(L("Stop program completed successfully."));
				return super.checkPowerCurrent(value);
			}
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
			}
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHCRUISE:
		case LAUNCHSEARCH:
		{
			if(programPlanet==null)
			{
				String reason = (programPlanet==null)?"no planetary information":"";
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
					final MOB mob=CMClass.getFactoryMOB();
					try
					{
						for(final ShipEngine engineE : programEngines)
						{
							CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(0.0));
							msg.setTargetMessage(code);
							this.trySendMsgToItem(mob, engineE, msg);
						}
					}
					finally
					{
						mob.destroy();
					}
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
				if(this.lastAccelleration .doubleValue() < (targetAccelleration * 0.9))
					newInject = new Double(1.07 * newInject.doubleValue());
				else
				if(this.lastAccelleration .doubleValue() > (targetAccelleration * 1.1))
					newInject = new Double(0.93 * newInject.doubleValue());
				else
				if(this.lastAccelleration .doubleValue() < targetAccelleration)
					newInject = new Double(1.0001 * newInject.doubleValue());
				else
				if(this.lastAccelleration .doubleValue() > targetAccelleration)
					newInject = new Double(0.999 * newInject.doubleValue());
			}
			final MOB mob=CMClass.getFactoryMOB();
			try
			{
				this.lastAccelleration =null;
				if(newInject != null)
				{
					for(final ShipEngine engineE : programEngines)
					{
						if((newInject != this.lastInject)||(!engineE.isConstantThruster()))
						{
							CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(newInject.doubleValue()));
							msg.setTargetMessage(code);
							this.trySendMsgToItem(mob, engineE, msg);
							this.lastInject=newInject;
						}
					}
				}
			}
			finally
			{
				mob.destroy();
			}
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHSEARCH:
		{
			//force/mass is the Gs felt by the occupants.. not force-mass
			//so go ahead and push it up to 3 * g forces on ship
			final double targetAccelleration = SpaceObject.ACCELLERATION_TYPICALSPACEROCKET; // 
			if((this.lastAccelleration !=null)
			&&(newInject != null)
			&& (targetAccelleration != 0.0))
			{
				if(this.lastAccelleration .doubleValue() < (targetAccelleration * 0.9))
					newInject = new Double(1.07 * newInject.doubleValue());
				else
				if(this.lastAccelleration .doubleValue() > (targetAccelleration * 1.1))
					newInject = new Double(0.93 * newInject.doubleValue());
			}
		}
		//$FALL-THROUGH$
		case LAUNCHCRUISE:
		{
			final MOB mob=CMClass.getFactoryMOB();
			try
			{
				this.lastAccelleration =null;
				if(newInject != null)
				{
					for(final ShipEngine engineE : programEngines)
					{
						if((newInject != this.lastInject)||(!engineE.isConstantThruster()))
						{
							CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(newInject.doubleValue()));
							msg.setTargetMessage(code);
							this.trySendMsgToItem(mob, engineE, msg);
							this.lastInject=newInject;
						}
					}
				}
			}
			finally
			{
				mob.destroy();
			}
			break;
		}
		default:
			break;
		}
		
		return true;
	}

	public boolean flipForAllStop(final SpaceShip ship)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		try
		{
			// step one, face opposite direction of motion
			final double[] stopFacing = CMLib.map().getOppositeDir(ship.direction());
			int tries=100;
			while(!Arrays.equals(stopFacing, ship.facing()) && (--tries>0))
			{
				final double[] oldFacing = Arrays.copyOf(ship.facing(), ship.facing().length);
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
						double angleAchievedPerPt = Math.abs(this.lastAngle.doubleValue()); //
						double[] angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
						if(Math.abs(angleDelta[0]) > 0.0000001)
						{
							final TechComponent.ShipDir dir = angleDelta[0] < 0 ? ShipDir.STARBOARD : ShipDir.PORT;
							Double thrust = new Double(Math.abs(angleDelta[0])/angleAchievedPerPt);
System.out.println("Thrusting "+thrust+" to "+dir+" to achieve "+angleDelta[0]+" by going from "+oldFacing[0]+" to "+stopFacing[0]);
							msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
							this.trySendMsgToItem(M, engineE, msg);
						}
						angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
						if(Math.abs(angleDelta[1]) > 0.0000001)
						{
							final TechComponent.ShipDir dir = angleDelta[1] < 0 ? ShipDir.VENTRAL : ShipDir.DORSEL;
							Double thrust = new Double(Math.abs(angleDelta[1])/angleAchievedPerPt);
System.out.println("Thrusting "+thrust+" to "+dir+" to achieve "+angleDelta[0]+" by going from "+oldFacing[1]+" to "+stopFacing[1]);
							msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
							this.trySendMsgToItem(M, engineE, msg);
						}
						angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
System.out.println("* Total Deltas now: "+angleDelta[0]+" + "+angleDelta[1] +"=="+(Math.abs(angleDelta[0])+Math.abs(angleDelta[1])));
						if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
							return true;
					}
				}
				if(Arrays.equals(oldFacing, ship.facing()))
					return false;
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
					System.out.println("WHOA!"+this.lastAccelleration);
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
						+ "Otherwise, see HELP ENGINE for engine commands."));
					return;
				}
				String secondWord = CMParms.combine(parsed,1);
				if(secondWord.equalsIgnoreCase("ENGINE"))
				{
					super.addScreenMessage(L("^HHELP:^N\n\r^N"+"The ENGINE command instructs the given " +
							"engine number or name to fire in the appropriate direction. What happens, " +
							"and how quickly, depends largely on the capabilities of the engine. " +
							"Giving a direction is optional, and if not given, AFT is assumed. All "+
							"directions result in corrected bursts, except for AFT, which will result " +
							"in sustained accelleration."));
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
				String secondWord = CMParms.combine(parsed,1);
				if(secondWord.startsWith("ENGINE"))
				{
					
				}
				else
				if(secondWord.startsWith("SYSTEM"))
				{
					
				}
				
			}
			CMMsg msg = null;
			Electronics E  = null;
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
				//TODO:
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
					if((command == TechCommand.SENSE) && (msg.tool() != null)) // this is a sensor report
					{
						this.sensorReport.add(msg.tool());
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
		}
		super.executeMsg(host,msg);
	}
}
