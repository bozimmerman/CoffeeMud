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
public class Mage extends StdCharClass
{
	public String ID(){return "Mage";}
	public String name(){return "Mage";}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return 4;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_INTELLIGENCE;}
	public int getLevelsPerBonusDamage(){ return 30;}
	public int getPracsFirstLevel(){return 6;}
	public int getTrainsFirstLevel(){return 3;}
	public int getMovementMultiplier(){return 8;}
	public int getHPDivisor(){return 6;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 5;}
	public int getManaDivisor(){return 3;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}
	protected String armorFailMessage(){return "<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_MAGELIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public int requiredArmorSourceMinor(){return CMMsg.TYP_CAST_SPELL;}
	protected boolean grantSomeSpells(){return true;}

	public Mage()
	{
		super();
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=7;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_ScrollCopy",false);
		// level 1
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_MagicMissile",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ResistMagicMissiles",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_Shield",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_IronGrip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_Erase",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_InsatiableThirst",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_MagicalAura",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_Ventriloquate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_SpiderClimb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_WizardsChest",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_RepairingAura",false);
		
		// level 2
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Blur",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Infravision",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_DetectUndead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_ObscureSelf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_LightenItem",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_SummonSteed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_ResistPoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_EnchantArrows",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Enlarge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Shrink",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Light",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Clog",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_AnimateItem",false);
		// level 3
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_KnowAlignment",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_DeadenSmell",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_DetectMetal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_Dream",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_ResistGas",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_Grease",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_SummoningWard",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_SummonCompanion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_WizardLock",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_Deafness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_Knock",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_FeatherFall",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_Sleep",false);
		// level 4
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_FakeFood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_FakeSpring",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_Frost",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_AcidArrow",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_Hunger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_DetectWater",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_DetectPoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_ResistAcid",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_MageArmor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_BurningHands",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_Friends",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_WaterBreathing",false);
		// level 5
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DetectMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_FloatingDisc",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DetectGold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_WallOfStone",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_Spook",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CharmWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_ResistCold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_Mend",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_WeaknessGas",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DispelMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_FaerieFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_MysticShine",false);
		// clan magic
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqAcid",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqCold",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqElectric",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqFire",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqGas",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqMind",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqParalysis",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqPoison",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqWater",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_CEqDisease",0,"",false,true);
		// level 6
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_WeaknessAcid",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_Augury",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_Charm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_Meld",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_IllusoryWall",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_FindFamiliar",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_StinkingCloud",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_DetectInvisible",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_ResistElectricity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_GraceOfTheCat",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Spell_ShockingGrasp",false);
		// level 7
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_WeaknessCold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Darkness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Invisibility",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_GhostSound",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_LightSensitivity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Tourettes",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Refit",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_ResistFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_DetectHidden",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_PassDoor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_Fireball",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Spell_SummonMonster",false);
		// level 8
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_WeaknessElectricity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_FaerieFog",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_Lightning",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_ResistDisease",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_TeleportationWard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_Shatter",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_ElementalStorm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_WallOfDarkness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_Fear",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",false);
		// level 9
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_WeaknessFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Blindness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Mirage",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Awe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_ComprehendLangs",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_MagicMouth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Daydream",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_IceSheet",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Farsight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Flameshield",false);
		// level 10
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_LocateObject",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_Slow",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_MassSleep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_ArcaneMark",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_PhantomHound",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_ResistArrows",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_Teleport",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_GustOfWind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_Ugliness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_Mute",false);
		// level 11
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Claireaudience",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Feeblemind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Stoneskin",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_WallOfIce",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_AcidFog",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_HeatMetal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Laughter",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Weaken",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_MirrorImage",false);
		// level 12
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Clairevoyance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_ChangeSex",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Haste",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_DestroyObject",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Enlightenment",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_MinorGlobe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Shelter",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Confusion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Delirium",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Fly",false);
		//level 13
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_DistantVision",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_Reinforce",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_ResistParalyzation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_FeignDeath",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_Earthquake",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_Gate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_GiantStrength",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_Frenzy",false);
		// level 14
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_Nondetection",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_KnowValue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_Silence",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_ArcanePossession",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_WallOfAir",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_AnimateWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_MassInvisibility",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Spell_Scribe",false);
		// level 15
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Advancement",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Ensnare",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Hold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_FakeWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Blink",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_Polymorph",false);
		// level 16
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_ChainLightening",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_Choke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_Dismissal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_Meditation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_MageClaws",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_FreeMovement",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_PredictWeather",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_FakeArmor",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_Youth",0,"",false,true);
		// level 17
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_ClarifyScroll",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_FoolsGold",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_WallOfFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_Siphon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_Portal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Spell_RechargeWand",false);
		// level 18
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_SeeAura",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_ReverseGravity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_Web",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_Delude",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_ResistPetrification",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_Summon",false);
		// level 19
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_FleshStone",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Forget",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_StoneFlesh",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_WallOfForce",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_ImprovedInvisibility",false);
		// level 20
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Scry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_ImprovedPolymorph",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Nightmare",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Cloudkill",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_Repulsion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);
		// level 21
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_Command",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_Immunity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_SummonEnemy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Spell_MassHaste",false);
		// level 22
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_Disenchant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_MajorGlobe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_Hungerless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_MeteorStorm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_DetectSentience",false);
		// level 23
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Dragonfire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_MindBlock",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Cogniportive",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_TimeStop",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_CombatPrecognition",false);
		// level 24
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Delay",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Frailty",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Boomerang",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Thirstless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_SummonFlyer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Anchor",false);
		// level 25
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_SpellTurning",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_Disintegrate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_DemonGate",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_MassFly",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_Geas",false);

		// level 30
		if(ID().equals(baseClass()))
			CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_Spellbinding",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Intelligence to become a Mage.");
				return false;
			}
			if(!(mob.charStats().getMyRace().racialCategory().equals("Human"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Elf"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Gith"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Dragon"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Humanoid"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Illithid"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Gnome"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("Fairy-kin"))
			&& !(mob.charStats().getMyRace().racialCategory().equals("HalfElf")))
			{
				if(!quiet)
					mob.tell("You need to be Human, Elf, Gnome, or Half Elf to be a Mage.");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
    
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(!grantSomeSpells())
			return;

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
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==level)
			&&((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())<=25)
			&&(!CMLib.ableMapper().getSecretSkill(ID(),true,A.ID()))
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)))
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
			String AID=(String)grantable.elementAt(CMLib.dice().roll(1,grantable.size(),-1));
			if(AID!=null)
			{
				grantable.removeElement(AID);
				giveMobAbility(mob,
							   CMClass.getAbility(AID),
							   CMLib.ableMapper().getDefaultProficiency(ID(),true,AID),
							   CMLib.ableMapper().getDefaultParm(ID(),true,AID),
							   isBorrowedClass);
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
}
