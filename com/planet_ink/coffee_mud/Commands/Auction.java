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
public class Auction extends Channel implements Tickable
{
	public Auction(){}

	private String[] access={"AUCTION"};
	public String[] getAccessWords(){return access;}
	public String name(){return "Auction";}
	
	public String liveAuctionStatus()
    { 
        if(liveData!=null)
        {
            String bidWords=CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid);
            if(bidWords.length()==0) bidWords="0";
            return "Up for live auction: "+liveAuctioningItem.name()+".  The current bid is "+bidWords+".";
        }
        return "";	
    }
	protected Environmental liveAuctioningItem=null;
	protected MOB           liveAuctionInvoker=null;
	protected class AuctionData
	{
		protected MOB           liveAuctionHighBidder=null;
		protected String        liveAuctionCurrency="";
		protected double        liveAuctionHighBid=Double.MIN_VALUE;
		protected double        liveAuctionBid=Double.MIN_VALUE;
		protected int           liveAuctionState=-1;
		protected long          liveAuctionTickDown=0;
		protected long          liveAuctionStart=0;
	}
	protected AuctionData   liveData=new AuctionData();
	
	protected static final int STATE_START=0;
	protected static final int STATE_RUNOUT=1;
	protected static final int STATE_ONCE=2;
    protected static final int STATE_TWICE=3;
    protected static final int STATE_THREE=4;
    protected static final int STATE_CLOSED=5;
    
    protected static final int RATE_LIVELIST=0;
    protected static final int RATE_TIMELIST=1;
    protected static final int RATE_TIMEPCTD=2;
    protected static final int RATE_LIVECUT=3;
    protected static final int RATE_TIMECUT=4;
    protected static final int RATE_MAXDAYS=5;
    protected static final int RATE_NUM=6;
    
	public long getTickStatus(){ return Tickable.STATUS_NOT;}

	public void setLiveAuctionState(int code)
	{
		liveData.liveAuctionState=code;
		liveData.liveAuctionTickDown=15000/Tickable.TIME_TICK;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((--liveData.liveAuctionTickDown)<=0)
		{
			if((liveData.liveAuctionState==STATE_START)&&((System.currentTimeMillis()-liveData.liveAuctionStart)<(5*15000)))
			{
				if(((System.currentTimeMillis()-liveData.liveAuctionStart)>(3*15000))
				&&((liveData.liveAuctionHighBidder==null)||(liveData.liveAuctionHighBidder==liveAuctionInvoker)))
					setLiveAuctionState(STATE_RUNOUT);
				else
					setLiveAuctionState(STATE_START);
				return true;
			}
			setLiveAuctionState(liveData.liveAuctionState+1);
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("CHANNEL");
			MOB M=liveAuctionInvoker;
			switch(liveData.liveAuctionState)
			{
			case STATE_RUNOUT:
				V.addElement("The live auction for "+liveAuctioningItem.name()+" is almost done. The current bid is "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+".");
				break;
			case STATE_ONCE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+" for "+liveAuctioningItem.name()+" going ONCE!");
				break;
			case STATE_TWICE:
				V.addElement(CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+" for "+liveAuctioningItem.name()+" going TWICE!");
				break;
			case STATE_THREE:
				V.addElement(liveAuctioningItem.name()+" going for "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+"! Last chance!");
				break;
			case STATE_CLOSED:
				{
					if((liveData.liveAuctionHighBidder!=null)&&(liveData.liveAuctionHighBidder!=liveAuctionInvoker))
					{
						V.addElement(liveAuctioningItem.name()+" SOLD to "+liveData.liveAuctionHighBidder.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+".");
						M.doCommand(V);
						if(liveAuctioningItem instanceof Item)
						{
							((Item)liveAuctioningItem).unWear();
							liveData.liveAuctionHighBidder.location().bringItemHere((Item)liveAuctioningItem,Item.REFUSE_PLAYER_DROP,false);
							Vector ratesV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_AUCTIONRATES),true);
							while(ratesV.size()<RATE_NUM) ratesV.addElement("0");
							double houseCut=Math.floor(liveData.liveAuctionBid*CMath.s_double((String)ratesV.elementAt(RATE_LIVECUT)));
							double finalAmount=liveData.liveAuctionBid-houseCut;
	                        returnMoney(liveData.liveAuctionHighBidder,liveData.liveAuctionCurrency,liveData.liveAuctionHighBid-liveData.liveAuctionBid);
	                        returnMoney(M,liveData.liveAuctionCurrency,finalAmount);
							M.tell(CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,finalAmount)+" has been transferred to you as payment from "+liveData.liveAuctionHighBidder.name()+", after the house took a cut of "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,houseCut)+".  The goods have also been transferred in exchange.");
							if(CMLib.commands().postGet(liveData.liveAuctionHighBidder,null,(Item)liveAuctioningItem,false)
	                        ||(liveData.liveAuctionHighBidder.isMine(liveAuctioningItem)))
	                        {
								liveData.liveAuctionHighBidder.tell(CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+" has been transferred to "+M.displayName(liveData.liveAuctionHighBidder)+".  You should have received the auctioned goods.  This auction is complete.");
	                            if(liveAuctioningItem instanceof LandTitle)
	                            {
	                                CMMsg msg=CMClass.getMsg(M,liveData.liveAuctionHighBidder,liveAuctioningItem,CMMsg.MASK_ALWAYS|CMMsg.TYP_GIVE,null);
	                                liveAuctioningItem.executeMsg(liveData.liveAuctionHighBidder,msg);
	                            }
	                        }
	                        else
	                        {
	                            M.giveItem((Item)liveAuctioningItem);
	                            M.tell("Your transaction could not be completed because "+liveData.liveAuctionHighBidder.name()+" was unable to collect the item.  Please contact "+liveData.liveAuctionHighBidder.name()+" about receipt of "+liveAuctioningItem.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+".");
	                            liveData.liveAuctionHighBidder.tell("Your transaction could not be completed because you were unable to collect the item.  Please contact "+M.displayName(liveData.liveAuctionHighBidder)+" about receipt of "+liveAuctioningItem.name()+" for "+CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid)+".");
	                        }
	                        liveData.liveAuctionHighBidder=null;
	                        liveData.liveAuctionHighBid=0.0;
	                        liveData.liveAuctionBid=0.0;
						}
					}
					if(M!=null)
						M.doCommand(CMParms.parse("AUCTION CLOSE LIVE"));
					liveAuctionInvoker=null;
					liveAuctioningItem=null;
				}
				return false;
			}
			M.doCommand(V);
		}
		return true;
	}
	
	
	public String[] bid(MOB mob, String newBid, AuctionData auctionData, Item I, Vector auctionAnnounces)
	{
		double b=0;
		String bwords="0";
		String myCurrency=CMLib.beanCounter().getCurrency(mob);
	    if(newBid.length()>0)
	    {
		    myCurrency=CMLib.english().numPossibleGoldCurrency(mob,newBid);
		    if(myCurrency!=null)
		    {
			    double denomination=CMLib.english().numPossibleGoldDenomination(null,auctionData.liveAuctionCurrency,newBid);
			    long num=CMLib.english().numPossibleGold(null,newBid);
			    b=CMath.mul(denomination,num);
			    bwords=CMLib.beanCounter().getDenominationName(myCurrency,denomination,num);
		    }
		    else
		        myCurrency=CMLib.beanCounter().getCurrency(mob);
	    }
		String bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.liveAuctionCurrency,auctionData.liveAuctionBid);
		if(bidWords.length()==0) bidWords="0";
		String currencyName=CMLib.beanCounter().getDenominationName(auctionData.liveAuctionCurrency);
		if(newBid.length()==0)
			return new String[]{"Up for live auction: "+I.name()+".  The current bid is "+bidWords+".",null};

		if(!myCurrency.equals(auctionData.liveAuctionCurrency))
		    return new String[]{"This live auction is being bid in "+currencyName+" only.",null};
		
		if(b>CMLib.beanCounter().getTotalAbsoluteValue(mob,auctionData.liveAuctionCurrency))
			return new String[]{"You don't have enough "+currencyName+" on hand to cover that bid.",null};

		if(b>auctionData.liveAuctionHighBid)
		{
            returnMoney(liveData.liveAuctionHighBidder,liveData.liveAuctionCurrency,liveData.liveAuctionHighBid);
			auctionData.liveAuctionHighBidder=mob;
			if(auctionData.liveAuctionHighBid<0.0) auctionData.liveAuctionHighBid=0.0;
			auctionData.liveAuctionBid=auctionData.liveAuctionHighBid+1.0;
			auctionData.liveAuctionHighBid=b;
            returnMoney(liveData.liveAuctionHighBidder,liveData.liveAuctionCurrency,-b);
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.liveAuctionCurrency,auctionData.liveAuctionBid);
			auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
			if((auctionData.liveAuctionHighBidder!=null)&&(auctionData.liveAuctionHighBidder==mob))
				return new String[]{"You have submitted a new bid for "+I.name()+".",null};
			else
			if((auctionData.liveAuctionHighBidder!=null)&&(auctionData.liveAuctionHighBidder!=mob))
				return new String[]{"You have the high bid for "+I.name()+".","You have been outbid for "+I.name()+"."};
			else
				return new String[]{"You have submitted a bid for "+I.name()+".",null};
		}
		else
		if((b<auctionData.liveAuctionBid)||(b==0))
		{
			return new String[]{"Your bid of "+bwords+" is insufficient."+((auctionData.liveAuctionBid>0)?" The current high bid is "+bidWords+".":""),null};
		}
		else
		if((b==auctionData.liveAuctionBid)&&(auctionData.liveAuctionHighBidder!=null))
		{
			return new String[]{"You must bid higher than "+bidWords+" to have your bid accepted.",null};
		}
		else
		if((b==auctionData.liveAuctionHighBid)&&(auctionData.liveAuctionHighBidder!=null))
		{
			if((auctionData.liveAuctionHighBidder!=null)&&(auctionData.liveAuctionHighBidder!=mob))
			{
				auctionData.liveAuctionBid=b;
				bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.liveAuctionCurrency,auctionData.liveAuctionBid);
				auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
				return new String[]{"You have been outbid by proxy for "+I.name()+".","Your high bid for "+I.name()+" has been reached."};
			}
		}
		else
		{
			auctionData.liveAuctionBid=b;
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.liveAuctionCurrency,auctionData.liveAuctionBid);
			auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
			return new String[]{"You have been outbid by proxy for "+I.name()+".",null};
		}
		return null;
	}

	public boolean doLiveAuction(MOB mob, Vector commands, Environmental target)
	throws java.io.IOException
	{
		Vector V=new Vector();
		V.addElement("AUCTION");
		V.addElement("CHANNEL");
		if(target!=null)
		{
			liveAuctionInvoker=mob;
			liveAuctioningItem=target;
			String sb=CMParms.combine(commands,0);
			liveData.liveAuctionCurrency=CMLib.english().numPossibleGoldCurrency(mob,sb);
		    if(liveData.liveAuctionCurrency.length()==0) liveData.liveAuctionCurrency=CMLib.beanCounter().getCurrency(mob);
		    double denomination=CMLib.english().numPossibleGoldDenomination(null,liveData.liveAuctionCurrency,sb);
		    long num=CMLib.english().numPossibleGold(null,sb);
		    liveData.liveAuctionBid=CMath.mul(denomination,num);
		    liveData.liveAuctionHighBid=liveData.liveAuctionBid-1;
		    liveData.liveAuctionStart=System.currentTimeMillis();
			setLiveAuctionState(STATE_START);
			CMLib.threads().startTickDown(this,Tickable.TICKID_QUEST,1);
			String bidWords=CMLib.beanCounter().nameCurrencyShort(liveData.liveAuctionCurrency,liveData.liveAuctionBid);
			if(target instanceof Item)
				mob.delInventory((Item)target);
			V.addElement("New live auction: "+liveAuctioningItem.name()+".  The opening bid is "+bidWords+".");
			if(liveAuctionInvoker!=null) liveAuctionInvoker.doCommand(V);
		}
		else
		{
			if(liveData.liveAuctionState>0)	setLiveAuctionState(STATE_RUNOUT);
			String sb="";
			if(commands!=null)
			    sb=CMParms.combine(commands,0);
			MOB M=liveData.liveAuctionHighBidder;
			String[] resp=this.bid(mob,sb,liveData,(Item)liveAuctioningItem,V);
			if(resp!=null)
			{
				if(resp[0]!=null) mob.tell(resp[0]);
				if((resp[1]!=null)&&(M!=null)) M.tell(resp[1]);
			}
			if((V.size()>2)
			&&(liveAuctionInvoker!=null))
				liveAuctionInvoker.doCommand(V);
		}
		return true;
	}
	
	public DVector getTimedAuctionNames(String by)
	{
        DVector keyAuctions=new DVector(2);
		if((liveAuctioningItem!=null)&&(liveAuctionInvoker!=null))
		{
			if((by==null)||(by.equals(liveAuctionInvoker.Name())))
				keyAuctions.addElement("LIVE",liveAuctioningItem);
		}
        Vector otherAuctions=CMLib.database().DBReadJournal("SYSTEM_AUCTIONS");
        for(int o=0;o<otherAuctions.size();o++)
        {
            Vector V=(Vector)otherAuctions.elementAt(o);
            String from=(String)V.elementAt(DatabaseEngine.JOURNAL_FROM);
            String xml=(String)V.elementAt(DatabaseEngine.JOURNAL_MSG);
            if((by==null)||(by.equalsIgnoreCase(from)))
            {
                String key=(String)V.elementAt(DatabaseEngine.JOURNAL_KEY);
                Item I=null;
                Vector xmlV=CMLib.xml().parseAllXML(xml);
                XMLLibrary.XMLpiece piece=null;
                for(int v=0;v<xmlV.size();v++)
                {
                	XMLLibrary.XMLpiece X=(XMLLibrary.XMLpiece)xmlV.elementAt(v);
                	if(X.tag.equalsIgnoreCase("AUCTIONITEM"))
                	{
                		I=CMLib.coffeeMaker().getItemFromXML(X.value);
                		break;
                	}
                }
            	keyAuctions.addElement(key,I);
            }
        }
        return keyAuctions;
	}
	
	public void returnMoney(MOB to, String currency, double amt)
	{
		if(amt>0)
			CMLib.beanCounter().giveSomeoneMoney(to, currency, amt);
		else
			CMLib.beanCounter().subtractMoney(to, currency,-amt);
		if(amt!=0)
			if(!CMLib.flags().isInTheGame(to,true))
				CMLib.database().DBUpdatePlayerItems(to);
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
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
		if(commands.size()<2)
		{
			cmd="";
		}
		else
		{
	        commands.removeElementAt(0);
	        cmd=((String)commands.elementAt(0)).toUpperCase();
	        commands.removeElementAt(0);
		}
        
        if(cmd.equals("LIST"))
        {
            commands.removeElementAt(1);
            String rest=CMParms.combine(commands,1).toUpperCase().trim();
            StringBuffer buf=new StringBuffer("^xUp for auction:^.^N\n\r");
    		if((liveAuctioningItem!=null)&&(liveAuctionInvoker!=null))
            {
            	buf.append("\n\r^HCurrent *live* auction: ^N\n\r");
            	buf.append(liveAuctionStatus()+"\n\r");
            }
            mob.tell(buf.toString());
            buf.setLength(-1);
            Vector otherAuctions=CMLib.database().DBReadJournal("SYSTEM_AUCTIONS");
            Vector auctionsToShow=new Vector();
            for(int o=0;o<otherAuctions.size();o++)
            {
                Vector V=(Vector)otherAuctions.elementAt(o);
                String subj=(String)V.elementAt(DatabaseEngine.JOURNAL_SUBJ); // type/level range/name
                if((rest.length()==0)||(subj.toUpperCase().indexOf(rest)>=0))
                	auctionsToShow.addElement(V);
            }
            TimeClock T=mob.location().getArea().getTimeObj();
            buf.append(CMStrings.padRight(CMStrings.padRight("Type",10)+" "+CMStrings.padRight("Lvl",3)+" "+CMStrings.padRight("Bid",10)+" Item",55));
            for(int a=0;a<auctionsToShow.size();a++)
            {
                Vector V=(Vector)auctionsToShow.elementAt(a);
                String from=(String)V.elementAt(DatabaseEngine.JOURNAL_FROM); // who is auctioning
                String to=(String)V.elementAt(DatabaseEngine.JOURNAL_TO); // end date/time of the auction
                String subj=(String)V.elementAt(DatabaseEngine.JOURNAL_SUBJ); // Type Level Bid name in 55 chars!
                T=(TimeClock)T.deriveClock(CMath.s_long(to)).copyOf();
                buf.append(CMStrings.padRight(from,10)+" "+CMStrings.padRight(subj,55)+" "+T.getShortestTimeDescription()+"\n\r");
            }
            return true;
        }
        else
        if(cmd.equals("LIVE"))
        {
    		if((liveAuctioningItem!=null)&&(liveAuctionInvoker!=null))
        	{
        		mob.tell("A live auction is already underway.  Do AUCTION LIST to see it.");
        		return false;
        	}
        	else
        	{
    			Vector V=new Vector();
    			if((commands.size()>2)
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
    			if(!(((Item)E).amWearingAt(Item.IN_INVENTORY)))
    			{
    				mob.tell(E.name()+" will have to be removed first.");
    				return false;
    			}
    			Vector ratesV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_AUCTIONRATES),true);
    			while(ratesV.size()<RATE_NUM) ratesV.addElement("0");
    			double deposit=CMath.s_double((String)ratesV.elementAt(RATE_LIVELIST));
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
    			if(liveAuctioningItem!=null) liveAuctionInvoker=mob;
                return true;
        	}
        }
        else
        if(cmd.equals("BID"))
        {
        	if(commands.size()<2)
        	{
        		mob.tell("Bid how much on WHAT?");
        		return false;
        	}
        	String amount=(String)commands.firstElement();
        	String which=CMParms.combine(commands,1);
        	if(((String)commands.lastElement()).equalsIgnoreCase("LIVE"))
        	{
        		commands.removeElementAt(commands.size()-1);
        		which="LIVE";
        		amount=CMParms.combine(commands,0);
        	}
        	Environmental E=null;
    		String key=null;
    		DVector auctions=getTimedAuctionNames(null);
        	if(which.equalsIgnoreCase("LIVE"))
        	{
        		if(liveAuctioningItem==null)
        		{
        			mob.tell("A live auction is not currently running.");
        			return false;
        		}
        		E=liveAuctioningItem;
        		key="LIVE";
        	}
        	else
        	{
        		E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,true);
        		if(E==null) E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,false);
        		if(E!=null)
        		for(int a=0;a<auctions.size();a++) 
        			if(auctions.elementAt(a,2)==E) 
        				key=(String)auctions.elementAt(a,1);
        		if(key==null)
        		{
        			mob.tell("'"+which+"' is not up for auction.  Try AUCTION LIST.");
        			return false;
        		}
        	}
        	if(E==liveAuctioningItem)
        	{
        		doLiveAuction(mob,CMParms.makeVector(amount),null);
    			return true;
    		}
        	Vector auctionData=null;
            Vector otherAuctions=CMLib.database().DBReadJournal("SYSTEM_AUCTIONS");
            for(int o=0;o<otherAuctions.size();o++)
            {
                Vector V=(Vector)otherAuctions.elementAt(o);
                String tkey=(String)V.elementAt(DatabaseEngine.JOURNAL_KEY);
                if(tkey.equalsIgnoreCase(key))
                { auctionData=V; break;}
            }
            if(auctionData==null){mob.tell("Error."); return false;}
            String from=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_FROM);
            String start=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_DATE);
            String xml=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_MSG);
            String subj=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_SUBJ);
            Vector xmlV=CMLib.xml().parseAllXML(xml);
            String bid=CMLib.xml().getValFromPieces(xmlV,"PRICE");
            String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
            String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
            Auction.AuctionData data=new Auction.AuctionData();
            data.liveAuctionBid=CMath.s_double(bid);
            MOB invoker=CMLib.map().getLoadPlayer(from);
			MOB M=liveData.liveAuctionHighBidder;
            data.liveAuctionCurrency=CMLib.beanCounter().getCurrency(invoker);
            data.liveAuctionHighBid=CMath.s_double(maxBid);
            if(highBidder.length()>0)
	            data.liveAuctionHighBidder=CMLib.map().getLoadPlayer(highBidder);
            data.liveAuctionStart=CMath.s_long(start);
            
            String[] resp=this.bid(mob,amount,data,(Item)E,new Vector());
            if(resp[0]!=null) 
            	mob.tell(resp[0]);
            if((resp[1]!=null)&&(M!=null))
            {
            	if(CMLib.flags().isInTheGame(M,true))
            		mob.tell(resp[1]);
            	else
            	if(M.playerStats()!=null)
            	{
                    CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),
						                            "auction@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
						                            "noreply@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
						                            M.playerStats().getEmail(),
						                            "Auction Update for item: "+E.name(),
						                            resp[1]);
            	}
            }
            if((!(""+data.liveAuctionBid).equals(bid))||((!(""+data.liveAuctionBid).equals(maxBid))))
            {
                StringBuffer xmlBuf=new StringBuffer("<AUCTION>");
                xmlBuf.append("<PRICE>"+data.liveAuctionBid+"</PRICE>");
                xmlBuf.append("<BIDDER>"+((data.liveAuctionHighBidder!=null)?data.liveAuctionHighBidder.Name():"")+"</BIDDER>");
                xmlBuf.append("<MAXBID>"+data.liveAuctionHighBid+"</MAXBID>");
                xmlBuf.append("<AUCTIONITEM>");
                xmlBuf.append(CMLib.coffeeMaker().getItemXML((Item)E).toString());
                xmlBuf.append("</AUCTIONITEM>");
                xmlBuf.append("</AUCTION>");
            	CMLib.database().DBUpdateJournal(key, getSubject((Item)E,data.liveAuctionCurrency,data.liveAuctionBid), xmlBuf.toString());
            }
        }
        else
		if(cmd.equals("CLOSE"))
		{
        	if(commands.size()==0)
        	{
        		mob.tell("Close which auction? Try LIST.");
        		return false;
        	}
        	String which=CMParms.combine(commands,0);
        	Environmental E=null;
    		String key=null;
    		DVector auctions=getTimedAuctionNames(which);
        	if(which.equalsIgnoreCase("LIVE"))
        	{
        		if((liveAuctioningItem==null)||(liveAuctionInvoker!=mob))
        		{
        			mob.tell("You are not currently running a live auction.");
        			return false;
        		}
        		E=liveAuctioningItem;
        		key="LIVE";
        	}
        	else
        	{
        		E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,true);
        		if(E==null) E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,false);
        		if(E!=null)
        		for(int a=0;a<auctions.size();a++) 
        			if(auctions.elementAt(a,2)==E) 
        				key=(String)auctions.elementAt(a,1);
        		if(key==null)
        		{
        			mob.tell("'"+which+"' is not up for auction by you.  Try AUCTION LIST.");
        			return false;
        		}
        	}
    		if(E==liveAuctioningItem)
    		{
    			Vector V=new Vector();
    			V.addElement("AUCTION");
    			V.addElement("The auction has been closed.");
    			CMLib.threads().deleteTick(this,Tickable.TICKID_QUEST);
    			if(E instanceof Item)
	    			liveAuctionInvoker.giveItem((Item)E);
    			returnMoney(liveData.liveAuctionHighBidder,liveData.liveAuctionCurrency,liveData.liveAuctionHighBid);
				liveAuctionInvoker=null;
				liveAuctioningItem=null;
    			super.execute(mob,V);
    			return true;
    		}
            Vector otherAuctions=CMLib.database().DBReadJournal("SYSTEM_AUCTIONS");
            for(int o=0;o<otherAuctions.size();o++)
            {
                Vector V=(Vector)otherAuctions.elementAt(o);
                String tkey=(String)V.elementAt(DatabaseEngine.JOURNAL_KEY);
                String from=(String)V.elementAt(DatabaseEngine.JOURNAL_FROM);
                if((tkey.equalsIgnoreCase(key))
                &&(from.equalsIgnoreCase(mob.Name())))
                {
        			if(E instanceof Item)
    	    			liveAuctionInvoker.giveItem((Item)E);
	                String xml=(String)V.elementAt(DatabaseEngine.JOURNAL_MSG);
	                Vector xmlV=CMLib.xml().parseAllXML(xml);
	                String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
	                String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
	                if(highBidder.length()>0)
	                {
	                	MOB M=CMLib.map().getLoadPlayer(highBidder);
	                	if(M!=null) returnMoney(M,liveData.liveAuctionCurrency,CMath.s_double(maxBid));
	                }
	                return true;
                }
            }
    		mob.tell("Not yet implemented");
    		return false;
		}
        else
		if(cmd.equals("INFO"))
		{
        	if(commands.size()==0)
        	{
        		mob.tell("Info on which auction? Try LIST.");
        		return false;
        	}
        	String which=CMParms.combine(commands,0);
        	Environmental E=null;
    		String key=null;
    		DVector auctions=getTimedAuctionNames(null);
        	if(which.equalsIgnoreCase("LIVE"))
        	{
        		if(liveAuctioningItem==null)
        		{
        			mob.tell("A live auction is not currently running.");
        			return false;
        		}
        		E=liveAuctioningItem;
        		key="LIVE";
        	}
        	else
        	{
        		E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,true);
        		if(E==null) E=CMLib.english().fetchEnvironmental(auctions.getDimensionVector(2),which,false);
        		if(E!=null)
        		for(int a=0;a<auctions.size();a++) 
        			if(auctions.elementAt(a,2)==E) 
        				key=(String)auctions.elementAt(a,1);
        		if(key==null)
        		{
        			mob.tell("'"+which+"' is not up for auction.  Try AUCTION LIST.");
        			return false;
        		}
        	}
        	Ability A=CMClass.getAbility("Spell_AnalyzeDweomer");
        	if(A!=null)
        	{
	        	mob.tell("^HDweomer of "+E.name()+": ^N\n\r");
	        	A.invoke(mob,E,true,0);
        	}
        	A=CMClass.getAbility("Spell_Identify");
        	if(A!=null)
        	{
	        	mob.tell("^HIdentification of "+E.name()+": ^N\n\r");
	        	A.invoke(mob,E,true,0);
        	}
        	return true;
		}
		else
		if(cmd.equals("CHANNEL"))
		{
        	if(commands.size()==0)
        	{
        		mob.tell("Channel what?");
        		return false;
        	}
    		if((liveAuctioningItem==null)||(liveAuctionInvoker==null))
    		{
    			mob.tell("Channeling is only allowed during live auctions.");
    			return false;
    		}
			super.execute(mob,commands);
			return true;
		}
        else
        if(cmd.equals("ADD"))
        {
			String startingPrice="0";
			if((commands.size()>2)
			&&((CMLib.english().numPossibleGold(mob,(String)commands.lastElement())>0)
				||(((String)commands.lastElement()).equals("0"))))
			{
				startingPrice=(String)commands.lastElement();
				commands.removeElementAt(commands.size()-1);
			}
			String s=CMParms.combine(commands,0);
			Item I=mob.fetchInventory(null,s);
			if(I==null)
			{
				mob.tell(s+" is not an item you have to auction.");
				return false;
			}
			if((I instanceof Container)&&(((Container)I).getContents().size()>0))
			{
				mob.tell(I.name()+" will have to be emptied first.");
				return false;
			}
			if(!(I.amWearingAt(Item.IN_INVENTORY)))
			{
				mob.tell(I.name()+" will have to be removed first.");
				return false;
			}
			
			Vector ratesV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_AUCTIONRATES),true);
			while(ratesV.size()<RATE_NUM) ratesV.addElement("0");
			
			startingPrice=mob.session().prompt("Starting price: ("+startingPrice+"): ",startingPrice);
			if((CMLib.english().numPossibleGold(mob,startingPrice)<=0)&&(!startingPrice.equals("0")))
			{
				mob.tell("'"+startingPrice+"' is not a valid price.  Aborting.");
				return false;
			}
			long startPrice=CMLib.english().numPossibleGold(mob,startingPrice);
			if(startPrice<0) startPrice=0;
			
			
			int maxDays=CMath.s_int((String)ratesV.elementAt(RATE_MAXDAYS));
			if(maxDays==0) maxDays=184;
			String days=mob.session().prompt("Number of game-days to run the auction (max="+maxDays+"): ","5");
			if((!CMath.isNumber(days))||(CMath.s_int(days)<=0))
			{
				mob.tell("'"+days+"' is not a valid number of days.  Aborting.");
				return false;
			}
			if((CMath.s_int(days)>maxDays))
			{
				mob.tell("The maximum number of game-days you may run an auction is "+maxDays+".  Aborting.");
				return false;
			}
			double deposit=CMath.s_double((String)ratesV.elementAt(RATE_TIMELIST));
			deposit+=(CMath.s_pct((String)ratesV.elementAt(RATE_TIMEPCTD))*new Integer(CMath.mul(CMath.s_int(days),I.baseGoldValue())).doubleValue());
			String depositAmt=CMLib.beanCounter().nameCurrencyLong(mob, deposit);
			
			if((mob.isMonster())
            ||(!mob.session().confirm("Auctioning "+I.name()+" will cost a listing fee of "+depositAmt+", proceed (Y/n)?","Y")))
				return false;
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(mob))<deposit)
			{
				mob.tell("You don't have enough "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(mob))+" to cover the listing fee!");
				return false;
			}
			long endTime=System.currentTimeMillis();
			endTime+=CMath.s_int(days)*(mob.location().getArea().getTimeObj().getHoursInDay()*Tickable.TIME_MILIS_PER_MUDHOUR);
			CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getCurrency(mob), deposit);
			if(I instanceof Container) ((Container)I).emptyPlease();
            StringBuffer xml=new StringBuffer("<AUCTION>");
            xml.append("<PRICE>"+startPrice+"</PRICE>");
            xml.append("<BIDDER />");
            xml.append("<MAXBID />");
            xml.append("<AUCTIONITEM>");
            xml.append(CMLib.coffeeMaker().getItemXML(I).toString());
            xml.append("</AUCTIONITEM>");
            xml.append("</AUCTION>");
			String subject=getSubject(I,CMLib.beanCounter().getCurrency(mob),new Long(startPrice).doubleValue());
			CMLib.database().DBWriteJournal("SYSTEM_AUCTION", 
											mob.Name(), 
											""+endTime,
											subject, 
											xml.toString(), 
											-1);
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("New "+CMath.s_int(days)+" day auction: "+subject);
			super.execute(mob,V);
			I.destroy();
    		return false;
        }
        
    	StringBuffer help=new StringBuffer("AUCTION Huh?  " +
    									   "Try AUCTION LIST ([MASK]), " +
    									   "AUCTION INFO [ITEM NAME], " +
    									   "AUCTION BID [AMOUNT] [NAME], " +
    									   "AUCTION BID [AMOUNT] LIVE, " +
    									   "AUCTION ADD [ITEM] [PRICE], " +
    									   "AUCTION LIVE [ITEM] [PRICE], " +
    									   "AUCTION CHANNEL [MESSAGE], " +
    									   "AUCTION CLOSE [ITEM NAME]" +
    									   "AUCTION CLOSE LIVE");
		mob.tell(help.toString());
		return false;
	}

	public String getSubject(Item I, String currency, double price)
	{
        String subject="";
        if(I instanceof Armor)
			subject="Armor";
        else
		if(I instanceof Pill)
			subject="Pill";
		else
		if(I instanceof Potion)
			subject="Potion";
		else
		if(I instanceof Scroll)
			subject="Scroll";
		else
		if(I instanceof Drink)
			subject="Drink";
		else
		if(I instanceof Food)
			subject="Food";
		else
		if(I instanceof Light)
			subject="Light";
		else
		if(I instanceof Wand)
			subject="Wand";
		else
		if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
			subject="Map";
		else
		if(I instanceof MiscMagic)
			subject="MagicItem";
		else
		if(I instanceof Electronics)
			subject="HighTech";
		else
		if(I instanceof InnKey)
			subject="InnKey";
		else
		if(I instanceof Key)
			subject="Key";
		else
		if(I instanceof LandTitle)
			subject="RealEstate";
		else
		if(CMLib.flags().isReadable(I))
			subject="Readable";
		else
		if(I instanceof DeadBody)
			subject="Corpse";
		else
		if(I instanceof Weapon)
			subject="Weapon";
		else
		if((I instanceof Container)&&(((Container)I).capacity()>0))
			subject="Container";
		else
		if(I instanceof Coins)
			subject="Currency";
		else
			subject="Unknown";
        return CMStrings.padRight(subject,10)+" "+CMStrings.padRight(""+I.envStats().level(),3)+" "+CMStrings.padRight(CMLib.beanCounter().nameCurrencyShort(currency, price),10)+" "+I.name();
	}
	
	public boolean canBeOrdered(){return true;}

	
}
