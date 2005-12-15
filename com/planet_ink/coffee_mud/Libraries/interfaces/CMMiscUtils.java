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
import com.planet_ink.coffee_mud.Libraries.CMMap;
import com.planet_ink.coffee_mud.Libraries.CoffeeUtensils;
import com.planet_ink.coffee_mud.Libraries.Sense;
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
public interface CMMiscUtils extends CMObject
{
    public boolean hasASky(Room room);
    public String niceCommaList(Vector V, boolean andTOrF);
    public Environmental unbundle(Item I, int number);
    public int getMaterialRelativeInt(String s);
    public int getMaterialCode(String s);
    public int getResourceCode(String s);
    public Law getTheLaw(Room R, MOB mob);
    public LegalBehavior getLegalBehavior(Area A);
    public LegalBehavior getLegalBehavior(Room R);
    public Area getLegalObject(Area A);
    public Area getLegalObject(Room R);
    public int getRandomResourceOfMaterial(int material);
    public Vector getAllUniqueTitles(Enumeration e, String owner, boolean includeRentals);
    public Environmental makeResource(int myResource, int localeCode, boolean noAnimals);
    public String getFormattedDate(Environmental E);
    public Item makeItemResource(int type);
    public void outfit(MOB mob, Vector items);
    public Trap makeADeprecatedTrap(Environmental unlockThis);
    public void setTrapped(Environmental myThang, boolean isTrapped);
    public void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped);
    public Trap fetchMyTrap(Environmental myThang);
    public boolean reachableItem(MOB mob, Environmental E);
    public Room roomLocation(Environmental E);
    public Room roomStart(Environmental E);
    public Area areaLocation(Object E);
    public double memoryUse ( Environmental E, int number );
    public void extinguish(MOB source, Environmental target, boolean mundane);
    public void obliterateRoom(Room deadRoom);
    public void roomAffectFully(CMMsg msg, Room room, int dirCode);
    public void obliteratePlayer(MOB deadMOB, boolean quiet);
    public void resetRoom(Room room);
    public void resetArea(Area area);
    public MOB getMobPossessingAnother(MOB mob);
    public void clearTheRoom(Room room);
    public void clearDebriAndRestart(Room room, int taskCode);
    public void obliterateArea(String areaName);
    public LandTitle getLandTitle(Area area);
    public LandTitle getLandTitle(Room room);
    public boolean doesHavePriviledgesHere(MOB mob, Room room);
    public boolean doesOwnThisProperty(String name, Room room);
    public boolean doesOwnThisProperty(MOB mob, Room room);
    public boolean armorCheck(MOB mob, int allowedArmorLevel);
    public String wornList(long wornCode);
    public int getWornCode(String name);
}
