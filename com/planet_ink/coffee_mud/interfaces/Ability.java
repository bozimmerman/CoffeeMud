package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Ability  extends Environmental
{
	// general classifications
	public static final int SKILL=0;
	public static final int SPELL=1;
	public static final int PRAYER=2;
	public static final int SONG=3;
	public static final int TRAP=4;
	public static final int PROPERTY=5;
	public static final int THIEF_SKILL=6;
	public static final int LANGUAGE=7;
	public static final int CHANT=8;
	public static final int COMMON_SKILL=9;
	public static final int ALL_CODES=31;
	public static final String[] TYPE_DESCS={
		"SKILL","SPELL","PRAYER","SONG","TRAP","PROPERTY","THIEF SKILL","LANGUAGE","CHANT","COMMON SKILL"
	};
	
	// domains
	public static final int DOMAIN_DIVINATION=1<<5;
	public static final int DOMAIN_ABJURATION=2<<5;
	public static final int DOMAIN_ILLUSION=3<<5;
	public static final int DOMAIN_EVOCATION=4<<5;
	public static final int DOMAIN_ALTERATION=5<<5;
	public static final int DOMAIN_TRANSMUTATION=6<<5;	
	public static final int DOMAIN_ENCHANTMENT=7<<5;	
	public static final int DOMAIN_CONJURATION=8<<5;
	public static final String[] DOMAIN_DESCS={
		"NOTHING","DIVINATION","ABJURATION","ILLUSION","INVOCATION/EVOCATION","ALTERATION",
		"TRANSMUTATION","ENCHANTMENT/CHARM","CONJURATION"
	};
	
	// flag
	public static final int CAN_MOBS=1;
	public static final int CAN_ITEMS=2;
	public static final int CAN_AREAS=4;
	public static final int CAN_ROOMS=8;
	public static final int CAN_EXITS=16;
	
	
	
	public static final int ALL_DOMAINS=(255<<5);
	// the classification incorporates the above
	public int classificationCode();
	
	// qualities
	public static final int MALICIOUS=0;
	public static final int INDIFFERENT=1;
	public static final int OK_SELF=2;
	public static final int OK_OTHERS=3;
	public static final int BENEFICIAL_SELF=4;
	public static final int BENEFICIAL_OTHERS=5;
	// the quality is used for more intelligent
	// usage by mobs.  it returns one of the above
	public int quality();
	// who is responsible for initiating this affect?
	public MOB invoker();
	// who (or what) is being affected by the abilitys use?
	public Environmental affecting();
	public void setAffectedOne(Environmental being);
	
	// whether or not this ability is also a command
	// most skills are, properties never are
	public boolean putInCommandlist();
	
	// the initial command word to activate this ability
	// or its brethren (cast, trip, etc..)
	public String[] triggerStrings();
	
	// when a mob uses an ability manually, this is the method
	// to make it happen.
	public boolean invoke(MOB mob, Environmental target, boolean auto);
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto);
	
	// for affects which may be uninvoked, this will do the trick!
	public void unInvoke();
	public boolean canBeUninvoked();
	public void makeNonUninvokable();

	// for abilities which are on a timer, this method will 
	// make those abilities last a very long time (until reboot perhaps)
	public void makeLongLasting();
	
	// an autoinvocating effect is an ability which affects the 
	// mob just by having the ability.  Dodge is an example of this.
	// the autoinvacation method is called to initiate this.
	// this method should be called WHENEVER an ability is 
	// added to a MOB for the first time.
	public boolean autoInvocation(MOB mob);
	public boolean isAutoInvoked();
	public boolean isNowAnAutoEffect();
	
	// a borrowed ability is one derived from some other source
	// than the mobs knowledge, such as a magic item, or 
	// a class behavior.  borrowed abilities are not saved, 
	// and neither are borrowed properties or affects.
	public boolean isBorrowed(Environmental toMe);
	public void setBorrowed(Environmental toMe, boolean truefalse);
	
	/** If it applies, the number of uses remaining
	 * for this Ability */
	public int usesRemaining();
	public void setUsesRemaining(int newUses);
	
	// methods to assist in teaching and learning the
	// abilities
	public boolean canBeTaughtBy(MOB teacher, MOB student);
	public boolean canBePracticedBy(MOB teacher, MOB student);
	public boolean canBeLearnedBy(MOB teacher, MOB student);
	public void teach(MOB teacher, MOB student);
	public void practice(MOB teacher, MOB student);
	public String requirements();

	public boolean canTarget(Environmental E);
	public boolean canAffect(Environmental E);

	// for use by the identify spell, this should return a
	// nice description of any properties incorporated
	// by this affect
	public String accountForYourself();

	// For clerics, usually, whether this ability is
	// appropriate for the mob using it
	public boolean appropriateToMyAlignment(int alignment);
	public int adjustedLevel(MOB mob);

	// intelligently add this ability as an affect upon a target,
	// and start a new clock (if necessary), setting the timer
	// on the affect as needed.
	public void startTickDown(Environmental affected, int tickTime);

	// as an ability, how profficient the mob is at it.
	public int profficiency();
	public void setProfficiency(int newProfficiency);
	public boolean profficiencyCheck(int adjustment, boolean auto);
	public void helpProfficiency(MOB mob);
}
