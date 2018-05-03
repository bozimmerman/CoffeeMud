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

public class SkyWatcher extends StdCharClass
{
	@Override
	public String ID()
	{
		return "SkyWatcher";
	}

	private final static String localizedStaticName = CMLib.lang().L("SkyWatcher");

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

	public SkyWatcher()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druidic",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_DruidicPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictWeather",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonHail",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictPhase",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_WindColor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_Moonbeam",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_ClearMoon",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SnuffFlame",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_PaleMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonDustdevil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_LoveMoon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonFire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Ranger_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_ColdMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_ControlFire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_CalmWind",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Sunray",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_HoneyMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_MuddyGrounds",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_LightningWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_StarGazing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_Dehydrate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_ColdWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_WindGust",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_PiercingMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_FireWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Ranger_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_WhisperWard",true);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_HeatMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_WarningWinds",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_HealingMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_AcidWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_WindShape",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_MoonCalf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_GroveWalk",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_BlueMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_RedMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_CalmWeather",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_SongWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_WakingMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Herbalism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SummonHeat",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_PeaceMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Druid_RecoverVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SoaringEagle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SummonMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_SummonCold",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_ChantWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Observation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_ControlWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_SummonRain",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SummonWind",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_PrayerWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_AcidRain",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Blindfighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_DistantWindColor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_ChargeMetal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_Shapelessness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SpellWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Meditation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_SummonLightning",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_ManicMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_WindSnatcher",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_AstralProjection",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_HowlersMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CloudWalk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_DeathMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SummonTornado",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_MeteorStrike",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_MoveSky",true);
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
		"Human","Humanoid","Elf","Giant-kin","Centaur","HalfElf",
		"LizardMan","Aarakocran","Faerie","Merfolk","-Drow"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9)),
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
		return L("Attains Lunar Changes (lunar phase based bonuses/penalties) at level 5.  Can create a druidic connection with an area.  "
				+ "Benefits from freeing animals from cities.  Benefits from balancing the weather.");
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
				final MoonPhase phase = room.getArea().getTimeObj().getMoonPhase(room);
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
				final MoonPhase phase = room.getArea().getTimeObj().getMoonPhase(room);
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
		&&(!skill.ID().equals("Masonry"))
		&&(!skill.ID().equals("Landscaping")))
			return duration*2;

		return duration;
	}
}
