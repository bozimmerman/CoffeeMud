package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop.ShelfProduct;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2007-2020 Bo Zimmerman

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
public class AuctionCoffeeShop implements CoffeeShop
{
	@Override
	public String ID()
	{
		return "AuctionCoffeeShop";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public static final Vector<Environmental> emptyV=new Vector<Environmental>();
	public static final Vector<ShelfProduct> emptyV2=new Vector<ShelfProduct>();
	public String auctionShop="";
	protected WeakReference<ShopKeeper> shopKeeper=null;

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new AuctionCoffeeShop();
		}
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(final Exception e)
		{
			return new AuctionCoffeeShop();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CoffeeShop build(final ShopKeeper SK)
	{
		shopKeeper=new WeakReference<ShopKeeper>(SK);
		return this;
	}

	@Override
	public ShopKeeper shopKeeper()
	{
		return (shopKeeper==null)?null:shopKeeper.get();
	}

	@Override
	public boolean isSold(final int code)
	{
		final ShopKeeper SK=shopKeeper();
		return (SK==null)?false:SK.isSold(code);
	}

	@Override
	public boolean inEnumerableInventory(final Environmental thisThang)
	{
		return false;
	}

	@Override
	public Environmental addStoreInventory(final Environmental thisThang)
	{
		return addStoreInventory(thisThang,1,-1);
	}

	@Override
	public int enumerableStockSize()
	{
		return 0;
	}

	@Override
	public int totalStockSize()
	{
		return 0;
	}

	@Override
	public Iterator<Environmental> getStoreInventory()
	{
		return emptyV.iterator();
	}

	@Override
	public Iterator<ShelfProduct> getStoreShelves()
	{
		return emptyV2.iterator();
	}


	@Override
	public boolean lowerStock(final String name)
	{
		return false;
	}

	@Override
	public CoffeeShop weakCopyOf()
	{
		return this;
	}

	@Override
	public Iterator<Environmental> getStoreInventory(final String srchStr)
	{
		return emptyV.iterator();
	}

	@Override
	public Iterator<Environmental> getEnumerableInventory()
	{
		return emptyV.iterator();
	}

	@Override
	public Environmental addStoreInventory(final Environmental thisThang,
										   final int number,
										   final int price)
	{
		if(shopKeeper() instanceof Auctioneer)
			auctionShop=((Auctioneer)shopKeeper()).auctionHouse();
		return thisThang;
	}

	@Override
	public int totalStockWeight()
	{
		return 0;
	}

	@Override
	public int totalStockSizeIncludingDuplicates()
	{
		return 0;
	}

	@Override
	public void delAllStoreInventory(final Environmental thisThang)
	{
	}

	@Override
	public boolean doIHaveThisInStock(final String name, final MOB mob)
	{
		return getStock(name,mob)!=null;
	}

	@Override
	public int stockPrice(final Environmental likeThis)
	{
		return -1;
	}

	@Override
	public int numberInStock(final Environmental likeThis)
	{
		return 1;
	}

	@Override
	public void resubmitInventory(final List<Environmental> V)
	{
	}

	@Override
	public Environmental getStock(final String name, final MOB mob)
	{
		final List<AuctionData> auctions=CMLib.coffeeShops().getAuctions(null,auctionShop);
		final Vector<Environmental> auctionItems=new Vector<Environmental>();
		for(int a=0;a<auctions.size();a++)
		{
			final Item I=auctions.get(a).getAuctionedItem();
			auctionItems.addElement(I);
		}
		for(int a=0;a<auctionItems.size();a++)
		{
			final Item I=(Item)auctionItems.elementAt(a);
			I.setExpirationDate(CMLib.english().getContextNumber(auctionItems,I));
		}
		Environmental item=CMLib.english().fetchEnvironmental(auctionItems,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(auctionItems,name,false);
		return item;
	}

	@Override
	public void destroyStoreInventory()
	{
	}

	@Override
	public Environmental removeStock(final String name, final MOB mob)
	{
		return null;
	}

	@Override
	public void emptyAllShelves()
	{
	}

	@Override
	public List<Environmental> removeSellableProduct(final String named, final MOB mob)
	{
		return emptyV;
	}

	@Override
	public String makeXML()
	{
		return "";
	}

	@Override
	public void buildShopFromXML(final String text)
	{
	}

	@Override
	public long contentHash()
	{
		return this.hashCode();
	}

	@Override
	public void deleteShelfProduct(final ShelfProduct P)
	{
	}
}
