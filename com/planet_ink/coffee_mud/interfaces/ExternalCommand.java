package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.IOException;
public interface ExternalCommand
{
	// combat
	public void postAttack(MOB attacker, MOB target, Item weapon);
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
	public boolean postHealing(MOB healer, MOB target, Environmental tool, int messageCode,int healing,String allDisplayMessage);
	public boolean postExperience(MOB mob, MOB victim, String homage, int amount, boolean quiet);
	public void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success);
	public Hashtable properTargets(Ability A, MOB caster, boolean beRuthless);
	public void justDie(MOB source, MOB target);
	public void postDeath(MOB source, MOB target,CMMsg addHere);
	public void postPanic(MOB mob, CMMsg msg);
	public void drawIfNecessary(MOB mob, boolean held);
	public void sheathIfPossible(MOB mob);

	// other actions
	public boolean wear(MOB mob, Item item, boolean quiet);
	public boolean remove(MOB mob, Item item, boolean quiet);
	public void standIfNecessary(MOB mob);
	public Ability getToEvoke(MOB mob, Vector commands);
	public void look(MOB mob, Vector commands, boolean quiet);
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook);
	public void flee(MOB mob, String direction);
	public StringBuffer getEquipment(MOB seer, MOB mob);
	public void doCommand(MOB mob, Vector commands)
		throws Exception;
	public MOB parseShopkeeper(MOB mob, Vector commands, String error);
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet);
	public boolean drop(MOB mob, Environmental dropThis, boolean quiet, boolean optimize);
	public void read(MOB mob, Environmental thisThang, String theRest);
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag);
	public void follow(MOB mob, MOB tofollow, boolean quiet);
	public void unfollow(MOB mob, boolean quiet);
	public void makePeaceInGroup(MOB mob);
	public boolean doesOwnThisProperty(MOB mob, Room room);

	// messages
	public StringBuffer getInventory(MOB seer, MOB mob);
	public StringBuffer getHelpText(String helpStr, MOB forMOB);
	public StringBuffer getScore(MOB mob);
	public StringBuffer niceLister(MOB mob, Vector items, boolean useName);
	public StringBuffer getAbilities(MOB able, Vector ofTypes, int mask, boolean addQualLine);
	public int channelInt(String channelName);
	public void channel(MOB mob, String channelName, String message, boolean systemMsg);
	public void channel(String channelName, String clanID, String message, boolean systemMsg);

	// misc
	public void roomAffectFully(CMMsg msg, Room room, int dirCode);
	public int getMyDirCode(Exit exit, Room room, int testCode);
	public boolean login(MOB mob) throws IOException;
	public Vector getTopics(boolean archonHelp, boolean standardHelp);
	public void resetRoom(Room room);
	public String getOpenRoomID(String areaName);
	public void obliterateArea(String areaName);
	public void obliterateRoom(Room deadRoom);
	public void destroyUser(MOB deadMOB, boolean quiet);
}
