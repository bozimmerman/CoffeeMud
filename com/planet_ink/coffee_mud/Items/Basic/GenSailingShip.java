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
   Copyright 2014-2018 Bo Zimmerman

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
public class GenSailingShip extends StdBoardable implements SailingShip
{
	@Override
	public String ID()
	{
		return "GenSailingShip";
	}

	protected volatile int				courseDirection		= -1;
	protected volatile boolean			anchorDown			= true;
	protected final List<Integer>		courseDirections	= new Vector<Integer>();
	protected volatile int				directionFacing		= 0;
	protected volatile int				ticksSinceMove		= 0;
	protected volatile PhysicalAgent	targetedShip		= null;
	protected volatile Item				tenderShip			= null;
	protected volatile Room				shipCombatRoom		= null;
	protected PairList<Item, int[]>		coordinates			= null;
	protected PairList<Weapon, int[]>	aimings				= new PairVector<Weapon, int[]>();
	protected List<Item>				smallTenderRequests	= new SLinkedList<Item>();

	protected int					maxHullPoints	= -1;
	protected volatile int			lastSpamCt		= 0;
	protected volatile String		lastSpamMsg		= "";

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
		else
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
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
		WEIGH_ANCHOR,
		LOWER_ANCHOR,
		STEER,
		SAIL,
		COURSE,
		SET_COURSE,
		TARGET,
		AIM,
		SINK,
		TENDER,
		RAISE,
		LOWER
		;
	}

	protected void announceToDeck(final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(null, CMMsg.MSG_OK_ACTION, msgStr);
		announceToDeck(msg);
	}

	@Override
	public int getShipSpeed()
	{
		int speed=phyStats().ability();
		if(subjectToWearAndTear())
		{
			if(usesRemaining()<10)
				return 0;
			speed=(int)Math.round(speed * CMath.div(usesRemaining(), 100));
		}
		if(speed <= 0)
			return 1;
		return speed;
	}

	protected void announceToDeck(final CMMsg msg)
	{
		MOB mob = null;
		final MOB msgSrc = msg.source();
		try
		{
			final Area A=this.getShipArea();
			if(A!=null)
			{
				Room mobR = null;
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null) && ((R.domainType()&Room.INDOORS)==0))
					{
						mobR=R;
						break;
					}
				}
				if(mobR!=null)
				{
					mob = CMClass.getFactoryMOB(name(),phyStats().level(),mobR);
					msg.setSource(mob);
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if((R!=null) && ((R.domainType()&Room.INDOORS)==0) && (R.okMessage(mob, msg)))
						{
							if(R == mobR)
								R.send(mob, msg); // this lets the source know, i guess
							else
								R.sendOthers(mob, msg); // this lets the source know, i guess
						}
					}
				}
			}
		}
		finally
		{
			msg.setSource(msgSrc);
			if(mob != null)
				mob.destroy();
		}
	}

	protected void announceActionToDeckOrUnderdeck(final MOB mob, final CMMsg msg, int INDOORS)
	{
		final Area A=this.getShipArea();
		final Room mobR=CMLib.map().roomLocation(mob);
		if(A!=null)
		{
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((R!=null) && ((R.domainType()&Room.INDOORS)==INDOORS) && (R.okMessage(mob, msg)))
				{
					if(R == mobR)
						R.send(mob, msg); // this lets the source know, i guess
					else
						R.sendOthers(mob, msg); // this lets the source know, i guess
				}
			}
		}
	}

	protected void announceActionToDeck(final MOB mob, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
		announceActionToDeckOrUnderdeck(mob,msg, 0);
	}

	protected void announceActionToDeck(final MOB mob, final Environmental target, final Environmental tool, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, target, tool, CMMsg.MSG_OK_ACTION, msgStr);
		announceActionToDeckOrUnderdeck(mob,msg, 0);
	}

	protected void announceActionToUnderDeck(final MOB mob, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
		announceActionToDeckOrUnderdeck(mob,msg, Room.INDOORS);
	}

	protected String getTargetedShipInfo()
	{
		final PhysicalAgent targetedShip = this.targetedShip;
		return getOtherShipInfo(targetedShip);
	}

	protected String getOtherShipInfo(PhysicalAgent targetedShip)
	{
		if((targetedShip != null)&&(targetedShip instanceof GenSailingShip))
		{
			final GenSailingShip targetShip = (GenSailingShip)targetedShip;
			final int[] targetCoords = targetShip.getMyCoords();
			final int[] myCoords = this.getMyCoords();
			if((myCoords!=null)&&(targetCoords != null))
			{
				final String dist = ""+this.getTacticalDistance(targetShip);
				final String dir=CMLib.directions().getDirectionName(targetShip.directionFacing);
				final String speed=""+targetShip.getShipSpeed();
				final String dirFromYou = CMLib.directions().getDirectionName(Directions.getRelative11Directions(myCoords, targetCoords));
				return L("@x1 is @x2 of you sailing @x3 at a speed of @x4 and a distance of @x5.",targetShip.name(),dirFromYou,dir,speed,dist);
			}
		}
		return "";
	}

	protected int getDirectionToTarget(PhysicalAgent targetedShip)
	{
		if((targetedShip != null)&&(targetedShip instanceof GenSailingShip))
		{
			final GenSailingShip targetShip = (GenSailingShip)targetedShip;
			final int[] targetCoords = targetShip.getMyCoords();
			final int[] myCoords = this.getMyCoords();
			if((myCoords!=null)&&(targetCoords != null))
				return Directions.getRelative11Directions(myCoords, targetCoords);
		}
		return -1;
	}

	protected String getDirectionStrToTarget(PhysicalAgent targetedShip)
	{
		if((targetedShip != null)&&(targetedShip instanceof GenSailingShip))
		{
			final GenSailingShip targetShip = (GenSailingShip)targetedShip;
			final int[] targetCoords = targetShip.getMyCoords();
			final int[] myCoords = this.getMyCoords();
			if((myCoords!=null)&&(targetCoords != null))
				return CMLib.directions().getDirectionName(Directions.getRelative11Directions(myCoords, targetCoords));
		}
		return "";
	}

	protected Room getRandomDeckRoom()
	{
		final Area A=this.getShipArea();
		if(A!=null)
		{
			List<Room> deckRooms=new ArrayList<Room>(2);
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((R!=null) 
				&& ((R.domainType()&Room.INDOORS)==0)
				&& (R.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
					deckRooms.add(R);
			}
			if(deckRooms.size()>0)
			{
				return deckRooms.get(CMLib.dice().roll(1, deckRooms.size(), -1));
			}
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_HUH:
		case CMMsg.TYP_COMMANDFAIL:
		case CMMsg.TYP_COMMAND:
			break;
		default:
			if(!confirmAreaMessage(msg, true))
				return false;
			break;
		}
		
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
			SailingCommand cmd=null;
			if(secondWord.length()>0)
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word+"_"+secondWord);
			if(cmd == null)
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word);
			if(cmd != null)
			{
				switch(cmd)
				{
				case TARGET:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another ship to target."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This ship is nowhere to be found!"));
						return false;
					}
					String rest = CMParms.combine(cmds,1);
					Boolean result = startAttack(msg.source(),thisRoom,rest);
					if(result  == Boolean.TRUE)
					{
						if(this.targetedShip != null)
							msg.source().tell(L("You are now targeting @x1.",this.targetedShip.Name()));
						msg.source().tell(getTargetedShipInfo());
					}
					else
					if(result  == Boolean.FALSE)
						return false;
					else
					{
						msg.source().tell(L("You don't see '@x1' here to target",rest));
						return false;
					}
					break;
				}
				case TENDER:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another ship to offer aboard."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This ship is nowhere to be found!"));
						return false;
					}
					if(this.targetedShip!=null)
					{
						msg.source().tell(L("Not while you are in combat!"));
						return false;
					}
					String rest = CMParms.combine(cmds,1);
					final Item I=thisRoom.findItem(rest);
					if((I instanceof GenSailingShip)&&(I!=this)&&(CMLib.flags().canBeSeenBy(I, msg.source())))
					{
						GenSailingShip otherShip = (GenSailingShip)I;
						if(otherShip.targetedShip != null)
						{
							msg.source().tell(L("Not while @x1 is in in combat!",otherShip.Name()));
							return false;
						}
						final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
						try
						{
							mob.setRiding(this);
							mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
							mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
							if(otherShip.tenderShip == this)
							{
								if(thisRoom.show(mob, otherShip, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> connect(s) her gangplank with <T-NAME>")))
								{
									this.tenderShip = otherShip;
									final BoardableShip myArea=(BoardableShip)this.getShipArea();
									final BoardableShip hisArea=(BoardableShip)otherShip.getShipArea();
									
									final Room hisExitRoom = hisArea.unDock(false);
									final Room myExitRoom = myArea.unDock(false);
									myArea.dockHere(hisExitRoom);
									hisArea.dockHere(myExitRoom);
								}
							}
							else
							{
								if(thisRoom.show(mob, otherShip, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> extend(s) her gangplank toward(s) <T-NAME>")))
									this.tenderShip = otherShip;
							}
						}
						finally
						{
							mob.destroy();
						}
					}
					else
					{
						msg.source().tell(L("You don't see the ship '@x1' here to board",rest));
						return false;
					}
					break;
				}
				case SINK:
				{
					if(!CMSecurity.isAllowedEverywhere(msg.source(), CMSecurity.SecFlag.CMDROOMS))
						return true;
					CMMsg damageMsg=CMClass.getMsg(msg.source(), this, CMMsg.MSG_DAMAGE, "SINK!!!");
					damageMsg.setValue(99999);
					this.executeMsg(this, damageMsg);
					return false;
				}
				case AIM:
				{
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This ship is nowhere to be found!"));
						return false;
					}
					if((!this.amInTacticalMode())
					||(this.targetedShip==null)
					||(!thisRoom.isContent((Item)this.targetedShip)))
					{
						msg.source().tell(L("You ship must be targeting an enemy to aim weapons."));
						return false;
					}
					if(cmds.size()<3)
					{
						msg.source().tell(L("Aim what weapon how far ahead?"));
						msg.source().tell(getTargetedShipInfo());
						return false;
					}
					String leadStr = cmds.remove(cmds.size()-1);
					String weaponStr = CMParms.combine(cmds,1);
					final Room mobR=msg.source().location();
					if((!CMath.isInteger(leadStr))||(CMath.s_int(leadStr)<0))
					{
						if(this.targetedShip!=null)
							msg.source().tell(L("'@x1' is not a valid distance ahead of @x2 to fire.",leadStr,this.targetedShip.name()));
						else
							msg.source().tell(L("'@x1' is not a valid distance.",leadStr));
						return false;
					}
					if(mobR!=null)
					{
						Item I=mobR.findItem(null, weaponStr);
						if((I==null)||(!CMLib.flags().canBeSeenBy(I,msg.source())))
						{
							msg.source().tell(L("You don't see any siege weapon called '@x1' here.",leadStr));
							return false;
						}
						if(!CMLib.combat().isAShipSiegeWeapon(I))
						{
							msg.source().tell(L("@x1 is not a useable siege weapon.",leadStr));
							return false;
						}
						AmmunitionWeapon weapon=(AmmunitionWeapon)I;
						int distance = weapon.maxRange();
						int[] targetCoords = new int[2];
						int leadAmt=0;
						if(this.targetedShip instanceof GenSailingShip)
						{
							targetCoords = ((GenSailingShip)this.targetedShip).getMyCoords();
							int direction = ((GenSailingShip)this.targetedShip).directionFacing;
							if(targetCoords == null)
							{
								msg.source().tell(L("You ship must be targeting an enemy to aim weapons."));
								return false;
							}
							distance = this.getTacticalDistance(this.targetedShip);
							leadAmt = CMath.s_int(leadStr);
							for(int i=0;i<leadAmt;i++)
								targetCoords = Directions.adjustXYByDirections(targetCoords[0], targetCoords[1], direction);
						}
						if((weapon.maxRange() < distance)||(weapon.minRange() > distance))
						{
							msg.source().tell(L("Your target is presently at distance of @x1, but this weapons range is @x2 to @x3.",
												""+distance,""+weapon.minRange(),""+weapon.maxRange()));
							return false;
						}
						if(weapon.requiresAmmunition() 
						&& (weapon.ammunitionCapacity() > 0)
						&& (weapon.ammunitionRemaining() == 0))
						{
							msg.source().tell(L("@x1 needs to be LOADed first.",weapon.Name()));
							return false;
						}
						String timeToFire=""+(CMLib.threads().msToNextTick((Tickable)CMLib.combat(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK) / 1000);
						String msgStr=L("<S-NAME> aim(s) <O-NAME> at <T-NAME> (@x1).",""+leadAmt);
						if(msg.source().isMonster() && aimings.contains(weapon))
						{
							msg.source().tell(L("@x1 is already aimed.",weapon.Name()));
							return false;
						}
						CMMsg msg2=CMClass.getMsg(msg.source(), targetedShip, weapon, CMMsg.MSG_NOISYMOVEMENT, msgStr);
						if(mobR.okMessage(msg.source(), msg2))
						{
							this.aimings.removeFirst(weapon);
							this.aimings.add(new Pair<Weapon,int[]>(weapon,targetCoords));
							mobR.send(msg.source(), msg2);
							msg.source().tell(L("@x1 is now aimed and will be fired in @x2 seconds.",I.name(),timeToFire));
						}
					}
					return false;
				}
				case RAISE:
				{
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					final Room R=CMLib.map().roomLocation(this);
					final Room targetR=msg.source().location();
					if((R!=null)&&(targetR!=null))
					{
						if(((targetR.domainType()&Room.INDOORS)==0)
						&& (targetR.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
						&&(msg.source().isPlayer()))
						{
							msg.source().tell(L("You must be on deck to raise a tendered ship."));
							return false;
						}
						String rest = CMParms.combine(cmds,1);
						final Item I=R.findItem(rest);
						if((I!=this)&&(CMLib.flags().canBeSeenBy(I, msg.source())))
						{
							if((I instanceof Rideable)
							&&(((Rideable)I).mobileRideBasis()))
							{
								if(smallTenderRequests.contains(I))
								{
									final MOB riderM=getBestRider(R,(Rideable)I);
									if(((riderM==null)||(R.show(riderM, R, CMMsg.MSG_LEAVE, null)))
									&&(R.show(msg.source(), I, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> raise(s) <T-NAME> up onto @x1.",name())))
									&&((riderM==null)||(R.show(riderM, targetR, CMMsg.MSG_ENTER, null))))
									{
										smallTenderRequests.remove(I);
										targetR.moveItemTo(I, Expire.Never, Move.Followers);
									}
									return false;
								}
								else
								{
									msg.source().tell(L("You can only raise @x1 once it has tendered itself to this one.",I.name()));
									return false;
								}
							}
							else
							{
								msg.source().tell(L("You don't think @x1 is a suitable boat.",I.name()));
								return false;
							}
						}
						else
						{
							msg.source().tell(L("You don't see '@x1' out there!",rest));
							return false;
						}
					}
					break;
				}
				case LOWER:
				{
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					final Room R=msg.source().location();
					final Room targetR=CMLib.map().roomLocation(this);
					if((R!=null)&&(targetR!=null))
					{
						if(((R.domainType()&Room.INDOORS)==0)
						&& (R.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
						&&(msg.source().isPlayer()))
						{
							msg.source().tell(L("You must be on deck to lower a boat."));
							return false;
						}
						String rest = CMParms.combine(cmds,1);
						final Item I=R.findItem(rest);
						if((I!=this)&&(CMLib.flags().canBeSeenBy(I, msg.source())))
						{
							if((I instanceof Rideable)
							&&(((Rideable)I).mobileRideBasis())
							&&((((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER)
								||(((Rideable)I).rideBasis()==Rideable.RIDEABLE_AIR)))
							{
								final MOB riderM=getBestRider(R,(Rideable)I);
								if(((riderM==null)||(R.show(riderM, R, CMMsg.MSG_LEAVE, null)))
								&&(targetR.show(msg.source(), I, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> lower(s) <T-NAME> off of @x1.",name())))
								&&((riderM==null)||(R.show(riderM, targetR, CMMsg.MSG_ENTER, null))))
								{
									this.smallTenderRequests.remove(I);
									targetR.moveItemTo(I, Expire.Never, Move.Followers);
								}
								return false;
							}
							else
							{
								msg.source().tell(L("You don't think @x1 is a suitable thing for lowering.",I.name()));
								return false;
							}
						}
						else
						{
							msg.source().tell(L("You don't see '@x1' out there!",rest));
							return false;
						}
					}
					break;
				}
				case WEIGH_ANCHOR:
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
						CMMsg msg2=CMClass.getMsg(msg.source(), this, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> raise(s) anchor on <T-NAME>."));
						if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
						{
							R.send(msg.source(), msg2);
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
						CMMsg msg2=CMClass.getMsg(msg.source(), this, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> lower(s) anchor on <T-NAME>."));
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
					if(CMLib.flags().isFalling(this) || ((this.subjectToWearAndTear() && (usesRemaining()<=0))))
					{
						msg.source().tell(L("The ship won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The ship has moved!"));
						return false;
					}
					if((courseDirection >=0)||(courseDirections.size()>0))
					{
						if(!this.amInTacticalMode())
							msg.source().tell(L("Your previous course has been cancelled."));
						courseDirection = -1;
						courseDirections.clear();
					}
					int dir=CMLib.directions().getCompassDirectionCode(secondWord);
					if(dir<0)
					{
						msg.source().tell(L("Steer the ship which direction?"));
						return false;
					}
					final Room R=CMLib.map().roomLocation(this);
					if((R==null)||(msg.source().location()==null))
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
						return false;
					}
					if(((msg.source().location().domainType()&Room.INDOORS)==Room.INDOORS)
					&&(msg.source().isPlayer()))
					{
						msg.source().tell(L("You must be on deck to steer your ship."));
						return false;
					}
					final String dirName = CMLib.directions().getDirectionName(dir);
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
					else
					{
						directionFacing = getDirectionFacing(dir);
						if(dir == this.directionFacing)
						{
							msg.source().tell(L("Your ship is already sailing @x1.",dirName));
							return false;
						}
					}
					if(anchorDown)
					{
						msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
						return false;
					}
					break;
				}
				case SAIL:
				{
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					if(CMLib.flags().isFalling(this) || ((this.subjectToWearAndTear() && (usesRemaining()<=0))))
					{
						msg.source().tell(L("The ship won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The ship has moved!"));
						return false;
					}
					if((courseDirection >=0)||(courseDirections.size()>0))
					{
						if(!this.amInTacticalMode())
							msg.source().tell(L("Your previous course has been cancelled."));
						courseDirection = -1;
						courseDirections.clear();
					}
					final Room R=CMLib.map().roomLocation(this);
					if((R==null)||(msg.source().location()==null))
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
						return false;
					}
					if(((msg.source().location().domainType()&Room.INDOORS)==Room.INDOORS)
					&&(msg.source().isPlayer()))
					{
						msg.source().tell(L("You must be on deck to sail your ship."));
						return false;
					}
					int dir=CMLib.directions().getCompassDirectionCode(secondWord);
					if(dir<0)
					{
						msg.source().tell(L("Sail the ship which direction?"));
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
					else
					{
						String dirName = CMLib.directions().getDirectionName(directionFacing);
						directionFacing = getDirectionFacing(dir);
						if(dir != this.directionFacing)
						{
							msg.source().tell(L("When in tactical mode, your ship can only SAIL @x1.  Use COURSE for more complex maneuvers, or STEER.",dirName));
							return false;
						}
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
					if(CMLib.flags().isFalling(this) || ((this.subjectToWearAndTear() && (usesRemaining()<=0))))
					{
						msg.source().tell(L("The ship won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The ship has moved!"));
						return false;
					}
					if((courseDirection >=0)||(courseDirections.size()>0))
					{
						if(!this.amInTacticalMode())
							msg.source().tell(L("Your previous course has been cancelled."));
						courseDirection = -1;
						courseDirections.clear();
					}
					final Room R=CMLib.map().roomLocation(this);
					if((R==null)||(msg.source().location()==null))
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
						return false;
					}
					if(((msg.source().location().domainType()&Room.INDOORS)==Room.INDOORS)
					&&(msg.source().isPlayer()))
					{
						msg.source().tell(L("You must be on deck to steer your ship."));
						return false;
					}
					int dirIndex = 1;
					if(word.equals("SET"))
						dirIndex = 2;
					int firstDir = -1;
					this.courseDirections.clear();
					if(amInTacticalMode())
					{
						final int speed=getShipSpeed();
						final String dirFacingName = CMLib.directions().getDirectionName(directionFacing);
						if(dirIndex >= cmds.size())
						{
							msg.source().tell(L("Your ship is currently sailing @x1. To set a course, you must specify up to @x2 directions of travel, "
												+ "of which only the last may be something other than @x3.",dirFacingName,""+speed,dirFacingName));
							return false;
						}
						List<String> dirNames = new ArrayList<String>();
						final int[] coordinates = Arrays.copyOf(getMyCoords(),2);
						int otherDir = -1;
						for(;dirIndex<cmds.size();dirIndex++)
						{
							final String dirWord=cmds.get(dirIndex);
							int dir=CMLib.directions().getCompassDirectionCode(dirWord);
							if(dir<0)
							{
								msg.source().tell(L("@x1 is not a valid direction.",dirWord));
								return false;
							}
							if((otherDir < 0) && (dir == directionFacing))
							{
								Directions.adjustXYByDirections(coordinates[0], coordinates[1], dir);
								final Room targetRoom=R.getRoomInDir(dir);
								final Exit targetExit=R.getExitInDir(dir);
								if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
								{
									if(getLowestTacticalDistanceFromThis() >= R.maxRange())
									{
										msg.source().tell(L("There doesn't look to be anywhere you can sail in that direction."));
										return false;
									}
								}
							}
							if(this.courseDirections.size() >= speed)
							{
								msg.source().tell(L("Your course may not exceed your tactical speed, which is @x1 moves.", ""+speed));
								return false;
							}
							if(otherDir > 0)
							{
								msg.source().tell(L("Your course includes a change of direction, from @x1 to @x2.  "
												+ "In tactical maneuvers, a changes of direction must be at the end of the course settings.", 
												dirFacingName,CMLib.directions().getDirectionName(otherDir)));
								return false;
							}
							else
							if(dir != directionFacing)
								otherDir = dir;
							dirNames.add(CMLib.directions().getDirectionName(dir).toLowerCase());
							this.courseDirections.add(Integer.valueOf(dir));
						}
						if(this.courseDirections.size()>0)
							this.courseDirection = this.courseDirections.remove(0).intValue();
						if((this.courseDirections.size()==0)||(this.courseDirections.get(this.courseDirections.size()-1).intValue()>=0))
							this.courseDirections.add(Integer.valueOf(-1));
						
						this.announceActionToDeck(msg.source(),L("<S-NAME> order(s) a course setting of @x1.",CMLib.english().toEnglishStringList(dirNames.toArray(new String[0]))));
					}
					else
					{
						if(dirIndex >= cmds.size())
						{
							msg.source().tell(L("To set a course, you must specify some directions of travel, separated by spaces."));
							return false;
						}
						for(;dirIndex<cmds.size();dirIndex++)
						{
							final String dirWord=cmds.get(dirIndex);
							int dir=CMLib.directions().getCompassDirectionCode(dirWord);
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
							this.courseDirection=-1;
							this.courseDirections.clear();
							msg.source().tell(L("There doesn't look to be anything in that direction."));
							return false;
						}
						if((this.courseDirections.size()==0)||(this.courseDirections.get(this.courseDirections.size()-1).intValue()>=0))
							this.courseDirections.add(Integer.valueOf(-1));
						steer(msg.source(),R, firstDir);
					}
					if(anchorDown)
						msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
					return false;
				}
				}
			}
			if(cmd != null)
			{
				cmds.add(0, "METAMSGCOMMAND");
				double speed=getShipSpeed();
				if(speed == 0)
					speed=0;
				else
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
			SailingCommand cmd = null;
			if(secondWord.length()>0)
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word+"_"+secondWord);
			if(cmd == null)
				cmd = (SailingCommand)CMath.s_valueOf(SailingCommand.class, word);
			if(cmd == null)
				return true;
			switch(cmd)
			{
			case SAIL:
			{
				int dir=CMLib.directions().getCompassDirectionCode(secondWord);
				if(dir<0)
					return false;
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
					return false;
				if(!this.amInTacticalMode())
				{
					this.courseDirections.clear(); // sail eliminates a course
					this.courseDirections.add(Integer.valueOf(-1));
					this.beginSail(msg.source(), R, dir);
				}
				else
				{
					if(this.courseDirections.size()>0)
						msg.source().tell(L("Your prior course has been overridden."));
					this.courseDirections.clear();
					this.courseDirections.add(Integer.valueOf(-1));
					this.courseDirection = dir;
					this.announceActionToDeck(msg.source(),L("<S-NAME> start(s) sailing @x1.",CMLib.directions().getDirectionName(dir)));
				}
				return false;
			}
			case STEER:
			{
				int dir=CMLib.directions().getCompassDirectionCode(secondWord);
				if(dir<0)
					return false;
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
					return false;
				if(!this.amInTacticalMode())
				{
					steer(msg.source(),R, dir);
				}
				else
				{
					if(this.courseDirections.size()>0)
						msg.source().tell(L("Your prior tactical course has been overridden."));
					this.courseDirections.clear();
					this.courseDirections.add(Integer.valueOf(-1));
					this.courseDirection = dir;
					this.announceActionToDeck(msg.source(),L("<S-NAME> start(s) steering the ship @x1.",CMLib.directions().getDirectionName(dir)));
				}
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
		&&(!CMLib.flags().isFlying(msg.source()))
		&&(!CMLib.flags().isFalling((Physical)msg.target()))
		&&(!CMLib.law().doesHavePriviledgesHere(msg.source(), super.getDestinationRoom(msg.source().location()))))
		{
			final Rideable ride=msg.source().riding();
			if(ticksSinceMove < 2)
			{
				if(ride == null)
					msg.source().tell(CMLib.lang().L("You'll need some assistance to board a ship from the water."));
				else
					msg.source().tell(msg.source(),this,ride,CMLib.lang().L("<S-NAME> chase(s) <T-NAME> around in <O-NAME>."));
				return false;
			}
			else
			if(ride == null)
			{
				msg.source().tell(CMLib.lang().L("You'll need some assistance to board a ship from the water."));
				return false;
			}
			else
			if(!CMLib.flags().isClimbing(msg.source()))
			{
				msg.source().tell(CMLib.lang().L("You'll need some assistance to board a ship from @x1, such as some means to climb up.",ride.name(msg.source())));
				return false;
			}
			else
				msg.source().setRiding(null); // if you're climbing, you're not riding any more
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			switch(Character.toUpperCase(msg.targetMessage().charAt(0)))
			{
			case 'A':
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				String cmd=parsedFail.get(0).toUpperCase();
				if(("ATTACK".startsWith(cmd))&&(owner() instanceof Room))
				{
					final Room thisRoom = (Room)owner();
					String rest = CMParms.combine(parsedFail,1);
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					Boolean result = startAttack(msg.source(),thisRoom,rest);
					if(result == Boolean.FALSE)
						return false;
					else
					if(result == Boolean.TRUE)
					{
						msg.source().tell(getTargetedShipInfo());
						return false;
					}
				}
				break;
			}
			case 'E':
			case 'L':
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				String cmd=parsedFail.get(0).toUpperCase();
				if(("LOOK".startsWith(cmd)||"LLOOK".startsWith(cmd)||"EXAMINE".startsWith(cmd))
				&&(owner() instanceof Room))
				{
					final int msgType = "EXAMINE".startsWith(cmd) ? CMMsg.MSG_EXAMINE : CMMsg.MSG_LOOK;
					final Room R = (Room)owner();
					String rest = CMParms.combine(parsedFail,1);
					final Item I = R.findItem(null, rest);
					if((I instanceof GenSailingShip)
					||((I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER)))
					{
						final CMMsg lookMsg=CMClass.getMsg(msg.source(),I,null,msgType,null,msgType,null,msgType,null);
						if(R.okMessage(msg.source(),lookMsg))
						{
							R.send(msg.source(),lookMsg);
							return false;
						}
					}
				}
				break;
			}
			case 'T': // throwing things to another ship, like a grapple
			{
				List<String> parsedFail = CMParms.parse(msg.targetMessage());
				String cmd=parsedFail.get(0).toUpperCase();
				if(("THROW".startsWith(cmd))
				&&(owner() instanceof Room)
				&&(msg.source().location()!=null)
				&&((msg.source().location().domainType()&Room.INDOORS)==0)
				&&(parsedFail.size()>2))
				{
					parsedFail.remove(0);
					final MOB mob=msg.source();
					final Room R = (Room)owner();
					String str=parsedFail.get(parsedFail.size()-1);
					parsedFail.remove(str);
					final String what=CMParms.combine(parsedFail,0);
					Item item=mob.fetchItem(null,Wearable.FILTER_WORNONLY,what);
					if(item==null)
						item=mob.findItem(null,what);
					if((item!=null)
					&&(CMLib.flags().canBeSeenBy(item,mob))
					&&((item.amWearingAt(Wearable.WORN_HELD))||(item.amWearingAt(Wearable.WORN_WIELD))))
					{
						str=str.toLowerCase();
						if(str.equals("water")||str.equals("overboard")||CMLib.english().containsString(R.displayText(), str))
						{
							final Room target=R;
							final CMMsg msg2=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> overboard."));
							final CMMsg msg3=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<O-NAME> fl(ys) in from overboard."));
							if(mob.location().okMessage(mob,msg2)&&target.okMessage(mob,msg3))
							{
								mob.location().send(mob,msg2);
								target.sendOthers(mob,msg3);
							}
							return false;
						}
							
						final Item I = R.findItem(null, str);
						if(I!=this)
						{
							if((I instanceof GenSailingShip)
							||((I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER)))
							{
								if((!amInTacticalMode())
								||(I.maxRange() < getTacticalDistance(I)))
								{
									msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
									return false;
								}
								else
								if(getMyCoords()!=null)
								{
									final int[] targetCoords = ((GenSailingShip)I).getMyCoords();
									if(targetCoords!=null)
									{
										int dir = Directions.getRelativeDirection(getMyCoords(), targetCoords);
										final String inDir=CMLib.directions().getShipInDirectionName(dir);
										final String fromDir=CMLib.directions().getFromShipDirectionName(Directions.getOpDirectionCode(dir));
										Room target = ((GenSailingShip)I).getRandomDeckRoom();
										if(target == null)
											target=R;
										final CMMsg msg2=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> @x1.",inDir.toLowerCase()));
										final CMMsg msg3=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<O-NAME> fl(ys) in from @x1.",fromDir.toLowerCase()));
										if(mob.location().okMessage(mob,msg2)&&target.okMessage(mob,msg3))
										{
											mob.location().send(mob,msg2);
											target.sendOthers(mob,msg3);
										}
										return false;
									}
									else
									{
										msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
										return false;
									}
								}
								else
								{
									msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
									return false;
								}
							}
						}
					}
				}
				break;
			}
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() instanceof Room)
		&&(msg.target() == owner()))
		{
			if((msg.source().riding() != null)
			&&(this.targetedShip == msg.source().riding())
			&&(CMLib.flags().isWaterWorthy(msg.source().riding())))
			{
				msg.source().tell(L("Your small vessel can not get away during combat."));
				return false;
			}
			if(msg.tool() instanceof Exit)
			{
				final Room R=msg.source().location();
				int dir=CMLib.map().getExitDir(R,(Exit)msg.tool());
				if((dir >= 0)
				&&(R.getRoomInDir(dir)!=null)
				&&(R.getRoomInDir(dir).getArea()==this.getShipArea())
				&&(msg.othersMessage()!=null)
				&&(msg.othersMessage().indexOf("<S-NAME>")>=0)
				&&(msg.othersMessage().indexOf(L(CMLib.flags().getPresentDispositionVerb(msg.source(),CMFlagLibrary.ComingOrGoing.LEAVES)))>=0))
					msg.setOthersMessage(L("<S-NAME> board(s) @x1.",Name()));
			}
			
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() == owner())
		&&(msg.source().location()!=null)
		&&(msg.source().location().getArea()==this.getShipArea())
		&&(msg.tool() instanceof Exit)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().indexOf("<S-NAME>")>=0)
		&&(msg.othersMessage().indexOf(L(CMLib.flags().getPresentDispositionVerb(msg.source(),CMFlagLibrary.ComingOrGoing.ARRIVES)))>=0))
			msg.setOthersMessage(L("<S-NAME> disembark(s) @x1.",Name()));
		else
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(msg.source().riding() instanceof Item)
		&&(msg.source().riding().mobileRideBasis())
		&&(owner() == CMLib.map().roomLocation(msg.source())))
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
				default:
					break;
				case TENDER:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another ship to offer to board."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This ship is nowhere to be found!"));
						return false;
					}
					/*//TODO: maybe check to see if the lil ship is 
					if(this.targetedShip!=null)
					{
						msg.source().tell(L("Not while you are in combat!"));
						return false;
					}
					*/
					String rest = CMParms.combine(cmds,1);
					final Item meI=thisRoom.findItem(rest);
					if((meI==this)&&(CMLib.flags().canBeSeenBy(this, msg.source())))
					{
						if(targetedShip != null)
						{
							msg.source().tell(L("Not while @x1 is in in combat!",Name()));
							return false;
						}
						final Room R=CMLib.map().roomLocation(msg.source());
						if((R!=null)&&(R.show(msg.source(), this, CMMsg.TYP_ADVANCE, L("<S-NAME> tender(s) @x1 alonside <T-NAME>, wating to be raised on board.",msg.source().riding().name()))))
						{
							for(Iterator<Item> i=smallTenderRequests.iterator();i.hasNext();)
							{
								final Item I=i.next();
								if(!R.isContent(I))
									smallTenderRequests.remove(I);
							}
							if(!smallTenderRequests.contains(msg.source().riding()))
								smallTenderRequests.add((Item)msg.source().riding());
						}
					}
					else
					{
						msg.source().tell(L("You don't see the ship '@x1' here to tender with",rest));
						return false;
					}
					break;
				}
				}
			}
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}
	
	protected Boolean startAttack(MOB sourceM, Room thisRoom, String rest)
	{
		final Item I=thisRoom.findItem(rest);
		if((I instanceof Rideable)&&(I!=this)&&(CMLib.flags().canBeSeenBy(I, sourceM)))
		{
			if(!sourceM.mayPhysicallyAttack(I))
			{
				sourceM.tell(L("You are not permitted to attack @x1",I.name()));
				return Boolean.FALSE;
			}
			if(!CMLib.flags().isDeepWaterySurfaceRoom(thisRoom))
			{
				sourceM.tell(L("You are not able to engage in combat with @x1 here.",I.name()));
				return Boolean.FALSE;
			}
			final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
			try
			{
				mob.setRiding(this);
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
				mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
				final CMMsg maneuverMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_ADVANCE,null,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,L("<S-NAME> engage(s) @x1.",I.Name()));
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
					return Boolean.TRUE;
				}
			}
			finally
			{
				mob.destroy();
			}
		}
		return null;
	}

	protected int[] getMagicCoords()
	{
		final Room R=CMLib.map().roomLocation(this);
		final int[] coords;
		final int middle = (int)Math.round(Math.floor(R.maxRange() / 2.0));
		final int extreme = R.maxRange()-1;
		final int midDiff = (middle > 0) ? CMLib.dice().roll(1, middle, -1) : 0;
		final int midDiff2 = (middle > 0) ? CMLib.dice().roll(1, middle, -1) : 0;
		final int extremeRandom = (extreme > 0) ? CMLib.dice().roll(1, R.maxRange(), -1) : 0;
		final int extremeRandom2 = (extreme > 0) ? CMLib.dice().roll(1, R.maxRange(), -1) : 0;
		switch(this.directionFacing)
		{
		case Directions.NORTH:
			coords = new int[] {extremeRandom,extreme-midDiff};
			break;
		case Directions.SOUTH:
			coords = new int[] {extremeRandom,midDiff};
			break;
		case Directions.EAST:
			coords = new int[] {midDiff,extremeRandom};
			break;
		case Directions.WEST:
			coords = new int[] {extreme-midDiff,extremeRandom};
			break;
		case Directions.UP:
			coords = new int[] {extremeRandom,extremeRandom2};
			break;
		case Directions.DOWN:
			coords = new int[] {extremeRandom,extremeRandom2};
			break;
		case Directions.NORTHEAST:
			coords = new int[] {midDiff,extreme-midDiff2};
			break;
		case Directions.NORTHWEST:
			coords = new int[] {extreme-midDiff,extreme-midDiff2};
			break;
		case Directions.SOUTHEAST:
			coords = new int[] {extreme-midDiff,midDiff2};
			break;
		case Directions.SOUTHWEST:
			coords = new int[] {midDiff,midDiff2};
			break;
		default:
			coords = new int[] {extremeRandom,extremeRandom2};
			break;
		}
		return coords;
	}

	protected void clearTacticalModeHelper()
	{
		synchronized((""+shipCombatRoom + "_SHIP_TACTICAL").intern())
		{
			PairList<Item,int[]> coords = this.coordinates;
			if(coords != null)
			{
				coords.removeFirst(this);
			}
		}
		this.targetedShip = null;
		this.shipCombatRoom = null;
		this.coordinates = null;
		this.aimings.clear();
	}
	
	protected synchronized void clearTacticalMode()
	{
		final Room shipCombatRoom = this.shipCombatRoom;
		if(shipCombatRoom != null)
		{
			PairList<Item,int[]> coords = null;
			synchronized((""+shipCombatRoom + "_SHIP_TACTICAL").intern())
			{
				 coords = this.coordinates;
			}
			clearTacticalModeHelper();
			if(coords != null)
			{
				for(Iterator<Item> s = coords.firstIterator();s.hasNext();)
				{
					Item I=s.next();
					if((I instanceof GenSailingShip)
					&&(((GenSailingShip)I).targetedShip == this))
						((GenSailingShip)I).clearTacticalModeHelper();
				}
			}
		}
	}
	
	protected boolean isAnyoneAtCoords(int[] xy)
	{
		PairList<Item, int[]> coords = this.coordinates;
		if(coords != null)
		{
			for(final Iterator<int[]> i = coords.secondIterator(); i.hasNext();)
			{
				if(Arrays.equals(xy, i.next()))
					return true;
			}
		}
		return false;
	}
	
	protected int[] getMyCoords()
	{
		PairList<Item, int[]> coords = this.coordinates;
		if(coords != null)
		{
			for(final Iterator<Pair<Item,int[]>> i = coords.iterator(); i.hasNext();)
			{
				final Pair<Item,int[]> P=i.next();
				if(P.first == this)
					return P.second;
			}
		}
		return null;
	}
	
	protected synchronized boolean amInTacticalMode()
	{
		final Item targetedShip = (Item)this.targetedShip;
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
						int[] newCoords = null;
						for(int i=0;i<10;i++)
						{
							newCoords = this.getMagicCoords();
							if(!isAnyoneAtCoords(newCoords))
								break;
						}
						coords.add(new Pair<Item,int[]>(this,newCoords));
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
		final int sailingTickID = amInTacticalMode() ? Tickable.TICKID_SPECIALMANEUVER : Tickable.TICKID_AREA;
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			Area area = this.getShipArea();
			if(area instanceof BoardableShip)
			{
				if((this.tenderShip != null)
				&&(this.tenderShip.owner()==owner())
				&&(this.targetedShip==null)
				&&(this.tenderShip instanceof GenSailingShip)
				&&(((GenSailingShip)this.tenderShip).targetedShip==null))
				{
					// yay!
				}
				else
				{
					this.tenderShip=null;
					if((((BoardableShip)area).getIsDocked() != owner())
					&&(owner() instanceof Room))
					{
						this.dockHere((Room)owner());
					}
				}
			}
		}
		if(tickID == sailingTickID)
		{
			ticksSinceMove++;
			if((!this.anchorDown) 
			&& (area != null) 
			&& (courseDirection != -1) )
			{
				final int speed=getShipSpeed();
				for(int s=0;s<speed && (courseDirection>=0);s++)
				{
					switch(sail(courseDirection & 127))
					{
					case CANCEL:
					{
						courseDirection=-1;
						break;
					}
					case CONTINUE:
					{
						if(this.courseDirections.size()>0)
						{
							final Integer newDir=this.courseDirections.remove(0);
							courseDirection = newDir.intValue();
						}
						else
						{
							if((courseDirection & COURSE_STEER_MASK) == 0)
								courseDirection = -1;
						}
						break;
					}
					case REPEAT:
					{
						break;
					}
					}
				}
				if(tickID == Tickable.TICKID_SPECIALMANEUVER)
				{
					final Room combatRoom=this.shipCombatRoom;
					if(combatRoom != null)
					{
						final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),null);
						try
						{
							mob.setRiding(this);
							mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
							mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
							combatRoom.show(mob, this, CMMsg.MSG_ACTIVATE|CMMsg.MASK_MALICIOUS, null);
						}
						finally
						{
							mob.destroy();
						}
					}
					PairList<Item,int[]> coords = this.coordinates;
					if(coords != null)
					{
						for(Iterator<Item> i= coords.firstIterator(); i.hasNext();)
						{
							Item I=i.next();
							if((I instanceof GenSailingShip)
							&&(((GenSailingShip)I).targetedShip == this))
								((GenSailingShip)I).announceToDeck(((GenSailingShip)I).getTargetedShipInfo());
						}
					}
				}
				
			}
		}
		if(tickID == Tickable.TICKID_SPECIALCOMBAT)
		{
			if(this.amInTacticalMode())
			{
				final List<Weapon> weapons = new LinkedList<Weapon>();
				for(Enumeration<Room> r=this.getShipArea().getProperMap();r.hasMoreElements();)
				{
					try
					{
						final Room R=r.nextElement();
						if(R!=null)
						{
							for(Enumeration<Item> i=R.items();i.hasMoreElements();)
							{
								try
								{
									final Item I=i.nextElement();
									if(isAShipSiegeWeaponReadyToFire(I))
										weapons.add((Weapon)I);
								}
								catch(NoSuchElementException ne)
								{
								}
							}
						}
					}
					catch(NoSuchElementException ne)
					{
					}
				}
				if(weapons.size()>0)
				{
					final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),null);
					final int[] coordsToHit;
					if(this.targetedShip instanceof GenSailingShip)
						coordsToHit = ((GenSailingShip)this.targetedShip).getMyCoords();
					else
						coordsToHit = new int[2];
					try
					{
						mob.setRiding(this);
						mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
						mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
						int notLoaded = 0;
						int notAimed = 0;
						for(Weapon w : weapons)
						{
							final Room R=CMLib.map().roomLocation(w);
							if(R!=null)
							{
								mob.setLocation(R);
								if((w instanceof AmmunitionWeapon)
								&&(((AmmunitionWeapon)w).requiresAmmunition())
								&&(((AmmunitionWeapon)w).ammunitionRemaining() <=0))
									notLoaded++;
								else
								{
									//mob.setRangeToTarget(0);
									int index = aimings.indexOfFirst(w);
									if(index >= 0)
									{
										int[] coordsAimedAt = aimings.remove(index).second;
										boolean wasHit = Arrays.equals(coordsAimedAt, coordsToHit);
										CMLib.combat().postShipAttack(mob, this, this.targetedShip, w, wasHit);
									}
									else
										notAimed++;
								}
							}
						}
						final String spamMsg;
						if((notLoaded > 0) && (notAimed > 0))
							spamMsg = L("@x1 of your weapons were not loaded, and @x2 were ready but not aimed.",""+notLoaded, ""+notAimed);
						else
						if(notLoaded > 0)
							spamMsg = L("@x1 of your weapons were not loaded.",""+notLoaded);
						else
						if(notAimed > 0)
							spamMsg = L("@x1 of your weapons were ready but not aimed.",""+notAimed);
						else
							spamMsg = "";
						if(spamMsg.length()>0)
						{
							if(spamMsg.equals(lastSpamMsg))
							{
								if(lastSpamCt < 3)
								{
									announceToDeck(spamMsg);
									lastSpamCt++;
								}
							}
							else
							{
								announceToDeck(spamMsg);
								lastSpamCt=0;
							}
						}
						lastSpamMsg=spamMsg;
					}
					finally
					{
						mob.setRangeToTarget(0);
						mob.destroy();
					}
				}
			}
		}
		return super.tick(ticking, tickID);
	}

	protected final boolean isAShipSiegeWeaponReadyToFire(Item I)
	{
		if(CMLib.combat().isAShipSiegeWeapon(I))
		{
			if(((Rideable)I).riderCapacity() > 0)
				return ((Rideable)I).numRiders() >= ((Rideable)I).riderCapacity();
			return true;
		}
		return false;
	}
	
	protected static MOB getBestRider(final Room R, final Rideable rI)
	{
		MOB bestM=null;
		for(final Enumeration<Rider> m=rI.riders();m.hasMoreElements();)
		{
			final Rider R2=m.nextElement();
			if((R2 instanceof MOB) && (R.isInhabitant((MOB)R2)))
			{
				if(((MOB)R2).isPlayer())
					return (MOB)R2;
				else
				if((bestM!=null)&&(bestM.amFollowing()!=null))
					bestM=(MOB)R2;
				else
					bestM=(MOB)R2;
			}
		}
		return bestM;
	}
	
	protected static String staticL(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	public static void appendCondition(StringBuilder visualCondition, double pct, String name)
	{
		if(pct<=0.0)
			visualCondition.append(staticL("\n\r^r@x1^r is SINKING!^N",name));
		else
		if(pct<.10)
			visualCondition.append(staticL("\n\r^r@x1^r is near destruction!^N",name));
		else
		if(pct<.20)
			visualCondition.append(staticL("\n\r^r@x1^r is massively splintered and damaged.^N",name));
		else
		if(pct<.30)
			visualCondition.append(staticL("\n\r^r@x1^r is extremely splintered and damaged.^N",name));
		else
		if(pct<.40)
			visualCondition.append(staticL("\n\r^y@x1^y is very splintered and damaged.^N",name));
		else
		if(pct<.50)
			visualCondition.append(staticL("\n\r^y@x1^y is splintered and damaged.^N",name));
		else
		if(pct<.60)
			visualCondition.append(staticL("\n\r^p@x1^p is splintered and slightly damaged.^N",name));
		else
		if(pct<.70)
			visualCondition.append(staticL("\n\r^p@x1^p is showing large splinters.^N",name));
		else
		if(pct<.80)
			visualCondition.append(staticL("\n\r^g@x1^g is showing some splinters.^N",name));
		else
		if(pct<.90)
			visualCondition.append(staticL("\n\r^g@x1^g is showing small splinters.^N",name));
		else
		if(pct<.99)
			visualCondition.append(staticL("\n\r^g@x1^g is no longer in perfect condition.^N",name));
		else
			visualCondition.append(staticL("\n\r^c@x1^c is in perfect condition.^N",name));
	}

	protected void cleanMsgForRepeat(CMMsg msg)
	{
		msg.setSourceCode(CMMsg.NO_EFFECT);
		if(msg.trailerRunnables()!=null)
			msg.trailerRunnables().clear();
		if(msg.trailerMsgs()!=null)
		{
			for(CMMsg msg2 : msg.trailerMsgs())
				cleanMsgForRepeat(msg2);
		}
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_HUH:
		case CMMsg.TYP_COMMANDFAIL:
		case CMMsg.TYP_COMMAND:
			break;
		default:
			if((msg.source().riding()==this)
			||(msg.othersMessage()==null)
			)
				sendAreaMessage(msg, true);
			else
			{
				final CMMsg msg2=(CMMsg)msg.copyOf();
				msg2.setOthersMessage(L("^HOff the deck you see: ^N")+msg.othersMessage());
				cleanMsgForRepeat(msg2);
				sendAreaMessage(msg2, true);
			}
		}
		
		if(msg.target() == this)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
			{
				final StringBuilder visualCondition = new StringBuilder("");
				if(this.anchorDown)
					visualCondition.append(L("^HThe anchor on @x1 is lowered, holding her in place.^.^?",name(msg.source())));
				else
				if((this.courseDirection >= 0)
				&&((this.courseDirections.size()>0)&&(this.courseDirections.get(0).intValue()>=0)))
					visualCondition.append(L("^H@x1 is under full sail, traveling @x2^.^?",CMStrings.capitalizeFirstLetter(name(msg.source())), CMLib.directions().getDirectionName(courseDirection & 127)));
				if(this.subjectToWearAndTear() && (usesRemaining() <= 100))
				{
					final double pct=(CMath.div(usesRemaining(),100.0));
					appendCondition(visualCondition,pct,name(msg.source()));
				}
				if(visualCondition.length()>0)
				{
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							msg.source().tell(visualCondition.toString());
						}
					});
				}
				break;
			}
			default:
				break;
			}
		}
		
		if((msg.target() instanceof Room)
		&&(msg.target() == owner()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((CMLib.map().areaLocation(msg.source())==area))
				{
					final StringBuilder visualCondition = new StringBuilder("");
					if(this.anchorDown)
						visualCondition.append(L("\n\r^HThe anchor on @x1 is lowered, holding her in place.^.^?",name(msg.source())));
					else
					if((this.courseDirection >= 0)
					&&((this.courseDirections.size()>0)&&(this.courseDirections.get(0).intValue()>=0)))
						visualCondition.append(L("\n\r^H@x1 is under full sail, traveling @x2^.^?",name(msg.source()), CMLib.directions().getDirectionName(courseDirection & 127)));
					if(this.subjectToWearAndTear() && (usesRemaining() <= 100) && (this.targetedShip != null))
					{
						final double pct=(CMath.div(usesRemaining(),100.0));
						appendCondition(visualCondition,pct,name(msg.source()));
					}
					if(visualCondition.length()>0)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, visualCondition.toString(), -1, null, -1, null));
				}
				break;
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
				if((owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area))
				{
					if(((msg.source().riding() == this)
						&&(msg.source().Name().equals(Name())))
					||((this.targetedShip!=null)
						&&(msg.source().riding() == targetedShip)
						&&(msg.source().Name().equals(targetedShip.Name()))))
					{
						clearTacticalMode();
					}
				}
				break;
			}
		}
		else
		if(msg.target()  == this)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
				break;
			case CMMsg.TYP_WEAPONATTACK:
				if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
				{
					Weapon weapon=null;
					if((msg.tool() instanceof Weapon))
						weapon=(Weapon)msg.tool();
					if((weapon!=null)
					&&(((msg.source().riding()!=null)&&(owner() instanceof Room))
						||((msg.source().location()!=null) && (weapon.owner()==null))))
					{
						final Room shipRoom=(Room)owner();
						final boolean isHit=msg.value()>0;
						if(isHit && CMLib.combat().isAShipSiegeWeapon(weapon) 
						&& (((AmmunitionWeapon)weapon).ammunitionCapacity() > 1))
						{
							int shotsRemaining = ((AmmunitionWeapon)weapon).ammunitionRemaining() + 1;
							((AmmunitionWeapon)weapon).setAmmoRemaining(0);
							final Area A=this.getShipArea();
							ArrayList<Pair<MOB,Room>> targets = new ArrayList<Pair<MOB,Room>>(5);
							if(A!=null)
							{
								for(Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
								{
									final Room R=r.nextElement();
									if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
									{
										for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
											targets.add(new Pair<MOB,Room>(m.nextElement(),R));
									}
								}
							}
							final int chanceToHit = targets.size() * 20;
							final Room oldRoom=msg.source().location();
							try
							{
								final double pctLoss = CMath.div(msg.value(), 100.0);
								while(shotsRemaining-- > 0)
								{
									final Pair<MOB,Room> randomPair = (targets.size()>0)? targets.get(CMLib.dice().roll(1,targets.size(),-1)) : null;
									if((CMLib.dice().rollPercentage() < chanceToHit)&&(randomPair != null))
									{
										msg.source().setLocation(shipRoom);
										int pointsLost = (int)Math.round(pctLoss * msg.source().maxState().getHitPoints());
										CMLib.combat().postWeaponDamage(msg.source(), randomPair.first, weapon, pointsLost);
									}
									else
									if(randomPair != null)
									{
										msg.source().setLocation(shipRoom);
										CMLib.combat().postWeaponAttackResult(msg.source(), randomPair.first, weapon, false);
									}
									else
									{
										this.announceActionToDeck(msg.source(), msg.target(), weapon, weapon.missString());
									}
								}
							}
							finally
							{
								msg.source().setLocation(oldRoom);
							}
						}
						else
						{
							PhysicalAgent attacker;
							if(msg.source().riding() instanceof BoardableShip)
								attacker=msg.source().riding();
							else
							{
								final Room R=msg.source().location();
								if((R!=null)
								&&(R.getArea() instanceof BoardableShip))
									attacker=((BoardableShip)R.getArea()).getShipItem();
								else
									attacker=null;
							}
							if(attacker != null)
								CMLib.combat().postShipWeaponAttackResult(msg.source(), attacker, this, weapon, isHit);
						}
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				if(msg.value() > 0)
				{
					final int maxHullPoints = CMLib.combat().getShipHullPoints(this);
					double pctLoss = CMath.div(msg.value(), maxHullPoints);
					int pointsLost = (int)Math.round(pctLoss * maxHullPoints);
					if(pointsLost > 0)
					{
						int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
						final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
						final String msgStr = (msg.targetMessage() == null) ? L("<O-NAME> fired from <S-NAME> hits and @x1 @x2.",hitWord,name()) : msg.targetMessage();
						final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
						this.announceActionToDeckOrUnderdeck(msg.source(), deckHitMsg, 0);
						final CMMsg underdeckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, L("Something hits and @x1 the ship.",hitWord));
						this.announceActionToDeckOrUnderdeck(msg.source(), underdeckHitMsg, Room.INDOORS);
						if(pointsLost >= this.usesRemaining())
						{
							this.setUsesRemaining(0);
							this.recoverPhyStats(); // takes away the swimmability!
							final Room shipR=CMLib.map().roomLocation(this);
							if(shipR!=null)
							{
								CMLib.tracking().makeSink(this, shipR, false);
								final String sinkString = L("<T-NAME> start(s) sinking!");
								shipR.show(msg.source(), this, CMMsg.MSG_OK_ACTION, sinkString);
								this.announceActionToUnderDeck(msg.source(), sinkString);
							}
							
							if(!CMLib.leveler().postExperienceToAllAboard(msg.source().riding(), 500))
								CMLib.leveler().postExperience(msg.source(), null, null, 500, false);
							this.clearTacticalMode();
						}
						else
						{
							this.setUsesRemaining(this.usesRemaining() - pointsLost);
						}
					}
				}
				break;
			}
		}
		else
		if(msg.target() instanceof AmmunitionWeapon)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_RELOAD:
				if((msg.tool() instanceof Ammunition)
				&&(CMLib.combat().isAShipSiegeWeapon((Item)msg.target())))
				{
					final MOB tellM=msg.source();
					final Item I= (Item)msg.target();
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							tellM.tell(L("@x1 is now loaded. Don't forget to aim.",I.name()));
						}
					});
				}
				break;
			}
		}
		else
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((msg.target() instanceof BoardableShip)
				&&(msg.target() instanceof Rideable)
				&&(msg.target()!=this))
				{
					final String otherShipInfo=getOtherShipInfo((Rideable)msg.target());
					if(otherShipInfo.length()>0)
					{
						msg.addTrailerRunnable(new Runnable()
						{
							@Override
							public void run()
							{
								msg.source().tell(otherShipInfo);
							}
						});
					}
				}
				break;
			}
		}
			
	}
	
	@Override
	public long expirationDate()
	{
		final Room R=CMLib.map().roomLocation(this);
		if(R==null)
			dispossessionTime = 0;
		else
		if(CMLib.flags().isUnderWateryRoom(R))
		{
			if(dispossessionTime == 0)
				dispossessionTime = System.currentTimeMillis()+(CMProps.getIntVar(CMProps.Int.EXPIRE_PLAYER_DROP) * TimeManager.MILI_MINUTE);
		}
		else
			dispossessionTime = 0;
		return dispossessionTime;
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
				if(CMLib.flags().isDeepWaterySurfaceRoom(R2))
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

	protected int getTacticalDistance(PhysicalAgent targetShip)
	{
		final int[] fromCoords = this.getMyCoords();
		final PairList<Item,int[]> coords = this.coordinates;
		int lowest = Integer.MAX_VALUE;
		if((coords != null) && (fromCoords != null))
		{
			final int p = coords.indexOfFirst((Item)targetShip);
			if(p >=0)
			{
				final Pair<Item,int[]> P=coords.get(p);
				int distance = (int)Math.round(Math.ceil(Math.sqrt(Math.pow(P.second[0]-fromCoords[0],2.0) + Math.pow(P.second[1]-fromCoords[1],2.0))));
				if(distance < lowest)
					lowest=distance;
			}
		}
		if(lowest == Integer.MAX_VALUE)
			return CMLib.map().roomLocation(this).maxRange() + 1;
		return lowest;
	}
	
	protected int getLowestTacticalDistanceFromThis()
	{
		final int[] fromCoords = this.getMyCoords();
		final PairList<Item,int[]> coords = this.coordinates;
		int lowest = Integer.MAX_VALUE;
		if((coords != null) && (fromCoords != null))
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
		if(lowest == Integer.MAX_VALUE)
			return CMLib.map().roomLocation(this).maxRange();
		return lowest;
	}

	protected int getDirectionFacing(final int direction)
	{
		final Room thisRoom=CMLib.map().roomLocation(this);
		if(directionFacing < 0)
		{
			if(thisRoom != null)
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=thisRoom.getRoomInDir(d);
					if((R2!=null)
					&&(thisRoom.getExitInDir(d)!=null)
					&&(thisRoom.getExitInDir(d).isOpen())
					&&(!CMLib.flags().isWateryRoom(R2)))
					{
						return Directions.getOpDirectionCode(d);
					}
				}
			}
			return direction;
		}
		return directionFacing;
	}
	
	protected SailResult sail(final int direction)
	{
		final Room thisRoom=CMLib.map().roomLocation(this);
		if(thisRoom != null)
		{
			directionFacing = getDirectionFacing(direction);
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
					if(directionFacing == direction)
					{
						final String directionName = CMLib.directions().getDirectionName(direction);
						final int[] newCoords = Directions.adjustXYByDirections(tacticalCoords[0], tacticalCoords[1], direction);
						final CMMsg maneuverMsg=CMClass.getMsg(mob, thisRoom, null,
																CMMsg.MSG_ADVANCE,newCoords[0]+","+newCoords[1],
																CMMsg.MSG_ADVANCE,directionName,
																CMMsg.MSG_ADVANCE,L("<S-NAME> maneuver(s) @x1.",directionName));
						if(thisRoom.okMessage(mob, maneuverMsg))
						{
							thisRoom.send(mob, maneuverMsg);
							final int oldDistance = this.getLowestTacticalDistanceFromThis();
							tacticalCoords[0] = newCoords[0];
							tacticalCoords[1] = newCoords[1];
							final int newDistance = this.getLowestTacticalDistanceFromThis();
							ticksSinceMove=0;
							if((newDistance <= oldDistance)||(newDistance < thisRoom.maxRange()))
								return SailResult.CONTINUE;
						}
						else
							return SailResult.REPEAT;
						// else we get to make a real Sailing move!
					}
					else
					{
						final int gradualDirection=Directions.getGradualDirectionCode(directionFacing, direction);
						final String directionName = CMLib.directions().getDirectionName(gradualDirection);
						final String finalDirectionName = CMLib.directions().getDirectionName(direction);
						final CMMsg maneuverMsg=CMClass.getMsg(mob, thisRoom, null,
																CMMsg.MSG_ADVANCE,directionName,
																CMMsg.MSG_ADVANCE,finalDirectionName,
																CMMsg.MSG_ADVANCE,L("<S-NAME> change(s) course, turning @x1.",directionName));
						if(thisRoom.okMessage(mob, maneuverMsg))
						{
							thisRoom.send(mob, maneuverMsg);
							directionFacing = CMLib.directions().getStrictDirectionCode(maneuverMsg.sourceMessage());
						}
						if(direction != directionFacing)
							return SailResult.REPEAT;
						else
							return SailResult.CONTINUE;
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
			this.clearTacticalMode();
			final Room destRoom=thisRoom.getRoomInDir(direction);
			final Exit exit=thisRoom.getExitInDir(direction);
			if((destRoom!=null)&&(exit!=null))
			{
				if((!CMLib.flags().isDeepWaterySurfaceRoom(destRoom))
				&&(destRoom.domainType()!=Room.DOMAIN_OUTDOORS_SEAPORT))
				{
					announceToShip(L("As there is no where to sail @x1, <S-NAME> meanders along the waves.",CMLib.directions().getInDirectionName(direction)));
					courseDirections.clear();
					return SailResult.CANCEL;
				}
				final int oppositeDirectionFacing=Directions.getOpDirectionCode(direction);
				final String directionName=CMLib.directions().getDirectionName(direction);
				final String otherDirectionName=CMLib.directions().getDirectionName(oppositeDirectionFacing);
				final Exit opExit=thisRoom.getExitInDir(oppositeDirectionFacing);
				final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),CMLib.map().roomLocation(this));
				mob.setRiding(this);
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SWIMMING);
				mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SWIMMING);
				try
				{
					final boolean isSneaking = CMLib.flags().isSneaking(this);
					final String sailEnterStr = isSneaking ? null : L("<S-NAME> sail(s) in from @x1.",otherDirectionName);
					final String sailAwayStr = isSneaking ? null : L("<S-NAME> sail(s) @x1.",directionName);
					final CMMsg enterMsg=CMClass.getMsg(mob,destRoom,exit,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,sailEnterStr);
					final CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,sailAwayStr);
					if((exit.okMessage(mob,enterMsg))
					&&(leaveMsg.target().okMessage(mob,leaveMsg))
					&&((opExit==null)||(opExit.okMessage(mob,leaveMsg)))
					&&(enterMsg.target().okMessage(mob,enterMsg)))
					{
						exit.executeMsg(mob,enterMsg);
						thisRoom.sendOthers(mob, leaveMsg);
						this.unDock(false);
						((Room)enterMsg.target()).moveItemTo(this);
						ticksSinceMove=0;
						this.dockHere(((Room)enterMsg.target()));
						//this.sendAreaMessage(leaveMsg, true);
						if(opExit!=null)
							opExit.executeMsg(mob,leaveMsg);
						((Room)enterMsg.target()).send(mob, enterMsg);
						haveEveryoneLookOverBow();
						return SailResult.CONTINUE;
					}
					else
					{
						announceToShip(L("<S-NAME> can not seem to travel @x1.",CMLib.directions().getInDirectionName(direction)));
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
				announceToShip(L("As there is no where to sail @x1, <S-NAME> meanders along the waves.",CMLib.directions().getInDirectionName(direction)));
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
				if((R!=null)
				&&((R.domainType()&Room.INDOORS)==0))
				{
					final Set<MOB> mobs=CMLib.players().getPlayersHere(R);
					for(final MOB mob : mobs)
					{
						if(mob == null)
							continue;
						final CMMsg lookMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK,null);
						final CMMsg lookExitMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK_EXITS,null);
						if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))
						&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH)
						&&(CMLib.flags().canBeSeenBy(targetR,mob)))
						{
							if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=CMProps.Int.EXVIEW_MIXED)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
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
		final String outerStr;
		final String innerStr = L("@x1 change(s) course, steering @x2.",name(mob),CMLib.directions().getDirectionName(dir));
		if(CMLib.flags().isSneaking(this))
			outerStr=null;
		else
			outerStr=innerStr;
		CMMsg msg=CMClass.getMsg(mob, null,null,CMMsg.MSG_NOISYMOVEMENT,innerStr,outerStr,outerStr);
		CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> change(s) course, steering @x1 @x2.",name(mob),CMLib.directions().getDirectionName(dir)));
		if((R.okMessage(mob, msg) && this.okAreaMessage(msg2, true)))
		{
			R.sendOthers(mob, msg);
			this.sendAreaMessage(msg2, true);
			this.courseDirection=dir | COURSE_STEER_MASK;
			return true;
		}
		return false;
	}

	protected boolean beginSail(final MOB mob, final Room R, final int dir)
	{
		directionFacing = dir;
		final String outerStr;
		final String innerStr = L("<S-NAME> sail(s) @x1 @x2.",name(mob),CMLib.directions().getDirectionName(dir));
		if(CMLib.flags().isSneaking(this))
			outerStr=null;
		else
			outerStr=innerStr;
		CMMsg msg2=CMClass.getMsg(mob, R, R.getExitInDir(dir), CMMsg.MSG_NOISYMOVEMENT, innerStr, outerStr,outerStr);
		if((R.okMessage(mob, msg2) && this.okAreaMessage(msg2, true)))
		{
			R.send(mob, msg2); // this lets the source know, i guess
			//this.sendAreaMessage(msg2, true); // this just sends to "others"
			this.courseDirection=dir;
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
			if((R!=null)&& CMLib.flags().isDeepWaterySurfaceRoom(R) &&(CMLib.map().getExtendedRoomID(R).length()>0))
				return R;
		}
		return null;
	}
	
	protected boolean safetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)
		|| R.amDestroyed() 
		|| ((!CMLib.flags().isFalling(this))
			&& (R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			&& (R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&& (getAnyExitDir(R)<0)))
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
			ticksSinceMove=0;
			this.unDock(false);
			R2.moveItemTo(this);
			this.dockHere(R2);
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

	@Override
	public void setRangeToTarget(int newRange)
	{
		//nothing to do atm
	}

	@Override
	public int rangeToTarget()
	{
		return getTacticalDistance(this.targetedShip);
	}

	@Override
	public boolean mayPhysicallyAttack(PhysicalAgent victim)
	{
		if(!mayIFight(victim))
			return false;
		return CMLib.map().roomLocation(this) == CMLib.map().roomLocation(victim);
	}

	protected static boolean ownerSecurityCheck(final String ownerName, final MOB mob)
	{
		return (ownerName.length()>0)
			 &&(mob!=null)
			 &&((mob.Name().equals(ownerName))
				||(mob.getLiegeID().equals(ownerName)&mob.isMarriedToLiege())
				||(CMLib.clans().checkClanPrivilege(mob, ownerName, Clan.Function.PROPERTY_OWNER)));
	}

	@Override
	public boolean mayIFight(PhysicalAgent victim)
	{
		final Area myArea=this.getShipArea();
		final PhysicalAgent defender=victim;
		MOB mob = null;
		if(myArea != null)
		{
			final LegalLibrary law=CMLib.law();
			int legalLevel=0;
			for(Enumeration<Room> r=myArea.getProperMap();r.hasMoreElements() && (legalLevel<2);)
			{
				final Room R=r.nextElement();
				if((R!=null)&&(R.numInhabitants()>0))
				{
					for(Enumeration<MOB> i=R.inhabitants();i.hasMoreElements();)
					{
						final MOB M=i.nextElement();
						if(M != null)
						{
							if(mob == null)
								mob = M;
							if((legalLevel==0)&&(mob.isMonster())&&(law.doesHaveWeakPriviledgesHere(M, R)))
							{
								mob=M;
								legalLevel=1;
							}
							if(M.isPlayer())
							{
								if(!mob.isPlayer())
								{
									mob=M;
									legalLevel=0;
								}
								if((legalLevel<2) && (law.doesHavePriviledgesHere(M, R)))
								{
									legalLevel=2;
									mob=M;
								}
								if((legalLevel<1) && (law.doesHaveWeakPriviledgesHere(M, R)))
								{
									legalLevel=1;
									mob=M;
								}
							}
						}
					}
				}
			}
		}
		if(mob==null)
			return false;
		return CMLib.combat().mayIAttackThisVessel(mob, defender);
	}

	@Override
	public void makePeace(boolean includePlayerFollowers)
	{
		clearTacticalMode();
	}
	
	@Override
	public PhysicalAgent getCombatant()
	{
		return this.targetedShip;
	}

	@Override
	public void setCombatant(PhysicalAgent other)
	{
		final Room R=(owner() instanceof Room)?(Room)owner():CMLib.map().roomLocation(this);
		if(other == null)
			clearTacticalMode();
		else
		if(CMLib.flags().isDeepWaterySurfaceRoom(R))
		{
			targetedShip = other;
			if(R != null)
				shipCombatRoom = R;
			if(other instanceof Combatant)
			{
				if(((Combatant)other).getCombatant()==null)
					((Combatant)other).setCombatant(this);
				if(other instanceof GenSailingShip)
					((GenSailingShip)other).amInTacticalMode(); // now he is in combat
			}
			amInTacticalMode(); // now he is in combat
		}
	}

	@Override
	public int getDirectionToTarget()
	{
		return this.getDirectionToTarget(this.targetedShip);
	}

	@Override
	public int getDirectionFacing()
	{
		return this.directionFacing;
	}

	@Override
	public boolean isAnchorDown()
	{
		return this.anchorDown;
	}

	@Override
	public void setAnchorDown(boolean truefalse)
	{
		this.anchorDown = truefalse;
	}
	
	@Override
	public PairList<Weapon,int[]> getSiegeWeaponAimings()
	{
		return this.aimings;
	}
	
	@Override
	public List<Integer> getCurrentCourse()
	{
		if((this.courseDirections.size()>0)
		&&(this.courseDirections.get(0).intValue()>=0))
		{
			return this.courseDirections;
		}
		return new ArrayList<Integer>(0);
	}

	@Override
	public void setCurrentCourse(List<Integer> course)
	{
		this.courseDirection=-1;
		this.courseDirections.clear();
		for(Integer dirIndex : course)
		{
			int dir=dirIndex.intValue();
			if(dir>=0)
			{
				if(courseDirection < 0)
				{
					courseDirection = dir;
					directionFacing = dir;
				}
				else
					this.courseDirections.add(Integer.valueOf(dir));
			}
		}
		this.courseDirections.add(Integer.valueOf(-1));
	}
	
	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES",
											"RESETTIME","RIDEBASIS","MOBSHELD",
											"AREA","OWNER","PRICE","DEFCLOSED","DEFLOCKED",
											"EXITNAME",
											"PUTSTR","MOUNTSTR","DISMOUNTSTR","STATESTR","STATESUBJSTR","RIDERSTR"
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
			return "" + defaultsClosed();
		case 11:
			return "" + defaultsLocked();
		case 12:
			return "" + doorName();
		case 13:
			return this.getPutString();
		case 14:
			return this.getMountString();
		case 15:
			return this.getDismountString();
		case 16:
			return this.getStateString();
		case 17:
			return this.getStateStringSubject();
		case 18:
			return this.getRideString();
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
			setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
			break;
		case 11:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
			break;
		case 12:
			this.doorName = val;
			break;
		case 13:
			setPutString(val);
			break;
		case 14:
			setMountString(val);
			break;
		case 15:
			setDismountString(val);
			break;
		case 16:
			setStateString(val);
			break;
		case 17:
			setStateStringSubject(val);
			break;
		case 18:
			setRideString(val);
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
