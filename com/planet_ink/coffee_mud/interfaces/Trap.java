package com.planet_ink.coffee_mud.interfaces;

public interface Trap extends Ability
{
	public final static int TRAP_NEEDLE=0;
	public final static int TRAP_PIT_BLADE=1;
	public final static int TRAP_GAS=2;
	public final static int TRAP_SPELL=3;

	public boolean disabled();
	public void disable();
	public void spring(MOB target);
	public boolean sprung();
	public void setReset(int reset);
	public int getReset();
	
	public boolean maySetTrap(MOB mob, int asLevel);
	public boolean canSetTrapOn(MOB mob, Environmental E);
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel);
	public String requiresToSet();
	
}
