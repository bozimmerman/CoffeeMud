package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Druid extends StdCharClass
{
	public String ID(){return "Druid";}
	public String name(){return "Druid";}
	public int getMaxHitPointsLevel(){return 25;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 6;}
	private static boolean abilitiesLoaded=false;
	private static long wearMask=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;
	
	public Druid()
	{
		super();
		maxStat[CharStats.CONSTITUTION]=25;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictWeather",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonPlants",true);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_SummonWater",true);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_LocatePlants",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonFood",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_Moonbeam",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SenseLife",true);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_Tangle",true);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonFire",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_FortifyFood",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_LocateAnimals",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_Farsight",true);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_CalmAnimal",true);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Sunray",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Goodberry",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Hunger",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Treeform",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_WarpWood",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_ControlFire",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_AnimalFriendship",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Barkskin",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_WaterWalking",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_Poison",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_SummonPeace",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_ResistPoison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantPass",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_WindGust",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_WaterBreathing",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_HoldAnimal",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_Treemind",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_WhisperWard",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_Bury",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_ResistFire",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_FeelHeat",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_ResistCold",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_FeelCold",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_CharmAnimal",true);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_CalmWind",true);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_FeelElectricity",true);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_ResistLightning",true);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_Shillelagh",true);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_Plague",true);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_CalmWeather",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_DistantGrowth",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Earthquake",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_ResistGas",true);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_SummonAnimal",true);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_SummonHeat",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonCold",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonInsects",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_Hibernation",true);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_AnimalSpy",true);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_SummonRain",true);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SummonWind",true);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SummonMount",true);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_NeutralizePoison",true);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_PlantSnare",true);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonLightning",true);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_Treemorph",true);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SummonElemental",true);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SummonFear",true);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_IllusionaryForest",true);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_CharmArea",true);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SpeedTime",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
			return(false);
		return true;
	}

	public String weaponLimitations(){return "To avoid fumbling, must be Natural, Wooden, or Vegetation-based weapons.";}
	public String armorLimitations(){return "Must wear cloth, paper, leather, or vegetation armor to avoid chant failure.";}
	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			&&((affect.tool()==null)||((affect.tool() instanceof Ability)&&(myChar.isMine(affect.tool())))))
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if(I==null) break;
					if((!I.amWearingAt(Item.INVENTORY))&&((I instanceof Armor)||(I instanceof Shield)))
					{
						switch(I.material()&EnvResource.MATERIAL_MASK)
						{
						case EnvResource.MATERIAL_CLOTH:
						case EnvResource.MATERIAL_VEGETATION:
						case EnvResource.MATERIAL_LEATHER:
						case EnvResource.MATERIAL_FLESH:
						case EnvResource.MATERIAL_ROCK:
						case EnvResource.MATERIAL_WOODEN:
						case EnvResource.MATERIAL_PAPER:
						case EnvResource.MATERIAL_UNKNOWN:
							break;
						default:
							if((Dice.rollPercentage()>myChar.charStats().getStat(CharStats.INTELLIGENCE)*2)
							&&(I.rawProperLocationBitmap()!=(Item.ON_RIGHT_FINGER|Item.ON_LEFT_FINGER))
							&&(I.rawProperLocationBitmap()!=Item.ON_EARS)
							&&(I.rawProperLocationBitmap()!=(Item.ON_EARS|Item.HELD))
							&&(I.rawProperLocationBitmap()!=Item.ON_EYES)
							&&(I.rawProperLocationBitmap()!=(Item.ON_EYES|Item.HELD))
							&&(I.rawProperLocationBitmap()!=Item.ON_NECK)
							&&(I.rawProperLocationBitmap()!=(Item.ON_NECK|Item.HELD))
							&&(I.rawProperLocationBitmap()!=(Item.ON_RIGHT_FINGER|Item.ON_LEFT_FINGER|Item.HELD)))
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
								return false;
							}
							break;
						}
					}
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
				switch(((Weapon)affect.tool()).material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_UNKNOWN:
				case EnvResource.MATERIAL_LEATHER:
				case EnvResource.MATERIAL_VEGETATION:
				case EnvResource.MATERIAL_FLESH:
					break;
				default:
					if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.CONSTITUTION)*2)
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
					break;
				}
		}
		return true;
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Quarterstaff");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
}
