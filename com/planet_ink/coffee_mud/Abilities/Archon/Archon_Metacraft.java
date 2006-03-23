package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2000-2006 Bo Zimmerman

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

public class Archon_Metacraft extends ArchonSkill
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
		    Vector V=new Vector();
			for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=(Ability)e.nextElement();
				if(A instanceof ItemCraftor)
					V.addElement(A.copyOf());
			}
			while(V.size()>0)
			{
				int lowest=Integer.MAX_VALUE;
				Ability lowestA=null;
				for(int i=0;i<V.size();i++)
				{
				    Ability A=(Ability)V.elementAt(i);
				    int ii=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				    if(ii<lowest)
				    { 
				        lowest=ii; 
				        lowestA=A;
				    }
				}
				if(lowestA==null) 
				    lowestA=(Ability)V.firstElement();
				if(lowestA!=null)
				{
				    V.removeElement(lowestA);
				    craftingSkills.addElement(lowestA);
				}
				else
				    break;
			}
		}
		if(commands.size()<1)
		{
			mob.tell("Metacraft what, and (optionally) out of what material?");
			return false;
		}
		String mat=null;
		if(commands.size()>1)
		{
			mat=((String)commands.lastElement()).toUpperCase();
			commands.removeElementAt(commands.size()-1);
		}
		int material=-1;
		if(mat!=null)
		for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
			if(RawMaterial.RESOURCE_DESCS[i].startsWith(mat))
			{ material=RawMaterial.RESOURCE_DATA[i][0]; break;}
		if((mat!=null)&&(material<0))
		{
			mob.tell("'"+mat+"' is not a recognized material.");
			return false;
		}
		ItemCraftor skill=null;
		String recipe=CMParms.combine(commands,0);
		Vector skillsToUse=new Vector();
		boolean everyFlag=false;
		if(recipe.equalsIgnoreCase("everything"))
		{
			skillsToUse=craftingSkills;
			everyFlag=true;
			recipe=null;
		}
		else
	    if(recipe.toUpperCase().startsWith("EVERY "))
	    {
			everyFlag=true;
	    	recipe=recipe.substring(6).trim();
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.elementAt(i);
				Vector V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0)) skillsToUse.addElement(skill);
			}
			if(skillsToUse.size()==0)
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.elementAt(i);
				Vector V=skill.matchingRecipeNames(recipe,true);
				if((V!=null)&&(V.size()>0)) skillsToUse.addElement(skill);
			}
	    }
		else
		{
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.elementAt(i);
				Vector V=skill.matchingRecipeNames(recipe,false);
				if((V!=null)&&(V.size()>0)){ skillsToUse.addElement(skill); break;}
			}
			if(skillsToUse.size()==0)
			for(int i=0;i<craftingSkills.size();i++)
			{
				skill=(ItemCraftor)craftingSkills.elementAt(i);
				Vector V=skill.matchingRecipeNames(recipe,true);
				if((V!=null)&&(V.size()>0)){ skillsToUse.addElement(skill); break;}
			}
		}
		if(skillsToUse.size()==0)
		{
			mob.tell("'"+recipe+"' can not be made with any of the known crafting skills.");
			return false;
		}
		
		boolean success=false;
		for(int s=0;s<skillsToUse.size();s++)
		{
			skill=(ItemCraftor)skillsToUse.elementAt(s);
			Vector items=null;
			if(everyFlag)
			{
				items=new Vector();
				if(recipe==null)
				{
					Vector V=null;
					if(material>=0)
						V=skill.craftAllItemsVectors(material);
					else
					{
						Vector V2=new Vector();
						V=skill.craftAllItemsVectors();
						if(V!=null)
						{
							for(int v=0;v<V.size();v++)
								V2.addAll((Vector)V.elementAt(v));
							V=V2;
						}
					}
					if(V!=null)
					for(int v=0;v<V.size();v++)
						CMParms.addToVector((Vector)V.elementAt(v),items);
				}
				else
				if(material>=0)
					items=skill.craftItem(recipe,material);
				else
					items=skill.craftItem(recipe);
			}
			else
			if(material>=0)
				items=skill.craftItem(recipe,material);
			else
				items=skill.craftItem(recipe);
			if((items==null)||(items.size()==0)) continue;
			success=true;
			for(int v=0;v<items.size();v++)
			{
				Item building=(Item)items.elementAt(v);
				mob.giveItem(building);
				mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,building.name()+" appears in <S-YOUPOSS> hands.");
			}
			mob.location().recoverEnvStats();
		}
		if(!success)
		{
			mob.tell("The metacraft failed.");
			return false;
		}
		return true;
	}
}
