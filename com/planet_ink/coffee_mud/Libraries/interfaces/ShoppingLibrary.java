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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer.AuctionData;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public interface ShoppingLibrary extends CMLibrary
{
    public ShopKeeper getShopKeeper(Environmental E);
    public Vector getAllShopkeepers(Room here, MOB notMOB);
    public String getViewDescription(Environmental E);
    public double rawSpecificGoldPrice(Environmental product,  CoffeeShop shop, double numberOfThem);
    public double prejudiceValueFromPart(MOB customer, boolean sellTo, String part);
    public double prejudiceFactor(MOB customer, String factors, boolean sellTo);
    public ShopKeeper.ShopPrice sellingPrice(MOB seller, MOB buyer, Environmental product, ShopKeeper shop, boolean includeSalesTax);
    public double devalue(ShopKeeper shop, Environmental product);
    public ShopKeeper.ShopPrice pawningPrice(MOB seller, MOB buyer, Environmental product, ShopKeeper shop);
    public double getSalesTax(Room homeRoom, MOB seller);
    public boolean standardSellEvaluation(MOB seller, MOB buyer, Environmental product, ShopKeeper shop, double maxToPay, double maxEverPaid, boolean sellNotValue);
    public boolean standardBuyEvaluation(MOB seller, MOB buyer, Environmental product, ShopKeeper shop, boolean buyNotView);
    public String getListInventory(MOB seller,  MOB buyer, Vector inventory, int limit, ShopKeeper shop, String mask);
    public String findInnRoom(InnKey key, String addThis, Room R);
    public MOB parseBuyingFor(MOB buyer, String message);
    public double transactPawn(MOB shopkeeper, MOB pawner, ShopKeeper shop, Environmental product);
    public void transactMoneyOnly(MOB seller, MOB buyer, ShopKeeper shop, Environmental product, boolean sellerGetsPaid);
    public boolean purchaseItems(Item baseProduct, Vector products, MOB seller, MOB mobFor);
    public boolean purchaseMOB(MOB product, MOB seller, ShopKeeper shop, MOB mobFor);
    public void purchaseAbility(Ability A,  MOB seller, ShopKeeper shop, MOB mobFor);
    public Vector addRealEstateTitles(Vector V, MOB buyer, CoffeeShop shop, Room myRoom);
    public boolean ignoreIfNecessary(MOB mob, String ignoreMask, MOB whoIgnores);
    public String storeKeeperString(CoffeeShop shop);
    public boolean doISellThis(Environmental thisThang, ShopKeeper shop);
	public String[] bid(MOB mob, double bid, String bidCurrency, Auctioneer.AuctionData auctionData, Item I, Vector auctionAnnounces);
	public void returnMoney(MOB to, String currency, double amt);
    public String getAuctionInventory(MOB seller,MOB buyer,Auctioneer auction,String mask);
    public String getListForMask(String targetMessage);
    public Vector getAuctions(Object ofLike, String auctionHouse);
	public Auctioneer.AuctionData getEnumeratedAuction(String named, String auctionHouse);
	public void auctionNotify(MOB M, String resp, String regardingItem);
    public void cancelAuction(String auctionHouse, Auctioneer.AuctionData data);
	public void saveAuction(Auctioneer.AuctionData data, String auctionHouse, boolean updateOnly);
}