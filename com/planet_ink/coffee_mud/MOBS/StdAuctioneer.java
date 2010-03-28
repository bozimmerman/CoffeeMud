package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2010 Bo Zimmerman

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
public class StdAuctioneer extends StdMOB implements Auctioneer
{
    public String ID(){return "StdAuctioneer";}

    public StdAuctioneer()
    {
        super();
        Username="an auctioneer";
        setDescription("He talks faster than you!");
        setDisplayText("The local auctioneer is here calling prices.");
        CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);
        setMoney(0);
        baseEnvStats.setWeight(150);
        setWimpHitPoint(0);

        baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
        baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

        baseEnvStats().setArmor(0);

        baseState.setHitPoints(1000);

        recoverMaxState();
        resetToMaxState();
        recoverEnvStats();
        recoverCharStats();
    }

    public AuctionData lastMsgData=null;
    protected static final Hashtable lastCheckTimes=new Hashtable();

    public CoffeeShop getShop(){
    	CoffeeShop shop=((CoffeeShop)CMClass.getCommon("AuctionCoffeeShop")).build(this);
    	shop.addStoreInventory(null);
    	return shop;
    }

    public String auctionHouse(){return text();}
    public void setAuctionHouse(String name){setMiscText(name);}

    protected double timedListingPrice=-1.0;
    public double timedListingPrice(){return timedListingPrice;}
    public void setTimedListingPrice(double d){timedListingPrice=d;}

    protected double timedListingPct=-1.0;
    public double timedListingPct(){return timedListingPct;}
    public void setTimedListingPct(double d){timedListingPct=d;}

    protected double timedFinalCutPct=-1.0;
    public double timedFinalCutPct(){return timedFinalCutPct;}
    public void setTimedFinalCutPct(double d){timedFinalCutPct=d;}

    protected int maxTimedAuctionDays=-1;
    public int maxTimedAuctionDays(){return maxTimedAuctionDays;}
    public void setMaxTimedAuctionDays(int d){maxTimedAuctionDays=d;}

    protected int minTimedAuctionDays=-1;
    public int minTimedAuctionDays(){return minTimedAuctionDays;}
    public void setMinTimedAuctionDays(int d){minTimedAuctionDays=d;}

    public void destroy()
    {
        super.destroy();
        CMLib.map().delAuctionHouse(this);
    }
    public void bringToLife(Room newLocation, boolean resetStats)
    {
        super.bringToLife(newLocation,resetStats);
        CMLib.map().addAuctionHouse(this);
    }

    public long getWhatIsSoldMask(){ return DEAL_AUCTIONEER;}
    public boolean isSold(int mask){return mask==ShopKeeper.DEAL_AUCTIONEER;}
    public void setWhatIsSoldMask(long newSellCode){ }
    public void addSoldType(int mask){}


    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) return true;
		if(CMLib.flags().isInTheGame(this,true))
		synchronized(("AUCTION_HOUSE_"+auctionHouse().toUpperCase().trim()).intern())
		{
			Long lastTime=(Long)StdAuctioneer.lastCheckTimes.get(auctionHouse().toUpperCase().trim());
			if((lastTime==null)||(System.currentTimeMillis()-lastTime.longValue())>(Tickable.TIME_MILIS_PER_MUDHOUR-5))
			{
				StdAuctioneer.lastCheckTimes.remove(auctionHouse().toUpperCase().trim());
				long thisTime=System.currentTimeMillis();
				StdAuctioneer.lastCheckTimes.put(auctionHouse().toUpperCase().trim(),Long.valueOf(thisTime));
				Vector auctions=CMLib.coffeeShops().getAuctions(null, auctionHouse());
				for(int a=0;a<auctions.size();a++)
				{
					Auctioneer.AuctionData data=(Auctioneer.AuctionData)auctions.elementAt(a);
					if(thisTime>=data.tickDown)
					{
						if((lastTime==null)||(data.tickDown>lastTime.longValue()))
						{
							if(data.highBidderM!=null)
							{
	                            //CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
	                            CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" sold to "+data.highBidderM.Name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)+".  When the high bidder comes to claim "+data.highBidderM.charStats().hisher()+" property, you will automatically receive your payment along with another notice.",data.auctioningI.Name());
	                            CMLib.coffeeShops().auctionNotify(data.highBidderM,"You won the auction for "+data.auctioningI.name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)+".  Your winnings, along with the difference from your high bid ("+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)+") will be given to you as soon as you claim your property.  To claim your winnings, come to "+name()+" at "+location().displayText()+" and enter the BUY command for the item again (you will not be charged).",data.auctioningI.Name());
							}
							else
							{
	                            CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" went unsold.  '"+data.auctioningI.name()+"' has been automatically returned to your inventory.",data.auctioningI.Name());
	                            data.auctioningM.giveItem(data.auctioningI);
                                if(!CMLib.flags().isInTheGame(data.auctioningM,true))
                                    CMLib.database().DBUpdatePlayerItems(data.auctioningM);
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
        CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
        location().send(this,msg2);
        msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
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
	        	int y=targetMessage.indexOf('\'',x+1);
	        	if(y>x)
	        		return targetMessage.substring(x+1,y);
	        }
        }
        return null;
    }


    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
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
                    Item I=(Item)msg.tool();
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
                    AuctionRates aRates=new AuctionRates(this);
                    CMLib.commands().postSay(this,mob,"Ok, so how many local days will your auction run for ("+aRates.minDays+"-"+aRates.maxDays+")?",true,false);
                    int days=0;
                    try{days=CMath.s_int(mob.session().prompt(":","",10000));}catch(Exception e){return false;}
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
                    deposit+=(aRates.timeListPct*((double)CMath.mul(days,I.baseGoldValue())));
                    String depositAmt=CMLib.beanCounter().nameCurrencyLong(mob, deposit);
                    if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(mob))<deposit)
                    {
                        CMLib.commands().postSay(this,mob,"You don't have enough to cover the listing fee of "+depositAmt+".  Sell a cheaper item, use fewer days, or come back later.",true,false);
                        return false;
                    }
                    CMLib.commands().postSay(this,mob,"Auctioning "+I.name()+" will cost a listing fee of "+depositAmt+", proceed?",true,false);
                    try{if(!mob.session().confirm("(Y/N):","Y",10000)) return false;}catch(Exception e){return false;}
                    lastMsgData=new AuctionData();
                    lastMsgData.auctioningI=(Item)msg.tool();
                    lastMsgData.auctioningM=msg.source();
                    lastMsgData.currency=CMLib.beanCounter().getCurrency(msg.source());
                    Area area=CMLib.map().getStartArea(this);
                    if(area==null) area=CMLib.map().getStartArea(msg.source());
                    lastMsgData.tickDown=System.currentTimeMillis()+(days*area.getTimeObj().getHoursInDay()*Tickable.TIME_MILIS_PER_MUDHOUR)+60000;
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
                    String bidStr=parseBidString(msg.targetMessage());
					if(bidStr==null)
					{
	                    CMLib.commands().postSay(this,mob,"I can't seem to do business with you.",true,false);
	                    return false;
					}
                    if(msg.tool().envStats().level()>msg.source().envStats().level())
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
						Auctioneer.AuctionRates rates=new Auctioneer.AuctionRates(this);
						if((rates.minDays>0)&&(rates.minDays>=data.daysEllapsed(mob,this)))
						{
			                CMLib.commands().postSay(this,mob,"You may not close this auction until it has been active for "+rates.minDays+" days.",true,false);
			                return false;
						}
						if(msg.source().session()!=null)
						{
							try{
							if(!msg.source().session().confirm("This will cancel your auction on "+data.auctioningI.name()+", are you sure (y/N)?","N",10000))
								return false;
							}catch(Exception e){return false;}
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
						Object[] bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.currency);
						String myCurrency=(String)bidAmts[0];
						double myDenomination=((Double)bidAmts[1]).doubleValue();
						long myCoins=((Long)bidAmts[2]).longValue();
						double bid=CMath.mul(myCoins,myDenomination);
						if(!myCurrency.equals(data.currency))
						{
							String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
			                CMLib.commands().postSay(this,mob,"This auction is being handled in "+currencyName+".",true,false);
			                return false;
						}
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.currency)<bid)
						{
							String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
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
                    if(msg.tool().envStats().level()>msg.source().envStats().level())
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
						Auctioneer.AuctionRates rates=new Auctioneer.AuctionRates(this);
						if((rates.minDays>0)&&(rates.minDays>=data.daysEllapsed(mob,this)))
						{
			                CMLib.commands().postSay(this,mob,"You may not close this auction until it has been active for "+rates.minDays+" days.",true,false);
			                return false;
						}
						if(msg.source().session()!=null)
						{
							try{
							if(!msg.source().session().confirm("This will cancel your auction on "+data.auctioningI.name()+", are you sure (y/N)?","N",10000))
								return false;
							}catch(Exception e){return false;}
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
							String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
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

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_SELL:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
					AuctionData thisData=lastMsgData;
					if((thisData==null)||(thisData.auctioningM!=msg.source())||(msg.source().isMonster()))
					{
						lastMsgData=null;
	                    CMLib.commands().postSay(this,mob,"I'm confused. Please try to SELL again.",true,false);
					}
					else
					try{
						double lowestDenom=CMLib.beanCounter().getLowestDenomination(thisData.currency);
	                    CMLib.commands().postSay(this,mob,"What would you like your opening price to be (in "+CMLib.beanCounter().getDenominationName(thisData.currency, lowestDenom)+"?",true,false);
						String openPrice=mob.session().prompt(": ",30000);
	                    CMLib.commands().postSay(this,mob,"What would you like your buy-now price to be (in "+CMLib.beanCounter().getDenominationName(thisData.currency, lowestDenom)+"?",true,false);
						String buyPrice=mob.session().prompt(": ",30000);
						thisData.bid=CMath.s_double(openPrice)*lowestDenom;
						if(thisData.bid<0.0) thisData.bid=0.0;
						thisData.buyOutPrice=CMath.s_double(buyPrice)*lowestDenom;
						if(thisData.buyOutPrice<=0.0) thisData.buyOutPrice=-1.0;
						thisData.start=System.currentTimeMillis();
						CMLib.coffeeShops().saveAuction(thisData, auctionHouse(),false);
						CMLib.commands().postChannel(this,"AUCTION","New "+thisData.daysRemaining(thisData.auctioningM,msg.source())+" day auction: "+thisData.auctioningI.name(),true);
	                    AuctionRates aRates=new AuctionRates(this);
	                    double deposit=aRates.timeListPrice;
	                    deposit+=(aRates.timeListPct*((double)CMath.mul(thisData.daysRemaining(mob,this),thisData.auctioningI.baseGoldValue())));
	                    CMLib.beanCounter().subtractMoney(mob,deposit);
						thisData.auctioningI.destroy();
						CMLib.commands().postSay(this,mob,"Your auction for "+thisData.auctioningI.name()+" is now open.  When it is done, you will receive either your winnings automatically, or the returned item automatically.",true,false);
					}catch(Exception e){}
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
                            Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
                            double houseCut=Math.floor(data.bid*aRates.timeCutPct);
                            double finalAmount=data.bid-houseCut;
                            CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
                            CMLib.coffeeShops().auctionNotify(data.auctioningM,data.highBidderM.Name()+", who won your auction for "+data.auctioningI.name()+" has claimed "+data.highBidderM.charStats().hisher()+" property.  You have been credited with "+CMLib.beanCounter().nameCurrencyShort(data.currency,finalAmount)+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(data.currency,houseCut)+".",data.auctioningI.Name());
                            //CMLib.coffeeShops().auctionNotify(data.highBidderM,"You won the auction for "+data.auctioningI.name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid)+".  The difference from your high bid ("+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)+") has been returned to you along with the winning item.",data.auctioningI.Name());
							if((data.highBid-data.bid)>0.0)
				                CMLib.commands().postSay(this,mob,"Congratulations, and here is your "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.highBid-data.bid)+" in change as well.",true,false);
							else
				                CMLib.commands().postSay(this,mob,"Congratulations!",true,false);
                            CMLib.coffeeShops().returnMoney(mob,data.currency,data.highBid-data.bid);
                            CMLib.coffeeShops().purchaseItems(data.auctioningI,CMParms.makeVector(data.auctioningI),this,mob);
    						if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
    							mob.location().recoverRoomStats();
					    	CMLib.database().DBDeleteJournal(auctionHouse(),data.auctionDBKey);
						}
						else
						if(System.currentTimeMillis()<data.tickDown)
						{
                            Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
                            double houseCut=Math.floor(data.buyOutPrice*aRates.timeCutPct);
                            double finalAmount=data.buyOutPrice-houseCut;
                            CMLib.coffeeShops().returnMoney(data.auctioningM,data.currency,finalAmount);
                            CMLib.coffeeShops().auctionNotify(data.auctioningM,"Your auction for "+data.auctioningI.name()+" sold to "+mob.Name()+" for "+CMLib.beanCounter().nameCurrencyShort(data.currency,data.buyOutPrice)+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(data.currency,houseCut)+".",data.auctioningI.Name());
				            CMLib.beanCounter().subtractMoney(mob,data.currency,data.buyOutPrice);
                            CMLib.coffeeShops().purchaseItems(data.auctioningI,CMParms.makeVector(data.auctioningI),this,mob);
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
		                    String bidStr=parseBidString(msg.targetMessage());
							if(bidStr==null)
							{
			                    CMLib.commands().postSay(this,mob,"I can't seem to do business with you.",true,false);
			                    return;
							}
							Object[] bidAmts=CMLib.english().parseMoneyStringSDL(mob,bidStr,data.currency);
							String myCurrency=(String)bidAmts[0];
							double myDenomination=((Double)bidAmts[1]).doubleValue();
							long myCoins=((Long)bidAmts[2]).longValue();
							double bid=CMath.mul(myCoins,myDenomination);
							MOB M=data.highBidderM;
							double oldBid=data.bid;
							double oldMaxBid=data.highBid;
							String[] resp=CMLib.coffeeShops().bid(mob, bid, myCurrency,data, data.auctioningI,new Vector());
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
						String price=CMLib.beanCounter().nameCurrencyShort(data.currency,data.bid);
						String buyOut=(data.buyOutPrice<=0.0)?null:CMLib.beanCounter().nameCurrencyShort(data.currency,data.buyOutPrice);
						StringBuffer str=new StringBuffer(CMLib.coffeeShops().getViewDescription(msg.tool())+"\n\r\n\rThe current bid on "+msg.tool().name()+" is "+price+". Use the BID command to place your own bid.  ");
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
					String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
                    String s=CMLib.coffeeShops().getAuctionInventory(this,mob,this,forMask);
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

    public String storeKeeperString(){return CMLib.coffeeShops().storeKeeperString(getShop());}
	public boolean doISellThis(Environmental thisThang){return CMLib.coffeeShops().doISellThis(thisThang,this);}
    protected Area getStartArea(){
        Area A=CMLib.map().getStartArea(this);
        if(A==null) CMLib.map().areaLocation(this);
        if(A==null) A=(Area)CMLib.map().areas().nextElement();
        return A;
    }

    public String finalPrejudiceFactors(){
        if(prejudiceFactors().length()>0) return prejudiceFactors();
        return getStartArea().finalPrejudiceFactors();
    }
	public String prejudiceFactors(){return CMLib.encoder().decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=CMLib.encoder().compressString(factors);}

    public String finalIgnoreMask(){
        if(ignoreMask().length()>0) return ignoreMask();
        return getStartArea().finalIgnoreMask();
    }
    public String ignoreMask(){return "";}
    public void setIgnoreMask(String factors){}

    public String[] finalItemPricingAdjustments(){
        if((itemPricingAdjustments()!=null)&&(itemPricingAdjustments().length>0))
            return itemPricingAdjustments();
        return getStartArea().finalItemPricingAdjustments();
    }
    public String[] itemPricingAdjustments(){ return new String[0];}
    public void setItemPricingAdjustments(String[] factors){}

    public String finalBudget(){
        if(budget().length()>0) return budget();
        return getStartArea().finalBudget();
    }
	public String budget(){return "";}
	public void setBudget(String factors){}

    public String finalDevalueRate(){
        if(devalueRate().length()>0) return devalueRate();
        return getStartArea().finalDevalueRate();
    }
	public String devalueRate(){return "";}
	public void setDevalueRate(String factors){}

    public int finalInvResetRate(){
        if(invResetRate()!=0) return invResetRate();
        return getStartArea().finalInvResetRate();
    }
	public int invResetRate(){return 0;}
	public void setInvResetRate(int ticks){}
}
