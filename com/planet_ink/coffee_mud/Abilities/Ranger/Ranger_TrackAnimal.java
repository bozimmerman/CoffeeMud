package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Ranger_TrackAnimal extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_TrackAnimal";
	}

	private final static String localizedName = CMLib.lang().L("Track Animal");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String displayText=L("(tracking an animal)");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"TRACKANIMAL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_COMBATLORE;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRACKING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected List<Room> theTrail=null;
	public int nextDirection=-2;

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
				if(mob.isMonster())
				{
					final Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						final int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().walk(mob,dir,false,false);
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
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public MOB animalHere(Room room)
	{
		if(room==null)
			return null;

		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB mob=room.fetchInhabitant(i);
			if(CMLib.flags().isAnimalIntelligence(mob))
				return mob;
		}
		return null;
	}

	@Override
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TRACK);
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().isAliveAwakeMobile(mob,false))
			return false;

		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			mob.tell(L("You can't see anything to track!"));
			return false;
		}

		final List<Ability> V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(final Ability A : V) A.unInvoke();
		if(V.size()>0)
		{
			mob.tell(L("You stop tracking."));
			if(commands.size()==0)
				return true;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(animalHere(mob.location())!=null)
		{
			mob.tell(L("Try 'look'."));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		TrackingLibrary.TrackingFlags flags;
		flags=CMLib.tracking().newFlags()
			.plus(TrackingLibrary.TrackingFlag.OPENONLY)
			.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			.plus(TrackingLibrary.TrackingFlag.NOAIR)
			.plus(TrackingLibrary.TrackingFlag.NOWATER);

		final Vector<Room> rooms=new Vector<Room>();
		int range=50 + (2*super.getXLEVELLevel(mob))+(10*super.getXMAXRANGELevel(mob));
		final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,range);
		for (final Room room : checkSet)
		{
			final Room R=CMLib.map().getRoom(room);
			if(animalHere(R)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findTrailToAnyRoom(mob.location(),rooms,flags,range);

		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=animalHere(theTrail.get(0));

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.add(mob.location());
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> begin(s) to track <T-NAMESELF>."),null,L("<S-NAME> begin(s) to track <T-NAMESELF>."));
			if((mob.location().okMessage(mob,msg))&&(target.okMessage(target,msg)))
			{
				mob.location().send(mob,msg);
				target.executeMsg(target,msg);
				invoker=mob;
				displayText=L("(tracking @x1)",target.name());
				final Ranger_TrackAnimal newOne=(Ranger_TrackAnimal)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverPhyStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to track an animal, but can't find the trail."));

		// return whether it worked
		return success;
	}

}
