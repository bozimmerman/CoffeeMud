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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
	public void setAbilityCode(int newCode)
	{
		cacheCode = newCode;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
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
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
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

			if(nextDirection==999)
			{
				mob.tell(L("The trail seems to pause here."));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell(L("The trail dries up here."));
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell(L("The trail seems to continue @x1.",CMLib.directions().getDirectionName(nextDirection)));
				if((mob.isMonster())&&(mob.location()!=null))
				{
					final Room oldRoom=mob.location();
					final Room nextRoom=oldRoom.getRoomInDir(nextDirection);
					final Exit nextExit=oldRoom.getExitInDir(nextDirection);
					final int opDirection=Directions.getOpDirectionCode(nextDirection);
					if((nextRoom!=null)&&(nextExit!=null))
					{
						boolean reclose=false;
						boolean relock=false;
						// handle doors!
						if(nextExit.hasADoor()&&(!nextExit.isOpen()))
						{
							if((nextExit.hasALock())&&(nextExit.isLocked()))
							{
								CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
								if(oldRoom.okMessage(mob,msg))
								{
									relock=true;
									msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> unlock(s) <T-NAMESELF>."));
									CMLib.utensils().roomAffectFully(msg,oldRoom,nextDirection);
								}
							}
							CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
							if(oldRoom.okMessage(mob,msg))
							{
								reclose=true;
								msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OPEN,CMMsg.MSG_OK_VISUAL,L("<S-NAME> @x1(s) <T-NAMESELF>.",nextExit.openWord()));
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
											msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_CLOSE,CMMsg.MSG_OK_VISUAL,L("<S-NAME> @x1(s) <T-NAMESELF>.",nextExit.closeWord()));
											CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
										}
										if((opExit.hasALock())&&(relock))
										{
											msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
											if(nextRoom.okMessage(mob,msg))
											{
												msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> lock(s) <T-NAMESELF>."));
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
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		tickStatus=Tickable.STATUS_MISC6;
		if((!CMLib.flags().isAliveAwakeMobile(mob,false))||(mob.location()==null)||(!CMLib.flags().isInTheGame(mob,true)))
		{
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}
		tickStatus=Tickable.STATUS_MISC6+1;
		final Room thisRoom=mob.location();

		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V) 
			A.unInvoke();
		if(V.size()>0)
		{
			mob.tell(L("You stop tracking."));
			if((commands.size()==0)||(CMParms.combine(commands,0).equalsIgnoreCase("stop")))
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
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
		int radius=50 + (10*(super.getXMAXRANGELevel(mob)+super.getXLEVELLevel(mob)));
		boolean allowAir=true;
		boolean allowWater=true;
		if((commands.size()>1)
		&&((commands.get(commands.size()-1)).toUpperCase().startsWith("RADIUS="))
		&&(CMath.isInteger((commands.get(commands.size()-1)).substring(7))))
		{
			radius=CMath.s_int((commands.get(commands.size()-1)).substring(7));
			commands.remove(commands.size()-1);
		}
		if((commands.size()>1)&&((commands.get(commands.size()-1)).equalsIgnoreCase("LANDONLY")))
		{
			allowAir=false;
			allowWater=false;
			commands.remove(commands.size()-1);
		}
		if((commands.size()>1)&&((commands.get(commands.size()-1)).equalsIgnoreCase("NOAIR")))
		{
			allowAir=false;
			commands.remove(commands.size()-1);
		}
		if((commands.size()>1)&&((commands.get(commands.size()-1)).equalsIgnoreCase("NOWATER")))
		{
			allowWater=false;
			commands.remove(commands.size()-1);
		}

		final String mobName=CMParms.combine(commands,0);
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
		if((givenTarget==null)
		&&(thisRoom.fetchInhabitant(mobName)!=null))
		{
			mob.tell(L("Try 'look'."));
			tickStatus=Tickable.STATUS_NOT;
			return false;
		}

		final Vector<Room> rooms=new Vector<Room>();
		if(givenTarget instanceof Area)
			rooms.addElement(((Area)givenTarget).getRandomMetroRoom());
		else
		if(givenTarget instanceof Room)
			rooms.addElement((Room)givenTarget);
		else
		if((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()!=null))
			rooms.addElement(((MOB)givenTarget).location());
		else
		if(mobName.length()>0)
		{
			final Room R=CMLib.map().getRoom(mobName);
			if(R!=null)
				rooms.addElement(R);
		}

		final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
		if(!(allowAir||allowWater))
			flags.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		if(!allowAir)
			flags.plus(TrackingLibrary.TrackingFlag.NOAIR);
		if(!allowWater)
			flags.plus(TrackingLibrary.TrackingFlag.NOWATER);
		tickStatus=Tickable.STATUS_MISC6+5;
		if(rooms.size()<=0)
		{
			try
			{
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(thisRoom,flags,radius);
				for (final Room room : checkSet)
				{
					final Room R=CMLib.map().getRoom(room);
					if(R!=null)
					{
						if(R.fetchInhabitant(mobName)!=null)
							rooms.addElement(R);
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		tickStatus=Tickable.STATUS_MISC6+6;

		tickStatus=Tickable.STATUS_MISC6+7;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(rooms.size()>0)
		{
			theTrail=null;
			tickStatus=Tickable.STATUS_MISC6+8;
			if((cacheCode==1)&&(rooms.size()==1))
				theTrail=cachedPaths.get(CMLib.map().getExtendedRoomID(thisRoom)+"->"+CMLib.map().getExtendedRoomID(rooms.firstElement()));
			tickStatus=Tickable.STATUS_MISC6+9;
			if(theTrail==null)
				theTrail=CMLib.tracking().findTrailToAnyRoom(thisRoom,rooms,flags,radius);
			tickStatus=Tickable.STATUS_MISC6+10;
			if((cacheCode==1)&&(rooms.size()==1)&&(theTrail!=null))
				cachedPaths.put(CMLib.map().getExtendedRoomID(thisRoom)+"->"+CMLib.map().getExtendedRoomID(rooms.firstElement()),theTrail);
		}

		tickStatus=Tickable.STATUS_MISC6+11;
		if((success)&&(theTrail!=null))
		{
			theTrail.add(thisRoom);

			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,mob.isMonster()?null:L("<S-NAME> begin(s) to track."));
			if(thisRoom.okMessage(mob,msg))
			{
				tickStatus=Tickable.STATUS_MISC6+12;
				thisRoom.send(mob,msg);
				invoker=mob;
				final Skill_Track newOne=(Skill_Track)copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				tickStatus=Tickable.STATUS_MISC6+13;
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,thisRoom,false);
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
