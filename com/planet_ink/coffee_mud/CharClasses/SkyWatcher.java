package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SkyWatcher extends StdCharClass
{
	public String ID(){return "SkyWatcher";}
	public String name(){return "SkyWatcher";}
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
	
	public SkyWatcher()
	{
		super();
		maxStat[CharStats.CONSTITUTION]=22;
		maxStat[CharStats.INTELLIGENCE]=22;
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
			
			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictWeather",true);
CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictPhase",true);
			
CMAble.addCharAbilityMapping(ID(),2,"Chant_WindColor",true);
CMAble.addCharAbilityMapping(ID(),2,"Chant_Moonbeam",true);
	
CMAble.addCharAbilityMapping(ID(),3,"Chant_SnuffFlame",true);
CMAble.addCharAbilityMapping(ID(),3,"Chant_ClearMoon",true);
	
CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonDustdevil",true);
CMAble.addCharAbilityMapping(ID(),4,"Chant_LoveMoon",true);
	
			CMAble.addCharAbilityMapping(ID(),5,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_SummonFire",true);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_ControlFire",true);
	
			CMAble.addCharAbilityMapping(ID(),6,"Chant_CalmWind",true);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Sunray",true);
	
CMAble.addCharAbilityMapping(ID(),7,"Chant_MuddyGrounds",true);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_LightningWard",true);
	
CMAble.addCharAbilityMapping(ID(),8,"Chant_Dehydrate",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_ColdWard",true);
	
			CMAble.addCharAbilityMapping(ID(),9,"Chant_WindGust",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_FireWard",true);
	
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_Whisperward",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_HeatMetal",true);
	
CMAble.addCharAbilityMapping(ID(),11,"Chant_WarningWinds",true);
CMAble.addCharAbilityMapping(ID(),11,"Chant_HealingMoon",true);
	
			CMAble.addCharAbilityMapping(ID(),12,"Skill_Dirt",false);
CMAble.addCharAbilityMapping(ID(),12,"Chant_WindShape",true);
CMAble.addCharAbilityMapping(ID(),12,"Chant_AcidWard",true);
	
CMAble.addCharAbilityMapping(ID(),13,"Chant_GroveWalk",false);
CMAble.addCharAbilityMapping(ID(),13,"Chant_BlueMoon",true);
CMAble.addCharAbilityMapping(ID(),13,"Chant_RedMoon",true);
	
CMAble.addCharAbilityMapping(ID(),14,"Chant_CalmWeather",true);
CMAble.addCharAbilityMapping(ID(),14,"Chant_SongWard",true);
	
			CMAble.addCharAbilityMapping(ID(),15,"Herbalism",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SummonHeat",true);
CMAble.addCharAbilityMapping(ID(),15,"Chant_PeaceMoon",true);
	
CMAble.addCharAbilityMapping(ID(),16,"Druid_RecoverVoice",true);
CMAble.addCharAbilityMapping(ID(),16,"Chant_SoaringEagle",true);
CMAble.addCharAbilityMapping(ID(),16,"Chant_ChantWard",false);
	
			CMAble.addCharAbilityMapping(ID(),17,"Chant_SummonCold",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_WakingMoon",true);
	
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Observation",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_SummonRain",true);
CMAble.addCharAbilityMapping(ID(),18,"Chant_SummonMoon",true);
	
			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonWind",true);
CMAble.addCharAbilityMapping(ID(),19,"Chant_PrayerWard",true);
	
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Blindfighting",false);
CMAble.addCharAbilityMapping(ID(),20,"Chant_DistantWindcolor",true);
CMAble.addCharAbilityMapping(ID(),20,"Chant_ChargeMetal",true);
	
CMAble.addCharAbilityMapping(ID(),21,"Chant_Shapelessness",true);
CMAble.addCharAbilityMapping(ID(),21,"Chant_SpellWard",true);
	
			CMAble.addCharAbilityMapping(ID(),22,"Skill_Meditation",true);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonLightning",true);
CMAble.addCharAbilityMapping(ID(),22,"Chant_FalseMoon",true);
	
CMAble.addCharAbilityMapping(ID(),23,"Chant_WindSnatcher",true);
CMAble.addCharAbilityMapping(ID(),23,"Chant_AstralProjection",true);
	
CMAble.addCharAbilityMapping(ID(),24,"Chant_CloudWalk",true);
CMAble.addCharAbilityMapping(ID(),24,"Chant_DeadlySilence",true);
	
CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonTornado",true);
CMAble.addCharAbilityMapping(ID(),25,"Chant_MeteorStrike",true);
	
			CMAble.addCharAbilityMapping(ID(),30,"Chant_AlterTime",false);
			CMAble.addCharAbilityMapping(ID(),30,"Chant_MoveSky",true);
CMAble.addCharAbilityMapping(ID(),30,"Chant_ChangePhase",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	
	public String statQualifications(){return "Constitution 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a SkyWatcher.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a SkyWatcher.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("Dwarf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Dwarf, or Half Elf to be a SkyWatcher");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String weaponLimitations(){return "To avoid fumbling, must be Natural, Wooden, or Vegetation-based weapons.";}
	public String armorLimitations(){return "Must wear cloth, paper, leather, or vegetation armor to avoid chant failure.";}
	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "Attains Lunar Changes (lunar phase based bonuses/penalties) at level 5.";}

	private static final double[] moonfactors={2.0,1.0,0.0,-1.0,-2.0,-1.0,0.0,2.0,5,0};
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			Room room=affected.location();
			if((affected.charStats().getClassLevel(this)>=5)
			   &&(room.getArea().getMoonPhase()<moonfactors.length))
			{
				affectableState.setMovement(affectableState.getMovement()
											+(int)Math.round(Util.mul(Util.div(affectableState.getMovement(),8.0),moonfactors[room.getArea().getMoonPhase()])));
				affectableState.setHitPoints(affectableState.getHitPoints()
											+(int)Math.round(Util.mul(Util.div(affectableState.getHitPoints(),8.0),moonfactors[room.getArea().getMoonPhase()])));
				affectableState.setMana(affectableState.getMana()
											-(int)Math.round(Util.mul(Util.div(affectableState.getMana(),4.0),moonfactors[room.getArea().getMoonPhase()])));
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
			if((classLevel>=5)&&(room.getArea().getMoonPhase()<moonfactors.length))
			{
				affectableStats.setArmor(affectableStats.armor()
										 +(int)Math.round(Util.mul(classLevel,moonfactors[room.getArea().getMoonPhase()])));
				affectableStats.setArmor(affectableStats.attackAdjustment()
										 -(int)Math.round(Util.mul(classLevel,moonfactors[room.getArea().getMoonPhase()])));
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
