package com.planet_ink.coffee_mud.interfaces;
public interface CharState extends Cloneable
{
	public final static int ANNOYANCE_DEFAULT_TICKS=30;
	public final static int ADJUST_FACTOR=4;
	
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
	
	public void adjState(MOB mob, CharState maxState);
	
	public void expendEnergy(MOB mob, CharState maxState, boolean moving);
	
	// create a new one of these
	public CharState cloneCharState();
}
