package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Druidic extends Language
{
	public String ID() { return "Druidic"; }
	public String name(){ return "Druidic";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public Druidic()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Druid",1,ID(),true);
					CMAble.addCharAbilityMapping("Beastmaster",1,ID(),true);
					}
	}

	public Vector translationVector()
	{
		if(wordLists==null)
		{
			String[] one={""};
			String[] two={"hissssss","hoo","caw","arf","bow-wow","bzzzzzz"};
			String[] three={"chirp","tweet","mooooo","oink","quack","tweet"};
			String[] four={"ruff","meow","grrrrowl","roar","cluck","honk"};
			String[] five={"croak","bark","blub-blub","cuckoo","squeak","peep"};
			String[] six={"gobble-gobble","ribbit","b-a-a-a-h","n-a-a-a-y","heehaw","cock-a-doodle-doo"};
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
