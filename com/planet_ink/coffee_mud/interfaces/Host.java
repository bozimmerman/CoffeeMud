package com.planet_ink.coffee_mud.interfaces;
import java.util.Properties;

public interface Host
{
	public final static long TICK_TIME=4000;
	public final static long TIME_TICK_DELAY=10*60000; // 10 minutes, right now.
	public final static long TIME_SAVE_DELAY=18; // 3 hours...
	public final static long TICKS_PER_RLMIN=(int)Math.round(60000.0/new Long(TICK_TIME).doubleValue());
	public final static long TICKS_PER_MUDDAY=(TIME_TICK_DELAY*Area.A_FULL_DAY)/TICK_TIME;

	public static final int MOB_TICK=0;
	public static final int ITEM_BEHAVIOR_TICK=1;
	public static final int EXIT_REOPEN=2;
	public static final int DEADBODY_DECAY=3;
	public static final int LIGHT_FLICKERS=4;
	public static final int TRAP_RESET=5;
	public static final int TRAP_DESTRUCTION=6;
	public static final int ITEM_BOUNCEBACK=7;
	public static final int ROOM_BEHAVIOR_TICK=8;
	public static final int AREA_TICK=9;
	public final static int ROOM_ITEM_REJUV=10;
	public static final int EXIT_BEHAVIOR_TICK=11;
	public final static int SPELL_AFFECT=12;
	public final static int QUEST_TICK=13;
	public final static int CLAN_TICK=14;
	
	public final static int MAX_TICK_CLIENTS=32;
	
	public String getVer();
	public boolean isGameRunning();
	public void shutdown(Session S, boolean keepItDown, String externalCommand);
	public int getPort();
	public String getPortStr();
	public String gameStatusStr();
	public String ServerVersionString();
	public Properties getCommonPropPage();
	public void setGameStatusStr(String str);
}
