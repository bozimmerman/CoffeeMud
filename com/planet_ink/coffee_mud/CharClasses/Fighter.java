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

public class Fighter extends StdCharClass
{
	public Fighter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=24;
		maxStat[CharStats.STRENGTH]=25;
		bonusPracLevel=-1;
		manaMultiplier=8;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=3;
		name=myID;
		practicesAtFirstLevel=3;
		trainsAtFirstLevel=4;
		damageBonusPerLevel=2;
	}
	
	public boolean playerSelectable()
	{
		return true;
	}
	
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength()>8)
			return true;
		return false;
	}
	
	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)
			   &&(!(A instanceof Skill_Write))
			   &&(!(A instanceof Skill_Climb)))
				giveMobAbility(mob,A);
		}
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
