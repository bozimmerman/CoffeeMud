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
public class Ranger extends StdCharClass
{
	public String ID(){return "Ranger";}
	public String name(){return "Ranger";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 22;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusAttackLevel(){return 2;}
	public int getMovementMultiplier(){return 12;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Ranger()
	{
		super();
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.INTELLIGENCE]=4;
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
			
			CMAble.addCharAbilityMapping(ID(),2,"Ranger_FindWater",false);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Ranger_TrackAnimal",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_TwoWeaponFighting",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_PredictWeather",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Chant_LocatePlants",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_TrueShot",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Moonbeam",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_SenseLife",false);
			CMAble.addCharAbilityMapping(ID(),8,"Ranger_Enemy1",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Chant_BestowName",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_LocateAnimals",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Farsight",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Ranger_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_CalmAnimal",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_Hunger",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Chant_ControlFire",false);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_PointBlank",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Ranger_Enemy2",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_AnimalFriendship",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Chant_SummonPeace",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_VenomWard",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Ranger_Lore",true);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_CriticalShot",false);
			CMAble.addCharAbilityMapping(ID(),15,"PlantLore",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_BreatheWater",false);
			
			CMAble.addCharAbilityMapping(ID(),16,"Chant_WindGust",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_HoldAnimal",true);
			
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Bury",false);
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_FarShot",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Ranger_Enemy3",true);
			CMAble.addCharAbilityMapping(ID(),18,"Fighter_Sweep",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_ColdWard",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Chant_CharmAnimal",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_LightningWard",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);	
			CMAble.addCharAbilityMapping(ID(),20,"Chant_WaterWalking",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Chant_GasWard",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_Sunray",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonAnimal",true);
			CMAble.addCharAbilityMapping(ID(),22,"Ranger_Hide",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SummonInsects",false);
			CMAble.addCharAbilityMapping(ID(),23,"Ranger_Enemy4",true);
			
			CMAble.addCharAbilityMapping(ID(),24,"Chant_AnimalSpy",false);
			CMAble.addCharAbilityMapping(ID(),24,"Skill_RegionalAwareness",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonMount",false);
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_CalledShot",true);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_NeutralizePoison",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Ranger_AnimalFrenzy",true);
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

		if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Troll-kin"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You need to be Human, Elf, or Half Elf to be a Ranger.");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
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

	protected boolean isValidBeneficiary(MOB killer, 
									   MOB killed, 
									   MOB mob,
									   HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!Sense.isAnimalIntelligence(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
	
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
