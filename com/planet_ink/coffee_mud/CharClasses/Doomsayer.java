package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Doomsayer extends Cleric
{
	public String ID(){return "Doomsayer";}
	public String name(){return "Doomsayer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_EVILCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 1000;}

	public Doomsayer()
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
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Ember",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_CurseFlames",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtFire",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Faithless",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_FlameWeapon",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Cannibalism",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_CurseMetal",false);
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_Intimidate",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_CurseMind",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Fortress",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_Demonshield",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_AuraHarm",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_MassDeafness",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_DailyBread",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_CurseLuck",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassBlindness",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_DemonicConsumption",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Condemnation",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_CurseMinds",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Doomspout",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"FIRE",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_FireHealing",true);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Stoning",0,"",false,true);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Wisdom 9+ Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Doomsayer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Doomsayer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives 1 pt damage reduction/level from fire attacks.";}
	public String otherLimitations(){return "Always fumbles good prayers, and fumbles all prayers when alignment is above 500.  Qualifies and receives evil prayers.  Using non-aligned prayers introduces failure chance.  Vulnerable to cold attacks.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
		{
			int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_COLD))
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
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
