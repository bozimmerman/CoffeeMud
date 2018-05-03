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
   Copyright 2002-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Druid extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Druid";
	}

	private final static String localizedStaticName = CMLib.lang().L("Druid");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return ID();
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

	public static Hashtable<Environmental, Object[]> animalChecking = new Hashtable<Environmental, Object[]>();

	public Druid()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=7;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Staff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",75,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_BestowName",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonPlants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_HardenSkin",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_SummonWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_LocatePlants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_SensePoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SummonFood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_Moonbeam",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_RestoreMana",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SenseLife",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_Tangle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_LocateAnimals",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_FortifyFood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_Farsight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_FeelElectricity",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_CalmAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Sunray",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Treeform",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_Goodberry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_Hunger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_FeelCold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_NaturalBalance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_WarpWood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_ControlFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_VenomWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CalmWind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_Barkskin",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_WaterWalking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CallCompanion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_AnimalFriendship",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_FeelHeat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_GrowClub",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_Brittle",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_PlantPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_WindGust",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_Poison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_Treemind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_WhisperWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_BreatheWater",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_HoldAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_PlantBed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_LightningWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_ColdWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Bury",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_IllusionaryForest",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_Hippieness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Herbalism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_Fertilization",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_CharmAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_CalmWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"PlantLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_FireWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_Shillelagh",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SummonPeace",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Plague",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_DistantGrowth",false,CMParms.parseSemicolons("Chant_SummonPlants(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_Earthquake",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_PlantMaze",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_GasWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Hibernation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_Reabsorb",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SummonAnimal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_Nectar",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SummonHeat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SenseSentience",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_Grapevine",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SummonCold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_SummonInsects",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_AnimalSpy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SummonRain",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_PlantSnare",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SensePregnancy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SenseFluids",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_Treemorph",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_SummonWind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_NeutralizePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_FindPlant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_SensePlants",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_GrowItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SummonLightning",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SummonMount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_FindOre",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_SenseOres",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SummonElemental",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SummonFear",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SenseAge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_FindGem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_SenseGems",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SpeedTime",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SummonSapling",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_Feralness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_Reincarnation",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Chant_PlaneWalking",false);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
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
			return;
		}

		final Vector<String> grantable=new Vector<String>();

		final int level=mob.charStats().getClassLevel(this);
		int numChants=2;
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==level)
			&&((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())<=25)
			&&(!CMLib.ableMapper().getSecretSkill(ID(),true,A.ID()))
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)))
			{
				if (!grantable.contains(A.ID()))
					grantable.addElement(A.ID());
			}
		}
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if(grantable.contains(A.ID()))
			{
				grantable.remove(A.ID());
				numChants--;
			}
		}
		for(int i=0;i<numChants;i++)
		{
			if(grantable.size()==0)
				break;
			final String AID=grantable.elementAt(CMLib.dice().roll(1,grantable.size(),-1));
			if(AID!=null)
			{
				grantable.removeElement(AID);
				giveMobAbility(mob,
							   CMClass.getAbility(AID),
							   CMLib.ableMapper().getDefaultProficiency(ID(),true,AID),
							   CMLib.ableMapper().getDefaultParm(ID(),true,AID),
							   isBorrowedClass);
			}
		}
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			for(int i=0;i<affected.location().numItems();i++)
			{
				final Item I=affected.location().getItem(i);
				if((I!=null)&&(I.ID().equals("DruidicMonument")))
					affectableState.setMana(affectableState.getMana()+(affectableState.getMana()/2));
			}
		}
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Vegetation","Dwarf","Giant-kin",
		"Goblinoid","HalfElf","Centaur","Gnoll","LizardMan","Aarakocran","Merfolk","Faerie"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
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
		return L("When leading animals into battle, will not divide experience among animal followers.  Can create a druidic connection with an area.  "
				+ "Benefits from animal/plant/stone followers leveling.  Benefits from freeing animals from cities.  Benefits from balancing the weather.");
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

	public static void doAnimalFollowerLevelingCheck(CharClass C, Environmental host, CMMsg msg)
	{
		if((msg.sourceMessage()==null)
		&&(msg.sourceMinor()==CMMsg.TYP_LEVEL)
		&&(msg.source().isMonster())
		&&(msg.source().basePhyStats().level() < msg.value()))
		{
			final MOB druidM=msg.source().amUltimatelyFollowing();
			if((druidM!=null)
			&&(!druidM.isMonster())
			&&(druidM.charStats().getCurrentClass().ID().equals(C.ID()))
			&&(CMLib.flags().isAnimalIntelligence(msg.source())
			  ||msg.source().charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation")
			  ||msg.source().charStats().getMyRace().racialCategory().equalsIgnoreCase("Stone Golem")))
			{
				final int xp=msg.source().phyStats().level()*5;
				if(xp>0)
				{
					druidM.tell(CMLib.lang().L("Your stewardship has benefitted @x1.",msg.source().name(druidM)));
					CMLib.leveler().postExperience(druidM,null,null,xp,false);
				}
			}
		}
	}

	public static void doAnimalFreeingCheck(CharClass C, Environmental host, CMMsg msg)
	{
		if((msg.source()!=host)
		&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW)
		&&(msg.source().isMonster())
		&&(host instanceof MOB)
		&&(!((MOB)host).isMonster())
		&&(msg.target()==host)
		&&(msg.source().getStartRoom()!=null)
		&&(CMLib.law().isACity(msg.source().getStartRoom().getArea()))
		&&(((MOB)host).charStats().getCurrentClass().ID().equals(C.ID()))
		&&(CMLib.flags().isAnimalIntelligence(msg.source())
		  ||msg.source().charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation")
		  ||msg.source().charStats().getMyRace().racialCategory().equalsIgnoreCase("Stone Golem"))
		&&(CMLib.flags().flaggedAffects(msg.source(),Ability.FLAG_SUMMONING).size()==0)
		&&(msg.source().location()!=null)
		&&(!msg.source().amDestroyed())
		&&(CMLib.flags().isInTheGame((MOB)host,true))
		&&(!CMLib.law().isACity(msg.source().location().getArea())))
		{
			Object[] stuff=animalChecking.get(host);
			final Room room=msg.source().location();
			if((stuff==null)||(System.currentTimeMillis()-((Long)stuff[0]).longValue()>(room.getArea().getTimeObj().getDaysInMonth()*room.getArea().getTimeObj().getHoursInDay()*CMProps.getMillisPerMudHour())))
			{
				stuff=new Object[3];
				stuff[0]=Long.valueOf(System.currentTimeMillis());
				animalChecking.remove(host);
				animalChecking.put(host,stuff);
				stuff[1]=Integer.valueOf(0);
				stuff[2]=new Vector<String>();
			}
			if((((Integer)stuff[1]).intValue()<19)&&(!((List)stuff[2]).contains(""+msg.source())))
			{
				stuff[1]=Integer.valueOf(((Integer)stuff[1]).intValue()+1);
				((MOB)host).tell(CMLib.lang().L("You have freed @x1 from @x2.",msg.source().name((MOB)host),(msg.source().getStartRoom().getArea().name())));
				CMLib.leveler().postExperience((MOB)host,null,null,((Integer)stuff[1]).intValue(),false);
			}
		}
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Druid.doAnimalFollowerLevelingCheck(this,host,msg);
		Druid.doAnimalFreeingCheck(this,host,msg);
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
		&&(!skill.ID().equals("Masonry")))
			return duration*2;

		return duration;
	}
}
