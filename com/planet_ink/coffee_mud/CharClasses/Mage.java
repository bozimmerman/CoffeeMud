package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Mage extends StdCharClass
{
	public String ID(){return "Mage";}
	public String name(){return "Mage";}
	public String baseClass(){return ID();}
	public int getMaxHitPointsLevel(){return 8;}
	public int getBonusPracLevel(){return 4;}
	public int getBonusManaLevel(){return 20;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.INTELLIGENCE;}
	public int getLevelsPerBonusDamage(){ return 10;}
	public int getPracsFirstLevel(){return 6;}
	public int getTrainsFirstLevel(){return 3;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_MAGELIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}

	public Mage()
	{
		super();
		maxStatAdj[CharStats.INTELLIGENCE]=7;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_ScrollCopy",false);
			// level 1
			CMAble.addCharAbilityMapping(ID(),1,"Spell_MagicMissile",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_ResistMagicMissiles",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Shield",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_IronGrip",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Erase",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_InsatiableThirst",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_MagicalAura",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_Ventriloquate",false);
			// level 2
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Blur",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Infravision",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_DetectUndead",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_ObscureSelf",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_LightenItem",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_SummonSteed",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_ResistPoison",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Enlarge",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Shrink",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Light",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Clog",false);
			// level 3
			CMAble.addCharAbilityMapping(ID(),3,"Spell_KnowAlignment",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_DeadenSmell",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_DetectMetal",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Dream",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_ResistGas",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Grease",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_SummoningWard",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_WizardLock",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Deafness",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Knock",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_FeatherFall",false);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_Sleep",false);
			// level 4
			CMAble.addCharAbilityMapping(ID(),4,"Spell_AnimateItem",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_FakeFood",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_FakeSpring",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_Frost",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_AcidArrow",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_Hunger",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_DetectWater",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_DetectPoison",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_ResistAcid",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_MageArmor",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_BurningHands",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_Friends",false);
			CMAble.addCharAbilityMapping(ID(),4,"Spell_WaterBreathing",false);
			// level 5
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectMagic",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_FloatingDisc",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectGold",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_WallOfStone",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_Spook",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CharmWard",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_ResistCold",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_Mend",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_WeaknessGas",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DispelMagic",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_FaerieFire",false);
			// clan magic
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqAcid",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqCold",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqElectric",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqFire",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqGas",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqMind",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqParalysis",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqPoison",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqWater",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_CEqDisease",0,"",false,true);
			// level 6
			CMAble.addCharAbilityMapping(ID(),6,"Spell_WeaknessAcid",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Augury",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Charm",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Meld",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_IllusoryWall",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_FindFamiliar",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_StinkingCloud",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_DetectInvisible",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_ResistElectricity",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_GraceOfTheCat",false);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_ShockingGrasp",false);
			// level 7
			CMAble.addCharAbilityMapping(ID(),7,"Spell_WeaknessCold",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Darkness",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Invisibility",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_GhostSound",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_LightSensitivity",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Tourettes",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Refit",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_ResistFire",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_DetectHidden",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_PassDoor",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_Fireball",false);
			CMAble.addCharAbilityMapping(ID(),7,"Spell_SummonMonster",false);
			// level 8
			CMAble.addCharAbilityMapping(ID(),8,"Spell_WeaknessElectricity",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_FaerieFog",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Lightning",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_ResistDisease",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_TeleportationWard",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Shatter",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_ElementalStorm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_WallOfDarkness",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_Fear",false);
			CMAble.addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",false);
			// level 9
			CMAble.addCharAbilityMapping(ID(),9,"Spell_WeaknessFire",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Mirage",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Awe",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_ComprehendLangs",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_MagicMouth",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_IceSheet",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Farsight",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_Flameshield",false);
			// level 10
			CMAble.addCharAbilityMapping(ID(),10,"Spell_LocateObject",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Slow",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_MassSleep",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_ArcaneMark",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_PhantomHound",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_ResistArrows",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Teleport",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_GustOfWind",false);
			CMAble.addCharAbilityMapping(ID(),10,"Spell_Mute",false);
			// level 11
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Claireaudience",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Feeblemind",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Stoneskin",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_WallOfIce",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_AcidFog",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_HeatMetal",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Laughter",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Weaken",false);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_MirrorImage",false);
			// level 12
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Clairevoyance",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_ChangeSex",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Haste",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_DestroyObject",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Enlightenment",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_MinorGlobe",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Shelter",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Confusion",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Delirium",false);
			CMAble.addCharAbilityMapping(ID(),12,"Spell_Fly",false);
			//level 13
			CMAble.addCharAbilityMapping(ID(),13,"Spell_DistantVision",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Reinforce",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_ResistParalyzation",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_FeignDeath",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Earthquake",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Gate",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_GiantStrength",false);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_Frenzy",false);
			// level 14
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Nondetection",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_KnowValue",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Silence",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_ArcanePossession",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_WallOfAir",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_AnimateWeapon",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_MassInvisibility",false);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Scribe",false);
			// level 15
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Advancement",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Ensnare",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Hold",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_FakeWeapon",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Blink",false);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Polymorph",false);
			// level 16
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ChainLightening",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_Choke",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_Dismissal",false);
			CMAble.addCharAbilityMapping(ID(),16,"Skill_Meditation",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_MageClaws",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_FreeMovement",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_PredictWeather",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_FakeArmor",false);
			// level 17
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ClarifyScroll",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_FoolsGold",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_WallOfFire",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_Siphon",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_Portal",false);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_RechargeWand",false);
			// level 18
			CMAble.addCharAbilityMapping(ID(),18,"Spell_SeeAura",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_ReverseGravity",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Web",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Delude",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_ResistPetrification",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Summon",false);
			// level 19
			CMAble.addCharAbilityMapping(ID(),19,"Spell_FleshStone",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Forget",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_StoneFlesh",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_WallOfForce",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_ImprovedInvisibility",false);
			// level 20
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Scry",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_ImprovedPolymorph",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Nightmare",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Cloudkill",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Repulsion",false);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);
			// level 21
			CMAble.addCharAbilityMapping(ID(),21,"Spell_Command",false);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_Immunity",false);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_SummonEnemy",false);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_MassHaste",false);
			// level 22
			CMAble.addCharAbilityMapping(ID(),22,"Spell_Disenchant",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_MajorGlobe",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_Hungerless",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_MeteorStorm",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_DetectSentience",false);
			// level 23
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Dragonfire",false);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_MindBlock",false);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Cogniportive",false);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_TimeStop",false);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_CombatPrecognition",false);
			// level 24
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Delay",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Frailty",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Boomerang",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Thirstless",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_SummonFlyer",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Anchor",false);
			// level 25
			CMAble.addCharAbilityMapping(ID(),25,"Spell_SpellTurning",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Disintegrate",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_DemonGate",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_PolymorphSelf",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Geas",false);

			// level 30
			if(ID().equals("Mage"))
				CMAble.addCharAbilityMapping(ID(),30,"Spell_Spellbinding",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Mage.");
			return false;
		}
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("Gnome"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
		{
			if(!quiet)
				mob.tell("You need to be Human, Elf, Gnome, or Half Elf to be a Mage.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		Vector grantable=new Vector();

		int level=mob.charStats().getClassLevel(this);
		int numSpells=3;
		if(level<8)
			numSpells=3;
		else
		if(level<19)
			numSpells=2;
		else
			numSpells=1;
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),true,A.ID())==level)
			&&((CMAble.getQualifyingLevel(ID(),true,A.ID())<=25)
			&&(!CMAble.getSecretSkill(ID(),true,A.ID()))
			&&(!CMAble.getDefaultGain(ID(),true,A.ID()))
			&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)))
			{if (!grantable.contains(A.ID())) grantable.addElement(A.ID());}
		}
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(grantable.contains(A.ID()))
			{
				grantable.remove(A.ID());
				numSpells--;
			}
		}
		for(int i=0;i<numSpells;i++)
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
	

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
