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
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 4;}
	private static boolean abilitiesLoaded=false;
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	
	public Bard()
	{
		super();
		maxStat[CharStats.CHARISMA]=25;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Edged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Detection",true);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
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
			CMAble.addCharAbilityMapping(ID(),13,"Song_Silence",true);
			CMAble.addCharAbilityMapping(ID(),14,"Song_Dexterity",true);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
			CMAble.addCharAbilityMapping(ID(),15,"Song_Protection",true);
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

		}
	}
	
	public void gainExperience(MOB mob, 
							   MOB victim, 
							   String homage, 
							   int amount)
	{
		double theAmount=new Integer(amount).doubleValue();
		if((mob!=null)&&(victim!=null)&&(theAmount>10.0))
		{
			Hashtable H=mob.getGroupMembers(new Hashtable());
			double origAmount=theAmount;
			for(Enumeration e=H.elements();e.hasMoreElements();)
			{
				MOB mob2=(MOB)e.nextElement();
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
		super.gainExperience(mob,victim,homage,(int)Math.round(theAmount));
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
				mob.tell("You need at least a 9 Charisma to become a Bard.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Bard.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human or Half Elf to be a Bard");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear non-metal armor to avoid skill failure.";}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "Receives bonus combat experience when in a group.";}
	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(((affect.sourceMajor()&Affect.MASK_DELICATE)>0)
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) in <S-HIS-HER> maneuver!");
					return false;
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				switch(classification)
				{
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_DAGGER:
					break;
				default:
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
					break;
				}
			}
		}
		return super.okAffect(myChar,affect);
	}
}
