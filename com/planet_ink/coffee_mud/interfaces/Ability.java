package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Ability extends Environmental 
{
	/** If it applies, the number of uses remaining
	 * for this Ability */
	public int usesRemaining();
	public void setUsesRemaining(int newUses);
	
	public MOB invoker();
	public Vector triggerStrings();
	public boolean invoke(MOB mob, Vector commands);
	public boolean invoke(MOB mob, Environmental target, boolean automatic);
	public boolean autoInvocation(MOB mob);
	public void unInvoke();
	public boolean canBeUninvoked();
	
	public boolean canBeTaughtBy(MOB mob);
	public boolean canBePracticedBy(MOB teacher, MOB student);
	public boolean canBeLearnedBy(MOB teacher, MOB student);
	public void teach(MOB teacher, MOB student);
	public void practice(MOB teacher, MOB student);
	
	public boolean qualifies(MOB student);
	public int qualifyingLevel(MOB student);
	
	public void startTickDown(Environmental affected, long tickTime);
	
	public int profficiency();
	public void setProfficiency(int newProfficiency);
	public boolean profficiencyCheck(int adjustment);
	
	public Environmental affecting();
	public void setAffectedOne(Environmental being);
	
	public int classificationCode();
	public boolean putInCommandlist();
	public boolean isMalicious();
	
	public static final int SKILL=0;
	public static final int SPELL=1;
	public static final int PRAYER=2;
	public static final int SONG=3;
	public static final int TRAP=4;
}
