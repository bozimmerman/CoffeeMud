package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;
public interface Race extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public String ID();
	public String name();
	public String racialCategory();
	public Race copyOf();
	
	public boolean playerSelectable();
	public void startRacing(MOB mob, boolean verifyOnly);
	
	public void setHeightWeight(EnvStats stats, char gender);
	public int shortestMale();
	public int shortestFemale();
	public int heightVariance();
	public int lightestWeight();
	public int weightVariance();
	public int getMaxWeight();
	public long forbiddenWornBits();
	public int[] bodyMask();
	public boolean fertile();
	public void outfit(MOB mob);
	public String healthText(MOB mob);
	public Weapon myNaturalWeapon();
	public Vector myResources();
	
	public DeadBody getCorpse(MOB mob, Room room);
	public void reRoll(MOB mob, CharStats C);
	
	public boolean isGeneric();
	public String racialParms();
	public void setRacialParms(String parms);
	
	public String arriveStr();
	public String leaveStr();
	
	public void level(MOB mob);
	
	public String[] getStatCodes();
	public String getStat(String code);
	public void setStat(String code, String val);
	public boolean sameAs(Race E);
	
	public Vector racialAbilities(MOB mob);
	
	public final static int BODY_ANTENEA=0;
	public final static int BODY_EYE=1;
	public final static int BODY_EAR=2;
	public final static int BODY_HEAD=3;
	public final static int BODY_NECK=4;
	public final static int BODY_ARM=5;
	public final static int BODY_HAND=6;
	public final static int BODY_TORSO=7;
	public final static int BODY_LEG=8;
	public final static int BODY_FOOT=9;
	public final static int BODY_NOSE=10;
	public final static int BODY_GILL=11;
	public final static int BODY_MOUTH=12;
	public final static int BODY_WAIST=13;
	public final static int BODY_TAIL=14;
	public final static int BODY_WING=15;
	public final static int BODY_PARTS=16;
	public final static int BODY_OTHERMASKCODE=1048576;
	public final static String[] BODYPARTSTR={
		"ANTENEA","EYE","EAR","HEAD","NECK","ARM","HAND","TORSO","LEG","FOOT",
		"NOSE","GILL","MOUTH","WAIST","TAIL","WING"};
	
	public final static long[][] BODY_WEARGRID={
		{Item.ON_HEAD,-1}, // ANTENEA, having any of these removes that pos
		{Item.ON_EYES,2}, // EYES, having any of these adds this position
		{Item.ON_EARS,2}, // EARS, gains a wear position here for every 2
		{Item.ON_HEAD,1}, // HEAD, gains a wear position here for every 1
		{Item.ON_NECK,1}, // NECK, gains a wear position here for every 1
		{Item.ON_ARMS,2}, // ARMS, gains a wear position here for every 2
		{Item.WIELD|Item.HELD|Item.ON_HANDS
	     |Item.ON_LEFT_FINGER|Item.ON_LEFT_WRIST
		 |Item.ON_RIGHT_FINGER|Item.ON_RIGHT_WRIST,1}, // HANDS, gains a wear position here for every 1 
			// lots of exceptions apply to the above
		{Item.ON_TORSO|Item.ON_BACK,1}, // TORSO, gains a wear position here for every 1
		{Item.ON_LEGS,2}, // LEGS, gains a wear position here for every 2
		{Item.ON_FEET,2}, // FEET, gains a wear position here for every 2
		{-1,-1}, // NOSE, No applicable wear position for this body part
		{-1,-1}, // GILLS, No applicable wear position for this body part
		{Item.ON_MOUTH,1}, // MOUTH, gains a wear position here for every 1
		{Item.ON_WAIST,1}, // WAIST, gains a wear position here for every 1
		{Item.ON_LEGS,-1}, // TAIL, having any of these removes that pos
		{Item.ON_BACK,-1}, // WINGS, having any of these removes that pos
	};
}
