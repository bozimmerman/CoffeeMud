package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
public class Prancer extends StdCharClass
{
	public String ID(){return "Prancer";}
	public String name(){return "Prancer";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getMovementMultiplier(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Prancer()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=4;
		maxStatAdj[CharStats.STRENGTH]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Dance_Stop",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Dance_CanCan",true);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),2,"Dance_Foxtrot",true);

			CMAble.addCharAbilityMapping(ID(),3,"Fighter_Kick",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Dance_Tarantella",true);

			CMAble.addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
			CMAble.addCharAbilityMapping(ID(),4,"Dance_Waltz",true);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),5,"Dance_Salsa",true);
			CMAble.addCharAbilityMapping(ID(),5,"Dance_Grass",true);

			CMAble.addCharAbilityMapping(ID(),6,"Dance_Clog",true);

			CMAble.addCharAbilityMapping(ID(),7,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),7,"Dance_Capoeira",true);

			CMAble.addCharAbilityMapping(ID(),8,"Dance_Tap",true);
			CMAble.addCharAbilityMapping(ID(),8,"Dance_Swing",true);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),9,"Dance_Basse",true);

			CMAble.addCharAbilityMapping(ID(),10,"Fighter_BodyFlip",true);
			CMAble.addCharAbilityMapping(ID(),10,"Dance_Tango",true);

			CMAble.addCharAbilityMapping(ID(),11,"Fighter_Spring",false);
			CMAble.addCharAbilityMapping(ID(),11,"Dance_Polka",true);

			CMAble.addCharAbilityMapping(ID(),12,"Dance_RagsSharqi",true);
			CMAble.addCharAbilityMapping(ID(),12,"Dance_Manipuri",true);

			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Dance_Cotillon",true);

			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),14,"Dance_Ballet",true);

			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Tumble",false);
			CMAble.addCharAbilityMapping(ID(),15,"Dance_Jitterbug",true);

			CMAble.addCharAbilityMapping(ID(),16,"Dance_Butoh",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Dance_Courante",true);

			CMAble.addCharAbilityMapping(ID(),18,"Dance_Musette",true);

			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Endurance",true);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Cartwheel",false);
			CMAble.addCharAbilityMapping(ID(),19,"Dance_Swords",true);

			CMAble.addCharAbilityMapping(ID(),20,"Dance_Flamenco",true);

			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Roll",false);
			CMAble.addCharAbilityMapping(ID(),21,"Dance_Jingledress",true);

			CMAble.addCharAbilityMapping(ID(),22,"Dance_Morris",true);

			CMAble.addCharAbilityMapping(ID(),23,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),23,"Dance_Butterfly",true);

			CMAble.addCharAbilityMapping(ID(),24,"Dance_Macabre",true);

			CMAble.addCharAbilityMapping(ID(),25,"Fighter_CircleTrip",false);
			CMAble.addCharAbilityMapping(ID(),25,"Dance_War",true);

			CMAble.addCharAbilityMapping(ID(),30,"Dance_Square",true);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Charisma 9+, Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Prancer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Prancer.");
			return false;
		}
		if((!(mob.charStats().getMyRace().ID().equals("Human")))
		&&(!(mob.charStats().getMyRace().ID().equals("Elf")))
		&&(!(mob.charStats().getMyRace().ID().equals("Halfling")))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Halfling, or Half Elf to be a Prancer");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherLimitations(){return "";}
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
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if((!Sense.isSleeping(affected))&&(!Sense.isSitting(affected)))
			{
				MOB mob=(MOB)affected;
				int attArmor=(((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1)*(mob.charStats().getClassLevel(this)-1);
				affectableStats.setArmor(affectableStats.armor()-attArmor);
			}
		}
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

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level.";}

	public void level(MOB mob)
	{
	    if(CMSecurity.isDisabled("LEVELS")) 
	        return;
		super.level(mob);
		int dexStat=mob.charStats().getStat(CharStats.DEXTERITY);
		int maxDexStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.DEXTERITY));
		if(dexStat>maxDexStat) dexStat=maxDexStat;
		
		int attArmor=((int)Math.round(Util.div(dexStat,9.0)))+1;
		if(dexStat>=25)attArmor+=2;
		else
		if(dexStat>=22)attArmor+=1;
		
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^NYour grace grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}

