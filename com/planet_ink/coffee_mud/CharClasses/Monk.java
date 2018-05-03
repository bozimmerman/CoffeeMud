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
public class Monk extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Monk";
	}

	private final static String localizedStaticName = CMLib.lang().L("Monk");

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
		return 1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 20;
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
		return "((@x6<@x7)/3)+(2*(1?7))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/8)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	public Monk()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Armor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Shield",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_Kick",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_MonkeyPunch",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Hide",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_Climb",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Dodge",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_ArmorTweaking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Sneak",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_DeflectProjectile",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_KnifeHand",false,CMParms.parseSemicolons("Fighter_MonkeyPunch",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_AxKick",false,CMParms.parseSemicolons("Fighter_Kick",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_BackHand",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_BodyToss",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_BodyFlip",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_BlindFighting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_PressurePoints",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_CatchProjectile",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_FlyingKick",false,CMParms.parseSemicolons("Fighter_AxKick",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_WeaponBreak",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Pin",false);

/**/		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Detection",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Sweep",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Cartwheel",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_SideKick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_BodyShield",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_CircleParry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_KiStrike",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_AttackHalf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Tumble",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Snatch",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Endurance",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Gouge",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_CircleTrip",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Listen",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_LightningStrike",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_ReturnProjectile",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_AtemiStrike",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Goblinoid","Githyanki","Orc","Mindflayer"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	public boolean anyWeapons(final MOB mob)
	{
		return (mob.fetchWieldedItem()!=null) || (mob.fetchHeldItem()!=null);
	}

	@Override 
	public void executeMsg(Environmental host, CMMsg msg)
	{ 
		super.executeMsg(host,msg); 
		Fighter.conquestExperience(this,host,msg);
		Fighter.duelExperience(this, host, msg);
	}
	
	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives defensive bonus for high dexterity.  Receives unarmed attack bonus.  Receives bonus attack when unarmed.  "
				+ "Has Slow Fall ability.  Receives trap avoidance.  Receives bonus conquest and duel experience.");
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final int classLevel=mob.charStats().getClassLevel(this);
			if(CMLib.flags().isStanding(mob))
			{
				final int attArmor=(((int)Math.round(CMath.div(mob.charStats().getStat(CharStats.STAT_DEXTERITY),9.0)))+1)*(classLevel-1);
				affectableStats.setArmor(affectableStats.armor()-attArmor);
			}
			if(!anyWeapons(mob))
			{
				affectableStats.setSpeed(affectableStats.speed()+1.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+classLevel);
			}
			if(affected.fetchEffect("Falling")!=null)
				affectableStats.setWeight(0);
		}
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,
			affectableStats.getStat(CharStats.STAT_SAVE_MIND)
			+(affectableStats.getClassLevel(this)*2));
		affectableStats.setStat(CharStats.STAT_SAVE_TRAPS,
			affectableStats.getStat(CharStats.STAT_SAVE_TRAPS)
			+(affectableStats.getClassLevel(this)*2));
	}

	@Override
	public void level(MOB mob, List<String> newAbilityIDs)
	{
		super.level(mob, newAbilityIDs);
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
			return;
		final int attArmor=(((int)Math.round(CMath.div(mob.charStats().getStat(CharStats.STAT_DEXTERITY),9.0)))+1);
		mob.tell(L("^NYour dexterity grants you a defensive bonus of ^H@x1^?.^N",""+attArmor));
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
		return null;
	}
}
