package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
public interface CharClass extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public static final int ARMOR_ANY=0;
	public static final int ARMOR_CLOTH=1;
	public static final int ARMOR_LEATHER=2;
	public static final int ARMOR_NONMETAL=3;
	
	public String ID();
	public String name();
	public String baseClass();
	public boolean playerSelectable();
	public boolean qualifiesForThisClass(MOB mob, boolean quiet);
	public CharClass copyOf();

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly);
	public void gainExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);
	public void loseExperience(MOB mob, int amount);
	public Hashtable dispenseExperience(MOB killer, MOB killed);
	public void level(MOB mob);
	public void unLevel(MOB mob);
	public void outfit(MOB mob);
	
	public boolean armorCheck(MOB mob);
	public int classDurationModifier(MOB myChar, Ability skill, int duration);

	public void buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender);

	public boolean canAdvance(MOB mob, int abilityCode);
	public int getMaxStat(int abilityCode);
	public int getLevelMana(MOB mob);
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
	public int[] maxStat();
}
