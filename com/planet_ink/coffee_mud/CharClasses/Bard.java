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

public class Bard extends StdCharClass
{
	public Bard()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=18;
		maxStat[CharStats.CHARISMA]=25;
		bonusPracLevel=2;
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
		if(!(mob.charStats().getMyRace() instanceof Human) && !(mob.charStats().getMyRace() instanceof HalfElf))
			return(false);
			
		return true;
	}
	
	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		
		
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
		
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if(A.qualifyingLevel(mob)>=0)
			{
				if(A instanceof ThiefSkill)
					giveMobAbility(mob,A);
				else
				if(A instanceof Song)
				{
					if((A.qualifyingLevel(mob)<5)&&(A.qualifyingLevel(mob)>=1))
						giveMobAbility(mob,A);
					else
					if(extras.get(new Integer(A.qualifyingLevel(mob)))!=null)
						giveMobAbility(mob,A);
				}
			}
		}
		if(!mob.isMonster())
		{
			Shortsword s=new Shortsword();
			s.wear(Item.WIELD);
			mob.addInventory(s);
		}
	}
	
	
	public boolean okAffect(Affect affect)
	{
		if(!new Thief().okAffect(affect))
			return false;
		return super.okAffect(affect);
	}
	
	public void level(MOB mob)
	{
		super.level(mob);
	}
}
