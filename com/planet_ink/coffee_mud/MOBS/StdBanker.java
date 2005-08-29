package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
public class StdBanker extends StdShopKeeper implements Banker
{
	public String ID(){return "StdBanker";}

	protected double coinInterest=-0.008;
	protected double itemInterest=-0.001;
	protected static Hashtable bankTimes=new Hashtable();

	public StdBanker()
	{
		super();
		Username="a banker";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A banker is waiting to serve you.");
		Factions.setAlignment(this,Faction.ALIGN_GOOD);
		setMoney(0);
		whatISell=ShopKeeper.DEAL_BANKER;
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}



	public int whatIsSold(){return whatISell;}
	public void setWhatIsSold(int newSellCode){
		if(newSellCode!=ShopKeeper.DEAL_CLANBANKER)
			whatISell=ShopKeeper.DEAL_BANKER;
		else
			whatISell=ShopKeeper.DEAL_CLANBANKER;
	}

	public String bankChain(){return text();}
	public void setBankChain(String name){setMiscText(name);}

	public void addDepositInventory(String mob, Item thisThang)
	{
		String name=thisThang.ID();
		if(thisThang instanceof Coins) name="COINS";
		CMClass.DBEngine().DBCreateData(mob,bankChain(),""+thisThang+Math.random(),name+";"+CoffeeMaker.getPropertiesStr(thisThang,true));
	}

	public void addDepositInventory(MOB mob, Item thisThang)
	{
		if(whatISell==ShopKeeper.DEAL_CLANBANKER)
		{
			if(mob.getClanID().length()==0)
				return;
			addDepositInventory(mob.getClanID(),thisThang);
		}
		else
			addDepositInventory(mob.Name(),thisThang);
	}

	public boolean delDepositInventory(MOB mob, Item thisThang)
	{
		if(whatISell==ShopKeeper.DEAL_CLANBANKER)
		{
			if(mob.getClanID().length()>0)
				return delDepositInventory(mob.getClanID(),thisThang);
		}
		else
			return delDepositInventory(mob.Name(),thisThang);
		return false;
	}

	protected Item makeItem(String data)
	{
		int x=data.indexOf(";");
		if(x<0) return null;
		Item I=null;
		if(data.substring(0,x).equals("COINS"))
			I=CMClass.getItem("StdCoins");
		else
			I=CMClass.getItem(data.substring(0,x));
		if(I!=null)
		{
			CoffeeMaker.setPropertiesStr(I,data.substring(x+1),true);
			if((I instanceof Coins)
			&&(((Coins)I).getDenomination()==0.0)
			&&(((Coins)I).getNumberOfCoins()>0)) 
			    ((Coins)I).setDenomination(1.0);
			I.recoverEnvStats();
			I.text();
			return I;
		}
		return null;
	}

