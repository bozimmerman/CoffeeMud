package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Cleric extends StdCharClass
{
	public String ID(){return "Cleric";}
	public String name(){return "Cleric";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 5;}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return false;}
	protected boolean disableAlignedSpells(){return false;}
	protected boolean disableClericSpellGrant(){return false;}

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

		if(disableClericSpellGrant()) return;

		// if he already has one, don't give another!
		if(!mob.isMonster())
		{
			for(int a=0;a<mob.numLearnedAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(CMAble.getQualifyingLevel(ID(),A.ID())==mob.baseCharStats().getClassLevel(this))
				&&(!CMAble.getDefaultGain(ID(),A.ID())))
					return;
			}
			// now only give one, for current level, respecting alignment!
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				&&(A.appropriateToMyAlignment(mob.getAlignment()))
				&&(!CMAble.getSecretSkill(ID(),A.ID()))
				&&(CMAble.getQualifyingLevel(ID(),A.ID())==mob.baseCharStats().getClassLevel(this))
				&&(!CMAble.getDefaultGain(ID(),A.ID())))
				{
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
					break; // one is enough
				}
			}
		}
		else // monsters get everything -- leave it to other code to pick the right
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			&&((CMAble.getQualifyingLevel(ID(),A.ID())<=mob.baseCharStats().getClassLevel(this)))
			&&(!CMAble.getDefaultGain(ID(),A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

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
	public String weaponLimitations(){return "To avoid fumbling: Evil must use polearm, sword, axe, edged, or natural.  Neutral must use blunt, ranged, thrown, staff, natural, or sword.  Good must use blunt, flailed, natural, staff, or hammer.";}

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

		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(!disableAlignedSpells())
			&&(msg.tool()!=null)
			&&(myChar.isMine(msg.tool()))
			&&(CMAble.getQualifyingLevel(ID(),msg.tool().ID())>0)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
			{
				int align=myChar.getAlignment();
				Ability A=(Ability)msg.tool();

				if(A.appropriateToMyAlignment(align))
					return true;
				int hq=holyQuality(A);

				int basis=0;
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
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(!disableAlignedWeapons())
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				int classification=((Weapon)msg.tool()).weaponClassification();
				if(myChar.getAlignment()<350)
				{
					if((classification==Weapon.CLASS_POLEARM)
					||(classification==Weapon.CLASS_SWORD)
					||(classification==Weapon.CLASS_AXE)
					||(classification==Weapon.CLASS_DAGGER)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_EDGED))
						return true;
				}
				else
				if(myChar.getAlignment()<650)
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_RANGED)
					||(classification==Weapon.CLASS_THROWN)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_SWORD))
						return true;
				}
				else
				{
					if((classification==Weapon.CLASS_BLUNT)
					||(classification==Weapon.CLASS_FLAILED)
					||(classification==Weapon.CLASS_STAFF)
					||(classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_HAMMER))
						return true;
				}
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"A conflict of <S-HIS-HER> conscience makes <S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
					return false;
				}
			}
		}
		return true;
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
