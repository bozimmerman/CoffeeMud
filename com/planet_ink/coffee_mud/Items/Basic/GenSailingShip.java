package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2014-2014 Bo Zimmerman

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
public class GenSailingShip extends StdPortal implements PrivateProperty, BoardableShip
{
	@Override public String ID(){	return "GenSailingShip";}
	protected String 			 readableText	 = "";
	protected String 			 ownerName 		 = "";
	protected int 				 price 			 = 1000;
	protected Area 				 area			 = null;
	protected volatile int		 directionFacing = -1;
	protected volatile boolean	 anchorDown		 = true;
	protected final List<Integer>courseDirections= new Vector<Integer>();
	protected String 			 putString		 = "load(s)";
	protected String 			 mountString	 = "board(s)";
	protected String 			 dismountString	 = "disembark(s) from";
	protected String			 homePortID		 = "";

	public GenSailingShip()
	{
		super();
		setName("a sailing ship");
		setDisplayText("a sailing ship is here.");
		setMaterial(RawMaterial.RESOURCE_OAK);
		setDescription("");
		myUses=100;
		this.doorName="hatch";
		basePhyStats().setWeight(10000);
		setUsesRemaining(100);
		recoverPhyStats();
		CMLib.flags().setGettable(this, false);
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
		if(usesRemaining()>0)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
	}
	
	@Override 
	public boolean subjectToWearAndTear()
	{
		return true;
	}
	
	@Override
	public Area getShipArea()
	{
		if(destroyed)
			return null;
		else
		if(area==null)
		{
			area=CMClass.getAreaType("StdBoardableShip");
			final String num=Double.toString(Math.random());
			area.setName(L("UNNAMED_@x1",num.substring(num.indexOf('.')+1)));
			area.setTheme(Area.THEME_FANTASY);
			final Room R=CMClass.getLocale("WoodenDeck");
			R.setRoomID(area.Name()+"#0");
			R.setSavable(false);
			area.addProperRoom(R);
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
	public Item getShipItem()
	{
		return this;
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
				Log.warnOut("Failed to unpack a sailing ship area for the sailing ship");
				getShipArea();
			}
		}
		catch (final CMException e)
		{
			Log.warnOut("Unable to parse sailing ship xml for some reason.");
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
		if(this.homePortID.length()==0)
			this.homePortID=CMLib.map().getExtendedRoomID(R);
		if (area instanceof BoardableShip)
			((BoardableShip)area).dockHere(R);
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
	}

	@Override
	public Room getIsDocked()
	{
		if (area instanceof BoardableShip)
			return ((BoardableShip)area).getIsDocked();
		if(owner() instanceof Room)
			return ((Room)owner());
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
		readableText=newKeyName;
	}

	@Override 
	public String readableText()
	{
		return readableText;
	}
	
	@Override 
	public void setReadableText(String text)
	{
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
		final GenSailingShip s=(GenSailingShip)super.copyOf();
		CMLib.flags().setSavable(s, false);
		s.destroyed=false;
		s.setOwnerName("");
		final String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		return s;
	}

	@Override
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().deleteAllTicks(area);
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
		return 0;
	}

	@Override
	public void setExpirationDate(long time)
	{
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
	
	@Override public String getTitleID() 
	{ 
		return this.toString(); 
	}

	@Override
	public void renameShip(String newName)
	{
		final Area area=this.area;
		if(area instanceof BoardableShip)
		{
			final Room oldEntry=getDestinationRoom();
			((BoardableShip)area).renameShip(newName);
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
	
	protected boolean securityCheck(final MOB mob)
	{
		return (getOwnerName().length()>0)
			 &&(mob!=null)
			 &&((mob.Name().equals(getOwnerName()))
				||(mob.getLiegeID().equals(getOwnerName())&mob.isMarriedToLiege())
				||(CMLib.clans().checkClanPrivilege(mob, getOwnerName(), Clan.Function.PROPERTY_OWNER)));
	}

	protected void announceToShip(final String msgStr)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),CMLib.map().roomLocation(this));
		try
		{
			final CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
			final Room R=CMLib.map().roomLocation(this);
			if((R!=null) && (R.okMessage(mob, msg2) && this.okAreaMessage(msg2, false)))
			{
				R.send(mob, msg2); // this lets the source know, i guess
				this.sendAreaMessage(msg2, false); // this just sends to "others"
			}
		}
		finally
		{
			mob.destroy();
		}
	}
	
