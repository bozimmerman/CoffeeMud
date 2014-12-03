package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2013-2014 Bo Zimmerman

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
public class GenSpaceShip extends StdPortal implements Electronics, SpaceShip, PrivateProperty
{
	@Override public String ID(){	return "GenSpaceShip";}
	protected String 		readableText	= "";
	protected String 		ownerName		= "";
	protected int 			price 			= 1000;
	protected Area 			area			= null;
	protected Manufacturer	cachedManufact  = null;
	protected String	 	manufacturer	= "RANDOM";
	public long[]   		coordinates 	= new long[3];
	public double[] 		direction   	= new double[2];
	public double			roll			= 0.0;
	public long 			speed			= 0;
	protected SpaceObject	spaceTarget 	= null;
	protected double[]		facing			= new double[2];
	protected Boolean		inAirFlag		= Boolean.FALSE;
	protected String 		putString		= "load(s)";
	protected String 		mountString		= "board(s)";
	protected String 		dismountString	= "disembark(s) from";
	protected String		homePortID		= "";

	public GenSpaceShip()
	{
		super();
		setName("the space ship [NEWNAME]");
		setDisplayText("the space ship [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setDescription("");
		myUses=100;
		this.doorName="hatch";
		basePhyStats().setWeight(10000);
		setUsesRemaining(100);
		recoverPhyStats();
		CMLib.flags().setGettable(this, false);
	}

	@Override
	public CMObject newInstance()
	{
		final GenSpaceShip ship = (GenSpaceShip)super.newInstance();
		ship.area=null;
		ship.getShipArea();
		return ship;
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
	public boolean subjectToWearAndTear()
	{
		return true;
	}
	
	@Override
	public Item getShipItem()
	{
		return this;
	}
	
	@Override
	public Area getShipArea()
	{
		if(destroyed)
			return null;
		else
		if(area==null)
		{
			area=CMClass.getAreaType("StdSpaceShip");
			CMLib.flags().setSavable(area, false);
			final String num=Double.toString(Math.random());
			final int x=num.indexOf('.')+1;
			final int len=((num.length()-x)/2)+1;
			area.setName(L("UNNAMED_@x1",num.substring(x,x+len)));
			area.setTheme(Area.THEME_TECHNOLOGY);
			final Room R=CMClass.getLocale("MetalRoom");
			R.setDisplayText("The Cockpit");
			R.setRoomID(area.Name()+"#0");
			R.setSavable(false);
			area.addProperRoom(R);
			((SpaceShip)area).setDockableItem(this);
			readableText=R.roomID();
		}
		return area;
	}

	@Override
	public void setDockableItem(Item dockableItem)
	{
		if(area instanceof BoardableShip)
			((BoardableShip)area).setDockableItem(dockableItem);
	}

	@Override
	public void setShipArea(String xml)
	{
		try
		{
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area instanceof BoardableShip)
			{
				area.setSavable(false);
				((BoardableShip)area).setDockableItem(this);
				for(final Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
					CMLib.flags().setSavable(r.nextElement(), false);
			}
			else
			{
				Log.warnOut("Failed to unpack a space ship area for the space ship");
				getShipArea();
			}
		}
		catch (final CMException e)
		{
			Log.warnOut("Unable to parse space ship xml for some reason.");
		}
	}

	@Override
	public void dockHere(Room R)
	{
		if(!R.isContent(this))
		{
			if(owner()==null)
				R.addItem(this,Expire.Never);
			else
				R.moveItemTo(me, Expire.Never, Move.Followers);
		}
		if(R instanceof LocationRoom)
			setCoords(((LocationRoom)R).coordinates());
		if(this.homePortID.length()==0)
			this.homePortID=CMLib.map().getExtendedRoomID(R);
		CMLib.map().delObjectInSpace(getShipSpaceObject());
		if (area instanceof SpaceShip)
			((SpaceShip)area).dockHere(R);
		setSpeed(0);
	}

	@Override
	public void unDock(boolean moveToOutside)
	{
		final Room R=getIsDocked();
		if(R!=null)
		{
			R.delItem(this);
			setOwner(null);
		}
		if (area instanceof BoardableShip)
			((BoardableShip)area).unDock(moveToOutside);
		if(R instanceof LocationRoom)
		{
			setDirection(((LocationRoom)R).getDirectionFromCore());
			setFacing(((LocationRoom)R).getDirectionFromCore());
		}
		if(moveToOutside)
		{
			final SpaceObject o = getShipSpaceObject();
			if((o != null)&&(R instanceof LocationRoom))
				CMLib.map().addObjectToSpace(o,((LocationRoom)R).coordinates());
		}
	}

	@Override
	public Room getIsDocked()
	{
		if (area instanceof BoardableShip)
			return ((BoardableShip)area).getIsDocked();
		if(owner() instanceof LocationRoom)
			return ((LocationRoom)owner());
		return null;
	}

	@Override 
	public String getHomePortID() 
	{ 
		return this.homePortID; 
	}
	
	@Override 
	public void setHomePortID(String portID) 
	{ 
		this.homePortID = portID;
	}

	@Override 
	public String keyName() 
	{ 
		return readableText;
	}
	
	@Override 
	public void setKeyName(String newKeyName) 
	{ 
		// don't do this, as MUDGrinder mucks it up
	}

	@Override 
	public String readableText()
	{
		return readableText;
	}
	
	@Override 
	public void setReadableText(String text)
	{
		if((text!=null)&&(text.length()>0))
			readableText=text;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public CMObject copyOf()
	{
		final GenSpaceShip s=(GenSpaceShip)super.copyOf();
		s.destroyed=false;
		s.setOwnerName("");
		final String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		s.setReadableText(readableText()); // in case this was first call to getShipArea()
		CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(s.getShipArea()));
		return s;
	}

	@Override
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().deleteAllTicks(area);
			final String key=CMLib.tech().getElectronicsKey(area);
			CMLib.tech().unregisterAllElectronics(key);
		}
		super.stopTicking();
		this.destroyed=false; // undo the weird thing
	}

