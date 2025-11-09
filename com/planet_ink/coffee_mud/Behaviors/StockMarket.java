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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

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

	public PhysicalAgent	host					= null;
	private volatile int	weatherDown				= Climate.WEATHER_TICK_DOWN;

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
	 * Categories of influence scores
	 */
	public static enum InfluType
	{
		POSITIVE,
		NEGATIVE,
		VARIABLE
	}
	/**
	 * A single stock definition, which is tracked
	 * and based on db records.
	 */
	private class StockDef
	{
		public final String ID;
		public final String name;
		public final String area;
		public volatile double price = 100.0;
		public volatile double manipulation = 0;
		public STreeMap<String,int[]> influences = new STreeMap<String,int[]>();

		public StockDef(final Area hostA, final String id, final String name)
		{
			this.ID = id;
			this.name = name;
			this.area = hostA.Name();
		}

		public synchronized void addInfluence(final String type, final InfluType which, final int amt)
		{
			if(!influences.containsKey(type.toUpperCase()))
				influences.put(type.toUpperCase(), new int[InfluType.values().length]);
			influences.get(type.toUpperCase())[which.ordinal()] += amt;
		}
	}

	private void savetHostStockXML(final Set<StockDef> stocks)
	{
		final XMLLibrary xmlLib = CMLib.xml();
		final StringBuilder xml = new StringBuilder("");
		for(final StockDef def : stocks)
		{
			xml.append("<S ID=\""+def.ID+"\" NAME=\""+xmlLib.parseOutAngleBracketsAndQuotes(def.name)+"\" "
					+ "M="+def.manipulation+" A=\""+xmlLib.parseOutAngleBracketsAndQuotes(def.area)+"\" ");
			if(def.influences.size()==0)
				xml.append(" />");
			else
			{
				xml.append(">");
				for(final String cat : def.influences.keySet())
				{
					final int[] vals = def.influences.get(cat);
					for(final InfluType typ : InfluType.values())
						if(vals[typ.ordinal()]!=0)
							xml.append("<I TYP="+typ.ordinal()+" AMT="+vals[typ.ordinal()]+" />");
				}
				xml.append("</S>");
			}
		}
		final String areaName = host.Name();
		CMLib.database().DBReCreateAreaData(areaName, "CMKTSTOCKS", "CMKTSTOCKS/"+areaName,xml.toString());
	}

	private synchronized Set<StockDef> getHostStocks()
	{
		final String areaName = host.Name();
		@SuppressWarnings("unchecked")
		Set<StockDef> stocks = (Set<StockDef>)Resources.getResource("CMKT_AREA_STOCKS/"+areaName);
		if(stocks == null)
		{
			stocks = new SHashSet<StockDef>();
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
						final Area hostA = CMLib.map().getArea(CMLib.xml().restoreAngleBrackets(tag.getParmValue("A")));
						if(hostA != null)
						{
							final StockDef d = new StockDef(hostA, ID, name);
							d.price=price;
							d.manipulation=manipulation;
							for(final XMLTag itag : tag.contents())
								if(itag.tag().equals("I"))
								{
									final InfluType typ = InfluType.values()[CMath.s_int(tag.getParmValue("TYP"))];
									d.addInfluence(areaName, typ, CMath.s_int(tag.getParmValue("AMT")));
								}
							stocks.add(d);
						}
					}
				}
			}
			Resources.submitResource("CMKT_AREA_DATA/"+areaName,stocks);
		}
		return stocks;
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
			shopkeeperMaskStr = props.getOrDefault("SHOPMASK", ""+defaults.shopkeeperMaskStr);
			shopkeeperMask = (shopkeeperMaskStr.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(shopkeeperMaskStr);
			areaMaskStr = props.getOrDefault("AREAMASK", ""+defaults.areaMaskStr);
			areaMask = (areaMaskStr.trim().length() == 0) ? null : CMLib.masking().getPreCompiledMask(areaMaskStr);
			final String groupByStr = props.getOrDefault("GROUPBY", ""+defaults.groupBy.name());
			final GroupBy gb = (GroupBy)CMath.s_valueOf(GroupBy.class, groupByStr);
			if(gb != null)
				groupBy=gb;
			if(host instanceof Area)
			{
				nextUpdate=(TimeClock)((Area)host).getTimeObj().copyOf();
				nextUpdate.bump(TimePeriod.DAY, updateDays);
			}
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
			final Set<StockDef> stocks = getHostStocks();
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
							if(def2.name.equals(name)&&def2.area.equals(hostA.Name()))
								def=def2;
						}
						if(def == null)
						{
							def = new StockDef(hostA,id,name);
							stocks.add(def);
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
							final String id = "R"+getCode(hostA.Name(), race)+getCode("CMKT_AREA_CODES", hostA.Name());
							def = new StockDef(hostA, id, name);
							stocks.add(def);
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
								final String id = "C"+getCode(hostA.Name(), type)+getCode("CMKT_AREA_CODES", hostA.Name());
								def = new StockDef(hostA,id,name);
								stocks.add(def);
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
								final String id = "G"+getCode("CMKT_AREA_CODES", hostA.Name());
								def = new StockDef(hostA,id,name);
								stocks.add(def);
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

	private enum ShortcutMarketTypes
	{
		STOCK("GROUPBY=SHOPTYPE AREAGROUP=FALSE NAME=\"Stock in @x1 of @x2\" "),
		REGIONAL("GROUPBY=ALL AREAGROUP=FALSE NAME=\"Regional Stock in @x1 of @x2\" "),
		BOND("AREAMASK=\"-EFFECTS +Arrest +EFFECTS -Conquerable\"  AREAGROUP=FALSE GROUPBY=ALL NAME=\"@x1 of @x2 Bond\" "),
		COMMODITY("GROUPBY=SHOPTYPE AREAGROUP=TRUE NAME=\"Commodity: @x1 of @x2\" "),
		RACIAL("GROUPBY=RACE AREAGROUP=TRUE NAME=\"@x1 of @x2 Racial Stock\" "),
		PLAYER("ALLOWCLANS=TRUE SHOPMASK=\"-NPC\" GROUPBY=NOTHING NAME=\"Clan Stock: @x1 of @x2\" ")
		;
		public String parms = "";
		private ShortcutMarketTypes(final String pms)
		{
			parms=pms;
		}
	}

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
	}

	@Override
	public void endBehavior(final PhysicalAgent forMe)
	{
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
		if((configs.size()>0) || (getParms().length()==0) || (!hostReady()))
			return;
		final Map<String,String> mapped = CMParms.parseEQParms(getParms());
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
				final Set<StockDef> stocks = getHostStocks();
				synchronized(stocks)
				{
					for(final StockDef def : stocks)
					{
						if(weatherAreas.contains(def.area))
							def.addInfluence("W", InfluType.NEGATIVE, 1);
					}
				}
			}
		}
		final TimeClock now = ((Area)host).getTimeObj();
		for(final MarketConf conf : configs)
		{
			if(conf.nextUpdate.isBefore(now))
				continue;
			conf.nextUpdate=(TimeClock)now.copyOf();
			conf.nextUpdate.bump(TimePeriod.DAY, conf.updateDays);
		}
		return true;
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
		case CMMsg.TYP_LIFE:
			if(msg.source().isMonster())
			{
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.source());
				if(SK != null)
				{
					for(final MarketConf conf : configs)
						conf.getShopStock(SK, msg.source());
				}
			}
			break;
		case CMMsg.TYP_SELL:
		case CMMsg.TYP_BUY:
		{
			if(msg.source().isMonster())
			{
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.source());
				if(SK != null)
				{
					for(final MarketConf conf : configs)
					{
						final List<StockDef> stocks = conf.getShopStock(SK, msg.source());
						if(stocks != null)
						{
							for(final StockDef def : stocks)
								def.addInfluence("S", InfluType.POSITIVE, 1);
						}
					}
				}
			}
			break;
		}
		case CMMsg.TYP_SHUTDOWN:
			savetHostStockXML(this.getHostStocks());
			break;
		default:
			break;
		}
	}
}
