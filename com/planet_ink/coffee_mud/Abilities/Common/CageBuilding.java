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

public class CageBuilding extends Wainwrighting
{
	public String ID() { return "CageBuilding"; }
	public String name(){ return "Cage Building";}
	private static final String[] triggerStrings = {"BUILDCAGE","CAGEBUILDING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected String supportedResourceString(){return "WOODEN";}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("CAGEBUILD RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"cagebuilding.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("CageBuilding","Recipes not found!");
			Resources.submitResource("CAGEBUILD RECIPES",V);
		}
		return V;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Build what? Enter \"buildcage list\" for a list.");
			return false;
		}
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
