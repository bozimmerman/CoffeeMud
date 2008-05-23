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
   Copyright 2000-2008 Bo Zimmerman

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
public interface CatalogLibrary 
{
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
    public void changeCatalogUsage(Environmental E, boolean add);
    public void unLoad();
}
