package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

public interface Room extends Environmental
{
	public void setID(String newID);
	public String objectID();
	public void startItemRejuv();
	public void recoverRoomStats();
	
	public Area getArea();
	public void setArea(Area newArea);
	
	public Exit[] exits();
	public Room[] doors();
	public Exit getReverseExit(int direction);
	public Exit getPairedExit(int direction);
	
	public int pointsPerMove();
	public void look(MOB mob);
	public void listExits(MOB mob);
	public void bringMobHere(MOB mob, boolean andFollowers);
	
	public void send(MOB source, Affect msg);
	public void sendOthers(MOB source, Affect msg);
	public void show(MOB source,
					 Environmental target,
					 int allCode,
					 String allMessage);
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
	
	public Item fetchItem(Item goodLocation, String itemID);
	public void addItem(Item item);
	public void delItem(Item item);
	public int numItems();
	public boolean isContent(Item item);
	public Item fetchItem(int i);
	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName);
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName);
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName);
	
	
	public int domainType();
	public int domainConditions();
	
	
	public final static int OUTDOORS=0;
	public final static int INDOORS=128;
	
	public final static int DOMAIN_OUTDOORS_CITY=OUTDOORS+0;
	public final static int DOMAIN_OUTDOORS_WOODS=OUTDOORS+1;
	public final static int DOMAIN_OUTDOORS_ROCKS=OUTDOORS+2;
	public final static int DOMAIN_OUTDOORS_PLAINS=OUTDOORS+3;
	public final static int DOMAIN_OUTDOORS_UNDERWATER=OUTDOORS+4;
	public final static int DOMAIN_OUTDOORS_AIR=OUTDOORS+5;
	public final static int DOMAIN_OUTDOORS_WATERSURFACE=OUTDOORS+6;
	
	public final static int DOMAIN_INDOORS_STONE=INDOORS+0;
	public final static int DOMAIN_INDOORS_WOOD=INDOORS+1;
	public final static int DOMAIN_INDOORS_CAVE=INDOORS+2;
	public final static int DOMAIN_INDOORS_MAGIC=INDOORS+3;
	
	public final static int CONDITION_NORMAL=0;
	public final static int CONDITION_WET=1;
	public final static int CONDITION_HOT=2;
	public final static int CONDITION_COLD=3;
}