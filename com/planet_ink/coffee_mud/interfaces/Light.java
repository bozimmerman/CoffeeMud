package com.planet_ink.coffee_mud.interfaces;

public interface Light extends Item
{
	public void setDuration(int duration);
	public int getDuration();
	public boolean destroyedWhenBurnedOut();
	public boolean goesOutInTheRain();
	public boolean isLit();
	public void light(boolean isLit);
	
}
