package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Bard extends StdCharClass
{
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
				if(A.classificationCode()==Ability.THIEF_SKILL)
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
