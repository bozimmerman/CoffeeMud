package com.planet_ink.coffee_mud.interfaces;

public interface Drink extends Environmental
{
	public int thirstQuenched();
	public int liquidHeld();
	public int liquidRemaining();
	public int liquidType();
		
	public void setThirstQuenched(int amount);
	public void setLiquidHeld(int amount);
	public void setLiquidRemaining(int amount);
	
	public boolean containsDrink();
}
