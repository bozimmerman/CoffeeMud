package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Mage extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Mage()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=8;
		maxStat[CharStats.INTELLIGENCE]=25;
		bonusPracLevel=4;
		manaMultiplier=20;
		attackAttribute=CharStats.INTELLIGENCE;
		bonusAttackLevel=0;
		name=myID;
		practicesAtFirstLevel=6;
		damageBonusPerLevel=0;
		trainsAtFirstLevel=3;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			// level 1
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Erase",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_MagicMissile",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_InsatiableThirst",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_MagicalAura",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Ventriloquate",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Shield",true);
			// level 2
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Blur",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Infravision",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_DetectUndead",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_ObscureSelf",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Enlarge",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_ResistMagicMissiles",true);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Light",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Clog",false);
			// level 3
			CMAble.addCharAbilityMapping(ID(),3,"Spell_KnowAlignment",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_DeadenSmell",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_DetectMetal",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Grease",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_WizardLock",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Deafness",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Knock",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_FeatherFall",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Sleep",false);
			// level 4
			CMAble.addCharAbilityMapping(ID(),4,"Spell_AnimateItem",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_Frost",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_DetectWater",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_BurningHands",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_ResistCold",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_Friends",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_WaterBreathing",false);
			// level 5
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectMagic",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_FloatingDisc",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectGold",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_Spook",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_Mend",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DispelMagic",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_MageArmor",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_ResistAcid",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_FaerieFire",false);
			// level 6
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Augury",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Charm",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Meld",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_DetectInvisible",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_ResistFire",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_GraceOfTheCat",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_ShockingGrasp",false);
			// level 7
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Darkness",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Invisibility",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_DetectHidden",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_PassDoor",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Fireball",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_SummonMonster",false);
			// level 8
			CMAble.addCharAbilityMapping(ID(),8,"Spell_FaerieFog",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Lightning",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Shatter",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Fear",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_ResistElectricity",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",false);
			// level 9
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_ComprehendLangs",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_MagicMouth",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Farsight",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_ResistGas",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Flameshield",false);
			// level 10
			CMAble.addCharAbilityMapping(ID(),10,"Spell_LocateObject",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Slow",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_MassSleep",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Teleport",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Mute",false);
			// level 11
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Claireaudience",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Feeblemind",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Stoneskin",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_HeatMetal",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Weaken",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_MirrorImage",false);
			// level 12
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Clairevoyance",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_ChangeSex",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Haste",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_DestroyObject",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Shelter",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Fly",false);
			//level 13
			CMAble.addCharAbilityMapping(ID(),13,"Spell_DistantVision",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_FeignDeath",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Gate",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_GiantStrength",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Frenzy",false);
			// level 14
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Alarm",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_KnowValue",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Silence",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_MassInvisibility",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Scribe",false);
			// level 15
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Advancement",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Earthquake",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Hold",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Polymorph",false);
			// level 16
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ChainLightening",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_Choke",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_PredictWeather",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_Cloudkill",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_Web",false);
			// level 17
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ClarifyScroll",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_RechargeWand",false);
			// level 18
			CMAble.addCharAbilityMapping(ID(),18,"Spell_SeeAura",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Delude",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Portal",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Summon",false);
			// level 19
			CMAble.addCharAbilityMapping(ID(),19,"Spell_FleshStone",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_StoneFlesh",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_ImprovedInvisibility",false);
			// level 20
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Scry",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);
			// level 21
			CMAble.addCharAbilityMapping(ID(),21,"Spell_Command",false);
			// level 22
			CMAble.addCharAbilityMapping(ID(),22,"Spell_Disenchant",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);
			// level 23
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Dragonfire",false);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_CombatPrecognition",false);
			// level 24
			// level 25
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Disintegrate",false);
			
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
			return(false);
		return true;
	}

	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);

		int numTotal=0;
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if(A.qualifyingLevel(mob)>0)
			{
				if(CMAble.getDefaultGain(ID(),A.ID()))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),isBorrowedClass);
				else
				if((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
					numTotal++;
			}
		}
		Hashtable given=new Hashtable();
		for(int level=2;level<19;level++)
		{
			int numSpells=(int)Math.floor(Util.div(26-level,8));
			int numLevel=0;
			while(numLevel<numSpells)
			{
				int randSpell=(int)Math.round(Math.random()*numTotal);
				for(int a=0;a<CMClass.abilities.size();a++)
				{
					Ability A=(Ability)CMClass.abilities.elementAt(a);
					if((A.qualifyingLevel(mob)>0)
					&&(!CMAble.getDefaultGain(ID(),A.ID()))
					&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
					{
						if(randSpell==0)
						{
							if((A.qualifyingLevel(mob)==level)&&(given.get(A.ID())==null))
							{
								giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),isBorrowedClass);
								given.put(A.ID(),A);
								numLevel++;
							}
							break;
						}
						else
							randSpell--;
					}
				}
			}
		}
		int numLevel=0;
		while(numLevel<2)
		{
			int randSpell=(int)Math.round(Math.random()*numTotal);
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				if((A.qualifyingLevel(mob)>0)
				&&(!CMAble.getDefaultGain(ID(),A.ID()))
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
				{
					if(randSpell==0)
					{
						if((A.qualifyingLevel(mob)>18)&&(given.get(A.ID())==null))
						{
							giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),isBorrowedClass);
							given.put(A.ID(),A);
							numLevel++;
						}
						break;
					}
					else
						randSpell--;
				}
			}
		}


		if(!mob.isMonster())
			outfit(mob);
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
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if(I==null) break;
					if((I.amWearingAt(Item.ON_TORSO))
					 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield))
					 ||(I.amWearingAt(Item.ON_LEGS))
					 ||(I.amWearingAt(Item.ON_ARMS))
					 ||(I.amWearingAt(Item.ON_WAIST))
					 ||(I.amWearingAt(Item.ON_HEAD)))
						if((I instanceof Armor)&&(((Armor)I).material()!=Armor.CLOTH))
							if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.INTELLIGENCE)*4)
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
								return false;
							}
				}
			}
			else
			if(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			{
				Item I=myChar.fetchWieldedItem();
				if((I!=null)&&(I instanceof Weapon))
				{
					int classification=((Weapon)I).weaponClassification();
					if(!((classification==Weapon.CLASS_NATURAL)
					||(classification==Weapon.CLASS_DAGGER)
					||(classification==Weapon.CLASS_STAFF))
					   )
						if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.INTELLIGENCE)*4)
						{
							myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+I.name()+".");
							return false;
						}
				}
			}
		}
		return super.okAffect(myChar,affect);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
