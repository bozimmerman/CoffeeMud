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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Software.SWServices;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.io.*;
import java.util.*;

/*
   Copyright 2013-2022 Bo Zimmerman

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

	protected static final int DEFAULT_ACT_8_SEC_COUNTDOWN = 100;

	protected volatile long nextPowerCycleTmr = System.currentTimeMillis()+(8*1000);
	protected volatile int	activationCounter = DEFAULT_ACT_8_SEC_COUNTDOWN;

	protected volatile List<ShipEngine>		engines		= null;
	protected volatile List<TechComponent>	sensors		= null;
	protected volatile List<TechComponent>	weapons		= null;
	protected volatile List<TechComponent>	shields		= null;
	protected volatile List<TechComponent>	components	= null;
	protected volatile List<TechComponent>	dampers		= null;
	protected final  Set<TechComponent>		activated	= Collections.synchronizedSet(new HashSet<TechComponent>());

	protected volatile Double				lastAcceleration	= null;
	protected volatile Double				lastAngle			= null;
	protected volatile Double				lastInject			= null;
	protected volatile Double				targetAcceleration	= Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
	protected volatile SpaceObject			approachTarget		= null;
	protected volatile long					deproachDistance	= 0;
	protected volatile RocketStateMachine	rocketState			= null;
	protected volatile SpaceObject			currentTarget		= null;
	protected volatile SpaceObject			programPlanet		= null;
	protected volatile List<ShipEngine>		programEngines		= null;

	protected final	Map<Technical, Set<SpaceObject>>sensorReps	= new SHashtable<Technical, Set<SpaceObject>>();
	protected final Map<ShipEngine, Double[]>		injects		= new Hashtable<ShipEngine, Double[]>();

	protected final static long[] emptyCoords = new long[] {0,0,0};
	protected final static double[] emptyDirection = new double[] {0,0};
	protected final static PrioritizingLimitedMap<String,TechComponent> cachedComponents = new PrioritizingLimitedMap<String,TechComponent>(1000,60000,600000,0);

	protected void decache()
	{
		engines = null;
		sensors		= null;
		weapons		= null;
		shields		= null;
		components	= null;
		dampers		= null;
		activated.clear();

		approachTarget = null;
		deproachDistance = 0;
		rocketState = null;
		currentTarget = null;
		programPlanet = null;
		programEngines = null;
		sensorReps.clear();
		injects.clear();
	}

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
		ORBITCRUISE,
		APPROACH,
		DEPROACH
	}

	public RocketShipProgram()
	{
		super();
		setName("a shuttle operations disk");
		setDisplayText("a small software disk sits here.");
		setDescription("It appears to be a program to operate a small shuttle or rocket.");

		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
		recoverPhyStats();
	}

	@Override
	protected SWServices[] getAppreciatedServices()
	{
		return new SWServices[] { Software.SWServices.IDENTIFICATION };
	}

	@Override
	protected SWServices[] getProvidedServices()
	{
		return new SWServices[] { Software.SWServices.TARGETING };
	}

	private static class DistanceSorter implements Comparator<SpaceObject>
	{
		private final GalacticMap space;
		private final SpaceObject spaceObject;

		private DistanceSorter(final SpaceObject me)
		{
			space=CMLib.space();
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
			final long distance1 = space.getDistanceFrom(spaceObject, o1) - o1.radius();
			final long distance2 = space.getDistanceFrom(spaceObject, o2) - o2.radius();
			if(distance1 < distance2)
				return -1;
			if(distance1 > distance2)
				return 1;
			return 0;
		}
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		if((container() instanceof Computer)
		&&((Computer)container()).getActiveMenu().equals(getInternalName()))
		{
			super.forceUpMenu();
			return this.getActivationMenu()+"\n\r";
		}
		else
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
				if ((E instanceof TechComponent)
				&& (E.getTechType()== type))
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

	protected boolean isWeaponLauncher(final TechComponent E)
	{
		if(E.getTechType()==TechType.SHIP_LAUNCHER)
		{
			if(!(E instanceof Container))
				return false;
			final List<Item> contents = ((Container)E).getContents();
			if(contents.size()==0)
				return true;
			if(contents.get(0) instanceof Weapon)
				return true;
			return false;
		}
		return false;
	}

	protected synchronized List<TechComponent> getShipWeapons()
	{
		if(weapons == null)
		{
			final List<TechComponent> stuff=getTechComponents();
			weapons=new Vector<TechComponent>(1);
			for(final TechComponent E : stuff)
			{
				if((E.getTechType()==TechType.SHIP_WEAPON)
				||(isWeaponLauncher(E)))
					weapons.add(E);
			}
		}
		return weapons;
	}

	protected synchronized List<TechComponent> getShipShields()
	{
		if(shields == null)
		{
			final List<TechComponent> stuff=getTechComponents();
			shields=new Vector<TechComponent>(1);
			for(final TechComponent E : stuff)
			{
				if(E.getTechType()==TechType.SHIP_SHIELD)
					shields.add(E);
			}
		}
		return shields;
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
		return super.isActivationString(word);
	}

	@Override
	public boolean isDeActivationString(final String word)
	{
		return super.isDeActivationString(word);
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

	protected TechComponent findComponentByID(final List<? extends TechComponent> list, final String id)
	{
		if(list.size()==0)
			return null;
		if(cachedComponents.containsKey(id))
			return cachedComponents.get(id);
		for(final TechComponent C : list)
		{
			if((""+C).equalsIgnoreCase(id))
			{
				cachedComponents.put(id, C);
				return C;
			}
		}
		return null;
	}

	protected ShipEngine findEngineByName(final String name)
	{
		return (ShipEngine)findComponentByName(getEngines(), "ENGINE", name);
	}

	protected TechComponent findSensorByName(final String name)
	{
		return findComponentByName(getShipSensors(), "SENSOR", name);
	}

	protected TechComponent findWeaponByName(final String name)
	{
		return findComponentByName(getShipWeapons(), "WEAPON", name);
	}

	protected ShipWarComponent findShieldByName(final String name)
	{
		return (ShipWarComponent)findComponentByName(getShipShields(), "SHIELD", name);
	}

	protected ShipEngine findEngineByPort(final ShipDirectional.ShipDir portdir)
	{
		for(final ShipEngine E : getEngines())
		{
			if(CMParms.contains(E.getAvailPorts(), portdir))
				return E;
		}
		return null;
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
		||uword.equals("FACE")
		||uword.equals("APPROACH")
		||uword.equals("CANCEL")
		||uword.equals("MOON")
		||uword.equals("FIRE")
		||(uword.startsWith("SHIELD"))
		||(uword.startsWith("WEAPON")&&(CMath.isInteger(uword.substring(6))))
		||(uword.startsWith("ENGINE")&&(CMath.isInteger(uword.substring(6))))
		||(uword.startsWith("SENSOR")&&(CMath.isInteger(uword.substring(6))))
		||(uword.startsWith("SYSTEM")&&(CMath.isInteger(uword.substring(6))))
		)
			return true;
		return findEngineByName(uword)!=null;
	}

	protected Set<SpaceObject> getLocalSensorReport(final TechComponent sensor)
	{
		if(sensor==null)
			return new TreeSet<SpaceObject>(XTreeSet.comparator);
		final Set<SpaceObject> localSensorReport;
		synchronized(sensorReps)
		{
			if(sensorReps.containsKey(sensor))
				localSensorReport=sensorReps.get(sensor);
			else
			{
				localSensorReport=new TreeSet<SpaceObject>(XTreeSet.comparator);
				sensorReps.put(sensor, localSensorReport);
			}
		}
		return localSensorReport;
	}

	protected Collection<SpaceObject> takeNewSensorReport(final TechComponent sensor)
	{
		final Set<SpaceObject> localSensorReport=getLocalSensorReport(sensor);
		localSensorReport.clear();
		final String code=Technical.TechCommand.SENSE.makeCommand(sensor,Boolean.TRUE);
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
		return localSensorReport;
	}

	public String getFlightStatus()
	{
		final StringBuilder str=new StringBuilder();
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		str.append("^X").append(CMStrings.centerPreserve(L(" -- Flight Status -- "),60)).append("^.^N\n\r");
		if(ship==null)
			str.append("^Z").append(CMStrings.centerPreserve(L(" -- Can Not Determine -- "),60));
		else
		if(ship.getIsDocked() != null)
		{
			str.append("^H").append(CMStrings.padRight(L("Docked at ^w@x1",ship.getIsDocked().displayText(null)),60));
			final SpaceObject planet=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
			if(planet!=null)
				str.append("^.^N\n\r^H").append(CMStrings.padRight(L("On Planet ^w@x1",planet.name()),60));
		}
		else
		if((shipSpaceObject==null)||(!CMLib.space().isObjectInSpace(shipSpaceObject)))
			str.append("^Z").append(CMStrings.centerPreserve(L(" -- System Malfunction-- "),60));
		else
		{
			final List<SpaceObject> orbs=CMLib.space().getSpaceObjectsWithin(shipSpaceObject,0,SpaceObject.Distance.LightMinute.dm);
			SpaceObject orbitingPlanet=null;
			SpaceObject altitudePlanet=null;
			for(final SpaceObject orb : orbs)
			{
				if(orb instanceof Area)
				{
					final long distance=CMLib.space().getDistanceFrom(shipSpaceObject, orb);
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
				str.append("^N").append(CMStrings.padRight(display(CMLib.space().getDistanceFrom(shipSpaceObject, altitudePlanet)-shipSpaceObject.radius()-altitudePlanet.radius()),25));
			}
			else
			{
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Sector"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.space().getSectorName(ship.coordinates()),50));
				str.append("\n\r");
				str.append("^H").append(CMStrings.padRight(L("Location"),10));
				str.append("^N").append(CMStrings.padRight(CMLib.english().coordDescShort(CMLib.space().getInSectorCoords(shipSpaceObject.coordinates())),25));
			}
			str.append("^H").append(CMStrings.padRight(L("Facing"),10));
			final String facStr=display(ship.facing());
			str.append("^N").append(CMStrings.padRight(facStr,15));
		}
		str.append("^.^N\n\r");
		return trimColorsAndTrim(str.toString());
	}

	public String getSensorMenu()
	{
		final StringBuilder str=new StringBuilder();
		final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final List<TechComponent> sensors = getShipSensors();
		if(sensors.size()>0)
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Sensors -- "),60)).append("^.^N\n\r");
			int sensorNumber=1;
			for(final TechComponent sensor : sensors)
			{
				if(sensor.powerCapacity()==0) // its just a window
					continue;
				str.append("^H").append(CMStrings.padRight(L("SENSOR@x1",""+sensorNumber),9));
				str.append(CMStrings.padRight(sensor.activated()?L("^gA"):L("^rI"),2));
				str.append("^H").append(CMStrings.padRight(L("Pow."),5));
				str.append("^N").append(CMStrings.padRight(Long.toString(sensor.powerRemaining()),11));
				str.append("^H").append(CMStrings.padRight(sensor.name(),31));
				str.append("^.^N\n\r");
				if(sensor.activated())
				{
					final Collection<SpaceObject> localSensorReport=takeNewSensorReport(sensor);
					if(localSensorReport.size()==0)
						str.append("^R").append(L("No Report^.^N\n\r"));
					else
					for(final Object o : localSensorReport)
					{
						if(o == spaceObject)
							continue;
						if(o instanceof SpaceObject)
						{
							final SpaceObject spaceMe = ship;
							final SpaceObject obj = (SpaceObject)o;
							final long distance = CMLib.space().getDistanceFrom(spaceMe.coordinates(), obj.coordinates()) - spaceMe.radius() - obj.radius();
							final double[] direction = CMLib.space().getDirection(spaceMe, obj);
							if((currentTarget!=null)
							&&((currentTarget==o)||(currentTarget.ID().equals(obj.ID()))))
								str.append("^r*^N ");
							str.append("^W").append(obj.name());
							if(obj.getMass()>0)
								str.append("^N/^WMass: ^N").append(CMath.abbreviateLong(obj.getMass()));
							if(!Arrays.equals(obj.direction(),emptyDirection))
								str.append("^N/^WDir: ^N").append(CMLib.english().directionDescShortest(direction));
							else
							if(obj.radius()>0)
								str.append("^N/^WSize: ^N").append(CMLib.english().distanceDescShort(obj.radius()));
							if(!Arrays.equals(obj.coordinates(),emptyCoords))
								str.append("^N/^WDist: ^N").append(CMLib.english().distanceDescShort(distance));
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
				sensorNumber++;
			}
		}
		return trimColorsAndTrim(str.toString());
	}

	public String getShipSystemsMenu()
	{
		final StringBuilder str=new StringBuilder();
		final List<TechComponent> sensors = getShipSensors();
		final List<ShipEngine> engines = getEngines();
		final List<TechComponent> weapons = getShipWeapons();
		final List<TechComponent> shields = getShipShields();
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
				str.append("^H").append(CMStrings.padRight(engine.name(),31));
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
				str.append("^H").append(CMStrings.padRight(weapon.name(),31));
				str.append("^.^N\n\r");
				weaponNumber++;
			}
			int shieldNumber=1;
			for(final TechComponent shield : shields)
			{
				str.append("^H").append(CMStrings.padRight(L("SHIELD@x1",""+shieldNumber),9));
				str.append(CMStrings.padRight(shield.activated()?L("^gA"):L("^rI"),2));
				str.append("^H").append(CMStrings.padRight(L("Pow."),5));
				str.append("^N").append(CMStrings.padRight(Long.toString(shield.powerTarget()),11));
				str.append("^H").append(CMStrings.padRight(shield.name(),31));
				str.append("^.^N\n\r");
				shieldNumber++;
			}
			int systemNumber=1;
			for(final TechComponent component : components)
			{
				if((!engines.contains(component))
				&&(!sensors.contains(component))
				&&(!weapons.contains(component))
				&&(!shields.contains(component)))
				{
					str.append("^H").append(CMStrings.padRight(L("SYSTEM@x1",""+systemNumber),9));
					str.append(CMStrings.padRight(component.activated()?L("^gA"):L("^rI"),2));
					str.append("^H").append(CMStrings.padRight(L("Pow."),5));
					if(component instanceof Computer)
						str.append("^N").append(CMStrings.padRight(Long.toString(component.powerTarget()),11));
					else
						str.append("^N").append(CMStrings.padRight(Long.toString(component.powerRemaining()),11));
					str.append("^H").append(CMStrings.padRight(component.name(),31));
					str.append("^.^N\n\r");
					systemNumber++;
				}
			}
			str.append("^.^N\n\r");
		}
		return trimColorsAndTrim(str.toString());
	}

	@Override
	public String getActivationMenu()
	{
		final StringBuilder str=new StringBuilder();
		final String flightStatusMenu = getFlightStatus();
		str.append(flightStatusMenu);
		final String sensorReportMenu = getSensorMenu();
		if(sensorReportMenu.length()>0)
			str.append("\n\r^.^N"+sensorReportMenu);
		final String systemsReportMenu = this.getShipSystemsMenu();
		if(systemsReportMenu.length()>0)
			str.append("\n\r^.^N"+systemsReportMenu);
		str.append("\n\r^.^N");
		if(engines.size()==0)
			str.append("^rNo engine systems found.");
		else
		{
			str.append("^X").append(CMStrings.centerPreserve(L(" -- Commands -- "),60)).append("^.^N\n\r");
			str.append("^H").append(CMStrings.padRight(L("TYPE HELP INTO CONSOLE : Get help."),60)).append("\n\r");
			if((container() instanceof Rideable)
			&&(((Rideable)container()).rideBasis()==Rideable.Basis.FURNITURE_TABLE)
			&&(((Rideable)container()).numRiders()==0))
				str.append("^H").append(CMStrings.padRight(L("* Sit at "+container().name()+" to shorten commands *"),60)).append("\n\r");
			str.append("^X").append(CMStrings.centerPreserve("",60));
		}
		return trimColorsAndTrim(str.toString())+"^.^N";
	}

	@Override
	protected boolean checkActivate(final MOB mob, final String message)
	{
		return true;
	}

	@Override
	protected boolean checkDeactivate(final MOB mob, final String message)
	{

		return true;
	}

	@Override
	protected boolean checkTyping(final MOB mob, final String message)
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
					final String code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT,Double.valueOf(thrustInject.doubleValue()));
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

	protected void findTargetAcceleration(final ShipEngine E)
	{
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
	}

	@Override
	protected boolean checkPowerCurrent(final int value)
	{
		RocketShipProgram.RocketStateMachine state=this.rocketState;
		if(state == null)
			return super.checkPowerCurrent(value);
		final SpaceObject spaceObj=CMLib.space().getSpaceObject(this,true);
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
				final long distance=CMLib.space().getDistanceFrom(ship.coordinates(),programPlanet.coordinates());
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
				final double[] stopFacing = CMLib.space().getOppositeDir(ship.direction());
				final double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
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
		case APPROACH:
		case DEPROACH:
		{
			final long distance = (CMLib.space().getDistanceFrom(ship, approachTarget)-ship.radius()-approachTarget.radius());
			int safeDistance=100 + (int)Math.round(ship.speed());
			final double[] dirTo = CMLib.space().getDirection(ship, this.approachTarget);
			final double[] diffDelta = CMLib.space().getFacingAngleDiff(ship.direction(), dirTo); // starboard is -, port is +
			if((Math.abs(diffDelta[0])+Math.abs(diffDelta[1]))<.05)
				safeDistance += (int)Math.round(ship.speed());
			if(distance < safeDistance)
			{
				for(final ShipEngine engineE : programEngines)
					performSimpleThrust(engineE,Double.valueOf(0.0), true);
				this.rocketState=null;
				this.programEngines=null;
				this.lastInject=null;
				this.approachTarget=null;
				super.addScreenMessage(L("Approach program completed."));
				return super.checkPowerCurrent(value);
			}
			else
			if(distance < this.deproachDistance)
			{
				if(state == RocketStateMachine.APPROACH)
					state=RocketStateMachine.DEPROACH;
				final double[] desiredFacing;
				if(state == RocketStateMachine.APPROACH)
					desiredFacing = CMLib.space().getDirection(ship, this.approachTarget);
				else
					desiredFacing = CMLib.space().getOppositeDir(ship.direction());
				final double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), desiredFacing); // starboard is -, port is +
				if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))>.02)
				{
					if(!changeFacing(ship, desiredFacing))
					{
						this.rocketState=null;
						this.programEngines=null;
						this.lastInject=null;
						this.approachTarget=null;
						super.addScreenMessage(L("Stop program aborted with error (directional control failure)."));
						return super.checkPowerCurrent(value);
					}
					final double targetAcceleration = this.targetAcceleration.doubleValue();
					if(this.lastInject != null)
					{
						if(ship.speed() < targetAcceleration)
							this.lastInject = Double.valueOf(this.lastInject.doubleValue()/2.0);
						else
						if(ship.speed() < (targetAcceleration * 2))
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
				final long distance=CMLib.space().getDistanceFrom(ship, programPlanet);
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
		case APPROACH:
		case DEPROACH:
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
			final double[] dirToPlanet = CMLib.space().getDirection(ship, programPlanet);
			//final long distance=CMLib.space().getDistanceFrom(ship, programPlanet)
			//		- Math.round(CMath.mul(programPlanet.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
			//		- ship.radius();
			final double atmoWidth = CMath.mul(programPlanet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - programPlanet.radius();
			final long critRadius = Math.round(programPlanet.radius() + (atmoWidth / 2.0));
			final long distanceToCritRadius=CMLib.space().getDistanceFrom(ship, programPlanet)
					- critRadius
					- ship.radius();
			if(distanceToCritRadius <= 0)
				this.rocketState = RocketStateMachine.LANDING;
			else
			{
				//final double angleDiff = CMLib.space().getAngleDelta(ship.direction(), dirToPlanet);
				for(final ShipEngine engineE : programEngines)
				{
					final double ticksToDecellerate = CMath.div(ship.speed(),CMath.div(this.targetAcceleration.doubleValue(),2.0));
					final double ticksToDestinationAtCurrentSpeed = CMath.div(distanceToCritRadius, ship.speed());
					final double diff = Math.abs(ticksToDecellerate-ticksToDestinationAtCurrentSpeed);
					if((diff < 1) || (diff < Math.sqrt(ticksToDecellerate)))
					{
						final Double oldInject=this.lastInject;
						final Double oldAccel=this.lastAcceleration;
						performSimpleThrust(engineE,Double.valueOf(0.0), false);
						this.lastInject=oldInject;
						this.lastAcceleration=oldAccel;
						break;
					}
					else
					if(ticksToDecellerate > ticksToDestinationAtCurrentSpeed)
						this.changeFacing(ship, CMLib.space().getOppositeDir(dirToPlanet));
					else
					if((ticksToDecellerate<50)||(diff > 10.0))
						this.changeFacing(ship, dirToPlanet);
					final double targetAcceleration = this.targetAcceleration.doubleValue(); //
					newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
					if((targetAcceleration > 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship,true);
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
			final double[] dirToPlanet = CMLib.space().getDirection(ship, programPlanet);
			if(CMLib.space().getAngleDelta(dirToPlanet, ship.direction()) > 1)
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
				final long distance=CMLib.space().getDistanceFrom(ship, programPlanet)
						- programPlanet.radius()
						- ship.radius()
						-10; // margin for soft landing
				final double atmoWidth = CMath.mul(programPlanet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - programPlanet.radius();
				final long critRadius = Math.round(programPlanet.radius() + (atmoWidth / 2.0));
				final long distanceToCritRadius=CMLib.space().getDistanceFrom(ship, programPlanet)
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
					if(ship.speed()>CMLib.space().getDistanceFrom(ship, programPlanet)/4)
						targetAcceleration = ship.speed() - 1.0;
					else
					if(ship.speed()>2.0)
						targetAcceleration = 1.0;
					else
						targetAcceleration = 0.5;
					this.changeFacing(ship, CMLib.space().getOppositeDir(dirToPlanet));
					newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
						Log.debugOut("Landing Deccelerating @ "+  targetAcceleration +" because "+ticksToDecellerate+">"+ticksToDestinationAtCurrentSpeed+"  or "+distance+" < "+(ship.speed()*20));
					if((targetAcceleration >= 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship,true);
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
						primeMainThrusters(ship,true);
						Log.debugOut("Landing Accelerating Check "+  Math.abs(this.lastAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(targetAcceleration);
					}
				}
				else
				{
					//this.changeFacing(ship, CMLib.space().getOppositeDir(dirToPlanet));
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
		final double[] stopFacing = CMLib.space().getOppositeDir(ship.direction());
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
			final double angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			if(angleDiff < 0.0001)
				return true;
			// step one, face opposite direction of motion
			if(isDebugging)
				Log.debugOut(ship.Name()+" maneuvering to go from "+ship.facing()[0]+","+ship.facing()[1]+"  to  "+newFacing[0]+","+newFacing[1]);
			for(final ShipEngine engineE : engines)
			{
				if((CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.STARBOARD))
				&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.PORT))
				&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.DORSEL))
				&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.VENTRAL)))
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
					double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
					for(int i=0;i<100;i++)
					{
						if(Math.abs(angleDelta[0]) > 0.00001)
						{
							final ShipDirectional.ShipDir dir = angleDelta[0] < 0 ? ShipDir.PORT : ShipDir.STARBOARD;
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
						angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						if(isDebugging)
							Log.debugOut("* Total Deltas now: "+angleDelta[0]+" + "+angleDelta[1] +"=="+((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))));
						if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
							return true;
					}
					angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
					for(int i=0;i<100;i++)
					{
						if(Math.abs(angleDelta[1]) > 0.00001)
						{
							final ShipDirectional.ShipDir dir = angleDelta[1] < 0 ? ShipDir.VENTRAL : ShipDir.DORSEL;
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
						angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
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

	public ShipEngine primeMainThrusters(final SpaceShip ship, final boolean limit)
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
				if((CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.AFT))
				&&(engineE.getMaxThrust()>SpaceObject.ACCELERATION_G)
				&&(engineE.getMinThrust()<SpaceObject.ACCELERATION_PASSOUT))
				{
					int tries=100;
					double lastTryAmt;
					if(this.injects.containsKey(engineE))
					{
						lastTryAmt = this.injects.get(engineE)[0].doubleValue();
						lastAcceleration=this.injects.get(engineE)[1];
					}
					else
						lastTryAmt= 0.0001;
					final CMMsg deactMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
					msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					Double prevAcceleration = Double.valueOf(0.0);
					while(--tries>0)
					{
						this.lastAcceleration =null;
						final String code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT, Double.valueOf(lastTryAmt));
						msg.setTargetMessage(code);
						this.trySendMsgToItem(M, engineE, msg);
						final Double thisLastAccel=this.lastAcceleration ;
						if(thisLastAccel!=null)
						{
							final double ratio = targetAcceleration/thisLastAccel.doubleValue();
							if((thisLastAccel.doubleValue() >= targetAcceleration)
							&&((!isDocked)||(ship.getIsDocked()==null))
							&&(!limit))
							{
								this.lastInject=Double.valueOf(lastTryAmt);
								this.injects.put(engineE,new Double[] {lastInject,lastAcceleration});
								return engineE;
							}
							else
							if((thisLastAccel.doubleValue()>0.0) && (ratio>100))
								lastTryAmt *= (Math.sqrt(ratio)/5.0);
							else
							if(prevAcceleration.doubleValue() == thisLastAccel.doubleValue())
							{
								this.injects.put(engineE,new Double[] {lastInject,lastAcceleration});
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
	protected void onTyping(final MOB mob, final String message)
	{
		synchronized(this)
		{
			super.forceUpMenu();
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
						+ "FACE [NAME]            : face a sensor object\n\r"
						+ "APPROACH [NAME]        : approach a sensor object\n\r"
						+ "MOON [NAME]            : moon a sensor object\n\r"
						+ "[SENSOR NAME] [DIR]    : aim/use a sensor\n\r"
						+ "FIRE [WEAPON]          : fire weapon at target\n\r"
						+ "STOP                   : negate all velocity\n\r"
						+ "LAND                   : land on the nearest planet.\n\r"
						+ "CANCEL                 : cancel any running prgs.\n\r"
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
						code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT,Double.valueOf(.0000001));
					else
					{
						E=findSensorByName(rest);
						if(E==null)
							E=findShieldByName(rest);
					}
					if(E==null)
					{
						final List<TechComponent> others = new ArrayList<TechComponent>();
						for(final TechComponent component : getTechComponents())
						{
							if((!getEngines().contains(component))
							&&(!getShipShields().contains(component))
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
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
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
				this.programPlanet=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
				final ShipEngine engineE =this.primeMainThrusters(ship,true);
				if(engineE==null)
				{
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
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
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
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
					engineE=this.primeMainThrusters(ship,ship.getIsDocked()!=null);
				if(engineE==null)
				{
					this.rocketState=null;
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				this.programEngines=new XVector<ShipEngine>(engineE);
				this.rocketState = RocketShipProgram.RocketStateMachine.STOP;
				super.addScreenMessage(L("All Stop procedure initialized."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("LAND"))
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
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
				if(sensorReps.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify landing position."));
					return;
				}
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject landingPlanet = null;
				for(final SpaceObject O : allObjects)
				{
					if((O.coordinates()!=null)&&(O.radius()!=0))
					{
						final List<LocationRoom> rooms=CMLib.space().getLandingPoints(ship, O);
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
					engineE=this.primeMainThrusters(ship,true);
				if(engineE==null)
				{
					this.rocketState=null;
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				this.programPlanet = landingPlanet;
				this.programEngines=new XVector<ShipEngine>(engineE);
				// this lands you at the nearest point, which will pick the nearest location room, if any
				//TODO: picking the nearest landing zone, orbiting to it, and THEN landing would be better.
				this.rocketState = RocketShipProgram.RocketStateMachine.PRE_LANDING_STOP;
				final long distance=CMLib.space().getDistanceFrom(ship.coordinates(),landingPlanet.coordinates());
				if(distance > (ship.radius() + Math.round(landingPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					super.addScreenMessage(L("Landing approach procedure initialized."));
				else
					super.addScreenMessage(L("Landing procedure initialized."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("TARGET"))
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
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
				if(sensorReps.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify target."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeNewSensorReport(sensor));
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
					super.addScreenMessage(L("Can not target @x1 due to lack of coordinate information.",targetObj.name()));
					return;
				}
				this.currentTarget = targetObj;
				super.addScreenMessage(L("Target set for @x1.",targetObj.name()));
				return;
			}
			else
			if(uword.equalsIgnoreCase("FACE"))
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(parsed.size()<2)
				{
					super.addScreenMessage(L("Error: FACE requires the name of the object.   Try HELP."));
					return;
				}
				if(sensorReps.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify object."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
				if(targetObj == null)
				{
					super.addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
					return;
				}
				if(targetObj.coordinates() == null)
				{
					super.addScreenMessage(L("Can not face @x1 due to lack of coordinate information.",targetObj.name()));
					return;
				}
				final double[] facing=ship.facing();
				final double[] dirTo = CMLib.space().getDirection(spaceObject, targetObj);
				double fdist1=(facing[0]>dirTo[0])?facing[0]-dirTo[0]:dirTo[0]-facing[0];
				final double fdist2=(facing[1]>dirTo[1])?facing[1]-dirTo[1]:dirTo[1]-facing[1];
				if(fdist1>Math.PI)
					fdist1=(Math.PI*2)-fdist1;
				final double deltaTo=fdist1+fdist2;
				//final double deltaTo = CMLib.space().getAngleDelta(ship.facing(), dirTo);
				if(deltaTo < 0.02)
					super.addScreenMessage(L("Already facing @x1.",targetObj.name()));
				else
				{
					ShipDirectional.ShipDir portDir;
					if(facing[0]>dirTo[0])
					{
						if(fdist1 == facing[0]-dirTo[0])
							portDir=ShipDirectional.ShipDir.PORT;
						else
							portDir=ShipDirectional.ShipDir.STARBOARD;
					}
					else
					{
						if(fdist1 == dirTo[0]-facing[0])
							portDir=ShipDirectional.ShipDir.STARBOARD;
						else
							portDir=ShipDirectional.ShipDir.PORT;
					}
					final ShipEngine engineE=findEngineByPort(portDir);
					if(engineE==null)
					{
						super.addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
						return;
					}
					double[] oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
					String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
					msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, message))
					{
						if(oldFacing[0]==ship.facing()[0])
						{
							super.addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return;
						}
						else
						if(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)<.05)
						{}
						else
						{
							super.addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)*100.0)));
							return;
						}
						if(facing[1]>dirTo[1])
							portDir=ShipDirectional.ShipDir.VENTRAL;
						else
							portDir=ShipDirectional.ShipDir.DORSEL;
						code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
						oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
						msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(sendMessage(mob, engineE, msg, message))
						{
							if(oldFacing[1]==ship.facing()[1])
							{
								super.addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
								return;
							}
							else
							if(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)<.05)
								super.addScreenMessage(L("Now facing @x1.",targetObj.name()));
							else
							{
								super.addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
										portDir.name(),""+Math.round(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)*100.0)));
								return;
							}
						}
					}
				}
				return;
			}
			else
			if(uword.equalsIgnoreCase("CANCEL"))
			{
				if(this.rocketState == null)
				{
					super.addScreenMessage(L("Error: No programs running."));
					return;
				}
				switch(this.rocketState)
				{
				case APPROACH:
				case DEPROACH:
					super.addScreenMessage(L("Error: Approach program stopped."));
					break;
				case LANDING:
				case LANDING_APPROACH:
				case PRE_LANDING_STOP:
					super.addScreenMessage(L("Error: Landing program stopped."));
					break;
				case LAUNCHCHECK:
				case LAUNCHCRUISE:
				case LAUNCHSEARCH:
					super.addScreenMessage(L("Error: Launch program stopped."));
					break;
				case ORBITCHECK:
				case ORBITCRUISE:
				case ORBITSEARCH:
					super.addScreenMessage(L("Error: Orbit program stopped."));
					break;
				case STOP:
					super.addScreenMessage(L("Error: Stop program stopped."));
					break;
				}
				this.rocketState = null;
				return;
			}
			else
			if(uword.equalsIgnoreCase("APPROACH"))
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(parsed.size()<2)
				{
					super.addScreenMessage(L("Error: APPROACH requires the name of the object.   Try HELP."));
					return;
				}
				if(sensorReps.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify object."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
				if(targetObj == null)
				{
					super.addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
					return;
				}
				if(targetObj.coordinates() == null)
				{
					super.addScreenMessage(L("Can not approach @x1 due to lack of coordinate information.",targetObj.name()));
					return;
				}
				this.approachTarget = targetObj;
				final long distance = CMLib.space().getDistanceFrom(ship, targetObj);
				this.deproachDistance = (distance - ship.radius() - targetObj.radius())/2;
				if(deproachDistance < 100)
				{
					super.addScreenMessage(L("Can not approach @x1 due being too close.",targetObj.name()));
					return;
				}
				ShipEngine engineE=null;
				final double[] dirTo = CMLib.space().getDirection(ship, targetObj);
				if(!this.changeFacing(ship, dirTo))
				{
					super.addScreenMessage(L("Warning. Approach program cancelled due to engine failure."));
					this.rocketState=null;
					this.programEngines=null;
					return;
				}
				engineE=this.primeMainThrusters(ship,ship.getIsDocked()!=null);
				if(engineE==null)
				{
					this.rocketState=null;
					this.programEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				this.programEngines=new XVector<ShipEngine>(engineE);
				this.rocketState = RocketStateMachine.APPROACH;
				super.addScreenMessage(L("Approach to @x1 procedure engaged.",targetObj.name()));
				return;
			}
			else
			if(uword.equalsIgnoreCase("MOON"))
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning hull interface."));
					return;
				}
				if(parsed.size()<2)
				{
					super.addScreenMessage(L("Error: MOON requires the name of the object.   Try HELP."));
					return;
				}
				if(sensorReps.size()==0)
				{
					super.addScreenMessage(L("Error: no sensor data found to identify object."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : sensors)
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
				if(targetObj == null)
				{
					super.addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
					return;
				}
				if(targetObj.coordinates() == null)
				{
					super.addScreenMessage(L("Can not moon @x1 due to lack of coordinate information.",targetObj.name()));
					return;
				}
				final double[] facing=ship.facing();
				final double[] notDirTo=CMLib.space().getDirection(spaceObject, targetObj);
				final double[] dirTo = CMLib.space().getOppositeDir(notDirTo);
				double fdist1=(facing[0]>dirTo[0])?facing[0]-dirTo[0]:dirTo[0]-facing[0];
				final double fdist2=(facing[1]>dirTo[1])?facing[1]-dirTo[1]:dirTo[1]-facing[1];
				if(fdist1>Math.PI)
					fdist1=(Math.PI*2)-fdist1;
				final double deltaTo=fdist1+fdist2;
				//final double deltaTo = CMLib.space().getAngleDelta(ship.facing(), dirTo);
				if(deltaTo < 0.02)
					super.addScreenMessage(L("Already mooning @x1.",targetObj.name()));
				else
				{
					ShipDirectional.ShipDir portDir;
					if(facing[0]>dirTo[0])
					{
						if(fdist1 == facing[0]-dirTo[0])
							portDir=ShipDirectional.ShipDir.PORT;
						else
							portDir=ShipDirectional.ShipDir.STARBOARD;
					}
					else
					{
						if(fdist1 == dirTo[0]-facing[0])
							portDir=ShipDirectional.ShipDir.STARBOARD;
						else
							portDir=ShipDirectional.ShipDir.PORT;
					}
					final ShipEngine engineE=findEngineByPort(portDir);
					if(engineE==null)
					{
						super.addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
						return;
					}
					double[] oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
					String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
					msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, message))
					{
						if(oldFacing[0]==ship.facing()[0])
						{
							super.addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return;
						}
						else
						if(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)<.05)
						{}
						else
						{
							super.addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)*100.0)));
							return;
						}
						if(facing[1]>dirTo[1])
							portDir=ShipDirectional.ShipDir.VENTRAL;
						else
							portDir=ShipDirectional.ShipDir.DORSEL;
						code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
						oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
						msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(sendMessage(mob, engineE, msg, message))
						{
							if(oldFacing[1]==ship.facing()[1])
							{
								super.addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
								return;
							}
							else
							if(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)<.05)
								super.addScreenMessage(L("Now mooning @x1.",targetObj.name()));
							else
							{
								super.addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
										portDir.name(),""+Math.round(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)*100.0)));
								return;
							}
						}
					}
				}
				return;
			}
			else
			if(uword.equalsIgnoreCase("FIRE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
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
					allObjects.addAll(takeNewSensorReport(sensor));
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
				//final double[] targetDirection = CMLib.space().getDirection(ship.coordinates(), CMLib.space().moveSpaceObject(targetObj.coordinates(), targetObj.direction(), (long)targetObj.speed()));
				double[] targetDirection = CMLib.space().getDirection(ship.coordinates(), targetObj.coordinates());
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
					Log.debugOut("Fire: "+ship.Name()+" -> "+targetObj.Name()+"@"+Math.toDegrees(targetDirection[0])+","+Math.toDegrees(targetDirection[1]));
				TechComponent finalWeaponToFire = null;
				final String weapName = CMParms.combine(parsed,1);
				if(weapName.length()>0)
				{
					final TechComponent weapon = this.findWeaponByName(rest);
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
						if(T.activated())
						{
							if(T instanceof ShipDirectional)
							{

								final ShipDir dir = CMLib.space().getDirectionFromDir(ship.facing(), ship.roll(), targetDirection);
								if(CMParms.contains(CMLib.space().getCurrentBattleCoveredDirections((ShipDirectional)T), dir))
								{
									finalWeaponToFire = T;
									break;
								}
							}
							else
								finalWeaponToFire = T;
						}
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
					super.addScreenMessage(L("Info: Auto selected weapon '@x1'.",finalWeaponToFire.name()));
				}
				{
					E=finalWeaponToFire;
					String code;
					code=TechCommand.TARGETSET.makeCommand(Long.valueOf(targetObj.coordinates()[0]),
														   Long.valueOf(targetObj.coordinates()[1]),
														   Long.valueOf(targetObj.coordinates()[2]));
					msg=CMClass.getMsg(mob, finalWeaponToFire, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if((finalWeaponToFire.getTechType()!=Technical.TechType.SHIP_LAUNCHER)
					||sendMessage(mob, finalWeaponToFire, msg, message))
					{
						if((finalWeaponToFire.getTechType()==Technical.TechType.SHIP_LAUNCHER)
						&&(finalWeaponToFire instanceof Container))
						{
							final List<Item> ammo = ((Container)finalWeaponToFire).getContents();
							if((ammo.size()>0)&&(ammo.get(0) instanceof SpaceObject))
							{
								final Item ammoI=ammo.get(0);
								final SpaceObject ammoO=(SpaceObject)ammoI;
								// expect the weapon to be slow.
								// first out of the toob is well directioned
								final SpaceObject targetO=targetObj;
								if(targetO != null)
								{
									// use initial direction to calculate starting position
									final double futureAccellerationInSameDirectionAsAmmo = 4.0; //TODO: magic number
									//TODO: adding ship.speed() here is still wrong because you could be firing aft.
									//The initial position of a launched object is tricky.
									ammoO.setCoords(CMLib.space().moveSpaceObject(ship.coordinates(), targetDirection,
											(int)Math.round(ship.radius()+ammoO.radius()+ship.speed()
											+futureAccellerationInSameDirectionAsAmmo)));
									final long maxChaseTimeMs = 300000; //TODO: magic numbers suck
									final int maxTicks = (int)(maxChaseTimeMs/CMProps.getTickMillis());
									final double maxSpeed = CMath.mul((ammoI.phyStats().speed()/100.0), SpaceObject.VELOCITY_LIGHT);
									final Pair<double[], Long> intercept = CMLib.space().calculateIntercept(ammoO, targetO, Math.round(maxSpeed), maxTicks);
									if(intercept != null)
									{
										ammoO.setSpeed(intercept.second.longValue());
										targetDirection=intercept.first;
									}
								}
							}
						}
						code=TechCommand.AIMSET.makeCommand(Double.valueOf(targetDirection[0]), Double.valueOf(targetDirection[1]));
						msg=CMClass.getMsg(mob, finalWeaponToFire, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(sendMessage(mob, finalWeaponToFire, msg, message))
						{
							code = TechCommand.FIRE.makeCommand();
							msg=CMClass.getMsg(mob, finalWeaponToFire, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						}
						else
							msg=null;
					}
					else
						msg=null;
				}
			}
			else
			if(uword.startsWith("WEAPON"))
			{
				final TechComponent weapon = this.findWeaponByName(uword);
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
				code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(pct * weapon.powerCapacity())));
				msg=CMClass.getMsg(mob, weapon, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			}
			else
			if(uword.startsWith("SHIELD"))
			{
				if(uword.startsWith("SHIELDS"))
				{
					if(parsed.size()==1)
					{
						super.addScreenMessage(L("Error: No UP or DOWN instruction."));
						return;
					}
					if(getShipShields().size()==0)
					{
						super.addScreenMessage(L("Error: No shields found."));
						return;
					}
					if(parsed.get(1).equalsIgnoreCase("UP"))
					{
						for(int s=0;s<getShipShields().size();s++)
						{
							final TechComponent shield = getShipShields().get(s);
							E=shield;
							final String code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(shield.powerCapacity())));
							msg=CMClass.getMsg(mob, shield, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
							if(s<getShipShields().size()-1)
								sendMessage(mob,E,msg,message);
						}
					}
					else
					if(parsed.get(1).equalsIgnoreCase("DOWN"))
					{
						for(int s=0;s<getShipShields().size();s++)
						{
							final TechComponent shield = getShipShields().get(s);
							E=shield;
							msg=CMClass.getMsg(mob, shield, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
							if(s<getShipShields().size()-1)
								sendMessage(mob,E,msg,message);
						}
					}
					else
					{
						super.addScreenMessage(L("Error: No UP or DOWN instruction."));
						return;
					}
				}
				else
				{
					final ShipWarComponent shield = this.findShieldByName(uword);
					if(shield == null)
					{
						super.addScreenMessage(L("Error: Unknown shield name or command word '"+uword+"'.   Try HELP."));
						return;
					}
					if(parsed.size()==1)
					{
						super.addScreenMessage(L("Error: No power percentage given."));
						return;
					}
					final String emission=parsed.get(1);
					if(!CMath.isPct(emission))
					{
						super.addScreenMessage(L("Error: Invalid power percentage given."));
						return;
					}
					final double pct=CMath.s_pct(emission);
					if((pct < 0)||(pct > 1))
					{
						super.addScreenMessage(L("Error: Invalid power percentage given."));
						return;
					}
					E=shield;
					String code;

					code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(pct * shield.powerCapacity())));
					msg=CMClass.getMsg(mob, shield, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				}
			}
			else
			if(uword.startsWith("SENSOR"))
			{
				final TechComponent sensor = this.findSensorByName(uword);
				if(sensor == null)
				{
					super.addScreenMessage(L("Error: Unknown sensor name or command word '"+uword+"'.   Try HELP."));
					return;
				}
				if(parsed.size()==1)
				{
					super.addScreenMessage(L("Error: No direction given."));
					return;
				}
				E=sensor;
				final List<ShipDir> dirs = new ArrayList<ShipDir>();
				parsed.remove(0);
				for(final String dirStr : parsed)
				{
					final ShipDir dir=(ShipDir)CMath.s_valueOf(ShipDir.class, dirStr.toUpperCase().trim());
					if(dir==null)
					{
						super.addScreenMessage(L("Error: Invalid direction given."));
						return;
					}
					dirs.add(dir);
				}
				String code;
				code=TechCommand.DIRSET.makeCommand(dirs.get(0));
				msg=CMClass.getMsg(mob, sensor, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				for(int i=1;i<dirs.size();i++)
				{
					sendMessage(mob,E,msg,message);
					code=TechCommand.DIRSET.makeCommand(dirs.get(1));
					msg=CMClass.getMsg(mob, sensor, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				}
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
				ShipDirectional.ShipDir portDir=ShipDirectional.ShipDir.AFT;
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
					portDir=(ShipDirectional.ShipDir)CMath.s_valueOf(ShipDirectional.ShipDir.class, parsed.get(1).toUpperCase().trim());
					if(portDir!=null)
					{
						if(!CMParms.contains(engineE.getAvailPorts(), portDir))
						{
							super.addScreenMessage(L("Error: '@x1' is not a valid direction for that engine.  Try: @x2.",parsed.get(1),CMParms.toListString(engineE.getAvailPorts())));
							return;
						}
					}
					else
					if("aft".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), ShipDirectional.ShipDir.AFT))
						portDir=ShipDirectional.ShipDir.AFT;
					else
					if("port".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), ShipDirectional.ShipDir.PORT))
						portDir=ShipDirectional.ShipDir.PORT;
					else
					if("starboard".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), ShipDirectional.ShipDir.STARBOARD))
						portDir=ShipDirectional.ShipDir.STARBOARD;
					else
					if("ventral".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), ShipDirectional.ShipDir.VENTRAL))
						portDir=ShipDirectional.ShipDir.VENTRAL;
					else
					if("dorsel".startsWith(parsed.get(1).toLowerCase()) && CMParms.contains(engineE.getAvailPorts(), ShipDirectional.ShipDir.DORSEL))
						portDir=ShipDirectional.ShipDir.DORSEL;
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
	protected void onActivate(final MOB mob, final String message)
	{
		super.onActivate(mob, message);
		if((message!=null)&&(message.length()>0))
			onTyping(mob,message);
	}

	@Override
	protected void onDeactivate(final MOB mob, final String message)
	{
		super.onDeactivate(mob, message);
		if(message == null)
		{
			// whole system shutdown
			this.decache();
			return;
		}
		final Vector<String> parsed=CMParms.parse(message);
		if(parsed.size()==0)
		{
			super.addScreenMessage(L("Syntax Error!"));
			Log.debugOut("Strange program deactivation: ",new Exception());
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
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			engines = null;
			nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
			if(--activationCounter <= 0)
			{
				activationCounter = DEFAULT_ACT_8_SEC_COUNTDOWN;
				sensors = null;
			}
			else // periodically take a new sensor report
			{
				for(final TechComponent sensor : getShipSensors())
					takeNewSensorReport(sensor);
			}
		}
		if(sensors == null)
		{
			for(final TechComponent sensor : getShipSensors())
			{
				if(sensor.activated()
				&&(!activated.contains(sensor)))
				{
					// make sure sensors are activated BY this software, so it has a feedback mech
					final MOB mob=CMClass.getFactoryMOB();
					try
					{
						final CMMsg msg=CMClass.getMsg(mob, sensor, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE, null, CMMsg.NO_EFFECT,null);
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
					activated.add(sensor);
				}
			}
		}

	}

	protected String getDataName(final String realName, final String coords, final String notName)
	{
		final String[] parms = new String[] {coords, realName};
		final List<String[]> names = super.doServiceTransaction(SWServices.IDENTIFICATION, parms);
		for(final String[] res : names)
		{
			for(final String r : res)
			{
				if((r.length()>0)
				&&(!r.equals(realName))
				&&(!r.equals(coords))
				&&(!r.equals(notName)))
					return r;
			}
		}
		return "";
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
				if(msg.isTarget(CMMsg.MASK_CNTRLMSG)
				&& (msg.targetMessage()!=null))
				{
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(msg.targetMessage());
					if((command == TechCommand.SENSE)
					&& (msg.tool() instanceof SpaceObject)) // this is probably a sensor report
					{
						final Object[] parms=command.confirmAndTranslate(msg.targetMessage());
						if((parms!=null)&&(parms.length>1))
						{
							final TechComponent sensorSystem = findComponentByID(getShipSensors(), parts[1]);
							final Boolean tf=(parms[1] instanceof Boolean)?(Boolean)parms[1]:Boolean.TRUE;
							final Set<SpaceObject> sensorReport=getLocalSensorReport(sensorSystem);
							final SpaceObject spaceObj = (SpaceObject)msg.tool();
							if(tf.booleanValue())
							{
								sensorReport.add(spaceObj);
								if((!spaceObj.Name().equals(spaceObj.name()))
								&&(svcs.containsKey(SWServices.IDENTIFICATION)))
								{
									final String coords = CMParms.toListString(spaceObj.coordinates());
									final String realName=spaceObj.Name();
									final String notName = spaceObj.name();
									final String name = getDataName(realName, coords, notName);
									if((name!=null)&&(name.length()>0))
										spaceObj.setName(name);
								}
							}
							else
								sensorReport.remove(spaceObj);
						}
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
			final TechCommand command=TechCommand.findCommand(msg.targetMessage());
			if(command == TechCommand.ACCELERATED)
			{
				final Object[] parms=command.confirmAndTranslate(msg.targetMessage());
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
		else
		if((msg.targetMinor()==CMMsg.TYP_ACTIVATE)
		&&(msg.target() instanceof TechComponent)
		&&(((TechComponent)msg.target()).getTechType() == TechType.SHIP_SENSOR))
		{
			if(!activated.contains(msg.target()))
				activationCounter=0;
		}

		if((container() instanceof Computer)
		&&(msg.target() == container())
		&&(msg.targetMinor() == CMMsg.TYP_DEACTIVATE))
		{
			this.components = null;
			this.engines = null;
			this.sensors = null;
			this.sensorReps.clear();
		}
		super.executeMsg(host,msg);
	}

	@Override
	protected void provideService(final SWServices service, final Software S, final String[] parms, final CMMsg msg)
	{
		if((service == SWServices.TARGETING)
		&&(S!=null)
		&&(S!=this)
		&&(this.currentTarget != null))
		{
			final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
			try
			{
				final String Name = currentTarget.Name();
				final String name = currentTarget.name();
				String coords = "";
				if((currentTarget.speed()==0)
				&&(currentTarget.radius()>SpaceObject.Distance.AsteroidRadius.dm))
					coords = CMParms.toListString(currentTarget.coordinates());
				final String code=TechCommand.SWSVCRES.makeCommand(service,new String[] { Name,name,coords });
				final CMMsg msg2=CMClass.getMsg(factoryMOB, S, this,
								CMMsg.NO_EFFECT, null,
								CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, code,
								CMMsg.NO_EFFECT, null);
				msg2.setTargetMessage(code);
				msg.addTrailerMsg(msg2);
			}
			finally
			{
				factoryMOB.destroy();
			}
		}
		else
		if((service == SWServices.COORDQUERY)
		&&(S!=null)
		&&(S!=this)
		&&(parms.length>0))
		{
			final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
			final List<SpaceObject> allObjects = new ArrayList<SpaceObject>();
			for(final TechComponent sensor : sensors)
				allObjects.addAll(takeNewSensorReport(sensor));
			Collections.sort(allObjects, new DistanceSorter(spaceObject));
			for(final String parm : parms)
			{
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, parm, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, parm, true);
				final long[] coords=(targetObj!=null) ? targetObj.coordinates() : null;
				if(coords != null)
				{
					final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
					try
					{
						final String code=TechCommand.SWSVCRES.makeCommand(service,new String[] { CMParms.toListString(coords) });
						final CMMsg msg2=CMClass.getMsg(factoryMOB, S, this,
								CMMsg.NO_EFFECT, null,
								CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, code,
								CMMsg.NO_EFFECT, null);
						msg2.setTargetMessage(code);
						msg.addTrailerMsg(msg2);
					}
					finally
					{
						factoryMOB.destroy();
					}
				}
			}
		}
	}
}
