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
Copyright 2008-2013 Bo Zimmerman

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
	public String ID(){return "DefaultCoffeeShop";}
	public String name() { return ID();}
	WeakReference<ShopKeeper> shopKeeper = null;
	public SVector<Environmental> enumerableInventory=new SVector<Environmental>(); // for Only Inventory situations
	public List<ShelfProduct> storeInventory=new SVector<ShelfProduct>();
	
	private static Converter<ShelfProduct,Environmental> converter=new Converter<ShelfProduct,Environmental>()
	{
		public Environmental convert(ShelfProduct obj) {
			return obj.product;
		}
	};
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			Object O=this.clone();
			((DefaultCoffeeShop)O).cloneFix(this);
			return (CMObject)O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultCoffeeShop();
		}
	}
	
	public CoffeeShop build(ShopKeeper SK) {
		shopKeeper=new WeakReference<ShopKeeper>(SK);
		return this;
	}
	
	public ShopKeeper shopKeeper(){ return (shopKeeper==null)?null:shopKeeper.get();}
	public boolean isSold(int code){ShopKeeper SK=shopKeeper(); return (SK==null)?false:SK.isSold(code);}
	protected Room startRoom() { return CMLib.map().getStartRoom(shopKeeper());}
	
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCoffeeShop();}}
	public void initializeClass(){}
	
	public void cloneFix(DefaultCoffeeShop E)
	{
		storeInventory=new SVector<ShelfProduct>();
		enumerableInventory=new SVector<Environmental>();
		Hashtable<Environmental,Environmental> copyFix=new Hashtable<Environmental,Environmental>();
		for(ShelfProduct SP: storeInventory)
			if(SP.product!=null)
			{
				Environmental I3=(Environmental)SP.product.copyOf();
				copyFix.put(SP.product,I3);
				CMLib.threads().deleteTick(I3,-1);
				storeInventory.add(new ShelfProduct(I3,SP.number,SP.price));
			}
		for(int i=0;i<E.enumerableInventory.size();i++)
		{
			Environmental I2=(Environmental)E.enumerableInventory.elementAt(i);
			if(I2!=null)
			{
				Environmental I3=(Environmental)copyFix.get(I2);
				if(I3==null) I3=(Environmental)I2.copyOf();
				CMLib.threads().deleteTick(I3,-1);
				enumerableInventory.addElement(I3);
			}
		}
	}

	protected boolean shopCompare(Environmental thang1, Environmental thang2)
	{
		if((thang1==null)&&(thang2==null)) return true;
		if((thang1==null)||(thang2==null)) return false;
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
	
	public boolean inEnumerableInventory(Environmental thisThang)
	{
		for(int x=0;x<enumerableInventory.size();x++)
		{
			Environmental E=(Environmental)enumerableInventory.elementAt(x);
			if(shopCompare(E,thisThang)) return true;
		}
		return false;
	}

	public Environmental addStoreInventory(Environmental thisThang)
	{
		return addStoreInventory(thisThang,1,-1);
	}

	public int enumerableStockSize()
	{
		return enumerableInventory.size();
	}

	public int totalStockSize()
	{
		return storeInventory.size();
	}

	public Iterator<Environmental> getStoreInventory()
	{
		return new ConvertingIterator<ShelfProduct,Environmental>(storeInventory.iterator(),converter);
	}
	public Iterator<Environmental> getStoreInventory(String srchStr)
	{
		List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		List<Environmental> V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, true);
		if((V!=null)&&(V.size()>0)) return V.iterator();
		V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, false);
		if(V!=null) return V.iterator();
		return new Vector<Environmental>(1).iterator();
	}
	public Iterator<Environmental> getEnumerableInventory()
	{
		return enumerableInventory.iterator();
	}

	public Environmental addStoreInventory(Environmental thisThang, 
										   int number, 
										   int price)
	{
		if(number<0) number=1;
		if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(!inEnumerableInventory(thisThang)))
		{
			Environmental E=(Environmental)thisThang.copyOf();
			CMLib.threads().deleteTick(E,-1);
			if(!E.amDestroyed())
			{
				enumerableInventory.addElement(E);
			}
		}
		Environmental originalUncopiedThang=thisThang;
		if(thisThang instanceof InnKey)
		{
			Environmental copy=null;
			for(int v=0;v<number;v++)
			{
				copy=(Environmental)thisThang.copyOf();
				CMLib.threads().deleteTick(copy,-1);
				if(!copy.amDestroyed())
				{
					((InnKey)copy).hangOnRack(shopKeeper());
					storeInventory.add(new ShelfProduct(copy,1,-1));
				}
			}
		}
		else
		{
			Environmental copy=null;
			thisThang=(Environmental)thisThang.copyOf();
			CMLib.threads().deleteTick(thisThang,-1);
			if(!thisThang.amDestroyed())
			{
				for(ShelfProduct SP : storeInventory)
				{
					copy=(Environmental)SP.product;
					if(copy.Name().equals(thisThang.Name()))
					{
						SP.number+=number;
						if(price>0) SP.price=price;
						return copy;
					}
				}
				storeInventory.add(new ShelfProduct(thisThang,number,price));
			}
		}
		if(originalUncopiedThang instanceof Item)
			((Item)originalUncopiedThang).destroy();
		return thisThang;
	}
	
	public int totalStockWeight()
	{
		int weight=0;
		for(ShelfProduct SP : storeInventory)
			if(SP.product instanceof Physical)
				if(SP.number<=1)
					weight+=((Physical)SP.product).phyStats().weight();
				else
					weight+=(((Physical)SP.product).phyStats().weight()*SP.number);
		return weight;
	}
	public int totalStockSizeIncludingDuplicates()
	{
		int num=0;
		for(ShelfProduct SP : storeInventory)
			if(SP.number<=1)
				num++;
			else
				num+=SP.number;
		return num;
	}

	public void delAllStoreInventory(Environmental thisThang)
	{
		if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(inEnumerableInventory(thisThang)))
		{
			for(int v=enumerableInventory.size()-1;v>=0;v--)
			{
				Environmental E=(Environmental)enumerableInventory.elementAt(v);
				if(shopCompare(E,thisThang))
					enumerableInventory.removeElement(E);
			}
		}
		for(ShelfProduct SP : storeInventory)
			if(shopCompare(SP.product,thisThang))
				storeInventory.remove(SP);
	}
	
	public boolean doIHaveThisInStock(String name, MOB mob)
	{
		List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		Environmental item=CMLib.english().fetchEnvironmental(storeInv,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(storeInv,name,false);
		if((item==null)
		   &&(mob!=null)
		   &&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
			  ||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER))))
		{
			List<Environmental> titles=CMLib.coffeeShops().addRealEstateTitles(new Vector<Environmental>(),mob,this,startRoom());
			item=CMLib.english().fetchEnvironmental(titles,name,true);
			if(item==null)
				item=CMLib.english().fetchEnvironmental(titles,name,false);
		}
		if(item!=null)
		   return true;
		return false;
	}

	public int stockPrice(Environmental likeThis)
	{
		for(ShelfProduct SP : storeInventory)
			if(shopCompare(SP.product,likeThis))
				return SP.price;
		return -1;
	}
	public int numberInStock(Environmental likeThis)
	{
		int num=0;
		for(ShelfProduct SP : storeInventory)
			if(shopCompare(SP.product,likeThis))
				num+=SP.number;
		return num;
	}

	public Environmental getStock(String name, MOB mob)
	{
		List<Environmental> storeInv=new ConvertingList<ShelfProduct,Environmental>(storeInventory,converter);
		Environmental item=CMLib.english().fetchEnvironmental(storeInv,name,true);
		if(item==null)
			item=CMLib.english().fetchEnvironmental(storeInv,name,false);
		if((item==null)
		&&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
		   ||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER)))
		&&(mob!=null))
		{
			List<Environmental> titles=CMLib.coffeeShops().addRealEstateTitles(new Vector<Environmental>(),mob,this,startRoom());
			item=CMLib.english().fetchEnvironmental(titles,name,true);
			if(item==null)
				item=CMLib.english().fetchEnvironmental(titles,name,false);
		}
		return item;
	}


	public Environmental removeStock(String name, MOB mob)
	{
		Environmental item=getStock(name,mob);
		if(item instanceof Ability)
			return item;
		if(item instanceof Physical)
		{
			for(ShelfProduct SP : storeInventory)
				if(SP.product==item)
				{
					Environmental copyItem=(Environmental)item.copyOf();
					if(SP.number>1)
						SP.number--;
					else
					{
						storeInventory.remove(SP);
						item.destroy();
					}
					item=copyItem;
				}
			((Physical)item).basePhyStats().setRejuv(PhyStats.NO_REJUV);
			((Physical)item).phyStats().setRejuv(PhyStats.NO_REJUV);
		}
		return item;
	}
	
	public void resubmitInventory(List<Environmental> shopItems)
	{
		DVector addBacks=new DVector(3);
		for(Environmental shopItem : shopItems)
		{
			int num=numberInStock(shopItem);
			int price=stockPrice(shopItem);
			addBacks.addElement(shopItem,Integer.valueOf(num),Integer.valueOf(price));
		}
		emptyAllShelves();
		for(int a=0;a<addBacks.size();a++)
			addStoreInventory(
					(Environmental)addBacks.elementAt(a,1),
					((Integer)addBacks.elementAt(a,2)).intValue(),
					((Integer)addBacks.elementAt(a,3)).intValue());
	}
	
	public void emptyAllShelves()
	{
		if(storeInventory!=null)storeInventory.clear();
		if(enumerableInventory!=null)enumerableInventory.clear();
	}
	public List<Environmental> removeSellableProduct(String named, MOB mob)
	{
		Vector<Environmental> V=new Vector<Environmental>();
		Environmental product=removeStock(named,mob);
		if(product==null) return V;
		V.addElement(product);
		if(product instanceof Container)
		{
			DoorKey foundKey=null;
			Container C=((Container)product);
			for(ShelfProduct SP : storeInventory)
			{
				Environmental I=SP.product;
				if((I instanceof Item)&&(((Item)I).container()==product))
				{
					if((I instanceof DoorKey)&&(((DoorKey)I).getKey().equals(C.keyName())))
						foundKey=(DoorKey)I;
					((Item)I).unWear();
					V.addElement(I);
					storeInventory.remove(SP);
					((Item)I).setContainer(C);
				}
			}
			if((C.isLocked())&&(foundKey==null))
			{
				String keyName=Double.toString(Math.random());
				C.setKeyName(keyName);
				C.setLidsNLocks(C.hasALid(),true,C.hasALock(),false);
				DoorKey key=(DoorKey)CMClass.getItem("StdKey");
				key.setKey(keyName);
				key.setContainer(C);
				V.addElement(key);
			}
		}
		return V;
	}
	
	public String makeXML()
	{
		StringBuffer itemstr=new StringBuffer("");
		itemstr.append(CMLib.xml().convertXMLtoTag("ISELL",shopKeeper().getWhatIsSoldMask()));
		itemstr.append(CMLib.xml().convertXMLtoTag("IPREJ",shopKeeper().prejudiceFactors()));
		itemstr.append(CMLib.xml().convertXMLtoTag("IBUDJ",shopKeeper().budget()));
		itemstr.append(CMLib.xml().convertXMLtoTag("IDVAL",shopKeeper().devalueRate()));
		itemstr.append(CMLib.xml().convertXMLtoTag("IGNOR",shopKeeper().ignoreMask()));
		itemstr.append(CMLib.xml().convertXMLtoTag("PRICM",CMParms.toStringList(shopKeeper().itemPricingAdjustments())));
		itemstr.append("<INVS>");
		for(Iterator<Environmental> i=getStoreInventory();i.hasNext();)
		{
			Environmental E=i.next();
			itemstr.append("<INV>");
			itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("INUM",""+numberInStock(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("IVAL",""+stockPrice(E)));
			itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.coffeeMaker().getPropertiesStr(E,true)));
			itemstr.append("</INV>");
		}
		return itemstr.toString()+"</INVS>";
	}

	public void buildShopFromXML(String text)
	{
		Vector<Environmental> V=new Vector<Environmental>();
		storeInventory=new SVector<ShelfProduct>();
		enumerableInventory=new SVector<Environmental>();
		
		if(text.length()==0) return;
		ShopKeeper shop=shopKeeper();
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
			for(int s=0;s<ShopKeeper.DEAL_DESCS.length;s++)
				if(parm.equalsIgnoreCase(ShopKeeper.DEAL_DESCS[s]))
					shop.setWhatIsSoldMask(s);
			parm=CMParms.getParmStr(text,"IPREJ","");
			if(parm!=null) shop.setPrejudiceFactors(parm);
			parm=CMParms.getParmStr(text,"IBUDJ","1000000");
			if(parm!=null) shop.setBudget(parm);
			parm=CMParms.getParmStr(text,"IDVAL","");
			if(parm!=null) shop.setDevalueRate(parm);
			parm=CMParms.getParmStr(text,"IGNOR","");
			if(parm!=null) shop.setIgnoreMask(parm);
			parm=CMParms.getParmStr(text,"PRICM","");
			if(parm!=null) shop.setItemPricingAdjustments((parm.trim().length()==0)?new String[0]:CMParms.toStringArray(CMParms.parseCommas(parm,true)));
			return;
		}

		List<XMLLibrary.XMLpiece> xmlV=CMLib.xml().parseAllXML(text);
		if(xmlV==null)
		{
			Log.errOut("DefaultCoffeeShop","Error parsing data.");
			return;
		}
		String parm=CMLib.xml().getValFromPieces(xmlV,"ISELL");
		if((parm!=null)&&(CMath.isNumber(parm))) 
			shop.setWhatIsSoldMask(CMath.s_long(parm));
		parm=CMLib.xml().getValFromPieces(xmlV,"IPREJ");
		if(parm!=null) shop.setPrejudiceFactors(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IBUDJ");
		if(parm!=null) shop.setBudget(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IDVAL");
		if(parm!=null) shop.setDevalueRate(parm);
		parm=CMLib.xml().getValFromPieces(xmlV,"IGNOR");
		if(parm!=null) shop.setIgnoreMask(parm);
		
		List<XMLLibrary.XMLpiece> iV=CMLib.xml().getContentsFromPieces(xmlV,"INVS");
		if(iV==null)
		{
			Log.errOut("DefaultCoffeeShop","Error parsing 'INVS'.");
			return;
		}
		for(int i=0;i<iV.size();i++)
		{
			XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)iV.get(i);
			if((!iblk.tag.equalsIgnoreCase("INV"))||(iblk.contents==null))
			{
				Log.errOut("DefaultCoffeeShop","Error parsing 'INVS' data.");
				return;
			}
			String itemi=CMLib.xml().getValFromPieces(iblk.contents,"ICLASS");
			int itemnum=CMLib.xml().getIntFromPieces(iblk.contents,"INUM");
			int val=CMLib.xml().getIntFromPieces(iblk.contents,"IVAL");
			PhysicalAgent newOne=CMClass.getItem(itemi);
			if(newOne==null) newOne=CMClass.getMOB(itemi);
			List<XMLLibrary.XMLpiece> idat=CMLib.xml().getContentsFromPieces(iblk.contents,"IDATA");
			if((idat==null)||(newOne==null)||(!(newOne instanceof Item)))
			{
				Log.errOut("DefaultCoffeeShop","Error parsing 'INV' data.");
				return;
			}
			CMLib.coffeeMaker().setPropertiesStr(newOne,idat,true);
			PhysicalAgent P=(PhysicalAgent)newOne;
			P.recoverPhyStats();
			V.addElement(P);
			addStoreInventory(P,itemnum,val);
		}
	}
}
