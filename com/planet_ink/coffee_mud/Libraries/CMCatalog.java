package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class CMCatalog extends StdLibrary implements CatalogLibrary
{
    public String ID(){return "CMCatalog";}
    public DVector icatalog=new DVector(3);
    public DVector mcatalog=new DVector(3);

    protected int getGlobalIndex(Vector list, String name)
    {
        if(list.size()==0) return -1;
        int start=0;
        int end=list.size()-1;
        while(start<=end)
        {
            int mid=(end+start)/2;
            int comp=((Environmental)list.elementAt(mid)).Name().compareToIgnoreCase(name);
            if(comp==0)
                return mid;
            else
            if(comp>0)
                end=mid-1;
            else
                start=mid+1;

        }
        return -1;
    }
    
    public void addCatalogReplace(DVector DV, Environmental E)
    {
        int start=0;
        int end=DV.size()-1;
        String name=E.Name();
        int lastStart=0;
        int lastEnd=DV.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=((Environmental)DV.elementAt(mid,1)).Name().compareToIgnoreCase(name);
            if(comp==0)
                break;
            else
            if(comp>0)
            {
                lastEnd=end;
                end=mid-1;
            }
            else
            {
                lastStart=start;
                start=mid+1;
            }
        }
        if(comp==0)
        {
            ((Environmental)DV.elementAt(mid,1)).destroy();
            DV.setElementAt(mid,1,E);
        }
        else
        {
            if(mid>=0)
                for(comp=lastStart;comp<=lastEnd;comp++)
                    if(((Environmental)DV.elementAt(comp,1)).Name().compareToIgnoreCase(name)>0)
                    {
                        DV.insertElementAt(comp,E,new int[]{0},new CataData(""));
                        return;
                    }
            DV.addElement(E,new int[]{0},new CataData(""));
        }
        
    }
    
    public DVector getCatalogItems(){return icatalog;}
    public DVector getCatalogMobs(){return mcatalog;}
    
    public boolean isCatalogObj(Environmental E)
    {
        if(E instanceof MOB) return mcatalog.contains(E);
        if(E instanceof Item) return icatalog.contains(E);
        return false;
    }
    
    public boolean isCatalogObj(String name)
    {
        int index=getCatalogMobIndex(name);
        if(index<0) index=getCatalogItemIndex(name);
        return index>=0;
    }
    
    public int getCatalogItemIndex(String called)
    { 
        return getGlobalIndex(icatalog.getDimensionVector(1),called);
    }
    
    public int getCatalogMobIndex(String called)
    { 
        return getGlobalIndex(mcatalog.getDimensionVector(1),called);
    }
    
    public Item getCatalogItem(int index)
    { 
        try
        {
            return (Item)icatalog.elementAt(index,1);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    public MOB getCatalogMob(int index)
    { 
        try
        {
            return (MOB)mcatalog.elementAt(index,1);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public int[] getCatalogItemUsage(int index)
    { 
        try
        {
            return (int[])icatalog.elementAt(index,2);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public int[] getCatalogMobUsage(int index)
    { 
        try
        {
            return (int[])mcatalog.elementAt(index,2);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public CataData getCatalogItemData(int index)
    { 
        try
        {
            return (CataData)icatalog.elementAt(index,3);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public CataData getCatalogMobData(int index)
    { 
        try
        {
            return (CataData)mcatalog.elementAt(index,3);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public void delCatalog(Environmental E)
    {
        if(E==null) return;
        if(E instanceof Item)
            icatalog.removeElement((Item)E);
        else
        if(E instanceof MOB)
            mcatalog.removeElement((MOB)E);
    }
    
    public void addCatalogReplace(Environmental E)
    {
        if(E==null) return;
        if(E instanceof Item)
            addCatalogReplace(icatalog,(Item)E);
        else
        if(E instanceof MOB)
            addCatalogReplace(mcatalog,(MOB)E);
    }
    
    public void addCatalog(Environmental E)
    {
        if(E==null) return;
        if(E instanceof Item)
        {
            int oldIndex=getCatalogItemIndex(E.Name());
            Environmental E2=null;
            if(oldIndex>=0) E2=getCatalogItem(oldIndex);
            if(E2!=null) delCatalog((Item)E2);
        }
        else
        {
            int oldIndex=getCatalogMobIndex(E.Name());
            Environmental E2=null;
            if(oldIndex>=0) E2=getCatalogMob(oldIndex);
            if(E2!=null) delCatalog((Item)E2);
        }
        addCatalogReplace(E);
    }
    
    public boolean shutdown()
    {
        icatalog=new DVector(2);
        mcatalog=new DVector(2);
        return true;
    }
    
    public void changeCatalogUsage(Environmental E, boolean add)
    {
        try {
            if((E.baseEnvStats()!=null)&&(!E.amDestroyed()))
            {
                boolean iscataloged=CMLib.flags().isCataloged(E);
                if((!add)&&(iscataloged))
                {
                    CMLib.flags().setCataloged(E,false);
                    int index=getCatalogItemIndex(E.Name());
                    if(index>=0) getCatalogItemUsage(index)[0]--;
                }
                else
                if((add)&&(!iscataloged))
                {
                    CMLib.flags().setCataloged(E,true);
                    int index=getCatalogItemIndex(E.Name());
                    if(index>=0) getCatalogItemUsage(index)[0]++;
                }
            }
        } catch(Throwable t){}
    }
    
    private void propogateShopChange(ShopKeeper SK, Environmental thang)
    {
        boolean isMob=(thang instanceof MOB);
        Environmental E=null;
        int i=0;
        Vector V=SK.getShop().getStoreInventory();
        for(i=0;i<V.size();i++)
        {
            E=(Environmental)V.elementAt(i);
            if((isMob)&&(E instanceof MOB)
            &&(CMLib.flags().isCataloged(E))
            &&(thang.Name().equalsIgnoreCase(E.Name())))
                E.setMiscText(E.text());
            if((!isMob)&&(E instanceof Item)
            &&(CMLib.flags().isCataloged(E))
            &&(thang.Name().equalsIgnoreCase(E.Name())))
                E.setMiscText(E.text());
        }
    }
    
    public void propogateCatalogChange(Environmental thang)
    {
        boolean isMob=(thang instanceof MOB);
        MOB M=null;
        Room R=null;
        Item I=null;
        ShopKeeper SK=null;
        int m=0,i=0;
        for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
        {
            Area A=(Area)e.nextElement();
            SK=CMLib.coffeeShops().getShopKeeper(A);
            if(SK!=null) propogateShopChange(SK,thang);
        }
        for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
        {
            R=(Room)e.nextElement();
            if(!isMob)
            for(i=0;i<R.numItems();i++)
            {
                I=R.fetchItem(i);
                if((CMLib.flags().isCataloged(I))
                &&(thang.Name().equalsIgnoreCase(I.Name())))
                    I.setMiscText(I.text());
            }
            for(m=0;m<R.numInhabitants();m++)
            {
                M=R.fetchInhabitant(m);
                if(!M.isMonster()) continue;
                if((isMob)
                &&(CMLib.flags().isCataloged(M))
                &&(thang.Name().equalsIgnoreCase(M.Name())))
                    M.setMiscText(M.text());
                if(!isMob)
                {
                    for(i=0;i<M.inventorySize();i++)
                    {
                        I=M.fetchInventory(i);
                        if((CMath.bset(I.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
                        &&(thang.Name().equalsIgnoreCase(I.Name())))
                            I.setMiscText(I.text());
                    }
                    SK=CMLib.coffeeShops().getShopKeeper(M);
                    if(SK!=null) propogateShopChange(SK,thang);
                }
            }
            SK=CMLib.coffeeShops().getShopKeeper(R);
            if(SK!=null) propogateShopChange(SK,thang);
        }
        if(!isMob)
        for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
        {
            M=(MOB)e.nextElement();
            for(i=0;i<M.inventorySize();i++)
            {
                I=M.fetchInventory(i);
                if((CMath.bset(I.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
                &&(thang.Name().equalsIgnoreCase(I.Name())))
                {
                    I.setMiscText(I.text());
                    if(M.playerStats()!=null)
                        M.playerStats().setLastUpdated(0);
                }
            }
            SK=CMLib.coffeeShops().getShopKeeper(M);
            if(SK!=null) propogateShopChange(SK,thang);
        }
    }
    
    public Item getDropItem(MOB M, boolean live)
    {
        if(M==null) return null;
        CatalogLibrary.CataData data=null;
        Vector selections=null;
        for(int d=0;d<icatalog.size();d++)
        {
            data=(CatalogLibrary.CataData)icatalog.elementAt(d,3);
            if((data.live==live)
            &&(data.rate>0.0)
            &&(data.lmaskV != null)
            &&(Math.random() <= data.rate)
            &&(CMLib.masking().maskCheck(data.lmaskV,M,true)))
            {
                if(selections==null)
                    selections=new Vector();
                selections.addElement(icatalog.elementAt(d,1));
            }
        }
        if(selections==null) return null;
        Item I=(Item)selections.elementAt(CMLib.dice().roll(1,selections.size(),-1));
        I=(Item)I.copyOf();
        CMLib.flags().setCataloged(I,true);
        return I;
    }
}
