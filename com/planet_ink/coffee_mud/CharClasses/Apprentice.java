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
   Copyright 2000-2006 Bo Zimmerman

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
public class Apprentice extends StdCharClass
{
	public String ID(){return "Apprentice";}
	public String name(){return "Apprentice";}
	public String baseClass(){return "Commoner";}
	public int getMaxHitPointsLevel(){return 5;}
	public int getBonusPracLevel(){return 5;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int getLevelsPerBonusDamage(){ return 25;}
	public int getTrainsFirstLevel(){return 6;}
	public int getHPDivisor(){return 9;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 4;}
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


	public Apprentice()
	{
		super();
		if(!loaded())
		{
			setLoaded(true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SmokeRings",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Butchering",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chopping",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Digging",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Drilling",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fishing",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Foraging",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Hunting",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Mining",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"FireBuilding",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Searching",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Blacksmithing",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"CageBuilding",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Carpentry",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Cooking",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Baking",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"FoodPrep",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"LeatherWorking",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"GlassBlowing",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Pottery",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"JewelMaking",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"ScrimShaw",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Sculpting",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Tailoring",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Weaving",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Dyeing",false);
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Embroidering",false,CMParms.parseSemicolons("Skill_Write",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Engraving",false,CMParms.parseSemicolons("Skill_Write",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Lacquerring",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Shipwright",false,CMParms.parseSemicolons("Carpentry",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Wainwrighting",false,CMParms.parseSemicolons("Carpentry",true));
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),8,"PaperMaking",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Farming",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),11,"LockSmith",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Distilling",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Speculate",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Smelting",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Taxidermy",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Armorsmithing",false,CMParms.parseSemicolons("Blacksmithing",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fletching",false,CMParms.parseSemicolons("Specialization_Ranged",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Weaponsmithing",false,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true));
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Merchant",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Construction",false,CMParms.parseSemicolons("Carpentry",true));
			CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Masonry",false,CMParms.parseSemicolons("Sculpting",true));
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Painting",false);
			
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Scrapping",false);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_HEROIC|Area.THEME_TECHNOLOGY;}

	public String statQualifications(){return "Wisdom 5+, Intelligence 5+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=4)
		{
			if(!quiet)
				mob.tell("You need at least a 5 Wisdom to become a Apprentice.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)<=4)
		{
			if(!quiet)
				mob.tell("You need at least a 5 Intelligence to become a Apprentice.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Dagger");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public String otherBonuses(){return "";}
}
