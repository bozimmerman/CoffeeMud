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
public class Templar extends Cleric
{
	public String ID(){return "Templar";}
	public String name(){return "Templar";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ANY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 1000;}
	protected boolean disableClericSpellGrant(){return true;}

	private int tickDown=0;

	public Templar()
	{
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.WISDOM]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_InfuseUnholiness",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),3,"Specialization_EdgedWeapon",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_UnholyArmament",false);

			CMAble.addCharAbilityMapping(ID(),5,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);

			CMAble.addCharAbilityMapping(ID(),6,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_Heresy",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_HuntGood",false);

			CMAble.addCharAbilityMapping(ID(),8,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_Behemoth",false);
			CMAble.addCharAbilityMapping(ID(),9,"Specialization_Hammer",false);

			CMAble.addCharAbilityMapping(ID(),10,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_DispelGood",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_DesecrateLand",false);

			CMAble.addCharAbilityMapping(ID(),14,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Bash",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Specialization_Natural",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_BloodHearth",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_BoneMoon",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_Tithe",true);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Maladiction",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Absorption",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Corruption",false);
			CMAble.addCharAbilityMapping(ID(),21,"Skill_Attack2",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Haunted",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_CreateIdol",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_SunCurse",0,"",false,false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Avatar",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(ticking instanceof MOB)) return super.tick(ticking,tickID);
		MOB myChar=(MOB)ticking;
		if((tickID==MudHost.TICK_MOB)&&((--tickDown)<=0))
		{
			tickDown=5;
			if(myChar.fetchEffect("Prayer_AuraStrife")==null)
			{
				Ability A=CMClass.getAbility("Prayer_AuraStrife");
				if(A!=null) A.invoke(myChar,myChar,true,0);
			}
		}
		return super.tick(myChar,tickID);
	}

	public String statQualifications(){return "Wisdom 9+ Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Templar.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Templar.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives Aura of Strife which increases in power.";}
	public String otherLimitations(){return "Always fumbles good prayers.  Using non-evil prayers introduces failure chance.";}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
