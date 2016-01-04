package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdBoardable;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine.ThrustPort;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2013-2016 Bo Zimmerman

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
public class GenSpaceShip extends StdBoardable implements Electronics, SpaceShip
{
	@Override public String ID(){	return "GenSpaceShip";}
	protected Manufacturer	cachedManufact  = null;
	protected String	 	manufacturer	= "RANDOM";
	public long[]   		coordinates 	= new long[3];
	public double[] 		direction   	= new double[2];
	public double			roll			= 0.0;
	public double 			speed			= 0;
	protected SpaceObject	spaceTarget 	= null;
	protected double[]		facing			= new double[2];
	protected Boolean		inAirFlag		= Boolean.FALSE;

	public GenSpaceShip()
	{
		super();
		setName("the space ship [NEWNAME]");
		setDisplayText("the space ship [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_STEEL);
		this.doorName="hatch";
	}

	@Override 
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
	}
	
	@Override
	protected String getAreaClassType()
	{
		return "StdSpaceShip";
	}
	
	@Override
	protected Room createFirstRoom()
	{
		final Room R=CMClass.getLocale("MetalRoom");
		R.setDisplayText(L("The Cockpit"));
		return R;
	}
	
	@Override
	public Area getShipArea()
	{
		if((!destroyed)&&(area==null))
		{
			final Area area=super.getShipArea();
			if(area != null)
				area.setTheme(Area.THEME_TECHNOLOGY);
			return area;
		}
		return super.getShipArea();
	}

	@Override
	public void dockHere(Room R)
	{
		if((CMSecurity.isDebugging(DbgFlag.SPACESHIP))&&(getIsDocked()==null))
			Log.debugOut("SpaceShip "+name()+" is docking at '"+R.displayText()+"' ("+R.roomID()+")");
		if(R instanceof LocationRoom)
			setCoords(((LocationRoom)R).coordinates());
		CMLib.map().delObjectInSpace(getShipSpaceObject());
		setSpeed(0);
		super.dockHere(R);
	}

	@Override
	public void unDock(boolean moveToOutside)
	{
		if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			Log.debugOut("SpaceShip "+name()+" is undocking"+(moveToOutside?"into space":""));
		final Room R=getIsDocked();
		super.unDock(moveToOutside);
		if(R instanceof LocationRoom)
		{
			setDirection(Arrays.copyOf(((LocationRoom)R).getDirectionFromCore(),2));
			setFacing(Arrays.copyOf(((LocationRoom)R).getDirectionFromCore(),2));
		}
		if(moveToOutside)
		{
			final SpaceObject o = getShipSpaceObject();
			if((o != null)&&(R instanceof LocationRoom))
				CMLib.map().addObjectToSpace(o,((LocationRoom)R).coordinates());
		}
	}