	protected void haveEveryoneLookOverBow()
	{
		if((area != null)&&(owner() instanceof Room))
		{
			final Room targetR=(Room)owner();
			for(final Enumeration<Room> r=area.getProperMap(); r.hasMoreElements(); )
			{
				final Room R=r.nextElement();
				if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
				{
					final Set<MOB> mobs=CMLib.players().getPlayersHere(R);
					for(final MOB mob : mobs)
					{
						if(mob == null)
							continue;
						final CMMsg lookMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK,null);
						final CMMsg lookExitMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK_EXITS,null);
						if((mob.isAttribute(MOB.Attrib.AUTOEXITS))&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=1)&&(CMLib.flags().canBeSeenBy(targetR,mob)))
						{
							if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=2)!=mob.isAttribute(MOB.Attrib.BRIEF))
								lookExitMsg.setValue(CMMsg.MASK_OPTIMIZE);
							lookMsg.addTrailerMsg(lookExitMsg);
						}
						if(targetR.okMessage(mob,lookMsg))
							targetR.send(mob,lookMsg);
					}
				}
			}
		}
	}
	
	protected boolean steer(final MOB mob, final Room R, final int dir)
	{
		CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> change(s) coarse, steering @x1 @x2.",name(mob),Directions.getDirectionName(dir)));
		if((R.okMessage(mob, msg2) && this.okAreaMessage(msg2, true)))
		{
			R.send(mob, msg2); // this lets the source know, i guess
			this.sendAreaMessage(msg2, true); // this just sends to "others"
			this.directionFacing=dir;
			return true;
		}
		return false;
	}
	
	protected int getAnyExitDir(Room R)
	{
		if(R==null)
			return -1;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			Room R2=R.getRoomInDir(d);
			Exit E2=R.getExitInDir(d);
			if((R2!=null)&&(E2!=null)&&(CMLib.map().getExtendedRoomID(R2).length()>0))
				return d;
		}
		return -1;
	}
	
	protected Room findOceanRoom(Area A)
	{
		if(A==null)
			return null;
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null)&&(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(CMLib.map().getExtendedRoomID(R).length()>0))
				return R;
		}
		return null;
	}
	
	protected boolean safetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)|| R.amDestroyed() || (getAnyExitDir(R)<0))
		{
			Room R2=CMLib.map().getRoom(getHomePortID());
			if((R2==null)&&(R.getArea()!=null))
				R2=findOceanRoom(R.getArea());
			if(R2==null)
				for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					R2=findOceanRoom(a.nextElement());
					if(R2!=null)
						break;
				}
			if(R2==null)
				return false;
			R2.moveItemTo(this);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.tool() instanceof ShopKeeper))
		{
			final ShopKeeper shop=(ShopKeeper)msg.tool();
			final boolean clanSale = 
					   shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN) 
					|| shop.isSold(ShopKeeper.DEAL_CSHIPSELLER) 
					|| shop.isSold(ShopKeeper.DEAL_CLANDSELLER);
			transferOwnership(msg.source(),clanSale);
			return false;
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=(cmds.size()>1) ? cmds.get(1).toUpperCase() : "";
			if(word.equals("RAISE") && secondWord.equals("ANCHOR"))
			{
				if(!securityCheck(msg.source()))
				{
					msg.source().tell(L("The captain does not permit you."));
					return false;
				}
				if(safetyMove())
				{
					msg.source().tell(L("The ship has moved!"));
					return false;
				}
				final Room R=CMLib.map().roomLocation(this);
				if(!anchorDown)
					msg.source().tell(L("The anchor is already up."));
				else
				if(R!=null)
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, "<S-NAME> raise(s) anchor.");
					if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
					{
						R.send(msg.source(), msg2);
						this.sendAreaMessage(msg2, true);
						anchorDown=false;
					}
				}
				return false;
			}
			else
			if(word.equals("LOWER")  && secondWord.equals("ANCHOR"))
			{
				if(!securityCheck(msg.source()))
				{
					msg.source().tell(L("The captain does not permit you."));
					return false;
				}
				if(safetyMove())
				{
					msg.source().tell(L("The ship has moved!"));
					return false;
				}
				final Room R=CMLib.map().roomLocation(this);
				if(anchorDown)
					msg.source().tell(L("The anchor is already down."));
				else
				if(R!=null)
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, "<S-NAME> lower(s) anchor.");
					if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
					{
						R.send(msg.source(), msg2);
						this.sendAreaMessage(msg2, true);
						anchorDown=true;
					}
				}
				return false;
			}
			else
			if(word.equals("STEER"))
			{
				if(!securityCheck(msg.source()))
				{
					msg.source().tell(L("The captain does not permit you."));
					return false;
				}
				if(safetyMove())
				{
					msg.source().tell(L("The ship has moved!"));
					return false;
				}
				this.directionFacing=-1;
				int dir=Directions.getCompassDirectionCode(secondWord);
				if(dir<0)
				{
					msg.source().tell(L("Steer the ship which direction?"));
					return false;
				}
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
				{
					msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
					return false;
				}
				final Room targetRoom=R.getRoomInDir(dir);
				final Exit targetExit=R.getExitInDir(dir);
				if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
				{
					msg.source().tell(L("There doesn't look to be anything in that direction."));
					return false;
				}
				steer(msg.source(),R, dir);
				if(anchorDown)
					msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
				return false;
			}
			else
			if(word.equals("SAIL"))
			{
				if(!securityCheck(msg.source()))
				{
					msg.source().tell(L("The captain does not permit you."));
					return false;
				}
				if(safetyMove())
				{
					msg.source().tell(L("The ship has moved!"));
					return false;
				}
				int dir=Directions.getCompassDirectionCode(secondWord);
				if(dir<0)
				{
					msg.source().tell(L("Sail the ship which direction?"));
					return false;
				}
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
				{
					msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
					return false;
				}
				final Room targetRoom=R.getRoomInDir(dir);
				final Exit targetExit=R.getExitInDir(dir);
				if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
				{
					msg.source().tell(L("There doesn't look to be anything in that direction."));
					return false;
				}
				if(anchorDown)
				{
					msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
					return false;
				}
				sail(dir);
				this.directionFacing=-1;
				return false;
			}
			else
			if(word.equals("COURSE") || (word.equals("SET") && word.equals("COURSE")))
			{
				if(!securityCheck(msg.source()))
				{
					msg.source().tell(L("The captain does not permit you."));
					return false;
				}
				if(safetyMove())
				{
					msg.source().tell(L("The ship has moved!"));
					return false;
				}
				this.directionFacing=-1;
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
				{
					msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
					return false;
				}
				int dirIndex = 1;
				if(word.equals("SET"))
					dirIndex = 2;
				if(dirIndex >= cmds.size())
				{
					msg.source().tell(L("To set a course, you must specify some directions of travel, separated by spaces."));
					return false;
				}
				int firstDir = -1;
				this.courseDirections.clear();
				for(;dirIndex<cmds.size();dirIndex++)
				{
					final String dirWord=cmds.get(dirIndex);
					int dir=Directions.getCompassDirectionCode(dirWord);
					if(dir<0)
					{
						msg.source().tell(L("@x1 is not a valid direction.",dirWord));
						return false;
					}
					if(firstDir < 0)
						firstDir = dir;
					else
						this.courseDirections.add(Integer.valueOf(dir));
				}
				final Room targetRoom=R.getRoomInDir(firstDir);
				final Exit targetExit=R.getExitInDir(firstDir);
				if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
				{
					msg.source().tell(L("There doesn't look to be anything in that direction."));
					return false;
				}
				steer(msg.source(),R, firstDir);
				if(anchorDown)
					msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
				return false;
			}
			return true;
		}
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
				if((msg.value() < 2) || (!okAreaMessage(msg,false)))
					return false;
				break;
			}
			}
		}
		return true;
	}

	protected int sail(final int direction)
	{
		final Room thisRoom=CMLib.map().roomLocation(this);
		if(thisRoom != null)
		{
			final Room destRoom=thisRoom.getRoomInDir(direction);
			final Exit exit=thisRoom.getExitInDir(direction);
			if((destRoom!=null)&&(exit!=null))
			{
				if((destRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
				&&(destRoom.domainType()!=Room.DOMAIN_OUTDOORS_SEAPORT))
				{
					announceToShip(L("As there is no where to sail @x1, <S-NAME> meanders along the waves.",Directions.getInDirectionName(direction)));
					courseDirections.clear();
					return -1;
				}
				final int oppositeDirectionFacing=Directions.getOpDirectionCode(direction);
				final String directionName=Directions.getDirectionName(direction);
				final String otherDirectionName=Directions.getDirectionName(oppositeDirectionFacing);
				final Exit opExit=thisRoom.getExitInDir(oppositeDirectionFacing);
				final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),CMLib.map().roomLocation(this));
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
				mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
				try
				{
					final CMMsg enterMsg=CMClass.getMsg(mob,destRoom,exit,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> sail(s) in from @x1.",otherDirectionName));
					final CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,L("<S-NAME> sail(s) @x1.",directionName));
					if((exit.okMessage(mob,enterMsg))
					&&(leaveMsg.target().okMessage(mob,leaveMsg))
					&&((opExit==null)||(opExit.okMessage(mob,leaveMsg)))
					&&(enterMsg.target().okMessage(mob,enterMsg)))
					{
						exit.executeMsg(mob,enterMsg);
						thisRoom.sendOthers(mob, leaveMsg);
						destRoom.moveItemTo(this);
						this.dockHere(destRoom);
						this.sendAreaMessage(leaveMsg, true);
						if(opExit!=null)
							opExit.executeMsg(mob,leaveMsg);
						destRoom.send(mob, enterMsg);
						haveEveryoneLookOverBow();
						return direction;
					}
					else
					{
						announceToShip(L("<S-NAME> can not seem to travel @x1.",Directions.getInDirectionName(direction)));
						courseDirections.clear();
						return -1;
					}
						
				}
				finally
				{
					mob.destroy();
				}
			}
			else
			{
				announceToShip(L("As there is no where to sail @x1, <S-NAME> meanders along the waves.",Directions.getInDirectionName(direction)));
				courseDirections.clear();
				return -1;
			}
		}
		return -1;
	}
	
	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			if((!this.anchorDown) && (area != null) && (directionFacing != -1))
			{
				if(sail(directionFacing)!=-1)
				{
					if(this.courseDirections.size()>0)
					{
						final Integer newDir=this.courseDirections.remove(0);
						directionFacing = newDir.intValue();
					}
				}
			}
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
	
	protected boolean okAreaMessage(final CMMsg msg, boolean deckOnly)
	{
		boolean failed = false;
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!deckOnly)||((R.domainType()&Room.INDOORS)==0))
				{
					failed = failed || R.okMessage(R, msg);
					if(failed)
						break;
				}
			}
		}
		return failed;
	}
	
	protected void sendAreaMessage(final CMMsg msg, boolean deckOnly)
	{
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!deckOnly)||((R.domainType()&Room.INDOORS)==0))
					R.sendOthers(msg.source(), msg);
			}
		}
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

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
			transferOwnership((MOB)msg.target(), clanSale);
		}
		else
		if((msg.target() instanceof Room)
		&&(msg.target() == owner()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((CMLib.map().areaLocation(msg.source())==area))
				{
					if(this.anchorDown)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, L("\n\r^HThe anchor on @x1 is lowered, holding her in place.^.^?",name(msg.source())), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
					else
					if(this.directionFacing >= 0)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, L("\n\r^H@x1 is under full sail, traveling @x2^.^?",name(msg.source()), Directions.getDirectionName(directionFacing)), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
				}
				break;
			case CMMsg.TYP_LEAVE:
				if((!msg.source().Name().equals(Name()))
				&&(owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area)
				&&(!getDestinationRoom().isHere(msg.tool())))
					sendAreaMessage(CMClass.getMsg(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, msg.sourceMessage(), msg.targetMessage(), msg.othersMessage()), true);
				break;
			case CMMsg.TYP_ENTER:
			{
				if((!msg.source().Name().equals(Name()))
				&&(owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area)
				&&(!getDestinationRoom().isHere(msg.tool())))
					sendAreaMessage(CMClass.getMsg(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, msg.sourceMessage(), msg.targetMessage(), msg.othersMessage()), true);
				break;
			}
			}
		}
	}

	protected Room findNearestDocks(Room R)
	{
		if(R!=null)
		{
			if(R.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
				return R;
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY)
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
			{
				if(R2.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
					return R2;
			}
			for(final Room R2 : rooms)
			{
				if(R2.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				{
					final Room underWaterR=R.getRoomInDir(Directions.DOWN);
					if((underWaterR!=null)
					&&(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(R.getExitInDir(Directions.DOWN)!=null)
					&&(R.getExitInDir(Directions.DOWN).isOpen()))
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room adjacentR = underWaterR.getRoomInDir(d);
							final Exit adjacentE = underWaterR.getExitInDir(d);
							if((adjacentR!=null)
							&&(adjacentE!=null)
							&&(adjacentE.isOpen())
							&&(R2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
							&&(R2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
							&&(R2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
							&&(R2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
								return adjacentR;
						}
					}
				}
			}
		}
		return null;
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
		final Session session=buyer.session();
		final Room R=CMLib.map().roomLocation(this);
		if(session!=null)
		{
			final GenSailingShip me=this;
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
					||(!CMLib.login().isOkName(this.input.trim(),true)))
					{
						session.println(L("^ZThat is not a permitted name.^N"));
						session.prompt(namer[0].reset());
						return;
					}
					me.renameShip(this.input.trim());
					buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
					final Room finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of water surface.");
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
			final Room finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of SeaDock or water surface.");
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

	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME","RIDEBASIS","MOBSHELD",
											"AREA","OWNER","PRICE","PUTSTR","MOUNTSTR","DISMOUNTSTR","DEFCLOSED","DEFLOCKED",
											"EXITNAME"
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
		case 7: return (area==null)?"":CMLib.coffeeMaker().getAreaXML(area, null, null, null, true).toString();
		case 8: return getOwnerName();
		case 9: return ""+getPrice();
		case 10: return putString;
		case 11: return mountString;
		case 12: return dismountString;
		case 13: return ""+defaultsClosed();
		case 14: return ""+defaultsLocked();
		case 15: return ""+doorName();
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
		case 7: setShipArea(val); break;
		case 8: setOwnerName(val); break;
		case 9: setPrice(CMath.s_int(val)); break;
		case 10: putString=val; break;
		case 11: mountString=val; break;
		case 12: dismountString=val; break;
		case 13: setDoorsNLocks(hasADoor(),isOpen(),CMath.s_bool(val),hasALock(),isLocked(),defaultsLocked()); break;
		case 14: setDoorsNLocks(hasADoor(),isOpen(),defaultsClosed(),hasALock(),isLocked(),CMath.s_bool(val)); break;
		case 15: this.doorName = val; break;
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
		final String[] MYCODES=CMProps.getStatCodesList(GenSailingShip.MYCODES,this);
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
		if(!(E instanceof GenSailingShip))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
