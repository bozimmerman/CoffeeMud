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
	public static void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage)
	{
		if(player!=null) player.postDamage(attacker,target,weapon,damage,messageCode,damageType,allDisplayMessage);
	}
	public static String standardHitWord(int weaponType, int damageAmount)
	{
		if(player!=null) return player.standardHitWord(weaponType,damageAmount);
		return "";
	}
	public static String getOpenRoomID(String areaName)
	{
		if(player!=null) return player.getOpenRoomID(areaName);
		return "";
	}
	public static void resetRoom(Room room)
	{
		if(player!=null) player.resetRoom(room);
		
	}
	public static String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
	{
		if(player!=null) return player.standardMissString(weaponType,weaponClassification,weaponName,useExtendedMissString);
		return "";
	}
	public static String standardMobCondition(MOB mob)
	{
		if(player!=null) return player.standardMobCondition(mob);
		return "";
	}
	public static StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		if(player!=null) return player.niceLister(mob,items,useName);
		return new StringBuffer("");
	}
	public static boolean remove(MOB mob, Item item)
	{
		if(player!=null) return player.remove(mob,item);
		return false;
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
	public static void die(MOB source, MOB target)
	{
		if(player!=null) player.die(source,target);
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
	public static boolean move(MOB mob, int directionCode, boolean flee)
	{
		if(player!=null) return player.move(mob,directionCode,flee);
		return false;
	}
	public static void flee(MOB mob, String direction)
	{
		if(player!=null) player.flee(mob,direction);
	}
	public static void roomAffectFully(Affect msg, Room room, int dirCode)
	{
		if(player!=null) player.roomAffectFully(msg,room,dirCode);
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
	public static String shortAlignmentStr(int al)
	{
		if(player!=null) return player.shortAlignmentStr(al);
		return "";
	}
	public static String alignmentStr(int al)
	{
		if(player!=null) return player.alignmentStr(al);
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

	public static StringBuffer getScore(MOB mob)
	{
		if(player!=null) return player.getScore(mob);
		return new StringBuffer("");
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
	public static void DBCreateRoom(Room room, String LocaleID)
	{
		if(sysPlayer!=null) sysPlayer.DBCreateRoom(room,LocaleID);
	}
	public static Area DBCreateArea(String areaName, String areaType)
	{
		if(sysPlayer!=null) return sysPlayer.DBCreateArea(areaName,areaType);
		return null;
	}
	public static void DBDeleteArea(Area A)
	{
		if(sysPlayer!=null) sysPlayer.DBDeleteArea(A);
	}
	public static void DBUpdateArea(Area A)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateArea(A);
	}
	public static void DBUpdateRoom(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateRoom(room);
	}
	public static void DBUpdateMOB(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateMOB(mob);
	}
	public static void DBUpdateItems(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateItems(room);
	}
	public static void DBReCreate(Room room, String oldID)
	{
		if(sysPlayer!=null) sysPlayer.DBReCreate(room,oldID);
	}
	public static void DBDeleteRoom(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBDeleteRoom(room);
	}
	public static void DBReadMOB(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBReadMOB(mob);
	}
	public static void listUsers(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.listUsers(mob);
	}
	public static void DBReadFollowers(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBReadFollowers(mob);
	}
	public static void DBDeleteMOB(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBDeleteMOB(mob);
	}
	public static void DBCreateCharacter(MOB mob)
	{
		if(sysPlayer!=null) sysPlayer.DBCreateCharacter(mob);
	}
	public static void clearDebri(Room room, int taskCode)
	{
		if(sysPlayer!=null) sysPlayer.clearDebri(room,taskCode);
	}
	public static StringBuffer listTicks(int whichTick)
	{
		if(sysPlayer!=null) return sysPlayer.listTicks(whichTick);
		return new StringBuffer();
	}
	public static boolean DBUserSearch(MOB mob, String Login)
	{
		if(sysPlayer!=null) return sysPlayer.DBUserSearch(mob,Login);
		return false;
	}
	public static Vector DBReadJournal(String Journal)
	{
		if(sysPlayer!=null) return sysPlayer.DBReadJournal(Journal);
		return new Vector();
	}
	public static void DBDeleteJournal(String Journal, int which)
	{
		if(sysPlayer!=null) sysPlayer.DBDeleteJournal(Journal,which);
	}
	public static void DBWriteJournal(String Journal, String from, String to, String subject, String message, int which)
	{
		if(sysPlayer!=null) sysPlayer.DBWriteJournal(Journal,from,to,subject,message,which);
	}
	public static void follow(MOB mob, MOB tofollow, boolean quiet)
	{
		if(player!=null) player.follow(mob,tofollow,quiet);
	}

	
}
