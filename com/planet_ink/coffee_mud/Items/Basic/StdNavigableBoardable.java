package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

import java.util.*;

/*
   Copyright 2021-2021 Bo Zimmerman

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
public class StdNavigableBoardable extends StdSiegableBoardable implements NavigableItem
{
	@Override
	public String ID()
	{
		return "StdNavigableBoardable";
	}

	protected volatile int				courseDirection		= -1;
	protected volatile boolean			anchorDown			= true;
	protected final List<Integer>		courseDirections	= new Vector<Integer>();
	protected volatile int				directionFacing		= 0;
	protected volatile int				ticksSinceMove		= 0;
	protected volatile Item				tenderItem			= null;
	protected List<Item>				smallTenderRequests	= new SLinkedList<Item>();
	protected volatile Room				prevItemRoom		= null;

	protected String		verb_sail		= "navigate";
	protected String		verb_sailing	= "navigating";
	protected String		anchor_name		= "anchor";
	protected String		anchor_verbed	= "lowered";

	public StdNavigableBoardable()
	{
		super();
		setName("a navigable transport");
		setDisplayText("a navigable transport is here.");
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(2);
		noun_word = "transport";
		head_offTheDeck = "^HOff the side you see: ^N";
		this.recoverPhyStats();
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
	public Basis navBasis()
	{
		return Rideable.Basis.ENTER_IN;
	}

	public void fixArea(final Area area)
	{
		final Ability oldA=area.fetchEffect("NavigableListener");
		if(oldA!=null)
			area.delEffect(oldA);
		final ExtendableAbility extAble = (ExtendableAbility)CMClass.getAbility("ExtAbility");
		extAble.setAbilityID("NavigableListener");
		extAble.setName("Navigable Listener");
		extAble.setSavable(false);
		final StdNavigableBoardable thisMe = this;
		extAble.setMsgListener(new MsgListener()
		{
			final StdNavigableBoardable me=thisMe;
			final Area meA=area;

			protected void lookOverBow(final Room R, final CMMsg msg)
			{
				msg.addTrailerRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						if(CMLib.flags().canBeSeenBy(R, msg.source()) && (msg.source().session()!=null))
							msg.source().session().print(L(me.head_offTheDeck));
						final CMMsg msg2=CMClass.getMsg(msg.source(), R, msg.tool(), msg.sourceCode(), null, msg.targetCode(), null, msg.othersCode(), null);
						if((msg.source().isAttributeSet(MOB.Attrib.AUTOEXITS))
						&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
							msg2.addTrailerMsg(CMClass.getMsg(msg.source(),R,null,CMMsg.MSG_LOOK_EXITS,null));
						if(R.okMessage(msg.source(), msg))
							R.send(msg.source(),msg2);
					}
				});
			}

			@Override
			public void executeMsg(final Environmental myHost, final CMMsg msg)
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_LOOK:
				case CMMsg.TYP_EXAMINE:
					if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isOpen()))
					{
						final Room hereR=msg.source().location();
						if((hereR!=null)
						&&(me.canViewOuterRoom(hereR))
						&&(hereR.getArea()==meA))
						{
							final Room lookingR=hereR.getRoomInDir(CMLib.map().getExitDir(hereR, (Exit)msg.target()));
							final Room R=CMLib.map().roomLocation(me);
							if(lookingR==R)
								lookOverBow(R,msg);
						}
					}
					else
					if((msg.target() instanceof Room)
					&&(me.canViewOuterRoom((Room)msg.target()))
					&&(((Room)msg.target()).getArea()==meA))
					{
						if(msg.targetMinor()==CMMsg.TYP_EXAMINE)
						{
							final Room R=CMLib.map().roomLocation(me);
							if((R!=null)
							&&(R.getArea()!=meA))
								lookOverBow(R,msg);
						}
					}
					break;
				}
			}

			@Override
			public boolean okMessage(final Environmental myHost, final CMMsg msg)
			{
				return true;
			}

		});
		area.addNonUninvokableEffect(extAble);
		extAble.setSavable(false);
	}

	@Override
	public Area getArea()
	{
		if((!destroyed)
		&&(area==null))
		{
			final Area area=super.getArea();
			if(area != null)
				area.setTheme(Area.THEME_FANTASY);
			fixArea(area);
			return area;
		}
		return super.getArea();
	}


	@Override
	public void setArea(final String xml)
	{
		super.setArea(xml);
		if(this.area!=null)
			fixArea(this.area);
	}

	protected enum NavigatingCommand
	{
		RAISE_ANCHOR,
		LOWER_ANCHOR,
		STEER,
		NAVIGATE,
		COURSE,
		SET_COURSE,
		TENDER,
		RAISE,
		LOWER,
		JUMP
		;
	}

	private final static Map<String, NavigatingCommand> commandWords = new Hashtable<String, NavigatingCommand>();

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(this.area!=null)
			this.phyStats.setWeight(phyStats.weight()+(this.area.numberOfProperIDedRooms() * 1200));
	}

	protected NavigatingCommand findNavCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		NavigatingCommand cmd=null;
		if(commandWords.size()==0)
		{
			for(final NavigatingCommand N : NavigatingCommand.values())
				commandWords.put(N.name().toUpperCase().trim(), N);
		}

		if((secondWord!=null)&&(secondWord.length()>0))
			cmd = commandWords.get((word+"_"+secondWord).toUpperCase().trim());
		if(cmd == null)
			cmd = commandWords.get(word.toUpperCase().trim());
		return cmd;
	}

	@Override
	public int getMaxSpeed()
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

	protected boolean canTenderFromHere(final Room R)
	{
		if(((R.domainType()&Room.INDOORS)!=0)
		&& (R.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
			return false;
		return true;
	}

	protected boolean canSteer(final MOB mob, final Room R)
	{
		if((R.domainType()&Room.INDOORS)!=0)
		{
			if(mob.isPlayer())
				mob.tell(L("You must be on deck to steer your "+noun_word+"."));
			return false;
		}
		return true;
	}

	protected boolean canJumpFromHere(final Room R)
	{
		if(((R.domainType()&Room.INDOORS)!=0)
		&& (R.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
			return false;
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=(cmds.size()>1) ? cmds.get(1).toUpperCase() : "";
			final NavigatingCommand cmd=this.findNavCommand(word, secondWord);
			if(cmd != null)
			{
				switch(cmd)
				{
				case TENDER:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another "+noun_word+" to offer aboard."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This "+noun_word+" is nowhere to be found!"));
						return false;
					}
					if(this.siegeTarget!=null)
					{
						msg.source().tell(L("Not while you are in combat!"));
						return false;
					}
					final String rest = CMParms.combine(cmds,1);
					final Item I=thisRoom.findItem(rest);
					if((I instanceof StdNavigableBoardable)&&(I!=this)&&(CMLib.flags().canBeSeenBy(I, msg.source())))
					{
						final StdNavigableBoardable tenderToI = (StdNavigableBoardable)I;
						if(tenderToI.siegeTarget != null)
						{
							msg.source().tell(L("Not while @x1 is in in combat!",tenderToI.Name()));
							return false;
						}
						final MOB mob = createNavMob(thisRoom);
						try
						{
							if(tenderToI.tenderItem == this)
							{
								final BoardableItem myArea=(BoardableItem)this.getArea();
								final BoardableItem hisArea=(BoardableItem)tenderToI.getArea();
								if((myArea.getIsDocked()==null)||(hisArea.getIsDocked()==null))
								{
									msg.source().tell(L("Both ships must first be docked."));
									return false;
								}
								if(thisRoom.show(mob, tenderToI, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> connect(s) her gangplank with <T-NAME>")))
								{
									this.tenderItem = tenderToI;
									final Room hisExitRoom = hisArea.unDock(false);
									final Room myExitRoom = myArea.unDock(false);
									myArea.dockHere(hisExitRoom);
									hisArea.dockHere(myExitRoom);
								}
							}
							else
							{
								if(thisRoom.show(mob, tenderToI, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> extend(s) her gangplank toward(s) <T-NAME>")))
									this.tenderItem = tenderToI;
							}
						}
						finally
						{
							mob.destroy();
						}
						return false;
					}
					else
					{
						msg.source().tell(L("You don't see the "+noun_word+" '@x1' here to board",rest));
						return false;
					}
				}
				case JUMP:
				{
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This "+noun_word+" is nowhere to be found!"));
						return false;
					}
					final MOB mob=msg.source();
					final Room mobR = mob.location();
					if(mobR != null)
					{
						if(cmds.size()<2)
							mobR.show(mob, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> jump(s) in place."));
						else
						{
							final String str=CMParms.combine(cmds,1).toLowerCase();
							if(("overboard").startsWith(str) || ("water").startsWith(str))
							{
								if(!canJumpFromHere(mobR))
								{
									mob.tell(L("You must be on deck to jump overboard."));
									return false;
								}
								if(mobR.show(mob, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> jump(s) overboard.")))
								{
									CMLib.tracking().walkForced(mob, mobR, thisRoom, true, true, L("<S-NAME> arrive(s) from @x1.",name()));
									if((mob.location()==thisRoom)
									&&(!CMLib.flags().isWateryRoom(thisRoom)))
									{
										final int directDamage = mob.maxState().getHitPoints()/10;
										CMLib.combat().postDamage(mob,mob,null,directDamage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,
												L("The fall <DAMAGES> <T-NAME>!"));
									}
								}
							}
							else
								msg.source().tell(L("Jump where?  Try JUMP OVERBOARD."));
						}
						return false;
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
						if(!canTenderFromHere(targetR))
						{
							msg.source().tell(L("You must be on deck to raise a tendered item."));
							return false;
						}
						final String rest = CMParms.combine(cmds,1);
						final Item I=R.findItem(rest);
						if((I!=this)
						&&(I!=null)
						&&(CMLib.flags().canBeSeenBy(I, msg.source())))
						{
							if((I instanceof Rideable)
							&&(((Rideable)I).mobileRideBasis())
							&&(!(I instanceof BoardableItem)))
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
									msg.source().tell(L("You can only raise @x1 once it has tendered itself.",I.name()));
									return false;
								}
							}
							else
							{
								msg.source().tell(L("You don't think @x1 is a suitable target.",I.name()));
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
						if(!canTenderFromHere(targetR))
						{
							msg.source().tell(L("You must be on deck to lower that."));
							return false;
						}
						final String rest = CMParms.combine(cmds,1);
						final Item I=R.findItem(rest);
						if((I!=this)&&(CMLib.flags().canBeSeenBy(I, msg.source())))
						{
							if((I instanceof Rideable)
							&&(((Rideable)I).mobileRideBasis())
							&&((((Rideable)I).rideBasis()==Rideable.Basis.WATER_BASED)
								||(((Rideable)I).rideBasis()==Rideable.Basis.AIR_FLYING)
								||(((Rideable)I).rideBasis()==Rideable.Basis.LAND_BASED)
								||(((Rideable)I).rideBasis()==Rideable.Basis.WAGON)))
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
				case RAISE_ANCHOR:
				{
					if(disableCmds.contains("ANCHOR"))
						return true;
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The "+noun_word+" has moved!"));
						return false;
					}
					final Room R=CMLib.map().roomLocation(this);
					if(!anchorDown)
						msg.source().tell(L("The "+anchor_name+" is not "+anchor_verbed+"."));
					else
					if(R!=null)
					{
						final CMMsg msg2=CMClass.getMsg(msg.source(), this, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> "+word.toLowerCase()+"(s) the "+anchor_name+" on <T-NAME>."));
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
					if(disableCmds.contains("ANCHOR"))
						return true;
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The "+noun_word+" has moved!"));
						return false;
					}
					final Room R=CMLib.map().roomLocation(this);
					if(anchorDown)
						msg.source().tell(L("The "+anchor_name+" is already "+anchor_verbed+"."));
					else
					if(R!=null)
					{
						final CMMsg msg2=CMClass.getMsg(msg.source(), this, null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> "+word.toLowerCase()+"(s) the "+anchor_name+" on <T-NAME>."));
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
						msg.source().tell(L("The "+noun_word+" won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The "+noun_word+" has moved!"));
						return false;
					}
					if((courseDirection >=0)||(courseDirections.size()>0))
					{
						if(!this.amInTacticalMode())
							msg.source().tell(L("Your previous course has been cancelled."));
						courseDirection = -1;
						courseDirections.clear();
					}
					final int dir=CMLib.directions().getCompassDirectionCode(secondWord);
					if(dir<0)
					{
						msg.source().tell(L("Steer the "+noun_word+" which direction?"));
						return false;
					}
					final Room R=CMLib.map().roomLocation(this);
					if((R==null)||(msg.source().location()==null))
					{
						msg.source().tell(L("You are nowhere, so you won`t be moving anywhere."));
						return false;
					}
					if(!canSteer(msg.source(), msg.source().location()))
						return false;
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
							msg.source().tell(L("Your "+noun_word+" is already "+verb_sailing+" @x1.",dirName));
							return false;
						}
					}
					if(anchorDown)
					{
						msg.source().tell(L("The "+anchor_name+" is "+anchor_verbed+", so you won`t be moving anywhere."));
						return false;
					}
					break;
				}
				case NAVIGATE:
				{
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					if(CMLib.flags().isFalling(this) || ((this.subjectToWearAndTear() && (usesRemaining()<=0))))
					{
						msg.source().tell(L("The "+noun_word+" won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The "+noun_word+" has moved!"));
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
					if(!canSteer(msg.source(), msg.source().location()))
						return false;
					final int dir=CMLib.directions().getCompassDirectionCode(secondWord);
					if(dir<0)
					{
						msg.source().tell(L(""+CMStrings.capitalizeFirstLetter(verb_sail)+" the "+noun_word+" which direction?"));
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
						final String dirName = CMLib.directions().getDirectionName(directionFacing);
						directionFacing = getDirectionFacing(dir);
						if(dir != this.directionFacing)
						{
							msg.source().tell(L("When in tactical mode, your "+noun_word+" can only "+verb_sail.toUpperCase()+
											    " @x1.  Use COURSE for more complex maneuvers, or STEER.",dirName));
							return false;
						}
					}
					if(anchorDown)
					{
						msg.source().tell(L("The "+anchor_name+" is "+anchor_verbed+", so you won`t be moving anywhere."));
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
						msg.source().tell(L("The "+noun_word+" won't seem to move!"));
						return false;
					}
					if(safetyMove())
					{
						msg.source().tell(L("The "+noun_word+" has moved!"));
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
					if(!canSteer(msg.source(), msg.source().location()))
						return false;
					int dirIndex = 1;
					if(word.equals("SET"))
						dirIndex = 2;
					int firstDir = -1;
					this.courseDirections.clear();
					if(amInTacticalMode())
					{
						final int speed=getMaxSpeed();
						final String dirFacingName = CMLib.directions().getDirectionName(directionFacing);
						if(dirIndex >= cmds.size())
						{
							msg.source().tell(L("Your "+noun_word+" is currently "+verb_sailing+" @x1. To set a course, you must specify up to @x2 directions of travel, "
												+ "of which only the last may be something other than @x3.",dirFacingName,""+speed,dirFacingName));
							return false;
						}
						final List<String> dirNames = new ArrayList<String>();
						final int[] coordinates = Arrays.copyOf(getTacticalCoords(),2);
						int otherDir = -1;
						for(;dirIndex<cmds.size();dirIndex++)
						{
							final String dirWord=cmds.get(dirIndex);
							final int dir=CMLib.directions().getCompassDirectionCode(dirWord);
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
										msg.source().tell(L("There doesn't look to be anywhere you can "+verb_sail+" in that direction."));
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
						this.courseDirection = this.removeTopCourse();
						if((this.courseDirections.size()==0)||(getBottomCourse()>=0))
							this.courseDirections.add(Integer.valueOf(-1));

						this.announceToOuterViewers(msg.source(),L("<S-NAME> order(s) a course setting of @x1.",CMLib.english().toEnglishStringList(dirNames.toArray(new String[0]))));
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
							final int dir=CMLib.directions().getCompassDirectionCode(dirWord);
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
						if((this.courseDirections.size()==0)||(getBottomCourse()>=0))
							this.courseDirections.add(Integer.valueOf(-1));
						steer(msg.source(),R, firstDir);
					}
					if(anchorDown)
						msg.source().tell(L("The "+anchor_name+" is "+anchor_verbed+", so you won`t be moving anywhere."));
					return false;
				}
				}
			}
			if(cmd != null)
			{
				cmds.add(0, "METAMSGCOMMAND");
				double speed=getMaxSpeed();
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
			final NavigatingCommand cmd=this.findNavCommand(word, secondWord);
			if(cmd == null)
				return true;
			switch(cmd)
			{
			case NAVIGATE:
			{
				final int dir=CMLib.directions().getCompassDirectionCode(secondWord);
				if(dir<0)
					return false;
				final Room R=CMLib.map().roomLocation(this);
				if(R==null)
					return false;
				if(!this.amInTacticalMode())
				{
					this.courseDirections.clear(); // sail eliminates a course
					this.courseDirections.add(Integer.valueOf(-1));
					this.beginNavigate(msg.source(), R, dir);
				}
				else
				{
					if(this.courseDirections.size()>0)
						msg.source().tell(L("Your prior course has been overridden."));
					this.courseDirections.clear();
					this.courseDirections.add(Integer.valueOf(-1));
					this.courseDirection = dir;
					this.announceToOuterViewers(msg.source(),L("<S-NAME> start(s) "+verb_sailing+" @x1.",CMLib.directions().getDirectionName(dir)));
				}
				return false;
			}
			case STEER:
			{
				final int dir=CMLib.directions().getCompassDirectionCode(secondWord);
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
					this.announceToOuterViewers(msg.source(),L("<S-NAME> start(s) steering the "+noun_word+" @x1.",CMLib.directions().getDirectionName(dir)));
				}
				return false;
			}
			default:
				// already done...
				return false;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() instanceof Room)
		&&(msg.target() == owner()))
		{
			if(disableCmds.contains("LEAVE"))
				return false;
			if((msg.source().riding() != null)
			&&(this.siegeTarget == msg.source().riding()))
			{
				msg.source().tell(L("You can not get away during combat."));
				return false;
			}
			if(msg.tool() instanceof Exit)
			{
				final Room R=msg.source().location();
				final int dir=CMLib.map().getExitDir(R,(Exit)msg.tool());
				if((dir >= 0)
				&&(R.getRoomInDir(dir)!=null)
				&&(R.getRoomInDir(dir).getArea()==this.getArea())
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
		&&(msg.source().location().getArea()==this.getArea())
		&&(msg.tool() instanceof Exit)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().indexOf("<S-NAME>")>=0)
		&&(msg.othersMessage().indexOf(L(CMLib.flags().getPresentDispositionVerb(msg.source(),CMFlagLibrary.ComingOrGoing.ARRIVES)))>=0))
		{
			if(disableCmds.contains("LEAVE"))
				return false;
			msg.setOthersMessage(L("<S-NAME> disembark(s) @x1.",Name()));
		}
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
			final NavigatingCommand cmd=this.findNavCommand(word, secondWord);
			if(cmd != null)
			{
				switch(cmd)
				{
				default:
					break;
				case TENDER:
				{
					if(disableCmds.contains("TENDER"))
						return false;
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify another "+noun_word+" to offer to board."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This "+noun_word+" is nowhere to be found!"));
						return false;
					}
					/*//TODO: maybe check to see if the lil "+noun_word+" is
					if(this.targetedI!=null)
					{
						msg.source().tell(L("Not while you are in combat!"));
						return false;
					}
					*/
					final String rest = CMParms.combine(cmds,1);
					final Item meI=thisRoom.findItem(rest);
					if((meI==this)
					&&(CMLib.flags().canBeSeenBy(this, msg.source())))
					{
						if(siegeTarget != null)
						{
							msg.source().tell(L("Not while @x1 is in in combat!",Name()));
							return false;
						}
						final Room R=CMLib.map().roomLocation(msg.source());
						if((R!=null)
						&&(R.show(msg.source(), this, CMMsg.TYP_ADVANCE, L("<S-NAME> tender(s) @x1 alonside <T-NAME>, waiting to be raised on board.",msg.source().riding().name()))))
						{
							for(final Iterator<Item> i=smallTenderRequests.iterator();i.hasNext();)
							{
								final Item I=i.next();
								if(!R.isContent(I))
									smallTenderRequests.remove(I);
							}
							final Rideable sR=msg.source().riding();
							if(sR instanceof Item)
							{
								final Item isR = (Item)sR;
								if(!smallTenderRequests.contains(isR))
									smallTenderRequests.add(isR);
							}
						}
						return false;
					}
					else
					{
						msg.source().tell(L("You don't see the "+noun_word+" '@x1' here to tender with",rest));
						return false;
					}
				}
				}
			}
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
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
		switch(directionFacing)
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

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final int navTickID = amInTacticalMode() ? Tickable.TICKID_SPECIALMANEUVER : Tickable.TICKID_AREA;
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			final Area area = this.getArea();
			if(area instanceof BoardableItem)
			{
				if((this.tenderItem != null)
				&&(this.tenderItem.owner()==owner())
				&&(this.siegeTarget==null)
				&&(this.tenderItem instanceof StdNavigableBoardable)
				&&(((StdNavigableBoardable)this.tenderItem).siegeTarget==null))
				{
					// yay!
				}
				else
				{
					if(this.tenderItem != null)
						this.tenderItem=null;
					if((((BoardableItem)area).getIsDocked() != owner())
					&&(owner() instanceof Room))
					{
						this.dockHere((Room)owner());
					}
				}
			}
		}
		if(tickID == navTickID)
		{
			ticksSinceMove++;
			if((!this.anchorDown)
			&& (area != null)
			&& (courseDirection != -1) )
			{
				final int speed=getMaxSpeed();
				for(int s=0;s<speed && (courseDirection>=0);s++)
				{
					switch(navMove(courseDirection & 127))
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
							this.courseDirection = this.removeTopCourse();
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
					final Room combatRoom=this.siegeCombatRoom;
					if(combatRoom != null)
					{
						final MOB mob = createNavMob(null);
						try
						{
							combatRoom.show(mob, this, CMMsg.MSG_ACTIVATE|CMMsg.MASK_MALICIOUS, null);
						}
						finally
						{
							mob.destroy();
						}
					}
					final PairList<Item,int[]> coords = this.coordinates;
					if(coords != null)
					{
						for(final Iterator<Item> i= coords.firstIterator(); i.hasNext();)
						{
							final Item I=i.next();
							if((I instanceof SiegableItem)
							&&(I instanceof StdBoardable)
							&&(((SiegableItem)I).getCombatant() == this))
								((StdBoardable)I).announceToOuterViewers(getTacticalView((SiegableItem)I));
						}
					}
				}

			}
		}
		return super.tick(ticking, tickID);
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

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.target() == this)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
			{
				final StringBuilder visualCondition = new StringBuilder("");
				if(this.anchorDown)
					visualCondition.append(L("^HThe "+anchor_name+" on @x1 is "+anchor_verbed+", holding her in place.^.^?",name(msg.source())));
				else
				if((this.courseDirection >= 0)
				&&(getTopCourse()>=0))
					visualCondition.append(L("^H@x1 is "+verb_sailing+" @x2^.^?",CMStrings.capitalizeFirstLetter(name(msg.source())), CMLib.directions().getDirectionName(courseDirection & 127)));
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
							msg.trailerRunnables().remove(this);
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
						visualCondition.append(L("\n\r^HThe "+anchor_name+" on @x1 is "+anchor_verbed+", holding her in place.^.^?",name(msg.source())));
					else
					if((this.courseDirection >= 0)
					&&(getTopCourse()>=0))
						visualCondition.append(L("\n\r^H@x1 is "+verb_sailing+" @x2^.^?",name(msg.source()), CMLib.directions().getDirectionName(courseDirection & 127)));
					if(visualCondition.length()>0)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, visualCondition.toString(), -1, null, -1, null));
				}
				break;
			}
		}
	}

	@Override
	public void setExpirationDate(final long time)
	{
		// necessary because stdboardable protects its things in rooms
		this.dispossessionTime=time;
	}

	@Override
	public String getTacticalView(final SiegableItem viewer)
	{
		final int[] targetCoords = getTacticalCoords();
		final int[] myCoords;
		final String dist = ""+getTacticalDistance(viewer);
		if(viewer instanceof PhysicalAgent)
		{
			myCoords = viewer.getTacticalCoords();
			if((myCoords!=null)&&(targetCoords != null))
			{
				final String dir=CMLib.directions().getDirectionName(getDirectionFacing());
				final String speed=""+getMaxSpeed();
				final String dirFromYou = CMLib.directions().getDirectionName(Directions.getRelative11Directions(myCoords, targetCoords));
				return L("@x1 is @x2 of you "+verb_sailing+" @x3 at a speed of @x4 and a distance of @x5.",name(),dirFromYou,dir,speed,dist);
			}
			else
				return L("@x1 is at a distance of @x2.",name(),dist);
		}
		else
			return L("@x1 is at a distance of @x2.",name(),dist);
	}

	@Override
	public long expirationDate()
	{
		final Room R=CMLib.map().roomLocation(this);
		if(R==null)
			return 0;
		return super.expirationDate();
	}

	private static enum NavResult
	{
		CANCEL,
		CONTINUE,
		REPEAT
	}

	protected int[] getCoordAdjustments(final int[] newOnes)
	{
		final PairList<Item,int[]> coords = this.coordinates;
		final int[] lowests = new int[2];
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
					final Pair<Item,int[]> P = coords.get(p);
					if((newOnes==null)||(P.first!=this))
					{
						if(P.second[0] < lowests[0])
							lowests[0]=P.second[0];
						if(P.second[1] < lowests[1])
							lowests[1]=P.second[1];
					}
				}
				catch(final Exception e)
				{
				}
			}
		}
		lowests[0]=-lowests[0];
		lowests[1]=-lowests[1];
		return lowests;
	}

	protected int getDirectionFacing(final int direction)
	{
		if(directionFacing < 0)
			return direction;
		return directionFacing;
	}

	@Override
	public void setDirectionFacing(final int direction)
	{
		this.directionFacing=direction;
	}

	protected boolean preNavigateCheck(final Room thisRoom, final int direction, final Room destRoom)
	{
		return true;
	}

	protected MOB createNavMob(final Room thisRoom)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
		mob.setRiding(this);
		if(getOwnerObject() instanceof Clan)
			mob.setClan(getOwnerObject().name(), ((Clan)getOwnerObject()).getAutoPosition());
		return mob;
	}

	protected NavResult navMove(final int direction)
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
						final Pair<Item,int[]> pair = (x>=0) ? this.coordinates.get(x) : null;
						if(pair == null)
							break;
						else
						if(pair.first != this)
							x=this.coordinates.indexOfFirst(this);
						else
							tacticalCoords = pair.second;
					}
				}
				catch(final Exception e)
				{
				}
			}
			if(tacticalCoords != null)
			{
				final MOB mob = createNavMob(thisRoom);
				try
				{
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
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+" maneuvers to "+CMParms.toListString(tacticalCoords));
							final int newDistance = this.getLowestTacticalDistanceFromThis();
							ticksSinceMove=0;
							if((newDistance <= oldDistance)||(newDistance < thisRoom.maxRange()))
								return NavResult.CONTINUE;
						}
						else
							return NavResult.REPEAT;
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
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+" turns "+directionName);
						}
						if(direction != directionFacing)
							return NavResult.REPEAT;
						else
							return NavResult.CONTINUE;
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
			this.clearTacticalModeInternal();
			final Room destRoom=thisRoom.getRoomInDir(direction);
			final Exit exit=thisRoom.getExitInDir(direction);
			if((destRoom!=null)&&(exit!=null))
			{
				if(!preNavigateCheck(thisRoom, direction, destRoom))
					return NavResult.CANCEL;
				final int oppositeDirectionFacing=thisRoom.getReverseDir(direction);
				final String directionName=CMLib.directions().getDirectionName(direction);
				final String otherDirectionName=CMLib.directions().getDirectionName(oppositeDirectionFacing);
				final Exit opExit=destRoom.getExitInDir(oppositeDirectionFacing);
				final MOB mob = createNavMob(CMLib.map().roomLocation(this));
				try
				{
					final boolean isSneaking = CMLib.flags().isSneaking(this);
					final String navEnterStr = isSneaking ? null : L("<S-NAME> "+verb_sail+"(s) in from @x1.",otherDirectionName);
					final String navAwayStr = isSneaking ? null : L("<S-NAME> "+verb_sail+"(s) @x1.",directionName);
					final CMMsg enterMsg=CMClass.getMsg(mob,destRoom,exit,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,navEnterStr);
					final CMMsg leaveMsg=CMClass.getMsg(mob,thisRoom,opExit,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,null,CMMsg.MSG_LEAVE,navAwayStr);
					if((exit.okMessage(mob,enterMsg))
					&&(leaveMsg.target().okMessage(mob,leaveMsg))
					&&((opExit==null)||(opExit.okMessage(mob,leaveMsg)))
					&&(enterMsg.target().okMessage(mob,enterMsg)))
					{
						exit.executeMsg(mob,enterMsg);
						thisRoom.sendOthers(mob, leaveMsg);
						if((owner()!=enterMsg.target())
						||(!((Room)enterMsg.target()).isContent(this)))
						{
							this.unDock(false);
							((Room)enterMsg.target()).moveItemTo(this);
						}
						ticksSinceMove=0;
						this.dockHere(((Room)enterMsg.target()));
						//this.sendAreaMessage(leaveMsg, true);
						if(opExit!=null)
							opExit.executeMsg(mob,leaveMsg);
						((Room)enterMsg.target()).send(mob, enterMsg);
						haveEveryoneLookOutside();
						return NavResult.CONTINUE;
					}
					else
					{
						announceToAllAboard(L("<S-NAME> can not seem to travel @x1.",CMLib.directions().getInDirectionName(direction)));
						courseDirections.clear();
						return NavResult.CANCEL;
					}
				}
				finally
				{
					mob.destroy();
				}
			}
			else
			{
				announceToAllAboard(L("As there is no where to "+verb_sail+" @x1, <S-NAME> meanders along the waves.",CMLib.directions().getInDirectionName(direction)));
				courseDirections.clear();
				return NavResult.CANCEL;
			}
		}
		return NavResult.CANCEL;
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
		final CMMsg msg=CMClass.getMsg(mob, null,null,CMMsg.MSG_NOISYMOVEMENT,innerStr,outerStr,outerStr);
		final CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> change(s) course, steering @x1 @x2.",name(mob),CMLib.directions().getDirectionName(dir)));
		if((R.okMessage(mob, msg) && this.okAreaMessage(msg2, true)))
		{
			R.sendOthers(mob, msg);
			this.sendAreaMessage(msg2, true);
			this.courseDirection=dir | COURSE_STEER_MASK;
			return true;
		}
		return false;
	}

	protected boolean beginNavigate(final MOB mob, final Room R, final int dir)
	{
		directionFacing = dir;
		final String outerStr;
		final String innerStr = L("<S-NAME> "+verb_sail+"(s) @x1 @x2.",name(mob),CMLib.directions().getDirectionName(dir));
		if(CMLib.flags().isSneaking(this))
			outerStr=null;
		else
			outerStr=innerStr;
		final CMMsg msg2=CMClass.getMsg(mob, R, R.getExitInDir(dir), CMMsg.MSG_NOISYMOVEMENT, innerStr, outerStr,outerStr);
		if((R.okMessage(mob, msg2) && this.okAreaMessage(msg2, true)))
		{
			R.send(mob, msg2); // this lets the source know, i guess
			//this.sendAreaMessage(msg2, true); // this just sends to "others"
			this.courseDirection=dir;
			return true;
		}
		return false;
	}

	protected int getAnyExitDir(final Room R)
	{
		if(R==null)
			return -1;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R2=R.getRoomInDir(d);
			final Exit E2=R.getExitInDir(d);
			if((R2!=null)&&(E2!=null)&&(CMLib.map().getExtendedRoomID(R2).length()>0))
				return d;
		}
		return -1;
	}

	protected Room findSafeRoom(final Area A)
	{
		if(A==null)
			return null;
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null) &&(CMLib.map().getExtendedRoomID(R).length()>0))
				return R;
		}
		return null;
	}

	protected boolean requiresSafetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)
		|| R.amDestroyed())
			return true;
		return false;

	}

	protected boolean safetyMove()
	{
		if(requiresSafetyMove())
		{
			final Room R=CMLib.map().roomLocation(this);
			Room R2=CMLib.map().getRoom(getHomePortID());
			if((R2==null)&&(R!=null)&&(R.getArea()!=null))
				R2=findSafeRoom(R.getArea());
			if(R2==null)
			{
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					R2=findSafeRoom(a.nextElement());
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

	protected static boolean ownerSecurityCheck(final String ownerName, final MOB mob)
	{
		return (ownerName.length()>0)
			 &&(mob!=null)
			 &&((mob.Name().equals(ownerName))
				||(mob.getLiegeID().equals(ownerName)&mob.isMarriedToLiege())
				||(CMLib.clans().checkClanPrivilege(mob, ownerName, Clan.Function.PROPERTY_OWNER)));
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
	public void setAnchorDown(final boolean truefalse)
	{
		this.anchorDown = truefalse;
	}

	protected int getTopCourse()
	{
		try
		{
			if(this.courseDirections.size()>0)
				return this.courseDirections.get(0).intValue();
		}
		catch(final Exception e)
		{
		}
		return -1;
	}

	protected int removeTopCourse()
	{
		try
		{
			if(this.courseDirections.size()>0)
				return this.courseDirections.remove(0).intValue();
		}
		catch(final Exception e)
		{
		}
		return -1;
	}

	protected int getBottomCourse()
	{
		try
		{
			final int size=this.courseDirections.size();
			if(size>0)
				return this.courseDirections.get(size-1).intValue();
		}
		catch(final Exception e)
		{
		}
		return -1;
	}

	@Override
	public List<Integer> getCurrentCourse()
	{
		if(getTopCourse()>=0)
		{
			return this.courseDirections;
		}
		return new ArrayList<Integer>(0);
	}

	@Override
	public void setCurrentCourse(final List<Integer> course)
	{
		this.courseDirection=-1;
		this.courseDirections.clear();
		for(final Integer dirIndex : course)
		{
			final int dir=dirIndex.intValue();
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

	@Override
	public void setStat(final String code, final String val)
	{
		final String up_code = (""+code).toUpperCase();
		if(up_code.startsWith("SPECIAL_"))
		{
			if(up_code.equals("SPECIAL_NOUN_SHIP"))
				noun_word=val;
			else
			if(up_code.equals("SPECIAL_VERB_SAIL"))
				verb_sail=val;
			else
			if(up_code.equals("SPECIAL_VERB_SAILING"))
				verb_sailing=val;
			else
			if(up_code.equals("SPECIAL_HEAD_OFFTHEDECK"))
				head_offTheDeck=val;
		}
		super.setStat(up_code, val);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdNavigableBoardable))
			return false;
		return super.sameAs(E);
	}
}
