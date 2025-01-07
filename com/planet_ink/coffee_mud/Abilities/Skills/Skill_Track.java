package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.RFilter;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class Skill_Track extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Track";
	}

	protected int				cacheCode		= -1;
	private int					tickStatus		= 0;
	protected List<Room>		theTrail		= null;
	public int					nextDirection	= -2;
	protected boolean			andBefriend		= false;
	protected List<String>		persistantV		= null;
	protected Reference<MOB>	targetMob		= null;
	protected boolean			noUnlock		= false;

	private final static String	localizedName	= CMLib.lang().L("Tracking");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Tracking)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS | CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TRACKTO" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING;
	}

	private final Map<String, List<Room>>	cachedPaths	= new Hashtable<String, List<Room>>();

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public int abilityCode()
	{
		return cacheCode;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		cacheCode = newCode;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void unInvoke()
	{
		if(!unInvoked)
		{
		}
		super.unInvoke();
	}


	public synchronized boolean bePersistant(final MOB mob)
	{
		final List<String> persistantV = this.persistantV;
		if(persistantV != null)
		{
			final MOB target=(this.targetMob!=null)?this.targetMob.get():null;
			CMLib.threads().scheduleRunnable(new Runnable()
			{
				final MOB targetM=target;
				@Override
				public void run()
				{
					if(!CMLib.flags().isTracking(mob))
					{
						final Ability A=new Skill_Track();
						A.invoke(mob, persistantV, targetM, true, 0);
					}
				}
			}, 30000);
			return true;
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			final MOB mob=(MOB)affected;

			if(checkIfMobFound(mob))
				return false;

			if(nextDirection==999)
			{
				mob.tell(L("The trail seems to pause here."));
				nextDirection=-2;
				unInvoke();
				// ok, you made it, but if you were looking
				// for a specific person, and didn't find them,
				// then look some more and be persistant.
				if((this.targetMob!=null)
				&&(this.targetMob.get()!=null))
				{
					if(bePersistant(mob))
						return false;
				}
			}
			else
			if(nextDirection==-1)
			{
				mob.tell(L("The trail dries up here."));
				nextDirection=-999;
				unInvoke();
				if(bePersistant(mob))
					return false;
			}
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
						boolean relock=false;
						// handle doors!
						if(nextExit.hasADoor()&&(!nextExit.isOpen()))
						{
							if(noUnlock)
							{
								final CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OPEN,
										L("<S-NAME> @x1 <T-NAMESELF>.",((nextExit.openWord().indexOf('(')>0)?nextExit.openWord():(nextExit.openWord()+"(s)"))));
								if(oldRoom.okMessage(mob,msg))
								{
									reclose=true;
									relock=false;
									CMLib.utensils().roomAffectFully(msg,oldRoom,nextDirection);
								}
							}
							else
							if((nextExit.hasALock())&&(nextExit.isLocked()))
							{
								CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(oldRoom.okMessage(mob,msg))
								{
									relock=true;
									msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,
											L("<S-NAME> unlock(s) <T-NAMESELF><O-WITHNAME>."));
									CMLib.utensils().roomAffectFully(msg,oldRoom,nextDirection);
								}
							}
							CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
							if(oldRoom.okMessage(mob,msg))
							{
								reclose=true;
								msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,
										L("<S-NAME> @x1 <T-NAMESELF>.",((nextExit.openWord().indexOf('(')>0)?nextExit.openWord():(nextExit.openWord()+"(s)"))));
								CMLib.utensils().roomAffectFully(msg,oldRoom,nextDirection);
							}
						}
						if(!nextExit.isOpen())
							unInvoke();
						else
						{
							final int dir=nextDirection;
							nextDirection=-2;
							CMLib.tracking().walk(mob,dir,false,false);
							if(mob.location()==nextRoom)
							{
								// backup follower mover for handcuffed followers
								final LinkedList<MOB> reMoveV=new LinkedList<MOB>();
								for(final Enumeration<Pair<MOB,Short>> e=mob.followers(); e.hasMoreElements();)
								{
									final Pair<MOB,Short> F=e.nextElement();
									if((F.first != null)
									&&(F.first != mob)
									&&(F.first.location()==oldRoom)
									&&(F.first.location()!=nextRoom))
										reMoveV.add(F.first);
								}
								for(final MOB M : reMoveV)
								{
									if(CMLib.flags().isBoundOrHeld(M))
										CMLib.tracking().walk(M,dir,false,false);
								}
								if(reclose)
								{
									final Exit opExit=nextRoom.getExitInDir(opDirection);
									if((opExit!=null)
									&&(opExit.hasADoor())
									&&(opExit.isOpen()))
									{
										CMMsg msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
										if(nextRoom.okMessage(mob,msg))
										{
											msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_CLOSE,CMMsg.MSG_OK_VISUAL,
													L("<S-NAME> @x1 <T-NAMESELF>.",((nextExit.closeWord().indexOf('(')>0)?nextExit.closeWord():(nextExit.closeWord()+"(s)"))));
											CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
										}
										if((opExit.hasALock())&&(relock))
										{
											msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
											if(nextRoom.okMessage(mob,msg))
											{
												msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,
														L("<S-NAME> lock(s) <T-NAMESELF><O-WITHNAME>."));
												CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
											}
										}
									}
								}
							}
						}
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	protected boolean checkIfMobFound(final MOB searcherM)
	{
		if(this.targetMob!=null)
		{
			final MOB M=this.targetMob.get();
			if(M!=null)
			{
				if((M.location()==searcherM.location())
				&&(CMLib.flags().isInTheGame(searcherM, true))
				&&(CMLib.flags().isInTheGame(M, true)))
				{
					searcherM.tell(L("The trail seems to pause here."));
					nextDirection=-2;
					unInvoke();
					if(andBefriend)
						CMLib.commands().postFollow(searcherM, M , false);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
		{
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
			checkIfMobFound(mob);
		}
	}

	private static class FilterMOBName implements RFilter
	{
		final String mobName;
		final MOB viewerM;
		public FilterMOBName(final MOB viewerM, final String mobName)
		{
			this.viewerM=viewerM;
			this.mobName=mobName;
		}
		@Override
		public boolean isFilteredOut(final Room hostR, Room R, final Exit E, final int dir)
		{
			R=CMLib.map().getRoom(R);
			if(R!=null)
			{
				final MOB M=R.fetchInhabitant(mobName);
				if((M!=null)
				&&((viewerM==null)||(CMLib.flags().canAccess(viewerM, R)))
				&&((viewerM==null)||(CMLib.flags().isSeeable(M))))
					return false;
			}
			return true;
		}
	}

	private static class FilterMOB implements RFilter
	{
		final MOB M;
		public FilterMOB(final MOB M)
		{
			this.M = M;
		}
		@Override
		public boolean isFilteredOut(final Room hostR, final Room R, final Exit E, final int dir)
		{
			if((R==M.location())||(hostR==M.location()))
				return false;
			return true;
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> origArgs = (commands != null)?new XVector<String>(commands):new Vector<String>(1);
		tickStatus=Tickable.STATUS_MISC6;
		if((!CMLib.flags().isAliveAwakeMobile(mob,false))
		||(mob.location()==null)
		||(!CMLib.flags().isInTheGame(mob,true)))
		{
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}
		tickStatus=Tickable.STATUS_MISC6+1;
		final Room startRoom=mob.location();

		boolean befriend=false;
		boolean persist = false;
		if(commands != null)
		{
			if(commands.size()>2)
			{
				if(commands.get(0).equalsIgnoreCase("and")
				&&(commands.get(1).equalsIgnoreCase("follow")||commands.get(1).equalsIgnoreCase("befriend")))
				{
					commands.remove(0);
					commands.remove(0);
					befriend=true;
				}
			}
			if(commands.size()>2)
			{
				if(commands.get(0).equalsIgnoreCase("and")
				&&(commands.get(1).equalsIgnoreCase("persist")))
				{
					commands.remove(0);
					commands.remove(0);
					persist=true;
				}
			}

			final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
			for(final Ability A : V)
			{
				if((A.ID().equals(ID()))
				&&(A.text().length()>0)
				&&(A.text().equals(CMParms.combine(commands,0))))
				{
					mob.tell(L("You are already tracking that!"));
					return true;
				}
				A.unInvoke();
			}
			if(V.size()>0)
			{
				mob.tell(L("You stop tracking."));
				if((commands.size()==0)||(CMParms.combine(commands,0).equalsIgnoreCase("stop")))
				{
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}
		}


		tickStatus=Tickable.STATUS_MISC6+2;
		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}

		tickStatus=Tickable.STATUS_MISC6+3;
		boolean noUnlock = false;
		int radius=50 + (10*(super.getXMAXRANGELevel(mob)+super.getXLEVELLevel(mob)));
		final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
		flags.plus(TrackingLibrary.TrackingFlag.PASSABLE);
		if(commands != null)
		{
			while(commands.size()>1)
			{
				if(((commands.get(commands.size()-1)).toUpperCase().startsWith("RADIUS="))
				&&(CMath.isInteger((commands.get(commands.size()-1)).substring(7))))
				{
					radius=CMath.s_int((commands.get(commands.size()-1)).substring(7));
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).equalsIgnoreCase("LANDONLY"))
				{
					flags.plus(TrackingLibrary.TrackingFlag.NOAIR);
					flags.plus(TrackingLibrary.TrackingFlag.NOWATER);
					flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).equalsIgnoreCase("NOUNLOCK"))
				{
					noUnlock=true;
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).equalsIgnoreCase("NPC"))
				{
					if(!mob.isPlayer())
					{
						if(!CMLib.flags().canSwim(mob))
							flags.plus(TrackingLibrary.TrackingFlag.NOWATER);
						if(!CMLib.flags().isFlying(mob))
							flags.plus(TrackingLibrary.TrackingFlag.NOAIR);
						flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
					}
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).equalsIgnoreCase("NOAIR"))
				{
					flags.plus(TrackingLibrary.TrackingFlag.NOAIR);
					if(flags.contains(TrackingFlag.NOWATER))
						flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).equalsIgnoreCase("NOWATER"))
				{
					flags.plus(TrackingLibrary.TrackingFlag.NOWATER);
					if(flags.contains(TrackingFlag.NOAIR))
						flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
					commands.remove(commands.size()-1);
				}
				else
				if((commands.get(commands.size()-1)).startsWith("FLAG="))
				{
					final TrackingFlag flag = (TrackingFlag)CMath.s_valueOf(TrackingFlag.class, (commands.get(commands.size()-1)).substring(5).trim());
					if(flag != null)
						flags.plus(flag);
					commands.remove(commands.size()-1);
				}
				else
					break;
			}
		}

		final String mobName=CMParms.combine(commands,0);
		setMiscText(mobName);
		if((givenTarget==null)&&(mobName.length()==0))
		{
			mob.tell(L("Track whom?"));
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}

		if(givenTarget==null)
			givenTarget=CMLib.map().getRoom(mobName);

		if(givenTarget==null)
			givenTarget=CMLib.map().getArea(mobName);

		tickStatus=Tickable.STATUS_MISC6+4;
		if(((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()==mob.location()))
		||(startRoom.fetchInhabitant(mobName)!=null))
		{
			final MOB M = startRoom.fetchInhabitant(mobName);
			mob.tell(L("Try 'look'."));
			if(befriend)
				CMLib.commands().postFollow(mob, M, false);
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}

		final ArrayList<Room> targetRoomsV=new ArrayList<Room>();
		if(givenTarget instanceof Area)
			targetRoomsV.add(((Area)givenTarget).getRandomMetroRoom());
		else
		if(givenTarget instanceof Room)
			targetRoomsV.add((Room)givenTarget);
		else
		if((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()!=null))
			targetRoomsV.add(((MOB)givenTarget).location());
		else
		if(mobName.length()>0)
		{
			final Room R=CMLib.map().getRoom(mobName);
			if(R!=null)
				targetRoomsV.add(R);
		}

		tickStatus=Tickable.STATUS_MISC6+5;
		if(targetRoomsV.size()<=0)
		{
			try
			{
				final List<Room> trashRooms = new ArrayList<Room>();
				final MOB M1=CMLib.players().findPlayerOnline(mobName, true);
				final RFilter targetFilter;
				if((M1!=null)
				&&(CMLib.flags().canAccess(mob, M1.location()))
				&&(CMLib.flags().isSeeable(M1)))
					targetFilter = new FilterMOB(M1);
				else
					targetFilter = new FilterMOBName(mob,mobName);
				if(CMLib.tracking().getRadiantRoomsToTarget(startRoom, trashRooms, flags, targetFilter, radius))
					targetRoomsV.add(trashRooms.get(trashRooms.size()-1));
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		tickStatus=Tickable.STATUS_MISC6+6;

		tickStatus=Tickable.STATUS_MISC6+7;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(targetRoomsV.size()>0)
		{
			if((targetRoomsV.size()==1)
			&&(mob.location()==targetRoomsV.get(0)))
			{
				mob.tell(L("Try 'look'."));
				if(befriend)
				{
					if(givenTarget instanceof MOB)
						CMLib.commands().postFollow(mob, (MOB)givenTarget, false);
					else
					if(mob.location().fetchInhabitant(mobName)!=null)
					{
						final MOB M = mob.location().fetchInhabitant(mobName);
						CMLib.commands().postFollow(mob, M, false);
					}
				}
				tickStatus=Tickable.STATUS_NOT;
				return false;
			}
			theTrail=null;
			tickStatus=Tickable.STATUS_MISC6+8;
			if((cacheCode==1)&&(targetRoomsV.size()==1))
				theTrail=cachedPaths.get(CMLib.map().getExtendedRoomID(startRoom)+"->"+CMLib.map().getExtendedRoomID(targetRoomsV.get(0)));
			tickStatus=Tickable.STATUS_MISC6+9;
			if(theTrail==null)
				theTrail=CMLib.tracking().findTrailToAnyRoom(startRoom,targetRoomsV,flags,radius);
			tickStatus=Tickable.STATUS_MISC6+10;
			if((cacheCode==1)&&(targetRoomsV.size()==1)&&(theTrail!=null))
				cachedPaths.put(CMLib.map().getExtendedRoomID(startRoom)+"->"+CMLib.map().getExtendedRoomID(targetRoomsV.get(0)),theTrail);
		}

		tickStatus=Tickable.STATUS_MISC6+11;
		if((success)&&(theTrail!=null))
		{
			theTrail.add(startRoom);

			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,mob.isMonster()?null:L("<S-NAME> begin(s) to track."));
			if(startRoom.okMessage(mob,msg))
			{
				tickStatus=Tickable.STATUS_MISC6+12;
				startRoom.send(mob,msg);
				invoker=mob;
				final Skill_Track newOne=(Skill_Track)copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				tickStatus=Tickable.STATUS_MISC6+13;
				newOne.noUnlock = noUnlock;
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,startRoom,false);
				newOne.andBefriend=befriend;
				if(persist)
				{
					newOne.persistantV=new XVector<String>();
					newOne.persistantV.addAll(origArgs);
				}
				if(givenTarget instanceof MOB)
					newOne.targetMob = new WeakReference<MOB>((MOB)givenTarget);
			}
			tickStatus=Tickable.STATUS_MISC6+14;
		}
		else
		{
			tickStatus=Tickable.STATUS_NOT;
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to track, but can't find the trail."));
		}
		tickStatus=Tickable.STATUS_NOT;
		// return whether it worked
		return success;
	}
}
