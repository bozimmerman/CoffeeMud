package com.planet_ink.coffee_mud.interfaces;

public interface Electronics
{
	public int fuelType();
	public void setFuelType(int resource);
	
	public long powerCapacity();
	public void setPowerCapacity(long capacity);
	
	public long powerRemaining();
	public void setPowerRemaining(long remaining);
	
	public boolean activated();
	public void activate(boolean truefalse);
}
