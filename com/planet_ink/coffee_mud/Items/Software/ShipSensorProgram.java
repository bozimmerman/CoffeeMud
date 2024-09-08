package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.BasicTech.GenElecItem;
import com.planet_ink.coffee_mud.Items.Software.ShipSensorProgram.DistanceSorter;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Software.SWServices;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;

/*
   Copyright 2022-2024 Bo Zimmerman

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
public class ShipSensorProgram extends GenShipProgram
{
	@Override
	public String ID()
	{
		return "ShipSensorProgram";
	}

	protected final static BigVector emptyCoords = new BigVector(new long[] {0,0,0});
	protected final static Dir3D  emptyDirection = new Dir3D();

	protected final Map<Technical, Set<SpaceObject>>	sensorReps	= new SHashtable<Technical, Set<SpaceObject>>();
	protected final Set<TechComponent>					activated	= Collections.synchronizedSet(new HashSet<TechComponent>());

	@Override
	protected void decache()
	{
		super.decache();
		sensorReps.clear();
		activated.clear();
	}

	@Override
	protected void onDeactivate(final MOB mob, final String message)
	{
		if(message == null)
		{
			sensorReps.clear();
			activated.clear();
		}
		super.onDeactivate(mob, message);
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

	protected static class DistanceSorter implements Comparator<SpaceObject>
	{
		private final GalacticMap space;
		private final SpaceObject spaceObject;

		protected DistanceSorter(final SpaceObject me)
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

	protected boolean containsSameCoordinates(final List<SpaceObject> objs, final BigVector coordinates)
	{
		for(final SpaceObject o : objs)
		{
			if(o.coordinates().equals(coordinates))
				return true;
		}
		return false;
	}

	@Override
	protected void onPowerTimer(final int value)
	{
		for(final TechComponent sensor : getShipSensors())
			takeNewSensorReport(sensor);
	}

	@Override
	protected void onActivationTimer(final int value)
	{
		sensors = null;
	}

	@Override
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if (System.currentTimeMillis() > nextPowerCycleTmr)
		{
			engines = null; // why only this?!
			nextPowerCycleTmr = System.currentTimeMillis() + (8 * 1000);
			if(--activationCounter <= 0) // so--- every 800 seconds?
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

	@Override
	protected SWServices[] getAppreciatedServices()
	{
		return new SWServices[] { Software.SWServices.IDENTIFICATION };
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
									final String coords = CMParms.toListString(spaceObj.coordinates().toLongs());
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
		if((msg.targetMinor()==CMMsg.TYP_ACTIVATE)
		&&(msg.target() instanceof TechComponent)
		&&(((TechComponent)msg.target()).getTechType() == TechType.SHIP_SENSOR))
		{
			if(!activated.contains(msg.target()))
				activationCounter=0;
		}
		super.executeMsg(host,msg);
	}

	protected SoftwareProcedure sensorProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			CMMsg msg = null;
			Electronics E;
			final TechComponent sensor = findSensorByName(uword);
			if(sensor == null)
			{
				addScreenMessage(L("Error: Unknown sensor name or command word '"+uword+"'.   Try HELP."));
				return false;
			}
			if(parsed.size()==1)
			{
				addScreenMessage(L("Error: No direction or target given."));
				return false;
			}
			E=sensor;
			final String combined = CMParms.combine(parsed,1).toLowerCase().trim();
			final Set<SpaceObject> sensorReport=getLocalSensorReport(sensor);
			SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(sensorReport, combined, false);
			if(targetObj == null)
				targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(sensorReport, combined, true);
			if(targetObj != null)
			{
				final SpaceObject spaceObject=CMLib.space().getSpaceObject(sensor,true);
				final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
				if(ship != null)
				{
					final PairList<String,String> entries = new PairArrayList<String,String>();
					final long distance = CMLib.space().getDistanceFrom(ship.coordinates(), targetObj.coordinates()) - ship.radius() - targetObj.radius();
					final Dir3D direction = CMLib.space().getDirection(ship, targetObj);
					entries.add("Identifier",targetObj.name());
					if(!targetObj.coordinates().equals(emptyCoords))
						entries.add("Direction",CMLib.english().directionDescShortest(direction.toDoubles()));
					if(!targetObj.coordinates().equals(emptyCoords))
						entries.add("Distance",CMLib.english().distanceDescShort(distance));
					if(!targetObj.direction().equals(emptyDirection))
						entries.add("Moving",CMLib.english().directionDescShortest(targetObj.direction().toDoubles()));
					if(targetObj.speed()>0)
						entries.add("Speed",CMath.abbreviateLong((long)targetObj.speed()));
					if(targetObj.radius()>0)
						entries.add("Size",CMLib.english().distanceDescShort(targetObj.radius()));
					if(targetObj.getMass()>0)
						entries.add("Mass",CMath.abbreviateLong(targetObj.getMass()));

					final StringBuilder str = new StringBuilder("");
					for(final Pair<String,String> p : entries)
						str.append("^H").append(CMStrings.padRight(L(p.first),12)).append(": ^N").append(p.second).append("\n\r");
					addScreenMessage(str.toString());
				}
				else
					addScreenMessage(L("Error: Error communicating with @x1.",sensor.name()));
				return false;
			}
			final List<ShipDir> dirs = new ArrayList<ShipDir>();
			parsed.remove(0);
			for(final String dirStr : parsed)
			{
				final ShipDir dir=(ShipDir)CMath.s_valueOf(ShipDir.class, dirStr.toUpperCase().trim());
				if(dir==null)
				{
					addScreenMessage(L("Error: Invalid direction or target given."));
					return false;
				}
				dirs.add(dir);
			}
			String code;
			code=TechCommand.DIRSET.makeCommand(dirs.get(0));
			msg=CMClass.getMsg(mob, sensor, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			for(int i=1;i<dirs.size();i++)
			{
				sendMessage(mob,E,msg,unparsed);
				code=TechCommand.DIRSET.makeCommand(dirs.get(1));
				msg=CMClass.getMsg(mob, sensor, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			}
			sendMessage(mob,E,msg,unparsed);
			return false;
		}
	};

	@Override
	protected void provideService(final SWServices service, final Software S, final String[] parms, final CMMsg msg)
	{
		super.provideService(service, S, parms, msg);
		if((service == SWServices.COORDQUERY)
		&&(S!=null)
		&&(S!=this)
		&&(parms.length>0))
		{
			final SpaceObject spaceObject=CMLib.space().getSpaceObject(this,true);
			final List<SpaceObject> allObjects = new ArrayList<SpaceObject>();
			for(final TechComponent sensor : getShipSensors())
				allObjects.addAll(takeNewSensorReport(sensor));
			Collections.sort(allObjects, new DistanceSorter(spaceObject));
			for(final String parm : parms)
			{
				SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, parm, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, parm, true);
				final BigVector coords=(targetObj!=null) ? targetObj.coordinates() : null;
				if(coords != null)
				{
					final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
					try
					{
						final String code=TechCommand.SWSVCRES.makeCommand(service,new String[] { CMParms.toListString(coords.toLongs()) });
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
