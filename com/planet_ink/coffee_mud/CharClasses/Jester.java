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
public class Jester extends StdCharClass
{
	public String ID(){return "Jester";}
	public String name(){return "Jester";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 4;}
	public int getBonusAttackLevel(){return 1;}
	public int getMovementMultiplier(){return 16;}
	public int getAttackAttribute(){return CharStats.CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Jester()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=4;
		maxStatAdj[CharStats.DEXTERITY]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Alchemy",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Juggle",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_BellyRolling",true);

			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Climsiness",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Haggle",true);

			CMAble.addCharAbilityMapping(ID(),2,"Skill_IdentifyPoison",true);
			CMAble.addCharAbilityMapping(ID(),2,"Song_Inebriation",false);

			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",false);

			CMAble.addCharAbilityMapping(ID(),4,"Skill_Slapstick",true);
			CMAble.addCharAbilityMapping(ID(),4,"Song_Babble",false);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Mimicry",true);

			CMAble.addCharAbilityMapping(ID(),6,"Skill_EscapeBonds",true);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_MinorTrap",false);
			CMAble.addCharAbilityMapping(ID(),6,"Song_Detection",false);

			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);

			CMAble.addCharAbilityMapping(ID(),8,"Thief_UsePoison",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),8,"Song_Rage",false);

			CMAble.addCharAbilityMapping(ID(),9,"Thief_Peek",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_FireBreathing",false);

			CMAble.addCharAbilityMapping(ID(),10,"Skill_Joke",true);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Distraction",false);

			CMAble.addCharAbilityMapping(ID(),11,"Thief_Bind",false);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Lightness",false);

			CMAble.addCharAbilityMapping(ID(),12,"Skill_SlowFall",true);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Seeing",false);

			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);

			CMAble.addCharAbilityMapping(ID(),14,"Dance_Stop",100,true);
			CMAble.addCharAbilityMapping(ID(),14,"Dance_Clog",true);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_CriticalShot",false);
			CMAble.addCharAbilityMapping(ID(),14,"Song_Mercy",false);

			CMAble.addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);

			CMAble.addCharAbilityMapping(ID(),16,"Skill_Stability",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),16,"Song_Charm",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);

			CMAble.addCharAbilityMapping(ID(),18,"Fighter_Tumble",true);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Thanks",false);

			CMAble.addCharAbilityMapping(ID(),19,"Thief_Swipe",false);

			CMAble.addCharAbilityMapping(ID(),20,"Thief_AvoidTraps",true);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_CritStrike",false);
			CMAble.addCharAbilityMapping(ID(),20,"Song_Mute",false);

			CMAble.addCharAbilityMapping(ID(),21,"Thief_Steal",false);

			CMAble.addCharAbilityMapping(ID(),22,"Skill_Feint",true);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Quickness",false);

			CMAble.addCharAbilityMapping(ID(),23,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),23,"Song_SingleMindedness",false);

			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Cartwheel",true);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Disgust",false);

			CMAble.addCharAbilityMapping(ID(),25,"Skill_Puppeteer",true);
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_Roll",false);

			CMAble.addCharAbilityMapping(ID(),30,"Skill_Buffoonery",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Charisma 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Jester.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Jester.");
			return false;
		}
		if((!(mob.charStats().getMyRace().ID().equals("Human")))
		&&(!(mob.charStats().getMyRace().ID().equals("Gnome")))
		&&(!(mob.charStats().getMyRace().ID().equals("Halfling")))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human, Gnome, Halfling, or Half Elf to be a Jester");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,
			affectableStats.getStat(CharStats.SAVE_POISON)
			+(affectableStats.getClassLevel(this)*2));
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.isMonster())
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

	public String otherLimitations(){return "";}
	public String otherBonuses(){return "Receives 2%/level bonus to saves versus poison.";}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
