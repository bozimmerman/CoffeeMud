package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Potion extends MiscMagic
{
	public Vector getSpells(Potion me);
	public String getSpellList();
	public void setSpellList(String list);
	public boolean isDrunk();
	public void setDrunk(Potion me, boolean isTrue);
	public void drinkIfAble(MOB mob, Potion me);
}
