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
public class SkyWatcher extends StdCharClass
{
	public String ID(){return "SkyWatcher";}
	public String name(){return "SkyWatcher";}
	public String baseClass(){return "Druid";}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_CONSTITUTION;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 7;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_NATURAL;}
	private HashSet requiredWeaponMaterials=buildRequiredWeaponMaterials();
	protected HashSet requiredWeaponMaterials(){return requiredWeaponMaterials;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public SkyWatcher()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",100,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",0,false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Druid_DruidicPass",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictWeather",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_SummonHail",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_PredictPhase",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_WindColor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_Moonbeam",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Chant_ClearMoon",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_SnuffFlame",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_PaleMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonDustdevil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_LoveMoon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SummonFire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Ranger_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_ColdMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_ControlFire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_CalmWind",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_Sunray",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_HoneyMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_MuddyGrounds",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_LightningWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Chant_StarGazing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_Dehydrate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_ColdWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_WindGust",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_PiercingMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_FireWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Ranger_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_Whisperward",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Chant_HeatMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_WarningWinds",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_HealingMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_AcidWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_WindShape",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Chant_MoonCalf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_GroveWalk",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_BlueMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_RedMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_CalmWeather",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_SongWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_WakingMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Herbalism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_SummonHeat",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_PeaceMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Druid_RecoverVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SoaringEagle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_SummonMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_SummonCold",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Chant_ChantWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Observation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_ControlWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_SummonRain",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_SummonWind",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_PrayerWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_AcidRain",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Blindfighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_DistantWindColor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_ChargeMetal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Scrapping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_Shapelessness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_SpellWard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Meditation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_SummonLightning",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_ManicMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_WindSnatcher",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_AstralProjection",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Chant_HowlersMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CloudWalk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_DeathMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_SummonTornado",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_MeteorStrike",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_MoveSky",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(CMLib.dice().rollPercentage()<50))
		{
			if(((Ability)msg.tool()).appropriateToMyFactions(myChar))
				return true;
			myChar.tell("Extreme emotions disrupt your chant.");
			return false;
		}
		return true;
	}

	public void executeMsg(Environmental host, CMMsg msg)
    {
        super.executeMsg(host,msg);
        Druid.doAnimalFreeingCheck(this,host,msg);
    }
    
	public String getStatQualDesc(){return "Constitution 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Constitution to become a SkyWatcher.");
				return false;
			}
			if(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Intelligence to become a SkyWatcher.");
				return false;
			}
			if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Dwarf"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Giant-kin")))
			{
				if(!quiet)
					mob.tell("You must be Human, Elf, Dwarf, Giant-kin, or Half Elf to be a SkyWatcher");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String getOtherLimitsDesc(){return "Must remain Neutral to avoid skill and chant failure chances.";}
	public String getOtherBonusDesc(){return "Attains Lunar Changes (lunar phase based bonuses/penalties) at level 5.  Can create a druidic connection with an area.  Benefits from freeing animals from cities.  Benefits from balancing the weather.";}

	private static final double[] moonfactors={2.0,1.0,0.0,-1.0,-2.0,-1.0,0.0,2.0,4.0};

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
											+(int)Math.round(CMath.mul(CMath.div(affectableState.getMovement(),8.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableState.setHitPoints(affectableState.getHitPoints()
											+(int)Math.round(CMath.mul(CMath.div(affectableState.getHitPoints(),8.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableState.setMana(affectableState.getMana()
											-(int)Math.round(CMath.mul(CMath.div(affectableState.getMana(),4.0),moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
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
										 -(int)Math.round(CMath.mul(classLevel,moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() // - is bad
										 -(int)Math.round(CMath.mul(classLevel,moonfactors[room.getArea().getTimeObj().getMoonPhase()])));
			}
		}
	}

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			DVector V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.getDimensionVector(1).elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Quarterstaff");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public int classDurationModifier(MOB myChar,
									 Ability skill,
									 int duration)
	{
		if(myChar==null) return duration;
		if(((skill.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
		&&(myChar.charStats().getCurrentClass().ID().equals(ID()))
		&&(!skill.ID().equals("FoodPrep"))
		&&(!skill.ID().equals("Cooking"))
		&&(!skill.ID().equals("Herbalism"))
		&&(!skill.ID().equals("Masonry")))
			return duration*2;

		return duration;
	}
}
