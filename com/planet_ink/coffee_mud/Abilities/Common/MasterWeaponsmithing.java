package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2004 Tim Kassebaum

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
public class MasterWeaponsmithing extends Weaponsmithing implements ItemCraftor
{
	public String ID() { return "MasterWeaponsmithing"; }
	public String name(){ return "Master Weaponsmithing";}
	private static final String[] triggerStrings = {"MWEAPONSMITH","MASTERWEAPONSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}
    protected int displayColumns(){return 2;}

    public String parametersFile(){ return "masterweaponsmith.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"mweaponsmith list\" for a list, \"mweaponsmith scan\", or \"mweaponsmith mend <item>\".");
			return false;
		}
		if(autoGenerate>0)
			commands.insertElementAt(Integer.valueOf(autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}

}
