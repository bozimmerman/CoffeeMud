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

public class Gnomish extends Language
{
	public String ID() { return "Gnomish"; }
	public String name(){ return "Gnomish";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public Gnomish()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Vector translationVector()
	{
		if(wordLists==null)
		{
			String[] one={"y"};
			String[] two={"te","it","at","to"};
			String[] three={"nep","tem","tit","nip","pop","pon","upo","wip","pin"};
			String[] four={"peep","meep","neep","pein","nopo","popo","woop","weep","teep","teet"};
			String[] five={"whemp","thwam","nippo","punno","upoon","teepe","tunno","ponno","twano","ywhap"};
			String[] six={"tawhag","ponsol","paleep","ponpopol","niptittle","minwap","tinmipmip","niptemtem","wipwippoo"};
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