	public boolean delDepositInventory(String mob, Item thisThang)
	{
		Vector V=getDepositInventory(mob);
		boolean money=thisThang instanceof Coins;
		boolean found=false;
		for(int v=V.size()-1;v>=0;v--)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(money&&((String)V2.elementAt(DATA_DATA)).startsWith("COINS;"))
			{
				CMClass.DBEngine().DBDeleteData(((String)V2.elementAt(DATA_USERID)),((String)V2.elementAt(DATA_BANK)),((String)V2.elementAt(DATA_KEY)));
				found=true;
			}
			if(!money)
			{
				Item I=makeItem((String)V2.elementAt(DATA_DATA));
				if(I==null) continue;
				if(thisThang.sameAs(I))
				{
					CMClass.DBEngine().DBDeleteData(((String)V2.elementAt(DATA_USERID)),((String)V2.elementAt(DATA_BANK)),((String)V2.elementAt(DATA_KEY)));
					return true;
				}
			}
		}
		return found;
	};
	public void delAllDeposits(String mob)
	{
		CMClass.DBEngine().DBDeleteData(mob,bankChain());
	};
	public int numberDeposited(String mob)
	{
		return getDepositInventory(mob).size();
	};
	public Vector getDepositedItems(String mob)
	{
		Vector V=getDepositInventory(mob);
		Vector mine=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(V2.size()>3)
			{
				Item I=makeItem((String)V2.elementAt(DATA_DATA));
				if(I!=null)	mine.addElement(I);
			}
		}
		return mine;
	}
	public Vector getDepositInventory(String mob)
	{
		return CMClass.DBEngine().DBReadData(mob,bankChain());
	};
	public Vector getAccountNames()
	{
		Vector V=CMClass.DBEngine().DBReadData(bankChain());
		HashSet h=new HashSet();
		Vector mine=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(!h.contains(V2.elementAt(DATA_USERID)))
			{
				h.add(V2.elementAt(DATA_USERID));
				mine.addElement(V2.elementAt(DATA_USERID));
			}
		}
		return mine;
	}

	public void bankLedger(MOB mob, String msg)
	{
	    String date=CoffeeUtensils.getFormattedDate(mob);
		if(whatISell==ShopKeeper.DEAL_CLANBANKER)
		{
			if(mob.getClanID().length()==0) return;
		    BeanCounter.bankLedger(bankChain(),mob.getClanID(),date+": "+msg);
		}
		else
		    BeanCounter.bankLedger(bankChain(),mob.Name(),date+": "+msg);
	}
	
	public Item findDepositInventory(MOB mob, String likeThis)
	{
		if(whatISell==ShopKeeper.DEAL_CLANBANKER)
		{
			if(mob.getClanID().length()==0) return null;
			return findDepositInventory(mob.getClanID(),likeThis);
		}
		return findDepositInventory(mob.Name(),likeThis);
	}

	public Item findDepositInventory(String mob, String likeThis)
	{
		Vector V=getDepositInventory(mob);
		if(Util.s_int(likeThis)>0)
			for(int v=0;v<V.size();v++)
			{
				Vector V2=(Vector)V.elementAt(v);
				if(((String)V2.elementAt(DATA_DATA)).startsWith("COINS;"))
					return makeItem((String)V2.elementAt(DATA_DATA));
			}
		else
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			Item I=makeItem((String)V2.elementAt(DATA_DATA));
			if(I==null) continue;
			if(EnglishParser.containsString(I.Name(),likeThis))
				return I;
		}
		return null;
	};



	public void setCoinInterest(double interest){coinInterest=interest;};
	public void setItemInterest(double interest){itemInterest=interest;};
	public double getCoinInterest(){return coinInterest;};
	public double getItemInterest(){return itemInterest;};

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		try{
		if(tickID==MudHost.TICK_MOB)
		{
			boolean proceed=false;
			// handle interest by watching the days go by...
			// put stuff up for sale if the account runs out
			synchronized(bankChain())
			{
				Long L=(Long)bankTimes.get(bankChain());
				if((L==null)||(L.longValue()<System.currentTimeMillis()))
				{
					L=new Long(System.currentTimeMillis()+new Long((location().getArea().getTimeObj().getHoursInDay())*MudHost.TIME_MILIS_PER_MUDHOUR*5).longValue());
					proceed=true;
					bankTimes.remove(bankChain());
					bankTimes.put(bankChain(),L);
				}
				if(proceed)
				{
					Vector V=CMClass.DBEngine().DBReadData(bankChain());
					Vector userNames=new Vector();
					for(int v=0;v<V.size();v++)
					{
						Vector V2=(Vector)V.elementAt(v);
						String name=(String)V2.elementAt(0);
						if(!userNames.contains(name))
						{
							if(!CMClass.DBEngine().DBUserSearch(null,name))
							{
								if((Clans.getClan(name))==null)
									delAllDeposits(name);
								else
									userNames.addElement(name);
							}
							else
								userNames.addElement(name);
						}
					}
					for(int u=0;u<userNames.size();u++)
					{
						String name=(String)userNames.elementAt(u);
						Coins coinItem=null;
						int totalValue=0;
						V=getDepositedItems(name);
						for(int v=0;v<V.size();v++)
						{
							Item I=(Item)V.elementAt(v);
							if(I instanceof Coins)
								coinItem=(Coins)I;
							else
							if(itemInterest!=0.0)
								totalValue+=I.value();
						}
						double newBalance=0.0;
						if(coinItem!=null) newBalance=coinItem.getTotalValue();
						newBalance+=Util.mul(newBalance,coinInterest);
						if(totalValue>0)
							newBalance+=Util.mul(totalValue,itemInterest);
						if(newBalance<0)
						{
							for(int v=0;v<V.size();v++)
							{
								Item I=(Item)V.elementAt(v);
								if(!(I instanceof Coins))
									addStoreInventory(I);
							}
							delAllDeposits(name);
						}
						else
						if((coinItem==null)||(newBalance!=coinItem.getTotalValue()))
						{
						    if(coinItem!=null)
						    {
							    if(newBalance>coinItem.getTotalValue())
							        BeanCounter.bankLedger(bankChain(),name,CoffeeUtensils.getFormattedDate(this)+": Deposit of "+BeanCounter.nameCurrencyShort(this,newBalance-coinItem.getTotalValue())+": Interest paid.");
							    else
							        BeanCounter.bankLedger(bankChain(),name,CoffeeUtensils.getFormattedDate(this)+": Withdrawl of "+BeanCounter.nameCurrencyShort(this,coinItem.getTotalValue()-newBalance)+": Interest charged.");
								delDepositInventory(name,coinItem);
						    }
							String currency=BeanCounter.getCurrency(this);
						    coinItem=BeanCounter.makeBestCurrency(currency,newBalance);
						    if(coinItem!=null)
								addDepositInventory(name,coinItem);
						}
					}
				}
			}
		}
		}catch(Exception e){Log.errOut("StdBanker",e);}
		return true;
	}

	protected double getBalance(MOB mob)
	{
		Item old=findDepositInventory(mob,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).getTotalValue();
		return 0;
	}

	protected double minBalance(MOB mob)
	{
		Vector V=null;
		if(whatISell==ShopKeeper.DEAL_CLANBANKER)
			V=getDepositedItems(mob.getClanID());
		else
			V=getDepositedItems(mob.Name());
		double min=0;
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.elementAt(v);
			if(I instanceof Coins) continue;
			min+=Util.div(I.value(),2.0);
		}
		return min;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
					if(msg.tool() instanceof Container)
						((Container)msg.tool()).emptyPlease();
					FullMsg msg2=new FullMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
					location().send(this,msg2);
					msg2=new FullMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
					location().send(this,msg2);
					if(msg.tool() instanceof Coins)
					{
						Coins older=(Coins)msg.tool();
						double newValue=older.getTotalValue();
						Item old=findDepositInventory(msg.source(),""+Integer.MAX_VALUE);
						MOB owner=msg.source();
						if((old==null)
						&&(whatISell!=ShopKeeper.DEAL_CLANBANKER)
						&&(msg.source().isMarriedToLiege()))
						{
							old=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if(old!=null) owner=CMMap.getPlayer(msg.source().getLiegeID());
						}
						if((old!=null)&&(old instanceof Coins))
						    newValue+=((Coins)old).getTotalValue();
						Coins item=BeanCounter.makeBestCurrency(BeanCounter.getCurrency(this),newValue);
					    bankLedger(owner,"Deposit of "+BeanCounter.nameCurrencyShort(this,newValue)+": "+msg.source().Name());
						if(old!=null) delDepositInventory(owner,old);
						if(item!=null)
							addDepositInventory(owner,item);
						if(whatISell==ShopKeeper.DEAL_CLANBANKER)
						    CommonMsgs.say(this,mob,"Ok, Clan "+owner.getClanID()+" now has a balance of "+BeanCounter.nameCurrencyLong(this,getBalance(owner))+".",true,false);
						else
						    CommonMsgs.say(this,mob,"Ok, your new balance is "+BeanCounter.nameCurrencyLong(this,getBalance(owner))+".",true,false);
						recoverEnvStats();
						((Coins)msg.tool()).setNumberOfCoins(0);
						double riches=BeanCounter.getTotalAbsoluteNativeValue(this);
						if(riches>0.0) BeanCounter.subtractMoney(this,riches);
					}
					else
					{
						addDepositInventory(msg.source(),(Item)msg.tool());
					    CommonMsgs.say(this,mob,"Thank you, "+msg.tool().name()+" is safe with us.",true,false);
						((Item)msg.tool()).destroy();
					}
				}
				return;
			case CMMsg.TYP_WITHDRAW:
				{
					Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						Item depositInventoryItem=findDepositInventory(msg.source(),""+Integer.MAX_VALUE);
						MOB owner=msg.source();
						if((whatISell!=ShopKeeper.DEAL_CLANBANKER)
						&&(msg.source().isMarriedToLiege())
						&&((depositInventoryItem==null)
						        ||((depositInventoryItem instanceof Coins)
						                &&(((Coins)depositInventoryItem).getTotalValue()<((Coins)old).getTotalValue()))))
						{
							Item item2=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if((item2!=null)&&(item2 instanceof Coins)&&(((Coins)item2).getTotalValue()>=((Coins)old).getTotalValue()))
							{
							    depositInventoryItem=item2;
								owner=CMMap.getPlayer(msg.source().getLiegeID());
							}
						}
						if((depositInventoryItem!=null)
						&&(depositInventoryItem instanceof Coins)
						&&(old instanceof Coins)
						&&(((Coins)depositInventoryItem).getTotalValue()>=((Coins)old).getTotalValue()))
						{
							Coins coins=BeanCounter.makeBestCurrency(this,((Coins)depositInventoryItem).getTotalValue()-((Coins)old).getTotalValue());
						    bankLedger(owner,"Withdrawl of "+BeanCounter.nameCurrencyShort(this,((Coins)old).getTotalValue())+": "+msg.source().Name());
							delDepositInventory(owner,depositInventoryItem);
							
					        addInventory(old);
							FullMsg newMsg=new FullMsg(this,msg.source(),old,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
							if(location().okMessage(this,newMsg))
							{
								location().send(this,newMsg);
								((Coins)old).putCoinsBack();
						    }
							else
								CommonMsgs.drop(this,old,true,false);
							if((coins==null)||(coins.getNumberOfCoins()<=0))
							{
								if(whatISell==ShopKeeper.DEAL_CLANBANKER)
									CommonMsgs.say(this,mob,"I have closed the account for Clan "+owner.getClanID()+". Thanks for your business.",true,false);
								else
									CommonMsgs.say(this,mob,"I have closed that account. Thanks for your business.",true,false);
								return;
							}
							addDepositInventory(owner,coins);
							if(whatISell==ShopKeeper.DEAL_CLANBANKER)
							    CommonMsgs.say(this,mob,"Ok, Clan "+owner.getClanID()+" now has a balance of "+BeanCounter.nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
							else
							    CommonMsgs.say(this,mob,"Ok, your new balance is "+BeanCounter.nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
						}
						else
						    CommonMsgs.say(this,mob,"But, your balance is "+BeanCounter.nameCurrencyLong(this,((Coins)depositInventoryItem).getTotalValue())+".",true,false);
					}
					else
					{
						if((!delDepositInventory(msg.source(),old))
						&&(whatISell!=ShopKeeper.DEAL_CLANBANKER)
						&&(msg.source().isMarriedToLiege()))
							delDepositInventory(msg.source().getLiegeID(),old);

					    CommonMsgs.say(this,mob,"Thank you for your trust.",true,false);
						if(location()!=null)
							location().addItemRefuse(old,Item.REFUSE_PLAYER_DROP);
						FullMsg msg2=new FullMsg(mob,old,this,CMMsg.MSG_GET,null);
						if(location().okMessage(mob,msg2))
							location().send(mob,msg2);
					}

				}
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				Vector V=null;
				if(whatISell==ShopKeeper.DEAL_CLANBANKER)
					V=getDepositedItems(mob.getClanID());
				else
				{
					V=getDepositedItems(mob.Name());
					if(mob.isMarriedToLiege())
					{
						Vector V2=getDepositedItems(mob.getLiegeID());
						if((V2!=null)&&(V2.size()>0))
							Util.addToVector(V2,V);
					}
				}

				StringBuffer str=new StringBuffer("");
				str.append("\n\rAccount balance at '"+bankChain()+"'.\n\r");
				String c="^x[Item                              ] ";
				str.append(c+c+"^.^N\n\r");
				int colNum=0;
				boolean otherThanCoins=false;
				for(int i=0;i<V.size();i++)
				{
					Item I=(Item)V.elementAt(i);
					if(!(I instanceof Coins))
					{
						otherThanCoins=true;
						String col=null;
						col="["+Util.padRight(I.name(),34)+"] ";
						if((++colNum)>2)
						{
							str.append("\n\r");
							colNum=1;
						}
						str.append(col);
					}
				}
				if(!otherThanCoins)
					str=new StringBuffer("\n\r^N");
				else
					str.append("\n\r\n\r");
				double balance=getBalance(mob);
				if(balance>0)
				{
					if(whatISell==ShopKeeper.DEAL_CLANBANKER)
						str.append("Clan "+mob.getClanID()+" has a balance of ^H"+BeanCounter.nameCurrencyLong(this,balance)+"^?.");
					else
						str.append("Your balance is ^H"+BeanCounter.nameCurrencyLong(this,balance)+"^?.");
				}
				if((whatISell!=ShopKeeper.DEAL_CLANBANKER)
				&&(mob.isMarriedToLiege()))
				{
					balance=getBalance(CMMap.getPlayer(mob.getLiegeID()));
					str.append("Your spouses balance is ^H"+balance+"^? gold coins.");
				}
				if(coinInterest!=0.0)
				{
					double cci=Util.mul(Math.abs(coinInterest),100.0);
					String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
					str.append("\n\rThey "+ci+"weekly on money deposited here.");
				}
				if(itemInterest!=0.0)
				{
					double cci=Util.mul(Math.abs(itemInterest),100.0);
					String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
					str.append("\n\rThey "+ci+"weekly on items deposited here.");
				}
				mob.tell(str.toString()+"^T");
				return;
			}
			default:
				break;
			}
		}
		else
		if(msg.sourceMinor()==CMMsg.TYP_RETIRE)
			delAllDeposits(msg.source().Name());
		super.executeMsg(myHost,msg);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
                    if(!ignoreIfNecessary(msg.source())) 
                        return false;
					if(msg.tool()==null) 
                        return false;
					double balance=getBalance(mob);
					if(msg.tool() instanceof Coins)
					{
						if((Double.MAX_VALUE-balance)<=((Coins)msg.tool()).getTotalValue())
						{
							CommonMsgs.say(this,mob,"I'm sorry, the law prevents us from holding that much money in one account.",true,false);
							return false;
						}
						if(!((Coins)msg.tool()).getCurrency().equalsIgnoreCase(BeanCounter.getCurrency(this)))
						{
						    CommonMsgs.say(this,mob,"I'm sorry, this bank only deals in "+BeanCounter.getDenominationName(BeanCounter.getCurrency(this))+".",true,false);
						    return false;
						}
						return true;
					}
					if((whatISell==ShopKeeper.DEAL_CLANBANKER)
					&&((msg.source().getClanID().length()==0)
					  ||(Clans.getClan(msg.source().getClanID())==null)))
					{
						CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
						return false;
					}
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
						return false;
					}
					double minbalance=minBalance(mob)+Util.div(((Item)msg.tool()).value(),2.0);
					if(balance<minbalance)
					{
						if(whatISell==ShopKeeper.DEAL_CLANBANKER)
							CommonMsgs.say(this,mob,"Clan "+msg.source().getClanID()+" will need a total balance of "+BeanCounter.nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
						else
							CommonMsgs.say(this,mob,"You'll need a total balance of "+BeanCounter.nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
						return false;
					}
				}
				return true;
			case CMMsg.TYP_WITHDRAW:
				{
                    if(!ignoreIfNecessary(msg.source())) 
                        return false;
					String thename=msg.source().Name();
					if(whatISell==ShopKeeper.DEAL_CLANBANKER)
					{
						thename=msg.source().getClanID();
						Clan C=Clans.getClan(msg.source().getClanID());
						if((msg.source().getClanID().length()==0)
						  ||(C==null))
						{
							CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
							return false;
						}

						if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANWITHDRAW)<0)
						{
							CommonMsgs.say(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
							return false;
						}
					}
					if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
					{
						CommonMsgs.say(this,mob,"What do you want? I'm busy!",true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					    return false;
					MOB owner=msg.source();
					double balance=getBalance(owner);
					if(msg.tool() instanceof Coins)
					{
					    if(!((Coins)msg.tool()).getCurrency().equals(BeanCounter.getCurrency(this)))
					    {
							CommonMsgs.say(this,mob,"I'm sorry, I can only give you "+BeanCounter.getDenominationName(BeanCounter.getCurrency(this))+".",true,false);
							return false;
					    }
					    
						if((whatISell!=ShopKeeper.DEAL_CLANBANKER)
						&&(owner.isMarriedToLiege())
						&&(balance<((Coins)msg.tool()).getTotalValue()))
						{
							MOB M=CMMap.getLoadPlayer(owner.getLiegeID());
							double b=0.0;
							if(M!=null) b=getBalance(M);
							if((M!=null)&&(b>=((Coins)msg.tool()).getTotalValue()))
							{
								owner=M;
								balance=b;
								thename=owner.Name();
							}
						}
					}
					else
					if(findDepositInventory(thename,msg.tool().Name())==null)
					{
						if((whatISell!=ShopKeeper.DEAL_CLANBANKER)
						&&(msg.source().isMarriedToLiege())
						&&(findDepositInventory(msg.source().getLiegeID(),msg.tool().Name())!=null))
							owner=CMMap.getPlayer(msg.source().getLiegeID());
						else
						{
							CommonMsgs.say(this,mob,"You want WHAT?",true,false);
							return false;
						}
					}
					double minbalance=minBalance(owner);
					if(msg.tool() instanceof Coins)
					{
						if(((Coins)msg.tool()).getTotalValue()>balance)
						{
							if(whatISell==ShopKeeper.DEAL_CLANBANKER)
								CommonMsgs.say(this,mob,"I'm sorry, Clan "+thename+" has only "+BeanCounter.nameCurrencyShort(this,balance)+" in its account.",true,false);
							else
								CommonMsgs.say(this,mob,"I'm sorry, you have only "+BeanCounter.nameCurrencyShort(this,balance)+" in that account.",true,false);
							return false;
						}
						if(minbalance==0) return true;
						if(((Coins)msg.tool()).getTotalValue()>(balance-minbalance))
						{
							if((balance-minbalance)>0)
								CommonMsgs.say(this,mob,"I'm sorry, you may only withdraw "+BeanCounter.nameCurrencyShort(this,balance-minbalance)+"  at this time.",true,false);
							else
								CommonMsgs.say(this,mob,"I am holding other items in trust, so you may not withdraw funds at this time.",true,false);
							return false;
						}
					}
				}
				return true;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_BUY:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_LIST:
			{
                if(!ignoreIfNecessary(msg.source())) 
                    return false;
				String thename=msg.source().Name();
				if(whatISell==ShopKeeper.DEAL_CLANBANKER)
				{
					thename=msg.source().getClanID();
					Clan C=Clans.getClan(msg.source().getClanID());
					if((msg.source().getClanID().length()==0)
					  ||(C==null))
					{
						CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
						return false;
					}
					if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANDEPOSITLIST)<0)
					{
						CommonMsgs.say(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
						return false;
					}
				}
				else
				if((numberDeposited(thename)==0)
				&&((whatISell==ShopKeeper.DEAL_CLANBANKER)
				   ||(!msg.source().isMarriedToLiege())
				   ||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					StringBuffer str=new StringBuffer("");
					if(whatISell==ShopKeeper.DEAL_CLANBANKER)
						str.append("The Clan "+thename+" does not have an account with us, I'm afraid.");
					else
						str.append("You don't have an account with us, I'm afraid.");
					if(coinInterest!=0.0)
					{
						double cci=Util.mul(Math.abs(coinInterest),100.0);
						String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"weekly on money deposited here.");
					}
					if(itemInterest!=0.0)
					{
						double cci=Util.mul(Math.abs(itemInterest),100.0);
						String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"weekly on items kept with us.");
					}
					if(bankChain().length()>0)
						str.append("\n\rI am a banker for "+bankChain()+".");
					CommonMsgs.say(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				else
					return true;
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
