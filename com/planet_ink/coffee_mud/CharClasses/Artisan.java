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
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Butchering",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chopping",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Digging",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Drilling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fishing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Foraging",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Hunting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Mining",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Searching",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Pottery",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"ScrimShaw",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Shearing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Blacksmithing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"LeatherWorking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"GlassBlowing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Sculpting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Tailoring",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Weaving",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"CageBuilding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Cooking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Baking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"FoodPrep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"JewelMaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Warrants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Costuming",false,CMParms.parseSemicolons("Tailoring",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Dyeing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Embroidering",false,CMParms.parseSemicolons("Skill_Write",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Engraving",false,CMParms.parseSemicolons("Skill_Write",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Lacquerring",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Smelting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Armorsmithing",false,CMParms.parseSemicolons("Blacksmithing",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fletching",false,CMParms.parseSemicolons("Specialization_Ranged",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Weaponsmithing",false,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Shipwright",false,CMParms.parseSemicolons("Carpentry",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Wainwrighting",false,CMParms.parseSemicolons("Carpentry",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"PaperMaking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Cobbling",false,CMParms.parseSemicolons("LeatherWorking",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Siegecraft",false,CMParms.parseSemicolons("Carpentry",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Distilling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Farming",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_WandUse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Speculate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Painting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"LockSmith",0,"",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Composting",false,CMParms.parseSemicolons("Farming",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Excavation",false,CMParms.parseSemicolons("Sculpting",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Construction",true,CMParms.parseSemicolons("Carpentry",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Masonry",true,CMParms.parseSemicolons("Sculpting",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Landscaping",false,CMParms.parseSemicolons("Farming",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Cage",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Merchant",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Taxidermy",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"AnimalHusbandry",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Stability",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Appraise",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"InstrumentMaking",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_Haggle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Irrigation",false,CMParms.parseSemicolons("Drilling",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"MasterTailoring",false,CMParms.parseSemicolons("Tailoring(100)",true),"+DEX 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"MasterCostuming",false,CMParms.parseSemicolons("Costuming(100)",true),"+INT 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"MasterButchering",false,CMParms.parseSemicolons("Butchering(100)",true),"+STR 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"MasterLeatherWorking",false,CMParms.parseSemicolons("LeatherWorking(100)",true),"+CON 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"MasterShearing",false,CMParms.parseSemicolons("Shearing(100)",true),"+WIS 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"MasterArmorsmithing",false,CMParms.parseSemicolons("Armorsmithing(100)",true),"+STR 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"MasterDrilling",false,CMParms.parseSemicolons("Drilling(100)",true),"+INT 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"MasterWeaponsmithing",false,CMParms.parseSemicolons("Weaponsmithing(100);Specialization_*",true),"+STR 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"MasterFishing",false,CMParms.parseSemicolons("Fishing(100)",true),"+DEX 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Scrapping",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"MasterFarming",false,CMParms.parseSemicolons("Farming(100)",true),"+WIS 16");

		CMLib.ableMapper().addCharAbilityMapping(ID(),26,"MasterForaging",false,CMParms.parseSemicolons("Foraging(100)",true),"+CHA 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),26,"MasterDistilling",false,CMParms.parseSemicolons("Distilling(100)",true),"+CHA 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),27,"MasterChopping",false,CMParms.parseSemicolons("Chopping(100)",true),"+STR 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),27,"MasterFoodPrep",false,CMParms.parseSemicolons("FoodPrep(100)",true),"+DEX 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),28,"MasterDigging",false,CMParms.parseSemicolons("Digging(100)",true),"+CON 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),28,"MasterCooking",false,CMParms.parseSemicolons("Cooking(100)",true),"+INT 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),29,"MasterMining",false,CMParms.parseSemicolons("Mining(100)",true),"+STR 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),29,"MasterBaking",false,CMParms.parseSemicolons("Baking(100)",true),"+CON 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_Lore",false);

	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.source() == myHost)
		&&(msg.targetMinor() == CMMsg.TYP_ITEMGENERATED)
		&&(msg.target() != null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode() & Ability.ALL_DOMAINS) == Ability.DOMAIN_CRAFTINGSKILL)
		&&(msg.value() > 0))
			CMLib.leveler().postExperience(msg.source(),null,null,msg.value(),false);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
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
	public List<Item> outfit(MOB myChar)
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
