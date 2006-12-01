package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Vector;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public interface CoffeeShop extends CMCommon
{
    public boolean inBaseInventory(Environmental thisThang);
    public Environmental addStoreInventory(Environmental thisThang, ShopKeeper shop);
    public int baseStockSize();
    public int totalStockSize();
    public Vector getStoreInventory();
    public Vector getBaseInventory();
    public void emptyAllShelves();
    public Environmental addStoreInventory(Environmental thisThang, int number, int price, ShopKeeper shop);
    public int totalStockWeight();
    public int totalStockSizeIncludingDuplicates();
    public void delAllStoreInventory(Environmental thisThang, int whatISell);
    public boolean doIHaveThisInStock(String name, MOB mob, int whatISell, Room startRoom);
    public int stockPrice(Environmental likeThis);
    public int numberInStock(Environmental likeThis);
    public Environmental getStock(String name, MOB mob, int whatISell, Room startRoom);
    public Environmental removeStock(String name, MOB mob, int whatISell, Room startRoom);
    public Vector removeSellableProduct(String named, MOB mob, int whatISell, Room startRoom);
    public String makeXML(ShopKeeper shop);
    public void buildShopFromXML(String text, ShopKeeper shop);
}
