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

public class Ranger extends StdCharClass
{
	public Ranger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.DEXTERITY]=22;
		bonusPracLevel=2;
		manaMultiplier=10;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=3;
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

		if(!(mob.charStats().getMyRace() instanceof Human) && !(mob.charStats().getMyRace() instanceof Elf) && !(mob.charStats().getMyRace() instanceof HalfElf))
			return(false);
			

		return true;
	}
	
	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		giveMobAbility(mob,new Ranger_Track());
		giveMobAbility(mob,new Fighter_BlindFighting());
		giveMobAbility(mob,new Fighter_Rescue());
		giveMobAbility(mob,new Skill_Attack2());
		giveMobAbility(mob,new Skill_Attack3());
		giveMobAbility(mob,new Skill_Bash());
		giveMobAbility(mob,new Skill_Disarm());
		giveMobAbility(mob,new Skill_Dirt());
		giveMobAbility(mob,new Skill_Dodge());
		giveMobAbility(mob,new Skill_Parry());
		giveMobAbility(mob,new Skill_Trip());
		giveMobAbility(mob,new Spell_ReadMagic());
		giveMobAbility(mob,new Spell_Light());
		
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
