package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Cleric extends StdCharClass
{
	public String ID(){return "Cleric";}
	public String name(){return "Cleric";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 5;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 10;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 8;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ALLCLERIC;}
	private HashSet disallowedWeaponsG=buildDisallowedWeaponClasses(CharClass.WEAPONS_GOODCLERIC);
	private HashSet disallowedWeaponsN=buildDisallowedWeaponClasses(CharClass.WEAPONS_NEUTRALCLERIC);
	private HashSet disallowedWeaponsE=buildDisallowedWeaponClasses(CharClass.WEAPONS_EVILCLERIC);
	protected HashSet disallowedWeaponClasses(MOB mob)
	{
		if(mob.getAlignment()<=350) return disallowedWeaponsE;
		if(mob.getAlignment()>=650) return disallowedWeaponsG;
		return disallowedWeaponsN;
	}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected boolean disableClericSpellGrant(){return false;}
	protected int alwaysFlunksThisQuality(){return -1;}

	public Cleric()
	{
		super();
		if(disableClericSpellGrant()) return;

		maxStatAdj[CharStats.WISDOM]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Christen",false);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",false);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_ControlUndead",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CauseSerious",false);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_SenseAlignment",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",false);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelGood",false);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_Silence",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CauseCritical",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_SenseHidden",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_RemoveCurse",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_SanctifyRoom",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_Drain",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Thunderbolt",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Harm",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Stasis",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHarm",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AnimateDead",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Restoration",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Deathfinger",false);
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Contagion",false);
		}
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		if(mob.playerStats()==null)
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
			return;
		}
		
		if(disableClericSpellGrant()) return;

		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((CMAble.getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			&&(CMAble.getQualifyingLevel(ID(),true,A.ID())==mob.baseCharStats().getClassLevel(this))
			&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
				return;
		}
		// now only give one, for current level, respecting alignment!
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			&&(A.appropriateToMyAlignment(mob.getAlignment()))
			&&(!CMAble.getSecretSkill(ID(),true,A.ID()))
			&&(CMAble.getQualifyingLevel(ID(),true,A.ID())==mob.baseCharStats().getClassLevel(this))
			&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
			{
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
				break; // one is enough
			}
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Cleric.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherLimitations(){return "Using prayers outside your alignment introduces failure chance.";}

	protected int holyQuality(Ability A)
	{
		if(Util.bset(A.flags(),Ability.FLAG_HOLY))
		{
			if(!Util.bset(A.flags(),Ability.FLAG_UNHOLY))
				return 1000;
		}
		else
		if(Util.bset(A.flags(),Ability.FLAG_UNHOLY))
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
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool())))
		{
			int align=myChar.getAlignment();
			Ability A=(Ability)msg.tool();

			if(A.appropriateToMyAlignment(align))
				return true;
			int hq=holyQuality(A);
			int basis=0;
				
			switch(alwaysFlunksThisQuality())
			{
			case 0:
				if(align<500)
				{
					myChar.tell("Your immoral strife disrupts the prayer.");
					return false;
				}
				if(hq==0) basis=100;
				break;
			case 500:
				if((align>350)&&(align<650))
				{
					myChar.tell("Your moral weakness disrupts the prayer.");
					return false;
				}
				if(hq==500) basis=100;
				break;
			case 1000:
				if(align>500)
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
					basis=align/10;
				else
				if(hq==1000)
					basis=(1000-align)/10;
				else
				{
					basis=(500-align)/10;
					if(basis<0) basis=basis*-1;
					basis-=10;
				}
			}
			if(Dice.rollPercentage()>basis)
				return true;

			if(hq==0)
				myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
			else
			if(hq==1000)
				myChar.tell("The goodness of "+A.name()+" disrupts your prayer.");
			else
			if(align>650)
				myChar.tell("The anti-good nature of "+A.name()+" disrupts your thought.");
			else
			if(align<350)
				myChar.tell("The anti-evil nature of "+A.name()+" disrupts your thought.");
			return false;
		}
		return true;
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
