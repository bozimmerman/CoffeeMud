package com.planet_ink.coffee_mud.interfaces;

public interface Affect
{
	public int targetType();
	public int targetCode();
	public String targetMessage();

	public int sourceType();
	public int sourceCode();
	public String sourceMessage();

	public int othersType();
	public int othersCode();
	public String othersMessage();

	public Environmental target();
	public Environmental tool();
	public MOB source();

	public boolean amITarget(Environmental thisOne);
	public boolean amISource(MOB thisOne);

	public boolean wasModified();
	public void tagModified(boolean newStatus);

	public void modify(MOB source,
						Environmental target,
						Environmental tool,
						int newSourceCode,
						String sourceMessage,
						int newTargetCode,
						String targetMessage,
						int newOthersCode,
						String othersMessage);

	// target and others major types
	public static final int GENERAL=256;
	public static final int HANDS=512;
	public static final int VISUAL=1024;
	public static final int SOUND=2048;
	public static final int AIR=4096;
	public static final int TASTE=8192;
	public static final int MOVE=16384;
	public static final int AREA=32768;
	public static final int STRIKE=65536;

	public static final int COMBINED=GENERAL+AREA+MOVE+TASTE+AIR+SOUND+VISUAL+HANDS+STRIKE;

	// all major types
	public static final int NO_EFFECT=0;

	public static final int HANDS_GENERAL=HANDS+0;
	public static final int HANDS_RECALL=HANDS+1;
	public static final int HANDS_HOLD=HANDS+2;
	public static final int HANDS_PUSH=HANDS+3;
	public static final int HANDS_PULL=HANDS+4;
	public static final int HANDS_OPEN=HANDS+5;
	public static final int HANDS_CLOSE=HANDS+6;
	public static final int HANDS_PUT=HANDS+7;
	public static final int HANDS_GET=HANDS+8;
	public static final int HANDS_UNLOCK=HANDS+9;
	public static final int HANDS_LOCK=HANDS+10;
	public static final int HANDS_WIELD=HANDS+11;
	public static final int HANDS_GIVE=HANDS+12;
	public static final int HANDS_BUY=HANDS+13;
	public static final int HANDS_SELL=HANDS+14;
	public static final int HANDS_DROP=HANDS+15;
	public static final int HANDS_WEAR=HANDS+16;
	public static final int HANDS_FILL=HANDS+17;
	public static final int HANDS_DELICATE=HANDS+18;

	public static final int VISUAL_WNOISE=VISUAL+0;
	public static final int VISUAL_ONLY=VISUAL+1;
	//public static final int VISUAL_ONLY=VISUAL+2;
	public static final int VISUAL_LOOK=VISUAL+3;
	public static final int VISUAL_READ=VISUAL+4;

	public static final int SOUND_NOISE=SOUND+0;
	public static final int SOUND_WORDS=SOUND+1;
	public static final int SOUND_MAGIC=SOUND+2;
	public static final int SOUND_LIST=SOUND+3;

	public static final int AIR_NORMAL=AIR+0;
	public static final int AIR_SMELL=AIR+1;
	public static final int AIR_WATER=AIR+2;

	public static final int TASTE_FOOD=TASTE+0;
	public static final int TASTE_WATER=TASTE+1;

	public static final int MOVE_GENERAL=MOVE+0;
	public static final int MOVE_ENTER=MOVE+1;
	public static final int MOVE_LEAVE=MOVE+2;
	public static final int MOVE_SLEEP=MOVE+4;
	public static final int MOVE_SIT=MOVE+5;
	public static final int MOVE_STAND=MOVE+6;
	public static final int MOVE_FLEE=MOVE+7;

	public static final int AREA_MSG_1=AREA+0;
	public static final int AREA_MSG_2=AREA+1;
	public static final int AREA_MSG_3=AREA+2;
	public static final int AREA_MSG_4=AREA+3;
	public static final int AREA_MSG_5=AREA+4;

	public static final int STRIKE_HANDS=STRIKE+0;
	public static final int STRIKE_FIRE=STRIKE+1;
	public static final int STRIKE_COLD=STRIKE+2;
	public static final int STRIKE_WATER=STRIKE+3;
	public static final int STRIKE_GAS=STRIKE+4;
	public static final int STRIKE_MIND=STRIKE+5;
	public static final int STRIKE_MAGIC=STRIKE+6;
	public static final int STRIKE_JUSTICE=STRIKE+7;
	public static final int STRIKE_ACID=STRIKE+8;
	public static final int STRIKE_ELECTRIC=STRIKE+9;
	public static final int STRIKE_POISON=STRIKE+10;
}
