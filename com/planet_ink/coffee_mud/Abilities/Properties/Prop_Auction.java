package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_Auction extends Property
{
	public String ID() { return "Prop_Auction"; }
	public String name(){ return "Auction Ticker";}
	protected int canAffectCode(){return 0;}
	public String accountForYourself(){ return "";	}
	public Environmental auctioning=null;
	public MOB highBidder=null;
	String currency="";
	public double highBid=Integer.MIN_VALUE;
	public double bid=Integer.MIN_VALUE;
	public int state=-1;
	public long tickDown=0;
	public long auctionStart=0;

	public static final int STATE_START=0;
	public static final int STATE_RUNOUT=1;
	public static final int STATE_ONCE=2;
	public static final int STATE_TWICE=3;
	public static final int STATE_THREE=4;
	public static final int STATE_CLOSED=5;

	public void setAbilityCode(int code)
	{
		state=code;
		tickDown=15000/MudHost.TICK_TIME;
	}

	private MOB invoker=null;
	public MOB invoker(){return invoker;}
	public void setInvoker(MOB mob)
	{
		invoker=mob;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((--tickDown)<=0)
		{
			if((state==STATE_START)&&((System.currentTimeMillis()-auctionStart)<(5*15000)))
			{
				if(((System.currentTimeMillis()-auctionStart)>(3*15000))
				&&((highBidder==null)||(highBidder==invoker)))
					setAbilityCode(STATE_RUNOUT);
				else
					setAbilityCode(STATE_START);
				return true;
			}
			setAbilityCode(state+1);
			Vector V=new Vector();
			V.addElement("AUCTION");
			V.addElement("CHANNEL");
			MOB M=invoker();
			switch(state)
			{
			case STATE_RUNOUT:
				V.addElement("The auction for "+auctioning.name()+" is almost done. The current bid is "+BeanCounter.nameCurrencyShort(M,bid)+".");
				break;
			case STATE_ONCE:
				V.addElement(BeanCounter.nameCurrencyShort(M,bid)+" for "+auctioning.name()+" going ONCE!");
				break;
			case STATE_TWICE:
				V.addElement(BeanCounter.nameCurrencyShort(M,bid)+" for "+auctioning.name()+" going TWICE!");
				break;
			case STATE_THREE:
				V.addElement(auctioning.name()+" going for "+BeanCounter.nameCurrencyShort(M,bid)+"! Last chance!");
				break;
			case STATE_CLOSED:
				{
					if((highBidder!=null)&&(highBidder!=invoker()))
					{
						V.addElement(auctioning.name()+" SOLD to "+highBidder.name()+" for "+BeanCounter.nameCurrencyShort(M,bid)+".");
						M.doCommand(V);
						if(!Sense.canMove(highBidder))
						{
							highBidder.tell("You have won the auction, but are unable to pay or collect.  Please contact "+M.name()+" about this matter immediately.");
							M.tell(highBidder.name()+" is unable to pay or collect at this time. Please contact "+highBidder.charStats().himher()+" immediately.");
						}
						else
						if(BeanCounter.getTotalAbsoluteValue(highBidder,currency)<bid)
						{
							highBidder.tell("You can no longer cover your bid.  Please contact "+M.name()+" about this matter immediately.");
							M.tell(highBidder.name()+" can not cover the bid any longer! Please contact "+highBidder.charStats().himher()+" immediately.");
						}
						else
						{
							if((auctioning instanceof Item)
						    &&(Sense.isInTheGame(highBidder,true))
                            &&(Sense.isInTheGame(M,true)))
							{
								((Item)auctioning).unWear();
								highBidder.location().bringItemHere((Item)auctioning,Item.REFUSE_PLAYER_DROP);
								if(CommonMsgs.get(highBidder,null,(Item)auctioning,false)
                                ||(highBidder.isMine(auctioning)))
                                {
                                    BeanCounter.subtractMoney(highBidder,currency,bid);
                                    BeanCounter.addMoney(M,currency,bid);
    								M.tell(BeanCounter.nameCurrencyShort(M,bid)+" has been transferred to you as payment from "+highBidder.name()+".  The goods have also been transferred in exchange.");
    								highBidder.tell(BeanCounter.nameCurrencyShort(M,bid)+" has been transferred to "+M.name()+".  You should have received the auctioned goods.  This auction is complete.");
                                    if(auctioning instanceof LandTitle)
                                    {
                                        FullMsg msg=new FullMsg(M,highBidder,auctioning,CMMsg.MASK_GENERAL|CMMsg.TYP_GIVE,null);
                                        auctioning.executeMsg(highBidder,msg);
                                    }
                                }
                                else
                                {
                                    M.giveItem((Item)auctioning);
                                    M.tell("Your transaction could not be completed because "+highBidder.name()+" was unable to collect the item.  Please contact "+highBidder.name()+" about receipt of "+auctioning.name()+" for "+BeanCounter.nameCurrencyShort(M,bid)+".");
                                    highBidder.tell("Your transaction could not be completed because you were unable to collect the item.  Please contact "+M.name()+" about receipt of "+auctioning.name()+" for "+BeanCounter.nameCurrencyShort(M,bid)+".");
                                }
							}
							else
							{
								M.tell("Your transaction could not be completed.  Please contact "+highBidder.name()+" about receipt of "+auctioning.name()+" for "+BeanCounter.nameCurrencyShort(M,bid)+".");
								highBidder.tell("Your transaction could not be completed.  Please contact "+M.name()+" about receipt of "+auctioning.name()+" for "+BeanCounter.nameCurrencyShort(M,bid)+".");
							}
						}
					}
					if(M!=null)
						M.doCommand(Util.parse("AUCTION CLOSE"));
					setInvoker(null);
				}
				return false;
			}
			M.doCommand(V);
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		Vector V=new Vector();
		V.addElement("AUCTION");
		V.addElement("CHANNEL");
		if(target!=null)
		{
			setInvoker(mob);
			auctioning=target;
			String sb=Util.combine(commands,0);
		    currency=EnglishParser.numPossibleGoldCurrency(mob,sb);
		    double denomination=EnglishParser.numPossibleGoldDenomination(mob,currency,sb);
		    long num=EnglishParser.numPossibleGold(mob,sb);
		    bid=Util.mul(denomination,num);
			highBid=bid-1;
			auctionStart=System.currentTimeMillis();
			setAbilityCode(STATE_START);
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_QUEST,1);
			String bidWords=BeanCounter.nameCurrencyShort(currency,bid);
			V.addElement("New lot: "+auctioning.name()+".  The opening bid is "+bidWords+".");
		}
		else
		{
			if(state>0)	setAbilityCode(STATE_RUNOUT);
			double b=0;
			String sb="";
			String bwords="0";
			String myCurrency=BeanCounter.getCurrency(mob);
			if(commands!=null)
			{ 
			    sb=Util.combine(commands,0);
			    if(sb.length()>0)
			    {
				    myCurrency=EnglishParser.numPossibleGoldCurrency(mob,sb);
				    if(myCurrency!=null)
				    {
					    double denomination=EnglishParser.numPossibleGoldDenomination(mob,currency,sb);
					    long num=EnglishParser.numPossibleGold(mob,sb);
					    b=Util.mul(denomination,num);
					    bwords=BeanCounter.getDenominationName(myCurrency,denomination,num);
				    }
				    else
				        myCurrency=BeanCounter.getCurrency(mob);
			    }
			}
			String bidWords=BeanCounter.nameCurrencyShort(currency,bid);
			if(bidWords.length()==0) bidWords="0";
			String currencyName=BeanCounter.getDenominationName(currency);
			if(sb.length()==0)
			{
				mob.tell("Up for auction: "+auctioning.name()+".  The current bid is "+bidWords+".");
				return true;
			}

			if(!myCurrency.equals(currency))
			{
			    mob.tell("This auction is being bid in "+currencyName+" only.");
				return false;
			}
			
			if(b>BeanCounter.getTotalAbsoluteValue(mob,currency))
			{
				mob.tell("You don't have enough "+currencyName+" on hand to cover that bid.");
				return false;
			}

			if(b>highBid)
			{
				if((highBidder!=null)&&(highBidder!=mob))
				{
					highBidder.tell("You have been outbid for "+auctioning.name()+".");
					mob.tell("You have the high bid for "+auctioning.name()+".");
				}

				highBidder=mob;
				if(highBid<0.0) highBid=0.0;
				bid=highBid+1.0;
				highBid=b;
			}
			else
			if((b<bid)||(b==0))
			{
				mob.tell("Your bid of "+bwords+" is insufficient."+((bid>0)?" The current high bid is "+bidWords+".":""));
				return false;
			}
			else
			if((b==bid)&&(highBidder!=null))
			{
				mob.tell("You must bid higher than "+bidWords+" to have your bid accepted.");
				return false;
			}
			else
			if((b==highBid)&&(highBidder!=null))
			{
				if((highBidder!=null)&&(highBidder!=mob))
				{
					mob.tell("You have been outbid by proxy for "+auctioning.name()+".");
					highBidder.tell("Your high bid for "+auctioning.name()+" has been reached.");
				}
				bid=b;
			}
			else
			{
				bid=b;
				mob.tell("You have been outbid by proxy for "+auctioning.name()+".");
			}
			bidWords=BeanCounter.nameCurrencyShort(currency,bid);
			V.addElement("A new bid has been entered for "+auctioning.name()+". The current bid is "+bidWords+".");
		}
		if(invoker()!=null) invoker().doCommand(V);
		return true;
	}
}
