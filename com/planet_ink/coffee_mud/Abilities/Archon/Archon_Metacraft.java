package com.planet_ink.coffee_mud.Abilities.Archon;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill;
import java.util.*;
import java.io.File;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Archon_Metacraft extends CraftingSkill
{
	public String ID() { return "Archon_Metacraft"; }
	public String name(){ return "Metacrafting";}
	private static final String[] triggerStrings = {"METACRAFT"};
	public String[] triggerStrings(){return triggerStrings;}
	
	public static Vector craftingSkills=new Vector();
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(craftingSkills.size()==0)
		{
			for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=(Ability)e.nextElement();
				if(((A.classificationCode()&Ability.ALL_CODES)==A.COMMON_SKILL)
				&&(Util.bset(A.flags(),Ability.FLAG_CRAFTING)))
					craftingSkills.addElement(A.copyOf());
			}
		}
		if(commands.size()<2)
		{
			mob.tell("Metacraft what, out of what material?");
			return false;
		}
		String mat=((String)commands.lastElement()).toUpperCase();
		commands.removeElementAt(commands.size()-1);
		int material=-1;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			if(EnvResource.RESOURCE_DESCS[i].startsWith(mat))
			{ material=EnvResource.RESOURCE_DATA[i][0]; break;}
		if(material<0)
		{
			mob.tell("'"+mat+"' is not a recognized material.");
			return false;
		}
		Ability skill=null;
		String recipe=Util.combine(commands,0);
		for(int i=0;i<craftingSkills.size();i++)
		{
			skill=(Ability)craftingSkills.elementAt(i);
			if(skill instanceof CraftingSkill)
			{
				Vector V=((CraftingSkill)skill).loadRecipes();
				V=matchingRecipeNames(V,recipe);
				if((V!=null)&&(V.size()>0))
					break;
			}
			skill=null;
		}
		if(skill==null)
		{
			mob.tell("'"+recipe+"' can not be made with any of the known crafting skills.");
			return false;
		}
		
		Item building=null;
		Item key=null;
		int tries=0;
		while(((building==null)||(building.name().endsWith(" bundle")))&&(((++tries)<1000)))
		{
			Vector V=new Vector();
			V.addElement(new Integer(material));
			skill.invoke(mob,V,skill,true,asLevel);
			if((V.size()>0)&&(V.lastElement() instanceof Item))
			{
				if((V.size()>1)&&((V.elementAt(V.size()-2) instanceof Item)))
					key=(Item)V.elementAt(V.size()-2);
				else
					key=null;
				building=(Item)V.lastElement();
			}
			else
				building=null;
		}
		if(building==null)
		{
			mob.tell("The metacraft failed.");
			return false;
		}
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		mob.giveItem(building);
		if(key!=null) mob.giveItem(key);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" appears in your hands.");
		mob.location().recoverEnvStats();
		return true;
	}
}
