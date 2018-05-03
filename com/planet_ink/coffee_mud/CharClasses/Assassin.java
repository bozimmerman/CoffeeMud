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
@SuppressWarnings("unchecked")
public class Assassin extends Thief
{
	@Override
	public String ID()
	{
		return "Assassin";
	}

	private final static String localizedStaticName = CMLib.lang().L("Assassin");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[] { new Pair<String, Integer>("Dexterity", Integer.valueOf(5)), new Pair<String, Integer>("Wisdom", Integer.valueOf(5)) };

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Strong resistance to all poisons at 21st level.");
	}

	public Assassin()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false,"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Mark",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Specialization_Dagger",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_SneakAttack",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_KillLog",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_IdentifyPoison",false,CMParms.parseSemicolons("Apothecary",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Dirt",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_DetectTraps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_DaggerDefense",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_MarkInvisibility",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Specialization_Natural",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Shadow",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Specialization_FlailedWeapon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_Spying",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_CritStrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Trip",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_UsePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Specialization_BluntWeapon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_AnalyzeMark",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Observation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Assassinate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Shadowpass",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_TrueShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Specialization_Axe",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_DualParry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_AttackHalf",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_CutThroat",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_CriticalShot",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Specialization_Hammer",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Peek",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Scratch",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Sap",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_HighMarks",true,CMParms.parseSemicolons("Thief_Mark",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_TwoDaggerFighting",false,CMParms.parseSemicolons("Skill_TwoWeaponFighting",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Trap",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_Stability",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_MarkerSpying",false,CMParms.parseSemicolons("Thief_Mark;Thief_Spying", true),null);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_TapRoom",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_Ambush",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_Flank",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_FrameMark",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Cleave",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_Tumble",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Evesdrop",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_DeepCut",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_Espionage",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_Shadowstrike",true);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affectableStats.getClassLevel(this)>=21)
			affectableStats.setStat(CharStats.STAT_SAVE_POISON,200);
	}
}
