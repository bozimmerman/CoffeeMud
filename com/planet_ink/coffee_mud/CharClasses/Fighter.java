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
public class Fighter extends StdCharClass
{
	public String ID(){return "Fighter";}
	public String name(){return "Fighter";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 24;}
	public int getBonusPracLevel(){return -1;}
	public int getBonusManaLevel(){return 4;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int getMovementMultiplier(){return 12;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Fighter()
	{
		super();
		maxStatAdj[CharStats.STRENGTH]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Disarm",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Attack2",true); 
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_TrueShot",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_CritStrike",false);
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_ShieldBlock",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_BlindFighting",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_WeaponBreak",true);
			
			CMAble.addCharAbilityMapping(ID(),13,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_DualParry",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Trip",true);
			
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Sweep",false);
			
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_CriticalShot",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_Whomp",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Attack3",true);
			
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Endurance",false);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_PointBlank",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Tumble",true);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_AutoBash",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_SizeOpponent",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Fighter_Berzerk",false);
			CMAble.addCharAbilityMapping(ID(),22,"Fighter_ImprovedShieldDefence",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Fighter_CoverDefence",false);
			CMAble.addCharAbilityMapping(ID(),23,"Fighter_WeaponCatch",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_CalledStrike",true);
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_CounterAttack",true);
			
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_Heroism",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Fighter_CoupDeGrace",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Fighter.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.isMonster())
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.COMMON_SKILL)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
