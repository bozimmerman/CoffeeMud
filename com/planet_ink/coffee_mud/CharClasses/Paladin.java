package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.db.*;

public class Paladin extends StdCharClass
{
	public Paladin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.WISDOM]=22;
		bonusPracLevel=2;
		manaMultiplier=15;
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

	public boolean okAffect(Affect affect)
	{
		switch(affect.sourceCode())
		{
		case Affect.SOUND_MAGIC:
		case Affect.STRIKE_MAGIC:
			if(affect.source().getAlignment() < 650)
				if(Dice.rollPercentage()<affect.source().charStats().getWisdom()*2)
				{
					affect.source().location().show(affect.source(),null,Affect.VISUAL_ONLY,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
					return false;
				}
			break;
		default:
			break;
		}
		return super.okAffect(affect);
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength() <= 8)
			return false;

		if(mob.baseCharStats().getWisdom() <= 8)
			return false;

		if(!(mob.charStats().getMyRace() instanceof Human))
			return(false);

		return true;
	}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		giveMobAbility(mob,new Paladin_LayHands());
		giveMobAbility(mob,new Fighter_BlindFighting());
		giveMobAbility(mob,new Fighter_Rescue());
		giveMobAbility(mob,new Skill_Attack2());
		giveMobAbility(mob,new Skill_Attack3());
		giveMobAbility(mob,new Skill_Bash());
		giveMobAbility(mob,new Skill_Dirt());
		giveMobAbility(mob,new Skill_Disarm());
		giveMobAbility(mob,new Skill_Dodge());
		giveMobAbility(mob,new Skill_Parry());
		giveMobAbility(mob,new Skill_Trip());
		giveMobAbility(mob,new Cleric_Turn());
		giveMobAbility(mob,new Prayer_CureLight());
		giveMobAbility(mob,new Prayer_DetectEvil());
		if(!mob.isMonster())
		{
			Longsword s=new Longsword();
			s.wear(Item.WIELD);
			mob.addInventory(s);
		}
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
