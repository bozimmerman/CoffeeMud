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
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_FortuneTelling extends Thief_Runecasting
{
	@Override
	public String ID()
	{
		return "Thief_FortuneTelling";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Fortune Telling)");
		else
			return "";
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	private final static String localizedName = CMLib.lang().L("Fortune Telling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"FORTUNETELLING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected static String[] astroStarts = new String[]
	{
		"I see your future is affected by...",
		"You are surrounded by...",
		"In your future there are...",
		"Your future holds...",
		"Be ye warned..."
	};

	protected static String[] astroFails = new String[]
	{
		"Astral clouds are blocking your aura.",
		"Your future is unbound. Tread carefully.",
		"Your path is clear.",
		"The fates` gaze is elsewhere."
	};

	@Override
	protected String[] getStartPhrases()
	{
		return astroStarts;
	}

	@Override
	protected String[] getFailPhrases()
	{
		return astroFails;
	}

	@Override
	protected String getSuccessMsg()
	{
		return L("<S-NAME> peer(s) into a crystal ball to see the future of <T-NAMESELF>...");
	}

	@Override
	protected String getFailureMsg()
	{
		return L("<S-NAME> peer(s) into a crystal ball for <T-NAMESELF>, but <S-IS-ARE> confused.");
	}

	@Override
	protected Filterer<AutoProperties> getPlayerFilter()
	{
		return new Filterer<AutoProperties>()
		{
			@Override
			public boolean passesFilter(final AutoProperties obj)
			{
				return true;
			}
		};
	}

	protected long timeToNextCast = 0;

	@Override
	protected int getTicksBetweenCasts()
	{
		return (int)CMProps.getTicksPerMudHour() * CMLib.time().globalClock().getHoursInDay();
	}

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast=absoluteTime;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT+50;
	}
}
