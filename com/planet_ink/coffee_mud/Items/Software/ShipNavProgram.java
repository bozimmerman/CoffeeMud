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
   Copyright 2022-2023 Bo Zimmerman

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

	protected final List<long[]>	course				= new LinkedList<long[]>();
	protected volatile long[]		courseTargetCoords	= null;
	protected volatile long			courseTargetRadius	= 0;
	protected volatile Double		lastAcceleration	= null;
	protected volatile Double		lastAngle			= null;
	protected volatile Double		lastInject			= null;
	protected volatile Double		targetAcceleration	= Double.valueOf(SpaceObject.ACCELERATION_TYPICALSPACEROCKET);
	protected volatile ShipNavTrack	navTrack			= null;

	protected final Map<ShipEngine, Double[]>	injects	= new Hashtable<ShipEngine, Double[]>();

	protected static class ShipNavTrack
	{
		protected ShipNavProcess proc;
		protected ShipNavState state;
		protected Object[] args;
		protected Class<?>[] types;
		protected Map<Class<?>, Integer> classMap;
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
		LAUNCHING(),
		STOP(),
		PRE_LANDING_STOP(),
		LANDING_APPROACH(),
		LANDING(),
		ORBITSEARCH(),
		ORBITCHECK(),
		ORBITCRUISE(),
		APPROACH(),
		DEPROACH()
		;
	}

	protected static enum ShipNavProcess
	{
		STOP(ShipNavState.STOP, List.class),
		LAUNCH(ShipNavState.LAUNCHING, SpaceObject.class, List.class),
		LAND(ShipNavState.PRE_LANDING_STOP, SpaceObject.class, List.class),
		ORBIT(ShipNavState.ORBITSEARCH, SpaceObject.class, List.class),
		APPROACH(ShipNavState.APPROACH, SpaceObject.class, List.class, Long.class)
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

	protected void cancelNavigation()
	{
		lastInject = null;
		navTrack=null;
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

	protected Double forceAccelerationAllProgramEngines(final Collection<ShipEngine> programEngines, final double targetAcceleration)
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

	protected boolean doCollisionDetection()
	{
		// generate a warning, or an alert
		// warning = can be halted at max acceleration
		// alert = can not
		//TODO:BZ
		return false;
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
		return SpaceObject.ACCELERATION_DAMAGED;
	}

	protected boolean flipForAllStop(final SpaceShip ship)
	{
		final double[] stopFacing = CMLib.space().getOppositeDir(ship.direction());
		return changeFacing(ship, stopFacing);
	}

	protected boolean changeFacing(final SpaceShip ship, final double[] newFacing)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		M.setName(ship.Name());
		final boolean isDebugging = CMSecurity.isDebugging(DbgFlag.SPACESHIP);
		CMLib.space().getOppositeDir(ship.facing()); // I think this is to normalize the facing dir
		try
		{
			double angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			int tries=100;
			while((angleDiff > 0.0001)&&(--tries>0))
			{
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
						final double angleAchievedPerPt = Math.abs(this.lastAngle.doubleValue()); //
						double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						for(int i=0;i<100;i++)
						{
							if(Math.abs(angleDelta[0]) > 0.00001)
							{
								final ShipDirectional.ShipDir dir = angleDelta[0] < 0 ? ShipDir.PORT : ShipDir.STARBOARD;
								final Double thrust = Double.valueOf(Math.abs(angleDelta[0]) / angleAchievedPerPt);
								if(isDebugging)
									Log.debugOut("Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+dir+" to achieve delta, and go from "+ship.facing()[0]+" to "+newFacing[0]);
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
							{
								Log.debugOut("Turn Deltas now: "+(Math.round(angleDelta[0]*100)/100.0)+" + "+(Math.round(angleDelta[1]*100)/100.0)
										+"=="+(Math.round(Math.abs((angleDelta[0])+Math.abs(angleDelta[1]))*100)/100.0));
							}
							if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
								break;
						}
						angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), newFacing); // starboard is -, port is +
						for(int i=0;i<100;i++)
						{
							if(Math.abs(angleDelta[1]) > 0.00001)
							{
								final ShipDirectional.ShipDir dir = angleDelta[1] < 0 ? ShipDir.VENTRAL : ShipDir.DORSEL;
								final Double thrust = Double.valueOf(Math.abs(angleDelta[1]) / angleAchievedPerPt);
								if(isDebugging)
									Log.debugOut("Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+dir+" to achieve delta, and go from "+ship.facing()[1]+" to "+newFacing[1]);
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
							{
								Log.debugOut("Turn Deltas now: "+(Math.round(angleDelta[0]*100)/100.0)+" + "+(Math.round(angleDelta[1]*100)/100.0)
										+"=="+(Math.round(Math.abs((angleDelta[0])+Math.abs(angleDelta[1]))*100)/100.0));
							}
						}
						if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))<.01)
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

	protected ShipEngine primeMainThrusters(final SpaceShip ship, final double maxAceleration)
	{
		CMMsg msg;
		final List<ShipEngine> engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final boolean isDocked = ship.getIsDocked()!=null;
		try
		{
			for(final ShipEngine engineE : engines)
			{
				if(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.AFT))
				{
					final double targetAcceleration = findTargetAcceleration(engineE);
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
					int stableCounter = 0;
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
							&&((!isDocked)||(ship.getIsDocked()==null)))
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
		if(this.courseTargetCoords != null)
		{
			final SpaceObject me = CMLib.space().getSpaceObject(this,true);
			if(me == null)
				this.courseTargetCoords = null;
			else
			{
				long[] srcCoords = me.coordinates();
				if(this.course.size()>0)
					srcCoords = course.get(this.course.size()-1).clone();
				final List<long[]> newBits = CMLib.space().plotCourse(srcCoords, me.radius(), courseTargetCoords, courseTargetRadius, 1);
				if(newBits.size()==0)
				{
					this.courseTargetCoords = null;
					super.addScreenMessage(L("Failed to plot course."));
				}
				else
				{
					this.course.addAll(newBits);
					for(final long[] bit : newBits)
					{
						if(Arrays.equals(bit, courseTargetCoords))
						{
							this.courseTargetCoords = null;
							super.addScreenMessage(L("Course plotted."));
							break;
						}
					}
					if(courseTargetCoords != null)
					{
						// still plotting
					}
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
		super.executeMsg(myHost, msg);
	}

	protected long calculateDeproachDistance(final SpaceObject ship, final SpaceObject targetObj)
	{
		long distance = CMLib.space().getDistanceFrom(ship, targetObj);
		distance = (distance - ship.radius() - Math.round(CMath.mul(targetObj.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
		if(ship.speed() < SpaceObject.VELOCITY_SOUND)
			return distance/2;
		else
		if(ship instanceof SpaceShip)
		{
			if(targetAcceleration == null)
			{
				final ShipEngine engineE = this.primeMainThrusters((SpaceShip)ship, SpaceObject.ACCELERATION_DAMAGED);
				if(engineE != null)
					this.targetAcceleration = Double.valueOf( findTargetAcceleration(engineE) );
			}
			final Double targetAcceleration = this.targetAcceleration;
			if(targetAcceleration != null)
			{
				final double ticksToZero = ship.speed() / targetAcceleration.doubleValue();
				final double distanceToZero = ticksToZero * (ship.speed()/2);
				final long d20 = Math.round(distanceToZero);
				final long baseDistance = distance - d20;
				if(baseDistance < 0)
					return distance;
				return Math.round(ship.speed()) + d20 + (baseDistance/2);
			}
		}
		return distance; // begin deproach IMMEDIATELY!
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

	protected void doNavigation(final ShipNavTrack track)
	{
		final ShipNavProcess proc = track.proc;
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

		// check pre-reqs and completions of the overall process first
		switch(proc)
		{
		case APPROACH:
		{
			if(targetObject==null)
			{
				final String reason = "no target information";
				cancelNavigation();
				super.addScreenMessage(L("Approach program aborted with error ("+reason+")."));
				return;
			}
			final long distance = (CMLib.space().getDistanceFrom(ship, targetObject)-ship.radius()
								-Math.round(CMath.mul(targetObject.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
			int safeDistance=100 + (int)Math.round(ship.speed());
			final double[] dirTo = CMLib.space().getDirection(ship, targetObject);
			final double[] diffDelta = CMLib.space().getFacingAngleDiff(ship.direction(), dirTo); // starboard is -, port is +
			if((Math.abs(diffDelta[0])+Math.abs(diffDelta[1]))<.05)
				safeDistance += (int)Math.round(ship.speed());
			if(distance < safeDistance)
			{
				for(final ShipEngine engineE : programEngines)
					performSimpleThrust(engineE,Double.valueOf(0.0), true);
				this.cancelNavigation();
				super.addScreenMessage(L("Approach program completed."));
				return;
			}
			break;
		}
		case LAND:
			if(ship.getIsDocked()!=null)
			{
				cancelNavigation();
				super.addScreenMessage(L("Landing program completed successfully."));
				return;
			}
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				cancelNavigation();
				super.addScreenMessage(L("Launding program aborted with error ("+reason+")."));
				return;
			}
			break;
		case LAUNCH:
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				this.cancelNavigation();
				super.addScreenMessage(L("Launch program aborted with error ("+reason+")."));
				return;
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
					return;
				}
			}
			break;
		case ORBIT:
			if(targetObject==null)
			{
				final String reason = "no planetary information";
				this.cancelNavigation();
				super.addScreenMessage(L("Orbit program aborted with error ("+reason+")."));
				return;
			}
			{
				//TODO: this is completely wrong
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject);
				if(distance > (targetObject.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					super.addScreenMessage(L("Orbit program completed. Neutralizing velocity."));
					this.cancelNavigation();
					return;
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
				return;
			}
			track.state = ShipNavState.STOP; // no need to have any other state
			break;
		}

		// now determine state pre-reqs and completion

		switch(track.state)
		{
		case LANDING:
		case LANDING_APPROACH:
		case PRE_LANDING_STOP:
		{
			if(targetObject==null)
			{
				final String reason = "no planetary target information";
				this.cancelNavigation();
				super.addScreenMessage(L("Launch program aborted with error ("+reason+")."));
				return;
			}
			if(track.state!=ShipNavState.LANDING)
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
		//$FALL-THROUGH$
		case STOP:
		{
			if(ship.speed()  > 0.0)
			{
				final double[] stopFacing = CMLib.space().getOppositeDir(ship.direction());
				final double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), stopFacing); // starboard is -, port is +
				if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))>.02)
				{
					if(!flipForAllStop(ship))
					{
						cancelNavigation();
						super.addScreenMessage(L("Stop program aborted with error (directional control failure)."));
						return;
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
			if(targetObject==null)
			{
				final String reason = "no target information";
				this.cancelNavigation();
				super.addScreenMessage(L("Last program aborted with error ("+reason+")."));
				return;
			}
			final Long deproachDistance = track.getArg(Long.class);
			final long distance = (CMLib.space().getDistanceFrom(ship, targetObject)-ship.radius()-targetObject.radius());
			if(distance < deproachDistance.longValue())
			{
				if(track.state == ShipNavState.APPROACH)
					track.state=ShipNavState.DEPROACH;
			}
			else
			{
				if(track.state == ShipNavState.DEPROACH)
					track.state=ShipNavState.APPROACH;
			}
			final double[] desiredFacing;
			if(track.state == ShipNavState.APPROACH)
				desiredFacing = CMLib.space().getDirection(ship, targetObject);
			else
				desiredFacing = CMLib.space().getOppositeDir(ship.direction());
			final double[] angleDelta = CMLib.space().getFacingAngleDiff(ship.facing(), desiredFacing); // starboard is -, port is +
			if((Math.abs(angleDelta[0])+Math.abs(angleDelta[1]))>.02)
			{
				if(!changeFacing(ship, desiredFacing))
				{
					this.cancelNavigation();
					super.addScreenMessage(L("Last program aborted with error (directional control failure)."));
					return;
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
		switch(track.state)
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
			if((ship.speed()<0.1)
			&&(track.state==ShipNavState.DEPROACH)
			&&(targetObject != null))
			{
				final long distance = (CMLib.space().getDistanceFrom(ship, targetObject)
										-ship.radius()
										-Math.round(CMath.mul(targetObject.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				if(distance > 100)
				{
					track.state=ShipNavState.APPROACH;
					track.setArg(Long.class, Long.valueOf(calculateDeproachDistance(ship, targetObject)));
					primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
				}
			}
			break;
		}
		case LAUNCHING:
		case ORBITSEARCH:
		case ORBITCHECK:
		{
			final double targetAcceleration = this.targetAcceleration.doubleValue(); //
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
		}
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
			final double[] dirToPlanet = CMLib.space().getDirection(ship, targetObject);
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
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
			final double[] dirToPlanet = CMLib.space().getDirection(ship, targetObject);
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
					double targetAcceleration = 0.0;
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
						Log.debugOut("Landing Deccelerating Check "+  Math.abs(this.lastAcceleration.doubleValue()-targetAcceleration));
						newInject = forceAccelerationAllProgramEngines(programEngines, targetAcceleration);
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
						Log.debugOut("Landing Accelerating Check "+  Math.abs(this.lastAcceleration.doubleValue()-targetAcceleration));
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
				Log.debugOut("Landing: dir="+CMLib.english().directionDescShort(ship.direction())+"/speed="+ship.speed()+"/inject="+((newInject != null) ? newInject.toString():"null"));
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
}
