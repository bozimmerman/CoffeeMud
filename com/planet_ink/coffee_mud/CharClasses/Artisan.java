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
   Copyright 2003-2020 Bo Zimmerman

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
public class Artisan extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Artisan";
	}

	private final static String localizedStaticName = CMLib.lang().L("Artisan");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Commoner";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 2;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_WISDOM;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 30;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/6)+(1*(1?5))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/10)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_DAGGERONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(final MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	public Artisan()
	{
		super();
		for(final int i : CharStats.CODES.BASECODES())
			maxStatAdj[i]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Searching",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FireBuilding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Bandaging",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Foraging",false,"+WIS 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Drilling",false,"+INT 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Hunting",false,"+DEX 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Butchering",false,"+INT 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fishing",false,"+DEX 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chopping",false,"+CON 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Mining",false,"+STR 9");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Digging",false,"+STR 9");

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false,CMParms.parseSemicolons("Searching(75);Foraging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Autoswim",false,CMParms.parseSemicolons("Skill_Swim(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Farming",false,CMParms.parseSemicolons("Foraging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FoodPreserving",false,CMParms.parseSemicolons("Foraging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Shearing",false,CMParms.parseSemicolons("Foraging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_EdgedWeapon",false,CMParms.parseSemicolons("Proficiency_EdgedWeapon(75);Drilling(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Speculate",false,CMParms.parseSemicolons("Foraging(75);Drilling(75);Hunting(75);Fishing(75);Chopping(75);Mining(75);Digging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Sculpting",false,CMParms.parseSemicolons("Mining(75)",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Smelting",false,CMParms.parseSemicolons("FireBuilding(75);Mining(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Pottery",false,CMParms.parseSemicolons("Digging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Masonry",false,CMParms.parseSemicolons("Mining(75);Digging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Composting",false,CMParms.parseSemicolons("Farming(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FoodPrep",false,CMParms.parseSemicolons("FoodPreserving(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Cooking",false,CMParms.parseSemicolons("FoodPreserving(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Baking",false,CMParms.parseSemicolons("FoodPreserving(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Distilling",false,CMParms.parseSemicolons("FoodPreserving(75);Drilling(75);FireBuilding(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Landscaping",false,CMParms.parseSemicolons("Composting",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterFarming",false,CMParms.parseSemicolons("Landscaping(75);Farming(100)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",false,CMParms.parseSemicolons("FoodPrep(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Textiling",false,CMParms.parseSemicolons("Shearing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Gardening",false,CMParms.parseSemicolons("Composting(75);Herbology(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Floristry",false,CMParms.parseSemicolons("Skill_WildernessLore(75);Herbology(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterForaging",false,CMParms.parseSemicolons("Foraging(100);Distilling(75);Herbology(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterBaking",false,CMParms.parseSemicolons("Baking(100);MasterFarming(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterFoodPrep",false,CMParms.parseSemicolons("FoodPrep(100);Gardening(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Staff",false,CMParms.parseSemicolons("Fishing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FishLore",false,CMParms.parseSemicolons("Fishing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Irrigation",false,CMParms.parseSemicolons("Composting(75);FishLore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterGardening",false,CMParms.parseSemicolons("Gardening(100);MasterFoodPrep(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Tanning",false,CMParms.parseSemicolons("Butchering(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"MeatCuring",false,CMParms.parseSemicolons("Butchering(75);Fishing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Carpentry",false,CMParms.parseSemicolons("Chopping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",false,CMParms.parseSemicolons("Chopping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Autoclimb",false,CMParms.parseSemicolons("Skill_Climb(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Axe",false,CMParms.parseSemicolons("Chopping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ScrimShaw",false,CMParms.parseSemicolons("Butchering(75);Sculpting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_BluntWeapon",false,CMParms.parseSemicolons("Sculpting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Dyeing",false,CMParms.parseSemicolons("Textiling(75);Tanning(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Tailoring",false,CMParms.parseSemicolons("Textiling(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"CageBuilding",false,CMParms.parseSemicolons("Carpentry(75);Hunting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalHusbandry",false,CMParms.parseSemicolons("Shearing(75);Bandaging(75);CageBuilding(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Branding",false,CMParms.parseSemicolons("Shearing(75);Bandaging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Taxidermy",false,CMParms.parseSemicolons("Hunting(75);Tanning(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterCooking",false,CMParms.parseSemicolons("Cooking(100);MasterForaging(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"LeatherWorking",false,CMParms.parseSemicolons("Hunting(75);Tanning(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Baiting",false,CMParms.parseSemicolons("FishLore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Trawling",false,CMParms.parseSemicolons("FishLore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterButchering",false,CMParms.parseSemicolons("Butchering(100);Taxidermy(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Cobbling",false,CMParms.parseSemicolons("LeatherWorking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Staff",false,CMParms.parseSemicolons("Proficiency_Staff(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Trip",false,CMParms.parseSemicolons("Familiarity_Staff(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterFishing",false,CMParms.parseSemicolons("Fishing(100);Baiting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Stability",false,CMParms.parseSemicolons("Trawling(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Autocrawl",false,CMParms.parseSemicolons("Skill_Stability(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterTrawling",false,CMParms.parseSemicolons("Trawling(100);MasterFishing(75);Skill_Stability(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"PaperMaking",false,CMParms.parseSemicolons("Carpentry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fletching",false,CMParms.parseSemicolons("Carpentry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Wainwrighting",false,CMParms.parseSemicolons("Carpentry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"InstrumentMaking",false,CMParms.parseSemicolons("Carpentry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_BluntWeapon",false,CMParms.parseSemicolons("Proficiency_BluntWeapon(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Blacksmithing",false,CMParms.parseSemicolons("Smelting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Construction",false,CMParms.parseSemicolons("Carpentry(75);Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Shield",false,CMParms.parseSemicolons("Carpentry(75);Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"GlassBlowing",false,CMParms.parseSemicolons("Pottery(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"JewelMaking",false,CMParms.parseSemicolons("Pottery(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Siegecraft",false,CMParms.parseSemicolons("Fletching(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Boatwright",false,CMParms.parseSemicolons("Wainwrighting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterChopping",false,CMParms.parseSemicolons("Chopping(100);Siegecraft(75);Boatwright(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Shipwright",false,CMParms.parseSemicolons("Boatwright(75);Trawling(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"ClanShipwrighting",false,CMParms.parseSemicolons("Shipwright(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Weaving",false,CMParms.parseSemicolons("Chopping(75);Skill_WildernessLore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"PlantLore",false,CMParms.parseSemicolons("Herbology(75);Floristry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_FindHome",false,CMParms.parseSemicolons("Construction(75);Masonry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_FindShip",false,CMParms.parseSemicolons("Fishing(75);Shipwright(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Hammer",false,CMParms.parseSemicolons("Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Hammer",false,CMParms.parseSemicolons("Proficiency_Hammer(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_HammerRing",false,CMParms.parseSemicolons("Familiarity_Hammer(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterShearing",false,CMParms.parseSemicolons("Shearing(100);AnimalHusbandry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Cage",false,CMParms.parseSemicolons("AnimalHusbandry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_MountedCombat",false,CMParms.parseSemicolons("AnimalHusbandry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Embroidering",false,CMParms.parseSemicolons("Skill_Write(50);Tailoring(75);Leatherworking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterFloristry",false,CMParms.parseSemicolons("Floristry(100);Painting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterHerbology",false,CMParms.parseSemicolons("Herbology(100);MasterCooking(75);MasterFoodPrep(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Lore",false,CMParms.parseSemicolons("Skill_Write(75);PaperMaking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Ranged",false,CMParms.parseSemicolons("Fletching(100)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Ranged",false,CMParms.parseSemicolons("Proficiency_Ranged(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_CoverDefence",false,CMParms.parseSemicolons("Proficiency_Ranged(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false,CMParms.parseSemicolons("Speculate(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterDrilling",false,CMParms.parseSemicolons("Drilling(100);Speculate(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"StaffMaking",false,CMParms.parseSemicolons("Familiarity_Staff(75);Thief_Lore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"WandMaking",false,CMParms.parseSemicolons("Skill_WandUse(75);Thief_Lore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Rodsmithing",false,CMParms.parseSemicolons("Skill_WandUse(75);Thief_Lore(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Natural",false,CMParms.parseSemicolons("AnimalHusbandry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_Kick",false,CMParms.parseSemicolons("Proficiency_Natural(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Dodge",false,CMParms.parseSemicolons("Proficiency_Natural(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Weaponsmithing",false,CMParms.parseSemicolons("Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Armorsmithing",false,CMParms.parseSemicolons("Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"LockSmith",false,CMParms.parseSemicolons("Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Scrapping",false,CMParms.parseSemicolons("Armorsmithing(75);LeatherWorking(75);Tailoring(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterTailoring",false,CMParms.parseSemicolons("Tailoring(100);Scrapping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterLeatherWorking",false,CMParms.parseSemicolons("LeatherWorking(100);Scrapping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterArmorsmithing",false,CMParms.parseSemicolons("Armorsmithing(100);Scrapping(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Costuming",false,CMParms.parseSemicolons("Tailoring(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterDistilling",false,CMParms.parseSemicolons("Distilling(100);Herbology(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Appraise",false,CMParms.parseSemicolons("JewelMaking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Lacquerring",false,CMParms.parseSemicolons("JewelMaking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Proficiency_Sword",false,CMParms.parseSemicolons("Weaponsmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_AttackHalf",false,CMParms.parseSemicolons("Proficiency_Sword(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Sword",false,CMParms.parseSemicolons("Proficiency_Sword(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Parry",false,CMParms.parseSemicolons("Proficiency_Sword(100)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterWeaponsmithing",false,CMParms.parseSemicolons("Weaponsmithing(100);Familiarity_Sword(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Engraving",false,CMParms.parseSemicolons("Skill_Write(50);Blacksmithing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterCostuming",false,CMParms.parseSemicolons("Costuming(100);PaperMaking(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Merchant",false,CMParms.parseSemicolons("Engraving(75);Embroidering(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",false,CMParms.parseSemicolons("Merchant(75);Thief_Appraise(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Excavation",false,CMParms.parseSemicolons("Masonry(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterMining",false,CMParms.parseSemicolons("Mining(100);Excavation(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterDigging",false,CMParms.parseSemicolons("Digging(100);Excavation(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Painting",false,CMParms.parseSemicolons("Lacquerring(75);Dyeing(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Warrants",false,CMParms.parseSemicolons("Distilling(75);Painting(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Familiarity_Axe",false,CMParms.parseSemicolons("Proficiency_Axe(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SmokeRings",false,CMParms.parseSemicolons("Herbology(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Decorating",false,CMParms.parseSemicolons("Painting(75);Construction(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_StrategicRetreat",false,CMParms.parseSemicolons("Skill_Autoclimb(75);Skill_Autoswim(75);Skill_Autocrawl(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_ShipLore",false,CMParms.parseSemicolons("Shipwright(75)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),60,"LegendaryWeaponsmithing",false,CMParms.parseSemicolons("MasterWeaponsmithing(100)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterDyeing",false,CMParms.parseSemicolons("Dyeing(100)",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"MasterLacquerring",false,CMParms.parseSemicolons("Lacquerring(100)",true));
	}

	@Override
	public void startCharacter(final MOB mob, final boolean isBorrowedClass, final boolean verifyOnly)
	{
		super.startCharacter(mob, isBorrowedClass, verifyOnly);
		if(mob.fetchEffect("ArtisanalFocus")==null)
		{
			final Ability A=CMClass.getAbility("ArtisanalFocus");
			if(A!=null)
			{
				A.setSavable(true);
				mob.addNonUninvokableEffect(A);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source() == myHost)
		&&(msg.source().charStats().getCurrentClass() == this))
		{
			if((msg.targetMinor() == CMMsg.TYP_ITEMGENERATED)
			&&(msg.target() != null)
			&&(msg.tool() instanceof Ability)
			&&(msg.value() > 0)
			&&((!(msg.target() instanceof DoorKey))||(msg.tool().ID().equals("LockSmith")))
			&&(((((Ability)msg.tool()).classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_CRAFTINGSKILL)
			 ||((((Ability)msg.tool()).classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_EPICUREAN)
			 ||((((Ability)msg.tool()).classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_BUILDINGSKILL)))
			{
				CMLib.leveler().postExperience(msg.source(),null,null,msg.value(),false);
			}
			else
			if((msg.targetMinor() == CMMsg.TYP_RECIPELEARNED)
			&&(msg.target() != null)
			&&(msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_CRAFTINGSKILL)
			&&(msg.value() > 0))
			{
				final Map<String,Object> persMap = msg.source().playerStats().getClassVariableMap(this);
				if(persMap != null)
				{
					final String key = "LAST_DATE_FOR_"+msg.tool().ID().toUpperCase().trim();
					long[] lastTime = (long[])persMap.get(key);
					if(lastTime == null)
					{
						lastTime = new long[1];
						persMap.put(key, lastTime);
					}
					final Area homeA=CMLib.map().areaLocation(msg.source().getStartRoom());
					final TimeClock homeL = (homeA == null) ? null : homeA.getTimeObj();
					if((homeL!=null)
					&&((homeL.toHoursSinceEpoc() - lastTime[0])>0))
					{
						lastTime[0] = homeL.toHoursSinceEpoc();
						CMLib.leveler().postExperience(msg.source(), null, null, msg.value(), false);
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(ID().equals(mob.charStats().getCurrentClass().ID())&&(CMLib.dice().rollPercentage()<20))
			{
				int exp=0;
				for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(!A.isAutoInvoked())
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
					&&(mob.isMine(A)))
						exp++;
				}
				if(exp>0)
				{
					exp=exp/2;
					if(exp<1)
						exp=1;
					CMLib.leveler().postExperience(mob,null,null,exp,false);
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Dagger");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Gains experience when using common skills and no common skill limits.");
	}
}
