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
public class Minstrel extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Minstrel";
	}

	private final static String localizedStaticName = CMLib.lang().L("Minstrel");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Bard";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_CHARISMA;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 10;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/3)+(2*(1?6))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/6)+(1*(1?2))";
	}

	@Override
	protected String armorFailMessage()
	{
		return L("<S-NAME> armor makes <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!");
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_NONMETAL;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_THIEFLIKE;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	public Minstrel()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Befriend",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"InstrumentMaking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Play_Tempo",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Play_Break",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Play_Woods",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_Dirt",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Play_Flutes",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Play_Rhythm",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Play_Drums",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Play_March",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Ranger_FindWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Play_Harps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Play_Background",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_TuneInstrument",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Play_Cymbals",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Play_Melody",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_TrophyCount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Play_Guitars",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Play_LoveSong",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Song_Armor",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Play_Clarinets",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Play_Carol",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Play_Violins",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Play_Blues",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Dodge",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Song_Serenity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Play_Oboes",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Play_Ballad",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_InstrumentBash",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Play_Horns",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Play_Retreat",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Play_Charge",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_Listen",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Play_Xylophones",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Play_Reveille",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Play_Symphony",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Parry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Play_Trumpets",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Play_Dirge",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Play_Ditty",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Play_Pianos",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Play_Solo",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Song_Quickness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Struggle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Play_Harmonicas",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Play_Lullabies",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Song_Thanks",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_Feint",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Play_Tubas",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Play_Accompaniment",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Play_Spiritual",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Paladin_Defend",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Play_Organs",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Play_Tribal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Play_Harmony",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Play_Trombones",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Play_Mystical",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Play_Battlehymn",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Skill_Conduct",true);
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
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Bard.visitationBonusMessage(host,msg);
	}

	@Override
	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(E instanceof MusicalInstrument)
			return true;
		return super.weaponCheck(mob,sourceCode,E);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[] 
	{ 
			new Pair<String, Integer>("Charisma", Integer.valueOf(9)), 
			new Pair<String, Integer>("Intelligence", Integer.valueOf(9)) 
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON, affectableStats.getStat(CharStats.STAT_SAVE_POISON) + (affectableStats.getClassLevel(this) * 2));
	}

	@Override
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount)
	{
		return Bard.bardAdjustExperienceGain(host, mob, victim, amount, 5.0);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return "";
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives group bonus combat experience when in an intelligent group, and "
				+ "more for a group of players.  Receives exploration and "
				+ "pub-finding experience based on danger level.");
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
			//if(w!=null)
				outfitChoices.add(w);
			final Item i=CMClass.getItem("GenInstrument");
			if(i!=null)
			{
				i.setName(L("pan pipes"));
				i.setDisplayText("some pan pipes lie here");
				i.setDescription("A simple musical instrument that minstrels use.");
				i.basePhyStats().setLevel(1);
				i.basePhyStats().setWeight(2);
				i.setBaseValue(0);
				i.setRawProperLocationBitmap(Wearable.WORN_HELD|Wearable.WORN_MOUTH);
				i.setRawLogicalAnd(true);
				i.setMaterial(RawMaterial.RESOURCE_IRON);
				((MusicalInstrument)i).setInstrumentType(MusicalInstrument.InstrumentType.FLUTES);
				i.recoverPhyStats();
				outfitChoices.add(i);
			}
		}
		return outfitChoices;
	}
}
