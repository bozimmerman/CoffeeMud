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
	public int allowedWeaponLevel(){return CharClass.WEAPONS_FLAILONLY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int availabilityCode(){return Area.THEME_FANTASY;}


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
			CMAble.addCharAbilityMapping(ID(),1,"FireBuilding",true);
			CMAble.addCharAbilityMapping(ID(),1,"ClanCrafting",false);
			CMAble.addCharAbilityMapping(ID(),1,"SmokeRings",true);
			CMAble.addCharAbilityMapping(ID(),1,"Cooking",false);
			CMAble.addCharAbilityMapping(ID(),2,"Butchering",false);
			CMAble.addCharAbilityMapping(ID(),3,"BodyPiercing",true);
			CMAble.addCharAbilityMapping(ID(),4,"Searching",false);
			CMAble.addCharAbilityMapping(ID(),5,"Blacksmithing",true);
			CMAble.addCharAbilityMapping(ID(),6,"Carpentry",false);
			CMAble.addCharAbilityMapping(ID(),7,"Tattooing",true);
			CMAble.addCharAbilityMapping(ID(),8,"LockSmith",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Warrants",true);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Brainwash",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_HandCuff",false);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_TarAndFeather",true);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Flay",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_ArrestingSap",true);
			CMAble.addCharAbilityMapping(ID(),16,"Torturesmithing",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Leeching",false);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_CollectBounty",true);
			CMAble.addCharAbilityMapping(ID(),19,"Skill_Arrest",false);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Behead",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Stoning",false);
			CMAble.addCharAbilityMapping(ID(),22,"SlaveTrading",true);
			CMAble.addCharAbilityMapping(ID(),23,"Skill_Enslave",false);
			CMAble.addCharAbilityMapping(ID(),24,"Skill_JailKey",true);
			CMAble.addCharAbilityMapping(ID(),25,"Skill_Chirgury",false);
			CMAble.addCharAbilityMapping(ID(),30,"Amputation",true);
			
			// to separate from artisam
			CMAble.addCharAbilityMapping(ID(),30,"MasterArmorsmithing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"MasterTailoring",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"MasterWeaponsmithing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"MasterCostuming",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"MasterLeatherWorking",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Chopping",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Digging",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Drilling",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Fishing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Foraging",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Herbology",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Cobbling",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Hunting",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Mining",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Pottery",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"ScrimShaw",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"LeatherWorking",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"GlassBlowing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Sculpting",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Tailoring",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Weaving",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"CageBuilding",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"JewelMaking",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Dyeing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Embroidering",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Engraving",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Lacquerring",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Smelting",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Armorsmithing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Fletching",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Weaponsmithing",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Shipwright",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Wainwrighting",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"PaperMaking",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Distilling",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Farming",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Speculate",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Painting",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Construction",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Masonry",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Taxidermy",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Merchant",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Scrapping",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),30,"Costuming",0,"",false,true);
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
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Gaoler.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
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
