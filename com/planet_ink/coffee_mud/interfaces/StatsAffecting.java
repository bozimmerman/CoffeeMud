package com.planet_ink.coffee_mud.interfaces;

public interface StatsAffecting
{
	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats);
	public void affectCharStats(MOB affectedMob, CharStats affectableStats);
	public void affectCharState(MOB affectedMob, CharState affectableMaxState);
}
