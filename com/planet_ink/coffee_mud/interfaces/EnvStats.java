package com.planet_ink.coffee_mud.interfaces;

public interface EnvStats extends Cloneable
{
	// sensemask stuff
	public final static int CAN_NOT_SEE=1;
	public final static int CAN_SEE_HIDDEN=2;
	public final static int CAN_SEE_INVISIBLE=4;
	public final static int CAN_SEE_EVIL=8;
	public final static int CAN_SEE_GOOD=16;
	public final static int CAN_SEE_SNEAKERS=32;
	public final static int CAN_SEE_BONUS=64;
	public final static int CAN_SEE_DARK=128;
	public final static int CAN_SEE_INFRARED=256;
	public final static int CAN_NOT_HEAR=512;
	public final static int CAN_NOT_MOVE=1024;
	public final static int CAN_NOT_SMELL=2048;
	public final static int CAN_NOT_TASTE=4096;
	public final static int CAN_NOT_SPEAK=8192;
	public final static int CAN_NOT_BREATHE=16384;
	public final static int CAN_SEE_VICTIM=32768;
	public final static int CAN_SEE_METAL=65536;

	public final static long ALLMASK=Integer.MAX_VALUE;
	
	// dispositions
	public final static int IS_NOT_SEEN=1;
	public final static int IS_HIDDEN=2;
	public final static int IS_INVISIBLE=4;
	public final static int IS_EVIL=8;
	public final static int IS_GOOD=16;
	public final static int IS_SNEAKING=32;
	public final static int IS_BONUS=64;
	public final static int IS_DARK=128;
	public final static int IS_GOLEM=256;
	public final static int IS_SLEEPING=512;
	public final static int IS_SITTING=1024;
	public final static int IS_FLYING=2048;
	public final static int IS_SWIMMING=4096;
	public final static int IS_GLOWING=8192;
	public final static int IS_CLIMBING=16384;
	public final static int IS_FALLING=32768;
	public final static int IS_LIGHTSOURCE=65536;

	public int sensesMask(); // mobs
	public int disposition(); // items, mobs
	public int level(); // items, exits, mobs
	public int ability(); // items, mobs
	public int rejuv(); // items, mobs
	public int weight(); // items, mobs
	public int height(); // items, mobs
	public int armor(); // armor items, mobs
	public int damage(); // weapon items, mobs
	public double speed(); // mobs
	public int attackAdjustment(); // weapon items, mobs
	public String replacementName(); // items, mobs
	
	public void setRejuv(int newRejuv);
	public void setLevel(int newLevel);
	public void setArmor(int newArmor);
	public void setDamage(int newDamage);
	public void setWeight(int newWeight);
	public void setSpeed(double newSpeed);
	public void setAttackAdjustment(int newAdjustment);
	public void setAbility(int newAdjustment);
	public void setDisposition(int newDisposition);
	public void setSensesMask(int newMask);
	public void setReplacementName(String newName);
	public void setHeight(int newHeight);
	
	public String[] getCodes();
	public void setStat(String code, String val);
	public String getStat(String code);
	public boolean sameAs(EnvStats E);

	public EnvStats cloneStats();
	
	public static final String[] sensesNames={"CANNOTSEE",
											  "CANSEEHIDDEN",
											  "CANSEEINVISIBLE",
											  "CANSEEEVIL",
											  "CANSEEGOOD",
											  "CANSEESNEAKERS",
											  "CANSEEBONUS",
											  "CANSEEDARK",
											  "CANSEEINFRARED",
											  "CANNOTHEAR",
											  "CANNOTMOVE",
											  "CANNOTSMELL",
											  "CANNOTTASTE",
											  "CANNOTSPEAK",
											  "CANNOTBREATHE",
											  "CANSEEVICTIM",
											  "CANSEEMETAL"};
	public static final String[] sensesDesc={"Is Blind",
											 "Can see hidden",
											 "Can see invisible",
											 "Can see evil",
											 "Can see good",
											 "Can detect sneakers",
											 "Can see magic",
											 "Can see in the dark",
											 "Has infravision",
											 "Is Deaf",
											 "Is Paralyzed",
											 "Can not smell",
											 "Can not eat",
											 "Is Mute",
											 "Can not breathe",
											 "Can detect victims",
											 "Can detect metal"};
	
	public static final String[] dispositionsNames={"ISSEEN",
													"ISHIDDEN",
													"ISINVISIBLE",
													"ISEVIL",
													"ISGOOD",
													"ISSNEAKING",
													"ISBONUS",
													"ISDARK",
													"ISGOLEM",
													"ISSLEEPING",
													"ISSITTING",
													"ISFLYING",
													"ISSWIMMING",
													"ISGLOWING",
													"ISCLIMBING",
													"ISFALLING",
													"ISLIGHT"};
	public static final String[] dispositionsDesc= {"Is never seen",
													"Is hidden",
													"Is invisible",
													"Evil aura",
													"Good aura",
													"Is sneaking",
													"Is magical",
													"Is dark",
													"Is golem",
													"Is sleeping",
													"Is sitting",
													"Is flying",
													"Is swimming",
													"Is glowing",
													"Is climbing",
													"Is falling",
													"Is a light source"};
}
