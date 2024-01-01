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
   Copyright 2013-2024 Bo Zimmerman

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
public class RocketShipProgram extends ShipTacticalProgram
{
	@Override
	public String ID()
	{
		return "RocketShipProgram";
	}

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
			catch(final NullPointerException npe)
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
			for(final TechComponent component : getSystemMiscComponents())
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
				if(component instanceof FuelConsumer)
				{
					str.append("^H").append(CMStrings.padRight(" ",9));
					str.append(CMStrings.padRight("  ",2));
					str.append("^H").append(CMStrings.padRight(L("Fuel"),5));
					str.append("^N").append(CMStrings.padRight(Long.toString(((FuelConsumer)component).getFuelRemaining()),11));
					str.append("^.^N\n\r");
				}
				systemNumber++;
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

	@Override
	protected void onTyping(final MOB mob, final String message)
	{
		synchronized(this)
		{
			super.forceUpMenu();
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
					addScreenMessage(CMLib.lang().L(
							  "^HHELP:^N\n\r^N"
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
					final Electronics E  = findEngineByName(secondWord);
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
					final Electronics E=this.findSensorByName(secondWord);
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
					final List<TechComponent> others = getSystemMiscComponents();
					final Electronics E=findComponentByName(others,"SYSTEM",secondWord);
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
			if(uword.equalsIgnoreCase("ACTIVATE"))
			{
				if(!activateProcedure.execute(this,uword,mob,message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("DEACTIVATE"))
			{
				if(!deactivateProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("LAUNCH") || uword.equalsIgnoreCase("ORBIT"))
			{
				if(!launchProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("STOP"))
			{
				if(!stopProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("LAND"))
			{
				if(!landProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("TARGET"))
			{
				if(!targetProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("COURSE"))
			{
				if(!courseProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("FACE"))
			{
				if(!faceProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("CANCEL"))
			{
				if(!cancelProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("APPROACH"))
			{
				if(!approachProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("MOON"))
			{
				if(!moonProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.equalsIgnoreCase("FIRE"))
			{
				if(!fireProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.startsWith("WEAPON"))
			{
				if(!weaponProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.startsWith("SHIELD"))
			{
				if(!shieldProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(uword.startsWith("SENSOR"))
			{
				if(!sensorProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
			else
			if(!uword.equalsIgnoreCase("HELP"))
			{
				final ShipEngine engineE=findEngineByName(uword);
				if(engineE==null)
				{
					addScreenMessage(L("Error: Unknown engine name or command word '"+uword+"'.   Try HELP."));
					return;
				}
				if(!engineProcedure.execute(this, uword, mob, message, parsed))
					return;
			}
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

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
	}


}
