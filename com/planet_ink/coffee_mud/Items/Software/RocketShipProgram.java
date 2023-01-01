package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javax.xml.transform.Source;

/*
   Copyright 2013-2023 Bo Zimmerman

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
public class RocketShipProgram extends ShipNavProgram
{
	@Override
	public String ID()
	{
		return "RocketShipProgram";
	}

	protected volatile SpaceObject			currentTarget		= null;

	protected final static long[] 	emptyCoords = new long[] {0,0,0};
	protected final static double[] emptyDirection = new double[] {0,0};

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
	protected SWServices[] getProvidedServices()
	{
		return new SWServices[] { Software.SWServices.TARGETING };
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
		||uword.equals("COURSE")
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
		if((this.navTrack != null)
		&&(this.navTrack.proc != null))
		{
			str.append("^H").append(CMStrings.padRight(L("Running"),10));
			final StringBuilder nstr = new StringBuilder(navTrack.proc.name());
			try
			{
				final SpaceObject obj = navTrack.getArg(SpaceObject.class);
				if(obj != null)
					nstr.append(" ").append(obj.name());
			}
			catch(NullPointerException npe)
			{}
			if(navTrack.state != null)
				nstr.append(" (").append(navTrack.state.name()).append(")");
			str.append("^N").append(CMStrings.padRight(nstr.toString(),50));
			str.append("^.^N\n\r");
		}
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
					{
						final SpaceObject spaceMe = ship;
						final List<SpaceObject> sortedReport = new ArrayList<SpaceObject>(localSensorReport.size());
						sortedReport.addAll(localSensorReport);
						Collections.sort(sortedReport, new DistanceSorter(spaceMe));
						for(final Object o : sortedReport)
						{
							if(o == spaceObject)
								continue;
							if(o instanceof SpaceObject)
							{
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
				if(this.navTrack!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.cancelNavigation();
				}
				final SpaceObject programPlanet=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
				final ShipEngine engineE =this.primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
				if(engineE==null)
				{
					super.addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
					return;
				}
				if(findTargetAcceleration(engineE) < SpaceObject.ACCELERATION_DAMAGED)
				{
					int gs = (int)Math.round(this.targetAcceleration.doubleValue()/SpaceObject.ACCELERATION_G);
					super.addScreenMessage(L("No inertial dampeners found.  Limiting acceleration to "+gs+"Gs."));
				}
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				if(uword.equalsIgnoreCase("ORBIT"))
					this.navTrack = new ShipNavTrack(ShipNavProcess.ORBIT, programPlanet, programEngines);
				else
					this.navTrack = new ShipNavTrack(ShipNavProcess.LAUNCH, programPlanet, programEngines);
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
				if(this.navTrack!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.cancelNavigation();
				}
				ShipEngine engineE=null;
				if(!flipForAllStop(ship))
				{
					super.addScreenMessage(L("Warning. Stop program cancelled due to engine failure."));
					this.cancelNavigation();
					return;
				}
				else
					engineE=this.primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
				if(engineE==null)
				{
					this.cancelNavigation();
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				this.navTrack = new ShipNavTrack(ShipNavProcess.STOP, programEngines);
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

				if(this.navTrack!=null)
				{
					super.addScreenMessage(L("Warning. Previous program cancelled."));
					this.cancelNavigation();
				}
				ShipEngine engineE=null;
				if(!flipForAllStop(ship))
				{
					super.addScreenMessage(L("Warning. Landing program cancelled due to engine failure."));
					this.cancelNavigation();
					return;
				}
				else
					engineE=this.primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
				if(engineE==null)
				{
					this.cancelNavigation();
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				final SpaceObject programPlanet = landingPlanet;
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				// this lands you at the nearest point, which will pick the nearest location room, if any
				//TODO: picking the nearest landing zone, orbiting to it, and THEN landing would be better.
				this.navTrack = new ShipNavTrack(ShipNavProcess.LAND, programPlanet, programEngines);
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
			if(uword.equalsIgnoreCase("COURSE"))
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
					super.addScreenMessage(L("Error: COURSE requires the name/coordinates of the target.   Try HELP."));
					return;
				}
				final String targetStr=CMParms.combine(parsed, 1);
				long[] targetCoords = null;
				if(sensorReps.size()>0)
				{
					final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
					for(final TechComponent sensor : sensors)
						allObjects.addAll(takeNewSensorReport(sensor));
					Collections.sort(allObjects, new DistanceSorter(spaceObject));
					SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
					if(targetObj == null)
						targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
					if(targetObj != null)
					{
						if(targetObj.coordinates() == null)
						{
							super.addScreenMessage(L("Error: Can not plot course to @x1 due to lack of coordinate information.",targetObj.name()));
							return;
						}
						targetCoords = targetObj.coordinates();
					}
				}
				if(targetCoords == null)
					targetCoords = findCoordinates(targetStr);
				if(targetCoords == null)
				{
					super.addScreenMessage(L("Error: Unable to find course target '@x1'.",targetStr));
					return;
				}
				else
				{
					// yes, it's cheating.  deal
					final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(targetCoords, 0, 10);
					for(final SpaceObject o1 : objs)
					{
						if(Arrays.equals(targetCoords, o1.coordinates()))
							this.courseTargetRadius = o1.radius();
					}
				}
				this.course.clear();
				this.courseTargetCoords = targetCoords;
				super.addScreenMessage(L("Plotting course to @x1.",CMParms.toListString(this.courseTargetCoords)));
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
				if(this.navTrack == null)
				{
					super.addScreenMessage(L("Error: No programs running."));
					return;
				}
				final String name = CMStrings.capitalizeAndLower(navTrack.proc.name());
				super.addScreenMessage(L("Confirmed: "+name+" program stopped."));
				this.cancelNavigation();
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
				final SpaceObject approachTarget = targetObj;
				long distance = CMLib.space().getDistanceFrom(ship, targetObj);
				distance = (distance - ship.radius() - targetObj.radius())/2;
				if(distance < 100)
				{
					super.addScreenMessage(L("Can not approach @x1 due being too close.",targetObj.name()));
					return;
				}
				long deproachDistance = calculateDeproachDistance(ship, targetObj);
				ShipEngine engineE=null;
				final double[] dirTo = CMLib.space().getDirection(ship, targetObj);
				if(!this.changeFacing(ship, dirTo))
				{
					super.addScreenMessage(L("Warning. Approach program cancelled due to engine failure."));
					this.cancelNavigation();
					return;
				}
				engineE=this.primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
				if(engineE==null)
				{
					this.cancelNavigation();
					super.addScreenMessage(L("Error: Malfunctioning thrusters interface."));
					return;
				}
				findTargetAcceleration(engineE);
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				this.navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, approachTarget, programEngines, Long.valueOf(deproachDistance));
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
	}

	protected long[] convertStringToCoords(final String coordStr)
	{
		final List<String> coordCom = CMParms.parseCommas(coordStr,true);
		if(coordCom.size()==3)
		{
			final long[] coords=new long[3];
			for(int i=0;(i<coordCom.size()) && (i<3);i++)
			{
				final Long coord=CMLib.english().parseSpaceDistance(coordCom.get(i));
				if(coord != null)
					coords[i]=coord.longValue();
				else
					return null;
			}
			return coords;
		}
		return null;
	}

	protected long[] findCoordinates(final String name)
	{
		final String[] parms = new String[] {name};
		final List<String[]> names = super.doServiceTransaction(SWServices.COORDQUERY, parms);
		for(final String[] res : names)
		{
			for(final String r : res)
			{
				if(r.length()>0)
				{
					final long[] coords = convertStringToCoords(r);
					if(coords !=null)
						return coords;
				}
			}
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
	}

	@Override
	protected void provideService(final SWServices service, final Software S, final String[] parms, final CMMsg msg)
	{
		super.provideService(service, S, parms, msg);
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
	}

	@Override
	protected void decache()
	{
		super.decache();
		currentTarget = null;
	}

}
