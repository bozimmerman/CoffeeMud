package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdAuctioneer extends StdMOB implements Auctioneer
{
	@Override public String ID(){return "StdAuctioneer";}

	public StdAuctioneer()
	{
		super();
		username="an auctioneer";
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

	public AuctionData lastMsgData=null;
	protected static final Hashtable lastCheckTimes=new Hashtable();

	@Override
	public CoffeeShop getShop()
	{
		final CoffeeShop shop=((CoffeeShop)CMClass.getCommon("AuctionCoffeeShop")).build(this);
		shop.addStoreInventory(null);
		return shop;
	}

	@Override public String auctionHouse(){return text();}
	@Override public void setAuctionHouse(String name){setMiscText(name);}

	protected double timedListingPrice=-1.0;
	@Override public double timedListingPrice(){return timedListingPrice;}
	@Override public void setTimedListingPrice(double d){timedListingPrice=d;}

	protected double timedListingPct=-1.0;
	@Override public double timedListingPct(){return timedListingPct;}
	@Override public void setTimedListingPct(double d){timedListingPct=d;}

	protected double timedFinalCutPct=-1.0;
	@Override public double timedFinalCutPct(){return timedFinalCutPct;}
	@Override public void setTimedFinalCutPct(double d){timedFinalCutPct=d;}

	protected int maxTimedAuctionDays=-1;
	@Override public int maxTimedAuctionDays(){return maxTimedAuctionDays;}
	@Override public void setMaxTimedAuctionDays(int d){maxTimedAuctionDays=d;}

	protected int minTimedAuctionDays=-1;
	@Override public int minTimedAuctionDays(){return minTimedAuctionDays;}
	@Override public void setMinTimedAuctionDays(int d){minTimedAuctionDays=d;}

	@Override public long getWhatIsSoldMask(){ return DEAL_AUCTIONEER;}
	@Override public boolean isSold(int mask){return mask==ShopKeeper.DEAL_AUCTIONEER;}
	@Override public void setWhatIsSoldMask(long newSellCode){ }
	@Override public void addSoldType(int mask){}


	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)) return true;
		if(CMLib.flags().isInTheGame(this,true))
		synchronized(("AUCTION_HOUSE_"+auctionHouse().toUpperCase().trim()).intern())
		{
			final Long lastTime=(Long)StdAuctioneer.lastCheckTimes.get(auctionHouse().toUpperCase().trim());
			if((lastTime==null)||(System.currentTimeMillis()-lastTime.longValue())>(CMProps.getMillisPerMudHour()-5))
			{
				StdAuctioneer.lastCheckTimes.remove(auctionHouse().toUpperCase().trim());
				final long thisTime=System.currentTimeMillis();
				StdAuctioneer.lastCheckTimes.put(auctionHouse().toUpperCase().trim(),Long.valueOf(thisTime));
				final List<AuctionData> auctions=CMLib.coffeeShops().getAuctions(null, auctionHouse());
				for(int a=0;a<auctions.size();a++)
				{
					final Auctioneer.AuctionData data=auctions.get(a);
					if(thisTime>=data.tickDown)
					{
						if((lastTime==null)||(data.tickDown>lastTime.longValue()))
						{
							if(data.highBidderM!=null)
							{
								//CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
								CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" sold to "
										+data.highBidderM.Name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)
										+".  When the high bidder comes to claim "+data.highBidderM.charStats().hisher()
										+" property, you will automatically receive your payment along with another notice.",data.auctioningI.Name());
								CMLib.coffeeShops().auctionNotify(data.highBidderM,"You won the auction for "+data.auctioningI.name()+" for "
										+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)+".  Your winnings, along with the " +
										"difference from your high bid ("+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)
										+") will be given to you as soon as you claim your property.  To claim your winnings, come to "+name()
										+" at "+location().displayText(data.auctioningM)+" and enter the BUY command for the item again (you " +
										"will not be charged).",data.auctioningI.Name());
							}
							else
							if(data.auctioningM!=null)
							{
								CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" went unsold.  '"+data.auctioningI.name()
										+"' has been automatically returned to your inventory.",data.auctioningI.Name());
								data.auctioningM.moveItemTo(data.auctioningI);
								if(!CMLib.flags().isInTheGame(data.auctioningM,true))
									CMLib.database().DBUpdatePlayerItems(data.auctioningM);
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

	public void autoGive(MOB src, MOB tgt, Item I)
	{
		CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
		msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
		location().send(this,msg2);
	}

	protected String parseBidString(String targetMessage)
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
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(!(msg.tool() instanceof Item))
					{
						CMLib.commands().postSay(this,mob,"I can't seem to auction "+msg.tool().name()+".",true,false);
						return false;
					}
					if(msg.source().isMonster())
					{
						CMLib.commands().postSay(this,mob,"You can't sell anything.",true,false);
						return false;
					}
					final Item I=(Item)msg.tool();
					if((I instanceof Container)&&(((Container)I).getContents().size()>0))
					{
						CMLib.commands().postSay(this,mob,I.name()+" will have to be emptied first.",true,false);
						return false;
					}
					if(!(I.amWearingAt(Wearable.IN_INVENTORY)))
					{
						CMLib.commands().postSay(this,mob,I.name()+" will have to be removed first.",true,false);
						return false;
					}
					final AuctionRates aRates=new AuctionRates(this);
					CMLib.commands().postSay(this,mob,"Ok, so how many local days will your auction run for ("+aRates.minDays+"-"+aRates.maxDays+")?",true,false);
					int days=0;
					try{days=CMath.s_int(mob.session().prompt(":","",30000));}catch(final Exception e){return false;}
					if(days==0) return false;
					if(days<aRates.minDays)
					{
						CMLib.commands().postSay(this,mob,"Minimum number of local days on an auction is "+aRates.minDays+".",true,false);
						return false;
					}
					if(days>aRates.maxDays)
					{
						CMLib.commands().postSay(this,mob,"Maximum number of local days on an auction is "+aRates.maxDays+".",true,false);
						return false;
					}
					double deposit=aRates.timeListPrice;
					deposit+=(aRates.timeListPct*(CMath.mul(days,I.baseGoldValue())));
					final String depositAmt=CMLib.beanCounter().nameCurrencyLong(mob, deposit);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(mob))<deposit)
					{
						CMLib.commands().postSay(this,mob,"You don't have enough to cover the listing fee of "+depositAmt+".  Sell a cheaper item, use fewer days, or come back later.",true,false);
						return false;
					}
					CMLib.commands().postSay(this,mob,"Auctioning "+I.name()+" will cost a listing fee of "+depositAmt+", proceed?",true,false);
					try{if(!mob.session().confirm(_("(Y/N):"),_("Y"),10000)) return false;}catch(final Exception e){return false;}
					lastMsgData=new AuctionData();
					lastMsgData.auctioningI=(Item)msg.tool();
					lastMsgData.auctioningM=msg.source();
					lastMsgData.currency=CMLib.beanCounter().getCurrency(msg.source());
					Area area=CMLib.map().getStartArea(this);
					if(area==null) area=CMLib.map().getStartArea(msg.source());
					lastMsgData.tickDown=System.currentTimeMillis()+(days*area.getTimeObj().getHoursInDay()*CMProps.getMillisPerMudHour())+60000;
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_BID:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					final String bidStr=parseBidString(msg.targetMessage());
					if(bidStr==null)
					{
						CMLib.commands().postSay(this,mob,"I can't seem to do business with you.",true,false);
						return false;
					}
					if((msg.tool() instanceof Physical)
					&&(((Physical)msg.tool()).phyStats().level()>msg.source().phyStats().level()))
					{
						CMLib.commands().postSay(this,msg.source(),"That's too advanced for you, I'm afraid.",true,false);
						return false;
					}
					String itemName=msg.tool().name();
					if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
						itemName+="."+((Item)msg.tool()).expirationDate();
					AuctionData data=CMLib.coffeeShops().getEnumeratedAuction(itemName, auctionHouse());
					if(data==null) data=CMLib.coffeeShops().getEnumeratedAuction(msg.tool().name(), auctionHouse());
					if(data==null)
					{
						CMLib.commands().postSay(this,mob,"That's not up for auction.",true,false);
						return false;
					}
					if(data.auctioningM==msg.source())
					{
						final Auctioneer.AuctionRates rates=new Auctioneer.AuctionRates(this);
						if((rates.minDays>0)&&(rates.minDays>=data.daysEllapsed(mob,this)))
						{
							CMLib.commands().postSay(this,mob,"You may not close this auction until it has been active for "+rates.minDays+" days.",true,false);
							return false;
						}
						if(msg.source().session()!=null)
						{
							try
							{
							if(!msg.source().session().confirm(_("This will cancel your auction on @x1, are you sure (y/N)?",data.auctioningI.name()),_("N"),10000))
								return false;
							}catch(final Exception e){return false;}
						}
					}
					else
					if(System.currentTimeMillis()>=data.tickDown)
					{
						if(data.highBidderM==msg.source())
						{
							CMLib.commands().postSay(this,mob,"You have won this auction -- use the BUY command to complete the transaction.",true,false);
							return false;
						}
						CMLib.commands().postSay(this,mob,"That auction is closed.",true,false);
						return false;
					}
					else
					{
						final Object[] bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.currency);
						final String myCurrency=(String)bidAmts[0];
						final double myDenomination=((Double)bidAmts[1]).doubleValue();
						final long myCoins=((Long)bidAmts[2]).longValue();
						final double bid=CMath.mul(myCoins,myDenomination);
						if(!myCurrency.equals(data.currency))
						{
							final String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
							CMLib.commands().postSay(this,mob,"This auction is being handled in "+currencyName+".",true,false);
							return false;
						}
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.currency)<bid)
						{
							final String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
							CMLib.commands().postSay(this,mob,"You don't have enough "+currencyName+" on hand to cover your bid.",true,false);
							return false;
						}
					}
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_BUY:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					if((msg.tool() instanceof Physical)
					&&(((Physical)msg.tool()).phyStats().level()>msg.source().phyStats().level()))
					{
						CMLib.commands().postSay(this,msg.source(),"That's too advanced for you, I'm afraid.",true,false);
						return false;
					}
					String itemName=msg.tool().name();
					if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
						itemName+="."+((Item)msg.tool()).expirationDate();
					AuctionData data=CMLib.coffeeShops().getEnumeratedAuction(itemName, auctionHouse());
					if(data==null) data=CMLib.coffeeShops().getEnumeratedAuction(msg.tool().name(), auctionHouse());
					if(data==null)
					{
						CMLib.commands().postSay(this,mob,"That's not up for auction.",true,false);
						return false;
					}
					else
					if(data.auctioningM==msg.source())
					{
						final Auctioneer.AuctionRates rates=new Auctioneer.AuctionRates(this);
						if((rates.minDays>0)&&(rates.minDays>=data.daysEllapsed(mob,this)))
						{
							CMLib.commands().postSay(this,mob,"You may not close this auction until it has been active for "+rates.minDays+" days.",true,false);
							return false;
						}
						if(msg.source().session()!=null)
						{
							try
							{
							if(!msg.source().session().confirm(_("This will cancel your auction on @x1, are you sure (y/N)?",data.auctioningI.name()),_("N"),10000))
								return false;
							}catch(final Exception e){return false;}
						}
					}
					else
					if(System.currentTimeMillis()>=data.tickDown)
					{
						if(data.highBidderM==msg.source())
						{

						}
						else
						{
							CMLib.commands().postSay(this,mob,"That auction is closed.",true,false);
							return false;
						}
					}
					else
					{
						if(data.buyOutPrice<=0.0)
						{
							CMLib.commands().postSay(this,mob,"You'll have to BID on that.  BUY is not available for that particular item.",true,false);
							return false;
						}
						else
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.currency)<data.buyOutPrice)
						{
							final String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
							CMLib.commands().postSay(this,mob,"You don't have enough "+currencyName+" on hand to buy that.",true,false);
							return false;
						}
					}
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_VALUE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					return super.okMessage(myHost, msg);
				}
				return false;
			case CMMsg.TYP_VIEW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
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
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					final AuctionData thisData=lastMsgData;
					if((thisData==null)||(thisData.auctioningM!=msg.source())||(msg.source().isMonster()))
					{
						lastMsgData=null;
						CMLib.commands().postSay(this,mob,"I'm confused. Please try to SELL again.",true,false);
					}
					else
					try
					{
						final double lowestDenom=CMLib.beanCounter().getLowestDenomination(thisData.currency);
						CMLib.commands().postSay(this,mob,"What would you like your opening price to be (in "+CMLib.beanCounter().getDenominationName(thisData.currency, lowestDenom)+"?",true,false);
						final String openPrice=mob.session().prompt(": ",30000);
						CMLib.commands().postSay(this,mob,"What would you like your buy-now price to be (in "+CMLib.beanCounter().getDenominationName(thisData.currency, lowestDenom)+"?",true,false);
						final String buyPrice=mob.session().prompt(": ",30000);
						thisData.bid=CMath.s_double(openPrice)*lowestDenom;
						if(thisData.bid<0.0) thisData.bid=0.0;
						thisData.buyOutPrice=CMath.s_double(buyPrice)*lowestDenom;
						if(thisData.buyOutPrice<=0.0) thisData.buyOutPrice=-1.0;
						thisData.start=System.currentTimeMillis();
						CMLib.coffeeShops().saveAuction(thisData, auctionHouse(),false);
						CMLib.commands().postChannel(this,"AUCTION","New "+thisData.daysRemaining(thisData.auctioningM,msg.source())+" day auction: "+thisData.auctioningI.name(),true);
						final AuctionRates aRates=new AuctionRates(this);
						double deposit=aRates.timeListPrice;
						deposit+=(aRates.timeListPct*(CMath.mul(thisData.daysRemaining(mob,this),thisData.auctioningI.baseGoldValue())));
						CMLib.beanCounter().subtractMoney(mob,deposit);
						thisData.auctioningI.destroy();
						CMLib.commands().postSay(this,mob,"Your auction for "+thisData.auctioningI.name()+" is now open.  When it is done, you will receive either your winnings automatically, or the returned item automatically.",true,false);
					}catch(final Exception e){}
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Item)
					{
						String itemName=msg.tool().name();
						if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
							itemName+="."+((Item)msg.tool()).expirationDate();
						AuctionData data=CMLib.coffeeShops().getEnumeratedAuction(itemName, auctionHouse());
						if(data==null) data=CMLib.coffeeShops().getEnumeratedAuction(msg.tool().name(), auctionHouse());
						if(data==null)
							CMLib.commands().postSay(this,mob,"That's not up for auction.",true,false);
						else
						if(data.auctioningM==mob)
						{
							CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							CMLib.commands().postSay(this,mob,"Your auction for "+data.auctioningI.name()+" has been canceled.",true,false);
						}
						else
						if((System.currentTimeMillis()>=data.tickDown)&&(data.highBidderM==mob))
						{
							final Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
							final double houseCut=Math.floor(data.bid*aRates.timeCutPct);
							final double finalAmount=data.bid-houseCut;
							CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
							CMLib.coffeeShops().auctionNotify(data.auctioningM,data.highBidderM.Name()+", who won your auction for "+data.auctioningI.name()+" has claimed "+data.highBidderM.charStats().hisher()+" property.  You have been credited with "+CMLib.beanCounter().nameCurrencyShort(data.currency,finalAmount)+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(data.currency,houseCut)+".",data.auctioningI.Name());
							//CMLib.coffeeShops().auctionNotify(data.highBidderM,"You won the auction for "+data.auctioningI.name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)+".  The difference from your high bid ("+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)+") has been returned to you along with the winning item.",data.auctioningI.Name());
							if((data.highBid-data.bid)>0.0)
								CMLib.commands().postSay(this,mob,"Congratulations, and here is your "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)+" in change as well.",true,false);
							else
								CMLib.commands().postSay(this,mob,"Congratulations!",true,false);
							CMLib.coffeeShops().returnMoney(mob,data.currency,data.highBid-data.bid);
							CMLib.coffeeShops().purchaseItems(data.auctioningI,new XVector(data.auctioningI),this,mob);
							if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
								mob.location().recoverRoomStats();
							CMLib.database().DBDeleteJournal(auctionHouse(),data.auctionDBKey);
						}
						else
						if(System.currentTimeMillis()<data.tickDown)
						{
							final Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
							final double houseCut=Math.floor(data.buyOutPrice*aRates.timeCutPct);
							final double finalAmount=data.buyOutPrice-houseCut;
							CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
							CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" sold to "+mob.Name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.buyOutPrice)+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(data.currency,houseCut)+".",data.auctioningI.Name());
							CMLib.beanCounter().subtractMoney(mob,data.currency,data.buyOutPrice);
							CMLib.coffeeShops().purchaseItems(data.auctioningI,new XVector(data.auctioningI),this,mob);
							CMLib.database().DBDeleteJournal(auctionHouse(),data.auctionDBKey);
						}
					}
					else
						CMLib.commands().postSay(this,mob,"I can't seem to auction "+msg.tool().name()+".",true,false);
				}
				break;
			case CMMsg.TYP_BID:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Item)
					{
						String itemName=msg.tool().name();
						if((((Item)msg.tool()).expirationDate()>0)&&(((Item)msg.tool()).expirationDate()<1000))
							itemName+="."+((Item)msg.tool()).expirationDate();
						AuctionData data=CMLib.coffeeShops().getEnumeratedAuction(itemName, auctionHouse());
						if(data==null) data=CMLib.coffeeShops().getEnumeratedAuction(msg.tool().name(), auctionHouse());
						if(data==null)
							CMLib.commands().postSay(this,mob,"That's not up for auction.",true,false);
						else
						if(data.auctioningM==mob)
						{
							CMLib.coffeeShops().cancelAuction(auctionHouse(), data);
							CMLib.commands().postSay(this,mob,"Your auction for "+data.auctioningI.name()+" has been canceled.",true,false);
						}
						else
						{
							final String bidStr=parseBidString(msg.targetMessage());
							if(bidStr==null)
							{
								CMLib.commands().postSay(this,mob,"I can't seem to do business with you.",true,false);
								return;
							}
							final Object[] bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.currency);
							final String myCurrency=(String)bidAmts[0];
							final double myDenomination=((Double)bidAmts[1]).doubleValue();
							final long myCoins=((Long)bidAmts[2]).longValue();
							final double bid=CMath.mul(myCoins,myDenomination);
							final MOB M=data.highBidderM;
							final double oldBid=data.bid;
							final double oldMaxBid=data.highBid;
							final String[] resp=CMLib.coffeeShops().bid(mob, bid, myCurrency,data, data.auctioningI,new Vector());
							if(resp!=null)
							{
								if(resp[0]!=null)
									mob.tell(resp[0]);
								if((resp[1]!=null)&&(M!=null))
									CMLib.coffeeShops().auctionNotify(M,resp[1],data.auctioningI.name());
							}
							if((oldBid!=data.bid)||(oldMaxBid!=data.highBid))
								CMLib.coffeeShops().saveAuction(data, auctionHouse(),true);
						}
					}
					else
						CMLib.commands().postSay(this,mob,"I can't seem to auction "+msg.tool().name()+".",true,false);
				}
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					CMLib.commands().postSay(this,mob,"That's for the people to decide.  Why don't you use the SELL command and see what you can get?",true,false);
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
					AuctionData data=CMLib.coffeeShops().getEnumeratedAuction(itemName, auctionHouse());
					if(data==null) data=CMLib.coffeeShops().getEnumeratedAuction(msg.tool().name(), auctionHouse());
					if(data==null)
						CMLib.commands().postSay(this,mob,"That's not up for auction.",true,false);
					else
					{
						final String price=CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid);
						final String buyOut=(data.buyOutPrice<=0.0)?null:CMLib.beanCounter().nameCurrencyShort(data.currency,data.buyOutPrice);
						final StringBuffer str=new StringBuffer(CMLib.coffeeShops().getViewDescription(msg.tool())+"\n\r\n\rThe current bid on "+msg.tool().name()+" is "+price+". Use the BID command to place your own bid.  ");
						if(buyOut!=null) str.append("You may also buy this item immediately for "+buyOut+" by using the BUY command.");
						CMLib.commands().postSay(this,mob,str.toString(),true,false);
					}
				}
				break;
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					final String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
					final String s=CMLib.coffeeShops().getAuctionInventory(this,mob,this,forMask);
					if(s.length()>0) mob.tell(s);
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

	@Override public String storeKeeperString(){return CMLib.coffeeShops().storeKeeperString(getShop());}
	@Override public boolean doISellThis(Environmental thisThang){return CMLib.coffeeShops().doISellThis(thisThang,this);}
	protected Area getStartArea()
	{
		Area A=CMLib.map().getStartArea(this);
		if(A==null) CMLib.map().areaLocation(this);
		if(A==null) A=CMLib.map().areas().nextElement();
		return A;
	}

	@Override
	public String finalPrejudiceFactors()
	{
		if(prejudiceFactors().length()>0) return prejudiceFactors();
		return getStartArea().finalPrejudiceFactors();
	}
	@Override public String prejudiceFactors(){return CMStrings.bytesToStr(miscText);}
	@Override public void setPrejudiceFactors(String factors){miscText=factors;}

	@Override
	public String finalIgnoreMask()
	{
		if(ignoreMask().length()>0) return ignoreMask();
		return getStartArea().finalIgnoreMask();
	}
	@Override public String ignoreMask(){return "";}
	@Override public void setIgnoreMask(String factors){}

	@Override
	public String[] finalItemPricingAdjustments()
	{
		if((itemPricingAdjustments()!=null)&&(itemPricingAdjustments().length>0))
			return itemPricingAdjustments();
		return getStartArea().finalItemPricingAdjustments();
	}
	@Override public String[] itemPricingAdjustments(){ return new String[0];}
	@Override public void setItemPricingAdjustments(String[] factors){}

	@Override
	public String finalBudget()
	{
		if(budget().length()>0) return budget();
		return getStartArea().finalBudget();
	}
	@Override public String budget(){return "";}
	@Override public void setBudget(String factors){}

	@Override
	public String finalDevalueRate()
	{
		if(devalueRate().length()>0) return devalueRate();
		return getStartArea().finalDevalueRate();
	}
	@Override public String devalueRate(){return "";}
	@Override public void setDevalueRate(String factors){}

	@Override
	public int finalInvResetRate()
	{
		if(invResetRate()!=0) return invResetRate();
		return getStartArea().finalInvResetRate();
	}
	@Override public int invResetRate(){return 0;}
	@Override public void setInvResetRate(int ticks){}
}
