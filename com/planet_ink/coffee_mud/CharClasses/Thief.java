package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Thief extends StdCharClass
{
	public String ID(){return "Thief";}
	public String name(){return "Thief";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 12;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 5;}
	private static boolean abilitiesLoaded=false;
	public int allowedArmorLevel(){return CharClass.ARMOR_LEATHER;}
	
	public Thief()
	{
		super();
		maxStat[CharStats.DEXTERITY]=25;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Edged",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Appraise",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",true);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",true);
			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),7,"Thief_Peek",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Listen",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",true);
			CMAble.addCharAbilityMapping(ID(),11,"Thief_Steal",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),12,"Thief_Snatch",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Observation",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Bind",true);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Surrender",true);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_SilentGold",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),16,"Thief_SilentLoot",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Search",true);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Knock",false);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",true);
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Lore",true);
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sap",true);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_Poison",true);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Charm",false);
			CMAble.addCharAbilityMapping(ID(),24,"Thief_Bribe",true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_ComprehendLangs",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Ventrilloquate",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Thief.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
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
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear leather, cloth, or vegetation based armor to avoid skill failure.";}
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(((affect.sourceMajor()&Affect.MASK_DELICATE)>0)
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) in <S-HIS-HER> maneuver!");
					return false;
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				switch(classification)
				{
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_DAGGER:
					break;
				default:
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
					break;
				}
			}
		}
		return super.okAffect(myChar,affect);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);

		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level after 1st.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isSleeping(affected)||Sense.isSitting(affected))
			affectableStats.setArmor(affectableStats.armor()+(100-affected.baseEnvStats().armor()));
	}
	public void level(MOB mob)
	{
		super.level(mob);
		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^NYour stealthiness grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}
