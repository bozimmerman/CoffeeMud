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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Chant_SweetScent extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SweetScent";
	}

	private final static String	localizedName	= CMLib.lang().L("Sweet Scent");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	private final List<Room> rooms = new ArrayList<Room>();
	private volatile int tickDown = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected instanceof Item)&&((tickDown--)<=0))
		{
			tickDown = 2;
			final Item I=(Item)affected;
			if(I.owner() instanceof Room)
			{
				final Room room=(Room)I.owner();
				for(int i=0;i<room.numInhabitants();i++)
				{
					final MOB M=room.fetchInhabitant(i);
					if((M!=null)
					&&(CMLib.flags().isAnAnimal(M))
					&&(CMLib.flags().canSmell(M,I)))
						M.tell(M,I,null,L("<T-NAME> smell(s) absolutely intoxicating!"));
				}
				for(int r=0;r<rooms.size();r++)
				{
					final Room R=rooms.get(r);
					if(R!=room)
					{
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(CMLib.flags().isAnAnimal(M))
							&&(!M.isInCombat())
							&&((!M.isMonster())||(CMLib.flags().isMobile(M)))
							&&(CMLib.flags().canSmell(M,I)))
							{
								final int dir=CMLib.tracking().radiatesFromDir(R,rooms);
								if(dir>=0)
								{
									M.tell(M,null,null,L("You smell something irresistable @x1.",CMLib.directions().getInDirectionName(dir)));
									if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_MIND))
										CMLib.tracking().walk(M,dir,false,false);
								}
							}
						}
					}

				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,L("<T-NAME> smell(s) absolutely intoxicating!"));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(
			(CMLib.flags().isWateryRoom(mob.location()))
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_AIR)
		   )
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}

		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		if(!Druid_MyPlants.isMyPlant(target,mob))
		{
			mob.tell(L("@x1 is not one of your plants!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Room room=CMLib.map().roomLocation(target);
				if(room != null)
				{
					final Chant_SweetScent A = (Chant_SweetScent)beneficialAffect(mob,target,asLevel,0);
					if(A!=null)
					{
						TrackingLibrary.TrackingFlags flags;
						flags = CMLib.tracking().newFlags()
								.plus(TrackingLibrary.TrackingFlag.OPENONLY);
						final int range=10 + super.getXLEVELLevel(mob)+(2*super.getXMAXRANGELevel(mob));
						A.rooms.clear();
						CMLib.tracking().getRadiantRooms(room,A.rooms,flags,null,range,null);
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to the <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
