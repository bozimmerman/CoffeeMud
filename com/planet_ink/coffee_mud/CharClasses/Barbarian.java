package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Barbarian extends StdCharClass
{
	public String ID(){return "Barbarian";}
	public String name(){return "Barbarian";}
	public String baseClass(){return "Fighter";}
	public int getBonusPracLevel(){return -1;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getMovementMultiplier(){return 13;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 7;}
	public int getManaDivisor(){return 8;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}

	public Barbarian()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_Charge",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_SmokeSignals",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Scalp",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Fighter_Battlecry",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Disarm",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Berzerk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_ArmorTweaking",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Spring",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Apothecary",0,"ANTIDOTES",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_JungleTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_Intimidate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_SwampTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Warcry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_DesertTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_ImprovedThrowing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_MountainTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_WeaponBreak",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Sweep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Rallycry",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_MountedCombat",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_HillsTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Endurance",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_IdentifyPoison",true,CMParms.parseSemicolons("Apothecary",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Roll",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_ForestTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_BullRush",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_Fragmentation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_PlainsTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Stonebody",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_Shrug",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Strength 9+, Constitution 9+";}
	public String getOtherBonusDesc(){return "Damage reduction 1pt/5 levels.  A 1%/level resistance to Enchantments.  Receives bonus conquest experience.";}
    public void executeMsg(Environmental host, CMMsg msg){ super.executeMsg(host,msg); Fighter.conquestExperience(this,host,msg);}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Barbarian.");
				return false;
			}
	
			if(mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Constitution to become a Barbarian.");
				return false;
			}
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
		   &&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		   &&(msg.tool()!=null)
		   &&(msg.tool() instanceof Ability)
		   &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
		{
			if(CMLib.dice().rollPercentage()<=myChar.charStats().getClassLevel(this))
			{
				myChar.location().show(myChar,null,msg.source(),CMMsg.MSG_OK_ACTION,"<S-NAME> resist(s) the "+msg.tool().name()+" attack from <O-NAMESELF>!");
				return false;
			}
		}
		return super.okMessage(myChar,msg);
	}

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			DVector V=CMLib.ableMapper().getUpToLevelListings(ID(),
															  mob.charStats().getClassLevel(ID()),
															  false,
															  false);
			for(Enumeration a=V.getDimensionVector(1).elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public Vector outfit(MOB myChar)
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
