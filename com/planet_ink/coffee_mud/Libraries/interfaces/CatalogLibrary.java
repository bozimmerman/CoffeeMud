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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface CatalogLibrary extends CMLibrary
{
    public String[] getCatalogItemNames();
    public String[] getCatalogMobNames();
    public Item[] getCatalogItems();
    public MOB[] getCatalogMobs();
    public boolean isCatalogObj(Environmental E);
    public boolean isCatalogObj(String name);
    public Item getCatalogItem(String name);
    public MOB getCatalogMob(String name);
    public Environmental getCatalogObj(Environmental E);
    public CataData getCatalogItemData(String name);
    public CataData getCatalogMobData(String name);
    public CataData getCatalogData(Environmental E);
    public void delCatalog(Environmental E);
    public void addCatalog(Environmental E);
    public void submitToCatalog(Environmental E);
    public void updateCatalog(Environmental E);
    public StringBuffer checkCatalogIntegrity(Environmental E);
    public void updateCatalogIntegrity(Environmental E);
    public void changeCatalogUsage(Environmental E, boolean add);
    public Item getDropItem(MOB M, boolean live);
    public CataData sampleCataData(String xml);
    public Vector<RoomContent> roomContent(Room R);
    public void updateRoomContent(String roomID, Vector<RoomContent> content);
    public void newInstance(Environmental E);
    public void bumpDeathPickup(Environmental E);
    
    public static interface RoomContent
    {
    	public Environmental E();
    	public Environmental holder();
    	public boolean isDirty();
    	public void flagDirty();
    	public boolean deleted();
    }
    
    public static interface CataData 
    {
        public Vector getMaskV();
        public String getMaskStr();
        public boolean getWhenLive();
        public double getRate();
        public void setMaskStr(String s);
        public void setWhenLive(boolean l);
        public void setRate(double r);
        public Enumeration<Environmental> enumeration();
        public void addReference(Environmental E);
        public boolean isReference(Environmental E);
        public void delReference(Environmental E);
        public int numReferences();
        public String mostPopularArea();
        public String randomRoom();
        public void cleanHouse();
        public Environmental getLiveReference();
        public int getDeathsPicksups();
        public void bumpDeathPickup();
        
        public String data();
        
        public void build(String catadata);
    }
}
