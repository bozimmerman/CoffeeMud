package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Paladin extends StdCharClass
{
	public String ID(){return "Paladin";}
	public String name(){return "Paladin";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 22;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusAttackLevel(){return 2;}
	public int getMovementMultiplier(){return 12;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
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
			CMAble.addCharAbilityMapping(ID(),2,"Paladin_ImprovedResists",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Paladin_SummonMount",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureLight",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_SenseEvil",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_WandUse",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Paladin_DiseaseImmunity",true);
			
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
			CMAble.addCharAbilityMapping(ID(),16,"Paladin_MountedCharge",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Paladin_PoisonImmunity",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Sanctuary",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_CureCritical",false);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Trip",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Paladin_Aura",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_HolyAura",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Calm",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_CureBlindness",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BladeBarrier",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Godstrike",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Sweep",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_MassFreedom",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Paladin_Goodness",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Heal",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Paladin_CraftHolyAvenger",true);

		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.COMMON_SKILL)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public String otherLimitations(){return "Must remain good to avoid spell/skill failure chance.";}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if((msg.amISource(myChar))
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(myChar.getAlignment() < 650)
		&&((msg.tool()==null)||((CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0)
								&&(myChar.isMine(msg.tool()))))
		&&(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2))
		{
			myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> angry god absorb <S-HIS-HER> magical energy!");
			return false;
		}
		return super.okMessage(myChar, msg);
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

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
