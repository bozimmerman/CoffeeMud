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
			
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",true);
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CauseSerious",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",true);
			//CMAble.addCharAbilityMapping(ID(),7,"Prayer_SenseAlignment",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelGood",true);
			
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CauseCritical",true);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);
			
				CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",true);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Harm",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_HolyWord",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Nullification",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_UnholyWord",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHarm",true);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_BlessItem",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Disenchant",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_CurseItem",true);
			
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AnimateDead",true);
			// placeholders
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Deathfinger",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Drain",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Contagion",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
			return false;
		return true;
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
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
}
