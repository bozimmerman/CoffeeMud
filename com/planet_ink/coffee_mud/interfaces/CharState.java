package com.planet_ink.coffee_mud.interfaces;
public interface CharState extends Cloneable
{
	public final static int ANNOYANCE_DEFAULT_TICKS=30;
	public final static int ADJUST_FACTOR=5;
	public final static int DEATH_THIRST_TICKS=(30*30)*6; // 6 hours
	public final static int DEATH_HUNGER_TICKS=(30*30)*12; // 12 hours
	public final static long REST_PER_TICK=MudHost.TICK_TIME*200;
	public final static long FATIGUED_MILLIS=MudHost.TICK_TIME*3000;

	public long getFatigue();
	public void setFatigue(long newVal);
	public boolean adjFatigue(long byThisMuch, CharState max);

	public int getHitPoints();
	public void setHitPoints(int newVal);
	public boolean adjHitPoints(int byThisMuch, CharState max);

	public int getHunger();
	public void setHunger(int newVal);
	public boolean adjHunger(int byThisMuch, CharState max);

	public int getThirst();
	public void setThirst(int newVal);
	public boolean adjThirst(int byThisMuch, CharState max);

	public int getMana();
	public void setMana(int newVal);
	public boolean adjMana(int byThisMuch, CharState max);

	public int getMovement();
	public void setMovement(int newVal);
	public boolean adjMovement(int byThisMuch, CharState max);

	public void recoverTick(MOB mob, CharState maxState);

	public void expendEnergy(MOB mob, CharState maxState, boolean expendMovement);

	// create a new one of these
	public CharState cloneCharState();
	public String[] getCodes();
	public void setStat(String code, String val);
	public String getStat(String code);
	public boolean sameAs(EnvStats E);
}
