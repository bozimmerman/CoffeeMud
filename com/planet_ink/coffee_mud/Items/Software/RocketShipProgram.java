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
   Copyright 2013-2020 Bo Zimmerman

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
	protected volatile List<TechComponent>	weapons		= null;
	protected volatile List<TechComponent>	components	= null;
	protected volatile List<TechComponent>	dampers		= null;

	protected volatile Double				lastAcceleration	= null;
	protected volatile Double				lastAngle			= null;
	protected volatile Double				lastInject			= null;
	protected volatile Double				targetAcceleration	= Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
	protected volatile RocketStateMachine	rocketState			= null;
	protected volatile SpaceObject			currentTarget		= null;
	protected volatile SpaceObject			programPlanet		= null;
	protected volatile List<ShipEngine>		programEngines		= null;
	protected final	   List<SpaceObject>	sensorReport		= new LinkedList<SpaceObject>();

	protected final PairSLinkedList<Long, List<SpaceObject>>	sensorReports		= new PairSLinkedList<Long, List<SpaceObject>>();
	protected volatile Map<ShipEngine, Double[]>				primeInjects		= new Hashtable<ShipEngine, Double[]>();

	protected enum RocketStateMachine
	{
		LAUNCHSEARCH,
		LAUNCHCHECK,
		LAUNCHCRUISE,
		STOP,
		PRE_LANDING_STOP,
		LANDING_APPROACH,
		LANDING,
		ORBITSEARCH,
		ORBITCHECK,
		ORBITCRUISE
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

	private static class DistanceSorter implements Comparator<SpaceObject>
	{
		private final WorldMap map;
		private final SpaceObject spaceObject;

		private DistanceSorter(final SpaceObject me)
		{
			map=CMLib.map();
			spaceObject=me;
		}

		@Override
		public int compare(final SpaceObject o1, final SpaceObject o2)
		{
			if(o1 == null)
				return (o2 == null) ? 0 : 1;
			if(o2 == null)
				return -1;
			if(o1.coordinates() == null)
				return (o2.coordinates() == null) ? 0 : 1;
			if(o2.coordinates() == null)
				return -1;
			final long distance1 = map.getDistanceFrom(spaceObject, o1) - o1.radius();
			final long distance2 = map.getDistanceFrom(spaceObject, o2) - o2.radius();
			if(distance1 < distance2)
				return -1;
			if(distance1 > distance2)
				return 1;
			return 0;
		}
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

	protected synchronized List<TechComponent> getShipWeapons()
	{
		if(weapons == null)
		{
			final List<TechComponent> stuff=getTechComponents();
			weapons=new Vector<TechComponent>(1);
			for(final TechComponent E : stuff)
			{
				if(E.getTechType()==TechType.SHIP_WEAPON)
					weapons.add(E);
			}
		}
		return weapons;
	}

	protected synchronized List<TechComponent> getDampeners()
	{
		if(dampers == null)
		{
			final List<TechComponent> stuff=getTechComponents();
			dampers=new Vector<TechComponent>(1);
			for(final TechComponent E : stuff)
			{
				if(E.getTechType()==TechType.SHIP_DAMPENER)
					dampers.add(E);
			}
		}
		return dampers;
	}

	@Override
	public boolean isActivationString(final String word)
	{
		return isCommandString(word, false);
	}

	@Override
	public boolean isDeActivationString(final String word)
	{
		return isCommandString(word, false);
	}

	protected TechComponent findComponentByName(final List<? extends TechComponent> list, final String prefix, String name)
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

	protected ShipEngine findEngineByName(final String name)
	{
		return (ShipEngine)findComponentByName(getEngines(), "ENGINE", name);
	}

	protected TechComponent findSensorByName(final String name)
	{
		return findComponentByName(getShipSensors(), "SENSOR", name);
	}

	protected ShipWarComponent findWeaponByName(final String name)
	{
		return (ShipWarComponent)findComponentByName(getShipWeapons(), "WEAPON", name);
	}

	protected ShipWarComponent findShieldByName(final String name)
	{
		return (ShipWarComponent)findComponentByName(getEngines(), "SHIELD", name);
	}

	@Override
	public boolean isCommandString(final String word, final boolean isActive)
	{
		final Vector<String> parsed=CMParms.parse(word);
		if(parsed.size()==0)
			return false;
		final String uword=parsed.get(0).toUpperCase();
		if(uword.equals("HELP")
		||uword.equals("STOP")
		||uword.equals("LAND")
		||uword.equals("LAUNCH")
		||uword.equals("ACTIVATE")
		||uword.equals("DEACTIVATE")
		||uword.equals("TARGET")
		||uword.equals("FIRE")
		||(uword.startsWith("WEAPON")&&(CMath.isInteger(uword.substring(6))))
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
			this.sensorReports.addFirst(new Pair<Long,List<SpaceObject>>(Long.valueOf(System.currentTimeMillis()),localSensorReport));
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
			str.append("^N").append(CMStrings.padRight(displayPerSec(Math.round(ship.speed())),25));
			str.append("^H").append(CMStrings.padRight(L("Direction"),10));
			final String dirStr=display(ship.direction());
			str.append("^N").append(CMStrings.padRight(dirStr,15));
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
				str.append("^N").append(CMStrings.padRight(display(CMLib.map().getDistanceFrom(shipSpaceObject, altitudePlanet)-shipSpaceObject.radius()-altitudePlanet.radius()),25));
			}
			else
			{
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Sector"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.map().getSectorName(ship.coordinates()),50));
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Location"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.english().coordDescShort(CMLib.map().getInSectorCoords(shipSpaceObject.coordinates())),25));
			}
			str.append("^H").append(CMStrings.padRight(L("Facing"),10));
			final String facStr=display(ship.facing());
			str.append("^N").append(CMStrings.padRight(facStr,15));
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
				str.append(CMStrings.padRight(sensor.activated()?L("^gA"):L("^rI"),2));
				str.append("^H").append(CMStrings.padRight(L("Pow."),5));
				str.append("^N").append(CMStrings.padRight(Long.toString(sensor.powerRemaining()),11));
				str.append("^H").append(CMStrings.padRight(sensor.Name(),31));
				str.append("^.^N\n\r");
				if(sensor.activated())
				{
					final List<SpaceObject> localSensorReport=takeSensorReport(sensor);
					if(localSensorReport.size()==0)
						str.append("^R").append(L("No Report"));
					else
					for(final Object o : localSensorReport)
					{
						if(o == spaceObject)
							continue;
						if(o instanceof SpaceObject)
						{
							final SpaceObject spaceMe = ship;
							final SpaceObject obj = (SpaceObject)o;
							final long distance = CMLib.map().getDistanceFrom(spaceMe.coordinates(), obj.coordinates()) - spaceMe.radius() - obj.radius();
							final double[] direction = CMLib.map().getDirection(spaceMe, obj);
							final String mass = CMath.abbreviateLong(obj.getMass());
							final String dirStr = CMLib.english().directionDescShortest(direction);
							final String distStr = CMLib.english().distanceDescShort(distance);
							str.append("^W").append(obj.name()).append("^N/^WMass: ^N"+mass+"/^WDir: ^N"+dirStr+"/^WDist: ^N"+distStr);
						}
						else
						if(o instanceof CMObject)
							str.append("^W").append(L("Found: ")).append("^N").append(((CMObject)o).name());
						else
						if(o instanceof String)
							str.append("^W").append(L("Found: ")).append("^N").append(o.toString());
						str.append("^.^N\n\r");
					}
				}
				str.append("^.^N\n\r");
				sensorNumber++;
			}
		}

		final List<ShipEngine> engines = getEngines();
		final List<TechComponent> weapons = getShipWeapons();
		final List<TechComponent> components = getTechComponents();
		if(components.size()> engines.size() + sensors.size())
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Ship Systems -- "),60)).append("^.^N\n\r");
			int engineNumber=1;
			for(final ShipEngine engine : engines)
			{
				str.append("^H").append(CMStrings.padRight(L("ENGINE@x1",""+engineNumber),9));
				str.append(CMStrings.padRight(engine.activated()?L("^gA"):L("^rI"),2));
				if(engine instanceof FuelConsumer)
				{
					str.append("^H").append(CMStrings.padRight(L("Fuel"),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(((FuelConsumer)engine).getFuelRemaining()),11));
				}
				else
				{
					str.append("^H").append(CMStrings.padRight(L("Pow."),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(engine.powerRemaining()),11));
				}
				str.append("^H").append(CMStrings.padRight(engine.Name(),31));
				str.append("^.^N\n\r");
				engineNumber++;
			}
			int weaponNumber=1;
			for(final TechComponent weapon : weapons)
			{
				str.append("^H").append(CMStrings.padRight(L("WEAPON@x1",""+weaponNumber),9));
				str.append(CMStrings.padRight(weapon.activated()?L("^gA"):L("^rI"),2));
				str.append("^H").append(CMStrings.padRight(L("Pow."),5));
				str.append("^N").append(CMStrings.padRight(Long.toString(weapon.powerRemaining()),11));
				str.append("^H").append(CMStrings.padRight(weapon.Name(),31));
				str.append("^.^N\n\r");
				weaponNumber++;
			}
			int systemNumber=1;
			for(final TechComponent component : components)
			{
				if((!engines.contains(component))
				&&(!sensors.contains(component))
				&&(!weapons.contains(component)))
				{
					str.append("^H").append(CMStrings.padRight(L("SYSTEM@x1",""+systemNumber),9));
					str.append(CMStrings.padRight(component.activated()?L("^gA"):L("^rI"),2));
					str.append("^H").append(CMStrings.padRight(L("Pow."),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(component.powerRemaining()),11));
					str.append("^H").append(CMStrings.padRight(component.Name(),31));
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
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Commands -- "),60)).append("^.^N\n\r");
			str.append("^H").append(CMStrings.padRight(L("TYPE HELP INTO CONSOLE : Get help."),60)).append("\n\r");
			if((container() instanceof Rideable)
			&&(((Rideable)container()).rideBasis()==Rideable.RIDEABLE_TABLE)
			&&(((Rideable)container()).numRiders()==0))
				str.append("^H").append(CMStrings.padRight(L("* Sit at "+container().name()+" to shorten commands *"),60)).append("\n\r");
			str.append("^X").append(CMStrings.centerPreserve("",60)).append("^.^N\n\r");
			str.append("^N\n\r");
		}
		return str.toString();
	}

	@Override
	public boolean checkActivate(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	public boolean checkDeactivate(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	public boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	protected void trySendMsgToItem(final MOB mob, final Item engineE, final CMMsg msg)
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

	protected Double fixInjection(final Double lastInject, final Double lastAcceleration, final double targetAcceleration)
	{
		final Double newInject;
		if(lastAcceleration.doubleValue() < targetAcceleration)
		{
			if(lastAcceleration.doubleValue() < (targetAcceleration * .00001))
				newInject = Double.valueOf(lastInject.doubleValue()*200.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .001))
				newInject = Double.valueOf(lastInject.doubleValue()*20.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .1))
				newInject = Double.valueOf(lastInject.doubleValue()*2.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .5))
				newInject = Double.valueOf(lastInject.doubleValue()*1.25);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * 0.9))
				newInject = Double.valueOf(1.07 * lastInject.doubleValue());
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * 0.95))
				newInject = Double.valueOf(1.02 * lastInject.doubleValue());
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * 0.99))
				newInject = Double.valueOf(1.01 * lastInject.doubleValue());
			else
				newInject = Double.valueOf(1.001 * lastInject.doubleValue());
		}
		else
		if(lastAcceleration.doubleValue() > targetAcceleration)
		{
			if(lastAcceleration.doubleValue() > (targetAcceleration * 1000000))
				newInject = Double.valueOf(lastInject.doubleValue()/200.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 10000))
				newInject = Double.valueOf(lastInject.doubleValue()/20.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 100))
				newInject = Double.valueOf(lastInject.doubleValue()/2.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 2))
				newInject = Double.valueOf(lastInject.doubleValue()/1.25);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 1.1))
				newInject = Double.valueOf(0.93 * lastInject.doubleValue());
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 1.05))
				newInject = Double.valueOf(0.98 * lastInject.doubleValue());
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 1.01))
				newInject = Double.valueOf(0.99 * lastInject.doubleValue());
			else
				newInject = Double.valueOf(0.999 * lastInject.doubleValue());
		}
		else
			newInject=lastInject;
		return newInject;
	}

	protected Double calculateMarginalTargetInjection(Double newInject, final double targetAcceleration)
	{
		//force/mass is the Gs felt by the occupants.. not force-mass
		//so go ahead and push it up to 3 * g forces on ship
		if((this.lastAcceleration !=null)
		&&(newInject != null)
		&& (targetAcceleration != 0.0))
			newInject=fixInjection(newInject,this.lastAcceleration,targetAcceleration);
		return newInject;
	}

	protected Double forceAccelerationAllProgramEngines(final double targetAcceleration)
	{
		Double newInject = this.calculateMarginalTargetInjection(this.lastInject, targetAcceleration);
		int tries=100;
		do
		{
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, true);
			if((CMath.abs(targetAcceleration)-this.lastAcceleration.doubleValue())<.01)
				break;
			newInject = this.calculateMarginalTargetInjection(this.lastInject, targetAcceleration);
		}
		while((--tries)>0);
		return newInject;
	}

	protected void performSimpleThrust(final ShipEngine engineE, final Double thrustInject, final boolean alwaysThrust)
	{
		final MOB mob=CMClass.getFactoryMOB();
		try
		{
			this.lastAcceleration =null;
			if(thrustInject != null)
			{
				if((thrustInject != this.lastInject)
				||(!engineE.isConstantThruster())
				||((thrustInject.doubleValue()>0.0)&&(engineE.getThrust()==0.0)))
				{
					final CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
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
		if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			Log.debugOut("Program state: "+state.toString());
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
				final String reason = "no planetary information";
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
				if(distance < (ship.radius() + Math.round(programPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
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
				if(state == RocketStateMachine.STOP)
				{
					ship.setSpeed(0.0); // that's good enough, for now.
					for(final ShipEngine engineE : programEngines)
						performSimpleThrust(engineE,Double.valueOf(0.0), true);
					this.rocketState=null;
					this.programEngines=null;
					this.lastInject=null;
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
				final double[] angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
				if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))>.02)
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
						if(ship.speed() < targetAcceleration.doubleValue())
							this.lastInject = Double.valueOf(this.lastInject.doubleValue()/2.0);
						else
						if(ship.speed() < (targetAcceleration.doubleValue() * 2))
							this.lastInject = Double.valueOf(this.lastInject.doubleValue()/1.5);
					}
				}
			}
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHCRUISE:
		case LAUNCHSEARCH:
		case ORBITSEARCH:
		case ORBITCHECK:
		case ORBITCRUISE:
		{
			if(programPlanet==null)
			{
				final String reason = "no planetary information";
				this.rocketState = null;
				this.programEngines = null;
				this.lastInject = null;
				super.addScreenMessage(L("Launch program aborted with error ("+reason+")."));
				return super.checkPowerCurrent(value);
			}
			else
			{
				final long distance=CMLib.map().getDistanceFrom(ship, programPlanet);
				if(distance > (programPlanet.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					this.lastInject = null;
					if((this.rocketState == RocketStateMachine.ORBITCHECK)
					||(this.rocketState == RocketStateMachine.ORBITSEARCH)
					||(this.rocketState == RocketStateMachine.ORBITCRUISE))
					{
						super.addScreenMessage(L("Launch program completed. Neutralizing velocity."));
						this.rocketState = RocketShipProgram.RocketStateMachine.STOP;
					}
					else
					{
						super.addScreenMessage(L("Launch program completed. Shutting down thrust."));
						this.rocketState = null;
						for(final ShipEngine engineE : programEngines)
							performSimpleThrust(engineE,Double.valueOf(0.0), true);
						this.programEngines = null;
					}
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
			double targetAcceleration = this.targetAcceleration.doubleValue(); //
			if(targetAcceleration > ship.speed())
				targetAcceleration = ship.speed();
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
			break;
		}
		case LAUNCHCHECK:
		case LAUNCHSEARCH:
		case ORBITSEARCH:
		case ORBITCHECK:
		{
			final double targetAcceleration = this.targetAcceleration.doubleValue(); //
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
		}
		//$FALL-THROUGH$
		case LAUNCHCRUISE:
		case ORBITCRUISE:
		{
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
			break;
		}
		case LANDING_APPROACH:
		{
			final double[] dirToPlanet = CMLib.map().getDirection(ship, programPlanet);
			//final long distance=CMLib.map().getDistanceFrom(ship, programPlanet)
			//		- Math.round(CMath.mul(programPlanet.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
			//		- ship.radius();
			final double atmoWidth = CMath.mul(programPlanet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - programPlanet.radius();
			final long critRadius = Math.round(programPlanet.radius() + (atmoWidth / 2.0));
			final long distanceToCritRadius=CMLib.map().getDistanceFrom(ship, programPlanet)
					- critRadius
					- ship.radius();
			if(distanceToCritRadius <= 0)
				this.rocketState = RocketStateMachine.LANDING;
			else
			{
				//final double angleDiff = CMLib.map().getAngleDelta(ship.direction(), dirToPlanet);
				for(final ShipEngine engineE : programEngines)
				{
					final double ticksToDecellerate = CMath.div(ship.speed(),CMath.div(this.targetAcceleration.doubleValue(),2.0));
					final double ticksToDestinationAtCurrentSpeed = CMath.div(distanceToCritRadius, ship.speed());
					final double diff = Math.abs(ticksToDecellerate-ticksToDestinationAtCurrentSpeed);
					if((diff < 1) || (diff < Math.sqrt(ticksToDecellerate)))
					{
						//System.out.println("** Coast: "+ticksToDecellerate+"/"+ticksToDestinationAtCurrentSpeed+"                    /"+ship.speed()+"/"+this.lastInject); //BZ:COMMENTMEOUT
						final Double oldInject=this.lastInject;
						final Double oldAccel=this.lastAcceleration;
						performSimpleThrust(engineE,Double.valueOf(0.0), false);
						this.lastInject=oldInject;
						this.lastAcceleration=oldAccel;
						break;
					}
					else
					if(ticksToDecellerate > ticksToDestinationAtCurrentSpeed)
					{
						//System.out.println("** Decelerate: "+ticksToDecellerate+"/"+ticksToDestinationAtCurrentSpeed+"                    /"+ship.speed()); //BZ:COMMENTMEOUT
						this.changeFacing(ship, CMLib.map().getOppositeDir(dirToPlanet));
					}
					else
					if((ticksToDecellerate<50)||(diff > 10.0))
					{
						//System.out.println("** Accelerate: "+ticksToDecellerate+"/"+ticksToDestinationAtCurrentSpeed+"                    /"+ship.speed()); //BZ:COMMENTMEOUT
						this.changeFacing(ship, dirToPlanet);
					}
					final double targetAcceleration = this.targetAcceleration.doubleValue(); //
					newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
					if((targetAcceleration > 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship);
						newInject = forceAccelerationAllProgramEngines(targetAcceleration);
					}
					performSimpleThrust(engineE,newInject, false);
				}
				break;
			}
		}
		//$FALL-THROUGH$
		case LANDING:
		{
			final double[] dirToPlanet = CMLib.map().getDirection(ship, programPlanet);
			if(CMLib.map().getAngleDelta(dirToPlanet, ship.direction()) > 1)
			{
				this.changeFacing(ship, dirToPlanet);
				if(ship.speed() > this.targetAcceleration.doubleValue())
					newInject=calculateMarginalTargetInjection(this.lastInject, this.targetAcceleration.doubleValue());
				else
				if(ship.speed() > 1)
					newInject=calculateMarginalTargetInjection(this.lastInject, ship.speed() / 2);
				else
					newInject=calculateMarginalTargetInjection(this.lastInject, 1);
			}
			else
			{
				final long distance=CMLib.map().getDistanceFrom(ship, programPlanet)
						- programPlanet.radius()
						- ship.radius()
						-10; // margin for soft landing
				final double atmoWidth = CMath.mul(programPlanet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - programPlanet.radius();
				final long critRadius = Math.round(programPlanet.radius() + (atmoWidth / 2.0));
				final long distanceToCritRadius=CMLib.map().getDistanceFrom(ship, programPlanet)
						- critRadius
						- ship.radius();
				final double ticksToDestinationAtCurrentSpeed = Math.abs(CMath.div(distance, ship.speed()));
				final double ticksToDecellerate = CMath.div(ship.speed(),CMath.div(this.targetAcceleration.doubleValue(), 2.0));
				if((ticksToDecellerate > ticksToDestinationAtCurrentSpeed)
				||(distance < ship.speed() * 20))
				{
					double targetAcceleration = 0.0;
					if(ship.speed() > this.targetAcceleration.doubleValue())
					{
						if(ship.speed() < (this.targetAcceleration.doubleValue() + 1.0))
							targetAcceleration = 1.0;
						else
							targetAcceleration = this.targetAcceleration.doubleValue();
					}
					else
					if(ship.speed()>CMLib.map().getDistanceFrom(ship, programPlanet)/4)
						targetAcceleration = ship.speed() - 1.0;
					else
					if(ship.speed()>2.0)
						targetAcceleration = 1.0;
					else
						targetAcceleration = 0.5;
					this.changeFacing(ship, CMLib.map().getOppositeDir(dirToPlanet));
					newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
						Log.debugOut("Landing Deccelerating @ "+  targetAcceleration +" because "+ticksToDecellerate+">"+ticksToDestinationAtCurrentSpeed+"  or "+distance+" < "+(ship.speed()*20));
					if((targetAcceleration >= 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship);
						Log.debugOut("Landing Deccelerating Check "+  Math.abs(this.lastAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(targetAcceleration);
					}
				}
				else
				if((distance > distanceToCritRadius) && (ship.speed() < Math.sqrt(distance)))
				{
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
						Log.debugOut("Landing Accelerating because " +  distance +" > "+distanceToCritRadius+" and "+ship.speed()+"<"+Math.sqrt(distance));
					this.changeFacing(ship, dirToPlanet);
					final double targetAcceleration = this.targetAcceleration.doubleValue();
					newInject=calculateMarginalTargetInjection(this.lastInject, targetAcceleration);
					if((targetAcceleration >= 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship);
						Log.debugOut("Landing Accelerating Check "+  Math.abs(this.lastAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(targetAcceleration);
					}
				}
				else
				{
					//this.changeFacing(ship, CMLib.map().getOppositeDir(dirToPlanet));
					newInject=Double.valueOf(0.0);
				}
			}
			if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				Log.debugOut("Landing: dir="+CMLib.english().directionDescShort(ship.direction())+"/speed="+ship.speed()+"/inject="+((newInject != null) ? newInject.toString():"null"));
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

	public boolean changeFacing(final SpaceShip ship, final double[] newFacing)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final boolean isDebugging = CMSecurity.isDebugging(DbgFlag.SPACESHIP);
		try
		{
			final double angleDiff = CMLib.map().getAngleDelta(ship.facing(), newFacing);
			if(angleDiff < 0.0001)
				return true;
			// step one, face opposite direction of motion
			if(isDebugging)
				Log.debugOut(ship.Name()+" maneuvering to go from "+ship.facing()[0]+","+ship.facing()[1]+"  to  "+newFacing[0]+","+newFacing[1]);
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
					final double angleAchievedPerPt = Math.abs(this.lastAngle.doubleValue()); //
					double[] angleDelta = CMLib.map().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
					for(int i=0;i<100;i++)
					{
						if(Math.abs(angleDelta[0]) > 0.00001)
						{
							final TechComponent.ShipDir dir = angleDelta[0] < 0 ? ShipDir.PORT : ShipDir.STARBOARD;
							final Double thrust = Double.valueOf(Math.abs(angleDelta[0]) / angleAchievedPerPt);
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
							final Double thrust = Double.valueOf(Math.abs(angleDelta[1]) / angleAchievedPerPt);
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
		final double targetAcceleration = SpaceObject.ACCELERATION_G;
		try
		{
			for(final ShipEngine engineE : engines)
			{
				if((CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.AFT))
				&&(engineE.getMaxThrust()>SpaceObject.ACCELERATION_G)
				&&(engineE.getMinThrust()<SpaceObject.ACCELERATION_PASSOUT))
				{
					int tries=100;
					double lastTryAmt;
					if(this.primeInjects.containsKey(engineE))
					{
						lastTryAmt = this.primeInjects.get(engineE)[0].doubleValue();
						lastAcceleration=this.primeInjects.get(engineE)[1];
					}
					else
						lastTryAmt= 0.0001;
					final CMMsg deactMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
					msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					Double prevAcceleration = Double.valueOf(0.0);
					while(--tries>0)
					{
						this.lastAcceleration =null;
						final String code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT, Double.valueOf(lastTryAmt));
						msg.setTargetMessage(code);
						this.trySendMsgToItem(M, engineE, msg);
						final Double thisLastAccel=this.lastAcceleration ;
						if(thisLastAccel!=null)
						{
							final double ratio = targetAcceleration/thisLastAccel.doubleValue();
							if((thisLastAccel.doubleValue() >= targetAcceleration)
							&&((!isDocked)||(ship.getIsDocked()==null)))
							{
								this.lastInject=Double.valueOf(lastTryAmt);
								this.primeInjects.put(engineE,new Double[] {lastInject,lastAcceleration});
								return engineE;
							}
							else
							if((thisLastAccel.doubleValue()>0.0) && (ratio>100))
								lastTryAmt *= (Math.sqrt(ratio)/5.0);
							else
							if(prevAcceleration.doubleValue() == thisLastAccel.doubleValue())
							{
								this.primeInjects.put(engineE,new Double[] {lastInject,lastAcceleration});
								break;
							}
							else
							{
								this.trySendMsgToItem(M, engineE, deactMsg);
								lastTryAmt *= 1.1;
							}
							prevAcceleration = thisLastAccel;
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

	private boolean sendMessage(final MOB mob, final Item E, final CMMsg msg, final String command)
	{
		if((E!=null) && (msg != null))
		{
			if(E.owner() instanceof Room)
			{
				if(((Room)E.owner()).okMessage(mob, msg))
				{
					((Room)E.owner()).send(mob, msg);
					return true;
				}
			}
			else
			if(E.okMessage(mob, msg))
			{
				E.executeMsg(mob, msg);
				return true;
			}
		}
		else
		{
			super.addScreenMessage(L("Error: Unknown command '"+command+"'.   Try HELP."));
		}
		return false;
	}

	@Override
	public void onTyping(final MOB mob, final String message)
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
					super.addScreenMessage(L("^HHELP:^N\n\r^N"
						+ "ACTIVATE [SYSTEM/ALL]  : turn on specified system\n\r"
						+ "DEACTIVATE [SYSTEM/ALL]: turn off any system specified\n\r"
						+ "LAUNCH / ORBIT         : take your ship off the planet\n\r"
						+ "TARGET [NAME]          : target a sensor object\n\r"
						+ "FIRE [WEAPON]          : fire weapon at target\n\r"
						+ "STOP   : negate all velocity\n\r"
						+ "LAND   : land your ship on the nearest planet. \n\r"
						+ "HELP [ENGINE/SYSTEM/SENSOR/WEAPON/...] : more info"));
					return;
				}
				final String secondWord = CMParms.combine(parsed,1).toUpperCase();
				if(secondWord.startsWith("ENGINE"))
				{
					E=findEngineByName(secondWord);
					if(E==null)
					{
						super.addScreenMessage(
							L("^HHELP:^N\n\r"
							+"^H[ENGINE#/NAME] ([AFT/PORT/STARBOARD/DORSEL/VENTRAL]) [AMT]\n\r"
							+ "^N"+"The ENGINE command instructs the given " +
							"engine number or name to fire in the appropriate direction. What happens, " +
							"and how quickly, depends largely on the capabilities of the engine. " +
							"Giving a direction is optional, and if not given, AFT is assumed. All "+
							"directions result in corrected bursts, except for AFT, which will result " +
							"in sustained acceleration."));
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
				if(secondWord.startsWith("WEAPON"))
				{
					super.addScreenMessage(
						L("^HHELP:^N\n\r"
						+"^H[WEAPON#/NAME] [AMT]\n\r"));
					return;
				}
				else
				if(secondWord.startsWith("SENSOR"))
				{
					E=this.findSensorByName(secondWord);
					if(E==null)
					{
						super.addScreenMessage(L("^HINFO:^N\n\r^N"+"Specified sensor system not found.  No information available."));
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
					for(final TechComponent component : getTechComponents())
					{
						if((!getEngines().contains(component))
						&&(!getShipSensors().contains(component)))
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
					super.addScreenMessage(L("^HHELP:^N\n\r^N"+"No help on "+secondWord.toUpperCase()+" available."));
					return;
				}
			}
			CMMsg msg = null;
			if(uword.equalsIgnoreCase("ACTIVATE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				if(rest.equalsIgnoreCase("ALL"))
				{
					int num=0;
					for(final TechComponent component : getTechComponents())
					{
						if((!getEngines().contains(component))
						&&(component.getTechType()!=TechType.SHIP_WEAPON)
						&&(component.getTechType()!=TechType.SHIP_TRACTOR)
						&&(!component.activated()))
						{
							msg=CMClass.getMsg(mob, component, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							if(component.owner() instanceof Room)
							{
								if(((Room)component.owner()).okMessage(mob, msg))
									((Room)component.owner()).send(mob, msg);
							}
							else
							if(component.okMessage(mob, msg))
								component.executeMsg(mob, msg);
							if(component.activated())
								num++;
						}
					}
					super.addScreenMessage(L("@x1 systems activated..",""+num));
					return;
				}
				else
				{
					String code = null;
					E=findEngineByName(rest);
					if(E!=null)
						code=TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(.0000001));
					else
						E=findSensorByName(rest);
					if(E==null)
					{
						final List<TechComponent> others = new ArrayList<TechComponent>();
						for(final TechComponent component : getTechComponents())
						{
							if((!getEngines().contains(component))
							&&(!getShipSensors().contains(component)))
								others.add(component);
						}
						E=findComponentByName(others,"SYSTEM",rest);
					}
					if(E!=null)
						msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					else
					{
						super.addScreenMessage(L("Error: Unknown system to activate '"+rest+"'."));
						return;
					}
				}
			}
			else
			if(uword.equalsIgnoreCase("DEACTIVATE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				if(rest.equalsIgnoreCase("ALL"))
				{
					int num=0;
					for(final TechComponent component : getTechComponents())
					{
						if((!getEngines().contains(component))
						&&(component.getTechType()!=TechType.SHIP_WEAPON)
						&&(component.getTechType()!=TechType.SHIP_TRACTOR)
						&&(component.activated()))
						{
							msg=CMClass.getMsg(mob, component, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							if(component.owner() instanceof Room)
							{
								if(((Room)component.owner()).okMessage(mob, msg))
									((Room)component.owner()).send(mob, msg);
							}
							else
							if(component.okMessage(mob, msg))
								component.executeMsg(mob, msg);
							if(!component.activated())
								num++;
						}
					}
					super.addScreenMessage(L("@x1 systems de-activated..",""+num));
					return;
				}
				else
				{
					E=findEngineByName(rest);
					if(E==null)
						E=findSensorByName(rest);
					if(E==null)
					{
						final List<TechComponent> others = new ArrayList<TechComponent>();
						for(final TechComponent component : getTechComponents())
						{
							if((!getEngines().contains(component))&&(!getShipSensors().contains(component)))
								others.add(component);
						}
						E=findComponentByName(others,"SYSTEM",rest);
					}
					if(E!=null)
						msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					else
					{
						super.addScreenMessage(L("Error: Unknown system to deactivate '"+rest+"'."));
						return;
					}
				}
			}
			else
			if(uword.equalsIgnoreCase("LAUNCH") || uword.equalsIgnoreCase("ORBIT"))
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
				final ShipEngine engineE =this.primeMainThrusters(ship);
				if(engineE==null)
				{
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
					return;
				}
				boolean dampenerFound = false;
				for(final TechComponent T : this.getDampeners())
				{
					if(T.activated()
					&&((!T.subjectToWearAndTear()))||(T.usesRemaining()>30))
						dampenerFound = true;
				}
				if(!dampenerFound)
				{
					super.addScreenMessage(L("No inertial dampeners found.  Limiting acceleration to 3G."));
					this.targetAcceleration = Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
				}
				else
					this.targetAcceleration = Double.valueOf(30);

				this.programEngines=new XVector<ShipEngine>(engineE);
				if(uword.equalsIgnoreCase("ORBIT"))
					this.rocketState = RocketShipProgram.RocketStateMachine.ORBITSEARCH;
				else
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
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject landingPlanet = null;
				for(final SpaceObject O : allObjects)
				{
					if((O.coordinates()!=null)&&(O.radius()!=0))
					{
						final List<LocationRoom> rooms=CMLib.map().getLandingPoints(ship, O);
						if(rooms.size()>0)
						{
							landingPlanet=O;
							break;
						}
					}
				}
				if(landingPlanet == null)
				{
					for(final SpaceObject O : allObjects)
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
				this.programPlanet = landingPlanet;
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
			if(uword.equalsIgnoreCase("TARGET"))
			{
				final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(parsed.size()<2)
				{
					super.addScreenMessage(L("Error: TARGET requires the name of the target.   Try HELP."));
					return;
				}
				if(sensorReports.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify landing position."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
				if(targetObj == null)
				{
					super.addScreenMessage(L("No suitable target @x1 found within sensor range.",targetStr));
					return;
				}
				if(targetObj.coordinates() == null)
				{
					super.addScreenMessage(L("Can not target @x1 due to lack of coordinate information.",targetObj.Name()));
					return;
				}
				this.currentTarget = targetObj;
				super.addScreenMessage(L("Target set for @x1.",targetObj.Name()));
				return;
			}
			else
			if(uword.equalsIgnoreCase("FIRE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(currentTarget == null)
				{
					super.addScreenMessage(L("Target not set."));
					return;
				}
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				final SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, currentTarget.ID(), true);
				if(targetObj == null)
				{
					super.addScreenMessage(L("Target no longer in sensor range."));
					return;
				}
				if(targetObj.coordinates()==null)
				{
					super.addScreenMessage(L("Unable to determine target direction and range."));
					return;
				}
				final double[] targetDirection = CMLib.map().getDirection(ship, targetObj);
				TechComponent finalWeaponToFire = null;
				final String weapName = CMParms.combine(parsed,1);
				if(weapName.length()>0)
				{
					final ShipWarComponent weapon = this.findWeaponByName(rest);
					if(weapon == null)
					{
						super.addScreenMessage(L("Error: Unknown weapon name or command word '"+rest+"'.   Try HELP."));
						return;
					}
					finalWeaponToFire = weapon;
				}
				else
				{
					for(final TechComponent T : getShipWeapons())
					{
						if(T instanceof ShipWarComponent)
						{

							final ShipDir dir = CMLib.map().getDirectionFromDir(ship.facing(), ship.roll(), targetDirection);
							if(CMParms.contains(CMLib.tech().getCurrentBattleCoveredDirections((ShipWarComponent)T), dir))
							{
								finalWeaponToFire = T;
								break;
							}
						}
						else
							finalWeaponToFire = T;
					}
					if(finalWeaponToFire == null)
					{
						if(getShipWeapons().size()>0)
							finalWeaponToFire = getShipWeapons().get(0);
					}
					if(finalWeaponToFire == null)
					{
						super.addScreenMessage(L("Error: No weapons found."));
						return;
					}
					super.addScreenMessage(L("Info: Auto selected weapon '@x1'.",finalWeaponToFire.Name()));
				}
				{
					E=finalWeaponToFire;
					String code;

					code=TechCommand.WEAPONTARGETSET.makeCommand(Double.valueOf(targetDirection[0]), Double.valueOf(targetDirection[1]));
					msg=CMClass.getMsg(mob, finalWeaponToFire, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					this.sendMessage(mob, finalWeaponToFire, msg, message);

					code = TechCommand.WEAPONFIRE.makeCommand();
					msg=CMClass.getMsg(mob, finalWeaponToFire, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				}
			}
			else
			if(uword.startsWith("WEAPON"))
			{
				final ShipWarComponent weapon = this.findWeaponByName(uword);
				if(weapon == null)
				{
					super.addScreenMessage(L("Error: Unknown weapon name or command word '"+uword+"'.   Try HELP."));
					return;
				}
				if(parsed.size()==1)
				{
					super.addScreenMessage(L("Error: No emission percentage given."));
					return;
				}
				final String emission=parsed.get(1);
				if(!CMath.isPct(emission))
				{
					super.addScreenMessage(L("Error: Invalid emission percentage given."));
					return;
				}
				final double pct=CMath.s_pct(emission);
				if((pct < 0)||(pct > 1))
				{
					super.addScreenMessage(L("Error: Invalid emission percentage given."));
					return;
				}
				E=weapon;
				String code;

				code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(pct * 100.0)));
				msg=CMClass.getMsg(mob, weapon, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			}
			else
			if(!uword.equalsIgnoreCase("HELP"))
			{
				final ShipEngine engineE=findEngineByName(uword);
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
			sendMessage(mob,E,msg,message);
		}
	}

	@Override
	public void onActivate(final MOB mob, final String message)
	{
		onTyping(mob,message);
	}

	@Override
	public void onDeactivate(final MOB mob, final String message)
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
	public void onPowerCurrent(final int value)
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
			if(command == TechCommand.ACCELERATED)
			{
				final Object[] parms=command.confirmAndTranslate(parts);
				if(parms != null)
				{
					switch((ShipDir)parms[0])
					{
					case AFT:
					case FORWARD:
						if(this.lastAcceleration==null)
							this.lastAcceleration =(Double)parms[1];
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
