package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Thief extends StdCharClass
{
	public String ID(){return "Thief";}
	public String name(){return "Thief";}
	public String baseClass(){return "Thief";}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 5;}
	public int getMovementMultiplier(){return 10;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 10;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public int allowedArmorLevel(){return CharClass.ARMOR_LEATHER;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};


	public Thief()
	{
		super();
		maxStatAdj[CharStats.DEXTERITY]=7;
		if(ID().equals(baseClass())&&(!loaded()))
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"ThievesCant",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,false);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",false);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_SneakAttack",false);

			CMAble.addCharAbilityMapping(ID(),3,"Thief_Countertracking",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",true);

			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Autosneak",false);

			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dirt",false);

			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);

			CMAble.addCharAbilityMapping(ID(),7,"Thief_Peek",true);
			CMAble.addCharAbilityMapping(ID(),7,"Thief_UsePoison",false);

			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);

			CMAble.addCharAbilityMapping(ID(),9,"Thief_Observation",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",false);

			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Haggle",false);

			CMAble.addCharAbilityMapping(ID(),11,"Thief_Steal",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",false);

			CMAble.addCharAbilityMapping(ID(),12,"Thief_Listen",false);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Thief_Graffiti",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_Detection",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Bind",false);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Arsonry",false);

			CMAble.addCharAbilityMapping(ID(),14,"Thief_Surrender",false);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);

			CMAble.addCharAbilityMapping(ID(),15,"Thief_Snatch",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);

			CMAble.addCharAbilityMapping(ID(),16,"Thief_SilentGold",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);

			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_CarefulStep",true);

			CMAble.addCharAbilityMapping(ID(),18,"Thief_SilentLoot",false);
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Comprehension",false);

			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",true);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Snatch",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Ventrilloquate",false);

			CMAble.addCharAbilityMapping(ID(),20,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Alertness",false);

			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sap",true);
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Panhandling",true);

			CMAble.addCharAbilityMapping(ID(),22,"Thief_Flank",false);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_ImprovedDistraction",false);

			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",false);
			CMAble.addCharAbilityMapping(ID(),23,"Skill_Warrants",true);

			CMAble.addCharAbilityMapping(ID(),24,"Thief_Bribe",false);
			CMAble.addCharAbilityMapping(ID(),24,"Skill_EscapeBonds",false);

			CMAble.addCharAbilityMapping(ID(),25,"Thief_Ambush",true);
			CMAble.addCharAbilityMapping(ID(),25,"Thief_Squatting",false);

			CMAble.addCharAbilityMapping(ID(),30,"Thief_Nondetection",true);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Thief.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

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
	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.COMMON_SKILL)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(myHost instanceof MOB)
		{
			MOB myChar=(MOB)myHost;
			if(msg.amISource(myChar)
			   &&(!myChar.isMonster())
			   &&(msg.sourceCode()==CMMsg.MSG_THIEF_ACT)
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof MOB)
			   &&(msg.targetMessage()==null)
			   &&(msg.tool()!=null)
			   &&(msg.tool() instanceof Ability)
			   &&(msg.tool().ID().equals("Thief_Steal")
				  ||msg.tool().ID().equals("Thief_Robbery")
				  ||msg.tool().ID().equals("Thief_Embezzle")
				  ||msg.tool().ID().equals("Thief_Mug")
				  ||msg.tool().ID().equals("Thief_Swipe")))
				MUDFight.postExperience(myChar,(MOB)msg.target()," for a successful "+msg.tool().name(),10,false);
		}
		super.executeMsg(myHost,msg);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);

		int dexStat=mob.charStats().getStat(CharStats.DEXTERITY);
		int maxDexStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.DEXTERITY));
		if(dexStat>maxDexStat) dexStat=maxDexStat;
		int attArmor=(int)Math.round(Util.div(dexStat,9.0));
		if(dexStat>=25)attArmor+=2;
		else
		if(dexStat>=22)attArmor+=1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level after 1st.  Bonus experience for using certain skills.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isSleeping(affected)||Sense.isSitting(affected))
			affectableStats.setArmor(affectableStats.armor()+(100-affected.baseEnvStats().armor()));
	}
	public void level(MOB mob)
	{
		super.level(mob);
		int dexStat=mob.charStats().getStat(CharStats.DEXTERITY);
		int maxDexStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.DEXTERITY));
		if(dexStat>maxDexStat) dexStat=maxDexStat;
		
		int attArmor=((int)Math.round(Util.div(dexStat,9.0)));
		if(dexStat>=25)attArmor+=2;
		else
		if(dexStat>=22)attArmor+=1;
		
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^NYour stealthiness grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}
