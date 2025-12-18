package com.planet_ink.coffee_mud.Behaviors;

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
import com.planet_ink.coffee_mud.Common.interfaces.CoffeeShop.ShopProvider;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.BankDepositEntry;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class StockMarket extends StdBehavior
{
	@Override
	public String ID()
	{
		return "StockMarket";
	}

	public PhysicalAgent			host			= null;
	public StockMarket				me				= this;
	public String					journalName		= "";
	private volatile int			weatherDown		= Climate.WEATHER_TICK_DOWN;
	private final AtomicBoolean		processing		= new AtomicBoolean(false);
	private final Set<ShopKeeper>	stockbrokers	= Collections.synchronizedSet(new WeakSHashSet<ShopKeeper>());
	private final Set<CMMsg> 		lastGives 		= Collections.synchronizedSet(new ExpireHashSet<CMMsg>(1000));

	private final Map<String,Pair<LegalBehavior,Area>>	legalCache = new Hashtable<String,Pair<LegalBehavior,Area>>();

	private final ShopProvider shopProvider = new ShopProvider()
	{
		final String ID = "StockMarker_"+hashCode();
		@Override
		public String ID()
		{
			return ID;
		}

		@Override
		public String name()
		{
			return ID;
		}

		@Override
		public CMObject newInstance()
		{
			return this;
		}

		@Override
		public CMObject copyOf()
		{
			return this;
		}

		@Override
		public void initializeClass()
		{
		}
		@Override
		public int compareTo(final CMObject o)
		{
			return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
		}

		@Override
		public Collection<ShelfProduct> getStock(final MOB buyer, final CoffeeShop shop, final Room myRoom)
		{
			final List<ShelfProduct> stockList = new ArrayList<ShelfProduct>();
			final Area A = myRoom!=null?myRoom.getArea():null;
			if((A==null)||(!(host instanceof Area)))
				return stockList;
			final String currency = CMLib.beanCounter().getCurrency(shop.shopKeeper());
			final String areaName = A.Name();
			final Set<StockDef> done = new HashSet<StockDef>();
			final ShoppingLibrary shops = CMLib.coffeeShops();
			for(final MarketConf conf : configs)
			{
				final Collection<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
						&&(!done.contains(def)))
						{
							//TODO: should stockbrokers sell replacement certs when you aren't carrying enough of them?
							// Players who keep their stocks where the adjuster can't find them depend on it.  But HOW?!
							if(def.deed == null)
							{
								def.deed = CMClass.getItem("GenCertificate");
								((PrivateProperty)def.deed).setOwnerName("");
								def.deed.setName(def.name());
								def.deed.setReadableText(def.getTitleID());
								def.deed.basePhyStats().setWeight(1);
								def.deed.phyStats().setWeight(1);
								def.deed.basePhyStats().setAbility(0);
								def.deed.phyStats().setAbility(0);
								final Ability nosellA = CMClass.getAbility("Prop_Unsellable");
								if(nosellA != null)
								{
									nosellA.setMiscText("SEXCEPTMASK=\"-IFSTAT +WHATISELL|=8796093022208\" MESSAGE=\"I don't buy those.\"");
									def.deed.addNonUninvokableEffect(nosellA);
								}
							}
							int remain = Integer.MAX_VALUE/2;
							if(conf.maxSharesPerStock<Integer.MAX_VALUE)
							{
								remain = conf.maxSharesPerStock - getOutstandingShares(def,0);
								if(remain <= 0)
									continue;
							}
							//final int amtOwned = me.getStocksOwned(buyer, def);
							final Item deed = (Item)def.deed.copyOf();
							((PrivateProperty)deed).setPrice(def.getPrice());
							final ShelfProduct product = shops.createShelfProduct(deed, 1, remain, currency);
							stockList.add(product);
							done.add(def);
						}
					}
				}
			}
			return stockList;
		}
	};

	/**
	 * Ways that shopkeepers can be grouped to form single stocks
	 */
	private enum GroupBy
	{
		NOTHING,
		ALL,
		SHOPTYPE,
		RACE
	}

	/**
	 * Direction of influence scores
	 */
	private static enum InfluDir
	{
		POSITIVE,
		NEGATIVE,
		VARIABLE
	}

	private static double[][] THE_TABLE = new double[][]
	{
		/*-95*/ {  -8,  8,  8},
		/*-90*/ {  -7, -7,  7},		/*-85*/ {  -7, -7,  7},		/*-80*/ {  -7, -7,  7},		/*-75*/ {  -7, -7,  7},
		/*-70*/ {  -6,  6,  6},		/*-65*/ {  -6,  6,  6},		/*-60*/ {  -6,  6,  6},		/*-55*/ {  -6,  6,  6},		/*-50*/ {  -6,  6,  6},
		/*-45*/ {  -5, -5,  5},		/*-40*/ {  -5, -5,  5},		/*-35*/ {  -5, -5,  5},		/*-30*/ {  -5, -5,  5},		/*-25*/ {  -5, -5,  5},
		/*-20*/ {  -4,  4,  4},		/*-15*/ {  -4,  4,  4},		/*-10*/ {  -4,  4,  4},		/*-05*/ {  -4,  4,  4},		/*-00*/ {  -4,  4,  4},
		/*  5*/ {  -3, -3,  3},
		/* 10*/ {  -2,  2,  2},		/* 15*/ {  -2,  2,  2},
		/* 20*/ {  -1, -1,  1},		/* 25*/ {  -1, -1,  1},
		/* 30*/ {  -0,  0,-.1},		/* 35*/ {  -0,  0, .1},		/* 40*/ {  -0,  0,-.1},		/* 45*/ {  -0,  0,-.1},
		/* 50*/ {  .2, .1,-.2},		/* 55*/ {  .2, .1, .2},
		/* 60*/ {   1,  0, -1},		/* 65*/ {   1,  0, -1},		/* 70*/ {   1,  0, -1},		/* 75*/  {   1,  0, -1},
		/* 80*/ {   2,  1, -2},		/* 85*/ {   2,  1, -2},
		/* 90*/ {   3, -2, -3},		/* 95*/ {   3, -2, -3},
		/*100*/ {   5,  3, -5},
		/*105*/ {   6, -4, -6},		/*110*/ {   6, -4, -6},		/*115*/ {   6, -4, -6},		/*120*/ {   6, -4, -6},		/*125*/ {   6, -4, -6},
		/*130*/ {   7,  5, -7},		/*135*/ {   7,  5, -7},		/*140*/ {   7,  5, -7},		/*145*/ {   7,  5, -7},		/*150*/ {   7,  5, -7},
		/*155*/ {   8, -6, -8},		/*160*/ {   8, -6, -8},		/*165*/ {   8, -6, -8},		/*170*/ {   8, -6, -8},		/*175*/ {   8, -6, -8},
		/*180*/ {   9,  7, -9},		/*185*/ {   9,  7, -9},		/*190*/ {   9,  7, -9},		/*195*/ {   9,  7, -9},
		/*200*/ {  10, -8,-10},
	};

	final static Set<Event> EVENTS_LISTEN = new XHashSet<Event>(new Event[] {
		Event.BIRTHS, Event.QUESTOR, Event.CONQUEREDAREAS
	});

	/**
	 * Category of influence scores
	 */
	private static enum InfluCat
	{
		WEATHER,
		LEVELING,
		BIRTHS,
		SHOPPING,
		THIEVERY,
		ROBBERIES,
		QUESTING,
		CONQUEST,
		BUILDING,
		OFFICER_DEATHS,
		JUDGE_DEATHS,
		GATHERING,
		CRAFTING,
		HOLDINGS_,
		HOLDINGS,
		ASTROLOGICAL_INFLUENCES,
		RACIAL_BOONS,
		AREA_EVENTS
		;
		public int rolls(final int num)
		{
			return (int)Math.round(Math.ceil(Math.sqrt(num)));
		}
	}

	/**
	 * A single stock definition, which is tracked
	 * and based on db records.
	 */
	private class StockDef implements PrivateProperty
	{
		private final String				ID;
		private final String				name;
		public final String					area;
		public int							version				= 0;
		public Item							deed				= null;
		public volatile double				price				= 100.0;
		public volatile double				manipulation		= 0;
		public volatile TimeClock			bankruptUntil		= null;
		public int							outstandingShares	= -1;
		public STreeMap<InfluCat, int[]>	influences			= new STreeMap<InfluCat, int[]>();

		public StockDef(final String area, final String id, final String name)
		{
			this.ID = id;
			this.name = name;
			this.area = area;
		}

		public synchronized void addInfluence(final InfluCat cat, final InfluDir which, final int amt)
		{
			if(!influences.containsKey(cat))
				influences.put(cat, new int[InfluDir.values().length]);
			influences.get(cat)[which.ordinal()] += amt;
		}

		@Override
		public String ID()
		{
			return "StockDef";
		}

		@Override
		public String name()
		{
			return ID+": "+name;
		}

		@Override
		public CMObject newInstance()
		{
			final StockDef def = new StockDef(area,ID,name);
			def.price=price;
			def.manipulation=manipulation;
			return def;
		}

		@Override
		public CMObject copyOf()
		{
			return null;
		}

		@Override
		public void initializeClass()
		{
		}

		@Override
		public int compareTo(final CMObject o)
		{
			if(o == null)
				return -1;
			if(Objects.equals(this,o))
				return 0;
			final int ret = Integer.compare(hashCode(), o.hashCode());
			return (ret == 0)? 1 : ret;
		}

		@Override
		public int getPrice()
		{
			return (int)Math.round(price);
		}

		@Override
		public void setPrice(final int price)
		{
			this.price = price;
		}

		@Override
		public String getOwnerName()
		{
			return "";
		}

		@Override
		public void setOwnerName(final String owner)
		{
		}

		@Override
		public boolean isProperlyOwned()
		{
			return false;
		}

		@Override
		public String getTitleID()
		{
			return name()+"#"+version;
		}
	}

	private void updatePlayerStockXML(final String mobName, final StockDef stock, final int delta)
	{
		final List<PAData> stocksOwned = CMLib.database().DBReadPlayerData(mobName, "STOCKMARKET_STOCKS", stock.getTitleID());
		for(final PAData stockData : stocksOwned)
		{
			final String xml = stockData.xml().trim();
			if(xml.startsWith("<AMT>") && xml.endsWith("</AMT>"))
			{
				final int newAmt = CMath.s_int(xml.substring(5,xml.length()-6))+delta;
				if(newAmt <= 0)
					CMLib.database().DBDeletePlayerData(mobName, "STOCKMARKET_STOCKS", stock.getTitleID());
				else
					CMLib.database().DBUpdatePlayerData(mobName, "STOCKMARKET_STOCKS", stock.getTitleID(), "<AMT>"+newAmt+"</AMT>");
			}
		}
	}

	private synchronized int getOutstandingShares(final StockDef stock, final int adj)
	{
		if(stock.outstandingShares>=0)
		{
			stock.outstandingShares = Math.max(0,stock.outstandingShares+adj);
			return stock.outstandingShares;
		}
		int amt=0;
		final List<PAData> stocksOwned = CMLib.database().DBReadPlayerDataEntries("STOCKMARKET_STOCKS", stock.getTitleID());
		for(final PAData stockData : stocksOwned)
		{
			final String xml = stockData.xml().trim();
			if(xml.startsWith("<AMT>") && xml.endsWith("</AMT>"))
				amt += CMath.s_int(xml.substring(5,xml.length()-6));
		}
		stock.outstandingShares = Math.max(0,amt+adj);
		return stock.outstandingShares;
	}

	private int getStocksOwned(final MOB mob, final StockDef stock)
	{
		int amt=0;
		final List<PAData> stocksOwned = CMLib.database().DBReadPlayerData(mob.Name(), "STOCKMARKET_STOCKS", stock.getTitleID());
		for(final PAData stockData : stocksOwned)
		{
			final String xml = stockData.xml().trim();
			if(xml.startsWith("<AMT>") && xml.endsWith("</AMT>"))
			{
				amt += CMath.s_int(xml.substring(5,xml.length()-6));
			}
		}
		return amt;
	}

	private PairList<String,Integer> getStockOwners(final StockDef stock)
	{
		final Set<String> owners = new TreeSet<String>();
		final PairList<String,Integer> owned = new PairArrayList<String,Integer>();
		final List<PAData> stocksOwned = CMLib.database().DBReadPlayerDataEntries("STOCKMARKET_STOCKS",stock.getTitleID()); // titleid for ownership records!
		for(final PAData stockData : stocksOwned)
		{
			if(!owners.contains(stockData.who()))
			{
				owners.add(stockData.who());
				if(stockData.xml().startsWith("<AMT>") && stockData.xml().endsWith("</AMT>"))
				{
					final int amt = CMath.s_int(stockData.xml().substring(5,stockData.xml().length()-6));
					if(amt <=0)
						CMLib.database().DBDeletePlayerData(stockData.who(), "STOCKMARKET_STOCKS", stock.getTitleID());
					else
						owned.add(stockData.who(), Integer.valueOf(amt));
				}
			}
		}
		return owned;
	}

	private void savetHostStockXML(final Collection<StockDef> stocks)
	{
		final XMLLibrary xmlLib = CMLib.xml();
		final StringBuilder xml = new StringBuilder("");
		for(final StockDef def : stocks)
		{
			final String bankruptUntil=(def.bankruptUntil==null)?"":def.bankruptUntil.toTimePeriodCodeString();
			xml.append("<S ID=\""+def.ID+"\" NAME=\""+xmlLib.parseOutAngleBracketsAndQuotes(def.name)+"\" "
					+ "M="+def.manipulation+" A=\""+xmlLib.parseOutAngleBracketsAndQuotes(def.area)+"\" "
					+" PRICE="+def.price+" V="+def.version+" "
					+ "U=\""+bankruptUntil+"\" ");
			if(def.influences.size()==0)
				xml.append(" />");
			else
			{
				xml.append(">");
				for(final InfluCat cat : def.influences.keySet())
				{
					final int[] vals = def.influences.get(cat);
					for(final InfluDir typ : InfluDir.values())
					{
						if(vals[typ.ordinal()]!=0)
							xml.append("<I CAT=\""+cat.name()+"\" TYP="+typ.ordinal()+" AMT="+vals[typ.ordinal()]+" />");
					}
				}
				xml.append("</S>");
			}
		}
		final String areaName = host.Name();
		CMLib.database().DBReCreateAreaData(areaName, "CMKTSTOCKS", "CMKTSTOCKS/"+areaName,xml.toString());
	}

	private synchronized void addHostStock(final StockDef def)
	{
		final Map<String, StockDef> map = getHostStocksMap();
		if(!map.containsKey(def.name())) // name doesn't include version, so it can persist
			map.put(def.name(), def);
	}

	private synchronized Collection<StockDef> getHostStocks()
	{
		return getHostStocksMap().values();
	}

	private synchronized Map<String, StockDef> getHostStocksMap()
	{
		final String areaName = host.Name();

		@SuppressWarnings("unchecked")
		Map<String,StockDef> stocks = (Map<String,StockDef>)Resources.getResource("CMKT_AREA_STOCKS/"+areaName);
		if(stocks == null)
		{
			stocks = new STreeMap<String,StockDef>();
			final List<PAData> dat = CMLib.database().DBReadAreaData(areaName, "CMKTSTOCKS", "CMKTSTOCKS/"+areaName);
			if((dat != null)&&(dat.size()>0))
			{
				final List<XMLTag> tags = CMLib.xml().parseAllXML(dat.get(0).xml());
				for(final XMLTag tag : tags)
				{
					if(tag.tag().equals("S"))
					{
						final String ID = tag.getParmValue("ID");
						final String name  = CMLib.xml().restoreAngleBrackets(tag.getParmValue("NAME"));
						final double price  = CMath.s_double(tag.getParmValue("PRICE"));
						final int manipulation  = CMath.s_int(tag.getParmValue("M"));
						final String bankruptUntil = tag.getParmValue("U");
						final Area hostA = CMLib.map().getArea(CMLib.xml().restoreAngleBrackets(tag.getParmValue("A")));
						if(hostA != null)
						{
							final StockDef d = new StockDef(hostA.Name(), ID, name);
							d.version = CMath.s_int(tag.getParmValue("V"));
							d.price=price;
							d.manipulation=manipulation;
							for(final XMLTag itag : tag.contents())
								if(itag.tag().equals("I"))
								{
									final InfluDir typ = InfluDir.values()[CMath.s_int(itag.getParmValue("TYP"))];
									final InfluCat cat = (InfluCat)CMath.s_valueOf(InfluCat.class,itag.getParmValue("CAT"));
									d.addInfluence(cat, typ, CMath.s_int(tag.getParmValue("AMT")));
								}
							if((bankruptUntil != null)&&(bankruptUntil.trim().length()>0))
							{
								d.bankruptUntil = (TimeClock)CMClass.getCommon("DefaultTimeClock");
								d.bankruptUntil = d.bankruptUntil.fromTimePeriodCodeString(bankruptUntil);
							}
							stocks.put(d.name(),d); // name doesn't include version, which is needed for lookups
						}
					}
				}
			}
			Resources.submitResource("CMKT_AREA_STOCKS/"+areaName,stocks);
		}
		return stocks;
	}

	/**
	 * Process a deed item
	 */
	private static interface DeedProcessor
	{
		public boolean process(final Item I);
	}

	private void processDeeds(final StockDef stock, final DeedProcessor processor)
	{
		for(final Pair<String,Integer> owner : this.getStockOwners(stock))
			processDeedsByOwner(owner.first, stock, processor);
		final List<String> roomItemRooms = CMLib.database().DBFindRoomItemLocs(null, "GenCertificate", stock.getTitleID());
		for(final String roomID : roomItemRooms)
			processDeedsByRoom(CMLib.map().getRoom(roomID), stock, processor);
		final List<BankDepositEntry> entries = CMLib.beanCounter().findBankDepositedItems(null, null, "GenCertificate", stock.getTitleID());
		for(final BankDepositEntry entry : entries)
		{
			final Item I = entry.item();
			if(I == null)
				continue;
			if((I instanceof PrivateProperty) && (I.ID().equals("GenCertificate")))
			{
				if(processor.process(I))
					CMLib.beanCounter().updateBankDepositInventory(entry);
			}
			I.destroy();

		}
	}

	private boolean processDeedsByRoom(final Room R, final StockDef stock, final DeedProcessor processor)
	{
		if(R==null)
			return false;
		boolean didSomething=false;
		for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I instanceof PrivateProperty)
			&& (I.ID().equals("GenCertificate")))
			{
				if(processor.process(I))
					didSomething=true;
			}
			else
			if(I instanceof Boardable)
			{
				final Area A = ((Boardable)I).getArea();
				if(A != null)
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
						if(processDeedsByRoom(r.nextElement(), stock, processor))
							didSomething=true;
			}
		}
		return didSomething;
	}

	private void processDeedsByOwner(final String owner, final StockDef stock, final DeedProcessor processor)
	{
		final MOB M = CMLib.players().getPlayer(owner);
		if(M != null)
		{
			for(final Enumeration<Item> i=M.items();i.hasMoreElements();)
			{
				final Item I = i.nextElement();
				if((I instanceof PrivateProperty) && (I.ID().equals("GenCertificate")))
					processor.process(I);
				else
				if(I instanceof Boardable)
				{
					final Area A = ((Boardable)I).getArea();
					if(A != null)
						for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
							processDeedsByRoom(r.nextElement(), stock, processor);
				}
			}
		}
		else
		{
			for(final Triad<String, Item, String> i : CMLib.database().DBReadPlayerItems(owner, "GenCertificate", stock.getTitleID()))
			{
				final Item I = i.second;
				if((I instanceof PrivateProperty)
				&& (I.ID().equals("GenCertificate"))
				&& processor.process(I))
					CMLib.database().DBUpdatePlayerItem(i.first, I, i.third);
				else
				if(I instanceof Boardable)
				{
					final Area A = ((Boardable)I).getArea();
					boolean updated=false;
					if(A != null)
						for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
							if(processDeedsByRoom(r.nextElement(), stock, processor))
								updated=true;
					if(updated)
						CMLib.database().DBUpdatePlayerItem(i.first, I, i.third);
				}
			}
		}
	}

	private synchronized String getCode(final String areaName, String name)
	{
		final String letters="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
		@SuppressWarnings("unchecked")
		Map<String,String> names = (Map<String,String>)Resources.getResource("CMKT_AREA_DATA/"+areaName);
		if(names == null)
		{
			names = new TreeMap<String,String>();
			final List<PAData> dat = CMLib.database().DBReadAreaData(areaName, "CMKTDATA", "CMKTDATA/"+areaName);
			if((dat != null)&&(dat.size()>0))
			{
				final List<XMLTag> tags = CMLib.xml().parseAllXML(dat.get(0).xml());
				for(final XMLTag tag : tags)
				{
					if(tag.tag().equals("N"))
						names.put(tag.getParmValue("ID"),tag.value());
				}
			}
			Resources.submitResource("CMKT_AREA_DATA/"+areaName,names);
		}
		name = CMLib.english().removeArticleLead(name);
		name = CMStrings.removePunctuation(name).toLowerCase();
		if(names.containsKey(name))
			return names.get(name);
		final TreeSet<String> ids = new TreeSet<String>();
		for(final String n : names.keySet())
			ids.add(names.get(n));
		String cd = "";
		final List<String> words = CMParms.parseSpaces(name,true);
		for(int x=0;x<words.size()-1 && (cd.length()<2);x++)
			for(int y=x+1;y<words.size();y++)
			{
				final String c = (""+words.get(x).charAt(0)+words.get(y).charAt(0)).toUpperCase();
				if(!ids.contains(c))
				{
					cd = c;
					break;
				}
			}
		if(cd.length()==0)
		{
			final String word = words.get(0);
			for(int x=0;x<word.length()-1 && (cd.length()<2);x++)
				for(int y=x+1;y<word.length();y++)
				{
					final String c = (""+word.charAt(x)+word.charAt(y)).toUpperCase();
					if(!ids.contains(c))
					{
						cd = c;
						break;
					}
				}
			if(cd.length()==0)
				cd=word.substring(0,2).toUpperCase();
		}
		int x=0;
		while(ids.contains(cd))
			cd = ""+cd.charAt(0)+letters.charAt(x++);
		names.put(name, cd);
		final StringBuilder data = new StringBuilder("");
		for(final String key : names.keySet())
			data.append("<N ID=\""+names.get(key)+"\">").append(key).append("</N>");
		CMLib.database().DBReCreateAreaData(areaName, "CMKTDATA", "CMKTDATA/"+areaName,data.toString());
		return cd;
	}

	/**
	 * Configuration for a stock or set of stocks
	 */
	private class MarketConf
	{
		public int				updateDays				= 24;
		public int				waitDaysAfterBankruptcy	= 10;
		public int				maxStocks				= 10;
		public boolean			allowsClans				= true;
		public boolean			groupAreas				= false;
		public GroupBy			groupBy					= GroupBy.NOTHING;
		public String			nameMask				= "Stock in @x1 of @x2";
		public String			shopkeeperMaskStr		= "";
		public CompiledZMask	shopkeeperMask			= null;
		public String			areaMaskStr				= "";
		public CompiledZMask	areaMask				= null;
		public Integer			hash					= null;
		public boolean			playerInfluence			= false;
		public int				maxSharesPerStock		= Integer.MAX_VALUE;

		public final Map<String, InfluDir>				questMoves		= new STreeMap<String, InfluDir>();
		public final Set<ShopKeeper>					nonShops		= new SHashSet<ShopKeeper>();
		public final Map<ShopKeeper, List<StockDef>>	shopStocksMap	= new SHashtable<ShopKeeper, List<StockDef>>();
		public volatile TimeClock						nextUpdate;

		public MarketConf()
		{
			updateDays = 24;
			waitDaysAfterBankruptcy = 10;
			maxStocks = 10;
			nameMask = "Stock in @x1 of @x2";
			allowsClans = false;
			groupAreas = false;
			shopkeeperMaskStr = "";
			shopkeeperMask = null;
			questMoves.clear();
			for(final String posiQuest : new String[] { "newyear", "celebration", "party", "leadership"})
				questMoves.put(posiQuest, InfluDir.POSITIVE);
			for(final String negaQuest : new String[] { "revolt", "crimewave", "swindler", "brutality", "harsh winter", "famine", "drought"})
				questMoves.put(negaQuest, InfluDir.NEGATIVE);
			maxSharesPerStock = Integer.MAX_VALUE;
			areaMaskStr = "";
			this.areaMask = null;
			groupBy = GroupBy.NOTHING;
		}

		public MarketConf(final Map<String,String> props, MarketConf defaults)
		{
			final String marketTypeStr = props.getOrDefault("MARKETTYPE", "");
			final ShortcutMarketTypes mt = (ShortcutMarketTypes)CMath.s_valueOf(ShortcutMarketTypes.class, marketTypeStr);
			if(mt != null)
			{
				final Map<String,String> mapped = CMParms.parseEQParms(mt.parms);
				defaults = new MarketConf(mapped, defaults); // basically just copies defaults
			}
			updateDays = CMath.s_int(props.getOrDefault("UPDATEDAYS", ""+defaults.updateDays));
			waitDaysAfterBankruptcy = CMath.s_int(props.getOrDefault("WAITDAYAB", ""+defaults.waitDaysAfterBankruptcy));
			maxStocks = CMath.s_int(props.getOrDefault("MAXSTOCKS", ""+defaults.maxStocks));
			nameMask = props.getOrDefault("NAME", ""+defaults.nameMask);
			allowsClans = CMath.s_bool(props.getOrDefault("ALLOWCLANS", ""+defaults.allowsClans));
			groupAreas = CMath.s_bool(props.getOrDefault("AREAGROUP", ""+defaults.groupAreas));
			playerInfluence = CMath.s_bool(props.getOrDefault("PLAYINFLU", ""+defaults.playerInfluence));
			shopkeeperMaskStr = props.getOrDefault("SHOPMASK", ""+defaults.shopkeeperMaskStr);
			maxSharesPerStock = CMath.s_int(props.getOrDefault("MAXSHARES", ""+defaults.maxSharesPerStock));
			questMoves.clear();
			final String commas = props.getOrDefault("AREAEVENTS", defaults.flattenQuestMoves());
			for(final String entry : CMParms.parseCommas(commas, true))
			{
				if(entry.startsWith("+"))
					questMoves.put(entry.substring(1).toLowerCase(), InfluDir.POSITIVE);
				else
				if(entry.startsWith("-"))
					questMoves.put(entry.substring(1).toLowerCase(), InfluDir.NEGATIVE);
				else
					questMoves.put(entry.toLowerCase(), InfluDir.POSITIVE);
			}
			shopkeeperMask = (shopkeeperMaskStr.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(shopkeeperMaskStr);
			areaMaskStr = props.getOrDefault("AREAMASK", ""+defaults.areaMaskStr);
			areaMask = (areaMaskStr.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(areaMaskStr);
			final String groupByStr = props.getOrDefault("GROUPBY", ""+defaults.groupBy.name());
			final GroupBy gb = (GroupBy)CMath.s_valueOf(GroupBy.class, groupByStr);
			if(gb != null)
				groupBy=gb;
			nextUpdate=null;
		}

		private String flattenQuestMoves()
		{
			final StringBuilder str = new StringBuilder("");
			for(final String q : questMoves.keySet())
			{
				if(str.length()>0)
					str.append(",");
				final InfluDir dir = questMoves.get(q);
				switch(dir)
				{
				case NEGATIVE:
					str.append("-"+q);
					break;
				case POSITIVE:
					str.append("+"+q);
					break;
				case VARIABLE:
					str.append(q);
					break;
				}
			}
			return str.toString();
		}

		public boolean isApplicableArea(final Area A)
		{
			if(CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
				return false;
			return areaMask == null || (CMLib.masking().maskCheck(areaMask, A, true));
		}

		public List<StockDef> getShopStock(final ShopKeeper SK, final Environmental E)
		{
			if((SK == null)||(nonShops.contains(SK)))
				return null;
			final Room R = CMLib.map().roomLocation(E);
			if(R ==null)
				return null;
			final Area A = R.getArea();
			if(A ==null)
				return null;
			Area hostA=A;
			if(groupAreas)
				hostA = CMLib.map().areaLocation(host);
			if(hostA == null)
				return null;
			if(shopStocksMap.containsKey(SK))
				return shopStocksMap.get(SK);
			if(E instanceof MOB)
			{
				final Area sA = CMLib.map().getStartArea(E);
				if((sA == null)||(!hostA.inMyMetroArea(sA)))
				{
					nonShops.add(SK);
					return null;
				}
				if(((MOB)E).isPlayer())
				{
					if(!this.allowsClans)
					{
						nonShops.add(SK);
						return null;
					}
				}
			}
			if(shopkeeperMask != null && (!CMLib.masking().maskCheck(shopkeeperMask, E, true)))
			{
				nonShops.add(SK);
				return null;
			}
			if(!isApplicableArea(A))
			{
				nonShops.add(SK);
				return null;
			}
			final Collection<StockDef> stocks = getHostStocks();
			synchronized(stocks)
			{
				synchronized(shopStocksMap)
				{
					if(shopStocksMap.containsKey(SK))
						return shopStocksMap.get(SK);
					final ArrayList<StockDef> list = new ArrayList<StockDef>();
					switch(groupBy)
					{
					case NOTHING:
					{
						String name;
						if((E instanceof PhysicalAgent) && CMLib.flags().isMobile((PhysicalAgent)E))
							name = L(nameMask,E.name(),hostA.Name());
						else
							name = L(nameMask,R.displayText(),hostA.Name());
						final String id = "G"+getCode(hostA.Name(), E.name())+getCode("CMKT_AREA_CODES", hostA.Name());
						StockDef def = null;
						for(final Iterator<StockDef> d = stocks.iterator();d.hasNext();)
						{
							final StockDef def2 = d.next();
							if(def2.name.equals(name) && def2.area.equals(hostA.Name()))
								def=def2;
						}
						if(def == null)
						{
							if(stocks.size() >= maxStocks)
								return null;
							def = new StockDef(hostA.Name(),id,name);
							addHostStock(def);
						}
						list.add(def);
						break;
					}
					case RACE:
					{
						final String race =(E instanceof MOB)?((MOB)E).baseCharStats().raceName():L("Vendor");
						final String name = L(nameMask,race,hostA.Name());
						StockDef def = null;
						for(final Iterator<StockDef> d = stocks.iterator();d.hasNext();)
						{
							final StockDef def2 = d.next();
							if(def2.name.equals(name)&&def2.area.equals(hostA.Name()))
								def=def2;
						}
						if(def == null)
						{
							if(stocks.size() >= maxStocks)
								return null;
							final String id = "R"+getCode(hostA.Name(), race)+getCode("CMKT_AREA_CODES", hostA.Name());
							def = new StockDef(hostA.Name(), id, name);
							addHostStock(def);
						}
						list.add(def);
						break;
					}
					case SHOPTYPE:
					{
						final List<Integer> types = new ArrayList<Integer>();
						if(SK.getShop().isSold(ShopKeeper.DEAL_ANYTHING))
							types.add(Integer.valueOf(ShopKeeper.DEAL_ANYTHING));
						else
						for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
						{
							if(SK.getShop().isSold(d))
								types.add(Integer.valueOf(d));
						}
						for(final Integer typeCode : types)
						{
							final String type = CMLib.coffeeShops().localizedStoreItemTerm(typeCode.intValue());
							final String name = L(nameMask,type,hostA.Name());
							StockDef def = null;
							for(final Iterator<StockDef> d = stocks.iterator();d.hasNext();)
							{
								final StockDef def2 = d.next();
								if(def2.name.equals(name)&&def2.area.equals(hostA.Name()))
									def=def2;
							}
							if(def == null)
							{
								if(stocks.size() >= maxStocks)
									return null;
								final String id = "C"+getCode(hostA.Name(), type)+getCode("CMKT_AREA_CODES", hostA.Name());
								def = new StockDef(hostA.Name(),id,name);
								addHostStock(def);
							}
							list.add(def);
						}
						break;
					}
					case ALL:
						{
							final String name = L(nameMask,L("Shops"),hostA.Name());
							StockDef def = null;
							for(final Iterator<StockDef> d = stocks.iterator();d.hasNext();)
							{
								final StockDef def2 = d.next();
								if(def2.name.equals(name)&&def2.area.equals(hostA.Name()))
									def=def2;
							}
							if(def == null)
							{
								if(stocks.size() >= maxStocks)
									return null;
								final String id = "G"+getCode("CMKT_AREA_CODES", hostA.Name());
								def = new StockDef(hostA.Name(),id,name);
								addHostStock(def);
							}
							list.add(def);
						}
						break;
					}
					shopStocksMap.put(SK, list);
				}
				return shopStocksMap.get(SK);
			}
		}

		@Override
		public int hashCode()
		{
			if(this.hash == null)
			{
				this.hash = Integer.valueOf(Objects.hash(
						Integer.valueOf(updateDays),Integer.valueOf(waitDaysAfterBankruptcy),Integer.valueOf(maxStocks),
						Boolean.valueOf(allowsClans), Boolean.valueOf(groupAreas), groupBy, nameMask, shopkeeperMaskStr,
						areaMaskStr));
			}
			return this.hash.intValue();
		}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final MarketConf m = (MarketConf)o;
			return (m.updateDays==updateDays) && (m.waitDaysAfterBankruptcy==waitDaysAfterBankruptcy) && (m.maxStocks==maxStocks)
				&& (m.allowsClans == allowsClans) && (m.groupAreas == groupAreas) && (m.groupBy==groupBy) && Objects.equals(m.nameMask,nameMask)
				&& Objects.equals(m.shopkeeperMaskStr,shopkeeperMaskStr)&& Objects.equals(m.areaMaskStr,areaMaskStr);
		}
	}

	public int whichLegalDude(final String areaName, final MOB mob)
	{
		if(!legalCache.containsKey(areaName))
		{
			legalCache.put(areaName,new Pair<LegalBehavior,Area>(
				CMLib.law().getLegalBehavior(mob.location().getArea()),
				CMLib.law().getLegalObject(mob.location().getArea())
			));
		}
		final Pair<LegalBehavior,Area> chk = legalCache.get(areaName);
		if((chk !=null) && (chk.first != null))
		{
			if(chk.first.isAnyOfficer(chk.second, mob))
				return 1;
			if(chk.first.isJudge(chk.second, mob))
				return 2;
		}
		return 0;
	}

	/**
	 * Type flags that stock market builders can use to 'shortcut' various other flags into existence,
	 * instead of composing them all in parameters.
	 */
	private enum ShortcutMarketTypes
	{
		STOCK("GROUPBY=SHOPTYPE AREAGROUP=FALSE NAME=\"Stock in @x1 of @x2\" PLAYINFLU=FALSE "),
		REGIONAL("GROUPBY=ALL AREAGROUP=FALSE NAME=\"Regional Stock in @x1 of @x2\" PLAYINFLU=TRUE "),
		BOND("AREAMASK=\"-EFFECTS +Arrest +EFFECTS -Conquerable\"  AREAGROUP=FALSE GROUPBY=ALL NAME=\"@x1 of @x2 Bond\"  PLAYINFLU=TRUE "),
		COMMODITY("GROUPBY=SHOPTYPE AREAGROUP=TRUE NAME=\"Commodity: @x1 of @x2\" PLAYINFLU=FALSE "),
		RACIAL("GROUPBY=RACE AREAGROUP=TRUE NAME=\"@x1 of @x2 Racial Stock\" PLAYINFLU=FALSE "),
		PLAYER("ALLOWCLANS=TRUE SHOPMASK=\"-NPC\" GROUPBY=NOTHING NAME=\"Clan Stock: @x1 of @x2\" PLAYINFLU=FALSE ")
		;
		public String parms = "";
		private ShortcutMarketTypes(final String pms)
		{
			parms=pms;
		}
	}

	/**
	 * As a StockMarket behavior covers an area or group of areas, it may include a lot of different types
	 * of markets composed of different stocks.  Each one of these markets  is a MarketConf (config).
	 */
	private final List<MarketConf> configs = Collections.synchronizedList(new LinkedList<MarketConf>());

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_AREAS;
	}

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		if (forMe != null)
			host = forMe;
		CMLib.map().addGlobalHandler(this, CMMsg.TYP_GIVE);
		CMLib.map().addGlobalHandler(this, CMMsg.TYP_QUESTSTART);
	}

	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
		final Set<ShopKeeper> removeFroms = new XHashSet<ShopKeeper>(this.stockbrokers);
		this.stockbrokers.clear();
		for(final ShopKeeper SK : removeFroms)
			SK.getShop().delShopProvider(shopProvider);
		CMLib.map().delGlobalHandler(this, CMMsg.TYP_GIVE);
		CMLib.map().delGlobalHandler(this, CMMsg.TYP_QUESTSTART);
		super.endBehavior(forMe);
	}

	@Override
	public List<String> externalFiles()
	{
		final List<String> externalFiles = new ArrayList<String>();
		for(final CMFile F : getConfigFiles(CMParms.parseEQParms(getParms())))
			externalFiles.add(F.getAbsolutePath());
		return externalFiles;
	}

	private boolean hostReady()
	{
		if(!(host instanceof Area))
			return false;
		return true;
	}

	private synchronized List<CMFile> getConfigFiles(final Map<String,String> mapped)
	{
		final List<CMFile> files = new ArrayList<CMFile>();
		final String configFiles = mapped.getOrDefault("CONF", mapped.getOrDefault("CONFS",mapped.getOrDefault("CONFIG",mapped.getOrDefault("CONFIGS",""))));
		if(configFiles.length()>0)
		{
			for(final String fileName : CMParms.parseCommas(configFiles, true))
			{
				final CMFile F = new CMFile(fileName,null,0);
				if(F.exists() && F.canRead())
					files.add(F);
			}
		}
		return files;
	}

	private synchronized void configure()
	{
		if((configs.size()>0) || (!hostReady()))
			return;
		if(getParms().length()==0)
		{
			final TreeMap<String,String> parms = new TreeMap<String,String>();
			parms.put("MARKETTYPE",StockMarket.ShortcutMarketTypes.STOCK.name());
			final MarketConf base = new MarketConf(parms,new MarketConf());
			configs.add(base);
			return;
		}
		final Map<String,String> mapped = CMParms.parseEQParms(getParms());
		journalName = "";
		if(mapped.containsKey("JOURNAL"))
			journalName=mapped.get("JOURNAL");
		final MarketConf base = new MarketConf(mapped,new MarketConf());
		for(final CMFile F : getConfigFiles(mapped))
		{
			final List<String> lines = Resources.getFileLineVector(F.text());
			final ByteArrayInputStream in = new ByteArrayInputStream(CMParms.combine(lines,'\n').getBytes());
			final Properties props = new Properties();
			try
			{
				props.load(in);
				configs.add(new MarketConf(mapped, base));
			}
			catch (final IOException e)
			{
			}
		}
		if(configs.size()==0)
			configs.add(base);
	}

	@Override
	public void setParms(final String parameters)
	{
		super.setParms(parameters);
		configs.clear();
		configure();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		configure();
		if((configs.size()==0) || (!hostReady()))
			return true;
		if(--weatherDown <=0)
		{
			weatherDown=Climate.WEATHER_TICK_DOWN;
			if(host instanceof Area)
			{
				final Area hostA = (Area)host;
				final Set<String> weatherAreas = new HashSet<String>();
				final List<Area> children = ((Area)host).getChildrenRecurse();
				children.add(hostA);
				for(final Area A : children)
				{
					switch(A.getClimateObj().weatherType(null))
					{
					case Climate.WEATHER_BLIZZARD:
					case Climate.WEATHER_DROUGHT:
					case Climate.WEATHER_DUSTSTORM:
					case Climate.WEATHER_HAIL:
					case Climate.WEATHER_SLEET:
					case Climate.WEATHER_THUNDERSTORM:
						weatherAreas.add(A.Name());
						break;
					default:
						break;
					}
				}
				final Collection<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if(weatherAreas.contains(def.area))
							def.addInfluence(InfluCat.WEATHER, InfluDir.NEGATIVE, 1);
					}
				}
			}
		}
		boolean tryProcessPlease=false;
		if(host instanceof Area)
		{
			final TimeClock now = ((Area)host).getTimeObj();
			for(final MarketConf conf : configs)
			{
				if(conf.nextUpdate == null)
				{
					conf.nextUpdate=(TimeClock)((Area)host).getTimeObj().copyOf();
					conf.nextUpdate.bump(TimePeriod.DAY, conf.updateDays);
				}
				if(conf.nextUpdate.isAfter(now))
					continue;  // we have not arrived at the time yet
				tryProcessPlease=true;
			}
		}
		if(tryProcessPlease)
		{
			if(!processing.getAndSet(true))
			{
				CMLib.threads().executeRunnable(processStockMarket); // will reset processing when done
			}
		}
		return true;
	}

	public Runnable processStockMarket = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				final TimeClock now = ((Area)host).getTimeObj();
				boolean resave=false;
				final Set<String> archonNames = new TreeSet<String>();
				for(final ThinPlayer archonPlayer : CMLib.players().getArchonUserList())
					archonNames.add(archonPlayer.name());
				final Set<StockDef> done = new HashSet<StockDef>();
				for(final MarketConf conf : configs)
				{
					if(conf.nextUpdate.isAfter(now))
						continue;  // we have not arrived at the time yet
					resave=true;
					// calculate final
					final Map<ShopKeeper,List<StockDef>> shopStocks;
					synchronized(conf.shopStocksMap)
					{
						shopStocks = new HashMap<ShopKeeper,List<StockDef>>();
						for(final ShopKeeper SK : conf.shopStocksMap.keySet())
						{
							final List<StockDef> stockSnapshot=new XArrayList<StockDef>(conf.shopStocksMap.get(SK));
							shopStocks.put(SK, stockSnapshot);
						}
					}
					boolean racialAstrological = false;
					if(conf.shopkeeperMask != null)
					{
						final Set<Race> requiredRaces = CMLib.masking().getRequiredRaces(conf.shopkeeperMask);
						if(requiredRaces.size()>0)
						{
							final MOB M = CMClass.getFactoryMOB("testmob", 1, ((Area)host).getRandomProperRoom());
							try
							{
								M.setStartRoom(M.location());
								M.baseCharStats().setMyRace(requiredRaces.iterator().next());
								M.charStats().setMyRace(M.baseCharStats().getMyRace());
								CMLib.awards().giveAutoProperties(M, false);
								final Ability awardA = M.fetchEffect("AutoAwards");
								final String awardList = (awardA==null)?"":awardA.getStat("AUTOAWARDS");
								if(awardList.length()>0)
								{
									final Set<Integer> currentSet = new TreeSet<Integer>();
									for(final String s : awardList.split(";"))
										currentSet.add(Integer.valueOf(CMath.s_int(s)));
									for(final Enumeration<AutoProperties> p = CMLib.awards().getAutoProperties();p.hasMoreElements();)
									{
										final AutoProperties P = p.nextElement();
										if((P.getProps() != null)
										&&(P.getProps().length>0)
										&&(currentSet.contains(Integer.valueOf(P.hashCode())))
										&&(P.getPlayerMask()!=null)
										&&(P.getPlayerMask().length()>0))
										{
											final Set<Race> awardReqRaces = CMLib.masking().getRequiredRaces(P.getPlayerCMask());
											if((awardReqRaces.size()>0) && (awardReqRaces.retainAll(requiredRaces)))
											{
												racialAstrological=true;
												break;
											}
										}
									}
								}
							}
							finally
							{
								M.destroy();
							}
						}
					}
					for(final ShopKeeper SK : shopStocks.keySet())
					{
						final String currency;
						boolean astrological = false;
						if(SK instanceof MOB)
						{
							currency = CMLib.beanCounter().getCurrency(SK);
							CMLib.awards().giveAutoProperties((MOB)SK, false);
							final Ability awardA = ((MOB)SK).fetchEffect("AutoAwards");
							if((awardA != null) && (awardA.getStat("AUTOAWARDS").length()>0))
								astrological=true;
						}
						else
							currency = CMLib.beanCounter().getCurrency(host);
						for(final StockDef def : shopStocks.get(SK))
						{
							if(done.contains(def))
								continue;
							done.add(def);
							if(def.bankruptUntil != null)
							{
								if(now.isAfter(def.bankruptUntil))
								{
									def.bankruptUntil = null;
									def.price=100.0;
									def.version++;
								}
								else
									continue; // ignore bankrupt stocks
							}
							if(astrological)
								def.addInfluence(InfluCat.ASTROLOGICAL_INFLUENCES, InfluDir.VARIABLE, 1);
							if(racialAstrological)
								def.addInfluence(InfluCat.RACIAL_BOONS, InfluDir.VARIABLE, 1);
							final PairList<String,Integer> owners = getStockOwners(def);
							int numArchons = 0;
							for(final Pair<String,Integer> p : owners)
							{
								if(archonNames.contains(p.first))
									numArchons++;
							}
							if(numArchons == 0)
								def.addInfluence(InfluCat.HOLDINGS_, InfluDir.NEGATIVE, 1);
							if(owners.size() - numArchons > 0)
								def.addInfluence(InfluCat.HOLDINGS, InfluDir.POSITIVE, 1);
							double price = def.getPrice();
							double dividendMultiplier = 0.0;
							final DiceLibrary dice = CMLib.dice();
							final Set<String> influenceList = new TreeSet<String>();
							for(final InfluCat cat : def.influences.keySet())
							{
								for(final InfluDir dir : InfluDir.values())
								{
									final int rolls = cat.rolls(def.influences.get(cat)[dir.ordinal()]);
									if((rolls > 0)&&(journalName.length()>0))
										influenceList.add(cat.name().toLowerCase().replace('_', ' ').trim());
									for(int r=0;r<rolls;r++)
									{
										final double roll = dice.rollPercentage();
										int index = (int)Math.round((roll + def.manipulation + 95.0) / 5.0);
										index = CMath.minMax(0, index, THE_TABLE.length-1);
										final double[] dirChart = StockMarket.THE_TABLE[index];
										final double value = dirChart[dir.ordinal()];
										if((value>0.0)&&(value<1.0)) // is percentage for dividends, no negative dividends
											dividendMultiplier += (value/100.0);
										else
										if(value < 0) // must be negative whole price
											price -= dice.roll(1, -(int)Math.round(value), 0);
										else
											price += dice.roll(1, (int)Math.round(value), 0);
									}
								}
							}
							def.influences.clear();
							if((journalName.length()>0)&&(influenceList.size()>0))
								CMLib.database().DBWriteJournal(journalName,"StockMarket","ALL",L("@x1 influences: @x2",def.name(),CMParms.toListString(influenceList)),L("See the subject line."));
							if(price < 0.0) // GO BANKRUPT!
							{
								def.price = 0.0;
								getOutstandingShares(def, 0); // cache them
								getOutstandingShares(def, -def.outstandingShares);//effectively negates them
								final TimeClock untilTime=(TimeClock)now.copyOf();
								untilTime.bump(TimeClock.TimePeriod.DAY, conf.waitDaysAfterBankruptcy);
								def.bankruptUntil = untilTime;
								if(journalName.length()>0)
									CMLib.database().DBWriteJournal(journalName,"StockMarket","ALL",L("@x1 goes bankrupt!",def.name()),L("See the subject line."));
								CMLib.database().DBDeletePlayerSectionKeyData("STOCKMARKET_STOCKS", def.getTitleID());
								processDeeds(def, new DeedProcessor()
								{
									final String titleID = def.getTitleID();
									@Override
									public boolean process(final Item I)
									{
										if((I instanceof PrivateProperty)
										&&(((PrivateProperty)I).getTitleID().equals(titleID)))
										{
											((PrivateProperty)I).setPrice(0);
											return true;
										}
										return false;
									}

								});
							}
							else
							{
								//final boolean changed = def.price != price;
								final double priceDelta = price - def.price;
								def.price = price; // normal price change
								if(journalName.length()>0)
								{
									if(priceDelta != 0.0)
									{
										final String priceStr = CMLib.beanCounter().abbreviatedPrice(currency, price);
										final String deltaStr = CMLib.beanCounter().abbreviatedPrice(currency, Math.abs(priceDelta));
										if(priceDelta > 0)
											CMLib.database().DBWriteJournal(journalName,"StockMarket","ALL",L("@x1 up @x2 to @x3!",def.name(),deltaStr,priceStr),L("See the subject line."));
										else
											CMLib.database().DBWriteJournal(journalName,"StockMarket","ALL",L("@x1 down @x2 to @x3!",def.name(),deltaStr,priceStr),L("See the subject line."));
									}
								}
								if(dividendMultiplier > 0.0)
								{
									final double dividend = CMath.mul(dividendMultiplier, def.price);
									if(journalName.length()>0)
									{
										final String dividendAmt = (dividendMultiplier * 100.0)+"%";
										CMLib.database().DBWriteJournal(journalName,"StockMarket","ALL",L("@x1 pays a @x2 dividend per share.",def.name(),dividendAmt),L("See the subject line."));
									}
									for(final Pair<String,Integer> p : getStockOwners(def))
										CMLib.beanCounter().modifyLocalBankGold((Area)host, p.first, currency, CMath.mul(dividend, p.second.intValue()));
								}
								if((def.price > 200.0)
								&& (CMLib.dice().rollPercentage() <= 20)
								&& (conf.maxSharesPerStock < Integer.MAX_VALUE/2))
								{
									conf.maxSharesPerStock *= 2.0;
									def.price = def.price / 2.0;
									getOutstandingShares(def, 0);
									getOutstandingShares(def, def.outstandingShares);//effectively doubles them
									for(final Pair<String,Integer> p : getStockOwners(def))
									{
										updatePlayerStockXML(p.first, def, p.second.intValue());
										processDeeds(def, new DeedProcessor()
										{
											final String titleID = def.getTitleID();
											final int newPrice = (int)Math.round(def.price);
											@Override
											public boolean process(final Item I)
											{
												if((I instanceof PrivateProperty)
												&&(I instanceof AutoBundler)
												&&(((PrivateProperty)I).getTitleID().equals(titleID)))
												{
													((PrivateProperty)I).setPrice(newPrice);
													((AutoBundler)I).setBundleSize(((AutoBundler)I).getBundleSize()*2);
													return true;
												}
												return false;
											}
										});
									}
								}
							}
						}
					}
					conf.nextUpdate=(TimeClock)now.copyOf();
					conf.nextUpdate.bump(TimePeriod.DAY, conf.updateDays);
				}
				if(resave)
					savetHostStockXML(getHostStocks());
			}
			finally
			{
				processing.set(false);
			}
		}
	};

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_LIST)
		{
			final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper(msg.target());
			if((SK != null)
			&&(SK.isSold(ShopKeeper.DEAL_STOCKBROKER))
			&&(!SK.getShop().hasShopProvider(shopProvider.ID())))
			{
				SK.getShop().addShopProvider(shopProvider);
				stockbrokers.add(SK);
			}
		}
		else
		if(((msg.targetMinor()==CMMsg.TYP_SELL)||(msg.targetMinor()==CMMsg.TYP_VALUE))
		&&(msg.target() instanceof ShopKeeper)
		&&(msg.tool() instanceof PrivateProperty)
		&&(msg.tool().ID().equals("GenCertificate")))
		{
			StockDef foundDef=null;
			boolean worthless = false;
			if(((ShopKeeper)msg.target()).isSold(ShopKeeper.DEAL_STOCKBROKER))
			{
				final String sellingTitleID = ((PrivateProperty)msg.tool()).getTitleID();
				final String subsellingTitleID;
				if(sellingTitleID.indexOf('#')>0)
					subsellingTitleID = sellingTitleID.substring(0,sellingTitleID.lastIndexOf('#')+1);
				else
					subsellingTitleID = sellingTitleID;
				final Collection<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if(def.getTitleID().equals(sellingTitleID))
						{
							((PrivateProperty)msg.tool()).setPrice(def.getPrice() );
							foundDef=def;
							break;
						}
						else
						if(def.getTitleID().startsWith(subsellingTitleID))
							worthless=true;
					}
				}
			}
			if(worthless)
			{
				if(msg.target() instanceof MOB)
					CMLib.commands().postSay((MOB)msg.target(), L("Sorry, but those are worthless now."));
				else
					msg.source().tell(L("Those are worthless now."));
				return false;
			}
			if(foundDef==null)
			{
				if(msg.target() instanceof MOB)
					CMLib.commands().postSay((MOB)msg.target(), L("Sorry, but I don't deal in those."));
				else
					msg.source().tell(L("You can't sell that here."));
				return false;
			}
			final int amtOwned = this.getStocksOwned(msg.source(), foundDef);
			if(amtOwned ==0)
			{
				if(msg.target() instanceof MOB)
					CMLib.commands().postSay((MOB)msg.target(), L("Sorry, but those are worthless."));
				else
					msg.source().tell(L("Those are worthless now."));
				return false;
			}
			if((msg.target() instanceof Item)
			&&(msg.target() instanceof AutoBundler)
			&&(((AutoBundler)msg.target()).getBundleSize()>amtOwned))
				((AutoBundler)msg.target()).setBundleSize(amtOwned);
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		configure();
		if((configs.size()==0) || (!hostReady()))
			return;

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_GIVE:
			if((msg.tool() instanceof PrivateProperty)
			&&(msg.tool() instanceof AutoBundler)
			&&(msg.target() instanceof MOB)
			&&(msg.tool().ID().equals("GenCertificate")))
			{
				final PrivateProperty cert = (PrivateProperty)msg.tool();
				final Map<String,StockDef> stocksMap = this.getHostStocksMap();
				StockDef def = null;
				synchronized(stocksMap)
				{
					final String itemTitleID = cert.getTitleID();
					def = stocksMap.get(itemTitleID);
					if(def == null)
						def = stocksMap.get(cert.name());
					if(def == null)
					{
						for(final String key : stocksMap.keySet())
							if(itemTitleID.equals(key))
							{
								def=stocksMap.get(key);
								break;
							}
					}
				}
				if((def != null) && (!lastGives.contains(msg)))
				{
					lastGives.add(msg);
					final int amt = ((AutoBundler)msg.tool()).getBundleSize();
					final String oldOwner = msg.source().name();
					final String newOwner = msg.target().name();
					this.updatePlayerStockXML(oldOwner, def, -amt);
					this.updatePlayerStockXML(newOwner, def, amt);
				}
			}
			break;
		case CMMsg.TYP_LIFE:
			if (msg.source().isMonster())
			{
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.source());
				if(SK != null)
				{
					CMLib.awards().giveAutoProperties(msg.source(), false);
					for(final MarketConf conf : configs)
						conf.getShopStock(SK, msg.source());
				}
			}
			break;
		case CMMsg.TYP_JUSTICE:
			if((msg.target() instanceof MOB)
			&&(msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_THIEF_SKILL)
			&&((((Ability)msg.tool())).abstractQuality()==Ability.QUALITY_MALICIOUS))
			{
				final MOB targM = (MOB)msg.target();
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(targM);
				if(SK != null)
				{
					for(final MarketConf conf : configs)
					{
						final List<StockDef> stock = conf.getShopStock(SK, msg.source());
						if(stock!=null)
						{
							for(final StockDef def : stock)
								def.addInfluence(InfluCat.THIEVERY, InfluDir.NEGATIVE, 1);
						}
					}
				}
			}
			break;
		case CMMsg.TYP_LEGALWARRANT:
			if(msg.tool() instanceof MOB)
			{
				final MOB targM = (MOB)msg.target();
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(targM);
				if(SK != null)
				{
					for(final MarketConf conf : configs)
					{
						final List<StockDef> stock = conf.getShopStock(SK, msg.source());
						if(stock!=null)
						{
							for(final StockDef def : stock)
								def.addInfluence(InfluCat.ROBBERIES, InfluDir.NEGATIVE, 1);
						}
					}
				}
			}
			break;
		case CMMsg.TYP_LEVEL:
		{
			final boolean unlevel = msg.value() == msg.source().basePhyStats().level()-1;
			final Area A = CMLib.map().areaLocation(msg.source());
			if((A==null)||(!(host instanceof Area)))
				break;
			final String areaName = A.Name();
			final Set<StockDef> done = new HashSet<StockDef>();
			for(final MarketConf conf : configs)
			{
				if(!conf.playerInfluence)
					continue;
				final Collection<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
						&&(!done.contains(def)))
						{
							def.addInfluence(InfluCat.LEVELING, unlevel?InfluDir.NEGATIVE:InfluDir.POSITIVE, 1);
							done.add(def);
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_QUESTSTART: // influences from area events come from holidays, e.g. Quests.
		{
			final Area A = CMLib.map().areaLocation(msg.source());
			if((A==null)||(!(host instanceof Area))||(msg.sourceMessage()==null))
				break;
			final String areaName = A.Name();
			final Set<StockDef> done = new HashSet<StockDef>();
			for(final MarketConf conf : configs)
			{
				final InfluDir dir = conf.questMoves.get(msg.sourceMessage().toLowerCase().trim());
				if(dir == null)
					continue;
				final Collection<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
						&&(!done.contains(def)))
						{
							def.addInfluence(InfluCat.AREA_EVENTS, dir, 1);
							done.add(def);
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_ACHIEVE:
		{
			final Area A = CMLib.map().areaLocation(msg.source());
			if((A==null)||(!(host instanceof Area)))
				break;
			final String areaName = A.Name();
			if((msg.target() instanceof Room)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().startsWith("E:")))
			{
				final Event ev = (Event)CMath.s_valueOf(Event.class,msg.targetMessage().substring(2));
				if((ev != null) && EVENTS_LISTEN.contains(ev))
				{
					final Set<StockDef> done = new HashSet<StockDef>();
					for(final MarketConf conf : configs)
					{
						if(!conf.playerInfluence)
							continue;
						final Collection<StockDef> stocks = getHostStocks();
						synchronized(stocks)
						{
							for(final StockDef def : stocks)
							{
								if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
								&&(!done.contains(def)))
								{
									done.add(def);
									switch(ev)
									{
									case QUESTOR:
										def.addInfluence(InfluCat.QUESTING, InfluDir.VARIABLE, 1);
										break;
									case BIRTHS:
										def.addInfluence(InfluCat.BIRTHS, InfluDir.POSITIVE, 1);
										break;
									case CONQUEREDAREAS:
										def.addInfluence(InfluCat.CONQUEST, InfluDir.NEGATIVE, 1);
										break;
									default:
										break;
									}
								}
							}
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_ITEMGENERATED:
		{
			if((msg.tool() instanceof Ability)
			&&(msg.target() instanceof Room)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL))
			{
				final Area A = CMLib.map().areaLocation(msg.source());
				if((A==null)||(!(host instanceof Area)))
					break;
				final String areaName = A.Name();
				final Set<StockDef> done = new HashSet<StockDef>();
				for(final MarketConf conf : configs)
				{
					if(!conf.playerInfluence)
						continue;
					final Collection<StockDef> stocks = getHostStocks();
					synchronized(stocks)
					{
						for(final StockDef def : stocks)
						{
							if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
							&&(!done.contains(def)))
							{
								def.addInfluence(InfluCat.BUILDING, InfluDir.POSITIVE, 1);
								done.add(def);
							}
						}
					}
				}
			}
			else
			if((msg.tool() instanceof Ability)
			&&(msg.target() instanceof Item)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
			{
				if((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_GATHERINGSKILL)
				{
					for(final MarketConf conf : configs)
					{
						if(!conf.playerInfluence)
							continue;
						synchronized(conf.shopStocksMap)
						{
							for(final ShopKeeper SK : conf.shopStocksMap.keySet())
							{
								if(SK.doISellThis(msg.target()))
								{
									for(final StockDef def : conf.shopStocksMap.get(SK))
										def.addInfluence(InfluCat.GATHERING, InfluDir.VARIABLE, 1);
								}
							}
						}
					}
				}
				else
				if((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
				{
					for(final MarketConf conf : configs)
					{
						if(!conf.playerInfluence)
							continue;
						synchronized(conf.shopStocksMap)
						{
							for(final ShopKeeper SK : conf.shopStocksMap.keySet())
							{
								if(SK.doISellThis(msg.target()))
								{
									for(final StockDef def : conf.shopStocksMap.get(SK))
										def.addInfluence(InfluCat.CRAFTING, InfluDir.VARIABLE, 1);
								}
							}
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_SELL:
		case CMMsg.TYP_BUY:
		{
			if(msg.target() instanceof MOB)
			{
				final MOB mob=(MOB)msg.target();
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(mob);
				if((SK != null)
				&&(msg.source().isPlayer()))
				{
					final String id;
					if(msg.tool() instanceof PrivateProperty)
						id = ((PrivateProperty)msg.tool()).getTitleID();
					else
						id=null;
					for(final MarketConf conf : configs)
					{
						final List<StockDef> stocks = conf.getShopStock(SK, mob);
						if(stocks != null)
						{
							for(final StockDef def : stocks)
							{
								if((id != null)
								&& (def.getTitleID().equals(id)))
								{
									if(msg.tool() instanceof AutoBundler)
									{
										final int num = ((AutoBundler)msg.tool()).getBundleSize();
										updatePlayerStockXML(msg.source().Name(),def,(msg.targetMinor()==CMMsg.TYP_BUY)?num:-num);
										getOutstandingShares(def,(msg.targetMinor()==CMMsg.TYP_BUY)?num:-num);
									}
									else
									{
										updatePlayerStockXML(msg.source().Name(),def,(msg.targetMinor()==CMMsg.TYP_BUY)?1:-1);
										getOutstandingShares(def,(msg.targetMinor()==CMMsg.TYP_BUY)?1:-1);
									}
									def.addInfluence(InfluCat.SHOPPING, InfluDir.VARIABLE, 1);
								}
								def.addInfluence(InfluCat.SHOPPING, InfluDir.POSITIVE, 1); //doing non-stock deed busines is positive
							}
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_SHUTDOWN:
			savetHostStockXML(this.getHostStocks());
			break;
		case CMMsg.TYP_DEATH:
			if(msg.source().isMonster())
			{
				final Area A = CMLib.map().areaLocation(msg.source());
				if((A==null)||(!(host instanceof Area)))
					break;
				final String areaName = A.Name();
				final int amt = this.whichLegalDude(areaName, msg.source());
				if(amt != 0)
				{
					final InfluCat cat = (amt == 2) ? InfluCat.JUDGE_DEATHS : InfluCat.OFFICER_DEATHS;
					final Set<StockDef> done = new HashSet<StockDef>();
					for(final MarketConf conf : configs)
					{
						final Collection<StockDef> stocks = getHostStocks();
						synchronized(stocks)
						{
							for(final StockDef def : stocks)
							{
								if((areaName.equals(def.area)||(conf.groupAreas && (conf.isApplicableArea(A))))
								&&(!done.contains(def)))
								{
									def.addInfluence(cat, InfluDir.NEGATIVE, amt);
									done.add(def);
								}
							}
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}
}
