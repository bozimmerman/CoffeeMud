package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Ranger extends StdCharClass
{
	public Ranger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.DEXTERITY]=22;
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

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength()<=8)
			return false;

		if(mob.baseCharStats().getIntelligence()<=8)
			return false;

		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
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
		giveMobAbility(mob,CMClass.getAbility("Ranger_Track"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Fighter_BlindFighting"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Fighter_Rescue"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Attack2"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Attack3"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Bash"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Disarm"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Dirt"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Dodge"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Parry"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Trip"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Spell_ReadMagic"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Spell_Light"), isBorrowedClass);

		if(!mob.isMonster())
			outfit(mob);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
