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
public class Burglar extends Thief
{
	@Override
	public String ID()
	{
		return "Burglar";
	}

	private final static String localizedStaticName = CMLib.lang().L("Burglar");

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
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_BURGLAR;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	public Burglar()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false,"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Swipe",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_UndergroundConnections",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Appraise",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Palm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_Intimidate",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_TagTurf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_FenceLoot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Peek",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Observation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Forgery",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Listen",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_ImprovedHiding",false,CMParms.parseSemicolons("Thief_Hide",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_BackStab",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Steal",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_TurfWar",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_SlipItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_ImprovedPeek",false,CMParms.parseSemicolons("Thief_Peek",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_PlantItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Detection",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Bribe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_ImprovedSwipe",false,CMParms.parseSemicolons("Thief_Swipe",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_SilentGold",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_HideOther",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_Safecracking",false,CMParms.parseSemicolons("Thief_Pick",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_SilentLoot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_BlindFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_SilentDrop",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Robbery",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Map",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_SilentOpen",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_SenseLaw",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Mug",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Safehouse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Footlocks",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Racketeer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_StripItem",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_UsePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_ImprovedSteal",false,CMParms.parseSemicolons("Thief_Steal",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_AnalyzeDweomer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Tumble",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Con",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Comprehension",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_Embezzle",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_HideInPlainSight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_ContractHit",true);
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9)),
		new Pair<String,Integer>("Charisma",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}
}
