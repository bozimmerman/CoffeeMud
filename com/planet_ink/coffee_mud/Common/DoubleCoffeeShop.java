package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
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
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2005-2024 Bo Zimmerman

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
public class DoubleCoffeeShop extends DefaultCoffeeShop
{
	@Override
	public String ID()
	{
		return "DoubleCoffeeShop";
	}

	private CoffeeShop secondShop = null;

	@Override
	public CoffeeShop build(final ShopKeeper SK)
	{
		shopKeeper=new WeakReference<ShopKeeper>(SK);
		secondShop = SK.getShop();
		return this;
	}

	@Override
	public void destroyStoreInventory()
	{
		for(final Environmental E : enumerableInventory)
			E.destroy();
		for(final ShelfProduct SP : storeInventory)
			SP.product.destroy();
		enumerableInventory.clear();
		storeInventory.clear();
		this.contentHash=null;
	}

	@Override
	public void deleteShelfProduct(final ShelfProduct P)
	{
		storeInventory.remove(P);
	}

	@Override
	public boolean inEnumerableInventory(final Environmental thisThang)
	{
		return super.inEnumerableInventory(thisThang) && secondShop.inEnumerableInventory(thisThang);
	}

	@Override
	public int enumerableStockSize()
	{
		return super.enumerableStockSize() + secondShop.enumerableStockSize();
	}

	@Override
	public int totalStockSize()
	{
		return super.totalStockSize() + secondShop.totalStockSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Environmental> getStoreInventory()
	{
		return new MultiIterator<Environmental>(new Iterator[] {
				super.getStoreInventory(), secondShop.getStoreInventory()
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<ShelfProduct> getStoreShelves()
	{
		return new MultiIterator<ShelfProduct>(new Iterator[] {
				super.getStoreShelves(), secondShop.getStoreShelves()
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Environmental> getStoreInventory(final String srchStr)
	{
		return new MultiIterator<Environmental>(new Iterator[] {
				super.getStoreInventory(srchStr), secondShop.getStoreInventory(srchStr)
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Environmental> getEnumerableInventory()
	{
		return new MultiIterator<Environmental>(new Iterator[] {
				super.getEnumerableInventory(), secondShop.getEnumerableInventory()
		});
	}

	@Override
	public int totalStockWeight()
	{
		return super.totalStockSize() + secondShop.totalStockSize();
	}

	@Override
	public int totalStockSizeIncludingDuplicates()
	{
		return super.totalStockSizeIncludingDuplicates() + secondShop.totalStockSizeIncludingDuplicates();
	}

	@Override
	public void delAllStoreInventory(final Environmental thisThang)
	{
		super.delAllStoreInventory(thisThang);
		secondShop.delAllStoreInventory(thisThang);
	}

	@Override
	public boolean doIHaveThisInStock(final String name, final MOB mob)
	{
		return super.doIHaveThisInStock(name, mob) || secondShop.doIHaveThisInStock(name, mob);
	}

	@Override
	public int stockPrice(final Environmental likeThis)
	{
		int price = super.stockPrice(likeThis);
		if(price < 0)
			price = secondShop.stockPrice(likeThis);
		return price;
	}

	@Override
	public int numberInStock(final Environmental likeThis)
	{
		int price = super.numberInStock(likeThis);
		if(price < 0)
			price = secondShop.numberInStock(likeThis);
		return price;
	}

	@Override
	public Environmental getStock(final String name, final MOB mob)
	{
		final Environmental E = super.getStock(name, mob);
		if(E == null)
			return secondShop.getStock(name, mob);
		return E;
	}

	@Override
	public Environmental removeStock(final String name, final MOB mob)
	{
		final Environmental E = super.removeStock(name, mob);
		if(E == null)
			return secondShop.removeStock(name, mob);
		return E;
	}

	@Override
	public boolean lowerStock(final String name)
	{
		if(super.lowerStock(name))
			return true;
		return secondShop.lowerStock(name);
	}

	@Override
	public void emptyAllShelves()
	{
		secondShop.emptyAllShelves();
	}

	@Override
	public List<Environmental> removeSellableProduct(final String named, final MOB mob)
	{
		List<Environmental> removed = super.removeSellableProduct(named, mob);
		if((removed == null) || (removed.size()==0))
			removed = secondShop.removeSellableProduct(named, mob);
		return removed;
	}
}
