package com.planet_ink.coffee_mud.interfaces;

public interface DeadBody extends Container
{
	public void startTicker(Room thisRoom);
	public CharStats charStats();
	public void setCharStats(CharStats newStats);
}
  