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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.MoonPhase;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TidePhase;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Mer extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Mer";
	}

	private final static String localizedStaticName = CMLib.lang().L("Mer");

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
		return "((@x6<@x7)/2)+(2*(1?5))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/4)+(1*(1?6))";
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
		return CharClass.WEAPONS_MERLIKE;
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

	public Mer()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_SeaLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_BreatheWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonSeaweed",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_Phosphorescence",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_AquaticPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictTides",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_SenseWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_Darkvision",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_BestowName",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"AnimalBonding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_FilterWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SummonChum",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SnuffFlame",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_EelShock",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_FindDriftwood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_LocateAnimals",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_SpeakWithAnimals",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_WaterWalking",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_CalmAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_SummonCoral",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_FeelElectricity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_BloodyWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Druid_KnowPlants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_NaturalBalance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Druid_WaterCover",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_UnderwaterAction",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_WaterHammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_AnimalCompanion",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_AnimalFriendship",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CalmWind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_SummonSchool",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CallCompanion",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_Drown",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_Waterguard",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_ReefWalking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_CallMate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_VenomWard",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_CharmAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_HighTide",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_WhisperWard",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_Capsize",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_BreatheAir",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_FeedingFrenzy",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Observation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_MuddyGrounds",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_CalmSeas",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_WarningWinds",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SummonJellyfish",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Flippers",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Druid_ShapeShift2",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Flood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Hippieness",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SiftWrecks",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_RustCurse",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_FavorableWinds",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SenseFluids",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_TideMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_AnimalSpy",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_AnimalGrowth",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_NeutralizePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_AttackHalf",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_TidalWave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SenseGems",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_Waterspout",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Whirlpool",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Crossbreed",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_Tsunami",true);
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
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Druid.doAnimalFreeingCheck(this,host,msg);
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Giant-kin","Goblin","Drow","HalfElf","Ogre",
		"Gnoll","LizardMan","Merfolk","Faerie"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
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
		return L("May breathe and cast spells underwater. Attains Tidal Changes (tidal phase based bonuses/penalties) at level 5.");
	}
	
	private final static int[] breathableStuff = new int[]{
		RawMaterial.RESOURCE_SALTWATER,
		RawMaterial.RESOURCE_FRESHWATER
	};
	
	private final Map<int[],int[]> oldSets = new Hashtable<int[],int[]>(); 
	
	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		// this is necessary because the race happens AFTER the breathe is modified,
		// so we're actually adding water breathing to baseStat race (human, whatever)
		affected.eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public void apply(Ability a)
			{
				if(a.ID().startsWith("Druid_ShapeShift"))
					a.affectCharStats(affected, affectableStats);
			}
		});
		
		if(!CMParms.contains(affectableStats.getBreathables(),breathableStuff))
		{
			final int[] newSet = oldSets.get(affectableStats.getBreathables());
			if(newSet != null) {
				affectableStats.setBreathables(newSet);
			}
			else
			{
				int[] newerSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+breathableStuff.length);
				for(int i=breathableStuff.length-1;i>=0;i--)
					newerSet[newerSet.length-(i+1)]=breathableStuff[i];
				if(oldSets.size()>100)
					oldSets.clear();
				oldSets.put(affectableStats.getBreathables(), newerSet);
				Arrays.sort(newerSet);
				affectableStats.setBreathables(newerSet);
			}
		}
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			final Room room=affected.location();
			if(affected.charStats().getClassLevel(this)>=5)
			{
				final TidePhase phase = room.getArea().getTimeObj().getTidePhase(room);
				affectableState.setMovement(affectableState.getMovement()
											+(int)Math.round(CMath.mul(CMath.div(affectableState.getMovement(),8.0),phase.getFactor())));
				affectableState.setHitPoints(affectableState.getHitPoints()
											+(int)Math.round(CMath.mul(CMath.div(affectableState.getHitPoints(),8.0),phase.getFactor())));
				affectableState.setMana(affectableState.getMana()
											-(int)Math.round(CMath.mul(CMath.div(affectableState.getMana(),4.0),phase.getFactor())));
			}
		}
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
			if(classLevel>=5)
			{
				final TidePhase phase = room.getArea().getTimeObj().getTidePhase(room);
				affectableStats.setArmor(affectableStats.armor() // - is good
										 -(int)Math.round(CMath.mul(classLevel,phase.getFactor())));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() // - is bad
										 -(int)Math.round(CMath.mul(classLevel,phase.getFactor())));
			}
		}
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
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Trident");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
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
		&&(!skill.ID().equals("Masonry"))
		&&(!skill.ID().equals("Landscaping")))
			return duration*2;

		return duration;
	}
}
