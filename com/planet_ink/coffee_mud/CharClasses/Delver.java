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
public class Delver extends StdCharClass
{
	public String ID(){return "Delver";}
	public String name(){return "Delver";}
	public String baseClass(){return "Druid";}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 6;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_OREONLY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ROCKY;}
	private HashSet requiredWeaponMaterials=buildRequiredWeaponMaterials();
	protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public Delver()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=4;
		maxStatAdj[CharStats.STRENGTH]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",50,false);
			CMAble.addCharAbilityMapping(ID(),1,"Fishing",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonFungus",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonPool",50,true);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_Tether",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_SummonWater",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Chant_CaveFishing",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_Darkvision",false);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_Boulderbash",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_SenseMetal",false);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_StrikeBarren",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_DeepDarkness",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Mold",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_MagneticField",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Chant_EndureRust",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Brittle",false);

			CMAble.addCharAbilityMapping(ID(),8,"Chant_FodderSignal",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Den",false);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_Rockfeet",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Earthpocket",true);

			CMAble.addCharAbilityMapping(ID(),10,"Chant_CrystalGrowth",false);
			CMAble.addCharAbilityMapping(ID(),10,"Druid_GolemForm",false);

			CMAble.addCharAbilityMapping(ID(),11,"Chant_CaveIn",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantPass",true);

			CMAble.addCharAbilityMapping(ID(),12,"Chant_Rockthought",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_SnatchLight",false);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_Drifting",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_DistantFungalGrowth",false);

			CMAble.addCharAbilityMapping(ID(),14,"Chant_Stonewalking",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Bury",false);

			CMAble.addCharAbilityMapping(ID(),15,"Chant_FungalBloom",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SacredEarth",true);

			CMAble.addCharAbilityMapping(ID(),16,"Chant_BrownMold",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_SenseOres",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_MagneticEarth",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Earthquake",true);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_Labyrinth",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_FungusFeet",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Chant_RustCurse",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_TremorSense",true);

			CMAble.addCharAbilityMapping(ID(),20,"Chant_StoneFriend",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);

			CMAble.addCharAbilityMapping(ID(),21,"Chant_Worms",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SenseGems",true);

			CMAble.addCharAbilityMapping(ID(),22,"Chant_Unbreakable",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_Homeopathy",false);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_FindOre",true);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_MassFungalGrowth",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Chant_FindGem",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_VolcanicChasm",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonRockGolem",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_MetalMold",true);
			
			CMAble.addCharAbilityMapping(ID(),30,"Chant_ExplosiveDecompression",false);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(Dice.rollPercentage()<50))
		{
			if(((Ability)msg.tool()).appropriateToMyAlignment(myChar.getAlignment()))
				return true;
			myChar.tell("Extreme emotions disrupt your chant.");
			return false;
		}
		return true;
	}


	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().endsWith("Elemental")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
	public String statQualifications(){return "Constitution 9+, Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Delver.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Delver.");
			return false;
		}
		if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Gnome"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Troll-kin"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Halfling"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Halfling, Dwarf, or Half Elf to be a Delver");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "";}

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
		&&(!skill.ID().equals("Sculpting"))
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;
		   
		return duration;
	}
}
