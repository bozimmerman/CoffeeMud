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
import java.math.BigDecimal;
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
public class ShipNavProgram extends ShipSensorProgram
{
	@Override
	public String ID()
	{
		return "ShipNavProgram";
	}

	protected volatile Double		savedAcceleration	= null;
	protected volatile Double		savedSpeedDelta		= null;
	protected volatile Double		savedAngle			= null;
	protected volatile Double		lastInject			= null;
	protected volatile Double		targetAcceleration	= Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
	protected volatile ShipNavTrack	navTrack			= null;
	protected final List<SpaceObject>course				= new LinkedList<SpaceObject>();
	protected volatile boolean		courseSet			= false;
	protected volatile SpaceObject	courseTarget		= null;

	protected final Map<ShipEngine, Double[]>	injects	= new Hashtable<ShipEngine, Double[]>();

	protected final static double MAX_DIR_DIFF = 0.08;

	protected static class ShipNavTrack
	{
		protected long						speedLimit 			= Long.MAX_VALUE;
		protected ShipNavProcess			proc;
		protected ShipNavState				state;
		protected Object[]					args;
		protected Class<?>[]				types;
		protected Map<Class<?>, Integer>	classMap;

		protected ShipNavTrack(final ShipNavProcess proc, final Object... args)
		{
			this.proc = proc;
			this.state = proc.initialState;
			this.args = args;
			this.types = proc.argTypes;
			this.classMap = proc.argMap;
			if(args.length>types.length)
				throw new IllegalArgumentException("Too many arguments ("+args.length+">"+types.length+")");
			if(args.length<types.length)
				throw new IllegalArgumentException("Too few arguments ("+args.length+"<"+types.length+")");
			for(int i=0;i <args.length;i++)
			{
				if(!types[i].isAssignableFrom(args[i].getClass()))
				{
					throw new IllegalArgumentException("Illegal argument "+i+": '"
							+args[i].getClass().getSimpleName()+" is not "+types[i].getSimpleName());
				}
			}
		}
		protected <T> T getArg(final Class<T> argT)
		{
			final Integer dexI = classMap.get(argT);
			if(dexI != null)
			{
				@SuppressWarnings("unchecked")
				final T arg = (T)args[dexI.intValue()];
				return arg;
			}
			throw null;
		}

		protected <T> T setArg(final Class<T> argT, final T t)
		{
			final Integer dexI = classMap.get(argT);
			if(dexI != null)
			{
				args[dexI.intValue()] = t;
				return t;
			}
			throw null;
		}

		protected Object getArg(final int index)
		{
			if((index>=0)&&(index < args.length))
				return args[index];
			throw new IllegalArgumentException();
		}
	}

	protected static enum ShipNavState
	{
		LAUNCHING,
		STOP,
		PRE_LANDING_STOP,
		LANDING_APPROACH,
		LANDING,
		ORBITSEARCH,
		ORBITCHECK,
		ORBITCRUISE,
		APPROACH,
		DEPROACH
		;
	}

	protected static enum ShipNavProcess
	{
		STOP(ShipNavState.STOP, List.class),
		LAUNCH(ShipNavState.LAUNCHING, SpaceObject.class, List.class),
		LAND(ShipNavState.PRE_LANDING_STOP, SpaceObject.class, List.class),
		ORBIT(ShipNavState.ORBITSEARCH, SpaceObject.class, List.class),
		APPROACH(ShipNavState.APPROACH, SpaceObject.class, List.class, LinkedList.class)
		;

		private final ShipNavState				initialState;
		private final Class<?>[]				argTypes;
		private final Map<Class<?>, Integer>	argMap;

		private ShipNavProcess(final ShipNavState initialState, final Class<?>... argTemplate)
		{
			this.initialState = initialState;
			argTypes = argTemplate;
			argMap = new Hashtable<Class<?>, Integer>();
			for(int i=0;i<argTypes.length;i++)
				argMap.put(argTypes[i], Integer.valueOf(i));
		}
	}

	@Override
	protected void decache()
	{
		super.decache();
		cancelNavigation();
		injects.clear();
	}

	protected boolean cancelNavigation()
	{
		final boolean didSomething = navTrack != null || courseSet;
		lastInject = null;
		navTrack=null;
		course.clear();
		courseTarget = null;
		courseSet = false;
		return didSomething;
	}

