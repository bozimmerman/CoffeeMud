package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
public class Archon extends StdCharClass
{
	public String ID(){return "Archon";}
	public String name(){return "Archon";}
	public String baseClass(){return ID();}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	public Archon()
	{
		super();
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			maxStatAdj[i]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"AnimalTaming",false);
			CMAble.addCharAbilityMapping(ID(),1,"AnimalTrading",false);
			CMAble.addCharAbilityMapping(ID(),1,"AnimalTraining",false);
			CMAble.addCharAbilityMapping(ID(),1,"Domesticating",false);
			CMAble.addCharAbilityMapping(ID(),1,"InstrumentMaking",false);
			CMAble.addCharAbilityMapping(ID(),20,"PlantLore",false);
			CMAble.addCharAbilityMapping(ID(),10,"Scrapping",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Resistance",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Archon_Multiwatch",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Archon_Wrath",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Archon_Hush",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Archon_Banish",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Archon_Metacraft",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Amputation",100,"",true,true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_AlterTime",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_MoveSky",true);
			
			// temporarily here until we find a place for them
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Enslave",false);
			CMAble.addCharAbilityMapping(ID(),1,"SlaveTrading",false);
		}
	}

	public int availabilityCode(){return 0;}

	public String statQualifications(){return "Must be granted by another Archon.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell("This class cannot be learned.");
		return false;
	}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("ArchonStaff");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)

	{
		// archons ALWAYS use borrowed abilities
		super.startCharacter(mob, true, verifyOnly);
		if(verifyOnly)
			grantAbilities(mob,true);
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		// the most efficient way of doing this -- just hash em!
		Hashtable alreadyAble=new Hashtable();
		Hashtable alreadyAff=new Hashtable();
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if(A!=null) alreadyAff.put(A.ID(),A);
		}
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)
			{
				A.setProfficiency(100);
				A.setBorrowed(mob,true);
				Ability A2=(Ability)alreadyAff.get(A.ID());
				if(A2!=null)
					A2.setProfficiency(100);
				else
					A.autoInvocation(mob);
				alreadyAble.put(A.ID(),A);
			}
		}
		int classLevel=mob.charStats().getClassLevel(this);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			int lvl=CMAble.getQualifyingLevel(ID(),true,A.ID());
			if((lvl>0)
			&&(lvl<=classLevel)
			&&(!alreadyAble.containsKey(A.ID())))
				giveMobAbility(mob,A,100,"",true,false);
		}
		alreadyAble.clear();
		alreadyAff.clear();
	}
}
