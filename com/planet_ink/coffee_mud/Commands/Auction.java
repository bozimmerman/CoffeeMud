package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Auction extends Channel implements Tickable
{
	public Auction(){}

	private String[] access={"AUCTION"};
	public String[] getAccessWords(){return access;}
	public String name(){return "Auction";}
	protected final static String MESSAGE_NOAUCTION(){return "There is not currently a live auction.  Use AUCTION UP syntax to add one, or visit an auctioneer for a long auction.";}
	public String liveAuctionStatus()
    { 
        if(liveData.auctioningI!=null)
        {
            String bidWords=CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid);
            if(bidWords.length()==0) bidWords="0";
            return "Up for live auction: "+liveData.auctioningI.name()+".  The current bid is "+bidWords+".";
        }
        return "";	
    }
	protected Auctioneer.AuctionData   liveData=new Auctioneer.AuctionData();
	
	protected static final int STATE_START=0;
	protected static final int STATE_RUNOUT=1;
	protected static final int STATE_ONCE=2;
    protected static final int STATE_TWICE=3;
    protected static final int STATE_THREE=4;
    protected static final int STATE_CLOSED=5;
    
	public long getTickStatus(){ return Tickable.STATUS_NOT;}

	public void setLiveAuctionState(int code)
	{
		liveData.state=code;
		liveData.tickDown=15000/Tickable.TIME_TICK;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_LIVEAUCTION)&&((--liveData.tickDown)<=0))
		{
            MOB auctioneerM=liveData.auctioningM;
            MOB winnerM=liveData.highBidderM;
			if((liveData.state==STATE_START)&&((System.currentTimeMillis()-liveData.start)<(5*15000)))
			{
				if(((System.currentTimeMillis()-liveData.start)>(3*15000))
				&&((winnerM==null)||(winnerM==auctioneerM)))
					setLiveAuctionState(STATE_RUNOUT);
				else
					setLiveAuctionState(STATE_START);
				return true;
			}
			setLiveAuctionState(liveData.state+1);
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("CHANNEL");
			switch(liveData.state)
			{
			case STATE_RUNOUT:
				V.addElement("The live auction for "+liveData.auctioningI.name()+" is almost done. The current bid is "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+".");
				break;
			case STATE_ONCE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+" for "+liveData.auctioningI.name()+" going ONCE!");
				break;
			case STATE_TWICE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+" for "+liveData.auctioningI.name()+" going TWICE!");
				break;
			case STATE_THREE:
				V.addElement(liveData.auctioningI.name()+" going for "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+"! Last chance!");
				break;
			case STATE_CLOSED:
				{
					if((winnerM!=null)&&(winnerM!=liveData.auctioningM))
					{
						V.addElement(liveData.auctioningI.name()+" SOLD to "+winnerM.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+".");
						auctioneerM.doCommand(V,Command.METAFLAG_FORCED);
						if(liveData.auctioningI instanceof Item)
						{
							((Item)liveData.auctioningI).unWear();
                            Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
							winnerM.location().bringItemHere((Item)liveData.auctioningI,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),false);
							double houseCut=Math.floor(liveData.bid*aRates.liveCutPct);
							double finalAmount=liveData.bid-houseCut;
							CMLib.coffeeShops().returnMoney(winnerM,liveData.currency,liveData.highBid-liveData.bid);
							CMLib.coffeeShops().returnMoney(auctioneerM,liveData.currency,finalAmount);
                            auctioneerM.tell(CMLib.beanCounter().nameCurrencyShort(liveData.currency,finalAmount)+" has been transferred to you as payment from "+winnerM.name()+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,houseCut)+".  The goods have also been transferred in exchange.");
							if(CMLib.commands().postGet(winnerM,null,(Item)liveData.auctioningI,false)
	                        ||(winnerM.isMine(liveData.auctioningI)))
	                        {
								winnerM.tell(CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+" has been transferred to "+auctioneerM.displayName(winnerM)+".  You should have received the auctioned goods.  This auction is complete.");
	                            if(liveData.auctioningI instanceof LandTitle)
	                            {
	                                CMMsg msg=CMClass.getMsg(auctioneerM,winnerM,liveData.auctioningI,CMMsg.MASK_ALWAYS|CMMsg.TYP_GIVE,null);
	                                liveData.auctioningI.executeMsg(winnerM,msg);
	                            }
	                        }
	                        else
	                        {
	                            auctioneerM.giveItem((Item)liveData.auctioningI);
                                auctioneerM.tell("Your transaction could not be completed because "+winnerM.name()+" was unable to collect the item.  Please contact "+winnerM.name()+" about receipt of "+liveData.auctioningI.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+".");
	                            winnerM.tell("Your transaction could not be completed because you were unable to collect the item.  Please contact "+auctioneerM.displayName(winnerM)+" about receipt of "+liveData.auctioningI.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid)+".");
	                        }
						}
					}
					else
					if(!auctioneerM.isMine((Item)liveData.auctioningI))
                        auctioneerM.giveItem((Item)liveData.auctioningI);
                    liveData.auctioningM=null;
                    liveData.auctioningI=null;
                    liveData.highBidderM=null;
                    liveData.highBid=0.0;
                    liveData.bid=0.0;
                    liveData.state=0;
                    CMLib.threads().deleteTick(this,Tickable.TICKID_LIVEAUCTION);
				}
				return false;
			}
            auctioneerM.doCommand(V,Command.METAFLAG_FORCED);
		}
		return true;
	}
	
	
	public boolean doLiveAuction(MOB mob, Vector commands, Environmental target)
	{
		Vector V=new Vector();
		V.addElement("AUCTION");
		V.addElement("CHANNEL");
		if(target!=null)
		{
			if(!(target instanceof Item))
				return false;
			liveData.auctioningM=mob;
			liveData.auctioningI=(Item)target;
			String sb=CMParms.combine(commands,0);
			liveData.currency=CMLib.english().numPossibleGoldCurrency(mob,sb);
		    if(liveData.currency.length()==0) liveData.currency=CMLib.beanCounter().getCurrency(mob);
		    double denomination=CMLib.english().numPossibleGoldDenomination(null,liveData.currency,sb);
		    long num=CMLib.english().numPossibleGold(null,sb);
		    liveData.bid=CMath.mul(denomination,num);
		    liveData.highBid=liveData.bid-1;
		    liveData.start=System.currentTimeMillis();
			setLiveAuctionState(STATE_START);
			CMLib.threads().startTickDown(this,Tickable.TICKID_LIVEAUCTION,1);
			String bidWords=CMLib.beanCounter().nameCurrencyShort(liveData.currency,liveData.bid);
			if(target instanceof Item)
				mob.delInventory((Item)target);
			V.addElement("New live auction: "+liveData.auctioningI.name()+".  The opening bid is "+bidWords+".");
			if(liveData.auctioningM!=null) liveData.auctioningM.doCommand(V,Command.METAFLAG_FORCED);
		}
		else
		{
			if(liveData.state>0)	setLiveAuctionState(STATE_RUNOUT);
			String sb="";
			if(commands!=null)
			    sb=CMParms.combine(commands,0);
			MOB M=liveData.highBidderM;
			Object[] bidObjs=CMLib.english().parseMoneyStringSDL(mob,sb,liveData.currency);
			String currency=(String)bidObjs[0];
			double amt=CMath.mul(((Double)bidObjs[1]).doubleValue(),((Long)bidObjs[2]).doubleValue());
			String[] resp=CMLib.coffeeShops().bid(mob,amt,currency,liveData,(Item)liveData.auctioningI,V);
			if(resp!=null)
			{
				if(resp[0]!=null) mob.tell(resp[0]);
				if((resp[1]!=null)&&(M!=null)) M.tell(resp[1]);
			}
			if((V.size()>2)
			&&(liveData.auctioningM!=null))
				liveData.auctioningM.doCommand(V,Command.METAFLAG_FORCED);
		}
		return true;
	}
	
	public void auctionNotify(MOB M, String resp, String regardingItem)
	throws java.io.IOException
	{
    	if(CMLib.flags().isInTheGame(M,true))
    		M.tell(resp);
    	else
    	if(M.playerStats()!=null)
    	{
            CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),
				                            "auction@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
				                            "noreply@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
				                            M.playerStats().getEmail(),
				                            "Auction Update for item: "+regardingItem,
				                            resp);
    	}
	}
	
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
        //mob.tell("Auctions are currently closed for maintenance.  When it re-opens, this command will continue to remain available for live auctions, and new auctioneer mobs will be placed in the major cities for doing multi-day auctions, so keep your eyes open for that coming soon!");
        //if((mob!=null)||(commands!=null)) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		int channelInt=CMLib.channels().getChannelIndex("AUCTION");
		int channelNum=CMLib.channels().getChannelCodeNumber("AUCTION");

		if(CMath.isSet(pstats.getChannelMask(),channelInt))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell("The AUCTION channel has been turned on.  Use `NOAUCTION` to turn it off again.");
		}

		String cmd=null;
        commands.removeElementAt(0);
		if(commands.size()<1)
			cmd="";
		else
	        cmd=((String)commands.elementAt(0)).toUpperCase();
        
        if(cmd.equals("LIST"))
        {
            commands.removeElementAt(0);
            StringBuffer buf=new StringBuffer("");
    		if((liveData.auctioningI!=null)&&(liveData.auctioningM!=null))
            {
            	buf.append("\n\r^HCurrent *live* auction: ^N\n\r");
            	buf.append(liveAuctionStatus()+"\n\r");
            }
            else
                buf.append(MESSAGE_NOAUCTION());
            mob.tell(buf.toString());
            return true;
        }
        else
        if(cmd.equals("UP"))
        {
            commands.removeElementAt(0);
    		if((liveData.auctioningI!=null)&&(liveData.auctioningM!=null))
        	{
        		mob.tell("A live auction is already underway.  Do AUCTION LIST to see it.");
        		return false;
        	}
			Vector V=new Vector();
			if((commands.size()>=2)
			&&((CMLib.english().numPossibleGold(mob,(String)commands.lastElement())>0)||(((String)commands.lastElement()).equals("0"))))
			{
				V.addElement(commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			else
				V.addElement("0");
			
			String s=CMParms.combine(commands,0);
			Environmental E=mob.fetchInventory(null,s);
			if((E==null)||(E instanceof MOB))
			{
				mob.tell(s+" is not an item you can auction.");
				return false;
			}
			if((E instanceof Container)&&(((Container)E).getContents().size()>0))
			{
				mob.tell(E.name()+" will have to be emptied first.");
				return false;
			}
			if(!(((Item)E).amWearingAt(Wearable.IN_INVENTORY)))
			{
				mob.tell(E.name()+" will have to be removed first.");
				return false;
			}
            Auctioneer.AuctionRates aRates=new Auctioneer.AuctionRates();
			double deposit=aRates.liveListPrice;
			String depositAmt=CMLib.beanCounter().nameCurrencyLong(mob, deposit);
			
			if(deposit>0.0)
			{
    			if((mob.isMonster())
                ||(!mob.session().confirm("Auctioning "+E.name()+" will cost a listing fee of "+depositAmt+", proceed (Y/n)?","Y")))
    				return false;
			}
			else
			if((mob.isMonster())
            ||(!mob.session().confirm("Auction "+E.name()+" live, with a starting bid of "+((String)V.firstElement())+" (Y/n)?","Y")))
				return false;
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(mob))<deposit)
			{
				mob.tell("You don't have enough "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(mob))+" to cover the listing fee!");
				return false;
			}
			CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getCurrency(mob), deposit);
			doLiveAuction(mob,V,E);
			if(liveData.auctioningI!=null) liveData.auctioningM=mob;
            return true;
        }
        else
        if(cmd.equals("BID"))
        {
            commands.removeElementAt(0);
            if((liveData.auctioningI==null)||(liveData.auctioningM==null))
            {
                mob.tell(MESSAGE_NOAUCTION());
                return false;
            }
        	if(commands.size()<1)
        	{
        		mob.tell("Bid how much?");
        		return false;
        	}
        	String amount=CMParms.combine(commands,0);
    		doLiveAuction(mob,CMParms.makeVector(amount),null);
			return true;
        }
        else
		if(cmd.equals("CLOSE"))
		{
            commands.removeElementAt(0);
            if((liveData.auctioningI==null)||(liveData.auctioningM==null))
            {
                mob.tell(MESSAGE_NOAUCTION());
                return false;
            }
            if((liveData.auctioningI==null)||(liveData.auctioningM!=mob))
            {
                mob.tell("You are not currently running a live auction.");
                return false;
            }
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("The auction has been closed.");
			CMLib.threads().deleteTick(this,Tickable.TICKID_LIVEAUCTION);
			if(liveData.auctioningI instanceof Item)
    			liveData.auctioningM.giveItem((Item)liveData.auctioningI);
            if(liveData.highBid>0.0)
            	CMLib.coffeeShops().returnMoney(liveData.highBidderM,liveData.currency,liveData.highBid);
			liveData.auctioningM=null;
			liveData.auctioningI=null;
			super.execute(mob,V,metaFlags);
			return true;
		}
        else
		if(cmd.equals("INFO"))
		{
            commands.removeElementAt(0);
            if((liveData.auctioningI==null)||(liveData.auctioningM==null))
            {
                mob.tell(MESSAGE_NOAUCTION());
                return false;
            }
        	Environmental E=null;
    		E=liveData.auctioningI;
        	mob.tell("Item: "+E.name());
        	CMLib.commands().handleBeingLookedAt(CMClass.getMsg(mob,CMMsg.MASK_ALWAYS|CMMsg.MSG_EXAMINE,null));
            mob.tell(CMLib.coffeeShops().getViewDescription(E));
        	return true;
		}
		else
		if(cmd.equals("CHANNEL"))
		{
            commands.removeElementAt(0);
        	if(commands.size()==0)
        	{
        		mob.tell("Channel what?");
        		return false;
        	}
    		if((liveData.auctioningI==null)||(liveData.auctioningM==null))
    		{
    			mob.tell("Channeling is only allowed during live auctions.");
    			return false;
    		}
    		commands.insertElementAt("AUCTION",0);
			super.execute(mob,commands,metaFlags);
			return true;
        }
        commands.insertElementAt("AUCTION",0);
        super.execute(mob,commands,metaFlags);
		return false;
	}

	
	public boolean canBeOrdered(){return true;}

	
}
