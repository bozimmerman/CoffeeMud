package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Fighter extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Fighter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=24;
		maxStat[CharStats.STRENGTH]=25;
		bonusPracLevel=-1;
		manaMultiplier=8;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=2;
		name=myID;
		practicesAtFirstLevel=3;
		trainsAtFirstLevel=4;
		damageBonusPerLevel=1;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
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
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Attack2",true); 
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_CritStrike",true);
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_BlindFighting",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_WeaponBreak",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_Whomp",true);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Attack3",true);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Endurance",true);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Berzerk",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength()>8)
			return true;
		return false;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(CMAble.getDefaultGain(ID(),A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),isBorrowedClass);
		}
		if(!mob.isMonster())
			outfit(mob);
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public void level(MOB mob)
	{
		super.level(mob);
	}
}
