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
public class Thief_Astrology extends Thief_Runecasting
{
	@Override
	public String ID()
	{
		return "Thief_Astrology";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Astrology)");
		else
			return "";
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	private final static String localizedName = CMLib.lang().L("Astrology");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"ASTROLOGY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected String getStartPhrase()
	{
		switch(CMLib.dice().roll(1, 3, -1))
		{
		case 0: return L("I see your stars are affected by...");
		case 1: return L("I see the alignments will affect...");
		case 2: return L("I see the calestial motions will cause...");
		}
		return "";
	}

	@Override
	protected String getFailPhrase()
	{
		switch(CMLib.dice().roll(1, 4, -1))
		{
		case 0: return L("Astral clouds are blocking your aura.");
		case 1: return L("Your future is unbound. Tread carefully.");
		case 2: return L("Your path is clear.");
		case 3: return L("The fates` gaze is elsewhere.");
		}
		return "";
	}

	@Override
	protected String getSuccessMsg()
	{
		return L("<S-NAME> consult(s) the stars for <T-NAMESELF>...");
	}

	@Override
	protected String getFailureMsg()
	{
		return L("<S-NAME> consult(s) the stars for <T-NAMESELF>, but <S-IS-ARE> confused.");
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
						case BIRTHDAY:
						case BIRTHDAYOFYEAR:
						case BIRTHMONTH:
						case BIRTHSEASON:
						case BIRTHWEEK:
						case BIRTHWEEKOFYEAR:
						case BIRTHYEAR:
						case _BIRTHDAY:
						case _BIRTHDAYOFYEAR:
						case _BIRTHMONTH:
						case _BIRTHSEASON:
						case _BIRTHWEEK:
						case _BIRTHWEEKOFYEAR:
						case _BIRTHYEAR:
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
