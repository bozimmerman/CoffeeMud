package com.planet_ink.coffee_mud.interfaces;

public interface Host
{
	public final static int TICK_TIME=4000;

	public static final int MOB_TICK=0;
	public static final int ITEM_BEHAVIOR_TICK=1;
	public static final int EXIT_REOPEN=2;
	public static final int DEADBODY_DECAY=3;
	public static final int LIGHT_FLICKERS=4;
	public static final int TRAP_RESET=5;
	public static final int TRAP_DESTRUCTION=6;
	public static final int ITEM_BOUNCEBACK=7;
	public static final int ROOM_BEHAVIOR_TICK=8;

	public final static int ROOM_ITEM_REJUV=10;


	public final static int SPELL_AFFECT=12;
	
	public final static int MAX_TICK_CLIENTS=32;
	
	public String getVer();
	public void shutdown(Session S, boolean keepItDown, String externalCommand);
}
