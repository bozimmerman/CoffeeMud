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
public class Missionary extends Cleric
{
	public String ID(){return "Missionary";}
	public String name(){return "Missionary";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_NEUTRALCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected boolean disableClericSpellGrant(){return true;}

	public Missionary()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.DEXTERITY]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_DivineLuck",true);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",true);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_Position",false);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_BirdsEye",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_SenseTraps",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_ElectricStrike",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Revival",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_AiryForm",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_HolyWind",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_Wings",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Etherealness",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_ChainStrike",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Monolith",0,"AIR",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Gateway",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_MoralBalance",true);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Weather",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UndeniableFaith",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"AIR",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_ElectricHealing",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Sermon",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	private static final int[] allSaves={
		CharStats.SAVE_ACID,
		CharStats.SAVE_COLD,
		CharStats.SAVE_DISEASE,
		CharStats.SAVE_ELECTRIC,
		CharStats.SAVE_FIRE,
		CharStats.SAVE_GAS,
		CharStats.SAVE_GENERAL,
		CharStats.SAVE_JUSTICE,
		CharStats.SAVE_MAGIC,
		CharStats.SAVE_MIND,
		CharStats.SAVE_PARALYSIS,
		CharStats.SAVE_POISON,
		CharStats.SAVE_UNDEAD,
		CharStats.SAVE_WATER,
		CharStats.SAVE_TRAPS};

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		for(int i=0;i<allSaves.length;i++)
			affectableStats.setStat(allSaves[i],
				affectableStats.getStat(allSaves[i])
					+(affectableStats.getClassLevel(this)));
	}

	public String statQualifications(){return "Wisdom 9+ Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Missionary.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Missionary.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Never fumbles neutral prayers, and receives 1pt/level luck bonus to all saving throws per level.  Receives 1pt/level electricity damage reduction.";}
	public String otherLimitations(){return "Using non-neutral prayers introduces failure chance.  Vulnerable to acid attacks.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ELECTRIC))
		{
			int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ACID))
		{
			int recovery=msg.value();
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
