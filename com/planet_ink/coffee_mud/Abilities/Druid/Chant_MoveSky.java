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
   Copyright 2003-2024 Bo Zimmerman

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
public class Chant_MoveSky extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_MoveSky";
	}

	private final static String	localizedName	= CMLib.lang().L("Move The Sky");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_MOONSUMMONING;
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
	protected int overrideMana()
	{
		return Ability.COST_ALL - 99;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Room room=mob.location();
		if(success && (room!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s), and the sky starts moving.^?"));
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				final TimeClock TO = room.getArea().getTimeObj();
				TimeClock.TimeOfDay targetTOD;
				final String msgStr;
				switch(TO.getTODCode())
				{
				case NIGHT:
					msgStr = L("The moon begins to descend!");
					targetTOD = TimeClock.TimeOfDay.DAWN;
					break;
				case DUSK:
					msgStr = L("The moon races across the sky!");
					targetTOD = TimeClock.TimeOfDay.DAWN;
					break;
				case DAWN:
					msgStr = L("The sun races across the sky!");
					targetTOD = TimeClock.TimeOfDay.DUSK;
					break;
				case DAY:
					msgStr = L("The sun hurries towards the horizon!");
					targetTOD = TimeClock.TimeOfDay.DUSK;
					break;
				default:
					mob.tell(L("You can't get there from here."));
					return false;
				}
				if(TO.getDawnToDusk()[targetTOD.ordinal()]==TO.getDawnToDusk()[targetTOD.ordinal()+1])
					targetTOD=TimeClock.TimeOfDay.values()[targetTOD.ordinal()+1];
				if(TO.getDawnToDusk()[targetTOD.ordinal()]==TO.getDawnToDusk()[0])
					targetTOD=TimeClock.TimeOfDay.values()[0];
				room.showHappens(CMMsg.MSG_OK_VISUAL,msgStr);
				for(int i=0;i<TO.getHoursInDay();i++)
				{
					if(TO.getTODCode() != targetTOD)
						TO.tickTock(1);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s), but the magic fades"));

		// return whether it worked
		return success;
	}
}
