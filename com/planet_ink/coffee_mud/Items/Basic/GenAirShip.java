package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.Items.Basic.StdNavigableBoardable.NavigatingCommand;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Int;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class GenAirShip extends GenNavigableBoardable
{
	@Override
	public String ID()
	{
		return "GenAirShip";
	}

	public GenAirShip()
	{
		super();
		setName("the air ship [NEWNAME]");
		setDisplayText("the air ship [NEWNAME] is here.");
		this.verb_sail = L("fly");
		this.verb_sailing = L("flying");
		this.noun_word = L("ship");
		this.anchor_name= L("rope");
		this.anchor_verbed = L("secured");
		this.head_offTheDeck = L("^HOff the side you see: ^N");
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(2);
		this.recoverPhyStats();
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("an air ship");
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(owner instanceof Room)
		{
			if(usesRemaining()>0)
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FLYING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
	}

	@Override
	protected boolean canAnchorFromHere(final Room R)
	{
		if ((R == null)
		|| (!CMLib.map().hasASky(R))
		|| ((R.getRoomInDir(Directions.DOWN) != null))&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT))
			return false;
		return true;
	}

	@Override
	public Basis navBasis()
	{
		return Rideable.Basis.AIR_FLYING;
	}

	private final static Map<String, NavigatingCommand> navCommandWords = new Hashtable<String, NavigatingCommand>();

	@Override
	protected Pair<NavigatingCommand, Integer> findNavCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		if(navCommandWords.size()==0)
		{
			for(final NavigatingCommand N : NavigatingCommand.values())
			{
				switch(N)
				{
				case NAVIGATE:
					navCommandWords.put("SAIL", N);
					break;
				case RAISE_ANCHOR:
					navCommandWords.put("RELEASE_TIEDOWNS", N);
					navCommandWords.put("RELEASE_ROPES", N);
					navCommandWords.put("RELEASE_ROPE", N);
					navCommandWords.put("RELEASE_TIEDOWN", N);
					navCommandWords.put("RELEASE_TIES", N);
					break;
				case LOWER_ANCHOR:
					navCommandWords.put("TIEDOWN", N);
					navCommandWords.put("TIE_DOWN", N);
					break;
				default:
					navCommandWords.put(N.name().toUpperCase().trim(), N);
				}
			}
		}

		if((secondWord!=null)&&(secondWord.length()>0)&&(navCommandWords.containsKey((word+"_"+secondWord).toUpperCase().trim())))
			return new Pair<NavigatingCommand, Integer>(navCommandWords.get((word+"_"+secondWord).toUpperCase().trim()),Integer.valueOf(2));
		if (navCommandWords.containsKey(word.toUpperCase().trim()))
			return new Pair<NavigatingCommand, Integer>(navCommandWords.get(word.toUpperCase().trim()), Integer.valueOf(1));
		return null;
	}

	private final static Map<String, SiegeCommand> siegeCommandWords = new Hashtable<String, SiegeCommand>();

	@Override
	protected SiegeCommand findSiegeCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		SiegeCommand cmd=null;
		if(siegeCommandWords.size()==0)
		{
			for(final SiegeCommand N : SiegeCommand.values())
			{
				switch(N)
				{
				case IMPLODE:
					siegeCommandWords.put("SINK", N);
					break;
				default:
					siegeCommandWords.put(N.name().toUpperCase().trim(), N);
				}
			}
		}

		if((secondWord!=null)&&(secondWord.length()>0))
			cmd = siegeCommandWords.get((word+"_"+secondWord).toUpperCase().trim());
		if(cmd == null)
			cmd = siegeCommandWords.get(word.toUpperCase().trim());
		return cmd;
	}

	@Override
	public final int getMaxHullPoints()
	{
		return (25 * getArea().numberOfProperIDedRooms())+(phyStats().armor());
	}

	@Override
	protected Room findNearestDocks(final Room R)
	{
		if(R!=null)
		{
			if(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
				return R;
			TrackingLibrary.TrackingFlags flags;
			flags = CMLib.tracking().newFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY)
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.OUTDOORONLY)
					.plus(TrackingLibrary.TrackingFlag.OPENONLY)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
			{
				if(R2.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
					return R2;
			}
			for(final Room R2 : rooms)
			{
				if(CMLib.map().hasASky(R2)
				&&(R2.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
				&&(R2.domainType()!=Room.DOMAIN_INDOORS_AIR))
					return R2;
			}
		}
		return null;
	}

	@Override
	protected Room findSafeRoom(final Area A)
	{
		if(A==null)
			return null;
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null)
			&&(CMLib.map().hasASky(R)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
				||(R.domainType()==Room.DOMAIN_INDOORS_AIR))
			&&(CMLib.map().getExtendedRoomID(R).length()>0))
				return R;
		}
		return null;
	}

	@Override
	protected boolean requiresSafetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)
		|| R.amDestroyed()
		|| ((!CMLib.flags().isFalling(this)) && (getAnyExitDir(R)<0)))
			return true;
		return false;

	}

	protected boolean isAirShipRoom(final Room R)
	{
		if((CMLib.map().hasASky(R))
		||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		||(R.domainType()==Room.DOMAIN_INDOORS_AIR))
			return true;
		return false;
	}

	@Override
	protected boolean preNavigateCheck(final Room thisRoom, final int direction, final Room destRoom)
	{
		if(!isAirShipRoom(destRoom))
		{
			announceToAllAboard(L("As there is no where to @x2 @x1, <S-NAME> go(es) nowhere.",CMLib.directions().getInDirectionName(direction),verb_sail));
			courseDirections.clear();
			return false;
		}
		return true;
	}

	@Override
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
					&&(!isAirShipRoom(R2)))
					{
						return Directions.getOpDirectionCode(d);
					}
				}
			}
			return direction;
		}
		return directionFacing;
	}

	@Override
	public long expirationDate()
	{
		final Room R=CMLib.map().roomLocation(this);
		if(R==null)
			return 0;
		if((!isAirShipRoom(R))
		&&(this.usesRemaining()>0))
			return 0;
		return super.expirationDate();
	}

	@Override
	protected MOB getFactoryAttacker(final Room thisRoom)
	{
		final MOB mob = super.getFactoryAttacker(thisRoom);
		mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_FLYING);
		mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_FLYING);
		return mob;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target() == this)
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("AnAirCurrent")))
		{
			if(anchorDown)
				return false;
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	protected Boolean startAttack(final MOB sourceM, final Room thisRoom, final String rest)
	{
		final Item I=thisRoom.findItem(rest);
		if((I instanceof SiegableItem)
		&&(I!=this)
		&&(CMLib.flags().canBeSeenBy(I, sourceM)))
		{
			if((thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_AIR))
			{
				sourceM.tell(L("You are not able to engage in combat with @x1 here.",I.name()));
				return Boolean.FALSE;
			}
		}
		return super.startAttack(sourceM, thisRoom, rest);
	}

	@Override
	protected Item doCombatDefeat(final MOB victorM, final boolean createBody)
	{
		final Room baseR=CMLib.map().roomLocation(this);
		if(baseR!=null)
		{
			CMLib.tracking().makeFall(victorM, this, baseR, false);
			final String sinkString = L("<T-NAME> start(s) falling!");
			baseR.show(victorM, this, CMMsg.MSG_OK_ACTION, sinkString);
			this.announceToNonOuterViewers(victorM, sinkString);
		}
		if((victorM.riding() instanceof Boardable)
		&&((victorM.Name().equals(victorM.riding().Name()))))
		{
			final Area A=((Boardable)victorM.riding()).getArea();
			if(A!=null)
			{
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null)
					&&(R.numInhabitants()>0))
					{
						for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							final CMMsg msg2=CMClass.getMsg(M, this, CMMsg.MSG_CAUSESINK, null);
							this.sendAreaMessage(msg2, false);
							R.showSource(M, this, CMMsg.MSG_CAUSESINK, null);
							CMLib.achievements().possiblyBumpAchievement(M, Event.SHIPSSUNK, 1, this);
						}
					}
				}
			}
		}
		if(!CMLib.leveler().postExperienceToAllAboard(victorM.riding(), "SIEGE:"+ID(), 500, this))
			CMLib.leveler().postExperience(victorM, "SIEGE:"+ID(), null, null, 500, false);
		return this;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_HUH:
		case CMMsg.TYP_COMMANDFAIL:
		case CMMsg.TYP_SKILLFAIL:
		case CMMsg.TYP_COMMAND:
			break;
		default:
			if(msg.source().riding()==this)
			{
				final ItemPossessor owner = owner();
				if((owner instanceof Room)
				&&(owner != prevItemRoom)
				&&(this.area instanceof Boardable))
				{
					final Room R = (Room)owner;
					boolean fixSky=false;
					final Room oldR;
					synchronized(this)
					{
						oldR=this.prevItemRoom;
						fixSky = ((R!=oldR)&&(oldR!=null));
						if(oldR!=R)
							this.prevItemRoom=R;
					}
					if(fixSky)
					{
						final boolean wasAir=this.isAirShipRoom(oldR);
						final boolean isSunk = isGrounded();
						if(wasAir || isSunk)
						{
							for(final Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
							{
								final Room inR=r.nextElement();
								if(((inR.domainType()&Room.INDOORS)==0)
								&&(inR.roomID().length()>0))
								{
									inR.clearSky();
									if(isSunk)
									{
										if((inR.getRoomInDir(Directions.UP)==null)
										&&(inR.getExitInDir(Directions.UP)==null))
										{
											inR.giveASky(0);
											final Exit redirExit = CMClass.getExit("NamedRedirectable");
											redirExit.setDisplayText(R.displayText());
											redirExit.setDescription(R.displayText());
											redirExit.lastRoomUsedFrom(R);
											inR.setRawExit(Directions.UP, redirExit);
										}
									}
									else
										inR.giveASky(0);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenAirShip))
			return false;
		return super.sameAs(E);
	}

	protected boolean isGrounded()
	{
		if(this.usesRemaining()<=0)
		{
			if(owner() instanceof Room)
			{
				final Room R = (Room)owner();
				if(((R.domainType()!=Room.DOMAIN_OUTDOORS_AIR) && (R.domainType()!=Room.DOMAIN_INDOORS_AIR))
				||(R.getRoomInDir(Directions.DOWN)==null))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean amDead()
	{
		return isGrounded() || amDestroyed();
	}
}
