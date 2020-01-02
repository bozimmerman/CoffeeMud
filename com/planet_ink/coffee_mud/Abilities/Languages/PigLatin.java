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
   Copyright 2019-2020 Bo Zimmerman

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
public class PigLatin extends StdLanguage
{
	@Override
	public String ID()
	{
		return "PigLatin";
	}

	private final static String localizedName = CMLib.lang().L("Pig Latin");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected final static char[] vowels = new char[] {'a','e','u','i','o','A','E','U','I','O'};
	
	protected final static String[] baseComboSounds = new String[] {
		"bl","br","ch","cl","cr","dr","fl","fr",
		"gl","gr","pl","pr","sc","sh","sk","sl",
		"sm","sn","sp","st","sw","th","tr","tw",
		"wh","wr","sch","scr","shr","sph","spl",
		"spr","squ","str","thr" };
	protected final static String[] comboSounds;
	static
	{
		final List<String> builder = new ArrayList<String>();
		for(final String bs : baseComboSounds)
		{
			builder.add(bs);
			builder.add(bs.toUpperCase());
			builder.add(Character.toUpperCase(bs.charAt(0))+bs.substring(1));
		}
		Collections.sort(builder, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				if(o1.length()==o2.length())
					return 0;
				if(o1.length()>o2.length())
					return -1;
				return 1;
			}
	
		});
		comboSounds = builder.toArray(new String[0]);
	}
	
	
	@Override
	public String translate(final String language, final String word)
	{
		if(word.length()==0)
			return "";
		if(CMStrings.indexOf(vowels, word.charAt(0))>=0)
		{
			if(Character.isUpperCase(word.charAt(word.length()-1)))
				return word+"WAY";
			else
				return word+"way";
		}
		else
		{
			for(final String bs : comboSounds)
			{
				if(word.startsWith(bs))
					return word.substring(bs.length())+bs+CMStrings.sameCase("ay", bs.charAt(bs.length()-1));
			}
			return word.substring(1)+word.charAt(0)+CMStrings.sameCase("ay", word.charAt(0));
		}
	}
}
