package com.planet_ink.coffee_mud.interfaces;

public interface Drink extends Item
{
	public int thirstQuenched();
	public int liquidHeld();
	public int liquidRemaining();
		
	public void setThirstQuenched(int amount);
	public void setLiquidHeld(int amount);
	public void setLiquidRemaining(int amount);
	
	public boolean containsDrink();
}
