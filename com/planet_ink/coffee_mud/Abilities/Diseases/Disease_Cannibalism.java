package com.planet_ink.coffee_mud.Abilities.Diseases;

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
 Copyright 2004-2018 Bo Zimmerman

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

public class Disease_Cannibalism extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Cannibalism";
	}

	private final static String	localizedName	= CMLib.lang().L("Cannibalism");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Cannibalism)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 6;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 999999;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 100;
	}

	@Override
	protected String DISEASE_DONE()
	{
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB) affected;
			return L("<S-NAME> no longer hunger for @x1 meat.",mob.charStats().raceName());
		}
		else
		{
			return L("<S-NAME> no longer hunger for your race's meat.");
		}
	}

	@Override
	protected String DISEASE_START()
	{
		String desiredMeat = "";
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB) affected;
			desiredMeat = mob.charStats().raceName();
		}
		else
		{
			desiredMeat = "your race's";
		}
		return L("^G<S-NAME> hunger(s) for " + desiredMeat + " meat.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int spreadBitmap()
	{
		return DiseaseAffect.SPREAD_CONSUMPTION;
	}

	@Override
	public void unInvoke()
	{
		if (affected == null)
			return;
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB) affected;

			super.unInvoke();
			if (canBeUninvoked())
				mob.tell(mob, null, this, DISEASE_DONE());
		}
		else
			super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (affected instanceof MOB)
		{
			final MOB source = msg.source();
			if (source == null)
				return false;
			final MOB mob = (MOB) affected;
			if (msg.targetMinor() == CMMsg.TYP_EAT)
			{
				final Environmental food = msg.target();
				if ((food != null) && (food.name().toLowerCase().indexOf(mob.charStats().raceName()) < 0))
				{
					final CMMsg newMessage = CMClass.getMsg(mob, null, this, CMMsg.MSG_OK_VISUAL, L("^S<S-NAME> attempt(s) to eat @x1, but can't stomach it....^?", food.Name()));
					if (mob.location().okMessage(mob, newMessage))
						mob.location().send(mob, newMessage);
					return false;
				}
			}
		}
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB) affected;
			if (msg.amITarget(mob) && (msg.tool() != null) && (msg.tool().ID().equals("Spell_Hungerless")))
			{
				mob.tell(L("You don't feel any less hungry."));
				return false;
			}
		}

		return super.okMessage(myHost, msg);
	}
}
