package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Wand extends MiscMagic
{
	public boolean useTheWand(Ability A, MOB mob);
	public void waveIfAble(MOB mob, 
						   Environmental afftarget, 
						   String message, 
						   Wand me);
	public String magicWord();
	public void setSpell(Ability theSpell);
	public Ability getSpell();
    public int maxUses();
    public void setMaxUses(int maxUses);
}
