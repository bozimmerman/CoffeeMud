package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.interfaces.Places;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenPortal;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class StdSpaceShip extends StdBoardableShip implements SpaceShip
{
	private static final long STALE_AIR_INTERVAL = 5 * 60 * 1000;

	private static final long STALE_WARN_INTERVAL = 5 * 30 * 1000;
	
	protected static Climate climateObj=null;

	protected volatile int	mass			= -1;
	protected SpaceObject	spaceSource		= null;
	protected TimeClock		localClock		= (TimeClock) CMClass.getCommon("DefaultTimeClock");
	protected int			atmosphere		= RawMaterial.RESOURCE_AIR;
	protected long			radius			= 50;
	protected double		omlCoeff		= SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE + ((SpaceObject.ATMOSPHERIC_DRAG_BRICK - SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE) / 2.0);
	protected volatile long	nextStaleCheck	= System.currentTimeMillis() + STALE_AIR_INTERVAL;
	protected volatile long	nextStaleWarn	= System.currentTimeMillis() + STALE_WARN_INTERVAL;
	protected Set<String> 	staleAirList	= new HashSet<String>();
	protected Ability 		gravityFloaterA = null;
	
	@Override
	public String ID()
	{
		return "StdSpaceShip";
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public Room getIsDocked()
	{
		return CMLib.map().getRoom(savedDock);
	}

	@Override
	public void setClimateObj(Climate obj)
	{
		climateObj = obj;
	}

	@Override
	public Climate getClimateObj()
	{
		if (climateObj == null)
		{
			climateObj = (Climate) CMClass.getCommon("DefaultClimate");
			climateObj.setCurrentWeatherType(Climate.WEATHER_CLEAR);
			climateObj.setNextWeatherType(Climate.WEATHER_CLEAR);
		}
		return climateObj;
	}

	@Override
	public double getOMLCoeff()
	{
		return omlCoeff;
	}

	@Override
	public void setOMLCoeff(double coeff)
	{
		omlCoeff = coeff;
	}

	@Override
	public TimeClock getTimeObj()
	{
		return localClock;
	}

	@Override
	public void setTimeObj(TimeClock obj)
	{
		localClock = obj;
	}

	@Override
	public int getAtmosphereCode()
	{
		return atmosphere;
	}

	@Override
	public void setAtmosphere(int resourceCode)
	{
		atmosphere = resourceCode;
	}

	@Override
	public int getAtmosphere()
	{
		return atmosphere == ATMOSPHERE_INHERIT ? RawMaterial.RESOURCE_AIR : atmosphere;
	}

	@Override
	public long radius()
	{
		return radius;
	}

	@Override
	public void setRadius(long radius)
	{
		this.radius = radius;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public SpaceObject knownSource()
	{
		return spaceSource;
	}

	@Override
	public void setKnownSource(SpaceObject O)
	{
		spaceSource = O;
	}

	@Override
	public long[] coordinates()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).coordinates() : new long[3];
	}

	@Override
	public void setCoords(long[] coords)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setCoords(coords);
	}

	@Override
	public double[] direction()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).direction() : new double[2];
	}

	@Override
	public double roll()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).roll() : 0;
	}

	@Override
	public void setRoll(double dir)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setRoll(dir);
	}

	@Override
	public void setDirection(double[] dir)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setDirection(dir);
	}

	@Override
	public double[] facing()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).facing() : new double[2];
	}

	@Override
	public void setFacing(double[] dir)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setFacing(dir);
	}

	@Override
	public double speed()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).speed() : 0;
	}

	@Override
	public void setSpeed(double v)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setSpeed(v);
	}

	@Override
	public SpaceObject knownTarget()
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).knownTarget() : null;
	}

	@Override
	public void setKnownTarget(SpaceObject O)
	{
		if (shipItem instanceof SpaceShip)
			((SpaceShip) shipItem).setKnownTarget(O);
	}

	@Override
	public void setShipFlag(final ShipFlag flag, final boolean setShipFlag)
	{
		if(shipItem instanceof SpaceShip) 
			((SpaceShip) shipItem).setShipFlag(flag,setShipFlag);
	}

	@Override
	public boolean getShipFlag(final ShipFlag flag)
	{
		return (shipItem instanceof SpaceShip) ? ((SpaceShip) shipItem).getShipFlag(flag) : false;
	}

	@Override
	public void setDockableItem(Item dockableItem)
	{
		if(dockableItem instanceof SpaceShip)
			shipItem=(SpaceShip)dockableItem;
	}

	@Override
	public BoundedCube getBounds()
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
	}

	@Override
	public long getMass()
	{
		final long mass=this.mass;
		if(mass<0)
		{
			int newMass=phyStats().weight();
			for(final Enumeration<Room> r=getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R!=null)
				{
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if(I!=null)
							newMass += I.phyStats().weight();
					}
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if(M!=null)
							newMass += M.phyStats().weight();
					}
				}
			}
			this.mass=newMass;
		}
		return this.mass;
	}

	@Override
	public void destroy()
	{
		CMLib.map().delObjectInSpace(this);
		super.destroy();
		spaceSource=null;
		climateObj=null;
	}
	
	@Override
	public int getClimateTypeCode()
	{
		return Places.CLIMASK_NORMAL;
	}

	@Override
	public int getClimateType()
	{
		return Places.CLIMASK_NORMAL;
	}

	@Override
	public void setClimateType(int newClimateType)
	{
	}

	public StdSpaceShip()
	{
		super();
		setName("a space ship");
	}
	
	@Override
	public void setName(String newName)
	{
		super.setName(newName);
		localClock.setLoadName(newName);
	}

	@Override
	public int getTheme()
	{
		return Area.THEME_TECHNOLOGY;
	}

	@Override
	public int getThemeCode()
	{
		return Area.THEME_TECHNOLOGY;
	}

	@Override
	public void setTheme(int level)
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdSpaceShip();
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	protected void cloneFix(StdBoardableShip ship)
	{
		super.cloneFix(ship);
		setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
	}
	
	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdSpaceShip E=(StdSpaceShip)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.AREA);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);

		if((msg.sourceMinor()==CMMsg.TYP_DROP)||(msg.sourceMinor()==CMMsg.TYP_GET))
			mass=-1;

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
				{
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(parts);
					if(command!=null)
					{
						final Object[] parms=command.confirmAndTranslate(parts);
						if(parms!=null)
						{
							if(command==Technical.TechCommand.AIRREFRESH)
							{
								if((staleAirList.size()==0)
								&&(msg.tool() instanceof Item)
								&&(((Item)msg.tool()).owner() instanceof Room)
								&&(((Room)((Item)msg.tool()).owner()).getAtmosphere()<=0))
									doStaleCheck();
								if(staleAirList.size()>0)
								{
									final double pct=((Double)parms[0]).doubleValue();
									final int atmoResource=((Integer)parms[1]).intValue();
									int numToClear=(int)Math.round(CMath.mul(staleAirList.size(),pct));
									while((numToClear>0)&&(staleAirList.size()>0))
									{
										final String roomID=staleAirList.iterator().next();
										staleAirList.remove(roomID);
										changeRoomAir(getRoom(roomID),null,atmoResource);
										numToClear--;
									}
									changeRoomAir(getRandomMetroRoom(),null,atmoResource);
									for(final Pair<Room,Integer> p  : shipExitCache)
										changeRoomAir(p.first,null,atmoResource);
								}
								if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
									Log.debugOut("Refreshed the air in "+Name()+", stale rooms: "+staleAirList.size());
							}
						}
					}
				}
				break;
			}
		}
		else
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_STAND:
			case CMMsg.TYP_DISMOUNT:
			{
				if(this.getShipFlag(ShipFlag.NO_GRAVITY))
				{
					msg.addTrailerRunnable(new Runnable(){
						@Override
						public void run()
						{
							final Ability floater = getGravityFloat();
							if(floater != null)
								floater.invoke(floater.invoker(), msg.source(), false, 0);
						}
					});
				}
				break;
			}
			}
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_THROW:
				if((msg.target() instanceof Item)
				||(msg.tool() instanceof Item))
				{
					if(this.getShipFlag(ShipFlag.NO_GRAVITY))
					{
						final Item I=(msg.target() instanceof Item) ? (Item)msg.target(): (Item)msg.tool();
						msg.addTrailerRunnable(new Runnable(){
							@Override
							public void run()
							{
								final Ability floater = getGravityFloat();
								if(floater != null)
									floater.invoke(floater.invoker(), I, false, 0);
							}
						});
					}
				}
				break;
			}
		}
	}

	public int[] addMaskAndReturn(int[] one, int[] two)
	{
		if(one.length!=two.length)
			return one;
		final int[] returnable=new int[one.length];
		for(int o=0;o<one.length;o++)
			returnable[o]=one[o]+two[o];
		return returnable;
	}

	protected boolean changeRoomAir(Room R, Room notifyRoom, int atmoResource)
	{
		if(R==null)
			return false;
		if(R.getAtmosphere()!=atmoResource)
		{
			if(atmoResource==0)
			{
				R.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 rushes out of the room.",RawMaterial.CODES.NAME(R.getAtmosphere()).toLowerCase()));
				if((notifyRoom!=null)&&(notifyRoom!=R))
					notifyRoom.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 rushes out of the room.",RawMaterial.CODES.NAME(R.getAtmosphere()).toLowerCase()));
			}
			else
			{
				R.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 rushes into the room.",RawMaterial.CODES.NAME(atmoResource).toLowerCase()));
				if((notifyRoom!=null)&&(notifyRoom!=R))
					notifyRoom.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 rushes into the room.",RawMaterial.CODES.NAME(atmoResource).toLowerCase()));
			}
			if(atmoResource==getAtmosphere())
				R.setAtmosphere(-1);
			else
				R.setAtmosphere(atmoResource);
			return true;
		}
		return false;
	}

	protected void moveAtmosphereOut(Set<Room> doneRooms, Room startRoom, int atmo)
	{
		final LinkedList<Room> toDoRooms=new LinkedList<Room>();
		toDoRooms.add(startRoom);
		while(toDoRooms.size()>0)
		{
			final Room R=toDoRooms.removeFirst();
			doneRooms.add(R);
			staleAirList.remove(R.roomID());
			if(changeRoomAir(R,startRoom,atmo))
				break;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room R2=R.getRoomInDir(d);
				final Exit E2=R.getExitInDir(d);
				if((R2!=null)&&(R2.getArea()==R.getArea())&&(E2!=null)&&(E2.isOpen())&&(!doneRooms.contains(R2)))
					toDoRooms.add(R2);
			}
		}
	}
	
	protected void doStaleCheck()
	{
		nextStaleCheck=System.currentTimeMillis()+STALE_AIR_INTERVAL;
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(!staleAirList.contains(R.roomID()))
				staleAirList.add(R.roomID());
			else
				R.setAtmosphere(RawMaterial.RESOURCE_NOTHING); // WE NOW HAVE A VACUUM HERE!!!
		}
	}
	
	protected Ability getGravityFloat()
	{
		if(gravityFloaterA == null)
		{
			gravityFloaterA=CMClass.getAbility("GravityFloat");
			if(gravityFloaterA != null)
			{
				MOB M=CMClass.getMOB("StdMOB");
				M.setName(Name());
				M.setLocation(this.getRandomProperRoom());
				gravityFloaterA.setInvoker(M);
			}
		}
		return gravityFloaterA;
	}
	
	protected void doGravityChanges()
	{
		final boolean gravExistsNow = getShipFlag(ShipFlag.IN_THE_AIR) || (this.getIsDocked()!=null);
		if(gravExistsNow == getShipFlag(ShipFlag.NO_GRAVITY)) // opposite, so it needs changing
		{
			final Ability floater = getGravityFloat();
			if(floater != null)
			{
				final SpaceObject spaceObject=getShipSpaceObject();
				final String code=Technical.TechCommand.GRAVITYCHANGE.makeCommand(Boolean.valueOf(gravExistsNow));
				final String msgStr;
				if(gravExistsNow)
					msgStr=L("You feel the pull of gravity returning.");
				else
					msgStr=L("You no longer feel the pull of gravity.");
				final CMMsg msg=CMClass.getMsg(floater.invoker(), spaceObject, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.MSG_QUIETMOVEMENT,msgStr);
				boolean cancelled = false;
				for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						if(!R.okMessage(msg.source(), msg))
							cancelled=true;
					}
				}
				setShipFlag(ShipFlag.NO_GRAVITY, !gravExistsNow);
				if(cancelled)
				{
					return;
				}
				for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
						R.send(floater.invoker(), msg);
				}
				for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if(M!=null)
								floater.invoke(floater.invoker(), M, gravExistsNow, 0);
						}
						for(int i=0;i<R.numItems();i++)
						{
							final Item I=R.getItem(i);
							if(I!=null)
								floater.invoke(floater.invoker(), I, gravExistsNow, 0);
						}
					}
				}
			}
		}
	}
	
	protected void doAtmosphereChanges()
	{
		final Set<Room> doneRooms=new HashSet<Room>();
		for(final Pair<Room,Integer> p : shipExitCache)
		{
			final Room R=p.first;
			final Exit E=R.getExitInDir(p.second.intValue());
			if((E!=null)&&(E.isOpen()))
			{
				final Room exitRoom=R;
				final Room otherRoom=R.getRoomInDir(p.second.intValue());
				final int atmo=otherRoom.getAtmosphere();
				moveAtmosphereOut(doneRooms,exitRoom,atmo);
			}
		}
		if((System.currentTimeMillis() > nextStaleWarn)&&(staleAirList.size()>0))
		{
			nextStaleWarn = System.currentTimeMillis() + STALE_WARN_INTERVAL;
			for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((staleAirList.contains(R.roomID()))
				&&(R.numInhabitants()>0)
				&&(R.numPCInhabitants()>0))
				{
					final int atmo=R.getAtmosphere();
					if(atmo>0)
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(!M.isMonster())
						&&(!CMLib.flags().canBreatheThis(M,RawMaterial.RESOURCE_NOTHING)))
							M.tell(L("The @x1 is seeming a bit stale.",RawMaterial.CODES.NAME(atmo).toLowerCase()));
					}
				}
			}
		}
		if(System.currentTimeMillis() >= nextStaleCheck)
		{
			int numStaleRooms = staleAirList.size();
			doStaleCheck();
			if(staleAirList.size()>numStaleRooms)
				nextStaleWarn = System.currentTimeMillis() + STALE_WARN_INTERVAL;
			if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
				Log.debugOut("Used up the air in "+Name()+", stale rooms: "+staleAirList.size());
			
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			doAtmosphereChanges();
			doGravityChanges();
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	@Override
	public void dockHere(Room roomR)
	{
		super.dockHere(roomR);
		if(roomR==null)
			return;
		CMLib.map().delObjectInSpace(getShipSpaceObject());
	}

	@Override
	public SpaceObject getShipSpaceObject()
	{
		return (shipItem instanceof SpaceObject) ? (SpaceObject)shipItem : null;
	}

	private static final String[] LOCAL_CODES={"RADIUS","AUTHOR"};
	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] superCodes=super.getStatCodes();
		codes=new String[superCodes.length+LOCAL_CODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<LOCAL_CODES.length;i++,x++)
			codes[i]=LOCAL_CODES[x];
		return codes;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	@Override
	public String getStat(String code)
	{
		if(getCodeNum(code) < super.getStatCodes().length)
			return super.getStat(code);
		switch(CMParms.indexOf(LOCAL_CODES,code.toUpperCase().trim()))
		{
		case 0:
			return "" + getOMLCoeff();
		case 1:
			return "" + radius();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		if(getCodeNum(code) < super.getStatCodes().length)
			super.setStat(code, val);
		else
		switch(CMParms.indexOf(LOCAL_CODES,code.toUpperCase().trim()))
		{
			case 0:
				setOMLCoeff(CMath.s_double(val));
				break;
			case 1:
				setRadius(CMath.s_long(val));
				break;
		}
	}
}