	/**
	 * Generate a new engine injection amount from a previous and desired acceleration.
	 *
	 * @param lastInject the last injection amount
	 * @param lastAcceleration the previous acceleration in dm/s
	 * @param targetAcceleration the target acceleration in dm/s
	 * @return the new injection amount
	 */
	protected Double fixInjection(final Double lastInject, final Double lastAcceleration, final double targetAcceleration)
	{
		final Double newInject;
		if(targetAcceleration <= 0)
			return Double.valueOf(0);
		if(lastAcceleration.doubleValue() < targetAcceleration)
		{
			if(lastAcceleration.doubleValue() < (targetAcceleration * .00001))
				newInject = Double.valueOf(lastInject.doubleValue()*10000.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .0001))
				newInject = Double.valueOf(lastInject.doubleValue()*999.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .001))
				newInject = Double.valueOf(lastInject.doubleValue()*99.0);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .01))
				newInject = Double.valueOf(lastInject.doubleValue()*5.9);
			else
			if(lastAcceleration.doubleValue() < (targetAcceleration * .1))
				newInject = Double.valueOf(lastInject.doubleValue()*9.9);
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
				newInject = Double.valueOf(lastInject.doubleValue()/90000.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 100000))
				newInject = Double.valueOf(lastInject.doubleValue()/9000.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 10000))
				newInject = Double.valueOf(lastInject.doubleValue()/900.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 1000))
				newInject = Double.valueOf(lastInject.doubleValue()/90.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 100))
				newInject = Double.valueOf(lastInject.doubleValue()/9.0);
			else
			if(lastAcceleration.doubleValue() > (targetAcceleration * 10))
				newInject = Double.valueOf(lastInject.doubleValue()/5.0);
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

	/**
	 * Generate a new engine injection amount from a previous and desired acceleration.
	 *
	 * @param newInject the last injection amount
	 * @param targetAcceleration the target acceleration in dm/s
	 * @return the new injection amount
	 */
	protected Double calculateMarginalTargetInjection(Double newInject, final double targetAcceleration)
	{
		//force/mass is the Gs felt by the occupants.. not force-mass
		//so go ahead and push it up to 3 * g forces on ship
		if((this.savedAcceleration !=null)
		&&(newInject != null)
		&& (targetAcceleration != 0.0))
			newInject=fixInjection(newInject,this.savedAcceleration,targetAcceleration);
		return newInject;
	}

	protected Double forceAccelerationAllProgramEngines(final Collection<ShipEngine> programEngines, final double targetAcceleration)
	{
		Double newInject = this.calculateMarginalTargetInjection(this.lastInject, targetAcceleration);
		int tries=100;
		do
		{
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, true);
			if((CMath.abs(targetAcceleration)-this.savedAcceleration.doubleValue())<.01)
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
			this.savedAcceleration =null;
			this.savedSpeedDelta=null;
			if(thrustInject != null)
			{
				if((thrustInject != this.lastInject)
				||(!engineE.isReactionEngine())
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

	protected void performSingleThrust(final ShipEngine engineE, final Double thrustInject, final boolean alwaysThrust)
	{
		final Double oldInject=this.lastInject;
		final Double oldAccel=this.savedAcceleration;
		final Double oldDelta=this.savedSpeedDelta;
		performSimpleThrust(engineE, thrustInject, alwaysThrust);
		this.lastInject=oldInject;
		this.savedAcceleration=oldAccel;
		this.savedSpeedDelta=oldDelta;
	}

	protected double findTargetAcceleration(final ShipEngine E)
	{
		boolean dampenerFound = false;
		for(final TechComponent T : this.getDampeners())
		{
			if(T.activated()
			&&((!T.subjectToWearAndTear()))||(T.usesRemaining()>30))
				dampenerFound = true;
		}
		if(!dampenerFound)
			return SpaceObject.ACCELERATION_TYPICALSPACEROCKET;
		return SpaceObject.ACCELERATION_DAMAGED*10;
	}

	protected boolean flipForAllStop(final SpaceShip ship)
	{
		final Dir3D stopFacing = CMLib.space().getOppositeDir(ship.direction());
		return changeFacing(ship, stopFacing);
	}

	protected boolean changeFacing(final SpaceShip ship, final Dir3D newFacing)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		M.setName(ship.Name());
		final boolean isDebugging = CMSecurity.isDebugging(DbgFlag.SPACESHIP);
		final boolean isDebuggingTurns = false; // isDebugging
		CMLib.space().getOppositeDir(ship.facing()); // I think this is to normalize the facing dir
		try
		{
			double angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			int tries=100;
			while((angleDiff > 0.0001)&&(--tries>0))
			{
				// step one, face opposite direction of motion
				if(isDebugging)
				{
					Log.debugOut(ship.Name()+" maneuvering to go from "+
								CMLib.english().directionDescShort(ship.facing().toDoubles())
							+"  to  "+CMLib.english().directionDescShort(newFacing.toDoubles()));
				}
				for(final ShipEngine engineE : engines)
				{
					if((CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.STARBOARD))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.PORT))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.DORSEL))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.VENTRAL)))
					{
						msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
						this.savedAngle = null;
						final String code=TechCommand.THRUST.makeCommand(ShipDir.PORT,Double.valueOf(1));
						msg.setTargetMessage(code);
						this.trySendMsgToItem(M, engineE, msg);
						if(this.savedAngle==null)
							break;
						final double angleAchievedPerPt = Math.abs(this.savedAngle.doubleValue()); //
						Dir3D angleDelta = CMLib.space().getAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						for(int i=0;i<100;i++)
						{
							if(angleDelta.xy().abs().doubleValue() > 0.00001)
							{
								final ShipDirectional.ShipDir dir = angleDelta.xyd() < 0 ? ShipDir.PORT : ShipDir.STARBOARD;
								final Double thrust = Double.valueOf(angleDelta.xy().abs().doubleValue() / angleAchievedPerPt);
								if(isDebuggingTurns)
								{
									Log.debugOut("Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+
											dir+" to delta, and go from "+
											Math.toDegrees(ship.facing().xyd())+" to "+Math.toDegrees(newFacing.xyd())+
											", angle delta = "+Math.toDegrees(angleDelta.xyd()));
								}
								msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
								this.savedAngle = null;
								this.trySendMsgToItem(M, engineE, msg);
								if(this.savedAngle==null)
									break;
							}
							else
								break;
							angleDelta = CMLib.space().getAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
							/*
							if(isDebuggingTurns)
							{
								Log.debugOut("Turn Deltas now: "+(Math.round(angleDelta[0]*100)/100.0)+" + "+(Math.round(angleDelta[1]*100)/100.0)
										+"=="+(Math.round(Math.abs((angleDelta[0])+Math.abs(angleDelta[1]))*100)/100.0));
							}
							*/
							if((Math.abs(angleDelta.xyd())+Math.abs(angleDelta.zd()))<.01)
								break;
						}
						angleDelta = CMLib.space().getAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						for(int i=0;i<100;i++)
						{
							if(Math.abs(angleDelta.zd()) > 0.00001)
							{
								final ShipDirectional.ShipDir dir = angleDelta.zd() < 0 ? ShipDir.VENTRAL : ShipDir.DORSEL;
								final Double thrust = Double.valueOf(Math.abs(angleDelta.zd()) / angleAchievedPerPt);
								if(isDebuggingTurns)
								{
									Log.debugOut("Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+dir+" to achieve delta, and go from "
												+ship.facing().zd()+" to "+newFacing.zd());
								}
								msg.setTargetMessage(TechCommand.THRUST.makeCommand(dir,thrust));
								this.savedAngle = null;
								this.trySendMsgToItem(M, engineE, msg);
								if(this.savedAngle==null)
									break;
							}
							else
								break;
							angleDelta = CMLib.space().getAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
							if(isDebuggingTurns)
							{
								Log.debugOut("Turn Deltas now: "+(Math.round(angleDelta.xyd()*100)/100.0)+" + "+(Math.round(angleDelta.zd()*100)/100.0)
										+"=="+(Math.round(Math.abs((angleDelta.xyd())+Math.abs(angleDelta.zd()))*100)/100.0));
							}
						}
						if((Math.abs(angleDelta.xyd())+Math.abs(angleDelta.zd()))<.01)
							break;
					}
				}
				angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			}
			if(tries > 0)
				return true;
		}
		finally
		{
			M.destroy();
		}
		return false;
	}

	protected ShipEngine primeMainThrusters(final SpaceShip ship, final double maxAceleration, final ShipEngine overrideE)
	{
		CMMsg msg;
		final List<ShipEngine> engines;
		if(overrideE != null)
			engines = new XVector<ShipEngine>(overrideE);
		else
			engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final boolean isDocked = ship.getIsDocked()!=null;
		try
		{
			for(final ShipEngine engineE : engines)
			{
				if(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.AFT))
				{
					double targetAcceleration = findTargetAcceleration(engineE);
					if(targetAcceleration > maxAceleration)
						targetAcceleration = maxAceleration;
					int tries=100;
					double lastTryAmt;
					if(this.injects.containsKey(engineE))
					{
						lastTryAmt = this.injects.get(engineE)[0].doubleValue();
						savedAcceleration=this.injects.get(engineE)[1];
					}
					else
						lastTryAmt= 0.0001;
					final CMMsg deactMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
					msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
					Double prevAcceleration = Double.valueOf(0.0);
					int stableCounter = 0;
					while(--tries>0)
					{
						this.savedAcceleration =null;
						final String code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT, Double.valueOf(lastTryAmt));
						msg.setTargetMessage(code);
						ship.tick(M, Tickable.TICKID_PROPERTY_SPECIAL); // clear the speed ticker
						this.trySendMsgToItem(M, engineE, msg);
						final Double thisLastAccel=this.savedAcceleration ;
						if(thisLastAccel!=null)
						{
							final double ratio = targetAcceleration/thisLastAccel.doubleValue();
							if((thisLastAccel.doubleValue() >= targetAcceleration)
							&&((!isDocked)||(ship.getIsDocked()==null)))
							{
								this.lastInject=Double.valueOf(lastTryAmt);
								this.injects.put(engineE,new Double[] {lastInject,savedAcceleration});
								return engineE;
							}
							else
							if((thisLastAccel.doubleValue()>0.0) && (ratio>100))
								lastTryAmt *= (Math.sqrt(ratio)/5.0);
							else
							if(prevAcceleration.doubleValue() == thisLastAccel.doubleValue())
							{
								this.injects.put(engineE,new Double[] {lastInject,savedAcceleration});
								break;
							}
							else
							{
								ship.tick(M, Tickable.TICKID_PROPERTY_SPECIAL); // clear the speed ticker
								this.trySendMsgToItem(M, engineE, deactMsg);
								lastTryAmt *= 1.1;
								final double lastPct = (prevAcceleration.doubleValue()/thisLastAccel.doubleValue());
								if((lastPct >= .891) && (lastPct <= .911))
								{
									stableCounter++;
									if(stableCounter>2)
									{
										double newAccelWillDo = thisLastAccel.doubleValue();
										for(int i=0;i<100;i++)
										{
											newAccelWillDo *= 1.1;
											if(newAccelWillDo >= targetAcceleration)
												break;
											lastTryAmt *= 1.1;
										}
									}
								}
								else
									stableCounter=0;
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

	@Override
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if((courseTarget != null) && (!courseSet))
		{
			final SpaceObject me = CMLib.space().getSpaceObject(this,true);
			if(me == null)
				cancelNavigation();
			else
			{
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : getShipSensors())
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(me));
				course.clear();
				course.addAll(this.calculateNavigation(me, courseTarget, allObjects));
				if(!course.contains(courseTarget))
				{
					cancelNavigation();
					super.addScreenMessage(L("Failed to plot course."));
				}
				else
				{
					courseSet = true;
					super.addScreenMessage(L("Course plotted."));
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
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
						if(this.savedAcceleration==null)
							this.savedAcceleration =(Double)parms[1];
						if(this.savedSpeedDelta==null)
							this.savedSpeedDelta =(Double)parms[3];
						break;
					default:
						if(savedAngle==null)
							this.savedAngle =(Double)parms[1];
						break;
					}
				}
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	protected void onDeactivate(final MOB mob, final String message)
	{
		if(message == null)
		{
			savedAcceleration	= null;
			savedSpeedDelta		= null;
			savedAngle			= null;
			lastInject			= null;
			targetAcceleration	= Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
			navTrack			= null;
			injects.clear();
		}
		super.onDeactivate(mob, message);
	}

	protected boolean checkDatabase(final Coord3D coords)
	{
		final String[] parms = new String[] {CMParms.toListString(coords.toLongs())};
		final List<String[]> names = super.doServiceTransaction(SWServices.IDENTIFICATION, parms);
		for(final String[] res : names)
		{
			for(final String r : res)
			{
				if(r.length()>0)
					return true;
			}
		}
		return false;
	}

	protected boolean sameAs(Environmental obj1, Environmental obj2)
	{
		if((obj1 == null)||(obj2 == null))
			return false;
		if(obj1 == obj2)
			return true;
		if(obj1 instanceof SensedEnvironmental)
			obj1 = ((SensedEnvironmental)obj1).get();
		if(obj2 instanceof SensedEnvironmental)
			obj2 = ((SensedEnvironmental)obj2).get();
		return obj1 == obj2;
	}

	protected boolean sameAs(final Environmental obj1, final SpaceObject[] others)
	{
		if(others==null)
			return false;
		for(final SpaceObject o : others)
		{
			if(sameAs(obj1, o))
				return true;
		}
		return false;
	}

	protected SpaceObject getCollision(final SpaceObject fromObj, final SpaceObject toObj, final long radius, final SpaceObject[] others)
	{
		final long distance = CMLib.space().getDistanceFrom(fromObj, toObj);
		final Dir3D direction = CMLib.space().getDirection(fromObj, toObj);
		BoundedCube baseCube=new BoundedCube(fromObj.coordinates(), SpaceObject.Distance.StarBRadius.dm);
		baseCube=baseCube.expand(direction, distance);
		final BoundedSphere fromSphere=new BoundedSphere(fromObj.coordinates(), radius);
		final BoundedTube compTube=fromSphere.expand(direction, distance);
		SpaceObject collO = null;
		long collDistance=Long.MAX_VALUE;
		for(final SpaceObject O : CMLib.space().getSpaceObjectsInBound(baseCube))
		{
			if((O.speed()==0.0)
			&&(!sameAs(fromObj, O))
			&&(!sameAs(toObj, O))
			&&(!sameAs(O, others)))
			{
				final BoundedSphere enemySphere = O.getSphere();
				final BoundedSphere enemyBounds = new BoundedSphere(enemySphere.center(),
						Math.round(CMath.mul(enemySphere.radius, SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				if(compTube.intersects(enemyBounds))
				{
					final long dist = CMLib.space().getDistanceFrom(fromObj, O);
					if((dist < collDistance) || (collO == null))
					{
						// it's legit, a real collision!
						collDistance = dist;
						collO = O;
					}
				}
			}
		}
		return collO;
	}

	protected SpaceObject subCourseCheck(final SpaceObject ship,
										 final SpaceObject fromObj, final SpaceObject toObj,
										 final Coord3D[] points, final SpaceObject[] others)
	{
		SpaceObject newObj = null;
		// one of these is always behind the object, so we have to check
		CMLib.dice().scramble(points);
		long closestPoint = Long.MAX_VALUE;
		final SpaceObject winnerObj = (SpaceObject)CMClass.getBasicItem("Moonlet");
		winnerObj.setRadius(ship.radius());
		winnerObj.setName("Nav Point");
		try
		{
			for(final Coord3D p : points)
			{
				winnerObj.setCoords(p.copyOf());
				final SpaceObject coll2O = getCollision(fromObj, winnerObj, ship.radius(), others);
				if(coll2O == null)
				{
					final long d = CMLib.space().getDistanceFrom(fromObj, winnerObj);
					if((newObj == null)
					||(d < closestPoint))
					{
						newObj = (SpaceObject)winnerObj.copyOf(); // wont add to space
						closestPoint = d;
					}
				}
			}
		}
		finally
		{
			winnerObj.destroy();
		}
		return newObj;
	}

	protected LinkedList<SpaceObject> calculateNavigation(final SpaceObject ship,
														  final SpaceObject targetObj,
														  final List<SpaceObject> sensorObjs)
	{
		final List<SpaceObject> navs = new ArrayList<SpaceObject>();

		navs.add(targetObj);
		int navSize = 0;
		final SpaceObject[] others = new SpaceObject[] { ship, targetObj };
		while(navSize != navs.size())
		{
			navSize = navs.size();
			SpaceObject fromObj = ship;
			for(int i=0;i<navs.size();i++)
			{
				final SpaceObject toObj = navs.get(i);
				final SpaceObject collO = getCollision(fromObj, toObj, ship.radius(), others);
				if((collO != null)
				&&((containsSameCoordinates(sensorObjs, collO.coordinates()))
					||(checkDatabase(collO.coordinates()))))
				{
					final Dir3D angleFromOrigin = CMLib.space().getDirection(fromObj.coordinates(), collO.coordinates());
					final Dir3D angleFromCollider = CMLib.space().getDirection(collO.coordinates(), fromObj.coordinates());
					final long gravRadius = Math.round(CMath.mul(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS, collO.radius()));
					final long distAdd = gravRadius - collO.radius();
					final long distanceCollRadius = collO.radius() + (distAdd * 2) + 2;
					final Coord3D[] pointsFromOrigin = CMLib.space().getPerpendicularPoints(collO.coordinates(), angleFromCollider, distanceCollRadius);
					final Coord3D[] pointsFromCollider = CMLib.space().getPerpendicularPoints(fromObj.coordinates(), angleFromOrigin, distanceCollRadius);
					Coord3D[] points = CMParms.combine(pointsFromCollider, pointsFromOrigin);

					// one of these is always behind the object, so we have to check
					SpaceObject newObj = subCourseCheck(ship,fromObj,toObj,points,others);
					if(newObj == null)
					{

						final Dir3D revAngleFromOrigin = CMLib.space().getDirection(fromObj.coordinates(), collO.coordinates());
						points = CMLib.space().getPerpendicularPoints(fromObj.coordinates(), revAngleFromOrigin, distanceCollRadius);
						newObj = subCourseCheck(ship,fromObj,toObj,points,others);
						if(newObj == null)
							return null;
					}
					navs.add(i, newObj);
					fromObj = newObj;
				}
				else
					fromObj = toObj;
			}
		}
		return new XLinkedList<SpaceObject>(navs);
	}

	protected boolean confirmNavEnginesOK(final SpaceShip ship, final Collection<ShipEngine> programEngines)
	{
		final Double lastInject=this.lastInject;
		if((ship==null)||(programEngines==null))
		{
			String reason =  (programEngines == null)?"no engines":"";
			reason = (ship==null)?"no ship interface":reason;
			super.addScreenMessage(L("Last program aborted with error ("+reason+")."));
			return false;
 		}
		if((programEngines.size()==0)||(lastInject==null))
		{
			String reason =  (programEngines.size()==0)?"no aft engines":"";
			reason = (lastInject==null)?"no engine injection data":reason;
			super.addScreenMessage(L("Program aborted with error ("+reason+")."));
			return false;
		}
		return true;
	}

	protected boolean checkNavComplete(final ShipNavTrack track, final SpaceShip ship, final SpaceObject targetObject)
	{
		// check pre-reqs and completions of the overall process first
		final ShipNavProcess proc = track.proc;
		@SuppressWarnings("unchecked")
		final List<ShipEngine> programEngines=track.getArg(List.class);
		switch(proc)
		{
		case APPROACH:
		{
			if(targetObject==null)
			{
				final String reason = "no target information";
				cancelNavigation();
				super.addScreenMessage(L("Approach program aborted with error ("+reason+")."));
				return false;
			}
			final long distance = (CMLib.space().getDistanceFrom(ship, targetObject)-ship.radius()
								-Math.round(CMath.mul(targetObject.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
			int safeDistance=100 + (int)Math.round(ship.speed());
			final Dir3D dirTo = CMLib.space().getDirection(ship, targetObject);
			final double diffDelta = CMLib.space().getAngleDelta(ship.direction(), dirTo);
			if(diffDelta<MAX_DIR_DIFF)
				safeDistance += (int)Math.round(ship.speed());
			if(distance < safeDistance)
			{
				if(targetObject.speed()==0)
				{
					if(ship.speed()<1)
					{
						ship.setSpeed(0.0);
						super.addScreenMessage(L("Approach program completed."));
						for(final ShipEngine engineE : programEngines)
							performSimpleThrust(engineE,Double.valueOf(0.0), true);
						this.cancelNavigation();
						return false;
					}
					else
					if(track.state != ShipNavState.STOP)
					{
						super.addScreenMessage(L("Approach completed, stop initiated."));
						track.state = ShipNavState.STOP;
					}
				}
				else
				{
					for(final ShipEngine engineE : programEngines)
						performSimpleThrust(engineE,Double.valueOf(0.0), true);
					this.cancelNavigation();
					super.addScreenMessage(L("Approach program completed."));
					return false;
				}
			}
			@SuppressWarnings("unchecked")
			final LinkedList<SpaceObject> navList = track.getArg(LinkedList.class);
			if(navList.isEmpty())
			{
				final String reason = "no nav target information";
				cancelNavigation();
				super.addScreenMessage(L("Approach program aborted with error ("+reason+")."));
				return false;
			}
			break;
		}
		case LAND:
			if(ship.getIsDocked()!=null)
			{
				cancelNavigation();
				super.addScreenMessage(L("Landing program completed successfully."));
				return false;
			}
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				cancelNavigation();
				super.addScreenMessage(L("Launding program aborted with error ("+reason+")."));
				return false;
			}
			break;
		case LAUNCH:
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				this.cancelNavigation();
				super.addScreenMessage(L("Launch program aborted with error ("+reason+")."));
				return false;
			}
			{
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject);
				if(distance > (targetObject.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					this.lastInject = null;
					super.addScreenMessage(L("Launch program completed. Shutting down thrust."));
					for(final ShipEngine engineE : programEngines)
						performSimpleThrust(engineE,Double.valueOf(0.0), true);
					this.cancelNavigation();
					return false;
				}
			}
			break;
		case ORBIT:
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				this.cancelNavigation();
				super.addScreenMessage(L("Orbit program aborted with error ("+reason+")."));
				return false;
			}
			{
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject);
				final long maxDistance=Math.round(CMath.mul(targetObject.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS));
				final long minDistance = targetObject.radius() + Math.round(CMath.mul(.75, maxDistance-targetObject.radius()));
				if((distance < maxDistance) && (distance > minDistance))
				{
					final Pair<Dir3D,Double> orb = CMLib.space().calculateOrbit(ship,targetObject);
					if((orb != null)
					&&(CMath.isWithin(ship.speed(), orb.second.doubleValue(), 0.05)))
					{
						super.addScreenMessage(L("Orbit program completed. Neutralizing acceleration."));
						this.cancelNavigation();
						return false;
					}
				}
			}
			break;
		case STOP:
			if(ship.speed()  <= 0.0)
			{
				ship.setSpeed(0.0); // that's good enough, for now.
				for(final ShipEngine engineE : programEngines)
					performSimpleThrust(engineE,Double.valueOf(0.0), true);
				this.cancelNavigation();
				super.addScreenMessage(L("Stop program completed successfully."));
				return false;
			}
			track.state = ShipNavState.STOP; // no need to have any other state
			break;
		}
		return true;
	}

	protected void doNavigation(final ShipNavTrack track)
	{
		final SpaceObject spaceObj=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship = (spaceObj instanceof SpaceShip) ? (SpaceShip)spaceObj : null;
		@SuppressWarnings("unchecked")
		final List<ShipEngine> programEngines=track.getArg(List.class);
		SpaceObject targetObject;
		try
		{
			targetObject=track.getArg(SpaceObject.class);
		}
		catch(final NullPointerException npe)
		{
			targetObject=null;
		}

		if((ship==null)||(!confirmNavEnginesOK(ship, programEngines)))
			return;
		if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			Log.debugOut("Program "+track.proc.name()+" state: "+track.state.toString());

		if(!this.checkNavComplete(track, ship, targetObject))
			return;

		// now determine state pre-reqs and completion
		double targetAcceleration = (this.targetAcceleration != null)?this.targetAcceleration.doubleValue():0;
		if(targetAcceleration > ship.speed())
			targetAcceleration = ship.speed();
		switch(track.state)
		{
		// the landing steps check if you are in position to go into land phase, and if you
		// are in the proper landing phase, that you are trying to stop
		case LANDING:
		case LANDING_APPROACH:
		case PRE_LANDING_STOP:
		{
			//TODO: Landing approach should check your speed, by determining the top speed you can be going
			// and still reach a speed of 0, gravity included, at the distance from radius
			if((track.state!=ShipNavState.LANDING)
			&&(targetObject != null))
			{
				final long distance=CMLib.space().getDistanceFrom(ship.coordinates(),targetObject.coordinates());
				if(distance < (ship.radius() + Math.round(targetObject.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					track.state=ShipNavState.LANDING;
			}
			if(track.state!=ShipNavState.PRE_LANDING_STOP)
				break;
			if(ship.speed()  <= 0.0)
			{
				track.state=ShipNavState.LANDING_APPROACH;
				break;
			}
		}
		// the stop part makes sure you are facing correctly, and might goose injection
		//$FALL-THROUGH$
		case STOP:
		{
			if(ship.speed()  > 0.0)
			{
				final Dir3D stopFacing = CMLib.space().getOppositeDir(ship.direction());
				final double angleDelta = CMLib.space().getAngleDelta(ship.facing(), stopFacing); // starboard is -, port is +
				if(angleDelta>.02)
				{
					if(!flipForAllStop(ship))
					{
						cancelNavigation();
						super.addScreenMessage(L("Stop program aborted with error (directional control failure)."));
						return;
					}
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
		// check distance from intermediary target, switching nav targets if necessary
		// if the last nav target, make sure there is plenty of room to stop, and
		// switch direction (and state) if necessary
		case APPROACH:
		case DEPROACH:
		{
			@SuppressWarnings("unchecked")
			final LinkedList<SpaceObject> navList = track.getArg(LinkedList.class);
			if(!navList.isEmpty())
			{
				SpaceObject intTarget = navList.getFirst();
				long distToITarget = (CMLib.space().getDistanceFrom(ship, intTarget)-ship.radius()
						-Math.round(CMath.mul(intTarget.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				Dir3D dirToITarget = CMLib.space().getDirection(ship.coordinates(), intTarget.coordinates());
				double directionDiff = CMLib.space().getAngleDelta(ship.direction(), dirToITarget);
				// see if we've hit a waypoint
				{
					int safeDistance=100 + (int)Math.round(ship.speed());
					if(directionDiff<MAX_DIR_DIFF)
						safeDistance += (int)Math.round(ship.speed());
					if(distToITarget < safeDistance)
					{
						if(navList.size()>1)
						{
							navList.removeFirst();
							intTarget = navList.getFirst();
							distToITarget = (CMLib.space().getDistanceFrom(ship, intTarget)-ship.radius()
									-Math.round(CMath.mul(intTarget.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
							dirToITarget = CMLib.space().getDirection(ship.coordinates(), intTarget.coordinates());
							directionDiff = CMLib.space().getAngleDelta(ship.direction(), dirToITarget);
							targetAcceleration=this.findTargetAcceleration(programEngines.get(0));
							track.state = ShipNavState.APPROACH;
						}
					}
				}

				// Check if we should be speeding up, or slowing down, and ideal facing direction

				// first see if we are actually underway...
				if((ship.speed()>targetAcceleration)
				&& (targetAcceleration > 0.0))
				{
					final double ticksToStop = ship.speed() / targetAcceleration;
					final double stopDistance = (ship.speed()/2.0) * (ticksToStop+1);
					// now see if we need to adjust decelleration during deproach
					Dir3D correctFacing;
					//final Dir3D correctDirection = dirToITarget;
					if((ticksToStop > 0)
					&&(track.state==ShipNavState.DEPROACH))
					{
						correctFacing = CMLib.space().getOppositeDir(ship.direction());
						//if(CMLib.space().getAngleDelta(ship.direction(), dirToITarget)>0)
						//	correctFacing = CMLib.space().getExaggeratedAngle(correctFacing, CMLib.space().getOppositeDir(ship.direction()));
						final double overUnderDistance = stopDistance - distToITarget;
						//final double oldTargetAcceleration = targetAcceleration;
						if(overUnderDistance > targetAcceleration * 2) // means we are stopping too slowly
							targetAcceleration += CMath.div(overUnderDistance , ticksToStop);
						else
						if(overUnderDistance < -(targetAcceleration * 2)) // stopping too quickly
							targetAcceleration += CMath.div(overUnderDistance , ticksToStop); // minus is already minus
						if(ship.speed()>=distToITarget) // this is deproach, so we want to STOP!
							targetAcceleration = ship.speed();
					}
					else  // APPROACH -- so see if it is time to decelerate
					{
						correctFacing = dirToITarget;
						if((stopDistance >= distToITarget)
						&&(targetObject != null)
						&&(ship.speed() > (targetObject.speed() * 2)))
						{
							track.state = ShipNavState.DEPROACH;
							// ensure we are mooning our direction
							correctFacing = CMLib.space().getOppositeDir(ship.direction()); // just slower at first
						}
						//else // during approach, facing should be = direction, but offset angle works TERRIBLY -- dunno why.
						//if(CMLib.space().getAngleDelta(ship.direction(), correctFacing)>0)
						//	correctFacing = CMLib.space().getOffsetAngle(correctFacing, ship.direction());
					}
					// correctFacing is now at the Ideal point.

					// if we are presently traveling towards the target, get detailed.
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SPACESHIP))
					{
						final double facingDiff = CMLib.space().getAngleDelta(ship.facing(), correctFacing);
						Log.debugOut(ship.name(),
									"Face diff: "+	CMath.div(Math.round(facingDiff * 10000),10000.0)
									+", Dir diff: "+CMath.div(Math.round(directionDiff * 10000),10000.0)
									+", speed: "+	CMLib.english().distanceDescShort(Math.round(ship.speed()))
									+", dist: "+	CMLib.english().distanceDescShort(distToITarget)
									+", dir: "+		CMLib.english().directionDescShort(correctFacing.toDoubles()));
					}
					if(CMLib.space().getAngleDelta(ship.facing(), correctFacing)>0)
						changeFacing(ship, correctFacing);
				}
				else // since we aren't moving yet, Begin standard approach.
				{
					track.state = ShipNavState.APPROACH;
					changeFacing(ship, dirToITarget);
				}
			}
			break;
		}
		case LAUNCHING:
		case ORBITSEARCH:
		case ORBITCHECK:
		case ORBITCRUISE:
			break;
		}

		// the state of the meat.
		Double newInject=this.lastInject;
		boolean doInject=true;
		switch(track.state)
		{
		case APPROACH:
			if((track.speedLimit < Long.MAX_VALUE)
			&&(ship.speed() >= track.speedLimit))
			{
				if(Math.toDegrees(CMLib.space().getAngleDelta(ship.facing(), ship.direction()))<90)
					doInject=false;
			}
			//$FALL-THROUGH$
		case STOP:
		case DEPROACH:
		case PRE_LANDING_STOP:
		{
			//final Double oldInject = newInject;
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
			//if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			//	Log.debugOut(ship.Name(),"Old engine inject value = "+oldInject+", new="+newInject);
			if(doInject)
			{
				for(final ShipEngine engineE : programEngines)
					performSimpleThrust(engineE,newInject, false);
			}
			break;
		}
		case LAUNCHING:
		case ORBITSEARCH:
		case ORBITCHECK:
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
		//$FALL-THROUGH$
		case ORBITCRUISE:
		{
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
			break;
		}
		case LANDING_APPROACH:
		{
			if(targetObject==null)
			{
				final String reason = "no target planetary information";
				this.cancelNavigation();
				super.addScreenMessage(L("Landing program aborted with error ("+reason+")."));
				return;
			}
			final Dir3D dirToPlanet = CMLib.space().getDirection(ship, targetObject);
			//final long distance=CMLib.space().getDistanceFrom(ship, programPlanet)
			//		- Math.round(CMath.mul(programPlanet.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
			//		- ship.radius();
			final double atmoWidth = CMath.mul(targetObject.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - targetObject.radius();
			final long critRadius = Math.round(targetObject.radius() + (atmoWidth / 2.0));
			final long distanceToCritRadius=CMLib.space().getDistanceFrom(ship, targetObject)
					- critRadius
					- ship.radius();
			if(distanceToCritRadius <= 0)
				track.state = ShipNavState.LANDING;
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
						performSingleThrust(engineE,Double.valueOf(0.0), false);
						break;
					}
					else
					if(ticksToDecellerate > ticksToDestinationAtCurrentSpeed)
						this.changeFacing(ship, CMLib.space().getOppositeDir(dirToPlanet));
					else
					if((ticksToDecellerate<50)||(diff > 10.0))
						this.changeFacing(ship, dirToPlanet);
					newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
					if((targetAcceleration > 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
						newInject = forceAccelerationAllProgramEngines(programEngines, targetAcceleration);
					}
					performSimpleThrust(engineE,newInject, false);
				}
				break;
			}
		}
		//$FALL-THROUGH$
		case LANDING:
		{
			final Dir3D dirToPlanet = CMLib.space().getDirection(ship, targetObject);
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
				if(targetObject==null)
				{
					final String reason = "no target planetary information";
					this.cancelNavigation();
					super.addScreenMessage(L("Landing program aborted with error ("+reason+")."));
					return;
				}
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject)
						- targetObject.radius()
						- ship.radius()
						-10; // margin for soft landing
				final double atmoWidth = CMath.mul(targetObject.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - targetObject.radius();
				final long critRadius = Math.round(targetObject.radius() + (atmoWidth / 2.0));
				final long distanceToCritRadius=CMLib.space().getDistanceFrom(ship, targetObject)
						- critRadius
						- ship.radius();
				final double ticksToDestinationAtCurrentSpeed = Math.abs(CMath.div(distance, ship.speed()));
				final double ticksToDecellerate = CMath.div(ship.speed(),CMath.div(this.targetAcceleration.doubleValue(), 2.0));
				if((ticksToDecellerate > ticksToDestinationAtCurrentSpeed)
				||(distance < ship.speed() * 20))
				{
					targetAcceleration = 0.0;
					if(ship.speed() > this.targetAcceleration.doubleValue())
					{
						if(ship.speed() < (this.targetAcceleration.doubleValue() + 1.0))
							targetAcceleration = 1.0;
						else
							targetAcceleration = this.targetAcceleration.doubleValue();
					}
					else
					if(ship.speed()>CMLib.space().getDistanceFrom(ship, targetObject)/4)
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
						Log.debugOut("Landing Deccelerating Check "+  Math.abs(this.savedAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(programEngines, targetAcceleration);
					}
				}
				else
				if((distance > distanceToCritRadius) && (ship.speed() < Math.sqrt(distance)))
				{
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
						Log.debugOut("Landing Accelerating because " +  distance +" > "+distanceToCritRadius+" and "+ship.speed()+"<"+Math.sqrt(distance));
					this.changeFacing(ship, dirToPlanet);
					newInject=calculateMarginalTargetInjection(this.lastInject, targetAcceleration);
					if((targetAcceleration >= 1.0) && (newInject.doubleValue()==0.0))
					{
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
						Log.debugOut("Landing Accelerating Check "+  Math.abs(this.savedAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(programEngines, targetAcceleration);
					}
				}
				else
				{
					//this.changeFacing(ship, CMLib.space().getOppositeDir(dirToPlanet));
					newInject=Double.valueOf(0.0);
				}
			}
			if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			{
				Log.debugOut("Landing: dir="+CMLib.english().directionDescShort(ship.direction().toDoubles())
						+"/speed="+ship.speed()+"/inject="+((newInject != null) ? newInject.toString():"null"));
			}
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, true);
			break;
		}
		}
	}

	@Override
	protected boolean checkPowerCurrent(final int value)
	{
		if(navTrack != null)
			doNavigation(navTrack);
		return super.checkPowerCurrent(value);
	}

	protected SoftwareProcedure launchProcedure = new SoftwareProcedure()
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
			if(ship.getIsDocked() == null)
			{
				addScreenMessage(L("Error: Ship is already launched."));
				return false;
			}
			if(navTrack!=null)
			{
				addScreenMessage(L("Warning. Previous program cancelled."));
				cancelNavigation();
			}
			final SpaceObject programPlanet=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
			final ShipEngine engineE =primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
			if(engineE==null)
			{
				addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
				return false;
			}
			targetAcceleration= Double.valueOf(findTargetAcceleration(engineE));
			if(targetAcceleration.doubleValue() < SpaceObject.ACCELERATION_DAMAGED)
			{
				final int gs = (int)Math.round(targetAcceleration.doubleValue()/SpaceObject.ACCELERATION_G);
				addScreenMessage(L("No inertial dampeners found.  Limiting acceleration to "+gs+"Gs."));
			}
			if(cancelNavigation())
				addScreenMessage(L("Warning. Previous program cancelled."));
			final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
			if(uword.equalsIgnoreCase("ORBIT"))
				navTrack = new ShipNavTrack(ShipNavProcess.ORBIT, programPlanet, programEngines);
			else
				navTrack = new ShipNavTrack(ShipNavProcess.LAUNCH, programPlanet, programEngines);
			addScreenMessage(L("Launch procedure initialized."));
			return false;
		}
	};

	protected SoftwareProcedure orbitProcedure = launchProcedure;

	protected SoftwareProcedure stopProcedure = new SoftwareProcedure()
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
			if((ship.getIsDocked() != null)||(ship.speed()==0.0))
			{
				addScreenMessage(L("Error: Ship is already stopped."));
				return false;
			}
			if(cancelNavigation())
				addScreenMessage(L("Warning. Previous program cancelled."));
			ShipEngine engineE=null;
			if(!flipForAllStop(ship))
			{
				addScreenMessage(L("Warning. Stop program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			else
				engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
			if(engineE==null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			findTargetAcceleration(engineE);
			final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
			navTrack = new ShipNavTrack(ShipNavProcess.STOP, programEngines);
			addScreenMessage(L("All Stop procedure initialized."));
			return false;
		}
	};

	protected SoftwareProcedure landProcedure = new SoftwareProcedure()
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
			if(ship.getIsDocked() != null)
			{
				addScreenMessage(L("Error: Ship is already landed."));
				return false;
			}
			if(sensorReps.size()==0)
			{
				addScreenMessage(L("Error: no sensor data found to identify landing position."));
				return false;
			}
			final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
			for(final TechComponent sensor : getShipSensors())
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
				addScreenMessage(L("No suitable landing target found within near sensor range."));
				return false;
			}

			if(cancelNavigation())
				addScreenMessage(L("Warning. Previous program cancelled."));
			ShipEngine engineE=null;
			if(!flipForAllStop(ship))
			{
				addScreenMessage(L("Warning. Landing program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			else
				engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
			if(engineE==null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			targetAcceleration=Double.valueOf(findTargetAcceleration(engineE));
			final SpaceObject programPlanet = landingPlanet;
			final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
			// this lands you at the nearest point, which will pick the nearest location room, if any
			//TODO: picking the nearest landing zone, orbiting to it, and THEN landing would be better.
			navTrack = new ShipNavTrack(ShipNavProcess.LAND, programPlanet, programEngines);
			final long distance=CMLib.space().getDistanceFrom(ship.coordinates(),landingPlanet.coordinates());
			if(distance > (ship.radius() + Math.round(landingPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
				addScreenMessage(L("Landing approach procedure initialized."));
			else
				addScreenMessage(L("Landing procedure initialized."));
			return false;
		}
	};

	protected SoftwareProcedure courseProcedure = new SoftwareProcedure()
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
				addScreenMessage(L("Error: COURSE requires the name/coordinates of the target.   Try HELP."));
				return false;
			}
			final String targetStr=CMParms.combine(parsed, 1);
			SpaceObject targetObj = null;
			if(sensorReps.size()>0)
			{
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for(final TechComponent sensor : getShipSensors())
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(spaceObject));
				targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, false);
				if(targetObj == null)
					targetObj = (SpaceObject)CMLib.english().fetchEnvironmental(allObjects, targetStr, true);
				if(targetObj != null)
				{
					if(targetObj.coordinates() == null)
					{
						addScreenMessage(L("Error: Can not plot course to @x1 due to lack of coordinate information.",targetObj.name()));
						return false;
					}
				}
			}
			if(targetObj == null)
			{
				final Coord3D targetCoords = findCoordinates(targetStr);
				if(targetCoords == null)
				{
					addScreenMessage(L("Error: Unable to find course target '@x1'.",targetStr));
					return false;
				}
				final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(targetCoords, 0, 10);
				for(final SpaceObject o1 : objs)
				{
					if(targetCoords.equals(o1.coordinates()))
						courseTarget = o1;
				}
				if(courseTarget == null)
				{
					courseTarget = (SpaceObject)CMClass.getBasicItem("Moonlet");
					courseTarget.setRadius(ship.radius());
					courseTarget.setName("Nav Point");
					courseTarget.setCoords(targetCoords);
				}
			}
			if(cancelNavigation())
				addScreenMessage(L("Warning. Previous program cancelled."));
			addScreenMessage(L("Plotting course to @x1.",targetStr));
			return false;
		}
	};

	protected SoftwareProcedure faceProcedure = new SoftwareProcedure()
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
				addScreenMessage(L("Error: FACE requires the name of the object.   Try HELP."));
				return false;
			}
			if(sensorReps.size()==0)
			{
				addScreenMessage(L("Error: no sensor data found to identify object."));
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
				addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
				return false;
			}
			if(targetObj.coordinates() == null)
			{
				addScreenMessage(L("Can not face @x1 due to lack of coordinate information.",targetObj.name()));
				return false;
			}
			final Dir3D facing=ship.facing();
			final Dir3D dirTo = CMLib.space().getDirection(spaceObject, targetObj);
			double fdist1=(facing.xy().compareTo(dirTo.xy())>0)?facing.xyd()-dirTo.xyd():dirTo.xyd()-facing.xyd();
			final double fdist2=(facing.z().compareTo(dirTo.z())>0)?facing.zd()-dirTo.zd():dirTo.zd()-facing.zd();
			if(fdist1>Math.PI)
				fdist1=(Math.PI*2)-fdist1;
			final double deltaTo=fdist1+fdist2;
			//final double deltaTo = CMLib.space().getAngleDelta(ship.facing(), dirTo);
			if(deltaTo < 0.02)
				addScreenMessage(L("Already facing @x1.",targetObj.name()));
			else
			{
				ShipDirectional.ShipDir portDir;
				if(facing.xy().compareTo(dirTo.xy())>0)
				{
					if(fdist1 == facing.xyd()-dirTo.xyd())
						portDir=ShipDirectional.ShipDir.PORT;
					else
						portDir=ShipDirectional.ShipDir.STARBOARD;
				}
				else
				{
					if(fdist1 == dirTo.xyd()-facing.xyd())
						portDir=ShipDirectional.ShipDir.STARBOARD;
					else
						portDir=ShipDirectional.ShipDir.PORT;
				}
				final ShipEngine engineE=findEngineByPort(portDir);
				if(engineE==null)
				{
					addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
					return false;
				}
				Dir3D oldFacing=ship.facing().copyOf();
				String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
				CMMsg msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(sendMessage(mob, engineE, msg, unparsed))
				{
					if(oldFacing.xy().equals(ship.facing().xy()))
					{
						addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
						return false;
					}
					else
					if(CMath.pctDiff(dirTo.xyd(),ship.facing().xyd(),Math.PI*2.0)<.05)
					{}
					else
					{
						addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
								portDir.name(),""+Math.round(CMath.pctDiff(dirTo.xyd(),ship.facing().xyd(),Math.PI*2.0)*100.0)));
						return false;
					}
					if(cancelNavigation())
						addScreenMessage(L("Warning. Previous program cancelled."));
					if(facing.z().compareTo(dirTo.z())>0)
						portDir=ShipDirectional.ShipDir.VENTRAL;
					else
						portDir=ShipDirectional.ShipDir.DORSEL;
					code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
					oldFacing=ship.facing().copyOf();
					msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, unparsed))
					{
						if(oldFacing.z().equals(ship.facing().z()))
						{
							addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return false;
						}
						else
						if(CMath.pctDiff(dirTo.zd(),ship.facing().zd(),Math.PI)<.05)
							addScreenMessage(L("Now facing @x1.",targetObj.name()));
						else
						{
							addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo.zd(),ship.facing().zd(),Math.PI)*100.0)));
							return false;
						}
					}
				}
			}
			return false;
		}
	};

	protected SoftwareProcedure cancelProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			if(navTrack == null)
			{
				addScreenMessage(L("Error: No programs running."));
				return false;
			}
			final String name = CMStrings.capitalizeAndLower(navTrack.proc.name());
			addScreenMessage(L("Confirmed: "+name+" program stopped."));
			cancelNavigation();
			return false;
		}
	};

	protected SoftwareProcedure approachProcedure = new SoftwareProcedure()
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
				addScreenMessage(L("Error: APPROACH requires the name of the object.   Try HELP."));
				return false;
			}
			if(sensorReps.size()==0)
			{
				addScreenMessage(L("Error: no sensor data found to identify object."));
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
				addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
				return false;
			}
			if(targetObj.coordinates() == null)
			{
				addScreenMessage(L("Can not approach @x1 due to lack of coordinate information.",targetObj.name()));
				return false;
			}
			final SpaceObject approachTarget = targetObj;
			long distance = CMLib.space().getDistanceFrom(ship, targetObj);
			distance = (distance - ship.radius() - targetObj.radius())/2;
			if(distance < 100)
			{
				addScreenMessage(L("Can not approach @x1 due being too close.",targetObj.name()));
				return false;
			}
			if(cancelNavigation())
				addScreenMessage(L("Warning. Previous program cancelled."));
			ShipEngine engineE=null;
			final Dir3D dirTo = CMLib.space().getDirection(ship, targetObj);
			if(!changeFacing(ship, dirTo))
			{
				addScreenMessage(L("Warning. Approach program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED, null);
			if(engineE==null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			targetAcceleration=Double.valueOf(findTargetAcceleration(engineE));
			final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
			final List<SpaceObject> navs = calculateNavigation(ship, targetObj, allObjects);
			if(navs == null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Unable to navigate to target."));
				return false;
			}
			navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, approachTarget, programEngines, navs);
			addScreenMessage(L("Approach to @x1 procedure engaged.",targetObj.name()));
			return false;
		}
	};

	protected SoftwareProcedure engineProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final ShipEngine engineE=findEngineByName(uword);
			if(engineE==null)
			{
				addScreenMessage(L("Error: Unknown engine name or command word '"+uword+"'.   Try HELP."));
				return false;
			}
			final Electronics E=engineE;
			ShipDirectional.ShipDir portDir=ShipDirectional.ShipDir.AFT;
			if(parsed.size()>3)
			{
				addScreenMessage(L("Error: Too many parameters."));
				return false;
			}
			if(parsed.size()==1)
			{
				addScreenMessage(L("Error: No thrust amount given."));
				return false;
			}
			final String amountStr = parsed.get(parsed.size()-1);
			double injectAmount;
			long speedLimit = Long.MAX_VALUE;
			if(CMath.isNumber(amountStr))
				injectAmount=CMath.s_double(amountStr);
			else
			{
				final BigDecimal d = CMLib.english().parseSpaceSpeed(amountStr);
				if((d==null)||(d.doubleValue()<=0.0))
				{
					addScreenMessage(L("Error: '@x1' is not a valid amount or speed.",amountStr));
					return false;
				}
				injectAmount = d.doubleValue();
				final double targetAmt = findTargetAcceleration(engineE);
				if(injectAmount > targetAmt)
				{
					speedLimit = Math.round(injectAmount);
					injectAmount = targetAmt;
				}
			}
			if((engineE.isReactionEngine())
			&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.AFT)))
			{
				if(lastInject == null)
				{
					final SpaceObject spaceObject=CMLib.space().getSpaceObject(sw,true);
					final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
					if((primeMainThrusters(ship, injectAmount, engineE) == engineE)
					&&(lastInject != null))
						injectAmount = calculateMarginalTargetInjection(lastInject,injectAmount).doubleValue();
					else
					{
						addScreenMessage(L("Error: '@x1' priming failure.",engineE.name()));
						return false;
					}
				}
				else
					injectAmount = calculateMarginalTargetInjection(lastInject,injectAmount).doubleValue();
			}
			if(parsed.size()==3)
			{
				final String dirNm = parsed.get(1).toUpperCase().trim();
				portDir=(ShipDirectional.ShipDir)CMath.s_valueOf(ShipDirectional.ShipDir.class, dirNm);
				if(portDir == null)
					portDir=(ShipDirectional.ShipDir)CMath.s_valueOfStartsWith(ShipDirectional.ShipDir.class, dirNm);
				if(portDir!=null)
				{
					if(!CMParms.contains(engineE.getAvailPorts(), portDir))
					{
						addScreenMessage(L("Error: '@x1' is not a valid direction for that engine.  Try: @x2.",dirNm,CMParms.toListString(engineE.getAvailPorts())));
						return false;
					}
				}
				else
				{
					addScreenMessage(L("Error: '@x1' is not a valid direction for that engine.  Try: @x2.",dirNm,CMParms.toListString(engineE.getAvailPorts())));
					return false;
				}
			}
			if(navTrack != null)
				navTrack.speedLimit = speedLimit;
			else
			if(courseSet && (course.size()>0))
			{
				targetAcceleration=Double.valueOf(findTargetAcceleration(engineE));
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, courseTarget, programEngines, course);
				return true;
			}
			CMMsg msg = null;
			if(injectAmount > 0)
			{
				final String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(injectAmount));
				msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			}
			else
				msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
			sendMessage(mob,E,msg,unparsed);
			return true;
		}
	};

	protected SoftwareProcedure moonProcedure = new SoftwareProcedure()
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
				addScreenMessage(L("Error: MOON requires the name of the object.   Try HELP."));
				return false;
			}
			if(sensorReps.size()==0)
			{
				addScreenMessage(L("Error: no sensor data found to identify object."));
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
				addScreenMessage(L("No suitable object @x1 found within sensor range.",targetStr));
				return false;
			}
			if(targetObj.coordinates() == null)
			{
				addScreenMessage(L("Can not moon @x1 due to lack of coordinate information.",targetObj.name()));
				return false;
			}
			final Dir3D facing=ship.facing();
			final Dir3D notDirTo=CMLib.space().getDirection(spaceObject, targetObj);
			final Dir3D dirTo = CMLib.space().getOppositeDir(notDirTo);
			double fdist1=(facing.xy().compareTo(dirTo.xy())>0)?facing.xyd()-dirTo.xyd():dirTo.xyd()-facing.xyd();
			final double fdist2=(facing.z().compareTo(dirTo.z())>0)?facing.zd()-dirTo.zd():dirTo.zd()-facing.zd();
			if(fdist1>Math.PI)
				fdist1=(Math.PI*2)-fdist1;
			final double deltaTo=fdist1+fdist2;
			//final double deltaTo = CMLib.space().getAngleDelta(ship.facing(), dirTo);
			if(deltaTo < 0.02)
				addScreenMessage(L("Already mooning @x1.",targetObj.name()));
			else
			{
				ShipDirectional.ShipDir portDir;
				if(facing.xy().compareTo(dirTo.xy())>0)
				{
					if(fdist1 == facing.xyd()-dirTo.xyd())
						portDir=ShipDirectional.ShipDir.PORT;
					else
						portDir=ShipDirectional.ShipDir.STARBOARD;
				}
				else
				{
					if(fdist1 == dirTo.xyd()-facing.xyd())
						portDir=ShipDirectional.ShipDir.STARBOARD;
					else
						portDir=ShipDirectional.ShipDir.PORT;
				}
				final ShipEngine engineE=findEngineByPort(portDir);
				if(engineE==null)
				{
					addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
					return false;
				}
				if(cancelNavigation())
					addScreenMessage(L("Warning. Previous program cancelled."));
				Dir3D oldFacing=ship.facing().copyOf();
				String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
				CMMsg msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(sendMessage(mob, engineE, msg, unparsed))
				{
					if(oldFacing.xy().equals(ship.facing().xy()))
					{
						addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
						return false;
					}
					else
					if(CMath.pctDiff(dirTo.xyd(),ship.facing().xyd(),Math.PI*2.0)<.05)
					{}
					else
					{
						addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
								portDir.name(),""+Math.round(CMath.pctDiff(dirTo.xyd(),ship.facing().xyd(),Math.PI*2.0)*100.0)));
						return false;
					}
					if(facing.z().compareTo(dirTo.z())>0)
						portDir=ShipDirectional.ShipDir.VENTRAL;
					else
						portDir=ShipDirectional.ShipDir.DORSEL;
					code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
					oldFacing=ship.facing().copyOf();
					msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, unparsed))
					{
						if(oldFacing.z().equals(ship.facing().z()))
						{
							addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return false;
						}
						else
						if(CMath.pctDiff(dirTo.zd(),ship.facing().zd(),Math.PI)<.05)
							addScreenMessage(L("Now mooning @x1.",targetObj.name()));
						else
						{
							addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo.zd(),ship.facing().zd(),Math.PI)*100.0)));
							return false;
						}
					}
				}
			}
			return false;
		}
	};

}
