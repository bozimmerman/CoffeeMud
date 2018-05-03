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
	
	protected volatile Double			lastThrust		= null;
	protected volatile List<ShipEngine> launchEngines	= null;
	protected final List<CMObject>		sensorReport	= new LinkedList<CMObject>();

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
		if(uword.equals("ENGINEHELP")
		||uword.equals("HELP")
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
			str.append("^H").append(CMStrings.padRight(L("[ENGINEHELP]/[SHIELDHELP]/[WEAPONHELP] : Get details."),60)).append("\n\r");
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
	public boolean checkPowerCurrent(int value)
	{
		final List<ShipEngine> engines = launchEngines;
		if((engines != null) && (engines.size() > 0))
		{
			final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
			final List<SpaceObject> orbs=CMLib.map().getSpaceObjectsWithin(spaceObject,0,SpaceObject.Distance.LightMinute.dm);
			if(spaceObject.speed()>0)
			{
				for(final SpaceObject orb : orbs)
				{
					if(orb instanceof Area)
					{
						final long distance=CMLib.map().getDistanceFrom(spaceObject, orb);
						if((distance > (orb.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
						||(distance > orb.radius()*SpaceObject.MULTIPLIER_ORBITING_RADIUS_MAX))
						{
							//TODO: only if it is the proper direction away .. is this what I actually launched from?
							System.out.println("*****LAUNCH COMPLETE++++");
							this.launchEngines=null;
							//this.lastSpeed=0.0;
							//this.lastThrust=0;
							//bestGuessThrusts.clear();
						}
						if(((distance > orb.radius())&&(distance < (orb.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
						||((distance > orb.radius()*SpaceObject.MULTIPLIER_ORBITING_RADIUS_MIN)&&(distance<orb.radius()*SpaceObject.MULTIPLIER_ORBITING_RADIUS_MAX)))
						{
							final double[] directionFromMeToOrb = CMLib.map().getDirection(spaceObject, orb);
							final double[] myDirection=Arrays.copyOf(spaceObject.direction(),2);
							myDirection[0]=directionFromMeToOrb[0]+Math.PI;
							if(myDirection[0] > (2*Math.PI))
								myDirection[0] = Math.abs(myDirection[0]-(2*Math.PI));
							myDirection[1]=directionFromMeToOrb[1]+(Math.PI/2.0);
							if(myDirection[1] > Math.PI)
								myDirection[1] = Math.abs(myDirection[1]-Math.PI);
							//TODO: this would be better
						}
					}
				}
			}
		}
		return true;
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
				super.addScreenMessage(L("^HHELP:^N\n\r^N"+"The ACTIVATE command can be used to turn on any engine, "
					+ "sensor, or other system in your ship.  The DEACTIVATE command will turn off any system specified. "
					+ "LAUNCH will take your ship off away from the planet. "
					+ "STOP will attempt to negate all velocity. "
					+ "Otherwise, see ENGINE help for engine commands."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("ENGINEHELP"))
			{
				super.addScreenMessage(L("^HENGINEHELP:^N\n\r^N"+"The ENGINE command instructs the given " +
						"engine number or name to fire in the appropriate direction. What happens, " +
						"and how quickly, depends largely on the capabilities of the engine. " +
						"Giving a direction is optional, and if not given, AFT is assumed. All "+
						"directions result in corrected bursts, except for AFT, which will result " +
						"in sustained accelleration."));
				return;
			}
			CMMsg msg = null;
			Electronics E  = null;
			if(uword.equalsIgnoreCase("ACTIVATE") || uword.equalsIgnoreCase("DEACTIVATE"))
			{
				final String rest = CMParms.combine(parsed,1).toUpperCase();
				String code = null;
				E=findEngineByName(rest);
				if(E!=null)
					code=Technical.TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(.0000001));
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
				final List<ShipEngine> readyEngines = new ArrayList<ShipEngine>(1);
				final List<ShipEngine> engines = getEngines();
				final MOB M=CMClass.getFactoryMOB();
				try
				{
					final double accellerationTarget = SpaceObject.ACCELLERATION_TYPICALROCKET-1.0;
					for(ShipEngine engineE : engines)
					{
						if((CMParms.contains(engineE.getAvailPorts(),TechComponent.ShipDir.AFT))
						&&(engineE.getMaxThrust()>SpaceObject.ACCELLERATION_G)
						&&(engineE.getMinThrust()<SpaceObject.ACCELLERATION_PASSOUT))
						{
							int tries=10000;
							double lastTryAmt=0.001;
							final CMMsg deactMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
							msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
							while((readyEngines.size()==0)&&(--tries>0))
							{
								this.lastThrust=null;
								final String code=Technical.TechCommand.THRUST.makeCommand(TechComponent.ShipDir.AFT,Double.valueOf(lastTryAmt));
								msg.setTargetMessage(code);
								this.trySendMsgToItem(mob, engineE, msg);
								if((this.lastThrust!=null)&&(this.lastThrust.doubleValue()>0.0))
								{
									if(this.lastThrust.doubleValue() >= (accellerationTarget *0.9))
										readyEngines.add(engineE);
									else
									{
										this.trySendMsgToItem(mob, engineE, deactMsg);
										double newDivider=this.lastThrust.doubleValue() / lastTryAmt;
										lastTryAmt = accellerationTarget / newDivider;
									}
								}
								else
								{
									this.trySendMsgToItem(mob, engineE, deactMsg);
									lastTryAmt += 0.001;
								}
							}
						}
					}
				}
				finally
				{
					M.destroy();
				}
				if(readyEngines.size()==0)
				{
					this.launchEngines = null;
					super.addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
					return;
				}
				//this.lastSpeed=0.0;
				//this.lastThrust=0;
				this.launchEngines=readyEngines;
				super.addScreenMessage(L("Launch procedure initialized."));
				return;
			}
			else
			if(uword.equalsIgnoreCase("STOP"))
			{
				//TODO:
			}
			else
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
					final String code=Technical.TechCommand.THRUST.makeCommand(portDir,Double.valueOf(amount));
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
	public void executeMsg(Environmental host, CMMsg msg)
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
		&&(lastThrust==null)
		&&(msg.targetMinor()==CMMsg.TYP_ACTIVATE)
		&&(msg.isTarget(CMMsg.MASK_CNTRLMSG))
		&&(msg.targetMessage()!=null))
		{
			final String[] parts=msg.targetMessage().split(" ");
			final TechCommand command=TechCommand.findCommand(parts);
			if(command == TechCommand.THRUSTED)
			{
				final Object[] parms=command.confirmAndTranslate(parts);
				if((parms!=null)&&(parms[0]==ShipDir.AFT))
				{
					this.lastThrust=(Double)parms[1];
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
