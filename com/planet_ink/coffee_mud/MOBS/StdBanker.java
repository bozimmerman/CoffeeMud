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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class StdBanker extends StdShopKeeper implements Banker
{
	public String ID(){return "StdBanker";}

	protected double coinInterest=-0.008;
	protected double itemInterest=-0.001;
	protected double loanInterest=0.01;
	protected static Hashtable bankTimes=new Hashtable();

	public StdBanker()
	{
		super();
		Username="a banker";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A banker is waiting to serve you.");
		CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);
		setMoney(0);
		whatIsSoldMask=ShopKeeper.DEAL_BANKER;
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

    public void destroy()
    {
        super.destroy();
        CMLib.map().delBank(this);
    }
    public void bringToLife(Room newLocation, boolean resetStats)
    {
        super.bringToLife(newLocation,resetStats);
        CMLib.map().addBank(this);
    }

    public void addSoldType(int mask){setWhatIsSoldMask(CMath.abs(mask));}
	public void setWhatIsSoldMask(long newSellCode){
    	super.setWhatIsSoldMask(newSellCode);
    	if(!isSold(ShopKeeper.DEAL_CLANBANKER))
    		whatIsSoldMask=ShopKeeper.DEAL_BANKER;
    	else
    		whatIsSoldMask=ShopKeeper.DEAL_CLANBANKER;
	}

	public String bankChain(){return text();}
	public void setBankChain(String name){setMiscText(name);}

	public void addDepositInventory(String mob, Item thisThang)
	{
		String name=thisThang.ID();
		if(thisThang instanceof Coins) name="COINS";
    	CMLib.catalog().updateCatalogIntegrity(thisThang);
		CMLib.database().DBCreateData(mob,bankChain(),""+thisThang+Math.random(),name+";"+CMLib.coffeeMaker().getPropertiesStr(thisThang,true));
	}

	public void addDepositInventory(MOB mob, Item thisThang)
	{
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
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
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
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
			CMLib.coffeeMaker().setPropertiesStr(I,data.substring(x+1),true);
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
		Vector V=getRawPDDepositInventory(mob);
		boolean money=thisThang instanceof Coins;
		boolean found=false;
		for(int v=V.size()-1;v>=0;v--)
		{
			DatabaseEngine.PlayerData PD=(DatabaseEngine.PlayerData)V.elementAt(v);
			if(money&&(PD.xml).startsWith("COINS;"))
			{
				CMLib.database().DBDeleteData(PD.who,PD.section,PD.key);
				found=true;
			}
			if(!money)
			{
				Item I=makeItem(PD.xml);
				if(I==null) continue;
				if(thisThang.sameAs(I))
				{
					CMLib.database().DBDeleteData(PD.who,PD.section,PD.key);
					return true;
				}
			}
		}
		return found;
	}
	public void delAllDeposits(String mob)
	{
		CMLib.database().DBDeleteData(mob,bankChain());
	}
	public int numberDeposited(String mob)
	{
		return getRawPDDepositInventory(mob).size();
	}
	public Vector getDepositedItems(MOB mob)
	{
		if(mob==null) return new Vector();
		return getDepositedItems((isSold(ShopKeeper.DEAL_CLANBANKER))?mob.getClanID():mob.Name());
	}
	public Vector getDepositedItems(String mob)
	{
		if((mob==null)||(mob.length()==0)) return new Vector();
		Vector V=getRawPDDepositInventory(mob);
		Vector mine=new Vector();
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData PD=(DatabaseEngine.PlayerData)V.elementAt(v);
			Item I=makeItem(PD.xml);
			if(I!=null)	mine.addElement(I);
		}
		return mine;
	}
	public Vector getRawPDDepositInventory(String mob)
	{
		return CMLib.database().DBReadData(mob,bankChain());
	}
	public Vector getAccountNames()
	{
		Vector V=CMLib.database().DBReadData(bankChain());
		HashSet h=new HashSet();
		Vector mine=new Vector();
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData V2=(DatabaseEngine.PlayerData)V.elementAt(v);
			if(!h.contains(V2.who))
			{
				h.add(V2.who);
				mine.addElement(V2.who);
			}
		}
		return mine;
	}

	public void bankLedger(MOB mob, String msg)
	{
	    String date=CMLib.utensils().getFormattedDate(mob);
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
		{
			if(mob.getClanID().length()==0) return;
		    CMLib.beanCounter().bankLedger(bankChain(),mob.getClanID(),date+": "+msg);
		}
		else
		    CMLib.beanCounter().bankLedger(bankChain(),mob.Name(),date+": "+msg);
	}

	public Item findDepositInventory(MOB mob, String likeThis)
	{
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
		{
			if(mob.getClanID().length()==0) return null;
			return findDepositInventory(mob.getClanID(),likeThis);
		}
		return findDepositInventory(mob.Name(),likeThis);
	}

	public Item findDepositInventory(String mob, String likeThis)
	{
		Vector V=getRawPDDepositInventory(mob);
		if(CMath.s_int(likeThis)>0)
			for(int v=0;v<V.size();v++)
			{
				DatabaseEngine.PlayerData PD=(DatabaseEngine.PlayerData)V.elementAt(v);
				if(PD.xml.startsWith("COINS;"))
					return makeItem(PD.xml);
			}
		else
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData PD=(DatabaseEngine.PlayerData)V.elementAt(v);
			Item I=makeItem(PD.xml);
			if(I==null) continue;
			if(CMLib.english().containsString(I.Name(),likeThis))
				return I;
		}
		return null;
	}

	public long timeInterval()
	{
		return (location().getArea().getTimeObj().getHoursInDay())
				*Tickable.TIME_MILIS_PER_MUDHOUR
				*location().getArea().getTimeObj().getDaysInMonth();
	}

	public void setCoinInterest(double interest){coinInterest=interest;}
	public void setItemInterest(double interest){itemInterest=interest;}
	public double getCoinInterest(){return coinInterest;}
	public double getItemInterest(){return itemInterest;}
	public void setLoanInterest(double interest){loanInterest=interest;}
	public double getLoanInterest(){return loanInterest;}
	public MoneyLibrary.DebtItem getDebtInfo(MOB mob)
	{
		Vector<MoneyLibrary.DebtItem> debt=CMLib.beanCounter().getDebtOwed(bankChain());
		if(mob==null) return null;
		String whom=(isSold(ShopKeeper.DEAL_CLANBANKER))?mob.getClanID():mob.Name();
		if((whom==null)||(whom.length()==0)) return null;
		for(int d=0;d<debt.size();d++)
			if(debt.elementAt(d).debtor.equalsIgnoreCase(whom))
				return debt.elementAt(d);
		return null;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) return true;
		try{
		if(tickID==Tickable.TICKID_MOB)
		{
			boolean proceed=false;
			// handle interest by watching the days go by...
			// put stuff up for sale if the account runs out
			synchronized(bankChain().intern())
			{
				Long L=(Long)bankTimes.get(bankChain());
				long timeInterval=1;
				if(((L==null)||(L.longValue()<System.currentTimeMillis()))
				&&(location!=null)
				&&(location.getArea()!=null)
				&&(location.getArea().getTimeObj()!=null)
				&&(CMLib.flags().isInTheGame(this,true)))
				{
					timeInterval=timeInterval();
					L=Long.valueOf(System.currentTimeMillis()+timeInterval);
					proceed=true;
					bankTimes.remove(bankChain());
					bankTimes.put(bankChain(),L);
				}
				if(proceed)
				{
					Vector bankDataV=CMLib.database().DBReadData(bankChain());
					Vector userNames=new Vector();
					for(int v=0;v<bankDataV.size();v++)
					{
						DatabaseEngine.PlayerData dat=(DatabaseEngine.PlayerData)bankDataV.elementAt(v);
						String name=dat.who;
						if(!userNames.contains(name))
						{
							if(!CMLib.players().playerExists(name))
							{
								if((CMLib.clans().getClan(name))==null)
									delAllDeposits(name);
								else
									userNames.addElement(name);
							}
							else
								userNames.addElement(name);
						}
					}
					Vector<MoneyLibrary.DebtItem> debts=CMLib.beanCounter().getDebtOwed(bankChain());
					for(int u=0;u<userNames.size();u++)
					{
						String name=(String)userNames.elementAt(u);
						Coins coinItem=null;
						int totalValue=0;
						Vector items=getDepositedItems(name);
						for(int v=0;v<items.size();v++)
						{
							Item I=(Item)items.elementAt(v);
							if(I instanceof Coins)
								coinItem=(Coins)I;
							else
							if(itemInterest!=0.0)
								totalValue+=I.value();
						}
						double newBalance=0.0;
						if(coinItem!=null) newBalance=coinItem.getTotalValue();
						newBalance+=CMath.mul(newBalance,coinInterest);
						if(totalValue>0)
							newBalance+=CMath.mul(totalValue,itemInterest);
						for(int d=debts.size()-1;d>=0;d--)
						{
							MoneyLibrary.DebtItem debtItem=debts.elementAt(d);
							String debtor=debtItem.debtor;
							if(debtor.equalsIgnoreCase(name))
							{
								long debtDueAt=debtItem.due;
								double intRate=debtItem.interest;
								double dueAmount=debtItem.amt;
								String reason=debtItem.reason;
								double intDue=CMath.mul(intRate,dueAmount);
								long timeRemaining=debtDueAt-System.currentTimeMillis();
								if((timeRemaining<0)&&(newBalance<((dueAmount)+intDue)))
									newBalance=-1.0;
								else
								{
									double amtDueNow=(timeRemaining<0)?(dueAmount+intDue):CMath.div((dueAmount+intDue),(timeRemaining/timeInterval));
									if(newBalance>=amtDueNow)
									{
								        CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Withdrawal of "+CMLib.beanCounter().nameCurrencyShort(this,amtDueNow)+": Loan payment made.");
										CMLib.beanCounter().adjustDebt(debtor,bankChain(),intDue-amtDueNow,reason,intRate,debtDueAt);
										newBalance-=amtDueNow;
									}
									else
										CMLib.beanCounter().adjustDebt(debtor,bankChain(),intDue,reason,intRate,debtDueAt);
								}
								debts.removeElementAt(d);
							}
						}
						if(newBalance<0)
						{
							for(int v=0;v<items.size();v++)
							{
								Item I=(Item)items.elementAt(v);
								if((I instanceof LandTitle)&&(((LandTitle)I).landOwner().length()>0))
								{
									((LandTitle)I).setLandOwner("");
									((LandTitle)I).updateTitle();
									((LandTitle)I).updateLot(null);
								}
								if(!(I instanceof Coins))
                                    getShop().addStoreInventory(I);
							}
							delAllDeposits(name);
							CMLib.beanCounter().delAllDebt(name,bankChain());
						}
						else
						if((coinItem==null)||(newBalance!=coinItem.getTotalValue()))
						{
						    if(coinItem!=null)
						    {
							    if(newBalance>coinItem.getTotalValue())
							        CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(this,newBalance-coinItem.getTotalValue())+": Interest paid.");
							    else
							        CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(this,coinItem.getTotalValue()-newBalance)+": Interest charged.");
								delDepositInventory(name,coinItem);
						    }
							String currency=CMLib.beanCounter().getCurrency(this);
						    coinItem=CMLib.beanCounter().makeBestCurrency(currency,newBalance);
						    if(coinItem!=null)
								addDepositInventory(name,coinItem);
						}
					}
					for(int d=debts.size()-1;d>=0;d--)
						CMLib.beanCounter().delAllDebt(debts.elementAt(d).debtor,bankChain());
				}
			}
		}
		}catch(Exception e){Log.errOut("StdBanker",e);}
		return true;
	}

	public double getBalance(MOB mob)
	{
		Item old=findDepositInventory(mob,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).getTotalValue();
		return 0;
	}

	public double totalItemsWorth(MOB mob)
	{
		Vector V=null;
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
			V=getDepositedItems(mob.getClanID());
		else
			V=getDepositedItems(mob.Name());
		double min=0;
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.elementAt(v);
			if(!(I instanceof Coins))
				min+=I.value();
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
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if(msg.tool() instanceof Container)
						((Container)msg.tool()).emptyPlease();
					CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
					location().send(this,msg2);
					msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
					location().send(this,msg2);
					if(msg.tool() instanceof Coins)
					{
						Coins older=(Coins)msg.tool();
						double newValue=older.getTotalValue();
						Item old=findDepositInventory(msg.source(),""+Integer.MAX_VALUE);
						MOB owner=msg.source();
						if((old==null)
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
						{
							old=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if(old!=null) owner=CMLib.players().getPlayer(msg.source().getLiegeID());
						}
						if((old!=null)&&(old instanceof Coins))
						    newValue+=((Coins)old).getTotalValue();
						Coins item=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(this),newValue);
					    bankLedger(owner,"Deposit of "+CMLib.beanCounter().nameCurrencyShort(this,newValue)+": "+msg.source().Name());
						if(old!=null) delDepositInventory(owner,old);
						if(item!=null)
							addDepositInventory(owner,item);
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
						    CMLib.commands().postSay(this,mob,"Ok, Clan "+owner.getClanID()+" now has a balance of "+CMLib.beanCounter().nameCurrencyLong(this,getBalance(owner))+".",true,false);
						else
						    CMLib.commands().postSay(this,mob,"Ok, your new balance is "+CMLib.beanCounter().nameCurrencyLong(this,getBalance(owner))+".",true,false);
						recoverEnvStats();

						if(msg.sourceMessage()!=null) msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.targetMessage()!=null) msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.othersMessage()!=null) msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(),"<O-NAME>",msg.tool().name()));
						((Coins)msg.tool()).setNumberOfCoins(0); // prevents banker from accumulating wealth
						double riches=CMLib.beanCounter().getTotalAbsoluteNativeValue(this);
						if(riches>0.0) CMLib.beanCounter().subtractMoney(this,riches);
					}
					else
					{
						addDepositInventory(msg.source(),(Item)msg.tool());
					    CMLib.commands().postSay(this,mob,"Thank you, "+msg.tool().name()+" is safe with us.",true,false);
						((Item)msg.tool()).destroy();
					}
				}
				return;
			case CMMsg.TYP_BORROW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						MOB borrower=msg.source();
					    bankLedger(borrower,"Loan of "+old.Name()+": "+msg.source().Name());
				        addInventory(old);
				        double amt=((Coins)old).getTotalValue();
						CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
						if(location().okMessage(this,newMsg))
						{
							location().send(this,newMsg);
							((Coins)old).putCoinsBack();
					    }
						else
							CMLib.commands().postDrop(this,old,true,false);
						double interestRate=getLoanInterest();
						int months=2;
						while((months<(location().getArea().getTimeObj().getMonthsInYear()*10))
							&&(CMath.div(amt,months)>250.0)) months++;
						long dueAt=System.currentTimeMillis()+(timeInterval()*months);
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.beanCounter().adjustDebt(msg.source().getClanID(),bankChain(),amt,"Bank Loan",interestRate,dueAt);
						else
							CMLib.beanCounter().adjustDebt(msg.source().Name(),bankChain(),amt,"Bank Loan",interestRate,dueAt);
					}
				}
				break;
			case CMMsg.TYP_WITHDRAW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						Item depositInventoryItem=findDepositInventory(msg.source(),""+Integer.MAX_VALUE);
						MOB owner=msg.source();
						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege())
						&&((depositInventoryItem==null)
						        ||((depositInventoryItem instanceof Coins)
						                &&(((Coins)depositInventoryItem).getTotalValue()<((Coins)old).getTotalValue()))))
						{
							Item item2=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if((item2!=null)&&(item2 instanceof Coins)&&(((Coins)item2).getTotalValue()>=((Coins)old).getTotalValue()))
							{
							    depositInventoryItem=item2;
								owner=CMLib.players().getPlayer(msg.source().getLiegeID());
							}
						}
						if((depositInventoryItem!=null)
						&&(depositInventoryItem instanceof Coins)
						&&(old instanceof Coins)
						&&(((Coins)depositInventoryItem).getTotalValue()>=((Coins)old).getTotalValue()))
						{
							Coins coins=CMLib.beanCounter().makeBestCurrency(this,((Coins)depositInventoryItem).getTotalValue()-((Coins)old).getTotalValue());
						    bankLedger(owner,"Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(this,((Coins)old).getTotalValue())+": "+msg.source().Name());
							delDepositInventory(owner,depositInventoryItem);

					        addInventory(old);
							CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
							if(location().okMessage(this,newMsg))
							{
								location().send(this,newMsg);
								((Coins)old).putCoinsBack();
						    }
							else
								CMLib.commands().postDrop(this,old,true,false);
							if((coins==null)||(coins.getNumberOfCoins()<=0))
							{
								if(isSold(ShopKeeper.DEAL_CLANBANKER))
									CMLib.commands().postSay(this,mob,"I have closed the account for Clan "+owner.getClanID()+". Thanks for your business.",true,false);
								else
									CMLib.commands().postSay(this,mob,"I have closed that account. Thanks for your business.",true,false);
								return;
							}
							addDepositInventory(owner,coins);
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
							    CMLib.commands().postSay(this,mob,"Ok, Clan "+owner.getClanID()+" now has a balance of "+CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
							else
							    CMLib.commands().postSay(this,mob,"Ok, your new balance is "+CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
						}
						else
						if(depositInventoryItem!=null)
						    CMLib.commands().postSay(this,mob,"But, your balance is "+CMLib.beanCounter().nameCurrencyLong(this,((Coins)depositInventoryItem).getTotalValue())+".",true,false);
					}
					else
					{
						if((!delDepositInventory(msg.source(),old))
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
							delDepositInventory(msg.source().getLiegeID(),old);

					    CMLib.commands().postSay(this,mob,"Thank you for your trust.",true,false);
						if(location()!=null)
							location().addItemRefuse(old,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
						CMMsg msg2=CMClass.getMsg(mob,old,this,CMMsg.MSG_GET,null);
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
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					Vector V=null;
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						V=getDepositedItems(mob.getClanID());
					else
					{
						V=getDepositedItems(mob.Name());
						if(mob.isMarriedToLiege())
						{
							Vector V2=getDepositedItems(mob.getLiegeID());
							if((V2!=null)&&(V2.size()>0))
								CMParms.addToVector(V2,V);
						}
					}
					for(int v=V.size()-1;v>=0;v--)
						if(V.elementAt(v) instanceof LandTitle)
						{
							LandTitle L=(LandTitle)V.elementAt(v);
							if(L.landOwnerObject()==null)
							{
								delDepositInventory(mob,(Item)L);
								V.removeElement(L);
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
							col="["+CMStrings.padRight(I.name(),34)+"] ";
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
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							str.append("Clan "+mob.getClanID()+" has a balance of ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
						else
							str.append("Your balance is ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
					}
					Vector<MoneyLibrary.DebtItem> debts=null;
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						debts=CMLib.beanCounter().getDebt(mob.getClanID(),bankChain());
					else
						debts=CMLib.beanCounter().getDebt(mob.Name(),bankChain());
					if(debts!=null)
					for(int d=0;d<debts.size();d++)
					{
						MoneyLibrary.DebtItem debt=debts.elementAt(d);
						long debtDueAt=debt.due;
						double intRate=debt.interest;
						double dueAmount=debt.amt;
						long timeRemaining=debtDueAt-System.currentTimeMillis();
						if(timeRemaining>0)
							str.append("\n\r"
									+((isSold(ShopKeeper.DEAL_CLANBANKER))?"Clan "+mob.getClanID():"You")
									+" owe ^H"+CMLib.beanCounter().nameCurrencyLong(this,dueAmount)+"^? in debt.\n\r"
									+"Monthly interest is "+(intRate*100.0)+"%.  "
									+"The loan must be paid in full in "
									+(timeRemaining/timeInterval())+" months.");
					}
					if(coinInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(coinInterest),100.0);
						String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rThey "+ci+"monthly on money deposited here.");
					}
					if(itemInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(itemInterest),100.0);
						String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rThey "+ci+"monthly on items deposited here.");
					}
					mob.tell(str.toString()+"^T");
				}
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
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
                        return false;
					if(msg.tool()==null)
                        return false;
					double balance=getBalance(mob);
					if((isSold(ShopKeeper.DEAL_CLANBANKER))
					&&((msg.source().getClanID().length()==0)
					  ||(CMLib.clans().getClan(msg.source().getClanID())==null)))
					{
						CMLib.commands().postSay(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
						return false;
					}
					if(msg.tool() instanceof Coins)
					{
						if((Double.MAX_VALUE-balance)<=((Coins)msg.tool()).getTotalValue())
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, the law prevents us from holding that much money in one account.",true,false);
							return false;
						}
						if(!((Coins)msg.tool()).getCurrency().equalsIgnoreCase(CMLib.beanCounter().getCurrency(this)))
						{
						    CMLib.commands().postSay(this,mob,"I'm sorry, this bank only deals in "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))+".",true,false);
						    return false;
						}
						return true;
					}
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
						return false;
					}
					double minbalance=(totalItemsWorth(mob)/MIN_ITEM_BALANCE_DIVIDEND)+CMath.div(((Item)msg.tool()).value(),MIN_ITEM_BALANCE_DIVIDEND);
					if(balance<minbalance)
					{
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.commands().postSay(this,mob,"Clan "+msg.source().getClanID()+" will need a total balance of "+CMLib.beanCounter().nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
						else
							CMLib.commands().postSay(this,mob,"You'll need a total balance of "+CMLib.beanCounter().nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
						return false;
					}
				}
				return true;
			case CMMsg.TYP_WITHDRAW:
				{
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
                        return false;
					String thename=msg.source().Name();
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
					{
						thename=msg.source().getClanID();
						Clan C=CMLib.clans().getClan(msg.source().getClanID());
						if((msg.source().getClanID().length()==0)
						  ||(C==null))
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
							return false;
						}

						if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANWITHDRAW)<0)
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
							return false;
						}
					}
					if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
					{
						CMLib.commands().postSay(this,mob,"What do you want? I'm busy!",true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					    return false;
					MOB owner=msg.source();
					double balance=getBalance(owner);
					double collateral=totalItemsWorth(owner);
					if(msg.tool() instanceof Coins)
					{
					    if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(this)))
					    {
							CMLib.commands().postSay(this,mob,"I'm sorry, I can only give you "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))+".",true,false);
							return false;
					    }

						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(owner.isMarriedToLiege())
						&&(balance<((Coins)msg.tool()).getTotalValue()))
						{
							MOB M=CMLib.players().getLoadPlayer(owner.getLiegeID());
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
						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege())
						&&(findDepositInventory(msg.source().getLiegeID(),msg.tool().Name())!=null))
							owner=CMLib.players().getPlayer(msg.source().getLiegeID());
						else
						{
							CMLib.commands().postSay(this,mob,"You want WHAT?",true,false);
							return false;
						}
					}
					else
					if(msg.tool() instanceof Item)
					{
						double debt=(isSold(ShopKeeper.DEAL_CLANBANKER))?
										CMLib.beanCounter().getDebtOwed(mob.getClanID(),bankChain()):
											CMLib.beanCounter().getDebtOwed(mob.Name(),bankChain());
						if((debt>0.0)
						&&((collateral-((Item)msg.tool()).value())<debt))
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, but that item is being held as collateral against your debt at this time.",true,false);
							return false;
						}
					}
					double minbalance=(collateral/MIN_ITEM_BALANCE_DIVIDEND);
					if(msg.tool() instanceof Coins)
					{
						if(((Coins)msg.tool()).getTotalValue()>balance)
						{
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
								CMLib.commands().postSay(this,mob,"I'm sorry, Clan "+thename+" has only "+CMLib.beanCounter().nameCurrencyShort(this,balance)+" in its account.",true,false);
							else
								CMLib.commands().postSay(this,mob,"I'm sorry, you have only "+CMLib.beanCounter().nameCurrencyShort(this,balance)+" in that account.",true,false);
							return false;
						}
						if(minbalance==0) return true;
						if(((Coins)msg.tool()).getTotalValue()>(balance-minbalance))
						{
							if((balance-minbalance)>0)
								CMLib.commands().postSay(this,mob,"I'm sorry, you may only withdraw "+CMLib.beanCounter().nameCurrencyShort(this,balance-minbalance)+"  at this time.",true,false);
							else
								CMLib.commands().postSay(this,mob,"I am holding other items in trust, so you may not withdraw funds at this time.",true,false);
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
			case CMMsg.TYP_BORROW:
			{
                if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
                    return false;
                if(!(msg.tool() instanceof Coins))
                {
					CMLib.commands().postSay(this,mob,"I'm sorry, only MONEY can be borrowed.",true,false);
                	return false;
                }
				String thename=msg.source().Name();
				if(isSold(ShopKeeper.DEAL_CLANBANKER))
				{
					thename=msg.source().getClanID();
					Clan C=CMLib.clans().getClan(msg.source().getClanID());
					if((msg.source().getClanID().length()==0)
					  ||(C==null))
					{
						CMLib.commands().postSay(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
						return false;
					}
					if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANWITHDRAW)<0)
					{
						CMLib.commands().postSay(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
						return false;
					}
				}
				else
				if((numberDeposited(thename)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
				   ||(!msg.source().isMarriedToLiege())
				   ||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append("The Clan "+thename+" does not have an account with us, I'm afraid.");
					else
						str.append("You don't have an account with us, I'm afraid.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				double debt=CMLib.beanCounter().getDebtOwed(thename,bankChain());
				if(debt>0.0)
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append("The Clan "+thename+" already has a "+CMLib.beanCounter().nameCurrencyShort(this,debt)+" loan out with us.");
					else
						str.append("You already have a "+CMLib.beanCounter().nameCurrencyShort(this,debt)+" loan out with us.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				double collateralRemaining=((Coins)msg.tool()).getTotalValue()-totalItemsWorth(mob);
				if(collateralRemaining>0)
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append("The Clan "+thename+" ");
					else
						str.append("You ");
					str.append("will need to deposit enough items with us as collateral.  You'll need items worth "
							+CMLib.beanCounter().nameCurrencyShort(this,collateralRemaining)+" more to qualify.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				return true;
			}
			case CMMsg.TYP_LIST:
			{
                if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
                    return false;
				String thename=msg.source().Name();
				if(isSold(ShopKeeper.DEAL_CLANBANKER))
				{
					thename=msg.source().getClanID();
					Clan C=CMLib.clans().getClan(msg.source().getClanID());
					if((msg.source().getClanID().length()==0)
					  ||(C==null))
					{
						CMLib.commands().postSay(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
						return false;
					}
					if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANDEPOSITLIST)<0)
					{
						CMLib.commands().postSay(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
						return false;
					}
				}
				else
				if((numberDeposited(thename)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
				   ||(!msg.source().isMarriedToLiege())
				   ||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append("The Clan "+thename+" does not have an account with us, I'm afraid.");
					else
						str.append("You don't have an account with us, I'm afraid.");
					if(coinInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(coinInterest),100.0);
						String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"monthly on money deposited here.");
					}
					if(itemInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(itemInterest),100.0);
						String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"monthly on items kept with us.");
					}
					if(bankChain().length()>0)
						str.append("\n\rI am a banker for "+bankChain()+".");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
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
