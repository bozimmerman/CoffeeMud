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
   Copyright 2000-2007 Bo Zimmerman

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
public class StdAuctioneer extends StdShopKeeper implements Auctioneer
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
        whatISell=ShopKeeper.DEAL_AUCTIONEER;
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
    
    public CoffeeShop getShop(){
    	shop=(CoffeeShop)CMClass.getCommon("AuctionCoffeeShop");
    	shop.addStoreInventory(null,this);
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

    public int whatIsSold(){return ShopKeeper.DEAL_AUCTIONEER;}
    public void setWhatIsSold(int newSellCode){ }
    

    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) return true;

        return true;
    }

    public void autoGive(MOB src, MOB tgt, Item I)
    {
        CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
        location().send(this,msg2);
        msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
        location().send(this,msg2);
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
                    if(!(I.amWearingAt(Item.IN_INVENTORY)))
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
                    deposit+=(aRates.timeListPct*new Integer(CMath.mul(days,I.baseGoldValue())).doubleValue());
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
                    lastMsgData.tickDown=System.currentTimeMillis()+(area.getTimeObj().getHoursInDay()*Tickable.TIME_MILIS_PER_MUDHOUR);
                }
                return true;
            case CMMsg.TYP_BID:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                        return false;
                    if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                        return false;
					if(msg.value()<0)
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
                    double bid=CMath.div(msg.value(),1000);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,data.currency)<bid)
					{
						String currencyName=CMLib.beanCounter().getDenominationName(data.currency);
		                CMLib.commands().postSay(this,mob,"You don't have enough "+currencyName+" on hand to cover your bid.",true,false);
		                return false;
					}
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
                return true;
            case CMMsg.TYP_VIEW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                        return false;
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
					if((thisData==null)||(thisData.auctioningM!=msg.source()))
					{
						lastMsgData=null;
	                    CMLib.commands().postSay(this,mob,"I'm confused. Please try to SELL again.",true,false);
					}
					
                }
                return;
            case CMMsg.TYP_BUY:
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
						{
							
						}
					}
					else
	                    CMLib.commands().postSay(this,mob,"I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return;
            case CMMsg.TYP_VALUE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                CMLib.commands().postSay(this,mob,"That's for the people to decide.  Why don't you use the SELL command and see what you can get?",true,false);
	                return;
	            }
                return;
            case CMMsg.TYP_VIEW:
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
						StringBuffer str=new StringBuffer(CMLib.coffeeShops().getViewDescription(msg.tool())+"  The current bid on "+msg.tool().name()+" is "+price+". Use the BID command to place your own bid.  ");
						if(buyOut!=null) str.append("You may also buy this item immediately for "+buyOut+" by using the BUY command.");
		                CMLib.commands().postSay(this,mob,str.toString(),true,false);
					}
				}
                return;
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
                break;
            }
        }
        super.executeMsg(myHost,msg);
    }
}