package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Barbarian extends StdCharClass
{
	public String ID(){return "Barbarian";}
	public String name(){return "Barbarian";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 26;}
	public int getBonusPracLevel(){return -1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getMovementMultiplier(){return 8;}
	private static boolean abilitiesLoaded=false;
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	
	public Barbarian()
	{
		super();
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.CONSTITUTION]=22;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Fighter_Charge",true);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			CMAble.addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),6,"Fighter_Battlecry",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_Berzerk",true);
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true); 
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_Spring",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_Warcry",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_WeaponBreak",false);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Sweep",false);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Rallycry",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_MountedCombat",false);
			CMAble.addCharAbilityMapping(ID(),18,"Fighter_Endurance",true);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Roll",false);
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_StoneBody",true);
			CMAble.addCharAbilityMapping(ID(),30,"Fighter_Shrug",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Strength 9+, Constitution 9+";}
	public String otherBonuses(){return "Damage reduction 1pt/5 levels.  A 1%/level resistance to Enchantments.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Barbarian.");
			return false;
		}

		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Barbarian.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String weaponLimitations(){return "";}
	public String armorLimitations(){return "Must use non-metal armors to avoid skill failure.";}
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if((affect.amITarget(myChar))
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Weapon)
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
		{
			int recovery=(affect.targetCode()-Affect.MASK_HURT)-(myChar.charStats().getClassLevel(this)/5);
			if(recovery<=0) recovery=0;
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		else
		if((affect.amITarget(myChar))
		   &&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Ability)
		   &&((((Ability)affect.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
		{
			if(Dice.rollPercentage()<=myChar.charStats().getClassLevel(this))
			{
				myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> resist(s) the "+affect.tool().name()+" attack from "+affect.source().name()+"!");
				return false;
			}
		}
		else
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(myChar.isMine(affect.tool()))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.CONSTITUTION)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> fumble(s) <S-HIS-HER> "+affect.tool().name()+" attempt due to <S-HIS-HER> armor!");
					return false;
				}
			}
		}
		return super.okAffect(myChar,affect);
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
}
