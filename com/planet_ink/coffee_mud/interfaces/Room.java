package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

public interface Room extends Environmental
{
	public final static int INDOORS=128;
	
	public final static int DOMAIN_OUTDOORS_CITY=0;
	public final static int DOMAIN_OUTDOORS_WOODS=1;
	public final static int DOMAIN_OUTDOORS_ROCKS=2;
	public final static int DOMAIN_OUTDOORS_PLAINS=3;
	public final static int DOMAIN_OUTDOORS_UNDERWATER=4;
	public final static int DOMAIN_OUTDOORS_AIR=5;
	public final static int DOMAIN_OUTDOORS_WATERSURFACE=6;
	public final static int DOMAIN_OUTDOORS_JUNGLE=7;
	public final static int DOMAIN_OUTDOORS_SWAMP=8;
	public final static int DOMAIN_OUTDOORS_DESERT=9;
	public final static int DOMAIN_OUTDOORS_HILLS=10;
	public final static int DOMAIN_OUTDOORS_MOUNTAINS=11;
	
	public final static int DOMAIN_INDOORS_STONE=INDOORS+0;
	public final static int DOMAIN_INDOORS_WOOD=INDOORS+1;
	public final static int DOMAIN_INDOORS_CAVE=INDOORS+2;
	public final static int DOMAIN_INDOORS_MAGIC=INDOORS+3;
	public final static int DOMAIN_INDOORS_UNDERWATER=INDOORS+4;
	public final static int DOMAIN_INDOORS_AIR=INDOORS+5;
	public final static int DOMAIN_INDOORS_WATERSURFACE=INDOORS+6;
	
	public final static int CONDITION_NORMAL=0;
	public final static int CONDITION_WET=1;
	public final static int CONDITION_HOT=2;
	public final static int CONDITION_COLD=3;
	
	public int domainType();
	public int domainConditions();
	public int myResource();
	public Vector resourceChoices();
	public void toggleMobility(boolean onoff);
	public boolean getMobility();
	
	public void setID(String newID);
	public String objectID();
	public void startItemRejuv();
	public void recoverRoomStats();
	
	public void destroyRoom();
	
	public Area getArea();
	public void setArea(Area newArea);
	
	public Exit[] rawExits();
	public Room[] rawDoors();
	public Exit getReverseExit(int direction);
	public Exit getPairedExit(int direction);
	public Room getRoomInDir(int direction);
	public Exit getExitInDir(int direction);
	
	public int pointsPerMove(MOB mob);
	public int thirstPerRound(MOB mob);
	
	public void listExits(MOB mob);
	
	public void send(MOB source, Affect msg);
	public void sendOthers(MOB source, Affect msg);
	public void show(MOB source,
					 Environmental target,
					 int allCode,
					 String allMessage);
	public void showHappens(int allCode, String allMessage);
	public void showOthers(MOB source,
						   Environmental target,
						   int allCode,
						   String allMessage);
	public void showSource(MOB source,
						   Environmental target,
						   int allCode,
						   String allMessage);
	
	public MOB fetchInhabitant(String inhabitantID);
	public void addInhabitant(MOB mob);
	public void delInhabitant(MOB mob);
	public int numInhabitants();
	public boolean isInhabitant(MOB mob);
	public MOB fetchInhabitant(int i);
	public int numPCInhabitants();
	public MOB fetchPCInhabitant(int i);
	public void bringMobHere(MOB mob, boolean andFollowers);
	
	public Item fetchItem(Item goodLocation, String itemID);
	public void addItem(Item item);
	public void addItemRefuse(Item item, int survivalTime);
	public void delItem(Item item);
	public int numItems();
	public boolean isContent(Item item);
	public Item fetchItem(int i);
	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName,int wornReqCode);
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName, int wornReqCode);
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornReqCode);
	public void bringItemHere(Item I);
	
	
	
}