package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GenCharClass extends StdCharClass
{
	protected String ID="GenCharClass";
	protected String name="genmob";
	protected String baseClass="Commoner";
	protected int minHitPointsLevel=2;
	protected int maxHitPointsLevel=12;
	protected int bonusPracLevel=0;
	protected int bonusManaLevel=15;
	protected int bonusAttackLevel=1;
	protected int attackAttribute=CharStats.STRENGTH;
	protected int pracsFirstLevel=5;
	protected int trainsFirstLevel=3;
	protected int levelsPerBonusDamage=1;
	protected int movementMultiplier=5;
	protected int allowedArmorLevel=CharClass.ARMOR_ANY;
	protected String weaponLimitations="";
	protected String armorLimitations="";
	protected String otherLimitations="";
	protected String otherBonuses="";
	protected String qualifications="";
	protected boolean playerSelectable=false;
	protected HashSet disallowedWeapons=null; // set of Integers for weapon classes
	
	public String ID(){return ID;}
	public String name(){return name;}
	public String baseClass(){return baseClass;}
	public int getMinHitPointsLevel(){return minHitPointsLevel;}
	public int getMaxHitPointsLevel(){return maxHitPointsLevel;}
	public int getBonusPracLevel(){return bonusPracLevel;}
	public int getBonusManaLevel(){return bonusManaLevel;}
	public int getBonusAttackLevel(){return bonusAttackLevel;}
	public int getAttackAttribute(){return attackAttribute;}
	public int getPracsFirstLevel(){return pracsFirstLevel;}
	public int getTrainsFirstLevel(){return trainsFirstLevel;}
	public int getLevelsPerBonusDamage(){ return levelsPerBonusDamage;}
	public int getMovementMultiplier(){return movementMultiplier;}
	public int allowedArmorLevel(){return allowedArmorLevel;}
	public String weaponLimitations(){return weaponLimitations;}
	public String armorLimitations(){return armorLimitations;}
	public String otherLimitations(){return otherLimitations;}
	public String otherBonuses(){return otherBonuses;}
	public boolean playerSelectable(){	return playerSelectable;}
	
	//protected int maxStatAdj[]={0,0,0,0,0,0};  from stdcharclass -- but don't forget them!
	//protected Vector outfitChoices=null; from stdcharclass -- but don't forget them!


	public void cloneFix(CharClass C)
	{
	}

	public CharClass copyOf()
	{
		try
		{
			GenCharClass E=(GenCharClass)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public boolean loaded(){return true;}
	public void setLoaded(boolean truefalse){};

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) 
			return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(!myChar.isMonster()))
		{
			if((allowedArmorLevel()!=CharClass.ARMOR_ANY)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(CMAble.getQualifyingLevel(ID(),msg.tool().ID())>0)
			&&(myChar.isMine(msg.tool()))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(getAttackAttribute())*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) <S-HIS-HER> "+msg.tool().name()+" attempt due to <S-HIS-HER> armor!");
					return false;
				}
			}
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(disallowedWeapons!=null)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				if(((disallowedWeapons.contains(new Integer(((Weapon)msg.tool()).weaponClassification())))
				&&(myChar.fetchWieldedItem()!=null)))
				{
					if(Dice.rollPercentage()>myChar.charStats().getStat(getAttackAttribute())*2)
					{
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
				}
			}
		}
		return super.okMessage(myChar,msg);
	}
	
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!super.qualifiesForThisClass(mob,quiet))
			return false;
		if((!mob.isMonster())&&(mob.baseEnvStats().level()>0))
		{
			if(!MUDZapper.zapperCheck(qualifications,mob))
			{
				if(!quiet)
					mob.tell("You must meet the following qualifications to be a "+name()+":\n"+statQualifications());
				return false;
			}
		}
		return true;
	}
	public String statQualifications(){return MUDZapper.zapperDesc(qualifications);}

	public int compareTo(Object o){ 
		return (((CharClass)o).ID().compareTo(ID()));
	}
}
