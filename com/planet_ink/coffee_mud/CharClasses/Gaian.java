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
public class Gaian extends StdCharClass
{
	public String ID(){return "Gaian";}
	public String name(){return "Gaian";}
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

	public Gaian()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=4;
		maxStatAdj[CharStats.WISDOM]=4;
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
			CMAble.addCharAbilityMapping(ID(),1,"Herbology",0,false);
			CMAble.addCharAbilityMapping(ID(),1,"Foraging",50,true);

			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonFlower",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonHerb",false);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_LocatePlants",true);

			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonFood",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonIvy",false);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonVine",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_FreeVine",true);

			CMAble.addCharAbilityMapping(ID(),5,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_FortifyFood",false);

			CMAble.addCharAbilityMapping(ID(),6,"Chant_Barkskin",true);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_SenseSentience",false);

			CMAble.addCharAbilityMapping(ID(),7,"Ranger_Hide",false);
			CMAble.addCharAbilityMapping(ID(),7,"Druid_KnowPlants",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Goodberry",false);

			CMAble.addCharAbilityMapping(ID(),8,"Chant_GrowClub",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Root",false);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_PlantPass",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_KillerVine",false);
			CMAble.addCharAbilityMapping(ID(),9,"PlantLore",true);

			CMAble.addCharAbilityMapping(ID(),10,"Druid_PlantForm",true);
			CMAble.addCharAbilityMapping(ID(),10,"Herbalism",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_SummonTree",false);

			CMAble.addCharAbilityMapping(ID(),11,"Farming",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_VineWeave",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantBed",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_SummonSeed",false);

			CMAble.addCharAbilityMapping(ID(),12,"Chant_Shillelagh",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_PlantWall",true);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_DistantGrowth",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_SummonFlyTrap",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_SummonSeaweed",true);

			CMAble.addCharAbilityMapping(ID(),14,"Thief_Observation",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_PlantMaze",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Thorns",false);

			CMAble.addCharAbilityMapping(ID(),15,"Chant_PoisonousVine",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_ControlPlant",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SummonHouseplant",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SensePlants",true);

			CMAble.addCharAbilityMapping(ID(),16,"Chant_GrowItem",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_Blight",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_PlantSnare",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_PlantConstriction",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_FindPlant",true);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_VampireVine",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_Chlorophyll",false);

			CMAble.addCharAbilityMapping(ID(),19,"Chant_DistantOvergrowth",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_PlantChoke",false);

			CMAble.addCharAbilityMapping(ID(),20,"Chant_Grapevine",true);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_SaplingWorkers",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);

			CMAble.addCharAbilityMapping(ID(),21,"Chant_Treehouse",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_VineMass",false);

			CMAble.addCharAbilityMapping(ID(),22,"Chant_GrowForest",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_TapGrapevine",true);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_GrowFood",false);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_DistantIngrowth",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_PlantTrap",false);

			CMAble.addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SummonSapling",true);

			CMAble.addCharAbilityMapping(ID(),25,"Chant_SweetScent",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_Shamblermorph",false);

			CMAble.addCharAbilityMapping(ID(),30,"Chant_GrowOak",true);
		}
	}

	public int areaSelectablility(){return Area.THEME_FANTASY;}

	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!Sense.isVegetable(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
	public String statQualifications(){return "Constitution 9+, Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Gaian.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Gaian.");
			return false;
		}
		if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Vegetation"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().racialCategory().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Dwarf, or Half Elf to be a Gaian");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "Attains Greenskin (sunlight based bonuses/penalties) at level 5.  At level 30, becomes totally undetectable in wilderness settings while hidden.";}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			Room room=affected.location();
			if(affected.charStats().getClassLevel(this)>=5)
			{
				if(Sense.isInDark(room))
				{
					affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/4));
					affectableState.setMovement(affectableState.getMovement()-(affectableState.getMovement()/4));
				}
				else
				if((room.domainType()&Room.INDOORS)==0)
					switch(room.getArea().getClimateObj().weatherType(room))
					{
					case Climate.WEATHER_BLIZZARD:
					case Climate.WEATHER_CLOUDY:
					case Climate.WEATHER_DUSTSTORM:
					case Climate.WEATHER_HAIL:
					case Climate.WEATHER_RAIN:
					case Climate.WEATHER_SLEET:
					case Climate.WEATHER_SNOW:
					case Climate.WEATHER_THUNDERSTORM:
						break;
					default:
						affectableState.setMana(affectableState.getMana()+(affectableState.getMana()/4));
						affectableState.setMovement(affectableState.getMovement()+(affectableState.getMovement()/4));
						break;
					}
			}
		}

	}
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

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			MOB mob=(MOB)affected;
			Room room=mob.location();
			int classLevel=mob.charStats().getClassLevel(this);
			if((Sense.isHidden(mob))
			&&(classLevel>=30)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_CITY))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);

			if(classLevel>=5)
			{
				if(Sense.isInDark(room))
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((classLevel/5)+1));
				else
				if((room.domainType()&Room.INDOORS)==0)
					switch(room.getArea().getClimateObj().weatherType(room))
					{
					case Climate.WEATHER_BLIZZARD:
					case Climate.WEATHER_CLOUDY:
					case Climate.WEATHER_DUSTSTORM:
					case Climate.WEATHER_HAIL:
					case Climate.WEATHER_RAIN:
					case Climate.WEATHER_SLEET:
					case Climate.WEATHER_SNOW:
					case Climate.WEATHER_THUNDERSTORM:
						break;
					default:
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((classLevel/5)+1));
						break;
					}
			}
		}
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
		&&(myChar.charStats().getCurrentClass()==this)
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Weaving"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;
		   
		return duration;
	}
}
