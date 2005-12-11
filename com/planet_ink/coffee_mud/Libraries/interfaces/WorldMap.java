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
import com.planet_ink.coffee_mud.Libraries.CoffeeUtensils;
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public interface WorldMap extends CMObject
{
    public void theWorldChanged();
    public int numAreas();
    public void addArea(Area newOne);
    public void delArea(Area oneToDel);
    public void trimRoomsList();
    public Area getArea(String calledThis);
    public Area findArea(String calledThis);
    public Enumeration areas();
    public Area getFirstArea();
    public Area getRandomArea();
    public void addGlobalHandler(Environmental E, int category);
    public void delGlobalHandler(Environmental E, int category);
    public MOB god(Room R);
    public boolean isObjectInSpace(SpaceObject O);
    public void delObjectInSpace(SpaceObject O);
    public void addObjectToSpace(SpaceObject O);
    public long getDistanceFrom(SpaceObject O1, SpaceObject O2);
    public double[] getDirection(SpaceObject FROM, SpaceObject TO);
    public void moveSpaceObject(SpaceObject O);
    public long getRelativeVelocity(SpaceObject O1, SpaceObject O2);
    public String createNewExit(Room from, Room room, int direction);
    public String getOpenRoomID(String AreaID);
    public int numRooms();
    public void addRoom(Room newOne);
    public void delRoom(Room oneToDel);
    public void justDelRoom(Room oneToDel);
    public boolean sendGlobalMessage(MOB host, int category, CMMsg msg);
    public String getExtendedRoomID(Room R);
    public Room getRoom(String calledThis);
    public Enumeration rooms();
    public void replaceRoom(Room newOne, Room oldOne);
    public Room getFirstRoom();
    public Room getRandomRoom();
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
    public int numPlayers();
    public void addPlayer(MOB newOne);
    public void delPlayer(MOB oneToDel);
    public MOB getPlayer(String calledThis);
    public MOB getLoadPlayer(String last);
    public Enumeration players();
    public Room getStartRoom(MOB mob);
    public Room getDeathRoom(MOB mob);
    public Room getBodyRoom(MOB mob);
    public void pageRooms(CMProps page, Hashtable table, String start);
    public void initStartRooms(CMProps page);
    public void initDeathRooms(CMProps page);
    public void initBodyRooms(CMProps page);
    public void renameRooms(Area A, String oldName, Vector allMyDamnRooms);
    public void unLoad();
    public boolean explored(Room R, Vector areas);
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
}
