package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.IOException;
public class ExternalPlay
{
	private static ExternalCommand player=null;
	private static ExternalSystem sysPlayer=null;
	private static final long startTime=System.currentTimeMillis();
	private static boolean systemStarted=false;
	private static I3Interface i3interface=null;
	
	public static void setPlayer(ExternalCommand newPlayer, ExternalSystem otherNewPlayer, I3Interface i3)
	{
		player=newPlayer;
		sysPlayer=otherNewPlayer;
		i3interface=i3;
	}
	
	public static long getStartTime()
	{
		return startTime;
	}
	public static I3Interface i3(){return i3interface;}
	public static int channelInt(String channelName)
	{
		if(player!=null) return player.channelInt(channelName);
		return -1;
	}
	public static boolean doesOwnThisProperty(MOB mob, Room room)
	{
		if(player!=null) return player.doesOwnThisProperty(mob,room);
		return false;
	}
	public static void postAttack(MOB attacker, MOB target, Item weapon)
	{
		if(player!=null) player.postAttack(attacker,target,weapon);
	}
	public static void postDamage(MOB attacker, MOB target, Environmental weapon, int damage, int messageCode, int damageType, String allDisplayMessage)
	{
		if(player!=null) player.postDamage(attacker,target,weapon,damage,messageCode,damageType,allDisplayMessage);
	}
	public static void drawIfNecessary(MOB mob, boolean held)
	{
		if(player!=null) player.drawIfNecessary(mob,held);
	}

	public static String getOpenRoomID(String areaName)
	{
		if(player!=null) return player.getOpenRoomID(areaName);
		return "";
	}
	public static void setSystemStarted(){systemStarted=true;}
	public static boolean getSystemStarted(){return systemStarted;}
	
	public static void obliterateArea(String areaName)
	{
		if(player!=null) player.obliterateArea(areaName);
	}
	public static void obliterateRoom(Room deadRoom)
	{
		if(player!=null) player.obliterateRoom(deadRoom);
	}
	public static void destroyUser(MOB deadMOB)
	{
		if(player!=null) player.destroyUser(deadMOB);
	}
	public static void resetRoom(Room room)
	{
		if(player!=null) player.resetRoom(room);
	}
	public static StringBuffer systemReport()
	{
		if(sysPlayer!=null) return sysPlayer.systemReport();
		return new StringBuffer("");
	}
	
	public static Ability getToEvoke(MOB mob, Vector commands)
	{
		if(player!=null) return player.getToEvoke(mob,commands);
		return null;
	}
	public static void makePeaceInGroup(MOB mob)
	{
		if(player!=null) player.makePeaceInGroup(mob);
	}
	
	public static StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		if(player!=null) return player.niceLister(mob,items,useName);
		return new StringBuffer("");
	}
	public static boolean remove(MOB mob, Item item, boolean quiet)
	{
		if(player!=null) return player.remove(mob,item,quiet);
		return false;
	}
	public static void unfollow(MOB mob, boolean quiet)
	{
		if(player!=null) player.unfollow(mob,quiet);
	}
	public static void follow(MOB mob, MOB tofollow, boolean quiet)
	{
		if(player!=null) player.follow(mob,tofollow,quiet);
	}
	public static boolean wear(MOB mob, Item item, boolean quiet)
	{
		if(player!=null) return player.wear(mob,item,quiet);
		return false;
	}
	public static boolean zapperCheck(String text, MOB mob)
	{
		if(player!=null) return player.zapperCheck(text,mob);
		return true;
	}
	public static void standIfNecessary(MOB mob)
	{
		if(player!=null) player.standIfNecessary(mob);
	}
	public static void look(MOB mob, Vector commands, boolean quiet)
	{
		if(player!=null) player.look(mob,commands,quiet);
	}
	
	public static void postPanic(MOB mob, Affect affect)
	{
		if(player!=null) player.postPanic(mob,affect);
	}
	public static void postWeaponDamage(MOB source, MOB target, Weapon weapon, boolean success)
	{
		if(player!=null) player.postWeaponDamage(source,target,weapon,success);
	}
	public static void postDeath(MOB source, MOB target,Affect addHere)
	{
		if(player!=null) player.postDeath(source,target,addHere);
	}
	public static void justDie(MOB source, MOB target)
	{
		if(player!=null) player.justDie(source,target);
	}
	public static Hashtable properTargets(Ability A, MOB caster, boolean beRuthless)
	{
		if(player!=null) return player.properTargets(A,caster,beRuthless);
		return new Hashtable();
	}
	public static boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		if(player!=null) return player.move(mob,directionCode,flee,nolook);
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
	public static boolean drop(MOB mob, Environmental dropThis, boolean quiet)
	{
		if(player!=null) return player.drop(mob,dropThis,quiet);
		return false;
	}
	public static StringBuffer getHelpText(String helpStr)
	{
		if(player!=null) return player.getHelpText(helpStr);
		return null;
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
	public static Vector findBastardTheBestWay(Room location, Vector destRooms, boolean noWater)
	{
		if(player!=null) return player.findBastardTheBestWay(location,destRooms,noWater);
		return new Vector();
	}
	public static int trackNextDirectionFromHere(Vector theTrail,Room location,boolean noWater)
	{
		if(player!=null) return player.trackNextDirectionFromHere(theTrail,location,noWater);
		return -1;
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
	
	public static void DBReadContent(Room thisRoom, Hashtable rooms)
	{
		if(sysPlayer!=null) sysPlayer.DBReadContent(thisRoom,rooms);
	}
	public static void DBUpdateExits(Room room)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateExits(room);
	}
	public static void DBUpdateTheseMOBs(Room room, Vector mobs)
	{
		if(sysPlayer!=null) sysPlayer.DBUpdateTheseMOBs(room,mobs);
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
	public static void DBUpdateIP(MOB mob)
	{  if(sysPlayer!=null) sysPlayer.DBUpdateIP(mob);}
	public static void DBClanFill(String clan, Vector members, Vector roles)
	{  if(sysPlayer!=null) sysPlayer.DBClanFill(clan,members,roles);}
	public static void DBUpdateClan(String name, String clan, int role)
	{  if(sysPlayer!=null) sysPlayer.DBUpdateClan(name,clan,role);}
	
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
	public static Vector userList()
	{
		if(sysPlayer!=null) return sysPlayer.userList();
		return new Vector();
	}
	public static void listUsers(MOB mob, int sortBy)
	{
		if(sysPlayer!=null) sysPlayer.listUsers(mob,sortBy);
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
	public static void suspendTicking(Environmental E, int tickID)
	{
		if(sysPlayer!=null) sysPlayer.suspendTicking(E,tickID);
	}
	public static void resumeTicking(Environmental E, int tickID)
	{
		if(sysPlayer!=null) sysPlayer.resumeTicking(E,tickID);
	}
	public static void tickAllTickers(Room here)
	{
		if(sysPlayer!=null) sysPlayer.tickAllTickers(here);
	}
	public static StringBuffer listTicks(int whichTick)
	{
		if(sysPlayer!=null) return sysPlayer.listTicks(whichTick);
		return new StringBuffer();
	}
	public static boolean DBReadUserOnly(MOB mob)
	{
		if(sysPlayer!=null) return sysPlayer.DBReadUserOnly(mob);
		return false;
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
	public static void vassals(MOB mob, String leigeID)
	{
		if(sysPlayer!=null) sysPlayer.vassals(mob,leigeID);
	}
}
