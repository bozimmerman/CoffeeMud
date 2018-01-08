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
public class Jester extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Jester";
	}

	private final static String localizedStaticName = CMLib.lang().L("Jester");

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
	public String getMovementFormula()
	{
		return "16*((@x2<@x3)/18)";
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
		return "((@x4<@x5)/6)+(1*(1?3))";
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

	public Jester()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Juggle",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Befriend",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_BellyRolling",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Clumsiness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_IdentifyPoison",true,CMParms.parseSemicolons("Apothecary",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Song_Inebriation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_QuickChange",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Slapstick",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Song_Babble",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Mimicry",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Struggle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_MinorTrap",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Song_Detection",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_UsePoison",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Distract",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Song_Rage",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Peek",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_FireBreathing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Joke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Distraction",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Bind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Lightness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_SlowFall",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Seeing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_CenterOfAttention",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Dance_Stop",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Dance_Clog",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_CriticalShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Song_Mercy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Stability",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Song_Charm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Tumble",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Thanks",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_Satire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_AvoidTraps",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_CritStrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Song_Mute",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Steal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Feint",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Song_Quickness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_BlindFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Song_SingleMindedness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_Cartwheel",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Disgust",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_Puppeteer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Roll",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Skill_Buffoonery",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Gnome","Halfling","HalfElf","Svirfneblin","Merfolk","Faerie"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Charisma",Integer.valueOf(9)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,
			affectableStats.getStat(CharStats.STAT_SAVE_POISON)
			+(affectableStats.getClassLevel(this)*2));
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
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount)
	{
		return Bard.bardAdjustExperienceGain(host, mob, victim, amount, 6.0);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return "";
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives 2%/level bonus to saves versus poison.  Receives extra natural damaging skill. Receives group bonus combat experience when in an intelligent group, "
				+ "and more for a group of players.  Receives exploration and pub-finding experience based on danger level.");
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
