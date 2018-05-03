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
public class Barbarian extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Barbarian";
	}

	private final static String localizedStaticName = CMLib.lang().L("Barbarian");

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
		return -1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
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
	public String getMovementFormula()
	{
		return "13*((@x2<@x3)/18)";
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?7))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/8)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_NONMETAL;
	}

	public Barbarian()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_Charge",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_SmokeSignals",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Scalp",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Fighter_Battlecry",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Disarm",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Berzerk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_BloodBrother",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_ArmorTweaking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Spring",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Apothecary",0,"ANTIDOTES",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_JungleTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_Intimidate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_SwampTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Warcry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_DesertTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_ImprovedThrowing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_MountainTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_WeaponBreak",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Sweep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Rallycry",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_MountedCombat",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_HillsTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Endurance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_IdentifyPoison",true,CMParms.parseSemicolons("Apothecary",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Roll",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_ForestTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_BullRush",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_Fragmentation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_PlainsTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Stonebody",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_Shrug",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override 
	public String getOtherBonusDesc()
	{
		return L("Damage reduction 1pt/5 levels.  A 1%/level resistance to Enchantments.  Receives bonus conquest and duel experience.");
	}
	
	@Override 
	public void executeMsg(Environmental host, CMMsg msg)
	{ 
		super.executeMsg(host,msg); 
		Fighter.conquestExperience(this,host,msg);
		Fighter.duelExperience(this, host, msg);
	}
	
	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
		new Pair<String,Integer>("Constitution",Integer.valueOf(9))
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

		if((msg.amITarget(myChar))
		&&(msg.tool() instanceof Weapon)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			final int classLevel=myChar.charStats().getClassLevel(this);
			int recovery=(classLevel/5);
			final double minPct=.10+((classLevel>33)?((classLevel-30)*.0025):0);
			final int minAmount=(int)Math.round(CMath.mul(msg.value(), minPct));
			if(recovery < minAmount)
				recovery=minAmount;
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
		{
			if(CMLib.dice().rollPercentage()<=myChar.charStats().getClassLevel(this))
			{
				myChar.location().show(myChar,null,msg.source(),CMMsg.MSG_OK_ACTION,L("<S-NAME> resist(s) the @x1 attack from <O-NAMESELF>!",msg.tool().name()));
				return false;
			}
		}
		return super.okMessage(myChar,msg);
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
	
	@Override
	public int classDurationModifier(MOB myChar, Ability skill, int duration)
	{
		if(myChar==null)
			return duration;
		if((((skill.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
			||((skill.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL))
		&&(!skill.ID().equals("Foraging"))
		&&(!skill.ID().equals("Hunting")))
			return duration*2;

		return duration;
	}
}
