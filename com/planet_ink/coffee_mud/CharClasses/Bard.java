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
   Copyright 2000-2006 Bo Zimmerman

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
public class Bard extends StdCharClass
{
	public String ID(){return "Bard";}
	public String name(){return "Bard";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STAT_CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Bard()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Detection",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Song_Seeing",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Lore",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Song_Valor",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Song_Charm",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Song_Armor",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Song_Babble",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Song_Clumsiness",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Song_Rage",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Song_Mute",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Distract",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Peek",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Song_Serenity",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Revelation",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Friendship",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Unbinding",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Inebriation",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Comprehension",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Health",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Mercy",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Song_Silence",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Song_Dexterity",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Song_Protection",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_SongWrite",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Song_Mana",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Song_Quickness",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Lethargy",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Flight",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Song_Knowledge",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Song_Blasting",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Strength",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Thanks",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Song_Lullibye",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Song_Distraction",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Song_Flying",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Steal",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Death",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Disgust",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Song_Rebirth",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Song_Ode",true);
		}
	}

	public void gainExperience(MOB mob,
							   MOB victim,
							   String homage,
							   int amount,
							   boolean quiet)
	{
		double theAmount=new Integer(amount).doubleValue();
		if((mob!=null)&&(victim!=null)&&(theAmount>10.0))
		{
			HashSet H=mob.getGroupMembers(new HashSet());
			double origAmount=theAmount;
			for(Iterator e=H.iterator();e.hasNext();)
			{
				MOB mob2=(MOB)e.next();
				if((mob2!=mob)
				   &&(mob2!=victim)
				   &&(mob2.location()!=null)
				   &&(mob2.location()==mob.location()))
				{
					if(!mob2.isMonster())
						theAmount+=(origAmount/5.0);
					else
					if(mob2.charStats().getStat(CharStats.STAT_INTELLIGENCE)>3)
						theAmount+=1.0;
				}
			}
		}
		super.gainExperience(mob,victim,homage,(int)Math.round(theAmount),quiet);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			Vector V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProfficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(E instanceof MusicalInstrument)
			return true;
		return super.weaponCheck(mob,sourceCode,E);
	}
	public String statQualifications(){return "Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Bard.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "Receives bonus combat experience when in a group.";}
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
}
