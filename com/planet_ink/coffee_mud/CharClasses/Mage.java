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

public class Mage extends StdCharClass
{
	public Mage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=8;
		maxStat[CharStats.INTELLIGENCE]=25;
		bonusPracLevel=6;
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
		if(!(mob.charStats().getMyRace() instanceof Human) && !(mob.charStats().getMyRace() instanceof Elf) && !(mob.charStats().getMyRace() instanceof HalfElf))
			return(false);
		return true;
	}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		mob.addAbility(new Spell_ReadMagic());
		mob.addAbility(new Skill_Revoke());
		mob.addAbility(new Spell_Shield());
		mob.addAbility(new Spell_MagicMissile());
		mob.addAbility(new Skill_Write());

		int numTotal=0;
		for(int a=0;a<MUD.abilities.size();a++)
		{
			Ability A=(Ability)MUD.abilities.elementAt(a);
			if(A.qualifyingLevel(mob)>0)
				numTotal++;
		}
		for(int level=2;level<19;level++)
		{
			int numSpells=(int)Math.floor(Util.div(26-level,8));
			int numLevel=0;
			while(numLevel<numSpells)
			{
				int randSpell=(int)Math.round(Math.random()*numTotal);
				for(int a=0;a<MUD.abilities.size();a++)
				{
					Ability A=(Ability)MUD.abilities.elementAt(a);
					if(A.qualifyingLevel(mob)>0)
					{
						if(randSpell==0)
						{
							if((A.qualifyingLevel(mob)==level)&&(mob.fetchAbility(A.ID())==null))
							{
								mob.addAbility((Ability)A.copyOf());
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
			for(int a=0;a<MUD.abilities.size();a++)
			{
				Ability A=(Ability)MUD.abilities.elementAt(a);
				if(A.qualifyingLevel(mob)>0)
				{
					if(randSpell==0)
					{
						if((A.qualifyingLevel(mob)>18)&&(mob.fetchAbility(A.ID())==null))
						{
							mob.addAbility((Ability)A.copyOf());
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
		{
			Quarterstaff s=new Quarterstaff();
			s.wear(Item.WIELD);
			mob.addInventory(s);
		}
	}

	public boolean okAffect(Affect affect)
	{
		switch(affect.sourceCode())
		{
		case Affect.SOUND_MAGIC:
		case Affect.STRIKE_MAGIC:
			for(int i=0;i<affect.source().inventorySize();i++)
			{
				Item I=affect.source().fetchInventory(i);
				if((I.amWearingAt(Item.ON_TORSO))
				 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield))
				 ||(I.amWearingAt(Item.ON_LEGS))
				 ||(I.amWearingAt(Item.ON_ARMS))
				 ||(I.amWearingAt(Item.ON_WAIST))
				 ||(I.amWearingAt(Item.ON_HEAD)))
					if((I instanceof Armor)&&(((Armor)I).material()!=Armor.CLOTH))
						if(Dice.rollPercentage()<affect.source().charStats().getIntelligence()*2)
						{
							affect.source().location().show(affect.source(),null,Affect.VISUAL_ONLY,"<S-NAME> watch(es) <S-HIS-HER> armor absorb(s) <S-HIS-HER> magical energy!");
							return false;
						}
			}
			break;
		case Affect.STRIKE_HANDS:
			Item I=affect.source().fetchWieldedItem();
			if((I!=null)&&(I instanceof Weapon))
			{
				int classification=((Weapon)I).weaponClassification;
				if(!((classification==Weapon.CLASS_NATURAL)
				||(I instanceof Quarterstaff)
				||(I instanceof Dagger))
				   )
					if(Dice.rollPercentage()<affect.source().charStats().getIntelligence()*2)
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

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
