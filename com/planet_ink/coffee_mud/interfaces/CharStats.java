package com.planet_ink.coffee_mud.interfaces;
public interface CharStats extends Cloneable
{
	public static final int STRENGTH=0;
	public static final int INTELLIGENCE=1;
	public static final int DEXTERITY=2;
	public static final int CONSTITUTION=3;
	public static final int CHARISMA=4;
	public static final int WISDOM=5;
	public static final int NUM_BASE_STATS=6;

	public static final int GENDER=6;

	public static final int NUM_SAVE_START=7;
	public static final int SAVE_PARALYSIS=7;
	public static final int SAVE_FIRE=8;
	public static final int SAVE_COLD=9;
	public static final int SAVE_WATER=10;
	public static final int SAVE_GAS=11;
	public static final int SAVE_MIND=12;
	public static final int SAVE_GENERAL=13;
	public static final int SAVE_JUSTICE=14;
	public static final int SAVE_ACID=15;
	public static final int SAVE_ELECTRIC=16;
	public static final int SAVE_POISON=17;
	public static final int SAVE_UNDEAD=18;
	public static final int SAVE_MAGIC=19;
	public static final int SAVE_DISEASE=20;
	public static final int SAVE_TRAPS=21;

	public static final int MAX_STRENGTH_ADJ=22;
	public static final int MAX_INTELLIGENCE_ADJ=23;
	public static final int MAX_DEXTERITY_ADJ=24;
	public static final int MAX_CONSTITUTION_ADJ=25;
	public static final int MAX_CHARISMA_ADJ=26;
	public static final int MAX_WISDOM_ADJ=27;

	public final static int NUM_STATS=28;

	public static final String[] TRAITS=
	{
		"STRENGTH",
		"INTELLIGENCE",
		"DEXTERITY",
		"CONSTITUTION",
		"CHARISMA",
		"WISDOM",
		"GENDER",
		"PARALYSIS SAVE",
		"SAVE VS FIRE",
		"SAVE VS COLD",
		"SAVE VS WATER",
		"SAVE VS GAS",
		"SAVE VS MIND",
		"GENERAL SAVE",
		"JUSTICE SAVE",
		"SAVE VS ACID",
		"SAVE VS ELECTRICITY",
		"SAVE VS POISON",
		"SAVE VS UNDEAD",
		"SAVE VS MAGIC",
		"SAVE VS DISEASE",
		"SAVE VS TRAPS",
		"MAX STRENGTH ADJ.",
		"MAX INTELLIGENCE ADJ.",
		"MAX DEXTERITY ADJ.",
		"MAX CONSTITUTION ADJ.",
		"MAX CHARISMA ADJ.",
		"MAX WISDOM ADJ.",
	};

	public static final String[] TRAITABBR1=
	{
		"S",
		"I",
		"D",
		"C",
		"CH",
		"W",
		"G",
		"vPY",
		"vF",
		"vC",
		"vW",
		"vG",
		"vMI",
		"V",
		"vJ",
		"vA",
		"vE",
		"vP",
		"vU",
		"vM",
		"vD",
		"vT",
		"mS",
		"mI",
		"mD",
		"mC",
		"mCH",
		"mW"
	};
	public String getSavesStr();
	public void setSaves(String str);

	public int getBodyPart(int racialPartNumber);
	public void alterBodypart(int racialPartNumber, int number);
	public String getBodyPartStr();
	public void setBodyPartStrAfterRace(String str);

	public int getStat(int statNum);
	public int getStat(String abilityName);
	public void setStat(int statNum, int value);
	public void setPermaStat(int statNum, int value);
	public int getCode(String abilityName);
	public StringBuffer getStats();
	public int getSave(int which);

	// physical and static properties
	public int numClasses();
	public CharClass getMyClass(int i);
	public CharClass getCurrentClass();
	public void setMyClasses(String classes);
	public void setMyLevels(String levels);
	public String getMyClassesStr();
	public String getMyLevelsStr();
	public void setCurrentClass(CharClass aClass);
	public int getClassLevel(CharClass aClass);
	public int getClassLevel(String aClass);
	public int combinedSubLevels();
	public void setClassLevel(CharClass aClass, int level);
	public Race getMyRace();
	public void setMyRace(Race newVal);
	public String raceName();
	public void setRaceName(String newRaceName);

	public void setDisplayClassName(String newname);
	public void setDisplayClassLevel(String newlevel);
	public String displayClassName();
	public String displayClassLevel(MOB mob, boolean shortForm);

	public static int[] affectTypeMap={-1, // strength
									   -1, // intelligence
									   -1, // dexterity
									   -1, // constitution
									   -1, // charisma
									   -1, // wisdom
									   -1, // gender
										CMMsg.TYP_PARALYZE,
										CMMsg.TYP_FIRE,
										CMMsg.TYP_COLD,
										CMMsg.TYP_WATER,
										CMMsg.TYP_GAS,
										CMMsg.TYP_MIND,
										CMMsg.TYP_GENERAL,
										CMMsg.TYP_JUSTICE,
										CMMsg.TYP_ACID,
										CMMsg.TYP_ELECTRIC,
										CMMsg.TYP_POISON,
										CMMsg.TYP_UNDEAD,
									    CMMsg.TYP_CAST_SPELL,
										CMMsg.TYP_DISEASE,
										-1, // traps
										-1, // max str
										-1, // max int
										-1, // max
										-1, // max dex
										-1, // max con
										-1,	// max cha
									    -1, // max wis
									   };

	// create a new one of these
	public CharStats cloneCharStats();

	public void setGenderName(String gname);
	public String genderName();
	public String himher();
	public String hisher();
	public String heshe();
	public String HeShe();

}
