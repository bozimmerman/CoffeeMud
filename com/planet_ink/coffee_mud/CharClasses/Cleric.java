package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Cleric extends StdCharClass
{
	public String ID(){return "Cleric";}
	public String name(){return "Cleric";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 5;}
	private static boolean abilitiesLoaded=false;
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	
	public Cleric()
	{
		super();
		maxStat[CharStats.WISDOM]=25;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_TurnUndead",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",false);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",false);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CauseSerious",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
			//CMAble.addCharAbilityMapping(ID(),7,"Prayer_SenseAlignment",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",false);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelGood",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CauseCritical",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",false);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
			
				CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Harm",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHarm",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AnimateDead",false);
			// placeholders
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Deathfinger",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Drain",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Contagion",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Restoration",false);
		}
	}

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		
		// if he already has one, don't give another!
		if(!mob.isMonster())
		{
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(CMAble.getQualifyingLevel(ID(),A.ID())==mob.baseCharStats().getClassLevel(this))
				&&(!CMAble.getDefaultGain(ID(),A.ID())))
					return;
			}
			// now only give one, for current level, respecting alignment!
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(A.appropriateToMyAlignment(mob.getAlignment()))
				&&(CMAble.getQualifyingLevel(ID(),A.ID())==mob.baseCharStats().getClassLevel(this))
				&&(!CMAble.getDefaultGain(ID(),A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
			}
		}
		else // monsters get everything -- leave it to other code to pick the right 
		for(int a=0;a<CMClass.abilities.size();a++) // ...ones to use.
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			&&((CMAble.getQualifyingLevel(ID(),A.ID())<=mob.baseCharStats().getClassLevel(this)))
			&&(!CMAble.getDefaultGain(ID(),A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Cleric.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherLimitations(){return "Using prayers outside your alignment introduces failure chance.";}
	public String weaponLimitations(){return "To avoid fumbling: Evil must use polearm, sword, axe, edged, or natural.  Neutral must use blunt, ranged, thrown, staff, natural, or sword.  Good must use blunt, flailed, natural, staff, or hammer.";}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon))
		{
			int classification=((Weapon)affect.tool()).weaponClassification();
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
			if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
			{
				myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"A conflict of <S-HIS-HER> conscience makes <S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
				return false;
			}
		}
		return true;
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
		if(mob.getAlignment()<350)
			w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
}
