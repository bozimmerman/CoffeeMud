package com.planet_ink.coffee_mud.interfaces;

public interface CagedAnimal
{
	public boolean cageMe(MOB M);
	public MOB unCageMe();
	public String cageText();
	public void setCageText(String text);
}
