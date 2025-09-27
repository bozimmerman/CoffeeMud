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
   Copyright 2022-2025 Bo Zimmerman

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

	/**
	 * A map of the last injection level for each engine.  Used only to 'remember' last programmed injections
	 */
	protected Map<ShipEngine, Double> injectMap			= new Hashtable<ShipEngine, Double>();

	protected volatile ShipNavTrack	navTrack			= null;
	protected final List<SpaceObject>course				= new LinkedList<SpaceObject>();
	protected volatile boolean		courseSet			= false;
	protected volatile SpaceObject	courseTarget		= null;


	protected final static double MAX_DIR_DIFF = 0.08;

	protected static class ShipNavTrack
	{
		protected long						speedLimit 			= Long.MAX_VALUE;
		protected ShipNavProcess			proc;
		protected ShipNavState				state;
		protected Object[]					args;
		protected Class<?>[]				types;
		protected Map<Class<?>, Integer>	classMap;
		protected ShipNavTrack				nextTrack;

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

		protected void setNextTrack(final ShipNavTrack nextTrack)
		{
			this.nextTrack = nextTrack;
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
		PRE_STOP,
		LANDING_APPROACH,
		LANDING,
		ORBITSEARCH,
		ORBITCRUISE,
		APPROACH,
		DEPROACH
		;
	}

	protected static enum ShipNavProcess
	{
		STOP(ShipNavState.STOP, List.class),
		LAUNCH(ShipNavState.LAUNCHING, SpaceObject.class, List.class),
		LAND(ShipNavState.PRE_STOP, SpaceObject.class, List.class, LocationRoom.class),
		ORBIT(ShipNavState.ORBITSEARCH, SpaceObject.class, List.class, Map.class),
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

		public String description()
		{
			switch(this)
			{
			case APPROACH:
				return CMLib.lang().L("approach");
			case LAND:
				return CMLib.lang().L("land");
			case LAUNCH:
				return CMLib.lang().L("launch");
			case ORBIT:
				return CMLib.lang().L("orbit");
			case STOP:
				return CMLib.lang().L("stop");
			default:
				return CMLib.lang().L("unknown");

			}
		}
	}

	/**
	 * Returns whether this ship has sufficient inertial dampeners, and if so, whether
	 * they are active and in good repair.
	 * @return true if dampeners are active and in good repair
	 */
	private boolean hasDampeners()
	{
		for (final TechComponent T : getDampeners())
		{
			if (T.activated()
			&& ((!T.subjectToWearAndTear()) || (T.usesRemaining() > 30))) //TODO: less than 30 is arbitrary
				return true;
		}
		return false;
	}

	protected class EngProfile
	{
		public final ShipEngine engine;
		public boolean canAccelerate;
		private final double[] accelChart;
		// stickyThrust is whether this engine is a reaction engine, and thus will maintain thrust until told otherwise
		public final boolean stickyThrust;
		// canTurn is whether this engine has all 4 directional thrusters
		public final boolean canTurn;
		public EngProfile(final ShipEngine engine, final double[] accelChart)
		{
			this.engine = engine;
			// indexed from 0 to 10 (11 entries)
			// 0 value is actually just 'close to zero';
			this.accelChart = accelChart;
			this.canAccelerate = CMParms.contains(engine.getAvailPorts(), ShipDir.AFT);
			this.stickyThrust = engine.isReactionEngine();
			this.canTurn = (CMParms.contains(engine.getAvailPorts(), ShipDir.DORSEL)
					&& CMParms.contains(engine.getAvailPorts(), ShipDir.STARBOARD)
					&& CMParms.contains(engine.getAvailPorts(), ShipDir.VENTRAL)
					&& CMParms.contains(engine.getAvailPorts(), ShipDir.PORT));
		}
		/**
		 * Returns whether this engine is capable of speed-agnostic turning
		 * @return true if it will probably turn faster than typical space rockets
		 */
		public boolean fasterTurns()
		{
			if(!canTurn)
				return false;
			for(final TechComponent T : getInertials())
			{
				if(T.activated()
				&&((!T.subjectToWearAndTear()))||(T.usesRemaining()>30))
					return true;
			}
			return true;
		}

		/**
		 * Calculates the expected acceleration at a given thrust injection level.
		 * A return value of 0 means either no aft thrust is possible, or none
		 * at that injection level.
		 * @param inject 0 to 1.0, 0 with 0 being minimal thrust, 1.0 being max
		 * @return the expected SAFE acceleration at that thrust injection level
		 */
		public double accelleration(double inject)
		{
			if(inject > 1.0)
				inject = 1.0;
			if (inject < 0.0)
				inject = 0.0;
			final int index = (int)Math.round(inject * 10.0);
			if((accelChart[index] > SpaceObject.ACCELERATION_TYPICALSPACEROCKET)
			&& !hasDampeners())
				return SpaceObject.ACCELERATION_TYPICALSPACEROCKET;
			return accelChart[index];
		}

		/**
		 * Calculates the injection level needed to achieve the desired acceleration.
		 *
		 * @param desiredAccel the desired acceleration
		 * @return desired injection level, close as possible
		 */
		public double injection(double desiredAccel)
		{
			if((desiredAccel > SpaceObject.ACCELERATION_TYPICALSPACEROCKET)
			&& !hasDampeners())
				desiredAccel = SpaceObject.ACCELERATION_TYPICALSPACEROCKET;
			if (desiredAccel < accelChart[10])
			{
				if (desiredAccel < accelChart[0])
					return 0.001 * (desiredAccel / accelChart[0]);
				for (int i = 0; i < 10; i++)
				{
					if ((accelChart[i] <= desiredAccel)
					&& (desiredAccel < accelChart[i+1]))
						return (i/10.0)+(0.1 * (desiredAccel-accelChart[i])/(accelChart[i+1]-accelChart[i]));
				}
			}
			return 1.0;
		}
	}


	@Override
	protected void decache()
	{
		super.decache();
		cancelNavigation(false);
	}

	protected boolean cancelNavigation(final boolean isComplete)
	{
		final boolean didSomething = navTrack != null || courseSet;
		navTrack=null;
		course.clear();
		courseTarget = null;
		courseSet = false;
		return didSomething;
	}

	protected void programThrust(final ShipEngine engineE, final Double thrustInject)
	{
		this.setThrust(engineE, thrustInject, true);
	}
	protected void setThrust(final ShipEngine engineE, final Double thrustInject, final boolean program)
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
					final int type = CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG|(program?CMMsg.MASK_INTERMSG:0);
					final CMMsg msg=CMClass.getMsg(mob, engineE, this, CMMsg.NO_EFFECT, null, type, null, CMMsg.NO_EFFECT,null);
					injectMap.put(engineE, thrustInject);
					final String code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT,Double.valueOf(thrustInject.doubleValue()));
					msg.setTargetMessage(code);
					this.trySendMsgToItem(mob, engineE, msg);
					if (thrustInject.doubleValue() > 0.0)
						this.lastInject=thrustInject;
					injectMap.put(engineE, thrustInject);
				}
			}
		}
		finally
		{
			mob.destroy();
		}
	}

	protected void performSingleThrust(final ShipEngine engineE, final Double thrustInject)
	{
		final Double oldInject=this.lastInject;
		final Double oldAccel=this.savedAcceleration;
		final Double oldDelta=this.savedSpeedDelta;
		setThrust(engineE, thrustInject, false);
		this.lastInject=oldInject;
		this.savedAcceleration=oldAccel;
		this.savedSpeedDelta=oldDelta;
	}

	protected double findTargetAcceleration(final ShipEngine E)
	{
		if(!hasDampeners())
		{
			addScreenMessage(L("No inertial dampeners found."));
			return SpaceObject.ACCELERATION_TYPICALSPACEROCKET;
		}
		return SpaceObject.ACCELERATION_DAMAGED*10;
	}

	protected boolean flipForAllStop(final SpaceShip ship, final EngProfile turnProfile)
	{
		if(turnProfile == null)
			return false;
		final Dir3D stopFacing = CMLib.space().getOppositeDir(ship.direction());
		return changeFacing(ship, turnProfile, stopFacing);
	}

	protected boolean changeFacing(final SpaceShip ship, final EngProfile turnProfile, final Dir3D newFacing)
	{
		if(ship.getIsDocked() != null)
		{
			ship.setFacing(newFacing);
			return true;
		}
		if(turnProfile == null)
			return false;
		CMMsg msg;
		final MOB M=CMClass.getFactoryMOB();
		M.setName(ship.Name());
		final boolean isDebugging = CMSecurity.isDebugging(DbgFlag.SPACESHIP);
		final boolean isDebuggingTurns = false; // isDebugging
		CMLib.space().getOppositeDir(ship.facing()); // I think this is to normalize the facing dir
		try
		{
			final Dir3D currentFacing = ship.facing().copyOf();
			double angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			int tries=100;
			while((angleDiff > 0.0001)&&(--tries>0))
			{
				// step one, face opposite direction of motion
				final ShipEngine engineE = turnProfile.engine;
				{
					if((CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.STARBOARD))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.PORT))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.DORSEL))
					&&(CMParms.contains(engineE.getAvailPorts(),ShipDirectional.ShipDir.VENTRAL)))
					{
						// no intermsg, so this is a real thrust message
						msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, null, CMMsg.NO_EFFECT,null);
						this.savedAngle = null;
						final String code=TechCommand.THRUST.makeCommand(ShipDir.PORT,Double.valueOf(1.0));
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
									Log.debugOut(ship.Name(),"Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+
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
								Log.debugOut(ship.Name(),"Turn Deltas now: "+(Math.round(angleDelta[0]*100)/100.0)+" + "+(Math.round(angleDelta[1]*100)/100.0)
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
									Log.debugOut(ship.Name(),"Thrusting "+thrust+"*"+angleAchievedPerPt+" to "+dir+" to achieve delta, and go from "
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
								Log.debugOut(ship.Name(),"Turn Deltas now: "+(Math.round(angleDelta.xyd()*100)/100.0)+" + "+(Math.round(angleDelta.zd()*100)/100.0)
										+"=="+(Math.round(Math.abs((angleDelta.xyd())+Math.abs(angleDelta.zd()))*100)/100.0));
							}
						}
						if((Math.abs(angleDelta.xyd())+Math.abs(angleDelta.zd()))<.01)
							break;
					}
				}
				angleDiff = CMLib.space().getAngleDelta(ship.facing(), newFacing);
			}
			if(isDebugging && !Arrays.equals(currentFacing.toDoubles(), ship.facing().toDoubles()))
			{
				Log.debugOut(ship.Name(),"Facing Change from "+
							CMLib.english().directionDescShort(currentFacing.toDoubles())
							+" to "+CMLib.english().directionDescShort(ship.facing().toDoubles()));
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

	protected List<EngProfile> profileThrusters(final SpaceShip ship, final ShipEngine overrideE)
	{
		final List<ShipEngine> engines;
		if(overrideE != null)
			engines = new XVector<ShipEngine>(overrideE);
		else
			engines = getEngines();
		final MOB M=CMClass.getFactoryMOB();
		final List<EngProfile> profiles = new Vector<EngProfile>();
		try
		{
			final double[] accelValues = new double[11];
			for(final ShipEngine engineE : engines)
			{
				if(CMParms.contains(engineE.getAvailPorts(),ShipDir.AFT))
				{
					String code;
					final CMMsg restoreMsg;
					if((engineE.activated()) && (injectMap.containsKey(engineE)))
					{
						restoreMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG|CMMsg.MASK_INTERMSG, null, CMMsg.NO_EFFECT,null);
						code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT, injectMap.get(engineE));
						restoreMsg.setTargetMessage(code);
					}
					else
						restoreMsg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
					for(int i=0;i<=10;i++)
					{
						final double inject = (i==0?0.001:(i/10.0));
						final CMMsg msg=CMClass.getMsg(M, engineE, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG|CMMsg.MASK_INTERMSG, null, CMMsg.NO_EFFECT,null);
						code=TechCommand.THRUST.makeCommand(ShipDirectional.ShipDir.AFT, Double.valueOf(inject));
						msg.setTargetMessage(code);
						ship.tick(M, Tickable.TICKID_PROPERTY_SPECIAL); // clear the speed ticker
						this.savedAcceleration = null;
						this.trySendMsgToItem(M, engineE, msg);
						if(this.savedAcceleration != null)
							accelValues[i] = this.savedAcceleration.doubleValue();
					}
					ship.tick(M, Tickable.TICKID_PROPERTY_SPECIAL); // clear the speed ticker
					this.trySendMsgToItem(M, engineE, restoreMsg);
				}
				final EngProfile profile = new EngProfile(engineE, accelValues);
				profiles.add(profile);
			}
		}
		finally
		{
			M.destroy();
		}
		return profiles;
	}

	@Override
	protected void onPowerCurrent(final int value)
	{
		super.onPowerCurrent(value);
		if((courseTarget != null) && (!courseSet))
		{
			final SpaceObject me = CMLib.space().getSpaceObject(this,true);
			if(me == null)
				cancelNavigation(false);
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
					cancelNavigation(false);
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
			navTrack			= null;
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
		final BoundedSphere fromSphere=fromObj.getSphere();
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
				BoundedSphere enemyBounds = new BoundedSphere(enemySphere.center(),
						Math.round(CMath.mul(enemySphere.radius, SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				if(enemyBounds.intersects(fromSphere))
				{
					if(enemySphere.intersects(fromSphere))
						enemyBounds=enemySphere;
					else
					{
						final long dist = CMLib.space().getDistanceFrom(fromObj.coordinates(), O.coordinates());
						enemyBounds=new BoundedSphere(O.coordinates(),dist-fromObj.radius()-1);
					}
				}
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
		winnerObj.setName(L("Nav Point"));
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
		if (newObj == null && CMSecurity.isDebugging(DbgFlag.SPACESHIP))
		{
			Log.debugOut(ship.Name(), "No valid points from " + fromObj.name()
						+ " to " + toObj.name() + ", tested " + points.length + " points");
		}
		return newObj;
	}

	protected LinkedList<SpaceObject> calculateNavigation(final SpaceObject ship,
														  final SpaceObject targetObj,
														  final List<SpaceObject> sensorObjs)
	{
		final GalacticMap spaceLibrary = CMLib.space();
		final List<SpaceObject> navs = new ArrayList<SpaceObject>();
		final SpaceObject[] others = new SpaceObject[] { ship, targetObj };
		navs.add(targetObj);
		int navSize = 0;
		while((navSize != navs.size()) && (navs.size()<50))
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
					final Dir3D angleFromOrigin = spaceLibrary.getDirection(fromObj.coordinates(), collO.coordinates());
					final Dir3D angleFromCollider = spaceLibrary.getDirection(collO.coordinates(), fromObj.coordinates());
					final long gravRadius = Math.round(CMath.mul(SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS, collO.radius()));
					final long distAdd = gravRadius - collO.radius();
					final long distanceCollRadius = collO.radius() + (distAdd * 2) + 2;
					final Coord3D[] pointsFromOrigin = spaceLibrary.getPerpendicularPoints(collO.coordinates(), angleFromCollider, distanceCollRadius);
					final Coord3D[] pointsFromCollider = spaceLibrary.getPerpendicularPoints(fromObj.coordinates(), angleFromOrigin, distanceCollRadius);
					Coord3D[] points = CMParms.combine(pointsFromCollider, pointsFromOrigin);
					SpaceObject newObj = subCourseCheck(ship,fromObj,toObj,points,others);
					if(newObj == null)
					{
						final Dir3D revAngleFromOrigin = spaceLibrary.getDirection(fromObj.coordinates(), collO.coordinates());
						points = spaceLibrary.getPerpendicularPoints(fromObj.coordinates(), revAngleFromOrigin, distanceCollRadius);
						newObj = subCourseCheck(ship,fromObj,toObj,points,others);
						if ((newObj == null)
						&& (fromObj == ship)
						&& (spaceLibrary.getDistanceFrom(ship, collO) < collO.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
						{
							newObj = (SpaceObject) CMClass.getBasicItem("Moonlet");
							newObj.setName(L("Safe Point"));
							newObj.setRadius(ship.radius());
							newObj.setCoords(
									spaceLibrary.getLocation(ship.coordinates(), CMLib.space().getOppositeDir(angleFromCollider),
										Math.round(collO.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)
										+ (ship.radius()*2)));
						}
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
		for(int i=1; i<navs.size()-1; i++)
		{
			final SpaceObject prev = navs.get(i-1);
			final SpaceObject curr = navs.get(i);
			final SpaceObject next = navs.get(i+1);
			if(spaceLibrary.isCollinear(prev, curr, next, 0.01))
				navs.remove(i--);
		}
		return new XLinkedList<SpaceObject>(navs);
	}

	protected void stopAllThrust(final List<EngProfile> programEngines, final boolean complete)
	{
		if(complete)
		{
			if((navTrack!=null)&&(navTrack.nextTrack!=null))
				return;
		}
		for(final EngProfile engineE : programEngines)
			setThrust(engineE.engine,Double.valueOf(0.0), false);
	}

	protected boolean checkNavComplete(final ShipNavTrack track, final SpaceShip ship, final SpaceObject targetObject)
	{
		// check pre-reqs and completions of the overall process first
		final ShipNavProcess proc = track.proc;
		@SuppressWarnings("unchecked")
		final List<EngProfile> programEngines=track.getArg(List.class);
		switch(proc)
		{
		case APPROACH:
		{
			if(targetObject==null)
			{
				final String reason = L("no target information");
				cancelNavigation(false);
				super.addScreenMessage(L("Approach program aborted with error (@x1).",reason));
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
						this.stopAllThrust(programEngines, true);
						this.cancelNavigation(true);
						return false;
					}
					else
					if((track.state != ShipNavState.STOP)&&(track.nextTrack==null))
					{
						super.addScreenMessage(L("Approach completed, stop initiated."));
						track.state = ShipNavState.STOP;
					}
					else
					{
						super.addScreenMessage(L("Approach completed."));
						this.stopAllThrust(programEngines, true);
						this.cancelNavigation(true);
						return false;
					}
				}
				else
				{
					this.stopAllThrust(programEngines, true);
					this.cancelNavigation(true);
					super.addScreenMessage(L("Approach program completed."));
					return false;
				}
			}
			@SuppressWarnings("unchecked")
			final LinkedList<SpaceObject> navList = track.getArg(LinkedList.class);
			if(navList.isEmpty())
			{
				final String reason = L("no nav target information");
				cancelNavigation(false);
				super.addScreenMessage(L("Approach program aborted with error (@x1).",reason));
				return false;
			}
			break;
		}
		case LAND:
			if(ship.getIsDocked()!=null)
			{
				cancelNavigation(true);
				super.addScreenMessage(L("Landing program completed successfully."));
				return false;
			}
			if(targetObject==null)
			{
				final String reason = L("no planetary information");
				cancelNavigation(false);
				super.addScreenMessage(L("Landing program aborted with error (@x1).",reason));
				return false;
			}
			break;
		case LAUNCH:
			if(targetObject==null)
			{
				final String reason = L("no planetary information");
				this.cancelNavigation(false);
				super.addScreenMessage(L("Launch program aborted with error (@x1).",reason));
				return false;
			}
			{
				final long distance=CMLib.space().getDistanceFrom(ship, targetObject);
				if(distance > (targetObject.radius()*SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS))
				{
					this.lastInject = null;
					super.addScreenMessage(L("Launch program completed. Shutting down thrust."));
					this.stopAllThrust(programEngines, true);
					this.cancelNavigation(true);
					return false;
				}
			}
			break;
		case ORBIT:
			if(targetObject==null)
			{
				final String reason = L("no planetary information");
				this.cancelNavigation(false);
				super.addScreenMessage(L("Orbit program aborted with error (@x1).",reason));
				return false;
			}
			// orbit is forever now
			break;
		case STOP:
			if(ship.speed()  <= 0.01)
			{
				ship.setSpeed(0.0); // that's good enough, for now.
				this.stopAllThrust(programEngines, true);
				this.cancelNavigation(true);
				super.addScreenMessage(L("Stop program completed successfully."));
				return false;
			}
			track.state = ShipNavState.STOP; // no need to have any other state
			break;
		}
		return true;
	}

	protected Dir3D graviticCourseAdjustments(final SpaceShip ship, final Dir3D dir, final double thrustAccel)
	{
		final Pair<Dir3D, Double> gravitor = CMLib.space().getGravityForcer(ship);
		if((gravitor != null)&&(gravitor.second.doubleValue()>0))
		{
			final Dir3D gravDir = gravitor.first;
			final double gravAccel = gravitor.second.doubleValue();
			return CMLib.space().getGraviticCourseCorrection(dir, thrustAccel, gravDir, gravAccel);
		}
		return dir;
	}

	protected EngProfile getTurnProfile(final List<EngProfile> profiles)
	{
		EngProfile best = null;
		for (final EngProfile prof : profiles)
		{
			if (prof.canTurn)
			{
				if((best == null)
				||(prof.fasterTurns() && (!best.fasterTurns()))
				||(prof.fasterTurns() && best.engine.usesRemaining() < prof.engine.usesRemaining()))
					best = prof;
			}
		}
		return best;
	}

	protected double getMaxAcceleration(final SpaceShip ship, final List<EngProfile> engines)
	{
		double bestAccel = Double.MAX_VALUE;
		for (final EngProfile prof : engines)
		{
			if(prof.canAccelerate)
			{
				if(bestAccel == Double.MAX_VALUE)
					bestAccel = prof.accelleration(1.0);
				else
				{
					final double bestAccelChk = prof.accelleration(1.0);
					if (bestAccel < bestAccelChk)
						bestAccel = bestAccelChk;
				}
			}
		}
		return bestAccel;
	}

	protected EngProfile getAccelProfile(final SpaceShip ship, final List<EngProfile> engines, final double targetAcceleration)
	{
		final double turnDiff = Math.abs(CMLib.space().getAngleDelta(ship.direction(), ship.facing()));
		final double offTurnDiff = Math.abs(CMLib.space().getAngleDelta(ship.direction(), CMLib.space().getOppositeDir(ship.facing())));
		final List<EngProfile> profiles = new XArrayList<EngProfile>(engines);
		for (final Iterator<EngProfile> i = profiles.iterator(); i.hasNext();)
		{
			final EngProfile prof = i.next();
			if (!prof.canAccelerate)
				i.remove();
			final double minAccel = prof.accelleration(prof.injection(0));
			if ((targetAcceleration < minAccel)
			&& (Math.abs(targetAcceleration - minAccel) > Math.max(0.5, minAccel * 0.2)))
				i.remove();
		}
		// we check angles forward and back aligned because sometimes we are slowing down
		if((turnDiff < 1) && (offTurnDiff < 1))
		{
			// pick profile optimized for speed
			EngProfile best = null;
			double bestDiff = Double.MAX_VALUE;
			for (final EngProfile prof : profiles)
			{
				final double testAccel = prof.accelleration(prof.injection(targetAcceleration));
				final double diff = Math.abs(targetAcceleration - testAccel);
				if(diff < bestDiff)
				{
					best = prof;
					bestDiff = diff;
				}
			}
			if(best != null)
				return best;
		}
		// pick profile optimized for control
		EngProfile best = null;
		double bestDiff = Double.MAX_VALUE;
		for (final EngProfile prof : profiles)
		{
			if(best == null)
				best = prof;
			else
			if(prof.fasterTurns() && !best.fasterTurns())
				best = prof;
			else
			{
				final double testAccel = prof.accelleration(prof.injection(targetAcceleration));
				final double diff = Math.abs(targetAcceleration - testAccel);
				if(diff < bestDiff)
				{
					best = prof;
					bestDiff = diff;
				}
			}
		}
		return best;
	}

	private boolean programAccelerationThrust(final SpaceShip ship, final List<EngProfile> programEngines, final double acceleration)
	{
		final EngProfile accelEngine = getAccelProfile(ship, programEngines, acceleration);
		if (accelEngine == null)
		{
			cancelNavigation(false);
			if (this.navTrack != null)
				super.addScreenMessage(L("@x1 program aborted with error (acceleration control failure).", CMStrings.capitalizeAndLower(this.navTrack.proc.name())));
			else
				super.addScreenMessage(L("Program aborted with error (acceleration control failure)."));
			return false;
		}
		final Double newInject = Double.valueOf(accelEngine.injection(acceleration));
		programThrust(accelEngine.engine, newInject);
		return true;
	}

	protected void doNavigation(final ShipNavTrack track)
	{
		final GalacticMap spaceLibrary = CMLib.space();
		final SpaceObject spaceObj=CMLib.space().getSpaceObject(this,true);
		final SpaceShip ship = (spaceObj instanceof SpaceShip) ? (SpaceShip)spaceObj : null;
		@SuppressWarnings("unchecked")
		final List<EngProfile> programEngines=track.getArg(List.class);
		SpaceObject targetObject;
		try
		{
			targetObject=track.getArg(SpaceObject.class);
		}
		catch(final NullPointerException npe)
		{
			targetObject=null;
		}

		final EngProfile turnEngine = getTurnProfile(programEngines);
		if((ship==null)||(turnEngine == null))
			return;
		if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
		{
			Log.debugOut(ship.name(),"** Program "+track.proc.name()
						+" state: "+track.state.toString()
						+", speed: "+	CMLib.english().distanceDescShort(Math.round(ship.speed()))
						+", dir: "+		CMLib.english().directionDescShort(ship.direction().toDoubles())
						+", coord:"+	CMParms.toListString(ship.coordinates().toLongs())
						);
		}

		if(!this.checkNavComplete(track, ship, targetObject))
		{
			if(track.nextTrack != null)
			{
				this.navTrack = track.nextTrack;
				super.addScreenMessage(L("@x1 program completed, transitioning to @x2.",
						track.proc.name(), navTrack.proc.name()));
			}
			return;
		}

		double targetAcceleration = this.getMaxAcceleration(ship, programEngines);
		if(targetAcceleration < 0)
		{
			final String reason = L("no available engines");
			cancelNavigation(false);
			super.addScreenMessage(L("@x1 program aborted with error (@x2).", track.proc.name(), reason));
			return;
		}
		switch(track.state)
		{
		// the landing steps check if you are in position to go into land phase, and if you
		// are in the proper landing phase, that you are trying to stop
		case LANDING:
		case LANDING_APPROACH:
		case PRE_STOP:
		{
			if(track.proc==ShipNavProcess.LAND)
			{
				if((track.state!=ShipNavState.LANDING)
				&&(targetObject != null))
				{
					final long distance=spaceLibrary.getDistanceFrom(ship.coordinates(),targetObject.coordinates());
					if((distance < (ship.radius() + Math.round(targetObject.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					&&(ship.speed()<SpaceObject.VELOCITY_SOUND))
						track.state=ShipNavState.LANDING;
				}
			}
			if(track.state!=ShipNavState.PRE_STOP)
				break;
		}
		// the stop part makes sure you are facing correctly, and might goose injection
		//$FALL-THROUGH$
		case STOP:
		{
			if(ship.speed() > 0.5)
			{
				final Dir3D stopFacing = spaceLibrary.getOppositeDir(ship.direction());
				//stopFacing = this.graviticCourseAdjustments(ship, stopFacing);
				final double angleDelta = spaceLibrary.getAngleDelta(ship.facing(), stopFacing); // starboard is -, port is +
				if(angleDelta>.02)
				{
					if(!changeFacing(ship, turnEngine, stopFacing))
					{
						cancelNavigation(false);
						super.addScreenMessage(L("Stop program aborted with error (directional control failure)."));
						return;
					}
				}
				if(ship.speed() < targetAcceleration)
					targetAcceleration = ship.speed();
			}
			else
			{
				ship.setSpeed(0.0);
				targetAcceleration = 0.0;
				switch(track.proc)
				{
				case LAND:
					track.state=ShipNavState.LANDING_APPROACH;
					break;
				case APPROACH:
					track.state=ShipNavState.APPROACH;
					break;
				case LAUNCH:
					track.state=ShipNavState.LAUNCHING;
					break;
				case ORBIT:
					track.state=ShipNavState.ORBITSEARCH;
					break;
				case STOP:
					break;
				default:
					break;
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
			if (!navList.isEmpty())
			{
				SpaceObject intTarget = navList.getFirst();
				long distToITarget = (spaceLibrary.getDistanceFrom(ship, intTarget) -
						ship.radius() - Math.round(CMath.mul(intTarget.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
				Dir3D dirToITarget = spaceLibrary.getDirection(ship.coordinates(), intTarget.coordinates());
				if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SPACESHIP))
				{
					Log.debugOut(ship.name(), "Approach Target: " + intTarget.Name() + ", Dist: "
							+ CMLib.english().distanceDescShort(distToITarget) + ", Dir: "
							+ CMLib.english().directionDescShort(dirToITarget.toDoubles()));
				}
				double directionDiff = spaceLibrary.getAngleDelta(ship.direction(), dirToITarget);
				final Pair<Dir3D, Double> grav = spaceLibrary.getGravityForcer(ship);
				final double gravAccel = (grav != null) ? grav.second.doubleValue() : 0.0;
				double radialAngle = 0.0;
				if (grav != null)
					radialAngle = spaceLibrary.getAngleDelta(ship.direction(), grav.first);
				double tangentialGrav = gravAccel * Math.abs(Math.sin(radialAngle));
				final double ticksToStop = ship.speed() / targetAcceleration;
				double gravityDrag = (tangentialGrav * ticksToStop * ticksToStop) / 2.0;
				double effectiveDist = distToITarget + gravityDrag;
				int safeDistance = 100 + (int) Math.round(ship.speed());
				if (directionDiff < MAX_DIR_DIFF)
					safeDistance += (int) Math.round(ship.speed());
				if (distToITarget < safeDistance)
				{
					boolean popped = false;
					while ((navList.size() > 1) && (distToITarget < safeDistance))
					{
						navList.removeFirst();
						intTarget = navList.getFirst();
						distToITarget = (spaceLibrary.getDistanceFrom(ship, intTarget) - ship.radius()
										- Math.round(CMath.mul(intTarget.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)));
						dirToITarget = spaceLibrary.getDirection(ship.coordinates(), intTarget.coordinates());
						directionDiff = spaceLibrary.getAngleDelta(ship.direction(), dirToITarget);
						// Recompute effective for new target
						if(grav != null)
							radialAngle = spaceLibrary.getAngleDelta(ship.direction(), grav.first);
						tangentialGrav = gravAccel * Math.abs(Math.sin(radialAngle));
						gravityDrag = (tangentialGrav * ticksToStop * ticksToStop) / 2.0;
						effectiveDist = distToITarget + gravityDrag;
						safeDistance = 100 + (int) Math.round(ship.speed());
						if (directionDiff < MAX_DIR_DIFF)
							safeDistance += (int) Math.round(ship.speed());
						popped = true;
					}
					if (popped)
					{
						targetAcceleration = getMaxAcceleration(ship, programEngines);
						track.state = ShipNavState.APPROACH;
					}
				}

				// Check if we should be speeding up, or slowing down, and
				// ideal facing direction

				// first see if we are actually underway...
				if ((ship.speed() > targetAcceleration) && (targetAcceleration > 0.0))
				{
					final double stopDistance = (ship.speed() / 2.0) * (ticksToStop + 1);
					// now see if we need to adjust decelleration during deproach
					Dir3D correctFacing;
					// final Dir3D correctDirection = dirToITarget;
					if ((ticksToStop > 0) && (track.state == ShipNavState.DEPROACH))
					{
						correctFacing = spaceLibrary.getOppositeDir(ship.direction());
						// if(spaceLibrary.getAngleDelta(ship.direction(),
						final double overUnderDistance = stopDistance - distToITarget;
						if (overUnderDistance > targetAcceleration * 2)
							targetAcceleration += CMath.div(overUnderDistance, ticksToStop);
						else
						if (overUnderDistance < -(targetAcceleration * 2))
							targetAcceleration += CMath.div(overUnderDistance, ticksToStop);
						if (ship.speed() >= distToITarget)
							targetAcceleration = ship.speed();
						if (targetAcceleration < 0.0)
							targetAcceleration = Math.abs(targetAcceleration);
					}
					else // APPROACH -- so see if it is time to decelerate
					{
						correctFacing = dirToITarget;
						if ((stopDistance >= effectiveDist)
						&& (stopDistance >= (distToITarget - gravityDrag))
						&& (targetObject != null)
						&& (ship.speed() > (targetObject.speed() * 2)))
						{
							track.state = ShipNavState.DEPROACH;
							// ensure we are mooning our direction
							correctFacing = spaceLibrary.getOppositeDir(ship.direction());
						}
					}
					// correctFacing is now at the Ideal point.
					correctFacing = this.graviticCourseAdjustments(ship, correctFacing, targetAcceleration);
					if ((grav != null) && (grav.second.doubleValue() > 0.5))
					{
						final double radialDrift = spaceLibrary.getAngleDelta(correctFacing, grav.first);
						if (radialDrift < 0.1)
						{
							final Dir3D antiGravDir = spaceLibrary.getOppositeDir(grav.first);
							correctFacing = spaceLibrary.getMiddleAngle(correctFacing, antiGravDir);
						}
					}
					if (CMSecurity.isDebugging(CMSecurity.DbgFlag.SPACESHIP))
					{
						final double facingDiff = spaceLibrary.getAngleDelta(ship.facing(), correctFacing);
						Log.debugOut(ship.name(), "Face diff: " + Math.round(Math.toDegrees(facingDiff)) + " degrees"
								+ ", dist: " + CMLib.english().distanceDescShort(distToITarget) + ", 2dir: "
								+ CMLib.english().directionDescShort(correctFacing.toDoubles()));
					}
					if (spaceLibrary.getAngleDelta(ship.facing(), correctFacing) > 0)
						changeFacing(ship, turnEngine, correctFacing);
				}
				else // since we aren't moving yet, Begin standard approach.
				{
					track.state = ShipNavState.APPROACH;
					changeFacing(ship, turnEngine, dirToITarget);
				}
			}
			break;
		}
		case LAUNCHING:
		case ORBITSEARCH:
		case ORBITCRUISE:
			break;
		}

		// the state of the meat.
		boolean doInject=true;
		switch(track.state)
		{
		case APPROACH:
			if((track.speedLimit < Long.MAX_VALUE)
			&&(ship.speed() >= track.speedLimit))
			{
				if(Math.toDegrees(spaceLibrary.getAngleDelta(ship.facing(), ship.direction()))<90)
					doInject=false;
			}
			//$FALL-THROUGH$
		case STOP:
		case DEPROACH:
		case PRE_STOP:
		{
			if(doInject)
			{
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
			}
			break;
		}
		case LAUNCHING:
		{
			if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
				return;
			break;
		}
		case ORBITSEARCH:
		{
			if (track.getArg(Map.class).containsKey("ORBIT_DISTANCE"))
				track.getArg(Map.class).clear();
			if (targetObject == null)
			{
				cancelNavigation(false);
				super.addScreenMessage(L("Orbit program aborted: no target planet."));
				return;
			}
			final long distanceFromPlanet = spaceLibrary.getDistanceFrom(ship, targetObject);
			final double maxDistance = CMath.mul(targetObject.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS);
			final double minDistance = targetObject.radius() + CMath.mul(0.75, maxDistance - targetObject.radius());
			final long medDistance = Math.round(minDistance + ((maxDistance - minDistance) / 2.0));
			final Dir3D dirToPlanet = spaceLibrary.getDirection(ship, targetObject);
			final Pair<Dir3D, Double> orbitParams = spaceLibrary.calculateOrbit(ship, targetObject);
			final double currentSpeed = ship.speed();
			double targetSpeed;
			Dir3D targetDir;
			if ((orbitParams == null)
			||(distanceFromPlanet > maxDistance)
			||(distanceFromPlanet < minDistance))
			{
				final Dir3D[] perpDirs = spaceLibrary.getPerpendicularAngles(dirToPlanet);
				Dir3D perpFacing = perpDirs[0];
				double minDiff = spaceLibrary.getAngleDelta(ship.direction(), perpDirs[0]);
				for (int i=1; i<perpDirs.length; i++)
				{
					final double diff = spaceLibrary.getAngleDelta(ship.direction(), perpDirs[i]);
					if (diff < minDiff)
					{
						perpFacing = perpDirs[i];
						minDiff = diff;
					}
				}
				targetSpeed = spaceLibrary.estimateOrbitalSpeed(targetObject);
				// Base direction toward median: inward for outer, outward for inner
				Dir3D baseDir = dirToPlanet;
				if (distanceFromPlanet < minDistance)
					baseDir = spaceLibrary.getOppositeDir(dirToPlanet);
				targetDir = spaceLibrary.getMiddleAngle(baseDir, perpFacing);
				final double speedDelta = targetSpeed - currentSpeed;
				Dir3D thrustDir = targetDir;
				double accelAmount = 0.0;
				final double maxAccel = getMaxAcceleration(ship, programEngines);
				if (currentSpeed > targetSpeed)
				{
					final Dir3D brakeDir = spaceLibrary.getOppositeDir(ship.direction());
					thrustDir = spaceLibrary.getMiddleAngle(brakeDir, targetDir);
					accelAmount = Math.min(currentSpeed, maxAccel);
				}
				else
					accelAmount = Math.min(Math.abs(speedDelta), maxAccel);
				thrustDir = graviticCourseAdjustments(ship, thrustDir, targetAcceleration);
				targetAcceleration = accelAmount;
				changeFacing(ship, turnEngine, thrustDir);
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
				// dam/s is km/s times 10, which means km/s is dam/s divided by 10
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				{
					Log.debugOut(ship.name(), "ORBITSEARCH: Navigating towards orbital zone median ("
							+ (distanceFromPlanet > maxDistance ? "outer" : "inner") + "), "
							+ "distance=" + distanceFromPlanet
							+ ", speed=" + Math.round(currentSpeed) + "dam/s, "
							+ "targetSpeed=" + Math.round(targetSpeed) + "dam/s,"
							+ " targetAccel=" + targetAcceleration);
				}
			}
			else
			{
				// Ship is within orbital range, align for stable orbit
				targetDir = orbitParams.first;
				targetSpeed = orbitParams.second.doubleValue();
				if ((ship.speed() > (targetSpeed * 0.5))
				&& (ship.speed() < (targetSpeed * 2))
				&& (distanceFromPlanet > minDistance)
				&& (distanceFromPlanet < maxDistance))
				{
					// Stable orbit achieved, transition to ORBITCRUISE
					final Dir3D planetDir = spaceLibrary.getDirection(targetObject, ship);
					final Coord3D newCoords = spaceLibrary.getLocation(targetObject.coordinates(), planetDir, medDistance);
					ship.setCoords(newCoords);
					ship.setFacing(targetDir.copyOf());
					ship.setDirection(targetDir.copyOf());
					ship.setSpeed(targetSpeed);
					track.state = ShipNavState.ORBITCRUISE;
					this.stopAllThrust(programEngines, false);
					if (CMSecurity.isDebugging(DbgFlag.SPACESHIP))
					{
						Log.debugOut(ship.name(),
								"ORBITSEARCH: Transition to ORBITCRUISE, distance=" + distanceFromPlanet
								+ ", speed=" + ship.speed()
								+ ", dir=" + CMLib.english().directionDescShort(targetDir.toDoubles()));
					}
				}
				else
				{
					// Adjust direction and speed to maintain orbit
					final Dir3D angleDiff = spaceLibrary.getAngleDiff(ship.direction(), targetDir); // - or +
					final double yawDelta = Math.abs(angleDiff.xyd());
					final double pitchDelta = Math.abs(angleDiff.zd());
					final double speedDelta = Math.abs(currentSpeed - targetSpeed);
					//targetDir = graviticCourseAdjustments(ship, targetDir);
					final double maxAccel = this.getMaxAcceleration(ship, programEngines);
					targetAcceleration = maxAccel * Math.min((yawDelta + pitchDelta + speedDelta / targetSpeed) / Math.PI, 0.5);

					// Apply thrust only if it improves direction or speed
					if ((yawDelta > 0.01)
					||(pitchDelta > 0.01))
						changeFacing(ship, turnEngine, targetDir);
					if (speedDelta > 0.01)
					{
						if (currentSpeed > targetSpeed * 1.2)
							targetDir = spaceLibrary.getOppositeDir(ship.direction()); // Decelerate
						if (!this.programAccelerationThrust(ship, programEngines, speedDelta))
							return;
					}
				}
			}
			break;
		}
		case ORBITCRUISE:
		{
			if(targetObject==null)
				break;
			@SuppressWarnings("unchecked")
			final Map<String,Object> vars = track.getArg(Map.class);
			if(!vars.containsKey("ORBIT_DISTANCE"))
				vars.put("ORBIT_DISTANCE", Long.valueOf(spaceLibrary.getDistanceFrom(ship, targetObject)));
			final Long orbitDistance = (Long)vars.get("ORBIT_DISTANCE");
			final Triad<Dir3D, Double, Coord3D> orbitParams = spaceLibrary.getOrbitalMaintenance(ship, targetObject, orbitDistance.longValue());
			if (orbitParams == null)
			{
				cancelNavigation(false);
				super.addScreenMessage(L("Orbit program aborted: unable to calculate orbital maintenance."));
				return;
			}
			final Dir3D targetDir = orbitParams.first;
			final double targetSpeed = orbitParams.second.doubleValue();
			ship.setFacing(targetDir.copyOf());
			ship.setDirection(targetDir.copyOf());
			ship.setSpeed(targetSpeed);
			ship.setCoords(orbitParams.third.copyOf());
			break;
		}
		case LANDING_APPROACH:
		{
			targetAcceleration = targetAcceleration * 0.8; // give slightly more room
			if(targetObject==null)
			{
				final String reason = L("no target planetary information");
				this.cancelNavigation(false);
				super.addScreenMessage(L("Landing program aborted with error (@x1).",reason));
				return;
			}
			final LocationRoom room = navTrack.getArg(LocationRoom.class);
			final Dir3D dirToTarget;
			final Dir3D dirToPlanet = spaceLibrary.getDirection(ship, targetObject);
			if(room != null)
				dirToTarget = spaceLibrary.getDirection(ship.coordinates(), room.coordinates());
			else
				dirToTarget = dirToPlanet;
			final long distanceToPlanet=spaceLibrary.getDistanceFrom(ship, targetObject);
			final double atmoWidth = CMath.mul(targetObject.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - targetObject.radius();
			final long critRadius = Math.round(targetObject.radius() + (atmoWidth/2.0));
			final long distanceToCritRadius=distanceToPlanet - critRadius - ship.radius();
			final boolean movingAway = spaceLibrary.getAngleDiff(ship.direction(), dirToTarget).magnitude().doubleValue()>Math.PI/4.0;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.SPACESHIP))
			{
				Log.debugOut(ship.name(),
						"Target: " + targetObject.Name()+(room!=null?(" @ "+room.name()):"")
					+ ", Dist2CR: " + CMLib.english().distanceDescShort(distanceToCritRadius)
					+ ", Dir2P: " + CMLib.english().directionDescShort(dirToPlanet.toDoubles()));
			}
			if(distanceToCritRadius <= 0)
				track.state = ShipNavState.LANDING;
			else
			if(movingAway)
			{
				this.changeFacing(ship, turnEngine, dirToTarget);
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
				break;
			}
			else
			{
				final double ticksToDecellerate = CMath.div(ship.speed(),targetAcceleration);
				final double ticksToDestinationAtCurrentSpeed = CMath.div(distanceToCritRadius, ship.speed());
				final double diff = Math.abs(ticksToDecellerate-ticksToDestinationAtCurrentSpeed);
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				{
					Log.debugOut(ship.Name(),"LAPPROACH: T2Decel:"+ticksToDecellerate
											+", T2D@CS="+ticksToDestinationAtCurrentSpeed
											+", Diff="+diff);
				}
				if((diff < 1) || (diff < Math.sqrt(0.5 * ticksToDecellerate)))
				{
					this.stopAllThrust(programEngines, false);
					break;
				}
				else
				if(ticksToDecellerate > ticksToDestinationAtCurrentSpeed/2)
					this.changeFacing(ship, turnEngine, spaceLibrary.getOppositeDir(ship.direction()));
				else
				if(ticksToDecellerate < ticksToDestinationAtCurrentSpeed*1.2)
					this.changeFacing(ship, turnEngine, dirToTarget);
				else
				{
					this.changeFacing(ship, turnEngine, dirToTarget);
					this.stopAllThrust(programEngines, false);
					break;
				}
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
				break;
			}
		}
		//$FALL-THROUGH$
		case LANDING:
		{
			if(targetObject==null)
			{
				final String reason = L("no target planetary information");
				this.cancelNavigation(false);
				super.addScreenMessage(L("Landing program aborted with error (@x1).",reason));
				return;
			}
			targetAcceleration = Math.min(targetAcceleration, ship.speed());
			final Dir3D dirToPlanet = spaceLibrary.getDirection(ship, targetObject);
			//if we aren't facing correctly, now is the time to worry about that
			if(spaceLibrary.getAngleDelta(dirToPlanet, ship.direction()) > 1)
				this.changeFacing(ship, turnEngine, spaceLibrary.getOppositeDir(dirToPlanet)); // so face opposite direction of PLANET
			final long distance=spaceLibrary.getDistanceFrom(ship, targetObject)
								- targetObject.radius()
								- ship.radius()
								-10; // margin for soft landing
			final double atmoWidth = CMath.mul(targetObject.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS) - targetObject.radius();
			final long critRadius = Math.round(targetObject.radius() + (atmoWidth / 2.0));
			final long distanceToCritRadius=spaceLibrary.getDistanceFrom(ship, targetObject)-critRadius-ship.radius();
			final double ticksToDestinationAtCurrentSpeed = Math.abs(CMath.div(distance, ship.speed()));
			final double ticksToDecellerate = CMath.div(ship.speed(),targetAcceleration);
			final double distanceLimit1 = ship.speed()*40;
			final double distanceLimit2 = Math.cbrt(distance*1000);
			if((ticksToDecellerate > ticksToDestinationAtCurrentSpeed/2)
			||(distance < distanceLimit1))
			{
				if(ship.speed() > targetAcceleration)
				{
					if(ship.speed() < (targetAcceleration + 1.0))
						targetAcceleration = 1.0;
				}
				else
				if(ship.speed()>spaceLibrary.getDistanceFrom(ship, targetObject)/4)
					targetAcceleration = ship.speed() - 1.0;
				else
				if(ship.speed()>4.0)
					targetAcceleration = ship.speed()/2;
				else
				if(ship.speed()>2.0)
					targetAcceleration = 1.0;
				else
					targetAcceleration = 0.5;
				this.changeFacing(ship, turnEngine, spaceLibrary.getOppositeDir(dirToPlanet));
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				{
					Log.debugOut(ship.Name(),"Landing Deccelerating @ " +  targetAcceleration + " because "+Math.round(ticksToDecellerate)
							+ " > " + Math.round(ticksToDestinationAtCurrentSpeed)+"  or "+Math.round(distance)+" < "+Math.round(distanceLimit1));
				}
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
			}
			else
			if((distance > distanceToCritRadius) && (ship.speed() < distanceLimit2))
			{
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				{
					Log.debugOut(ship.Name(),"Landing Accelerating because " +  Math.round(distance) +" > "+Math.round(distanceToCritRadius)
					+" and "+Math.round(ship.speed())+"dam/s < "+Math.round(distanceLimit2));
				}
				this.changeFacing(ship, turnEngine, dirToPlanet);
				if (!this.programAccelerationThrust(ship, programEngines, targetAcceleration))
					return;
			}
			else
			{
				//this.changeFacing(ship, spaceLibrary.getOppositeDir(dirToPlanet));
				if (!this.programAccelerationThrust(ship, programEngines, 0.0))
					return;
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	protected boolean checkPowerCurrent(final int value)
	{
		if(navTrack != null)
			doNavigation(navTrack);
		return super.checkPowerCurrent(value);
	}

	protected SpaceShip getShip(final Software sw)
	{
		final SpaceObject spaceObject = CMLib.space().getSpaceObject(sw, true);
		final SpaceShip ship = (spaceObject instanceof SpaceShip) ? (SpaceShip) spaceObject : null;
		if (ship == null)
			addScreenMessage(L("Error: Malfunctioning hull interface."));
		return ship;
	}

	protected SoftwareProcedure launchProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
			if(ship.getIsDocked() == null)
			{
				addScreenMessage(L("Error: Ship is already launched."));
				return false;
			}
			if(navTrack!=null)
			{
				addScreenMessage(L("Warning. Previous program cancelled."));
				cancelNavigation(false);
			}
			final SpaceObject programPlanet=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
			final List<EngProfile> engines = profileThrusters(ship, null);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			if(cancelNavigation(false))
				addScreenMessage(L("Warning. Previous program cancelled."));
			if(uword.equalsIgnoreCase("ORBIT"))
				navTrack = new ShipNavTrack(ShipNavProcess.ORBIT, programPlanet, engines, new HashMap<String,Object>());
			else
				navTrack = new ShipNavTrack(ShipNavProcess.LAUNCH, programPlanet, engines);
			addScreenMessage(L("Launch procedure initiated."));
			return false;
		}
	};

	protected SoftwareProcedure orbitProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
			SpaceObject programPlanet = null;
			if(ship.getIsDocked() != null)
				programPlanet = CMLib.space().getSpaceObject(ship.getIsDocked(), true);
			else
			{
				// Find the nearest planet from sensor data
				final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
				for (final TechComponent sensor : getShipSensors())
					allObjects.addAll(takeNewSensorReport(sensor));
				Collections.sort(allObjects, new DistanceSorter(ship));
				for (final SpaceObject O : allObjects)
				{
					if (O.getMass() > SpaceObject.MOONLET_MASS)
					{
						programPlanet = O;
						break;
					}
				}
			}
			if (programPlanet == null)
			{
				addScreenMessage(L("Error: No suitable planet found for orbit."));
				return false;
			}
			if (navTrack != null)
			{
				addScreenMessage(L("Warning. Previous program cancelled."));
				cancelNavigation(false);
			}
			final List<EngProfile> engines = profileThrusters(ship, null);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			final double maxAcceleration = getMaxAcceleration(ship, engines);
			if (maxAcceleration < SpaceObject.ACCELERATION_DAMAGED)
			{
				final int gs = (int) Math.round(maxAcceleration / SpaceObject.ACCELERATION_G);
				addScreenMessage(L("Limiting acceleration to @x1 Gs.",""+gs));
			}
			final List<SpaceObject> allObjects = new LinkedList<SpaceObject>();
			for (final TechComponent sensor : getShipSensors())
				allObjects.addAll(takeNewSensorReport(sensor));
			SpaceObject approachTarget = programPlanet;
			final long distance = CMLib.space().getDistanceFrom(ship, programPlanet);
			final double maxDistance = CMath.mul(programPlanet.radius(), SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS);
			final long minDistance = Math.round(programPlanet.radius() + CMath.mul(0.75, maxDistance - programPlanet.radius()));
			final long pickDistance = (ship.getIsDocked() != null) ? minDistance : Math.round(maxDistance);
			List<SpaceObject> navs;
			if (distance < minDistance)
			{
				final Dir3D dirFromPlanetToShip = CMLib.space().getDirection(programPlanet, ship);
				final Coord3D orbitalPointCoords = CMLib.space().moveSpaceObject(programPlanet.coordinates(), dirFromPlanetToShip, pickDistance);
				final SpaceObject orbitalPoint = (SpaceObject) CMClass.getBasicItem("Moonlet");
				orbitalPoint.setRadius(ship.radius());
				orbitalPoint.setName(L("Orbital Point"));
				orbitalPoint.setCoords(orbitalPointCoords);
				approachTarget = orbitalPoint;
				navs = calculateNavigation(ship, orbitalPoint, allObjects);
			}
			else
				navs = calculateNavigation(ship, programPlanet, allObjects);
			if (navs == null)
			{
				addScreenMessage(L("Error: Unable to navigate to orbital position."));
				cancelNavigation(false);
				return false;
			}
			navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, approachTarget, engines, new XLinkedList<SpaceObject>(navs));
			final ShipNavTrack orbitTrack = new ShipNavTrack(ShipNavProcess.ORBIT, programPlanet, engines, new HashMap<String,Object>());
			navTrack.setNextTrack(orbitTrack);
			addScreenMessage(L("Orbit procedure initiated."));
			return false;
		}
	};

	protected SoftwareProcedure stopProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
			if((ship.getIsDocked() != null)||(ship.speed()==0.0))
			{
				addScreenMessage(L("Error: Ship is already stopped."));
				return false;
			}
			if(cancelNavigation(false))
				addScreenMessage(L("Warning. Previous program cancelled."));
			lastInject = null; // force recalculation of inject
			final List<EngProfile> engines = profileThrusters(ship, null);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			if(!flipForAllStop(ship, getTurnProfile(engines)))
			{
				addScreenMessage(L("Warning. Stop program cancelled due to engine failure."));
				cancelNavigation(false);
				return false;
			}
			navTrack = new ShipNavTrack(ShipNavProcess.STOP, engines);
			addScreenMessage(L("All Stop procedure initiated."));
			return false;
		}
	};

	protected SoftwareProcedure landProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
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
			Collections.sort(allObjects, new DistanceSorter(ship));
			SpaceObject landingPlanet = null;
			LocationRoom landingZone = null;
			for(final SpaceObject O : allObjects)
			{
				if((O.coordinates()!=null)&&(O.radius()!=0))
				{
					final List<LocationRoom> rooms=CMLib.space().getLandingPoints(ship, O);
					if(rooms.size()>0)
					{
						landingPlanet=O;
						landingZone = rooms.get(0);
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

			if(cancelNavigation(false))
				addScreenMessage(L("Warning. Previous program cancelled."));
			final List<EngProfile> engines = profileThrusters(ship, null);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			if(!flipForAllStop(ship, getTurnProfile(engines)))
			{
				addScreenMessage(L("Warning. Landing program cancelled due to engine failure."));
				cancelNavigation(false);
				return false;
			}
			final SpaceObject programPlanet = landingPlanet;
			if(landingZone != null)
			{
				final SpaceObject orbitTarget = (SpaceObject) CMClass.getBasicItem("Moonlet");
				orbitTarget.setRadius(ship.radius());
				orbitTarget.setName(L("Orbit Point above @x1",landingZone.Name()));
				final Dir3D dirToLandingZone = CMLib.space().getDirection(landingPlanet.coordinates(), landingZone.coordinates());
				final long orbitalRadius = Math.round(landingPlanet.radius() +
						CMath.mul(landingPlanet.radius(),SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)*0.75);
				final Coord3D orbitalCoords = CMLib.space().getLocation(landingPlanet.coordinates(), dirToLandingZone, orbitalRadius);
				orbitTarget.setCoords(orbitalCoords);
				final List<SpaceObject> navs = calculateNavigation(ship, orbitTarget, allObjects);
				if (navs == null)
				{
					addScreenMessage(L("Error: Unable to navigate to orbital position above landing zone."));
					cancelNavigation(false);
					return false;
				}
				navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, orbitTarget, engines, new XLinkedList<SpaceObject>(navs));
				for (int i=0; i<navs.size(); i++)
				{
					final SpaceObject navPoint = navs.get(i);
					final long distFromShip = CMLib.space().getDistanceFrom(ship, navPoint) - ship.radius()
							- Math.round(navPoint.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS);
					final Dir3D dirFromShip = CMLib.space().getDirection(ship, navPoint);
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
					{
						Log.debugOut(ship.Name(),"Nav point " + (i+1) + ": Dist: " + CMLib.english().distanceDescShort(distFromShip)
								+ ", Dir: " + CMLib.english().directionDescShort(dirFromShip.toDoubles())
								+ ", Coords: " + Arrays.toString(navPoint.coordinates().toLongs()));
					}
				}
				final ShipNavTrack landTrack = new ShipNavTrack(ShipNavProcess.LAND, landingPlanet, engines, landingZone);
				navTrack.setNextTrack(landTrack);
				final long distance = CMLib.space().getDistanceFrom(ship, programPlanet);
				if((CMLib.space().getGravityForcer(ship) != null)
				&& (distance < (programPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS * 0.5)))
					navTrack.state = ShipNavState.PRE_STOP;
				addScreenMessage(L("Navigating to orbital position above landing zone: @x1.", landingZone.Name()));
			}
			else
			{
				navTrack = new ShipNavTrack(ShipNavProcess.LAND, programPlanet, engines, null);
				if(CMLib.space().getGravityForcer(ship) != null)
					navTrack.state = ShipNavState.PRE_STOP;
				final long distance=CMLib.space().getDistanceFrom(ship.coordinates(),landingPlanet.coordinates());
				if(distance > (ship.radius() + Math.round(landingPlanet.radius() * SpaceObject.MULTIPLIER_GRAVITY_EFFECT_RADIUS)))
					addScreenMessage(L("Landing approach procedure initiated."));
				else
					addScreenMessage(L("Landing procedure initiated."));
			}
			return false;
		}
	};

	protected SoftwareProcedure courseProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
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
				Collections.sort(allObjects, new DistanceSorter(ship));
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
					courseTarget.setName(L("Nav Point"));
					courseTarget.setCoords(targetCoords);
				}
			}
			if(cancelNavigation(false))
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
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
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
			Collections.sort(allObjects, new DistanceSorter(ship));
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
			final Dir3D dirTo = CMLib.space().getDirection(ship, targetObj);
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
					if(cancelNavigation(false))
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
			final String name = CMStrings.capitalizeAndLower(navTrack.proc.description());
			addScreenMessage(L("Confirmed: @x1 program stopped.",name));
			cancelNavigation(false);
			return false;
		}
	};

	protected SoftwareProcedure approachProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
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
			Collections.sort(allObjects, new DistanceSorter(ship));
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
			if(cancelNavigation(false))
				addScreenMessage(L("Warning. Previous program cancelled."));
			final Dir3D dirTo = CMLib.space().getDirection(ship, targetObj);
			final List<EngProfile> engines = profileThrusters(ship, null);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			if(!changeFacing(ship, getTurnProfile(engines), dirTo))
			{
				addScreenMessage(L("Warning. Approach program cancelled due to engine failure."));
				cancelNavigation(false);
				return false;
			}
			final List<SpaceObject> navs = calculateNavigation(ship, targetObj, allObjects);
			if(navs == null)
			{
				cancelNavigation(false);
				addScreenMessage(L("Error: Unable to navigate to target."));
				return false;
			}
			navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, approachTarget, engines, navs);
			if(CMLib.space().getGravityForcer(ship) != null)
				navTrack.state = ShipNavState.PRE_STOP;
			addScreenMessage(L("Approach to @x1 procedure engaged.",targetObj.name()));
			return false;
		}
	};

	protected SoftwareProcedure engineProcedure = new SoftwareProcedure()
	{
		@Override
		public boolean execute(final Software sw, final String uword, final MOB mob, final String unparsed, final List<String> parsed)
		{
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
			final ShipEngine engineE=findEngineByName(uword);
			if(engineE==null)
			{
				addScreenMessage(L("Error: Unknown engine name or command word '@x1'.   Try HELP.",uword));
				return false;
			}

			final Electronics E=engineE;
			final List<EngProfile> engines = profileThrusters(ship, engineE);
			if((engines==null)||(engines.size()==0))
			{
				addScreenMessage(L("Error: Malfunctioning thrusters interface."));
				return false;
			}
			EngProfile profile = null;
			for (final EngProfile p : engines)
			{
				if (p.engine == engineE)
				{
					profile = p;
					break;
				}
			}

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
			double thrustAmount;
			final double maxAccel = getMaxAcceleration(ship, new XArrayList<EngProfile>(profile));
			if(CMath.isNumber(amountStr))
				thrustAmount=CMath.s_double(amountStr);
			else
			if(CMath.isPct(amountStr))
				thrustAmount = CMath.s_pct(amountStr) * maxAccel;
			else
			{
				final BigDecimal d = CMLib.english().parseSpaceSpeed(amountStr);
				if((d==null)||(d.doubleValue()<=0.0))
				{
					addScreenMessage(L("Error: '@x1' is not a valid amount or speed.",amountStr));
					return false;
				}
				thrustAmount = d.doubleValue();
				if(thrustAmount > maxAccel)
					thrustAmount = maxAccel;
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
				navTrack.speedLimit = Math.round(maxAccel);
			else
			if(courseSet && (course.size()>0))
			{
				final List<ShipEngine> programEngines=new XVector<ShipEngine>(engineE);
				navTrack = new ShipNavTrack(ShipNavProcess.APPROACH, courseTarget, programEngines, course);
				return true;
			}
			CMMsg msg = null;
			if((thrustAmount > 0)&&(profile!=null))
			{
				final String code=TechCommand.THRUST.makeCommand(portDir,Double.valueOf(profile.injection(thrustAmount)));
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
			final SpaceShip ship=getShip(sw);
			if(ship==null)
				return false;
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
			Collections.sort(allObjects, new DistanceSorter(ship));
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
			final Dir3D notDirTo=CMLib.space().getDirection(ship, targetObj);
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
				if(cancelNavigation(false))
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
