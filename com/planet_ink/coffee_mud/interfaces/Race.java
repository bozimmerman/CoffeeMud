package com.planet_ink.coffee_mud.interfaces;

public interface Race
{
	public String ID();
	public String name();
	public boolean playerSelectable();
	public void newCharacter(MOB mob);
	public void setWeight(MOB mob);
	
	/** some general statistics about such an item
	 * see class "Stats" for more information. */
	public void affectEnvStats(Environmental affected, Stats affectableStats);
	public void affectCharStats(MOB affectedMob, CharStats affectableStats);
	
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect);
	
	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(Affect affect);
	
	public void level(MOB mob);
}
