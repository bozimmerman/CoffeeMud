package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Paladin extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Paladin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.WISDOM]=22;
		bonusPracLevel=0;
		manaMultiplier=10;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=2;
		name=myID;
		practicesAtFirstLevel=3;
		trainsAtFirstLevel=4;
		damageBonusPerLevel=1;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
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
			CMAble.addCharAbilityMapping(ID(),1,"Paladin_HealingHands",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",75,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",true);
			CMAble.addCharAbilityMapping(ID(),2,"Paladin_ImprovedResists",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
			CMAble.addCharAbilityMapping(ID(),6,"Paladin_SummonMount",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),10,"Paladin_DiseaseImmunity",true);
			CMAble.addCharAbilityMapping(ID(),11,"Paladin_MountedCombat",true);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",true);
			CMAble.addCharAbilityMapping(ID(),13,"Paladin_Defend",true);
			CMAble.addCharAbilityMapping(ID(),14,"Paladin_Courage",true);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Cleave",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),16,"Paladin_Breakup",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),18,"Paladin_PoisonImmunity",true);
			CMAble.addCharAbilityMapping(ID(),19,"Paladin_Aura",true);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);	
			CMAble.addCharAbilityMapping(ID(),22,"Paladin_Goodness",true);
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Sweep",true);	
			CMAble.addCharAbilityMapping(ID(),25,"Craft_HolyAvenger",true);
			
			// qualify for all prayers
			for(int level=1;level<22;level++)
			{
				Vector V=CMAble.getLevelListings("Cleric",level);
				for(int v=0;v<V.size();v++)
				{
					String prayer=(String)V.elementAt(v);
						CMAble.addCharAbilityMapping(ID(),level+4,prayer,false);
				}
			}
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar))
		if(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			if(myChar.getAlignment() < 650)
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
					return false;
				}
		return super.okAffect(myChar, affect);
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH) <= 8)
			return false;

		if(mob.baseCharStats().getStat(CharStats.WISDOM) <= 8)
			return false;

		if(!(mob.charStats().getMyRace().ID().equals("Human")))
			return(false);

		return true;
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
