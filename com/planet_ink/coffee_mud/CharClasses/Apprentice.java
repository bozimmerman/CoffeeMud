package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Apprentice extends StdCharClass
{
	public String ID(){return "Apprentice";}
	public String name(){return "Apprentice";}
	public String baseClass(){return "Commoner";}
	public int getMaxHitPointsLevel(){return 5;}
	public int getBonusPracLevel(){return 5;}
	public int getBonusManaLevel(){return 6;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 25;}
	public int getTrainsFirstLevel(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_DAGGERONLY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(){return disallowedWeapons;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};


	public Apprentice()
	{
		super();
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);
			CMAble.addCharAbilityMapping(ID(),1,"ClanCrafting",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"SmokeRings",false);
			
			CMAble.addCharAbilityMapping(ID(),2,"Butchering",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chopping",false);
			CMAble.addCharAbilityMapping(ID(),2,"Digging",false);
			CMAble.addCharAbilityMapping(ID(),2,"Drilling",false);
			CMAble.addCharAbilityMapping(ID(),2,"Fishing",false);
			CMAble.addCharAbilityMapping(ID(),2,"Foraging",false);
			CMAble.addCharAbilityMapping(ID(),2,"Hunting",false);
			CMAble.addCharAbilityMapping(ID(),2,"Mining",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"FireBuilding",false);
			CMAble.addCharAbilityMapping(ID(),3,"Searching",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Blacksmithing",false);
			CMAble.addCharAbilityMapping(ID(),4,"CageBuilding",false);
			CMAble.addCharAbilityMapping(ID(),4,"Carpentry",false);
			CMAble.addCharAbilityMapping(ID(),4,"Cooking",false);
			CMAble.addCharAbilityMapping(ID(),4,"LeatherWorking",false);
			CMAble.addCharAbilityMapping(ID(),4,"GlassBlowing",false);
			CMAble.addCharAbilityMapping(ID(),4,"Pottery",false);
			CMAble.addCharAbilityMapping(ID(),4,"JewelMaking",false);
			CMAble.addCharAbilityMapping(ID(),4,"ScrimShaw",false);
			CMAble.addCharAbilityMapping(ID(),4,"Sculpting",false);
			CMAble.addCharAbilityMapping(ID(),4,"Tailoring",false);
			CMAble.addCharAbilityMapping(ID(),4,"Weaving",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Dyeing",false);
			CMAble.addCharAbilityMapping(ID(),5,"Embroidering",false);
			CMAble.addCharAbilityMapping(ID(),5,"Engraving",false);
			CMAble.addCharAbilityMapping(ID(),5,"Lacquerring",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Shipwright",false);
			CMAble.addCharAbilityMapping(ID(),6,"Wainwrighting",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"PaperMaking",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Farming",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"LockSmith",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Distilling",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Speculate",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Smelting",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Taxidermy",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Armorsmithing",false);
			CMAble.addCharAbilityMapping(ID(),18,"Fletching",false);
			CMAble.addCharAbilityMapping(ID(),19,"Weaponsmithing",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Merchant",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Construction",false);
			CMAble.addCharAbilityMapping(ID(),22,"Masonry",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Painting",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Scrapping",false);
		}
	}

	public boolean playerSelectable(){	return true;}

	public String statQualifications(){return "Wisdom 5+, Intelligence 5+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=4)
		{
			if(!quiet)
				mob.tell("You need at least a 5 Wisdom to become a Apprentice.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=4)
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
			Weapon w=(Weapon)CMClass.getWeapon("Dagger");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public String otherBonuses(){return "";}
}