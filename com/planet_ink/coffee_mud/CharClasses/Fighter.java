package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Fighter extends StdCharClass
{
	public String ID(){return "Fighter";}
	public String name(){return "Fighter";}
	public String baseClass(){return ID();}
	private static boolean abilitiesLoaded=false;
	public int getMaxHitPointsLevel(){return 24;}
	public int getBonusPracLevel(){return -1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public Fighter()
	{
		super();
		maxStat[CharStats.STRENGTH]=25;
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
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Cleave",true);
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),7,"Fighter_RapidShot",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Attack2",true); 
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_CritStrike",true);
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_BlindFighting",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_MountedCombat",true);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_WeaponBreak",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_DuelParry",true);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Sweep",true);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_Whomp",true);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Attack3",true);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Endurance",true);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Tumble",true);
			CMAble.addCharAbilityMapping(ID(),22,"Fighter_Berzerk",true);
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
}
