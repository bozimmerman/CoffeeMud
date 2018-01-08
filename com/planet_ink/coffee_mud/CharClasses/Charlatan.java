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

import java.lang.ref.WeakReference;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class Charlatan extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Charlatan";
	}

	private final static String localizedStaticName = CMLib.lang().L("Charlatan");

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

	protected volatile WeakReference<Ability> invokable = new WeakReference(null);

	public Charlatan()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Befriend",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Swipe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_Revoke",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Disguise",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Mark",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Song_Charm",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_StrikeTheSet",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Songcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_ReadMagic",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Imitation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_CastBlocking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Distract",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_DecipherScript",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Warrants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_BreakALeg",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Detection",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_FalseArrest",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Comprehension",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Spellcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Ranger_Track",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_MagicMissile",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Rage",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Thiefcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_AnalyzeMark",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Upstage",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Chantcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Song_Protection",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Shuffle",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_ExitStageLeft",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_SummonPlants",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Prayercraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Mana",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Song_Knowledge",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_CurtainCall",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Detection",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_Monologue",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Thanks",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_LocateObject",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Parry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_AdLib",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Con",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Song_Strength",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Disgust",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_FrameMark",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_MarkDisguise",true);

		// 30 -- cheaper skills
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","HalfElf","Drow","Gnome","Svirfneblin"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Charisma",Integer.valueOf(9)),
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9))
	};
	
	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override 
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount)
	{ 
		return Bard.bardAdjustExperienceGain(host,mob,victim,amount,6.0);
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return "";
	}

	@Override 
	public String getOtherBonusDesc()
	{
		return L("Receives 2% resistance per level to mind affects, 4% resistance per level to divination spells.  "
				+ "Non-class skills become cheaper at 30th level.  Gains a random non-class skill or spell every other level! "
				+ "Receives exploration and pub-finding experience based on danger level.");
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
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(host instanceof MOB)
		{
			final MOB myChar=(MOB)host;
			if(msg.amISource(myChar)
			&&(msg.tool() instanceof Ability)
			&&(!myChar.isMonster())
			&&(msg.sourceMinor()==CMMsg.TYP_PREINVOKE)
			&&(myChar.isMine(msg.tool()))
			&&(myChar.charStats().getClassLevel(this)>=30)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,msg.tool().ID())<1))
				invokable=new WeakReference(msg.tool());
		}
		super.executeMsg(host,msg);
		Bard.visitationBonusMessage(host,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB myChar=(MOB)myHost;
		if(msg.tool() instanceof Ability)
		{
			if(msg.amISource(myChar)
			&&(!myChar.isMonster())
			&&(msg.sourceMinor()!=CMMsg.TYP_PREINVOKE))
			{
				final WeakReference<Ability> curRef=invokable;
				if((curRef!=null)
				&&(msg.tool()==curRef.get()))
				{
					curRef.clear();
					final Ability A=((Ability)msg.tool());
					final int[] usageCost=A.usageCost(myChar, false);
					if(CMath.bset(A.usageType(),Ability.USAGE_MANA))
						myChar.curState().adjMana(usageCost[Ability.USAGEINDEX_MANA]/4,myChar.maxState());
					if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
						myChar.curState().adjMovement(usageCost[Ability.USAGEINDEX_MOVEMENT]/4,myChar.maxState());
					if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
						myChar.curState().adjHitPoints(usageCost[Ability.USAGEINDEX_HITPOINTS]/4,myChar.maxState());
				}
			}
			else
			if(msg.amITarget(myChar))
			{
				if(((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_DIVINATION)
				&&(CMLib.dice().roll(1,100,0)<(myChar.charStats().getClassLevel(this)*4)))
				{
					myChar.location().show(msg.source(),myChar,CMMsg.MSG_OK_ACTION,L("<T-NAME> fool(s) <S-NAMESELF>, causing <S-HIM-HER> to fizzle @x1.",msg.tool().name()));
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		// if he already has one, don't give another!
		if(mob.playerStats()!=null)
		{
			final int classLevel=mob.baseCharStats().getClassLevel(this);
			if(classLevel<2) 
				return;
			if((classLevel%2)!=0) 
				return;

			int maxSkills=classLevel/2;

			// now only give one, for current level, respecting alignment!
			// first, get a list of all skills you don't qualify for that you MIGHT have gained or will gain
			final List<Ability> choices=new Vector<Ability>();
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				final int lql=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				if((CMLib.ableMapper().qualifyingLevel(mob,A)<=0)
				&&(lql<25)
				&&(lql>0)
				&&(!CMLib.ableMapper().getSecretSkill(A.ID()))
				&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
				&&(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))
				&&(!CMLib.ableMapper().qualifiesOnlyByClan(mob, A))
				&&(!CMLib.ableMapper().qualifiesOnlyByRace(mob, A))
				&&(A.isAutoInvoked()||((A.triggerStrings()!=null)&&(A.triggerStrings().length>0))))
				{
					final DVector prereqs=CMLib.ableMapper().getUnmetPreRequisites(mob,A);
					if((prereqs==null)||(prereqs.size()==0))
						choices.add(A);
				}
			}
			
			// now count those you already have
			for(int a=choices.size()-1;a>=0;a--)
			{
				final Ability A=choices.get(a);
				if(mob.fetchAbility(A.ID())!=null)
					maxSkills--;
			}
			if(maxSkills<1) // if that reduced you to 0, you are done.
				return;
			// now eliminate those you already have, and those that are
			// above your level, if you are <25
			for(int a=choices.size()-1;a>=0;a--)
			{
				final Ability A=choices.get(a);
				final int lql=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				if((mob.fetchAbility(ID())!=null)
				||((lql!=classLevel)&&(lql!=classLevel-1)&&(classLevel<25)))
					choices.remove(a);
			}
			if(choices.size()==0)
				return;
			final Ability A=choices.get(CMLib.dice().roll(1,choices.size(),-1));
			if(A!=null)
				giveMobAbility(mob,A,0,"",isBorrowedClass);
		}
		else
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
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+(2*affectableStats.getClassLevel(this)));
	}
}
