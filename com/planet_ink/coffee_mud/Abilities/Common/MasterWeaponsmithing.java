package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

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

public class MasterWeaponsmithing extends Weaponsmithing
{
	public String ID() { return "MasterWeaponsmithing"; }
	public String name(){ return "Master Weaponsmithing";}
	private static final String[] triggerStrings = {"MWEAPONSMITH","MASTERWEAPONSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("MASTERWEAPONSMITHING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"masterweaponsmith.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("MasterWeaponsmithing","Recipes not found!");
			Resources.submitResource("MASTERWEAPONSMITHING RECIPES",V);
		}
		return V;
	}
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Weaponsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned weaponsmithing.");
			student.tell("You need to learn weaponsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

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
			commands.insertElementAt(new Integer(autoGenerate),0);
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}

}
