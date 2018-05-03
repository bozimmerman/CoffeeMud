package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Gaian";
	}

	private final static String localizedStaticName = CMLib.lang().L("Gaian");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Druid";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 2;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_CONSTITUTION;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 30;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?7))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/4)+(1*(1?4))";
	}

	@Override
	protected String armorFailMessage()
	{
		return L("<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_NONMETAL;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_NATURAL;
	}

	private final Set<Integer> requiredWeaponMaterials = buildRequiredWeaponMaterials();

	@Override
	protected Set<Integer> requiredWeaponMaterials()
	{
		return requiredWeaponMaterials;
	}

	@Override
	public int requiredArmorSourceMinor()
	{
		return CMMsg.TYP_CAST_SPELL;
	}

	public Gaian()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",100,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Foraging",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonFlower",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonHerb",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_LocatePlants",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SummonFood",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SummonIvy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonVine",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_FreeVine",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_FortifyFood",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Barkskin",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_SenseSentience",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Ranger_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Druid_KnowPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_Goodberry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_PlantSelf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_GrowClub",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_Root",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_PlantPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_KillerVine",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"PlantLore",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Druid_PlantForm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Herbalism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_SummonTree",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Farming",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_VineWeave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_PlantBed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_SummonSeed",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_Shillelagh",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_PlantWall",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_DistantGrowth",false,CMParms.parseSemicolons("Chant_SummonPlants(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_SummonFlyTrap",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_SummonSeaweed",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Bury",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_PlantMaze",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Thorns",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_PoisonousVine",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_ControlPlant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SummonHouseplant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SensePlants",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_GrowItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_Blight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_PlantSnare",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_PlantConstriction",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_FindPlant",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_VampireVine",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Chlorophyll",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_DistantOvergrowth",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_PlantChoke",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_Grapevine",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SaplingWorkers",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_Treehouse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_VineMass",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_GrowForest",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_TapGrapevine",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_GrowFood",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_DistantIngrowth",false,CMParms.parseSemicolons("Chant_SummonHouseplant(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_PlantTrap",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SummonSapling",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SweetScent",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Shamblermorph",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_GrowOak",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public boolean isValidClassDivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		if((mob!=null)
		&&(mob!=killed)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!CMLib.flags().isVegetable(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Elf","Vegetation","Humanoid","Centaur",
		"LizardMan","Aarakocran","Merfolk","Faerie","-Drow"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9)),
		new Pair<String,Integer>("Constitution",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Must remain Neutral to avoid skill and chant failure chances.");
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Attains Greenskin (sunlight based bonuses/penalties) at level 5.  At level 30, becomes totally undetectable in wilderness settings while hidden.  "
				+ "Can create a druidic connection with an area.  Benefits from animal/plant/stone followers leveling.  Benefits from freeing animals from cities.");
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host, msg);
		Druid.doAnimalFollowerLevelingCheck(this, host, msg);
		Druid.doAnimalFreeingCheck(this, host, msg);
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		final Room room=affected.location();
		if(room!=null)
		{
			if(affected.charStats().getClassLevel(this)>=5)
			{
				if(CMLib.flags().isInDark(room))
				{
					affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/4));
					affectableState.setMovement(affectableState.getMovement()-(affectableState.getMovement()/4));
				}
				else
				if(room.getArea().getClimateObj().canSeeTheSun(room))
				{
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

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(CMLib.dice().rollPercentage()<50))
		{
			if(((Ability)msg.tool()).appropriateToMyFactions(myChar))
				return true;
			myChar.tell(L("Extreme emotions disrupt your chant."));
			return false;
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			final MOB mob=(MOB)affected;
			final Room room=mob.location();
			final int classLevel=mob.charStats().getClassLevel(this);
			if((CMLib.flags().isHidden(mob))
			&&(classLevel>=30)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_CITY))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);

			if(classLevel>=5)
			{
				if(CMLib.flags().isInDark(room))
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((classLevel/5)+1));
				else
				if(room.getArea().getClimateObj().canSeeTheSun(room))
				{
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
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Quarterstaff");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(final AbilityMapper.AbilityMapping able : V)
			{
				final Ability A=CMClass.getAbility(able.abilityID());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	@Override
	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null)
			return duration;
		if((((skill.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
			||((skill.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL))
		&&(myChar.charStats().getCurrentClass().ID().equals(ID()))
		&&(!skill.ID().equals("FoodPrep"))
		&&(!skill.ID().equals("Cooking"))
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Weaving"))
		&&(!skill.ID().equals("Landscaping"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;

		return duration;
	}
}
