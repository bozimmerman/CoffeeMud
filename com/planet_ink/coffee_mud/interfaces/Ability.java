package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Ability  extends Environmental
{
	/** If it applies, the number of uses remaining
	 * for this Ability */
	public int usesRemaining();
	public void setUsesRemaining(int newUses);
	
	public MOB invoker();
	public Vector triggerStrings();
	public boolean invoke(MOB mob, Environmental target, boolean auto);
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto);
	public boolean autoInvocation(MOB mob);
	
	public void makeNonUninvokable();
	public void unInvoke();
	public boolean canBeUninvoked();
	public boolean isAnAutoEffect();
	public boolean isBorrowed(Environmental toMe);
	public void setBorrowed(Environmental toMe, boolean truefalse);
	
	public boolean canBeTaughtBy(MOB teacher, MOB student);
	public boolean canBePracticedBy(MOB teacher, MOB student);
	public boolean canBeLearnedBy(MOB teacher, MOB student);
	public void teach(MOB teacher, MOB student);
	public void practice(MOB teacher, MOB student);
	
	public String accountForYourself();
	
	public boolean qualifies(MOB student);
	public int qualifyingLevel(MOB student);
	public boolean appropriateToMyAlignment(MOB mob);
	
	public void startTickDown(Environmental affected, long tickTime);
	public void makeLongLasting();
	
	public int profficiency();
	public void setProfficiency(int newProfficiency);
	public boolean profficiencyCheck(int adjustment, boolean auto);
	public void helpProfficiency(MOB mob);

	public Environmental affecting();
	public void setAffectedOne(Environmental being);
	
	public int classificationCode();
	public boolean putInCommandlist();
	public int quality();
	
	// general classifications
	public static final int SKILL=0;
	public static final int SPELL=1;
	public static final int PRAYER=2;
	public static final int SONG=3;
	public static final int TRAP=4;
	public static final int PROPERTY=5;
	public static final int THIEF_SKILL=6;
	public static final int LANGUAGE=7;
	public static final int ALL_CODES=31;
	
	// domains
	public static final int DOMAIN_DIVINATION=1<<5;
	public static final int DOMAIN_ABJURATION=2<<5;
	public static final int DOMAIN_ILLUSION=3<<5;
	public static final int DOMAIN_EVOCATION=4<<5;
	public static final int DOMAIN_ALTERATION=5<<5;
	public static final int DOMAIN_TRANSMUTATION=6<<5;	
	public static final int DOMAIN_ENCHANTMENT=7<<5;	
	public static final int DOMAIN_CONJURATION=8<<5;	

	// qualities
	public static final int MALICIOUS=0;
	public static final int INDIFFERENT=1;
	public static final int OK_SELF=2;
	public static final int OK_OTHERS=3;
	public static final int BENEFICIAL_SELF=4;
	public static final int BENEFICIAL_OTHERS=5;
}
