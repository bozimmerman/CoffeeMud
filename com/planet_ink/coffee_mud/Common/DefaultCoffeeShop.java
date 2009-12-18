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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
Copyright 2008-2010 Bo Zimmerman

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
public class DefaultCoffeeShop implements CoffeeShop
{
    public String ID(){return "DefaultCoffeeShop";}
	WeakReference<ShopKeeper> shopKeeper = null;
	
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
    	shopKeeper=new WeakReference(SK);
    	return this;
    }
    
    public ShopKeeper shopKeeper(){ return (shopKeeper==null)?null:shopKeeper.get();}
    public boolean isSold(int code){ShopKeeper SK=shopKeeper(); return (SK==null)?false:SK.isSold(code);}
    protected Room startRoom() { return CMLib.map().getStartRoom(shopKeeper());}
    
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCoffeeShop();}}
    public void initializeClass(){}
    
    public Vector baseInventory=new Vector(); // for Only Inventory situations
    public DVector storeInventory=new DVector(3);
    
    public void cloneFix(DefaultCoffeeShop E)
    {
        storeInventory=new DVector(3);
        baseInventory=new Vector();
        Hashtable copyFix=new Hashtable();
        for(int i=0;i<E.storeInventory.size();i++)
        {
            Environmental I2=(Environmental)E.storeInventory.elementAt(i,1);
            Integer N=(Integer)E.storeInventory.elementAt(i,2);
            Integer P=(Integer)E.storeInventory.elementAt(i,3);
            if(I2!=null)
            {
                Environmental I3=(Environmental)I2.copyOf();
                copyFix.put(I2,I3);
            	CMLib.threads().deleteTick(I3,-1);
                storeInventory.addElement(I3,N,P);
            }
        }
        for(int i=0;i<E.baseInventory.size();i++)
        {
            Environmental I2=(Environmental)E.baseInventory.elementAt(i);
            if(I2!=null)
            {
                Environmental I3=(Environmental)copyFix.get(I2);
                if(I3==null) I3=(Environmental)I2.copyOf();
            	CMLib.threads().deleteTick(I3,-1);
                baseInventory.addElement(I3);
            }
        }
    }

    protected boolean shopCompare(Environmental thang1, Environmental thang2)
    {
    	if((thang1==null)&&(thang2==null)) return true;
    	if((thang1==null)||(thang2==null)) return false;
    	if((thang1 instanceof Key)&&(thang2 instanceof Key))
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
    
    public boolean inBaseInventory(Environmental thisThang)
    {
        for(int x=0;x<baseInventory.size();x++)
        {
            Environmental E=(Environmental)baseInventory.elementAt(x);
            if(shopCompare(E,thisThang)) return true;
        }
        return false;
    }

    public Environmental addStoreInventory(Environmental thisThang)
    {
        return addStoreInventory(thisThang,1,-1);
    }

    public int baseStockSize()
    {
        return baseInventory.size();
    }

    public int totalStockSize()
    {
        return storeInventory.size();
    }

    public Vector getStoreInventory()
    {
        return (Vector)storeInventory.getDimensionVector(1).clone();
    }
    public Vector getStoreInventory(String srchStr)
    {
    	Vector storeInv=(Vector)storeInventory.getDimensionVector(1).clone();
    	Vector V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, true);
    	if((V!=null)&&(V.size()>0)) return V;
    	V=CMLib.english().fetchEnvironmentals(storeInv, srchStr, false);
    	if(V!=null) return V;
        return new Vector(1);
    }
    public Vector getBaseInventory()
    {
        return baseInventory;
    }

    public Environmental addStoreInventory(Environmental thisThang, 
                                           int number, 
                                           int price)
    {
        if(number<0) number=1;
        if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(!inBaseInventory(thisThang)))
        {
        	Environmental E=(Environmental)thisThang.copyOf();
        	CMLib.threads().deleteTick(E,-1);
            baseInventory.addElement(E);
        }
        Environmental originalUncopiedThang=thisThang;
        if(thisThang instanceof InnKey)
        {
            Environmental copy=null;
            for(int v=0;v<number;v++)
            {
                copy=(Environmental)thisThang.copyOf();
                ((InnKey)copy).hangOnRack(shopKeeper());
            	CMLib.threads().deleteTick(copy,-1);
                storeInventory.addElement(copy,Integer.valueOf(1),Integer.valueOf(-1));
            }
        }
        else
        {
            Environmental copy=null;
            thisThang=(Environmental)thisThang.copyOf();
        	CMLib.threads().deleteTick(thisThang,-1);
            for(int e=0;e<storeInventory.size();e++)
            {
                copy=(Environmental)storeInventory.elementAt(e,1);
                if(copy.Name().equals(thisThang.Name()))
                {
                    Integer I=(Integer)storeInventory.elementAt(e,2);
                    storeInventory.setElementAt(e,2,Integer.valueOf(I.intValue()+number));
                    if(price>0) storeInventory.setElementAt(e,3,Integer.valueOf(price));
                    return copy;
                }
            }
            storeInventory.addElement(thisThang,Integer.valueOf(number),Integer.valueOf(price));
        }
        if(originalUncopiedThang instanceof Item)
            ((Item)originalUncopiedThang).destroy();
        return thisThang;
    }
    
    public int totalStockWeight()
    {
        Environmental E=null;
        int weight=0;
        for(int i=0;i<storeInventory.size();i++)
        {
            E=(Environmental)storeInventory.elementAt(i,1);
            Integer I=(Integer)storeInventory.elementAt(i,2);
            if(I==null)
                weight+=E.envStats().weight();
            else
                weight+=(E.envStats().weight()*I.intValue());
        }
        return weight;
    }
    public int totalStockSizeIncludingDuplicates()
    {
        int num=0;
        for(int i=0;i<storeInventory.size();i++)
        {
            Integer I=(Integer)storeInventory.elementAt(i,2);
            if(I==null) 
                num++;
            else
                num+=I.intValue();
        }
        return num;
    }

    public void delAllStoreInventory(Environmental thisThang)
    {
        if((isSold(ShopKeeper.DEAL_INVENTORYONLY))&&(inBaseInventory(thisThang)))
        {
            for(int v=baseInventory.size()-1;v>=0;v--)
            {
                Environmental E=(Environmental)baseInventory.elementAt(v);
                if(shopCompare(E,thisThang))
                	baseInventory.removeElement(E);
            }
        }
        for(int v=storeInventory.size()-1;v>=0;v--)
        {
            Environmental E=(Environmental)storeInventory.elementAt(v,1);
            if(shopCompare(E,thisThang))
            	storeInventory.removeElement(E);
        }
    }
    
    public boolean doIHaveThisInStock(String name, MOB mob)
    {
        Environmental item=CMLib.english().fetchEnvironmental(storeInventory.getDimensionVector(1),name,true);
        if(item==null)
            item=CMLib.english().fetchEnvironmental(storeInventory.getDimensionVector(1),name,false);
        if((item==null)
           &&(mob!=null)
           &&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
              ||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER))))
        {
            Vector titles=CMLib.coffeeShops().addRealEstateTitles(new Vector(),mob,this,startRoom());
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
        Environmental E=null;
        Integer I=null;
        for(int v=0;v<storeInventory.size();v++)
        {
            E=(Environmental)storeInventory.elementAt(v,1);
            I=(Integer)storeInventory.elementAt(v,3);
            if(shopCompare(E,likeThis))
            	return I.intValue();
        }
        return -1;
    }
    public int numberInStock(Environmental likeThis)
    {
        int num=0;
        Environmental E=null;
        Integer N=null;
        for(int v=0;v<storeInventory.size();v++)
        {
            E=(Environmental)storeInventory.elementAt(v,1);
            N=(Integer)storeInventory.elementAt(v,2);
            if(shopCompare(E,likeThis))
            	num+=N.intValue();
        }

        return num;
    }

    public Environmental getStock(String name, MOB mob)
    {
        Environmental item=CMLib.english().fetchEnvironmental(storeInventory.getDimensionVector(1),name,true);
        if(item==null)
            item=CMLib.english().fetchEnvironmental(storeInventory.getDimensionVector(1),name,false);
        if((item==null)
        &&((isSold(ShopKeeper.DEAL_LANDSELLER))||(isSold(ShopKeeper.DEAL_CLANDSELLER))
           ||(isSold(ShopKeeper.DEAL_SHIPSELLER))||(isSold(ShopKeeper.DEAL_CSHIPSELLER)))
        &&(mob!=null))
        {
            Vector titles=CMLib.coffeeShops().addRealEstateTitles(new Vector(),mob,this,startRoom());
            item=CMLib.english().fetchEnvironmental(titles,name,true);
            if(item==null)
                item=CMLib.english().fetchEnvironmental(titles,name,false);
        }
        return item;
    }


    public Environmental removeStock(String name, MOB mob)
    {
        Environmental item=getStock(name,mob);
        if(item!=null)
        {
            if(item instanceof Ability)
                return item;

            int index=storeInventory.indexOf(item);
            if(index>=0)
            {
                Integer possNum=(Integer)storeInventory.elementAt(index,2);
                int possValue=possNum.intValue();
                possValue--;
                Environmental copyItem=(Environmental)item.copyOf();
                if(possValue>=1)
                    storeInventory.setElementAt(index,2,Integer.valueOf(possValue));
                else
                {
                    storeInventory.removeElementAt(index);
                    item.destroy();
                }
                item=copyItem;
            }
            else
                storeInventory.removeElement(item);
            item.baseEnvStats().setRejuv(0);
            item.envStats().setRejuv(0);
        }
        return item;
    }
    
    public void resubmitInventory(Vector shopItems)
    {
    	DVector addBacks=new DVector(3);
        for(int b=0;b<shopItems.size();b++)
        {
            Environmental shopItem=(Environmental)shopItems.elementAt(b);
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
        if(baseInventory!=null)baseInventory.clear();
    }
    public Vector removeSellableProduct(String named, MOB mob)
    {
        Vector V=new Vector();
        Environmental product=removeStock(named,mob);
        if(product==null) return V;
        V.addElement(product);
        if(product instanceof Container)
        {
            int i=0;
            Key foundKey=null;
            Container C=((Container)product);
            while(i<storeInventory.size())
            {
                int a=storeInventory.size();
                Environmental I=(Environmental)storeInventory.elementAt(i,1);
                if((I instanceof Item)&&(((Item)I).container()==product))
                {
                    if((I instanceof Key)&&(((Key)I).getKey().equals(C.keyName())))
                        foundKey=(Key)I;
                    ((Item)I).unWear();
                    V.addElement(I);
                    storeInventory.removeElement(I);
                    ((Item)I).setContainer((Item)product);
                }
                if(a==storeInventory.size())
                    i++;
            }
            if((C.isLocked())&&(foundKey==null))
            {
                String keyName=Double.toString(Math.random());
                C.setKeyName(keyName);
                C.setLidsNLocks(C.hasALid(),true,C.hasALock(),false);
                Key key=(Key)CMClass.getItem("StdKey");
                key.setKey(keyName);
                key.setContainer(C);
                V.addElement(key);
            }
        }
        return V;
    }
    
    public String makeXML()
    {
        Vector V=getStoreInventory();
        if((V!=null)&&(V.size()>0))
        {
            StringBuffer itemstr=new StringBuffer("");
            itemstr.append(CMLib.xml().convertXMLtoTag("ISELL",shopKeeper().getWhatIsSoldMask()));
            itemstr.append(CMLib.xml().convertXMLtoTag("IPREJ",shopKeeper().prejudiceFactors()));
            itemstr.append(CMLib.xml().convertXMLtoTag("IBUDJ",shopKeeper().budget()));
            itemstr.append(CMLib.xml().convertXMLtoTag("IDVAL",shopKeeper().devalueRate()));
            itemstr.append(CMLib.xml().convertXMLtoTag("IGNOR",shopKeeper().ignoreMask()));
            itemstr.append(CMLib.xml().convertXMLtoTag("PRICM",CMParms.toStringList(shopKeeper().itemPricingAdjustments())));
            itemstr.append("<INVS>");
            for(int i=0;i<V.size();i++)
            {
                Environmental E=(Environmental)V.elementAt(i);
                itemstr.append("<INV>");
                itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(E)));
                itemstr.append(CMLib.xml().convertXMLtoTag("INUM",""+numberInStock(E)));
                itemstr.append(CMLib.xml().convertXMLtoTag("IVAL",""+stockPrice(E)));
                itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",CMLib.coffeeMaker().getPropertiesStr(E,true)));
                itemstr.append("</INV>");
            }
            return itemstr.toString()+"</INVS>";
        }
        return "";
    }

    public void buildShopFromXML(String text)
    {
        Vector V=new Vector();
        storeInventory=new DVector(3);
        baseInventory=new Vector();
        
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

        Vector xmlV=CMLib.xml().parseAllXML(text);
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
        
        Vector iV=CMLib.xml().getRealContentsFromPieces(xmlV,"INVS");
        if(iV==null)
        {
            Log.errOut("DefaultCoffeeShop","Error parsing 'INVS'.");
            return;
        }
        for(int i=0;i<iV.size();i++)
        {
            XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)iV.elementAt(i);
            if((!iblk.tag.equalsIgnoreCase("INV"))||(iblk.contents==null))
            {
                Log.errOut("DefaultCoffeeShop","Error parsing 'INVS' data.");
                return;
            }
            String itemi=CMLib.xml().getValFromPieces(iblk.contents,"ICLASS");
            int itemnum=CMLib.xml().getIntFromPieces(iblk.contents,"INUM");
            int val=CMLib.xml().getIntFromPieces(iblk.contents,"IVAL");
            Environmental newOne=CMClass.getItem(itemi);
            if(newOne==null) newOne=CMClass.getMOB(itemi);
            Vector idat=CMLib.xml().getRealContentsFromPieces(iblk.contents,"IDATA");
            if((idat==null)||(newOne==null)||(!(newOne instanceof Item)))
            {
                Log.errOut("DefaultCoffeeShop","Error parsing 'INV' data.");
                return;
            }
            CMLib.coffeeMaker().setPropertiesStr(newOne,idat,true);
            Environmental E=(Environmental)newOne;
            E.recoverEnvStats();
            V.addElement(E);
            addStoreInventory(E,itemnum,val);
        }
    }
}
