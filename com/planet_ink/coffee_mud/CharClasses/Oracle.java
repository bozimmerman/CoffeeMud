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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
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
public class Oracle extends Cleric
{
	@Override
	public String ID()
	{
		return "Oracle";
	}

	private final static String localizedStaticName = CMLib.lang().L("Oracle");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Cleric";
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_WISDOM;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_GOODCLERIC;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	protected int alwaysFlunksThisQuality()
	{
		return 0;
	}

	public Oracle()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_SenseLife",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_SenseAllergies",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_SenseAlignment",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_Freedom",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_TurnUndead",false);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_ControlUndead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_SoulPeering",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_SenseDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_CureFatigue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_SenseFaithful",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_MinorInfusion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Prayercraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_RemoveParalysis",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_DivineGuidance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Omnipresence",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_SenseHidden",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Skillcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Titling",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_SpeakWithDead",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false,CMParms.parseSemicolons("Prayer_CureSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_SenseProfessions",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_LowerLaw",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Prophecy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Thiefcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_SenseSkills",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_SeekersPrayer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_InfuseHoliness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_CureExhaustion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_SenseSongs",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",true,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Philosophy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_SenseSpells",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false,CMParms.parseSemicolons("Prayer_Heal",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_DivinePerspective",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_SensePrayers",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Resurrect",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_AboveTheLaw",false);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public boolean tick(Tickable myChar, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
		}
		return true;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Dwarf","Elf","HalfElf","Elf-kin",
		"Fairy-kin","Centaur","Svirfneblin","Aarakocran","Merfolk",
		"Faerie","-Duergar","-Drow"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9)),
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives a non-class skill at 30th level, and every Oracle level thereafter.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.");
	}

	protected int numNonQualified(MOB mob)
	{
		int numNonQualified=0;
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			boolean qualifies=false;
			for(int c=0;c<mob.charStats().numClasses();c++)
			{
				final CharClass C=mob.charStats().getMyClass(c);
				if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>0)
					qualifies=true;
			}
			if((!qualifies)
			&&(CMLib.ableMapper().getQualifyingLevel(mob.baseCharStats().getMyRace().ID(),true,A.ID())<0)
			&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID())))
				numNonQualified++;
		}
		return numNonQualified;
	}

	protected int maxNonQualified(MOB mob)
	{
		int level=mob.charStats().getClassLevel(this)-30;
		level++;
		return level;
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		// if he already has one, don't give another!
		if((mob.playerStats()!=null)
		&&(mob.charStats().getClassLevel(this)>=30))
		{
			if(numNonQualified(mob)>=maxNonQualified(mob))
				return;

			Ability newOne=null;
			int tries=0;
			while((newOne==null)&&((++tries)<100))
			{
				final CharClass C=CMClass.randomCharClass();
				if((C!=null)
				&&(!C.ID().equals(ID()))
				&&(!C.ID().equalsIgnoreCase("Archon"))
				&&(mob.charStats().getClassLevel(C)<0))
				{
					int tries2=0;
					while((newOne==null)&&((++tries2)<10000))
					{
						final Ability A=CMClass.randomAbility();
						if( A != null )
						{
							final int lql=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
							if((lql<25)
							&&(lql>0))
							{
								if((!CMLib.ableMapper().getSecretSkill(C.ID(),true,A.ID()))
								&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())<0)
								&&(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))
								&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
								&&(A.isAutoInvoked()||((A.triggerStrings()!=null)&&(A.triggerStrings().length>0)))
								&&(mob.fetchAbility(A.ID())==null))
								{
									final DVector prereqs=CMLib.ableMapper().getUnmetPreRequisites(mob,A);
									if((prereqs==null)||(prereqs.size()==0))
										newOne=A;
								}
							}
						}
					}
				}
			}
			if(newOne!=null)
				mob.addAbility(newOne);
		}
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("SmallMace");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public void level(MOB mob, List<String> newAbilityIDs)
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
			return;
		if((!mob.isMonster())&&(mob.charStats().getClassLevel(this)>=30))
		{
			if((newAbilityIDs.size()==0)&&(numNonQualified(mob)>=maxNonQualified(mob)))
				mob.tell(L("^NYou have learned no new secrets this level, as you already know ^H@x1/@x2^? secret skills.^N",""+numNonQualified(mob),""+maxNonQualified(mob)));
		}
	}
}
