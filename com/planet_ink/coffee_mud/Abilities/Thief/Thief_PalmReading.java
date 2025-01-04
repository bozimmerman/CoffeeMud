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
public class Thief_PalmReading extends Thief_Runecasting
{
	@Override
	public String ID()
	{
		return "Thief_PalmReading";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Palm Reading)");
		else
			return "";
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	private final static String localizedName = CMLib.lang().L("Palm Reading");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"PALMREADING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected static String[] astroStarts = new String[]
	{
		"I see your life line is affected by...",
		"I see your head line is affected by...",
		"I see your sun line is affected by...",
		"I see your fate line is affected by...",
		"I see your heart line is affected by..."
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
		return L("<S-NAME> examine(s) <T-YOUPOSS> palm...");
	}

	@Override
	protected String getFailureMsg()
	{
		return L("<S-NAME> examine(s) <T-YOUPOSS> palm, but <S-IS-ARE> confused.");
	}

	@Override
	protected Filterer<AutoProperties> getPlayerFilter()
	{
		return new Filterer<AutoProperties>()
		{
			@Override
			public boolean passesFilter(final AutoProperties obj)
			{
				for(final CompiledZMaskEntry[] entrySet : obj.getPlayerCMask().entries())
				{
					if(entrySet == null)
						continue;
					for(final CompiledZMaskEntry entry : entrySet)
					{
						switch(entry.maskType())
						{
						case CLASS:
						case ANYCLASS:
						case ANYCLASSLEVEL:
						case BASECLASS:
						case MAXCLASSLEVEL:
						case _CLASS:
						case _ANYCLASS:
						case _ANYCLASSLEVEL:
						case _BASECLASS:
						case _MAXCLASSLEVEL:
							return true;
						default:
							break;
						}
					}
				}
				return false;
			}
		};
	}

}
