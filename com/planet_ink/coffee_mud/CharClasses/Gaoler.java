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
public class Gaoler extends StdCharClass
{
	public String ID(){return "Gaoler";}
	public String name(){return "Gaoler";}
	public String baseClass(){return "Commoner";}
	public int getMaxHitPointsLevel(){return 8;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 15;}
	public int getHPDivisor(){return 6;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 5;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_DAGGERONLY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public boolean playerSelectable(){	return true;}


	public Gaoler()
	{
		super();
		maxStatAdj[CharStats.STRENGTH]=6;
		maxStatAdj[CharStats.DEXTERITY]=6;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);

			CMAble.addCharAbilityMapping(ID(),1,"ClanCrafting",false);
			CMAble.addCharAbilityMapping(ID(),1,"SmokeRings",false);
			CMAble.addCharAbilityMapping(ID(),2,"Butchering",false);
			CMAble.addCharAbilityMapping(ID(),3,"Searching",false);
			CMAble.addCharAbilityMapping(ID(),4,"Blacksmithing",false);
			CMAble.addCharAbilityMapping(ID(),4,"Carpentry",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Warrants",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"SlaveTrading",true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Sap",false);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Bind",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Enslave",true);

		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass()==this)
			{
				int exp=0;
				for(int a=0;a<mob.numAllEffects();a++)
				{
					Ability A=mob.fetchEffect(a);
					if((A!=null)
					&&(!A.isAutoInvoked())
					&&(mob.isMine(A))
					&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
						exp++;
				}
				if(exp>0)
					MUDFight.postExperience(mob,null,null,exp,true);
			}
		}
		return super.tick(ticking,tickID);
	}

	public String statQualifications(){return "Strength 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Gaoler.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Gaoler.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Whip");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public String otherBonuses(){return "Gains experience when using certain skills.";}
}
