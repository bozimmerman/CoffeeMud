package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Cleric extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Cleric()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=16;
		maxStat[CharStats.WISDOM]=25;
		bonusPracLevel=2;
		manaMultiplier=15;
		attackAttribute=CharStats.WISDOM;
		bonusAttackLevel=1;
		damageBonusPerLevel=0;
		name=myID;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_DetectLife",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Cleric_Turn",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_DetectEvil",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_DetectGood",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtectionEvil",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtectionGood",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateWater",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CauseSerious",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelGood",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CauseCritical",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Harm",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_MassHeal",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_MassHarm",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_HolyWord",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_BlessItem",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Deathfinger",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getWisdom()<=8)
			return false;
		return true;
	}


	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		{
			Item I=myChar.fetchWieldedItem();
			if((I!=null)&&(I instanceof Weapon))
			{
				int classification=((Weapon)I).weaponClassification();
				if(myChar.getAlignment()<350)
				{
					if((classification==Weapon.CLASS_POLEARM)
					||(classification==Weapon.CLASS_SWORD)
					||(classification==Weapon.CLASS_AXE)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_EDGED))
						return true;
				}
				else
				if(myChar.getAlignment()<650)
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_RANGED)
					||(classification==Weapon.CLASS_THROWN)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_SWORD))
						return true;
				}
				else
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_FLAILED)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_HAMMER))
						return true;
				}
				if(Dice.rollPercentage()>myChar.charStats().getWisdom()*4)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"During a conflict of <S-HIS-HER> conscience, <S-NAME> fumble(s) horribly with "+I.name()+".");
					return false;
				}
			}
		}
		return true;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(CMAble.getDefaultGain(ID(),A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),isBorrowedClass);
		}
		if(!mob.isMonster())
			outfit(mob);
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public void level(MOB mob)
	{
		super.level(mob);
	}
}
