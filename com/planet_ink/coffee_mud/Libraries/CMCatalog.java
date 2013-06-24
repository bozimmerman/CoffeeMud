package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.RoomContent;
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
import java.lang.ref.WeakReference;
import java.util.*;
/*
   Copyright 2000-2013 Bo Zimmerman

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
	
	private TickClient serviceClient=null;
	public TickClient getServiceClient() { return serviceClient;}
	
	public DVector icatalog=new DVector(2);
	public DVector mcatalog=new DVector(2);

	public void changeCatalogFlag(Physical P, boolean truefalse)
	{
		if(P==null) return;
		if(CMath.bset(P.basePhyStats().disposition(),PhyStats.IS_CATALOGED))
		{
			if(!truefalse)
			{
				P.basePhyStats().setDisposition(CMath.unsetb(P.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
				P.phyStats().setDisposition(CMath.unsetb(P.phyStats().disposition(),PhyStats.IS_CATALOGED));
			}
		}
		else
		if(truefalse)
		{
			P.basePhyStats().setDisposition(CMath.setb(P.basePhyStats().disposition(),PhyStats.IS_CATALOGED));
			P.phyStats().setDisposition(CMath.setb(P.phyStats().disposition(),PhyStats.IS_CATALOGED));
		}
	}
	
	protected Object getCatalogObject(DVector list, String name, int dim)
	{
		synchronized(list)
		{
			try
			{
				if(list.size()==0) return null;
				int start=0;
				int end=list.size()-1;
				while(start<=end)
				{
					int mid=(end+start)/2;
					int comp=((Environmental)list.elementAt(mid,1)).Name().compareToIgnoreCase(name);
					if(comp==0)
						return list.elementAt(mid,dim);
					else
					if(comp>0)
						end=mid-1;
					else
						start=mid+1;
		
				}
			} catch(Exception e){}
			return null;
		}
	}
	
	protected void addCatalogReplace(DVector DV, Physical P)
	{
		int start=0;
		int end=DV.size()-1;
		String name=P.Name();
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
			if(P instanceof DBIdentifiable)
				((DBIdentifiable)P).setDatabaseID(((DBIdentifiable)DV.elementAt(mid,1)).databaseID());
			((Environmental)DV.elementAt(mid,1)).destroy();
			DV.setElementAt(mid,1,P);
		}
		else
		{
			if(mid>=0)
				for(comp=lastStart;comp<=lastEnd;comp++)
					if(((Environmental)DV.elementAt(comp,1)).Name().compareToIgnoreCase(name)>0)
					{
						DV.insertElementAt(comp,P,new CataDataImpl(""));
						return;
					}
			DV.addElement(P,new CataDataImpl(""));
		}
	}
	
	public String[] makeCatalogNames(Vector<Environmental> catalog)
	{
		String[] names=new String[catalog.size()];
		int x=0;
		for(Environmental E : catalog)
			names[x++]=E.Name();
		return names;
	}
	
	@SuppressWarnings("unchecked")
    public String[] getCatalogItemNames() 
	{ 
		return makeCatalogNames(icatalog.getDimensionVector(1)); 
	}
	@SuppressWarnings("unchecked")
    public String[] getCatalogMobNames() 
	{ 
		return makeCatalogNames(mcatalog.getDimensionVector(1)); 
	}
	
	@SuppressWarnings("unchecked")
    public Item[] getCatalogItems(){
		Vector<Item> itemsV=icatalog.getDimensionVector(1);
		Item[] items=new Item[itemsV.size()];
		int x=0;
		for(Iterator<Item> i=itemsV.iterator();i.hasNext();)
			items[x++]=i.next();
		return items;
	}
	@SuppressWarnings("unchecked")
    public MOB[] getCatalogMobs(){
		Vector<MOB> mobsV=mcatalog.getDimensionVector(1);
		MOB[] mobs=new MOB[mobsV.size()];
		int x=0;
		for(Iterator<MOB> i=mobsV.iterator();i.hasNext();)
			mobs[x++]=i.next();
		return mobs;
	}
	
	public boolean isCatalogObj(Environmental E)
	{
		if(E instanceof MOB) return getCatalogMob(E.Name()) != null;
		if(E instanceof Item) return getCatalogItem(E.Name()) != null;
		return false; 
	}
	
	public boolean isCatalogObj(String name)
	{
		Object o=getCatalogMob(name);
		if(o==null) o=getCatalogItem(name);
		return o!=null;
	}
	
	public Item getCatalogItem(String called) { return (Item)getCatalogObject(icatalog,called,1); }
	
	public MOB getCatalogMob(String called) { return (MOB)getCatalogObject(mcatalog,called,1); }
	
	public CataData getCatalogItemData(String called) { return (CataData)getCatalogObject(icatalog,called,2); }
	
	public CataData getCatalogMobData(String called) { return (CataData)getCatalogObject(mcatalog,called,2); }
	
	public Vector<RoomContent> roomContent(Room R) 
	{
		Item I=null;
		MOB M=null;
		Environmental E=null;
		ShopKeeper SK=null;
		List<Environmental> shops=null;
		Environmental shopItem=null;
		Vector<RoomContent> content =new Vector<RoomContent>();
		if(R!=null)
		{
			shops=CMLib.coffeeShops().getAllShopkeepers(R,null);
			for(int s=0;s<shops.size();s++)
			{
				E=shops.get(s);
				if(E==null) continue;
				SK=CMLib.coffeeShops().getShopKeeper(E);
				if(SK==null) continue;
				for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					shopItem=i.next();
					if(shopItem instanceof Physical)
						content.addElement(new RoomContentImpl((Physical)shopItem,SK));
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				I=R.getItem(i);
				if(I!=null) content.addElement(new RoomContentImpl(I));
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if(M==null) continue;
				for(int i=0;i<M.numItems();i++)
				{
					I=M.getItem(i);
					if(I!=null) content.addElement(new RoomContentImpl(I,M));
				}
				content.addElement(new RoomContentImpl(M));
			}
		}
		return content;
	}

	public void updateRoomContent(String roomID, Vector<RoomContent> content)
	{
		Vector<Environmental> updatables=new Vector<Environmental>();
		Vector<Environmental> deletables=new Vector<Environmental>();
		for(RoomContent C : content)
		{
			if(C.deleted())
			{
				if(C.holder()!=null)
				{
					if(!updatables.contains(C.holder()))
						updatables.addElement(C.holder());
				}
				else
				if(!updatables.contains(C.P()))
					deletables.add(C.P());
			}
			else
			if(C.isDirty())
			{
				if(C.holder()!=null)
				{
					if(!updatables.contains(C.holder()))
						updatables.addElement(C.holder());
				}
				else
				if(!updatables.contains(C.P()))
					updatables.add(C.P());
			}
		}
		for(Environmental E : deletables)
		{
			updatables.remove(E);
			if((!(E instanceof DBIdentifiable))
			||(!((DBIdentifiable)E).canSaveDatabaseID())
			||(((DBIdentifiable)E).databaseID().trim().length()==0))
				continue;
			if(E instanceof MOB)
				CMLib.database().DBDeleteMOB(roomID,(MOB)E);
			else
				CMLib.database().DBDeleteItem(roomID,(Item)E);
		}
		for(Environmental E : updatables)
		{
			if(E instanceof ShopKeeper)
			{
				ShopKeeper SK=(ShopKeeper)E;
				Vector<Environmental> newShop=new Vector<Environmental>();
				for(RoomContent C : content)
				{
					if((C.holder()==SK)&&(!C.deleted()))
						newShop.addElement(C.P());
				}
				SK.getShop().resubmitInventory(newShop);
			}
		}
		for(Environmental E : updatables)
		{
			if((!(E instanceof DBIdentifiable))
			||(!((DBIdentifiable)E).canSaveDatabaseID())
			||(((DBIdentifiable)E).databaseID().trim().length()==0))
				continue;
			if(E instanceof MOB)
				CMLib.database().DBUpdateMOB(roomID,(MOB)E);
			else
				CMLib.database().DBUpdateItem(roomID,(Item)E);
		}
	}
	
	public void addCatalog(Physical PA)
	{
		if((PA==null)||(!(PA instanceof DBIdentifiable))||(!((DBIdentifiable)PA).canSaveDatabaseID())) 
			return;
		synchronized(getSync(PA).intern())
		{
			changeCatalogFlag(PA,true);
			Physical origP=PA;
			PA=(Physical)origP.copyOf();
			submitToCatalog(PA);
			if(PA instanceof Item)
				CMLib.database().DBCreateThisItem("CATALOG_ITEMS",(Item)PA);
			else
			if(PA instanceof MOB)
				CMLib.database().DBCreateThisMOB("CATALOG_MOBS",(MOB)PA);
			CataData data=getCatalogData(PA);
			if(data!=null) data.addReference(origP);
		}
	}
	
	public void submitToCatalog(Physical P)
	{
		if(P==null) return;
		synchronized(getSync(P).intern())
		{
			if(getCatalogObj(P)!=null) return;
			changeCatalogFlag(P,false);
			P.text(); // to get cataloged status into xml
			if(P instanceof Item)
			{
				synchronized(icatalog)
				{
					addCatalogReplace(icatalog,P);
				}
			}
			else
			if(P instanceof MOB)
			{
				synchronized(mcatalog)
				{
					addCatalogReplace(mcatalog,P);
				}
			}
		}
	}
	
	public void delCatalog(Physical P)
	{
		if(P==null) return;
		P=getCatalogObj(P);
		if(P==null) return;
		CataData data=getCatalogData(P);
		if(P instanceof Item)
		{
			synchronized(icatalog)
			{
				icatalog.removeElement(P);
			}
			CMLib.database().DBDeleteItem("CATALOG_ITEMS",(Item)P);
		}
		else
		if(P instanceof MOB)
		{
			synchronized(mcatalog)
			{
				mcatalog.removeElement(P);
			}
			CMLib.database().DBDeleteMOB("CATALOG_MOBS",(MOB)P);
		}
		if(data!=null)
		{
			Vector<Room> rooms=new Vector<Room>();
			for(Enumeration<Physical> e=data.enumeration();e.hasMoreElements();)
			{
				Physical P2=e.nextElement();
				Room R=CMLib.map().getStartRoom(P2);
				if((R!=null)&&(!rooms.contains(R)))
					rooms.addElement(R);
				changeCatalogUsage(P2,false);
			}
			for(Room R : rooms)
			{
				Vector<RoomContent> contents=roomContent(R);
				for(RoomContent content : contents)
				{
					if((CMLib.flags().isCataloged(content.P()))&&(content.P().Name().equalsIgnoreCase(P.Name())))
					{
						if(((P instanceof MOB)&&(content.P() instanceof MOB))
						||((P instanceof Item)&&(content.P() instanceof Item)))
							changeCatalogFlag(content.P(),false);
					}
				}
				R=CMLib.coffeeMaker().makeNewRoomContent(R,false);
				contents=roomContent(R);
				boolean dirty=false;
				for(RoomContent content : contents)
				{
					if((CMLib.flags().isCataloged(content.P()))&&(content.P().Name().equalsIgnoreCase(P.Name())))
					{
						if(((P instanceof MOB)&&(content.P() instanceof MOB))
						||((P instanceof Item)&&(content.P() instanceof Item)))
						{
							changeCatalogFlag(content.P(),false);
							content.flagDirty();
							dirty=true;
						}
					}
				}
				if(dirty) updateRoomContent(R.roomID(),contents);
				R.destroy();
			}
		}
	}
	
	public void updateCatalog(Physical modelP)
	{
		if((modelP==null)
		||(!(modelP instanceof DBIdentifiable))
		||(!((DBIdentifiable)modelP).canSaveDatabaseID())) 
			return;
		synchronized(getSync(modelP).intern())
		{
			changeCatalogFlag(modelP,false);
			Physical cataP=(Physical)modelP.copyOf();
			cataP.text(); // to get cataloged status into xml
			if(modelP!=getCatalogObj(modelP))
				changeCatalogFlag(modelP,true);
			if(cataP instanceof Item)
			{
				synchronized(icatalog)
				{
					addCatalogReplace(icatalog,cataP);
				}
			}
			else
			if(cataP instanceof MOB)
			{
				synchronized(mcatalog)
				{
					addCatalogReplace(mcatalog,cataP);
				}
			}
			cataP=getCatalogObj(cataP);
			if(cataP instanceof MOB)
				CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)cataP);
			else
				CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)cataP);
			
			CataData data = getCatalogData(cataP);
			if(data!=null)
				data.delReference(cataP);
			SHashSet<Physical> ignored=null;
			if(data!=null)
				ignored=new SHashSet<Physical>(data.enumeration());
			else
				ignored=new SHashSet<Physical>(1);
			Physical P;
			for(Iterator<Physical> i=ignored.iterator();i.hasNext();)
			{
				P=i.next();
				if((!P.amDestroyed())
				&&(CMLib.flags().isCataloged(P))
				&&(cataP!=P)
				&&(P.Name().equalsIgnoreCase(cataP.Name())))
				{
					P.setMiscText(P.text());
					changeCatalogFlag(P,true);
				}
			}
			Vector<Environmental> all=new Vector<Environmental>();
			String srchStr="$"+cataP.Name()+"$";
			boolean isMob=(cataP instanceof MOB);
			if(cataP instanceof MOB)
			{
				all.addAll(CMLib.map().findInhabitants(CMLib.map().rooms(),null, srchStr, 50));
				all.addAll(CMLib.map().findShopStock(CMLib.map().rooms(),null, srchStr, 50));
			}
			else
			{
				all.addAll(CMLib.map().findRoomItems(CMLib.map().rooms(),null, srchStr, true, 50));
				all.addAll(CMLib.map().findInventory(CMLib.map().rooms(),null, srchStr, 50));
				all.addAll(CMLib.map().findInventory(null,null, srchStr, 50));
				all.addAll(CMLib.map().findShopStockers(CMLib.map().rooms(),null, srchStr, 50));
				all.addAll(CMLib.map().findShopStockers(null,null, srchStr, 50));
			}
			HashSet<ShopKeeper> doneShops=new HashSet<ShopKeeper>();
			ShopKeeper SK=null;
			for(Enumeration<Environmental> e=all.elements();e.hasMoreElements();)
			{
				P=(Physical)e.nextElement();
				if((CMLib.flags().isCataloged(P))
				&&(!ignored.contains(P))
				&&(((isMob)&&(P instanceof MOB))||((!isMob)&&(P instanceof Item)))
				&&(cataP.Name().equalsIgnoreCase(P.Name())))
				{
					ignored.add(P);
					P.setMiscText(P.text());
					changeCatalogFlag(P,true);
				}
				SK=CMLib.coffeeShops().getShopKeeper(P);
				if((SK!=null)&&(!doneShops.contains(SK))) {
					doneShops.add(SK);
					propogateShopChange(SK,ignored,cataP);
				}
			}
		}
	}
	
	public void newInstance(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			PhyStats stats=P.basePhyStats();
			if((stats!=null)&&(CMath.bset(stats.disposition(),PhyStats.IS_CATALOGED)))
			{
				CataData data=getCatalogData(P);
				if(data!=null) data.addReference(P);
			}
		}
	}
	
	public void bumpDeathPickup(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			PhyStats stats=P.basePhyStats();
			if((stats!=null)&&(CMath.bset(stats.disposition(),PhyStats.IS_CATALOGED)))
			{
				CataData data=getCatalogData(P);
				if(data!=null) data.bumpDeathPickup();
			}
		}
	}
	
	public void changeCatalogUsage(Physical P, boolean toCataloged)
	{
		synchronized(getSync(P).intern())
		{
			if((P!=null)&&(P.basePhyStats()!=null)&&(!P.amDestroyed()))
			{
				if(toCataloged)
				{
					changeCatalogFlag(P,true);
					CataData data=getCatalogData(P);
					if(data!=null) data.addReference(P);
				}
				else
				if(CMLib.flags().isCataloged(P))
				{
					changeCatalogFlag(P,false);
					CataData data=getCatalogData(P);
					if(data!=null) data.delReference(P);
				}
			}
		}
	}
	
	protected void propogateShopChange(ShopKeeper SK, Set<Physical> ignored, Physical cataP)
	{
		boolean isMob=(cataP instanceof MOB);
		Environmental E=null;
		boolean changes=false;
		Vector<Environmental> newShop=new Vector<Environmental>();
		for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
		{
			E=i.next();
			if(!ignored.contains(E) && (E instanceof Physical))
			{
				ignored.add((Physical)E);
				if((isMob)&&(E instanceof MOB)
				&&(CMLib.flags().isCataloged(E))
				&&(cataP.Name().equalsIgnoreCase(E.Name())))
				{ E.setMiscText(E.text()); changes=true;}
				if((!isMob)&&(E instanceof Item)
				&&(CMLib.flags().isCataloged(E))
				&&(cataP.Name().equalsIgnoreCase(E.Name())))
				{ E.setMiscText(E.text()); changes=true;}
			}
			newShop.add(E);
		}
		if(changes)
			SK.getShop().resubmitInventory(newShop);
	}
	
	public CataData getCatalogData(Physical P) {
		if(P==null) return null;
		return (P instanceof MOB)?getCatalogMobData(P.Name()):getCatalogItemData(P.Name());
	}
	
	public Physical getCatalogObj(Physical P) {
		if(P==null) return null;
		return (P instanceof MOB)?getCatalogMob(P.Name()):getCatalogItem(P.Name());
	}
	
	public void updateCatalogIntegrity(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			if(checkCatalogIntegrity(P)==null) return;
			changeCatalogFlag(P,false);
			P.text();
		}
	}

	private final String getSync(String name, boolean mobType) { return ((mobType)?"CATASYNC_MOB_":"CATASYNC_ITEM_")+name.toUpperCase();}
	private final String getSync(Environmental E) { return getSync(E.Name(),E instanceof MOB);}
	
	public StringBuffer checkCatalogIntegrity(Physical P) 
	{
		if(P==null) return null;
		synchronized(getSync(P).intern())
		{
			if(CMLib.flags().isCataloged(P))
			{
				CataData data=getCatalogData(P);
				Physical cataE=getCatalogObj(P);
				if((cataE==null)||(data==null))
				{
					if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) 
						return null; // if catalog isn't fully loaded, this can be a false correction
					if(data!=null)
						data.delReference(P);
					changeCatalogFlag(P,false);
					P.text();
					return null;
				} 
				else 
				{
					StringBuffer diffs=null;
					changeCatalogFlag(P,false);
					if(!cataE.sameAs(P))
					{
						diffs=new StringBuffer("");
						for(int i=0;i<cataE.getStatCodes().length;i++)
							if((!cataE.getStat(cataE.getStatCodes()[i]).equals(P.getStat(cataE.getStatCodes()[i]))))
								diffs.append(cataE.getStatCodes()[i]+",");
					}
					if((P instanceof MOB)&&(cataE instanceof MOB))
					{
						boolean firstChecked=false;
						if(diffs==null) diffs=new StringBuffer("");
						for(Pair<Clan,Integer> p : ((MOB)P).clans())
							if((((MOB)cataE).getClanRole(p.first.clanID())==null)||(((MOB)cataE).getClanRole(p.first.clanID()).second.intValue()!=p.second.intValue()))
								firstChecked=true;
						for(Pair<Clan,Integer> p : ((MOB)cataE).clans())
							if((((MOB)P).getClanRole(p.first.clanID())==null)||(((MOB)P).getClanRole(p.first.clanID()).second.intValue()!=p.second.intValue()))
								firstChecked=true;
						if(firstChecked)
							diffs.append("CLANID,");
					}
					changeCatalogFlag(P,true);
					data.addReference(P);
					return diffs;
				}
			}
			else
			{
				CataData data=getCatalogData(P);
				if(data!=null)
					data.delReference(P);
				return null;
			}
		}
	}
	
	public Item getDropItem(MOB M, boolean live)
	{
		if(M==null) return null;
		CatalogLibrary.CataData data=null;
		Vector<Item> selections=null;
		synchronized(icatalog)
		{
			try
			{
				for(int d=0;d<icatalog.size();d++)
				{
					data=(CatalogLibrary.CataData)icatalog.elementAt(d,2);
					if((data.getWhenLive()==live)
					&&(data.getRate()>0.0)
					&&(data.getMaskV()!= null)
					&&(Math.random() <= data.getRate())
					&&(CMLib.masking().maskCheck(data.getMaskV(),M,true)))
					{
						if(selections==null)
							selections=new Vector<Item>();
						selections.addElement((Item)icatalog.elementAt(d,1));
					}
				}
			} catch(IndexOutOfBoundsException e) {}
		}
		if(selections==null) return null;
		Item I=selections.elementAt(CMLib.dice().roll(1,selections.size(),-1));
		I=(Item)I.copyOf();
		changeCatalogUsage(I,true);
		return I;
	}
	
	public CataData sampleCataData(String xml) {return new CataDataImpl(xml);}
	
	public boolean activate() 
	{
		if(serviceClient==null)
			serviceClient=CMLib.threads().startTickDown(new Tickable(){
				private long tickStatus=Tickable.STATUS_NOT;
				@Override public String ID() { return "THCatalog"+Thread.currentThread().getThreadGroup().getName().charAt(0); }
				@Override public CMObject newInstance() { return this; }
				@Override public CMObject copyOf() { return this; }
				@Override public void initializeClass() { }
				@Override public int compareTo(CMObject o) { return (o==this)?0:1; }
				@Override public String name() { return ID(); }
				@Override public long getTickStatus() { return tickStatus; }
				@Override public boolean tick(Tickable ticking, int tickID) {
					if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CATALOGTHREAD))
					{
						tickStatus=Tickable.STATUS_ALIVE;
						isDebugging=CMSecurity.isDebugging(DbgFlag.CATALOGTHREAD);
						setThreadStatus(serviceClient,"checking catalog references.");
						String[] names = getCatalogItemNames();
						for(int n=0;n<names.length;n++)
						{
							CataData data=getCatalogItemData(names[n]);
							data.cleanHouse();
						}
						names = getCatalogMobNames();
						for(int n=0;n<names.length;n++)
						{
							CataData data=getCatalogMobData(names[n]);
							data.cleanHouse();
						}
						setThreadStatus(serviceClient,"sleeping");
					}
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		return true;
	}
	
	public boolean shutdown()
	{
		icatalog=new DVector(2);
		mcatalog=new DVector(2);
		if((serviceClient!=null)&&(serviceClient.getClientObject()!=null))
		{
			CMLib.threads().deleteTick(serviceClient.getClientObject(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
	
	public void forceTick()
	{
		serviceClient.tickTicker(true);
	}

	public static class RoomContentImpl implements RoomContent
	{
		private Physical 		obj=null;
		private boolean 		dirty=false;
		private Environmental	holder=null;
		
		public RoomContentImpl(Physical P){ obj=P;}
		public RoomContentImpl(Physical P, Environmental E){ obj=P; holder=E;}
		public Physical P(){return obj;}
		public void flagDirty(){ dirty=true;}
		public Environmental holder(){ return holder;}
		public boolean isDirty(){ return dirty;}
		public boolean deleted(){ return obj.amDestroyed();}
	}
	
	public static class CataDataImpl implements CataData 
	{
		public String 		lmaskStr=null;
		public boolean 		live=false;
		public double 		rate=0.0;
		public volatile int deathPickup=0;
		public SVector<WeakReference<Physical>> 
							refs=new SVector<WeakReference<Physical>>(1);
		public boolean noRefs = CMProps.getBoolVar(CMProps.SYSTEMB_CATALOGNOCACHE) 
								|| CMSecurity.isDisabled(CMSecurity.DisFlag.CATALOGCACHE);
		public MaskingLibrary.CompiledZapperMask 		lmaskV=null;
		
		public CataDataImpl(String catadata)
		{
			build(catadata);
		}
		
		private Vector<Physical> makeVector() {
			Vector<Physical> V=new Vector<Physical>(refs.size());
			WeakReference<Physical> R=null;
			for(int r=0;r<refs.size();r++)
			{
				R=refs.elementAt(r);
				if(R!=null)
				{
					Physical o=R.get();
					if((o!=null)
					&&(!o.amDestroyed())
					&&(CMath.bset(o.basePhyStats().disposition(),PhyStats.IS_CATALOGED)))
						V.addElement(o);
				}
			}
			return V;
		}

		public RoomnumberSet getLocations() {
			Vector<Physical> items=makeVector();
			RoomnumberSet homes=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			Room R=null;
			for(Environmental E : items)
			{
				R=CMLib.map().getStartRoom(E);
				if(R==null) R=CMLib.map().roomLocation(E);
				if(R==null) continue;
				if((E instanceof Item)
				&&(((Item)E).owner() instanceof MOB)
				&&(!((MOB)((Item)E).owner()).isMonster()))
					continue;
				if(CMLib.law().getLandTitle(R)!=null)
					homes.add(CMLib.map().getExtendedRoomID(R));
				else
					set.add(CMLib.map().getExtendedRoomID(R));
			}
			if(set.roomCountAllAreas()>0) return set;
			return homes;
		}
		
		public String randomRoom() {
			RoomnumberSet set=getLocations();
			if(set.roomCountAllAreas()==0)
				return "";
			return set.random();
		}
		
		public String mostPopularArea() 
		{
			RoomnumberSet set=getLocations();
			Iterator<String> e=set.getAreaNames();
			if(!e.hasNext()) return "";
			String maxArea=e.next();
			int maxCount=set.roomCount(maxArea);
			for(;e.hasNext();) 
			{
				String area=e.next();
				int count=set.roomCount(area);
				if(count>maxCount)
				{
					maxCount=count;
					maxArea=area;
				}
			}
			Area A=CMLib.map().getArea(maxArea);
			if(A!=null) return A.Name();
			return maxArea;
		}
		
		public int numReferences() {
			int num=0;
			for(int r=0;r<refs.size();r++)
				if(refs.elementAt(r).get()!=null)
					num++;
			return num;
		}
		
		public Enumeration<Physical> enumeration() { return makeVector().elements();}
		
		public int getDeathsPicksups(){ return deathPickup;}
		public void bumpDeathPickup(){ deathPickup++;}
		
		public synchronized void cleanHouse()
		{
			if(noRefs)
			{
				refs.clear();
				return;
			}
			Environmental o=null;
			try {
				for(int r=refs.size()-1;r>=0;r--)
				{
					o=refs.elementAt(r).get();
					if((o==null)||o.amDestroyed()||(!CMLib.flags().isCataloged(o)))
						refs.removeElementAt(r);
				}
			} catch(ArrayIndexOutOfBoundsException ex){}
		}
		
		public Physical getLiveReference()
		{
			if(noRefs) return null;
			Physical o=null;
			try {
				for(int r=0;r<refs.size();r++)
				{
					o=refs.elementAt(r).get();
					if((o!=null)&&(CMLib.flags().isInTheGame(o,true)))
						return o;
				}
			} catch(Exception e) { }
			return null;
		}
		public synchronized void addReference(Physical P) 
		{
			if(noRefs) return;
			if(isReference(P)) return;
			Environmental o=null;
			for(int r=0;r<refs.size();r++)
			{
				o=refs.elementAt(r).get();
				if(o==null)
				{
					refs.setElementAt(new WeakReference<Physical>(P),r);
					return;
				}
			}
			refs.addElement(new WeakReference<Physical>(P));
		}
		
		public boolean isReference(Physical P) {
			for(int r=0;r<refs.size();r++)
				if(refs.elementAt(r).get()==P) return true;
			return false;
		}
		
		public synchronized void delReference(Physical P) {
			if(!isReference(P)) return;
			Environmental o=null;
			for(int r=0;r<refs.size();r++)
			{
				o=refs.elementAt(r).get();
				if(o==P)
				{
					refs.removeElementAt(r);
					return;
				}
			}
		}
		
		public CataDataImpl(String _lmask, String _rate, boolean _live)
		{
			this(_lmask,CMath.s_pct(_rate),_live);
		}
		
		public CataDataImpl(String _lmask, double _rate, boolean _live)
		{
			live=_live;
			lmaskStr=_lmask;
			lmaskV=null;
			if(lmaskStr.length()>0)
				lmaskV=CMLib.masking().maskCompile(lmaskStr);
			rate=_rate;
		}
		
		public MaskingLibrary.CompiledZapperMask getMaskV(){return lmaskV;}
		public String getMaskStr(){return lmaskStr;}
		public boolean getWhenLive(){return live;}
		public double getRate(){return rate;}
		public void setMaskStr(String s){
			lmaskStr=s;
			if(s.trim().length()==0)
				lmaskV=null;
			else
				lmaskV=CMLib.masking().maskCompile(s);
		}
		public void setWhenLive(boolean l){live=l;}
		public void setRate(double r){rate=r;}
		
		public String data() 
		{
			StringBuffer buf=new StringBuffer("");
			buf.append("<CATALOGDATA>");
			buf.append("<RATE>"+CMath.toPct(rate)+"</RATE>");
			buf.append("<LMASK>"+CMLib.xml().parseOutAngleBrackets(lmaskStr)+"</LMASK>");
			buf.append("<LIVE>"+live+"</LIVE>");
			buf.append("</CATALOGDATA>");
			return buf.toString();
		}
		
		public void build(String catadata)
		{
			List<XMLLibrary.XMLpiece> V=null;
			if((catadata!=null)&&(catadata.length()>0))
			{
				V=CMLib.xml().parseAllXML(catadata);
				XMLLibrary.XMLpiece piece=CMLib.xml().getPieceFromPieces(V,"CATALOGDATA");
				if((piece!=null)&&(piece.contents!=null)&&(piece.contents.size()>0))
				{
					lmaskStr=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(piece.contents,"LMASK"));
					String ratestr=CMLib.xml().getValFromPieces(piece.contents,"RATE");
					rate=CMath.s_pct(ratestr);
					lmaskV=null;
					if(lmaskStr.length()>0)
						lmaskV=CMLib.masking().maskCompile(lmaskStr);
					live=CMath.s_bool(CMLib.xml().getValFromPieces(piece.contents,"LIVE"));
				}
			}
			else
			{
				lmaskV=null;
				lmaskStr="";
				live=false;
				rate=0.0;
			}
		}
	}
}
