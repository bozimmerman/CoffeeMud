package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.ShoppingLibrary.BuySellFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Librarian.CheckedOutRecord;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import javax.swing.text.html.HTML.Tag;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class StdCraftBroker extends StdShopKeeper implements CraftBroker
{
	@Override
	public String ID()
	{
		return "StdCraftBroker";
	}

	protected static final Map<String,Long> nextCheckTimes=new Hashtable<String,Long>();

	protected String		currency			= "";
	protected int			maxDays				= 900;
	protected int			maxListings			= 5;
	protected double		commissionPct		= 0.10;

	public StdCraftBroker()
	{
		super();
		_name="a craft broker";
		setDescription("He wants to help you make a deal!");
		setDisplayText("A craft broker is here.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_WISDOM,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,8);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public String brokerChain()
	{
		return text();
	}

	@Override
	public void setBrokerChain(final String name)
	{
		setMiscText(name);
	}

	@Override
	public CoffeeShop getShop(final MOB mob)
	{
		if(mob == null)
			return getShop();
		return loadResults(mob.Name());
	}

	private List<Quad<String,String,Integer,Double>> loadRequests(final String shelfName)
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final List<PlayerData> data;
		if(shelfName == null)
			data = CMLib.database().DBReadPlayerSectionData(shopKey);
		else
			data = CMLib.database().DBReadPlayerData(shelfName, shopKey);
		final List<Quad<String,String,Integer,Double>> list = new ArrayList<Quad<String,String,Integer,Double>>();
		final XMLLibrary xmlLib = CMLib.xml();
		for(final PlayerData dat : data)
		{
			if((dat.xml().length()>0)&&(dat.xml().startsWith("<")))
			{
				final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(dat.xml());
				for(final XMLLibrary.XMLTag tag : xml)
				{
					if(tag.tag().equals("REQUEST"))
					{
						final String author = tag.getParmValue("NAME");
						final String req = xmlLib.restoreAngleBrackets(tag.value());
						final int num = CMath.s_int(tag.getParmValue("NUM"));
						final double price = CMath.s_double(tag.getParmValue("PRICE"));
						list.add(new Quad<String,String,Integer,Double>(author+"/"+dat.key(),req,Integer.valueOf(num),Double.valueOf(price)));
					}
				}
			}
		}
		return list;
	}

	private String getExpirationString()
	{
		if(maxTimedListingDays()>0)
		{
			TimeClock time = CMLib.time().homeClock(this);
			time=(TimeClock)time.copyOf();
			time.bump(TimePeriod.DAY, this.maxTimedListingDays());
			return (" EXPIRE=\""+time.toTimeString()+"\"");
		}
		return "";
	}

	private void addRequest(final MOB mob, final String request, final int number, final double price)
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String key = shopKey+mob.Name()+(shopKey+mob.Name()).hashCode();
		final StringBuilder xml = new StringBuilder("");
		xml.append("<REQUEST NAME=\""+mob.Name()+"\" NUM="+number+" PRICE="+Math.round(price));
		xml.append(getExpirationString()).append(">");
		xml.append(CMLib.xml().parseOutAngleBrackets(request));
		xml.append("</REQUEST>");
		CMLib.database().DBCreatePlayerData(mob.Name(), shopKey, key, xml.toString());
	}

	private CoffeeShop loadRequestShop()
	{
		final CoffeeShop shop = ((CoffeeShop)CMClass.getCommon("DefaultCoffeeShop")).build(this);
		for(final Quad<String,String,Integer,Double> q : loadRequests(null))
		{
			final ExtendableAbility A = (ExtendableAbility)CMClass.getAbility("ExtAbility");
			A.setName(q.second);
			A.setMiscText(q.first);
			shop.addStoreInventory(A, q.third.intValue(), q.fourth.intValue());
		}
		return shop;
	}

	private Ability getMatchingRequestInventory(final CoffeeShop shop, final Item I)
	{
		if(I==null)
			return null;
		for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
		{
			final Environmental E = i.next();
			if(E instanceof Ability)
			{
				final CompiledZMask mask = CMLib.masking().parseSpecialItemMask(CMParms.parse(E.name()));
				if((mask != null)&&(CMLib.masking().maskCheck(mask, I, true)))
					return (Ability)E;
			}
		}
		return null;
	}

	private void removeRequestShop(final Ability A)
	{
		final int x =A.text().indexOf('/');
		if(x<0)
			return;
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String author = A.text().substring(0,x);
		final String key = A.text().substring(x+1);
		CMLib.database().DBDeletePlayerData(author, shopKey, key);
	}

	private void removeAllRequestsShop(final String shelfName)
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		CMLib.database().DBDeletePlayerData(shelfName, shopKey);
	}

	private void updateRequestShop(final CoffeeShop shop, final Ability A)
	{
		final int x =A.text().indexOf('/');
		if(x<0)
			return;
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final String author = A.text().substring(0,x);
		final String key = A.text().substring(x+1);
		final int num = shop.numberInStock(A);
		final int price = shop.stockPrice(A);
		final StringBuilder xml = new StringBuilder("");
		xml.append("<REQUEST NAME=\""+author+"\" NUM="+num+" PRICE="+price);
		xml.append(getExpirationString()).append(">");
		xml.append(CMLib.xml().parseOutAngleBrackets(A.name()));
		xml.append("</REQUEST>");
		CMLib.database().DBReCreatePlayerData(author, shopKey, key, xml.toString());
	}

	private List<Triad<String,String,TimeClock>> scanRequests()
	{
		final String shopKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
		final List<PlayerData> data = CMLib.database().DBReadPlayerSectionData(shopKey);
		final List<Triad<String,String,TimeClock>> list = new Vector<Triad<String,String,TimeClock>>();
		final XMLLibrary xmlLib=CMLib.xml();
		for(final PlayerData dat : data)
		{
			final TimeClock time = (TimeClock)CMLib.time().homeClock(this).copyOf();
			time.bump(TimePeriod.HOUR, -1);
			final String xmlStr = dat.xml();
			if((xmlStr.length()>0)&&(xmlStr.startsWith("<")))
			{
				final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(xmlStr);
				for(final XMLLibrary.XMLTag tag : xml)
				{
					if(tag.tag().equals("REQUEST"))
					{
						final String expireStr = tag.getParmValue("EXPIRE");
						if((expireStr!=null)&&(expireStr.length()>0))
							time.fromTimePeriodCodeString(expireStr);
					}
				}
			}
			list.add(new Triad<String,String,TimeClock>(dat.who(),dat.key(),time));
		}
		return list;
	}

	private CoffeeShop loadResults(final String shelfName)
	{
		final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
		final CoffeeShop shop = ((CoffeeShop)CMClass.getCommon("DoubleCoffeeShop")).build(this);
		final List<PlayerData> data = CMLib.database().DBReadPlayerData(shelfName, shopKey);
		for(final PlayerData dat : data)
		{
			if(dat.xml().length()>0)
			{
				String xml = dat.xml();
				if(!xml.startsWith("<"))
				{
					final int x = xml.indexOf('<');
					if(x<0)
						continue;
					// expire string
					xml = xml.substring(x);
				}
				shop.buildShopFromXML(xml);
			}
		}
		return shop;
	}

	private List<Triad<String,String,TimeClock>> scanResults()
	{
		final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
		final List<PlayerData> data = CMLib.database().DBReadPlayerSectionData(shopKey);
		final List<Triad<String,String,TimeClock>> list = new Vector<Triad<String,String,TimeClock>>();
		for(final PlayerData dat : data)
		{
			final TimeClock time = (TimeClock)CMLib.time().homeClock(this).copyOf();
			time.bump(TimePeriod.HOUR, -1);
			final String xml = dat.xml();
			if(!xml.startsWith("<"))
			{
				final int x = xml.indexOf('<');
				if(x>0)
				{
					final String parms = xml.substring(0,x);
					final String expireStr = CMParms.getParmStr(parms, "EXPIRE", "");
					if(expireStr.length()>0)
						time.fromTimePeriodCodeString(expireStr);
				}
			}
			list.add(new Triad<String,String,TimeClock>(dat.who(),dat.key(),time));
		}
		return list;
	}

	private String getShelfName(final String s)
	{
		final int x =s.indexOf('/');
		if(x<0)
			return "";
		return s.substring(0,x);
	}

	private String getShelfName(final Ability A)
	{
		if(A==null)
			return "";
		return getShelfName(A.text());
	}

	private void saveResults(final String shelfName, final CoffeeShop shop)
	{
		final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
		String key = shopKey+shelfName+(shopKey+shelfName).hashCode();
		if(key.length()>=100)
			key=key.substring(key.length()-99);
		CMLib.database().DBReCreatePlayerData(shelfName, shopKey, key,
				getExpirationString()+shop.makeXML());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;
		synchronized(CMClass.getSync(("BROKER_CHAIN_"+brokerChain().toUpperCase().trim())))
		{
			final Long nextTime=StdCraftBroker.nextCheckTimes.get(brokerChain().toUpperCase().trim());
			if((nextTime==null)||System.currentTimeMillis()>nextTime.longValue())
			{
				if(!CMLib.flags().isInTheGame(this,true))
					return true;
				StdCraftBroker.nextCheckTimes.remove(brokerChain().toUpperCase().trim());
				final long thisTime=System.currentTimeMillis();
				final TimeClock nowC = CMLib.time().homeClock(this);
				final long millisPerMudDay = (CMProps.getMillisPerMudHour() * nowC.getHoursInDay());
				StdCraftBroker.nextCheckTimes.put(brokerChain().toUpperCase().trim(), Long.valueOf(thisTime+millisPerMudDay));
				final int maxListingDays = maxTimedListingDays();
				if(maxListingDays > 0)
				{
					final String shopKey = "BROKER_SHOP_"+brokerChain().toUpperCase().replace(' ', '_');
					final List<Triad<String,String,TimeClock>> results = scanResults();
					for(final Triad<String,String,TimeClock> res : results)
					{
						if(nowC.isAfter(res.third))
							CMLib.database().DBDeletePlayerData(res.first, shopKey, res.second);
					}

					final String reqKey = "BROKER_REQ_"+brokerChain().toUpperCase().replace(' ', '_');
					final List<Triad<String,String,TimeClock>> requests = scanRequests();
					for(final Triad<String,String,TimeClock> req : requests)
					{
						if(nowC.isAfter(req.third))
							CMLib.database().DBDeletePlayerData(req.first, reqKey, req.second);
					}
				}
			}
		}
		return true;
	}

	protected void autoGive(final MOB src, final MOB tgt, final Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				super.executeMsg(myHost, msg);
				break;
			case CMMsg.TYP_VALUE:
			{
				super.stdMOBexecuteMsg(myHost, msg);
				CMLib.commands().postSay(this, mob, L("I'm sorry, I don't run a pawn shop."), true, false);
				break;
			}
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.stdMOBexecuteMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true)
				&&(msg.tool() instanceof Item))
				{
					final CoffeeShop shop = loadRequestShop();
					Ability A = this.getMatchingRequestInventory(shop, (Item)msg.tool());
					if(A != null)
					{
						final String forName = getShelfName(A);
						final int price = shop.stockPrice(A);
						int numberSold = 1;
						if(msg.tool() instanceof PackagedItems)
							numberSold=((PackagedItems)msg.tool()).numberOfItemsInPackage();
						else
						if(msg.tool() instanceof RawMaterial)
							numberSold=((RawMaterial)msg.tool()).phyStats().weight();
						shop.addStoreInventory(msg.tool(), shop.numberInStock(A), price);
						final double paid = CMLib.coffeeShops().transactPawn(this, msg.source(), this, msg.tool(), shop, BuySellFlag.WHOLESALE);
						if (paid > Double.MIN_VALUE)
						{
							budgetRemaining = budgetRemaining - Math.round(paid);
							if (mySession != null)
								mySession.stdPrintln(msg.source(), msg.target(), msg.tool(), msg.targetMessage());
						}
						for(int i=0;i<numberSold;i++)
							A=(Ability)shop.removeStock("$"+A.name()+"$", mob);
						shop.removeSellableProduct("$"+msg.tool().name()+"$", msg.source());
						final CoffeeShop itemShop = this.loadResults(forName);
						final double sellPrice = price + ((commissionPct()>0)?CMath.mul(paid, commissionPct()):0);
						itemShop.addStoreInventory(msg.tool(), 1, (int)Math.round(sellPrice));
						final int num = shop.numberInStock(A);
						if(num <= 0)
							this.removeRequestShop(A);
						else
							this.updateRequestShop(shop, A);
						if(forName.length()>0)
							this.saveResults(forName, itemShop);
					}
				}
				return;
			}
			case CMMsg.TYP_VIEW:
			{
				super.stdMOBexecuteMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final CoffeeShop shop = getShop(msg.source());
					if ((msg.tool() != null)
					&& (shop.doIHaveThisInStock("$" + msg.tool().Name() + "$", mob)))
					{
						final String prefix = L("Interested in @x1? Here is some information for you: ",msg.tool().Name());
						final String viewDesc = prefix + CMLib.coffeeShops().getViewDescription(msg.source(), msg.tool(), viewFlags());
						CMLib.commands().postSay(this, msg.source(), viewDesc, true, false);
					}
				}
				break;
			}
			case CMMsg.TYP_BUY:
				super.stdMOBexecuteMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final CoffeeShop shop = getShop(msg.source());
					if ((msg.tool() != null) && (shop.doIHaveThisInStock("$" + msg.tool().Name() + "$", msg.source()))
					&& (location() != null))
					{
						final Environmental item = shop.getStock("$" + msg.tool().Name() + "$",  msg.source());
						double scratch = 0;
						if(isMonster())
							scratch=CMLib.beanCounter().getTotalAbsoluteNativeValue(this);
						if (item != null)
							CMLib.coffeeShops().transactMoneyOnly(this, msg.source(), this, item, BuySellFlag.WHOLESALE);
						if(isMonster() && CMLib.beanCounter().getTotalAbsoluteNativeValue(this) > scratch)
							CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getTotalAbsoluteNativeValue(this) - scratch );

						final List<Environmental> products = shop.removeSellableProduct("$" + msg.tool().Name() + "$",  msg.source());
						if (products.size() == 0)
							break;
						saveResults(msg.source().Name(), shop);
						final Environmental product = products.get(0);

						if (product instanceof Item)
						{
							msg.modify(msg.source(), msg.target(), product, msg.sourceCode(), msg.sourceMessage(), msg.targetCode(), msg.targetMessage(), msg.othersCode(), msg.othersMessage());
							if (!CMLib.coffeeShops().purchaseItems((Item) product, products, this,  msg.source()))
								return;
						}
						if (mySession != null)
							mySession.stdPrintln(msg.source(), msg.target(), msg.tool(), msg.targetMessage());
						if (!CMath.bset(msg.targetMajor(), CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
					}
				}
				return;
			case CMMsg.TYP_BROKERADD:
			{
				super.executeMsg(myHost, msg);
				if((msg.targetMessage()!=null) || (msg.targetMessage().length()>0))
				{
					final List<String> parts = CMParms.parse(msg.targetMessage());
					if((parts.size()==1)
					&&(parts.get(0).equalsIgnoreCase("CANCEL")))
						this.removeAllRequestsShop(msg.source().Name());
					else
					if(parts.size()>2)
					{
						final int num = CMath.s_int(parts.remove(0));
						final Triad<String, Double, Long> triad =
								CMLib.english().parseMoneyStringSDL(getFinalCurrency(), parts.remove(parts.size()-1));
						final CompiledZMask mask = CMLib.masking().parseSpecialItemMask(parts);
						if((num>0)
						&&(triad != null)
						&&(mask != null))
						{
							addRequest(msg.source(), CMParms.combineQuoted(parts, 0), num, triad.second.doubleValue()*triad.third.intValue());
							if(this.maxTimedListingDays()>0)
							{
								CMLib.commands().postSay(this,msg.source(),
										L("Your new listing is posted and will remain for the next @x1 days.",""+this.maxTimedListingDays()));
							}
							else
							{
								CMLib.commands().postSay(this,msg.source(),
										L("Your new listing is posted.",""+this.maxTimedListingDays()));
							}
						}
					}
				}
				return;
			}
			case CMMsg.TYP_SPEAK:
			{
				super.executeMsg(myHost, msg);
				CMStrings.getSayFromMessage(msg.targetMessage());
				return;
			}
			case CMMsg.TYP_LIST:
			{
				super.stdMOBexecuteMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final String forMask = CMLib.coffeeShops().getListForMask(msg.targetMessage());
					boolean listedSomething = false;
					final int limit = CMParms.getParmInt(getFinalPrejudiceFactors(), "LIMIT", 0);
					{
						final CoffeeShop reqshop = this.loadRequestShop();
						final List<Environmental> inventory = new XVector<Environmental>(reqshop.getStoreInventory());
						final String s = CMLib.coffeeShops().getListInventory(this, null, inventory, limit, this, reqshop, forMask);
						listedSomething = listedSomething || s.length()>0;
						if (s.length() > 0)
							mob.tell(L("\n\r^HItem Requests:^?\n\r")+s);
					}
					{
						final CoffeeShop resshop = getShop(mob);
						final List<Environmental> inventory = new XVector<Environmental>(resshop.getStoreInventory());
						final String s = CMLib.coffeeShops().getListInventory(this, null, inventory, limit, this, resshop, forMask);
						listedSomething = listedSomething || s.length()>0;
						if (s.length() > 0)
							mob.tell(L("\n\r^HItem Pickups:^?\n\r")+s);
					}
					if(!listedSomething)
						mob.tell(L("There are no listings to see at this time."));
				}
				return;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if ((msg.targetMinor() == CMMsg.TYP_EXPIRE) && (msg.target() == location()) && (CMLib.flags().isInTheGame(this, true)))
			return false;
		else
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_BROKERADD:
			{
				if(!super.okMessage(myHost, msg))
					return false;
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				boolean wellFormatted = false;
				if((msg.targetMessage()!=null) || (msg.targetMessage().length()>0))
				{
					final List<String> parts = CMParms.parse(msg.targetMessage());
					Triad<String, Double, Long> triad;
					if((parts.size()==1)
					&&(parts.get(0).equalsIgnoreCase("CANCEL")))
						wellFormatted=true;
					else
					if((parts.size()>2)
					&&(CMath.s_int(parts.remove(0))>0)
					&&((triad=CMLib.english().parseMoneyStringSDL(getFinalCurrency(), parts.remove(parts.size()-1)))!=null)
					&&(CMLib.masking().parseSpecialItemMask(parts)!=null))
					{
						wellFormatted=true;
						if(!triad.first.equalsIgnoreCase(getFinalCurrency()))
						{
							CMLib.commands().postSay(this,msg.source(),L("Your new listing would be paid in currency I don't deal in."));
							return false;
						}
					}
				}
				if(!wellFormatted)
				{
					msg.source().tell(L("Your new listing is too badly formatted to list."));
					return false;
				}
				if(this.maxListings()>0)
				{
					final List<Quad<String,String,Integer,Double>> quads = loadRequests(msg.source().Name());
					final int numExisting = quads.size();
					if(numExisting > this.maxListings())
					{
						CMLib.commands().postSay(this,msg.source(),L("I'm sorry, you can't post any more listings."),true,false);
						return false;
					}
				}
				return true;
			}
			case CMMsg.TYP_GIVE:
				{
					if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
						return false;
					if (msg.tool() == null)
						return false;
					if (!(msg.tool() instanceof Item))
					{
						mob.tell(L("@x1 doesn't look interested.", mob.charStats().HeShe()));
						return false;
					}
					if (CMLib.flags().isEnspelled((Item) msg.tool()) || CMLib.flags().isOnFire((Item) msg.tool()))
					{
						mob.tell(this, msg.tool(), null, L("<S-HE-SHE> refuses to accept <T-NAME>."));
						return false;
					}
					return super.okMessage(myHost, msg);
				}
			case CMMsg.TYP_SELL:
			{
				if(!super.stdMOBokMessage(myHost, msg))
					return false;
				if((msg.tool()==null)
				||(!doISellThis(msg.tool()))
				||(!(msg.tool() instanceof Item)))
				{
					CMLib.commands().postSay(this,msg.source(),L("I'm sorry, I don't deal in that."),true,false);
					return false;
				}
				if((msg.tool() instanceof Item)
				&& (!CMLib.law().mayOwnThisItem(msg.source(), (Item)msg.tool()))
				&& ((!CMLib.flags().isEvil(this))
					||(CMLib.flags().isLawful(this))))
				{
					CMLib.commands().postSay(this,msg.source(),L("I don't deal in stolen goods."),true,false);
					return false;
				}
				if((msg.tool() instanceof Item)&&(msg.source().isMine(msg.tool())))
				{
					final CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),CMMsg.MSG_DROP,null);
					if(!msg.source().location().okMessage(msg.source(),msg2))
						return false;
				}
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				final CoffeeShop shop = loadRequestShop();
				final Ability item = this.getMatchingRequestInventory(shop, (Item)msg.tool());
				if(item == null)
				{
					CMLib.commands().postSay(this,msg.source(),L("That item does not fulfill any listed request.  Try LIST."),true,false);
					return false;
				}
				return true;
			}
			case CMMsg.TYP_VALUE:
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_BUY:
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				if ((msg.targetMinor() == CMMsg.TYP_BUY)
				&& (msg.tool() != null)
				&& (!msg.tool().okMessage(myHost, msg)))
					return false;
				final BuySellFlag buyFlag = (msg.targetMinor()==CMMsg.TYP_BUY)?BuySellFlag.WHOLESALE:BuySellFlag.INFO;
				if (CMLib.coffeeShops().sellEvaluation(this, msg.source(), msg.tool(), this, buyFlag))
					return super.stdMOBokMessage(myHost, msg);
				return false;
			case CMMsg.TYP_LIST:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				return super.stdMOBokMessage(myHost, msg);
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int maxTimedListingDays()
	{
		return maxDays;
	}

	@Override
	public void setMaxTimedListingDays(final int d)
	{
		maxDays=d;
	}

	@Override
	public int maxListings()
	{
		return maxListings;
	}

	@Override
	public void setMaxListings(final int d)
	{
		maxListings=d;
	}

	@Override
	public double commissionPct()
	{
		return commissionPct;
	}

	@Override
	public void setCommissionPct(final double d)
	{
		commissionPct = d;
	}
}
