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
public class Shaman extends Cleric
{
	public String ID(){return "Shaman";}
	public String name(){return "Shaman";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_NEUTRALCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected boolean disableClericSpellGrant(){return true;}
	
	public Shaman()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.CONSTITUTION]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_FortifyFood",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_EarthMud",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_Earthshield",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",true);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Sober",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctum",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_Fertilize",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_Rockskin",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GuardianHearth",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Tremor",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",true);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_RockFlesh",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_FleshRock",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_DrunkenStupor",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_MoralBalance",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Skill_Meditation",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_NeutralizeLand",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"EARTH",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AcidHealing",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_HolyDay",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean tick(MOB myChar, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
		}
		return true;
	}

	public String statQualifications(){return "Wisdom 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Shaman.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Shaman.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Never fumbles neutral prayers, receives smallest prayer fumble chance, and receives 1pt/level of acid damage reduction.";}
	public String otherLimitations(){return "Using non-neutral prayers introduces small failure chance.  Vulnerable to electric attacks.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ACID))
		{
			int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ELECTRIC))
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
			Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
