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

public class Thief extends StdCharClass
{
	public Thief()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=16;
		maxStat[CharStats.DEXTERITY]=25;
		bonusPracLevel=2;
		manaMultiplier=12;
		attackAttribute=CharStats.DEXTERITY;
		bonusAttackLevel=2;
		damageBonusPerLevel=0;
		name=myID;
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getDexterity()>8)
			return true;
		return false;
	}

	public void newCharacter(MOB mob)
	{
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(A instanceof ThiefSkill))
				giveMobAbility(mob,A);
		}
		giveMobAbility(mob,new Skill_Climb());
		if(!mob.isMonster())
		{
			Shortsword s=new Shortsword();
			s.wear(Item.WIELD);
			mob.addInventory(s);
		}
		super.newCharacter(mob);
	}

	public boolean okAffect(Affect affect)
	{
		switch(affect.sourceCode())
		{
		case Affect.HANDS_UNLOCK:
		case Affect.HANDS_DELICATE:
			for(int i=0;i<affect.source().inventorySize();i++)
			{
				Item I=affect.source().fetchInventory(i);
				if((I.amWearingAt(Item.ON_TORSO))
				 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield))
				 ||(I.amWearingAt(Item.ON_LEGS))
				 ||(I.amWearingAt(Item.ON_ARMS))
				 ||(I.amWearingAt(Item.ON_WAIST))
				 ||(I.amWearingAt(Item.ON_HEAD)))
					if((I instanceof Armor)&&(((Armor)I).material()!=Armor.CLOTH)&&(((Armor)I).material()!=Armor.LEATHER))
						if(Dice.rollPercentage()<(affect.source().charStats().getDexterity()*2))
						{
							affect.source().location().show(affect.source(),null,Affect.VISUAL_WNOISE,"<S-NAME> fumble(s) in <S-HIS-HER> maneuver!");
							return false;
						}
			}
			break;
		case Affect.STRIKE_HANDS:
			Item I=affect.source().fetchWieldedItem();
			if((I!=null)&&(I instanceof Weapon))
			{
				int classification=((Weapon)I).weaponClassification;
				if(!((classification==Weapon.CLASS_SWORD)
				||(classification==Weapon.CLASS_RANGED)
				||(I instanceof Natural)
				||(classification==Weapon.CLASS_NATURAL)
				||(I instanceof Dagger))
				   )
					if(Dice.rollPercentage()<(affect.source().charStats().getDexterity()*2))
					{
						affect.source().location().show(affect.source(),null,Affect.VISUAL_WNOISE,"<S-NAME> fumble(s) horribly with "+I.name()+".");
						return false;
					}
			}
			break;
		default:
			break;
		}
		return super.okAffect(affect);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);
		
		int attArmor=((int)Math.round(Util.div(mob.charStats().getDexterity(),9.0)))+1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		
		mob.recoverEnvStats();
		mob.recoverCharStats();
	}
	
	public void level(MOB mob)
	{
		super.level(mob);
		int attArmor=((int)Math.round(Util.div(mob.charStats().getDexterity(),9.0)))+1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("Your stealthiness grants you a defensive bonus of "+attArmor+".");
	}
}
