package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
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
public interface CharClass extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public String ID();
	public String name();
	public String baseClass();
	public boolean playerSelectable();
	public boolean qualifiesForThisClass(MOB mob, boolean quiet);
	public String classParms();
	public void setClassParms(String parms);
	public CharClass copyOf();
	public boolean isGeneric();

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly);
	public void endCharacter(MOB mob);
	public void gainExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);
	public void loseExperience(MOB mob, int amount);
	public int getLevelExperience(int level);
	public HashSet dispenseExperience(MOB killer, MOB killed);
	public void level(MOB mob);
	public void unLevel(MOB mob);
	public Vector outfit();
	
	public int classDurationModifier(MOB myChar, Ability skill, int duration);

	public MOB fillOutMOB(MOB mob, int level);

	public int getLevelMana(MOB mob);
	public double getLevelSpeed(MOB mob);
	public int getLevelMove(MOB mob);
	public int getLevelAttack(MOB mob);
	public int getLevelArmor(MOB mob);
	public int getLevelDamage(MOB mob);
	public int getMinHitPointsLevel();
	public int getMaxHitPointsLevel();
	public int getBonusPracLevel();
	public int getBonusManaLevel();
	public int getBonusAttackLevel();
	public int getAttackAttribute();
	public int getPracsFirstLevel();
	public int getTrainsFirstLevel();
	public int getLevelsPerBonusDamage();
	public int getMovementMultiplier();
	public int getHPDivisor();
	public int getHPDice();
	public int getHPDie();
	public int getManaDivisor();
	public int getManaDice();
	public int getManaDie();
	public String weaponLimitations();
	public String armorLimitations();
	public String otherLimitations();
	public String otherBonuses();
	public String statQualifications();
	public int[] maxStatAdjustments();
	
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(CharClass E);
	
	
	public static final int ARMOR_ANY=0;
	public static final int ARMOR_CLOTH=1;
	public static final int ARMOR_LEATHER=2;
	public static final int ARMOR_NONMETAL=3;
	public static final int ARMOR_VEGAN=4;
	public static final int ARMOR_METALONLY=5;
	public static final int ARMOR_OREONLY=6;
	public static long ARMOR_WEARMASK=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;
	public static final String[] ARMOR_DESCS={
		"ANY","CLOTH","LEATHER","NONMETAL","VEGAN","METALONLY","OREONLY"
	};
	
	public static final String[] ARMOR_LONGDESC={
		"May wear any armor.",
		"Must wear cloth, vegetation, or paper based armor.",
		"Must wear leather, cloth, or vegetation based armor.",
		"Must wear non-metal armor.",
		"Must wear wood or vegetation based armor.",
		"Must wear metal armor",
		"Must wear stone, crystal, or metal armor."
	};
	
	public static final int WEAPONS_ANY=0;
	public static final int WEAPONS_DAGGERONLY=1;
	public static final int WEAPONS_THIEFLIKE=2;
	public static final int WEAPONS_NATURAL=3;
	public static final int WEAPONS_BURGLAR=4;
	public static final int WEAPONS_ROCKY=5;
	public static final int WEAPONS_MAGELIKE=6;
	public static final int WEAPONS_EVILCLERIC=7;
	public static final int WEAPONS_GOODCLERIC=8;
	public static final int WEAPONS_NEUTRALCLERIC=9;
	public static final int WEAPONS_ALLCLERIC=10;
	public static final int[][] WEAPONS_SETS={
/*0*/{Weapon.CLASS_AXE,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED,Weapon.CLASS_FLAILED,Weapon.CLASS_HAMMER,Weapon.CLASS_NATURAL,Weapon.CLASS_POLEARM,Weapon.CLASS_RANGED,Weapon.CLASS_STAFF,Weapon.CLASS_SWORD,Weapon.CLASS_THROWN},
/*1*/{Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER},
/*2*/{Weapon.CLASS_SWORD,Weapon.CLASS_RANGED,Weapon.CLASS_THROWN,Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER},
/*3*/{EnvResource.MATERIAL_WOODEN,EnvResource.MATERIAL_UNKNOWN,EnvResource.MATERIAL_VEGETATION,EnvResource.MATERIAL_FLESH,EnvResource.MATERIAL_LEATHER},
/*4*/{Weapon.CLASS_NATURAL,Weapon.CLASS_SWORD,Weapon.CLASS_FLAILED,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER},
/*5*/{EnvResource.MATERIAL_ROCK,EnvResource.MATERIAL_UNKNOWN,EnvResource.MATERIAL_GLASS,EnvResource.MATERIAL_METAL,EnvResource.MATERIAL_MITHRIL,EnvResource.MATERIAL_PRECIOUS},
/*6*/{Weapon.CLASS_NATURAL,Weapon.CLASS_DAGGER,Weapon.CLASS_STAFF},
/*7*/{Weapon.CLASS_EDGED,Weapon.CLASS_POLEARM,Weapon.CLASS_AXE,Weapon.CLASS_SWORD,Weapon.CLASS_DAGGER},
/*8*/{Weapon.CLASS_BLUNT,Weapon.CLASS_HAMMER,Weapon.CLASS_FLAILED,Weapon.CLASS_NATURAL,Weapon.CLASS_STAFF},
/*9*/{Weapon.CLASS_BLUNT,Weapon.CLASS_RANGED,Weapon.CLASS_THROWN,Weapon.CLASS_STAFF,Weapon.CLASS_NATURAL,Weapon.CLASS_SWORD},
/*10*/{Weapon.CLASS_AXE,Weapon.CLASS_BLUNT,Weapon.CLASS_DAGGER,Weapon.CLASS_EDGED,Weapon.CLASS_FLAILED,Weapon.CLASS_HAMMER,Weapon.CLASS_NATURAL,Weapon.CLASS_POLEARM,Weapon.CLASS_RANGED,Weapon.CLASS_STAFF,Weapon.CLASS_SWORD,Weapon.CLASS_THROWN},
	};
	public static final String[] WEAPONS_LONGDESC={
/*0*/"May use any weapons.",
/*1*/"Must use dagger-like or natural weapons.",
/*2*/"Must use swords, daggers, natural, or ranged weapons.",
/*3*/"Must use wooden, plant-based, or leather weapons.",
/*4*/"Must use sword, daggers, flailed, blunt, or natural weapons.",
/*5*/"Must use stone, crystal, metal, or glass weapons.",
/*6*/"Must use daggers, staves, or natural weapons.",
/*7*/"Must use polearms, axes, swords, daggers, or edged weapons.",
/*8*/"Must use hammers, staves, flailed, natural, or blunt weapons.",
/*9*/"Must use swords, staves, natural, ranged, or blunt weapons",
/*10*/"Evil must use polearm, sword, axe, edged, or natural.  Neutral must use blunt, ranged, thrown, staff, natural, or sword.  Good must use blunt, flailed, natural, staff, or hammer."
	};
		
}
