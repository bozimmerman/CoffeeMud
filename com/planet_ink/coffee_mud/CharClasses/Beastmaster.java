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

public class Beastmaster extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Beastmaster";
	}

	private final static String	localizedStaticName	= CMLib.lang().L("Beastmaster");

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
		return 15;
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

	private final HashSet<Integer>	requiredWeaponMaterials	= buildRequiredWeaponMaterials();

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

	public Beastmaster()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_DruidicPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_BestowName",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_HardenSkin",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalBonding",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SensePregnancy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_SensePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_LocateAnimals",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_Farsight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SenseAge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SpeakWithAnimals",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_CalmAnimal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_EelShock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_Hunger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_CheetahBurst",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Druid_ShapeShift2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_VenomWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Druid_Bite",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false,CMParms.parseSemicolons("Apothecary",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_AnimalFriendship",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_NaturalBalance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Trip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_FurCoat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_AnimalCompanion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_Camelback",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CallCompanion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Intimidate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_CharmAnimal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_EnhanceBody",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Druid_ShapeShift3",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_Fertility",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_CallMate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_BullStrength",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_Pin",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_BreatheWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_Yearning",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Bind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_SummonAnimal",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Observation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_CatsGrace",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"AnimalTraining",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_BlindFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_Hawkeye",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Druid_ShapeShift4",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_AnimalSpy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Plague",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_SpeedBirth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_GiveLife",false,CMParms.parseSemicolons("Chant_BestowName",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Hibernation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_AntTrain",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_Bloodhound",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SpeedAging",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SoaringEagle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_AnimalGrowth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Druid_ShapeShift5",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Berzerk",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_NeutralizePoison",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_UnicornsHealth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_FindMate",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Ambush",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SummonFear",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Druid_Rend",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Crossbreed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Dragonsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Druid_PackCall",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Dwarf","Giant-kin","Centaur","Gnoll","LizardMan","Aarakocran","Merfolk","Faerie"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9)),
		new Pair<String,Integer>("Constitution",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
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
		&&(msg.tool() instanceof Ability)
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
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
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Druid.doAnimalFollowerLevelingCheck(this,host,msg);
		Druid.doAnimalFreeingCheck(this,host,msg);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Must remain Neutral to avoid skill and chant failure chances.");
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("When leading animals into battle, will not divide experience among animal followers.  Can create a druidic connection with an area.  "
				+ "Benefits from animal/plant/stone followers leveling.  Benefits from freeing animals from cities.");
	}

	@Override
	public boolean isValidClassDivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		if((mob!=null)
		&&(mob!=killed)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!CMLib.flags().isAnimalIntelligence(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
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
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;

		return duration;
	}
}
