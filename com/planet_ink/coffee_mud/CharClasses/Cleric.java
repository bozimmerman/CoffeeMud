package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.db.*;

public class Cleric extends StdCharClass
{
	public Cleric()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=16;
		maxStat[CharStats.WISDOM]=25;
		bonusPracLevel=4;
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


	public boolean okAffect(Affect affect)
	{
		switch(affect.sourceCode())
		{
		case Affect.STRIKE_HANDS:
			Item I=affect.source().fetchWieldedItem();
			if((I!=null)&&(I instanceof Weapon))
			{
				int classification=((Weapon)I).weaponClassification;
				if(affect.source().getAlignment()<350)
				{
					if((classification==Weapon.CLASS_POLEARM)
					||(classification==Weapon.CLASS_SWORD)
					||(classification==Weapon.CLASS_AXE)
					||(classification==Weapon.CLASS_EDGED))
						break;
				}
				else
				if(affect.source().getAlignment()<650)
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_RANGED)
					||(classification==Weapon.CLASS_SWORD))
						break;
				}
				else
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_FLAILED)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_HAMMER))
						break;
				}
				if(Dice.rollPercentage()<affect.source().charStats().getWisdom()*2)
				{
					affect.source().location().show(affect.source(),null,Affect.VISUAL_WNOISE,"During a conflict of <S-HIS-HER> conscience, <S-NAME> fumble(s) horribly with "+I.name()+".");
					return false;
				}
			}
			break;
		default:
			break;
		}
		return super.okAffect(affect);
	}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		giveMobAbility(mob,new Cleric_Turn());
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(A instanceof Prayer))
				giveMobAbility(mob,A);
		}
		if(!mob.isMonster())
		{
			Mace s=new Mace();
			s.wear(Item.WIELD);
			mob.addInventory(s);
			Random randomizer = new Random(System.currentTimeMillis());
			int money = 0;
			for (int i = 0; i < 3; i++)
				money += Math.abs(randomizer.nextInt() % 6) + 1;
			money *= 10;
			mob.setMoney(money);
		}
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
