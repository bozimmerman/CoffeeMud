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

	protected volatile int		 courseDirection = -1;
	protected volatile boolean	 anchorDown		 = true;
	protected final List<Integer>courseDirections= new Vector<Integer>();

	protected volatile int			directionFacing	= 0;
	protected volatile Rideable		targetedShip	= null;
	protected volatile Room			shipCombatRoom	= null;
	protected PairList<Item,int[]>	coordinates		= null;
	protected PairList<Weapon,int[]>aimings			= null;
	
	protected int maxHullPoints = -1;
	
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
		SET_COURSE,
		TARGET,
		AIM
		;
	}
	
	protected void announceToDeck(final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(null, CMMsg.MSG_OK_ACTION, msgStr);
		announceToDeck(msg);
	}
	
	protected void announceToDeck(final CMMsg msg)
	{
		MOB mob = null;
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
		if((this.targetedShip != null)&&(this.targetedShip instanceof GenSailingShip))
		{
			final GenSailingShip targetShip = (GenSailingShip)this.targetedShip;
			final int[] targetCoords = targetShip.getMyCoords();
			if(targetCoords != null)
			{
				final String dist = ""+this.getTacticalDistance(targetCoords);
				final String dir=Directions.getDirectionName(targetShip.directionFacing);
				final String speed=""+targetShip.getShipSpeed();
				final String dirFromYou = Directions.getDirectionName(Directions.getRelativeDirection(getMyCoords(), targetCoords));
				return L("@x1 is @x2 of you sailing @x3 at a speed of @x4 and a distance of @x5.",targetShip.name(),dirFromYou,dir,speed,dist);
			}
		}
		return "";
	}

	protected int getShipSpeed()
	{
		int speed=phyStats().ability();
		if(speed <= 0)
			return 1;
		return speed;
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
				case TARGET:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another ship to target."));
						return false;
					}
					final Room thisRoom = (Room)owner();
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
				case AIM:
				{
					final Room thisRoom = (Room)owner();
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
							msg.source().tell(L("You don't see any seige weapon called '@x1' here.",leadStr));
							return false;
						}
						if(!isAShipSiegeWeapon(I))
						{
							msg.source().tell(L("@x1 is not a useable siege weapon.",leadStr));
							return false;
						}
						AmmunitionWeapon weapon=(AmmunitionWeapon)I;
						int distance = weapon.maxRange();
						int[] targetCoords = new int[2];
						if(this.targetedShip instanceof GenSailingShip)
						{
							targetCoords = ((GenSailingShip)this.targetedShip).getMyCoords();
							int direction = ((GenSailingShip)this.targetedShip).directionFacing;
							if(targetCoords == null)
							{
								msg.source().tell(L("You ship must be targeting an enemy to aim weapons."));
								return false;
							}
							distance = this.getTacticalDistance(targetCoords);
							int leadAmt = CMath.s_int(leadStr);
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
							msg.source().tell(L("@x1 needs to be LOADed first.",leadStr));
							return false;
						}
						String timeToFire=""+(CMLib.threads().msToNextTick((Tickable)CMLib.combat(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK) / 1000);
						this.aimings.removeFirst(weapon);
						this.aimings.add(new Pair<Weapon,int[]>(weapon,targetCoords));
						msg.source().tell(L("@x1 is now aimed and will be fired in @x2 seconds.",I.name(),timeToFire));
					}
					break;
				}
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
					this.courseDirection=-1;
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
					else
					{
						msg.source().tell(L("You can only STEER when not under a tactical threat.  Use COURSE to make tactical maneuvers."));
						return false;
					}
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
					final Room R=CMLib.map().roomLocation(this);
					if(R==null)
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
						return false;
					}
					if(!this.amInTacticalMode())
					{
						int dir=Directions.getCompassDirectionCode(secondWord);
						if(dir<0)
						{
							msg.source().tell(L("Sail the ship which direction?"));
							return false;
						}
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
						msg.source().tell(L("You can only SAIL when not under a tactical threat.  Use COURSE to make tactical maneuvers."));
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
					this.courseDirection=-1;
					final Room R=CMLib.map().roomLocation(this);
					if(R==null)
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
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
						final String dirFacingName = Directions.getDirectionName(directionFacing);
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
							int dir=Directions.getCompassDirectionCode(dirWord);
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
									if((coordinates == null) || (getTacticalDistance(coordinates) >= R.maxRange()))
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
												dirFacingName,Directions.getDirectionName(otherDir)));
								return false;
							}
							else
							if(dir != directionFacing)
								otherDir = dir;
							dirNames.add(Directions.getDirectionName(dir).toLowerCase());
							this.courseDirections.add(Integer.valueOf(dir));
						}
						if(this.courseDirections.size()>0)
							this.courseDirection = this.courseDirections.get(0).intValue();
						
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
					}
				}
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() == owner())
		&&(msg.source().riding() != null)
		&&(this.targetedShip == msg.source().riding())
		&&(CMLib.flags().isWaterWorthy(msg.source().riding())))
		{
			msg.source().tell(L("Your small vessel can not get away during combat."));
			return false;
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
			if(!CMLib.combat().mayIAttack(sourceM, this, (Rideable)I))
			{
				sourceM.tell(L("You are not permitted to attack @x1",I.name()));
				return Boolean.FALSE;
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
					targetedShip	 = (Rideable)I;
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
		}
		if(tickID == sailingTickID)
		{
			if((!this.anchorDown) && (area != null) && (courseDirection != -1) )
			{
				int speed=phyStats().ability();
				if(speed <= 0)
					speed=1;
				
				for(int s=0;s<speed && (courseDirection>=0);s++)
				{
					switch(sail(courseDirection))
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
					PairList<Item,int[]> coords = this.coordinates;
					if(coords != null)
					{
						for(Iterator<Item> i= coords.firstIterator(); i.hasNext();)
						{
							Item I=i.next();
							if((I instanceof GenSailingShip)
							&&(((GenSailingShip)I).targetedShip == this))
								((GenSailingShip)I).announceToDeck(this.getTargetedShipInfo());
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
						for(Weapon w : weapons)
						{
							final Room R=CMLib.map().roomLocation(w);
							if(R!=null)
							{
								mob.setLocation(R);
								//mob.setRangeToTarget(0);
								int index = aimings.indexOfFirst(w);
								if(index >= 0)
								{
									int[] coordsAimedAt = aimings.getSecond(index);
									boolean wasHit = Arrays.equals(coordsAimedAt, coordsToHit);
									CMLib.combat().postAttack(mob, this, this.targetedShip, w, wasHit);
								}
							}
						}
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
		if(isAShipSiegeWeapon(I))
		{
			if(((Rideable)I).riderCapacity() > 0)
				return ((Rideable)I).numRiders() >= ((Rideable)I).riderCapacity();
			return true;
		}
		return false;
	}
	
	protected final boolean isAShipSiegeWeapon(Item I)
	{
		if((I instanceof AmmunitionWeapon)
		&&(I instanceof Rideable)
		&&((!CMLib.flags().isGettable(I))||(I.basePhyStats().weight()>=250))
		&&(((AmmunitionWeapon)I).requiresAmmunition()))
			return true;
		return false;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(((msg.sourceMajor(CMMsg.MASK_SOUND))||(msg.sourceMajor(CMMsg.MASK_MOVE)))
		&&(msg.source().location()==owner())
		&&((msg.source().riding()!=this)))
		{
			this.sendAreaMessage(msg, true);
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
					if(this.anchorDown)
					{
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, 
								CMMsg.MSG_OK_VISUAL, L("\n\r^HThe anchor on @x1 is lowered, holding her in place.^.^?",name(msg.source())), 
								CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
					}
					else
					if((this.courseDirection >= 0)&&(this.courseDirections.size()>0))
					{
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, 
								CMMsg.MSG_OK_VISUAL, L("\n\r^H@x1 is under full sail, traveling @x2^.^?",name(msg.source()), Directions.getDirectionName(courseDirection)), 
								CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
					}
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
						clearTacticalMode();
					}
					if((!msg.source().Name().equals(Name()))
					&&(!getDestinationRoom().isHere(msg.tool()))) // whats this second condition for?
					{
						sendAreaMessage(CMClass.getMsg(msg.source(), msg.target(), msg.tool(), CMMsg.MSG_OK_VISUAL, null, null, 
								L("^HOff the deck you see: ^N")+msg.othersMessage()), true);
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
			case CMMsg.TYP_WEAPONATTACK:
				if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
				{
					Weapon weapon=null;
					if((msg.tool() instanceof Weapon))
						weapon=(Weapon)msg.tool();
					if((weapon!=null)&&(msg.source().riding()!=null))
					{
						final boolean isHit=msg.value()>0;
						if(isHit && this.isAShipSiegeWeapon(weapon) 
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
								while(shotsRemaining-- > 0)
								{
									final Pair<MOB,Room> randomPair = (targets.size()>0)? targets.get(CMLib.dice().roll(1,targets.size(),-1)) : null;
									if((CMLib.dice().rollPercentage() < chanceToHit)&&(randomPair != null))
									{
										msg.source().setLocation(randomPair.second);
										double pctLoss = CMath.div(msg.value(), maxHullPoints);
										int pointsLost = (int)Math.round(pctLoss * msg.source().maxState().getHitPoints());
										CMLib.combat().postWeaponDamage(msg.source(), randomPair.first, weapon, pointsLost);
									}
									else
									if(randomPair != null)
									{
										msg.source().setLocation(randomPair.second);
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
							CMLib.combat().postWeaponAttackResult(msg.source(), msg.source().riding(), this, weapon, isHit);
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				if(msg.value() > 0)
				{
					if(maxHullPoints < 0)
					{
						maxHullPoints = 10 * this.getShipArea().numberOfProperIDedRooms();
					}
					double pctLoss = CMath.div(msg.value(), maxHullPoints);
					int pointsLost = (int)Math.round(pctLoss * maxHullPoints);
					if(pointsLost > 0)
					{
						int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
						final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
						final String msgStr = (msg.targetMessage() == null) ? L("<O-NAME> fired from <S-NAME> hits and @x1 the ship.",hitWord) : msg.targetMessage();
						final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
						this.announceActionToDeckOrUnderdeck(msg.source(), deckHitMsg, 0);
						final CMMsg underdeckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, L("Something hits and @x1 the ship.",hitWord));
						this.announceActionToDeckOrUnderdeck(msg.source(), underdeckHitMsg, Room.INDOORS);
						if(pointsLost >= this.usesRemaining())
						{
							this.setUsesRemaining(0);
							CMLib.tracking().makeSink(this, CMLib.map().roomLocation(this), 0);
							final String sinkString = L("<O-NAME> start(s) sinking!");
							this.announceActionToUnderDeck(msg.source(), sinkString);
							
							if(msg.source().riding() instanceof BoardableShip)
							{
								final Area A=this.getShipArea();
								if(A!=null)
								{
									for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
									{
										final Room R=r.nextElement();
										if(R!=null)
										{
											for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
											{
												final MOB M=m.nextElement();
												CMLib.leveler().postExperience(M, null, null, 500, false);
											}
										}
									}
								}
							}
							else
								CMLib.leveler().postExperience(msg.source(), null, null, 500, false);
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
				&&(this.isAShipSiegeWeapon((Item)msg.target())))
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
	}
	
	@Override
	public long expirationDate()
	{
		final Room R=CMLib.map().roomLocation(this);
		if(R==null)
			return 0;
		if(CMLib.flags().isUnderWateryRoom(R))
		{
			if(dispossessionTime == 0)
				setExpirationDate(System.currentTimeMillis()+(CMProps.getIntVar(CMProps.Int.EXPIRE_PLAYER_DROP) * TimeManager.MILI_MINUTE));
		}
		else
			dispossessionTime = 0;
		return super.expirationDate();
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
	
	protected int getTacticalDistance(int[] fromCoords)
	{
		final PairList<Item,int[]> coords = this.coordinates;
		int lowest = CMLib.map().roomLocation(this).maxRange();
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
				// because of the 'you can only turn on last move' rule, this is unimportant.
				//directionFacing = Directions.getGradualDirectionCode(directionFacing, direction);
				directionFacing = direction;
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
						final int newDistance = this.getTacticalDistance(tacticalCoords);
						if((newDistance <= oldDistance)||(newDistance < thisRoom.maxRange()))
						{
							final int[] newCoords = Directions.adjustXYByDirections(tacticalCoords[0], tacticalCoords[1], direction);
							final int[] adj=this.getCoordAdjustments(newCoords);
							final String coords = (newCoords[0]+adj[0])+","+(newCoords[1]+adj[1]);
							final CMMsg maneuverMsg=CMClass.getMsg(mob,thisRoom,null,CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,directionName,
									CMMsg.MSG_ADVANCE,L("<S-NAME> maneuver(s) @x1 to (@x2).",directionName,coords));
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
						final CMMsg maneuverMsg=CMClass.getMsg(mob,thisRoom,null,CMMsg.MSG_ADVANCE,directionName,CMMsg.MSG_ADVANCE,null,
								CMMsg.MSG_ADVANCE,L("<S-NAME> change(s) coarse, turning @x1.",directionName));
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
				if((!CMLib.flags().isDeepWaterySurfaceRoom(destRoom))
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
					this.courseDirection = -1;
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
			this.courseDirection=dir;
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
