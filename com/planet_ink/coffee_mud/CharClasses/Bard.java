package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Bard extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Bard()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=18;
		maxStat[CharStats.CHARISMA]=25;
		bonusPracLevel=1;
		manaMultiplier=8;
		attackAttribute=CharStats.DEXTERITY;
		damageBonusPerLevel=0;
		bonusAttackLevel=1;
		name=myID;
		
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Detection",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
			CMAble.addCharAbilityMapping(ID(),2,"Song_Seeing",false);
			CMAble.addCharAbilityMapping(ID(),2,"Skill_Lore",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),3,"Song_Valor",false);
			CMAble.addCharAbilityMapping(ID(),4,"Song_Charm",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Appraise",true);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Armor",false);
			CMAble.addCharAbilityMapping(ID(),5,"Song_Babble",false);
			CMAble.addCharAbilityMapping(ID(),6,"Song_Clumsiness",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Song_Rage",false);
			CMAble.addCharAbilityMapping(ID(),8,"Song_Mute",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Distract",true);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Peek",false);
			CMAble.addCharAbilityMapping(ID(),9,"Song_Serenity",false);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Revelation",false);
			CMAble.addCharAbilityMapping(ID(),10,"Song_Friendship",false);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Inebriation",false);
			CMAble.addCharAbilityMapping(ID(),11,"Song_Comprehension",false);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Health",false);
			CMAble.addCharAbilityMapping(ID(),12,"Song_Mercy",false);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Song_Silence",false);
			CMAble.addCharAbilityMapping(ID(),14,"Song_Dexterity",false);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
			CMAble.addCharAbilityMapping(ID(),15,"Song_Protection",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),16,"Song_Mana",false);
			CMAble.addCharAbilityMapping(ID(),17,"Song_Quickness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Lethargy",false);
			CMAble.addCharAbilityMapping(ID(),18,"Song_Flight",false);
			CMAble.addCharAbilityMapping(ID(),19,"Song_Knowledge",false);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
			CMAble.addCharAbilityMapping(ID(),20,"Song_Blasting",false);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Strength",false);
			CMAble.addCharAbilityMapping(ID(),21,"Song_Thanks",false);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Lullibye",false);
			CMAble.addCharAbilityMapping(ID(),22,"Song_Distraction",false);
			CMAble.addCharAbilityMapping(ID(),23,"Song_Flying",false);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Steal",false);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Death",false);
			CMAble.addCharAbilityMapping(ID(),24,"Song_Disgust",false);
			CMAble.addCharAbilityMapping(ID(),25,"Song_Rebirth",false);

		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Charisma 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
			return false;
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY) <= 8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
			return(false);

		return true;
	}
	public String weaponLimitations(){return new Thief().weaponLimitations();}
	public String armorLimitations(){return new Thief().armorLimitations();}
	public String otherLimitations(){return new Thief().otherLimitations();}

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		super.startCharacter(mob, isBorrowedClass, verifyOnly);
		if(!verifyOnly)
		{
			Hashtable extras=new Hashtable();
			int q=-1;
			for(int r=0;r<7;r++)
			{
				q=-1;
				while(q<5)
				{
					q=(int)Math.round(Math.floor(Math.random()*21.0))+5;
					if(extras.get(new Integer(q))==null)
						extras.put(new Integer(q), new Integer(q));
					else
						q=-1;
				}
			}

		
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				if((A.qualifyingLevel(mob)>=0)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG))
				{
					if((A.qualifyingLevel(mob)<5)&&(A.qualifyingLevel(mob)>=1))
						giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
					else
					if(extras.get(new Integer(A.qualifyingLevel(mob)))!=null)
						giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
				}
			}
		}
	}


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
		if(!Thief.thiefOk(myChar,affect))
			return false;
		return super.okAffect(myChar, affect);
	}

}
