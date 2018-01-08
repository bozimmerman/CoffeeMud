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
   Copyright 2005-2018 Bo Zimmerman

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

public class Druidic extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Druidic";
	}

	private final static String localizedName = CMLib.lang().L("Druidic");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static List<String[]> wordLists=null;
	public Druidic()
	{
		super();
	}

	@Override
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			final String[] one={""};
			final String[] two={"hissssss","hoo","caw","arf","bow-wow","bzzzzzz","grunt","bawl"};
			final String[] three={"chirp","tweet","mooooo","oink","quack","tweet","bellooooow","cackle","hooooowwwwl","!dook!"};
			final String[] four={"ruff","meow","grrrrowl","roar","cluck","honk","gibber","hoot","snort","groooan","trill","snarl"};
			final String[] five={"croak","bark","blub-blub","cuckoo","squeak","peep","screeech!","twitter","cherp","wail"};
			final String[] six={"hummmmmm","bleat","*whistle*","yelp","neigh","whinny","growl","screeaam!!"};
			final String[] seven={"gobble-gobble","ribbit","b-a-a-a-h","n-a-a-a-y","heehaw","cock-a-doodle-doo"};
			wordLists=new Vector<String[]>();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
			wordLists.add(seven);
		}
		return wordLists;
	}
}
