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
   Copyright 2002-2018 Bo Zimmerman

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

public class Dwarven extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Dwarven";
	}

	private final static String localizedName = CMLib.lang().L("Dwarven");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]> wordLists=null;
	public Dwarven()
	{
		super();
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			final String[] one={"o"};
			final String[] two={"`ai","`oi","`ul"};
			final String[] three={"aya","dum","mim","oyo","tum"};
			final String[] four={"menu","bund","ibun","khim","nala","rukhs","dumu","zirik","gunud","gabil","gamil"};
			final String[] five={"kibil","celeb","mahal","narag","zaram","sigin","tarag","uzbad","zigil","zirak","aglab","baraz","baruk","bizar","felak"};
			final String[] six={"azanul","bundushathur","morthond","felagund","gabilan","ganthol","khazad","kheled","khuzud","mazarbul","khuzdul"};
			wordLists=new Vector<String[]>();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
		}
		return wordLists;
	}
}
