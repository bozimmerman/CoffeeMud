package com.planet_ink.coffee_mud.Common;
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

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

@SuppressWarnings("unchecked")
public class AuctionCoffeeShop implements CoffeeShop
{
    public String ID(){return "AuctionCoffeeShop";}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public static final Vector emptyV=new Vector();
    public String auctionShop="";
    protected WeakReference<ShopKeeper> shopKeeper=null;
    
    public CMObject copyOf()
    {
        try
        {
            Object O=this.clone();
            return (CMObject)O;
        }
        catch(CloneNotSupportedException e)
        {
            return new AuctionCoffeeShop();
        }
    }
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new AuctionCoffeeShop();}}
    public void initializeClass(){}
    
    
    public CoffeeShop build(ShopKeeper SK) {
    	shopKeeper=new WeakReference(SK);
    	return this;
    }
    
    public ShopKeeper shopKeeper(){ return (shopKeeper==null)?null:shopKeeper.get();}
    public int whatIsSold(){ShopKeeper SK=shopKeeper(); return (SK==null)?ShopKeeper.DEAL_ANYTHING:SK.whatIsSold();}
    
    public boolean inBaseInventory(Environmental thisThang)
    {
        return false;
    }

    public Environmental addStoreInventory(Environmental thisThang){ return addStoreInventory(thisThang,1,-1);}
    public int baseStockSize(){ return 0;}
    public int totalStockSize(){ return 0;}
    public Vector getStoreInventory(){ return emptyV;}
    public Vector getStoreInventory(String srchStr){ return emptyV;}
    public Vector getBaseInventory(){ return emptyV;}
    
    public Environmental addStoreInventory(Environmental thisThang, 
                                           int number, 
                                           int price)
    {
    	if(shopKeeper() instanceof Auctioneer)
    		auctionShop=((Auctioneer)shopKeeper()).auctionHouse();
        return thisThang;
    }
    
    public int totalStockWeight(){return 0;}
    
    public int totalStockSizeIncludingDuplicates(){ return 0;}
    public void delAllStoreInventory(Environmental thisThang){}
    
    public boolean doIHaveThisInStock(String name, MOB mob){return getStock(name,mob)!=null;}

    public int stockPrice(Environmental likeThis)
    {
        return -1;
    }
    public int numberInStock(Environmental likeThis){ return 1;}
    public void resubmitInventory(Vector V){}
    
    public Environmental getStock(String name, MOB mob)
    {
    	Vector auctions=CMLib.coffeeShops().getAuctions(null,auctionShop);
    	Vector auctionItems=new Vector();
    	for(int a=0;a<auctions.size();a++)
    	{
    		Item I=((Auctioneer.AuctionData)auctions.elementAt(a)).auctioningI;
    		auctionItems.addElement(I);
    	}
    	for(int a=0;a<auctionItems.size();a++)
    	{
    		Item I=(Item)auctionItems.elementAt(a);
    		I.setExpirationDate(CMLib.english().getContextNumber(auctionItems,I));
    	}
        Environmental item=CMLib.english().fetchEnvironmental(auctionItems,name,true);
        if(item==null)
            item=CMLib.english().fetchEnvironmental(auctionItems,name,false);
        return item;
    }


    public Environmental removeStock(String name, MOB mob)
    {
        return null;
    }
    
    public void emptyAllShelves(){}
    
    public Vector removeSellableProduct(String named, MOB mob)
    {
        return emptyV;
    }
    
    public String makeXML(){return "";}
    public void buildShopFromXML(String text){}
}