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
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2007-2024 Bo Zimmerman

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
public class StdAuctioneer extends StdMOB implements Auctioneer
{
	@Override
	public String ID()
	{
		return "StdAuctioneer";
	}

	protected double		timedListingPrice	= -1.0;
	protected double		timedListingPct		= -1.0;
	protected double		timedFinalCutPct	= -1.0;
	protected int			maxTimedAuctionDays	= -1;
	protected int			minTimedAuctionDays	= -1;
	public AuctionData		lastMsgData			= null;
	protected String		currency			= "";
	protected Set<ViewType>	viewTypes			= new XHashSet<ViewType>(ViewType.BASIC);

	protected static final Map<String,Long> lastCheckTimes=new Hashtable<String,Long>();

	public StdAuctioneer()
	{
		super();
		_name="an auctioneer";
		setDescription("He talks faster than you!");
		setDisplayText("The local auctioneer is here calling prices.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public CoffeeShop getShop(final MOB mob)
	{
		return getShop();
	}

	@Override
	public CoffeeShop getShop()
	{
		final CoffeeShop shop=((CoffeeShop)CMClass.getCommon("AuctionCoffeeShop")).build(this);
		shop.addStoreInventory(null);
		return shop;
	}

	@Override
	public String auctionHouse()
	{
		return text();
	}

	@Override
	public void setAuctionHouse(final String name)
	{
		setMiscText(name);
	}

	@Override
	public double timedListingPrice()
	{
		return timedListingPrice;
	}

	@Override
	public void setTimedListingPrice(final double d)
	{
		timedListingPrice = d;
	}

	@Override
	public double timedListingPct()
	{
		return timedListingPct;
	}

	@Override
	public void setTimedListingPct(final double d)
	{
		timedListingPct = d;
	}

	@Override
	public double timedFinalCutPct()
	{
		return timedFinalCutPct;
	}

	@Override
	public void setTimedFinalCutPct(final double d)
	{
		timedFinalCutPct = d;
	}

	@Override
	public int maxTimedAuctionDays()
	{
		return maxTimedAuctionDays;
	}

	@Override
	public void setMaxTimedAuctionDays(final int d)
	{
		maxTimedAuctionDays = d;
	}

	@Override
	public int minTimedAuctionDays()
	{
		return minTimedAuctionDays;
	}

	@Override
	public void setMinTimedAuctionDays(final int d)
	{
		minTimedAuctionDays = d;
	}

	@Override
	public long getWhatIsSoldMask()
	{
		return DEAL_AUCTIONEER;
	}

	@Override
	public boolean isSold(final int mask)
	{
		return mask == ShopKeeper.DEAL_AUCTIONEER;
	}

	@Override
	public void setWhatIsSoldMask(final long newSellCode)
	{
	}

	@Override
	public void addSoldType(final int mask)
	{
	}

	@Override
	public Set<ViewType> viewFlags()
	{
		return viewTypes;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;
		//TODO: reconsider how this big this synchro block is, plz
		synchronized(CMClass.getSync(("AUCTION_HOUSE_"+auctionHouse().toUpperCase().trim())))
		{
			final Long lastTime=StdAuctioneer.lastCheckTimes.get(auctionHouse().toUpperCase().trim());
			if((lastTime==null)||(System.currentTimeMillis()-lastTime.longValue())>(CMProps.getMillisPerMudHour()-5))
			{
				if(!CMLib.flags().isInTheGame(this,true))
					return true;
				StdAuctioneer.lastCheckTimes.remove(auctionHouse().toUpperCase().trim());
				final long thisTime=System.currentTimeMillis();
				StdAuctioneer.lastCheckTimes.put(auctionHouse().toUpperCase().trim(),Long.valueOf(thisTime));
				for(final Enumeration<AuctionData> a=CMLib.coffeeShops().getAuctions(null,auctionHouse());a.hasMoreElements();)
				{
					final AuctionData data=a.nextElement();
					if(thisTime>=data.getAuctionTickDown())
					{
						if(thisTime > data.getAuctionTickDown() + TimeManager.MILI_MONTH)
							CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
						else
						if((lastTime==null)
						||(data.getAuctionTickDown()>lastTime.longValue()))
						{
							if(data.getHighBidderMob()!=null)
							{
								//CMLib.coffeeShops().returnMoney(data.getAuctioningM(),data.getCurrency(),finalAmount);
								CMLib.coffeeShops().auctionNotify(data.getAuctioningMob(),"Your auction for "+data.getAuctionedItem().name()+" sold to "
										+data.getHighBidderMob().Name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBid())
										+".  When the high bidder comes to claim "+data.getHighBidderMob().charStats().hisher()
										+" property, you will automatically receive your payment along with another notice.",data.getAuctionedItem().Name());
								CMLib.coffeeShops().auctionNotify(data.getHighBidderMob(),"You won the auction for "+data.getAuctionedItem().name()+" for "
										+CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBid())+".  Your winnings, along with the " +
										"difference from your high bid ("+CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getHighBid()-data.getBid())
										+") will be given to you as soon as you claim your property.  To claim your winnings, come to "+name()
										+" at "+location().displayText(data.getAuctioningMob())+" and enter the BUY command for the item again (you " +
										"will not be charged).",data.getAuctionedItem().Name());
							}
							else
							if(data.getAuctioningMob()!=null)
							{
								CMLib.coffeeShops().auctionNotify(data.getAuctioningMob(),"Your auction for "+data.getAuctionedItem().name()+" went unsold.  '"+data.getAuctionedItem().name()
										+"' has been automatically returned to your inventory.",data.getAuctionedItem().Name());
								data.getAuctioningMob().moveItemTo(data.getAuctionedItem());
								if(!CMLib.flags().isInTheGame(data.getAuctioningMob(),true))
									CMLib.database().DBUpdatePlayerItems(data.getAuctioningMob());
								CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							}
							else
							{
								CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void autoGive(final MOB src, final MOB tgt, final Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	protected String parseBidString(final String targetMessage)
	{
		int x=-1;
		if(targetMessage!=null)
		{
			x=targetMessage.indexOf('\'');
			if(x>=0)
			{
				final int y=targetMessage.indexOf('\'',x+1);
				if(y>x)
					return targetMessage.substring(x+1,y);
			}
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target()==location())
		&&(CMLib.flags().isInTheGame(this,true)))
			return false;
		else
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_SELL:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(!(msg.tool() instanceof Item))
					{
						CMLib.commands().postSay(this,mob,L("I can't seem to auction @x1.",msg.tool().name()),true,false);
						return false;
					}
					if(msg.source().isMonster())
					{
						CMLib.commands().postSay(this,mob,L("You can't sell anything."),true,false);
						return false;
					}
					final Item I=(Item)msg.tool();
					if((I instanceof Container)&&(((Container)I).hasContent()))
					{
						CMLib.commands().postSay(this,mob,L("@x1 will have to be emptied first.",I.name()),true,false);
						return false;
					}
					if(!(I.amWearingAt(Wearable.IN_INVENTORY)))
					{
						CMLib.commands().postSay(this,mob,L("@x1 will have to be removed first.",I.name()),true,false);
						return false;
					}
					final AuctionPolicy aRates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
					aRates.mergeAuctioneerPolicy(this);
					CMLib.commands().postSay(this,mob,L("Ok, so how many local days will your auction run for (@x1-@x2)?",""+aRates.minTimedAuctionDays(),""+aRates.maxTimedAuctionDays()),true,false);
					int days=0;
					try
					{
						days=CMath.s_int(mob.session().prompt(":","",30000));
					}
					catch(final Exception e)
					{
						return false;
					}
					if(days==0)
						return false;
					if(days<aRates.minTimedAuctionDays())
					{
						CMLib.commands().postSay(this,mob,L("Minimum number of local days on an auction is @x1.",""+aRates.minTimedAuctionDays()),true,false);
						return false;
					}
					if(days>aRates.maxTimedAuctionDays())
					{
						CMLib.commands().postSay(this,mob,L("Maximum number of local days on an auction is @x1.",""+aRates.maxTimedAuctionDays()),true,false);
						return false;
					}
					double deposit=aRates.timedListingPrice();
					deposit+=(aRates.timedListingPct()*(CMath.mul(days,I.baseGoldValue())));
					final String depositAmt=CMLib.beanCounter().nameCurrencyLong(this, deposit);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(this))<deposit)
					{
						CMLib.commands().postSay(this,mob,L("You don't have enough to cover the listing fee of @x1.  Sell a cheaper item, use fewer days, or come back later.",depositAmt),true,false);
						return false;
					}
					CMLib.commands().postSay(this,mob,L("Auctioning @x1 will cost a listing fee of @x2, proceed?",I.name(),depositAmt),true,false);
					try
					{
						if(!mob.session().confirm(L("(Y/N):"),"Y",10000))
							return false;
					}
					catch(final Exception e)
					{
						return false;
					}
					lastMsgData=(AuctionData)CMClass.getCommon("DefaultAuction");
					lastMsgData.setAuctionedItem((Item)msg.tool());
					lastMsgData.setAuctioningMob(msg.source());
					lastMsgData.setAuctioningMobName(null);
					lastMsgData.setCurrency(CMLib.beanCounter().getCurrency(this));
					Area area=CMLib.map().getStartArea(this);
					if(area==null)
						area=CMLib.map().getStartArea(msg.source());
					lastMsgData.setAuctionTickDown(System.currentTimeMillis()+(days*area.getTimeObj().getHoursInDay()*CMProps.getMillisPerMudHour())+60000);
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_BID:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),getFinalIgnoreMask(),this))
						return false;
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					final String bidStr=parseBidString(msg.targetMessage());
					if(bidStr==null)
					{
						CMLib.commands().postSay(this,mob,L("I can't seem to do business with you."),true,false);
						return false;
					}
					if((msg.tool() instanceof Physical)
					&&(((Physical)msg.tool()).phyStats().level()>msg.source().phyStats().level()))
					{
						CMLib.commands().postSay(this,msg.source(),L("That's too advanced for you, I'm afraid."),true,false);
						return false;
					}
					String itemName=msg.tool().name();
					if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
						itemName+="."+((Item)msg.tool()).expirationDate();
					AuctionData data=CMLib.coffeeShops().fetchAuctionByItemName(itemName, auctionHouse());
					if(data==null)
						data=CMLib.coffeeShops().fetchAuctionByItemName(msg.tool().name(), auctionHouse());
					if(data==null)
					{
						CMLib.commands().postSay(this,mob,L("That's not up for auction."),true,false);
						return false;
					}
					if(data.getAuctioningMob()==msg.source())
					{
						final AuctionPolicy rates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
						rates.mergeAuctioneerPolicy(this);
						if((rates.minTimedAuctionDays()>0)&&(rates.minTimedAuctionDays()>=data.daysEllapsed(mob,this)))
						{
							CMLib.commands().postSay(this,mob,L("You may not close this auction until it has been active for @x1 days.",""+rates.minTimedAuctionDays()),true,false);
							return false;
						}
						if(msg.source().session()!=null)
						{
							try
							{
								if (!msg.source().session().confirm(L("This will cancel your auction on @x1, are you sure (y/N)?", data.getAuctionedItem().name()), "N", 10000))
									return false;
							}
							catch (final Exception e)
							{
								return false;
							}
						}
					}
					else
					if(System.currentTimeMillis()>=data.getAuctionTickDown())
					{
						if(data.getHighBidderMob()==msg.source())
						{
							CMLib.commands().postSay(this,mob,L("You have won this auction -- use the BUY command to complete the transaction."),true,false);
							return false;
						}
						CMLib.commands().postSay(this,mob,L("That auction is closed."),true,false);
						return false;
					}
					else
					{
						final Triad<String,Double,Long> bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.getCurrency());
						if(bidAmts!=null)
						{
							final String myCurrency=bidAmts.first;
							final double myDenomination=bidAmts.second.doubleValue();
							final long myCoins=bidAmts.third.longValue();
							final double bid=CMath.mul(myCoins,myDenomination);
							if(!CMLib.beanCounter().isCurrencyMatch(myCurrency, data.getCurrency()))
							{
								final String currencyName=CMLib.beanCounter().getDenominationName(data.getCurrency());
								CMLib.commands().postSay(this,mob,L("This auction is being handled in @x1.",currencyName),true,false);
								return false;
							}
							if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.getCurrency())<bid)
							{
								final String currencyName=CMLib.beanCounter().getDenominationName(data.getCurrency());
								CMLib.commands().postSay(this,mob,L("You don't have enough @x1 on hand to cover your bid.",currencyName),true,false);
								return false;
							}
						}
					}
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_BUY:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),getFinalIgnoreMask(),this))
						return false;
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					if((msg.tool() instanceof Physical)
					&&(((Physical)msg.tool()).phyStats().level()>msg.source().phyStats().level()))
					{
						CMLib.commands().postSay(this,msg.source(),L("That's too advanced for you, I'm afraid."),true,false);
						return false;
					}
					String itemName=msg.tool().name();
					if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
						itemName+="."+((Item)msg.tool()).expirationDate();
					AuctionData data=CMLib.coffeeShops().fetchAuctionByItemName(itemName, auctionHouse());
					if(data==null)
						data=CMLib.coffeeShops().fetchAuctionByItemName(msg.tool().name(), auctionHouse());
					if(data==null)
					{
						CMLib.commands().postSay(this,mob,L("That's not up for auction."),true,false);
						return false;
					}
					else
					if(data.getAuctioningMob()==msg.source())
					{
						final AuctionPolicy rates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
						rates.mergeAuctioneerPolicy(this);
						if((rates.minTimedAuctionDays()>0)&&(rates.minTimedAuctionDays()>=data.daysEllapsed(mob,this)))
						{
							CMLib.commands().postSay(this,mob,L("You may not close this auction until it has been active for @x1 days.",""+rates.minTimedAuctionDays()),true,false);
							return false;
						}
						if(msg.source().session()!=null)
						{
							try
							{
								if (!msg.source().session().confirm(L("This will cancel your auction on @x1, are you sure (y/N)?", data.getAuctionedItem().name()), "N", 10000))
									return false;
							}
							catch (final Exception e)
							{
								return false;
							}
						}
					}
					else
					if(System.currentTimeMillis()>=data.getAuctionTickDown())
					{
						if(data.getHighBidderMob()==msg.source())
						{

						}
						else
						{
							CMLib.commands().postSay(this,mob,L("That auction is closed."),true,false);
							return false;
						}
					}
					else
					{
						if(data.getBuyOutPrice()<=0.0)
						{
							CMLib.commands().postSay(this,mob,L("You'll have to BID on that.  BUY is not available for that particular item."),true,false);
							return false;
						}
						else
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.getCurrency())<data.getBuyOutPrice())
						{
							final String currencyName=CMLib.beanCounter().getDenominationName(data.getCurrency());
							CMLib.commands().postSay(this,mob,L("You don't have enough @x1 on hand to buy that.",currencyName),true,false);
							return false;
						}
					}
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_VALUE:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),getFinalIgnoreMask(),this))
						return false;
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_VIEW:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),getFinalIgnoreMask(),this))
						return false;
					return super.okMessage(myHost, msg);
				}
				return false;
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_SELL:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					final AuctionData thisData=lastMsgData;
					if((thisData==null)
					||(msg.source().isMonster())
					||(thisData.getAuctioningMob()!=msg.source()))
					{
						lastMsgData=null;
						CMLib.commands().postSay(this,mob,L("I'm confused. Please try to SELL again."),true,false);
					}
					else
					try
					{
						final double lowestDenom=CMLib.beanCounter().getLowestDenomination(thisData.getCurrency());
						CMLib.commands().postSay(this,mob,L("What would you like your opening price to be (in @x1?",CMLib.beanCounter().getDenominationName(thisData.getCurrency(), lowestDenom)),true,false);
						final String openPrice=mob.session().prompt(": ",30000);
						CMLib.commands().postSay(this,mob,L("What would you like your buy-now price to be (in @x1?",CMLib.beanCounter().getDenominationName(thisData.getCurrency(), lowestDenom)),true,false);
						final String buyPrice=mob.session().prompt(": ",30000);
						thisData.setBid(CMath.s_double(openPrice)*lowestDenom);
						if(thisData.getBid()<0.0)
							thisData.setBid(0.0);
						thisData.setBuyOutPrice(CMath.s_double(buyPrice)*lowestDenom);
						if(thisData.getBuyOutPrice()<=0.0)
							thisData.setBuyOutPrice(-1.0);
						thisData.setStartTime(System.currentTimeMillis());
						CMLib.coffeeShops().saveAuction(thisData, auctionHouse(),false);
						CMLib.commands().postChannel(this,"AUCTION",L("New @x1 day auction: @x2",""+thisData.daysRemaining(thisData.getAuctioningMob(),msg.source()),thisData.getAuctionedItem().name()),true);
						final AuctionPolicy aRates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
						aRates.mergeAuctioneerPolicy(this);
						double deposit=aRates.timedListingPrice();
						deposit+=(aRates.timedListingPct()*(CMath.mul(thisData.daysRemaining(mob,this),thisData.getAuctionedItem().baseGoldValue())));
						CMLib.beanCounter().subtractMoney(mob,deposit);
						thisData.getAuctionedItem().destroy();
						CMLib.commands().postSay(this,mob,L("Your auction for @x1 is now open.  When it is done, you will receive either your winnings automatically, or the returned item automatically.",thisData.getAuctionedItem().name()),true,false);
					}
					catch(final Exception e)
					{
					}
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Item)
					{
						String itemName=msg.tool().name();
						if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
							itemName+="."+((Item)msg.tool()).expirationDate();
						AuctionData data=CMLib.coffeeShops().fetchAuctionByItemName(itemName, auctionHouse());
						if(data==null)
							data=CMLib.coffeeShops().fetchAuctionByItemName(msg.tool().name(), auctionHouse());
						if(data==null)
							CMLib.commands().postSay(this,mob,L("That's not up for auction."),true,false);
						else
						if(data.getAuctioningMob()==mob)
						{
							CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							CMLib.commands().postSay(this,mob,L("Your auction for @x1 has been canceled.",data.getAuctionedItem().name()),true,false);
						}
						else
						if((System.currentTimeMillis()>=data.getAuctionTickDown())&&(data.getHighBidderMob()==mob))
						{
							final AuctionPolicy aRates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
							aRates.mergeAuctioneerPolicy(this);
							final double houseCut=Math.floor(data.getBid()*aRates.timedFinalCutPct());
							final double finalAmount=data.getBid()-houseCut;
							CMLib.coffeeShops().returnMoney(data.getAuctioningMob(),data.getCurrency(),finalAmount);
							CMLib.coffeeShops().auctionNotify(data.getAuctioningMob(),
									data.getHighBidderMob().Name()+", who won your auction for "+data.getAuctionedItem().name()+" has claimed "+
									data.getHighBidderMob().charStats().hisher()+" property.  You have been credited with "+
									CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),finalAmount)+", after the house took a cut of "+
									CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),houseCut)+".",data.getAuctionedItem().Name());
							/*
							 * CMLib.coffeeShops().auctionNotify(data.getHighBidderM(),
									"You won the auction for "+data.getAuctioningI().name()+" for "+
									CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBid())+
									".  The difference from your high bid ("+
									CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getHighBid()-data.getBid())+
									") has been returned to you along with the winning item.",data.getAuctioningI().Name());
							*/
							if((data.getHighBid()-data.getBid())>0.0)
							{
								CMLib.commands().postSay(this,mob,L("Congratulations, and here is your @x1 in change as well.",
									CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getHighBid()-data.getBid())),true,false);
							}
							else
								CMLib.commands().postSay(this,mob,L("Congratulations!"),true,false);
							CMLib.coffeeShops().returnMoney(mob,data.getCurrency(),data.getHighBid()-data.getBid());
							CMLib.coffeeShops().purchaseItems(data.getAuctionedItem(),new XVector<Environmental>(data.getAuctionedItem()),this,mob);
							if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
								mob.location().recoverRoomStats();
							CMLib.database().DBDeleteJournal(auctionHouse(),data.getAuctionDBKey());
						}
						else
						if(System.currentTimeMillis()<data.getAuctionTickDown())
						{
							final AuctionPolicy aRates=(AuctionPolicy)CMClass.getCommon("DefaultAuctionPolicy");
							aRates.mergeAuctioneerPolicy(this);
							final double houseCut=Math.floor(data.getBuyOutPrice()*aRates.timedFinalCutPct());
							final double finalAmount=data.getBuyOutPrice()-houseCut;
							CMLib.coffeeShops().returnMoney(data.getAuctioningMob(),data.getCurrency(),finalAmount);
							CMLib.coffeeShops().auctionNotify(data.getAuctioningMob(),"Your auction for "+
								data.getAuctionedItem().name()+" sold to "+mob.Name()+" for "+
								CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBuyOutPrice())+
								", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),houseCut)+
								".",data.getAuctionedItem().Name());
							CMLib.beanCounter().subtractMoney(mob,data.getCurrency(),data.getBuyOutPrice());
							CMLib.coffeeShops().purchaseItems(data.getAuctionedItem(),new XVector<Environmental>(data.getAuctionedItem()),this,mob);
							CMLib.database().DBDeleteJournal(auctionHouse(),data.getAuctionDBKey());
						}
					}
					else
						CMLib.commands().postSay(this,mob,L("I can't seem to auction @x1.",msg.tool().name()),true,false);
				}
				break;
			case CMMsg.TYP_BID:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Item)
					{
						String itemName=msg.tool().name();
						if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
							itemName+="."+((Item)msg.tool()).expirationDate();
						AuctionData data=CMLib.coffeeShops().fetchAuctionByItemName(itemName, auctionHouse());
						if(data==null)
							data=CMLib.coffeeShops().fetchAuctionByItemName(msg.tool().name(), auctionHouse());
						if(data==null)
							CMLib.commands().postSay(this,mob,L("That's not up for auction."),true,false);
						else
						if(data.getAuctioningMob()==mob)
						{
							CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							CMLib.commands().postSay(this,mob,L("Your auction for @x1 has been canceled.",data.getAuctionedItem().name()),true,false);
						}
						else
						{
							final String bidStr=parseBidString(msg.targetMessage());
							if(bidStr==null)
							{
								CMLib.commands().postSay(this,mob,L("I can't seem to do business with you."),true,false);
								return;
							}
							final Triad<String,Double,Long> bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.getCurrency());
							if(bidAmts==null)
							{
								CMLib.commands().postSay(this,mob,L("I can't accept '@x1' as a bid.  This auction is in @x2.",bidStr,
									CMLib.beanCounter().getDenominationName(data.getCurrency())),true,false);
								return;
							}
							final String myCurrency=bidAmts.first;
							final double myDenomination=bidAmts.second.doubleValue();
							final long myCoins=bidAmts.third.longValue();
							final double bid=CMath.mul(myCoins,myDenomination);
							final MOB M=data.getHighBidderMob();
							final double oldBid=data.getBid();
							final double oldMaxBid=data.getHighBid();
							final String[] resp=CMLib.coffeeShops().bid(mob, bid, myCurrency,data, data.getAuctionedItem(),new Vector<String>(0));
							if(resp!=null)
							{
								if(resp[0]!=null)
									mob.tell(resp[0]);
								if((resp[1]!=null)&&(M!=null))
									CMLib.coffeeShops().auctionNotify(M,resp[1],data.getAuctionedItem().name());
							}
							if((oldBid!=data.getBid())||(oldMaxBid!=data.getHighBid()))
								CMLib.coffeeShops().saveAuction(data, auctionHouse(),true);
						}
					}
					else
						CMLib.commands().postSay(this,mob,L("I can't seem to auction @x1.",msg.tool().name()),true,false);
				}
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					CMLib.commands().postSay(this,mob,L("That's for the people to decide.  Why don't you use the SELL command and see what you can get?"),true,false);
					return;
				}
				break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if(msg.tool() instanceof Item)
				{
					String itemName=msg.tool().name();
					if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
						itemName+="."+((Item)msg.tool()).expirationDate();
					AuctionData data=CMLib.coffeeShops().fetchAuctionByItemName(itemName, auctionHouse());
					if(data==null)
						data=CMLib.coffeeShops().fetchAuctionByItemName(msg.tool().name(), auctionHouse());
					if(data==null)
						CMLib.commands().postSay(this,mob,L("That's not up for auction."),true,false);
					else
					{
						final String price=CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBid());
						final String buyOut=(data.getBuyOutPrice()<=0.0)?null:CMLib.beanCounter().nameCurrencyShort(data.getCurrency(),data.getBuyOutPrice());
						final StringBuffer str=new StringBuffer(
							L("Interested in @x2? Here is some information for you: @x1\n\r\n\rThe current bid on @x2 is @x3. Use the BID command to place your own bid.  ",
							CMLib.coffeeShops().getViewDescription(mob,msg.tool(), viewFlags()),msg.tool().name(),price));
						if(buyOut!=null)
							str.append(L("You may also buy this item immediately for @x1 by using the BUY command.",buyOut));
						CMLib.commands().postSay(this,mob,str.toString(),true,false);
					}
				}
				break;
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					final String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
					final String s=CMLib.coffeeShops().getAuctionInventory(this,mob,this,forMask);
					if(s.length()>0)
						mob.tell(s);
				}
				return;
			}
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
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
		return "";
	}

	@Override
	public void setPrejudiceFactors(final String factors)
	{
		//misc text already being used
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
		if ((getRawItemPricingAdjustments() != null) && (getRawItemPricingAdjustments().length > 0))
			return getRawItemPricingAdjustments();
		return getStartArea().getFinalItemPricingAdjustments();
	}

	@Override
	public String[] getRawItemPricingAdjustments()
	{
		return new String[0];
	}

	@Override
	public void setItemPricingAdjustments(final String[] factors)
	{
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

	@Override
	public Pair<Long, TimePeriod> getFinalBudget()
	{
		return getStartArea().getFinalBudget();
	}

	@Override
	public String getRawBbudget()
	{
		return "";
	}

	@Override
	public void setBudget(final String factors)
	{
	}

	@Override
	public double[] getFinalDevalueRate()
	{
		return getStartArea().getFinalDevalueRate();
	}

	@Override
	public String getRawDevalueRate()
	{
		return "";
	}

	@Override
	public void setDevalueRate(final String factors)
	{
	}


	@Override
	public void setWhatIsSoldZappermask(final String newSellMask)
	{
	}

	@Override
	public String getWhatIsSoldZappermask()
	{
		return "";
	}

	@Override
	public int getFinalInvResetRate()
	{
		if(getRawInvResetRate()!=0)
			return getRawInvResetRate();
		return getStartArea().getFinalInvResetRate();
	}

	@Override
	public int getRawInvResetRate()
	{
		return 0;
	}

	@Override
	public void setInvResetRate(final int ticks)
	{
	}

	@Override
	public double liveListingPrice()
	{
		return 0;
	}

	@Override
	public void setLiveListingPrice(final double d)
	{
	}

	@Override
	public double liveFinalCutPct()
	{
		return 0;
	}

	@Override
	public void setLiveFinalCutPct(final double d)
	{

	}

	@Override
	public void mergeAuctioneerPolicy(final Auctioneer auction)
	{
	}

}
