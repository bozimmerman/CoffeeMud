package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Paladin extends StdCharClass
{
	public String ID(){return "Paladin";}
	public String name(){return "Paladin";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 22;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusManaLevel(){return 10;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Paladin()
	{
		super();
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.WISDOM]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",75,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Paladin_HealingHands",true);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",false);
			CMAble.addCharAbilityMapping(ID(),2,"Paladin_ImprovedResists",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			CMAble.addCharAbilityMapping(ID(),5,"Paladin_SummonMount",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),7,"Paladin_DiseaseImmunity",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Sacrifice",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtEvil",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_CureSerious",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);
			CMAble.addCharAbilityMapping(ID(),11,"Paladin_Defend",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Bless",false);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Freedom",false);
			CMAble.addCharAbilityMapping(ID(),13,"Paladin_Courage",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_DispelEvil",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Cleave",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_RemovePoison",false);
			CMAble.addCharAbilityMapping(ID(),15,"Paladin_Breakup",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_CureDisease",false);
			CMAble.addCharAbilityMapping(ID(),17,"Paladin_PoisonImmunity",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Sanctuary",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_CureCritical",false);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),19,"Paladin_Aura",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_HolyAura",false);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);	
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Calm",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_CureBlindness",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Godstrike",false);
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Sweep",false);	
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_MassFreedom",false);
			CMAble.addCharAbilityMapping(ID(),25,"Paladin_Goodness",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Heal",false);
			CMAble.addCharAbilityMapping(ID(),30,"Paladin_CraftHolyAvenger",true);
		
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String otherLimitations(){return "Must remain good to avoid spell/skill failure chance.";}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		if((affect.amISource(myChar))
		&&(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
		&&(myChar.getAlignment() < 650)
		&&((affect.tool()==null)||((affect.tool() instanceof Ability)&&(myChar.isMine(affect.tool()))))
		&&(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2))
		{
			myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
			return false;
		}
		return super.okAffect(myChar, affect);
	}

	public String statQualifications(){return "Strength 9+, Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Paladin.");
			return false;
		}

		if(mob.baseCharStats().getStat(CharStats.WISDOM) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Paladin.");
			return false;
		}

		if(!(mob.charStats().getMyRace().ID().equals("Human")))
		{
			if(!quiet)
				mob.tell("You need to be Human to become a Paladin.");
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
	public void level(MOB mob)
	{
		super.level(mob);
	}
}
