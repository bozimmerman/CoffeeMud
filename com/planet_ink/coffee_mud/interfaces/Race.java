package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;
public interface Race extends Cloneable, Tickable, StatsAffecting, MsgListener, Comparable
{
	public String ID();
	public String name();
	public boolean playerSelectable();
	public void startRacing(MOB mob, boolean verifyOnly);
	public void setHeightWeight(EnvStats stats, char gender);
	public int getMaxWeight();
	public void outfit(MOB mob);
	public Weapon myNaturalWeapon();
	public String healthText(MOB mob);
	public DeadBody getCorpse(MOB mob, Room room);
	public Vector myResources();
	public String racialCategory();
	public void reRoll(MOB mob, CharStats C);
	public Race copyOf();
	public int[] bodyMask();
	
	public String arriveStr();
	public String leaveStr();

	public void level(MOB mob);
	
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
}
