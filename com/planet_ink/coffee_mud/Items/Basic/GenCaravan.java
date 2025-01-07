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
import com.planet_ink.coffee_mud.Libraries.interfaces.CommonCommands.LookView;

/*
   Copyright 2014-2024 Bo Zimmerman

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
public class GenCaravan extends GenNavigableBoardable
{
	@Override
	public String ID()
	{
		return "GenCaravan";
	}

	public GenCaravan()
	{
		super();
		setName("a caravan [NEWNAME]");
		setDisplayText("a caravan [NEWNAME] is here.");
		this.verb_sail = "drive";
		this.verb_sailing = "driving";
		this.noun_word = "drive";
		this.anchor_name= "brake";
		this.anchor_verbed = "set";
		this.head_offTheDeck = "^HOff the side you see: ^N";
		this.setRideBasis(Basis.WAGON);
		super.setRiderCapacity(0);
		setMaterial(RawMaterial.RESOURCE_OAK);
		basePhyStats().setAbility(2);
		this.recoverPhyStats();
	}

	private volatile boolean isDriving=false;

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a caravan");
	}

	@Override
	public Basis navBasis()
	{
		return Rideable.Basis.LAND_BASED;
	}

	@Override
	public Basis rideBasis()
	{
		return Rideable.Basis.WAGON;
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
					navCommandWords.put("DRIVE", N);
					break;
				case RAISE_ANCHOR:
					navCommandWords.put("RELEASE_BRAKE", N);
					navCommandWords.put("UNSET_BRAKE", N);
					navCommandWords.put("RELEASE_BREAK", N);
					navCommandWords.put("UNSET_BREAK", N);
					break;
				case LOWER_ANCHOR:
					navCommandWords.put("SET_BREAK", N);
					navCommandWords.put("SET_BRAKE", N);
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

	@Override
	protected Room findNearestDocks(final Room R)
	{
		if(R!=null)
		{
			if(CMLib.flags().isDrivableRoom(R))
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
				if(CMLib.flags().isDrivableRoom(R2))
					return R2;
			}
		}
		return null;
	}

	@Override
	public String displayText(final MOB viewerMob)
	{
		if (riding() != null)
		{
			final StringBuilder sendBack = new StringBuilder(name(viewerMob));
			sendBack.append(" ");
			sendBack.append(L(CMLib.flags().getPresentDispositionVerb(this, CMFlagLibrary.ComingOrGoing.IS) + " here"));
			if (riding() != null)
			{
				sendBack.append(" " + riding().stateString(this) + " ");
				if (riding() == viewerMob)
					sendBack.append(L("YOU"));
				else
				if (!CMLib.flags().canBeSeenBy(riding(), viewerMob))
				{
					if (riding() instanceof Item)
						sendBack.append(L("something"));
					else
						sendBack.append(L("someone"));
				}
				else
					sendBack.append(riding().name());
				sendBack.append(".");
				return sendBack.toString();
			}
		}
		return super.displayText(viewerMob);
	}

	@Override
	public boolean canSteer(final MOB mob, final Room R)
	{
		return true;
	}

	@Override
	protected Room findSafeRoom(final Area A)
	{
		if(A==null)
			return null;
		for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null)&& (CMLib.flags().isDrivableRoom(R)) &&(CMLib.map().getExtendedRoomID(R).length()>0))
				return R;
		}
		return null;
	}

	@Override
	public final int getMaxHullPoints()
	{
		return (10 * getArea().numberOfProperIDedRooms())+(phyStats().armor());
	}

	@Override
	protected boolean requiresSafetyMove()
	{
		final Room R=CMLib.map().roomLocation(this);
		if((R==null)
		|| R.amDestroyed()
		|| (!CMLib.flags().isDrivableRoom(R))
		|| ((!CMLib.flags().isFalling(this)) && (getAnyExitDir(R)<0)))
			return true;
		return false;

	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		if(list==null)
			return list;
		if(riding()==null)
			return list;
		final Rideable baseRide = this.riding();
		Physical rideLeader = baseRide;
		final WorldMap map=CMLib.map();
		final Room thisRoom = map.roomLocation(this);
		while((rideLeader!=null)
		&&(map.roomLocation(rideLeader) == thisRoom))
		{
			if(rideLeader instanceof MOB)
				list.add((MOB)rideLeader);
			if((rideLeader instanceof Rideable)
			&&(((Rideable)rideLeader).riding()!=null))
				rideLeader=((Rideable)rideLeader).riding();
			else
				break;
		}
		return list;
	}

	@Override
	protected boolean preNavigateCheck(final Room thisRoom, final int direction, final Room destRoom)
	{
		if(!CMLib.flags().isDrivableRoom(destRoom))
		{
			announceToAllAboard(L("As there is no where to "+verb_sail+" @x1, <S-NAME> go(es) nowhere.",CMLib.directions().getInDirectionName(direction)));
			courseDirections.clear();
			return false;
		}
		final Rideable baseRide = this.riding();
		Physical rideLeader = baseRide;
		final Set<Physical> allPullers = new HashSet<Physical>();
		final WorldMap map=CMLib.map();
		while((rideLeader!=null)
		&&((map.roomLocation(rideLeader) == thisRoom)
			||(map.roomLocation(rideLeader) == destRoom)))
		{
			allPullers.add(rideLeader);
			if((rideLeader instanceof Rideable)
			&&(((Rideable)rideLeader).riding()!=null))
				rideLeader=((Rideable)rideLeader).riding();
			else
				break;
		}
		final long totalPullWeight = (rideLeader==null)?0:CMLib.utensils().getPullWeight(rideLeader);
		if(totalPullWeight <= 0)
		{
			announceToAllAboard(L("You can't seem to "+verb_sail+" @x1 due to a lack of team to pull it, <S-NAME> go(es) nowhere.",CMLib.directions().getInDirectionName(direction)));
			courseDirections.clear();
			return false;
		}
		if(totalPullWeight < phyStats().weight())
		{
			announceToAllAboard(L("You can't seem to "+verb_sail+" @x1 due to weight, <S-NAME> go(es) nowhere.",CMLib.directions().getInDirectionName(direction)));
			courseDirections.clear();
			return false;
		}
		if(rideLeader instanceof MOB)
		{
			try
			{
				isDriving=true;
				final MOB leadM=(MOB)rideLeader;
				if((leadM.location() != destRoom)
				&&(!CMLib.tracking().walk(leadM, direction, false, false, false)))
				{
					announceToAllAboard(L("@x1 can't seem to "+verb_sail+" you @x2, <S-NAME> go(es) nowhere.",rideLeader.name(), CMLib.directions().getInDirectionName(direction)));
					courseDirections.clear();
					return false;
				}
				for(final Iterator<Physical> p=allPullers.iterator();p.hasNext();)
				{
					final Physical P=p.next();
					final Room R=CMLib.map().roomLocation(P);
					if(R!=destRoom)
					{
						announceToAllAboard(L("@x1 can't seem to "+verb_sail+" you @x2, <S-NAME> go(es) nowhere.",P.name(), CMLib.directions().getInDirectionName(direction)));
						courseDirections.clear();
						return false;
					}
				}
			}
			finally
			{
				isDriving=false;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!isDriving)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.target() instanceof Room)
		&&(msg.source().location()==owner())
		&&(this.riding()!=null)
		&&(msg.source().riding()!=this)
		&&((msg.source().riding()!=null)
			||(msg.source().numFollowers()>0)
			||((msg.source() instanceof Rideable)&&((Rideable)msg.source()).numRiders()>0)))
		{
			final Set<Physical> grp=CMLib.hunt().getAllGroupRiders(msg.source(), msg.source().location());
			if(grp.contains(this))
			{
				msg.source().tell(L("@x1 must be driven from the deck.",name()));
				return false;
			}
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINE)
		&&(msg.source().location()==owner))
		{
			boolean foundOne=false;
			for(final Enumeration<Room> r = this.getArea().getProperMap(); r.hasMoreElements(); )
			{
				final Room R = r.nextElement();
				if(super.canViewOuterRoom(R))
				{
					foundOne=true;
					break;
				}
			}
			if(foundOne)
			{
				final GenCaravan tempMe = this;
				msg.addTrailerRunnable(new Runnable()
				{
					final GenCaravan me = tempMe;
					final MOB viewerM = msg.source();
					@Override
					public void run()
					{
						for(final Enumeration<Room> r = me.getArea().getProperMap(); r.hasMoreElements(); )
						{
							final Room R = r.nextElement();
							if(me.canViewOuterRoom(R)
							&&(R.roomID().length()>0))
								viewerM.tell(CMLib.commands().getFullRoomView(viewerM, R, LookView.LOOK_MINIMAL, false));
						}
						msg.trailerRunnables().remove(this);
					}
				});
			}
		}
	}

	@Override
	protected Item doCombatDefeat(final MOB victorM, final boolean createBody)
	{
		final Room baseR=CMLib.map().roomLocation(this);
		if(baseR!=null)
		{
			final String sinkString = L("<T-NAME> start(s) collapsing!");
			baseR.show(victorM, this, CMMsg.MSG_OK_ACTION, sinkString);
			this.announceToNonOuterViewers(victorM, sinkString);
			final Area A=this.getArea();
			if(A!=null)
			{
				for(final Enumeration<Room> r=A.getFilledCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if(M!=null)
							{
								baseR.bringMobHere(M, false);
								final double pctDmg = CMath.div(CMLib.dice().roll(1, 150, 0), 100.0);
								final int dmg = (int)Math.round(CMath.mul(pctDmg, M.baseState().getHitPoints()));
								CMLib.combat().postDamage(victorM, M,this,dmg,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,Weapon.TYPE_SLASHING,null);
							}
						}
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I!=null)
							&&(CMLib.flags().isGettable(I))
							&&(I.container()==null))
							{
								baseR.moveItemTo(I, Expire.Monster_EQ, Move.Followers);
								if(I.subjectToWearAndTear())
								{
									final int dmg = CMLib.dice().roll(1, 150, 0);
									CMLib.combat().postItemDamage(victorM, I, this, dmg, CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK, null);
								}
							}
						}
					}
				}
			}
			phyStats.setDisposition(phyStats.disposition()&~PhyStats.IS_UNSAVABLE);
			Item newI = null;
			if(createBody)
			{
				newI = CMLib.utensils().ruinItem(this);
				if(newI != this)
					baseR.addItem(newI, Expire.Monster_EQ);
			}
			this.destroy();
			return newI;
		}
		if(!CMLib.leveler().postExperienceToAllAboard(victorM.riding(), "SIEGE:"+ID(), 500, this))
			CMLib.leveler().postExperience(victorM, "SIEGE:"+ID(), null, null, 500, false);
		return null;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenCaravan))
			return false;
		return super.sameAs(E);
	}
}
