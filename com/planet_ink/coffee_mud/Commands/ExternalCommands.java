package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import java.util.*;
import java.io.IOException;
public class ExternalCommands implements ExternalCommand
{
	CommandProcessor processor=null;
	private static ExternalCommands self = null;

	private ExternalCommands() {}

	public static ExternalCommands getInstance()
	{
	  if (self == null) self = new ExternalCommands();
	  return self;
	}

	public ExternalCommands(CommandProcessor newProcessor)
	{
		processor=newProcessor;
	}
	public boolean wear(MOB mob, Item item, boolean quiet)
	{
		return ItemUsage.wear(mob,item,quiet);
	}
	
	public void extinguish(MOB source, Environmental target, int level)
	{
		AbilityHelper.extinguish(source,target,level);
	}
	
	public Vector findBastardTheBestWay(Room location, 
										Vector destRooms,
										boolean noWater)
	{
		return AbilityHelper.findBastardTheBestWay(location,destRooms,noWater);
	}
	public String zapperDesc(String text)
	{
		return AbilityHelper.zapperDesc(text);
	}
	public boolean zapperCheck(String text, MOB mob)
	{
		return AbilityHelper.zapperCheck(text,mob);
	}
	public int trackNextDirectionFromHere(Vector theTrail, 
											Room location,
											boolean noWater)
	{
		return AbilityHelper.trackNextDirectionFromHere(theTrail,location,noWater);
	}
	public int channelInt(String channelName)
	{
		return Channels.getChannelInt(channelName);
	}
	
	public void obliterateArea(String areaName)
	{
		Rooms.obliterateArea(areaName);
	}
	public void obliterateRoom(Room deadRoom)
	{
		Rooms.obliterateRoom(deadRoom);
	}
	public void destroyUser(MOB deadMOB)
	{
		Scoring.destroyUser(deadMOB);
	}
	public boolean doesOwnThisProperty(MOB mob, Room room)
	{
		return ShopKeepers.doesOwnThisProperty(mob,room);
	}
	public boolean remove(MOB mob, Item item, boolean quiet)
	{
		return ItemUsage.remove(mob,item, quiet);
	}
	public void resetRoom(Room room)
	{
		Reset.resetRoom(room);
	}
	public String getOpenRoomID(String areaName)
	{
		return Reset.getOpenRoomID(areaName);
	}
	public void drawIfNecessary(MOB mob, boolean held)
	{
		TheFight.drawIfNecessary(mob, held);
	}
	public void postAttack(MOB attacker, MOB target, Item weapon)
	{
		TheFight.postAttack(attacker,target,weapon);
	}
	public void makePeaceInGroup(MOB mob)
	{
		Grouping.makePeaceInGroup(mob);
	}
	public Ability getToEvoke(MOB mob, Vector commands)
	{
		return AbilityEvoker.getToEvoke(mob,commands);
	}
	public void postDamage(MOB attacker, 
						   MOB target, 
						   Environmental weapon, 
						   int damage,
						   int messageCode,
						   int damageType,
						   String allDisplayMessage)
	{
		TheFight.postDamage(attacker,target,weapon,damage,messageCode,damageType,allDisplayMessage);
	}
	public StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		return Scoring.niceLister(mob,items,useName);
	}
	public void standIfNecessary(MOB mob)
	{
		Movement.standIfNecessary(mob);
	}
	public void look(MOB mob, Vector commands, boolean quiet)
	{
		BasicSenses.look(mob,commands,quiet);
	}
	public void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success)
	{
		TheFight.postWeaponDamage(source,target,weapon,success);
	}
	public void postPanic(MOB mob, Affect affect)
	{
		TheFight.postPanic(mob,affect);
	}
	
	public void postDeath(MOB source, MOB target,Affect addHere)
	{
		TheFight.postDeath(source,target,addHere);
	}
	public void justDie(MOB source, MOB target)
	{
		TheFight.justDie(source,target);
	}
	public Hashtable properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		return TheFight.properTargets(A,caster,beRuthless);
	}
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		return Movement.move(mob,directionCode,flee,nolook,false);
	}
	public void flee(MOB mob, String direction)
	{
		Movement.flee(mob,direction);
	}
	public void roomAffectFully(Affect msg, Room room, int dirCode)
	{
		Movement.roomAffectFully(msg,room,dirCode);
	}
	public StringBuffer getEquipment(MOB seer, MOB mob)
	{
		return Scoring.getEquipment(seer,mob);
	}
	public void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		processor.doCommand(mob,commands);
	}
	public StringBuffer getHelpText(String helpStr)
	{
		return Help.getHelpText(helpStr);
	}
	public StringBuffer getInventory(MOB seer, MOB mob)
	{
		return Scoring.getInventory(seer,mob);
	}
	public int getMyDirCode(Exit exit, Room room, int testCode)
	{
		return Movement.getMyDirCode(exit,room,testCode);
	}
	public boolean drop(MOB mob, Environmental dropThis, boolean quiet)
	{
		return ItemUsage.drop(mob,dropThis,quiet);
	}
	public void read(MOB mob, Environmental thisThang, String theRest)
	{
		ItemUsage.read(mob,thisThang,theRest);
	}
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		SocialProcessor.quickSay(mob,target,text,isPrivate,tellFlag);
	}
	public StringBuffer getScore(MOB mob)
	{
		return Scoring.getScore(mob);
	}
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{
		return ItemUsage.get(mob,container,getThis,quiet);
	}
	public void unfollow(MOB mob, boolean quiet)
	{
		Grouping.unfollow(mob,quiet);
	}
	public void follow(MOB mob, MOB tofollow, boolean quiet)
	{
		Grouping.processFollow(mob,tofollow, quiet);
	}
	public boolean login(MOB mob)
		throws IOException
	{
		return FrontDoor.login(mob);
	}
}