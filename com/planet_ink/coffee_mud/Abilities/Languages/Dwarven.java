package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Dwarven extends StdLanguage
{
	public String ID() { return "Dwarven"; }
	public String name(){ return "Dwarven";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public Dwarven()
	{
		super();
		if(!mapped){mapped=true;
					CMLib.ableMapper().addCharAbilityMapping("All",1,ID(),false);}
	}

	public Vector translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"o"};
			String[] two={"`ai","`oi","`ul"};
			String[] three={"aya","dum","mim","oyo","tum"};
			String[] four={"menu","bund","ibun","khim","nala","rukhs","dumu","zirik","gunud","gabil","gamil"};
			String[] five={"kibil","celeb","mahal","narag","zaram","sigin","tarag","uzbad","zigil","zirak","aglab","baraz","baruk","bizar","felak"};
			String[] six={"azanul","bundushathur","morthond","felagund","gabilan","ganthol","khazad","kheled","khuzud","mazarbul","khuzdul"};
			wordLists=new Vector();
			wordLists.addElement(one);
			wordLists.addElement(two);
			wordLists.addElement(three);
			wordLists.addElement(four);
			wordLists.addElement(five);
			wordLists.addElement(six);
		}
		return wordLists;
	}
}
