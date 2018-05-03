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
   Copyright 2003-2018 Bo Zimmerman

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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
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
				if(room.getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.NIGHT)
				{
					room.showHappens(CMMsg.MSG_OK_VISUAL,L("The moon begin(s) to descend!"));
					final int x=room.getArea().getTimeObj().getHoursInDay()-room.getArea().getTimeObj().getHourOfDay();
					room.getArea().getTimeObj().tickTock(x);
				}
				else
				{
					room.showHappens(CMMsg.MSG_OK_VISUAL,L("The sun hurries towards the horizon!"));
					final int x=room.getArea().getTimeObj().getDawnToDusk()[TimeClock.TimeOfDay.NIGHT.ordinal()]-room.getArea().getTimeObj().getHourOfDay();
					room.getArea().getTimeObj().tickTock(x);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s), but the magic fades"));

		// return whether it worked
		return success;
	}
}
