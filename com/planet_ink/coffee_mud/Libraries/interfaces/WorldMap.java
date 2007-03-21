package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2007 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public interface WorldMap extends CMLibrary
{
    /************************************************************************/
    /**							 AREAS		    							*/
    /************************************************************************/
    public int numAreas();
    public void addArea(Area newOne);
    public void delArea(Area oneToDel);
    public Area getArea(String calledThis);
    public Area findArea(String calledThis);
    public Enumeration areas();
	public Enumeration sortedAreas();
    public Area getFirstArea();
    public Area getRandomArea();
    public void obliterateArea(String areaName);
    
    
    /************************************************************************/
    /**							 ROOMS		    							*/
    /************************************************************************/
    public int numRooms();
	public Enumeration roomIDs();
    public String getExtendedRoomID(Room R);
    public Room getRoom(Room room);
    public Room getRoom(String calledThis);
    public Room getRoom(Vector roomSet, String calledThis);
	public Room getRoom(Hashtable hashedRoomSet, String areaName, String calledThis);
    public Enumeration rooms();
    public Room getRandomRoom();
    public void renameRooms(Area A, String oldName, Vector allMyDamnRooms);
    public void obliterateRoom(Room deadRoom);
    public Room findConnectingRoom(Room room);
    public int getRoomDir(Room from, Room to);
    
    /************************************************************************/
    /**							 ROOM-AREA-UTILITIES    					*/
    /************************************************************************/
    public void resetArea(Area area);
    public void resetRoom(Room room);
    public Room getStartRoom(Environmental E);
    public Area getStartArea(Environmental E);
    public Room roomLocation(Environmental E);
    public void emptyRoom(Room room, Room bringBackHere);
    public boolean hasASky(Room room);
    public boolean isClearableRoom(Room room);
    public String createNewExit(Room from, Room room, int direction);
    public Area areaLocation(Object E);
    
    /************************************************************************/
    /**							 MOB->ROOMS     							*/
    /************************************************************************/
    public void pageRooms(CMProps page, Hashtable table, String start);
    public void initStartRooms(CMProps page);
    public void initDeathRooms(CMProps page);
    public void initBodyRooms(CMProps page);
    public Room getDefaultStartRoom(MOB mob);
    public Room getDefaultDeathRoom(MOB mob);
    public Room getDefaultBodyRoom(MOB mob);
    
    
    /************************************************************************/
    /**                          CATALOGS                                   */
    /************************************************************************/
    public DVector getCatalogItems();
    public DVector getCatalogMobs();
    public boolean isCatalogObj(Environmental E);
    public boolean isCatalogObj(String name);
    public int getCatalogItemIndex(String called);
    public int getCatalogMobIndex(String called);
    public Item getCatalogItem(int index);
    public MOB getCatalogMob(int index);
    public int[] getCatalogItemUsage(int index);
    public int[] getCatalogMobUsage(int index);
    public void delCatalog(Item I);
    public void delCatalog(MOB M);
    public void addCatalogReplace(Item I);
    public void addCatalogReplace(MOB M);
    public void addCatalog(Item I);
    public void addCatalog(MOB M);
    public void propogateCatalogChange(Environmental thang);
    
    /************************************************************************/
    /**							 QUICK-MAPPINGS    							*/
    /************************************************************************/
    public int numDeities();
    public void addDeity(Deity newOne);
    public void delDeity(Deity oneToDel);
    public Deity getDeity(String calledThis);
    public Enumeration deities();
    public int numPostOffices();
    public void addPostOffice(PostOffice newOne);
    public void delPostOffice(PostOffice oneToDel);
    public PostOffice getPostOffice(String chain, String areaNameOrBranch);
    public Enumeration postOffices();
    public int numBanks();
    public void addBank(Banker newOne);
    public void delBank(Banker oneToDel);
    public Banker getBank(String chain, String areaNameOrBranch);
    public Enumeration banks();
	public Iterator bankChains(Area AreaOrNull);
    public int numAuctionHouses();
    public void addAuctionHouse(Auctioneer newOne);
    public void delAuctionHouse(Auctioneer oneToDel);
    public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch);
    public Enumeration auctionHouses();
    
    /************************************************************************/
    /**							 PLAYERS 	     							*/
    /************************************************************************/
    public boolean explored(Room R, Vector areas);
    public int numPlayers();
    public void addPlayer(MOB newOne);
    public void delPlayer(MOB oneToDel);
    public MOB getPlayer(String calledThis);
    public MOB getLoadPlayer(String last);
    public Enumeration players();
    public void obliteratePlayer(MOB deadMOB, boolean quiet);
    
    /************************************************************************/
    /**							 SPACE METHODS 								*/
    /************************************************************************/
    public long getRelativeVelocity(SpaceObject O1, SpaceObject O2);
    public boolean isObjectInSpace(SpaceObject O);
    public void delObjectInSpace(SpaceObject O);
    public void addObjectToSpace(SpaceObject O);
    public long getDistanceFrom(SpaceObject O1, SpaceObject O2);
    public double[] getDirection(SpaceObject FROM, SpaceObject TO);
    public void moveSpaceObject(SpaceObject O);
    
    /************************************************************************/
    /**							 MESSAGES	 								*/
    /************************************************************************/
    public void addGlobalHandler(Environmental E, int category);
    public void delGlobalHandler(Environmental E, int category);
    public MOB god(Room R);
    public boolean sendGlobalMessage(MOB host, int category, CMMsg msg);
    
    public static class CrossExit
    {
        public int x;
        public int y;
        public int dir;
        public String destRoomID="";
        public boolean out=false;
        public static CrossExit make(int xx, int xy, int xdir, String xdestRoomID, boolean xout)
        {   CrossExit EX=new CrossExit();
            EX.x=xx;EX.y=xy;EX.dir=xdir;EX.destRoomID=xdestRoomID;EX.out=xout;
            return EX;
        }
    }
	public final static long ROOM_EXPIRATION_MILLIS=2500000;
    public void unLoad();
    
    public class CompleteRoomIDEnumerator implements Enumeration
    {
    	Enumeration roomIDEnumerator=null;
    	Enumeration areaEnumerator=null;
    	public CompleteRoomIDEnumerator(WorldMap map){areaEnumerator=map.areas();}
    	public boolean hasMoreElements()
    	{
    		if((roomIDEnumerator==null)||(!roomIDEnumerator.hasMoreElements()))
	    		while(areaEnumerator.hasMoreElements())
	    		{
		    		Area A=(Area)areaEnumerator.nextElement();
		    		roomIDEnumerator=A.getProperRoomnumbers().getRoomIDs();
		    		if(roomIDEnumerator.hasMoreElements()) return true;
	    		}
    		return ((roomIDEnumerator!=null)&&(roomIDEnumerator.hasMoreElements()));
    	}
    	public Object nextElement(){ return hasMoreElements()?roomIDEnumerator.nextElement():null;}
    }

	
}
