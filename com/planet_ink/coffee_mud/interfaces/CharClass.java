package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface CharClass extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public static final int ARMOR_ANY=0;
	public static final int ARMOR_CLOTH=1;
	public static final int ARMOR_LEATHER=2;
	public static final int ARMOR_NONMETAL=3;
	public static final int ARMOR_VEGAN=4;
	public static final int ARMOR_METALONLY=5;
	public static long ARMOR_WEARMASK=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;
	public static final String[] ARMOR_DESCS={
		"ANY","CLOTH","LEATHER","NONMETAL","VEGAN","METALONLY"
	};
	public static final String[] ARMOR_LONGDESC={
		"Any",
		"Must wear cloth, vegetation, or paper based armor.",
		"Must wear leather, cloth, or vegetation based armor.",
		"Must wear non-metal armor.",
		"Must wear wood or vegetation based armor.",
		"Must wear metal armor"
	};
	
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
	public Hashtable dispenseExperience(MOB killer, MOB killed);
	public void level(MOB mob);
	public void unLevel(MOB mob);
	public Vector outfit();
	
	public boolean armorCheck(MOB mob);
	public int classDurationModifier(MOB myChar, Ability skill, int duration);

	public MOB buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender);

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
}
