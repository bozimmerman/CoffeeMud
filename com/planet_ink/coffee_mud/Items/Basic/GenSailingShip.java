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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
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

	protected volatile int		 coarseDirection = -1;
	protected volatile boolean	 anchorDown		 = true;
	protected volatile int		 tickDown		 = -1;
	protected final List<Integer>courseDirections= new Vector<Integer>();

	protected volatile long			roomEntryTime	 = System.currentTimeMillis();
	protected volatile int		 	directionFacing	 = 0;
	protected volatile Item			targetedShip	 = null;
	protected volatile Room		 	shipCombatRoom	 = null;
	protected PairList<Item,int[]>	coordinates		 = null;
	
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
	
	protected boolean mayIAttack(final MOB mob, final Item item)
	{
		if((item instanceof BoardableShip) && (item instanceof PrivateProperty))
		{
			final PrivateProperty privop=(PrivateProperty)item;
			// is this how we determine npc ships?
			if((privop.getOwnerName() == null)||(privop.getOwnerName().length()==0)||(privop.getOwnerObject()==null))
				return true;
			if(((getOwnerName() == null)||(getOwnerName().length()==0)) && (mob.isMonster()))
				return true;
			if(CMSecurity.isASysOp(mob) && mob.isAttributeSet(Attrib.PLAYERKILL))
				return true;
			CMObject enemyOwner = privop.getOwnerObject();
			if(enemyOwner instanceof MOB)
				return mob.mayIFight((MOB)enemyOwner);
			else
			if(enemyOwner instanceof Clan)
			{
				enemyOwner = ((Clan)enemyOwner).getResponsibleMember();
				if(enemyOwner != null)
				{
					//TODO: make sure the ship is populated .. just because it belongs
					// to a warring clan doesn't mean you can just sink it.
					return mob.mayIFight((MOB)enemyOwner);
				}
			}
		}
		return false;
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
					this.coarseDirection=-1;
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
					if(!this.amInTacticalMode())
					{
						final Room targetRoom=R.getRoomInDir(dir);
						final Exit targetExit=R.getExitInDir(dir);
						if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
						{
							msg.source().tell(L("There doesn't look to be anything in that direction."));
							return false;
						}
						steer(msg.source(),R, dir);
					}
					//TODO: make a tactical STEER that adds to the coarse list for this tick
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
					if(!this.amInTacticalMode())
					{
						final Room targetRoom=R.getRoomInDir(dir);
						final Exit targetExit=R.getExitInDir(dir);
						if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
						{
							msg.source().tell(L("There doesn't look to be anything in that direction."));
							return false;
						}
					}
					//TODO: make a tactical sail that's different.
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
					this.coarseDirection=-1;
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
					if(!this.amInTacticalMode())
					{
						final Room targetRoom=R.getRoomInDir(firstDir);
						final Exit targetExit=R.getExitInDir(firstDir);
						if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
						{
							msg.source().tell(L("There doesn't look to be anything in that direction."));
							return false;
						}
						steer(msg.source(),R, firstDir);
					}
					//TODO: make a tactical course that's different.
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
		else
		if((msg.targetMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			if(Character.toUpperCase(msg.targetMessage().charAt(0))=='A')
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				String cmd=parsedFail.get(0).toUpperCase();
				if(("ATTACK".startsWith(cmd))&&(owner() instanceof Room))
				{
					final Room thisRoom = (Room)owner();
					String rest = CMParms.combine(parsedFail,1);
					final Item I=thisRoom.findItem(rest);
					if((I instanceof BoardableShip)&&(I!=this))
					{
						if(!this.mayIAttack(msg.source(), I))
						{
							msg.source().tell(L("You are not permitted to attack @x1",I.name()));
							return false;
						}
						final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
						try
						{
							mob.setRiding(this);
							mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
							mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
							final CMMsg maneuverMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,L("<S-NAME> engage(s) @x1.",I.Name()));
							if(thisRoom.okMessage(mob, maneuverMsg))
							{
								thisRoom.send(mob, maneuverMsg);
								targetedShip	 = I;
								shipCombatRoom	 = thisRoom;
								if(I instanceof GenSailingShip)
								{
									final GenSailingShip otherI=(GenSailingShip)I;
									if(otherI.targetedShip == null)
										otherI.targetedShip = this;
									otherI.shipCombatRoom = thisRoom;
									otherI.amInTacticalMode(); // now he is in combat
								}
								amInTacticalMode(); // now he is in combat
								//also support ENGAGE <shipname> as an alternative to attack?
							}
							return false;
						}
						finally
						{
							mob.destroy();
						}
					}
				}
			}
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	protected int[] getMagicCoords()
	{
		//TODO: these suck because, on surprise attack, all ships might be in the same square.
		final int[] coords;
		final int middle = (int)Math.round(Math.floor(phyStats().weight() / 2.0));
		final int extreme = phyStats().weight()-1;
		switch(this.directionFacing)
		{
		case Directions.NORTH:
			coords = new int[] {middle,extreme};
			break;
		case Directions.SOUTH:
			coords = new int[] {middle,0};
			break;
		case Directions.EAST:
			coords = new int[] {0,middle};
			break;
		case Directions.WEST:
			coords = new int[] {extreme,middle};
			break;
		case Directions.UP:
			coords = new int[] {middle,middle};
			break;
		case Directions.DOWN:
			coords = new int[] {middle,middle};
			break;
		case Directions.NORTHEAST:
			coords = new int[] {0,extreme};
			break;
		case Directions.NORTHWEST:
			coords = new int[] {extreme,extreme};
			break;
		case Directions.SOUTHEAST:
			coords = new int[] {extreme,0};
			break;
		case Directions.SOUTHWEST:
			coords = new int[] {0,0};
			break;
		default:
			coords = new int[] {middle,middle};
			break;
		}
		return coords;
	}

	protected synchronized void clearTacticalMode()
	{
		final Room shipCombatRoom = this.shipCombatRoom;
		if(shipCombatRoom != null)
		{
			synchronized((""+shipCombatRoom + "_SHIP_TACTICAL").intern())
			{
				PairList<Item,int[]> coords = this.coordinates;
				if(coords != null)
				{
					coords.removeFirst(this);
				}
			}
		}
		this.targetedShip = null;
		this.shipCombatRoom = null;
		this.coordinates = null;
	}
	
	protected synchronized boolean amInTacticalMode()
	{
		final Item targetedShip = this.targetedShip;
		final Room shipCombatRoom = this.shipCombatRoom;
		if((targetedShip != null) 
		&& (shipCombatRoom != null)
		&& (shipCombatRoom.isContent(targetedShip))
		&& (shipCombatRoom.isContent(this))
		)
		{
			if(coordinates == null)
			{
				synchronized((""+shipCombatRoom + "_SHIP_TACTICAL").intern())
				{
					for(int i=0;i<shipCombatRoom.numItems();i++)
					{
						final Item I=shipCombatRoom.getItem(i);
						if((I instanceof GenSailingShip)
						&&(((GenSailingShip)I).coordinates != null))
						{
							this.coordinates = ((GenSailingShip)I).coordinates; 
						}
					}
					if(coordinates == null)
					{
						this.coordinates = new SPairList<Item,int[]>();
					}
				}
				final PairList<Item,int[]> coords = this.coordinates;
				if(coords != null)
				{
					if(!coords.containsFirst(this))
					{
						coords.add(new Pair<Item,int[]>(this,this.getMagicCoords()));
					}
				}
			}
			return true;
		}
		else
		{
			this.targetedShip = null;
			this.shipCombatRoom = null;
			this.coordinates = null;
			return false;
		}
	}
	
	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final int sailingTickID = amInTacticalMode() ? Tickable.TICKID_SPECIALCOMBAT : Tickable.TICKID_AREA;
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
		}
		if(tickID == sailingTickID)
		{
			if((!this.anchorDown) && (area != null) && (coarseDirection != -1) && (--tickDown <=0))
			{
				int speed=phyStats().ability();
				if(speed <= 0)
					speed=1;
				tickDown = -phyStats().ability();
				
				for(int s=0;s<speed && (coarseDirection>=0);s++)
				{
					switch(sail(coarseDirection))
					{
					case CANCEL:
					{
						coarseDirection=-1;
						break;
					}
					case CONTINUE:
					{
						if(this.courseDirections.size()>0)
						{
							final Integer newDir=this.courseDirections.remove(0);
							coarseDirection = newDir.intValue();
						}
						else
						{
							coarseDirection = -1;
						}
						break;
					}
					case REPEAT:
					{
						break;
					}
					}
				}
			}
			if(this.amInTacticalMode())
			{
				//TODO: iterate through weapons, see if they hit, send damage messages to the
				// other sip
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
					if((this.coarseDirection >= 0)&&(this.courseDirections.size()>0))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, L("\n\r^H@x1 is under full sail, traveling @x2^.^?",name(msg.source()), Directions.getDirectionName(coarseDirection)), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
				}
				break;
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
				if((owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area))
				{
					if((msg.source().riding() instanceof GenSailingShip)
					&&(msg.source().Name().equals(msg.source().riding().Name())))
					{
						roomEntryTime = System.currentTimeMillis();
						clearTacticalMode();
					}
					if((!msg.source().Name().equals(Name()))
					&&(!getDestinationRoom().isHere(msg.tool()))) // whats this second condition for?
					{
						sendAreaMessage(CMClass.getMsg(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, null, null, L("^HOff the deck you see: ^N")+msg.othersMessage()), true);
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				//TODO: take some damage
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

	private static enum SailResult
	{
		CANCEL,
		CONTINUE,
		REPEAT
	}
	
	protected int[] getCoordAdjustments(int[] newOnes)
	{
		final PairList<Item,int[]> coords = this.coordinates;
		int[] lowests = new int[2];
		if(coords != null)
		{
			if(newOnes != null)
			{
				if(newOnes[0] < lowests[0])
					lowests[0]=newOnes[0];
				if(newOnes[1] < lowests[1])
					lowests[1]=newOnes[1];
			}
			for(int p=0;p<coords.size();p++)
			{
				try
				{
					Pair<Item,int[]> P = coords.get(p);
					if((newOnes==null)||(P.first!=this))
					{
						if(P.second[0] < lowests[0])
							lowests[0]=P.second[0];
						if(P.second[1] < lowests[1])
							lowests[1]=P.second[1];
					}
				}
				catch(Exception e)
				{
				}
			}
		}
		lowests[0]=-lowests[0];
		lowests[1]=-lowests[1];
		return lowests;
	}
	
	protected int getTacticalDistance(int[] fromCoords)
	{
		final PairList<Item,int[]> coords = this.coordinates;
		int lowest = phyStats().weight();
		if(coords != null)
		{
			for(int p=0;p<coords.size();p++)
			{
				try
				{
					Pair<Item,int[]> P = coords.get(p);
					if((P.second != fromCoords)
					&&(this.shipCombatRoom != null)
					&&(this.shipCombatRoom.isHere(P.first))
					&&(P.first instanceof GenSailingShip)
					&&(((GenSailingShip)P.first).targetedShip == this))
					{
						int distance = (int)Math.round(Math.ceil(Math.sqrt(Math.pow(P.second[0]-fromCoords[0],2.0) + Math.pow(P.second[1]-fromCoords[1],2.0))));
						if(distance < lowest)
							lowest=distance;
					}
				}
				catch(Exception e)
				{
				}
			}
		}
		return lowest;
	}
	
	protected SailResult sail(final int direction)
	{
		final Room thisRoom=CMLib.map().roomLocation(this);
		if(thisRoom != null)
		{
			if(directionFacing < 0)
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=thisRoom.getRoomInDir(d);
					if((R2!=null)
					&&(thisRoom.getExitInDir(d)!=null)
					&&(thisRoom.getExitInDir(d).isOpen())
					&&(!CMLib.flags().isWateryRoom(R2)))
					{
						directionFacing = Directions.getOpDirectionCode(d);
						break;
					}
				}
				if(directionFacing < 0)
					directionFacing = direction;
			}
			if(direction != directionFacing)
			{
				directionFacing = Directions.getGradualDirectionCode(directionFacing, direction);
			}
			int[] tacticalCoords = null;
			if(amInTacticalMode())
			{
				int x=0;
				try
				{
					while((x>=0)&&(this.coordinates!=null)&&(tacticalCoords==null))
					{
						x=this.coordinates.indexOfFirst(this);
						Pair<Item,int[]> pair = (x>=0) ? this.coordinates.get(x) : null;
						if(pair == null)
							break;
						else
						if(pair.first != this)
							x=this.coordinates.indexOfFirst(this);
						else
							tacticalCoords = pair.second;
					}
				}
				catch(Exception e)
				{
				}
			}
			if(tacticalCoords != null)
			{
				final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
				try
				{
					mob.setRiding(this);
					mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
					mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
					final String directionName = Directions.getDirectionName(direction);
					if(directionFacing == direction)
					{
						final int oldDistance = this.getTacticalDistance(tacticalCoords);
						final int[] newCoords = Arrays.copyOf(tacticalCoords, 2);
						final int newDistance = this.getTacticalDistance(tacticalCoords);
						if((newDistance <= oldDistance)||(newDistance < phyStats.weight()))
						{
							final int[] adj=this.getCoordAdjustments(newCoords);
							final String coords = (newCoords[0]+adj[0])+","+(newCoords[1]+adj[1]);
							final CMMsg maneuverMsg=CMClass.getMsg(mob,thisRoom,null,CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,directionName,CMMsg.MSG_ADVANCE,L("<S-NAME> maneuver(s) @x1 to (@x2).",directionName,coords));
							if(thisRoom.okMessage(mob, maneuverMsg))
							{
								thisRoom.send(mob, maneuverMsg);
								tacticalCoords[0] = newCoords[0];
								tacticalCoords[1] = newCoords[1];
								return SailResult.CONTINUE;
							}
							return SailResult.REPEAT;
						}
						// else we get to make a real Sailing move!
					}
					else
					{
						final CMMsg maneuverMsg=CMClass.getMsg(mob,thisRoom,null,CMMsg.MSG_ADVANCE,directionName,CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,L("<S-NAME> change(s) coarse, turning @x1.",directionName));
						if(thisRoom.okMessage(mob, maneuverMsg))
							thisRoom.send(mob, maneuverMsg);
						return SailResult.REPEAT;
					}
				}
				finally
				{
					mob.destroy();
				}
			}
			else
			{
				directionFacing = direction;
			}
			final Room destRoom=thisRoom.getRoomInDir(direction);
			final Exit exit=thisRoom.getExitInDir(direction);
			if((destRoom!=null)&&(exit!=null))
			{
				if((destRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
				&&(destRoom.domainType()!=Room.DOMAIN_OUTDOORS_SEAPORT))
				{
					announceToShip(L("As there is no where to sail @x1, <S-NAME> meanders along the waves.",Directions.getInDirectionName(direction)));
					courseDirections.clear();
					return SailResult.CANCEL;
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
					this.coarseDirection = -1;
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
						return SailResult.CONTINUE;
					}
					else
					{
						announceToShip(L("<S-NAME> can not seem to travel @x1.",Directions.getInDirectionName(direction)));
						courseDirections.clear();
						return SailResult.CANCEL;
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
				return SailResult.CANCEL;
			}
		}
		return SailResult.CANCEL;
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
		directionFacing = dir;
		CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> change(s) course, steering @x1 @x2.",name(mob),Directions.getDirectionName(dir)));
		if((R.okMessage(mob, msg2) && this.okAreaMessage(msg2, true)))
		{
			R.send(mob, msg2); // this lets the source know, i guess
			this.sendAreaMessage(msg2, true); // this just sends to "others"
			this.coarseDirection=dir;
			return true;
		}
		return false;
	}
	
	protected boolean sail(final MOB mob, final Room R, final int dir)
	{
		directionFacing = dir;
		CMMsg msg2=CMClass.getMsg(mob, R, R.getExitInDir(dir), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> sail(s) @x1 @x2.",name(mob),Directions.getDirectionName(dir)));
		if((R.okMessage(mob, msg2) && this.okAreaMessage(msg2, true)))
		{
			R.send(mob, msg2); // this lets the source know, i guess
			this.sendAreaMessage(msg2, true); // this just sends to "others"
			this.coarseDirection=dir;
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
	
	@Override
	public boolean isInCombat()
	{
		return (targetedShip != null)
			&& (shipCombatRoom != null);
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
