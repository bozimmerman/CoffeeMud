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
public class Monk extends StdCharClass
{
	public String ID(){return "Monk";}
	public String name(){return "Monk";}
	public String baseClass(){return "Fighter";}
	public int getBonusPracLevel(){return -1;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STAT_STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	public int getHPDivisor(){return 2;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 7;}
	public int getManaDivisor(){return 8;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}

	public Monk()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_Kick",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fighter_MonkeyPunch",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Hide",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_Climb",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Dodge",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_ArmorTweaking",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Sneak",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_DeflectProjectile",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_KnifeHand",false,CMParms.parseSemicolons("Fighter_MonkeyPunch",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_AxKick",false,CMParms.parseSemicolons("Fighter_Kick",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_BackHand",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_BodyToss",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_BodyFlip",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_BlindFighting",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_CatchProjectile",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_FlyingKick",false,CMParms.parseSemicolons("Fighter_AxKick",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_WeaponBreak",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_Pin",false);
		
/**/		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Detection",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Sweep",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Cartwheel",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_SideKick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_BodyShield",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_CircleParry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_KiStrike",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_AttackHalf",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Tumble",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_Snatch",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Endurance",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Gouge",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_CircleTrip",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Listen",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_LightningStrike",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_ReturnProjectile",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_AtemiStrike",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Strength 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Monk.");
				return false;
			}
	
			if(mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Dexterity to become a Monk.");
				return false;
			}
			if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Goblinoids")))
			{
				if(!quiet)
					mob.tell("You must be Human, Elf, Goblinoid, or Half Elf to be a Monk");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Wearable.WORN_WIELD))
			      ||(I.amWearingAt(Wearable.WORN_HELD))))
				return true;
		}
		return false;
	}

    public void executeMsg(Environmental host, CMMsg msg){ super.executeMsg(host,msg); Fighter.conquestExperience(this,host,msg);}
	public String getOtherBonusDesc(){return "Receives defensive bonus for high dexterity.  Receives unarmed attack bonus.  Receives bonus attack when unarmed.  Has Slow Fall ability.  Receives trap avoidance.  Receives bonus conquest experience.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if(CMLib.flags().isStanding((MOB)affected))
			{
				MOB mob=(MOB)affected;
				int attArmor=(((int)Math.round(CMath.div(mob.charStats().getStat(CharStats.STAT_DEXTERITY),9.0)))+1)*(mob.charStats().getClassLevel(this)-1);
				affectableStats.setArmor(affectableStats.armor()-attArmor);
			}
			if(!anyWeapons((MOB)affected))
			{
				affectableStats.setSpeed(affectableStats.speed()+1.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((MOB)affected).charStats().getClassLevel(this));
			}
			if(affected.fetchEffect("Falling")!=null)
				affectableStats.setWeight(0);
		}
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,
			affectableStats.getStat(CharStats.STAT_SAVE_MIND)
			+(affectableStats.getClassLevel(this)*2));
		affectableStats.setStat(CharStats.STAT_SAVE_TRAPS,
			affectableStats.getStat(CharStats.STAT_SAVE_TRAPS)
			+(affectableStats.getClassLevel(this)*2));
	}
	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);
	    if(((mob.baseEnvStats().level()+1) % 2)==0)
	    {
			int dexStat=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
			int maxDexStat=(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)
						 +mob.charStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
			if(dexStat>maxDexStat) dexStat=maxDexStat;
			int attArmor=(int)Math.round(CMath.div(dexStat,9.0));
			attArmor=attArmor*-1;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
			mob.envStats().setArmor(mob.envStats().armor()-attArmor);
	    }

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	
	public void level(MOB mob, Vector newAbilityIDs)
	{
	    if(CMSecurity.isDisabled("LEVELS")) return;
	    if((mob.baseEnvStats().level() % 2)==0)
	    {
			int dexStat=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
			int maxDexStat=(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)
						 +mob.charStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
			if(dexStat>maxDexStat) dexStat=maxDexStat;
			int attArmor=((int)Math.round(CMath.div(dexStat,9.0)))+1;
			mob.tell("^NYour dexterity grants you a defensive bonus of ^H"+attArmor+"^?.^N");
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
				&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public Vector outfit(MOB myChar)
	{
		return null;
	}
}
