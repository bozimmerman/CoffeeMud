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
   Copyright 2014-2021 Bo Zimmerman

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
public class GenSailingShip extends GenNavigableBoardable
{
	@Override
	public String ID()
	{
		return "GenSailingShip";
	}

	public GenSailingShip()
	{
		super();
		setName("a sailing ship [NEWNAME]");
		setDisplayText("a sailing ship [NEWNAME] is here.");
		this.verb_sail = "sail";
		this.verb_sailing = "sailing";
		this.noun_word = "ship";
		this.anchor_name= "anchor";
		this.anchor_verbed = "lowered";
		this.head_offTheDeck = "^HOff the deck you see: ^N";
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(2);
		this.recoverPhyStats();
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(owner instanceof Room)
		{
			if(usesRemaining()>0)
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
	}

	@Override
	public Basis navBasis()
	{
		return Rideable.Basis.WATER_BASED;
	}

	private final static Map<String, NavigatingCommand> navCommandWords = new Hashtable<String, NavigatingCommand>();

	@Override
	protected NavigatingCommand findNavCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		NavigatingCommand cmd=null;
		if(navCommandWords.size()==0)
		{
			for(final NavigatingCommand N : NavigatingCommand.values())
			{
				switch(N)
				{
				case NAVIGATE:
					navCommandWords.put("SAIL", N);
					break;
				default:
					navCommandWords.put(N.name().toUpperCase().trim(), N);
				}
			}
		}

		if((secondWord!=null)&&(secondWord.length()>0))
			cmd = navCommandWords.get((word+"_"+secondWord).toUpperCase().trim());
		if(cmd == null)
			cmd = navCommandWords.get(word.toUpperCase().trim());
		return cmd;
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
	protected Room findNearestDocks(final Room R)
	{
		if(R!=null)
		{
			if((R.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
			||(R.domainType()==Room.DOMAIN_INDOORS_SEAPORT)
			||(R.domainType()==Room.DOMAIN_INDOORS_CAVE_SEAPORT))
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
				if((R2.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
				||(R2.domainType()==Room.DOMAIN_INDOORS_SEAPORT)
				||(R2.domainType()==Room.DOMAIN_INDOORS_CAVE_SEAPORT))
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

	@Override
	protected Room findSafeRoom(final Area A)
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

	@Override
	protected boolean requiresSafetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)
		|| R.amDestroyed()
		|| ((!CMLib.flags().isFalling(this))
			&& (R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			&& (R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&& (getAnyExitDir(R)<0)))
			return true;
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
			if(!CMLib.flags().isDeepWaterySurfaceRoom(thisRoom))
			{
				sourceM.tell(L("You are not able to engage in combat with @x1 here.",I.name()));
				return Boolean.FALSE;
			}
		}
		return super.startAttack(sourceM, thisRoom, rest);
	}

	@Override
	protected void doCombatDefeat(final MOB victorM)
	{
		final Room baseR=CMLib.map().roomLocation(this);
		if(baseR!=null)
		{
			CMLib.tracking().makeSink(this, baseR, false);
			final String sinkString = L("<T-NAME> start(s) sinking!");
			baseR.show(victorM, this, CMMsg.MSG_OK_ACTION, sinkString);
			this.announceToNonOuterViewers(victorM, sinkString);
		}
		if((victorM.riding() instanceof BoardableItem)
		&&((victorM.Name().equals(victorM.riding().Name()))))
		{
			final Area A=((BoardableItem)victorM.riding()).getArea();
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
		if(!CMLib.leveler().postExperienceToAllAboard(victorM.riding(), 500, this))
			CMLib.leveler().postExperience(victorM, null, null, 500, false);
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
			if(msg.source().riding()==this)
			{
				final ItemPossessor owner = owner();
				if((owner instanceof Room)
				&&(owner != prevItemRoom)
				&&(this.area instanceof BoardableItem))
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
						final boolean wasWater=CMLib.flags().isUnderWateryRoom(oldR);
						final boolean isWater=CMLib.flags().isUnderWateryRoom(R);
						final boolean isSunk = isWater && (R.getGridParent()!=null) && (R.getGridParent().roomID().length()==0);
						if(wasWater || isSunk)
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
		if(!(E instanceof GenSailingShip))
			return false;
		return super.sameAs(E);
	}

	@Override
	public boolean isDefeated()
	{
		final ItemPossessor owner = owner();
		if((owner instanceof Room)
		&&(this.area instanceof BoardableItem))
		{
			final Room R = (Room)owner;
			final boolean isWater=CMLib.flags().isUnderWateryRoom(R);
			return isWater && (R.getGridParent()!=null) && (R.getGridParent().roomID().length()==0);
		}
		return amDestroyed();
	}
}