	@Override
	public void renameShip(String newName)
	{
		final Area area=this.getShipArea();
		if(area instanceof BoardableShip)
		{
			final String oldName=area.Name();
			String registryNum=area.getBlurbFlag("REGISTRY");
			if(registryNum==null) 
				registryNum="";
			super.renameShip(newName);
			CMLib.tech().unregisterElectronics(null, oldName+registryNum);
			registryNum=Double.toString(Math.random());
			area.addBlurbFlag("REGISTRY Registry#"+registryNum.substring(registryNum.indexOf('.')+1));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WEAPONATTACK:
			{
				final double myMass=getMass();
				final double hardness = RawMaterial.CODES.HARDNESS(material()) * SpaceObject.Distance.Kilometer.dm;
				msg.setValue((int)Math.round((usesRemaining() * (msg.value() / myMass)) / hardness));
				if((msg.value() < 2) || (!okAreaMessage(msg,false)))
					return false;
				return true;
			}
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))&&(msg.targetMessage()!=null))
				{
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(parts);
					if(command!=null)
					{
						final Object[] parms=command.confirmAndTranslate(parts);
						if(parms!=null)
						{
							if(command==Technical.TechCommand.ACCELLLERATION)
							{
								final ThrustPort dir=(ThrustPort)parms[0];
								final int amount=((Integer)parms[1]).intValue();
								//long specificImpulse=((Long)parms[2]).longValue();
								if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
									Log.debugOut("SpaceShip "+name()+" accellerates "+amount+" to the "+dir.toString());
								if(amount != 0)
								{
									switch(dir)
									{
									case STARBOARD: 
										facing[0]-=CMath.mul(amount,0.017); 
										break;
									case PORT: 
										facing[0]+=CMath.mul(amount,0.017); 
										break;
									case DORSEL: 
										facing[1]-=CMath.mul(amount,0.017); 
										break;
									case VENTRAL: 
										facing[1]+=CMath.mul(amount,0.017); 
										break;
									case FORWARD: 
										break;
									case AFT:
									{
										if((getIsDocked()!=null) && (amount > SpaceObject.ACCELLERATION_G))
											unDock(true);
										// this will move it, but will also update speed and direction -- all good!
										final double inAirFactor=inAirFlag.booleanValue()?(1.0-getOMLCoeff()):1.0;
										CMLib.map().moveSpaceObject(this,facing(),Math.round((amount-1.0)*inAirFactor));
										break;
									}
									}
									facing[0]=Math.abs(facing[0]%(2*Math.PI));
									facing[1]=Math.abs(facing[1]%(2*Math.PI));  // should that really be 2*?
								}
							}
						}
					}
				}
				break;
			case CMMsg.TYP_WEAPONATTACK: // kinetic damage taken to the outside of the ship
			{
				final long myMass=getMass();
				if((msg.value() > 1)&&(myMass>0))
				{
					final int dmg = msg.value();
					if(dmg >= usesRemaining())
					{
						msg.setOthersMessage(L("For a split second, you hear a loud crash and feel a jolt."));
						sendAreaMessage(msg,false);
						destroyThisShip();
					}
					else
					{
						
						setUsesRemaining(usesRemaining() - Math.round(dmg));
						if(dmg > 75)
							msg.setOthersMessage(L("You hear a booming crash and feel a crushing jolt."));
						else
						if(dmg > 50)
							msg.setOthersMessage(L("You hear a loud crash and feel a hard jolt."));
						else
						if(dmg > 25)
							msg.setOthersMessage(L("You hear a noise and feel a small jolt."));
						else
							msg.setOthersMessage(L("You hear a bump and feel a short rattle."));
						sendAreaMessage(msg,false);
					}
				}
				break;
			}
			case CMMsg.TYP_COLLISION:
			{
				final MOB mob=msg.source();
				final double previousSpeed = speed();
				if((msg.tool() instanceof SpaceObject) // we hit something very very big
				&&(((SpaceObject)msg.tool()).getMass() >= (100 * SpaceObject.Distance.Kilometer.dm)))
				{
					if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
						Log.debugOut("SpaceShip "+name()+" just hit "+msg.tool().Name()+"!!!");
					stopThisShip(mob);
				}
				
				final long myMass=getMass();
				if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
					Log.debugOut("SpaceShip "+name()+" just collided with "+msg.tool().Name()+" of mass "+myMass+" at speed "+previousSpeed);
				
				// this only works because Areas don't move.
				// the only way to hit one is to be moving towards it.
				if((previousSpeed > (SpaceObject.ACCELLERATION_DAMAGED))
				&&(msg.tool() instanceof SpaceObject)
				&&(((SpaceObject)msg.tool()).getMass() >= (myMass / 100))) // hitting tiny things don't matter
				{
					SpaceObject O=(SpaceObject)msg.tool();
					// This is technically wrong. Imagine taping a huge object from behind because you 
					// are going just a tiny bit faster, even though you are both going very fast.
					// However, the odds of that happening are nothing.  Forget it.
					double absorbedDamage = (previousSpeed * myMass) + (O.speed() * O.getMass());
					if(absorbedDamage > Integer.MAX_VALUE / 10)
						absorbedDamage = Integer.MAX_VALUE / 10;
					if(absorbedDamage > 1)
					{
						// we've been -- hit? It's up to the item itself to see to it's own explosion or whatever
						//TODO: might want to vary this message..
						final CMMsg sMsg=CMClass.getMsg(msg.source(),this,O,CMMsg.MSG_WEAPONATTACK,null);
						sMsg.setValue((int)Math.round(absorbedDamage));
						if(O.okMessage(O, sMsg)  && okMessage(this,sMsg))
						{
							O.executeMsg(O, sMsg);
							if(sMsg.value() > 1)
								executeMsg(this,sMsg);
						}
					}
				}

				if((!amDestroyed()) && (msg.tool() instanceof Area))
				{
					long shortestDistance=Long.MAX_VALUE;
					LocationRoom LR = null;
					for(final Enumeration<Room> r=((Area)msg.tool()).getMetroMap();r.hasMoreElements();)
					{
						final Room R2=r.nextElement();
						if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
						&&(R2 instanceof LocationRoom))
						{
							long distanceFrom=CMLib.map().getDistanceFrom(coordinates(), ((LocationRoom)R2).coordinates());
							if(distanceFrom<shortestDistance)
							{
								shortestDistance=distanceFrom;
								LR=(LocationRoom)R2;
							}
						}
					}
					if(LR!=null)
					{
						//final CMMsg kMsg=CMClass.getMsg(msg.source(),getShipArea(),this,CMMsg.MSG_OK_ACTION,L("The ship comes to a resting stop."));
						dockHere(LR); // set location and so forth
					}
					else
					{
						// we landed, but there was nowhere to dock!
						stopThisShip(mob);
					}
				}
				sendComputerMessage(mob,msg);
				break;
			}
			default:
				break;
			}
		}
	}

	@Override
	protected LocationRoom findNearestDocks(Room R)
	{
		final List<LocationRoom> docks=new XVector<LocationRoom>();
		if(R!=null)
		{
			if((R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
			&&(R instanceof LocationRoom))
				return (LocationRoom)R;

			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY)
					.plus(TrackingLibrary.TrackingFlag.NOWATER);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
			{
				if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
				&&(R2 instanceof LocationRoom)
				&&(R.getArea().inMyMetroArea(R2.getArea())))
					docks.add((LocationRoom)R2);
			}
			if(docks.size()==0)
			{
				for(final Enumeration<Room> r=R.getArea().getMetroMap();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
				}
			}
			if(docks.size()==0)
			{
				for(final Room R2 : rooms)
				{
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
				}
			}
		}
		if(docks.size()==0)
			return null;
		return docks.get(CMLib.dice().roll(1, docks.size(), -1));
	}

	protected void stopThisShip(MOB mob)
	{
		if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
			Log.debugOut("SpaceShip "+name()+" is now STOPPED!");
		setSpeed(0); // if you collide with something massive, your speed ENDS
		final List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(CMLib.tech().getElectronicsKey(getShipArea()));
		for(final Electronics E : electronics)
		{
			if(E instanceof ShipComponent.ShipEngine)
			{
				final CMMsg msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE, null, CMMsg.NO_EFFECT,null);
				if(E.okMessage(mob, msg))
					E.executeMsg(mob, msg);
			}
		}
	}

	@Override
	public BoundedCube getBounds()
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
	}

	@Override
	public long powerCapacity()
	{
		return 0;
	}

	@Override
	public void setPowerCapacity(long capacity)
	{
	}

	@Override
	public long powerRemaining()
	{
		return 0;
	}

	@Override
	public int powerNeeds()
	{
		return 0;
	}

	@Override
	public void setPowerRemaining(long remaining)
	{
	}

	@Override
	public void activate(boolean truefalse)
	{
	}
	
	protected void sendComputerMessage(final MOB mob, final CMMsg msg)
	{
		final Area ship=getShipArea();
		if(ship!=null)
		{
			List<Electronics> electronics = CMLib.tech().getMakeRegisteredElectronics(CMLib.tech().getElectronicsKey(ship));
			for(final Electronics E : electronics)
			{
				if(E instanceof Electronics.Computer)
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
			}
		}
	}

	@Override 
	public TechType getTechType() 
	{ 
		return TechType.SHIP_SPACESHIP; 
	}

	@Override
	public Boolean getSetAirFlag(final Boolean setInAirFlag)
	{
		if((setInAirFlag != null) && (setInAirFlag != this.inAirFlag))
			this.inAirFlag = setInAirFlag;
		return this.inAirFlag;
	}

	@Override
	public SpaceObject getShipSpaceObject()
	{
		return this;
	}

	@Override 
	public boolean activated()
	{
		return true;
	}
	
	@Override 
	public int techLevel() 
	{ 
		return phyStats().ability();
	}
	
	@Override 
	public void setTechLevel(int lvl) 
	{ 
		basePhyStats.setAbility(lvl); recoverPhyStats(); 
	}
	
	@Override 
	public String getManufacturerName() 
	{ 
		return manufacturer; 
	}
	
	@Override 
	public void setManufacturerName(String name) 
	{ 
		cachedManufact=null; 
		if(name!=null) 
			manufacturer=name; 
	}

	@Override public long getMass()
	{
		return basePhyStats().weight()+((area instanceof SpaceShip)?((SpaceShip)area).getMass(): 1000);
	}

	@Override
	public Manufacturer getFinalManufacturer()
	{
		if(cachedManufact==null)
		{
			cachedManufact=CMLib.tech().getManufacturerOf(this,getManufacturerName().toUpperCase().trim());
			if(cachedManufact==null)
				cachedManufact=CMLib.tech().getDefaultManufacturer();
		}
		return cachedManufact;
	}

	@Override 
	public long[] coordinates()
	{
		return coordinates;
	}
	
	@Override 
	public double[] direction()
	{
		return direction;
	}
	
	@Override 
	public double roll() 
	{ 
		return roll; 
	}
	
	@Override 
	public void setRoll(double dir) 
	{ 
		roll =dir; 
	}
	
	@Override 
	public double[] facing() 
	{ 
		return facing; 
	}
	
	@Override 
	public void setFacing(double[] dir) 
	{ 
		if(dir!=null) 
			this.facing=dir; 
	}
	
	@Override 
	public SpaceObject knownTarget()
	{
		return spaceTarget;
	}
	
	@Override 
	public void setKnownTarget(SpaceObject O)
	{
		spaceTarget=O;
	}
	
	@Override 
	public void setCoords(long[] coords)
	{
		if((coords!=null)&&(coords.length==3))
			CMLib.map().moveSpaceObject(this,coords);
	}
	
	@Override 
	public void setDirection(double[] dir)
	{
		if(dir!=null)
			direction=dir;
	}
	
	@Override 
	public double speed()
	{
		return speed;
	}
	
	@Override 
	public void setSpeed(double v)
	{
		speed=v;
	}

	@Override
	public SpaceObject knownSource()
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).knownSource():null;
	}

	@Override
	public void setKnownSource(SpaceObject O)
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setKnownSource(O);
	}

	@Override
	public long radius()
	{
		return (area instanceof SpaceObject)?((SpaceObject)area).radius():50;
	}

	@Override
	public void setRadius(long radius)
	{
		if (area instanceof SpaceObject)
			((SpaceObject)area).setRadius(radius);
	}

	@Override
	public double getOMLCoeff()
	{
		return (area instanceof SpaceShip)?((SpaceShip)area).getOMLCoeff()
				:SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE + ((SpaceObject.ATMOSPHERIC_DRAG_BRICK-SpaceObject.ATMOSPHERIC_DRAG_STREAMLINE)/2.0);
	}

	@Override
	public void setOMLCoeff(double coeff)
	{
		if (area instanceof SpaceShip)
			((SpaceShip)area).setOMLCoeff(coeff);
	}

	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME","RIDEBASIS","MOBSHELD",
											"POWERCAP","ACTIVATED","POWERREM","MANUFACTURER","AREA","COORDS","RADIUS",
											"ROLL","DIRECTION","SPEED","FACING","OWNER","PRICE","DEFCLOSED","DEFLOCKED",
											"PUTSTR","MOUNTSTR","DISMOUNTSTR","EXITNAME"
										  };
	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return "" + hasALock();
		case 1:
			return "" + hasADoor();
		case 2:
			return "" + capacity();
		case 3:
			return "" + containTypes();
		case 4:
			return "" + openDelayTicks();
		case 5:
			return "" + rideBasis();
		case 6:
			return "" + riderCapacity();
		case 7:
			return "" + powerCapacity();
		case 8:
			return "" + activated();
		case 9:
			return "" + powerRemaining();
		case 10:
			return getManufacturerName();
		case 11:
			return CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		case 12:
			return CMParms.toListString(coordinates());
		case 13:
			return "" + radius();
		case 14:
			return "" + roll();
		case 15:
			return CMParms.toListString(direction());
		case 16:
			return "" + speed();
		case 17:
			return CMParms.toListString(facing());
		case 18:
			return getOwnerName();
		case 19:
			return "" + getPrice();
		case 20:
			return "" + defaultsClosed();
		case 21:
			return "" + defaultsLocked();
		case 22:
			return putString;
		case 23:
			return mountString;
		case 24:
			return dismountString;
		case 25:
			return "" + doorName();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), CMath.s_bool(val), false, CMath.s_bool(val) && defaultsLocked());
			break;
		case 1:
			setDoorsNLocks(CMath.s_bool(val), isOpen(), CMath.s_bool(val) && defaultsClosed(), hasALock(), isLocked(), defaultsLocked());
			break;
		case 2:
			setCapacity(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS, val));
			break;
		case 4:
			setOpenDelayTicks(CMath.s_parseIntExpression(val));
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			setPowerCapacity(CMath.s_parseIntExpression(val));
			break;
		case 8:
			activate(CMath.s_bool(val));
			break;
		case 9:
			setPowerRemaining(CMath.s_parseLongExpression(val));
			break;
		case 10:
			setManufacturerName(val);
			break;
		case 11:
			setShipArea(val);
			break;
		case 12:
			setCoords(CMParms.toLongArray(CMParms.parseCommas(val, true)));
			coordinates[0] = coordinates[0] % SpaceObject.Distance.GalaxyRadius.dm;
			coordinates[1] = coordinates[1] % SpaceObject.Distance.GalaxyRadius.dm;
			coordinates[2] = coordinates[2] % SpaceObject.Distance.GalaxyRadius.dm;
			break;
		case 13:
			setRadius(CMath.s_long(val));
			break;
		case 14:
			setRoll(CMath.s_double(val));
			break;
		case 15:
			setDirection(CMParms.toDoubleArray(CMParms.parseCommas(val, true)));
			break;
		case 16:
			setSpeed(CMath.s_double(val));
			break;
		case 17:
			setFacing(CMParms.toDoubleArray(CMParms.parseCommas(val, true)));
			break;
		case 18:
			setOwnerName(val);
			break;
		case 19:
			setPrice(CMath.s_int(val));
			break;
		case 20:
			setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
			break;
		case 21:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
			break;
		case 22:
			putString = val;
			break;
		case 23:
			mountString = val;
			break;
		case 24:
			dismountString = val;
			break;
		case 25:
			doorName = val;
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenSpaceShip.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSpaceShip))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
