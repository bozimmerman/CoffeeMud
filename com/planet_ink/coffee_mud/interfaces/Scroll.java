package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Scroll extends MiscMagic, Item
{
	
	public boolean useTheScroll(Ability A, MOB mob);
	public int numSpells();
	public Vector getSpells();
	public void setScrollText(String text);
	public String getScrollText();
	public boolean isReadableScroll();
	public void setReadableScroll(boolean isTrue);
	public void setSpellList(Vector newOne);
	public void readIfAble(MOB mob, Scroll me, String spellName);
	public void parseSpells(Scroll me, String names);
}
