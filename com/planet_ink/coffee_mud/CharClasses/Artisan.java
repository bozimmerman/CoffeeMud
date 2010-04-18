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
public class Artisan extends StdCharClass
{
	public String ID(){return "Artisan";}
	public String name(){return "Artisan";}
	public String baseClass(){return "Commoner";}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getHPDivisor(){return 6;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 5;}
	public int getManaDivisor(){return 10;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_DAGGERONLY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public int availabilityCode(){return Area.THEME_FANTASY;}


	public Artisan()
	{
		super();
		for(int i : CharStats.CODES.BASE())
			maxStatAdj[i]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SmokeRings",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Butchering",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chopping",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Digging",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Drilling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fishing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Foraging",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Hunting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Mining",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"FireBuilding",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Searching",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Pottery",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"ScrimShaw",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Shearing",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Blacksmithing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Carpentry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"LeatherWorking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"GlassBlowing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Sculpting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Tailoring",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Weaving",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"CageBuilding",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Cooking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Baking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"FoodPrep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"JewelMaking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Warrants",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Costuming",true,CMParms.parseSemicolons("Tailoring",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Dyeing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Embroidering",true,CMParms.parseSemicolons("Skill_Write",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Engraving",true,CMParms.parseSemicolons("Skill_Write",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Lacquerring",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Smelting",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Armorsmithing",true,CMParms.parseSemicolons("Blacksmithing",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fletching",true,CMParms.parseSemicolons("Specialization_Ranged",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Weaponsmithing",true,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Shipwright",true,CMParms.parseSemicolons("Carpentry",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Wainwrighting",true,CMParms.parseSemicolons("Carpentry",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"PaperMaking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Cobbling",false,CMParms.parseSemicolons("LeatherWorking",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Distilling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Farming",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_WandUse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Speculate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Painting",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"LockSmith",0,"",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Construction",true,CMParms.parseSemicolons("Carpentry",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Masonry",true,CMParms.parseSemicolons("Sculpting",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Cage",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Merchant",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Taxidermy",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Stability",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Appraise",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"InstrumentMaking",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Haggle",false);

        CMLib.ableMapper().addCharAbilityMapping(ID(),20,"MasterTailoring",false,CMParms.parseSemicolons("Tailoring(100)",true),"+DEX 16");
        
        CMLib.ableMapper().addCharAbilityMapping(ID(),21,"MasterCostuming",false,CMParms.parseSemicolons("Costuming(100)",true),"+INT 16");
        
        CMLib.ableMapper().addCharAbilityMapping(ID(),22,"MasterLeatherWorking",false,CMParms.parseSemicolons("LeatherWorking(100)",true),"+CON 16");
        
        CMLib.ableMapper().addCharAbilityMapping(ID(),23,"MasterArmorsmithing",false,CMParms.parseSemicolons("Armorsmithing(100)",true),"+STR 16");

        CMLib.ableMapper().addCharAbilityMapping(ID(),24,"MasterWeaponsmithing",false,CMParms.parseSemicolons("Weaponsmithing(100);Specialization_*",true),"+STR 16");
        
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Scrapping",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_Lore",false);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
			{
				int exp=0;
				for(int a=0;a<mob.numAllEffects();a++)
				{
					Ability A=mob.fetchEffect(a);
					if((A!=null)
					&&(!A.isAutoInvoked())
					&&(mob.isMine(A))
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
						exp++;
				}
				if(exp>0)
					CMLib.leveler().postExperience(mob,null,null,exp,true);
			}
		}
		return super.tick(ticking,tickID);
	}

	public String getStatQualDesc(){return "Strength 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Artisan.");
				return false;
			}
			if(mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Dexterity to become a Artisan.");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Dagger");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public String getOtherBonusDesc(){return "Gains experience when using common skills.";}
}
