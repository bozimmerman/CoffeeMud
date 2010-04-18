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
public class Cleric extends StdCharClass
{
	public String ID(){return "Cleric";}
	public String name(){return "Cleric";}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 10;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ALLCLERIC;}
	private HashSet disallowedWeaponsG=buildDisallowedWeaponClasses(CharClass.WEAPONS_GOODCLERIC);
	private HashSet disallowedWeaponsN=buildDisallowedWeaponClasses(CharClass.WEAPONS_NEUTRALCLERIC);
	private HashSet disallowedWeaponsE=buildDisallowedWeaponClasses(CharClass.WEAPONS_EVILCLERIC);
	protected HashSet disallowedWeaponClasses(MOB mob)
	{
		if(CMLib.flags().isEvil(mob)) return disallowedWeaponsE;
		if(CMLib.flags().isGood(mob)) return disallowedWeaponsG;
		return disallowedWeaponsN;
	}
	protected int alwaysFlunksThisQuality(){return -1;}

	public Cleric()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=7;
    }
    public void initializeClass()
    {
        super.initializeClass();
        if(!ID().equals(baseClass())) return;
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Christen",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CauseLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Desecrate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Bury",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_ControlUndead",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",false,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CauseSerious",false,CMParms.parseSemicolons("Prayer_CauseLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CreateWater",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Curse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_SenseAlignment",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Paralyze",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_DispelGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_SenseInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_Silence",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false,CMParms.parseSemicolons("Prayer_CureSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CauseCritical",false,CMParms.parseSemicolons("Prayer_CauseSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_SenseHidden",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_RemoveCurse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",false,CMParms.parseSemicolons("Prayer_Curse",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Calm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_SanctifyRoom",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Anger",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_InfuseHoliness",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_InfuseUnholiness",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_InfuseBalance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_Drain",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Thunderbolt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Hellfire",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false,CMParms.parseSemicolons("Prayer_Paralyze",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false,CMParms.parseSemicolons("Prayer_ProtParalyzation",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",false,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Harm",false,CMParms.parseSemicolons("Prayer_CauseCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Stasis",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_CurseItem",false,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false,CMParms.parseSemicolons("Prayer_Heal",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHarm",false,CMParms.parseSemicolons("Prayer_Harm",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Resurrect",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_AnimateDead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_Restoration",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_Deathfinger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_Contagion",false);
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
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
			return;
		}

		if(!ID().equals("Cleric")) return;

		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==mob.baseCharStats().getClassLevel(this))
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
				return;
		}
		// now only give one, for current level, respecting alignment!
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(A.appropriateToMyFactions(mob))
			&&(!CMLib.ableMapper().getSecretSkill(ID(),true,A.ID()))
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==mob.baseCharStats().getClassLevel(this))
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
			{
				giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
				break; // one is enough
			}
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Wisdom to become a Cleric.");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String getOtherLimitsDesc(){return "Using prayers outside your alignment introduces failure chance.";}

	protected int holyQuality(Ability A)
	{
		if(CMath.bset(A.flags(),Ability.FLAG_HOLY))
		{
			if(!CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
				return 1000;
		}
		else
		if(CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
			return 0;
		return 500;
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
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool())))
		{
			Ability A=(Ability)msg.tool();
			if(A.appropriateToMyFactions(myChar))
				return true;
            
			int hq=holyQuality(A);
			int basis=0;

			switch(alwaysFlunksThisQuality())
			{
			case 0:
				if(CMLib.flags().isEvil(myChar))
				{
					myChar.tell("Your immoral strife disrupts the prayer.");
					return false;
				}
				if(hq==0) basis=100;
				break;
			case 500:
				if(CMLib.flags().isNeutral(myChar))
				{
					myChar.tell("Your moral weakness disrupts the prayer.");
					return false;
				}
				if(hq==500) basis=100;
				break;
			case 1000:
				if(CMLib.flags().isGood(myChar))
				{
					myChar.tell("Your moral confusion disrupts the prayer.");
					return false;
				}
				if(hq==1000) basis=100;
				break;
			}
			if(basis==0)
			{
				if(hq==0)
					basis=CMLib.factions().getAlignPurity(myChar.fetchFaction(CMLib.factions().AlignID()),Faction.ALIGN_EVIL);
				else
				if(hq==1000)
					basis=CMLib.factions().getAlignPurity(myChar.fetchFaction(CMLib.factions().AlignID()),Faction.ALIGN_GOOD);
				else
				{
					basis=CMLib.factions().getAlignPurity(myChar.fetchFaction(CMLib.factions().AlignID()),Faction.ALIGN_NEUTRAL);
					basis-=10;
				}
			}
			if(CMLib.dice().rollPercentage()>basis)
				return true;

			if(hq==0)
				myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
			else
			if(hq==1000)
				myChar.tell("The goodness of "+A.name()+" disrupts your prayer.");
			else
			if(CMLib.flags().isGood(myChar))
				myChar.tell("The anti-good nature of "+A.name()+" disrupts your thought.");
			else
			if(CMLib.flags().isEvil(myChar))
				myChar.tell("The anti-evil nature of "+A.name()+" disrupts your thought.");
			return false;
		}
		return true;
	}

	public Vector outfit(MOB myChar)
	{
		Vector outfitChoices=new Vector();
		if(CMLib.flags().isEvil(myChar))
		{
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		else
		{
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
