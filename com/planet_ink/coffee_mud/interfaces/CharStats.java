package com.planet_ink.coffee_mud.interfaces;
public interface CharStats extends Cloneable
{
	
	public final static double AVG_VALUE=10.0;
	public int getStrength();
	public int getDexterity();
	public int getConstitution();
	public int getWisdom();
	public int getIntelligence();
	public int getCharisma();
	public void setStrength(int newVal);
	public void setDexterity(int newVal);
	public void setConstitution(int newVal);
	public void setWisdom(int newVal);
	public void setIntelligence(int newVal);
	public void setCharisma(int newVal);
	
	// physical and static properties
	public char getGender();
	public CharClass getMyClass();
	public Race getMyRace();
	public void setGender(char newGender);
	public void setMyClass(CharClass newVal);
	public void setMyRace(Race newVal);
	
	public static final int STRENGTH=0;
	public static final int INTELLIGENCE=1;
	public static final int DEXTERITY=2;
	public static final int CONSTITUTION=3;
	public static final int CHARISMA=4;
	public static final int WISDOM=5;
	
	public static final String[] TRAITS=
	{
		"STRENGTH",
		"INTELLIGENCE",
		"DEXTERITY",
		"CONSTITUTION",
		"CHARISMA",
		"WISDOM"
	};
	
	// create a new one of these
	public CharStats cloneCharStats();
	
	public String himher();
	public String hisher();
	public String heshe();
	public String HeShe();
	public void reRoll();
	public StringBuffer getStats(int maxStat[]);
	public int getCurStat(int abilityCode);
	public int getCurStat(String abilityName);
	public int getAbilityCode(String abilityName);
}
