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
public class Thief_Tasseography extends Thief_Runecasting
{
	@Override
	public String ID()
	{
		return "Thief_Tasseography";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Tasseography)");
		else
			return "";
	}

	private final static String localizedName = CMLib.lang().L("Tasseography");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"TASSEOGRAPHY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected String getStartPhrase()
	{
		switch(CMLib.dice().roll(1, 5, -1))
		{
		case 0: return L("I see your future is bright...");
		case 1: return L("I see your future is murky...");
		case 2: return L("I see your future is muddled...");
		case 3: return L("I see your future is sloppy...");
		case 4: return L("I see your future is orderly...");
		}
		return "";
	}

	@Override
	protected String getFailPhrase()
	{
		switch(CMLib.dice().roll(1, 4, -1))
		{
		case 0: return L("You drank too fast.  Try again later.");
		case 1: return L("The grounds are unclear.");
		case 2: return L("You need to drink more coffee.  Come back later.");
		case 3: return L("I see... I see.... I don't see.  Sorry, your life might be too uneventful.");
		}
		return "";
	}

	@Override
	protected String getSuccessMsg()
	{
		return L("<S-NAME> stud(ys) coffee grounds for <T-NAMESELF>...");
	}

	@Override
	protected String getFailureMsg()
	{
		return L("<S-NAME> stud(ys) coffee grounds for <T-NAMESELF>, but just make(s) a mess.");
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
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
						case ALIGNMENT:
						case FACTION:
						case TATTOO:
						case _ALIGNMENT:
						case _FACTION:
						case _TATTOO:
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
