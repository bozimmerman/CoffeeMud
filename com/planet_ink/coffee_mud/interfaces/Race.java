package com.planet_ink.coffee_mud.interfaces;

public interface Race
{
	public String ID();
	public String name();
	public boolean playerSelectable();
	public void startRacing(MOB mob, boolean verifyOnly);
	public void setHeightWeight(EnvStats stats, char gender);
	public void outfit(MOB mob);
	public Weapon myNaturalWeapon();
	public String healthText(MOB mob);
	public DeadBody getCorpse(MOB mob, Room room);
	
	public String arriveStr();
	public String leaveStr();

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats);
	public void affectCharStats(MOB affectedMob, CharStats affectableStats);


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(MOB myChar, Affect affect);

	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(MOB myChar, Affect affect);

	public void level(MOB mob);

}
