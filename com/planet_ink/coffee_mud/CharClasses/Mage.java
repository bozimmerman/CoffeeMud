package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Mage extends StdCharClass
{
	public Mage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=8;
		maxStat[CharStats.INTELLIGENCE]=25;
		bonusPracLevel=4;
		manaMultiplier=20;
		attackAttribute=CharStats.INTELLIGENCE;
		bonusAttackLevel=0;
		name=myID;
		practicesAtFirstLevel=6;
		damageBonusPerLevel=0;
		trainsAtFirstLevel=3;
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getIntelligence()<=8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
			return(false);
		return true;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_WandUse"),isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Spell_ReadMagic"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Revoke"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Spell_Shield"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Spell_MagicMissile"), isBorrowedClass);
		giveMobAbility(mob,CMClass.getAbility("Skill_Write"), isBorrowedClass);

		int numTotal=0;
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(A.classificationCode()==Ability.SPELL))
				numTotal++;
		}
		Hashtable given=new Hashtable();
		for(int level=2;level<19;level++)
		{
			int numSpells=(int)Math.floor(Util.div(26-level,8));
			int numLevel=0;
			while(numLevel<numSpells)
			{
				int randSpell=(int)Math.round(Math.random()*numTotal);
				for(int a=0;a<CMClass.abilities.size();a++)
				{
					Ability A=(Ability)CMClass.abilities.elementAt(a);
					if((A.qualifyingLevel(mob)>0)&&(A.classificationCode()==Ability.SPELL))
					{
						if(randSpell==0)
						{
							if((A.qualifyingLevel(mob)==level)&&(given.get(A.ID())==null))
							{
								giveMobAbility(mob,A,isBorrowedClass);
								given.put(A.ID(),A);
								numLevel++;
							}
							break;
						}
						else
							randSpell--;
					}
				}
			}
		}
		int numLevel=0;
		while(numLevel<2)
		{
			int randSpell=(int)Math.round(Math.random()*numTotal);
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				if((A.qualifyingLevel(mob)>0)&&(A.classificationCode()==Ability.SPELL))
				{
					if(randSpell==0)
					{
						if((A.qualifyingLevel(mob)>18)&&(given.get(A.ID())==null))
						{
							giveMobAbility(mob,A,isBorrowedClass);
							given.put(A.ID(),A);
							numLevel++;
						}
						break;
					}
					else
						randSpell--;
				}
			}
		}


		if(!mob.isMonster())
			outfit(mob);
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Quarterstaff");
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
			if(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if((I.amWearingAt(Item.ON_TORSO))
					 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield))
					 ||(I.amWearingAt(Item.ON_LEGS))
					 ||(I.amWearingAt(Item.ON_ARMS))
					 ||(I.amWearingAt(Item.ON_WAIST))
					 ||(I.amWearingAt(Item.ON_HEAD)))
						if((I instanceof Armor)&&(((Armor)I).material()!=Armor.CLOTH))
							if(Dice.rollPercentage()>myChar.charStats().getIntelligence()*4)
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
								return false;
							}
				}
			}
			else
			if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			{
				Item I=myChar.fetchWieldedItem();
				if((I!=null)&&(I instanceof Weapon))
				{
					int classification=((Weapon)I).weaponClassification();
					if(!((classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_DAGGER)
					||(classification==Weapon.CLASS_STAFF))
					   )
						if(Dice.rollPercentage()>myChar.charStats().getIntelligence()*4)
						{
							myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+I.name()+".");
							return false;
						}
				}
			}
		}
		return super.okAffect(myChar,affect);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
