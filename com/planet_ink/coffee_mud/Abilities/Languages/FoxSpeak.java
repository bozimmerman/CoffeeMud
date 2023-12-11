package com.planet_ink.coffee_mud.Abilities.Languages;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class FoxSpeak extends StdLanguage
{
	@Override
	public String ID()
	{
		return "FoxSpeak";
	}

	private final static String	localizedName	= CMLib.lang().L("Fox Speak");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String getTranslationVerb()
	{
		return "yip(s)";
	}

	private static final String[] phrases = {
		"Ring-ding-ding-ding-dingeringeding",
		"Gering-ding-ding-ding-dingeringeding",
		"Gering-ding-ding-ding-dingeringeding",
		"Wa-pa-pa-pa-pa-pa-pow",
		"Wa-pa-pa-pa-pa-pa-pow",
		"Wa-pa-pa-pa-pa-pa-pow",
		"Hatee-hatee-hatee-ho",
		"Hatee-hatee-hatee-ho",
		"Hatee-hatee-hatee-ho",
		"Joff-tchoff-tchoffo-tchoffo-tchoff",
		"Tchoff-tchoff-tchoffo-tchoffo-tchoff",
		"Joff-tchoff-tchoffo-tchoffo-tchoff",
		"Jacha-chacha-chacha-chow",
		"Chacha-chacha-chacha-chow",
		"Chacha-chacha-chacha-chow",
		"Fraka-kaka-kaka-kaka-kow",
		"Fraka-kaka-kaka-kaka-kow",
		"Fraka-kaka-kaka-kaka-kow",
		"A-hee-ahee ha-hee",
		"A-hee-ahee ha-hee",
		"A-hee-ahee ha-hee",
		"A-oo-oo-oo-ooo",
		"Woo-oo-oo-ooo",
		"Wa-wa-way-do",
		"wub-wid-bid-dum-way-do",
		"wa-wa-way-do",
		"Bay-budabud-dum-bam",
		"Mama-dum-day-do",
		"Abay-ba-da bum-bum bay-do"
	};

	@Override
	public String scrambleAll(final String language, final String str, final int numToMess)
	{
		return phrases[CMLib.dice().roll(1, phrases.length, -1)];
	}

}
