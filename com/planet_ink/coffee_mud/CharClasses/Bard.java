package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Bard extends StdCharClass
{
	public String ID(){return "Bard";}
	public String name(){return "Bard";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 1;}
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

	public Bard()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Detection",true);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Haggle",true);
			CMAble.addCharAbilityMapping(ID(),2,"Song_Seeing",true);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),3,"Song_Valor",true);
			CMAble.addCharAbilityMapping(ID(),4,"Song_Charm",true);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Armor",true);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Babble",true);
			CMAble.addCharAbilityMapping(ID(),6,"Song_Clumsiness",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Song_Rage",true);
			CMAble.addCharAbilityMapping(ID(),8,"Song_Mute",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Peek",false);
			CMAble.addCharAbilityMapping(ID(),9,"Song_Serenity",true);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Revelation",true);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Friendship",true);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Inebriation",true);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Comprehension",true);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Health",true);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Mercy",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Map",true);
			CMAble.addCharAbilityMapping(ID(),13,"Song_Silence",true);
			CMAble.addCharAbilityMapping(ID(),14,"Song_Dexterity",true);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
			CMAble.addCharAbilityMapping(ID(),15,"Song_Protection",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_SongWrite",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),16,"Song_Mana",true);
			CMAble.addCharAbilityMapping(ID(),17,"Song_Quickness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Lethargy",true);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Flight",true);
			CMAble.addCharAbilityMapping(ID(),19,"Song_Knowledge",true);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
			CMAble.addCharAbilityMapping(ID(),20,"Song_Blasting",true);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Strength",true);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Thanks",true);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Lullibye",true);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Distraction",true);
			CMAble.addCharAbilityMapping(ID(),23,"Song_Flying",true);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Steal",false);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Death",true);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Disgust",true);
			CMAble.addCharAbilityMapping(ID(),25,"Song_Rebirth",true);
			CMAble.addCharAbilityMapping(ID(),30,"Song_Ode",true);
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
					if(mob2.charStats().getStat(CharStats.INTELLIGENCE)>3)
						theAmount+=1.0;
				}
			}
		}
		super.gainExperience(mob,victim,homage,(int)Math.round(theAmount),quiet);
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
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
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
