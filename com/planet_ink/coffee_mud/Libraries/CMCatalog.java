package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.RoomContent;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
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
   Copyright 2008-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CMCatalog";
	}

	public DVector					icatalog				= new DVector(2);
	public DVector					mcatalog				= new DVector(2);
	public volatile CMFile.CMVFSDir	catalogFileMobsRoot		= null;
	public volatile CMFile.CMVFSDir	catalogFileItemsRoot	= null;

	public void changeCatalogFlag(Physical P, boolean truefalse)
	{
		if(P==null)
			return;
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
				if(list.size()==0)
					return null;
				int start=0;
				int end=list.size()-1;
				while(start<=end)
				{
					final int mid=(end+start)/2;
					final int comp=((Environmental)list.elementAt(mid,1)).Name().compareToIgnoreCase(name);
					if(comp==0)
						return list.elementAt(mid,dim);
					else
					if(comp>0)
						end=mid-1;
					else
						start=mid+1;

				}
			}
			catch(final Exception e)
			{
			}
			return null;
		}
	}

	protected void addCatalogReplace(DVector DV, String category, Physical P)
	{
		int start=0;
		int end=DV.size()-1;
		final String name=P.Name();
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
			if((P instanceof DBIdentifiable)
			&&((DBIdentifiable)DV.elementAt(mid,1)).databaseID().length()>0)
				((DBIdentifiable)P).setDatabaseID(((DBIdentifiable)DV.elementAt(mid,1)).databaseID());
			((Environmental)DV.elementAt(mid,1)).destroy();
			DV.setElementAt(mid,1,P);
		}
		else
		{
			final CataData data=new CataDataImpl("");
			if(category!=null)
				data.setCategory(category);
			if(mid>=0)
			{
				for(comp=lastStart;comp<=lastEnd;comp++)
				{
					if(((Environmental)DV.elementAt(comp,1)).Name().compareToIgnoreCase(name)>0)
					{
						DV.insertElementAt(comp,P,data);
						return;
					}
				}
			}
			DV.addElement(P,data);
		}
	}

	public String[] makeCatalogNames(String catName, DVector catalog)
	{
		final List<String> nameList=new ArrayList<String>(catalog.size());
		for(int x=0;x<catalog.size();x++)
		{
			if((catName==null)||(catName.equals(((CataData)catalog.elementAt(x, 2)).category())))
				nameList.add(((Environmental)catalog.elementAt(x, 1)).Name());
		}
		return nameList.toArray(new String[0]);
	}

	public String[] makeCatalogCatagories(DVector catalog)
	{
		final List<String> catalogList=new SortedListWrap<String>(new ArrayList<String>(2));
		for(int x=0;x<catalog.size();x++)
		{
			if(!catalogList.contains(((CataData)catalog.elementAt(x, 2)).category()))
				catalogList.add(((CataData)catalog.elementAt(x, 2)).category());
		}
		return catalogList.toArray(new String[catalogList.size()]);
	}

	@Override
	public String[] getCatalogItemNames()
	{
		return makeCatalogNames(null, icatalog);
	}

	@Override
	public String[] getCatalogItemNames(String cataName)
	{
		return makeCatalogNames(cataName, icatalog);
	}

	@Override
	public String[] getCatalogMobNames()
	{
		return makeCatalogNames(null, mcatalog);
	}

	@Override
	public String[] getCatalogMobNames(String cataName)
	{
		return makeCatalogNames(cataName, mcatalog);
	}

	@Override
	public String[] getMobCatalogCatagories()
	{
		return makeCatalogCatagories(mcatalog);
	}

	@Override
	public String[] getItemCatalogCatagories()
	{
		return makeCatalogCatagories(icatalog);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Item[] getCatalogItems()
	{
		final List<Item> itemsV=(List)icatalog.getDimensionList(1);
		final Item[] items=new Item[itemsV.size()];
		int x=0;
		for (final Item item : itemsV)
			items[x++]=item;
		return items;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MOB[] getCatalogMobs()
	{
		final List<MOB> mobsV=(List)mcatalog.getDimensionList(1);
		final MOB[] mobs=new MOB[mobsV.size()];
		int x=0;
		for (final MOB mob : mobsV)
			mobs[x++]=mob;
		return mobs;
	}

	@Override
	public boolean isCatalogObj(Environmental E)
	{
		if(E instanceof MOB)
			return getCatalogMob(E.Name()) != null;
		if(E instanceof Item)
			return getCatalogItem(E.Name()) != null;
		return false;
	}

	@Override
	public boolean isCatalogObj(String name)
	{
		Object o=getCatalogMob(name);
		if(o==null)
			o=getCatalogItem(name);
		return o!=null;
	}

	@Override
	public Item getCatalogItem(String called)
	{
		return (Item) getCatalogObject(icatalog, called, 1);
	}

	@Override
	public MOB getCatalogMob(String called)
	{
		return (MOB) getCatalogObject(mcatalog, called, 1);
	}

	@Override
	public CataData getCatalogItemData(String called)
	{
		return (CataData) getCatalogObject(icatalog, called, 2);
	}

	@Override
	public CataData getCatalogMobData(String called)
	{
		return (CataData) getCatalogObject(mcatalog, called, 2);
	}

	@Override
	public Vector<RoomContent> roomContent(Room R)
	{
		Item I=null;
		MOB M=null;
		Environmental E=null;
		ShopKeeper SK=null;
		List<Environmental> shops=null;
		Environmental shopItem=null;
		final Vector<RoomContent> content =new Vector<RoomContent>();
		if(R!=null)
		{
			shops=CMLib.coffeeShops().getAllShopkeepers(R,null);
			for(int s=0;s<shops.size();s++)
			{
				E=shops.get(s);
				if(E==null)
					continue;
				SK=CMLib.coffeeShops().getShopKeeper(E);
				if(SK==null)
					continue;
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					shopItem=i.next();
					if(shopItem instanceof Physical)
						content.addElement(new RoomContentImpl((Physical)shopItem,SK));
				}
			}
			for(int i=0;i<R.numItems();i++)
			{
				I=R.getItem(i);
				if(I!=null)
					content.addElement(new RoomContentImpl(I));
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if(M==null)
					continue;
				for(int i=0;i<M.numItems();i++)
				{
					I=M.getItem(i);
					if(I!=null)
						content.addElement(new RoomContentImpl(I,M));
				}
				content.addElement(new RoomContentImpl(M));
			}
		}
		return content;
	}

	@Override
	public void updateRoomContent(String roomID, List<RoomContent> content)
	{
		final List<Environmental> updatables=new LinkedList<Environmental>();
		final List<Environmental> deletables=new LinkedList<Environmental>();
		for(final RoomContent C : content)
		{
			if(C.deleted())
			{
				if(C.holder()!=null)
				{
					if(!updatables.contains(C.holder()))
						updatables.add(C.holder());
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
						updatables.add(C.holder());
				}
				else
				if(!updatables.contains(C.P()))
					updatables.add(C.P());
			}
		}
		for(final Environmental E : deletables)
		{
			updatables.remove(E);
			if((!(E instanceof DBIdentifiable))
			||(!((DBIdentifiable)E).canSaveDatabaseID())
			||(((DBIdentifiable)E).databaseID().trim().length()==0))
				continue;
			if(E instanceof MOB)
			{
				CMLib.database().DBDeleteMOB(roomID,(MOB)E);
				this.catalogFileMobsRoot=null;
			}
			else
			{
				CMLib.database().DBDeleteItem(roomID,(Item)E);
				this.catalogFileItemsRoot=null;
			}
		}
		for(final Environmental E : updatables)
		{
			if(E instanceof ShopKeeper)
			{
				final ShopKeeper SK=(ShopKeeper)E;
				final Vector<Environmental> newShop=new Vector<Environmental>();
				for(final RoomContent C : content)
				{
					if((C.holder()==SK)&&(!C.deleted()))
						newShop.addElement(C.P());
				}
				SK.getShop().resubmitInventory(newShop);
			}
		}
		for(final Environmental E : updatables)
		{
			if((!(E instanceof DBIdentifiable))
			||(!((DBIdentifiable)E).canSaveDatabaseID())
			||(((DBIdentifiable)E).databaseID().trim().length()==0))
				continue;
			if(E instanceof MOB)
			{
				CMLib.database().DBUpdateMOB(roomID,(MOB)E);
				this.catalogFileMobsRoot=null;
			}
			else
			{
				CMLib.database().DBUpdateItem(roomID,(Item)E);
				this.catalogFileItemsRoot=null;
			}
		}
	}

	@Override
	public void addCatalog(Physical PA)
	{
		addCatalog(null,PA);
	}

	@Override
	public void addCatalog(String category, Physical PA)
	{
		if((PA==null)
		||(!(PA instanceof DBIdentifiable))
		||(!((DBIdentifiable)PA).canSaveDatabaseID()))
			return;
		synchronized(getSync(PA).intern())
		{
			changeCatalogFlag(PA,true);
			final Physical origP=PA;
			PA=(Physical)origP.copyOf();
			submitToCatalog(PA);
			if(PA instanceof Item)
			{
				CMLib.database().DBCreateThisItem("CATALOG_ITEMS",(Item)PA);
				this.catalogFileItemsRoot=null;
			}
			else
			if(PA instanceof MOB)
			{
				CMLib.database().DBCreateThisMOB("CATALOG_MOBS",(MOB)PA);
				this.catalogFileMobsRoot=null;
			}
			final CataData data=getCatalogData(PA);
			if(data!=null)
			{
				if(category != null)
					data.setCategory(category);
				data.addReference(origP);
			}
		}
	}

	@Override
	public void submitToCatalog(Physical P)
	{
		submitToCatalog(null,P);
	}

	public void submitToCatalog(String category, Physical P)
	{
		if((P==null)
		||(!(P instanceof DBIdentifiable))
		||(!((DBIdentifiable)P).canSaveDatabaseID()))
			return;
		synchronized(getSync(P).intern())
		{
			if(getCatalogObj(P)!=null)
				return;
			changeCatalogFlag(P,false);
			P.text(); // to get cataloged status into xml
			if(P instanceof Item)
			{
				synchronized(icatalog)
				{
					addCatalogReplace(icatalog,category,P);
				}
			}
			else
			if(P instanceof MOB)
			{
				synchronized(mcatalog)
				{
					addCatalogReplace(mcatalog,category,P);
				}
			}
		}
	}

	@Override
	public void delCatalog(Physical P)
	{
		if(P==null)
			return;
		P=getCatalogObj(P);
		if(P==null)
			return;
		final CataData data=getCatalogData(P);
		if(P instanceof Item)
		{
			synchronized(icatalog)
			{
				icatalog.removeElement(P);
			}
			CMLib.database().DBDeleteItem("CATALOG_ITEMS",(Item)P);
			this.catalogFileItemsRoot=null;
		}
		else
		if(P instanceof MOB)
		{
			synchronized(mcatalog)
			{
				mcatalog.removeElement(P);
			}
			CMLib.database().DBDeleteMOB("CATALOG_MOBS",(MOB)P);
			this.catalogFileMobsRoot=null;
		}
		if(data!=null)
		{
			final Vector<Room> rooms=new Vector<Room>();
			for(final Enumeration<Physical> e=data.enumeration();e.hasMoreElements();)
			{
				final Physical P2=e.nextElement();
				final Room R=CMLib.map().getStartRoom(P2);
				if((R!=null)&&(!rooms.contains(R)))
					rooms.addElement(R);
				changeCatalogUsage(P2,false);
			}
			for(Room R : rooms)
			{
				List<RoomContent> contents=roomContent(R);
				for(final RoomContent content : contents)
				{
					if((CMLib.flags().isCataloged(content.P()))
					&&(content.P().Name().equalsIgnoreCase(P.Name())))
					{
						if(((P instanceof MOB)&&(content.P() instanceof MOB))
						||((P instanceof Item)&&(content.P() instanceof Item)))
							changeCatalogFlag(content.P(),false);
					}
				}
				R=CMLib.coffeeMaker().makeNewRoomContent(R,false);
				contents=roomContent(R);
				boolean dirty=false;
				for(final RoomContent content : contents)
				{
					if((CMLib.flags().isCataloged(content.P()))
					&&(content.P().Name().equalsIgnoreCase(P.Name())))
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
				if(dirty)
					updateRoomContent(R.roomID(),contents);
				R.destroy();
			}
		}
	}

	@Override
	public void updateCatalogCategory(Physical modelP, String newCat)
	{
		if((modelP==null)
		||(!(modelP instanceof DBIdentifiable))
		||(!((DBIdentifiable)modelP).canSaveDatabaseID()))
			return;
		synchronized(getSync(modelP).intern())
		{
			final CataData data=getCatalogData(modelP);
			if(data!=null)
			{
				data.setCategory(newCat.toUpperCase().trim());
				if(modelP instanceof MOB)
				{
					CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)modelP);
					this.catalogFileMobsRoot=null;
				}
				else
				{
					CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)modelP);
					this.catalogFileItemsRoot=null;
				}
			}
		}

	}

	@Override
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
					addCatalogReplace(icatalog,null,cataP);
				}
			}
			else
			if(cataP instanceof MOB)
			{
				synchronized(mcatalog)
				{
					addCatalogReplace(mcatalog,null,cataP);
				}
			}
			cataP=getCatalogObj(cataP);
			if(cataP instanceof MOB)
			{
				CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)cataP);
				this.catalogFileMobsRoot=null;
			}
			else
			{
				CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)cataP);
				this.catalogFileItemsRoot=null;
			}

			final CataData data = getCatalogData(cataP);
			if(data!=null)
				data.delReference(cataP);
			SHashSet<Physical> ignored=null;
			if(data!=null)
				ignored=new SHashSet<Physical>(data.enumeration());
			else
				ignored=new SHashSet<Physical>(1);
			Physical P;
			for(final Iterator<Physical> i=ignored.iterator();i.hasNext();)
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
			final Vector<Environmental> all=new Vector<Environmental>();
			final String srchStr="$"+cataP.Name()+"$";
			final boolean isMob=(cataP instanceof MOB);
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
			final HashSet<ShopKeeper> doneShops=new HashSet<ShopKeeper>();
			ShopKeeper SK=null;
			for (final Environmental environmental : all)
			{
				P=(Physical)environmental;
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
				if((SK!=null)&&(!doneShops.contains(SK)))
				{
					doneShops.add(SK);
					propogateShopChange(SK,ignored,cataP);
				}
			}
		}
	}

	@Override
	public void newInstance(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			final PhyStats stats=P.basePhyStats();
			if((stats!=null)&&(CMath.bset(stats.disposition(),PhyStats.IS_CATALOGED)))
			{
				final CataData data=getCatalogData(P);
				if(data!=null)
					data.addReference(P);
			}
		}
	}

	@Override
	public void bumpDeathPickup(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			final PhyStats stats=P.basePhyStats();
			if((stats!=null)&&(CMath.bset(stats.disposition(),PhyStats.IS_CATALOGED)))
			{
				final CataData data=getCatalogData(P);
				if(data!=null)
					data.bumpDeathPickup();
			}
		}
	}

	@Override
	public void changeCatalogUsage(Physical P, boolean toCataloged)
	{
		synchronized(getSync(P).intern())
		{
			if((P!=null)
			&&(P.basePhyStats()!=null)
			&&(!P.amDestroyed()))
			{
				if(toCataloged)
				{
					changeCatalogFlag(P,true);
					final CataData data=getCatalogData(P);
					if(data!=null)
						data.addReference(P);
				}
				else
				if(CMLib.flags().isCataloged(P))
				{
					changeCatalogFlag(P,false);
					final CataData data=getCatalogData(P);
					if(data!=null)
						data.delReference(P);
				}
			}
		}
	}

	protected void propogateShopChange(ShopKeeper SK, Set<Physical> ignored, Physical cataP)
	{
		final boolean isMob=(cataP instanceof MOB);
		Environmental E=null;
		boolean changes=false;
		final Vector<Environmental> newShop=new Vector<Environmental>();
		for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
		{
			E=i.next();
			if(!ignored.contains(E) && (E instanceof Physical))
			{
				ignored.add((Physical)E);
				if((isMob)
				&&(E instanceof MOB)
				&&(CMLib.flags().isCataloged(E))
				&&(cataP.Name().equalsIgnoreCase(E.Name())))
				{
					E.setMiscText(E.text());
					changes = true;
				}
				if((!isMob)
				&&(E instanceof Item)
				&&(CMLib.flags().isCataloged(E))
				&&(cataP.Name().equalsIgnoreCase(E.Name())))
				{
					E.setMiscText(E.text());
					changes = true;
				}
			}
			newShop.add(E);
		}
		if(changes)
			SK.getShop().resubmitInventory(newShop);
	}

	@Override
	public CataData getCatalogData(Physical P)
	{
		if(P==null)
			return null;
		return (P instanceof MOB)?getCatalogMobData(P.Name()):getCatalogItemData(P.Name());
	}

	@Override
	public Physical getCatalogObj(Physical P)
	{
		if(P==null)
			return null;
		return (P instanceof MOB)?getCatalogMob(P.Name()):getCatalogItem(P.Name());
	}

	@Override
	public void setCategory(Physical P, String category)
	{
		final CataData data=getCatalogData(P);
		if((data!=null)&&(category!=null))
			data.setCategory(category);
	}

	@Override
	public void updateCatalogIntegrity(Physical P)
	{
		synchronized(getSync(P).intern())
		{
			if(checkCatalogIntegrity(P)!=null)
			{
				changeCatalogFlag(P,false);
				P.text();
			}
		}
	}

	private final String getSync(String name, boolean mobType)
	{
		return ((mobType) ? "CATASYNC_MOB_" : "CATASYNC_ITEM_") + name.toUpperCase();
	}

	private final String getSync(Environmental E)
	{
		return getSync(E.Name(), E instanceof MOB);
	}

	@Override
	public StringBuffer checkCatalogIntegrity(Physical P)
	{
		if(P==null)
			return null;
		synchronized(getSync(P).intern())
		{
			if(CMLib.flags().isCataloged(P))
			{
				final CataData data=getCatalogData(P);
				final Physical cataE=getCatalogObj(P);
				if((cataE==null)||(data==null))
				{
					if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
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
						{
							if((!cataE.getStat(cataE.getStatCodes()[i]).equals(P.getStat(cataE.getStatCodes()[i]))))
								diffs.append(cataE.getStatCodes()[i]+",");
						}
					}
					if((P instanceof MOB)&&(cataE instanceof MOB))
					{
						boolean firstChecked=false;
						if(diffs==null)
							diffs=new StringBuffer("");
						for(final Pair<Clan,Integer> p : ((MOB)P).clans())
						{
							if((((MOB)cataE).getClanRole(p.first.clanID())==null)
							||(((MOB)cataE).getClanRole(p.first.clanID()).second.intValue()!=p.second.intValue()))
								firstChecked=true;
						}
						for(final Pair<Clan,Integer> p : ((MOB)cataE).clans())
						{
							if((((MOB)P).getClanRole(p.first.clanID())==null)
							||(((MOB)P).getClanRole(p.first.clanID()).second.intValue()!=p.second.intValue()))
								firstChecked=true;
						}
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
				final CataData data=getCatalogData(P);
				if(data!=null)
					data.delReference(P);
				return null;
			}
		}
	}

	@Override
	public Item getDropItem(MOB M, boolean live)
	{
		if(M==null)
			return null;
		CatalogLibrary.CataData data=null;
		List<Item> selections=null;
		synchronized(icatalog)
		{
			try
			{
				for(int d=0;d<icatalog.size();d++)
				{
					data=(CatalogLibrary.CataData)icatalog.elementAt(d,2);
					if((data.getRate()>0.0)
					&&(data.getMaskV()!= null)
					&&(data.getWhenLive()==live)
					&&(Math.random() <= data.getRate())
					&&(CMLib.masking().maskCheck(data.getMaskV(),M,true)))
					{
						if(selections==null)
							selections=new ArrayList<Item>();
						selections.add((Item)icatalog.elementAt(d,1));
					}
				}
			}
			catch(final IndexOutOfBoundsException e)
			{
			}
		}
		if(selections==null)
			return null;
		Item I=selections.get(CMLib.dice().roll(1,selections.size(),-1));
		I=(Item)I.copyOf();
		changeCatalogUsage(I,true);
		return I;
	}

	@Override
	public CataData sampleCataData(String xml)
	{
		return new CataDataImpl(xml);
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THCatalog"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}

	@Override 
	public boolean tick(Tickable ticking, int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CATALOGTHREAD))
			{
				tickStatus=Tickable.STATUS_ALIVE;
				isDebugging=CMSecurity.isDebugging(DbgFlag.CATALOGTHREAD);
				setThreadStatus(serviceClient,"checking catalog references.");
				String[] names = getCatalogItemNames();
				for (final String name2 : names)
				{
					final CataData data=getCatalogItemData(name2);
					data.cleanHouse();
				}
				names = getCatalogMobNames();
				for (final String name2 : names)
				{
					final CataData data=getCatalogMobData(name2);
					data.cleanHouse();
				}
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		icatalog=new DVector(2);
		mcatalog=new DVector(2);
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	protected void forceTick()
	{
		serviceClient.tickTicker(true);
	}

	protected static class RoomContentImpl implements RoomContent
	{
		private Physical 		obj=null;
		private boolean 		dirty=false;
		private Environmental	holder=null;

		public RoomContentImpl(Physical P)
		{
			obj = P;
		}

		public RoomContentImpl(Physical P, Environmental E)
		{
			obj = P;
			holder = E;
		}

		@Override
		public Physical P()
		{
			return obj;
		}

		@Override
		public void flagDirty()
		{
			dirty = true;
		}

		@Override
		public Environmental holder()
		{
			return holder;
		}

		@Override
		public boolean isDirty()
		{
			return dirty;
		}

		@Override
		public boolean deleted()
		{
			return obj.amDestroyed();
		}
	}

	protected static class CataDataImpl implements CataData
	{
		public String 		lmaskStr			= null;
		public String		category			= "";
		public boolean 		live				= false;
		public double 		rate				= 0.0;
		public volatile int deathPickup			= 0;
		public SVector<WeakReference<Physical>>
							refs				= new SVector<WeakReference<Physical>>(1);
		public boolean 		noRefs = CMProps.getBoolVar(CMProps.Bool.CATALOGNOCACHE)
								  || CMSecurity.isDisabled(CMSecurity.DisFlag.CATALOGCACHE);
		public MaskingLibrary.CompiledZMask 
							lmaskV				= null;

		public CataDataImpl(String catadata)
		{
			build(catadata);
		}

		private Vector<Physical> makeVector()
		{
			final Vector<Physical> V=new Vector<Physical>(refs.size());
			WeakReference<Physical> R=null;
			for(int r=0;r<refs.size();r++)
			{
				R=refs.elementAt(r);
				if(R!=null)
				{
					final Physical o=R.get();
					if((o!=null)
					&&(!o.amDestroyed())
					&&(CMath.bset(o.basePhyStats().disposition(),PhyStats.IS_CATALOGED)))
						V.addElement(o);
				}
			}
			return V;
		}

		@Override
		public String category()
		{
			return category;
		}

		@Override
		public void setCategory(String cat)
		{
			if(cat!=null)
				category=cat;
			else
				category="";
		}

		protected RoomnumberSet getLocations()
		{
			final Vector<Physical> items=makeVector();
			final RoomnumberSet homes=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			final RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
			Room R=null;
			for(final Environmental E : items)
			{
				R=CMLib.map().getStartRoom(E);
				if(R==null)
					R=CMLib.map().roomLocation(E);
				if(R==null)
					continue;
				if((E instanceof Item)
				&&(((Item)E).owner() instanceof MOB)
				&&(!((MOB)((Item)E).owner()).isMonster()))
					continue;
				if(CMLib.law().getLandTitle(R)!=null)
					homes.add(CMLib.map().getExtendedRoomID(R));
				else
					set.add(CMLib.map().getExtendedRoomID(R));
			}
			if(set.roomCountAllAreas()>0)
				return set;
			return homes;
		}

		@Override
		public String randomRoom()
		{
			final RoomnumberSet set=getLocations();
			if(set.roomCountAllAreas()==0)
				return "";
			return set.random();
		}

		@Override
		public String mostPopularArea()
		{
			final RoomnumberSet set=getLocations();
			final Iterator<String> e=set.getAreaNames();
			if(!e.hasNext())
				return "";
			String maxArea=e.next();
			int maxCount=set.roomCount(maxArea);
			for(;e.hasNext();)
			{
				final String area=e.next();
				final int count=set.roomCount(area);
				if(count>maxCount)
				{
					maxCount=count;
					maxArea=area;
				}
			}
			final Area A=CMLib.map().getArea(maxArea);
			if(A!=null)
				return A.Name();
			return maxArea;
		}

		@Override
		public int numReferences()
		{
			int num=0;
			for(int r=0;r<refs.size();r++)
			{
				if(refs.elementAt(r).get()!=null)
					num++;
			}
			return num;
		}

		@Override
		public Enumeration<Physical> enumeration()
		{
			return makeVector().elements();
		}

		@Override
		public int getDeathsPicksups()
		{
			return deathPickup;
		}

		@Override
		public void bumpDeathPickup()
		{
			deathPickup++;
		}

		@Override
		public synchronized void cleanHouse()
		{
			if(noRefs)
			{
				refs.clear();
				return;
			}
			Environmental o=null;
			try
			{
				for(int r=refs.size()-1;r>=0;r--)
				{
					o=refs.elementAt(r).get();
					if((o==null)||o.amDestroyed()||(!CMLib.flags().isCataloged(o)))
						refs.removeElementAt(r);
				}
			}
			catch (final ArrayIndexOutOfBoundsException ex)
			{
			}
		}

		@Override
		public Physical getLiveReference()
		{
			if(noRefs)
				return null;
			Physical o=null;
			try
			{
				for(int r=0;r<refs.size();r++)
				{
					o=refs.elementAt(r).get();
					if((o!=null)&&(CMLib.flags().isInTheGame(o,true)))
						return o;
				}
			}
			catch (final Exception e)
			{
			}
			return null;
		}

		@Override
		public synchronized void addReference(Physical P)
		{
			if(noRefs)
				return;
			if(isReference(P))
				return;
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

		@Override
		public boolean isReference(Physical P)
		{
			for(int r=0;r<refs.size();r++)
			{
				if(refs.elementAt(r).get()==P)
					return true;
			}
			return false;
		}

		@Override
		public synchronized void delReference(Physical P)
		{
			if(!isReference(P))
				return;
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

		protected CataDataImpl(String _lmask, String _rate, boolean _live)
		{
			this(_lmask,CMath.s_pct(_rate),_live);
		}

		protected CataDataImpl(String _lmask, double _rate, boolean _live)
		{
			live=_live;
			lmaskStr=_lmask;
			lmaskV=null;
			if(lmaskStr.length()>0)
				lmaskV=CMLib.masking().maskCompile(lmaskStr);
			rate=_rate;
		}

		@Override
		public MaskingLibrary.CompiledZMask getMaskV()
		{
			return lmaskV;
		}

		@Override
		public String getMaskStr()
		{
			return lmaskStr;
		}

		@Override
		public boolean getWhenLive()
		{
			return live;
		}

		@Override
		public double getRate()
		{
			return rate;
		}

		@Override
		public void setMaskStr(String s)
		{
			lmaskStr=s;
			if(s.trim().length()==0)
				lmaskV=null;
			else
				lmaskV=CMLib.masking().maskCompile(s);
		}

		@Override
		public void setWhenLive(boolean l)
		{
			live = l;
		}

		@Override
		public void setRate(double r)
		{
			rate = r;
		}

		@Override
		public String data(String name)
		{
			final StringBuffer buf=new StringBuffer("");
			buf.append("<CATALOGDATA ");
			if(name != null)
				buf.append("NAME=\""+CMLib.xml().parseOutAngleBracketsAndQuotes(name)+"\" ");
			buf.append("CATAGORY=\""+CMLib.xml().parseOutAngleBracketsAndQuotes(category)+"\">");
			buf.append("<RATE>"+CMath.toPct(rate)+"</RATE>");
			buf.append("<LMASK>"+CMLib.xml().parseOutAngleBrackets(lmaskStr)+"</LMASK>");
			buf.append("<LIVE>"+live+"</LIVE>");
			buf.append("</CATALOGDATA>");
			return buf.toString();
		}

		@Override
		public void build(String catadata)
		{
			List<XMLLibrary.XMLTag> V=null;
			if((catadata!=null)&&(catadata.length()>0))
			{
				V=CMLib.xml().parseAllXML(catadata);
				final XMLTag piece=CMLib.xml().getPieceFromPieces(V,"CATALOGDATA");
				if((piece!=null)&&(piece.contents()!=null)&&(piece.contents().size()>0))
				{
					category=CMLib.xml().restoreAngleBrackets(piece.getParmValue( "CATAGORY"));
					if(category==null)
						category="";
					lmaskStr=CMLib.xml().restoreAngleBrackets(piece.getValFromPieces("LMASK"));
					final String ratestr=piece.getValFromPieces("RATE");
					rate=CMath.s_pct(ratestr);
					lmaskV=null;
					if(lmaskStr.length()>0)
						lmaskV=CMLib.masking().maskCompile(lmaskStr);
					live=CMath.s_bool(piece.getValFromPieces("LIVE"));
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

	protected CMFile.CMVFSDir getCatalogMobsRoot(CMFile.CMVFSDir rootRoot)
	{
		if(catalogFileMobsRoot == null)
		{
			final CMFile.CMVFSDir newRoot=getCatalogRoot(mcatalog, "mobs", rootRoot);
			if(newRoot==null)
				return null;
			catalogFileMobsRoot=newRoot;
		}
		return catalogFileMobsRoot;
	}

	protected CMFile.CMVFSDir getCatalogItemsRoot(CMFile.CMVFSDir rootRoot)
	{
		if(catalogFileItemsRoot == null)
		{
			final CMFile.CMVFSDir newRoot=getCatalogRoot(icatalog, "items", rootRoot);
			if(newRoot==null)
				return null;
			catalogFileItemsRoot=newRoot;
		}
		return catalogFileItemsRoot;
	}

	@Override
	public CMFile.CMVFSDir getCatalogRoot(final CMFile.CMVFSDir root)
	{
		return new CMFile.CMVFSDir(root,root.getPath()+"catalog/")
		{
			private CMFile.CMVFSFile[]	myFiles		= null;
			private CMFile.CMVFSFile[]	oldFiles	= null;

			@Override 
			protected CMFile.CMVFSFile[] getFiles()
			{
				if((myFiles==null)||(oldFiles!=super.files)||(catalogFileItemsRoot==null)||(catalogFileMobsRoot==null))
				{
					oldFiles=super.files;
					final CMFile.CMVFSDir mdir = getCatalogMobsRoot(this);
					final CMFile.CMVFSDir idir = getCatalogItemsRoot(this);
					final int xtra=((mdir==null)?0:1)+((idir==null)?0:1);
					if(super.files!=null)
						myFiles=Arrays.copyOf(super.files, super.files.length+xtra);
					else
						myFiles=new CMFile.CMVFSFile[xtra];
					if(xtra==2)
					{
						myFiles[myFiles.length-2]=mdir;
						myFiles[myFiles.length-1]=idir;
					}
					else
					if(mdir != null)
						myFiles[myFiles.length-1]=mdir;
					else
					if(idir != null)
						myFiles[myFiles.length-1]=idir;
					Arrays.sort(myFiles,CMFile.CMVFSDir.fcomparator);
				}
				return myFiles;
			}
		};
	}

	protected CMFile.CMVFSDir getCatalogRoot(final DVector catalog, String rootName, CMFile.CMVFSDir rootRoot)
	{
		if(catalog.size()==0)
			return null;
		final CMFile.CMVFSDir catalogFileRoot=new CMFile.CMVFSDir(rootRoot, 48, rootRoot.getPath()+rootName+"/");

		final HashMap<String,List<Physical>> usedCats=new HashMap<String,List<Physical>>();
		for(int i=0;i<catalog.size();i++)
		{
			final Physical obj=(Physical)catalog.elementAt(i, 1);
			final CataData data=(CataData)catalog.elementAt(i, 2);

			catalogFileRoot.add(new CMFile.CMVFSFile(catalogFileRoot.getPath()+obj.Name().replace(' ','_')+".cmare",48,System.currentTimeMillis(),"SYS")
			{
				@Override 
				public Object readData()
				{
					if(obj instanceof MOB)
						return CMLib.coffeeMaker().getMobXML((MOB)obj);
					else
					if(obj instanceof Item)
						return CMLib.coffeeMaker().getItemXML((Item)obj);
					else
						return null;
				}
			});

			if(!usedCats.containsKey(data.category()))
				usedCats.put(data.category(), new Vector<Physical>());
			final List<Physical> list=usedCats.get(data.category());
			list.add(obj);
		}
		catalogFileRoot.add(new CMFile.CMVFSFile(catalogFileRoot.getPath()+"all.cmare",48,System.currentTimeMillis(),"SYS")
		{
			@Override 
			public Object readData()
			{
				final String tagName=(catalog.elementAt(0,1) instanceof MOB)?"MOBS":"ITEMS";
				final StringBuilder str=new StringBuilder("<"+tagName+">");
				for(int i=0;i<catalog.size();i++)
				{
					final Physical obj=(Physical)catalog.elementAt(i, 1);
					if(obj instanceof MOB)
						str.append(CMLib.coffeeMaker().getMobXML((MOB)obj));
					else
					if(obj instanceof Item)
						str.append(CMLib.coffeeMaker().getItemXML((Item)obj));
				}
				str.append("</"+tagName+">");
				return str.toString();
			}
		});
		for(final String cat : usedCats.keySet())
		{
			CMFile.CMVFSDir catagoryRoot=catalogFileRoot;
			if(cat.length()==0)
			{
				catagoryRoot=new CMFile.CMVFSDir(catalogFileRoot, 48, catalogFileRoot.getPath()+"uncategorized/");
				catalogFileRoot.add(catagoryRoot);
			}
			else
			{
				catagoryRoot=new CMFile.CMVFSDir(catalogFileRoot, 48, catalogFileRoot.getPath()+cat.toLowerCase()+"/");
				catalogFileRoot.add(catagoryRoot);
			}
			final List<Physical> objs=usedCats.get(cat);
			if(objs.size()>0)
			{
				for(final Physical obj : objs)
				{
					catagoryRoot.add(new CMFile.CMVFSFile(catagoryRoot.getPath()+obj.Name().replace(' ','_')+".cmare",48,System.currentTimeMillis(),"SYS")
					{
						@Override 
						public Object readData()
						{
							if(obj instanceof MOB)
								return CMLib.coffeeMaker().getMobXML((MOB)obj);
							else
							if(obj instanceof Item)
								return CMLib.coffeeMaker().getItemXML((Item)obj);
							else
								return null;
						}
					});
				}
			}
			catagoryRoot.add(new CMFile.CMVFSFile(catagoryRoot.getPath()+"all.cmare",48,System.currentTimeMillis(),"SYS")
			{
				@Override 
				public Object readData()
				{
					final String tagName=(objs.get(0) instanceof MOB)?"MOBS":"ITEMS";
					final StringBuilder str=new StringBuilder("<"+tagName+">");
					for(final Physical obj : objs)
					{
						if(obj instanceof MOB)
							str.append(CMLib.coffeeMaker().getMobXML((MOB)obj));
						else
						if(obj instanceof Item)
							str.append(CMLib.coffeeMaker().getItemXML((Item)obj));
					}
					str.append("</"+tagName+">");
					return str.toString();
				}
			});
		}
		return catalogFileRoot;
	}
}
