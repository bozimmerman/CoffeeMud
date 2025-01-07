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
public class DefaultCoffeeShop implements CoffeeShop
{
	@Override
	public String ID()
	{
		return "DefaultCoffeeShop";
	}

	@Override
	public String name()
	{
		return ID();
	}

	WeakReference<ShopKeeper>		shopKeeper			= null;
	public SVector<Environmental>	enumerableInventory	= new SVector<Environmental>(); // for Only Inventory situations
	public List<ShelfProduct>		storeInventory		= new SVector<ShelfProduct>();
	protected volatile Integer		contentHash			= null;

	private static Converter<ShelfProduct,Environmental> converter=new Converter<ShelfProduct,Environmental>()
	{
		@Override
		public Environmental convert(final ShelfProduct obj)
		{
			return obj.product;
		}
	};

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			((DefaultCoffeeShop)O).cloneFix(this);
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultCoffeeShop();
		}
	}

	@Override
	public CoffeeShop weakCopyOf()
	{
		try
		{
			final Object O=this.clone();
			((DefaultCoffeeShop)O).enumerableInventory = new SVector<Environmental>(enumerableInventory);
			((DefaultCoffeeShop)O).storeInventory = new SVector<ShelfProduct>(storeInventory);
			((DefaultCoffeeShop)O).contentHash			= null;
			return (DefaultCoffeeShop)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultCoffeeShop();
		}
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
		return (shopKeeper == null) ? null : shopKeeper.get();
	}

	@Override
	public boolean isSold(final int code)
	{
		final ShopKeeper SK = shopKeeper();
		return (SK == null) ? false : SK.isSold(code);
	}

	protected Room startRoom()
	{
		return CMLib.map().getStartRoom(shopKeeper());
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultCoffeeShop();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	public void cloneFix(final DefaultCoffeeShop E)
	{
		storeInventory=new SVector<ShelfProduct>();
		enumerableInventory=new SVector<Environmental>();
		this.contentHash = null;
		final Hashtable<Environmental,Environmental> copyFix=new Hashtable<Environmental,Environmental>();
		for(final ShelfProduct SP: E.storeInventory)
		{
			if(SP.product!=null)
			{
				final Environmental I3=(Environmental)SP.product.copyOf();
				copyFix.put(SP.product,I3);
				stopTicking(I3);
				storeInventory.add(new ShelfProduct(I3,SP.number,SP.price));
				this.contentHash = null;
			}
		}
		for(int i=0;i<E.enumerableInventory.size();i++)
		{
			final Environmental I2=E.enumerableInventory.elementAt(i);
			if(I2!=null)
			{
				Environmental I3=copyFix.get(I2);
				if(I3==null)
					I3=(Environmental)I2.copyOf();
				stopTicking(I3);
				enumerableInventory.addElement(I3);
				this.contentHash = null;
			}
		}
	}

	protected void stopTicking(final Environmental E)
	{
		if((E instanceof Boardable)&&(E instanceof Item))
		{
			((Item)E).stopTicking();
		}
		else
		if(E!=null)
			CMLib.threads().deleteTick(E, -1);
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

	protected boolean shopCompare(final Environmental thang1, final Environmental thang2)
	{
		if((thang1==null)&&(thang2==null))
			return true;
		if((thang1==null)||(thang2==null))
			return false;
		if((thang1 instanceof DoorKey)&&(thang2 instanceof DoorKey))
			return thang1.sameAs(thang2);
		else
		if((thang1.isGeneric())&&(thang2.isGeneric()))
		{
			if(thang1.Name().equals(thang2.Name()))
				return true;
		}
		else
		if(CMClass.classID(thang1).equals(CMClass.classID(thang2)))
			return true;
		return false;
	}

	@Override
	public boolean inEnumerableInventory(final Environmental thisThang)
	{
		for(int x=0;x<enumerableInventory.size();x++)
		{
			final Environmental E=enumerableInventory.elementAt(x);
			if(shopCompare(E,thisThang))
				return true;
		}
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
		return enumerableInventory.size();
	}

	@Override
	public int totalStockSize()
	{
		return storeInventory.size();
	}

	@Override
	public Iterator<Environmental> getStoreInventory()
	{
		return new ConvertingIterator<ShelfProduct,Environmental>(storeInventory.iterator(),converter);
	}

	@Override
	public Iterator<ShelfProduct> getStoreShelves()
	{
		return storeInventory.iterator();
	}

	@Override
	public Iterator<Environmental> getStoreInventory(final String srchStr)
	{
		final List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		List<Environmental> V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, true);
		if((V!=null)&&(V.size()>0))
			return V.iterator();
		V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, false);
		if(V!=null)
			return V.iterator();
		return new Vector<Environmental>(1).iterator();
	}

	@Override
	public Iterator<Environmental> getEnumerableInventory()
	{
		return enumerableInventory.iterator();
	}

	protected Environmental preSaleCopyFix(final Environmental thisThang)
	{
		final Environmental E=(Environmental)thisThang.copyOf();
		stopTicking(E);
		if(E instanceof PrivateProperty)
		{
			final PrivateProperty P=(PrivateProperty)E;
			P.setOwnerName("");
			if(P instanceof LandTitle)
			{
				final LandTitle T=(LandTitle)P;
				final Boardable ship = CMLib.map().getShip(T.landPropertyID());
				if(ship != null)
				{
					final Item I=ship.getBoardableItem();
					if(I!=null)
					{
						I.removeFromOwnerContainer();
						return I;
					}
				}
			}

		}
		return E;
	}

	@Override
	public Environmental addStoreInventory(Environmental thisThang, int number, final int price)
	{
		if(number<0)
			number=1;
		if((thisThang instanceof LandTitle)
		&&(isSold(ShopKeeper.DEAL_LANDSELLER)||isSold(ShopKeeper.DEAL_CLANDSELLER)))
			return null; // prevent duplicates, since landsellers auto-generate titles in their inventory
		if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(!inEnumerableInventory(thisThang)))
		{
			final Environmental E=preSaleCopyFix(thisThang);
			if(!E.amDestroyed())
			{
				enumerableInventory.addElement(E);
				this.contentHash = null;
			}
		}
		final Environmental originalUncopiedThang=thisThang;
		if(thisThang instanceof InnKey)
		{
			Environmental copy=null;
			for(int v=0;v<number;v++)
			{
				copy=preSaleCopyFix(thisThang);
				if(!copy.amDestroyed())
				{
					((InnKey)copy).hangOnRack(shopKeeper());
					storeInventory.add(new ShelfProduct(copy,1,-1));
					this.contentHash = null;
				}
			}
		}
		else
		{
			Environmental copy=null;
			thisThang=preSaleCopyFix(thisThang);
			if(!thisThang.amDestroyed())
			{
				for(final ShelfProduct SP : storeInventory)
				{
					copy=SP.product;
					if(copy.Name().equals(thisThang.Name()))
					{
						SP.number+=number;
						if(price>0)
							SP.price=price;
						this.contentHash = null;
						return copy;
					}
				}
				storeInventory.add(new ShelfProduct(thisThang,number,price));
				this.contentHash = null;
			}
		}
		if(originalUncopiedThang instanceof Item)
			((Item)originalUncopiedThang).destroy();
		return thisThang;
	}

	@Override
	public int totalStockWeight()
	{
		int weight=0;
		for(final ShelfProduct SP : storeInventory)
		{
			if(SP.product instanceof Physical)
			{
				if(SP.number<=1)
					weight+=((Physical)SP.product).phyStats().weight();
				else
					weight+=(((Physical)SP.product).phyStats().weight()*SP.number);
			}
		}
		return weight;
	}

	@Override
	public int totalStockSizeIncludingDuplicates()
	{
		int num=0;
		for(final ShelfProduct SP : storeInventory)
		{
			if(SP.number<=1)
				num++;
			else
				num+=SP.number;
		}
		return num;
	}

	@Override
	public void delAllStoreInventory(final Environmental thisThang)
	{
		if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(inEnumerableInventory(thisThang)))
		{
			for(int v=enumerableInventory.size()-1;v>=0;v--)
			{
				final Environmental E=enumerableInventory.elementAt(v);
				if(shopCompare(E,thisThang))
				{
					enumerableInventory.removeElement(E);
					E.destroy();
				}
			}
		}
		for(final ShelfProduct SP : storeInventory)
		{
			if(shopCompare(SP.product,thisThang))
			{
				storeInventory.remove(SP);
				SP.product.destroy();
			}
		}
		this.contentHash = null;
	}

	@Override
	public boolean doIHaveThisInStock(final String name, final MOB mob)
	{
		final List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		Environmental item=CMLib.english().fetchEnvironmental(storeInv,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(storeInv,name,false);
		if((item==null)
		&&(mob!=null)
		&&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
			||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER))))
		{
			final List<Environmental> titles=CMLib.coffeeShops().addRealEstateTitles(new ArrayList<Environmental>(0),mob,this,startRoom());
			item=CMLib.english().fetchEnvironmental(titles,name,true);
			if(item==null)
				item=CMLib.english().fetchEnvironmental(titles,name,false);
		}
		if(item!=null)
			return true;
		return false;
	}

	@Override
	public int stockPrice(final Environmental likeThis)
	{
		if(likeThis==null)
			return -1;
		for(final ShelfProduct SP : storeInventory)
		{
			if(shopCompare(SP.product,likeThis))
				return SP.price;
		}
		return -1;
	}

	@Override
	public int numberInStock(final Environmental likeThis)
	{
		if(likeThis==null)
			return -1;
		int num=0;
		for(final ShelfProduct SP : storeInventory)
		{
			if(shopCompare(SP.product,likeThis))
				num+=SP.number;
		}
		return num;
	}

	@Override
	public Environmental getStock(final String name, final MOB mob)
	{
		final List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		Environmental item=CMLib.english().fetchEnvironmental(storeInv,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(storeInv,name,false);
		if((item==null)
		&&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
		   ||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER)))
		&&(mob!=null))
		{
			final List<Environmental> titles=CMLib.coffeeShops().addRealEstateTitles(new ArrayList<Environmental>(0),mob,this,startRoom());
			item=CMLib.english().fetchEnvironmental(titles,name,true);
			if(item==null)
				item=CMLib.english().fetchEnvironmental(titles,name,false);
		}
		return item;
	}

	@Override
	public Environmental removeStock(final String name, final MOB mob)
	{
		Environmental item=getStock(name,mob);
		for(final ShelfProduct SP : storeInventory)
		{
			if(SP.product==item)
			{
				final Environmental copyItem=(Environmental)item.copyOf();
				if(SP.number>1)
					SP.number--;
				else
				{
					storeInventory.remove(SP);
					item.destroy();
				}
				item=copyItem;
			}
		}
		if(item instanceof Physical)
		{
			((Physical)item).basePhyStats().setRejuv(PhyStats.NO_REJUV);
			((Physical)item).phyStats().setRejuv(PhyStats.NO_REJUV);
		}
		return item;
	}

	@Override
	public boolean lowerStock(final String name)
	{
		final Environmental item=getStock(name,null);
		for(final ShelfProduct SP : storeInventory)
		{
			if(SP.product==item)
			{
				if(SP.number>1)
					SP.number--;
				else
				{
					storeInventory.remove(SP);
					item.destroy();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void resubmitInventory(final List<Environmental> shopItems)
	{
		final TriadList<Environmental,Integer,Integer> addBacks=new TriadArrayList<Environmental,Integer,Integer>();
		for(final Environmental shopItem : shopItems)
		{
			final int num=numberInStock(shopItem);
			final int price=stockPrice(shopItem);
			addBacks.add(shopItem,Integer.valueOf(num),Integer.valueOf(price));
		}
		emptyAllShelves();
		for(int a=0;a<addBacks.size();a++)
		{
			addStoreInventory(
					addBacks.get(a).first,
					addBacks.get(a).second.intValue(),
					addBacks.get(a).third.intValue());
		}
		for(final Environmental shopItem : shopItems)
			shopItem.destroy();
	}

	@Override
	public void emptyAllShelves()
	{
		if(storeInventory!=null)
			storeInventory.clear();
		if(enumerableInventory!=null)
			enumerableInventory.clear();
		this.contentHash=null;
	}

	@Override
	public List<Environmental> removeSellableProduct(final String named, final MOB mob)
	{
		final List<Environmental> removedProductsV=new Vector<Environmental>();
		final Environmental product=removeStock(named,mob);
		if(product==null)
			return removedProductsV;
		removedProductsV.add(product);
		if(product instanceof Container)
		{
			DoorKey foundKey=null;
			final Container C=((Container)product);
			for(final ShelfProduct SP : storeInventory)
			{
				final Environmental I=SP.product;
				if((I instanceof Item)&&(((Item)I).container()==product))
				{
					if((I instanceof DoorKey)&&(((DoorKey)I).getKey().equals(C.keyName())))
						foundKey=(DoorKey)I;
					((Item)I).unWear();
					removedProductsV.add(I);
					storeInventory.remove(SP);
					((Item)I).setContainer(C);
				}
			}
			if((C.isLocked())&&(foundKey==null))
			{
				final String keyName=Double.toString(Math.random());
				C.setKeyName(keyName);
				C.setDoorsNLocks(C.hasADoor(),true,C.hasADoor(),C.hasALock(),false,C.defaultsLocked());
				final DoorKey key=(DoorKey)CMClass.getItem("StdKey");
				key.setKey(keyName);
				key.setContainer(C);
				removedProductsV.add(key);
			}
		}
		return removedProductsV;
	}

	@Override
	public String makeXML()
	{
		final StringBuffer itemstr=new StringBuffer("");
		final ShopKeeper shopKeep = shopKeeper();
		if(shopKeep != null)
		{
			itemstr.append(CMLib.xml().convertXMLtoTag("ISELL",shopKeep.getWhatIsSoldMask()));
			itemstr.append(CMLib.xml().convertXMLtoTag("IIMSK",CMLib.xml().parseOutAngleBrackets(shopKeep.getWhatIsSoldZappermask())));
			itemstr.append(CMLib.xml().convertXMLtoTag("IVTYP",CMParms.toListString(shopKeep.viewFlags())));
			itemstr.append(CMLib.xml().convertXMLtoTag("IPREJ",shopKeep.getRawPrejudiceFactors()));
			itemstr.append(CMLib.xml().convertXMLtoTag("IBUDJ",shopKeep.getRawBbudget()));
			itemstr.append(CMLib.xml().convertXMLtoTag("IDVAL",shopKeep.getRawDevalueRate()));
			itemstr.append(CMLib.xml().convertXMLtoTag("IGNOR",shopKeep.getRawIgnoreMask()));
			itemstr.append(CMLib.xml().convertXMLtoTag("PRICM",CMParms.toListString(shopKeep.getRawItemPricingAdjustments())));
		}
		itemstr.append("<INVS>");
		for(final Iterator<Environmental> i=getStoreInventory();i.hasNext();)
		{
			final Environmental E=i.next();
			itemstr.append("<INV>");
			itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("INUM",""+numberInStock(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("IVAL",""+stockPrice(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.coffeeMaker().getEnvironmentalMiscTextXML(E,true)));
			itemstr.append("</INV>");
		}
		return itemstr.toString()+"</INVS>";
	}

	@Override
	public void buildShopFromXML(final String text)
	{
		destroyStoreInventory();
		storeInventory=new SVector<ShelfProduct>();
		enumerableInventory=new SVector<Environmental>();
		this.contentHash = null;

		if(text.length()==0)
			return;
		final ShopKeeper shop=shopKeeper();
		if(shop==null)
		{
			Log.errOut("DefaultCoffeeShop","Error getting base shopKeeper obj host from "+text);
			return;
		}
		if(!text.trim().startsWith("<"))
		{
			String parm=CMParms.getParmStr(text,"ISELL",""+ShopKeeper.DEAL_ANYTHING);
			if((parm!=null)&&(CMath.isNumber(parm)))
				shop.setWhatIsSoldMask(CMath.s_long(parm));
			else
			if(parm!=null)
			{
				for(int s=0;s<ShopKeeper.DEAL_DESCS.length;s++)
				{
					if(parm.equalsIgnoreCase(ShopKeeper.DEAL_DESCS[s]))
						shop.setWhatIsSoldMask(s);
				}
			}
			parm=CMParms.getParmStr(text,"IIMSK","");
			if(parm!=null)
				shop.setWhatIsSoldZappermask(CMLib.xml().restoreAngleBrackets(parm.trim()));
			parm=CMParms.getParmStr(text,"IPREJ","");
			if(parm!=null)
				shop.setPrejudiceFactors(parm);
			parm=CMParms.getParmStr(text, "IVTYP", null);
			if(parm!=null)
			{
				shop.viewFlags().clear();
				for(final String s : CMParms.parseCommas(parm.toUpperCase().trim(),true))
				{
					final ViewType V = (ViewType)CMath.s_valueOf(ViewType.class, s);
					if(V != null)
						shop.viewFlags().add(V);
				}
			}
			parm=CMParms.getParmStr(text,"IBUDJ","1000000");
			if(parm!=null)
				shop.setBudget(parm);
			parm=CMParms.getParmStr(text,"IDVAL","");
			if(parm!=null)
				shop.setDevalueRate(parm);
			parm=CMParms.getParmStr(text,"IGNOR","");
			if(parm!=null)
				shop.setIgnoreMask(parm);
			parm=CMParms.getParmStr(text,"PRICM","");
			if(parm!=null)
				shop.setItemPricingAdjustments((parm.trim().length()==0)?new String[0]:CMParms.toStringArray(CMParms.parseCommas(parm,true)));
			return;
		}

		final List<XMLLibrary.XMLTag> xmlV=CMLib.xml().parseAllXML(text);
		if(xmlV==null)
		{
			Log.errOut("DefaultCoffeeShop","Error parsing data.");
			return;
		}
		String parm=CMLib.xml().getValFromPieces(xmlV,"ISELL");
		if((parm!=null)&&(CMath.isNumber(parm)))
			shop.setWhatIsSoldMask(CMath.s_long(parm));
		parm=CMParms.getParmStr(text,"IIMSK","");
		if(parm!=null)
			shop.setWhatIsSoldZappermask(CMLib.xml().restoreAngleBrackets(parm.trim()));
		parm=CMLib.xml().getValFromPieces(xmlV,"IPREJ");
		if(parm!=null)
			shop.setPrejudiceFactors(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IBUDJ");
		if(parm!=null)
			shop.setBudget(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IDVAL");
		if(parm!=null)
			shop.setDevalueRate(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IGNOR");
		if(parm!=null)
			shop.setIgnoreMask(parm);

		final List<XMLLibrary.XMLTag> iV=CMLib.xml().getContentsFromPieces(xmlV,"INVS");
		if(iV==null)
		{
			Log.errOut("DefaultCoffeeShop","Error parsing 'INVS'.");
			return;
		}
		for(int i=0;i<iV.size();i++)
		{
			final XMLTag iblk=iV.get(i);
			if((!iblk.tag().equalsIgnoreCase("INV"))||(iblk.contents()==null))
			{
				Log.errOut("DefaultCoffeeShop","Error parsing 'INVS' data.");
				return;
			}
			final String itemi=iblk.getValFromPieces("ICLASS");
			final int itemnum=iblk.getIntFromPieces("INUM");
			final int val=iblk.getIntFromPieces("IVAL");
			PhysicalAgent newOne=CMClass.getItem(itemi);
			if(newOne==null)
				newOne=CMClass.getMOB(itemi);
			final List<XMLLibrary.XMLTag> idat=iblk.getContentsFromPieces("IDATA");
			if((idat==null)||(newOne==null))
			{
				Log.errOut("DefaultCoffeeShop","Error parsing 'INV' data.");
				return;
			}
			CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(newOne,idat,true);
			final PhysicalAgent P=newOne;
			P.recoverPhyStats();
			addStoreInventory(P,itemnum,val);
			P.destroy();
		}
	}

	@Override
	public long contentHash()
	{
		final Integer cHash = this.contentHash;
		if(cHash == null)
		{
			int hash = 0;
			final List<Environmental> einv = this.enumerableInventory;
			for(int v=einv.size()-1;v>=0;v--)
			{
				try
				{
					hash ^= einv.get(v).hashCode();
				}
				catch(final Exception e)
				{
				}
			}
			final List<ShelfProduct> sps = this.storeInventory;
			for(int v=sps.size()-1;v>=0;v--)
			{
				try
				{
					hash ^= sps.get(v).hashCode();
				}
				catch(final Exception e)
				{
				}
			}
			this.contentHash = Integer.valueOf(hash);
			return hash;
		}
		else
			return cHash.intValue();
	}
}
