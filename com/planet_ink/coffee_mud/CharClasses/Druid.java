package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Druid extends StdCharClass
{
	public String ID(){return "Druid";}
	public String name(){return "Druid";}
	public String baseClass(){return ID();}
	public int getMinHitPointsLevel(){return 5;}
	public int getMaxHitPointsLevel(){return 25;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 6;}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	public Druid()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Herbology",0,false);

			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictWeather",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonPlants",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_HardenSkin",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WildernessLore",false);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_SummonWater",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_LocatePlants",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_SensePoison",false);

			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonFood",false);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_Moonbeam",false);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_RestoreMana",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SenseLife",false);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_Tangle",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonFire",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_LocateAnimals",false);

			CMAble.addCharAbilityMapping(ID(),5,"Chant_FortifyFood",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_Farsight",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_FeelElectricity",false);

			CMAble.addCharAbilityMapping(ID(),6,"Chant_CalmAnimal",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Sunray",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Treeform",false);

			CMAble.addCharAbilityMapping(ID(),7,"Chant_Goodberry",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Hunger",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_FeelCold",false);

			CMAble.addCharAbilityMapping(ID(),8,"Chant_WarpWood",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_ControlFire",false);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_VenomWard",false);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_CalmWind",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_Barkskin",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_WaterWalking",false);

			CMAble.addCharAbilityMapping(ID(),10,"Chant_AnimalFriendship",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_FeelHeat",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_GrowClub",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_Brittle",false);

			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantPass",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_WindGust",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_Poison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Chant_Treemind",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_WhisperWard",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_BreatheWater",false);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_HoldAnimal",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_PlantBed",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_LightningWard",false);

			CMAble.addCharAbilityMapping(ID(),14,"Chant_ColdWard",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Bury",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_IllusionaryForest",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Hippieness",false);

			CMAble.addCharAbilityMapping(ID(),15,"Herbalism",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_Fertilization",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_CharmAnimal",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_CalmWeather",false);
			CMAble.addCharAbilityMapping(ID(),15,"PlantLore",false);

			CMAble.addCharAbilityMapping(ID(),16,"Chant_FireWard",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_Shillelagh",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_SummonPeace",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_Plague",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_DistantGrowth",false);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_Earthquake",false);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_PlantMaze",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_GasWard",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_Hibernation",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_Reabsorb",false);

			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonAnimal",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_Nectar",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonHeat",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_SenseSentience",false);

			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_Grapevine",false);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_SummonCold",false);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_SummonInsects",false);

			CMAble.addCharAbilityMapping(ID(),21,"Chant_AnimalSpy",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SummonRain",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_PlantSnare",false);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SensePregnancy",false);

			CMAble.addCharAbilityMapping(ID(),22,"Chant_Treemorph",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonWind",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_NeutralizePoison",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_FindPlant",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SensePlants",false);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_GrowItem",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SummonLightning",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SummonMount",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_FindOre",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_SenseOres",false);

			CMAble.addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SummonElemental",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SummonFear",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SenseAge",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_FindGem",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_SenseGems",false);

			CMAble.addCharAbilityMapping(ID(),25,"Chant_SpeedTime",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonSapling",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_Feralness",false);

			CMAble.addCharAbilityMapping(ID(),30,"Chant_Reincarnation",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		Vector grantable=new Vector();

		int level=mob.charStats().getClassLevel(this);
		int numChants=2;
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),true,A.ID())==level)
			&&((CMAble.getQualifyingLevel(ID(),true,A.ID())<=25)
			&&(!CMAble.getSecretSkill(ID(),true,A.ID()))
			&&(!CMAble.getDefaultGain(ID(),true,A.ID()))
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)))
			{if (!grantable.contains(A.ID())) grantable.addElement(A.ID());}
		}
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(grantable.contains(A.ID()))
			{
				grantable.remove(A.ID());
				numChants--;
			}
		}
		for(int i=0;i<numChants;i++)
		{
			if(grantable.size()==0) break;
			String AID=(String)grantable.elementAt(Dice.roll(1,grantable.size(),-1));
			if(AID!=null)
			{
				grantable.removeElement(AID);
				giveMobAbility(mob,
							   CMClass.getAbility(AID),
							   CMAble.getDefaultProfficiency(ID(),true,AID),
							   CMAble.getDefaultParm(ID(),true,AID),
							   isBorrowedClass);
			}
		}
	}


	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
			for(int i=0;i<affected.location().numItems();i++)
			{
				Item I=affected.location().fetchItem(i);
				if((I!=null)&&(I.ID().equals("DruidicMonument")))
					affectableState.setMana(affectableState.getMana()+(affectableState.getMana()/2));
			}
	}
	public String statQualifications(){return "Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Druid.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Dwarf, or Half Elf to be a Druid");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String weaponLimitations(){return "To avoid fumbling, must be Natural, Wooden, or Vegetation-based weapons.";}
	public String armorLimitations(){return "Must wear cloth, paper, leather, or vegetation armor to avoid chant failure.";}
	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "When leading animals into battle, will not divide experience among animal followers.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&((msg.tool()==null)
			   ||((CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0)&&(myChar.isMine(msg.tool()))))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
					return false;
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
				switch(((Weapon)msg.tool()).material()&EnvResource.MATERIAL_MASK)
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
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
					break;
				}
		}
		return true;
	}

	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   Hashtable followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!Sense.isAnimalIntelligence(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.get(mob)!=null)
		 ||(mob==killer)))
			return true;
		return false;
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Quarterstaff");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null) return duration;
		if(Util.bset(skill.flags(),Ability.FLAG_CRAFTING)
		&&(!skill.ID().equals("Weaving"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;
		   
		return duration;
	}
}