	@Override
	protected Room getDestinationRoom()
	{
		getShipArea();
		Room R=null;
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		if((V.size()>0)&&(getShipArea()!=null))
			R=getShipArea().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}

	@Override
	public void destroy()
	{
		if(area!=null)
			CMLib.map().obliterateArea(area);
		super.destroy();
	}

	@Override 
	public int getPrice() 
	{ 
		return price; 
	}
	
	@Override 
	public void setPrice(int price) 
	{ 
		this.price=price; 
	}

	@Override 
	public String getOwnerName() 
	{ 
		return ownerName; 
	}
	
	@Override 
	public void setOwnerName(String owner) 
	{ 
		this.ownerName=owner;
	}

	@Override
	public long expirationDate()
	{
		return super.expirationDate();
	}

	@Override
	public void setExpirationDate(long time)
	{
		if((time>0)&&(owner() instanceof Room))
			super.setExpirationDate(0);
		else
			super.setExpirationDate(time);
	}
		
	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0) 
			return null;
		final Clan C=CMLib.clans().getClan(owner);
		if(C!=null) 
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	@Override 
	public String getTitleID() 
	{ 
		return this.toString(); 
	}

	@Override
	public void renameShip(String newName)
	{
		final Area area=this.area;
		if(area instanceof SpaceShip)
		{
			final Room oldEntry=getDestinationRoom();
			final String oldName=area.Name();
			String registryNum=area.getBlurbFlag("REGISTRY");
			if(registryNum==null) 
				registryNum="";
			((SpaceShip)area).renameShip(newName);
			CMLib.tech().unregisterElectronics(null, oldName+registryNum);
			registryNum=Double.toString(Math.random());
			area.addBlurbFlag("REGISTRY Registry#"+registryNum.substring(registryNum.indexOf('.')+1));
			setReadableText(oldEntry.roomID());
			setShipArea(CMLib.coffeeMaker().getAreaObjectXML(area, null, null, null, true).toString());
		}
		for(final String word : new String[]{"NAME","NEWNAME","SHIPNAME","SHIP"})
		{
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(Name().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setName(CMStrings.replaceAll(Name(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(displayText().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setDisplayText(CMStrings.replaceAll(displayText(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
			{
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("@x1 on <T-NAME>",CMLib.english().startWithAorAn(doorName()))));
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",CMLib.english().startWithAorAn(doorName()))));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("@x1 on <T-NAME>",CMLib.english().startWithAorAn(doorName()))));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",CMLib.english().startWithAorAn(doorName()))));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("@x1 on <T-NAME>",CMLib.english().startWithAorAn(doorName()))));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",CMLib.english().startWithAorAn(doorName()))));
				break;
			}
			case CMMsg.TYP_WEAPONATTACK:
			{
				if((msg.value() < 2) || (!okAreaMessage(msg)))
					return false;
				break;
			}
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			
			return true;
		}
		return super.tick(ticking, tickID);
	}

	protected synchronized void destroyThisShip()
	{
		if(this.getOwnerName().length()>0)
		{
			final MOB M = CMLib.players().getLoadPlayer(this.getOwnerName());
			final PlayerStats pStats = (M!=null) ? M.playerStats() : null;
			final ItemCollection items= (pStats != null) ? pStats.getExtItems() : null;
			if(items != null)
				items.delItem(this);
		}
		final CMMsg expireMsg=CMClass.getMsg(CMLib.map().deity(), this, CMMsg.MASK_ALWAYS|CMMsg.MSG_EXPIRE, L("<T-NAME> is destroyed!"));
		for(final Enumeration<Room> r = getShipArea().getProperMap(); r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R!=null)
			{
				expireMsg.setTarget(R);
				final Set<MOB> players=CMLib.players().getPlayersHere(R);
				if(players.size()>0)
					for(final MOB M : players)
					{
						//players will get some fancy message when appearing in death room -- which means they should die!
						M.executeMsg(expireMsg.source(), expireMsg);
						CMLib.combat().justDie(expireMsg.source(), M);
					}
				R.send(expireMsg.source(), expireMsg);
			}
		}
		getShipArea().destroy();
		destroy();
	}
	
	protected boolean okAreaMessage(final CMMsg msg)
	{
		boolean failed = false;
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				failed = failed || R.okMessage(R, msg);
				if(failed)
					break;
			}
		}
		return failed;
	}
	
	protected void sendAreaMessage(final CMMsg msg)
	{
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				R.sendOthers(msg.source(), msg);
			}
		}
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				if(msg.tool() instanceof ShopKeeper)
				{
					final ShopKeeper shop=(ShopKeeper)msg.tool();
					final boolean clanSale = 
							   shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN) 
							|| shop.isSold(ShopKeeper.DEAL_CSHIPSELLER) 
							|| shop.isSold(ShopKeeper.DEAL_CLANDSELLER);
					CMLib.map().registerWorldObjectLoaded(null, null, this);
					transferOwnership(msg.source(),clanSale);
				}
				break;
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
								switch(dir)
								{
								case STARBOARD: 
									facing[0]-=amount; 
									break;
								case PORT: 
									facing[0]+=amount; 
									break;
								case DORSEL: 
									facing[1]-=amount; 
									break;
								case VENTRAL: 
									facing[1]+=amount; 
									break;
								case FORWARD: 
									break;
								case AFT:
								{
									if((getIsDocked()!=null) && (amount > SpaceObject.ACCELLERATION_G))
										unDock(true);
									// this will move it, but will also update speed and direction -- all good!
									final double inAirFactor=inAirFlag.booleanValue()?(1.0-getOMLCoeff()):1.0;
									CMLib.map().moveSpaceObject(this,facing(),Math.round((((double)amount/(double)getMass())-1.0)*inAirFactor));
									break;
								}
								}
								facing[0]=facing[0]%(2*Math.PI);
								facing[1]=facing[1]%(2*Math.PI);
							}
						}
					}
				}
				break;
			case CMMsg.TYP_WEAPONATTACK: // kinetic damage taken
			{
				final long myMass=getMass();
				if((msg.value() > 1)&&(myMass>0))
				{
					sendAreaMessage(msg);
					double dmg = usesRemaining() * (msg.value() / myMass) ;
					if(dmg >= usesRemaining())
					{
						destroyThisShip();
					}
					else
						setUsesRemaining(usesRemaining() - (int)Math.round(dmg));
				}
				break;
			}
			case CMMsg.TYP_COLLISION:
			{
				final MOB mob=msg.source();
				final boolean hitSomethingMassive;
				
				if((msg.tool() instanceof SpaceObject) // we hit something very very big
				&&(((SpaceObject)msg.tool()).getMass() >= (100 * SpaceObject.Distance.Kilometer.dm)))
				{
					hitSomethingMassive=true;
					stopThisShip(mob);
				}
				else
					hitSomethingMassive=false;
				
				// this only works because Areas don't move.
				// the only way to hit one is to be moving towards it.
				if((speed() <= SpaceObject.ACCELLERATION_DAMAGED) 
				&&(msg.tool() instanceof Area))
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
						dockHere(LR); // set location and so forth
					}
					else
					{
						// we landed, but there was nowhere to dock!
						stopThisShip(mob);
					}
				}
				else
				if(msg.tool() instanceof SpaceObject)
				{
					SpaceObject O=(SpaceObject)msg.tool();
					final long relSpeed = CMath.abs(speed() - O.speed());
					if((hitSomethingMassive) || (relSpeed > SpaceObject.VELOCITY_LIGHT)) // you hit a planet, or something moving too fast
					{
						destroyThisShip();
					}
					else
					{
						final long myMass=getMass();
						final long dmgSpeed = CMath.abs(speed() - O.speed()) % SpaceObject.VELOCITY_LIGHT;
						final long relMass = CMath.abs(myMass - O.getMass())  % (100 * SpaceObject.Distance.Kilometer.dm);
						final int hardness = (int)(RawMaterial.CODES.HARDNESS(material()) * SpaceObject.Distance.Kilometer.dm);
						final int kineticDamage = (hardness > 0) ? Math.round((dmgSpeed * relMass) / hardness ) : 0;
						if(kineticDamage > 1)
						{
							// we've been -- hit? It's up to the item itself to see to it's own explosion or whatever
							final CMMsg kMsg=CMClass.getMsg(msg.source(),getShipArea(),O,CMMsg.MSG_WEAPONATTACK,L("You hear a crash and feel the ship shake."));
							kMsg.setValue(kineticDamage);
							if(O.okMessage(O, kMsg) && okMessage(O,kMsg))
							{
								O.executeMsg(O, kMsg);
								if(kMsg.value() > 1)
									executeMsg(this,kMsg);
							}
						}
					}
				}
				else
				{
					//so there was a collision, but not with a space object?
					Log.errOut("SpaceShip","Collided with "+msg.tool());
				}
				sendComputerMessage(mob,msg);
				break;
			}
			default:
				break;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target() instanceof ShopKeeper))
		{
			setOwnerName("");
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(getOwnerName().length()>0)
		&&((msg.source().Name().equals(getOwnerName()))
			||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
			||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof PostOffice)))
		{
			final boolean clanSale = CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER);
			transferOwnership((MOB)msg.target(),clanSale);
		}
	}

	protected LocationRoom findNearestDocks(Room R)
	{
		final List<LocationRoom> docks=new XVector<LocationRoom>();
		if(R!=null)
		{
			if((R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
			&&(R instanceof LocationRoom))
				return (LocationRoom)R;
			
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY)
					.plus(TrackingLibrary.TrackingFlag.NOWATER);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
				if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
				&&(R2 instanceof LocationRoom)
				&&(R.getArea().inMyMetroArea(R2.getArea())))
					docks.add((LocationRoom)R2);
			if(docks.size()==0)
				for(final Enumeration<Room> r=R.getArea().getMetroMap();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
				}
			if(docks.size()==0)
				for(final Room R2 : rooms)
					if((R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(R2 instanceof LocationRoom))
						docks.add((LocationRoom)R2);
		}
		if(docks.size()==0)
			return null;
		return docks.get(CMLib.dice().roll(1, docks.size(), -1));
	}

	protected void transferOwnership(final MOB buyer, boolean clanSale)
	{
		if(getOwnerName().length()>0)
		{
			final MOB M=CMLib.players().getLoadPlayer(getOwnerName());
			if((M!=null)&&(M.playerStats()!=null))
			{
				M.playerStats().getExtItems().delItem(this);
				M.playerStats().setLastUpdated(0);
			}
			else
			{
				final Clan C=CMLib.clans().getClan(getOwnerName());
				if(C!=null)
				{
					C.getExtItems().delItem(this);
					CMLib.database().DBUpdateClanItems(C);
				}
			}
			setOwnerName("");
		}
		if(clanSale)
		{
			final Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if(targetClan!=null)
				setOwnerName(targetClan.first.clanID());
			else
				setOwnerName(buyer.Name());
		}
		else
			setOwnerName(buyer.Name());
		recoverPhyStats();
		//String registryNum=Double.toString(Math.random());
		//String randNum=CMStrings.limit(registryNum.substring(registryNum.indexOf('.')+1), 4);
		//renameSpaceShip("SS "+buyer.Name()+", Reg "+randNum);
		final Session session=buyer.session();
		final Room R=CMLib.map().roomLocation(this);
		if(session!=null)
		{
			final GenSpaceShip me=this;
			final InputCallback[] namer=new InputCallback[1];
			namer[0]=new InputCallback(InputCallback.Type.PROMPT)
			{
				@Override public void showPrompt() { session.println(L("\n\rEnter a new name for your ship: ")); }
				@Override public void timedOut() { }
				@Override public void callBack()
				{
					for(final Enumeration<BoardableShip> s=CMLib.map().ships();s.hasMoreElements();)
					{
						final BoardableShip ship=s.nextElement();
						if((ship!=null)&&(!ship.amDestroyed())&&(ship.getShipArea()!=null)&&(ship.getShipArea().Name().equalsIgnoreCase(this.input.trim())))
						{
							this.input="";
							break;
						}
					}
					if(CMLib.map().getArea(this.input.trim())!=null)
						this.input="";

					if((this.input.trim().length()==0)
					||(!CMLib.login().isOkName(this.input.trim(),true))
					||(CMLib.tech().getMakeRegisteredKeys().contains(this.input.trim())))
					{
						session.println(L("^ZThat is not a permitted name.^N"));
						session.prompt(namer[0].reset());
						return;
					}
					me.renameShip(this.input.trim());
					buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
					final LocationRoom finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of spaceport.");
						buyer.tell(L("Nowhere was found to dock your ship.  Please contact the administrators!."));
					}
					else
					{
						me.dockHere(finalR);
						buyer.tell(L("You'll find your ship docked at '@x1'.",finalR.displayText(buyer)));
					}
					if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(me)))
						buyer.playerStats().getExtItems().addItem(me);
				}
			};
			session.prompt(namer[0]);
		}
		else
		{
			buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
			if((this.getOwnerName().equals(buyer.Name()) && (buyer.playerStats() != null)))
			{
				if(!buyer.playerStats().getExtItems().isContent(this))
					buyer.playerStats().getExtItems().addItem(this);
			}
			else
			{
				final Clan C=CMLib.clans().getClan(getOwnerName());
				if(C!=null)
				{
					if(!C.getExtItems().isContent(this))
						C.getExtItems().addItem(this);
				}
				else
				{
					buyer.tell(L("However, there is no entity to actually take ownership.  Wierd."));
				}
			}
			final LocationRoom finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of spaceport.");
			else
				dockHere(finalR);
		}
	}

	@Override 
	public String putString(Rider R)
	{ 
		return putString;
	}
	
	@Override 
	public String mountString(int commandType, Rider R)
	{ 
		return mountString;
	}
	
	@Override 
	public String dismountString(Rider R)
	{	
		return dismountString;
	}
	
	@Override
	public boolean isSavable()
	{
		if(!super.isSavable())
			return false;
		return (getOwnerName().length()==0);
	}

	protected void stopThisShip(MOB mob)
	{
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

	@Override public long powerCapacity(){return 0;}
	@Override public void setPowerCapacity(long capacity){}
	@Override public long powerRemaining(){return 0;}
	@Override public int powerNeeds(){return 0;}
	@Override public void setPowerRemaining(long remaining){}
	@Override public void activate(boolean truefalse){}
	
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
	public long speed()
	{
		return speed;
	}
	
	@Override 
	public void setSpeed(long v)
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
		case 0: return ""+hasALock();
		case 1: return ""+hasADoor();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+openDelayTicks();
		case 5: return ""+rideBasis();
		case 6: return ""+riderCapacity();
		case 7: return ""+powerCapacity();
		case 8: return ""+activated();
		case 9: return ""+powerRemaining();
		case 10: return getManufacturerName();
		case 11: return CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		case 12: return CMParms.toStringList(coordinates());
		case 13: return ""+radius();
		case 14: return ""+roll();
		case 15: return CMParms.toStringList(direction());
		case 16: return ""+speed();
		case 17: return CMParms.toStringList(facing());
		case 18: return getOwnerName();
		case 19: return ""+getPrice();
		case 20: return ""+defaultsClosed();
		case 21: return ""+defaultsLocked();
		case 22: return putString;
		case 23: return mountString;
		case 24: return dismountString;
		case 25: return ""+doorName();
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
		case 0: setDoorsNLocks(hasADoor(),isOpen(),defaultsClosed(),CMath.s_bool(val),false,CMath.s_bool(val)&&defaultsLocked()); break;
		case 1: setDoorsNLocks(CMath.s_bool(val),isOpen(),CMath.s_bool(val)&&defaultsClosed(),hasALock(),isLocked(),defaultsLocked()); break;
		case 2: setCapacity(CMath.s_parseIntExpression(val)); break;
		case 3: setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS,val)); break;
		case 4: setOpenDelayTicks(CMath.s_parseIntExpression(val)); break;
		case 5: break;
		case 6: break;
		case 7: setPowerCapacity(CMath.s_parseIntExpression(val)); break;
		case 8: activate(CMath.s_bool(val)); break;
		case 9: setPowerRemaining(CMath.s_parseLongExpression(val)); break;
		case 10: setManufacturerName(val); break;
		case 11: setShipArea(val); break;
		case 12: setCoords(CMParms.toLongArray(CMParms.parseCommas(val, true))); break;
		case 13: setRadius(CMath.s_long(val)); break;
		case 14: setRoll(CMath.s_double(val)); break;
		case 15: setDirection(CMParms.toDoubleArray(CMParms.parseCommas(val,true))); break;
		case 16: setSpeed(CMath.s_long(val)); break;
		case 17: setFacing(CMParms.toDoubleArray(CMParms.parseCommas(val,true))); break;
		case 18: setOwnerName(val); break;
		case 19: setPrice(CMath.s_int(val)); break;
		case 20: setDoorsNLocks(hasADoor(),isOpen(),CMath.s_bool(val),hasALock(),isLocked(),defaultsLocked()); break;
		case 21: setDoorsNLocks(hasADoor(),isOpen(),defaultsClosed(),hasALock(),isLocked(),CMath.s_bool(val)); break;
		case 22: putString=val; break;
		case 23: mountString=val; break;
		case 24: dismountString=val; break;
		case 25: doorName = val; break;
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
		final String[] superCodes=GenericBuilder.GENITEMCODES;
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
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
