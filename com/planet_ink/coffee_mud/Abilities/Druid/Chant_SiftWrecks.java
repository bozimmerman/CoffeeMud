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

public class Chant_SiftWrecks extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SiftWrecks";
	}

	private final static String	localizedName	= CMLib.lang().L("Sift Wrecks");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!auto && !CMLib.flags().isWateryRoom(mob.location()))
		{
			mob.tell(L("You should be in a watery locale to use this magic."));
			return false;
		}
		
		List<Room> myPlantRoomsInThisArea=Druid_MyPlants.myAreaPlantRooms(mob, mob.location().getArea());
		List<Item> myPlants = Druid_MyPlants.getMyPlants(mob,myPlantRoomsInThisArea);
		Item myCoral = null;
		for(Item I : myPlants)
		{
			Ability A=Druid_MyPlants.getMyPlantsSpell(I,mob);
			if((A!=null)&&(A.ID().equalsIgnoreCase("Chant_SummonCoral")))
			{
				myCoral=I;
				break;
			}
		}
		
		if(myCoral==null)
		{
			mob.tell(L("You need a connection with a summoned coral in this area for this chant to reveal anything."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?L("You feel knowledge of the deeps."):L("^S<S-NAME> chant(s) to @x1 for knowledge of the deeps.^?",myCoral.Name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int radius = (adjustedLevel(mob,asLevel))+(10*super.getXMAXRANGELevel(mob))+super.getXLEVELLevel(mob);
				final TrackingLibrary.TrackingFlags filters=CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.AREAONLY)
						.plus(TrackingLibrary.TrackingFlag.UNDERWATERONLY);
				final List<Room> siftables = CMLib.tracking().getRadiantRooms(mob.location(), filters, radius);
				StringBuilder msgStr=new StringBuilder("");
				for(Room R : siftables)
				{
					if((R.numItems()>0)&&(R.getRoomInDir(Directions.DOWN)==null))
					{
						List<Integer> trailToThisRoom = null;
						for(Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I!=null)&&(I.container()==null))
							{
								if(trailToThisRoom == null)
									trailToThisRoom = CMLib.tracking().getShortestTrail(CMLib.tracking().findAllTrails(mob.location(), R, siftables));
								msgStr.append(CMStrings.padRight(I.name(mob),20));
								if(trailToThisRoom==null)
									msgStr.append(" here? ");
								else
								for(Integer dirI : trailToThisRoom)
									msgStr.append(CMLib.directions().getDirectionChar(dirI.intValue())).append(" ");
								msgStr.append("\n\r");
							}
						}
					}
				}
				if(msgStr.length()==0)
					mob.tell(L("The coral are not telling you about anything in the water."));
				else
					mob.tell(L("The coral sing to you about the following:\n\r")+msgStr.toString());
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to @x1 for knowledge, but nothing happens.",myCoral.Name()));

		// return whether it worked
		return success;
	}
}
