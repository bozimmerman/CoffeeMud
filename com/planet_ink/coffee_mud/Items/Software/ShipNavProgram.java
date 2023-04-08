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
	protected volatile Double		savedAcceleration	= null;
	protected volatile Double		savedSpeedDelta		= null;
	protected volatile Double		savedAngle			= null;
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
			this.savedAcceleration =null;
			this.savedSpeedDelta=null;
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
						this.savedAngle = null;
						final String code=TechCommand.THRUST.makeCommand(ShipDir.PORT,Double.valueOf(1));
						msg.setTargetMessage(code);
						this.trySendMsgToItem(M, engineE, msg);
						if(this.savedAngle==null)
							break;
						final double angleAchievedPerPt = Math.abs(this.savedAngle.doubleValue()); //
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
								this.savedAngle = null;
								this.trySendMsgToItem(M, engineE, msg);
								if(this.savedAngle==null)
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
								this.savedAngle = null;
								this.trySendMsgToItem(M, engineE, msg);
								if(this.savedAngle==null)
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
			course.clear();
			courseTargetCoords	= null;
			courseTargetRadius	= 0;
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

	protected LinkedList<SpaceObject> calculateNavigation(final SpaceObject ship, final SpaceObject targetObj)
	{
		final LinkedList<SpaceObject> navs = new LinkedList<SpaceObject>();
		//TODO: look for obsticles and make more complex nav
		navs.add(targetObj);
		return navs;
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
				return false;
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
				//TODO: this is completely wrong
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject);
				if(distance > (targetObject.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					super.addScreenMessage(L("Orbit program completed. Neutralizing velocity."));
					this.cancelNavigation();
					return false;
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
		// the stop part makes sure you are facing correctly, and might goose injection
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
				final SpaceObject intTarget = navList.getFirst();
				final long distToITarget = (CMLib.space().getDistanceFrom(ship, intTarget)-ship.radius()
						-Math.round(CMath.mul(intTarget.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				final double[] dirToITarget = CMLib.space().getDirection(ship.coordinates(), intTarget.coordinates());
				final double[] opShipDir = CMLib.space().getOppositeDir(ship.direction());
				final double[] opDirToTarget = CMLib.space().getOppositeDir(dirToITarget);
				final double toDirDiff = CMLib.space().getAngleDelta(ship.direction(), dirToITarget);
				//final double opDirDiff = CMLib.space().getAngleDelta(ship.direction(), opDir);
				if(toDirDiff < 0.08)
				{
					final double[] correctFacing;
					// first, check if we should be approaching, or deproaching
					if((ship.speed()>targetAcceleration)
					&& (targetAcceleration > 0.0))
					{
						final double ticksToStop = ship.speed() / targetAcceleration;
						if(ship.speed() * ticksToStop > distToITarget)
						{

							//correctFacing = CMLib.space().getMiddleDirection(opShipDir, opDirToTarget);
							track.state = ShipNavState.DEPROACH;
						}
						else
						{
							track.state = ShipNavState.APPROACH;
						}
					}
					else
					{
						track.state = ShipNavState.APPROACH;
					}
				}
				else
				if(ship.speed() > (targetAcceleration * 3))
				{
					//double[] correctFacing = CMLib.space().getMiddleDirection(opDir, opDirToTarget)
				}
				else
				if(CMLib.space().getAngleDelta(ship.facing(), dirToITarget)>0.3)
					changeFacing(ship, dirToITarget);
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
			final Double oldInject = newInject;
			newInject=calculateMarginalTargetInjection(newInject, targetAcceleration);
			if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				Log.debugOut(ship.Name(),"Old engine inject value = "+oldInject+", new="+newInject); //TODO:BZ:DELME
			for(final ShipEngine engineE : programEngines)
				performSimpleThrust(engineE,newInject, false);
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
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
						primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
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
			final ShipEngine engineE =primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
			if(engineE==null)
			{
				addScreenMessage(L("Error: Malfunctioning launch thrusters interface."));
				return false;
			}
			if(findTargetAcceleration(engineE) < SpaceObject.ACCELERATION_DAMAGED)
			{
				final int gs = (int)Math.round(targetAcceleration.doubleValue()/SpaceObject.ACCELERATION_G);
				addScreenMessage(L("No inertial dampeners found.  Limiting acceleration to "+gs+"Gs."));
			}
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
			if(navTrack!=null)
			{
				addScreenMessage(L("Warning. Previous program cancelled."));
				cancelNavigation();
			}
			ShipEngine engineE=null;
			if(!flipForAllStop(ship))
			{
				addScreenMessage(L("Warning. Stop program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			else
				engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
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
				addScreenMessage(L("No suitable landing target found within near sensor range."));
				return false;
			}

			if(navTrack!=null)
			{
				addScreenMessage(L("Warning. Previous program cancelled."));
				cancelNavigation();
			}
			ShipEngine engineE=null;
			if(!flipForAllStop(ship))
			{
				addScreenMessage(L("Warning. Landing program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			else
				engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
			if(engineE==null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			findTargetAcceleration(engineE);
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
						addScreenMessage(L("Error: Can not plot course to @x1 due to lack of coordinate information.",targetObj.name()));
						return false;
					}
					targetCoords = targetObj.coordinates();
				}
			}
			if(targetCoords == null)
				targetCoords = findCoordinates(targetStr);
			if(targetCoords == null)
			{
				addScreenMessage(L("Error: Unable to find course target '@x1'.",targetStr));
				return false;
			}
			else
			{
				// yes, it's cheating.  deal
				final List<SpaceObject> objs = CMLib.space().getSpaceObjectsByCenterpointWithin(targetCoords, 0, 10);
				for(final SpaceObject o1 : objs)
				{
					if(Arrays.equals(targetCoords, o1.coordinates()))
						courseTargetRadius = o1.radius();
				}
			}
			course.clear();
			courseTargetCoords = targetCoords;
			addScreenMessage(L("Plotting course to @x1.",CMParms.toListString(courseTargetCoords)));
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
			for(final TechComponent sensor : sensors)
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
			final double[] facing=ship.facing();
			final double[] dirTo = CMLib.space().getDirection(spaceObject, targetObj);
			double fdist1=(facing[0]>dirTo[0])?facing[0]-dirTo[0]:dirTo[0]-facing[0];
			final double fdist2=(facing[1]>dirTo[1])?facing[1]-dirTo[1]:dirTo[1]-facing[1];
			if(fdist1>Math.PI)
				fdist1=(Math.PI*2)-fdist1;
			final double deltaTo=fdist1+fdist2;
			//final double deltaTo = CMLib.space().getAngleDelta(ship.facing(), dirTo);
			if(deltaTo < 0.02)
				addScreenMessage(L("Already facing @x1.",targetObj.name()));
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
					addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
					return false;
				}
				double[] oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
				String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
				CMMsg msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(sendMessage(mob, engineE, msg, unparsed))
				{
					if(oldFacing[0]==ship.facing()[0])
					{
						addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
						return false;
					}
					else
					if(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)<.05)
					{}
					else
					{
						addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
								portDir.name(),""+Math.round(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)*100.0)));
						return false;
					}
					if(facing[1]>dirTo[1])
						portDir=ShipDirectional.ShipDir.VENTRAL;
					else
						portDir=ShipDirectional.ShipDir.DORSEL;
					code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
					oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
					msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, unparsed))
					{
						if(oldFacing[1]==ship.facing()[1])
						{
							addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return false;
						}
						else
						if(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)<.05)
							addScreenMessage(L("Now facing @x1.",targetObj.name()));
						else
						{
							addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)*100.0)));
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
			for(final TechComponent sensor : sensors)
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
			ShipEngine engineE=null;
			final double[] dirTo = CMLib.space().getDirection(ship, targetObj);
			if(!changeFacing(ship, dirTo))
			{
				addScreenMessage(L("Warning. Approach program cancelled due to engine failure."));
				cancelNavigation();
				return false;
			}
			engineE=primeMainThrusters(ship, SpaceObject.ACCELERATION_DAMAGED);
			if(engineE==null)
			{
				cancelNavigation();
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			findTargetAcceleration(engineE);
			final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
			final List<SpaceObject> navs = calculateNavigation(ship, targetObj);
			navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, approachTarget, programEngines, navs);
			addScreenMessage(L("Approach to @x1 procedure engaged.",targetObj.name()));
			return false;
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
			for(final TechComponent sensor : sensors)
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
				addScreenMessage(L("Already mooning @x1.",targetObj.name()));
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
					addScreenMessage(L("Error: Malfunctioning finding maneuvering engine."));
					return false;
				}
				double[] oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
				String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist1)));
				CMMsg msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(sendMessage(mob, engineE, msg, unparsed))
				{
					if(oldFacing[0]==ship.facing()[0])
					{
						addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
						return false;
					}
					else
					if(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)<.05)
					{}
					else
					{
						addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
								portDir.name(),""+Math.round(CMath.pctDiff(dirTo[0],ship.facing()[0],Math.PI*2.0)*100.0)));
						return false;
					}
					if(facing[1]>dirTo[1])
						portDir=ShipDirectional.ShipDir.VENTRAL;
					else
						portDir=ShipDirectional.ShipDir.DORSEL;
					code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(Math.toDegrees(fdist2)));
					oldFacing=Arrays.copyOf(ship.facing(),ship.facing().length);
					msg=CMClass.getMsg(mob, engineE, sw, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
					if(sendMessage(mob, engineE, msg, unparsed))
					{
						if(oldFacing[1]==ship.facing()[1])
						{
							addScreenMessage(L("Error: Malfunctioning firing @x1 engines.",portDir.name()));
							return false;
						}
						else
						if(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)<.05)
							addScreenMessage(L("Now mooning @x1.",targetObj.name()));
						else
						{
							addScreenMessage(L("Error: Fired @x1 engines, but only got to within @x2 percent",
									portDir.name(),""+Math.round(CMath.pctDiff(dirTo[1],ship.facing()[1],Math.PI)*100.0)));
							return false;
						}
					}
				}
			}
			return false;
		}
	};

}
