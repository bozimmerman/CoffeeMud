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
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_NATURAL;}
	private HashSet requiredWeaponMaterials=buildRequiredWeaponMaterials();
	protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public SkyWatcher()
	{
		super();
		maxStatAdj[CharStats.CONSTITUTION]=4;
		maxStatAdj[CharStats.INTELLIGENCE]=4;
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

			CMAble.addCharAbilityMapping(ID(),1,"Druid_DruidicPass",false);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictWeather",true);
			CMAble.addCharAbilityMapping(ID(),1,"Chant_PredictPhase",true);

			CMAble.addCharAbilityMapping(ID(),2,"Chant_WindColor",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_Moonbeam",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chant_ClearMoon",true);

			CMAble.addCharAbilityMapping(ID(),3,"Chant_SnuffFlame",false);
			CMAble.addCharAbilityMapping(ID(),3,"Chant_PaleMoon",false);

			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonDustdevil",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_LoveMoon",true);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_SummonFire",false);

			CMAble.addCharAbilityMapping(ID(),5,"Ranger_Hide",false);
			CMAble.addCharAbilityMapping(ID(),4,"Chant_ColdMoon",false);
			CMAble.addCharAbilityMapping(ID(),5,"Chant_ControlFire",false);

			CMAble.addCharAbilityMapping(ID(),6,"Chant_CalmWind",true);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_Sunray",false);
			CMAble.addCharAbilityMapping(ID(),6,"Chant_HoneyMoon",false);

			CMAble.addCharAbilityMapping(ID(),7,"Chant_MuddyGrounds",false);
			CMAble.addCharAbilityMapping(ID(),7,"Chant_LightningWard",false);

			CMAble.addCharAbilityMapping(ID(),8,"Chant_Dehydrate",true);
			CMAble.addCharAbilityMapping(ID(),8,"Chant_ColdWard",false);

			CMAble.addCharAbilityMapping(ID(),9,"Chant_WindGust",true);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_PiercingMoon",false);
			CMAble.addCharAbilityMapping(ID(),9,"Chant_FireWard",false);

			CMAble.addCharAbilityMapping(ID(),10,"Ranger_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_Whisperward",true);
			CMAble.addCharAbilityMapping(ID(),10,"Chant_HeatMetal",false);

			CMAble.addCharAbilityMapping(ID(),11,"Chant_WarningWinds",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_HealingMoon",false);
			CMAble.addCharAbilityMapping(ID(),11,"Chant_AcidWard",false);

			CMAble.addCharAbilityMapping(ID(),12,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_WindShape",true);
			CMAble.addCharAbilityMapping(ID(),12,"Chant_MoonCalf",false);

			CMAble.addCharAbilityMapping(ID(),13,"Chant_GroveWalk",true);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_BlueMoon",false);
			CMAble.addCharAbilityMapping(ID(),13,"Chant_RedMoon",false);

			CMAble.addCharAbilityMapping(ID(),14,"Chant_CalmWeather",true);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_SongWard",false);
			CMAble.addCharAbilityMapping(ID(),14,"Chant_WakingMoon",false);

			CMAble.addCharAbilityMapping(ID(),15,"Herbalism",false);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_SummonHeat",true);
			CMAble.addCharAbilityMapping(ID(),15,"Chant_PeaceMoon",false);

			CMAble.addCharAbilityMapping(ID(),16,"Druid_RecoverVoice",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_SoaringEagle",false);
			CMAble.addCharAbilityMapping(ID(),16,"Chant_SummonMoon",false);

			CMAble.addCharAbilityMapping(ID(),17,"Chant_SummonCold",true);
			CMAble.addCharAbilityMapping(ID(),17,"Chant_ChantWard",false);

			CMAble.addCharAbilityMapping(ID(),18,"Thief_Observation",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_ControlWeather",false);
			CMAble.addCharAbilityMapping(ID(),18,"Chant_SummonRain",false);

			CMAble.addCharAbilityMapping(ID(),19,"Chant_SummonWind",true);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_PrayerWard",false);
			CMAble.addCharAbilityMapping(ID(),19,"Chant_AcidRain",false);

			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Blindfighting",false);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_DistantWindColor",true);
			CMAble.addCharAbilityMapping(ID(),20,"Chant_ChargeMetal",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);

			CMAble.addCharAbilityMapping(ID(),21,"Chant_Shapelessness",true);
			CMAble.addCharAbilityMapping(ID(),21,"Chant_SpellWard",false);

			CMAble.addCharAbilityMapping(ID(),22,"Skill_Meditation",false);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_SummonLightning",true);
			CMAble.addCharAbilityMapping(ID(),22,"Chant_ManicMoon",false);

			CMAble.addCharAbilityMapping(ID(),23,"Chant_WindSnatcher",true);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_AstralProjection",false);
			CMAble.addCharAbilityMapping(ID(),23,"Chant_HowlersMoon",false);

			CMAble.addCharAbilityMapping(ID(),24,"Chant_CloudWalk",false);
			CMAble.addCharAbilityMapping(ID(),24,"Chant_DeathMoon",false);

			CMAble.addCharAbilityMapping(ID(),25,"Chant_SummonTornado",false);
			CMAble.addCharAbilityMapping(ID(),25,"Chant_MeteorStrike",true);

			CMAble.addCharAbilityMapping(ID(),30,"Chant_MoveSky",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
		&&(msg.tool()!=null)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(Dice.rollPercentage()<50))
		{
			if(((Ability)msg.tool()).appropriateToMyAlignment(myChar.getAlignment()))
				return true;
			myChar.tell("Extreme emotions disrupt your chant.");
			return false;
		}
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
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
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

	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String otherBonuses(){return "Attains Lunar Changes (lunar phase based bonuses/penalties) at level 5.";}

	private static final double[] moonfactors={2.0,1.0,0.0,-1.0,-2.0,-1.0,0.0,2.0,5.0};

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected.location()!=null)
		{
			Room room=affected.location();
			if((affected.charStats().getClassLevel(this)>=5)
			   &&(room.getArea().getTimeObj().getMoonPhase()<moonfactors.length))
			{
				affectableState.setMovement(affectableState.getMovement()
											+(int)Math.round(Util.mul(Util.div(affectableState.getMovement(),8.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableState.setHitPoints(affectableState.getHitPoints()
											+(int)Math.round(Util.mul(Util.div(affectableState.getHitPoints(),8.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableState.setMana(affectableState.getMana()
											-(int)Math.round(Util.mul(Util.div(affectableState.getMana(),4.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
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
			if((classLevel>=5)&&(room.getArea().getTimeObj().getMoonPhase()<moonfactors.length))
			{
				affectableStats.setArmor(affectableStats.armor() // - is good
										 -(int)Math.round(Util.mul(classLevel,moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() // - is bad
										 -(int)Math.round(Util.mul(classLevel,moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
			}
		}
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
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;
		   
		return duration;
	}
}
