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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2023 Bo Zimmerman

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
public class Paladin extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Paladin";
	}

	private final static String localizedStaticName = CMLib.lang().L("Paladin");
	private final static String localizedStaticName2 = CMLib.lang().L("Fallen Paladin");
	private final static String localizedStaticName3 = CMLib.lang().L("Anti-Paladin");

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
		return 0;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public String getMovementFormula()
	{
		return "7*((@x2<@x3)/18)";
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
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?6))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/8)+(1*(1?3))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_ANY;
	}

	public Paladin()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Armor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Shield",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",75,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Paladin_HealingHands",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Paladin_ImprovedResists",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Fighter_ArmorTweaking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Paladin_HolyStrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Paladin_UnholyStrike",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Paladin_SummonMount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CureLight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_SenseEvil",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_RelicUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_ReadPrayer",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Paladin_DiseaseImmunity",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Paladin_PaladinsMount",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtEvil",false);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.CHAOTIC))
			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtChaos",false);

		if(CMLib.factions().isAlignmentLoaded(Faction.Align.CHAOTIC))
			CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_SenseChaos",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_CureDeafness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_CureSerious",false,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_HealMount",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Headlock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Paladin_Defend",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Bless",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_Freedom",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Paladin_Courage",true);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_DispelEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_ClinchHold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_RestoreVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Paladin_Purity",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_RemovePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Paladin_Breakup",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_CureDisease",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Paladin_MountedCharge",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Paladin_PoisonImmunity",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Sanctuary",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_CureCritical",false,CMParms.parseSemicolons("Prayer_CureSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Trip",false);

		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Paladin_Aura",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_Calm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_CureBlindness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_ResurrectMount",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BladeBarrier",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_CureFatigue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Paladin_CommandHorse",false);

		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_LightHammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_Sweep",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Paladin_Goodness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Paladin_AbidingAura",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Heal",false,CMParms.parseSemicolons("Prayer_CureCritical",true));

		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Paladin_CraftHolyAvenger",true,CMParms.parseSemicolons("Specialization_Sword;Weaponsmithing",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Paladin_PlanarDefiance", 0, "", false,
												 SecretFlag.MASKED, null, "+PLANE \"-Prime Material\"");
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public boolean qualifiesForThisClass(final MOB mob, final boolean quiet)
	{
		if(quiet)
			return super.qualifiesForThisClass(mob, quiet);
		if(!isPaladinAlignment(mob))
		{
			if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
					mob.tell(L("You must be lawful/good or chaotic/evil to be a paladin."));
				else
					mob.tell(L("You must be good or evil to be a paladin."));
			}
			else
			if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				mob.tell(L("You must be lawful or chaotic to be a paladin."));
			return false;
		}
		return super.qualifiesForThisClass(mob, quiet);
	}

	@Override
	protected boolean allowedToAutoGain(final MOB mob, final Ability A)
	{
		return A.appropriateToMyFactions(mob);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		Fighter.conquestExperience(this,host,msg);
		Fighter.duelExperience(this, host, msg);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Must remain lawful/good or chaotic/evil to avoid prayer/skill failure chance.");
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives bonus conquest and duel experience.");
	}

	protected boolean isPaladinAlignment(final MOB mob)
	{
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
		{
			if(CMLib.flags().isGood(mob))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				{
					if(CMLib.flags().isLawful(mob))
						return true;
				}
				else
					return true;
			}
			else
			if(CMLib.flags().isEvil(mob))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				{
					if(CMLib.flags().isChaotic(mob))
						return true;
				}
				else
					return true;
			}
		}
		else
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
		{
			if((CMLib.flags().isLawful(mob))||(CMLib.flags().isChaotic(mob)))
				return true;
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;
		if((msg.amISource(myChar))
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(!isPaladinAlignment(myChar))
		&&((msg.tool()==null)||((CMLib.ableMapper().getQualifyingLevel(ID(),true,msg.tool().ID())>0)
								&&(myChar.isMine(msg.tool()))))
		&&(CMLib.dice().rollPercentage()>myChar.charStats().getStat(CharStats.STAT_WISDOM)*2))
		{
			myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!"));
			return false;
		}
		return super.okMessage(myChar, msg);
	}

	private final String[] raceRequiredList=new String[]{"Human","Humanoid","Githyanki"};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9)),
		new Pair<String,Integer>("Strength",Integer.valueOf(9))
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
			final Weapon w=CMClass.getWeapon("Shortsword");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
		{
			if(CMLib.flags().isGood(affectedMOB))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				{
					if(CMLib.flags().isLawful(affectedMOB))
						return;
					affectableStats.setDisplayClassLevel(localizedStaticName2);
					return;
				}
			}
			else
			if(CMLib.flags().isEvil(affectedMOB))
			{
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				{
					if(CMLib.flags().isChaotic(affectedMOB))
					{
						affectableStats.setDisplayClassLevel(localizedStaticName3);
						return;
					}
					affectableStats.setDisplayClassLevel(localizedStaticName2);
					return;
				}
				else
				{
					affectableStats.setDisplayClassLevel(localizedStaticName3);
					return;
				}
			}
			else
			{
				affectableStats.setDisplayClassLevel(localizedStaticName2);
				return;
			}
		}
		else
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
		{
			if(CMLib.flags().isLawful(affectedMOB))
				return;
			if(CMLib.flags().isChaotic(affectedMOB))
			{
				affectableStats.setDisplayClassLevel(localizedStaticName3);
				return;
			}
			affectableStats.setDisplayClassLevel(localizedStaticName2);
			return;
		}
	}
}
