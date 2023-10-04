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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Cavalier extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Cavalier";
	}

	private final static String localizedStaticName = CMLib.lang().L("Cavalier");

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
		return "7*((@x2<@x3)/18)";
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?6))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/8)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_ANY;
	}

	public Cavalier()
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Armor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Shield",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Unbinding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_RacialMount",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_MountedCombat",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Fighter_CallSteed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_RopeTricks",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_ArmorTweaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_ResistBuck",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_AweMounts",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_SetPolearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Branding",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_FindHome",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Herding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_SootheMount",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_TrueShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_TendMount",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_FavoredMount",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Forceback",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_RearGuard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_IndoorRiding",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_ScoutAhead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_BestowName",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_CommandMount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Headlock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Ranger_HuntersEndurance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_UnwaveringMark",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_GracefulDismount",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_PointBlank",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_MountedTactics",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"MountTaming",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_FavoredMount2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Vanguard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Ridethrough",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_MountedRetreat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_ConfidentVanity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Lassoing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_ArmHold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_CriticalShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Jousting",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_RopeTrip",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_FierceMount",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_MountedCharge",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_RopeGrab",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Trip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_FarShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_ArmoredVanity",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_FavoredMount3",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Sweep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_MountedLeap",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_CompanionMount",true); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_HoldTheLine",true); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_FaithfulMount",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_BreakMount",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_WagonTrain",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_CaravanTactics",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_LuckyVanity",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_PolearmSweep",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_RideToTheRescue",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_FavoredMount4",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Roping",false); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Runover",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_RegionalAwareness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_PlainsTactics",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_RidingFight",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_CalledShot",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_ProtectedMount",true); //TODO
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"CaravanConversion",false); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_StableMount",true); //TODO

		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Fighter_PlanarMount", 0, "", false, //TODO
				 SecretFlag.MASKED, null, "+PLANE \"-Prime Material\"");
	}

	@Override
	public int availabilityCode()
	{
		return 0;//Area.THEME_FANTASY;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Bonus conquest and duel experience.  Does not divide experience with his or her mount.");
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
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
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;

		return super.okMessage(myChar,msg);
	}

	@Override
	public boolean isValidClassDivider(final MOB killer, final MOB killed, final MOB mob, final Set<MOB> followers)
	{
		if((mob!=null)
		&&(mob!=killed)
		&&(!mob.amDead())
		&&((!mob.isMonster())
			||(!CMLib.flags().isAnimalIntelligence(mob))
			||(!(mob instanceof Rideable)))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

	@Override
	public void grantAbilities(final MOB mob, final boolean isBorrowedClass)
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
				{
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),
								   CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),
								   isBorrowedClass);
				}
			}
		}
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Spear");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}
}
