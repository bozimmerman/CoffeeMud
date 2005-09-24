package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/*
   Copyright 2000-2005 Bo Zimmerman

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
public class Beastmaster extends StdCharClass
{
	public String ID(){return "Beastmaster";}
	public String name(){return "Beastmaster";}
	public String baseClass(){return "Druid";}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 6;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 7;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 8;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_NATURAL;}
	private HashSet requiredWeaponMaterials=buildRequiredWeaponMaterials();
	protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public Beastmaster()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=4;
		maxStatAdj[CharStats.DEXTERITY]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",100,false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);

			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_BestowName",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_HardenSkin",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SensePregnancy",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_SensePoison",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_LocateAnimals",true);

			CMAble.addCharAbilityMapping(ID(),3,"Chant_Farsight",false);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SenseAge",false);

			CMAble.addCharAbilityMapping(ID(),4,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_CalmAnimal",true);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_EelShock",false);

			CMAble.addCharAbilityMapping(ID(),5,"Chant_Hunger",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_Yearning",false);

			CMAble.addCharAbilityMapping(ID(),6,"Druid_ShapeShift2",true);
			CMAble.addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_VenomWard",false);

			CMAble.addCharAbilityMapping(ID(),7,"Druid_Bite",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_AnimalFriendship",true);

			CMAble.addCharAbilityMapping(ID(),8,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_FurCoat",false);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Camelback",false);

			CMAble.addCharAbilityMapping(ID(),10,"Fighter_Intimidate",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_CharmAnimal",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_EnhanceBody",false);

			CMAble.addCharAbilityMapping(ID(),11,"Druid_ShapeShift3",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_CheetahBurst",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_Fertility",false);

			CMAble.addCharAbilityMapping(ID(),12,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_Pin",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_BreatheWater",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_BullStrength",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_Bind",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_SummonAnimal",true);

			CMAble.addCharAbilityMapping(ID(),14,"Chant_Bury",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_CatsGrace",false);

			CMAble.addCharAbilityMapping(ID(),15,"AnimalTraining",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_Hawkeye",true);

			CMAble.addCharAbilityMapping(ID(),16,"Druid_ShapeShift4",true);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_AnimalSpy",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_Plague",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_SpeedBirth",false);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_Hibernation",true);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_AntTrain",false);

			CMAble.addCharAbilityMapping(ID(),19,"Chant_Bloodhound",true);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_SpeedAging",false);

			CMAble.addCharAbilityMapping(ID(),20,"Chant_SoaringEagle",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_AnimalGrowth",false);

			CMAble.addCharAbilityMapping(ID(),21,"Druid_ShapeShift5",true);
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Berzerk",false);

			CMAble.addCharAbilityMapping(ID(),22,"Skill_AttackHalf",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_NeutralizePoison",true);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_UnicornsHealth",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_FindMate",false);

			CMAble.addCharAbilityMapping(ID(),24,"Thief_Ambush",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SummonFear",true);

			CMAble.addCharAbilityMapping(ID(),25,"Druid_Rend",true);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_Crossbreed",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_Dragonsight",false);

			CMAble.addCharAbilityMapping(ID(),30,"Druid_PackCall",true);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}


	public String statQualifications(){return "Constitution 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Beastmaster.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Beastmaster.");
			return false;
		}
		if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Dwarf, or Half Elf to be a Beastmaster");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.tool() instanceof Ability)
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(Dice.rollPercentage()<50))
		{
			if(((Ability)msg.tool()).appropriateToMyFactions(myChar))
				return true;
			myChar.tell("Extreme emotions disrupt your chant.");
			return false;
		}
		return true;
	}

	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "When leading animals into battle, will not divide experience among animal followers.";}

    public boolean isValidClassBeneficiary(MOB killer, MOB killed, MOB mob, HashSet followers)
	{
		if((mob!=null)
        &&(mob!=killed)
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
			Weapon w=CMClass.getWeapon("Quarterstaff");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
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
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null) return duration;
		if(Util.bset(skill.flags(),Ability.FLAG_CRAFTING)
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;

		return duration;
	}
}
