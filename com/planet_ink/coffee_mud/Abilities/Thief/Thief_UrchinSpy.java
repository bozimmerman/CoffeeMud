package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_UrchinSpy extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_UrchinSpy";
	}

	private final static String	localizedName	= CMLib.lang().L("Urchin Spy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"URCHINSPY"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING | Ability.FLAG_SUMMONING;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);

	}

	protected List<Room>		theTrail		= null;
	public int					nextDirection	= -2;
	protected boolean			reporting		= false;
	protected Reference<MOB>	targetMob		= null;
	protected boolean			targetRoomFlag	= false;
	protected StringBuilder		report			= new StringBuilder("");
	protected Area				homeArea		= null;
	protected Room				homeRoom		= null;

	protected boolean reTrackToTarget(final MOB mob, final MOB target)
	{
		final int range = 10 + (adjustedLevel(invoker(),0)) + (super.getXLEVELLevel(invoker())*2);
		final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.PASSABLE)
												.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
												.plus(TrackingLibrary.TrackingFlag.NOAIR)
												.plus(TrackingLibrary.TrackingFlag.NOWATER);
		theTrail=CMLib.tracking().findTrailToRoom(mob.location(),target.location(),flags,range);
		if(theTrail == null)
		{
			report.append(L("... and then I lost them.\n\r"));
			return false;
		}
		else
		{
			if(reporting)
			{
				final int remain = theTrail.size()*2;
				if(super.tickDown<remain)
					super.tickDown = remain;
			}
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
			return true;
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return !unInvoked;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
			{
				report.append(L("... and then I got confused.\n\r"));
				unInvoke();
				return !unInvoked;
			}

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
			{
				report.append(L("... and then I said 'screw this'.\n\r"));
				unInvoke();
				return !unInvoked;
			}

			final MOB mob=(MOB)affected;
			if((homeRoom == null)||(homeArea==null))
			{
				homeRoom = mob.location();
				homeArea = homeRoom.getArea();
			}
			if(nextDirection==999)
			{
				nextDirection=-2;
				// ok, you made it, but if you were looking
				// for a specific person, and didn't find them,
				// then look some more and be persistant.
				if(this.targetMob!=null)
				{
					final MOB target = this.targetMob.get();
					if((target!=null)
					&&(invoker()!=null))
					{
						if(target.location()==mob.location()) // Hey! He's here! Just sit and watch...
						{
							if(reporting)
								unInvoke();
							return !unInvoked;
						}
						final int range = 10 + (adjustedLevel(invoker(),0)) + (super.getXLEVELLevel(invoker())*2);
						final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
																.plus(TrackingLibrary.TrackingFlag.PASSABLE)
																.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
																.plus(TrackingLibrary.TrackingFlag.NOAIR)
																.plus(TrackingLibrary.TrackingFlag.NOWATER);
						theTrail=CMLib.tracking().findTrailToRoom(mob.location(),target.location(),flags,range);
					}
					else
						theTrail = null;
					if(theTrail == null)
					{
						report.append(L("... and then I lost them.\n\r"));
						unInvoke();
						return !unInvoked;
					}
					else
					{
						if(reporting)
						{
							final int remain = theTrail.size()*2;
							if(super.tickDown<remain)
								super.tickDown = remain;
						}
						nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
					}
				}
				else
				if(targetRoomFlag)
				{
					if(reporting)
						unInvoke();
					return !unInvoked;
				}
				else
				{
					unInvoke();
					return !unInvoked;
				}
			}
			else
			if(nextDirection==-1)
			{
				mob.tell(L("The trail dries up here."));
				report.append(L("... and then I lost the trail.\n\r"));
				nextDirection=-999;
				unInvoke();
				return !unInvoked;
			}
			else
			if(nextDirection==-2)
				nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
			else
			if(nextDirection>=0)
			{
				mob.tell(L("The trail seems to continue @x1.",CMLib.directions().getDirectionName(nextDirection)));
				final Room oldRoom=mob.location();
				if((mob.isMonster())&&(oldRoom!=null))
				{
					final Room nextRoom=oldRoom.getRoomInDir(nextDirection);
					final Exit nextExit=oldRoom.getExitInDir(nextDirection);
					final int opDirection=oldRoom.getReverseDir(nextDirection);
					if((nextRoom!=null)&&(nextExit!=null))
					{
						boolean reclose=false;
						// handle doors!
						if(nextExit.hasADoor()
						&&(!nextExit.isOpen()))
						{
							if((nextExit.hasALock())
							&&(nextExit.isLocked()))
							{
								unInvoke();
								return !unInvoked;
							}
							final CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OPEN,
									L("<S-NAME> @x1 <T-NAMESELF>.",((nextExit.openWord().indexOf('(')>0)?nextExit.openWord():(nextExit.openWord()+"(s)"))));
							if(oldRoom.okMessage(mob,msg))
							{
								reclose=true;
								CMLib.utensils().roomAffectFully(msg,oldRoom,nextDirection);
							}
						}
						if(!nextExit.isOpen())
						{
							unInvoke();
							return !unInvoked;
						}
						final int dir=nextDirection;
						nextDirection=-2;
						Thief_Sneak A=(Thief_Sneak)mob.fetchAbility("Thief_Sneak");
						if(A!=null)
						{
							final int[] consumed = A.usageCost(mob, false);
							if(!A.testUsageCost(mob,false,consumed,true))
								A=null;
						}
						if(A!=null)
						{
							final List<String> V=new Vector<String>();
							V.add(CMLib.directions().getDirectionName(dir));
							A.invoke(mob,V,null,false,0);
						}
						else
							CMLib.tracking().walk(mob,dir,false,false);
						if((mob.location()==nextRoom)
						&&(reclose))
						{
							final Exit opExit=nextRoom.getExitInDir(opDirection);
							if((opExit!=null)
							&&(opExit.hasADoor())
							&&(opExit.isOpen()))
							{
								final CMMsg msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_CLOSE,
										L("<S-NAME> @x1 <T-NAMESELF>.",((nextExit.closeWord().indexOf('(')>0)?nextExit.closeWord():(nextExit.closeWord()+"(s)"))));
								if(nextRoom.okMessage(mob,msg))
									CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
							}
						}
					}
					else
					{
						unInvoke();
						return !unInvoked;
					}
				}
				else
					nextDirection=-2;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if((!reporting)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if((msg.source()==mob)
			&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room))
			{
				if((msg.sourceMessage()!=null)&&(msg.sourceMessage().length()>0))
				{
					report.append(CMLib.coffeeFilter().fullOutFilter(null, mob, msg.source(), msg.target(), msg.tool(),
							msg.sourceMessage(), false)).append("\n\r");
				}
				final Room R = (Room)msg.target();
				if(CMLib.flags().canBeSeenBy(R, mob))
					report.append(L("This place is '@x1'.\n\r",R.displayText(mob)));
				final List<String> names = new ArrayList<String>(R.numInhabitants());
				for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
				{
					final MOB M = m.nextElement();
					if((M!=null)
					&&(M!=mob)
					&&(CMLib.flags().canBeSeenBy(M, mob)))
						names.add(M.name());
				}
				for(final Enumeration<Item> i = R.items();i.hasMoreElements();)
				{
					final Item I = i.nextElement();
					if((I!=null)
					&&(I!=mob)
					&&(I.container()==null)
					&&(I.displayText(mob).length()>0)
					&&(CMLib.flags().canBeSeenBy(I, mob)))
						names.add(I.name());
				}
				if(names.size()>0)
					report.append(L("I saw @x1 there.\n\r",CMLib.english().toEnglishStringList(names)));
			}
			else
			if((msg.othersCode() != CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null)
			&&(msg.othersMessage().length()>0))
			{
				final int othersMajor = msg.othersMajor();
				final boolean canseesrc = CMLib.flags().canBeSeenBy(msg.source(), mob) || (msg.source()==mob);
				final boolean canhearsrc = CMLib.flags().canBeHeardMovingBy(msg.source(), mob) || (msg.source()==mob);
				final boolean asleep=CMLib.flags().isSleeping(mob);
				final String msgStr = msg.othersMessage();
				final MOB M = CMClass.getFactoryMOB();
				try
				{
					if(CMath.bset(othersMajor, CMMsg.MASK_CHANNEL))
					{
					}
					else
					if((CMath.bset(othersMajor, CMMsg.MASK_SOUND)) && (!asleep) && (canhearsrc))
					{
						report.append(CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(),
								msgStr, false)).append("\n\r");
					}
					else
					if(msg.othersMinor() == CMMsg.TYP_AROMA)
					{
					}
					else
					if(((CMath.bset(othersMajor, CMMsg.MASK_EYES))
						|| (CMath.bset(othersMajor, CMMsg.MASK_HANDS))
						|| (CMath.bset(othersMajor, CMMsg.MASK_ALWAYS)))
					&& (!CMath.bset(msg.othersMajor(), CMMsg.MASK_CNTRLMSG))
					&& ((!asleep) && (canseesrc)))
					{
						report.append(CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(),
								msgStr, false)).append("\n\r");
					}
					else
					if(((CMath.bset(othersMajor, CMMsg.MASK_MOVE))
						|| ((CMath.bset(othersMajor, CMMsg.MASK_MOUTH))
							&& (!CMath.bset(othersMajor, CMMsg.MASK_SOUND))))
					&& (!asleep) && ((canseesrc) || (canhearsrc)))
					{
						report.append(CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(),
								msgStr, false)).append("\n\r");
					}
					else
					if((msg.sourceMinor() == CMMsg.TYP_TELL)
					&& (msg.targetCode() == CMMsg.NO_EFFECT)) // group// tell
					{
						report.append(CMLib.coffeeFilter().fullOutFilter(null, M, msg.source(), msg.target(), msg.tool(),
								msgStr, false)).append("\n\r");
					}
				}
				finally
				{
					M.destroy();
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		final Physical P = affected;
		if(!(P instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB M = (MOB)P;
		if(reporting)
		{
			if(M.location()==invoker().location())
				CMLib.commands().postSay(M, invoker(), report.toString());
			report.setLength(0);
			super.unInvoke();
			if((homeArea != null)
			&&(M.location()!=null)
			&&(homeRoom != null)
			&&(M.location().getArea()!=homeArea))
				CMLib.tracking().wanderFromTo(M, homeRoom, false);
		}
		else
		{
			if(invoker() != null)
			{
				reporting=true;
				this.targetMob = new WeakReference<MOB>(invoker());
				if(!reTrackToTarget(M,invoker()))
					super.unInvoke();
			}
			else
				super.unInvoke();
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("You must specify the urchin, and a target room or mob, or directions."));
			return false;
		}
		final String urchin = commands.get(0);
		final String myTarget=CMParms.combine(commands,1);
		if(myTarget.length()==0)
		{
			mob.tell(L("You must specify a target room or mob, or directions."));
			return false;
		}

		final MOB target=this.getTarget(mob,new XVector<String>(urchin),givenTarget);
		if(target==null)
			return false;

		if((!target.getLiegeID().equals(mob.Name())
		||(target.fetchBehavior("Thiefness")==null)
		||(target.fetchBehavior("Scavenger")==null)
		||(!Thief_MyUrchins.isMyUrchin(target, mob))))
		{
			mob.tell(L("@x1 is not one of your urchins.",target.name(mob)));
			return false;
		}

		boolean looksLikeDirections = true;
		boolean badDirections=false;
		Physical targetThing = null;
		Reference<MOB> targetMob = null;
		final List<Room> directions = new ArrayList<Room>();
		{
			Room R = mob.location();
			for(final String s : CMParms.parse(myTarget))
			{
				directions.add(R);
				final int code = CMLib.directions().getGoodDirectionCode(s);
				if(code<0)
				{
					looksLikeDirections=false;
					directions.clear();
					break;
				}
				R=R.getRoomInDir(code);
				if(R == null)
				{
					badDirections=true;
					directions.clear();
					break;
				}
			}
			if((looksLikeDirections)&&(!badDirections)&&(directions.size()>0))
			{
				directions.add(R);
				targetThing=R;
				Collections.reverse(directions);
			}
		}
		final int range = 10 + (adjustedLevel(mob,asLevel)) + (super.getXLEVELLevel(mob)*2);
		final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.PASSABLE)
												.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
												.plus(TrackingLibrary.TrackingFlag.NOAIR)
												.plus(TrackingLibrary.TrackingFlag.NOWATER);
		if((!looksLikeDirections)||(badDirections))
		{
			final List<Room> rooms = CMLib.tracking().getRadiantRooms(mob.location(), flags, range);
			for(final Room R : rooms)
			{
				final Physical P = R.fetchInhabitant(myTarget);
				if(P != null)
				{
					targetThing = P;
					targetMob = new WeakReference<MOB>((MOB)P);
					break;
				}
			}
			if(targetThing == null)
			{
				for(final Room R : rooms)
				{
					if(CMLib.english().containsString(R.displayText(target), myTarget))
					{
						targetThing=R;
						break;
					}
				}
			}
		}
		if(targetThing == null)
		{
			if(!looksLikeDirections)
			{
				mob.tell(L("Those don't appear to be directions, or the name of a mob or room in range."));
				return false;
			}
			else
			{
				mob.tell(L("Those look like directions, but lead to a dead-end."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),
					auto?"":L("^S<S-NAME> give(s) <T-NAME> a spy mission!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Thief_UrchinSpy spyA = (Thief_UrchinSpy)beneficialAffect(mob, target, asLevel, 0);
				spyA.reporting=false;
				spyA.targetMob=targetMob;
				if(looksLikeDirections && (!badDirections) && (directions.size()>0))
				{
					spyA.theTrail = directions;
					spyA.targetRoomFlag = false;
				}
				else
				{
					spyA.theTrail=CMLib.tracking().findTrailToRoom(target.location(),CMLib.map().roomLocation(targetThing),flags,range);
					spyA.targetRoomFlag = (spyA.targetMob==null);
				}
				spyA.nextDirection=CMLib.tracking().trackNextDirectionFromHere(spyA.theTrail,target.location(),false);
				spyA.report.setLength(0);
				spyA.homeRoom = target.location();
				spyA.homeArea = spyA.homeRoom.getArea();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to give <T-NAME> a spy mission, but <T-HE-SHE> won't do it."));

		// return whether it worked
		return success;
	}
}
