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
			CMAble.addCharAbilityMapping(ID(),1,"Paladin_LayHands",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",75,true);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),5,"Cleric_Turn",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_Attack3",true);	
			
			// qualify for all prayers
			Cleric c=new Cleric(); // make sure a cleric is available
			for(int level=1;level<22;level++)
			{
				Vector V=CMAble.getLevelListings(c.ID(),level);
				for(int v=0;v<V.size();v++)
				{
					String prayer=(String)V.elementAt(v);
					if(prayer.startsWith("Prayer_"))
						CMAble.addCharAbilityMapping(ID(),level+4,prayer,false);
				}
			}
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_DetectEvil",true);
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
				if(Dice.rollPercentage()>myChar.charStats().getWisdom()*4)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
					return false;
				}
		return super.okAffect(myChar, affect);
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength() <= 8)
			return false;

		if(mob.baseCharStats().getWisdom() <= 8)
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

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
