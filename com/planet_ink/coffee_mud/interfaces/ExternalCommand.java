package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.IOException;
public interface ExternalCommand
{
	// combat
	public void postAttack(MOB attacker, MOB target, Item weapon);
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage);
	public String standardHitWord(int weaponType, int damageAmount);
	public String standardMissString(int weaponType, String weaponName, boolean useExtendedMissString);
	public void strike(MOB source, MOB target, Weapon weapon, boolean success);
	public boolean isHit(MOB attacker, MOB target);
	public Hashtable properTargets(Ability A, MOB caster);
	public void doDamage(MOB source, MOB target, int damageAmount);
	public boolean doAttack(MOB attacker, MOB target, Weapon weapon);
	
	// other actions
	public boolean wear(MOB mob, Item item);
	public void standIfNecessary(MOB mob);
	public void look(MOB mob, Vector commands, boolean quiet);
	public void move(MOB mob, int directionCode, boolean flee);
	public void flee(MOB mob, String direction);
	public StringBuffer getEquipment(MOB seer, MOB mob);
	public void doCommand(MOB mob, Vector commands)
		throws Exception;
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet);
	public boolean drop(MOB mob, Environmental dropThis);
	public void read(MOB mob, Environmental thisThang, String theRest);
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag);
	public void follow(MOB mob, MOB tofollow, boolean quiet);
	
	// messages
	public long adjustedAttackBonus(MOB mob);
	public void resistanceMsgs(Affect affect, MOB source, MOB target);
	public String shortAlignmentStr(int al);
	public String alignmentStr(int al);
	public StringBuffer getInventory(MOB seer, MOB mob);
	public void score(MOB mob);
	public StringBuffer showWho(MOB who, boolean shortForm);
	public String standardMobCondition(MOB mob);
	
	
	// misc
	public void roomAffectFully(Affect msg, Room room, int dirCode);
	public int getMyDirCode(Exit exit, Room room, int testCode);
	public boolean login(MOB mob) throws IOException;
	
}
