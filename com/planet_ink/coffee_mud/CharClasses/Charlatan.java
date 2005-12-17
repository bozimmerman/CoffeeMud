package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Charlatan extends StdCharClass
{
	public String ID(){return "Charlatan";}
	public String name(){return "Charlatan";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Charlatan()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=4;
		maxStatAdj[CharStats.WISDOM]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Swipe",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Disguise",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Mark",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Song_Charm",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Songcraft",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_ReadMagic",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Song_Detection",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Imitation",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Distract",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Warrants",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Dodge",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_FalseArrest",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Comprehension",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Spellcraft",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Ranger_Track",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_MagicMissile",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Rage",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_AnalyzeMark",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Chantcraft",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Song_Protection",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SummonPlants",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Shuffle",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Prayercraft",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_CureLight",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Mana",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Song_Knowledge",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Detection",true);

			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Thanks",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_LocateObject",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Parry",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Con",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Song_Strength",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Disgust",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_FrameMark",false);

			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_MarkDisguise",true);

			// 30 -- cheaper skills
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Charisma 9+, Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Charlatan.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.WISDOM) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Charlatan.");
			return false;
		}
		if((!(mob.charStats().getMyRace().ID().equals("Human")))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human, or Half Elf to be a Charlatan");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "Receives 2% resistance per level to mind affects, 4% resistance per level to divination spells.  Non-class skills become cheaper at 30th level.  Gains a random non-class skill or spell every other level!";}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(myChar.isMine(msg.tool()))
			&&(myChar.charStats().getClassLevel(this)>=30)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,msg.tool().ID())<1))
			{
				Ability A=((Ability)msg.tool());
				if(CMath.bset(A.usageType(),Ability.USAGE_MANA))
					myChar.curState().adjMana(A.usageCost(myChar)[Ability.USAGE_MANAINDEX]/4,myChar.maxState());
				if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT))
					myChar.curState().adjMovement(A.usageCost(myChar)[Ability.USAGE_MOVEMENTINDEX]/4,myChar.maxState());
				if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS))
					myChar.curState().adjMovement(A.usageCost(myChar)[Ability.USAGE_HITPOINTSINDEX]/4,myChar.maxState());
			}
		}
		else
		if(msg.amITarget(myChar))
		{
			if((msg.tool()!=null)
			   &&(msg.tool() instanceof Ability)
			   &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			   &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_DIVINATION)
			   &&(CMLib.dice().roll(1,100,0)<(myChar.charStats().getClassLevel(this)*4)))
			{
				myChar.location().show(msg.source(),myChar,CMMsg.MSG_OK_ACTION,"<T-NAME> fool(s) <S-NAMESELF>, causing <S-HIM-HER> to fizzle "+msg.tool().name()+".");
				return false;
			}
		}
		return true;
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		// if he already has one, don't give another!
		if(mob.playerStats()!=null)
		{
			int classLevel=mob.baseCharStats().getClassLevel(this);
			if(classLevel<2) return;
			if((classLevel%2)!=0) return;

			for(int a=0;a<mob.numLearnedAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((CMLib.ableMapper().qualifyingLevel(mob,A)<=0)
				&&((CMLib.ableMapper().lowestQualifyingLevel(A.ID())==classLevel)||(CMLib.ableMapper().lowestQualifyingLevel(A.ID())==classLevel-1)))
					return;
			}
			// now only give one, for current level, respecting alignment!
			Vector choices=new Vector();
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				int lql=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
				if((CMLib.ableMapper().qualifyingLevel(mob,A)<=0)
				&&(mob.fetchAbility(A.ID())==null)
				&&(lql<25)
				&&(lql>0)
				&&((lql==classLevel)
				   ||(lql==classLevel-1)
				   ||(classLevel>=25))
				&&(!CMLib.ableMapper().getSecretSkill(A.ID()))
				&&(!CMLib.ableMapper().classOnly("Archon",A.ID()))
				&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID())))
					choices.addElement(A);
			}
			if(choices.size()==0) return;
			Ability A=(Ability)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
			if(A!=null)	giveMobAbility(mob,A,0,"",isBorrowedClass);
		}
		else
		{
			Vector V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.COMMON_SKILL)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProfficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+(2*affectableStats.getClassLevel(this)));
	}

	public void level(MOB mob)
	{
		if(CMSecurity.isDisabled("LEVELS")) return;
		
		Vector V=new Vector();
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)	V.addElement(A);
		}
		super.level(mob);
		Ability able=null;
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(!V.contains(A))
			&&(CMLib.ableMapper().qualifyingLevel(mob,A)<=0))
				able=A;
		}
		if(able!=null)
		{
			String type=Ability.TYPE_DESCS[(able.classificationCode()&Ability.ALL_CODES)].toLowerCase();
			mob.tell("^NYou have learned the secret to the "+type+" ^H"+able.name()+"^?.^N");
		}
	}
}
