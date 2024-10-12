package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Bool;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ShoppingLibrary.BuySellFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	@Override
	public String ID()
	{
		return "StdShopKeeper";
	}

	protected CoffeeShop	shop				= ((CoffeeShop) CMClass.getCommon("DefaultCoffeeShop")).build(this);
	protected long			whatIsSoldMask		= 0;
	protected int			invResetRate		= 0;
	protected int			invResetTickDown	= 0;
	protected long			budgetRemaining		= Long.MAX_VALUE / 2;
	protected long			budgetMax			= Long.MAX_VALUE / 2;
	protected int			budgetTickDown		= 2;
	protected double[]		devalueRate			= null;
	protected String		currency			= "";
	protected String[]		pricingAdjustments	= new String[0];
	protected String		itemZapperMask		= "";
	protected Set<ViewType>	viewTypes			= new XHashSet<ViewType>(ViewType.BASIC);

	protected Pair<Long,TimeClock.TimePeriod>	budget = null;

	public StdShopKeeper()
	{
		super();
		_name = "a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		CMLib.factions().setAlignment(this, Faction.Align.GOOD);
		setMoney(0);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE, 16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA, 25);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public boolean isSold(final int mask)
	{
		if (mask == 0)
			return whatIsSoldMask == 0;
		if ((whatIsSoldMask & 255) == mask)
			return true;
		return CMath.bset(whatIsSoldMask >> 8, CMath.pow(2, mask - 1));
	}

	@Override
	public void addSoldType(final int mask)
	{
		if (mask == 0)
			whatIsSoldMask = 0;
		else
		{
			if ((whatIsSoldMask > 0) && (whatIsSoldMask < 256))
				whatIsSoldMask = (CMath.pow(2, whatIsSoldMask - 1) << 8);

			for (int c = 0; c < ShopKeeper.DEAL_CONFLICTS.length; c++)
			{
				for (int c1 = 0; c1 < ShopKeeper.DEAL_CONFLICTS[c].length; c1++)
				{
					if (ShopKeeper.DEAL_CONFLICTS[c][c1] == mask)
					{
						for (c1 = 0; c1 < ShopKeeper.DEAL_CONFLICTS[c].length; c1++)
						{
							if ((ShopKeeper.DEAL_CONFLICTS[c][c1] != mask)
							&& (isSold(ShopKeeper.DEAL_CONFLICTS[c][c1])))
								addSoldType(-ShopKeeper.DEAL_CONFLICTS[c][c1]);
						}
						break;
					}
				}
			}

			if (mask < 0)
				whatIsSoldMask = CMath.unsetb(whatIsSoldMask, (CMath.pow(2, (-mask) - 1) << 8));
			else
				whatIsSoldMask |= (CMath.pow(2, mask - 1) << 8);
		}
	}

	@Override
	public long getWhatIsSoldMask()
	{
		return whatIsSoldMask;
	}

	@Override
	public void setWhatIsSoldMask(final long newSellCode)
	{
		whatIsSoldMask = newSellCode;
	}

	@Override
	protected void cloneFix(final MOB E)
	{
		super.cloneFix(E);
		if (E instanceof StdShopKeeper)
			shop = ((CoffeeShop) ((StdShopKeeper) E).shop.copyOf()).build(this);
	}

	@Override
	public Set<ViewType> viewFlags()
	{
		return viewTypes;
	}

	@Override
	public CoffeeShop getShop(final MOB mob)
	{
		return getShop();
	}

	@Override
	public CoffeeShop getShop()
	{
		return shop;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		getShop().destroyStoreInventory();
	}

	@Override
	public String storeKeeperString()
	{
		return CMLib.coffeeShops().storeKeeperString(getShop(), this);
	}

	@Override
	public boolean doISellThis(final Environmental thisThang)
	{
		return CMLib.coffeeShops().doISellThis(thisThang, this);
	}

	protected Area getStartArea()
	{
		Area A = CMLib.map().getStartArea(this);
		if (A == null)
			CMLib.map().areaLocation(this);
		if (A == null)
			A = CMLib.map().areas().nextElement();
		return A;
	}

	protected void doInventoryReset()
	{
		invResetTickDown = getFinalInvResetRate(); // we should now be at a
		// positive number.
		if (invResetTickDown <= 0)
			invResetTickDown = Ability.TICKS_FOREVER;
		else
		{
			final CoffeeShop shop=(this instanceof Librarian)?((Librarian)this).getBaseLibrary():this.getShop();
			shop.emptyAllShelves();
			if (miscText != null)
			{
				String shoptext;
				if (CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS) && (miscText instanceof byte[]))
					shoptext = CMLib.coffeeMaker().getGenMOBTextUnpacked(this, CMLib.encoder().decompressString((byte[]) miscText));
				else
					shoptext = CMLib.coffeeMaker().getGenMOBTextUnpacked(this, CMStrings.bytesToStr(miscText));
				final List<XMLLibrary.XMLTag> xml = CMLib.xml().parseAllXML(shoptext);
				if (xml != null)
				{
					CMLib.coffeeMaker().populateShops(this, xml);
					recoverPhyStats();
					recoverCharStats();
				}
			}
		}
	}

	protected void doBudgetReset()
	{
		budgetTickDown = 100;
		budgetRemaining = Long.MAX_VALUE / 2;
		final Pair<Long,TimeClock.TimePeriod> budget = getFinalBudget();
		if(budget != null)
		{
			budgetRemaining = budget.first.longValue();
			budgetTickDown = 100;
			TimeClock C=CMLib.time().homeClock(this);
			if(C==null)
				C=CMLib.time().globalClock();
			budgetTickDown = (int) (CMProps.getTicksPerMudHour() * C.getHoursPer(budget.second));
		}
		budgetMax = budgetRemaining;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (!super.tick(ticking, tickID))
			return false;
		if ((tickID == Tickable.TICKID_MOB)
		&& (isGeneric())
		&& (CMProps.getBoolVar(Bool.MUDSTARTED)))
		{
			if ((--invResetTickDown) <= 0)
				doInventoryReset();
			if ((--budgetTickDown) <= 0)
				doBudgetReset();
		}
		return true;
	}

	/**
	 * For bankers and stuff that override stdshopkeeper, but can't call
	 * its okmess,and needs to call the StdMob one.
	 *
	 * @param myHost the host
	 * @param msg the message
	 * @return true or false
	 */
	protected boolean stdMOBokMessage(final Environmental myHost, final CMMsg msg)
	{
		return super.okMessage(myHost, msg);
	}

	/**
	 * For bankers and stuff that override stdshopkeeper, but can't call
	 * its okmess,and needs to call the StdMob one.
	 *
	 * @param myHost the host
	 * @param msg the message
	 * @return true or false
	 */
	protected void stdMOBexecuteMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				final BuySellFlag flag = (msg.targetMinor()==CMMsg.TYP_SELL)?BuySellFlag.RETAIL:BuySellFlag.INFO;
				if (CMLib.coffeeShops().pawnEvaluation(this, msg.source(), msg.tool(), this, budgetRemaining, budgetMax, flag))
					return super.okMessage(myHost, msg);
				return false;
			}
			case CMMsg.TYP_BID:
			{
				CMLib.commands().postSay(this, msg.source(), L("I'm afraid my prices are firm."), false, false);
				return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				final MOB mobFor = CMLib.coffeeShops().parseBuyingFor(msg.source(), msg.targetMessage());
				if(mobFor!=msg.source())
				{
					final MOB srcM = msg.source();
					try
					{
						msg.setSource(mobFor);
						if(!mobFor.okMessage(mobFor, msg))
							return false;
					}
					finally
					{
						msg.setSource(srcM);
					}
				}
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				if ((msg.targetMinor() == CMMsg.TYP_BUY)
				&& (msg.tool() != null)
				&& (!msg.tool().okMessage(myHost, msg)))
					return false;
				final BuySellFlag buyFlag = (msg.targetMinor()==CMMsg.TYP_BUY)?BuySellFlag.RETAIL:BuySellFlag.INFO;
				if (CMLib.coffeeShops().sellEvaluation(this, msg.source(), msg.tool(), this, buyFlag))
					return super.okMessage(myHost, msg);
				return false;
			}
			case CMMsg.TYP_LIST:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), getFinalIgnoreMask(), this))
					return false;
				return super.okMessage(myHost, msg);
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if (msg.amITarget(this))
		{
			final MOB mob = msg.source();
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					if ((msg.tool() != null)
					&& ((CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.ORDER)
						|| (CMLib.law().doesHavePriviledgesHere(msg.source(), getStartRoom()))
						|| (CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.CMDMOBS) && (isMonster()))
						|| (CMSecurity.isAllowed(msg.source(), location(), CMSecurity.SecFlag.CMDROOMS) && (isMonster()))))
					&& ((doISellThis(msg.tool())) || (isSold(DEAL_INVENTORYONLY))))
					{
						CMLib.commands().postSay(this, msg.source(), L("Yes, I will now sell @x1.", msg.tool().name()), false, false);
						getShop().addStoreInventory(msg.tool(), 1, -1);
						if (isGeneric())
							text();
						return;
					}
				}
				super.executeMsg(myHost, msg);
				break;
			case CMMsg.TYP_VALUE:
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final double pawningPrice = CMLib.coffeeShops().pawningPrice(this, mob, msg.tool(), this).absoluteGoldPrice;
					final String currencyShort = CMLib.beanCounter().nameCurrencyShort(this, pawningPrice);
					CMLib.commands().postSay(this, mob, L("I'll give you @x1 for @x2.", currencyShort, msg.tool().name()), true, false);
				}
				break;
			}
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final double paid = CMLib.coffeeShops().transactPawn(this, msg.source(), this, msg.tool(), getShop(), BuySellFlag.RETAIL);
					if (paid > Double.MIN_VALUE)
					{
						budgetRemaining = budgetRemaining - Math.round(paid);
						if (mySession != null)
							mySession.stdPrintln(msg.source(), msg.target(), msg.tool(), msg.targetMessage());
						if (!CMath.bset(msg.targetMajor(), CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
						if (isGeneric())
							text();
					}
				}
				break;
			}
			case CMMsg.TYP_VIEW:
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					if ((msg.tool() != null)
					&& (getShop().doIHaveThisInStock("$" + msg.tool().Name() + "$", mob)))
					{
						final String prefix = L("Interested in @x1? Here is some information for you: ",msg.tool().Name());
						final String viewDesc = prefix + CMLib.coffeeShops().getViewDescription(msg.source(), msg.tool(), viewFlags());
						CMLib.commands().postSay(this, msg.source(), viewDesc, true, false);
					}
				}
				break;
			}
			case CMMsg.TYP_BUY: // buy-from -- this is a player buying from a shopkeeper
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final MOB mobFor = CMLib.coffeeShops().parseBuyingFor(msg.source(), msg.targetMessage());
					if ((msg.tool() != null) && (getShop().doIHaveThisInStock("$" + msg.tool().Name() + "$", mobFor))
					&& (location() != null))
					{
						final Environmental item = getShop().getStock("$" + msg.tool().Name() + "$", mobFor);
						if (item != null)
						{
							final BuySellFlag flag = !isMonster() ? BuySellFlag.RETAIL : BuySellFlag.INFO;
							CMLib.coffeeShops().transactMoneyOnly(this, msg.source(), this, item, flag);
						}

						final List<Environmental> products = getShop().removeSellableProduct("$" + msg.tool().Name() + "$", mobFor);
						if (products.size() == 0)
							break;
						final Environmental product = products.get(0);

						if (product instanceof Item)
						{
							msg.modify(msg.source(), msg.target(), product, msg.sourceCode(), msg.sourceMessage(), msg.targetCode(), msg.targetMessage(), msg.othersCode(), msg.othersMessage());
							if (!CMLib.coffeeShops().purchaseItems((Item) product, products, this, mobFor))
								return;
						}
						else
						if (product instanceof MOB)
						{
							if (CMLib.coffeeShops().purchaseMOB((MOB) product, this, this, mobFor))
							{
								msg.modify(msg.source(), msg.target(), product, msg.sourceCode(), msg.sourceMessage(), msg.targetCode(), msg.targetMessage(), msg.othersCode(), msg.othersMessage());
								product.executeMsg(myHost, msg);
							}
						}
						else
						if (product instanceof Ability)
							CMLib.coffeeShops().purchaseAbility((Ability) product, this, this, mobFor);

						if (mySession != null)
							mySession.stdPrintln(msg.source(), msg.target(), msg.tool(), msg.targetMessage());
						if (!CMath.bset(msg.targetMajor(), CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
					}
				}
				break;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final String forMask = CMLib.coffeeShops().getListForMask(msg.targetMessage());
					List<Environmental> inventory = new XVector<Environmental>(getShop().getStoreInventory());
					inventory = CMLib.coffeeShops().addRealEstateTitles(inventory, mob, getShop(), getStartRoom());
					final int limit = CMParms.getParmInt(getFinalPrejudiceFactors(), "LIMIT", 0);
					final String s = CMLib.coffeeShops().getListInventory(this, mob, inventory, limit, this, forMask);
					if (s.length() > 0)
						mob.tell(s);
				}
				break;
			}
			default:
				super.executeMsg(myHost, msg);
				break;
			}
		}
		else
			super.executeMsg(myHost, msg);
	}

	@Override
	public String getFinalPrejudiceFactors()
	{
		if (getRawPrejudiceFactors().length() > 0)
			return getRawPrejudiceFactors();
		return getStartArea().getFinalPrejudiceFactors();
	}

	@Override
	public String getRawPrejudiceFactors()
	{
		return CMStrings.bytesToStr(miscText);
	}

	@Override
	public void setPrejudiceFactors(final String factors)
	{
		miscText = factors;
	}

	@Override
	public String getFinalIgnoreMask()
	{
		if (getRawIgnoreMask().length() > 0)
			return getRawIgnoreMask();
		return getStartArea().getFinalIgnoreMask();
	}

	@Override
	public String getRawIgnoreMask()
	{
		return "";
	}

	@Override
	public void setIgnoreMask(final String factors)
	{
	}

	@Override
	public String[] getFinalItemPricingAdjustments()
	{
		if ((getRawItemPricingAdjustments() != null)
		&& (getRawItemPricingAdjustments().length > 0))
			return getRawItemPricingAdjustments();
		return getStartArea().getFinalItemPricingAdjustments();
	}

	@Override
	public String[] getRawItemPricingAdjustments()
	{
		return pricingAdjustments;
	}

	@Override
	public void setItemPricingAdjustments(final String[] factors)
	{
		pricingAdjustments = factors;
	}

	@Override
	public Pair<Long, TimePeriod> getFinalBudget()
	{
		if (budget != null)
			return budget;
		return getStartArea().getFinalBudget();
	}

	@Override
	public String getRawBbudget()
	{
		return budget == null ? "" : (budget.first + " " + budget.second.name());
	}

	@Override
	public void setBudget(final String factors)
	{
		budget = CMLib.coffeeShops().parseBudget(factors);
		budgetTickDown = 0;
	}

	@Override
	public double[] getFinalDevalueRate()
	{
		if (devalueRate != null)
			return devalueRate;
		return getStartArea().getFinalDevalueRate();
	}

	@Override
	public String getRawDevalueRate()
	{
		return (devalueRate == null) ? "" : (devalueRate[0] + " " + devalueRate[1]);
	}

	@Override
	public void setDevalueRate(final String factors)
	{
		devalueRate = CMLib.coffeeShops().parseDevalueRate(factors);
	}

	@Override
	public int getFinalInvResetRate()
	{
		if (getRawInvResetRate() != 0)
			return getRawInvResetRate();
		return getStartArea().getFinalInvResetRate();
	}

	@Override
	public int getRawInvResetRate()
	{
		return invResetRate;
	}

	@Override
	public void setInvResetRate(final int ticks)
	{
		invResetRate = ticks;
		invResetTickDown = 0;
	}

	@Override
	public void setWhatIsSoldZappermask(final String newSellMask)
	{
		itemZapperMask = newSellMask;
	}

	@Override
	public String getWhatIsSoldZappermask()
	{
		return itemZapperMask;
	}

	@Override
	public String getFinalCurrency()
	{
		if(currency.length()>0)
			return currency;
		return CMLib.beanCounter().getCurrency(this);
	}

	@Override
	public String getRawCurrency()
	{
		return currency;
	}

	@Override
	public void setCurrency(final String newCurrency)
	{
		if ((currency != null) && (currency.length() > 0))
		{
			CMLib.beanCounter().unloadCurrencySet(currency);
			currency = newCurrency;
		}
		else
		{
			currency = newCurrency;
			CMLib.beanCounter().getCurrencySet(currency);
		}
	}
}
