package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Paladin extends StdCharClass
{
	public Paladin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.WISDOM]=22;
		bonusPracLevel=0;
		manaMultiplier=10;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=2;
		name=myID;
		practicesAtFirstLevel=3;
		trainsAtFirstLevel=4;
		damageBonusPerLevel=1;
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar))
		if(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			if(myChar.getAlignment() < 650)
				if(Dice.rollPercentage()>myChar.charStats().getWisdom()*4)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
					return false;
				}
		return super.okAffect(myChar, affect);
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength() <= 8)
			return false;

		if(mob.baseCharStats().getWisdom() <= 8)
			return false;

		if(!(mob.charStats().getMyRace().ID().equals("Human")))
			return(false);

		return true;
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
	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Paladin_LayHands"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Fighter_BlindFighting"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Fighter_Rescue"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Attack2"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Attack3"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Bash"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Dirt"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Disarm"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Dodge"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Parry"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Trip"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Cleric_Turn"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Prayer_CureLight"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Prayer_DetectEvil"), isBorrowedClass);
		if(!mob.isMonster())
			outfit(mob);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
