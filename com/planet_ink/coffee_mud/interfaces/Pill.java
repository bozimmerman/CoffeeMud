package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Pill extends MiscMagic
{
	public Vector getSpells(Pill me);
	public String getSpellList();
	public void setSpellList(String list);
	public void eatIfAble(MOB mob, Pill me);
}
