package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

public interface Room extends Environmental
{
	public String roomID();
	public void setRoomID(String newRoomID);

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
	public final static int DOMAIN_OUTDOORS_SPACEPORT=12;
	public final static String[] outdoorDomainDescs={
		"CITY",
		"WOODS",
		"ROCKY",
		"PLAINS",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"JUNGLE",
		"SWAMP",
		"DESERT",
		"HILLS",
		"MOUNTAINS",
		"SPACEPORT"};

	public final static int DOMAIN_INDOORS_STONE=INDOORS+0;
	public final static int DOMAIN_INDOORS_WOOD=INDOORS+1;
	public final static int DOMAIN_INDOORS_CAVE=INDOORS+2;
	public final static int DOMAIN_INDOORS_MAGIC=INDOORS+3;
	public final static int DOMAIN_INDOORS_UNDERWATER=INDOORS+4;
	public final static int DOMAIN_INDOORS_AIR=INDOORS+5;
	public final static int DOMAIN_INDOORS_WATERSURFACE=INDOORS+6;
	public final static int DOMAIN_INDOORS_METAL=INDOORS+7;
	public final static String[] indoorDomainDescs={
		"STONE",
		"WOODEN",
		"CAVE",
		"MAGIC",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"METAL"};

	public final static int CONDITION_NORMAL=0;
	public final static int CONDITION_WET=1;
	public final static int CONDITION_HOT=2;
	public final static int CONDITION_COLD=3;

	public int domainType();
	public int domainConditions();
	public int myResource();
	public void setResource(int resourceCode);
	public Vector resourceChoices();
	public void toggleMobility(boolean onoff);
	public boolean getMobility();

	public void startItemRejuv();
	public void recoverRoomStats();

	public void destroyRoom();
	public void clearSky();
	public void giveASky(int zero);

	public Area getArea();
	public void setArea(Area newArea);
	public void setGridParent(GridLocale room);
	public GridLocale getGridParent();

	public Exit[] rawExits();
	public Room[] rawDoors();
	public Exit getReverseExit(int direction);
	public Exit getPairedExit(int direction);
	public Room getRoomInDir(int direction);
	public Exit getExitInDir(int direction);

	public int pointsPerMove(MOB mob);
	public int thirstPerRound(MOB mob);

	public String roomTitle();
	public String roomDescription();

	public void listShortExits(MOB mob);
	public void listExits(MOB mob);

	public void send(MOB source, CMMsg msg);
	public void sendOthers(MOB source, CMMsg msg);
	public void showHappens(int allCode, String allMessage);
	public void showHappens(int allCode, Environmental like, String allMessage);
	public boolean show(MOB source,
						Environmental target,
						int allCode,
						String allMessage);
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int allCode,
						String allMessage);
	public boolean showOthers(MOB source,
						      Environmental target,
						      int allCode,
						      String allMessage);
	public boolean showSource(MOB source,
						      Environmental target,
						      int allCode,
						      String allMessage);
	public boolean showOthers(MOB source,
							  Environmental target,
							  Environmental tool,
							  int allCode,
							  String allMessage);
	public boolean showSource(MOB source,
							  Environmental target,
							  Environmental tool,
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
	public Item fetchAnyItem(String itemID);
	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName,int wornReqCode);
	public Environmental fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, int wornReqCode);
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName, int wornReqCode);
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornReqCode);
	public void bringItemHere(Item I, int survivalCode);



}