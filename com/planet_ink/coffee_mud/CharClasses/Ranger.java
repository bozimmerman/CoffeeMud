package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Ranger extends StdCharClass
{
	public String ID(){return "Ranger";}
	public String name(){return "Ranger";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 22;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusManaLevel(){return 10;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Ranger()
	{
		super();
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.INTELLIGENCE]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Ranger_Track",true);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",0,"ANTIDOTES",false);
			CMAble.addCharAbilityMapping(ID(),2,"Ranger_FindWater",true);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
			CMAble.addCharAbilityMapping(ID(),3,"Ranger_TrackAnimal",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_TwoWeaponFighting",true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_PredictWeather",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_LocatePlants",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Fighter_RapidShot",true);
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_TrueShot",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Moonbeam",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_SenseLife",false);
			CMAble.addCharAbilityMapping(ID(),8,"Ranger_Enemy1",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_LocateAnimals",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Farsight",false);
			CMAble.addCharAbilityMapping(ID(),10,"Ranger_Sneak",true);
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_CalmAnimal",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_Hunger",false);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_ControlFire",false);
			CMAble.addCharAbilityMapping(ID(),13,"Ranger_Enemy2",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_AnimalFriendship",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_SummonPeace",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_VenomWard",false);
			CMAble.addCharAbilityMapping(ID(),15,"Ranger_Lore",true);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_CriticalShot",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_BreatheWater",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_WindGust",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_HoldAnimal",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_FireWard",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Bury",false);
			CMAble.addCharAbilityMapping(ID(),18,"Ranger_Enemy3",true);
			CMAble.addCharAbilityMapping(ID(),18,"Fighter_Sweep",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_ColdWard",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_CharmAnimal",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_LightningWard",false);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);	
			CMAble.addCharAbilityMapping(ID(),20,"Chant_WaterWalking",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_GasWard",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_Sunray",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonAnimal",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SummonInsects",false);
			CMAble.addCharAbilityMapping(ID(),23,"Ranger_Enemy4",true);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_AnimalSpy",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonMount",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_NeutralizePoison",false);
			CMAble.addCharAbilityMapping(ID(),30,"Ranger_AnimalFrenzy",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Strength 9+, Intelligence 9+";}
	public String otherBonuses(){return "When leading animals into battle, will not divide experience among animal followers.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Ranger.");
			return false;
		}

		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Ranger.");
			return false;
		}

		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You need to be Human, Elf, or Half Elf to be a Ranger.");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}

	private boolean isValidBeneficiary(MOB killer, 
									   MOB killed, 
									   MOB mob,
									   Hashtable followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(mob.charStats().getStat(CharStats.INTELLIGENCE)>1))
		&&((mob.getVictim()==killed)
		 ||(followers.get(mob)!=null)
		 ||(mob==killer)))
			return true;
		return false;
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
