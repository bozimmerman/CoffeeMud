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
   Copyright 2014-2016 Bo Zimmerman

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
public class GenSailingShip extends StdBoardable
{
	@Override
	public String ID()
	{
		return "GenSailingShip";
	}

	protected volatile int		 directionFacing = -1;
	protected volatile boolean	 anchorDown		 = true;
	protected volatile int		 tickDown		 = -1;
	protected final List<Integer>courseDirections= new Vector<Integer>();

	public GenSailingShip()
	{
		super();
		setName("a sailing ship [NEWNAME]");
		setDisplayText("a sailing ship [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(2);
		this.recoverPhyStats();
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
	protected String getAreaClassType()
	{
		return "StdBoardableShip";
	}
	
	@Override
	protected Room createFirstRoom()
	{
		final Room R=CMClass.getLocale("WoodenDeck");
		R.setDisplayText(L("The Deck"));
		return R;
	}
	
	@Override
	public Area getShipArea()
	{
		if((!destroyed)&&(area==null))
		{
			final Area area=super.getShipArea();
			if(area != null)
				area.setTheme(Area.THEME_FANTASY);
			return area;
		}
		return super.getShipArea();
	}

	private enum SailingCommand
	{
		RAISE_ANCHOR,
		LOWER_ANCHOR,
		STEER,
		SAIL,
		COURSE,
		SET_COURSE
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target() == this)
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("AWaterCurrent")))
		{
			if(anchorDown)
				return false;
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=(cmds.size()>1) ? cmds.get(1).toUpperCase() : "";
			SailingCommand cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word);
			if((cmd == null)&&(secondWord.length()>0))
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word+"_"+secondWord);
			if(cmd != null)
			{
				switch(cmd)
				{
				case RAISE_ANCHOR:
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
						CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> raise(s) anchor."));
						if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
						{
							R.send(msg.source(), msg2);
							this.sendAreaMessage(msg2, true);
							anchorDown=false;
						}
					}
					return false;
				}
				case LOWER_ANCHOR:
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
						CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> lower(s) anchor."));
						if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
						{
							R.send(msg.source(), msg2);
							this.sendAreaMessage(msg2, true);
							anchorDown=true;
						}
					}
					return false;
				}
				case STEER:
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
				case SAIL:
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
					break;
				}
				case COURSE:
				case SET_COURSE:
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
				}
			}
			if(cmd != null)
			{
				cmds.add(0, "METAMSGCOMMAND");
				double speed = (phyStats().ability()<=0) ? 1.0 : (double)phyStats().ability();
				speed = msg.source().phyStats().speed() / speed;
				msg.source().enqueCommand(cmds, MUDCmdProcessor.METAFLAG_ASMESSAGE, speed);
				return false;
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_COMMAND)
		&&(msg.sourceMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			final List<String> cmds=CMParms.parse(msg.sourceMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=(cmds.size()>1) ? cmds.get(1).toUpperCase() : "";
			SailingCommand cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word);
			if((cmd == null)&&(secondWord.length()>0))
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word+"_"+secondWord);
			if(cmd == null)
				return true;
			switch(cmd)
			{
			case SAIL:
			{
				int dir=Directions.getCompassDirectionCode(secondWord);
				if(dir<0)
					return false;
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
					return false;
				this.courseDirections.clear(); // sail eliminates a course
				this.courseDirections.add(Integer.valueOf(-1));
				this.sail(msg.source(), R, dir);
				return false;
			}
			default:
				// already done...
				return false;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_SIT)
		&&(msg.target()==this)
		&&(msg.source().location()==owner())
		&&(CMLib.flags().isWateryRoom(msg.source().location()))
		&&(!CMLib.flags().isClimbing(msg.source()))
		&&(!CMLib.flags().isFlying(msg.source()))
		&&(!CMLib.law().doesHavePriviledgesHere(msg.source(), super.getDestinationRoom())))
		{
			if(msg.source().riding() != null)
				msg.source().tell(CMLib.lang().L("You'll need some assistance to board a ship from @x1.",msg.source().riding().name(msg.source())));
			else
				msg.source().tell(CMLib.lang().L("You'll need some assistance to board a ship from the water."));
			return false;
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_SPECIALCOMBAT)
		{
			
		}
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			if((!this.anchorDown) && (area != null) && (directionFacing != -1) && (--tickDown <=0))
			{
				int speed=phyStats().ability();
				if(speed <= 0)
					speed=1;
				tickDown = -phyStats().ability();
				
				for(int s=0;s<speed;s++)
				{
					if(sail(directionFacing)!=-1)
					{
						if(this.courseDirections.size()>0)
						{
							final Integer newDir=this.courseDirections.remove(0);
							directionFacing = newDir.intValue();
						}
					}
					else
					{
						directionFacing=-1;
						break;
					}
				}
			}
			return true;
		}
		return super.tick(ticking, tickID);
	}
	

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

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
					if((this.directionFacing >= 0)&&(this.courseDirections.size()>0))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, L("\n\r^H@x1 is under full sail, traveling @x2^.^?",name(msg.source()), Directions.getDirectionName(directionFacing)), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
				}
				break;
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
				if((!msg.source().Name().equals(Name()))
				&&(owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area)
				&&(!getDestinationRoom().isHere(msg.tool())))
					sendAreaMessage(CMClass.getMsg(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, null, null, L("^HOff the deck you see: ^N")+msg.othersMessage()), true);
				break;
			}
		}
	}

	@Override
	protected Room findNearestDocks(Room R)
	{
		if(R!=null)
		{
			if(R.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
				return R;
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
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
				if(CMLib.flags().isWaterySurfaceRoom(R2))
				{
					final Room underWaterR=R2.getRoomInDir(Directions.DOWN);
					if((underWaterR!=null)
					&&(CMLib.flags().isUnderWateryRoom(underWaterR))
					&&(R.getExitInDir(Directions.DOWN)!=null)
					&&(R.getExitInDir(Directions.DOWN).isOpen()))
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room adjacentR = R2.getRoomInDir(d);
							final Exit adjacentE = R2.getExitInDir(d);
							if((adjacentR!=null)
							&&(adjacentE!=null)
							&&(adjacentE.isOpen())
							&&(!CMLib.flags().isWateryRoom(adjacentR)))
								return adjacentR;
						}
					}
				}
			}
		}
		return null;
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
				mob.setRiding(this);
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
				mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
				try
				{
					this.directionFacing = -1;
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
						if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=1)&&(CMLib.flags().canBeSeenBy(targetR,mob)))
						{
							if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=2)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
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
	
	protected boolean sail(final MOB mob, final Room R, final int dir)
	{
		CMMsg msg2=CMClass.getMsg(mob, R, R.getExitInDir(dir), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> sail(s) @x1 @x2.",name(mob),Directions.getDirectionName(dir)));
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
			if((R2==null)&&(R!=null)&&(R.getArea()!=null))
				R2=findOceanRoom(R.getArea());
			if(R2==null)
			{
				for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					R2=findOceanRoom(a.nextElement());
					if(R2!=null)
						break;
				}
			}
			if(R2==null)
				return false;
			R2.moveItemTo(this);
			return true;
		}
		return false;
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
			return CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		case 8:
			return getOwnerName();
		case 9:
			return "" + getPrice();
		case 10:
			return putString;
		case 11:
			return mountString;
		case 12:
			return dismountString;
		case 13:
			return "" + defaultsClosed();
		case 14:
			return "" + defaultsLocked();
		case 15:
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
				setShipArea(val);
				break;
			case 8:
				setOwnerName(val);
				break;
			case 9:
				setPrice(CMath.s_int(val));
				break;
			case 10:
				putString = val;
				break;
			case 11:
				mountString = val;
				break;
			case 12:
				dismountString = val;
				break;
			case 13:
				setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
				break;
			case 14:
				setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
				break;
			case 15:
				this.doorName = val;
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
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}
	
	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenSailingShip.MYCODES,this);
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
		if(!(E instanceof GenSailingShip))
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
