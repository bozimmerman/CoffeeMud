package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

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

public class Distilling extends Cooking
{
	public String ID() { return "Distilling"; }
	public String name(){ return "Distilling";}
	private static final String[] triggerStrings = {"DISTILLING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "distill";};
	public String cookWord(){return "distilling";};
	public boolean honorHerbs(){return false;}
    protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_BREW;}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("DISTILLING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"liquors.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Distilling","Recipes not found!");
			Resources.submitResource("DISTILLING RECIPES",V);
		}
		return V;
	}
}
