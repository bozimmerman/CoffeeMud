package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.State;
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

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Prayer_MalignedPortal extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_MalignedPortal";
	}

	private final static String localizedName = CMLib.lang().L("Maligned Portal");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}


	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL-90;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected volatile	Room			newRoom		= null;
	protected volatile	Room			oldRoom		= null;
	protected volatile	PlanarAbility	planeAble	= null;
	protected volatile	int				radius		= 1;
	protected 			MOB				fakeTarget	= null;
	protected final 	List<Item>		items		= new Vector<Item>();
	protected final		List<Room>		zoneRooms	= new Vector<Room>();

	@Override
	public void unInvoke()
	{
		final Physical P;
		if(canBeUninvoked())
		{
			P=affected;
			if(this.fakeTarget!=null)
				fakeTarget.destroy();
			final Room newRoom=this.newRoom;
			final PlanarAbility planeAble=this.planeAble;
			Item I;
			while(items.size()>0)
			{
				I=items.remove(0);
				if(I!=null)
					I.destroy();
			}
			if(newRoom!=null)
			{
				if((CMLib.flags().getPlaneOfExistence(newRoom)!=null)
				&&(planeAble != null))
				{
					planeAble.destroyPlane(newRoom.getArea());
					this.planeAble = null;
				}
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
			}
			final Room oldRoom=this.oldRoom;
			if(oldRoom!=null)
				oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
		}
		else
			P=null;
		super.unInvoke();
		if(P!=null)
			P.destroy();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			switch(Character.toUpperCase(msg.targetMessage().charAt(0)))
			{
			case 'A':
			case 'K':
			{
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				final String cmd=parsedFail.get(0).toUpperCase();
				final Room R=msg.source().location();
				if(("ATTACK".startsWith(cmd)||"KILL".startsWith(cmd))
				&&(R!=null)
				&&(R==oldRoom))
				{
					final String rest = CMParms.combine(parsedFail,1);
					if(R.findItem(null, rest) == affected)
					{
						if(msg.source().actions()<1.0)
							msg.source().tell(L("You can't do that yet."));
						if(msg.source().isInCombat())
							msg.source().tell(L("You are too busy right now."));
						else
						if(msg.source().curState().getMovement()<5)
							msg.source().tell(L("You are too tired right now."));
						else
						{
							final Item I=msg.source().fetchWieldedItem();
							final MOB fakeTarget=this.fakeTarget;
							if(!(I instanceof Weapon))
								msg.source().tell(L("You need a weapon to do that."));
							else
							if(fakeTarget!=null)
							{
								final double actions;
								synchronized(R)
								{
									actions=msg.source().actions();
								}
								synchronized(fakeTarget)
								{
									try
									{
										R.addInhabitant(fakeTarget);
										CMLib.combat().postAttack(msg.source(), fakeTarget, I);
										msg.source().setActions(actions-1.0);
										if((fakeTarget.curState().getHitPoints()<=0)
										||(fakeTarget.amDead()))
											unInvoke();
									}
									finally
									{
										fakeTarget.setVictim(null);
										msg.source().setVictim(null);
										if(R.isInhabitant(fakeTarget))
											R.delInhabitant(fakeTarget);
									}
								}
							}
							else
								msg.source().tell(L("The portal is unattackable."));
						}
						return false;
					}
				}
				return true;
			}
			default:
				return true;
			}
		}
		return true;
	}

	protected void fixPlanarMob(final MOB M)
	{
		final String aggroParms="CHECKLEVEL MOBKILL NOGANG +NAMES -"+invoker.Name();
		final Behavior B;
		if(CMLib.flags().isMobile(M))
		{
			if(!CMLib.flags().isAggressiveTo(M, null))
			{
				B=CMClass.getBehavior("Aggressive");
				B.setParms(aggroParms);
				M.addBehavior(B);
			}
		}
		else
		{
			B=CMClass.getBehavior("MobileAggressive");
			B.setParms(aggroParms);
			M.addBehavior(B);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Room newRoom=this.newRoom;
		final PlanarAbility planeAble=this.planeAble;
		if(planeAble != null)
			planeAble.setStat("TICKDOWN", ""+this.tickDown);
		if((newRoom!=null)
		&&(affected instanceof Exit))
		{
			final Area planeArea=newRoom.getArea();
			if(planeArea==null)
			{
				unInvoke();
				return false;
			}
			planeArea.setAreaState(State.ACTIVE);
			final MOB invokerM=invoker();
			if(invokerM==null)
			{
				unInvoke();
				return false;
			}
			final Area invokerA=CMLib.map().areaLocation(invokerM);
			if(invokerA==planeArea)
			{
				return true;
			}
			if(newRoom.numInhabitants()>0)
			{
				for(final Enumeration<MOB> m=newRoom.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)
					&&(M.isMonster())
					&&(!M.isInCombat())
					&&(M.amFollowing()==null)
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==planeArea)
					&&(!CMLib.flags().isTracking(M)))
					{
						CMLib.commands().forceStandardCommand(M, "ENTER", new XVector<String>("ENTER",affected.Name()));
						// dress the mob with Mobileaggressive with -name +invoker
						final MOB invoker=invoker();
						if((invoker!=null)
						&&(CMLib.flags().isInTheGame(invoker, true))
						&&(invoker.numFollowers()<invoker.maxFollowers()))
						{

							final List<String> trackCmds=new XVector<String>("and","befriend","and","persist",invoker.Name());
							final Ability trackA=CMClass.getAbility("Skill_Track");
							trackA.invoke(M, trackCmds, invoker, true, 0);
						}
						final Ability A=CMClass.getAbility("WanderHomeLater");
						final int ticks=super.tickDown;
						A.setMiscText("areaok=true destroy=true ignorepcs=true ignorefollow=true "
								+ "respectfollow=false once=true minticks="+ticks+" maxticks="+ticks);
						M.addNonUninvokableEffect(A);
						fixPlanarMob(M);
						if(!CMLib.flags().canBreatheHere(M, M.location()))
						{
							final int[] breathables = M.charStats().getBreathables();
							if(Arrays.binarySearch(breathables, M.location().getAtmosphere())<0)
							{
								final int[] newSet=Arrays.copyOf(breathables,breathables.length+1);
								newSet[newSet.length-1]=M.location().getAtmosphere();
								Arrays.sort(newSet);
								M.baseCharStats().setBreathables(newSet);
								M.charStats().setBreathables(newSet);
							}
						}
					}
				}
			}
			if(zoneRooms.size()==0)
			{
				final TrackingLibrary.TrackingFlags rflags = CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.AREAONLY);
				zoneRooms.addAll(CMLib.tracking().getRadiantRooms(newRoom, rflags,
																	10+
																	super.getXMAXRANGELevel(invoker())+
																	super.getXLEVELLevel(invoker())
																	));
			}
			else
			{
				int count=0;
				int attempts = 20;
				while((count < 4) && (--attempts > 0))
				{
					final Room R=zoneRooms.get(CMLib.dice().roll(1, zoneRooms.size(), -1));
					if(R.numInhabitants()>0)
					{
						final MOB M=R.fetchRandomInhabitant();
						if((M!=null)
						&&(M.isMonster())
						&&(!M.isInCombat())
						&&(M.amFollowing()==null)
						&&(M.getStartRoom()!=null)
						&&(M.getStartRoom().getArea()==planeArea)
						&&(!CMLib.flags().isTracking(M)))
						{
							final int dir=CMLib.tracking().trackNextDirectionFromHere(zoneRooms, R, false);
							if(dir >= 0)
							{
								CMLib.tracking().walk(M, dir, false, false);
								count++;
							}
						}
					}
				}
			}
		}
		return true;
	}

	protected String getCategory()
	{
		return "lower";
	}

	@Override
	public double castingTime(final MOB mob, final List<String> cmds)
	{
		return CMath.div(CMProps.getSkillCombatActionCost(ID(), 3000.0),100.0);
	}

	@Override
	public double combatCastingTime(final MOB mob, final List<String> cmds)
	{
		return CMath.div(CMProps.getSkillCombatActionCost(ID(), 3000.0),100.0);
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		if(!super.preInvoke(mob, commands, givenTarget, auto, asLevel, secondsElapsed, actionsRemaining))
			return false;

		final Room oldRoom=mob.location();
		if(oldRoom == null)
			return false;
		if(mob.isInCombat())
		{
			if(secondsElapsed > 0)
				oldRoom.show(mob,null,null,CMMsg.MSG_OK_VISUAL, "<S-YOUPOSS> prayer is disrupted.");
			else
				mob.tell(L("Being in combat disrupts your prayer."));
			return false;
		}
		if((!CMLib.flags().isAliveAwakeMobileUnbound(mob, false))
		||((secondsElapsed > 1) && (this.oldRoom != mob.location())))
		{
			if(secondsElapsed > 0)
				oldRoom.show(mob,null,null,CMMsg.MSG_OK_VISUAL, "<S-YOUPOSS> prayer is disrupted.");
			else
				mob.tell(L("You can't do that right now."));
			return false;
		}
		if(!CMLib.flags().isStanding(mob))
		{
			if(secondsElapsed > 0)
				oldRoom.show(mob,null,null,CMMsg.MSG_OK_VISUAL, "<S-YOUPOSS> prayer is disrupted.");
			else
				mob.tell(L("You need to stand up!"));
			return false;
		}

		if(secondsElapsed==0)
		{
			this.oldRoom=oldRoom;
			newRoom=null;
			planeAble=null;

			final PlanarAbility planeAble = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
			final List<String> choices = new ArrayList<String>();
			final List<String> choicesl = new ArrayList<String>();
			for(final String planeKey : planeAble.getAllPlaneKeys())
			{
				final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
				final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
				if(catStr != null)
				{
					final List<String> categories=CMParms.parseCommas(catStr.toLowerCase(), true);
					if(categories.contains(getCategory()))
					{
						choicesl.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
						final int align=CMath.s_int(planeVars.get(PlanarVar.ALIGNMENT.toString()));
						if(align < -7500)
							choices.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
					}
				}
			}
			if(choices.size()==0)
				choices.addAll(choicesl);
			if(choices.size()==0)
			{
				mob.tell(L("There is nowhere to portal to."));
				return false;
			}

			return oldRoom.show(mob,oldRoom,this,verbalCastCode(mob,oldRoom,auto),L("^S<S-NAME> begin(s) a powerful prayer to @x1.^?",super.hisHerDiety(mob)));
		}
		else
		if(secondsElapsed % 8 == 0)
		{
			if(!oldRoom.show(mob,null,null,CMMsg.MSG_NOISE, L("<S-NAME> continue(s) <S-HIS-HER> powerful prayer to @x1.^?",super.hisHerDiety(mob))))
				return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		newRoom=null;
		oldRoom=mob.location();
		planeAble=null;
		if(oldRoom == null)
			return false;

		if(mob.fetchFaction(CMLib.factions().getAlignmentID())!=Integer.MAX_VALUE)
		{
			if(CMath.bset(flags(), Ability.FLAG_UNHOLY))
				CMLib.factions().postFactionChange(mob,this, CMLib.factions().getAlignmentID(), -100);
			if(CMath.bset(flags(), Ability.FLAG_HOLY))
				CMLib.factions().postFactionChange(mob,this, CMLib.factions().getAlignmentID(), 100);
		}

		PlanarAbility planeAble = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		String planeName = "";
		final List<String> choices = new ArrayList<String>();
		final List<String> choicesl = new ArrayList<String>();
		for(final String planeKey : planeAble.getAllPlaneKeys())
		{
			final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
			final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
			if(catStr != null)
			{
				final List<String> categories=CMParms.parseCommas(catStr.toLowerCase(), true);
				if(categories.contains(getCategory()))
				{
					choicesl.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
					final int align=CMath.s_int(planeVars.get(PlanarVar.ALIGNMENT.toString()));
					if(align < -7500)
						choices.add(CMStrings.capitalizeAllFirstLettersAndLower(planeKey));
				}
			}
		}
		if(choices.size()==0)
			choices.addAll(choicesl);
		if(choices.size()==0)
		{
			mob.tell(L("There is nowhere to portal to."));
			return false;
		}
		choicesl.clear();
		if(mob.fetchFaction(CMLib.factions().getInclinationID())!=Integer.MAX_VALUE)
		{
			final Faction myF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());
			final int myInclination=mob.fetchFaction(CMLib.factions().getInclinationID());
			final Faction.FRange myFR = myF.fetchRange( myInclination);
			for(final String planeKey : choices)
			{
				final Map<String,String> planeVars = planeAble.getPlanarVars(planeKey);
				final String factions = planeVars.get(PlanarVar.FACTIONS.toString());
				if(factions!=null)
				{
					final PairList<String,String> factionList=new PairVector<String,String>(CMParms.parseSpaceParenList(factions));
					for(final Pair<String,String> p : factionList)
					{
						final String factionName = p.first;
						if(p.first.equals("*"))
							continue;
						Faction F=null;
						if(CMLib.factions().isFactionID(factionName))
							F=CMLib.factions().getFaction(factionName);
						if(F==null)
							F=CMLib.factions().getFactionByName(factionName);
						if((F!=null)
						&&(F.factionID().equalsIgnoreCase(CMLib.factions().getInclinationID())))
						{
							final Faction.FRange FR;
							if(CMath.isInteger(p.second))
								FR=F.fetchRange(CMath.s_int(p.second));
							else
								FR = F.fetchRange(p.second);
							if(FR==myFR)
								choicesl.add(planeKey);
						}
					}
				}
			}
		}
		if(choicesl.size()>0)
		{
			choices.clear();
			choices.addAll(choicesl);
		}

		planeName = choices.get(CMLib.dice().roll(1, choices.size(), -1));

		final int profNeg = 0; //
		final boolean success=proficiencyCheck(mob,-profNeg,auto);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		newRoom=null;

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,oldRoom,this,verbalCastCode(mob,oldRoom,auto),L("^S<S-NAME> @x1 a blinding, swirling portal here.^?",prayForWord(mob)));
			if(oldRoom.okMessage(mob,msg))
			{
				int minLevel = oldRoom.getArea().getAreaIStats()[Area.Stats.MIN_LEVEL.ordinal()];
				if(minLevel > mob.phyStats().level() + super.getXLEVELLevel(mob))
					minLevel = mob.phyStats().level() + super.getXLEVELLevel(mob);
				final MOB invoker = CMClass.getFactoryMOB(mob.Name(), minLevel, mob.location());
				invoker.basePhyStats().setDisposition(invoker.basePhyStats().disposition()|PhyStats.IS_NOT_SEEN);
				invoker.basePhyStats().setDisposition(invoker.phyStats().disposition()|PhyStats.IS_NOT_SEEN);
				final Vector<String> cmds=new XVector<String>(planeName);
				planeAble.invoke(invoker, cmds, null, true, asLevel);
				if(CMLib.flags().getPlaneOfExistence(invoker)!=null)
				{
					newRoom=invoker.location();
					for(final Enumeration<Ability> a=newRoom.getArea().effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if(A instanceof PlanarAbility)
							planeAble = (PlanarAbility)A;
					}
				}
				// invoker.destroy(); -- let them continue their mission
				planeAble.setPlanarLevel(minLevel);

				final CMMsg msg2=CMClass.getMsg(mob,newRoom,this,verbalCastCode(mob,newRoom,auto),L("A blinding, swirling portal appears here."));
				if(newRoom.okMessage(mob,msg2))
				{
					oldRoom.send(mob,msg);
					newRoom=(Room)msg2.target();
					newRoom.send(mob,msg2);
					final Exit e=(Exit)CMClass.getItem("GenPortal");
					e.setDescription(L("A swirling portal to somewhere"));
					e.setDisplayText(L("A swirling portal to somewhere"));
					e.setDoorsNLocks(false,true,false,false,false,false);
					e.setExitParams("portal","close","open","closed.");
					e.setName(L("a swirling portal"));
					final Ability A1=CMClass.getAbility("Prop_RoomView");
					if(A1!=null)
					{
						A1.setMiscText(CMLib.map().getExtendedRoomID(newRoom));
						e.addNonUninvokableEffect(A1);
					}
					e.setReadableText(CMLib.map().getExtendedRoomID(newRoom));
					final Exit e2=(Exit)e.copyOf();
					final Ability A2=CMClass.getAbility("Prop_RoomView");
					if(A2!=null)
					{
						A2.setMiscText(CMLib.map().getExtendedRoomID(oldRoom));
						e2.addNonUninvokableEffect(A2);
					}
					e2.setReadableText(CMLib.map().getExtendedRoomID(oldRoom));
					oldRoom.addItem((Item)e);
					newRoom.addItem((Item)e2);
					final Prayer_MalignedPortal portal = (Prayer_MalignedPortal)beneficialAffect(mob,e,asLevel,0);
					if(portal != null)
					{
						portal.planeAble = planeAble;
						portal.items.add((Item)e2);
						final MOB fakeTarget=CMClass.getFactoryMOB(e.Name(),1,oldRoom);
						final int hitPoints = mob.maxState().getHitPoints()+(10*super.getXLEVELLevel(mob));
						fakeTarget.baseState().setHitPoints(hitPoints);
						fakeTarget.maxState().setHitPoints(hitPoints);
						fakeTarget.curState().setHitPoints(hitPoints);
						final Ability immA=CMClass.getAbility("Prop_WeaponImmunity");
						if(immA!=null)
						{
							immA.setMiscText("+ALL -MAGIC");
							fakeTarget.addNonUninvokableEffect(immA);
						}
						portal.fakeTarget=fakeTarget;
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to evoke a portal, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
