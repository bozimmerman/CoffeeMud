package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Gaian extends StdCharClass
{
	public String ID(){return "Gaian";}
	public String name(){return "Gaian";}
	public String baseClass(){return "Druid";}
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
	
	public Gaian()
	{
		super();
		maxStat[CharStats.CONSTITUTION]=22;
		maxStat[CharStats.WISDOM]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",100,false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Herbology",0,false);
			CMAble.addCharAbilityMapping(ID(),1,"Foraging",50,true);
			
			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
			CMAble.addCharAbilityMapping(ID(),1,"Druid_MyPlants",true);
			
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonFlower",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_SummonHerb",true);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_LocatePlants",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonFood",true);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_SummonIvy",true);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonVine",true);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_FreeVine",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_FortifyFood",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Barkskin",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Ranger_Hide",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_Goodberry",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Chant_GrowClub",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_Root",true);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_PlantPass",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_KillerVine",true);

			CMAble.addCharAbilityMapping(ID(),10,"Druid_PlantForm",true);
			CMAble.addCharAbilityMapping(ID(),10,"Herbalism",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_SummonTree",false);

			CMAble.addCharAbilityMapping(ID(),11,"Farming",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_PlantBed",true);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_SummonSeed",true);

			CMAble.addCharAbilityMapping(ID(),12,"Chant_Shillelagh",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_PlantWall",true);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_DistantGrowth",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_SummonSeaweed",true);

			CMAble.addCharAbilityMapping(ID(),14,"Thief_Observation",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_PlantMaze",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_Thorns",true);

			CMAble.addCharAbilityMapping(ID(),15,"PlantLore",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_PoisonousVine",true);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SummonHouseplant",true);

			CMAble.addCharAbilityMapping(ID(),16,"Chant_GrowItem",true);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_Mold",true);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_PlantSnare",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_SummonFungus",true);

			CMAble.addCharAbilityMapping(ID(),18,"Chant_VampireVine",true);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_Chlorophyll",true);
			
			CMAble.addCharAbilityMapping(ID(),19,"Chant_DistantOvergrowth",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_MetalMold",true);

			CMAble.addCharAbilityMapping(ID(),20,"Chant_Grapevine",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Chant_Treehouse",true);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_VineMass",true);

			CMAble.addCharAbilityMapping(ID(),22,"Chant_GrowForest",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_GrowFood",true);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_DistantIngrowth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_FungalBloom",true);

			CMAble.addCharAbilityMapping(ID(),24,"Chant_CharmArea",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_BrownMold",true);

			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonSapling",true);
			
			CMAble.addCharAbilityMapping(ID(),30,"Chant_GrowOak",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	
	protected boolean isValidBeneficiary(MOB killer, 
									   MOB killed, 
									   MOB mob,
									   Hashtable followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!Sense.isVegetable(mob)))
		&&((mob.getVictim()==killed)
		 ||(followers.get(mob)!=null)
		 ||(mob==killer)))
			return true;
		return false;
	}
	public String statQualifications(){return "Constitution 9+, Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Gaian.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Gaian.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Dwarf, or Half Elf to be a Gaian");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String weaponLimitations(){return "To avoid fumbling, must be Natural, Wooden, or Vegetation-based weapons.";}
	public String armorLimitations(){return "Must wear cloth, paper, leather, or vegetation armor to avoid chant failure.";}
	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "Attains Greenskin (sunlight based bonuses/penalties) at level 5.  At level 30, becomes totally undetectable in wilderness settings while hidden.";}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			Room room=affected.location();
			if(affected.charStats().getClassLevel(this)>=5)
			{
				if(Sense.isInDark(room))
				{
					affectableState.setMana(affectableState.getMana()-(affectableState.getMana()/4));
					affectableState.setMovement(affectableState.getMovement()-(affectableState.getMovement()/4));
				}
				else
				if((room.domainType()&Room.INDOORS)==0)
					switch(room.getArea().weatherType(room))
					{
					case Area.WEATHER_BLIZZARD:
					case Area.WEATHER_CLOUDY:
					case Area.WEATHER_DUSTSTORM:
					case Area.WEATHER_HAIL:
					case Area.WEATHER_RAIN:
					case Area.WEATHER_SLEET:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
						break;
					default:
						affectableState.setMana(affectableState.getMana()+(affectableState.getMana()/4));
						affectableState.setMovement(affectableState.getMovement()+(affectableState.getMovement()/4));
						break;
					}
			}
		}
		
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			MOB mob=(MOB)affected;
			Room room=mob.location();
			int classLevel=mob.charStats().getClassLevel(this);
			if((Sense.isHidden(mob))
			&&(classLevel>=30)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_CITY))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
			
			if(classLevel>=5)
			{
				if(Sense.isInDark(room))
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((classLevel/5)+1));
				else
				if((room.domainType()&Room.INDOORS)==0)
					switch(room.getArea().weatherType(room))
					{
					case Area.WEATHER_BLIZZARD:
					case Area.WEATHER_CLOUDY:
					case Area.WEATHER_DUSTSTORM:
					case Area.WEATHER_HAIL:
					case Area.WEATHER_RAIN:
					case Area.WEATHER_SLEET:
					case Area.WEATHER_SNOW:
					case Area.WEATHER_THUNDERSTORM:
						break;
					default:
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((classLevel/5)+1));
						break;
					}
			}
		}
	}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			&&((affect.tool()==null)||((affect.tool() instanceof Ability)&&(myChar.isMine(affect.tool()))))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
					return false;
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
