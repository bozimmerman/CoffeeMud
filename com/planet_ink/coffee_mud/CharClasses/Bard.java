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
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Detection",false);
			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",true);
			CMAble.addCharAbilityMapping(ID(),"Song_Seeing",2,false);
			CMAble.addCharAbilityMapping(ID(),"Skill_Climb",3,false);
			CMAble.addCharAbilityMapping(ID(),"Skill_WandUse",3,false);
			CMAble.addCharAbilityMapping(ID(),"Thief_Hide",3,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Valor",3,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Charm",4,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Armor",5,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Clumsiness",6,false);
			CMAble.addCharAbilityMapping(ID(),"Skill_Dodge",7,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Rage",7,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Mute",8,false);
			CMAble.addCharAbilityMapping(ID(),"Skill_Trip",9,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Serenity",9,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Revelation",10,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Inebriation",11,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Health",12,false);
			CMAble.addCharAbilityMapping(ID(),"Thief_Peek",13,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Silence",13,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Dexterity",14,false);
			CMAble.addCharAbilityMapping(ID(),"Thief_DetectTraps",15,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Protection",15,false);
			CMAble.addCharAbilityMapping(ID(),"Spell_ReadMagic",16,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Mana",16,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Quickness",17,false);
			CMAble.addCharAbilityMapping(ID(),"Skill_Attack2",17,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Lethargy",18,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Friendship",19,false);
			CMAble.addCharAbilityMapping(ID(),"Thief_Swipe",19,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Blasting",20,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Strength",21,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Lullibye",22,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Flying",23,false);
			CMAble.addCharAbilityMapping(ID(),"Thief_Steal",23,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Death",24,false);
			CMAble.addCharAbilityMapping(ID(),"Song_Rebirth",25,false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getCharisma() <= 8)
			return false;
		if(mob.baseCharStats().getDexterity() <= 8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
			return(false);

		return true;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);

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
			if(A.qualifyingLevel(mob)>=0)
			{
				if(CMAble.getDefaultGain(ID(),A.ID()))
					giveMobAbility(mob,A, isBorrowedClass);
				else
				if(A.classificationCode()==Ability.SONG)
				{
					if((A.qualifyingLevel(mob)<5)&&(A.qualifyingLevel(mob)>=1))
						giveMobAbility(mob,A, isBorrowedClass);
					else
					if(extras.get(new Integer(A.qualifyingLevel(mob)))!=null)
						giveMobAbility(mob,A, isBorrowedClass);
				}
			}
		}
		if(!mob.isMonster())
			outfit(mob);
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
		if(!new Thief().okAffect(myChar, affect))
			return false;
		return super.okAffect(myChar, affect);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
