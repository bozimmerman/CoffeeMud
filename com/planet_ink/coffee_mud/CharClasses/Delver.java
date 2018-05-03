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
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Delver";
	}

	private final static String localizedStaticName = CMLib.lang().L("Delver");

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
		return "((@x6<@x7)/2)+(2*(1?6))";
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
		return CharClass.ARMOR_OREONLY;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_ROCKY;
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

	public Delver()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",50,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fishing",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonFungus",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonPool",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_Tether",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_SummonWater",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_CaveFishing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_Darkvision",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_Boulderbash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SenseMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_StrikeBarren",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_DeepDarkness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Mold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_MagneticField",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_EndureRust",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_Brittle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_DeepThoughts",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_FodderSignal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_Den",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_Rockfeet",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_Earthpocket",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_CrystalGrowth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Druid_GolemForm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_CaveIn",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_PlantPass",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_Rockthought",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_SnatchLight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_Drifting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_DistantFungalGrowth",false,CMParms.parseSemicolons("Chant_SummonFungus(75)",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Stonewalking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Bury",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_FungalBloom",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SacredEarth",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_BrownMold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SenseOres",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_MagneticEarth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Earthquake",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Labyrinth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_FungusFeet",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_RustCurse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_TremorSense",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_StoneFriend",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SenseFluids",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_Worms",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SenseGems",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_Unbreakable",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_Homeopathy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_FindOre",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_MagmaCannon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_MassFungalGrowth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_FindGem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_VolcanicChasm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SummonRockGolem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_MetalMold",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_ExplosiveDecompression",false);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
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
	public boolean isValidClassDivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		if((mob!=null)
		&&(mob!=killed)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().endsWith("Elemental")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Dwarf","Goblinoid","Giant-kin",
		"HalfElf","Gnoll","LizardMan","Merfolk","Drow"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
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
		return L("Can create a druidic connection with an area.  Benefits from freeing animals from cities.");
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Shortsword");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Druid.doAnimalFreeingCheck(this,host,msg);
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
		&&(!skill.ID().equals("FoodPrep"))
		&&(!skill.ID().equals("Cooking"))
		&&(!skill.ID().equals("Sculpting"))
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry"))
		&&(!skill.ID().equals("Excavation")))
			return duration*2;

		return duration;
	}
}
