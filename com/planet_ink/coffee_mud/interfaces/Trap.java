package com.planet_ink.coffee_mud.interfaces;

public interface Trap extends Ability
{
	public final static int TRAP_NEEDLE=0;
	public final static int TRAP_PIT_BLADE=1;
	public final static int TRAP_GAS=2;
	public final static int TRAP_SPELL=3;

	public boolean sprung();
	public void setSprung(boolean isSprung);
	public void spring(MOB target);
	public void setReset(int reset);
	public int getReset();
}
