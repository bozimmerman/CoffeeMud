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

import java.util.*;

/*
   Copyright 2024-2025 Bo Zimmerman

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
public class Thief_CallUrchins extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_CallUrchins";
	}

	private final static String	localizedName	= CMLib.lang().L("Call Urchins");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"CALLURCHINS"});

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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final int range = 3 + (adjustedLevel(mob,asLevel)/5) + super.getXLEVELLevel(mob);
		final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags()
												.plus(TrackingLibrary.TrackingFlag.AREAONLY)
												.plus(TrackingLibrary.TrackingFlag.PASSABLE);
		final List<Room> rooms = CMLib.tracking().getRadiantRooms(mob.location(), flags, range);
		MOB firstM=null;
		final List<MOB> H=Thief_MyUrchins.getMyUrchins(mob);
		for(final Iterator<MOB> m=H.iterator();m.hasNext();)
		{
			final MOB M=m.next();
			if(!rooms.contains(M.location()))
				m.remove();
		}
		if(H.size()==0)
		{
			mob.tell(L("You don't have any urchins in range in this area!"));
			return false;
		}

		boolean allHere=true;
		for (final MOB M : H)
		{
			if((M!=mob)
			&&(M.location()!=mob.location())
			&&(M.location()!=null))
			{
				allHere=false;
				if((CMLib.flags().canAccess(mob,M.location()))
				&&(!CMLib.flags().isTracking(M)))
				{
					firstM=M;
					break;
				}
			}
		}
		if((firstM==null)&&(allHere))
		{
			mob.tell(L("Better look around first."));
			return false;
		}

		if(firstM==null)
		{
			mob.tell(L("Either they are all en route, or you have not heard from your urchins."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),
					auto?"":L("^S<S-NAME> call(s) on <S-HIS-HER> local urchins to gather for a meeting!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				msg.setSourceMessage("");
				msg.setOthersMessage("");
				for (final Object element : H)
				{
					final MOB M=(MOB)element;
					if((M!=mob)
					&&(M.location()!=mob.location())
					&&(M.location()!=null))
					{
						final Room oldRoom = M.location();
						allHere=false;
						if((CMLib.flags().canAccess(mob,M.location()))
						&&(!CMLib.flags().isTracking(M)))
						{
							oldRoom.sendOthers(mob,msg);
							final Room newRoom=mob.location();
							final Ability A=CMClass.getAbility("Skill_Track");
							if(A!=null)
							{
								final List<String> lst = new XVector<String>(CMLib.map().getExtendedRoomID(newRoom),"NPC");
								A.invoke(M,lst,newRoom,true,0);
								return true;
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to call <S-HIS-HER> urchins, but fail(s)."));

		// return whether it worked
		return success;
	}
}
