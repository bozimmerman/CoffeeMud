package com.planet_ink.coffee_mud.interfaces;

public interface CharClass
{
	public String ID();
	public String name();
	public boolean playerSelectable();
	public boolean qualifiesForThisClass(MOB mob);

	public void newCharacter(MOB mob, boolean isBorrowedClass);
	public void gainExperience(MOB mob, MOB victim, String homage, int amount);
	public void level(MOB mob);
	public void unLevel(MOB mob);
	public void outfit(MOB mob);

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

	public void buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender);

	public boolean canAdvance(MOB mob, int abilityCode);
	public int getMaxStat(int abilityCode);
	public int getLevelMana(MOB mob);
	public int getLevelMove(MOB mob);
	public int getLevelAttack(MOB mob);
	public int getLevelArmor(MOB mob);
	public int getLevelDamage(MOB mob);
	public int[] maxStat();

	public void logon(MOB mob);
}
