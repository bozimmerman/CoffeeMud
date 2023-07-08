package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.BasicTech.GenElecItem;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class ShipTacticalProgram extends ShipNavProgram
{
	@Override
	public String ID()
	{
		return "ShipTacticalProgram";
	}

	protected volatile SpaceObject			currentTarget		= null;

	@Override
	protected SWServices[] getProvidedServices()
	{
		return new SWServices[] { Software.SWServices.TARGETING };
	}

	// **********************************************************************************************************************************
	protected SoftwareProcedure targetProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceObject spaceObject=CMLib.space().getSpaceObject(sw,true);
			final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
			if(ship==null)
			{
				addScreenMessage(L("Error: Malfunctioning hull interface."));
				return false;
			}
			if(parsed.size()<2)
			{
				addScreenMessage(L("Error: TARGET requires the name of the target.   Try HELP."));
				return false;
			}
			if(sensorReps.size()==0)
			{
				addScreenMessage(L("Error: no sensor data found to identify target."));
				return false;
			}
			final String targetStr=CMParms.combine(parsed, 1);
			final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
			for(final TechComponent sensor : getShipSensors())
				allObjects.addAll(takeNewSensorReport(sensor));
			Collections.sort(allObjects, new DistanceSorter(spaceObject));
			SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
			if(targetObj == null)
				targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
			if(targetObj == null)
			{
				addScreenMessage(L("No suitable target @x1 found within sensor range.",targetStr));
				return false;
			}
			if(targetObj.coordinates() == null)
			{
				addScreenMessage(L("Can not target @x1 due to lack of coordinate information.",targetObj.name()));
				return false;
			}
			currentTarget = targetObj;
			addScreenMessage(L("Target set for @x1.",targetObj.name()));
			return false;
		}
	};

	protected SoftwareProcedure fireProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			CMMsg msg = null;
			Electronics E = null;
			final String rest = CMParms.combine(parsed,1).toUpperCase();
			final SpaceObject spaceObject=CMLib.space().getSpaceObject(sw,true);
			final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
			if(ship==null)
			{
				addScreenMessage(L("Error: Malfunctioning hull interface."));
				return false;
			}
			if(currentTarget == null)
			{
				addScreenMessage(L("Target not set."));
				return false;
			}
			final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
			for(final TechComponent sensor : getShipSensors())
				allObjects.addAll(takeNewSensorReport(sensor));
			Collections.sort(allObjects, new DistanceSorter(spaceObject));
			final SpaceObject targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, currentTarget.ID(), true);
			if(targetObj == null)
			{
				addScreenMessage(L("Target no longer in sensor range."));
				return false;
			}
			if(targetObj.coordinates()==null)
			{
				addScreenMessage(L("Unable to determine target direction and range."));
				return false;
			}
			//final double[] targetDirection = CMLib.space().getDirection(ship.coordinates(), CMLib.space().moveSpaceObject(targetObj.coordinates(), targetObj.direction(), (long)targetObj.speed()));
			double[] targetDirection = CMLib.space().getDirection(ship.coordinates(), targetObj.coordinates());
			if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				Log.debugOut("Fire: "+ship.Name()+" -> "+targetObj.Name()+"@"+Math.toDegrees(targetDirection[0])+","+Math.toDegrees(targetDirection[1]));
			TechComponent finalWeaponToFire = null;
			final String weapName = CMParms.combine(parsed,1);
			if(weapName.length()>0)
			{
				final TechComponent weapon = findWeaponByName(rest);
				if(weapon == null)
				{
					addScreenMessage(L("Error: Unknown weapon name or command word '"+rest+"'.   Try HELP."));
					return false;
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
					addScreenMessage(L("Error: No weapons found."));
					return false;
				}
				addScreenMessage(L("Info: Auto selected weapon '@x1'.",finalWeaponToFire.name()));
			}
			{
				E=finalWeaponToFire;
				String code;
				code=TechCommand.TARGETSET.makeCommand(Long.valueOf(targetObj.coordinates()[0]),
													   Long.valueOf(targetObj.coordinates()[1]),
													   Long.valueOf(targetObj.coordinates()[2]));
				msg=CMClass.getMsg(mob, finalWeaponToFire, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if((finalWeaponToFire.getTechType()!=Technical.TechType.SHIP_LAUNCHER)
				||sendMessage(mob, finalWeaponToFire, msg, unparsed))
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
								final double futureAccellerationInSameDirectionAsAmmo;
								final double angleDiff = Math.cos(CMLib.space().getAngleDelta(ship.direction(), targetDirection));
								if(angleDiff > 0.0)
									futureAccellerationInSameDirectionAsAmmo=1.0 + CMath.mul(angleDiff,ship.speed());
								else
									futureAccellerationInSameDirectionAsAmmo=0.0;
								ammoO.setCoords(CMLib.space().moveSpaceObject(ship.coordinates(), targetDirection,
										(int)Math.round(ship.radius()+ammoO.radius()+1.0
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
					msg=CMClass.getMsg(mob, finalWeaponToFire, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, finalWeaponToFire, msg, unparsed))
					{
						code = TechCommand.FIRE.makeCommand();
						msg=CMClass.getMsg(mob, finalWeaponToFire, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					}
					else
						msg=null;
				}
				else
					msg=null;
			}
			sendMessage(mob,E,msg,unparsed);
			return false;
		}
	};

	protected SoftwareProcedure weaponProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			CMMsg msg = null;
			Electronics E;
			final TechComponent weapon = findWeaponByName(uword);
			if(weapon == null)
			{
				addScreenMessage(L("Error: Unknown weapon name or command word '"+uword+"'.   Try HELP."));
				return false;
			}
			if(parsed.size()==1)
			{
				addScreenMessage(L("Error: No emission percentage given."));
				return false;
			}
			final String emission=parsed.get(1);
			if(!CMath.isPct(emission))
			{
				addScreenMessage(L("Error: Invalid emission percentage given."));
				return false;
			}
			final double pct=CMath.s_pct(emission);
			if((pct < 0)||(pct > 1))
			{
				addScreenMessage(L("Error: Invalid emission percentage given."));
				return false;
			}
			E=weapon;
			String code;
			code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(pct * weapon.powerCapacity())));
			msg=CMClass.getMsg(mob, weapon, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			sendMessage(mob,E,msg,unparsed);
			return false;
		}
	};

	protected SoftwareProcedure shieldProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			CMMsg msg = null;
			Electronics E = null;
			if(uword.startsWith("SHIELDS"))
			{
				if(parsed.size()==1)
				{
					addScreenMessage(L("Error: No UP or DOWN instruction."));
					return false;
				}
				if(getShipShields().size()==0)
				{
					addScreenMessage(L("Error: No shields found."));
					return false;
				}
				if(parsed.get(1).equalsIgnoreCase("UP"))
				{
					for(int s=0;s<getShipShields().size();s++)
					{
						final TechComponent shield = getShipShields().get(s);
						E=shield;
						final String code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(shield.powerCapacity())));
						msg=CMClass.getMsg(mob, shield, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
						if(s<getShipShields().size()-1)
							sendMessage(mob,E,msg,unparsed);
					}
				}
				else
				if(parsed.get(1).equalsIgnoreCase("DOWN"))
				{
					for(int s=0;s<getShipShields().size();s++)
					{
						final TechComponent shield = getShipShields().get(s);
						E=shield;
						msg=CMClass.getMsg(mob, shield, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
						if(s<getShipShields().size()-1)
							sendMessage(mob,E,msg,unparsed);
					}
				}
				else
				{
					addScreenMessage(L("Error: No UP or DOWN instruction."));
					return false;
				}
			}
			else
			{
				final ShipWarComponent shield = findShieldByName(uword);
				if(shield == null)
				{
					addScreenMessage(L("Error: Unknown shield name or command word '"+uword+"'.   Try HELP."));
					return false;
				}
				if(parsed.size()==1)
				{
					addScreenMessage(L("Error: No power percentage given."));
					return false;
				}
				final String emission=parsed.get(1);
				if(!CMath.isPct(emission))
				{
					addScreenMessage(L("Error: Invalid power percentage given."));
					return false;
				}
				final double pct=CMath.s_pct(emission);
				if((pct < 0)||(pct > 1))
				{
					addScreenMessage(L("Error: Invalid power percentage given."));
					return false;
				}
				E=shield;
				String code;

				code=TechCommand.POWERSET.makeCommand(Long.valueOf(Math.round(pct * shield.powerCapacity())));
				msg=CMClass.getMsg(mob, shield, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			}
			sendMessage(mob,E,msg,unparsed);
			return false;
		}
	};

	// **********************************************************************************************************************************
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
