package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Cleric extends StdCharClass
{
	public Cleric()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=16;
		maxStat[CharStats.WISDOM]=25;
		bonusPracLevel=2;
		manaMultiplier=15;
		attackAttribute=CharStats.WISDOM;
		bonusAttackLevel=1;
		damageBonusPerLevel=0;
		name=myID;
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getWisdom()<=8)
			return false;
		return true;
	}


	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		{
			Item I=myChar.fetchWieldedItem();
			if((I!=null)&&(I instanceof Weapon))
			{
				int classification=((Weapon)I).weaponClassification();
				if(myChar.getAlignment()<350)
				{
					if((classification==Weapon.CLASS_POLEARM)
					||(classification==Weapon.CLASS_SWORD)
					||(classification==Weapon.CLASS_AXE)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_EDGED))
						return true;
				}
				else
				if(myChar.getAlignment()<650)
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_RANGED)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_SWORD))
						return true;
				}
				else
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_FLAILED)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_HAMMER))
						return true;
				}
				if(Dice.rollPercentage()>myChar.charStats().getWisdom()*4)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"During a conflict of <S-HIS-HER> conscience, <S-NAME> fumble(s) horribly with "+I.name()+".");
					return false;
				}
			}
		}
		return true;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Cleric_Turn"), isBorrowedClass);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(A.classificationCode()==Ability.PRAYER))
				giveMobAbility(mob,A, isBorrowedClass);
		}
		if(!mob.isMonster())
			outfit(mob);
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public void level(MOB mob)
	{
		super.level(mob);
	}
}
