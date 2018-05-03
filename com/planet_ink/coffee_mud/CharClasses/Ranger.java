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
   Copyright 2001-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Ranger";
	}

	private final static String	localizedStaticName	= CMLib.lang().L("Ranger");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Fighter";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 0;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public String getMovementFormula()
	{
		return "12*((@x2<@x3)/18)";
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 30;
	}

	@Override
	public int getPracsFirstLevel()
	{
		return 3;
	}

	@Override
	public int getTrainsFirstLevel()
	{
		return 4;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?6))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/7)+(1*(1?3))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_ANY;
	}

	public Ranger()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH] = 4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE] = 4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Armor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Shield",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Ranger_Track",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",0,"ANTIDOTES",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalBonding",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Ranger_FindWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Specialization_Bow",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Ranger_TrackAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_DelayPoison",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_TwoWeaponFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_ArmorTweaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_RepelVermin",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_ReadMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_PredictWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_LocatePlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Ranger_FierceCompanions",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false,CMParms.parseSemicolons("Apothecary",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_TrueShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_Moonbeam",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_SenseLife",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Ranger_Enemy1",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_BestowName",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_LocateAnimals",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_Farsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Ranger_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_CalmAnimal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_Hunger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Ranger_HuntersEndurance",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_ControlFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_PointBlank",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Ranger_SetSnare",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Ranger_Enemy2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_AnimalFriendship",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_SummonPeace",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_VenomWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Ranger_Hide",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Ranger_WoodlandLore",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_CriticalShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"PlantLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_BreatheWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_NaturalBalance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_WindGust",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_HoldAnimal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_AnimalCompanion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Bury",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_FarShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Ranger_Enemy3",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Sweep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_ColdWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_CharmAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_LightningWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_AirWall",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_WaterWalking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Ranger_SenseTraps",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_GasWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Ranger_Camouflage",false,CMParms.parseSemicolons("Ranger_Hide",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_SummonAnimal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_Sunray",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_ResuscitateCompanion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SummonInsects",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Ranger_Enemy4",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SpeakWithAnimals",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_AnimalSpy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_RegionalAwareness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Ranger_WoodlandCreep",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SummonMount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_CalledShot",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_NeutralizePoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Ranger_AnimalFrenzy",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Must remain Neutral to avoid chant failure chances.");
	}

	@Override 
	public String getOtherBonusDesc()
	{
		return L("When leading animals into battle, will not divide experience among animal followers.  Receives bonus conquest and duel experience.  "
				+ "Benefits from animal followers leveling.");
	}
	
	@Override 
	public void executeMsg(Environmental host, CMMsg msg)
	{ 
		super.executeMsg(host,msg); 
		Fighter.conquestExperience(this,host,msg);
		Fighter.duelExperience(this, host, msg);
		Druid.doAnimalFollowerLevelingCheck(this,host,msg);
	}
	
	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Giant-kin","Elf","Centaur","Gnoll",
		"Githyanki","LizardMan","Aarakocran","Merfolk",
		"Faerie","Orc"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9))
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
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
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
			final Weapon w=CMClass.getWeapon("Shortsword");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

}
