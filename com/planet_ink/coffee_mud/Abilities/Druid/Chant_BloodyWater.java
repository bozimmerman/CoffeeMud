package com.planet_ink.coffee_mud.Abilities.Druid;
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

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_BloodyWater extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_BloodyWater";
	}

	private final static String	localizedName	= CMLib.lang().L("Bloody Water");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Bloody Water)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS | CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		if(affectedEnv instanceof MOB)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	protected final List<MOB> bloodyMobs = new Vector<MOB>();
	protected Room theRoom = null;
	protected List<Room> theTrail=null;

	final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.AREAONLY)
												.plus(TrackingLibrary.TrackingFlag.UNDERWATERONLY);
	
	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if(mob.amFollowing()==null)
				{
					mob.tell(L("You are no longer sensing the bloody water."));
					if(!mob.amDead())
						CMLib.tracking().wanderAway(mob,true,false);
				}
			}
			else
			{
				for(final MOB mob : bloodyMobs)
				{
					Ability A=mob.fetchEffect(ID());
					if(A!=null)
						A.unInvoke();
				}
				bloodyMobs.clear();
				if(affected instanceof Room)
					((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,L("The water is no longer to bloody."));
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final MOB mob=invoker();
		final int limit = (mob==null)? 10 : (5 + (2 * (super.getXMAXRANGELevel(mob)+super.getXLEVELLevel(mob))));
		if((affected instanceof MOB)
		&&(theRoom!=null))
		{
			if(theTrail==null)
				unInvoke();
			else
			{
				MOB M=(MOB)affected;
				if(M.location() == theRoom)
				{
					unInvoke();
				}
				else
				{
					int dir=CMLib.tracking().trackNextDirectionFromHere(theTrail,M.location(),true);
					if(dir < 0)
						unInvoke();
					else
						CMLib.tracking().walk(M, dir, false, false);
				}
			}
		}
		else
		if(affected instanceof Room)
		{
			final Room room=(Room)affected;
			if((bloodyMobs.size()==0)&&(mob!=null))
			{
				final List<Room> destRooms = new XVector<Room>(room);
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(room,flags,limit);
				for(final Room R : checkSet)
				{
					if((R!=null)&&(R!=room))
					{
						for(Enumeration<MOB> m= R.inhabitants();m.hasMoreElements();)
						{
							final MOB M = m.nextElement();
							if(CMLib.flags().isAnimalIntelligence(M)
							&&(bloodyMobs.size()<limit)
							&&(M.isMonster())
							&&((M.amFollowing()==null)||(M.amFollowing().isMonster()))
							&&(M.charStats().getMyRace().racialCategory().equalsIgnoreCase("Fish"))
							&&(!M.amDead())
							&&(CMLib.flags().canActAtAll(M))
							&&(!M.isInCombat())
							&&(!CMLib.flags().isTracking(M))
							&(CMLib.flags().canTrack(M))
							&&(!bloodyMobs.contains(M)))
							{
								bloodyMobs.add(M);
								Chant_BloodyWater w=(Chant_BloodyWater)this.copyOf();
								w.theRoom = room;
								w.theTrail = CMLib.tracking().findTrailToAnyRoom(M.location(), destRooms, flags, limit);
								if(w.theTrail != null)
									w.startTickDown(mob, M, w.tickDown);
							}
						}
					}
				}
			}
			if(bloodyMobs.size()>0)
			{
				for(Iterator<MOB> m=bloodyMobs.iterator();m.hasNext();)
				{
					MOB M=m.next();
					if(M.location()==room)
						m.remove();
					else
					if(M.fetchEffect(ID())==null)
					{
						m.remove();
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("The waters here are already bloody."));
			return false;
		}
		
		if(!CMLib.flags().isUnderWateryRoom(mob.location()))
		{
			mob.tell(L("You must be underwater for this chant to work."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to the waters.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The water becomes red with streaks of blood!"));
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) to the water, but the magic fades."));
		// return whether it worked
		return success;
	}
}
