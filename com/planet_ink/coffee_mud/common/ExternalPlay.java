package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.IOException;
public class ExternalPlay
{
	private static ExternalCommand player=null;
	private static ExternalSystem sysPlayer=null;
	
	public static void setPlayer(ExternalCommand newPlayer, ExternalSystem otherNewPlayer)
	{
		player=newPlayer;
		sysPlayer=otherNewPlayer;
	}
	public static void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if(player!=null) player.postAttack(attacker,target,weapon);
	}
	public static void postDamage(MOB attacker, MOB target, Environmental weapon, int damage)
	{
		if(player!=null) player.postDamage(attacker,target,weapon,damage);
	}
	public static String hitWord(int weaponType, int damageAmount)
	{
		if(player!=null) return player.hitWord(weaponType,damageAmount);
		return "";
	}
	public static boolean wear(MOB mob, Item item)
	{
		if(player!=null) return player.wear(mob,item);
		return false;
	}
	public static void standIfNecessary(MOB mob)
	{
		if(player!=null) player.standIfNecessary(mob);
	}
	public static void look(MOB mob, Vector commands, boolean quiet)
	{
		if(player!=null) player.look(mob,commands,quiet);
	}
	
	public static void resistanceMsgs(Affect affect, MOB source, MOB target)
	{
		if(player!=null) player.resistanceMsgs(affect,source,target);
	}
	public static void strike(MOB source, MOB target, Weapon weapon, boolean success)
	{
		if(player!=null) player.strike(source,target,weapon,success);
	}
	public static void doDamage(MOB source, MOB target, int damageAmount)
	{
		if(player!=null) player.doDamage(source,target,damageAmount);
	}
	public static boolean isHit(MOB attacker, MOB target)
	{
		if(player!=null) return player.isHit(attacker,target);
		return false;
	}
	public static long adjustedAttackBonus(MOB mob)
	{
		if(player!=null) return player.adjustedAttackBonus(mob);
		return 0;
	}
	public static Hashtable properTargets(Ability A, MOB caster)
	{
		if(player!=null) return player.properTargets(A,caster);
		return new Hashtable();
	}
	public static String mobCondition(MOB mob)
	{
		if(player!=null) return player.mobCondition(mob);
		return "";
	}
	public static void move(MOB mob, int directionCode, boolean flee)
	{
		if(player!=null) player.move(mob,directionCode,flee);
	}
	public static void flee(MOB mob, String direction)
	{
		if(player!=null) player.flee(mob,direction);
	}
	public static void roomAffectFully(Affect msg, Room room, int dirCode)
	{
		if(player!=null) player.roomAffectFully(msg,room,dirCode);
	}
	public static boolean doAttack(MOB attacker, MOB target, Weapon weapon)
	{
		if(player!=null) return player.doAttack(attacker,target,weapon);
		return false;
	}
	public static StringBuffer getEquipment(MOB seer, MOB mob)
	{
		if(player!=null) return player.getEquipment(seer,mob);
		return new StringBuffer();
	}
	public static void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		if(player!=null) player.doCommand(mob,commands);
	}
	public static String shortAlignmentStr(MOB mob)
	{
		if(player!=null) return player.shortAlignmentStr(mob);
		return "";
	}
	public static String alignmentStr(MOB mob)
	{
		if(player!=null) return player.alignmentStr(mob);
		return "";
	}
	public static StringBuffer getInventory(MOB seer, MOB mob)
	{
		if(player!=null) return player.getInventory(seer,mob);
		return new StringBuffer();
	}
	public static int getMyDirCode(Exit exit, Room room, int testCode)
	{
		if(player!=null) return player.getMyDirCode(exit,room,testCode);
		return 0;
	}
	public static boolean drop(MOB mob, Environmental dropThis)
	{
		if(player!=null) return player.drop(mob,dropThis);
		return false;
	}
	public static void read(MOB mob, Environmental thisThang, String theRest)
	{
		if(player!=null) player.read(mob,thisThang, theRest);
	}
	public static void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		if(player!=null) player.quickSay(mob,target,text,isPrivate,tellFlag);
	}

	public static void score(MOB mob)
	{
		if(player!=null) player.score(mob);
	}
	public static void startTickDown(Environmental E,
									 int tickID,
									 int numTicks)
	{
		if(sysPlayer!=null) sysPlayer.startTickDown(E,tickID,numTicks);
	}
	
	public static boolean deleteTick(Environmental E, int tickID)
	{
		if(sysPlayer!=null) return sysPlayer.deleteTick(E,tickID);
		return false;
	}
	public static void DBUpdateFollowers(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateFollowers(mob);
	}
	public static StringBuffer showWho(MOB who, boolean shortForm)
	{
		if(player!=null) return player.showWho(who,shortForm);
		return new StringBuffer();
	}
	public static boolean login(MOB mob)
		throws IOException
	{
		if(player!=null) return player.login(mob);
		return false;
	}
	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(player!=null) return player.get(mob,container,getThis,quiet);
		return false;
	}
	
	public static void DBReadContent(Room thisRoom)
	{
		if(sysPlayer!=null) sysPlayer.DBReadContent(thisRoom);
	}
	public static void DBUpdateExits(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateExits(room);
	}
	public static void DBUpdateMOBs(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateMOBs(room);
	}
	public static void DBCreate(Room room, String LocaleID)
	{
		if(sysPlayer!=null) sysPlayer.DBCreate(room,LocaleID);
	}
	public static void DBUpdateRoom(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateRoom(room);
	}
	public static void DBUpdate(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdate(mob);
	}
	public static void DBUpdateItems(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateItems(room);
	}
	public static void DBReCreate(Room room, String oldID)
	{
		if(sysPlayer!=null) sysPlayer.DBReCreate(room,oldID);
	}
	public static void DBDelete(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBDelete(room);
	}
	public static void DBRead(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBRead(mob);
	}
	public static void listUsers(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.listUsers(mob);
	}
	public static void DBReadFollowers(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBReadFollowers(mob);
	}
	public static void DBDelete(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBDelete(mob);
	}
	public static void DBCreateCharacter(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBCreateCharacter(mob);
	}
	public static void clearDebri(Room room, int taskCode)
	{
		if(sysPlayer!=null) sysPlayer.clearDebri(room,taskCode);
	}
	public static StringBuffer listTicks()
	{
		if(sysPlayer!=null) return sysPlayer.listTicks();
		return new StringBuffer();
	}
	public static void DBUserSearch(MOB mob, String Login)
	{
		if(sysPlayer!=null) sysPlayer.DBUserSearch(mob,Login);
	}
	
}
