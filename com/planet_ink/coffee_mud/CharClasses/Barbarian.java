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
	public int getMinHitPointsLevel(){return 3;}
	public int getMaxHitPointsLevel(){return 25;}
	public int getBonusPracLevel(){return -1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getMovementMultiplier(){return 8;}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	public Barbarian()
	{
		super();
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.CONSTITUTION]=4;
		if(!loaded())
		{
			setLoaded(true);
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
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

			CMAble.addCharAbilityMapping(ID(),1,"Fighter_Charge",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_SmokeSignals",true);
			CMAble.addCharAbilityMapping(ID(),5,"Scalp",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),6,"Fighter_Battlecry",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Disarm",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_Berzerk",false);
			CMAble.addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			
			CMAble.addCharAbilityMapping(ID(),10,"Fighter_Spring",false);
			CMAble.addCharAbilityMapping(ID(),10,"Apothecary",0,"ANTIDOTES",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),11,"Fighter_JungleTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_Intimidate",false);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_SwampTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_Warcry",true);
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_DesertTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_ImprovedThrowing",false);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_MountainTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_WeaponBreak",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Sweep",false);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Rallycry",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Skill_MountedCombat",true);
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_HillsTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Fighter_Endurance",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Skill_IdentifyPoison",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Roll",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Fighter_ForestTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Fighter_BullRush",true);
			
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Fragmentation",false);
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_PlainsTactics",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_Stonebody",true);
			
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;

		if((msg.amITarget(myChar))
		   &&(msg.tool()!=null)
		   &&(msg.tool() instanceof Weapon)
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			int recovery=(myChar.charStats().getClassLevel(this)/5);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		   &&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		   &&(msg.tool()!=null)
		   &&(msg.tool() instanceof Ability)
		   &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
		{
			if(Dice.rollPercentage()<=myChar.charStats().getClassLevel(this))
			{
				myChar.location().show(myChar,null,msg.source(),CMMsg.MSG_OK_ACTION,"<S-NAME> resist(s) the "+msg.tool().name()+" attack from <O-NAMESELF>!");
				return false;
			}
		}
		return super.okMessage(myChar,msg);
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
