package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.IOException;
public interface ExternalCommand
{
	// combat
	public void postAttack(MOB attacker, MOB target, Item weapon);
	public void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage);
	public void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success);
	public Hashtable properTargets(Ability A, MOB caster, boolean beRuthless);
	public void justDie(MOB source, MOB target);
	public void postDeath(MOB source, MOB target,Affect addHere);
	public void postPanic(MOB mob, Affect affect);
	public void drawIfNecessary(MOB mob);

	// other actions
	public boolean wear(MOB mob, Item item, boolean quiet);
	public boolean remove(MOB mob, Item item, boolean quiet);
	public void standIfNecessary(MOB mob);
	public Ability getToEvoke(MOB mob, Vector commands);
	public void look(MOB mob, Vector commands, boolean quiet);
	public boolean move(MOB mob, int directionCode, boolean flee);
	public void flee(MOB mob, String direction);
	public StringBuffer getEquipment(MOB seer, MOB mob);
	public void doCommand(MOB mob, Vector commands)
		throws Exception;
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet);
	public boolean drop(MOB mob, Environmental dropThis, boolean quiet);
	public void read(MOB mob, Environmental thisThang, String theRest);
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag);
	public void follow(MOB mob, MOB tofollow, boolean quiet);
	public void unfollow(MOB mob, boolean quiet);
	public void makePeaceInGroup(MOB mob);
	
	// messages
	public StringBuffer getInventory(MOB seer, MOB mob);
	public StringBuffer getHelpText(String helpStr);
	public StringBuffer getScore(MOB mob);
	public StringBuffer niceLister(MOB mob, Vector items, boolean useName);
	public int channelInt(String channelName);
	
	
	// misc
	public void roomAffectFully(Affect msg, Room room, int dirCode);
	public int getMyDirCode(Exit exit, Room room, int testCode);
	public boolean login(MOB mob) throws IOException;
	public void resetRoom(Room room);
	public String getOpenRoomID(String areaName);
	public void obliterateArea(String areaName);
	public void obliterateRoom(Room deadRoom);
	public void destroyUser(MOB deadMOB);
	
	// ability helpers
	public Vector findBastardTheBestWay(Room location, Vector destRooms, boolean noWater);
	public int trackNextDirectionFromHere(Vector theTrail,Room location,boolean noWater);
	public boolean zapperCheck(String text, MOB mob);
	
}
